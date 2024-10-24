package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.EventMatrix;
import com.inter.proyecto_intergrupo.model.parametric.NoteTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteTemplateRepository extends JpaRepository<NoteTemplate,Integer> {
    NoteTemplate findAllById(int id);
    List<NoteTemplate> findByEstado(boolean estado);
}
