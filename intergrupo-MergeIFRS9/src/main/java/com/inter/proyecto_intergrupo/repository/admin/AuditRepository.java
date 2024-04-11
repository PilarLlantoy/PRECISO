package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditRepository extends JpaRepository<Audit,Long> {
    List<Audit> findAll();
    Audit findByIdAuditoria(Long id);

}
