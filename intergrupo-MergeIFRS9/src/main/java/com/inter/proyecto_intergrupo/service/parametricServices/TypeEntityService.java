package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdRepository;
import com.inter.proyecto_intergrupo.repository.parametric.TypeEntityRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
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
import java.util.*;

@Service
@Transactional
public class TypeEntityService {

    @Autowired
    private TypeEntityRepository typeEntityRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public TypeEntityService(TypeEntityRepository typeEntityRepository) {
        this.typeEntityRepository = typeEntityRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                clearTypeEntity(user);
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Se carga tabla Tipo de Entidad");
                insert.setCentro(user.getCentro());
                insert.setComponente("Paramétricas");
                insert.setFecha(today);
                insert.setInput("Tipo de Entidad");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else
            {
                Audit insert = new Audit();
                Date today = new Date();
                insert.setAccion("Fallo al carga tabla Tipo de Entidad");
                insert.setCentro(user.getCentro());
                insert.setComponente("Paramétricas");
                insert.setFecha(today);
                insert.setInput("Tipo de Entidad");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        String[] log=new String[3];
        log[0]="0";
        log[1]="0";
        log[2]="false";
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(0));
                String cellNit = formatter.formatCellValue(row.getCell(1));
                String cellContraparte = formatter.formatCellValue(row.getCell(2));
                String cellIntergrupo = formatter.formatCellValue(row.getCell(3));
                String cellTipoEntidad = formatter.formatCellValue(row.getCell(4));
                String cellEliminacion = formatter.formatCellValue(row.getCell(5));

                List<String> validate= List.of("SI","NO","SÍ");

                if(cellTipoContraparte.isEmpty() || cellTipoContraparte.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellTipoEntidad.isEmpty() || cellTipoEntidad.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="5";
                    log[2]="false";
                    break;
                }
                else if(cellEliminacion.isEmpty() || cellEliminacion.isBlank() || !validate.contains(cellEliminacion.toUpperCase(Locale.ROOT)))
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="6";
                    log[2]="false";
                    break;
                }
                else
                {
                    log[2]="true";
                }
            }
        }
        lista.add(log);
        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        ArrayList lista= new ArrayList();
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();

            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellTipoContraparte = formatter.formatCellValue(row.getCell(0));
                String cellNit = formatter.formatCellValue(row.getCell(1));
                String cellContraparte = formatter.formatCellValue(row.getCell(2));
                String cellIntergrupo = formatter.formatCellValue(row.getCell(3)).toUpperCase(Locale.ROOT);
                String cellTipoEntidad = formatter.formatCellValue(row.getCell(4));
                String cellEliminacion = formatter.formatCellValue(row.getCell(5)).toUpperCase(Locale.ROOT);

                log[0] = cellTipoContraparte+" - "+cellTipoEntidad;

                TypeEntity type = new TypeEntity();
                type.setTipoContraparte(cellTipoContraparte);
                type.setNit(cellNit);
                type.setContraparte(cellContraparte);
                if(cellIntergrupo.equals("NO")) {
                    type.setIntergrupo(false);
                }
                else if(cellIntergrupo.length()==2)
                {
                    type.setIntergrupo(true);
                }
                type.setTipoEntidad(cellTipoEntidad);
                if(cellEliminacion.equals("NO")) {
                    type.setEliminacion(false);
                }
                else
                {
                    type.setEliminacion(true);
                }
                typeEntityRepository.save(type);
                log[1] = "Registro insertado exitosamente.";
                lista.add(log);
            }
        }
        return lista;
    }

    public List<TypeEntity> findAll(){
        return typeEntityRepository.findAll();
    }

    public TypeEntity modifyTypeEntity(TypeEntity toModify,String id, User user){
        Date date=new Date();
        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se modifica un registro Tipo de Entidad");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas");
        insert.setFecha(today);
        insert.setInput("Tipo de Entidad");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        return typeEntityRepository.save(toModify);
    }

    public TypeEntity saveTypeEntity(TypeEntity type, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se agrega un registro Tipo de Entidad");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas");
        insert.setFecha(today);
        insert.setInput("Tipo de Entidad");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        return typeEntityRepository.save(type);
    }

    public void removeTypeEntity(Long id, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se elimina un registro Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        typeEntityRepository.deleteById(id);
    }

    public void clearTypeEntity(User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se eliminan los registros Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        typeEntityRepository.deleteAll();
    }

    public Page<TypeEntity> getAll(Pageable pageable){
        return typeEntityRepository.findAll(pageable);
    }

    public List<TypeEntity> findByFilter(String value, String filter) {
        List<TypeEntity> list=new ArrayList<TypeEntity>();
        switch (filter)
        {
            case "NIT":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_entidad as em " +
                        "WHERE em.nit LIKE ?", TypeEntity.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Contraparte":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_entidad as em " +
                        "WHERE em.contraparte LIKE ?", TypeEntity.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Tipo Contraparte":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_entidad as em " +
                        "WHERE em.tipo_contraparte LIKE ?", TypeEntity.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Intergrupo":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_entidad as em " +
                        "WHERE em.intergrupo LIKE ?", TypeEntity.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Tipo Entidad":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_entidad as em " +
                        "WHERE em.tipo_entidad LIKE ?", TypeEntity.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Eliminación":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_entidad as em " +
                        "WHERE em.eliminacion LIKE ?", TypeEntity.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

    public TypeEntity findTypeEntityById(Long id)
    {
        return typeEntityRepository.findByIdTipoEntidad(id);
    }
}
