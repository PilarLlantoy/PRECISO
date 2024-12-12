package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.model.parametric.CampoRC;
import com.inter.proyecto_intergrupo.model.parametric.LogAccountingLoad;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;


@Service
public class JobAutoService {

    @Autowired
    private AccountingRouteRepository accountingRouteRepository;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private LogAccountingLoadRepository logAccountingLoadRepository;

    public List<Object> findTemporal(){
        List<Object> listTemp = new ArrayList<>();
        try {
            Query querySelect = entityManager.createNativeQuery("SELECT * FROM PRECISO_TEMP_CONTABLES ");
            listTemp = querySelect.getResultList();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        return listTemp;
    }
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void loadLogCargueFallido(User user, AccountingRoute ac, String fecha, String tipo, String estado, String mensaje) {
        try {
            loadLogCargue(user, ac, fecha, tipo, estado, mensaje);
        } catch (Exception e) {
            e.printStackTrace(); // Loguea cualquier problema en el proceso de guardar el log
        }
    }



    public void loadLogCargue(User user,AccountingRoute ac, String fecha, String tipo, String estado, String mensaje)
    {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        List<Object> listTemp =findTemporal();
        Date today=new Date();
        LogAccountingLoad insert = new LogAccountingLoad();
        insert.setFechaCargue(fechaDate);
        insert.setFechaPreciso(today);
        insert.setCantidadRegistros((long) listTemp.size());
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");
        insert.setTipoProceso(tipo);
        insert.setNovedad(mensaje);
        insert.setEstadoProceso(estado);
        if(user!=null)
            insert.setUsuario(user.getUsuario());
        else
            insert.setUsuario("Automático");
        insert.setIdRc(ac);
        logAccountingLoadRepository.save(insert);
    }

}
