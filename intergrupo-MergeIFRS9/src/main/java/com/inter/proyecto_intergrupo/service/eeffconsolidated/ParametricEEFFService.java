package com.inter.proyecto_intergrupo.service.eeffconsolidated;
import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricEEFF;
import com.inter.proyecto_intergrupo.repository.accountsReceivable.AccountCcRepository;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ParametricEEFFRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ParametricEEFFService {

    @Autowired
    private ParametricEEFFRepository parametricEEFFRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ParametricEEFFService(ParametricEEFFRepository parametricEEFFRepository) {
        this.parametricEEFFRepository = parametricEEFFRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Cuentas por Cobrar");
        insert.setFecha(today);
        insert.setInput("Cuentas");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParametricEEFF findByIdTipoParametro(Long id){
        return parametricEEFFRepository.findByIdTipoParametro(id);
    }

    public List<ParametricEEFF> findAll()
    {
        return parametricEEFFRepository.findAll();
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
                loadAudit(user,"Cargue exitoso plantilla Cuentas");
            else
                loadAudit(user,"Cargue Fallido plantilla Cuentas");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ParametricEEFF> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();

                    String cellParametro = formatter.formatCellValue(row.getCell(0));
                    String cellConcepto = formatter.formatCellValue(row.getCell(1));
                    String cellCuenta = formatter.formatCellValue(row.getCell(2));
                    String cellCuenta2 = formatter.formatCellValue(row.getCell(3));
                    String cellPorcentaje = formatter.formatCellValue(row.getCell(4));

                    XSSFCell cell1 = row.getCell(4);
                    cell1.setCellType(CellType.STRING);
                    cellPorcentaje = formatter.formatCellValue(cell1).replace(" ", "");

                    if (cellParametro.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Parametro no puede estar vacio.";
                    }

                    if (cellConcepto.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Concepto no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCuenta.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Cuenta no puede estar vacio.";
                        lista.add(log);
                    }
                    try {
                        if (cellPorcentaje.trim().length() == 0) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(2);
                            log[2] = "El campo Porcentaje no puede estar vacío.";
                            lista.add(log);
                        } else {
                            double porcentaje = Double.parseDouble(cellPorcentaje.trim());
                            if (porcentaje < 0 || porcentaje > 999) {
                                String[] log = new String[3];
                                log[0] = String.valueOf(row.getRowNum() + 1);
                                log[1] = CellReference.convertNumToColString(2);
                                log[2] = "El campo Porcentaje debe ser un número entre 0 y 999.";
                                lista.add(log);
                            }
                        }
                    } catch (NumberFormatException e) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Porcentaje no es un número válido.";
                        lista.add(log);
                    }

                    ParametricEEFF parametriceeff = new ParametricEEFF();
                    parametriceeff.setParametro(cellParametro);
                    parametriceeff.setConcepto(cellConcepto);
                    parametriceeff.setCuenta(cellCuenta);
                    parametriceeff.setCuenta2(cellCuenta2);
                    try {
                        parametriceeff.setPorcentaje(Double.parseDouble(cellPorcentaje));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    toInsert.add(parametriceeff);
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
            parametricEEFFRepository.deleteAll();
            parametricEEFFRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ParametricEEFF modifyAccount(ParametricEEFF toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro Cuentas");
        return parametricEEFFRepository.save(toModify);
    }

    public ParametricEEFF saveAccount(ParametricEEFF toSave, User user){
        loadAudit(user,"Adición Exitosa registro Cuentas");
        return parametricEEFFRepository.save(toSave);
    }

    public void removeAccount(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro Cuentas");
        parametricEEFFRepository.deleteByIdTipoParametro(id);
    }

    public void clearAccount(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Cuentas");
        parametricEEFFRepository.deleteAll();
    }

    public Page<ParametricEEFF> getAll(Pageable pageable){
        return parametricEEFFRepository.findAll(pageable);
    }



    public List<ParametricEEFF> findByFilter(String value, String filter) {
        List<ParametricEEFF> list=new ArrayList<ParametricEEFF>();
        switch (filter)
        {
            case "Parametro":
                list=parametricEEFFRepository.findByParametroContainingIgnoreCase(value);
                break;
            case "Concepto":
                list=parametricEEFFRepository.findByConceptoContainingIgnoreCase(value);
                break;
            case "Cuenta":
                list=parametricEEFFRepository.findByCuentaContainingIgnoreCase(value);
                break;
            case "Cuenta Aux":
                list=parametricEEFFRepository.findByCuenta2ContainingIgnoreCase(value);
                break;
            case "Porcentaje":
                Double doubleValue = Double.parseDouble(value);
                list = parametricEEFFRepository.findByPorcentajeGreaterThanEqual(doubleValue);
                break;
            default:
                break;
        }
        return list;
    }

    public boolean validatorPatter(String dato,String tipo){
        String patron ="";
        if(tipo.equals("Mail"))
        {
            patron="^[A-Za-z0-9+_.-]+@\\w+\\.\\w+$";
        }
        else if(tipo.equals("Phone")){
            patron="^(\\+\\d{2})?\\d{10}$";
        }
        Pattern pattern= Pattern.compile(patron);
        Matcher matcher = pattern.matcher(dato);
        return matcher.matches();
    }
}