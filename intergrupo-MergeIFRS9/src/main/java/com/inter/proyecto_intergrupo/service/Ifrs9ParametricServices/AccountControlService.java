package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.AccountControl;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.AccountControlRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.AccountControlRepository;
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

@Service
@Transactional
public class AccountControlService {

    @Autowired
    private AccountControlRepository accountControlRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public AccountControlService(AccountControlRepository accountControlRepository, AuditRepository auditRepository) {
        this.accountControlRepository = accountControlRepository;
        this.auditRepository = auditRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file,User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows,user);
            String[] temporal= list.get(0);
            if(temporal[2].equals("0"))
            {
                getRows(rows1,user);
                auditCode("Inserción archivo Control Contable",user);
            }else{
                auditCode("Falla inserción archivo Control Contable",user);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";

                DataFormatter formatter = new DataFormatter();
                String cellCuenta = formatter.formatCellValue(row.getCell(0)).trim();
                String cellDescripcionCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(2)).trim();
                String cellDiasPlazo = formatter.formatCellValue(row.getCell(3)).trim();
                String cellIndicadorCuenta = formatter.formatCellValue(row.getCell(4)).trim();
                String cellTipoApunte = formatter.formatCellValue(row.getCell(5)).trim();
                String cellInventariable = formatter.formatCellValue(row.getCell(6)).trim();

                log[0]=String.valueOf(row.getRowNum()+1);

                if(cellCuenta.length()!=4)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Cuenta debe tener 4 carcateres";
                    fail++;
                    lista.add(log);
                }
                else if(cellDescripcionCuenta.length()==0 || cellDescripcionCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Descripción Cuenta no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoControl.length()==0 || cellCodigoControl.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Código Control no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellDiasPlazo.length()==0 || cellDiasPlazo.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló Días de Plazo no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellIndicadorCuenta.length()==0 || cellIndicadorCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló Indicador Cuenta no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellTipoApunte.length()==0 || cellTipoApunte.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló Tipo de Apunte no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellInventariable.length()==0 || cellInventariable.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló Inventariable no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="CUENTA";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Control Contable",user);
        }
        return lista;
    }

    public void getRows(Iterator<Row> rows,User user) {
        XSSFRow row;
        int firstRow=1;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuenta = formatter.formatCellValue(row.getCell(0)).trim();
                String cellDescripcionCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(2)).trim();
                String cellDiasPlazo = formatter.formatCellValue(row.getCell(3)).trim();
                String cellIndicadorCuenta = formatter.formatCellValue(row.getCell(4)).trim();
                String cellTipoApunte = formatter.formatCellValue(row.getCell(5)).trim();
                String cellInventariable = formatter.formatCellValue(row.getCell(6)).trim();

                AccountControl accountControl = new AccountControl();
                accountControl.setCUENTA(cellCuenta);
                accountControl.setDESCRIPCIONCUENTA(cellDescripcionCuenta);
                accountControl.setCODIGODECONTROL(cellCodigoControl);
                accountControl.setDIASDEPLAZO(cellDiasPlazo);
                accountControl.setINDICADORDELACUENTA(cellIndicadorCuenta);
                accountControl.setTIPODEAPUNTE(cellTipoApunte);
                accountControl.setINVENTARIABLE(cellInventariable);
                accountControlRepository.save(accountControl);

                Query verify = entityManager.createNativeQuery("UPDATE nexco_control_contable_ifrs\n" +
                        "SET DESCRIPCION_CUENTA = t2.DERECTA\n" +
                        "FROM nexco_control_contable_ifrs t1, CUENTAS_PUC t2\n" +
                        "WHERE t1.cuenta = t2.NUCTA");
                verify.executeUpdate();
            }
        }
        auditCode("Carga masiva apartado Control Contable realizada exitosamente",user);
    }

    public List<AccountControl> findAll(){
        return accountControlRepository.findAll();
    }

    public boolean insertAccountControl(AccountControl toInsert){

        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_control_contable_ifrs WHERE CUENTA = ?");
        verify.setParameter(1,toInsert.getCUENTA());

        if(verify.getResultList().isEmpty()){
            try {
                accountControlRepository.save(toInsert);
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return state;
    }

    public void auditCode (String info,User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Control Contable");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public AccountControl findByCUENTA(String inicial){
        return accountControlRepository.findByCUENTA(inicial);
    }

    public AccountControl modifyAccountControl(AccountControl toModify,String id, User user){
        if(toModify.getCUENTA()!=id)
            accountControlRepository.deleteById(id);
        auditCode("Modificacion registro tabla Control Contable",user);
        return accountControlRepository.save(toModify);
    }

    public AccountControl saveAccountControl(AccountControl accountControl){
        return accountControlRepository.save(accountControl);
    }

    public void removeAccountControl(String id, User user){
        accountControlRepository.deleteById(id);
        auditCode("Eliminar registro tabla Control Contable",user);
    }

    public void clearAccountControl(User user){
        accountControlRepository.deleteAll();
        auditCode("Limpiar tabla Control Contable",user);
    }

    public Page<AccountControl> getAll(Pageable pageable){
        return accountControlRepository.findAll(pageable);
    }

    public List<AccountControl> findByFilter(String value, String filter) {
        List<AccountControl> list=new ArrayList<AccountControl>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.CUENTA LIKE ?", AccountControl.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Descripción Cuenta":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.DESCRIPCION_CUENTA LIKE ?", AccountControl.class);
                query5.setParameter(1, value );

                list= query5.getResultList();

                break;
            case "Código de Control":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.CODIGO_DE_CONTROL LIKE ?", AccountControl.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Días de Plazo":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.DIAS_DE_PLAZO LIKE ?", AccountControl.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Indicador de la Cuenta":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.INDICADOR_DE_LA_CUENTA LIKE ?", AccountControl.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Tipo de Apunte":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.TIPO_DE_APUNTE LIKE ?", AccountControl.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Inventariable":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_control_contable_ifrs as em " +
                        "WHERE em.INVENTARIABLE LIKE ?", AccountControl.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
