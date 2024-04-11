package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.Sicc;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
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
public class SiccService{

    @Autowired
    EntityManager entityManager;

    @Autowired
    private ControlPanelJobsRepository controlPanelJobsRepository;

    @Autowired
    private ControlPanelJobsService controlPanelJobsService;

    @Autowired
    private AuditRepository auditRepository;

    public SiccService(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public ArrayList getActualSicc() throws ParseException {
        ArrayList result = new ArrayList();

        DateFormat formatter = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        Date date = formatter.parse(String.valueOf(calendar.get(Calendar.YEAR))+String.valueOf(calendar.get(Calendar.MONTH)));

        String fecont = formatter.format(date);

        result.add(0,getSicc(fecont));

        result.add(1,fecont);

        return result;
    }

    @Scheduled(cron = "0 50 23 * * ?")
    public void saveFromSiccJob() {

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_jobs as em WHERE em.nombre = ? AND em.estado = ?", ControlPanelJobs.class);
        query1.setParameter(1, "SICC");
        query1.setParameter(2, true);
        List<ControlPanelJobs> listTemporal=query1.getResultList();
        if(listTemporal.size()!=0)
        {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            String todayString="";
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            saveFromSiccDatabase(todayString.replace("-", ""),null);

            ControlPanelJobs update =listTemporal.get(0);
            update.setFechaEjecucion(new Date());
            controlPanelJobsRepository.save(update);
            auditCode("Ejecuciòn exitosa Job SICC",null);
        }

        ControlPanelJobs job =controlPanelJobsService.findByIdJob(4);
        job.setFechaEjecucion(new Date());
        job.setFechaEjecucionExitosa(new Date());
        controlPanelJobsService.save(job);

    }

    public boolean saveFromSiccDatabase(String fecont,User user){

        try {
            deleteFromSicc(fecont);

            Query getSICC = entityManager.createNativeQuery("INSERT INTO nexco_sicc (cod_neocon, divisa, periodo_contable, yntp, sociedad_yntp, contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local)\n" +
                    "SELECT b.CODICONS46, a.DIVISA,"+fecont+",yntp.yntp, yntp.sociedad_corta, a.CONTRATO,a.IDENTIFICACION,a.valor,p.id_pais ,p.nombre_pais, a.CUENTA FROM\n" +
                    "(SELECT sicc.cuenta, sicc.divisa, CAST(sicc.IMPORTE as varchar) valor, sicc.IDENTIFICACION, sicc.CONTRATO FROM Cargas_Anexos_SICC_"+fecont +" as sicc\n" +
                    "INNER JOIN (select cuenta_local from nexco_cuentas_responsables where aplica_sicc = 1) as resp \n" +
                    "ON sicc.CUENTA like  CAST(resp.cuenta_local AS varchar)+'%') as a \n" +
                    "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.CUENTA = b.NUCTA \n" +
                    "INNER JOIN nexco_terceros AS ter ON ter.nit_contraparte = a.IDENTIFICACION \n" +
                    "INNER JOIN nexco_sociedades_yntp AS yntp ON ter.yntp = yntp.yntp \n" +
                    "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais");

            getSICC.executeUpdate();
            if (user!=null)
                auditCode("Ejecuciòn manual exitosa Job SICC",user);
            return true;
        }
        catch(Exception e){
            if (user!=null)
                auditCode("Ejecuciòn manual fallida Job SICC",user);
            return false;
        }

    }

    public ArrayList getSiccByMonth(String fecont) throws ParseException {
        ArrayList result = new ArrayList();

        DateFormat formatter = new SimpleDateFormat("yyyy-MM");
        DateFormat toCorrect = new SimpleDateFormat("yyyyMM");
        Date fechaCon = formatter.parse(fecont);
        String correctDate = toCorrect.format(fechaCon);

        result.add(0, getSicc(correctDate));
        result.add(1,fecont);
        return result;
    }

    public List<Sicc> getSicc(String fecont){
        Query selectSicc = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE periodo_contable = ?", Sicc.class);
        selectSicc.setParameter(1, fecont.replace("-",""));
        return selectSicc.getResultList();
    }


    public void deleteFromSicc(String fecont){
        Query deleteSicc = entityManager.createNativeQuery("DELETE FROM nexco_sicc WHERE periodo_contable = ?");
        deleteSicc.setParameter(1, fecont);
        deleteSicc.executeUpdate();
    }


    public List<Sicc> findByFilter(String value, String filter, String period){
        List<Sicc> list = new ArrayList<>();

        switch (filter){
            case "Código Neocon":
                Query neocon = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE cod_neocon LIKE ? AND periodo_contable = ? ",Sicc.class);
                neocon.setParameter(1,value);
                neocon.setParameter(2,period.replace("-",""));
                list = neocon.getResultList();
                break;
            case "Código País":
                Query codPais = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE pais LIKE ? AND periodo_contable = ?", Sicc.class);
                codPais.setParameter(1,value);
                codPais.setParameter(2,period.replace("-",""));
                list = codPais.getResultList();
                break;
            case "Contrato":
                Query contrato = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE contrato LIKE ? AND periodo_contable = ?", Sicc.class);
                contrato.setParameter(1,value);
                contrato.setParameter(2,period.replace("-",""));
                list = contrato.getResultList();
                break;
            case "Cuenta Local":
                Query cuenta = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE cuenta_local LIKE ? AND periodo_contable = ?", Sicc.class);
                cuenta.setParameter(1,value);
                cuenta.setParameter(2,period.replace("-",""));
                list = cuenta.getResultList();
                break;
            case "Divisa":
                Query divisa = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE divisa LIKE ? AND periodo_contable = ?", Sicc.class);
                divisa.setParameter(1,value);
                divisa.setParameter(2,period.replace("-",""));
                list = divisa.getResultList();
                break;
            case "Nit Contraparte":
                Query nit = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE nit_contraparte LIKE ? AND periodo_contable = ?", Sicc.class);
                nit.setParameter(1,value);
                nit.setParameter(2,period.replace("-",""));
                list = nit.getResultList();
                break;
            case "País":
                Query pais = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE pais LIKE ? AND periodo_contable = ?", Sicc.class);
                pais.setParameter(1,value);
                pais.setParameter(2,period.replace("-",""));
                list = pais.getResultList();
                break;
            case "Sociedad YNTP":
                Query socYntp = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE sociedad_yntp LIKE ? AND periodo_contable = ?", Sicc.class);
                socYntp.setParameter(1,value);
                socYntp.setParameter(2,period.replace("-",""));
                list = socYntp.getResultList();
                break;
            case "Valor":
                Query valor = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE valor LIKE ? AND periodo_contable = ?", Sicc.class);
                valor.setParameter(1,value);
                valor.setParameter(2,period.replace("-",""));
                list = valor.getResultList();
                break;
            case "YNTP":
                Query yntp = entityManager.createNativeQuery("SELECT * FROM nexco_sicc WHERE yntp LIKE ? AND periodo_contable = ?", Sicc.class);
                yntp.setParameter(1,value);
                yntp.setParameter(2,period.replace("-",""));
                list = yntp.getResultList();
                break;
        }
        return list;
    }

    public ArrayList<Object[]> getDiferentAccounts(String fecont) throws ParseException {

        DateFormat formatter = new SimpleDateFormat("yyyy-MM");
        DateFormat toCorrect = new SimpleDateFormat("yyyyMM");
        Date fechaCon = formatter.parse(fecont);
        String correctDate = toCorrect.format(fechaCon);


        ArrayList toReturn = new ArrayList();

        Query result = entityManager.createNativeQuery("SELECT b.CODICONS46, a.DIVISA,yntp.yntp, yntp.sociedad_corta, a.CONTRATO,a.IDENTIFICACION,a.valor,p.id_pais ,p.nombre_pais, a.CUENTA,"+correctDate+" FROM\n" +
                "(SELECT sicc.cuenta, sicc.divisa, CAST(sicc.IMPORTE as varchar) valor, sicc.IDENTIFICACION, sicc.CONTRATO , yntp FROM Cargas_Anexos_SICC_"+correctDate+" as sicc\n" +
                "INNER JOIN nexco_terceros as ter ON sicc.IDENTIFICACION = ter.nit_contraparte) AS a\n" +
                "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.CUENTA = b.NUCTA \n" +
                "INNER JOIN nexco_sociedades_yntp AS yntp ON a.yntp = yntp.yntp\n" +
                "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais\n" +
                "WHERE a.CUENTA NOT IN (SELECT CAST(cuenta_local as varchar) FROM nexco_cuentas_responsables)");

        if(!result.getResultList().isEmpty()){
            toReturn = (ArrayList) result.getResultList();
        }

        return toReturn;
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("SICC");
        if(user!=null)
        {
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            insert.setCentro(user.getCentro());
        }
        else
        {
            insert.setNombre("SYSTEM JOB");
        }
        auditRepository.save(insert);
    }


}
