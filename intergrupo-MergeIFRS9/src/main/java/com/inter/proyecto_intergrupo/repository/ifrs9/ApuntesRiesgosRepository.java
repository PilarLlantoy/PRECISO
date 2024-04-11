package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.ApuntesRiesgos;
import com.inter.proyecto_intergrupo.model.ifrs9.SegmentosFinalTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApuntesRiesgosRepository extends JpaRepository<ApuntesRiesgos,Long> {
    List<ApuntesRiesgos> findAll();
    //List<ApuntesRiesgos> findByNumeroCliente(String numeroCliente);
}
