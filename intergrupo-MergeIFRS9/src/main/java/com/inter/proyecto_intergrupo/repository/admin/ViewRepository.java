package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.View;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ViewRepository extends CrudRepository<View,Long> {
    View findByViewName(String name);
    List<View> findAll();

    //ENCONTRAR POR MENU SELECCIONADO
    List<View> findByMenuPrincipal(String menu_principal);
}
