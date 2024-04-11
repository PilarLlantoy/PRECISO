package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.PduIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPduIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaPduIcrvRepository extends JpaRepository<PlantillaPduIcrv,Long> {
    List<PlantillaPduIcrv> findAll();
    PlantillaPduIcrv findByIdPdu(Long id);
    void deleteByIdPdu(Long idPdu);
    List<PlantillaPduIcrv> findByNoisin(String noisin);
    List<PlantillaPduIcrv> findByGrupo(String grupo);
    List<PlantillaPduIcrv> findByEntidad(String entidad);
}
