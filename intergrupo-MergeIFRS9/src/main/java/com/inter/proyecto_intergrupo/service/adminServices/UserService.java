package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.*;
import com.inter.proyecto_intergrupo.model.parametric.ConciliationRoute;
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

    public User findById(int id){
        return userRepository.findAllById(id);
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
            return (boolean) validate.getResultList().get(0);
        } catch (Exception e) {
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
                if (value.length()>1 && value.substring(0,1).equalsIgnoreCase("i"))
                    valor = false;
                Query sql = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios as em WHERE em.activo = ? ", User.class);
                sql.setParameter(1, valor);
                list = sql.getResultList();
                break;
            case "Código":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios as em " +
                        "WHERE em.codigo_usuario LIKE ? ", User.class);
                query.setParameter(1, "%" + value + "%" );
                list= query.getResultList();
                break;
            case "Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios as em\n" +
                        "WHERE concat(em.primer_nombre,em.segundo_nombre,em.primer_apellido,em.segundo_apellido) LIKE ? " , User.class);
                query0.setParameter(1, "%" + value.replace(" ","") + "%");
                list = query0.getResultList();
                break;
            case "Cargo":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios em " +
                        "JOIN preciso_administracion_cargos ec ON em.id_cargo = ec.id_cargo " +
                        "WHERE ec.nombre_cargo LIKE ? ", User.class);
                query1.setParameter(1, "%" + value + "%");
                list = query1.getResultList();
                break;
            case "Perfil":
                Query query2 = entityManager.createNativeQuery("SELECT em.* " +
                        "FROM preciso_administracion_usuarios em " +
                        "JOIN preciso_administracion_user_rol ur ON em.id_usuario = ur.id_usuario " +
                        "JOIN preciso_administracion_perfiles p ON ur.id_perfil = p.id_perfil " +
                        "WHERE p.nombre_perfil LIKE ? ", User.class);
                query2.setParameter(1, "%" + value + "%");
                list = query2.getResultList();
                break;
            case "Documento":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios em " +
                        "WHERE em.numero_documento LIKE ? ", User.class);
                query3.setParameter(1, "%" + value + "%");
                list = query3.getResultList();
                break;
            case "Tipo Documento":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_usuarios em \n" +
                        "inner join preciso_administracion_tipo_documento ep on em.id_tipo_documento=ep.id_tipo_documento\n" +
                        "WHERE ep.nombre_tipo_documento LIKE ? ", User.class);
                query4.setParameter(1, "%" + value + "%");
                list = query4.getResultList();
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
