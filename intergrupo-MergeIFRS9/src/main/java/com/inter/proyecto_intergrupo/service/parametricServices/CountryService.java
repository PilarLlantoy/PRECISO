package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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

    public Country findCountryByName(String nombre){
        return countryRepository.findAllByNombre(nombre);
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
                query.setParameter(1, value );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_paises as em " +
                        "WHERE em.nombre_pais LIKE ?", Country.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Sigla":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_paises as em " +
                        "WHERE em.sigla_pais LIKE ?", Country.class);
                query2.setParameter(1, value);
                list= query2.getResultList();
                break;
            case "Estado":
                Boolean valor = true;
                if ("inactivo".equalsIgnoreCase(value)) valor = false;
                Query query3 = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_paises as em WHERE em.activo_pais = ?", Country.class);
                query3.setParameter(1, valor);
                list= query3.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}
