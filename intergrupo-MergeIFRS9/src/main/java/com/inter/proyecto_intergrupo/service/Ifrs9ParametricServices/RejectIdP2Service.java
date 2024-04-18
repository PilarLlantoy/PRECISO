package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP1;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RejectionIdP2;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RejectionIdP1Repository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RejectionIdP2Repository;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class RejectIdP2Service {

    @Autowired
    private RejectionIdP2Repository rejectionIdP2Repository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public RejectIdP2Service(RejectionIdP2Repository rejectionIdP2Repository, AuditRepository auditRepository) {
        this.rejectionIdP2Repository = rejectionIdP2Repository;
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
                auditCode("Inserción archivo Identificación Rechazos",user);
            }else{
                auditCode("Falla inserción archivo Identificación Rechazos",user);
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
                String cellLinea = formatter.formatCellValue(row.getCell(0)).trim();
                String cellSegmentos = formatter.formatCellValue(row.getCell(1)).trim();

                log[0]=String.valueOf(row.getRowNum());

                Pattern pat = Pattern.compile("^[1-9](,[1-9])*$");
                Matcher mat = pat.matcher(cellSegmentos);

                if(cellLinea.length()==0 || cellLinea.length()!=3)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Linea producto debe tener 3 carcateres";
                    fail++;
                    lista.add(log);
                }
                else if(cellSegmentos.length()==0 || cellSegmentos.length()>17 || !mat.matches())
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Segmentos no cumple con la separación de comas por digito (1,3,5,7) con tamaño maximo de 17";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RECHAZOS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Identificación Rechazos",user);
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
                String cellLinea = formatter.formatCellValue(row.getCell(0)).trim();
                String cellSegmentos = formatter.formatCellValue(row.getCell(1)).trim();

                RejectionIdP2 rejectionIdP2 = new RejectionIdP2();
                rejectionIdP2.setLineaProducto(cellLinea);
                rejectionIdP2.setSegmentos(cellSegmentos);
                rejectionIdP2Repository.save(rejectionIdP2);

            }
        }
        auditCode("Carga masiva apartado Identificación Rechazos realizada exitosamente",user);
    }

    public List<RejectionIdP2> findAll(){
        return rejectionIdP2Repository.findAll();
    }

    public boolean insertRejectIdP2(RejectionIdP2 toInsert){
        toInsert.setLineaProducto(String.format("%03d", Integer.parseInt(toInsert.getLineaProducto())));
        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_identificacion_rechazos_p2 WHERE linea_producto = ?");
        verify.setParameter(1,toInsert.getLineaProducto());

        if(verify.getResultList().isEmpty()){
            try {
                rejectionIdP2Repository.save(toInsert);
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
        insert.setInput("Identificación Rechazos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public RejectionIdP2 findByLineaProducto(String inicial){
        return rejectionIdP2Repository.findByLineaProducto(inicial);
    }

    public RejectionIdP2 modifyRejectIdP1(RejectionIdP2 toModify,String id, User user){
        if(toModify.getLineaProducto()!=id)
            rejectionIdP2Repository.deleteById(id);
        auditCode("Modificacion registro tabla Identificación Rechazos",user);
        return rejectionIdP2Repository.save(toModify);
    }

    public RejectionIdP2 saveRejection(RejectionIdP2 rejectionIdP2){
        return rejectionIdP2Repository.save(rejectionIdP2);
    }

    public void removeRejectionIdP2(String id, User user){
        rejectionIdP2Repository.deleteById(id);
        auditCode("Eliminar registro tabla Identificación Rechazos",user);
    }

    public void clearRejectionIdP2(User user){
        rejectionIdP2Repository.deleteAll();
        auditCode("Limpiar tabla Identificación Rechazos",user);
    }

    public Page<RejectionIdP2> getAll(Pageable pageable){
        return rejectionIdP2Repository.findAll(pageable);
    }

    public List<RejectionIdP2> findByFilter(String value, String filter) {
        List<RejectionIdP2> list=new ArrayList<RejectionIdP2>();
        switch (filter)
        {
            case "Línea Producto":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p2 as em " +
                        "WHERE em.linea_producto LIKE ?", RejectionIdP2.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Segmentos":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_identificacion_rechazos_p2 as em " +
                        "WHERE em.segmentos LIKE ?", RejectionIdP2.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
