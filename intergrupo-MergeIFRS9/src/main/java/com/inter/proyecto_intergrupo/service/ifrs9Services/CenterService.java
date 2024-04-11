package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.CenterRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class CenterService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final CenterRepository centerRepository;

    public CenterService(CenterRepository centerRepository) {
        this.centerRepository = centerRepository;
    }

    public List<Centers> findCenterbyOficina(String oficina){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                "WHERE em.oficina = ?",Centers.class);

        query.setParameter(1, oficina);
        return query.getResultList();
    }


    public void modifyCenter(Centers toModify,String oficina){
        Centers toInsert = new Centers();
        toInsert.setOficina(toModify.getOficina());
        toInsert.setTipo_unidad(toModify.getTipo_unidad());
        toInsert.setNomtip(toModify.getNomtip());
        toInsert.setClase_unidad(toModify.getClase_unidad());
        toInsert.setNombre_unidad(toModify.getNombre_unidad());
        toInsert.setDar(toModify.getDar());
        toInsert.setDug(toModify.getDug());
        toInsert.setTerritorio(toModify.getTerritorio());
        toInsert.setDireccion_regional(toModify.getDireccion_regional());
        toInsert.setArea_operativa(toModify.getArea_operativa());
        toInsert.setSuprarea(toModify.getSuprarea());
        toInsert.setFecha_cierre(toModify.getFecha_cierre());
        toInsert.setOfinegocio(toModify.getOfinegocio());
        toInsert.setFecha_apertura(toModify.getFecha_apertura());
        toInsert.setDomicilio(toModify.getDomicilio());
        toInsert.setTelefono(toModify.getTelefono());
        Query query = entityManager.createNativeQuery("UPDATE nexco_centers SET oficina = ? , tipo_unidad = ? , nomtip = ? , clase_unidad = ? , nombre_unidad = ? , dar = ? , dug = ? , territorio = ? , direccion_regional = ? , area_operativa = ? , suprarea = ? , fecha_cierre = ? , ofinegocio = ? , fecha_apertura = ? , domicilio = ? , telefono = ? " +
                "WHERE oficina = ? ", Centers.class);
        query.setParameter(1,toInsert.getOficina());
        query.setParameter(2,toInsert.getTipo_unidad());
        query.setParameter(3,toInsert.getNomtip());
        query.setParameter(4,toInsert.getClase_unidad());
        query.setParameter(5,toInsert.getNombre_unidad());
        query.setParameter(6,toInsert.getDar());
        query.setParameter(7,toInsert.getDug());
        query.setParameter(8,toInsert.getTerritorio());
        query.setParameter(9,toInsert.getDireccion_regional());
        query.setParameter(10,toInsert.getArea_operativa());
        query.setParameter(11,toInsert.getSuprarea());
        query.setParameter(12,toInsert.getFecha_cierre());
        query.setParameter(13,toInsert.getOfinegocio());
        query.setParameter(14,toInsert.getFecha_apertura());
        query.setParameter(15,toInsert.getDomicilio());
        query.setParameter(16,toInsert.getTelefono());
        query.setParameter(17, oficina );
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveCenter(Centers center){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_centers (oficina, tipo_unidad, nomtip, clase_unidad, nombre_unidad, dar, dug, territorio, direccion_regional, area_operativa, suprarea, fecha_cierre, ofinegocio, fecha_apertura, domicilio, telefono) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", Centers.class);
        query.setParameter(1,center.getOficina());
        query.setParameter(2,center.getTipo_unidad());
        query.setParameter(3,center.getNomtip());
        query.setParameter(4,center.getClase_unidad());
        query.setParameter(5,center.getNombre_unidad());
        query.setParameter(6,center.getDar());
        query.setParameter(7,center.getDug());
        query.setParameter(8,center.getTerritorio());
        query.setParameter(9,center.getDireccion_regional());
        query.setParameter(10,center.getArea_operativa());
        query.setParameter(11,center.getSuprarea());
        query.setParameter(12,center.getFecha_cierre());
        query.setParameter(13,center.getOfinegocio());
        query.setParameter(14,center.getFecha_apertura());
        query.setParameter(15,center.getDomicilio());
        query.setParameter(16,center.getTelefono());
        query.executeUpdate();
    }

    public void removeCenter(String oficina){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_centers WHERE oficina = ? ", Centers.class);
        query.setParameter(1, oficina);
        query.executeUpdate();
    }

    public void clearCenter(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_centers", Centers.class);
        query.executeUpdate();
    }

    public Page<Centers> getAll(Pageable pageable){
        List<Centers> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Centers> pageCenter = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageCenter;
    }

    public List<Centers> findByFilter(String value, String filter) {
        List<Centers> list=new ArrayList<Centers>();
        switch (filter)
        {
            case "Oficina":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.oficina LIKE ?", Centers.class);
                query1.setParameter(1, value );

                list= query1.getResultList();

                break;
            case "Tipo unidad":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.tipo_unidad LIKE ?", Centers.class);
                query2.setParameter(1, value );

                list= query2.getResultList();

                break;
            case "Nomtip":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.nomtip LIKE ?", Centers.class);
                query3.setParameter(1, value );

                list= query3.getResultList();

                break;
            case "Clase unidad":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.clase_unidad LIKE ?", Centers.class);
                query4.setParameter(1, value );

                list= query4.getResultList();

                break;
            case "Nombre unidad":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.nombre_unidad LIKE ?", Centers.class);
                query5.setParameter(1, value );

                list= query5.getResultList();

                break;
            case "Dar":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.dar LIKE ?", Centers.class);
                query6.setParameter(1, value );

                list= query6.getResultList();

                break;
            case "Dug":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.dug LIKE ?", Centers.class);
                query7.setParameter(1, value );

                list= query7.getResultList();

                break;
            case "Territorio":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.territorio LIKE ?", Centers.class);
                query8.setParameter(1, value );

                list= query8.getResultList();

                break;
            case "Direccion regional":
                Query query9 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.direccion_regional LIKE ?", Centers.class);
                query9.setParameter(1, value );

                list= query9.getResultList();

                break;
            case "Area operativa":
                Query query10 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.area_operativa LIKE ?", Centers.class);
                query10.setParameter(1, value );

                list= query10.getResultList();

                break;
            case "Suprarea":
                Query query11 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.suprarea LIKE ?", Centers.class);
                query11.setParameter(1, value );

                list= query11.getResultList();

                break;
            case "Fecha cierre":
                Query query12 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.fecha_cierre LIKE ?", Centers.class);
                query12.setParameter(1, value );

                list= query12.getResultList();

                break;
            case "Ofinegocio":
                Query query13 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.ofinegocio LIKE ?", Centers.class);
                query13.setParameter(1, value );

                list= query13.getResultList();

                break;
            case "Fecha apertura":
                Query query14 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.fecha_apertura LIKE ?", Centers.class);
                query14.setParameter(1, value );

                list= query14.getResultList();

                break;
            case "Domicilio":
                Query query15 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.domicilio LIKE ?", Centers.class);
                query15.setParameter(1, value );

                list= query15.getResultList();

                break;
            case "Telefono":
                Query query16 = entityManager.createNativeQuery("SELECT em.* FROM nexco_centers as em " +
                        "WHERE em.telefono LIKE ?", Centers.class);
                query16.setParameter(1, value );

                list= query16.getResultList();

                break;
            default:
                break;
        }
        return list;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, String period) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows1 = sheet.iterator();
            list=getRows(rows1,period);
        }
        return list;
    }

    public ArrayList getRows(Iterator<Row> rows, String period) {
        XSSFRow row;
        ArrayList lista= new ArrayList();
        int firstRow = 1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter();
                String cellOficina= formatter.formatCellValue(row.getCell(0));
                String cellTipoUnidad = formatter.formatCellValue(row.getCell(1));
                String cellNomTip= formatter.formatCellValue(row.getCell(2));
                String cellClaseUnidad = formatter.formatCellValue(row.getCell(3));
                String cellNombreUnidad= formatter.formatCellValue(row.getCell(4));
                String cellDar= formatter.formatCellValue(row.getCell(5));
                String cellDug = formatter.formatCellValue(row.getCell(6));
                String cellTerritorio = formatter.formatCellValue(row.getCell(7));
                String cellDireccionRegional= formatter.formatCellValue(row.getCell(8));
                String cellAreaOperativa = formatter.formatCellValue(row.getCell(9));
                String cellSuprarea = formatter.formatCellValue(row.getCell(10));
                String cellFechaCierre= formatter.formatCellValue(row.getCell(11));
                String cellOfinegocio = formatter.formatCellValue(row.getCell(12));
                String cellFechaApertura = formatter.formatCellValue(row.getCell(13));
                String cellDomicilio = formatter.formatCellValue(row.getCell(14));
                String cellTelefono = formatter.formatCellValue(row.getCell(15));

                if((cellOficina.isEmpty() || cellOficina.isBlank()) && (cellTipoUnidad.isEmpty() || cellTipoUnidad.isBlank())
                        && (cellNomTip.isEmpty() || cellNomTip.isBlank()) && (cellClaseUnidad.isEmpty() || cellClaseUnidad.isBlank()) && (cellNombreUnidad.isEmpty() || cellNombreUnidad.isBlank())
                        && (cellDar.isEmpty() || cellDar.isBlank()) && (cellDug.isEmpty() || cellDug.isBlank()) && (cellTerritorio.isEmpty() || cellTerritorio.isBlank())
                        && (cellDireccionRegional.isEmpty() || cellDireccionRegional.isBlank()) && (cellAreaOperativa.isEmpty() || cellAreaOperativa.isBlank()) && (cellSuprarea.isEmpty() || cellSuprarea.isBlank())
                        && (cellOfinegocio.isEmpty() || cellOfinegocio.isBlank()) && (cellFechaApertura.isEmpty() || cellFechaApertura.isBlank()) && (cellDomicilio.isEmpty() || cellDomicilio.isBlank()) && (cellTelefono.isEmpty() || cellTelefono.isBlank()))
                {
                    break;
                } else {
                    Centers centers = new Centers();
                    centers.setOficina(cellOficina);
                    centers.setTipo_unidad(cellTipoUnidad);
                    centers.setArea_operativa(cellAreaOperativa);
                    centers.setDar(cellDar);
                    centers.setDug(cellDug);
                    centers.setClase_unidad(cellClaseUnidad);
                    centers.setDireccion_regional(cellDireccionRegional);
                    centers.setFecha_cierre(cellFechaCierre);
                    centers.setNomtip(cellNomTip);
                    centers.setOfinegocio(cellOfinegocio);
                    centers.setSuprarea(cellSuprarea);
                    centers.setTerritorio(cellTerritorio);
                    centers.setNombre_unidad(cellNombreUnidad);
                    centers.setFecha_apertura(cellFechaApertura);
                    centers.setDomicilio(cellDomicilio);
                    centers.setTelefono(cellTelefono);
                    centerRepository.save(centers);
                    log[0] = cellOficina;
                    log[1] = "Registro actualizado exitosamente.";
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Centers> findAll(){
        return centerRepository.findAll();
    }
}
