package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SignatureRepository;
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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ParamMDAService {

    @Autowired
    private ParamMDARepository paramMDARepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ParamMDAService(ParamMDARepository paramMDARepository) {
        this.paramMDARepository = paramMDARepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("NIC34");
        insert.setFecha(today);
        insert.setInput("Parametrica Moneda");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParamMDA findByIdMda(Long id){
        return paramMDARepository.findByIdMda(id);
    }

    public List<ParamMDA> findAllMda(String periodo,String divisa){
        Query consulta = entityManager.createNativeQuery("select * from nexco_mda where fecha = ? and divisa = ?",ParamMDA.class);
        consulta.setParameter(1,periodo);
        consulta.setParameter(2,divisa);
        return consulta.getResultList();
    }

    public List<ParamMDA> findAll()
    {
        return paramMDARepository.findAll();
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
                loadAudit(user,"Cargue Exitoso parametrica Moneda");
            else
                loadAudit(user,"Cargue Fallido parametrica Moneda");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ParamMDA> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellFecha = formatter.formatCellValue(row.getCell(0));
                    String cellDivisa = formatter.formatCellValue(row.getCell(1));
                    String cellMoneda = formatter.formatCellValue(row.getCell(2));

                    if (cellFecha.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Fecha no puede estar vacio.";
                        lista.add(log);
                    } else if (!validatorPatter(cellFecha.trim(), "Fecha")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "La Fecha ingresada no es valida, debe star en formato YYYY-MM-DD";
                        lista.add(log);
                    }
                    if (cellDivisa.trim().length() != 3) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Divisa debe tener 3 caracteres";
                        lista.add(log);
                    } else if (!validatorPatter(cellDivisa.trim(), "Divisa")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "La Divisa ingresada no es valida (ABC).";
                        lista.add(log);
                    }
                    if (!cellMoneda.trim().toUpperCase().equals("ML") && !cellMoneda.trim().toUpperCase().equals("ME")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Moneda debe ser ML o ME.";
                        lista.add(log);
                    }
                    List<ParamMDA> listaParam = findAllMda(cellFecha.trim(), cellDivisa.trim().toUpperCase());

                    if (listaParam.isEmpty()) {
                        ParamMDA paramMDA = new ParamMDA();
                        paramMDA.setFecha(cellFecha.trim());
                        paramMDA.setDivisa(cellDivisa.trim().toUpperCase());
                        paramMDA.setMoneda(cellMoneda.trim().toUpperCase());
                        toInsert.add(paramMDA);
                    }
                    else{
                        listaParam.get(0).setFecha(cellFecha.trim());
                        listaParam.get(0).setDivisa(cellDivisa.trim().toUpperCase());
                        listaParam.get(0).setMoneda(cellMoneda.trim().toUpperCase());
                        toInsert.add(listaParam.get(0));
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
        if (temp[2].equals("SUCCESS")){
            paramMDARepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ParamMDA modifyMda(ParamMDA toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro moneda");
        return paramMDARepository.save(toModify);
    }

    public ParamMDA saveMda(ParamMDA toSave, User user){
        loadAudit(user,"Adición Exitosa registro moneda");
        return paramMDARepository.save(toSave);
    }

    public void removeMda(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro moneda");
        paramMDARepository.deleteByIdMda(id);
    }

    public void clearMda(User user){
        loadAudit(user,"Limpieza de tabla Exitosa moneda");
        paramMDARepository.deleteAll();
    }

    public Page<ParamMDA> getAll(Pageable pageable){
        return paramMDARepository.findAll(pageable);
    }

    public List<ParamMDA> findByFilter(String value, String filter) {
        List<ParamMDA> list=new ArrayList<ParamMDA>();
        switch (filter)
        {
            case "Fecha":
                list=paramMDARepository.findByFechaContainingIgnoreCase(value);
                break;
            case "Divisa":
                list=paramMDARepository.findByDivisaContainingIgnoreCase(value);
                break;
            case "Moneda":
                list=paramMDARepository.findByMonedaContainingIgnoreCase(value);
                break;
            default:
                break;
        }
        return list;
    }

    public boolean validatorPatter(String dato,String tipo){
        String patron ="";
        if(tipo.equals("Fecha"))
        {
            patron="^(\\d{4})-(0[1-9]|1[0-2])-(0[1-9]|[12][0-9]|3[01])$";
        }
        else if(tipo.equals("Divisa")){
            patron="^[a-zA-Z]{3}$";
        }
        Pattern pattern= Pattern.compile(patron);
        Matcher matcher = pattern.matcher(dato);
        return matcher.matches();
    }

}
