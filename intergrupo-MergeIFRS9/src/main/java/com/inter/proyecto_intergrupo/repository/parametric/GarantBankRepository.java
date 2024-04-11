package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GarantBankRepository extends JpaRepository<GarantBank,String> {
    List<GarantBank> findAll();
    GarantBank findByNombreSimilar(String id);
    GarantBank findByPais(String id);
    List<GarantBank> findAllByNit(String id);
}
