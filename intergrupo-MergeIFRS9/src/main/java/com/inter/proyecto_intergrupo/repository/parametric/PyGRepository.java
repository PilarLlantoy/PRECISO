package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.PyG;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PyGRepository extends JpaRepository<PyG,Integer> {
    List<PyG> findAll();
    List<PyG> findAllById(Integer id);
}
