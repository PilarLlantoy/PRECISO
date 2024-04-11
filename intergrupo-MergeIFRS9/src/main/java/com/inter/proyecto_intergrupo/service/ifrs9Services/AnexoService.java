package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class AnexoService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private ControlPanelJobsRepository controlPanelJobsRepository;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    @Autowired
    private ControlPanelJobsService controlPanelJobsService;


    public ArrayList getActualAnexos() throws ParseException {
        ArrayList result = new ArrayList();

        DateFormat formatter = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        Date date = formatter.parse(String.valueOf(calendar.get(Calendar.YEAR))+String.valueOf(calendar.get(Calendar.MONTH)));

        String fecont = formatter.format(date);


        result.add(0,getAnexo(fecont));

        result.add(1,fecont);

        return result;
    }


    public boolean saveFromAnexoDatabase(String fecont){
        String fecont2=fecont.replace("-", "");
        String[] parts= fecont.split("-");
        boolean answer = false;
        try {
            deleteFromSicc(fecont);

            Query getAnexo = entityManager.createNativeQuery(
                    "INSERT INTO nexco_anexos (identificacion, divisa, cuenta, empresa, aplicativo, centro, tipo, digito_verif, nombre, forigen, fcierr, saldo, periodo, contrato) " +
                    "SELECT DISTINCT IDENTIFICACION, divisa, cuenta, '0013' AS empresa,'UA0' AS aplicativo, centro, tipo, digito_verif, nombre, CASE WHEN LEN(forigen)=9 THEN '0'+forigen ELSE forigen END, CASE WHEN LEN(fcierr)=9 THEN '0'+fcierr ELSE fcierr END, abs(saldo), (SELECT max(FechaHabil) FROM FECHAS_HABILES WHERE MONTH(FechaHabil) = ? AND YEAR (FechaHabil) = ?), " +
                            "CASE WHEN cupos.contrato_ifrs9 = 'CUP-' THEN CONCAT(cupos.contrato_ifrs9,centro,identificacion) ELSE cupos.contrato_ifrs9 END AS contrato " +
                    "FROM (" +
                            "SELECT identificacion, divisa, cuenta,  centro, TP AS tipo, DV AS digito_verif, nombre, min(FECHA_ORIGEN) AS forigen, max(FECHA_CIERRE) AS fcierr, sum(IMPORTE) AS saldo, '"+fecont2+"' AS periodo " +
                            "FROM Cargas_Anexos_SICC_" + fecont2 + " " +
                            "GROUP BY identificacion, divisa, cuenta, centro, TP, DV, nombre" +
                    ")AS sicc " +
                    "INNER JOIN nexco_cupos cupos " +
                    "ON sicc.cuenta = cupos.cuentas_puc AND ( cupos.contrato_ifrs9='CUP-')");

            getAnexo.setParameter(1,parts[1]);
            getAnexo.setParameter(2,parts[0]);
            getAnexo.executeUpdate();

            Query validate = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE MONTH(periodo)= ? AND YEAR(periodo)= ?");
            validate.setParameter(1, parts[1]);
            validate.setParameter(2, parts[0]);
            if(validate.getResultList().size()>0)
            {
                answer=true;
            }

            Date today = new Date();
            String input = "ANEXO8";

            StatusInfo validateStatus = statusInfoRepositoryL.findByInputAndPeriodo(input, fecont);

            if (validateStatus == null) {
                StatusInfo status = new StatusInfo();
                status.setInput(input);
                status.setPeriodo(fecont);
                status.setFecha(today);
                statusInfoRepositoryL.save(status);
            } else {
                validateStatus.setFecha(today);
                statusInfoRepositoryL.save(validateStatus);
            }

        }
        catch(NoResultException e){

            return false;
        }
        return answer;

    }

    public ArrayList getSiccByMonth(String fecont) throws ParseException {
        ArrayList result = new ArrayList();

        result.add(0, getAnexo(fecont));
        result.add(1,fecont);
        return result;
    }

    public List validarContraQuery(String fecont) throws ParseException {
        List result = new ArrayList();
        String[] parts= fecont.split("-");
        try{

            Query getAnexoAgrupado = entityManager.createNativeQuery("SELECT cuenta, divisa, periodo, sum(saldo) as saldo " +
                    "INTO anexo_agrupado" +
                    "FROM nexco_anexos " +
                    "WHERE MONTH(periodo)= ? AND YEAR(periodo)= ? " +
                    "GROUP BY cuenta divisa periodo"
            );
            getAnexoAgrupado.setParameter(1,parts[1]);
            getAnexoAgrupado.setParameter(2,parts[0]);
            getAnexoAgrupado.executeUpdate();

            Query cruceAnexoQuery = entityManager.createNativeQuery(
                    "SELECT a.cuenta, a.divisa, a.saldo as saldo_anexo, q.saldo as query_anexo, (a.saldo - q.saldo) as diferencia, ? as periodo_contable, q.periodo as fecha_proceso " +
                    "FROM anexo_agrupado a INNER JOIN " +
                    "nexco_query_eeff q ON " +
                    "a.cuenta = q.cuenta"
            );
            cruceAnexoQuery.setParameter(1,fecont);
            result = cruceAnexoQuery.getResultList();

            Query borrarAnexoAgrupado = entityManager.createNativeQuery("DROP TABLE anexo_agrupado");
            borrarAnexoAgrupado.executeUpdate();

            return result;
        } catch(NoResultException e){

            return result;
        }
    }

    public List<Anexo> getAnexo(String fecont){
        String[] parts = fecont.split("-");
        Query selectSicc = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
        selectSicc.setParameter(1, parts[1]);
        selectSicc.setParameter(2, parts[0]);
        return selectSicc.getResultList();
    }


    public void deleteFromSicc(String fecont){
        String fecont2=fecont.replace("-", "");
        String[] fecontPart=fecont.split("-");
        Query deleteSicc = entityManager.createNativeQuery("DELETE FROM nexco_anexos WHERE MONTH(periodo)= ? AND YEAR(periodo)= ?");
        deleteSicc.setParameter(1, fecontPart[1]);
        deleteSicc.setParameter(2, fecontPart[0]);
        deleteSicc.executeUpdate();
    }


    public List<Anexo> findByFilter(String value, String filter, String period){
        List<Anexo> list = new ArrayList<>();
        String[] fecontPart=period.split("-");
        switch (filter){
            case "identificacion":
                Query identificacion = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE identificacion LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ? ",Anexo.class);
                identificacion.setParameter(1,value);
                identificacion.setParameter(2, fecontPart[1]);
                identificacion.setParameter(3, fecontPart[0]);
                list = identificacion.getResultList();
                break;
            case "divisa":
                Query divisa = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE divisa LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                divisa.setParameter(1,value);
                divisa.setParameter(2, fecontPart[1]);
                divisa.setParameter(3, fecontPart[0]);
                list = divisa.getResultList();
                break;
            case "cuenta":
                Query cuenta = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE cuenta LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                cuenta.setParameter(1,value);
                cuenta.setParameter(2, fecontPart[1]);
                cuenta.setParameter(3, fecontPart[0]);
                list = cuenta.getResultList();
                break;
            case "empresa":
                Query empresa = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE empresa LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                empresa.setParameter(1,value);
                empresa.setParameter(2, fecontPart[1]);
                empresa.setParameter(3, fecontPart[0]);
                list = empresa.getResultList();
                break;
            case "contrato":
                Query contrato = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE contrato LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                contrato.setParameter(1,value);
                contrato.setParameter(2, fecontPart[1]);
                contrato.setParameter(3, fecontPart[0]);
                list = contrato.getResultList();
                break;
            case "centro":
                Query centro = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE centro LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                centro.setParameter(1,value);
                centro.setParameter(2, fecontPart[1]);
                centro.setParameter(3, fecontPart[0]);
                list = centro.getResultList();
                break;
            case "tipo":
                Query tipo = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE tipo LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                tipo.setParameter(1,value);
                tipo.setParameter(2, fecontPart[1]);
                tipo.setParameter(3, fecontPart[0]);
                list = tipo.getResultList();
                break;
            case "digito verificacion":
                Query digito_verif = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE digito_verif LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                digito_verif.setParameter(1,value);
                digito_verif.setParameter(2, fecontPart[1]);
                digito_verif.setParameter(3, fecontPart[0]);
                list = digito_verif.getResultList();
                break;
            case "nombre":
                Query nombre = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE nombre LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                nombre.setParameter(1,value);
                nombre.setParameter(2, fecontPart[1]);
                nombre.setParameter(3, fecontPart[0]);
                list = nombre.getResultList();
                break;
            case "fecha origen":
                Query forigen = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE forigen LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                forigen.setParameter(1,value);
                forigen.setParameter(2, fecontPart[1]);
                forigen.setParameter(3, fecontPart[0]);
                list = forigen.getResultList();
                break;
            case "fecha cierr":
                Query fcierr = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE fcierr LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                fcierr.setParameter(1,value);
                fcierr.setParameter(2, fecontPart[1]);
                fcierr.setParameter(3, fecontPart[0]);
                list = fcierr.getResultList();
                break;
            case "saldo":
                Query saldo = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE saldo LIKE ? AND MONTH(periodo)= ? AND YEAR(periodo)= ?", Anexo.class);
                saldo.setParameter(1,value);
                saldo.setParameter(2, fecontPart[1]);
                saldo.setParameter(3, fecontPart[0]);
                list = saldo.getResultList();
                break;
        }
        return list;
    }

    public List<Object[]> validateLoad(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT * FROM nexco_anexos WHERE periodo LIKE ? ");
        queryValidate.setParameter(1, period+"%");

        return queryValidate.getResultList();
    }

    public List<Object[]> getCompany(String periodo){

        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,periodo+"%");

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
        Query companies = entityManager.createNativeQuery(
                "select empresa from "+nameTable+" where "+tempValue+" substring(fecont, 1, 7) = ? GROUP BY empresa" );

        companies.setParameter(1,periodo);

        return companies.getResultList();

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

        Query query= entityManager.createNativeQuery("SELECT a.cuentas_puc, ISNULL(ISNULL(b.id_divisa, C.coddiv),'-') divisa,ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)) saldo_pesos,\n" +
                "abs(CAST(ISNULL(c.salmes, 0) AS FLOAT)) salmes, \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT) diferencia_pesos,c.fechproce \n" +
                "FROM nexco_cupos a \n" +
                "LEFT JOIN (SELECT cuenta cuenta_contable, divisa id_divisa, sum(saldo) saldo_pesos \n" +
                "FROM nexco_anexos\n" +
                "WHERE cuenta != '' \n" +
                "AND periodo like ? \n" +
                "group by cuenta, divisa) b \n" +
                "on a.cuentas_puc = b.cuenta_contable \n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(salmesd) salmesd,sum(salmes) salmes \n" +
                "FROM "+nameTable+" \n" +
                "WHERE "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = ? \n" +
                "group by fecont,fechproce, nucta, coddiv) c \n" +
                "on a.cuentas_puc = c.nucta \n" +
                "GROUP BY a.cuentas_puc, ISNULL(ISNULL(b.id_divisa, C.coddiv),'-'),ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)),\n" +
                "abs(CAST(ISNULL(c.salmes, 0) AS FLOAT)), \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT),c.fechproce \n" +
                "ORDER BY 1 asc \n" +
                ";");

        query.setParameter(1,period+"%");
        query.setParameter(2,period);
        query.setParameter(3,empresa);
        return query.getResultList();

    }
}


