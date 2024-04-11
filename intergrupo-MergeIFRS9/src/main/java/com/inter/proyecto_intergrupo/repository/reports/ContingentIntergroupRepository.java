package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.reports.ContingentIntergroup;
import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContingentIntergroupRepository extends JpaRepository<ContingentIntergroup,Long> {
    List<ContingentIntergroup> findAll();
}
