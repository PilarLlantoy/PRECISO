package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ControlPanelJobsRepository extends JpaRepository<ControlPanelJobs,Integer> {
    List<ControlPanelJobs> findAll();
    ControlPanelJobs findByIdJob(int id);
}
