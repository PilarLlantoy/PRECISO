package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.MasterInventRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jni.Local;
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
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class MasterInventService {

    @Autowired
    private MasterInventRepository masterInvertRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public MasterInventService(MasterInventRepository masterInvertRepository) {
        this.masterInvertRepository = masterInvertRepository;
    }

    public List<MasterInvent> findAll(){return masterInvertRepository.findAll();}

    public List<Object[]> findAllObj(){
        Query query = entityManager.createNativeQuery("select b.nombre as codigo_concil,a.fecha_conciliacion,c.nombre as codigo_conta,a.fecha_cargue_contable,a.estado_cargue_conciliacion,a.estado_cargue_cargue_contable,a.aplica_semana, a.id\n" +
                        "from preciso_maestro_inventarios a\n" +
                        "left join preciso_conciliaciones b on a.codigo_conciliacion=b.id\n" +
                        "left join preciso_rutas_contables c on a.codigo_cargue_contable=c.id_rc order by b.nombre, a.fecha_conciliacion,c.nombre,a.fecha_cargue_contable ");
        return query.getResultList();
    }

    public List<MasterInvent> findByConciliacionMaster(String conciliacion){
        Query query = entityManager.createNativeQuery(
                "select a.* from preciso_maestro_inventarios a\n" +
                        "inner join preciso_conciliaciones b on a.codigo_conciliacion=b.id where b.nombre = ? ", MasterInvent.class);
        query.setParameter(1, conciliacion);
        return query.getResultList();
    }

    public List<MasterInvent> findByContableMaster(String contable){
        Query query = entityManager.createNativeQuery("select a.* from preciso_maestro_inventarios a\n" +
                        "inner join preciso_rutas_contables b on a.codigo_cargue_contable=b.id_rc where b.nombre = ? ", MasterInvent.class);
        query.setParameter(1, contable);
        return query.getResultList();
    }

    public List<Conciliation> getAllConcil(){
        Query query = entityManager.createNativeQuery("select * from preciso_conciliaciones where activo = 1 ", Conciliation.class);
        return query.getResultList();
    }

    public List<MasterInvent> findByConcilConta(String conciliacion, String contable,LocalDate fecha){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Query query = entityManager.createNativeQuery("select a.* from preciso_maestro_inventarios a\n" +
                "inner join (select * from preciso_conciliaciones where upper(nombre) = ? ) b on a.codigo_conciliacion=b.id\n" +
                "inner join (select * from preciso_rutas_contables where upper(nombre) = ?) c on a.codigo_cargue_contable=c.id_rc\n" +
                "where a.fecha_conciliacion = ? ", MasterInvent.class);
        query.setParameter(1, conciliacion.toUpperCase());
        query.setParameter(2, contable.toUpperCase());
        query.setParameter(3, fecha.format(formatter));
        return query.getResultList();
    }

    public boolean isHoliday(LocalDate date){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Query query = entityManager.createNativeQuery("select * from CARGASMASIVAS.dbo.FECHAS_HABILES where FechaHabil = ? and DiaHabil = 0 ");
        query.setParameter(1,date.format(formatter));
        System.out.println(date.format(formatter));
        if(query.getResultList().size()>0)
            return true;
        else
            return false;
    }

    public LocalDate getCalculateRuteDate(Conciliation conciliation, LocalDate date)
    {
        SourceSystem sourceSystem = conciliation.getRutaContable().getSfrc();
        int soporte= 10;
        while (true) {
            if(soporte == 0) {
                System.out.println("--------------------------------------------SALIO POR ERROR------------------------------------------");
                break;
            }
            else {
                soporte--;
            }
            DayOfWeek dayOfWeek = date.getDayOfWeek();
            boolean dayApplies = false;
            switch (dayOfWeek) {
                case MONDAY:
                    dayApplies = sourceSystem.isLunes();
                    break;
                case TUESDAY:
                    dayApplies = sourceSystem.isMartes();
                    break;
                case WEDNESDAY:
                    dayApplies = sourceSystem.isMiercoles();
                    break;
                case THURSDAY:
                    dayApplies = sourceSystem.isJueves();
                    break;
                case FRIDAY:
                    dayApplies = sourceSystem.isViernes();
                    break;
                case SATURDAY:
                    dayApplies = sourceSystem.isSabado();
                    break;
                case SUNDAY:
                    dayApplies = sourceSystem.isDomingo();
                    break;
            }
            if (!dayApplies || (!sourceSystem.isFestivo() && isHoliday(date))) {
                date = date.minusDays(1);
                continue;
            }
            else {
                break;
            }
        }
        return date;
    }

    public void appendInsertValues(StringBuilder insertQuery, Conciliation conciliation, LocalDate date, DateTimeFormatter formatter) {
        String formattedDateContable = getCalculateRuteDate(conciliation,date).format(formatter);
        String formattedDate = date.format(formatter);
        insertQuery.append("(")
                .append(conciliation.getId()).append(", '")
                .append(formattedDate).append("', ")
                .append(conciliation.getRutaContable().getId()).append(", '")
                .append(formattedDateContable).append("'),");
    }
    public LocalDate calculateMonthlyDate(Conciliation conciliation, LocalDate monthStart) {
        LocalDate lastDayOfMonth = monthStart.withDayOfMonth(monthStart.lengthOfMonth()); // Último día del mes
        return lastDayOfMonth;
    }

    public boolean appliesOnDay(Conciliation conciliation, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        switch (dayOfWeek) {
            case MONDAY: return conciliation.isLunes();
            case TUESDAY: return conciliation.isMartes();
            case WEDNESDAY: return conciliation.isMiercoles();
            case THURSDAY: return conciliation.isJueves();
            case FRIDAY: return conciliation.isViernes();
            case SATURDAY: return conciliation.isSabado();
            case SUNDAY: return conciliation.isDomingo();
            default: return false;
        }
    }

    public void generateDates() {
        LocalDate today = LocalDate.now();
        LocalDate twoMonthsLater = today.plusMonths(11);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayStr = today.format(formatter);

        // Borrar registros existentes
        String deleteQuery = "DELETE FROM preciso_maestro_inventarios WHERE fecha_conciliacion >= :today";
        entityManager.createNativeQuery(deleteQuery)
                .setParameter("today", todayStr)
                .executeUpdate();

        List<Conciliation> lista = getAllConcil();

        for (Conciliation conciliation : lista) {
            StringBuilder insertQuery = new StringBuilder("INSERT INTO preciso_maestro_inventarios (codigo_conciliacion, fecha_conciliacion, codigo_cargue_contable, fecha_cargue_contable) VALUES ");

            if ("Diario".equalsIgnoreCase(conciliation.getPeriodicidad())) {
                for (LocalDate date = today; !date.isAfter(twoMonthsLater); date = date.plusDays(1)) {
                    if(conciliation.isFestivo() == true || (conciliation.isFestivo() == false && isHoliday(date) == false))
                        appendInsertValues(insertQuery, conciliation, date, formatter);
                }
            } else if ("Semanal".equalsIgnoreCase(conciliation.getPeriodicidad())) {
                List<String> data= new ArrayList<>();
                for (LocalDate date = today; !date.isAfter(twoMonthsLater); date = date.plusDays(1)) {
                    if (appliesOnDay(conciliation, date)) {
                        if(conciliation.isFestivo() == true || (conciliation.isFestivo() == false && isHoliday(date) == false)) {
                            appendInsertValues(insertQuery, conciliation, date, formatter);
                            data.add(conciliation.getId()+"_"+date);
                        }
                    }
                }
                for (LocalDate monthStart = today.withDayOfMonth(1); !monthStart.isAfter(twoMonthsLater); monthStart = monthStart.plusMonths(1)) {
                    LocalDate lastDayOfMonth = calculateMonthlyDate(conciliation, monthStart);
                    if (!lastDayOfMonth.isAfter(twoMonthsLater) && !data.contains(conciliation.getId()+"_"+lastDayOfMonth)) {
                        appendInsertValues(insertQuery, conciliation, lastDayOfMonth, formatter);
                    }
                }
            } else if ("Mensual".equalsIgnoreCase(conciliation.getPeriodicidad())) {
                for (LocalDate monthStart = today.withDayOfMonth(1); !monthStart.isAfter(twoMonthsLater); monthStart = monthStart.plusMonths(1)) {
                    LocalDate lastDayOfMonth = calculateMonthlyDate(conciliation, monthStart);
                    if (!lastDayOfMonth.isAfter(twoMonthsLater)) {
                        appendInsertValues(insertQuery, conciliation, lastDayOfMonth, formatter);
                    }
                }
            }

            // Ejecutar la consulta
            if (insertQuery.length() > 0) {
                String finalInsertQuery = insertQuery.substring(0, insertQuery.length() - 1);
                entityManager.createNativeQuery(finalInsertQuery).executeUpdate();
                updateLoads();
            }
        }
    }

    public void updateLoads(){
        for (MasterInvent master : findAll()) {
            Query query = entityManager.createNativeQuery("UPDATE preciso_maestro_inventarios SET estado_cargue_conciliacion = 1\n" +
                    "WHERE EXISTS ( SELECT top 1 * FROM preciso_rc_"+master.getCodigoConciliacion().getId()+" WHERE periodo_preciso = ? );");
            query.setParameter(1, master.getFechaConciliacion());
            query.executeUpdate();
        }
    }

    public List<Conciliation> findByConciliacion(String conciliacion){
        Query query = entityManager.createNativeQuery(
                "select a.* from preciso_conciliaciones a where upper(a.nombre) = ? ", Conciliation.class);
        query.setParameter(1, conciliacion.toUpperCase());
        return query.getResultList();
    }

    public List<AccountingRoute> findByContable(String conciliacion){
        Query query = entityManager.createNativeQuery(
                "select a.* from preciso_rutas_contables a where upper(a.nombre) = ? ", AccountingRoute.class);
        query.setParameter(1, conciliacion.toUpperCase());
        return query.getResultList();
    }

    public MasterInvent findAllById(Long id){
        return masterInvertRepository.findAllById(id);
    }

    public MasterInvent modificar(MasterInvent master){
        masterInvertRepository.save(master);
       return master;
    }

    public void eliminar(MasterInvent master){
        masterInvertRepository.delete(master);
    }

    public Page<MasterInvent> getAll(Pageable pageable){
        return masterInvertRepository.findAll(pageable);
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Prametros Generales");
        insert.setFecha(today);
        insert.setInput("Maestro Inventarios");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<Object[]> findByFilter(String value, String filter) {
        List<Object[]> list=new ArrayList<Object[]>();
        switch (filter)
        {
            case "Conciliación":
                Query quer = entityManager.createNativeQuery("select b.nombre as codigo_concil,a.fecha_conciliacion,c.nombre as codigo_conta,a.fecha_cargue_contable,a.estado_cargue_conciliacion,a.estado_cargue_cargue_contable,a.aplica_semana, a.id\n" +
                        "from preciso_maestro_inventarios a\n" +
                        "left join preciso_conciliaciones b on a.codigo_conciliacion=b.id\n" +
                        "left join preciso_rutas_contables c on a.codigo_cargue_contable=c.id_rc where b.nombre like ? order by b.nombre, a.fecha_conciliacion,c.nombre,a.fecha_cargue_contable ");
                quer.setParameter(1,value);
                list = quer.getResultList();
                break;
            case "Contable":
                Query query = entityManager.createNativeQuery("select b.nombre as codigo_concil,a.fecha_conciliacion,c.nombre as codigo_conta,a.fecha_cargue_contable,a.estado_cargue_conciliacion,a.estado_cargue_cargue_contable,a.aplica_semana, a.id\n" +
                        "from preciso_maestro_inventarios a\n" +
                        "left join preciso_conciliaciones b on a.codigo_conciliacion=b.id\n" +
                        "left join preciso_rutas_contables c on a.codigo_cargue_contable=c.id_rc where c.nombre like ? order by b.nombre, a.fecha_conciliacion,c.nombre,a.fecha_cargue_contable ");
                query.setParameter(1,value);
                list= query.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue exitoso plantilla Maestro Inventarios");
            else
                loadAudit(user,"Cargue Fallido plantilla Maestro Inventarios");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<MasterInvent> toInsert = new ArrayList<>();
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutivo=0;
                    DataFormatter formatter = new DataFormatter();
                    String cellConciliacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellFechaConciliacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellContable = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellFechaContable = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellEstadoConciliacion = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    String cellEstadoContable = formatter.formatCellValue(row.getCell(consecutivo++)).trim();
                    //String cellAplicaSemana = formatter.formatCellValue(row.getCell(consecutivo++)).trim();

                    if (cellConciliacion.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "La Conciliación no puede estar vacio.";
                        lista.add(log);
                    }
                    else if(findByConciliacion(cellConciliacion).size()==0)
                    {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "La Conciliación no encuentra creada, valida los datos.";
                        lista.add(log);
                    }
                    if (cellFechaConciliacion.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Fecha Conciliación no puede estar vacio.";
                        lista.add(log);
                    }
                    else
                    {
                        try{
                            dateFormat.parse(cellFechaConciliacion);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(1);
                            log[2] = "La Fecha coniliación deb estar en formato yyyy-MM-dd.";
                            lista.add(log);
                        }
                    }
                    if (cellContable.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El Contable no puede estar vacio.";
                        lista.add(log);
                    }
                    else if(findByContable(cellContable).size()==0)
                    {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El Contable no encuentra creado, valida los datos.";
                        lista.add(log);
                    }
                    if (cellFechaContable.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "La Fecha Contable no puede estar vacio.";
                        lista.add(log);
                    }
                    else
                    {
                        try{
                            dateFormat.parse(cellFechaContable);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(1);
                            log[2] = "La Fecha contable deb estar en formato yyyy-MM-dd.";
                            lista.add(log);
                        }
                    }
                    /*if (!cellAplicaSemana.trim().equalsIgnoreCase("si") && !cellAplicaSemana.trim().equalsIgnoreCase("no")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Aplica Semana debe contener un valor de 'Si' o 'No'.";
                        lista.add(log);
                    }*/

                    if (lista.isEmpty()) {
                        try{
                            MasterInvent masterInvent = new MasterInvent();
                            List<MasterInvent> masterInventsSearch= findByConcilConta(cellConciliacion,cellContable,LocalDate.parse(cellFechaConciliacion, dateFormat));
                            if(!masterInventsSearch.isEmpty())
                                masterInvent= masterInventsSearch.get(0);
                            masterInvent.setCodigoConciliacion(findByConciliacion(cellConciliacion).get(0));
                            masterInvent.setCodigoCargueContable(findByContable(cellContable).get(0));
                            masterInvent.setFechaConciliacion(LocalDate.parse(cellFechaConciliacion, dateFormat));
                            masterInvent.setFechaCargueContable(LocalDate.parse(cellFechaContable, dateFormat));
                            //masterInvent.setAplica_semana(cellAplicaSemana.equalsIgnoreCase("si") ? true : false);
                            toInsert.add(masterInvent);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 5) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            masterInvertRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }
}
