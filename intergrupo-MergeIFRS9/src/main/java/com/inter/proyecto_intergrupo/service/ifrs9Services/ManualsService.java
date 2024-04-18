package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Manuals;
import com.inter.proyecto_intergrupo.model.parametric.ChangeCurrency;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class ManualsService {

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    public ManualsService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<Manuals> findAll(){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_manuales_anexo", Manuals.class);
        return query.getResultList();
    }

    public List<Manuals> findAllByPeriod(String period){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_manuales_anexo WHERE periodo = ?", Manuals.class);
        query.setParameter(1,period);
        return query.getResultList();
    }

    public List<Object[]> findAllByPeriodResume(String period){
        Query query = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo WHERE periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
        query.setParameter(1,period);
        return query.getResultList();
    }

    public boolean loadData(String period)
    {
        boolean answer = true;
        try{
            deleteManualsDate(period);
            String[] dateParts =period.split("-");
            String datePart =period.replace("-","");

            Query query = entityManager.createNativeQuery("INSERT INTO nexco_manuales_anexo (centro,descripcion_centro,cuenta_puc,descripcion_cuenta_puc,divisa,importe,fecha_origen,fecha_cierre,tp,identificacion,dv,nombre,contrato,observacion,cuenta_prov,importe_prov,importe_original,probabilidad_recuperacion,altura,fuente_informacion,periodo,descripcion_provisiones) \n" +
                    "SELECT RIGHT('0000' + Ltrim(Rtrim(TA.CENTRO)),4),D.NOMBRE_UNIDAD,REPLACE(TA.NUCTA,' ',''),TA.DERECTA,TA.DIVISA,TA.IMPORTE,TA.FECHA_ORIGEN,TA.FECHA_CIERRE,TA.TP,TA.IDENTIFICACION,\n" +
                    "TA.DV,TA.NOMBRE,TA.CONTRATO,TA.OBSERVACION,TA.CUENTA_PROV,TA.IMPORTE_PROV,TA.IMPORTE_ORIGINAL,TA.PROBABILIDAD_RECUPERACION,DATEDIFF (DAY, FORMAT(CONVERT (date,TA.FECHA_ORIGEN,105),'yyyy/MM/dd'),FORMAT(CONVERT (date, (SELECT max(FechaHabil) FROM FECHAS_HABILES WHERE YEAR(FechaHabil) = ? AND MONTH(FechaHabil) = ?)),'yyyy/MM/dd') ),\n" +
                    "CASE WHEN SUBSTRING(TA.CUENTA,1,2) = '13' THEN 'RENTA FIJA' ELSE 'MANUALES' END, ? , TA.descripcion\n" +
                    "FROM (SELECT * FROM  (SELECT NUCTA,EMPRESA,HATAGIOB,CODICONS46,DERECTA FROM CUENTAS_PUC WHERE EMPRESA = ? AND HATAGIOB = 'D') C\n" +
                    "INNER JOIN (SELECT * FROM nexco_provisiones WHERE ifrs9 = 'CV' ) B ON C.CODICONS46 = B.cuenta_neocon\n" +
                    "INNER JOIN (SELECT * FROM Cargas_Anexos_SICC_"+datePart+" WHERE IMPORTE != 0) A ON A.CUENTA = C.NUCTA) TA \n" +
                    "LEFT JOIN OFI_GRUPO D ON TA.CENTRO = D.OFICINA \n" +
                    "WHERE TA.CUENTA NOT IN (SELECT CUENTA FROM nexco_parametrica_genericas)");

            query.setParameter(1,dateParts[0]);
            query.setParameter(2,dateParts[1]);
            query.setParameter(3,period);
            query.setParameter(4,"0013");
            query.executeUpdate();

            Date today = new Date();
            String input = "MANUALS";

            StatusInfo validateStatus = statusInfoRepositoryL.findByInputAndPeriodo(input, period);

            if (validateStatus == null) {
                StatusInfo status = new StatusInfo();
                status.setInput(input);
                status.setPeriodo(period);
                status.setFecha(today);
                statusInfoRepositoryL.save(status);
            } else {
                validateStatus.setFecha(today);
                statusInfoRepositoryL.save(validateStatus);
            }
        }
        catch(NoResultException e)
        {
            e.printStackTrace();
            return false;
        }
        return answer;

    }

    public boolean countTable(String period, String id){
        Query query = entityManager.createNativeQuery("SELECT count(*) FROM nexco_manuales_anexo WHERE periodo = ? ");
        query.setParameter(1,period);
        List<String> list= query.getResultList();
        if(Long.parseLong(list.get(0))<850000)
            return true;
        else
            return false;
    }

    public List<Manuals> findManualsByCCC(String period, String id){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_manuales_anexo WHERE periodo = ? AND id_manuales = ? ", Manuals.class);
        query.setParameter(1,period);
        query.setParameter(2,id);
        return query.getResultList();
    }

    public List<Object[]> getManualsNoApply(String period){
        String[] dateParts =period.split("-");
        String datePart =period.replace("-","");
        String parametricsString ="";
        String adParametrics ="";

        Query verify = entityManager.createNativeQuery("SELECT TOP 10 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,period+"%");

        if(verify.getResultList().isEmpty()){
            parametricsString = "nexco_query";
        }
        else
        {
            parametricsString = "nexco_query_marcados";
            adParametrics = " AND origen = 'LOCAL' ";
        }

        Query query = entityManager.createNativeQuery("SELECT A.CUENTAA, ISNULL(B.valor,0)  FROM (SELECT REPLACE(CP.NUCTA,' ','') AS CUENTAA FROM CUENTAS_PUC CP, nexco_provisiones NP WHERE CP.EMPRESA = ? AND CP.HATAGIOB = 'D' AND NP.ifrs9 = 'CV' \n" +
                "AND CP.CODICONS46 = NP.cuenta_neocon AND CP.NUCTA NOT IN (SELECT CUENTA FROM nexco_parametrica_genericas) \n" +
                "AND CP.NUCTA NOT IN ((SELECT CA.CUENTA FROM Cargas_Anexos_SICC_"+datePart+" CA WHERE IMPORTE != 0))\n" +
                "GROUP BY CP.NUCTA) A\n" +
                "LEFT JOIN (SELECT nucta, SUM(saldoquery) AS valor FROM "+parametricsString+" as query WHERE SUBSTRING(fecont,0,8) = ? AND empresa = ? "+adParametrics+" GROUP BY nucta) B\n" +
                "ON B.nucta = A.CUENTAA");


        query.setParameter(1,"0013");
        query.setParameter(2,period);
        query.setParameter(3,"0013");
        return query.getResultList();
    }

    public void modifyManuals(Manuals toModify,String id,String periodo,User user){
        Manuals toInsert = new Manuals();
        deleteManuals(id, periodo, user);

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro tabla Manuales");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("MANUALES (ANEXO8-SICC)");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        saveChangeCurrency(toInsert);
    }

    public void saveChangeCurrency(Manuals toInsert){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_manuales_anexo (periodo, centro,descripcion_centro,cuenta_puc,descripcion_cuenta_puc,divisa,importe,fecha_origen,fecha_cierre,tp,identificacion,dv,nombre,contrato,observacion,cuenta_prov,importe_prov,importe_original,probabilidad_recuperacion,altura,fuente_informacion) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Manuals.class);
        query.setParameter(1,toInsert.getPeriodo());
        query.setParameter(2,toInsert.getCentro());
        query.setParameter(3,toInsert.getDescripcionCentro());
        query.setParameter(4,toInsert.getCuentaPuc());
        query.setParameter(5,toInsert.getDescripcionCuentaPuc());
        query.setParameter(6,toInsert.getDivisa());
        query.setParameter(7,toInsert.getImporte());
        query.setParameter(8,toInsert.getFechaOrigen());
        query.setParameter(9,toInsert.getFechaCierre());
        query.setParameter(10,toInsert.getTp());
        query.setParameter(11,toInsert.getIdentificacion());
        query.setParameter(12,toInsert.getDv());
        query.setParameter(13,toInsert.getNombre());
        query.setParameter(14,toInsert.getContrato());
        query.setParameter(15,toInsert.getObservacion());
        query.setParameter(16,toInsert.getCuentaProv());
        query.setParameter(17,toInsert.getImporteProv());
        query.setParameter(18,toInsert.getImporteOriginal());
        query.setParameter(19,toInsert.getProbabilidadRecuperacion());
        query.setParameter(20,toInsert.getAltura());
        query.setParameter(21,toInsert.getFuenteInformacion());
        query.executeUpdate();
    }

    public void deleteManuals(String id,String periodo,User user)
    {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_manuales_anexo WHERE periodo = ? AND id_manuales = ?", Manuals.class);
        query.setParameter(1,periodo);
        query.setParameter(2,id);
        query.executeUpdate();
    }

    public void deleteManualsDate(String fecont){
        Query deleteSicc = entityManager.createNativeQuery("DELETE FROM nexco_manuales_anexo WHERE periodo = ? ");
        deleteSicc.setParameter(1, fecont);
        deleteSicc.executeUpdate();
    }

    public void removeManuals(String id,String periodo,User user){

        deleteManuals(id, periodo, user);
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla Manuales");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("MANUALES (ANEXO8-SICC)");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<Object[]> validateLoad(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT * FROM nexco_manuales_anexo WHERE periodo LIKE ? ");
        queryValidate.setParameter(1, period);
        return queryValidate.getResultList();
    }

    public boolean insertManuals(Manuals toInsert){

        boolean state = false;
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_manuales_anexo WHERE periodo = ?", Manuals.class);
        query.setParameter(1,toInsert.getPeriodo());

        if(query.getResultList().isEmpty()){
            try {
                saveChangeCurrency(toInsert);
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return state;
    }

    public void clearManuals(String period, User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_manuales_anexo WHERE periodo = ?", Manuals.class);
        query.setParameter(1,period);
        query.executeUpdate();

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Manuales");
        insert.setCentro(user.getCentro());
        insert.setComponente("PARAMÉTRICAS");
        insert.setFecha(today);
        insert.setInput("MANUALES (ANEXO8-SICC)");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<Object[]> findByFilter(String value, String filter,String period) {
        List<Object[]> list=new ArrayList<Object[]>();
        switch (filter)
        {
            case "Centro":
                Query query = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE centro LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query.setParameter(1, value);
                query.setParameter(2, period);
                list= query.getResultList();
                break;
            case "Cuenta PUC":
                Query query0 = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE em.cuenta_puc LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query0.setParameter(1, value);
                query0.setParameter(2, period);
                list= query0.getResultList();
                break;
            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE em.contrato LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                list= query1.getResultList();
                break;
            case "Divisa":
                Query query2 = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE em.divisa LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                list= query2.getResultList();
                break;
            case "Importe":
                Query query3 = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE em.importe LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query3.setParameter(1, value);
                query3.setParameter(2, period);
                list= query3.getResultList();
                break;
            case "Fecha Origen":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_manuales_anexo as em " +
                        "WHERE em.fecha_origen LIKE ? AND periodo = ?", Manuals.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period);
                list= query4.getResultList();
                break;
            case "Fecha Cierre":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_manuales_anexo as em " +
                        "WHERE em.fecha_cierre LIKE ? AND periodo = ?", Manuals.class);
                query5.setParameter(1, value);
                query5.setParameter(2, period);
                list= query5.getResultList();
                break;
            case "TP":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_manuales_anexo as em " +
                        "WHERE em.tp LIKE ? AND periodo = ?", Manuals.class);
                query6.setParameter(1, value);
                query6.setParameter(2, period);
                list= query6.getResultList();
                break;
            case "Identificación":
                Query query7 = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE em.identificacion LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query7.setParameter(1, value);
                query7.setParameter(2, period);
                list= query7.getResultList();
                break;
            case "DV":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_manuales_anexo as em " +
                        "WHERE em.dv LIKE ? AND periodo = ?", Manuals.class);
                query8.setParameter(1, value);
                query8.setParameter(2, period);
                list= query8.getResultList();
                break;
            case "Nombre":
                Query query9 = entityManager.createNativeQuery("SELECT em.* FROM nexco_manuales_anexo as em " +
                        "WHERE em.nombre LIKE ? AND periodo = ?", Manuals.class);
                query9.setParameter(1, value);
                query9.setParameter(2, period);
                list= query9.getResultList();
                break;
            case "Altura":
                Query query10 = entityManager.createNativeQuery("SELECT em.* FROM nexco_manuales_anexo as em " +
                        "WHERE em.altura LIKE ? AND periodo = ?", Manuals.class);
                query10.setParameter(1, value);
                query10.setParameter(2, period);
                list= query10.getResultList();
                break;
            case "Fuente Información":
                Query query11 = entityManager.createNativeQuery("SELECT centro,cuenta_puc,divisa,SUM(importe),fuente_informacion FROM nexco_manuales_anexo as em " +
                        "WHERE em.fuente_informacion LIKE ? AND periodo = ? GROUP BY centro,cuenta_puc,divisa,fuente_informacion");
                query11.setParameter(1, value);
                query11.setParameter(2, period);
                list= query11.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

    public List<Object[]> validateQuery(String empresa, String period)
    {
        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,period+"%");

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
            tempValue ="";
        }
        else
        {
            nameTable ="nexco_query_marcados";
            tempValue ="origen = 'LOCAL' AND";
        }
        Query query= entityManager.createNativeQuery("SELECT b.cuenta_contable,d.CODICONS46, ISNULL(ISNULL(b.id_divisa, C.coddiv),'-') divisa, ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)) saldo_pesos,\n" +
                "ABS(CAST(ISNULL(c.salmes, 0) AS FLOAT)) salmes, \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT) diferencia_pesos,c.fechproce \n" +
                "FROM (SELECT REPLACE(cuenta_puc,' ','') cuenta_contable, CASE WHEN divisa = 'COD' THEN 'COP' ELSE divisa END id_divisa,sum(importe) saldo_pesos\n" +
                "FROM nexco_manuales_anexo \n" +
                "WHERE cuenta_puc != '' and periodo = ? \n" +
                "group by REPLACE(cuenta_puc,' ',''), divisa) b \n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(salmesd) salmesd,sum(salmes) salmes\n" +
                "FROM "+nameTable+" \n" +
                "WHERE "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = ?\n" +
                "group by fecont,fechproce, nucta, coddiv) c\n" +
                "on b.cuenta_contable = REPLACE(c.nucta,' ','') AND b.id_divisa = c.coddiv \n" +
                "LEFT JOIN (select CODICONS46,NUCTA from CUENTAS_PUC where empresa = ?) d\n" +
                "on b.cuenta_contable = d.NUCTA ");

        query.setParameter(1,period);
        query.setParameter(2,period);
        query.setParameter(3,empresa);
        query.setParameter(4,empresa);
        return query.getResultList();

    }
}
