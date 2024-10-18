package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.ConstructionParameter;
import com.inter.proyecto_intergrupo.model.parametric.ValidationRC;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ConstructionParameterRepository extends JpaRepository<ConstructionParameter,Integer> {
    ConstructionParameter findAllById(int id);
    List<ConstructionParameter> findByEstado(boolean estado);

    List<ConstructionParameter> findByAccount(AccountEventMatrix cuenta);
}
