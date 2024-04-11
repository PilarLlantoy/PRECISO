package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.TypeEntity;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.inter.proyecto_intergrupo.repository.admin.RoleRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.*;

@Service
@Transactional
public class    UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public UserService( UserRepository userRepository,
                        RoleRepository roleRepository,
                        BCryptPasswordEncoder bCryptPasswordEncoder){
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public User findUserByEmail(String email){
        return userRepository.findByCorreo(email);
    }

    public User findUserByUserName(String username){
        return userRepository.findByUsuario(username);
    }

    public List<User> findAll(){return userRepository.findAll();}

    public List<User> findByEmpresa(String empresa){return userRepository.findByEmpresa(empresa);}

    public User saveUser(User user , HashSet<Role> userRoles){

        user.setContra(bCryptPasswordEncoder.encode("randomPass12345"));
        user.setEstado(true);
        user.setCorreo(user.getCorreo());
        user.setCreacion(new Date());
        user.setEmpresa(user.getEmpresa());
        user.setCentro(user.getCentro());
        user.setRoles(userRoles);
        user.setResetPasswordToken("");
        userRepository.save(user);
        asociarCentro(user.getCentro(),userRepository.findByUsuario(user.getUsuario()).getUsuario(),1);
        return user;
    }

    public void asociarCentro(String centro, String id, int operacion){

        if(operacion==2)
        {
            Query query1 = entityManager.createNativeQuery("DELETE FROM nexco_user_account WHERE id_usuario = ?");
            query1.setParameter(1, id);
            query1.executeUpdate();
        }

        //Query query = entityManager.createNativeQuery("SELECT nua.cuenta_local FROM nexco_user_account AS nua, nexco_usuarios AS nu WHERE nua.id_usuario=nu.usuario AND nu.centro = ? GROUP BY nua.cuenta_local");
        Query query = entityManager.createNativeQuery("SELECT ncr.cuenta_local FROM nexco_cuentas_responsables AS ncr WHERE ncr.centro = ? GROUP BY ncr.cuenta_local");
        query.setParameter(1,centro);
        List<Object> list =query.getResultList();

        for (Object account : list) {

            Query query1 = entityManager.createNativeQuery("INSERT INTO nexco_user_account (id_usuario,cuenta_local) VALUES (?,?)");
            query1.setParameter(1, id);
            query1.setParameter(2, account.toString());
            query1.executeUpdate();
        }
    }

    public User modifyUser(User toModify, Date fecha, Set<Role> roles, String id){
        User toInsert = new User();
        toInsert.setNombre(toModify.getNombre());
        toInsert.setCentro(toModify.getCentro());
        toInsert.setUsuario(id);
        toInsert.setCorreo(toModify.getCorreo());
        toInsert.setContra(toModify.getContra());
        toInsert.setEmpresa(toModify.getEmpresa());
        toInsert.setEstado(toModify.getEstado());
        toInsert.setCreacion(fecha);
        toInsert.setRoles(roles);
        asociarCentro(toModify.getCentro(),toModify.getUsuario(),2);
        userRepository.save(toInsert);
        return toInsert;
    }

    public void updateResetPasswordToken(String token, String username) throws UsernameNotFoundException {
         User user = userRepository.findByUsuario(username);
         if(user!=null){
             user.setResetPasswordToken(token);
             userRepository.save(user);
         }else{
             throw new UsernameNotFoundException("El usuario no existe");
         }
    }

    public User getByResetPasswordToken(String token){return userRepository.findByResetPasswordToken(token);}

    public void updatePassword(User user, String newPass){
        String encodedPass = bCryptPasswordEncoder.encode(newPass);
        user.setContra(encodedPass);

        user.setResetPasswordToken(null);
        userRepository.save(user);
    }

    public List<Object[]> findUserByCentroAccount(Long cuenta)
    {
        Query getCentro = entityManager.createNativeQuery("SELECT nu.centro FROM nexco_user_account AS nua, nexco_usuarios AS nu WHERE nua.cuenta_local = ? AND nu.usuario = nua.id_usuario");
        getCentro.setParameter(1,cuenta);

        return getCentro.getResultList();
    }

    public List<String> findUserByCentroAccount2(Long cuenta)
    {
        Query getCentro = entityManager.createNativeQuery("SELECT nu.centro FROM nexco_user_account AS nua, nexco_usuarios AS nu WHERE nua.cuenta_local = ? AND nu.usuario = nua.id_usuario");
        getCentro.setParameter(1,cuenta);

        return getCentro.getResultList();
    }

    public List<User> findUserByCentro(String centro)
    {
        return userRepository.findByCentro(centro);
    }

    public boolean validateEndpoint(String usuario,String vista)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.id_vista FROM nexco_user_rol AS nur, nexco_rol_vista AS nrv, nexco_vistas AS nv\n" +
                "WHERE nur.id_perfil = nrv.id_perfil AND nrv.id_vista = nv.id_vista AND nur.usuario = ? AND nv.nombre = ? ");
        validate.setParameter(1,usuario);
        validate.setParameter(2,vista);

        return !validate.getResultList().isEmpty();
    }

    public List<String> validatePrincipal(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.sub_menu_p1 FROM nexco_vistas AS nv WHERE nv.menu_principal = ? GROUP BY nv.sub_menu_p1");
        validate.setParameter(1,principal);
        return validate.getResultList();
    }

    public Page<User> getAll(Pageable pageable){
        return userRepository.findAll(pageable);
    }

    public List<User> findByFilter(String value, String filter) {
        List<User> list=new ArrayList<User>();
        switch (filter)
        {
            case "Usuario":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_usuarios as em " +
                        "WHERE em.usuario LIKE ?", User.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_usuarios as em " +
                        "WHERE em.nombre LIKE ?", User.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Correo":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_usuarios as em " +
                        "WHERE em.correo LIKE ?", User.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Centro":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_usuarios as em " +
                        "WHERE em.centro LIKE ?", User.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Estado":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_usuarios as em " +
                        "WHERE em.estado LIKE ?", User.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Roles":
                Query query4 = entityManager.createNativeQuery("SELECT nu.* FROM nexco_usuarios as nu, nexco_user_rol AS nur, nexco_perfiles AS np \n" +
                        "WHERE nu.usuario = nur.usuario AND nur.id_perfil = np.id_perfil AND np.nombre_perfil LIKE ?", User.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "YNTP Empresa":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_usuarios as em " +
                        "WHERE em.empresa LIKE ?", User.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

    public void loadAudit(User user)
    {
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_logueo where usuario = ?; " +
                "INSERT into nexco_logueo (usuario,nombre,fecha) values(?,?,?);");
        query.setParameter(1, user.getUsuario());
        query.setParameter(2, user.getUsuario());
        query.setParameter(3, user.getNombre());
        query.setParameter(4, new Date());
        query.executeUpdate();
    }
}
