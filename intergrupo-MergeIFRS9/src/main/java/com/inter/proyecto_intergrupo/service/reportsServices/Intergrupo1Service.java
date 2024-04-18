package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.TemplateBank;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV3;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.bank.IntergrupoV1FinalRepository;
import com.inter.proyecto_intergrupo.repository.bank.TaxBaseLoadRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class Intergrupo1Service {

    @Autowired
    EntityManager entityManager;

    @Autowired
    CountryRepository countryRepository;

    @Autowired
    YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    IntergrupoV1FinalRepository intergrupoV1FinalRepository;

    @Autowired
    UserService userService;

    @Autowired
    private AuditRepository auditRepository;


    public List<IntergrupoV1> getAllFromV1(String periodo) {

        intergroupUpdateNeocon(periodo);
        intergroupUpdateCountry(periodo);
        intergroupUpdateYNTP(periodo);
        intergroupUpdateThird(periodo);

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE periodo = ?", IntergrupoV1.class);
        result.setParameter(1, periodo);
        List<IntergrupoV1> data = result.getResultList();

        validateComponentsInter(periodo);

        return data;
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Versión 1");
        if(user!=null)
        {
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            insert.setCentro(user.getCentro());
        }
        else
        {
            insert.setNombre("SYSTEM JOB");
        }
        auditRepository.save(insert);
    }

    public List validateTableInterV1Aju(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 * FROM nexco_intergrupo_v1_final \n" +
                "WHERE periodo = ? ;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public void processAjuInterV1(String period){
        Query queryValidate1 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1_def \n" +
                "WHERE periodo = ? ;");
        queryValidate1.setParameter(1, period);
        queryValidate1.executeUpdate();

        Query result = entityManager.createNativeQuery("Insert into nexco_intergrupo_v1_def (yntp_empresa_reportante,cod_neocon,divisa,yntp,sociedad_yntp,contrato,\n" +
                "nit,valor,cod_pais,pais,cuenta_local,periodo,fuente,input,componente) SELECT base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,case when ajuste.valor is null then base.valor else (base.valor+ajuste.valor) end as valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM (SELECT cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante,sum(valor) as valor FROM nexco_intergrupo_v1 WHERE periodo = ? \n" +
                "group by cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante) AS base LEFT JOIN\n" +
                "(SELECT cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante,sum(valor) as valor FROM nexco_intergrupo_v1_final WHERE periodo = ? \n" +
                "group by cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante) AS ajuste ON  \n" +
                "base.cod_neocon=ajuste.cod_neocon and base.cod_pais=ajuste.cod_pais and\n" +
                "base.contrato=ajuste.contrato and base.cuenta_local=ajuste.cuenta_local and base.divisa=ajuste.divisa and\n" +
                "base.nit=ajuste.nit and\n" +
                "base.pais=ajuste.pais and base.periodo=ajuste.periodo and base.sociedad_yntp=ajuste.sociedad_yntp and\n" +
                "base.yntp=ajuste.yntp and base.yntp_empresa_reportante=ajuste.yntp_empresa_reportante ");
        result.setParameter(1, period);
        result.setParameter(2, period);
        result.executeUpdate();

        Query queryValidate2 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1_def \n" +
                "WHERE periodo = ? and valor=0;");
        queryValidate2.setParameter(1, period);
        queryValidate2.executeUpdate();

        Query queryValidate3 = entityManager.createNativeQuery("delete from nexco_intergrupo_v1_ndef where periodo = ? ;\n" +
                "insert into nexco_intergrupo_v1_ndef (id_reporte,cod_neocon,cod_pais,contrato,cuenta_local,divisa,fuente,nit,pais,periodo,sociedad_yntp,valor,yntp,yntp_empresa_reportante,componente,input)\n" +
                "(select a.id_reporte,a.cod_neocon,a.cod_pais,a.contrato,a.cuenta_local,a.divisa,a.fuente,a.nit,a.pais,a.periodo,a.sociedad_yntp,a.valor,a.yntp,a.yntp_empresa_reportante,a.componente,a.input \n" +
                "from (select * from nexco_intergrupo_v1_def where periodo = ? )a\n" +
                "inner join (select * from nexco_sociedades_yntp where id_metodo_ifrs = 'N') b on a.yntp = b.yntp);");
        queryValidate3.setParameter(1, period);
        queryValidate3.setParameter(2, period);
        queryValidate3.executeUpdate();

        Query queryValidate4 = entityManager.createNativeQuery("delete from nexco_intergrupo_v1_def where id_reporte IN\n" +
                "(select a.id_reporte from (select * from nexco_intergrupo_v1_def where periodo = ? )a\n" +
                "inner join (select * from nexco_sociedades_yntp where id_metodo_ifrs = 'N') b on a.yntp = b.yntp)");
        queryValidate4.setParameter(1, period);
        queryValidate4.executeUpdate();
    }

    public List<IntergrupoV1> getAllFromV1FinalAju(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT base.id_reporte,base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,base.valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM nexco_intergrupo_v1_def as base WHERE periodo = ?", IntergrupoV1.class);
        result.setParameter(1, periodo);
        List<IntergrupoV1> data = result.getResultList();

        return data;
    }

    public List<IntergrupoV1> getAllFromV1FinalAjuN(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT base.id_reporte,base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,base.valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM nexco_intergrupo_v1_ndef as base WHERE periodo = ?", IntergrupoV1.class);
        result.setParameter(1, periodo);
        List<IntergrupoV1> data = result.getResultList();

        return data;
    }

    public List<IntergrupoV1Final> getAllFromV1Final(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_final WHERE periodo = ?", IntergrupoV1Final.class);
        result.setParameter(1, periodo);
        List<IntergrupoV1Final> data = result.getResultList();

        validateComponentsInter(periodo);

        return data;
    }

    public List<Currency> getDivisas(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas as em ",Currency.class);

        return query.getResultList();
    }

    public List<Country> getPaises(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_paises as em order by em.nombre_pais ",Country.class);

        return query.getResultList();
    }

    public List<Country> getPais(String id){
        Query query = entityManager.createNativeQuery("SELECT top 1 em.* FROM nexco_paises as em where em.id_pais = ?",Country.class);
        query.setParameter(1, id);
        return query.getResultList();
    }

    public List<YntpSociety> getYntps(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_sociedades_yntp as em order by em.sociedad_corta",YntpSociety.class);

        return query.getResultList();
    }

    public List<IntergrupoV1> findIntergrupo1(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.id_reporte, em.cod_neocon, em.cod_pais, em.componente, em.contrato, \n" +
                "em.cuenta_local, em.divisa, em.fuente, em.input, em.nit, em.pais, em.periodo, em.sociedad_yntp, convert(numeric(20,2), em.valor) valor, em.yntp, em.yntp_empresa_reportante \n" +
                "FROM nexco_intergrupo_v1 as em \n" +
                "WHERE em.id_reporte = ?",IntergrupoV1.class);

        query.setParameter(1, id);
        return query.getResultList();
    }

    public List<IntergrupoV1> findIntergrupo1Aju(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.id_reporte, em.cod_neocon, em.cod_pais, em.componente, em.contrato, \n" +
                "em.cuenta_local, em.divisa, em.fuente, em.input, em.nit, em.pais, em.periodo, em.sociedad_yntp, convert(numeric(20,2), em.valor) valor, em.yntp, em.yntp_empresa_reportante \n" +
                "FROM nexco_intergrupo_v1_def as em \n" +
                "WHERE em.id_reporte = ?",IntergrupoV1.class);

        query.setParameter(1, id);
        return query.getResultList();
    }

    public boolean getFromTemplateBank(String periodo, String usuario) {

        User user = userService.findUserByUserName(usuario);

        boolean b = false;

        Query templateBank = entityManager.createNativeQuery("SELECT * FROM nexco_plantilla_carga as npc WHERE npc.periodo = ? AND centro = ? ", TemplateBank.class);
        templateBank.setParameter(1, periodo);
        templateBank.setParameter(2, user.getCentro());

        List<TemplateBank> result = templateBank.getResultList();

        if (!result.isEmpty()) {

            b = true;

            String fuente = "Plantilla Banco "+user.getCentro();

            Query deleteFirst = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1 WHERE periodo = ? " +
                    "AND fuente = ?");
            deleteFirst.setParameter(1, periodo);
            deleteFirst.setParameter(2,fuente);
            deleteFirst.executeUpdate();

            for (TemplateBank data : result) {

                IntergrupoV1 toInsert = new IntergrupoV1();
                if (data.getNeocon() != null)
                    toInsert.setCodNeocon(data.getNeocon());
                toInsert.setContrato(data.getCONTRATO());
                toInsert.setCodPais(data.getCOD_PAIS());
                toInsert.setDivisa(data.getDivisa());
                toInsert.setNit(data.getNit());
                toInsert.setCuentaLocal(data.getCuentaLocal());
                toInsert.setPeriodo(periodo);
                toInsert.setYntp(data.getYntp());
                toInsert.setSociedadYntp(data.getSociedad_yntp());
                toInsert.setValor(data.getValor());
                toInsert.setPais(data.getPAIS());
                toInsert.setYntpReportante(data.getYntpEmpresa());
                toInsert.setFuente("Plantilla Banco " + user.getCentro());

                addInfo(toInsert,null);
            }
            updateStateTemplate(user,periodo);
        }

        return b;
    }

    public void  updateStateTemplate(User user,String periodo)
    {
        Query templateBankUpdate = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ?,estado = 0, fecha_carga = ? where input = ? AND responsable = ? AND fecha_reporte = ?");
        templateBankUpdate.setParameter(1, "FULL");
        templateBankUpdate.setParameter(2, new Date());
        templateBankUpdate.setParameter(3, "PLANTILLA BANCO");
        templateBankUpdate.setParameter(4, user.getCentro());
        templateBankUpdate.setParameter(5, periodo);
        templateBankUpdate.executeUpdate();

        Query templateBank = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando where input = ? AND responsable = ? AND fecha_reporte = ? AND semaforo_input = ?");
        templateBank.setParameter(1, "PLANTILLA BANCO");
        templateBank.setParameter(2, user.getCentro());
        templateBank.setParameter(3, periodo);
        templateBank.setParameter(4, "EMPTY");

        if(templateBank.getResultList().size()==0)
        {
            Query templateBankUpdateC = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = ?, fecha_carga = ? where input = ? AND responsable = ? AND fecha_reporte = ?");
            templateBankUpdateC.setParameter(1, "FULL");
            templateBankUpdateC.setParameter(2, new Date());
            templateBankUpdateC.setParameter(3, "PLANTILLA BANCO");
            templateBankUpdateC.setParameter(4, user.getCentro());
            templateBankUpdateC.setParameter(5, periodo);
            templateBankUpdateC.executeUpdate();
        }
    }

    public boolean validateComponentTemplate(User user, String periodo)
    {
        Query templateBank = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando where input = ? AND responsable = ? AND fecha_reporte = ? AND estado = 1");
        templateBank.setParameter(1, "PLANTILLA BANCO");
        templateBank.setParameter(2, user.getCentro());
        templateBank.setParameter(3, periodo);

        if(templateBank.getResultList().size()>0)
        {
            return false;
        }
        else{
            return true;
        }
    }

    public boolean getFromTemplateBankPreCharge(List<String[]> preCharge,String periodo, String usuario) {

        User user = userService.findUserByUserName(usuario);

        boolean b = false;

        if (!preCharge.isEmpty()) {

            b = true;

            String fuente = "Plantilla Banco "+user.getCentro();

            Query deleteFirst = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1 WHERE periodo = ? " +
                    "AND fuente = ?");
            deleteFirst.setParameter(1, periodo);
            deleteFirst.setParameter(2,fuente);
            deleteFirst.executeUpdate();

            for (Object [] data : preCharge) {

                IntergrupoV1 toInsert = new IntergrupoV1();
                if (!Objects.equals(data[1], ""))
                    toInsert.setCodNeocon(data[1].toString());
                toInsert.setContrato(data[5].toString());
                toInsert.setCodPais(data[8].toString());
                toInsert.setDivisa(data[2].toString());
                toInsert.setNit(data[6].toString());
                toInsert.setCuentaLocal(data[10].toString());
                toInsert.setPeriodo(periodo);
                toInsert.setYntp(data[3].toString());
                toInsert.setSociedadYntp(data[4].toString());
                toInsert.setValor(Double.parseDouble(data[7].toString()));
                toInsert.setPais(data[9].toString());
                toInsert.setYntpReportante(data[0].toString());
                toInsert.setFuente("Plantilla Banco " + user.getCentro());

                addInfo(toInsert,null);
            }

            updateStateTemplate(user,periodo);
        }

        return b;
    }


    public boolean getFromComer(String periodo) {

        boolean b = false;

        ArrayList<String> months = new ArrayList<>();

        int month = Integer.parseInt(periodo.substring(5,7)) < 10 ? Integer.parseInt(periodo.substring(5,7).replace("0","")) :  Integer.parseInt(periodo.substring(5,7));
        String year = periodo.substring(0,4);
        for(int i = month; i>0 ; i--){
            String m;
            if(i<10){
                m = year + "-0"+ i;
            } else {
                m = year +"-"+ i;
            }
            months.add(m);
        }

        Query comer = entityManager.createNativeQuery("IF EXISTS (SELECT * FROM nexco_precarga_comer WHERE periodo = '"+periodo+"') \n" +
                "SELECT yntp_empresa_reportante, cod_neocon, divisa, yntp, sociedad_yntp, nit, SUM(valor) as valor, cod_pais, pais, cuenta_local FROM nexco_precarga_comer \n" +
                "WHERE periodo IN (:months) \n" +
                "GROUP BY cod_neocon, cod_pais, cuenta_local, divisa, nit, observaciones, pais, sociedad_yntp, yntp, yntp_empresa_reportante \n"+
                "ELSE \n"+
                "SELECT * FROM nexco_precarga_comer WHERE periodo = '"+periodo+"'");
        comer.setParameter("months", months);

        List<Object[]> result = comer.getResultList();

        if (!result.isEmpty()) {
            b=true;

            Query delete = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1 WHERE periodo = ? AND fuente = ?");
            delete.setParameter(1,periodo);
            delete.setParameter(2,"FUERZA DE VENTAS");
            delete.executeUpdate();

            for (Object[] data : result) {
                IntergrupoV1 toInsert = new IntergrupoV1();
                toInsert.setYntpReportante(data[0].toString());
                toInsert.setCodNeocon(data[1].toString());
                toInsert.setDivisa(data[2].toString());
                toInsert.setYntp(data[3].toString());
                toInsert.setSociedadYntp(data[4].toString());
                toInsert.setNit(data[5].toString());
                toInsert.setValor(Double.parseDouble(data[6].toString()));
                toInsert.setCodPais(data[7].toString());
                toInsert.setPais(data[8].toString());
                toInsert.setCuentaLocal(data[9].toString());
                toInsert.setFuente("FUERZA DE VENTAS");
                toInsert.setPeriodo(periodo);

                addInfo(toInsert,null);
            }
        }

        return b;
    }

    public void getFromRP21(String periodo) {


        Query deleteRP21 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v1 WHERE periodo = ? AND fuente = ?");
        deleteRP21.setParameter(1, periodo);
        deleteRP21.setParameter(2, "RP21");
        deleteRP21.executeUpdate();

        Query rp21 = entityManager.createNativeQuery("SELECT *" +
                " FROM nexco_reporte_rp21 as em WHERE CONVERT(varchar(7),fecont,120) = ? AND intergrupo = 'Si'", Rp21.class);
        rp21.setParameter(1, periodo);

        List<Rp21> result = rp21.getResultList();

        if (!result.isEmpty()) {

            for (Rp21 data : result) {
                IntergrupoV1 toInsert = new IntergrupoV1();

                if (!data.getYintp().equals("no")) {
                    YntpSociety yntp = yntpSocietyRepository.findByYntp(data.getYintp());
                    toInsert.setSociedadYntp(yntp.getSociedadDescripcionCorta());
                }

                toInsert.setCodNeocon(data.getNeocon2());
                toInsert.setContrato(data.getOperacion());
                toInsert.setCodPais(data.getPais());
                toInsert.setDivisa(data.getDivisa());
                toInsert.setNit(data.getNit());
                toInsert.setCuentaLocal(data.getLocalDerec());
                toInsert.setPeriodo(periodo);
                toInsert.setYntp(data.getYintp());
                toInsert.setValor(data.getMtmCOP());
                toInsert.setPais(data.getPais());
                toInsert.setFuente("RP21");

                addInfo(toInsert,null);
            }
        }

    }

    public void addInfo(IntergrupoV1 inter, User user) {
        String empresaFinal = inter.getYntpReportante();
        if(user!=null)
            empresaFinal = user.getEmpresa();
        Query insert = entityManager.createNativeQuery("INSERT INTO nexco_intergrupo_v1 (yntp_empresa_reportante,yntp,periodo,nit,divisa,cuenta_local,contrato,cod_neocon,cod_pais,pais,sociedad_yntp,valor,fuente) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");
        insert.setParameter(1, empresaFinal);
        insert.setParameter(2, inter.getYntp());
        insert.setParameter(3, inter.getPeriodo());
        insert.setParameter(4, inter.getNit());
        insert.setParameter(5, inter.getDivisa());
        insert.setParameter(6, inter.getCuentaLocal());
        insert.setParameter(7, inter.getContrato());
        insert.setParameter(8, inter.getCodNeocon());
        insert.setParameter(9, inter.getCodPais());
        insert.setParameter(10, inter.getPais());
        insert.setParameter(11, inter.getSociedadYntp());
        insert.setParameter(12, inter.getValor());
        insert.setParameter(13, inter.getFuente());

        insert.executeUpdate();
    }


    public void validateComponentsInter(String periodo) {
        Query query = entityManager.createNativeQuery("UPDATE ni\n" +
                "SET ni.input = ncr.input,\n" +
                "ni.componente =ncr.componente \n" +
                "FROM nexco_intergrupo_v1 AS ni\n" +
                "LEFT JOIN (SELECT * FROM nexco_cuentas_responsables) AS ncr\n" +
                "ON ni.cuenta_local LIKE CONVERT(varchar,ncr.cuenta_local) + '%' WHERE \n" +
                //"ON SUBSTRING(CONVERT(varchar,ncr.cuenta_local),1,4) = SUBSTRING(ni.cuenta_local,1,4) WHERE \n" +
                "ni.periodo= ?  \n" +
                "AND (LEN(ncr.cuenta_local) = 4 OR LEN(ncr.cuenta_local) = 6 OR ni.cuenta_local = CAST(ncr.cuenta_local AS varchar))  \n" +
                "AND ni.componente is null AND ni.input is null");
        query.setParameter(1, periodo);
        query.executeUpdate();
    }


    public void intergroupUpdateNeocon(String periodo){

        Query result = entityManager.createNativeQuery("update a\n" +
                "set a.cod_neocon = b.CODICONS46\n" +
                "from nexco_intergrupo_v1 a\n" +
                "inner join (select nucta, CODICONS46 from CUENTAS_PUC where EMPRESA = '0013') b\n" +
                "on a.cuenta_local = b.nucta\n" +
                "where a.periodo = ?\n" +
                ";");
        result.setParameter(1,periodo);
        result.executeUpdate();

    }

    public void intergroupUpdateCountry(String periodo){

        Query result = entityManager.createNativeQuery("update a \n" +
                "set a.cod_pais = b.id_pais, a.pais = upper(trim(a.pais)) \n" +
                "from nexco_intergrupo_v1 a \n" +
                "inner join nexco_paises b \n" +
                "on upper(trim(a.pais)) = b.nombre_pais \n" +
                "where a.periodo = ?\n" +
                ";");
        result.setParameter(1,periodo);
        result.executeUpdate();

    }

    public void intergroupUpdateYNTP(String periodo){

        Query result = entityManager.createNativeQuery("update a\n" +
                "set a.sociedad_yntp = b.sociedad_corta\n" +
                "from nexco_intergrupo_v1 a\n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where a.periodo = ?\n" +
                ";");
        result.setParameter(1,periodo);
        result.executeUpdate();

    }

    public void intergroupUpdateThird(String periodo){

        Query result = entityManager.createNativeQuery("update a\n" +
                "set a.nit = case when b.nit_contraparte is null then a.nit else CONVERT(VARCHAR, b.tipo)+b.nit_contraparte+CONVERT(VARCHAR, b.dv) end\n" +
                "from nexco_intergrupo_v1 a\n" +
                "left join nexco_terceros b\n" +
                "on a.nit = b.nit_contraparte\n" +
                "where a.periodo = ?\n" +
                ";");
        result.setParameter(1,periodo);
        result.executeUpdate();

    }


    public List<IntergrupoV1> findByFilter(String value, String filter, String period) {

        ArrayList<IntergrupoV1> toReturn;

        switch (filter) {

            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE cuenta_local LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query.setParameter(1, value);
                query.setParameter(2, period);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query.getResultList();
                }
                break;

            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE contrato LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query1.getResultList();
                }
                break;

            case "Nit":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE nit LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query2.getResultList();
                }
                break;

            case "Cod Neocon":
                Query query3 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE cod_neocon LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query3.setParameter(1, value);
                query3.setParameter(2, period);
                if (query3.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query3.getResultList();
                }
                break;

            case "Divisa":
                Query query4 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE divisa LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period);
                if (query4.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query4.getResultList();
                }
                break;

            case "YNTP":
                Query query5 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE yntp LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query5.setParameter(1, value);
                query5.setParameter(2, period);
                if (query5.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query5.getResultList();
                }
                break;

            case "Sociedad":
                Query query6 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE sociedad_yntp LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query6.setParameter(1, value);
                query6.setParameter(2, period);
                if (query6.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query6.getResultList();
                }
                break;

            case "Cod Pais":
                Query query7 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE cod_pais LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query7.setParameter(1, value);
                query7.setParameter(2, period);
                if (query7.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query7.getResultList();
                }
                break;

            case "Pais":
                Query query8 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1 WHERE pais LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query8.setParameter(1, value);
                query8.setParameter(2, period);
                if (query8.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query8.getResultList();
                }
                break;

            default:
                 toReturn = new ArrayList<>();
        }


        return toReturn;
    }

    public List<IntergrupoV1> findByFilterDef(String value, String filter, String period) {

        ArrayList<IntergrupoV1> toReturn;

        switch (filter) {

            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE cuenta_local LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query.setParameter(1, value);
                query.setParameter(2, period);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query.getResultList();
                }
                break;

            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE contrato LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query1.getResultList();
                }
                break;

            case "Nit":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE nit LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query2.getResultList();
                }
                break;

            case "Cod Neocon":
                Query query3 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE cod_neocon LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query3.setParameter(1, value);
                query3.setParameter(2, period);
                if (query3.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query3.getResultList();
                }
                break;

            case "Divisa":
                Query query4 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE divisa LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period);
                if (query4.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query4.getResultList();
                }
                break;

            case "YNTP":
                Query query5 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE yntp LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query5.setParameter(1, value);
                query5.setParameter(2, period);
                if (query5.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query5.getResultList();
                }
                break;

            case "Sociedad":
                Query query6 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE sociedad_yntp LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query6.setParameter(1, value);
                query6.setParameter(2, period);
                if (query6.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query6.getResultList();
                }
                break;

            case "Cod Pais":
                Query query7 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE cod_pais LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query7.setParameter(1, value);
                query7.setParameter(2, period);
                if (query7.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query7.getResultList();
                }
                break;

            case "Pais":
                Query query8 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v1_def WHERE pais LIKE ? and periodo LIKE ?", IntergrupoV1.class);
                query8.setParameter(1, value);
                query8.setParameter(2, period);
                if (query8.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV1>) query8.getResultList();
                }
                break;

            default:
                toReturn = new ArrayList<>();
        }


        return toReturn;
    }

    public List<Object[]> getCuenta(String cuenta){
        Query query = entityManager.createNativeQuery("select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013' and nucta = ?");
        query.setParameter(1, cuenta);
        return query.getResultList();
    }

    public List<Object[]> getCodiCons(String codicons){
        Query query = entityManager.createNativeQuery("select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013' and CODICONS46 = ?");
        query.setParameter(1, codicons);
        return query.getResultList();
    }

    public void modifyIntergrupoV1(IntergrupoV1 toModify,Integer id){
        IntergrupoV1 toInsert = new IntergrupoV1();
        toInsert.setCodNeocon(toModify.getCodNeocon());
        toInsert.setCodPais(toModify.getCodPais());
        toInsert.setContrato(toModify.getContrato());
        toInsert.setCuentaLocal(toModify.getCuentaLocal());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setNit(toModify.getNit());
        toInsert.setYntp(toModify.getYntp());
        toInsert.setValor(toModify.getValor());

        Country country = countryRepository.findAllById(toInsert.getCodPais());
        YntpSociety yntp = yntpSocietyRepository.findByYntp(toInsert.getYntp());

        Query query = entityManager.createNativeQuery("UPDATE nexco_intergrupo_v1 SET cod_neocon = ? , cod_pais = ? , contrato = ? , cuenta_local = ? , divisa = ? , nit = ? ,\n" +
                "yntp = ? , valor = ? , pais = ? , sociedad_yntp = ? \n" +
                "WHERE id_reporte = ? ", IntergrupoV1.class);
        query.setParameter(1, toInsert.getCodNeocon());
        query.setParameter(2, toInsert.getCodPais());
        query.setParameter(3, toInsert.getContrato());
        query.setParameter(4, toInsert.getCuentaLocal());
        query.setParameter(5, toInsert.getDivisa());
        query.setParameter(6, toInsert.getNit());
        query.setParameter(7, toInsert.getYntp());
        query.setParameter(8, toInsert.getValor());
        query.setParameter(9, country.getNombre());
        query.setParameter(10, yntp.getSociedadDescripcionCorta());
        query.setParameter(11, id);

        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void modifyIntergrupoV1Aju(IntergrupoV1 toModify,Integer id){
        IntergrupoV1 toInsert = new IntergrupoV1();
        toInsert.setCodNeocon(toModify.getCodNeocon());
        toInsert.setCodPais(toModify.getCodPais());
        toInsert.setContrato(toModify.getContrato());
        toInsert.setCuentaLocal(toModify.getCuentaLocal());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setNit(toModify.getNit());
        toInsert.setYntp(toModify.getYntp());
        toInsert.setValor(toModify.getValor());

        Country country = countryRepository.findAllById(toInsert.getCodPais());
        YntpSociety yntp = yntpSocietyRepository.findByYntp(toInsert.getYntp());

        Query query = entityManager.createNativeQuery("UPDATE nexco_intergrupo_v1_def SET cod_neocon = ? , cod_pais = ? , contrato = ? , cuenta_local = ? , divisa = ? , nit = ? ,\n" +
                "yntp = ? , valor = ? , pais = ? , sociedad_yntp = ? \n" +
                "WHERE id_reporte = ? ", IntergrupoV1.class);
        query.setParameter(1, toInsert.getCodNeocon());
        query.setParameter(2, toInsert.getCodPais());
        query.setParameter(3, toInsert.getContrato());
        query.setParameter(4, toInsert.getCuentaLocal());
        query.setParameter(5, toInsert.getDivisa());
        query.setParameter(6, toInsert.getNit());
        query.setParameter(7, toInsert.getYntp());
        query.setParameter(8, toInsert.getValor());
        query.setParameter(9, country.getNombre());
        query.setParameter(10, yntp.getSociedadDescripcionCorta());
        query.setParameter(11, id);

        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public ArrayList<String[]> saveFileFinalIntergrupo(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantillaFinal(rows, user, period);
            }
            catch (Exception e) {
                String[] error = new String[3];
                error[0] = "0";
                error[1] = "1";
                error[2] = "FAIL";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaFinal(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<IntergrupoV1Final> interList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;

        Query deleteAp1 = entityManager.createNativeQuery("delete from nexco_intergrupo_v1_final where periodo = ?;");
        deleteAp1.setParameter(1,period);
        deleteAp1.executeUpdate();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellYNTPEmpresaReportante = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodNeocon = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim();
                String cellYntp = formatter.formatCellValue(row.getCell(3)).trim();
                String cellSociedadYNTP = formatter.formatCellValue(row.getCell(4)).trim();
                String cellContrato = formatter.formatCellValue(row.getCell(5)).trim();
                String cellNITContraparte = formatter.formatCellValue(row.getCell(6)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(7)).trim();
                String cellCodPaís = formatter.formatCellValue(row.getCell(8)).trim();
                String cellPaís = formatter.formatCellValue(row.getCell(9)).trim();
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(10)).trim();
                String cellPeriodo = formatter.formatCellValue(row.getCell(11)).trim();
                String cellFuente = formatter.formatCellValue(row.getCell(12)).trim();
                String cellInput = formatter.formatCellValue(row.getCell(13)).trim();
                String cellComponente = formatter.formatCellValue(row.getCell(14)).trim();

                if (cellYNTPEmpresaReportante.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El YNTP Empresa Reportante debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellCodNeocon.trim().length() !=5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El CodNeocon debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellDivisa.trim().length() !=3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Divisa debe estar diligenciado a 3 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellYntp.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El YNTP debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellSociedadYNTP.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "La Sociedad YNTP no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellContrato.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Contrato no puede estar vacío.";
                    lista.add(log1);
                }*/
                if (cellNITContraparte.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El NIT Contraparte no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCodPaís.trim().length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El Cod País debe estar diligenciado a 2 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellPaís.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(9);
                    log1[2] = "El País no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCuentaLocal.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "La Cuenta Local no puede estar vacía.";
                    lista.add(log1);
                }
                if (!cellPeriodo.trim().equals(period)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El periodo reportado no es el mismo al cual se desea cargar.";
                    lista.add(log1);
                }
                if (cellFuente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El Fuente reportada no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellInput.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El Input reportado no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellComponente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El Componente reportado no puede estar vacío.";
                    lista.add(log1);
                }*/

                Double valapp;
                try{
                    valapp = !cellValor.isEmpty()?Double.parseDouble(cellValor.replace(" ","").replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El Valor debe ser informado númerico";
                    lista.add(log1);
                    valapp = .0;
                }

                IntergrupoV1Final temporalInter = new IntergrupoV1Final();
                temporalInter.setYntpReportante(cellYNTPEmpresaReportante);
                temporalInter.setCodNeocon(cellCodNeocon);
                temporalInter.setDivisa(cellDivisa);
                temporalInter.setYntp(cellYntp);
                temporalInter.setSociedadYntp(cellSociedadYNTP);
                temporalInter.setContrato(cellContrato);
                temporalInter.setNit(cellNITContraparte);
                temporalInter.setValor(valapp);
                temporalInter.setCodPais(cellCodPaís);
                temporalInter.setPais(cellPaís);
                temporalInter.setCuentaLocal(cellCuentaLocal);
                temporalInter.setPeriodo(period);
                temporalInter.setFuente(cellFuente);
                temporalInter.setInput(cellInput);
                temporalInter.setComponente(cellComponente);
                interList.add(temporalInter);

            } else {
                firstRow = 0;
            }
        }
        String[] log = new String[3];
        log[0] = String.valueOf(interList.size()-lista.size());
        log[1] = String.valueOf(lista.size());
        if(lista.isEmpty())
        {
            intergrupoV1FinalRepository.saveAll(interList);
            log[2] = "COMPLETE";
        }
        else
        {
            log[2] = "FAILED";
        }
        lista.add(log);

        return lista;
    }

}
