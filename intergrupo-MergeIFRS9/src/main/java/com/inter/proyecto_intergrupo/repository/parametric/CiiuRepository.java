package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CiiuRepository extends JpaRepository<Ciiu,String> {
    List<Ciiu> findAll();
    List<Ciiu> findByCiiu(String ciiu);
}
