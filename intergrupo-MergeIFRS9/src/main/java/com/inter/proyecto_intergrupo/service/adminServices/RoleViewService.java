package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.RoleView;
import com.inter.proyecto_intergrupo.model.admin.RoleViewId;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.repository.admin.RoleRepository;
import com.inter.proyecto_intergrupo.repository.admin.RoleViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RoleViewService {
    private final RoleViewRepository roleViewRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public RoleViewService(RoleViewRepository roleViewRepository) {
        this.roleViewRepository = roleViewRepository;
    }

    public List<RoleView> findAll(){return roleViewRepository.findAll();}

    public RoleView findByViewId(Role role, View view){
        RoleViewId roleViewId = new RoleViewId();
        roleViewId.setRoleId(role.getId());
        roleViewId.setViewId(view.getId());
        return this.roleViewRepository.findByIdRolView(roleViewId);
    }

    public RoleView saveRoleView(RoleView roleView){
        return roleViewRepository.save(roleView);
    }

    public List<View> findViewsVer(int idRol){
        Query consulta = entityManager.createNativeQuery("select b.* from (SELECT * FROM NEXCO.[dbo].nexco_rol_vista where id_perfil = ? and p_visualizar=1) a\n" +
                "\t\t inner join NEXCO.[dbo].nexco_vistas b on a.id_vista=b.id_vista ", View.class);
        consulta.setParameter(1,idRol);
        return consulta.getResultList();
    }

    public List<View> findViewsModificar(int idRol){
        Query consulta = entityManager.createNativeQuery("select b.* from (SELECT * FROM NEXCO.[dbo].nexco_rol_vista where id_perfil = ? and p_modificar=1) a\n" +
                "\t\t inner join NEXCO.[dbo].nexco_vistas b on a.id_vista=b.id_vista ", View.class);
        consulta.setParameter(1,idRol);
        return consulta.getResultList();
    }
}
