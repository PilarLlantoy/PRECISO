package com.inter.proyecto_intergrupo.repository.parametric;

import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface statusInfoRepository extends JpaRepository<StatusInfo,String> {
    List<StatusInfo> findAll();
    StatusInfo findByInputAndPeriodo(String input, String periodo);
    StatusInfo findByStatusAndPeriodo(String input, String periodo);
    List<StatusInfo> findByInputInAndPeriodo(List<String> inputs, String periodo);

}
