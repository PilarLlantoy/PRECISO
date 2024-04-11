package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.BalanceVistaNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.PygVistaNIC34;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceVistaNIC34Repository extends JpaRepository<BalanceVistaNIC34,Long> {
    List<BalanceVistaNIC34> findAll();
    List<PygVistaNIC34>  findByQaplica(String qaplica);
    void deleteByQaplica(String qaplica);
}
