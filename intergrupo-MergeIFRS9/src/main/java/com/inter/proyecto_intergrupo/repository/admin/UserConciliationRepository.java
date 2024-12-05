package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.UserConciliation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;

@Repository
public interface UserConciliationRepository extends JpaRepository<UserConciliation,Integer> {

    List<UserConciliation> findAll();

    @Query("SELECT uc.conciliacion FROM UserConciliation uc WHERE uc.usuario.id = :userId AND uc.rol = :role")
    List<Conciliation> findConciliationsByUserAndRole(@Param("userId") int userId, @Param("role") UserConciliation.RoleConciliation role);

    // Método para eliminar las relaciones de conciliación de un usuario específico
    @Modifying
    @Transactional
    @Query("DELETE FROM UserConciliation uc WHERE uc.usuario.id = :userId")
    void deleteByUsuarioId(@Param("userId") int userId);
}
