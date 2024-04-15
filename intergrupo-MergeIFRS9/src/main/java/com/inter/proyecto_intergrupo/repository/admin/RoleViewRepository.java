package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.RoleView;
import com.inter.proyecto_intergrupo.model.admin.RoleViewId;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoleViewRepository extends CrudRepository<RoleView,RoleViewId> {


    List<RoleView> findAll();

    RoleView findByIdRolView(RoleViewId idRolView);
}
