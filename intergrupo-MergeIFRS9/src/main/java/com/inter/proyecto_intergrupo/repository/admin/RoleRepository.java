package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.Role;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleRepository extends CrudRepository<Role,Integer> {
    Role findByNombre(String role);
    Role findById(int id);
    List<Role> findAll();

}
