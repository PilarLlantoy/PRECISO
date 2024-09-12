package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.Campo;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CampoRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRouteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ConciliationRouteService {

    @Autowired
    private final ConciliationRouteRepository conciliationRouteRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ConciliationRouteService(ConciliationRouteRepository conciliationRouteRepository) {
        this.conciliationRouteRepository = conciliationRouteRepository;
    }

    public List<ConciliationRoute> findAllActive() {
        return conciliationRouteRepository.findByEstado(true);
    }

    public ConciliationRoute findById(int id){
        return conciliationRouteRepository.findAllById(id);
    }

    public ConciliationRoute findByName(String nombre){
        return conciliationRouteRepository.findAllByDetalle(nombre);
    }

    public ConciliationRoute modificar(ConciliationRoute croute){
        conciliationRouteRepository.save(croute);
       return croute;
    }

    /*
    @Transactional
    public void crearRutaC(String nombre, String ruta) {

        Optional<Integer> rutaIdOptional = conciliationRouteRepository.findRutaIdByRuta(ruta);
        int rutaId;
        if (rutaIdOptional.isPresent()) {
            rutaId = rutaIdOptional.get();
        } else {
            rutaId = conciliationRouteRepository.findMaxRutaId().orElse(0) + 1;
        }
        int codigo = conciliationRouteRepository.findMaxCodigoByRuta(ruta).orElse(0) + 1;

        ConciliationRoute newRutaC = new ConciliationRoute();
        newRutaC.setDetalle(nombre);
        newRutaC.setRuta(ruta);
        newRutaC.setRutaId(rutaId);
        newRutaC.setOrdenRuta(codigo);

        conciliationRouteRepository.save(newRutaC);
    }

     */



}
