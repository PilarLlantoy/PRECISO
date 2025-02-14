package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Typification;
import com.inter.proyecto_intergrupo.model.parametric.TypificationConcil;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.TypificationConcilRepository;
import com.inter.proyecto_intergrupo.repository.parametric.TypificationRepository;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class TypificationService {

    @Autowired
    private TypificationRepository typificationRepository;

    @Autowired
    private ConciliationRepository conciliationRepository;

    @Autowired
    private ConciliationService conciliationService;

    @Autowired
    private TypificationConcilRepository typificationConcilRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public TypificationService(TypificationRepository typificationRepository) {
        this.typificationRepository = typificationRepository;
    }

    public List <Typification> findAll(){return typificationRepository.findAll();}
    public List<Typification> findAllActiveCountries() {
        return typificationRepository.findByEstado(true);
    }

    public Typification findById(Long id){
        return typificationRepository.findAllById(id);
    }

    public List<Typification> findByDetalle(String nombre){
        return typificationRepository.findAllByDetalleIgnoreCase(nombre);
    }

    public Typification modificarTypification(Typification tipificacion){
        typificationRepository.save(tipificacion);
       return tipificacion;
    }

    public void removeTypification(Long id, User user){
        typificationRepository.deleteById(id);
        loadAudit(user,"Remover registro Tipificación "+id);
    }

    public void clearTypification(User user){
        typificationRepository.deleteAll();
        loadAudit(user,"Limpiar tabla tipificaciones");
    }

    public Page<Typification> getAll(Pageable pageable){

        return typificationRepository.findAll(pageable);
    }

    public List<Typification> findByFilter(String value, String filter) {
        List<Typification> list=new ArrayList<Typification>();
        switch (filter) {
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipificacion as em " +
                        "WHERE em.id_tipificacion LIKE ?", Typification.class);
                query.setParameter(1, "%"+value+"%" );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_tipificacion as em " +
                        "WHERE em.detalle_tipificacion LIKE ?", Typification.class);
                query0.setParameter(1, "%"+value+"%");

                list= query0.getResultList();
                break;
            case "Estado":
                Boolean valor = true;
                if (value.substring(0,1).equalsIgnoreCase("i"))
                    valor = false;
                Query query3 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_tipificacion as em WHERE em.estado_tipificacion = ?", Typification.class);
                query3.setParameter(1, valor);
                list= query3.getResultList();
                break;
            case "Aplica Conciliación":
                Boolean valor1 = true;
                if (value.substring(0,1).equalsIgnoreCase("n"))
                    valor1 = false;
                Query query4 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_tipificacion as em WHERE em.aplica_concil = ?", Typification.class);
                query4.setParameter(1, valor1);
                list= query4.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Prametros Generales");
        insert.setFecha(today);
        insert.setInput("Tipificaciones");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
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
                loadAudit(user,"Cargue exitoso plantilla tipificaciones");
            else
                loadAudit(user,"Cargue Fallido plantilla tipificaciones");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Typification> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutivo=0;
                    DataFormatter formatter = new DataFormatter();
                    String cellNombre = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellAplicaConciliacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim().toUpperCase();

                    if (cellNombre.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellAplicaConciliacion.equalsIgnoreCase("SI") && !cellAplicaConciliacion.equalsIgnoreCase("NO") ) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Aplica Conciliación debe contener un SI o NO.";
                        lista.add(log);
                    }

                    if (lista.isEmpty()) {
                        Typification data = new Typification();
                        List<Typification> search= typificationRepository.findAllByDetalleIgnoreCase(cellNombre);
                        if(!search.isEmpty())
                            data= search.get(0);
                        data.setDetalle(cellNombre);
                        data.setAplicaConcil(cellAplicaConciliacion.equalsIgnoreCase("SI"));
                        data.setEstado(true);
                        toInsert.add(data);
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
            typificationRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public List<Integer> obtenerSeleccionadasPorTipificacion(Long idTipificacion) {
        Query query = entityManager.createNativeQuery("SELECT em.id_conciliacion FROM preciso_tipificacion_concil as em " +
                "WHERE em.id_tipificaciones = ?");
        query.setParameter(1, idTipificacion);
        return query.getResultList();
    }
    public List<Map<String, Object>> obtenerConciliaciones(Long idTipificacion) {
        List<Conciliation> todas = conciliationService.findAllActive();
        List<Integer> seleccionadas = obtenerSeleccionadasPorTipificacion(idTipificacion);
        return todas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", c.getId());
            map.put("nombre", c.getNombre());
            map.put("seleccionado", seleccionadas.contains(c.getId()));
            return map;
        }).collect(Collectors.toList());
    }
    public void guardarConciliaciones(Long idTipificacion, List<Integer> conciliaciones) {
        typificationConcilRepository.deleteByTipificacion(typificationRepository.findAllById(idTipificacion));
        for (Integer id:conciliaciones) {
            TypificationConcil typificationConcil = new TypificationConcil();
            typificationConcil.setTipificacion(typificationRepository.findAllById(idTipificacion));
            typificationConcil.setConciliacion(conciliationRepository.findAllById(id));
            typificationConcilRepository.save(typificationConcil);
        }
    }
}
