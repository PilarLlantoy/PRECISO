package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class AuditService {

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public List<Audit> findAll(){
        return auditRepository.findAll();
    }

    public Audit findByIdAuditoria(Long id){
        return auditRepository.findByIdAuditoria(id);
    }

    public Audit saveThird(Audit audit){
        audit.setFecha(new Date());
        return auditRepository.save(audit);
    }

    public Page<Audit> getAll(Pageable pageable){
        return auditRepository.findAll(pageable);
    }

    public List<Audit> getAllList(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                "ORDER BY em.fecha desc", Audit.class);

        return query.getResultList();
    }

    public List<Audit> findByFilter(String value, String filter) {
        List<Audit> list=new ArrayList<Audit>();
        switch (filter)
        {
            case "Id Auditoria":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.id_auditoria LIKE ?", Audit.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Usuario":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.usuario LIKE ?", Audit.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Nombre":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.nombre LIKE ?", Audit.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Centro Costos":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.centro LIKE ?", Audit.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Componente":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.componente LIKE ?", Audit.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Input":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.input LIKE ?", Audit.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Acción":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.accion LIKE ?", Audit.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Hora y Fecha":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM preciso_administracion_auditoria as em " +
                        "WHERE em.fecha LIKE ?", Audit.class);
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

    @Scheduled(cron = "0 0 4 20 * * ")
    public void downloadAuditTable() throws IOException {
        List<Audit> auditList= new ArrayList<Audit>();
        auditList = findAll();
        AuditListReport listReport = new AuditListReport(auditList);
        listReport.exportFile();
        //deletelogs();
    }

    @Scheduled(cron = "0 10 4 20 1,3,6,9,12 * ")
    public void deletelogs(){
        auditRepository.deleteAll();
    }

}
