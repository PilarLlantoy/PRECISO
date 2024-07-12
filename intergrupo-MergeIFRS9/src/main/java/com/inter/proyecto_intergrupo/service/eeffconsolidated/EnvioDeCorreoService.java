package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class EnvioDeCorreoService {


    @Autowired
    SendEmailService sendEmailService;

    @Autowired
    private DatesLoadEeffRepository datesLoadEeffRepository;

    @Autowired
    EntityManager entityManager;

    @Autowired
    private EnvioDeCorreoService envioDeCorreoService;



    public void sendEmailFilial(User userRecipientEmail ,String entidad) {


        String subject = "Notificación Ejecución Job Reducción Tabla Vertical de Saldos";

        String content = "<p>Se confirmo el cargue de los archivos para la filial: "+entidad+" .</p>" +
                "<br>" +
                "   <tbody>" +
                "       <td>Se confirmarion exitosamente de la filial.</td>" +
                "   </tbody>";

        System.out.println("Correo Enviado");
    }
    public void SendMailFiliales(String entidad, String periodo) {
        DatesLoadEeFF estado = datesLoadEeffRepository.findByEntidadAndPeriodo(entidad, periodo);

        Query emails = entityManager.createNativeQuery("SELECT DISTINCT a.* FROM preciso_administracion_usuarios a, preciso_administracion_vistas b, preciso_administracion_rol_vista c, preciso_administracion_user_rol d \n" +
                "WHERE a.usuario = d.usuario AND d.id_perfil = c.id_perfil AND c.id_vista=b.id_vista AND b.nombre IN ('Ver EEFF Consolidado Fiduciaria','Ver EEFF Consolidado Valores')", User.class);
        List<User> listEmails = emails.getResultList();

        for (User u : listEmails) {
            sendEmailFilial(u, entidad);
        }
        if (estado != null) {
            estado.setEstado("CONFIRMADO");
            datesLoadEeffRepository.save(estado); // Actualiza el estado a CONFIRMADO
        }
    }
}

