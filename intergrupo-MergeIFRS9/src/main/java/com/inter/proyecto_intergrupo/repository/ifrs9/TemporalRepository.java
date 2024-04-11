package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.TemporalTable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TemporalRepository  extends JpaRepository<TemporalTable,Long> {
}
