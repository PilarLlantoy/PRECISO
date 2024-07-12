package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRepository;
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
public class ConciliationService {

    @Autowired
    private final ConciliationRepository conciliationRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ConciliationService(ConciliationRepository conciliationRepository) {
        this.conciliationRepository = conciliationRepository;
    }

    public List <Conciliation> findAll(){return conciliationRepository.findAllByOrderByNombreAsc();}
    public List<Conciliation> findAllActive() {
        return conciliationRepository.findByEstado(true);
    }

    public Conciliation findById(int id){
        return conciliationRepository.findAllById(id);
    }

    public Conciliation findByName(String nombre){
        return conciliationRepository.findAllByNombre(nombre);
    }

    public Conciliation modificarConciliacion(Conciliation conciliacion){
        conciliationRepository.save(conciliacion);
       return conciliacion;
    }
/*
    public void clearConciliacion(User user){
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
*/
}
