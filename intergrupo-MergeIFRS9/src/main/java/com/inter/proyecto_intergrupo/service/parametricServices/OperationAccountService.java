package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;
import com.inter.proyecto_intergrupo.repository.parametric.OperationAccountRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ResponsibleAccountRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class OperationAccountService {

    @Autowired
    private final OperationAccountRepository responsibleAccountRepository;

    @Autowired
    private final UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private AuditRepository auditRepository;

    public OperationAccountService(OperationAccountRepository responsibleAccountRepository, UserRepository userRepository) {
        this.responsibleAccountRepository = responsibleAccountRepository;
        this.userRepository = userRepository;
    }

    public void modifyOperationAccount(OperationAccount toModify, String id, OperationAccount operation, User user) {
        OperationAccount toInsert = operation;
        toInsert.setOperacion(toModify.getOperacion());
        toInsert.setRiesgo(toModify.getRiesgo());
        Query query = entityManager.createNativeQuery("UPDATE nexco_operacion_riesgo SET tipo_operacion = ? , tipo_riesgo = ? " +
                "WHERE cuenta_local = ?", OperationAccount.class);
        query.setParameter(1, toInsert.getOperacion());
        query.setParameter(2, toInsert.getRiesgo());
        query.setParameter(3, id);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificar registro tabla Operación y Riesgo de Cuentas");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Operación y Riesgo de Cuentas");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public List<OperationAccount> getExceptEmpty() {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_operacion_riesgo as em ", OperationAccount.class);

        return query.getResultList();
    }

    public List<OperationAccount> findByFilter(String value, String filter) {
        List<OperationAccount> list = new ArrayList<OperationAccount>();
        switch (filter) {
            case "Cuenta Local":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_operacion_riesgo as em " +
                        "WHERE em.cuenta_local LIKE ?", OperationAccount.class);
                query.setParameter(1, value);

                list = query.getResultList();

                break;
            case "Tipo Operación":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_operacion_riesgo as em " +
                        "WHERE em.tipo_operacion LIKE ?", OperationAccount.class);

                query7.setParameter(1, value);

                list = query7.getResultList();
                break;
            case "Tipo Riesgo":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_operacion_riesgo as em " +
                        "WHERE em.tipo_riesgo LIKE ?", OperationAccount.class);

                query8.setParameter(1, value);

                list = query8.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public ArrayList<String[]> saveFileBDOperation(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list = validarPlantillaOperation(rows);
            String[] temporal = list.get(0);
            if (temporal[2].equals("true")) {
                list = getRowsOperation(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Operación y Riesgo de Cuentas");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Operación y Riesgo de Cuentas");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            } else {
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Falla Inserción archivo Operación y Riesgo de Cuentas");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Operación y Riesgo de Cuentas");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaOperation(Iterator<Row> rows) {
        ArrayList lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow != 1) {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(0)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(1));
                String cellRiesgo = formatter.formatCellValue(row.getCell(2));
                log[0] = String.valueOf(row.getRowNum());
                if (cellCuentaLocal.isEmpty() || cellCuentaLocal.isBlank() || (cellCuentaLocal.length() < 4 && !cellCuentaLocal.equals("-"))) {
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellOperacion.isEmpty() || cellOperacion.isBlank() || cellOperacion.length() > 254) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellRiesgo.isEmpty() || cellRiesgo.isBlank() || cellRiesgo.length() > 254) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        log[1] = "1";
                        if (!cellCuentaLocal.equals("-")) {
                            Long.parseLong(cellCuentaLocal);
                        }
                        log[2] = "true";
                    } catch (Exception e) {
                        log[2] = "falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            } else {
                firstRow = 0;
            }
        }
        lista.add(log);
        return lista;
    }

    public ArrayList getRowsOperation(Iterator<Row> rows) {
        XSSFRow row;
        Date today = new Date();
        ArrayList lista = new ArrayList();
        int firstRow = 1;
        while (rows.hasNext()) {
            String[] log = new String[3];
            log[2] = "true";
            row = (XSSFRow) rows.next();

            if (firstRow != 1 && row.getCell(0) != null) {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(0)).trim();
                String cellOperacion = formatter.formatCellValue(row.getCell(1));
                String cellRiesgo = formatter.formatCellValue(row.getCell(2));
                if (responsibleAccountRepository.findByCuentaLocal(cellCuentaLocal) != null) {

                    OperationAccount responsibleAccount = responsibleAccountRepository.findByCuentaLocal(cellCuentaLocal);
                    responsibleAccount.setOperacion(cellOperacion);
                    responsibleAccount.setRiesgo(cellRiesgo);
                    responsibleAccountRepository.save(responsibleAccount);
                    log[0] = String.valueOf(responsibleAccount.getCuentaLocal());
                    log[1] = "Registro actualizado exitosamente.";
                    lista.add(log);
                } else {
                    OperationAccount responsibleAccount = new OperationAccount();
                    responsibleAccount.setCuentaLocal(cellCuentaLocal);
                    responsibleAccount.setOperacion(cellOperacion);
                    responsibleAccount.setRiesgo(cellRiesgo);
                    responsibleAccountRepository.save(responsibleAccount);
                    log[0] = cellCuentaLocal;
                    log[1] = "Registro creado exitosamente.";
                    lista.add(log);
                }
            } else {
                firstRow = 0;
            }
        }
        return lista;
    }

    public OperationAccount findResponsibleAccountById(String id) {
        return responsibleAccountRepository.findByCuentaLocal(id);
    }

    public List<OperationAccount> findAll() {
        return responsibleAccountRepository.findAll();
    }

    public void removeOperation(String id, User user) {

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se elimina un registro de Operación y Riesgo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas");
        insert.setFecha(today);
        insert.setInput("Operación y Riesgo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        responsibleAccountRepository.deleteById(id);
    }

    public void clearOperation(User user){
        Query query = entityManager.createNativeQuery("DELETE from nexco_operacion_riesgo", OperationAccount.class);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Operación Riesgo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas");
        insert.setFecha(today);
        insert.setInput("Operación Riesgo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public OperationAccount saveOperation(OperationAccount operationAccount, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se agrega un registro a operación y riesgo");
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas");
        insert.setFecha(today);
        insert.setInput("Operación y Riesgo");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return responsibleAccountRepository.save(operationAccount);
    }

}
