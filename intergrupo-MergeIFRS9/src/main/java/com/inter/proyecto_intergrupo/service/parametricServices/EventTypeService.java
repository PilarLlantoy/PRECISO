package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.EventTypeRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.springframework.data.domain.Page;

@Service
@Transactional
public class EventTypeService {

    @Autowired
    private final EventTypeRepository eventTypeRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public EventTypeService(EventTypeRepository eventTypeRepository) {
        this.eventTypeRepository = eventTypeRepository;
    }

    public List<EventType> findAll(){return eventTypeRepository.findAll();}
    public List<EventType> findAllActive() {
        return eventTypeRepository.findByEstado(true);
    }


    public List<EventType> findByName(String nombre){
        return eventTypeRepository.findAllByNombreIgnoreCase(nombre);
    }


    public EventType findAllById(int id){
        return eventTypeRepository.findAllById(id);
    }

    public EventType modificar(EventType pais){
        eventTypeRepository.save(pais);
       return pais;
    }

    public Page<EventType> getAll(Pageable pageable){
        return eventTypeRepository.findAll(pageable);
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Prametros Generales");
        insert.setFecha(today);
        insert.setInput("Tipos de Eventos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<EventType> findByFilter(String value, String filter) {
        List<EventType> list=new ArrayList<EventType>();
        switch (filter)
        {
            case "Estado":
                Boolean valor = true;
                if (value.substring(0,1).equalsIgnoreCase("i"))
                    valor = false;
                Query quer = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_tipo_evento as em WHERE em.estado_tipo_evento = ?", EventType.class);
                quer.setParameter(1, valor);
                list = quer.getResultList();
                break;
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_evento as em " +
                        "WHERE em.id_tipo_evento LIKE ?", EventType.class);
                query.setParameter(1, "%"+value+"%" );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipo_evento as em " +
                        "WHERE em.nombre_tipo_evento LIKE ? ", EventType.class);
                query0.setParameter(1, "%"+value+"%");
                list = query0.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue exitoso plantilla Tipos de Eventos");
            else
                loadAudit(user,"Cargue Fallido plantilla Tipos de Eventos");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<EventType> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutivo=0;
                    DataFormatter formatter = new DataFormatter();
                    String cellNombre = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellValidaCruce= formatter.formatCellValue(row.getCell(consecutivo++)).trim();

                    if (cellNombre.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellValidaCruce.trim().equalsIgnoreCase("si") && !cellValidaCruce.trim().equalsIgnoreCase("no")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Valida Cruce debe contener un valor de 'Si' o 'No'.";
                        lista.add(log);
                    }

                    if (lista.isEmpty()) {
                        EventType eventType = new EventType();
                        List<EventType> eventTypeSerach= eventTypeRepository.findAllByNombreIgnoreCase(cellNombre);
                        if(!eventTypeSerach.isEmpty())
                            eventType= eventTypeSerach.get(0);
                        eventType.setNombre(cellNombre);
                        eventType.setValidaCruce(cellValidaCruce.equalsIgnoreCase("si") ? true : false);
                        eventType.setEstado(true);
                        toInsert.add(eventType);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 5) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            eventTypeRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }
}
