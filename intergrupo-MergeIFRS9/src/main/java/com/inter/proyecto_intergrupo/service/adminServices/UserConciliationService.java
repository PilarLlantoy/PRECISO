package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Conciliation;
import com.inter.proyecto_intergrupo.model.parametric.UserConciliation;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.RoleRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserConciliationRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Service
@Transactional
public class UserConciliationService {
    @Autowired
    private final UserConciliationRepository userConciliationRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public UserConciliationService(UserConciliationRepository userConciliationRepository) {
        this.userConciliationRepository = userConciliationRepository;
    }

    public List<Conciliation> getConciliationsByUserAndRole(int userId, UserConciliation.RoleConciliation role) {
        return userConciliationRepository.findConciliationsByUserAndRole(userId, role);
    }


    @Transactional
    public void eliminarRelacionesPorUsuario(int userId) {
        userConciliationRepository.deleteByUsuarioId(userId);
    }



}
