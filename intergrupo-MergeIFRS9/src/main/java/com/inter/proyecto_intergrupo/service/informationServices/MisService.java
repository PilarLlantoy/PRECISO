package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.Mis;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import com.sun.xml.bind.v2.runtime.output.SAXOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.SimpleFormatter;

@Service
@Transactional
public class MisService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    ControlPanelJobsService controlPanelJobsService;

    @Autowired
    ControlPanelJobsRepository controlPanelJobsRepository;

    @Autowired
    private AuditRepository auditRepository;


    @Scheduled(cron = "0 0 7 * * ?")
    public void saveFromMisJob() throws ParseException {

        boolean result = true;

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_jobs as em WHERE em.nombre = 'Depositos' AND em.estado = 1", ControlPanelJobs.class);
        if(!query1.getResultList().isEmpty())
        {
            List<ControlPanelJobs> listTemporal = query1.getResultList();
            Date todayString = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
            String realDate = formatter.format(todayString);


            Date dateMinusMonth = formatter.parse(realDate);
            Calendar c = Calendar.getInstance();
            c.setTime(dateMinusMonth);
            c.add(Calendar.MONTH,-1);
            Date resultAfter  = c.getTime();

            String finalDate = formatter.format(resultAfter);

            result = insertFromMis(finalDate,null);

            ControlPanelJobs update =listTemporal.get(0);
            update.setFechaEjecucion(new Date());
            controlPanelJobsRepository.save(update);
        }

        ControlPanelJobs job =controlPanelJobsService.findByIdJob(4);
        job.setFechaEjecucion(new Date());

        if(result){
            job.setFechaEjecucionExitosa(new Date());
            auditCode("Ejecuciòn exitosa Job Depositos",null);
        }
        else
        {
            auditCode("Ejecuciòn Fallida Job Depositos",null);
        }
        controlPanelJobsService.save(job);

    }

    public boolean insertFromMis(String fecont, User user){

        boolean response;

        try {

            Query delete = entityManager.createNativeQuery("DELETE FROM nexco_mis where periodo_contable = ?");
            delete.setParameter(1,fecont);

            delete.executeUpdate();

            Query getFromMis = entityManager.createNativeQuery("INSERT INTO nexco_mis (cod_neocon,divisa,yntp,sociedad_yntp,contrato,nit_contraparte,valor,cod_pais,pais,cuenta_local,observaciones,periodo_contable) \n" +
                    "SELECT b.CODICONS46, 'COP', soc.yntp, soc.sociedad_corta, a.CONTRATO, ter.nit_contraparte, (a.VALOR * -1) AS VALOR, p.id_pais, p.nombre_pais, a.NUCTA, 'HADT141-Concil Automática', '"+fecont+"' FROM \n" +
                    "(SELECT TRIM(NUCTA COLLATE SQL_Latin1_General_CP1_CI_AS) as NUCTA, CONTRATO COLLATE SQL_Latin1_General_CP1_CI_AS as CONTRATO, CONVERT(numeric(18,2),REPLACE(REPLACE(VALOR,'+',''),',','')) VALOR, EMPRESA \n" +
                    "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.cont_h141mes_"+fecont.substring(0,4)+"_"+fecont.substring(5,7)+" as mis \n" +
                    "INNER JOIN (SELECT cuenta_local from nexco_cuentas_responsables where aplica_mis = 1) as cr  \n" +
                    "ON mis.NUCTA LIKE CAST(cr.cuenta_local AS varchar)+'%') AS a\n" +
                    "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.NUCTA = b.NUCTA \n" +
                    "INNER JOIN nexco_contratos AS con ON con.id_contrato = a.CONTRATO \n" +
                    "INNER JOIN nexco_terceros as ter ON ter.nit_contraparte = con.banco \n" +
                    "INNER JOIN nexco_sociedades_yntp as soc ON ter.yntp = soc.yntp \n" +
                    "INNER JOIN nexco_paises as p ON p.id_pais = soc.id_pais \n" +
                    "WHERE con.tipo_proceso = 'MIS' ");
            getFromMis.executeUpdate();
            response = true;
            if(user != null)
                auditCode("Ejecuciòn exitosa Job Depositos",user);
        }
        catch (Exception e){
            e.printStackTrace();
            response = false;
            if(user != null)
                auditCode("Ejecuciòn Fallida Job Depositos",user);
        }

        return response;
    }

    public List<Mis> getFromMis(String fecont){
        List<Mis> toReturn = new ArrayList<>();
        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_mis WHERE periodo_contable = ?", Mis.class);
        result.setParameter(1,fecont);

        if(!result.getResultList().isEmpty()){
            toReturn = result.getResultList();
        }

        return toReturn;
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Depositos");
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
}
