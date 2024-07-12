package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.View;
import com.inter.proyecto_intergrupo.repository.admin.ViewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class ViewService {
    private ViewRepository viewRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public ViewService(ViewRepository viewRepository) {
        this.viewRepository = viewRepository;
    }

    public View findViewByName(String name){ return viewRepository.findByViewName(name);}

    public List<View> findAll(){
        Query query = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_vistas ORDER BY nombre", View.class);
        return query.getResultList();
    }

    public List<String> findAllPrincipal(){
        Query query = entityManager.createNativeQuery("SELECT nv.menu_principal FROM preciso_administracion_vistas AS nv GROUP BY nv.menu_principal");
        return query.getResultList();
    }

    //PARA BUSCAR VISTAS POR MENU

    public List<View> findByMenuPrincipal(String menu_principal){
        /*
        String queryString = "SELECT * FROM preciso_administracion_vistas WHERE menu_principal = :menu_principal ORDER BY nombre";
        if (menu_principal == null) {
            queryString = "SELECT * FROM preciso_administracion_vistas ORDER BY nombre";
        }
        Query query = entityManager.createNativeQuery(queryString, View.class);
        if (menu_principal != null) {
            query.setParameter("menu_principal", menu_principal);
        }
        */
        String queryString = "SELECT * FROM preciso_administracion_vistas WHERE menu_principal = :menu_principal ORDER BY nombre";
        Query query = entityManager.createNativeQuery(queryString, View.class);
        query.setParameter("menu_principal", menu_principal);
        return query.getResultList();
    }

    public View saveView(String name, String path){
        View view = new View();
        view.setViewName(name);
        view.setPath(path);

        return viewRepository.save(view);
    }

}
