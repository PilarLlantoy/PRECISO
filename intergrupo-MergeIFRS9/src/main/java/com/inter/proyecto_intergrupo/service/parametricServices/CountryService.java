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

    public Country findCountry(String pais){
        return countryRepository.findAllById(pais);
    }

    public Country findCountryById(String id){
        return countryRepository.findAllById(id);
    }

    public void modifyCountry(Country toModify,String id, User user){

        Country toInsert = new Country();
        toInsert.setId(toModify.getId().toUpperCase(Locale.ROOT));
        toInsert.setNombre(toModify.getNombre().toUpperCase(Locale.ROOT));
        Query query = entityManager.createNativeQuery("UPDATE nexco_paises SET id_pais = ? , nombre_pais = ? " +
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
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public void removeCountry(String id, User user){
        Query query = entityManager.createNativeQuery("DELETE from nexco_paises " +
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
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public void clearCountry(User user){
        //currencyRepository.deleteAll();
        Query query = entityManager.createNativeQuery("DELETE from nexco_paises", Country.class);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Paises");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Tabla Paises");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Page<Country> getAll(Pageable pageable){

        return countryRepository.findAll(pageable);
    }

    public List<Country> findByFilter(String value, String filter) {
        List<Country> list=new ArrayList<Country>();
        switch (filter)
        {
            case "Código País":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_paises as em " +
                        "WHERE em.id_pais LIKE ?", Country.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Nombre País":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_paises as em " +
                        "WHERE em.nombre_pais LIKE ?", Country.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}
