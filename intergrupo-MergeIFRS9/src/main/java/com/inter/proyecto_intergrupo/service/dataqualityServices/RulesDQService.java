package com.inter.proyecto_intergrupo.service.dataqualityServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.dataquality.PointRulesDQRepository;
import com.inter.proyecto_intergrupo.repository.dataquality.RulesDQRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.jni.Local;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.LocalDate;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class RulesDQService {

    @Autowired
    private RulesDQRepository rulesDQRepository;

    @Autowired
    private PointRulesDQRepository pointRulesDQRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    public RulesDQService(RulesDQRepository rulesDQRepository,PointRulesDQRepository pointRulesDQRepository) {
        this.rulesDQRepository = rulesDQRepository;
        this.pointRulesDQRepository = pointRulesDQRepository;
    }

    public List<String[]> getAllData1(String periodo){
        Query consulta = entityManager.createNativeQuery("select count(porcentaje_cumplimiento) as conteo,avg(porcentaje_cumplimiento) as porce " +
                "from nexco_puntuacion_validacion_dq where periodo = ? ");
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<String[]> getAllDataFallidos(String periodo){
        Query consulta = entityManager.createNativeQuery("select count(porcentaje_cumplimiento) as conteo,avg(porcentaje_cumplimiento) as porce " +
                "from nexco_puntuacion_validacion_dq where periodo = ? and porcentaje_cumplimiento < porcentaje_umbral_minimo");
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<String[]> getAllDataCumplidos(String periodo){
        Query consulta = entityManager.createNativeQuery("select count(porcentaje_cumplimiento) as conteo,avg(porcentaje_cumplimiento) as porce " +
                "from nexco_puntuacion_validacion_dq where periodo = ? and porcentaje_cumplimiento >= porcentaje_umbral_minimo");
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }
    public List<String[]> getAllData2(String periodo){
        Query consulta = entityManager.createNativeQuery("select tipo_principio,tipo_regla,identificador_secuencial_legacy,nombre_data_system,nombre_fisico_objeto,nombre_fisico_campo,avg(porcentaje_cumplimiento) as porce " +
                "from nexco_puntuacion_validacion_dq where periodo = ? and porcentaje_cumplimiento < porcentaje_umbral_minimo group by tipo_principio,tipo_regla,identificador_secuencial_legacy,nombre_data_system,nombre_fisico_objeto,nombre_fisico_campo");
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public int getCountAll(){
        return rulesDQRepository.findAll().size();
    }

    public List<RulesDQ> getAllList(){
        return rulesDQRepository.findAll();
    }

    public List<PointRulesDQ> getAllListPoint(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_puntuacion_validacion_dq where periodo = ?",PointRulesDQ.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<PointRulesDQ> getAllListPointIns(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_puntuacion_validacion_dq where periodo = ? and porcentaje_cumplimiento < porcentaje_umbral_minimo",PointRulesDQ.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public ArrayList<String[]> saveFileBD(InputStream file) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list = validarPlantilla(rows);
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<RulesDQ> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellTipoPrincipio = formatter.formatCellValue(row.getCell(0)).trim();
                String cellTipoRegla = formatter.formatCellValue(row.getCell(1)).trim();
                String cellTabla = formatter.formatCellValue(row.getCell(2)).trim();
                String cellColumna= formatter.formatCellValue(row.getCell(3)).trim();
                String cellFormato = formatter.formatCellValue(row.getCell(4)).trim();
                String cellLongitud = formatter.formatCellValue(row.getCell(5)).trim();
                String cellIdentificador = formatter.formatCellValue(row.getCell(6)).trim();
                    String cellFichero = formatter.formatCellValue(row.getCell(7)).trim();
                String cellContraparte= formatter.formatCellValue(row.getCell(8)).trim();
                String cellCampo = formatter.formatCellValue(row.getCell(9)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(10)).trim();
                String cellUmbralMinimo= formatter.formatCellValue(row.getCell(11)).trim();
                String cellUmbralObjetivo= formatter.formatCellValue(row.getCell(12)).trim();
                String cellDescripcion= formatter.formatCellValue(row.getCell(13)).trim();
                String cellVarMin= formatter.formatCellValue(row.getCell(14)).trim();
                String cellVarMax= formatter.formatCellValue(row.getCell(15)).trim();

                try {
                    XSSFCell cell1 = row.getCell(0);
                    cell1.setCellType(CellType.STRING);
                    cellTipoPrincipio = formatter.formatCellValue(cell1).replace(" ", "");
                    Integer.parseInt(cellTipoPrincipio);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(0);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico entero";
                    lista.add(log);
                }
                try {
                    XSSFCell cell1 = row.getCell(1);
                    cell1.setCellType(CellType.STRING);
                    cellTipoRegla = formatter.formatCellValue(cell1).replace(" ", "");
                    Integer.parseInt(cellTipoRegla);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(1);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico entero";
                    lista.add(log);
                }

                if (cellTabla.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El campo Tabla no puede estar vacío";
                    lista.add(log1);
                }

                if ( cellColumna.trim().length() != 0 && (cellTipoPrincipio.equals("1") || cellTipoPrincipio.equals("2"))){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El campo Columna debe estar " +
                            "vacio";
                    lista.add(log1);
                }
                if (cellColumna.trim().length() == 0 && !cellTipoPrincipio.equals("1") && !cellTipoPrincipio.equals("2"))
                {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(3);
                        log1[2] = "El campo Columna no puede estar " +
                                "vacio";
                        lista.add(log1);
                }

                if ((cellFormato.trim().equals("N/A")||cellFormato.trim().equals("Alfanúmerico")||cellFormato.trim().equals("Númerico")||cellFormato.trim().equals("Alfabetico")||cellFormato.trim().equals("Fecha"))==false) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El campo Formato debe contener uno de estos valores: (Alfanúmerico, Númerico, Alfabetico, Fecha, N/A)";
                    lista.add(log1);
                }

                if (cellLongitud.length()!=0)
                {
                    try {
                        XSSFCell cell1 = row.getCell(5);
                        cell1.setCellType(CellType.STRING);
                        cellLongitud = formatter.formatCellValue(cell1).replace(" ", "");
                        Integer.parseInt(cellLongitud);
                    } catch (Exception e) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "Tipo dato incorrecto, debe ser un númerico entero o N/A";
                        lista.add(log);
                    }
                }

                if (cellIdentificador.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El campo Identificador no puede estar vacio";
                    lista.add(log1);
                }

                if (cellFichero.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El campo Fichero no puede estar vacio";
                    lista.add(log1);
                }

                try {
                    XSSFCell cell1 = row.getCell(12);
                    cell1.setCellType(CellType.STRING);
                    cellUmbralObjetivo = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellUmbralObjetivo);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(12);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }
                try {
                    XSSFCell cell0 = row.getCell(11);
                    cell0.setCellType(CellType.STRING);
                    cellUmbralMinimo = formatter.formatCellValue(cell0).replace(" ", "");
                    Double.parseDouble(cellUmbralMinimo);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(11);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                try {
                    if (Double.parseDouble(cellUmbralMinimo)>Double.parseDouble(cellUmbralObjetivo)) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(11);
                        log1[2] = "El campo Umbral Minimo no puede ser mayor al campo Umbral Objetivo";
                        lista.add(log1);
                    }
                } catch (Exception e) {

                }

                if (cellDescripcion.trim().length() == 0 && cellTipoPrincipio.trim().equals("6") && cellTipoRegla.trim().equals("4")) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El campo Descripción no puede estar vacío";
                    lista.add(log1);
                }

                try {

                    XSSFCell cell = row.getCell(14);
                    cell.setCellType(CellType.STRING);
                    cellVarMin = formatter.formatCellValue(cell).replace(" ", "");

                    XSSFCell cell1 = row.getCell(15);
                    cell1.setCellType(CellType.STRING);
                    cellVarMax = formatter.formatCellValue(cell1).replace(" ", "");

                    if (Double.parseDouble(cellVarMin)>Double.parseDouble(cellVarMax)) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(14);
                        log1[2] = "El campo Porcentaje de variación minima no puede ser mayor al campo Porcentaje de variación maxima";
                        lista.add(log1);
                    }

                } catch (Exception e) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14) +" - "+ CellReference.convertNumToColString(15);
                    log1[2] = "El campo Porcentaje de variación minima y el campo Porcentaje de variación maxima deben ser númerico decimal";
                    lista.add(log1);
                }

                RulesDQ rule = new RulesDQ();
                rule.setTabla(cellTabla);
                rule.setColumna(cellColumna);
                rule.setFormato(cellFormato);
                rule.setIdentificador(cellIdentificador);
                rule.setFichero(cellFichero);
                rule.setCampo(cellCampo);
                rule.setValor(cellValor);
                rule.setDescripcion(cellDescripcion);
                rule.setLongitud(cellLongitud);
                rule.setContraparte(cellContraparte);
                try{
                    rule.setTipoPrincipio(Integer.parseInt(cellTipoPrincipio));
                    rule.setTipoRegla(Integer.parseInt(cellTipoRegla));
                    rule.setUmbralMinimo(Double.parseDouble(cellUmbralMinimo));
                    rule.setUmbralObjetivo(Double.parseDouble(cellUmbralObjetivo));
                    rule.setVariacionMin(Double.parseDouble(cellVarMin));
                    rule.setVariacionMax(Double.parseDouble(cellVarMax));

                }catch (Exception e){
                    e.printStackTrace();
                }
                toInsert.add(rule);
            }
        }
        if(lista.size()!=0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size()*11)-lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_reglasdq");
            delete.executeUpdate();
            rulesDQRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public void processRules(String periodo,String folder)
    {
        String[] fecha = periodo.split("-");
        LocalDate lastDay = LocalDate.of(Integer.parseInt(fecha[0]),Integer.parseInt(fecha[1]),1).plusMonths(1).minusDays(1);
        while (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY)
        {
            lastDay = lastDay.minusDays(1);
        }
        LocalDateTime localDateTime = lastDay.atStartOfDay();
        Date date = Timestamp.valueOf(localDateTime);

        for(RulesDQ rule: getAllFolder(folder,periodo)){
            if(validateTable(rule,periodo))
            {
                if (rule.getTipoPrincipio() == 1) {
                    if (rule.getTipoRegla() == 1) {
                        rule_1_1(rule, date, periodo);
                    } else if (rule.getTipoRegla() == 2) {
                        rule_1_2(rule, date, periodo);
                    }
                } else if (rule.getTipoPrincipio() == 2) {
                    if (rule.getTipoRegla() == 1) {
                        rule_2_1(rule, date, periodo);
                    }
                } else if (rule.getTipoPrincipio() == 3) {
                    if (rule.getTipoRegla() == 1) {
                        rule_3_1(rule, date, periodo);
                    } else if (rule.getTipoRegla() == 2) {
                        rule_3_2(rule, date, periodo);
                    } else if (rule.getTipoRegla() == 5) {
                        rule_3_5(rule, date, periodo);
                    }
                } else if (rule.getTipoPrincipio() == 4) {
                    if (rule.getTipoRegla() == 2) {
                        rule_4_2(rule, date, periodo);
                    }
                } else if (rule.getTipoPrincipio() == 5) {
                    if (rule.getTipoRegla() == 2) {
                        rule_5_2(rule, date, periodo);
                    }
                }
                else if (rule.getTipoPrincipio() == 6) {
                    if (rule.getTipoRegla() == 4) {
                        rule_6_4(rule, date, periodo);
                    }
                }
            }
            else
            {
                insertValidation(rule,date,0.0,1.0,periodo);
            }
        }
    }

    public boolean validateTable(RulesDQ rule, String periodo)
    {
        Query consulta = entityManager.createNativeQuery("select * from "+ rule.getTabla() +" where periodo = ? ");
        consulta.setParameter(1,periodo);
        if(consulta.getResultList().isEmpty())
            return false;
        else
            return true;
    }

    public void rule_1_1(RulesDQ rule,Date date,String periodo)
    {
        Calendar calendar = Calendar.getInstance();
        double numerador = 0.0;
        try {
            Date dateProcess = StatusInfoRepository.findByStatusAndPeriodo(rule.getTabla(), periodo).getFecha();
            calendar.setTime(dateProcess);
            if(calendar.get(Calendar.DAY_OF_MONTH)>=1 && calendar.get(Calendar.DAY_OF_MONTH) <= 10)
            {
                numerador=1.0;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        insertValidation(rule,date,numerador,1.0,periodo);
    }

    public void rule_1_2(RulesDQ rule,Date date,String periodo)
    {
        double numerador = 0.0;
        Query consulta = entityManager.createNativeQuery("select * from "+ rule.getTabla() +" where periodo = ? ");
        consulta.setParameter(1,periodo);
        List result = consulta.getResultList();
        if(!result.isEmpty())
        {
            numerador = 1.0;
        }
        insertValidation(rule,date,numerador,1.0,periodo);
    }

    public void rule_2_1_ext(RulesDQ rule,Date date,String periodo)
    {
        try {
            String numerador = "0.0";
            String denominador = "0.0";
            String queryFinal = "";
            Query consulta = entityManager.createNativeQuery("SELECT COLUMN_NAME FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME = ?");
            consulta.setParameter(1, rule.getTabla());
            List<String> result = consulta.getResultList();

            for (String columna : result) {
                queryFinal = queryFinal + "COUNT(CASE WHEN "+columna+" IS NOT NULL THEN 1 ELSE NULL END)+";
            }
            queryFinal = queryFinal.substring(0, queryFinal.length() - 1);

            Query consulta1 = entityManager.createNativeQuery("SELECT " + queryFinal + " FROM " + rule.getTabla() +" WHERE periodo = ?");
            consulta1.setParameter(1,periodo);
            numerador = consulta1.getResultList().get(0).toString();

            Query consulta2 = entityManager.createNativeQuery("select COUNT(*) * " +result.size() + " FROM " + rule.getTabla() + " WHERE periodo = ?");
            consulta2.setParameter(1,periodo);
            denominador = consulta2.getResultList().get(0).toString();
            insertValidation(rule,date,Double.parseDouble(numerador),Double.parseDouble(denominador),periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }
    public void rule_2_1(RulesDQ rule,Date date,String periodo)
    {
        try {
            Query consulta1 = entityManager.createNativeQuery("SELECT top 1 * FROM " + rule.getTabla() +" WHERE periodo = ?");
            consulta1.setParameter(1,periodo);

            if(consulta1.getResultList().isEmpty())
                insertValidation(rule,date,0.0,1.0,periodo);
            else
                insertValidation(rule,date,1.0,1.0,periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void rule_3_1(RulesDQ rule,Date date,String periodo)
    {
        try {
            String numerador = "0.0";
            String denominador = "0.0";

            Query consulta1 = entityManager.createNativeQuery("SELECT COUNT(CASE WHEN "+rule.getColumna()+" IS NOT NULL THEN 1 ELSE NULL END) FROM " + rule.getTabla() +" WHERE periodo = ?");
            consulta1.setParameter(1,periodo);
            List lista1 = consulta1.getResultList();
            if(lista1.size()>0)
                numerador = lista1.get(0).toString();

            Query consulta2 = entityManager.createNativeQuery("select COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ?");
            consulta2.setParameter(1,periodo);
            denominador = consulta2.getResultList().get(0).toString();
            insertValidation(rule,date,Double.parseDouble(numerador),Double.parseDouble(denominador),periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void rule_3_2(RulesDQ rule,Date date,String periodo)
    {
        try {
            String numerador = "0.0";
            String denominador = "0.0";
            String query ="";

            if(!rule.getFormato().trim().equals("N/A"))
            {
                if(rule.getFormato().trim().equals("Alfanúmerico"))
                {
                    query= " "+rule.getColumna()+" NOT LIKE '%[^a-zA-Z0-9]%' ";
                }
                else if(rule.getFormato().trim().equals("Númerico"))
                {
                    query= " ISNUMERIC("+rule.getColumna()+") = 1 ";
                }
                else if(rule.getFormato().trim().equals("Fecha"))
                {
                    query= " ISDATE("+rule.getColumna()+") = 1 ";
                }
                else if(rule.getFormato().trim().equals("Alfabetico"))
                {
                    query= " "+rule.getColumna()+" NOT LIKE '%[^a-zA-Z]%' ";
                }
            }
            if(rule.getLongitud().length()!=0)
            {
                if(query.length()!=0)
                {
                    query = query + " AND ";
                }
                query = query + "LEN("+rule.getColumna()+") = "+rule.getLongitud();
            }
            Query consulta1 = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + rule.getTabla() +" WHERE periodo = ? AND "+ query);
            consulta1.setParameter(1,periodo);
            numerador = consulta1.getResultList().get(0).toString();

            Query consulta2 = entityManager.createNativeQuery("select COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ?");
            consulta2.setParameter(1,periodo);
            denominador = consulta2.getResultList().get(0).toString();
            insertValidation(rule,date,Double.parseDouble(numerador),Double.parseDouble(denominador),periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void rule_3_5(RulesDQ rule,Date date,String periodo)
    {
        try {
            String numerador = "0.0";
            String denominador = "0.0";

            if(!rule.getContraparte().trim().equals("")) {
                Query consulta1 = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ? AND " + rule.getColumna() + " IN (select distinct " + rule.getCampo() + " from " + rule.getContraparte() + ")");
                consulta1.setParameter(1, periodo);
                numerador = consulta1.getResultList().get(0).toString();
            }
            else{
                Query consulta1 = entityManager.createNativeQuery("SELECT COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ? AND " + rule.getColumna() + " IN (" + rule.getValor()+ ")");
                consulta1.setParameter(1, periodo);
                numerador = consulta1.getResultList().get(0).toString();
            }

            Query consulta2 = entityManager.createNativeQuery("select COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ?");
            consulta2.setParameter(1,periodo);
            denominador = consulta2.getResultList().get(0).toString();
            insertValidation(rule,date,Double.parseDouble(numerador),Double.parseDouble(denominador),periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void rule_4_2(RulesDQ rule,Date date,String periodo)
    {
        try {
            double numerador = 0.0;
            String denominador = "0.0";

            Query consulta1 = entityManager.createNativeQuery("SELECT "+rule.getColumna()+" FROM "+rule.getTabla()+" WHERE periodo = ? group by "+rule.getColumna());
            consulta1.setParameter(1,periodo);
            numerador = consulta1.getResultList().size();

            Query consulta2 = entityManager.createNativeQuery("select COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ?");
            consulta2.setParameter(1,periodo);
            denominador = consulta2.getResultList().get(0).toString();
            insertValidation(rule,date,numerador,Double.parseDouble(denominador),periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void rule_5_2(RulesDQ rule,Date date,String periodo)
    {
        try {
            double numerador = 0.0;
            String denominador = "0.0";

            if(rule.getContraparte().trim().equals("cuentas_puc")) {
                Query consulta1 = entityManager.createNativeQuery("SELECT * FROM " + rule.getTabla() + " as a \n" +
                        "inner join (select nucta, codicons46 from " + rule.getContraparte() + " where empresa ='0060') as b \n" +
                        "ON a." + rule.getColumna() + "=b." + rule.getCampo() + " and a.cod_neocon=b.CODICONS46 where a.periodo = ?");
                consulta1.setParameter(1, periodo);
                numerador = consulta1.getResultList().size();
            }
            else if(rule.getContraparte().trim().equals("nexco_cuentas_neocon"))
            {
                Query consulta1 = entityManager.createNativeQuery("SELECT * FROM "+rule.getTabla()+" as a \n" +
                        "inner join (select "+rule.getCampo()+" from "+rule.getContraparte()+" where entrada ='S') as b\n" +
                        "ON a."+rule.getColumna()+"=b."+rule.getCampo()+"  where a.periodo = ?");
                consulta1.setParameter(1, periodo);
                numerador = consulta1.getResultList().size();
            }

            Query consulta2 = entityManager.createNativeQuery("select COUNT(*) FROM " + rule.getTabla() + " WHERE periodo = ?");
            consulta2.setParameter(1,periodo);
            denominador = consulta2.getResultList().get(0).toString();
            insertValidation(rule,date,numerador,Double.parseDouble(denominador),periodo);
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void rule_6_4(RulesDQ rule,Date date,String periodo)
    {
        try {
            LocalDate fechaAnterior = LocalDate.parse(periodo+"-01");
            fechaAnterior=fechaAnterior.minusMonths(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
            String fechaFinal=formatter.format(fechaAnterior);

            String query1="";
            String query2="";
            String[] divFils=rule.getColumna().replace("A.","").replace(" ","").split(",");
            for (String part : divFils)
            {
                if(query1.length()!=0)
                    query1=query1+"AND";
                query1=query1+" A."+part+" = B."+part+" ";
            }

            if(rule.getValor().trim().length()!=0) {
                query2=" AND "+rule.getValor().replace(","," OR ").replace("-"," AND ");
            }

            Query consulta1 = entityManager.createNativeQuery("SELECT "+rule.getColumna()+", A."+rule.getCampo()+" AS valor1, ISNULL(B."+rule.getCampo()+",0) AS valor2, ISNULL((A."+rule.getCampo()+"-B."+rule.getCampo()+")/B."+rule.getCampo()+"*100 ,100) AS calculo \n" +
                    "FROM (SELECT "+rule.getColumna()+",sum("+rule.getCampo()+") AS valor FROM "+rule.getTabla()+" AS A WHERE A.periodo=? "+query2+" GROUP BY "+rule.getColumna()+") AS A\n" +
                    "LEFT JOIN (SELECT "+rule.getColumna()+",sum("+rule.getCampo()+") AS valor FROM "+rule.getTabla()+" AS A WHERE A.periodo=? "+query2+" GROUP BY "+rule.getColumna()+") AS B\n" +
                    "ON "+query1+"\n");
            consulta1.setParameter(1,periodo);
            consulta1.setParameter(2,fechaFinal);
            List<Object[]> lista = consulta1.getResultList();

            double startPor=0.0;
            double startNum=0.0;
            double startDen=0.0;
            double startNumP=0.0;
            double startDenP=0.0;

            for (Object[] parte: lista) {
                double numerador = 0.0;
                startDenP=startDenP+1.0;
                if(Double.parseDouble(parte[4].toString()) > rule.getVariacionMin() && Double.parseDouble(parte[4].toString())<rule.getVariacionMax()) {
                    numerador = 1.0;
                    startNumP=startNumP+1.0;
                }
                if(rule.getValor().trim().length()!=0) {
                    insertValidation(rule, date, numerador, 1.0, periodo, Double.parseDouble(parte[4].toString()), Double.parseDouble(parte[2].toString()), Double.parseDouble(parte[3].toString()), rule.getCampo());
                }else{
                    startPor=startPor+Double.parseDouble(parte[4].toString());
                    startNum=startNum+Double.parseDouble(parte[2].toString());
                    startDen=startDen+Double.parseDouble(parte[3].toString());
                }
            }

            if(rule.getValor().trim().length()==0) {
                insertValidation(rule, date, startNumP, startDenP, periodo, startPor/startDenP, startNum, startDen, rule.getCampo());
            }
        }
        catch (Exception e)
        {
            insertValidation(rule,date,0.0,1.0,periodo);
            e.printStackTrace();
        }
    }

    public void insertValidation(RulesDQ rule,Date date,Double numerador, double denominador,String periodo)
    {
        PointRulesDQ point = new PointRulesDQ();
        point.setFechaCierre(date);
        point.setFechaEjecucion(new Date());
        point.setIdentificadorPais("COL");
        point.setIdentificadorUuaa("CSRU");
        point.setNombreDataSystem(rule.getTabla());
        point.setDescripcionDesglose(rule.getDescripcion());
        point.setIdentificadorSecuencialLegacy(rule.getIdentificador());
        point.setTipoPrincipio(String.valueOf(rule.getTipoPrincipio()));
        point.setTipoRegla(String.valueOf(rule.getTipoRegla()));
        point.setNombreFisicoObjeto(rule.getFichero());
        if(rule.getTipoPrincipio()==4  && rule.getTipoRegla() ==2)
            point.setNombreFisicoCampo("");
        else
            point.setNombreFisicoCampo(rule.getColumna());
        point.setPorcentajeCumplimiento((numerador/denominador)*100);
        point.setNumeradorCumplimiento(numerador);
        point.setDenominadorCumplimiento(denominador);
        point.setTipoFrecuenciaEjecucion("Monthly");
        point.setPorcentajeUmbralMinimo(rule.getUmbralMinimo());
        point.setPorcentajeUmbralObjetivo(rule.getUmbralObjetivo());
        point.setNombreCampoImporte("");
        point.setPorcentajeCumplimientoSaldo(0.0);
        point.setImporteNumerador(0.0);
        point.setImporteDenominador(0.0);
        point.setPeriodo(periodo);
        pointRulesDQRepository.save(point);
    }
    public void insertValidation(RulesDQ rule,Date date,Double numerador, double denominador,String periodo,double porcentajeSaldo, double importeNumerador,double importeDenominador,String nombreCampo)
    {
        PointRulesDQ point = new PointRulesDQ();
        point.setFechaCierre(date);
        point.setFechaEjecucion(new Date());
        point.setIdentificadorPais("COL");
        point.setIdentificadorUuaa("CSRU");
        point.setNombreDataSystem(rule.getTabla());
        point.setDescripcionDesglose(rule.getDescripcion());
        point.setIdentificadorSecuencialLegacy(rule.getIdentificador());
        point.setTipoPrincipio(String.valueOf(rule.getTipoPrincipio()));
        point.setTipoRegla(String.valueOf(rule.getTipoRegla()));
        point.setNombreFisicoObjeto(rule.getFichero());
        point.setNombreFisicoCampo(rule.getCampo());
        point.setPorcentajeCumplimiento((numerador/denominador)*100);
        point.setNumeradorCumplimiento(numerador);
        point.setDenominadorCumplimiento(denominador);
        point.setTipoFrecuenciaEjecucion("Monthly");
        point.setPorcentajeUmbralMinimo(rule.getUmbralMinimo());
        point.setPorcentajeUmbralObjetivo(rule.getUmbralObjetivo());
        point.setNombreCampoImporte(nombreCampo);
        point.setPorcentajeCumplimientoSaldo(porcentajeSaldo);
        point.setImporteNumerador(importeNumerador);
        point.setImporteDenominador(importeDenominador);
        point.setPeriodo(periodo);
        pointRulesDQRepository.save(point);
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Data Quality");
        insert.setFecha(today);
        insert.setInput("Reglas DQ");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<Object[]> getFolders()
    {
        Query folders = entityManager.createNativeQuery("select distinct fichero from nexco_reglasdq");
        return folders.getResultList();
    }

    public List<RulesDQ> getAllFolder(String folder,String periodo)
    {
        if(folder.equals("ALL"))
        {
            pointRulesDQRepository.deleteByPeriodo(periodo);
            Query folders = entityManager.createNativeQuery("select * from nexco_reglasdq", RulesDQ.class);
            return folders.getResultList();
        }
        else
        {
            pointRulesDQRepository.deleteByPeriodoAndNombreFisicoObjeto(periodo,folder);
            Query folders = entityManager.createNativeQuery("select * from nexco_reglasdq where fichero = ? ", RulesDQ.class);
            folders.setParameter(1, folder);
            return folders.getResultList();
        }
    }
}
