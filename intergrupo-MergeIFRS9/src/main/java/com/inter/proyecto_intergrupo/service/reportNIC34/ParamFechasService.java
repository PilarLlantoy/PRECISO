package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.BalanceNIC34;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamFechas;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamFechasRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamMDARepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ParamFechasService {

    @Autowired
    private ParamFechasRepository paramFechasRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ParamFechasService(ParamFechasRepository paramFechasRepository) {
        this.paramFechasRepository = paramFechasRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("NIC34");
        insert.setFecha(today);
        insert.setInput("Parametrica Fechas");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParamFechas findByIdFecha(Long id){
        return paramFechasRepository.findByIdFecha(id);
    }

    public List<ParamFechas> findAll()
    {
        return paramFechasRepository.findAll();
    }

    public List<ParamFechas> findAllOrder()
    {
        Query validateFecont = entityManager.createNativeQuery("select * from nexco_nic_fechas order by q_aplica desc, ano desc, mes", ParamFechas.class);
        return validateFecont.getResultList();
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso parametrica Fechas");
            else
                loadAudit(user,"Cargue Fallido parametrica Fechas");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ParamFechas> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int columnCount = 0;
                    DataFormatter formatter = new DataFormatter();
                    String cellAno= formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellMes = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellFecont = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellBalance = formatter.formatCellValue(row.getCell(columnCount++)).trim().toUpperCase();
                    String cellPyg = formatter.formatCellValue(row.getCell(columnCount++)).trim().toUpperCase();
                    String cellQAplica = formatter.formatCellValue(row.getCell(columnCount++)).trim();

                    if (cellAno.length() != 4) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Año debe tener 4 números.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Integer.parseInt(cellAno);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(0);
                            log[2] = "El campo Año debe ser númerico formato YYYY";
                            lista.add(log);
                        }
                    }
                    if (cellMes.length() > 2 || cellMes.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Mes debe tener 2 números.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Integer.parseInt(cellMes);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(1);
                            log[2] = "El campo Mes debe ser númerico formato MM";
                            lista.add(log);
                        }
                    }
                    if (cellFecont.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Fecont no puede estar vacío.";
                        lista.add(log);
                    }
                    else {
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
                            format.parse(cellFecont);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(2);
                            log[2] = "La Fecha ingresada no es valida debe ser formato(yyyy-mm-dd).";
                            lista.add(log);
                        }
                    }
                    if (!cellBalance.equals("X") && !cellBalance.equals("")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Balance debe ser X o vacío.";
                        lista.add(log);
                    }
                    if (!cellPyg.equals("X") && !cellPyg.equals("")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo PYG debe ser X o vacío.";
                        lista.add(log);
                    }
                    if (cellQAplica.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Q Aplica no puede estar vacío.";
                        lista.add(log);
                    }
                    else if (!validatorPatter(cellQAplica, "Quartil")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Q Aplica ingresado no es valido, debe ser formato(#Q-YYYY).";
                        lista.add(log);
                    }

                    ParamFechas paramFechas = new ParamFechas();
                    paramFechas.setAno(cellAno);
                    paramFechas.setMes(cellMes);
                    paramFechas.setFecont(cellFecont);
                    paramFechas.setBalance(cellBalance);
                    paramFechas.setPyg(cellPyg);
                    paramFechas.setQaplica(cellQAplica);
                    toInsert.add(paramFechas);

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
        if (temp[2].equals("SUCCESS")){
            paramFechasRepository.deleteAll();
            paramFechasRepository.saveAll(toInsert);
            validateStatus("CARGADO");
            validateStatus("PENDIENTE");
        }
        toInsert.clear();
        return lista;
    }

    public ParamFechas modifyFecha(ParamFechas toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro fechas");
        return paramFechasRepository.save(toModify);
    }

    public ParamFechas saveFecha(ParamFechas toSave, User user){
        loadAudit(user,"Adición Exitosa registro fechas");
        return paramFechasRepository.save(toSave);
    }

    public void removeFecha(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro fechas");
        paramFechasRepository.deleteByIdFecha(id);
    }

    public void clearFecha(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Fechas");
        paramFechasRepository.deleteAll();
    }

    public Page<ParamFechas> getAll(Pageable pageable){
        return paramFechasRepository.findAll(pageable);
    }

    public List<ParamFechas> findByFilter(String value, String filter) {
        List<ParamFechas> list=new ArrayList<ParamFechas>();
        switch (filter)
        {
            case "Año":
                list=paramFechasRepository.findByAnoContainingIgnoreCase(value);
                break;
            case "Mes":
                list=paramFechasRepository.findByMesContainingIgnoreCase(value);
                break;
            case "Balance":
                list=paramFechasRepository.findByBalanceContainingIgnoreCase(value);
                break;
            case "PYG":
                list=paramFechasRepository.findByPygContainingIgnoreCase(value);
                break;
            case "Q Aplica":
                list=paramFechasRepository.findByQaplicaContainingIgnoreCase(value);
                break;
            default:
                break;
        }
        return list;
    }

    public void validateStatus(String estado)
    {
        String no = "";
        if(estado.equals("PENDIENTE")) {
            no="NOT";
        }
        Query actualizar = entityManager.createNativeQuery("update nexco_nic_fechas set estado = ? \n" +
                "where fecont "+no+" IN (select fecont from nexco_query_nic34 group by fecont)");
        actualizar.setParameter(1, estado);
        actualizar.executeUpdate();

        Query actualizar3 = entityManager.createNativeQuery("update nexco_nic_fechas set fechproce=null");
        actualizar3.executeUpdate();

        Query actualizar2 = entityManager.createNativeQuery("update a set a.fechproce = b.fechproce \n" +
                "from nexco_nic_fechas a, (select fecont,fechproce from nexco_query_nic34 group by fecont,fechproce) b\n" +
                "where a.fecont = b.fecont");
        actualizar2.executeUpdate();
    }

    public void validateStatusConsol(String estado)
    {
        String no = "";
        if(estado.equals("PENDIENTE")) {
            no="NOT";
        }
        Query actualizar = entityManager.createNativeQuery("update nexco_nic_fechas set estado_consol = ? \n" +
                "where fecont "+no+" IN (select fecont from nexco_query_nic34_consol group by fecont)");
        actualizar.setParameter(1, estado);
        actualizar.executeUpdate();
    }

    public boolean validatorPatter(String dato,String tipo){
        String patron ="";
        if(tipo.equals("Fecha"))
        {
            patron="^(0[1-9]|[12][0-9]|3[01])/(0[1-9]|1[0-2])/(\\d{4})$";
        }
        else if(tipo.equals("Quartil")){
            patron="^[1-4]Q-\\d{4}$";
        }
        Pattern pattern= Pattern.compile(patron);
        Matcher matcher = pattern.matcher(dato);
        return matcher.matches();
    }
}
