package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPrecioIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlantillaCalculoIcrvRepository extends JpaRepository<PlantillaCalculoIcrv,Long> {
    List<PlantillaCalculoIcrv> findAll();
    PlantillaCalculoIcrv findByIdCalculo(Long id);
    void deleteByIdCalculo(Long idCalculo);
    List<PlantillaCalculoIcrv> findByValoracion(String valoracion);
    List<PlantillaCalculoIcrv> findByEmpresa(String empresa);
    List<PlantillaCalculoIcrv> findByNit(String nit);
    List<PlantillaCalculoIcrv> findByDv(String dv);
    List<PlantillaCalculoIcrv> findByIsin(String isin);
    List<PlantillaCalculoIcrv> findByParticipacion(Double participacion);
    List<PlantillaCalculoIcrv> findByVrAccion(Double vrAccion);
    List<PlantillaCalculoIcrv> findByNoAcciones(Integer noAcciones);
}
