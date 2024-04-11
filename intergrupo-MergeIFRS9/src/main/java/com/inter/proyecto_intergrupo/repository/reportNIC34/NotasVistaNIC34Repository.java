package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.BalanceVistaNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.NotasVistaNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.PygVistaNIC34;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotasVistaNIC34Repository extends JpaRepository<NotasVistaNIC34,Long> {
    List<NotasVistaNIC34> findAll();
    List<NotasVistaNIC34>  findByQaplica(String qaplica);
    void deleteByQaplica(String qaplica);
}
