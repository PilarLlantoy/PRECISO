package com.inter.proyecto_intergrupo.repository.temporal;

import com.inter.proyecto_intergrupo.model.ifrs9.RejectionsCc;
import com.inter.proyecto_intergrupo.model.temporal.RejectionsCcTemporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RejectionsCcTemporalRepository extends JpaRepository<RejectionsCcTemporal,Long> {
    List<RejectionsCcTemporal> findAll();
    RejectionsCcTemporal findByIdRechazos(Long id);
}
