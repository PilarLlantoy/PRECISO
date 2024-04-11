package com.inter.proyecto_intergrupo.repository.reportNIC34;

import com.inter.proyecto_intergrupo.model.reportNIC34.BalanceVistaNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.BalanceVistaNIC34Consol;
import com.inter.proyecto_intergrupo.model.reportNIC34.PygVistaNIC34;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BalanceVistaNIC34ConsolRepository extends JpaRepository<BalanceVistaNIC34Consol,Long> {
    List<BalanceVistaNIC34Consol> findAll();
    List<BalanceVistaNIC34Consol>  findByQaplica(String qaplica);
    void deleteByQaplica(String qaplica);
}
