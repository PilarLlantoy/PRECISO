package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Cargo;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.repository.admin.CargoRepository;
import com.inter.proyecto_intergrupo.repository.admin.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

    // Método para encontrar todos los roles con estado activo
    public List<Cargo> findAllActiveCargos() {
        return cargoRepository.findByEstado(true);
    }

    public Cargo actualizarCargo(Cargo cargo){
        return cargoRepository.save(cargo);
    }

    public Cargo findCargoByNombre(String name){return cargoRepository.findByNombre(name);}

}
