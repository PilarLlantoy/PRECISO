package com.inter.proyecto_intergrupo.repository.admin;

import com.inter.proyecto_intergrupo.model.admin.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User,Integer> {
    User findByUsuario(String userName);
    List<User> findByCentro(String centro);
    User findByCorreo(String email);
    User findByResetPasswordToken(String token);
    List<User> findAll();
    List<User> findByEmpresa(String empresa);
}
