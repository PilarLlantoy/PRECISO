package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
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

@Service
@Transactional
public class CountryService {

    @Autowired
    private final CountryRepository countryRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public CountryService(CountryRepository countryRepository) {
        this.countryRepository = countryRepository;
    }

    public List <Country> findAll(){return countryRepository.findAllByOrderByNombreAsc();}
    public List<Country> findAllActiveCountries() {
        return countryRepository.findByEstado(true);
    }

    public Country findCountryById(int id){
        return countryRepository.findAllById(id);
    }

    public List<Country>  findCountryByName(String nombre){
        return countryRepository.findAllByNombreIgnoreCase(nombre);
    }


    public void modifyCountry(Country toModify,String id, User user){

        Country toInsert = new Country();
        toInsert.setId(Integer.valueOf((toModify.getId()+"").toUpperCase(Locale.ROOT)));
        toInsert.setNombre(toModify.getNombre().toUpperCase(Locale.ROOT));
        Query query = entityManager.createNativeQuery("UPDATE preciso_paises SET id_pais = ? , nombre_pais = ? " +
                "WHERE id_pais = ?", Country.class);
        query.setParameter(1, toInsert.getId() );
        query.setParameter(2, toInsert.getNombre());
        query.setParameter(3, id);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro Paises");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Tabla Paises");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public Country modificarCountry(Country pais){
       countryRepository.save(pais);
       return pais;
    }

    public void removeCountry(String id, User user){
        Query query = entityManager.createNativeQuery("DELETE from preciso_paises " +
                "WHERE id_pais = ?", Country.class);
        query.setParameter(1, id );
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro Paises");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Tabla Paises");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public void clearCountry(User user){
        //currencyRepository.deleteAll();
        Query query = entityManager.createNativeQuery("DELETE from preciso_paises", Country.class);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Paises");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Tabla Paises");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Page<Country> getAll(Pageable pageable){

        return countryRepository.findAll(pageable);
    }

    public List<Country> findByFilter(String value, String filter) {
        List<Country> list=new ArrayList<Country>();
        switch (filter) {
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_paises as em " +
                        "WHERE em.id_pais LIKE ?", Country.class);
                query.setParameter(1, "%"+value+"%" );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_paises as em " +
                        "WHERE em.nombre_pais LIKE ?", Country.class);
                query0.setParameter(1, "%"+value+"%");

                list= query0.getResultList();
                break;
            case "Sigla":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_paises as em " +
                        "WHERE em.sigla_pais LIKE ?", Country.class);
                query2.setParameter(1, "%"+value+"%");
                list= query2.getResultList();
                break;
            case "Estado":
                Boolean valor = true;
                if (value.substring(0,1).equalsIgnoreCase("i"))
                    valor = false;
                Query query3 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_paises as em WHERE em.estado_pais = ?", Country.class);
                query3.setParameter(1, valor);
                list= query3.getResultList();
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
        insert.setInput("Países");
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
                loadAudit(user,"Cargue exitoso plantilla Países");
            else
                loadAudit(user,"Cargue Fallido plantilla Países");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Country> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutivo=0;
                    DataFormatter formatter = new DataFormatter();
                    String cellNombre = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellSigla= formatter.formatCellValue(row.getCell(consecutivo++)).trim().toUpperCase();

                    if (cellNombre.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellSigla.trim().length() != 2) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Sigla debe contener dos carcteres.";
                        lista.add(log);
                    }

                    if (lista.isEmpty()) {
                        Country country = new Country();
                        List<Country> countrySearch= countryRepository.findAllByNombreIgnoreCase(cellNombre);
                        if(!countrySearch.isEmpty())
                            country= countrySearch.get(0);
                        country.setNombre(cellNombre);
                        country.setSigla(cellSigla);
                        country.setEstado(true);
                        toInsert.add(country);
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
            countryRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }
}
