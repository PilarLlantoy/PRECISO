package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ControlPanelIfrsService {

    @PersistenceContext
    EntityManager entityManager;

    public ControlPanelIfrsService() {

    }

    public List<ControlPanelIfrs> findAll()
    {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_cuadro_mando_ifrs as em where componente != 'RECHAZOS' order by orden,componente,input", ControlPanelIfrs.class);
        return query.getResultList();
    }

    public List<ControlPanelIfrs> findByIdCuadroMando(String input, String componente){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_cuadro_mando_ifrs as em WHERE " +
                " em.input = ? AND em.componente = ?", ControlPanelIfrs.class);
        query.setParameter(1, input);
        query.setParameter(2, componente);
        return query.getResultList();
    }

    public void changeState(ControlPanelIfrs control){
        if(control.getEstado()==true){
            control.setEstado(false);
        }
        else{
            control.setEstado(true);
        }
        Query query = entityManager.createNativeQuery("" +
                "UPDATE preciso_administracion_cuadro_mando_ifrs SET estado = ? " +
                "WHERE input = ? AND componente = ?", ControlPanelIfrs.class);
        query.setParameter(1, control.getEstado());
        query.setParameter(2, control.getInput());
        query.setParameter(3, control.getComponente());
        query.executeUpdate();
    }

    public void changeSemaforoInput(ControlPanelIfrs control){
        Query query = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando_ifrs SET semaforo_input = ? " +
                "WHERE input = ? AND componente = ?", ControlPanelIfrs.class);
        query.setParameter(1, control.getSemaforoInput());
        query.setParameter(2, control.getInput());
        query.setParameter(3, control.getComponente());
        query.executeUpdate();
    }

    public void changeSemaforoComponente(ControlPanelIfrs control){
        Query query = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando_ifrs SET semaforo_componente = ? " +
                "WHERE componente = ?", ControlPanelIfrs.class);
        query.setParameter(1, control.getSemaforoComponente());
        query.setParameter(2, control.getComponente());
        query.executeUpdate();
    }

    public void sendEmail(ControlPanelIfrs panel, User usuario) {

        panel.setSemaforoInput("PENDING");

        Query query = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando_ifrs SET semaforo_input = ? " +
                "WHERE input = ? AND componente = ?", ControlPanelIfrs.class);
        query.setParameter(1, panel.getSemaforoInput());
        query.setParameter(2, panel.getInput());
        query.setParameter(3, panel.getComponente());
        query.executeUpdate();
    }

    public List<ControlPanelIfrs> findByCPI(User user){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_cuadro_mando_ifrs as em WHERE em.componente = ? ORDER BY em.input", ControlPanelIfrs.class);
        query.setParameter(1, "RECHAZOS");
        return query.getResultList();
    }

    public void updateState(List<ControlPanelIfrs> controlPanelList){
        for (ControlPanelIfrs panel: controlPanelList) {
            Query query = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando_ifrs SET semaforo_input = ? , semaforo_componente = ?" +
                    " WHERE input = ? AND componente = ?", ControlPanelIfrs.class);
            query.setParameter(1, "FULL");
            query.setParameter(2, "FULL");
            query.setParameter(3, panel.getInput());
            query.setParameter(4, panel.getComponente());
            query.executeUpdate();
        }
    }

    public void save(ControlPanelIfrs control) {
        Query query = entityManager.createNativeQuery("INSERT INTO preciso_administracion_cuadro_mando_ifrs(input, componente, empresa, estado, semaforo_componente, semaforo_input) VALUES (?,?,?,?,?,?)", ControlPanelIfrs.class);
        query.setParameter(1, control.getInput());
        query.setParameter(2, control.getComponente());
        query.setParameter(3, control.getEmpresa());
        query.setParameter(4, false);
        query.setParameter(5, "EMPTY");
        query.setParameter(6, "EMPTY");
        query.executeUpdate();
    }

    public void changeAllStates(String action){
        if(action.equals("Enable")){
            Query enable = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando_ifrs " +
                    "SET estado = 1 ");
            enable.executeUpdate();
        } else {
            Query disable = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando_ifrs " +
                    "SET estado = 0 ");
            disable.executeUpdate();
        }
    }

    public void loadDates()
    {
        List<String> listInputsRechazos = List.of("CUENTA PROV", "CUENTA IMPUESTOS","CUENTA RECLASIFICACION","RISTRAS PROV","RISTRAS IMPUESTOS","RISTRAS RECLASIFICACION");

        for (String listItem:listInputsRechazos) {
            ControlPanelIfrs insert= new ControlPanelIfrs();
            insert.setComponente("RECHAZOS");
            insert.setEmpresa("N/A");
            insert.setInput(listItem);
            save(insert);
        }
    }
}
