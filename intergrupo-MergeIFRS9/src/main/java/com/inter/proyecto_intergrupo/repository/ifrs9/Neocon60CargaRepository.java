package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.information.Neocon60Ajuste;
import com.inter.proyecto_intergrupo.model.information.Neocon60Carga;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Neocon60CargaRepository extends JpaRepository<Neocon60Carga,Long> {
    List<Neocon60Carga> findAll();
}
