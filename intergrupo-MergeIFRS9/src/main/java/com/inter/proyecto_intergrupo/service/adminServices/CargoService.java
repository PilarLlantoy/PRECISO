package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Cargo;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.repository.admin.CargoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class CargoService {
    private final CargoRepository cargoRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public CargoService(CargoRepository cargoRepository) {
        this.cargoRepository = cargoRepository;
    }

    public List<Cargo> findAll(){return cargoRepository.findAll();}
    public Cargo findCargoById(int id){ return cargoRepository.findById(id); }

    // Método para encontrar todos los cargos con estado activo
    public List<Cargo> findAllActiveCargos() {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_administracion_cargos WHERE activo_cargo = 1", Cargo.class);
        return query.getResultList();
    }

    // Encuentra los usuarios asociados a un rol por su ID de perfil
    public List<User> encontrarUsuarios(int idCargo) {
        Query query = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_usuarios WHERE id_cargo = ?", User.class);
        query.setParameter(1, idCargo);
        return query.getResultList();
    }

    public Cargo actualizarCargo(Cargo cargo){
        cargoRepository.save(cargo);
        return cargo;
    }

    public Cargo findCargoByNombre(String name){return cargoRepository.findByNombre(name);}

}
