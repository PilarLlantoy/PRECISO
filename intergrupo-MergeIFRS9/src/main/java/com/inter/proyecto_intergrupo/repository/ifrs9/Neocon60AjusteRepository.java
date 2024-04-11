package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.information.Neocon60Ajuste;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Neocon60AjusteRepository extends JpaRepository<Neocon60Ajuste,Long> {
    List<Neocon60Ajuste> findAll();
}
