package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.AccountEventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.AccountNoteTemplate;
import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountNoteTemplateRepository extends JpaRepository<AccountNoteTemplate,Integer> {
    AccountNoteTemplate findAllById(int id);
    List<AccountNoteTemplate> findByEstado(boolean estado);
}
