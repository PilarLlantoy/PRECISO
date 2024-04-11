package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.F351Icrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaF351Icrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaF351IcrvRepository extends JpaRepository<PlantillaF351Icrv,Long> {
    List<PlantillaF351Icrv> findAll();
    PlantillaF351Icrv findByIdF(Long id);
    void deleteByPeriodo(String periodo);
    List<PlantillaF351Icrv> findByPeriodo(String periodo);
}
