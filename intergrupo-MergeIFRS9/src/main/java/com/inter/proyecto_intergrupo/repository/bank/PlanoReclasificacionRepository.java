package com.inter.proyecto_intergrupo.repository.bank;

import com.inter.proyecto_intergrupo.model.bank.planoreclasificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlanoReclasificacionRepository extends JpaRepository<planoreclasificacion,String> {
    List<planoreclasificacion> findAll();
    List<planoreclasificacion> findAllByContrato(String contrato);
    planoreclasificacion findByContrato(String id);
}
