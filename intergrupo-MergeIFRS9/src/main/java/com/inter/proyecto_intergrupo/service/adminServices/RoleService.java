package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.repository.admin.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.stream.Collectors; //nuevo

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RoleService {
    private final RoleRepository roleRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll(){return roleRepository.findAll();}
    public Role findRole(String role){
        return roleRepository.findByNombre(role);
    }
    public Role findRoleById(int id){return roleRepository.findById(id);}

    public Role saveRole(Role role, ArrayList<View> roleViews){
        role.setNombre(role.getNombre());
        role.setEstado(role.isEstado());
        role.setVistas(roleViews);

        return roleRepository.save(role);
    }

    public Role modifyRole(Role role, ArrayList<View> roleViews){
        role.setId(role.getId());
        role.setNombre(role.getNombre());
        role.setEstado(role.isEstado());
        role.setVistas(roleViews);

        return roleRepository.save(role);
    }

    //NUEVOOO
    public void registrarNuevasVistas(Role role, List<View> nuevasVistas, String menu) {
        // Obtener el objeto Role de la base de datos
        Role roleDB = roleRepository.findById(role.getId());
        if(roleDB.getNombre()!=role.getNombre()) roleDB.setNombre(role.getNombre());
        roleDB.setEstado(true);
        //if(roleDB.isEstado()!=role.isEstado() && role.isEstado()) roleDB.setEstado(role.isEstado());
        System.out.println("HOLA");

        if (roleDB != null){
            System.out.println("roleDB != null");

            List<View> vistasExistentes = null;
            if(menu!=null){
                // Obtener las vistas existentes del objeto Role sin las del menu seleccionado
                vistasExistentes = roleDB.getVistas().stream()
                    .filter(view -> !(menu.equals(view.getMenuPrincipal())))
                    .collect(Collectors.toList());
                System.out.println("menu!=null");
                System.out.println(vistasExistentes.isEmpty());


                // Agregar las nuevas vistas a las vistas existentes
                if(!nuevasVistas.isEmpty() && vistasExistentes!=null) {
                    vistasExistentes.addAll(nuevasVistas);
                    System.out.println("nuevasVistas!=null");
                }

                // Actualizar las vistas del objeto Role con la lista combinada
                if(vistasExistentes!=null) {
                    roleDB.setVistas(vistasExistentes);
                    System.out.println("vistas existentes != null");
                    System.out.println(roleDB.getVistas());
                }

                }

            // Guardar el objeto Role actualizado en la base de datos
            roleRepository.save(roleDB);
        }
    }
}
