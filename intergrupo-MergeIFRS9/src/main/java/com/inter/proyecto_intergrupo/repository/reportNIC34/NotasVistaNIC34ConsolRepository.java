package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.NotasVistaNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.NotasVistaNIC34Consol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotasVistaNIC34ConsolRepository extends JpaRepository<NotasVistaNIC34Consol,Long> {
    List<NotasVistaNIC34Consol> findAll();
    List<NotasVistaNIC34Consol>  findByQaplica(String qaplica);
    void deleteByQaplica(String qaplica);
}
