package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SignatureRepository extends JpaRepository<Signature,Long> {
    List<Signature> findAll();
    Signature findByIdFirma(Long id);
    void deleteByIdFirma(Long idFirma);
    List<Signature> findByCargoContainingIgnoreCase(String cargo);
    List<Signature> findByNombreContainingIgnoreCase(String nombre);
    List<Signature> findByTelefonoContainingIgnoreCase(String telefono);
    List<Signature> findByCorreoContainingIgnoreCase(String correo);
    List<Signature> findByDireccionContainingIgnoreCase(String direccion);
}
