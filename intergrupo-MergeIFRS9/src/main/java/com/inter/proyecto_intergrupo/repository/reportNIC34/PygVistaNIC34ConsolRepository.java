package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.PygVistaNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.PygVistaNIC34Consol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PygVistaNIC34ConsolRepository extends JpaRepository<PygVistaNIC34Consol,Long> {
    List<PygVistaNIC34Consol> findAll();
    List<PygVistaNIC34Consol>  findByQaplica(String qaplica);
    void deleteByQaplica(String qaplica);
}
