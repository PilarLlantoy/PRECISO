package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.AccountBanco;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.AccountBancoRepository;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class AccountBancoService {

    @Autowired
    private AccountBancoRepository accountBancoRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public AccountBancoService(AccountBancoRepository accountBancoRepository) {
        this.accountBancoRepository = accountBancoRepository;
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

    public AccountBanco findByCuenta(String cuenta){
        return accountBancoRepository.findByCuenta(cuenta);
    }

    public void changeNaturaleza(String cuenta)
    {
        Query insert2 = entityManager.createNativeQuery("update nexco_param_cuenta_banco set naturaleza = case when naturaleza = '+' then '-' else '+' end  where cuenta = :cuenta ");
        insert2.setParameter("cuenta", cuenta);
        insert2.executeUpdate();
    }

    public List<AccountBanco> findAll()
    {
        return accountBancoRepository.findAll();
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
                loadAudit(user,"Cargue Exitoso parametrica naturaleza plantilla banco");
            else
                loadAudit(user,"Cargue Fallido parametrica naturaleza plantilla banco");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<AccountBanco> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellCuenta = formatter.formatCellValue(row.getCell(0)).trim();
                    String cellNaturaleza = formatter.formatCellValue(row.getCell(1)).trim().toUpperCase();

                    if (cellCuenta.trim().length() != 5) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Cuenta debe tener 5 números.";
                        lista.add(log);
                    }
                    if (!cellNaturaleza.equals("+") && !cellNaturaleza.equals("-")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Naturaleza debe ser + o -.";
                        lista.add(log);
                    }

                    if (lista.isEmpty())
                    {
                        AccountBanco accountBanco = new AccountBanco();
                        accountBanco.setCuenta(cellCuenta);
                        accountBanco.setNaturaleza(cellNaturaleza);
                        toInsert.add(accountBanco);
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
            accountBancoRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public AccountBanco modifyAccount(AccountBanco toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro naturaleza");
        return accountBancoRepository.save(toModify);
    }

    public AccountBanco saveAccount(AccountBanco toSave, User user){
        loadAudit(user,"Adición Exitosa registro naturaleza");
        return accountBancoRepository.save(toSave);
    }

    public void removeAccount(String id, User user){
        loadAudit(user,"Eliminación Exitosa registro naturaleza");
        accountBancoRepository.deleteByCuenta(id);
    }

    public void clearAccount(User user){
        loadAudit(user,"Limpieza de tabla Exitosa natrualeza");
        accountBancoRepository.deleteAll();
    }

    public Page<AccountBanco> getAll(Pageable pageable){
        return accountBancoRepository.findAll(pageable);
    }

    public List<AccountBanco> findByFilter(String value, String filter) {
        List<AccountBanco> list=new ArrayList<AccountBanco>();
        switch (filter)
        {
            case "Cuenta":
                list=accountBancoRepository.findByCuentaContainingIgnoreCase(value);
                break;
            case "Naturaleza":
                list=accountBancoRepository.findByNaturalezaContainingIgnoreCase(value);
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
