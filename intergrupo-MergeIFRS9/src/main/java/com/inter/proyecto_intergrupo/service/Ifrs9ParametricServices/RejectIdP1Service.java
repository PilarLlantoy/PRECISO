package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP1;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RejectionIdP1Repository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GarantBankRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class RejectIdP1Service {

    @Autowired
    private RejectionIdP1Repository rejectionIdP1Repository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public RejectIdP1Service(RejectionIdP1Repository rejectionIdP1Repository, AuditRepository auditRepository) {
        this.rejectionIdP1Repository = rejectionIdP1Repository;
        this.auditRepository = auditRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file,User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows,user);
            String[] temporal= list.get(0);
            if(temporal[2].equals("0"))
            {
                getRows(rows1,user);
                auditCode("Inserción archivo Identificación Cuenta",user);
            }else{
                auditCode("Falla inserción archivo Identificación Cuenta",user);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";

                DataFormatter formatter = new DataFormatter();
                String cellInicial = formatter.formatCellValue(row.getCell(0)).trim();
                String cellAsignacion = formatter.formatCellValue(row.getCell(1)).trim();
                String cellTipoCuenta = formatter.formatCellValue(row.getCell(2)).trim();
                String cellLineaInicial = formatter.formatCellValue(row.getCell(3)).trim();
                String cellLineaCant = formatter.formatCellValue(row.getCell(4)).trim();
                String cellSegmentoInicial = formatter.formatCellValue(row.getCell(5)).trim();
                String cellSegmentoCant = formatter.formatCellValue(row.getCell(6)).trim();
                String cellStageInicial = formatter.formatCellValue(row.getCell(7)).trim();
                String cellStageCant = formatter.formatCellValue(row.getCell(8)).trim();

                log[0]=String.valueOf(row.getRowNum());

                try
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)"; Integer.parseInt(cellLineaInicial);
                    log[1]=CellReference.convertNumToColString(4)+" - (5)"; Integer.parseInt(cellLineaCant);
                    log[1]=CellReference.convertNumToColString(5)+" - (6)"; Integer.parseInt(cellSegmentoInicial);
                    log[1]=CellReference.convertNumToColString(6)+" - (7)"; Integer.parseInt(cellSegmentoCant);
                    log[1]=CellReference.convertNumToColString(7)+" - (8)"; Integer.parseInt(cellStageInicial);
                    log[1]=CellReference.convertNumToColString(8)+" - (9)"; Integer.parseInt(cellStageCant);
                }
                catch (Exception e){
                    e.printStackTrace();
                    log[2]="false";
                    log[3]="Falló Tipo de dato debe ser Númerico";
                    fail++;
                    lista.add(log);
                    continue;
                }

                if(cellInicial.length()==0 || cellInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Inicial Cuenta no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellAsignacion.length()==0 || cellAsignacion.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Asignacion no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoCuenta.length()==0 ||cellTipoCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Tipo Cuenta no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellLineaInicial.length()==0 ||cellLineaInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló Línea Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellLineaCant.length()==0 ||cellLineaCant.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló Línea Cantidad no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellSegmentoInicial.length()==0 ||cellSegmentoInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló Segmento Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellSegmentoCant.length()==0 ||cellSegmentoCant.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló Segmento Cantidad no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellStageInicial.length()==0 ||cellStageInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló Stage Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellStageCant.length()==0 ||cellStageCant.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(8)+" - (9)";
                    log[2]="false";
                    log[3]="Falló Stage Cantidad no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="I CUENTA";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Identificación Cuenta",user);
        }
        return lista;
    }

    public void getRows(Iterator<Row> rows,User user) {
        XSSFRow row;
        int firstRow=1;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellInicial = formatter.formatCellValue(row.getCell(0)).trim();
                String cellAsignacion = formatter.formatCellValue(row.getCell(1)).trim();
                String cellTipoCuenta = formatter.formatCellValue(row.getCell(2)).trim();
                String cellLineaInicial = formatter.formatCellValue(row.getCell(3)).trim();
                String cellLineaCant = formatter.formatCellValue(row.getCell(4)).trim();
                String cellSegmentoInicial = formatter.formatCellValue(row.getCell(5)).trim();
                String cellSegmentoCant = formatter.formatCellValue(row.getCell(6)).trim();
                String cellStageInicial = formatter.formatCellValue(row.getCell(7)).trim();
                String cellStageCant = formatter.formatCellValue(row.getCell(8)).trim();

                RejectionIdP1 rejectionIdP1 = new RejectionIdP1();
                rejectionIdP1.setInicialCuenta(cellInicial);
                rejectionIdP1.setAsignacion(cellAsignacion);
                rejectionIdP1.setTipoCuenta(cellTipoCuenta);
                rejectionIdP1.setLineaInicial(Integer.parseInt(cellLineaInicial));
                rejectionIdP1.setLineaCantidad(Integer.parseInt(cellLineaCant));
                rejectionIdP1.setSegmentoInicial(Integer.parseInt(cellSegmentoInicial));
                rejectionIdP1.setSegmentoCantidad(Integer.parseInt(cellSegmentoCant));
                rejectionIdP1.setStageInicial(Integer.parseInt(cellStageInicial));
                rejectionIdP1.setStageCantidad(Integer.parseInt(cellStageCant));
                rejectionIdP1Repository.save(rejectionIdP1);

            }
        }
        auditCode("Carga masiva apartado Identificación Cuenta realizada exitosamente",user);
    }

    public List<RejectionIdP1> findAll(){
        return rejectionIdP1Repository.findAll();
    }

    public boolean insertRejectIdP1(RejectionIdP1 toInsert){

        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_identificacion_rechazos_p1 WHERE inicial_cuenta = ?");
        verify.setParameter(1,toInsert.getInicialCuenta());

        if(verify.getResultList().isEmpty()){
            try {
                rejectionIdP1Repository.save(toInsert);
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return state;
    }

    public void auditCode (String info,User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Identificación Cuenta");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public RejectionIdP1 findByInicialCuenta(String inicial){
        return rejectionIdP1Repository.findByInicialCuenta(inicial);
    }

    public RejectionIdP1 modifyRejectIdP1(RejectionIdP1 toModify,String id, User user){
        if(toModify.getInicialCuenta()!=id)
            rejectionIdP1Repository.deleteById(id);
        auditCode("Modificacion registro tabla Identificación Cuenta",user);
        return rejectionIdP1Repository.save(toModify);
    }

    public RejectionIdP1 saveRejection(RejectionIdP1 rejectionIdP1){
        return rejectionIdP1Repository.save(rejectionIdP1);
    }

    public void removeRejectionIdP1(String id, User user){
        rejectionIdP1Repository.deleteById(id);
        auditCode("Eliminar registro tabla Banco garante",user);
    }

    public void clearRejectionIdP1(User user){
        rejectionIdP1Repository.deleteAll();
        auditCode("Limpiar tabla Banco garante",user);
    }

    public Page<RejectionIdP1> getAll(Pageable pageable){
        return rejectionIdP1Repository.findAll(pageable);
    }

    public List<RejectionIdP1> findByFilter(String value, String filter) {
        List<RejectionIdP1> list=new ArrayList<RejectionIdP1>();
        switch (filter)
        {
            case "Inicial Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.inicial_cuenta LIKE ?", RejectionIdP1.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Asignación":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.asignacion LIKE ?", RejectionIdP1.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Tipo Cuenta":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.tipo_cuenta LIKE ?", RejectionIdP1.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Inicial Línea":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.linea_inicial LIKE ?", RejectionIdP1.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Cantidad Línea":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.linea_cantidad LIKE ?", RejectionIdP1.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Inicial Segmento":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.segmento_inicial LIKE ?", RejectionIdP1.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Cantidad Segmento":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.segmento_cantidad LIKE ?", RejectionIdP1.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Inicial Stage":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.stage_inicial LIKE ?", RejectionIdP1.class);
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
            case "Cantidad Stage":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p1 as em " +
                        "WHERE em.stage_cantidad LIKE ?", RejectionIdP1.class);
                query7.setParameter(1, value);

                list= query7.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
