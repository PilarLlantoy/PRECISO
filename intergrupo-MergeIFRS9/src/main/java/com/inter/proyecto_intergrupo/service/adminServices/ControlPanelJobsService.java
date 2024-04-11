package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Service
@Transactional
public class ControlPanelJobsService {

    @Autowired
    ControlPanelJobsRepository controlPanelJobsRepository;

    @PersistenceContext
    EntityManager entityManager;

    public ControlPanelJobsService() {

    }

    public List<ControlPanelJobs> findAll() {
        return controlPanelJobsRepository.findAll();
    }

    public ControlPanelJobs findByIdJob(int id) {
        return controlPanelJobsRepository.findByIdJob(id);
    }

    public void changeState(ControlPanelJobs control) {
        control.setEstado(control.getEstado() != true);
        controlPanelJobsRepository.save(control);
    }

    public void save(ControlPanelJobs control) {
        controlPanelJobsRepository.save(control);
    }
}
