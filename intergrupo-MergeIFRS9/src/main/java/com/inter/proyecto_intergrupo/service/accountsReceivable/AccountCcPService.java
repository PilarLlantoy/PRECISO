package com.inter.proyecto_intergrupo.service.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.repository.accountsReceivable.AccountCcRepository;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SignatureRepository;
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
import java.text.ParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class AccountCcPService {

    @Autowired
    private AccountCcRepository accountCcRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public AccountCcPService(AccountCcRepository accountCcRepository) {
        this.accountCcRepository = accountCcRepository;
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

    public AccountCc findByIdCuentas(Long id){
        return accountCcRepository.findByIdCuentas(id);
    }

    public List<AccountCc> findAll()
    {
        return accountCcRepository.findAll();
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
        ArrayList<AccountCc> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();

                    String cellConcepto = formatter.formatCellValue(row.getCell(0));
                    String cellCuenta = formatter.formatCellValue(row.getCell(1));
                    String cellCentro = formatter.formatCellValue(row.getCell(2));
                    String cellNaturaleza = formatter.formatCellValue(row.getCell(3));
                    String cellImpuesto = formatter.formatCellValue(row.getCell(4));
                    String cellEvento = formatter.formatCellValue(row.getCell(5));

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
                    if (cellCentro.trim().length() != 4) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Centro debe tener 4 números.";
                        lista.add(log);
                    }
                    if (!cellNaturaleza.trim().equals("H") && !cellNaturaleza.trim().equals("D")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Naturaleza debe ser 'H' o 'D'.";
                        lista.add(log);
                    }
                    if (!cellImpuesto.trim().toUpperCase().equals("IVA") && !cellImpuesto.trim().toUpperCase().equals("SIN IMPUESTO") && !cellImpuesto.trim().toUpperCase().equals("RETEFUENTE") && !cellImpuesto.trim().toUpperCase().equals("CUENTA POR COBRAR") && !cellImpuesto.trim().toUpperCase().equals("PAGO")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Naturaleza debe ser IVA, SIN IMPUESTO, RETEFUENTE, CUENTA POR COBRAR o PAGO.";
                        lista.add(log);
                    }
                    if (!cellEvento.trim().toUpperCase().equals("CAUSACIÓN") && !cellEvento.trim().toUpperCase().equals("PAGO")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Evento debe ser CAUSACIÓN o PAGO.";
                        lista.add(log);
                    }

                    AccountCc accountCc = new AccountCc();
                    accountCc.setConcepto(cellConcepto);
                    accountCc.setCuenta(cellCuenta);
                    accountCc.setCentro(cellCentro);
                    accountCc.setNaturaleza(cellNaturaleza);
                    accountCc.setImpuesto(cellImpuesto.trim().toUpperCase());
                    accountCc.setEvento(cellEvento.trim().toUpperCase());
                    toInsert.add(accountCc);
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
            accountCcRepository.deleteAll();
            accountCcRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public AccountCc modifyAccount(AccountCc toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro Cuentas");
        return accountCcRepository.save(toModify);
    }

    public AccountCc saveAccount(AccountCc toSave, User user){
        loadAudit(user,"Adición Exitosa registro Cuentas");
        return accountCcRepository.save(toSave);
    }

    public void removeAccount(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro Cuentas");
        accountCcRepository.deleteByIdCuentas(id);
    }

    public void clearAccount(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Cuentas");
        accountCcRepository.deleteAll();
    }

    public Page<AccountCc> getAll(Pageable pageable){
        return accountCcRepository.findAll(pageable);
    }

    public List<AccountCc> findByFilter(String value, String filter) {
        List<AccountCc> list=new ArrayList<AccountCc>();
        switch (filter)
        {
            case "Concepto":
                list=accountCcRepository.findByConceptoContainingIgnoreCase(value);
                break;
            case "Cuenta":
                list=accountCcRepository.findByCuentaContainingIgnoreCase(value);
                break;
            case "Centro":
                list=accountCcRepository.findByCentroContainingIgnoreCase(value);
                break;
            case "Naturaleza":
                list=accountCcRepository.findByNaturalezaContainingIgnoreCase(value);
                break;
            case "Impuesto":
                list=accountCcRepository.findByImpuestoContainingIgnoreCase(value);
                break;
            case "Evento":
                list=accountCcRepository.findByEventoContainingIgnoreCase(value);
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
