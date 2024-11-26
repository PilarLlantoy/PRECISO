package com.inter.proyecto_intergrupo.service.parametricServices;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.AccountingRouteRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogAccountingLoadRepository;
import com.inter.proyecto_intergrupo.repository.parametric.LogInformationCrossingRepository;
import org.apache.poi.ss.usermodel.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
@Service
@Transactional
public class InformationCrossingService {
    @Autowired
    private final LogInformationCrossingRepository logInformationCrossingRepository;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private AuditRepository auditRepository;
    @Autowired
    private LogAccountingLoadRepository logAccountingLoadRepository;
    @Autowired
    public InformationCrossingService(LogInformationCrossingRepository logInformationCrossingRepository) {
        this.logInformationCrossingRepository = logInformationCrossingRepository;
    }
    public List<LogInformationCrossing> findAllLog(Conciliation concil, String fecha, EventType evento) {
        LocalDate localDate = LocalDate.parse(fecha);
        Date fechaDate = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return logInformationCrossingRepository.findAllByIdConciliacionAndFechaProcesoAndIdEventoOrderByIdDesc(concil,fechaDate, evento);
    }
}