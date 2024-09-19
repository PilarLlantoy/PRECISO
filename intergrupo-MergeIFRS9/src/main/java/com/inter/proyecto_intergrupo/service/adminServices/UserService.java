package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.*;
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
import javax.persistence.NoResultException;
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
    public List<User> findAllActive() {
        Query query = entityManager.createNativeQuery(
                "SELECT * FROM preciso_administracion_usuarios WHERE activo = 1", User.class);
        return query.getResultList();
    }

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
        return user;
    }

    public void saveUsarLDAP(String usuario){
        Query createUser = entityManager.createNativeQuery("IF NOT EXISTS(SELECT * FROM preciso_administracion_usuarios WHERE codigo_usuario= :codigoUsuario ) BEGIN INSERT INTO preciso_administracion_usuarios(codigo_usuario,creacion) VALUES(:codigoUsuario, :fechaCreacion ) END\n");
        createUser.setParameter("codigoUsuario",usuario);
        createUser.setParameter("fechaCreacion",new Date());
        createUser.executeUpdate();
    }

    public void asociarCentro(String centro, int id, int operacion){

        if(operacion==2)
        {
            Query query1 = entityManager.createNativeQuery("DELETE FROM preciso_user_account WHERE id_usuario = ?");
            query1.setParameter(1, id);
            query1.executeUpdate();
        }

        //Query query = entityManager.createNativeQuery("SELECT nua.cuenta_local FROM preciso_user_account AS nua, preciso_administracion_usuarios AS nu WHERE nua.id_usuario=nu.usuario AND nu.centro = ? GROUP BY nua.cuenta_local");
        Query query = entityManager.createNativeQuery("SELECT ncr.cuenta_local FROM preciso_cuentas_responsables AS ncr WHERE ncr.centro = ? GROUP BY ncr.cuenta_local");
        query.setParameter(1,centro);
        List<Object> list =query.getResultList();

        for (Object account : list) {

            Query query1 = entityManager.createNativeQuery("INSERT INTO preciso_user_account (id_usuario,cuenta_local) VALUES (?,?)");
            query1.setParameter(1, id);
            query1.setParameter(2, account.toString());
            query1.executeUpdate();
        }
    }


    public User modifyUser(User toModify, Date fecha, Set<Role> roles, int id){
        User toInsert = new User();
        //Cargo cargo = new Cargo(); PRBLEMAS CON CARGO
        toInsert.setPrimerNombre(toModify.getPrimerNombre());
        toInsert.setSegundoNombre(toModify.getSegundoNombre());
        toInsert.setPrimerApellido(toModify.getPrimerApellido());
        toInsert.setSegundoApellido(toModify.getSegundoApellido());
        toInsert.setNumeroDocumento(toModify.getNumeroDocumento());
        toInsert.setTipoDocumento(toModify.getTipoDocumento());
        toInsert.setCargo(toModify.getCargo());
        toInsert.setActivo((toModify.isActivo()));
        toInsert.setCentro(toModify.getCentro());
        toInsert.setUsuario(toModify.getUsuario());
        toInsert.setId(id);
        toInsert.setCorreo(toModify.getCorreo());
        toInsert.setContra(toModify.getContra());
        toInsert.setEmpresa(toModify.getEmpresa());
        toInsert.setEstado(toModify.isEstado());
        toInsert.setCreacion(fecha);
        toInsert.setRoles(roles);
        asociarCentro(toModify.getCentro(),toModify.getId(),2);
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
        Query getCentro = entityManager.createNativeQuery("SELECT nu.centro FROM preciso_user_account AS nua, preciso_administracion_usuarios AS nu WHERE nua.cuenta_local = ? AND nu.usuario = nua.id_usuario");
        getCentro.setParameter(1,cuenta);

        return getCentro.getResultList();
    }

    public List<String> findUserByCentroAccount2(Long cuenta)
    {
        Query getCentro = entityManager.createNativeQuery("SELECT nu.centro FROM preciso_user_account AS nua, preciso_administracion_usuarios AS nu WHERE nua.cuenta_local = ? AND nu.usuario = nua.id_usuario");
        getCentro.setParameter(1,cuenta);

        return getCentro.getResultList();
    }

    public List<User> findUserByCentro(String centro)
    {
        return userRepository.findByCentro(centro);
    }

    public boolean validateEndpoint(int usuario,String vista)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.id_vista FROM preciso_administracion_user_rol AS nur, preciso_administracion_rol_vista AS nrv, preciso_administracion_vistas AS nv\n" +
                "WHERE nur.id_perfil = nrv.id_perfil AND nrv.id_vista = nv.id_vista AND nur.id_usuario = ? AND nv.nombre = ? ");
        validate.setParameter(1,usuario);
        validate.setParameter(2,vista);

        return !validate.getResultList().isEmpty();
    }

    public boolean validateEndpointVer(int usuario,String vista)
    {
        Query validate = entityManager.createNativeQuery("SELECT nrv.p_visualizar FROM preciso_administracion_user_rol AS nur, preciso_administracion_rol_vista AS nrv, preciso_administracion_vistas AS nv\n" +
                "WHERE nur.id_perfil = nrv.id_perfil AND nrv.id_vista = nv.id_vista AND nur.id_usuario = ? AND nv.nombre = ? ");
        validate.setParameter(1,usuario);
        validate.setParameter(2,vista);
        return (boolean) validate.getSingleResult()==true;
    }
    public boolean validateEndpointModificar(int usuario,String vista)
    {
        try{
            Query validate = entityManager.createNativeQuery("SELECT nrv.p_modificar FROM preciso_administracion_user_rol AS nur, preciso_administracion_rol_vista AS nrv, preciso_administracion_vistas AS nv\n" +
                    "WHERE nur.id_perfil = nrv.id_perfil AND nrv.id_vista = nv.id_vista AND nur.id_usuario = ? AND nv.nombre = ? ");
            validate.setParameter(1,usuario);
            validate.setParameter(2,vista);
            return (boolean) validate.getSingleResult();
        } catch (NoResultException e) {
            return false;
        }
    }

    public List<String> validatePrincipal(String principal)
    {
        Query validate = entityManager.createNativeQuery("SELECT nv.sub_menu_p1 FROM preciso_administracion_vistas AS nv WHERE nv.menu_principal = ? GROUP BY nv.sub_menu_p1");
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
            case "Estado":
                Boolean valor = true;
                if ("inactivo".equalsIgnoreCase(value)) {
                    valor = false;
                }
                System.out.println("valor: " + valor);
                Query quer = entityManager.createNativeQuery(
                        "SELECT em.* FROM preciso_administracion_usuarios as em WHERE em.activo = ?", User.class);
                quer.setParameter(1, valor);
                list = quer.getResultList();
                break;
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios as em " +
                        "WHERE em.codigo_usuario LIKE ?", User.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Nombre":
                String sql = "SELECT em.* FROM preciso_administracion_usuarios as em " +
                        "WHERE em.primer_nombre LIKE ? OR em.segundo_nombre LIKE ? " +
                        "OR em.primer_apellido LIKE ? OR em.segundo_apellido LIKE ?";

                Query query0 = entityManager.createNativeQuery(sql, User.class);
                String likeValue = "%" + value + "%";
                query0.setParameter(1, likeValue);
                query0.setParameter(2, likeValue);
                query0.setParameter(3, likeValue);
                query0.setParameter(4, likeValue);

                list = query0.getResultList();
                break;
            case "Cargo":
                String sql2 = "SELECT em.* FROM preciso_administracion_usuarios em " +
                        "JOIN preciso_administracion_cargos ec ON em.id_cargo = ec.id_cargo " +
                        "WHERE ec.nombre_cargo LIKE ?";

                Query query1 = entityManager.createNativeQuery(sql2, User.class);
                String likeValue1 = "%" + value + "%";
                query1.setParameter(1, likeValue1);

                list = query1.getResultList();
                break;
            case "Perfil":
                String sql3 = "SELECT em.* " +
                        "FROM preciso_administracion_usuarios em " +
                        "JOIN preciso_administracion_user_rol ur ON em.id_usuario = ur.id_usuario " +
                        "JOIN preciso_administracion_perfiles p ON ur.id_perfil = p.id_perfil " +
                        "WHERE p.nombre_perfil LIKE ?";

                Query query2 = entityManager.createNativeQuery(sql3, User.class);
                String likeValue2 = "%" + value + "%";
                query2.setParameter(1, likeValue2);

                list = query2.getResultList();
                break;

            default:
                break;
        }

        return list;
    }


    public void loadAudit(User user)
    {
        Query query = entityManager.createNativeQuery("DELETE FROM preciso_administracion_logueo where usuario = ?; " +
                "INSERT into preciso_administracion_logueo (usuario,nombre,fecha) values(?,?,?);");
        query.setParameter(1, user.getUsuario());
        query.setParameter(2, user.getUsuario());
        query.setParameter(3, user.getPrimerNombre());
        query.setParameter(4, new Date());
        query.executeUpdate();
    }


}
