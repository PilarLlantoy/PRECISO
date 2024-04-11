package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.Centers;
import com.inter.proyecto_intergrupo.model.ifrs9.Quotas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CenterRepository extends JpaRepository<Centers,String> {
    List<Centers> findAll();
    List<Centers> findByOficina(String oficina);
}
