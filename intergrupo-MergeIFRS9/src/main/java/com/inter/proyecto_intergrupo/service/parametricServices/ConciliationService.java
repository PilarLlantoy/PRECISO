package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.UserConciliation;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserConciliationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ConciliationRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
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
import java.util.stream.Collectors;

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
    private UserConciliationRepository userConciliationRepository;

    @Autowired
    private UserService userService;

    @Autowired
    public ConciliationService(ConciliationRepository conciliationRepository) {
        this.conciliationRepository = conciliationRepository;
    }

    public List <Conciliation> findAll(){return conciliationRepository.findAllByOrderByIdAsc();}
    public List<Conciliation> findAllActive() {
        return conciliationRepository.findByActivo(true);
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

    public List<Conciliation> findByFilter(String value, String filter) {
        List<Conciliation> list=new ArrayList<Conciliation>();
        switch (filter) {
            case "Estado":
                Boolean valor = true;
                if ("inactivo".equalsIgnoreCase(value)) {
                    valor = false;
                }
                Query quer = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_conciliaciones as em WHERE em.activo = ?", Conciliation.class);
                quer.setParameter(1, valor);
                list = quer.getResultList();
                break;
            case "Nombre":
                String sql = "SELECT em.* FROM preciso_conciliaciones as em WHERE em.nombre LIKE ? ";
                Query query0 = entityManager.createNativeQuery(sql, Conciliation.class);
                String likeValue = "%" + value + "%";
                query0.setParameter(1, likeValue);
                list = query0.getResultList();
                break;
            case "Sistema Fuente":
                String sql1 = "SELECT nv.* FROM preciso_conciliaciones as nv \n" +
                        "left join preciso_sistema_fuente b\n" +
                        "on nv.id_sf = b.id_sf WHERE  b.nombre_sf LIKE ?";
                Query query1 = entityManager.createNativeQuery(sql1, Conciliation.class);
                String likeValue2 = "%" + value + "%";
                query1.setParameter(1, likeValue2);
                list = query1.getResultList();
                break;
            case "Fuente Contable":
                String sql2 = "SELECT nv.* FROM preciso_conciliaciones as nv \n" +
                        "left join preciso_sistema_fuente b\n" +
                        "on nv.id_sfc = b.id_sf WHERE  b.nombre_sf LIKE ?";
                Query query2 = entityManager.createNativeQuery(sql2, Conciliation.class);
                String likeValue3 = "%" + value + "%";
                query2.setParameter(1, likeValue3);
                list = query2.getResultList();
                break;
            default:
                break;
        }

        return list;
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


    @Transactional
    public void generarRelacionUserConciliation(String usuario, List<String> titulares, List<String> backups) {
        try {
            User user = userService.findById(Integer.parseInt(usuario));
            userConciliationRepository.deleteByUsuarioId(Integer.parseInt(usuario));
            relacionPorCargo(user, titulares, UserConciliation.RoleConciliation.TITULAR);
            relacionPorCargo(user, backups, UserConciliation.RoleConciliation.BACKUP);
        }catch (Error e){
            e.printStackTrace();
        }

    }

    public void relacionPorCargo(User user, List<String> conciliations, UserConciliation.RoleConciliation role) {
        try {
            for (String concil : conciliations) {
                Conciliation conciliacion = conciliationRepository.findAllById(Integer.valueOf(concil));
                // Obtener las relaciones existentes de la conciliación
                List<UserConciliation> existingRoles = conciliacion.getUserConciliations()
                        .stream()
                        .filter(uc -> uc.getRol() == role)
                        .collect(Collectors.toList());

                // Validar si ya existe un usuario asignado como titular o backup
                if (role == UserConciliation.RoleConciliation.TITULAR && !existingRoles.isEmpty()) {
                    throw new IllegalStateException("La conciliación con ID " + conciliacion.getId() + " ya tiene un usuario titular asignado.");
                }

                if (role == UserConciliation.RoleConciliation.BACKUP && !existingRoles.isEmpty()) {
                    throw new IllegalStateException("La conciliación con ID " + conciliacion.getId() + " ya tiene un usuario backup asignado.");
                }

                // Crear la nueva relación usuario-conciliación
                UserConciliation userConciliation = UserConciliation.builder()
                        .usuario(user)
                        .conciliacion(conciliacion)
                        .rol(role)
                        .build();

                // Agregar la relación en ambas entidades
                conciliacion.getUserConciliations().add(userConciliation);
                user.getUserConciliations().add(userConciliation);

                // Guardar la relación en la base de datos
                userConciliationRepository.save(userConciliation);
                conciliationRepository.save(conciliacion);
            }
        }catch (Error e){
            e.printStackTrace();
        }

    }


}
