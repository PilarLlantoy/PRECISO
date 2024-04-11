package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.CondetaRI;
import com.inter.proyecto_intergrupo.model.ifrs9.IncurredLoss;
import com.inter.proyecto_intergrupo.model.ifrs9.RejectionsCc;
import com.inter.proyecto_intergrupo.model.reports.Rp21;
import com.inter.proyecto_intergrupo.model.temporal.RejectionsCcTemporal;
import com.inter.proyecto_intergrupo.model.temporal.Rp21Temporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RejectionsCcRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CurrencyRepository;
import com.inter.proyecto_intergrupo.repository.parametric.OperationAccountRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ResponsibleAccountRepository;
import com.inter.proyecto_intergrupo.repository.reports.Rp21Repository;
import com.inter.proyecto_intergrupo.repository.reports.Rp21TemporalRepository;
import com.inter.proyecto_intergrupo.repository.temporal.RejectionsCcTemporalRepository;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import com.inter.proyecto_intergrupo.utility.Utility;
import groovy.lang.Tuple2;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class RejectionsCcService {

    @Autowired
    private RejectionsCcRepository rejectionsCcRepository;

    @Autowired
    private RejectionsCcTemporalRepository rejectionsCcTemporalRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    SendEmailService sendEmailService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PersistenceContext
    EntityManager entityManager;

    private List<RejectionsCcTemporal> rejectionsCcTemporalList = new ArrayList<>();

    public RejectionsCcService(RejectionsCcRepository rejectionsCcRepository,AuditRepository auditRepository,RejectionsCcTemporalRepository rejectionsCcTemporalRepository) {
        this.rejectionsCcRepository=rejectionsCcRepository;
        this.auditRepository = auditRepository;
        this.rejectionsCcTemporalRepository = rejectionsCcTemporalRepository;
    }

    public List<String[]> saveFileBD(Collection<Part>  parts, User user) throws IOException, InvalidFormatException, ParseException {
        rejectionsCcTemporalList.clear();
        List<String[]> listCuentaProv=new ArrayList<String[]>();
        List<String[]> listCuentaRecla=new ArrayList<String[]>();
        List<String[]> listCuentaImpu=new ArrayList<String[]>();
        List<String[]> listRistrasProv=new ArrayList<String[]>();
        List<String[]> listRistrasRecla =new ArrayList<String[]>();
        List<String[]> listRistrasImpu=new ArrayList<String[]>();
        List<String[]> finalList= new ArrayList<String[]>();
        List<String> names=new ArrayList<String>();
        String[] listReportNames=new String[6];

        boolean duplicated=false;
        boolean error=false;
        boolean pass=true;

        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_ifrs as em " +
                "WHERE em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query.setParameter(1, "CUENTA PROV" );
        query.setParameter(2, "RECHAZOS");
        List<ControlPanelIfrs> cuentaProvList= query.getResultList();

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_ifrs as em " +
                "WHERE em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query1.setParameter(1, "CUENTA RECLASIFICACION" );
        query1.setParameter(2, "RECHAZOS");
        List<ControlPanelIfrs> cuentaReclaList= query1.getResultList();

        Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando_ifrs as em " +
                "WHERE em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query2.setParameter(1, "CUENTA IMPUESTOS" );
        query2.setParameter(2, "RECHAZOS");
        List<ControlPanelIfrs> cuentaImpuList= query2.getResultList();

        Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query3.setParameter(1, "RISTRAS PROV" );
        query3.setParameter(2, "RECHAZOS");
        List<ControlPanelIfrs> ristrasProvList= query3.getResultList();

        Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query4.setParameter(1, "RISTRAS RECLASIFICACION" );
        query4.setParameter(2, "RECHAZOS");
        List<ControlPanelIfrs> ristrasReclaList= query4.getResultList();

        Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query5.setParameter(1, "RISTRAS IMPUESTOS" );
        query5.setParameter(2, "RECHAZOS");
        List<ControlPanelIfrs> ristrasImpuList= query5.getResultList();

        String listNames="";
        for(Part partName : parts) {
            if(names.contains(partName.getSubmittedFileName()))
            {
                duplicated=true;
            }
            else{
                names.add(partName.getSubmittedFileName());
                listNames = listNames+partName.getSubmittedFileName();
            }
        }
        listReportNames[0]=listNames;

        for(int j=0;j<names.size();j++) {
            if(names.get(j)!=null){
                if (!names.get(j).equals("RECHAZOS_CUENTA_PROV_PLAN00.TXT")) {
                    if (!names.get(j).equals("RECHAZOS_CUENTA_RECLASIFICACION_PLAN00.TXT")) {
                        if (!names.get(j).equals("RECHAZOS_CUENTA_IMPUESTOS.TXT")) {
                            if (!names.get(j).equals("RECHAZOS_RISTRA_PROV_PLAN00.TXT")) {
                                if (!names.get(j).equals("RECHAZOS_RISTRA_RECLASIFICACION_PLAN00.TXT")) {
                                    if (!names.get(j).equals("RECHAZOS_RISTRA_IMPUESTOS.TXT")) {
                                        error = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if(cuentaProvList.size()>0 && names.contains("RECHAZOS_CUENTA_PROV_PLAN00.TXT") &&(cuentaProvList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(cuentaReclaList.size()>0 && names.contains("RECHAZOS_CUENTA_RECLASIFICACION_PLAN00.TXT") && (cuentaReclaList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(cuentaImpuList.size()>0 && names.contains("RECHAZOS_CUENTA_IMPUESTOS.TXT") && (cuentaImpuList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(ristrasProvList.size()>0 && names.contains("RECHAZOS_RISTRA_PROV_PLAN00.TXT") && (ristrasProvList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(ristrasReclaList.size()>0 && names.contains("RECHAZOS_RISTRA_RECLASIFICACION_PLAN00.TXT") && (ristrasReclaList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(ristrasImpuList.size()>0 && names.contains("RECHAZOS_RISTRA_IMPUESTOS.TXT") && (ristrasImpuList.get(0).getEstado()==false))
        {
            pass=false;
        }
        if(cuentaProvList.size()==0 && names.contains("RECHAZOS_CUENTA_PROV_PLAN00.TXT"))
        {
            pass=false;
        }
        if(cuentaReclaList.size()==0 && names.contains("RECHAZOS_CUENTA_RECLASIFICACION_PLAN00.TXT"))
        {
            pass=false;
        }
        if(cuentaImpuList.size()==0 && names.contains("RECHAZOS_CUENTA_IMPUESTOS.TXT"))
        {
            pass=false;
        }
        if(ristrasProvList.size()==0 && names.contains("RECHAZOS_RISTRA_PROV_PLAN00.TXT"))
        {
            pass=false;
        }
        if(ristrasReclaList.size()==0 && names.contains("RECHAZOS_RISTRA_RECLASIFICACION_PLAN00.TXT"))
        {
            pass=false;
        }
        if(ristrasImpuList.size()==0 && names.contains("RECHAZOS_RISTRA_IMPUESTOS.TXT"))
        {
            pass=false;
        }

        if(duplicated==true){
            String[] logFinal= new String[4];
            logFinal[0]="DUPLICADO";
            finalList.add(logFinal);
        }
        else if(error==true)
        {
            String[] logFinal= new String[4];
            logFinal[0]="ERROR";
            finalList.add(logFinal);
        }
        else if(pass==false)
        {
            String[] logFinal= new String[4];
            logFinal[0]="PERMISO";
            finalList.add(logFinal);
        }
        else
        {
            Query truncateTable = entityManager.createNativeQuery("TRUNCATE TABLE nexco_rechazos_cc_temporal");
            truncateTable.executeUpdate();

            for(Part part : parts) {
                InputStream file = part.getInputStream();

                if (part!=null && file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RECHAZOS_CUENTA_PROV_PLAN00.TXT"))
                {
                    listCuentaProv = validarPlantillaCuentas(file,user,"CUENTA PROV");
                }
                else if (part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RECHAZOS_CUENTA_RECLASIFICACION_PLAN00.TXT"))
                {
                    listCuentaRecla = validarPlantillaCuentas(file,user,"CUENTA RECLASIFICACION");
                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RECHAZOS_CUENTA_IMPUESTOS.TXT"))
                {
                    listCuentaImpu = validarPlantillaCuentas(file,user,"CUENTA IMPUESTOS");
                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RECHAZOS_RISTRA_PROV_PLAN00.TXT"))
                {
                    listRistrasProv = validarPlantillaCuentas(file,user,"RISTRAS PROV");
                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RECHAZOS_RISTRA_RECLASIFICACION_PLAN00.TXT"))
                {
                    listRistrasRecla= validarPlantillaCuentas(file,user,"RISTRAS RECLASIFICACION");
                }
                else if(part!=null &&file != null && part.getSubmittedFileName()!=null && part.getSubmittedFileName().equals("RECHAZOS_RISTRA_IMPUESTOS.TXT"))
                {
                    listRistrasImpu = validarPlantillaCuentas(file,user,"RISTRAS IMPUESTOS");
                }
            }

            if(!listCuentaProv.isEmpty())
            {
                finalList = Stream.concat(finalList.stream(), listCuentaProv.stream()).collect(Collectors.toList());
                if (listCuentaProv.get(listCuentaProv.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_rechazos_cc " +
                            "WHERE origen = ?", RejectionsCc.class);
                    queryFor.setParameter(1, "CUENTA PROV");
                    queryFor.executeUpdate();

                    ControlPanelIfrs temporal2 = cuentaProvList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ? " +
                            "WHERE input = ? AND componente = ? ", ControlPanelIfrs.class);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, temporal2.getInput());
                    query7.setParameter(3, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Rechazos CUENTA PROV");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("RECHAZOS");
                    insert.setFecha(today);
                    insert.setInput("CUENTA PROV");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("CUENTA PROV");
                }
            }
            else
            {
                changeEmpty("CUENTA PROV");
            }
            if(!listCuentaRecla.isEmpty()) {

                finalList = Stream.concat(finalList.stream(), listCuentaRecla.stream()).collect(Collectors.toList());
                if (listCuentaRecla.get(listCuentaRecla.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_rechazos_cc " +
                            "WHERE origen = ?", RejectionsCc.class);
                    queryFor.setParameter(1, "CUENTA RECLASIFICACION");
                    queryFor.executeUpdate();

                    ControlPanelIfrs temporal2 = cuentaReclaList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ? " +
                            "WHERE input = ? AND componente = ? ", ControlPanelIfrs.class);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, temporal2.getInput());
                    query7.setParameter(3, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Rechazos CUENTA RECLASIFICACION");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("RECHAZOS");
                    insert.setFecha(today);
                    insert.setInput("CUENTA RECLASIFICACION");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("CUENTA RECLASIFICACION");
                }
            }
            else
            {
                changeEmpty("CUENTA RECLASIFICACION");
            }
            if(!listCuentaImpu.isEmpty()) {

                finalList = Stream.concat(finalList.stream(), listCuentaImpu.stream()).collect(Collectors.toList());
                if (listCuentaImpu.get(listCuentaImpu.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_rechazos_cc " +
                            "WHERE origen = ?", RejectionsCc.class);
                    queryFor.setParameter(1, "CUENTA IMPUESTOS");
                    queryFor.executeUpdate();

                    ControlPanelIfrs temporal2 = cuentaImpuList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ? " +
                            "WHERE input = ? AND componente = ? ", ControlPanelIfrs.class);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, temporal2.getInput());
                    query7.setParameter(3, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Rechazos CUENTA IMPUESTOS");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("RECHAZOS");
                    insert.setFecha(today);
                    insert.setInput("CUENTA IMPUESTOS");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("CUENTA IMPUESTOS");
                }
            }
            else
            {
                changeEmpty("CUENTA IMPUESTOS");
            }
            if(!listRistrasProv.isEmpty()) {

                finalList = Stream.concat(finalList.stream(), listRistrasProv.stream()).collect(Collectors.toList());
                if (listRistrasProv.get(listRistrasProv.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_rechazos_cc " +
                            "WHERE origen = ?", RejectionsCc.class);
                    queryFor.setParameter(1, "RISTRAS PROV");
                    queryFor.executeUpdate();

                    ControlPanelIfrs temporal2 = ristrasProvList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ? " +
                            "WHERE input = ? AND componente = ? ", ControlPanelIfrs.class);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, temporal2.getInput());
                    query7.setParameter(3, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Rechazos RISTRAS PROV");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("RECHAZOS");
                    insert.setFecha(today);
                    insert.setInput("RISTRAS PROV");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("RISTRAS PROV");
                }
            }
            else
            {
                changeEmpty("RISTRAS PROV");
            }
            if(!listRistrasRecla.isEmpty()) {

                finalList = Stream.concat(finalList.stream(), listRistrasRecla.stream()).collect(Collectors.toList());
                if (listRistrasRecla.get(listRistrasRecla.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_rechazos_cc " +
                            "WHERE origen = ?", RejectionsCc.class);
                    queryFor.setParameter(1, "RISTRAS RECLASIFICACION");
                    queryFor.executeUpdate();

                    ControlPanelIfrs temporal2 = ristrasReclaList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ? " +
                            "WHERE input = ? AND componente = ? ", ControlPanelIfrs.class);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, temporal2.getInput());
                    query7.setParameter(3, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Rechazos RISTRAS RECLASIFICACION");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("RECHAZOS");
                    insert.setFecha(today);
                    insert.setInput("RISTRAS RECLASIFICACION");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("RISTRAS RECLASIFICACION");
                }
            }
            else
            {
                changeEmpty("RISTRAS RECLASIFICACION");
            }
            if(!listRistrasImpu.isEmpty()) {

                finalList = Stream.concat(finalList.stream(), listRistrasImpu.stream()).collect(Collectors.toList());
                if (listRistrasImpu.get(listRistrasImpu.size() - 1)[2].equals("0"))
                {
                    Query queryFor = entityManager.createNativeQuery("DELETE from nexco_rechazos_cc " +
                            "WHERE origen = ?", RejectionsCc.class);
                    queryFor.setParameter(1, "RISTRAS IMPUESTOS");
                    queryFor.executeUpdate();

                    ControlPanelIfrs temporal2 = cuentaProvList.get(0);
                    temporal2.setSemaforoInput("PENDING");
                    temporal2.setUsuarioCarga(user.getNombre());

                    Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ? " +
                            "WHERE input = ? AND componente = ? ", ControlPanelIfrs.class);
                    query7.setParameter(1, temporal2.getSemaforoInput());
                    query7.setParameter(2, temporal2.getInput());
                    query7.setParameter(3, temporal2.getComponente());
                    query7.executeUpdate();

                    Date today=new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Rechazos RISTRAS IMPUESTOS");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("RECHAZOS");
                    insert.setFecha(today);
                    insert.setInput("RISTRAS IMPUESTOS");
                    insert.setNombre(user.getNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
                else
                {
                    deleteByOrigen("RISTRAS IMPUESTOS");
                }
            }
            else
            {
                changeEmpty("RISTRAS IMPUESTOS");
            }

            insertLoad();
            validateComponent("RECHAZOS");
            finalList.add(listReportNames);

            rejectionsCcTemporalList.clear();
        }

        return finalList;
    }

    public void changeEmpty(String input)
    {
        Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = ?, semaforo_componente = ? " +
                "WHERE componente = ? AND input = ?", ControlPanelIfrs.class);
        query5.setParameter(1, "EMPTY");
        query5.setParameter(2, "EMPTY");
        query5.setParameter(3, "RECHAZOS");
        query5.setParameter(4, input);
        query5.executeUpdate();
    }

    public boolean processDataLoad()
    {
        try {
            Query truncateTable = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_cc_temporal");
            truncateTable.executeUpdate();

            Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_rechazos_cc_temporal (centro,contrato,cuenta,divisa,empresa,fecha,importe_local,importe_opc,origen,segmento,stage,tipo_rechazo,valor2)\n" +
                    "SELECT A.centro,A.contrato,A.cuenta,A.divisa,A.empresa,A.fecha,A.importe_local,A.importe_opc,A.origen,A.segmento,A.stage,A.tipo_rechazo,A.valor2\n" +
                    "FROM nexco_rechazos_cc AS A");
            insertData.executeUpdate();

            Query truncateTableP = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_cc");
            truncateTableP.executeUpdate();

            insertLoad();

            Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_componente = ?, semaforo_input = ? " +
                    "WHERE componente = ? AND semaforo_input = ?", ControlPanelIfrs.class);
            query5.setParameter(1, "PENDING");
            query5.setParameter(2, "PENDING");
            query5.setParameter(3, "RECHAZOS");
            query5.setParameter(4, "FULL");
            query5.executeUpdate();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void insertLoad()
    {
        Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_rechazos_cc (fecha, empresa, centro, contrato, cuenta, divisa, stage, segmento,importe_local,valor2,tipo_rechazo,importe_opc,origen,tipo_cta,tipo_cuenta,linea_producto,segemento_proceso,stage_proceso,tipo_rechazo_real) \n" +
                "SELECT A.fecha, A.empresa, A.centro, A.contrato, A.cuenta, A.divisa, A.stage, A.segmento,A.importe_local,A.valor2,A.tipo_rechazo,A.importe_opc,A.origen,B.asignacion, B.tipo_cuenta, SUBSTRING(A.cuenta,B.linea_inicial,B.linea_cantidad),SUBSTRING(A.cuenta,B.segmento_inicial,B.segmento_cantidad),SUBSTRING(A.cuenta,B.stage_inicial,B.stage_cantidad), \n" +
                "CASE WHEN SUBSTRING(A.cuenta,B.segmento_inicial,B.segmento_cantidad) like '%@%' OR SUBSTRING(A.cuenta,B.segmento_inicial,B.segmento_cantidad) like '% %' OR SUBSTRING(A.cuenta,B.linea_inicial,B.linea_cantidad) like '%@%' OR SUBSTRING(A.cuenta,B.linea_inicial,B.linea_cantidad) like '% %'  THEN 'Error de armado' WHEN (C.linea_producto IS NOT NULL AND D.segmentos IS NOT NULL) THEN 'Creación de cuenta' WHEN (C.linea_producto IS NOT NULL AND D.segmentos IS NULL) THEN 'Cambio de segmento' ELSE 'Línea no existente en paramétrica' END \n" +
                "FROM nexco_rechazos_cc_temporal A \n" +
                "LEFT JOIN nexco_identificacion_rechazos_p1 B ON SUBSTRING(A.cuenta,1,1) = B.inicial_cuenta \n" +
                "LEFT JOIN nexco_identificacion_rechazos_p2 C ON SUBSTRING(A.cuenta,B.linea_inicial,B.linea_cantidad) = C.linea_producto\n" +
                "LEFT JOIN nexco_identificacion_rechazos_p2 D ON SUBSTRING(A.cuenta,B.linea_inicial,B.linea_cantidad) = D.linea_producto AND D.segmentos LIKE '%'+SUBSTRING(A.cuenta,B.segmento_inicial,B.segmento_cantidad)+'%'");
        insertData.executeUpdate();

        Query updateReactivation = entityManager.createNativeQuery("UPDATE A SET tipo_rechazo_real='Reactivación de cuenta' \n" +
                "FROM nexco_rechazos_cc as A,(select  nucta,estacta from cuentas_puc  where empresa='0060' and estacta='B') as B \n" +
                "where a.cuenta=b.NUCTA and a.tipo_rechazo_real='Creación de cuenta'");
        updateReactivation.executeUpdate();
    }

    public void validateComponent(String component){

        Query queryFinal = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando_ifrs " +
                "WHERE componente = ? AND semaforo_input = 'PENDING' ", ControlPanelIfrs.class);
        queryFinal.setParameter(1, component);

        Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_componente = ? " +
                "WHERE componente = ? ", ControlPanelIfrs.class);
        query5.setParameter(2, component);

        if(queryFinal.getResultList().size()>0)
        {
            query5.setParameter(1, "PENDING");
            query5.executeUpdate();
        }
    }

    public void batchInsert(List<RejectionsCcTemporal> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_rechazos_cc_temporal (fecha, empresa, centro, contrato, cuenta, divisa, stage, segmento,importe_local,valor2,tipo_rechazo,importe_opc,origen) values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getFecha());
                        ps.setString(2, temporal.get(i).getEmpresa());
                        ps.setString(3, temporal.get(i).getCentro());
                        ps.setString(4, temporal.get(i).getContrato());
                        ps.setString(5, temporal.get(i).getCuenta());
                        ps.setString(6, temporal.get(i).getDivisa());
                        ps.setString(7, temporal.get(i).getStage());
                        ps.setString(8, temporal.get(i).getSegmento());
                        ps.setDouble(9, temporal.get(i).getImporteLocal());
                        ps.setString(10, temporal.get(i).getValor2());
                        ps.setString(11, temporal.get(i).getTipoRechazo());
                        ps.setDouble(12, temporal.get(i).getImporteOpc());
                        ps.setString(13, temporal.get(i).getOrigen());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public List<String[]> validarPlantillaCuentas(InputStream file, User user,String origen) throws ParseException, IOException {

        ArrayList lista = new ArrayList();
        ArrayList<RejectionsCcTemporal> toInsert = new ArrayList<>();
        Scanner scan = new Scanner(file);
        int fail=0;
        int success =0;
        int count =0;
        Date lastFECHPROCE = new Date();
        while (scan.hasNextLine()) {
            count++;
            String line = scan.nextLine();
            RejectionsCcTemporal rejection = new RejectionsCcTemporal();
            String[] data = line.split(";");

            rejection.setFecha(data[0].trim());
            rejection.setEmpresa(data[1].trim());
            rejection.setCentro(data[2].trim());
            rejection.setContrato(data[3].trim());
            rejection.setCuenta(data[4].trim());
            rejection.setDivisa(data[5].trim());
            rejection.setStage(data[6].trim());
            rejection.setSegmento(data[7].trim());
            if(data[8].trim().length()>0)
                rejection.setImporteLocal(Double.parseDouble(data[8].trim().replace(",","")));
            else
                rejection.setImporteLocal(0.0);
            rejection.setValor2(data[9].trim());
            rejection.setTipoRechazo(data[10].trim());
            if(data[11].trim().length()>0)
                rejection.setImporteOpc(Double.parseDouble(data[11].trim().replace(",","")));
            else
                rejection.setImporteOpc(0.0);
            rejection.setOrigen(origen);
            toInsert.add(rejection);
            success++;
        }
        if (!toInsert.isEmpty() && fail == 0) {
            batchInsert(toInsert);
            toInsert.clear();
        }
        String[] logFinal=new String[4];
        logFinal[0]=origen;
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Fallo Inserción Documento RECHAZOS CUENTA PROV");
            insert.setCentro(user.getCentro());
            insert.setComponente("RECHAZOS");
            insert.setFecha(today);
            insert.setInput(origen);
            insert.setNombre(user.getNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }

        return lista;
    }

    public List<RejectionsCc> findAll(){
        Query query = entityManager.createNativeQuery("SELECT *" +
                " FROM nexco_rechazos_cc as em", RejectionsCc.class);
        return query.getResultList();
    }

    public List<RejectionsCc> findAllExclude(Object ci,Object cp,Object cr,Object ri, Object rp,Object rr){

        String parametros="";
        if(cp!=null)
        {
            parametros=parametros+" OR ORIGEN ='CUENTA PROV'";
        }
        if(cr!=null)
        {
            parametros=parametros+" OR ORIGEN ='CUENTA RECLASIFICACION'";
        }
        if(ci!=null)
        {
            parametros=parametros+" OR ORIGEN ='CUENTA IMPUESTOS'";
        }
        if(rp!=null)
        {
            parametros=parametros+" OR ORIGEN ='RISTRAS PROV'";
        }
        if(rr!=null)
        {
            parametros=parametros+" OR ORIGEN ='RISTRAS RECLASIFICACION'";
        }
        if(ri!=null)
        {
            parametros=parametros+" OR ORIGEN ='RISTRAS IMPUESTOS'";
        }
        if(parametros.length()>0)
        {
            parametros=" WHERE ("+parametros.substring(3,parametros.length())+")";
        }

        Query query = entityManager.createNativeQuery("SELECT *" +
                " FROM nexco_rechazos_cc as em "+parametros, RejectionsCc.class);

        return query.getResultList();

    }

    public List<Object[]> getAllReport ()
    {
        Query query = entityManager.createNativeQuery("SELECT em.centro, em.empresa, em.cuenta, em.divisa, em.tipo_rechazo_real\n" +
                "FROM nexco_rechazos_cc AS em GROUP BY em.centro, em.empresa, em.cuenta, em.divisa, em.tipo_rechazo_real");
        return query.getResultList();
    }

    public RejectionsCc findRejectionsCcByIdRechazo(Long id){
        return rejectionsCcRepository.findByIdRechazos(id);
    }

    public List<RejectionsCc> findRejectionsCcByCuenta(String cuenta){
        return rejectionsCcRepository.findByCuenta(cuenta);
    }

    public boolean changeRejectReal (String tipo,String cuenta)
    {
        try {
            Query query = entityManager.createNativeQuery("UPDATE nexco_rechazos_cc SET tipo_rechazo_real = ? where cuenta = ?");
            query.setParameter(1,tipo);
            query.setParameter(2,cuenta);
            query.executeUpdate();
            return true;
        }
        catch(Exception e)
        {
            return false;
        }
    }

    public RejectionsCc saveRejectionsCc(RejectionsCc rejectionsCc){
        return rejectionsCcRepository.save(rejectionsCc);
    }

    public void removeRejectionsCc(Long id){
        rejectionsCcRepository.deleteById(id);
    }

    public Page<RejectionsCc> getAll(Pageable pageable){
        return rejectionsCcRepository.findAll(pageable);
    }


    public void deleteByOrigen(String origen) {
        Query deleteAll = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_cc_temporal where origen = ?");
        deleteAll.setParameter(1,origen);
        deleteAll.executeUpdate();
    }

    public boolean deleteAll()
    {
        Query query7 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando_ifrs SET semaforo_input = 'EMPTY' , semaforo_componente = 'EMPTY' " +
                "WHERE componente = ? ", ControlPanelIfrs.class);
        query7.setParameter(1, "RECHAZOS");
        query7.executeUpdate();

        try {
            Query truncateTableP = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_cc");
            truncateTableP.executeUpdate();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public boolean deleteInputRejectionsCc(String input,User user) {

        String inputTable = "";
        String inputTableC = "";

        if(input.equals("cp")){
            inputTable = "CUENTA PROV";
            inputTableC = "CUENTA PROV";
        }else if(input.equals("ci")){
            inputTable = "CUENTA IMPUESTOS";
            inputTableC = "CUENTA IMPUESTOS";
        }else if(input.equals("cr")){
            inputTable = "CUENTA RECLASIFICACION";
            inputTableC = "CUENTA RECLASIFICACION";
        }else if(input.equals("ri")){
            inputTable = "RISTRAS IMPUESTOS";
            inputTableC = "RISTRAS IMPUESTOS";
        }else if(input.equals("rp")){
            inputTable = "RISTRAS PROV";
            inputTableC = "RISTRAS PROV";
        }
        else if(input.equals("rr")){
            inputTable = "RISTRAS RECLASIFICACION";
            inputTableC = "RISTRAS RECLASIFICACION";
        }

        try{

            Query deleteAll = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_cc " +
                    "where origen = ? ");
            deleteAll.setParameter(1,inputTable);

            deleteAll.executeUpdate();

            Query updateAll2 = entityManager.createNativeQuery("update nexco_cuadro_mando_ifrs \n" +
                    "set semaforo_input = 'EMPTY' , usuario_carga = ?, fecha_cargue = ?, semaforo_componente = 'EMPTY'\n" +
                    "where input = ? AND componente = ?");
            updateAll2.setParameter(1,user.getNombre());
            updateAll2.setParameter(2, new Date());
            updateAll2.setParameter(3,inputTableC);
            updateAll2.setParameter(4,"RECHAZOS");
            updateAll2.executeUpdate();
        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    public boolean confirmData(User user, HttpServletRequest request)
    {
        try{
            Date hoy = new Date();
            Query updateAll2 = entityManager.createNativeQuery("update nexco_cuadro_mando_ifrs \n" +
                    "set semaforo_componente = 'FULL' , usuario_carga = ? , fecha_cargue = ?\n" +
                    "where componente = ?");
            updateAll2.setParameter(1,user.getNombre());
            updateAll2.setParameter(2,hoy);
            updateAll2.setParameter(3,"RECHAZOS");
            updateAll2.executeUpdate();

            Query updateAll4 = entityManager.createNativeQuery("update nexco_cuadro_mando_ifrs \n" +
                    "set semaforo_input = 'FULL' \n" +
                    "where componente = ? AND semaforo_input = ?");
            updateAll4.setParameter(1,"RECHAZOS");
            updateAll4.setParameter(2,"PENDING");
            updateAll4.executeUpdate();

            String resetPasswordLink = Utility.getSiteURL(request) + "/ifrs/accountCreation";
            Query emails = entityManager.createNativeQuery("SELECT distinct A.* FROM nexco_usuarios A, nexco_rol_vista B, nexco_vistas C, nexco_user_rol D WHERE A.usuario = D.usuario AND D.id_perfil = B.id_perfil AND B.id_vista = C.id_vista \n" +
                    "AND C.nombre = 'Ver Creación de Cuentas (General)' and a.estado=1 ", User.class);
            List<User>listEmails = emails.getResultList();
            for (User u : listEmails)
            {
                sendEmailG(u.getCorreo(), resetPasswordLink);
            }

            String resetPasswordLink1 = Utility.getSiteURL(request) + "/ifrs/segmentsRisk";
            Query emails1 = entityManager.createNativeQuery("SELECT distinct A.* FROM nexco_usuarios A, nexco_rol_vista B, nexco_vistas C, nexco_user_rol D WHERE A.usuario = D.usuario AND D.id_perfil = B.id_perfil AND B.id_vista = C.id_vista \n" +
                    "AND C.nombre = 'Ver Ajuste Segmentos Rechazos' and a.estado=1 ", User.class);
            List<User>listEmails1 = emails1.getResultList();
            for (User u : listEmails1)
            {
                sendEmailS(u.getCorreo(), resetPasswordLink1);
            }

        }
        catch (Exception e){
            return false;
        }

        return true;
    }

    public void sendEmailG(String recipientEmail, String link) {
        String subject = "Reporte cargue de Rechazos Stage y Cuentas";

        String content = "<p>Se ha habilitado el proceso de creación de cuentas.</p>"
                + "<p>Por favor siga los siguientes pasos:</p>"
                + "<p>1) Descargue la plantilla Excel.</p>"
                + "<p>2) Diligencie la información en la plantilla formato Excel</p>"
                + "<p>3) Realice su validación y confirme con el bóton.</p>"

                + "<p>Ingrese al siguiente enlace</p>"
                + "<p><a href=\"" + link + "\">Click Aquí</a></p>"
                + "<br>"
                + "<p>Ignore este correo si ya ha realizado el proceso.</p>";

        sendEmailService.sendEmail(recipientEmail, subject, content);
    }

    public void sendEmailS(String recipientEmail, String link) {
        String subject = "Reporte cargue de Rechazos Stage y Cuentas";

        String content = "<p>Se ha habilitado el proceso de ajuste segmentos rechazos.</p>"
                + "<p>Ingrese al siguiente enlace</p>"
                + "<p><a href=\"" + link + "\">Click Aquí</a></p>"
                + "<br>"
                + "<p>Ignore este correo si ya ha realizado el proceso.</p>";

        sendEmailService.sendEmail(recipientEmail, subject, content);
    }
}
