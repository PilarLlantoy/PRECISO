package com.inter.proyecto_intergrupo.repository.reports;

import com.inter.proyecto_intergrupo.model.temporal.Rp21Temporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Rp21TemporalRepository extends JpaRepository<Rp21Temporal,Long> {
    String deleteByOrigen(String origen);
}
