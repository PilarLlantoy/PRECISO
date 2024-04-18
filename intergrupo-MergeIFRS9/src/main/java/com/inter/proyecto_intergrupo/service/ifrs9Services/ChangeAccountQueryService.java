package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.ChangeAccountQueryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class ChangeAccountQueryService {

    @Autowired
    private ChangeAccountQueryRepository changeAccountQueryRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    public ChangeAccountQueryService(ChangeAccountQueryRepository changeAccountQueryRepository) {
        this.changeAccountQueryRepository = changeAccountQueryRepository;
    }

    public List<ChangeAccountQuery> findAll(){
        return changeAccountQueryRepository.findAll();
    }

    public ChangeAccountQuery findById(Long id){
        return changeAccountQueryRepository.findByIdCambio(id);
    }

    public ChangeAccountQuery modifyAccount(ChangeAccountQuery toModify,String id, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se modifica un registro Historico de Cambio Cuenta");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Historico de Cambio Cuenta");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return changeAccountQueryRepository.save(toModify);
    }

    public ChangeAccountQuery saveAccount(ChangeAccountQuery changeAccountQuery, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se agrega un registro Historico Cambio Cuenta");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Historico de Cambio Cuenta");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return changeAccountQueryRepository.save(changeAccountQuery);
    }

    public void removeChangeAccount(Long id, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se elimina un registro Historico de Cambio Cuenta");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Historico de Cambio Cuenta");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        changeAccountQueryRepository.deleteById(id);
    }

    public void clearChangeAccount(User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se eliminan los registros Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        changeAccountQueryRepository.deleteAll();
    }

    public Page<ChangeAccountQuery> getAll(Pageable pageable){
        return changeAccountQueryRepository.findAll(pageable);
    }

    public List<ChangeAccountQuery> findByFilter(String value, String filter, String period) {
        List<ChangeAccountQuery> list=new ArrayList<ChangeAccountQuery>();
        switch (filter)
        {
            case "Empresa":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.empresa LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query.setParameter(1, value );
                query.setParameter(2, period+"%" );

                list= query.getResultList();

                break;
            case "Cuenta":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.cuenta LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query0.setParameter(1, value);
                query0.setParameter(2, period+"%" );

                list= query0.getResultList();
                break;
            case "Código de consolidación anterior":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.codicons_anterior LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period+"%" );

                list= query1.getResultList();
                break;
            case "Código de consolidación nuevo":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.codicons_nuevo LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period+"%" );

                list= query2.getResultList();
                break;
            case "Fecha de corte":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.fecha_corte LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query3.setParameter(1, value);
                query3.setParameter(2, period+"%" );

                list= query3.getResultList();
                break;
            case "Perímetro IFRS9":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.perimetro_ifrs9 LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period+"%" );

                list= query4.getResultList();
                break;
            case "Observación":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cambios_cuentas as em " +
                        "WHERE em.observacion LIKE ? AND em.fecha_corte LIKE ?", ChangeAccountQuery.class);
                query5.setParameter(1, value);
                query5.setParameter(2, period+"%" );

                list= query5.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public List<Object[]> validateCodicons(String anterior, String nuevo, String tipo){
        String[] anteriorParts = anterior.split("-");
        String[] nuevoParts = nuevo.split("-");

        Query insert = entityManager.createNativeQuery("SELECT A.empresa empresa, A.nucta cuenta,B.codicons codicons_anterior, A.codicons codicons_nuevo, A.fecont fecha_corte, ISNULL(C.ifrs9,'NO') perimetro_ifrs9, CASE WHEN A.codicons = B.codicons THEN 'NO' ELSE 'SI' END cambio, A.fechproce \n" +
                "FROM (SELECT nqm.* FROM nexco_query_marcados nqm WHERE MONTH(nqm.fecont) = ? AND YEAR(nqm.fecont) = ? AND nqm.origen = ? ) A \n" +
                "LEFT JOIN (SELECT nqm2.* FROM nexco_query_marcados nqm2 WHERE MONTH(nqm2.fecont) = ? AND YEAR(nqm2.fecont) = ? AND nqm2.origen = ? ) B ON A.nucta = B.nucta AND a.origen = b.origen AND a.empresa = b.empresa \n" +
                "LEFT JOIN nexco_provisiones C ON A.codicons = C.cuenta_neocon \n" +
                "GROUP BY A.empresa, A.nucta,A.codicons, B.codicons, A.fecont, ISNULL(C.ifrs9,'NO'), CASE WHEN A.codicons = B.codicons THEN 'NO' ELSE 'SI' END, A.fechproce");

        insert.setParameter(1,nuevoParts[1]);
        insert.setParameter(2,nuevoParts[0]);
        insert.setParameter(3,tipo);
        insert.setParameter(4,anteriorParts[1]);
        insert.setParameter(5,anteriorParts[0]);
        insert.setParameter(6,tipo);
        return insert.getResultList();
    }

    public void searchCodicons(String anterior, String nuevo, String tipo){
        String[] anteriorParts = anterior.split("-");
        String[] nuevoParts = nuevo.split("-");
        javax.persistence.Query delete = entityManager.createNativeQuery("DELETE FROM nexco_cambios_cuentas WHERE tipo_marca = ? AND fecha_corte LIKE ?");
        delete.setParameter(1,tipo);
        delete.setParameter(2,nuevo+"%");
        delete.executeUpdate();

        String nameTable ="";
        String tempValue ="";
        String tempValue2 ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'IFRS9' AND MONTH(fecont) = ? AND YEAR(fecont) = ? ");
        verify.setParameter(1,anteriorParts[1]);
        verify.setParameter(2,anteriorParts[0]);

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
        }
        else
        {
            nameTable ="nexco_query_marcados";
            tempValue ="origen = 'IFRS9' AND";
            tempValue2 ="a.origen = b.origen AND";
        }

        javax.persistence.Query insert = entityManager.createNativeQuery("INSERT INTO nexco_cambios_cuentas (empresa,cuenta,codicons_anterior,codicons_nuevo,fecha_corte,perimetro_ifrs9,cambio,tipo_marca) \n" +
                "SELECT A.empresa, A.nucta,B.codicons, A.codicons, A.fecont, ISNULL(C.ifrs9,'NO') perimetro, CASE WHEN A.codicons = B.codicons THEN 'NO' WHEN B.codicons IS NULL THEN 'No Existente' ELSE 'SI' END, ? \n" +
                "FROM (SELECT * FROM nexco_query_marcados WHERE MONTH(fecont) = ? AND YEAR(fecont) = ? AND origen = ? ) A \n" +
                "LEFT JOIN (SELECT * FROM "+nameTable+" WHERE "+tempValue+" MONTH(fecont) = ? AND YEAR(fecont) = ? ) B ON A.nucta = B.nucta AND "+tempValue2+" a.empresa = b.empresa \n" +
                "LEFT JOIN nexco_provisiones C ON A.codicons = C.cuenta_neocon \n" +
                "GROUP BY A.empresa, A.nucta,A.codicons, B.codicons, A.fecont, ISNULL(C.ifrs9,'NO'), CASE WHEN A.codicons = B.codicons THEN 'NO' WHEN B.codicons IS NULL THEN 'No Existente' ELSE 'SI' END ");
        insert.setParameter(1,tipo);
        insert.setParameter(2,nuevoParts[1]);
        insert.setParameter(3,nuevoParts[0]);
        insert.setParameter(4,tipo);
        insert.setParameter(5,anteriorParts[1]);
        insert.setParameter(6,anteriorParts[0]);
        insert.executeUpdate();

        Date today = new Date();
        String input = "CUENTASCAMBIOS";

        StatusInfo validateStatus = statusInfoRepositoryL.findByInputAndPeriodo(input, nuevo);

        if (validateStatus == null) {
            StatusInfo status = new StatusInfo();
            status.setInput(input);
            status.setPeriodo(nuevo);
            status.setFecha(today);
            statusInfoRepositoryL.save(status);
        } else {
            validateStatus.setFecha(today);
            statusInfoRepositoryL.save(validateStatus);
        }
    }

    public List<ChangeAccountQuery> getAccountsCodicons(String period, String tipo) {
        javax.persistence.Query lastUpdate = entityManager.createNativeQuery("select * from nexco_cambios_cuentas WHERE tipo_marca = ? AND fecha_corte LIKE ?",ChangeAccountQuery.class);
        lastUpdate.setParameter(1,tipo);
        lastUpdate.setParameter(2,period+"%");
        return lastUpdate.getResultList();
    }

    public List<Object[]> validateLoad(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT * FROM nexco_cambios_cuentas WHERE fecha_corte LIKE ? ");
        queryValidate.setParameter(1, period+"%");

        return queryValidate.getResultList();
    }

}
