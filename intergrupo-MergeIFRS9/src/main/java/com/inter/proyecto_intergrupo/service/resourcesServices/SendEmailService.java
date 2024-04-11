package com.inter.proyecto_intergrupo.service.resourcesServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;

@Service
@Transactional
public class SendEmailService {

    @Autowired
    EntityManager entityManager;

    public SendEmailService(EntityManager entityManager){
        this.entityManager = entityManager;
    }

    public void sendEmail(String email, String subject, String content){
        Query query = entityManager.createNativeQuery("EXEC dbo.NEXCO_SEND_EMAIL @profile = ?," +
                " @email_rec = ?," +
                " @body_mes = ?," +
                " @subject_rec = ?,"+
                " @body_form = ?");

        query.setParameter(1,"INFOFINA");
        query.setParameter(2,email);
        query.setParameter(3, content);
        query.setParameter(4,subject);
        query.setParameter(5,"HTML");

        query.executeUpdate();
    }

    public void sendEmailCopAd(String email, String emailCop, String subject, String content){
        Query query = entityManager.createNativeQuery("EXEC dbo.NEXCO_SEND_EMAIL_COP_AD " +
                " @profile = ?," +
                " @email_rec = ?," +
                " @email_cop = ?," +
                " @body_mes = ?," +
                " @subject_rec = ?,"+
                " @body_form = ?");

        query.setParameter(1,"INFOFINA");
        query.setParameter(2,email);
        query.setParameter(3,emailCop);
        query.setParameter(4, content);
        query.setParameter(5,subject);
        query.setParameter(6,"HTML");

        query.executeUpdate();
    }

}
