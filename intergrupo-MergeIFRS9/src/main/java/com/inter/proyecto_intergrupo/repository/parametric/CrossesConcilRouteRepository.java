package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountConcil;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.CrossesConcilRoute;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRConcil;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrossesConcilRouteRepository extends JpaRepository<CrossesConcilRoute,Integer> {
    CrossesConcilRoute findAllById(int id);
    List<CrossesConcilRoute> findByEstado(boolean estado);
}
