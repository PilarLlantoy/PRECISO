package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInforme;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamInformeNotas;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamInformeNotasRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamInformeRepository;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class ParamInformeNotasService {

    @Autowired
    private ParamInformeNotasRepository paramInformeRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ParamInformeNotasService(ParamInformeNotasRepository paramInformeRepository) {
        this.paramInformeRepository = paramInformeRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("NIC34");
        insert.setFecha(today);
        insert.setInput("Parametrica Informe Notas");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParamInformeNotas findByIdNic34(Long id){
        return paramInformeRepository.findByIdNic34(id);
    }

    public List<ParamInformeNotas> findAll()
    {
        return paramInformeRepository.findAll();
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
                loadAudit(user,"Cargue Exitoso parametrica Informe Notas");
            else
                loadAudit(user,"Cargue Fallido parametrica Informe Notas");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ParamInformeNotas> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int counVal = 0;
                    DataFormatter formatter = new DataFormatter();
                    String cellAgrupa1 = formatter.formatCellValue(row.getCell(counVal++)).trim().toUpperCase();
                    String cellAplicaQuery = formatter.formatCellValue(row.getCell(counVal++)).trim().toUpperCase();
                    String cellAgrupa2 = formatter.formatCellValue(row.getCell(counVal++)).trim().toUpperCase();
                    String cellIdG = formatter.formatCellValue(row.getCell(counVal++)).trim();
                    String cellMoneda = formatter.formatCellValue(row.getCell(counVal++)).trim().toUpperCase();
                    String cellSigno = formatter.formatCellValue(row.getCell(counVal++)).trim();
                    String cellConcepto = formatter.formatCellValue(row.getCell(counVal++)).trim();
                    String cellCondicion = formatter.formatCellValue(row.getCell(counVal++)).trim().toUpperCase();
                    String cellNotas = formatter.formatCellValue(row.getCell(counVal++)).trim();
                    String cellAplica = formatter.formatCellValue(row.getCell(counVal++)).trim().toUpperCase();

                    if (!cellAgrupa1.equals("ID_GRUPO") && !cellAgrupa1.equals("ID_CAMPO") && !cellAgrupa1.equals("ID_NOTA") && !cellAgrupa1.equals("ID_SUBNOTA")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Agrupa 1 debe ser ID_GRUPO, ID_CAMPO, ID_NOTA o ID_SUBNOTA.";
                        lista.add(log);
                    }
                    if (!cellAplicaQuery.equals("BALANCE") && !cellAplicaQuery.equals("PYG")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Aplica Query debe ser BALANCE o PYG.";
                        lista.add(log);
                    }
                    if (cellAgrupa2.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Agrupa 2 no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellMoneda.equals("ML") && !cellMoneda.equals("ME") && !cellMoneda.equals("") && !cellMoneda.equals("MT")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Moneda debe ser MT, ML, ME o vacio.";
                        lista.add(log);
                    }
                    if (cellSigno.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Signo no puede estar vacio.";
                        lista.add(log);
                    }
                    else
                    {
                        try {
                            Double.parseDouble(cellSigno);
                        }
                        catch (Exception e){
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(5);
                            log[2] = "El campo Signo debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellConcepto.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Concepto no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellCondicion.equals("SALDO") && !cellCondicion.equals("SUMA")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Condición ser SALDO o SUMA.";
                        lista.add(log);
                    }
                    if (!cellAplica.equals("T") && !cellAplica.equals("A")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
                        log[2] = "El campo Aplica debe ser T o A.";
                        lista.add(log);
                    }

                    ParamInformeNotas paramInforme = new ParamInformeNotas();
                    paramInforme.setAgrupa1(cellAgrupa1);
                    paramInforme.setAplicaQuery(cellAplicaQuery);
                    paramInforme.setAgrupa2(cellAgrupa2);
                    paramInforme.setIdG(cellIdG);
                    paramInforme.setMoneda(cellMoneda);
                    try {
                        paramInforme.setSigno(Double.parseDouble(cellSigno));
                    }
                    catch (Exception e){

                    }
                    paramInforme.setConcepto(cellConcepto);
                    paramInforme.setCondicion(cellCondicion);
                    paramInforme.setNotas(cellNotas);
                    paramInforme.setAplica(cellAplica);
                    toInsert.add(paramInforme);
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 8) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")){
            paramInformeRepository.deleteAll();
            paramInformeRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ParamInformeNotas modifyInforme(ParamInformeNotas toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro Informe Notas");
        return paramInformeRepository.save(toModify);
    }

    public ParamInformeNotas saveInforme(ParamInformeNotas toSave, User user){
        loadAudit(user,"Adición Exitosa registro Informe Notas");
        return paramInformeRepository.save(toSave);
    }

    public void removeInforme(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro Informe Notas");
        paramInformeRepository.deleteByIdNic34(id);
    }

    public void clearInforme(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Informe Notas");
        paramInformeRepository.deleteAll();
    }

    public Page<ParamInformeNotas> getAll(Pageable pageable){
        return paramInformeRepository.findAll(pageable);
    }

    public List<ParamInformeNotas> findByFilter(String value, String filter) {
        List<ParamInformeNotas> list=new ArrayList<>();
        switch (filter)
        {
            case "Agupar 1":
                list=paramInformeRepository.findByAgrupa1ContainingIgnoreCase(value);
                break;
            case "Aplica Query":
                list=paramInformeRepository.findByAplicaQueryContainingIgnoreCase(value);
                break;
            case "Agupar 2":
                list=paramInformeRepository.findByAgrupa2ContainingIgnoreCase(value);
                break;
            case "Id":
                list=paramInformeRepository.findByIdGContainingIgnoreCase(value);
                break;
            case "Moneda":
                list=paramInformeRepository.findByMonedaContainingIgnoreCase(value);
                break;
            case "Concepto":
                list=paramInformeRepository.findByConceptoContainingIgnoreCase(value);
                break;
            case "Condición":
                list=paramInformeRepository.findByCondicionContainingIgnoreCase(value);
                break;
            case "Nota":
                list=paramInformeRepository.findByNotasContainingIgnoreCase(value);
                break;
            case "Aplica":
                list=paramInformeRepository.findByAplicaContainingIgnoreCase(value);
                break;
            default:
                break;
        }
        return list;
    }
}
