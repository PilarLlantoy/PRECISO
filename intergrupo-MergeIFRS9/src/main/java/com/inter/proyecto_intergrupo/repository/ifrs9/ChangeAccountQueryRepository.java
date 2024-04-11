package com.inter.proyecto_intergrupo.repository.ifrs9;

import com.inter.proyecto_intergrupo.model.ifrs9.Perimeter;
import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeAccountQueryRepository extends JpaRepository<ChangeAccountQuery,Long> {
    List<ChangeAccountQuery> findAll();
    ChangeAccountQuery findByIdCambio(Long idCambio);
}
