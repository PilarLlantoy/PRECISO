package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ResponsibleAccountRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.codehaus.groovy.transform.SourceURIASTTransformation;
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
public class ResponsibleAccountService {

    @Autowired
    private final ResponsibleAccountRepository responsibleAccountRepository;

    @Autowired
    private final UserRepository userRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ResponsibleAccountService(ResponsibleAccountRepository responsibleAccountRepository,UserRepository userRepository) {
        this.responsibleAccountRepository = responsibleAccountRepository;
        this.userRepository=userRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream file,User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Cuenta Responsable");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Cuenta resposable");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Cuenta Responsable");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Cuenta resposable");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int firstRow=1;
        String[] log=new String[3];
        log[0]="0";
        log[1]="0";
        log[2]="false";
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(0));
                String cellCentro = formatter.formatCellValue(row.getCell(1));
                String cellInput = formatter.formatCellValue(row.getCell(2));
                String cellComponente = formatter.formatCellValue(row.getCell(3));
                String cellSicc = formatter.formatCellValue(row.getCell(4));
                String cellBaseFiscal = formatter.formatCellValue(row.getCell(5));
                String cellMetodologia = formatter.formatCellValue(row.getCell(6));
                String cellMis = formatter.formatCellValue(row.getCell(7));
                if(cellCuentaLocal.isEmpty() || cellCuentaLocal.isBlank() ||cellCuentaLocal.length()<4)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellCentro.isEmpty() || cellCentro.isBlank() ||cellCentro.length()!=4)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else if(cellInput.isEmpty() || cellInput.isBlank() ||cellInput.length()>254)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="3";
                    log[2]="false";
                    break;
                }
                else if(cellComponente.isEmpty() || cellComponente.isBlank() ||cellComponente.length()>254)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="4";
                    log[2]="false";
                    break;
                }
                else if(cellSicc.isEmpty() || cellSicc.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="5";
                    log[2]="false";
                    break;
                }
                else if(cellBaseFiscal.isEmpty() || cellBaseFiscal.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="6";
                    log[2]="false";
                    break;
                }
                else if(cellMetodologia.isEmpty() || cellMetodologia.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="7";
                    log[2]="false";
                    break;
                } else if (cellMis.isEmpty() || cellMis.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="8";
                    log[2]="false";
                    break;
                }
                else
                {
                    try
                    {
                        log[0]=String.valueOf(row.getRowNum());log[1]="1";
                        Long nit = Long.parseLong(cellCuentaLocal);log[1]="2";
                        Long centro = Long.parseLong(cellCentro);log[1]="5";
                        boolean sicc = Boolean.parseBoolean(cellSicc);log[1]="6";
                        boolean baseFiscal = Boolean.parseBoolean(cellBaseFiscal);log[1]="7";
                        boolean metodologia = Boolean.parseBoolean(cellMetodologia);log[1]="8";
                        boolean mis = Boolean.parseBoolean(cellMetodologia);log[2]="true";
                    }
                    catch(Exception e){
                        log[2]="falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            }
            else
            {
                firstRow=0;
            }
        }
        lista.add(log);
        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        ArrayList lista= new ArrayList();

        String todayString="";
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(today);
        if(calendar1.get(Calendar.MONTH)==0)
        {
            calendar1.add(Calendar.YEAR,-1);
            todayString = calendar1.get(Calendar.YEAR) + "-12";
        }
        else if (String.valueOf(calendar1.get(Calendar.MONTH)).length() == 2) {
            todayString = calendar1.get(Calendar.YEAR) + "-" + String.valueOf(calendar1.get(Calendar.MONTH));
        } else {
            todayString = calendar1.get(Calendar.YEAR) + "-0" + String.valueOf(calendar1.get(Calendar.MONTH));
        }
        String[] parte = todayString.split("-");

        Query queryCuadroUpdate = entityManager.createNativeQuery("drop table preciso_administracion_cuadro_mando_trunc; \n" +
                "select * into preciso_administracion_cuadro_mando_trunc from preciso_administracion_cuadro_mando where fecha_reporte = ? and semaforo_input='FULL'; ");
        queryCuadroUpdate.setParameter(1,todayString);
        queryCuadroUpdate.executeUpdate();

        Query queryCuadroDelete = entityManager.createNativeQuery("DELETE FROM preciso_administracion_cuadro_mando WHERE year(convert(DATE,fecha_reporte+'-01',23)) >= ? AND month(convert(DATE,fecha_reporte+'-01',23)) >= ?");
        queryCuadroDelete.setParameter(1,parte[0]);
        queryCuadroDelete.setParameter(2,parte[1]);
        queryCuadroDelete.executeUpdate();

        int firstRow=1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();

            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(0));
                String cellCentro = formatter.formatCellValue(row.getCell(1));
                String cellInput = formatter.formatCellValue(row.getCell(2));
                String cellComponente = formatter.formatCellValue(row.getCell(3));
                String cellSicc = formatter.formatCellValue(row.getCell(4));
                String cellBaseFiscal = formatter.formatCellValue(row.getCell(5));
                String cellMetodologia = formatter.formatCellValue(row.getCell(6));
                String cellMis = formatter.formatCellValue(row.getCell(7));

                List<User> listCenter = userRepository.findByCentro(cellCentro.trim());

                Query querySearch = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_cuadro_mando where input = ? AND componente = ? AND responsable = ? AND fecha_reporte = ? ", ControlPanel.class);
                querySearch.setParameter(1, cellInput);
                querySearch.setParameter(2, cellComponente);
                querySearch.setParameter(3, cellCentro);
                querySearch.setParameter(4, todayString);

                List<ControlPanel> result = querySearch.getResultList();
                if(result.size()==0) {

                    for (int i = Integer.parseInt(parte[1]); i <= 12; i++) {
                        Query queryCuadro = entityManager.createNativeQuery("INSERT INTO preciso_administracion_cuadro_mando (responsable, input, fecha_reporte, componente,empresa,estado,semaforo_componente,semaforo_input) " +
                                "VALUES (?,?,?,?,?,?,?,?)", ControlPanel.class);
                        queryCuadro.setParameter(1, cellCentro);
                        queryCuadro.setParameter(2, cellInput);
                        if (i < 10) {
                            queryCuadro.setParameter(3, calendar.get(Calendar.YEAR) + "-0" + i);
                        } else {
                            queryCuadro.setParameter(3, calendar.get(Calendar.YEAR) + "-" + i);
                        }
                        queryCuadro.setParameter(4, cellComponente);
                        if (listCenter.size() > 0 && listCenter.get(0).getEmpresa() != null) {
                            queryCuadro.setParameter(5, listCenter.get(0).getEmpresa());
                        } else {
                            queryCuadro.setParameter(5, "00548");
                        }
                        queryCuadro.setParameter(6, false);
                        queryCuadro.setParameter(7, "EMPTY");
                        queryCuadro.setParameter(8, "EMPTY");
                        queryCuadro.executeUpdate();
                    }
                }

                if(responsibleAccountRepository.findByCuentaLocal(Long.parseLong(cellCuentaLocal.trim()))==null)
                {
                        ResponsibleAccount responsibleAccount = new ResponsibleAccount();
                        responsibleAccount.setCuentaLocal(Long.parseLong(cellCuentaLocal));
                        responsibleAccount.setEntrada(cellInput.toUpperCase());
                        responsibleAccount.setComponente(cellComponente.toUpperCase());
                        responsibleAccount.setSicc(Boolean.parseBoolean(cellSicc));
                        responsibleAccount.setBaseFiscal(Boolean.parseBoolean(cellBaseFiscal));
                        responsibleAccount.setMetodologia(Boolean.parseBoolean(cellMetodologia));
                        responsibleAccount.setMis(Boolean.parseBoolean(cellMis));
                        responsibleAccount.setCentro(cellCentro);
                        responsibleAccountRepository.save(responsibleAccount);

                        for (int i=0;i<listCenter.size();i++)
                        {
                            Query query = entityManager.createNativeQuery("INSERT INTO preciso_user_account VALUES (?,?)", UserAccount.class);
                            query.setParameter(1, listCenter.get(i).getUsuario());
                            query.setParameter(2, Long.parseLong(cellCuentaLocal));
                            query.executeUpdate();
                        }

                        /*Query queryCuadroDelete = entityManager.createNativeQuery("DELETE FROM preciso_administracion_cuadro_mando WHERE componente = ? AND input = ? AND responsable = ? AND year(convert(DATE,fecha_reporte+'-01',23)) = ? AND month(convert(DATE,fecha_reporte+'-01',23)) >= ?");
                        queryCuadroDelete.setParameter(1,cellComponente);
                        queryCuadroDelete.setParameter(2,cellInput);
                        queryCuadroDelete.setParameter(3,cellCentro);
                        queryCuadroDelete.setParameter(4,parte[0]);
                        queryCuadroDelete.setParameter(5,parte[1]);
                        queryCuadroDelete.executeUpdate();

                        for (int i=Integer.parseInt(parte[1]);i<=12;i++) {
                            Query queryCuadro = entityManager.createNativeQuery("INSERT INTO preciso_administracion_cuadro_mando (responsable, input, fecha_reporte, componente,empresa,estado,semaforo_componente,semaforo_input) " +
                                    "VALUES (?,?,?,?,?,?,?,?)", ControlPanel.class);
                            queryCuadro.setParameter(1, cellCentro);
                            queryCuadro.setParameter(2, cellInput);
                            if(i<10){
                                queryCuadro.setParameter(3, calendar.get(Calendar.YEAR)+"-0"+i);
                            }
                            else{
                                queryCuadro.setParameter(3, calendar.get(Calendar.YEAR)+"-"+i);
                            }
                            queryCuadro.setParameter(4, cellComponente);
                            if(listCenter.size()>0 && listCenter.get(0).getEmpresa()!=null) {
                                queryCuadro.setParameter(5, listCenter.get(0).getEmpresa());
                            }
                            else{
                                queryCuadro.setParameter(5, "00548");
                            }
                            queryCuadro.setParameter(6, false);
                            queryCuadro.setParameter(7, "EMPTY");
                            queryCuadro.setParameter(8, "EMPTY");
                            queryCuadro.executeUpdate();
                        }*/

                        log[0] = String.valueOf(responsibleAccount.getCuentaLocal());
                        log[1] = "Registro insertado exitosamente.";
                    if(listCenter.size()==0)
                    {
                        log[0] = cellCuentaLocal;
                        log[1] = "Registro Insertado, Centro "+cellCentro+" sin usuario responsable.";
                    }
                }
                else {

                    ResponsibleAccount responsibleAccount = responsibleAccountRepository.findByCuentaLocal(Long.parseLong(cellCuentaLocal));
                    responsibleAccount.setEntrada(cellInput.toUpperCase());
                    responsibleAccount.setComponente(cellComponente.toUpperCase());
                    responsibleAccount.setSicc(Boolean.parseBoolean(cellSicc));
                    responsibleAccount.setBaseFiscal(Boolean.parseBoolean(cellBaseFiscal));
                    responsibleAccount.setMetodologia(Boolean.parseBoolean(cellMetodologia));
                    responsibleAccount.setMis(Boolean.parseBoolean(cellMis));
                    responsibleAccount.setCentro(cellCentro);
                    responsibleAccountRepository.save(responsibleAccount);

                    Query queryD = entityManager.createNativeQuery("DELETE FROM preciso_user_account WHERE cuenta_local= ?", UserAccount.class);
                    queryD.setParameter(1, Long.parseLong(cellCuentaLocal));
                    queryD.executeUpdate();

                    for (int i = 0; i < listCenter.size(); i++) {

                        Query query1 = entityManager.createNativeQuery("INSERT INTO preciso_user_account(id_usuario,cuenta_local) VALUES (?,?)", UserAccount.class);
                        query1.setParameter(1, listCenter.get(i).getUsuario());
                        query1.setParameter(2, Long.parseLong(cellCuentaLocal));
                        query1.executeUpdate();
                    }

                    /*Query querySearch = entityManager.createNativeQuery("SELECT * FROM preciso_cuentas_responsables where input = ? AND componente = ? AND cuenta_local = ?", ResponsibleAccount.class);
                    querySearch.setParameter(1, cellInput);
                    querySearch.setParameter(2, cellComponente);
                    querySearch.setParameter(3, cellCuentaLocal);

                    List<ResponsibleAccount> result = querySearch.getResultList();

                    if(result.size()==0)
                    {
                        Query queryCuadroDelete = entityManager.createNativeQuery("DELETE FROM preciso_administracion_cuadro_mando WHERE componente = ? AND input = ? AND year(convert(DATE,fecha_reporte+'-01',23)) = ? AND month(convert(DATE,fecha_reporte+'-01',23)) >= ? ");
                        queryCuadroDelete.setParameter(1, cellComponente);
                        queryCuadroDelete.setParameter(2, cellInput);
                        queryCuadroDelete.setParameter(3, parte[0]);
                        queryCuadroDelete.setParameter(4, parte[1]);
                        queryCuadroDelete.executeUpdate();

                        for (int i = Integer.parseInt(parte[1]); i <= 12; i++) {
                            Query queryCuadro = entityManager.createNativeQuery("INSERT INTO preciso_administracion_cuadro_mando (responsable, input, fecha_reporte, componente,empresa,estado,semaforo_componente,semaforo_input) " +
                                    "VALUES (?,?,?,?,?,?,?,?)", ControlPanel.class);
                            queryCuadro.setParameter(1, cellCentro);
                            queryCuadro.setParameter(2, cellInput);
                            if (i < 10) {
                                queryCuadro.setParameter(3, calendar.get(Calendar.YEAR) + "-0" + i);
                            } else {
                                queryCuadro.setParameter(3, calendar.get(Calendar.YEAR) + "-" + i);
                            }
                            queryCuadro.setParameter(4, cellComponente);
                            if (listCenter.size() > 0 && listCenter.get(0).getEmpresa() != null) {
                                queryCuadro.setParameter(5, listCenter.get(0).getEmpresa());
                            } else {
                                queryCuadro.setParameter(5, "00548");
                            }
                            queryCuadro.setParameter(6, false);
                            queryCuadro.setParameter(7, "EMPTY");
                            queryCuadro.setParameter(8, "EMPTY");
                            queryCuadro.executeUpdate();
                        }
                    }
                    else
                    {
                        Query validate = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_cuadro_mando WHERE responsable = ? AND input = ? AND componente = ?");
                        validate.setParameter(1, cellCentro);
                        validate.setParameter(2, cellInput);
                        validate.setParameter(3, cellComponente);

                        if(!validate.getResultList().isEmpty())
                        {
                            Query validate2 = entityManager.createNativeQuery("SELECT * FROM preciso_cuentas_responsables WHERE centro = ? AND input = ? AND componente = ?");
                            validate2.setParameter(1, result.get(0).getCentro());
                            validate2.setParameter(2, cellInput);
                            validate2.setParameter(3, cellComponente);

                            if(validate2.getResultList().isEmpty())
                            {
                                Query validate3 = entityManager.createNativeQuery("DELETE FROM preciso_administracion_cuadro_mando WHERE responsable = ? AND input = ? AND componente = ? AND year(convert(DATE,fecha_reporte+'-01',23)) = ? AND month(convert(DATE,fecha_reporte+'-01',23)) >= ? ");
                                validate3.setParameter(1, result.get(0).getCentro());
                                validate3.setParameter(2, cellInput);
                                validate3.setParameter(3, cellComponente);
                                validate3.setParameter(4, parte[0]);
                                validate3.setParameter(5, parte[1]);
                                validate3.executeUpdate();
                            }
                        }
                        else
                        {
                            Query validate2 = entityManager.createNativeQuery("SELECT * FROM preciso_cuentas_responsables WHERE centro = ? AND input = ? AND componente = ?");
                            validate2.setParameter(1, result.get(0).getCentro());
                            validate2.setParameter(2, cellInput);
                            validate2.setParameter(3, cellComponente);

                            if(validate2.getResultList().isEmpty())
                            {
                                Query queryCuadro2 = entityManager.createNativeQuery("UPDATE preciso_administracion_cuadro_mando SET responsable = ? WHERE responsable = ? AND input = ? AND componente = ? AND year(convert(DATE,fecha_reporte+'-01',23)) = ? AND month(convert(DATE,fecha_reporte+'-01',23)) >= ? ");
                                queryCuadro2.setParameter(1, cellCentro);
                                queryCuadro2.setParameter(2, result.get(0).getCentro());
                                queryCuadro2.setParameter(3, cellInput);
                                queryCuadro2.setParameter(4, cellComponente);
                                queryCuadro2.setParameter(5, parte[0]);
                                queryCuadro2.setParameter(6, parte[1]);
                                queryCuadro2.executeUpdate();
                            }
                            else
                            {
                                for (int i = Integer.parseInt(parte[1]); i <= 12; i++) {
                                    Query queryCuadro = entityManager.createNativeQuery("INSERT INTO preciso_administracion_cuadro_mando (responsable, input, fecha_reporte, componente,empresa,estado,semaforo_componente,semaforo_input) " +
                                            "VALUES (?,?,?,?,?,?,?,?)", ControlPanel.class);
                                    queryCuadro.setParameter(1, cellCentro);
                                    queryCuadro.setParameter(2, cellInput);
                                    if (i < 10) {
                                        queryCuadro.setParameter(3, calendar.get(Calendar.YEAR) + "-0" + i);
                                    } else {
                                        queryCuadro.setParameter(3, calendar.get(Calendar.YEAR) + "-" + i);
                                    }
                                    queryCuadro.setParameter(4, cellComponente);
                                    if (listCenter.size() > 0 && listCenter.get(0).getEmpresa() != null) {
                                        queryCuadro.setParameter(5, listCenter.get(0).getEmpresa());
                                    } else {
                                        queryCuadro.setParameter(5, "00548");
                                    }
                                    queryCuadro.setParameter(6, false);
                                    queryCuadro.setParameter(7, "EMPTY");
                                    queryCuadro.setParameter(8, "EMPTY");
                                    queryCuadro.executeUpdate();
                                }
                            }

                        }

                    }*/

                    log[0] = String.valueOf(responsibleAccount.getCuentaLocal());
                    log[1] = "Cuenta Actualizada exitosamente.";

                    if(listCenter.size()==0)
                    {
                        log[0] = cellCuentaLocal;
                        log[1] = "Registro Actualizado, Centro "+cellCentro+" sin usuario responsable.";
                    }
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }

        Query queryCuadroUpdate1 = entityManager.createNativeQuery("update a set a.usuario_carga=b.usuario_carga, a.semaforo_componente = b.semaforo_componente, a.semaforo_input=b.semaforo_input, a.fecha_carga=b.fecha_carga\n" +
                "from (select * from preciso_administracion_cuadro_mando where fecha_reporte = ? ) a, preciso_administracion_cuadro_mando_trunc b \n" +
                "where a.fecha_reporte=b.fecha_reporte and a.input=b.input and a.responsable = b.responsable and a.componente = b.componente and a.empresa=b.empresa;");
        queryCuadroUpdate1.setParameter(1,todayString);
        queryCuadroUpdate1.executeUpdate();

        return lista;
    }

    public List <ResponsibleAccount> findAll(){return responsibleAccountRepository.findAll();}

    public ResponsibleAccount findResponsibleAccountById(Long id){
        return responsibleAccountRepository.findByCuentaLocal(id);
    }

    public String modifyResponsibleAccount(ResponsibleAccount toModify,Long id, List<User> listUser,String centro,String input, String componente,User userid){

        String resp="Modify1";
        Query queryC = entityManager.createNativeQuery("DELETE FROM preciso_administracion_cuadro_mando WHERE responsable = ? AND input = ? AND componente = ? ");
        queryC.setParameter(1, centro);
        queryC.setParameter(2, input);
        queryC.setParameter(3, componente);
        queryC.executeUpdate();

        queryC.setParameter(1, toModify.getCentro());
        queryC.setParameter(2, toModify.getEntrada());
        queryC.setParameter(3, toModify.getComponente());
        queryC.executeUpdate();

        entityManager.clear();

        Query queryD = entityManager.createNativeQuery("DELETE FROM preciso_user_account WHERE cuenta_local = ? OR cuenta_local = ?");
        queryD.setParameter(1, id);
        queryD.setParameter(2, toModify.getCuentaLocal());
        queryD.executeUpdate();

        entityManager.clear();

        Query queryDR = entityManager.createNativeQuery("DELETE FROM preciso_cuentas_responsables WHERE (cuenta_local = ? OR cuenta_local = ?) AND input = ? AND componente = ? AND centro = ?");
        queryDR.setParameter(1, id);
        queryDR.setParameter(2, toModify.getCuentaLocal());
        queryDR.setParameter(3, toModify.getEntrada());
        queryDR.setParameter(4, toModify.getComponente());
        queryDR.setParameter(5, centro);
        queryDR.executeUpdate();

        entityManager.clear();

        Query query = entityManager.createNativeQuery("INSERT INTO preciso_cuentas_responsables (input,componente,aplica_sicc,aplica_base_fiscal,aplica_metodologia,aplica_mis,cuenta_local,centro) " +
                " VALUES (?,?,?,?,?,?,?,?)", ResponsibleAccount.class);
        query.setParameter(1, toModify.getEntrada());
        query.setParameter(2, toModify.getComponente());
        query.setParameter(3, toModify.getSicc());
        query.setParameter(4, toModify.getBaseFiscal());
        query.setParameter(5, toModify.getMetodologia());
        query.setParameter(6, toModify.getMis());
        query.setParameter(7, toModify.getCuentaLocal());
        query.setParameter(8, toModify.getCentro());
        query.executeUpdate();

        entityManager.clear();

        Date today=new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);

        if(listUser.size()>0)
        {
            for (User user:listUser) {
                Query queryI = entityManager.createNativeQuery("INSERT INTO preciso_user_account (cuenta_local,id_usuario) VALUES (?,?)");
                queryI.setParameter(1, toModify.getCuentaLocal());
                queryI.setParameter(2, user.getUsuario());
                queryI.executeUpdate();

                entityManager.clear();
            }
        }
        else
        {
            resp="ModifyCenter/"+toModify.getCentro();
        }

        for (int i = 1; i <=12 ; i++) {
            Query queryCuadro = entityManager.createNativeQuery("INSERT INTO preciso_administracion_cuadro_mando (responsable, input, fecha_reporte, componente,empresa,estado,semaforo_componente,semaforo_input) " +
                    "VALUES (?,?,?,?,?,?,?,?)", ControlPanel.class);
            queryCuadro.setParameter(1, toModify.getCentro());
            queryCuadro.setParameter(2, toModify.getEntrada());
            if(i<10){
                queryCuadro.setParameter(3, calendar.get(Calendar.YEAR)+"-0"+i);
            }
            else{
                queryCuadro.setParameter(3, calendar.get(Calendar.YEAR)+"-"+i);
            }
            queryCuadro.setParameter(4, toModify.getComponente());
            if (listUser.size()>0){
                queryCuadro.setParameter(5, listUser.get(0).getEmpresa());
            }
            else
            {
                queryCuadro.setParameter(5, "00548");
            }
            queryCuadro.setParameter(6, false);
            queryCuadro.setParameter(7, "EMPTY");
            queryCuadro.setParameter(8, "EMPTY");
            queryCuadro.executeUpdate();

            entityManager.clear();
        }

        Audit insert = new Audit();
        insert.setAccion("Modificado registro Cuenta Responsable");
        insert.setCentro(userid.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuenta Responsable");
        insert.setNombre(userid.getPrimerNombre());
        insert.setUsuario(userid.getUsuario());
        auditRepository.save(insert);

        return resp;
    }

    public ResponsibleAccount saveResponsibleAccount(ResponsibleAccount responsibleAccount, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Agregar registro Cuenta Responsable");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuenta Responsable");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return responsibleAccountRepository.save(responsibleAccount);
    }

    public void removeResponsibleAccount(ResponsibleAccount cuenta, User user){
        Query query1 = entityManager.createNativeQuery("DELETE from preciso_user_account " +
                "WHERE cuenta_local = ?");
        query1.setParameter(1, cuenta.getCuentaLocal() );
        query1.executeUpdate();

        Query queryS = entityManager.createNativeQuery("SELECT * from preciso_cuentas_responsables " +
                "WHERE componente = ? AND input = ? AND centro = ?");
        queryS.setParameter(1, cuenta.getComponente() );
        queryS.setParameter(2, cuenta.getEntrada() );
        queryS.setParameter(3, cuenta.getCentro() );

        if(queryS.getResultList().size()==1)
        {
            Query query2 = entityManager.createNativeQuery("DELETE from preciso_administracion_cuadro_mando " +
                    "WHERE componente = ? AND input = ? AND responsable = ?");
            query2.setParameter(1, cuenta.getComponente());
            query2.setParameter(2, cuenta.getEntrada());
            query2.setParameter(3, cuenta.getCentro());
            query2.executeUpdate();
        }

        Query query = entityManager.createNativeQuery("DELETE from preciso_cuentas_responsables " +
                "WHERE cuenta_local = ?", ResponsibleAccount.class);
        query.setParameter(1, cuenta.getCuentaLocal() );
        query.executeUpdate();

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro Cuenta Responsable");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuenta Responsable");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public void clearResponsibleAccount(User user){
        //currencyRepository.deleteAll();

        Query query = entityManager.createNativeQuery("DELETE from preciso_user_account");
        query.executeUpdate();
        Query query1 = entityManager.createNativeQuery("DELETE from preciso_cuentas_responsables", ResponsibleAccount.class);
        query1.executeUpdate();
        Query queryCuadroDelete = entityManager.createNativeQuery("DELETE FROM preciso_administracion_cuadro_mando");
        queryCuadroDelete.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Cuenta Responsable");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuenta Responsable");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<ResponsibleAccount> getAll(){
        Query query = entityManager.createNativeQuery("SELECT ncr.* FROM preciso_cuentas_responsables as ncr order by ncr.cuenta_local",ResponsibleAccount.class);
        return query.getResultList();
    }

    public List<UserAccount> getCostCenterUser(String value){
        Query query = entityManager.createNativeQuery("SELECT em.cuenta_local, us.nombre, us.centro FROM preciso_user_account as em, preciso_administracion_usuarios as us " +
                "WHERE em.cuenta_local = ? AND us.usuario = em.id_usuario");
        query.setParameter(1, Long.parseLong(value));
        return query.getResultList();
    }

    public List<ResponsibleAccount> findByFilter(String value, String filter) {
        List<ResponsibleAccount> list=new ArrayList<ResponsibleAccount>();
        switch (filter)
        {
            case "Cuenta Local":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                        "WHERE em.cuenta_local LIKE ?", ResponsibleAccount.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Usuario":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em, preciso_administracion_usuarios as us " +
                        "WHERE us.usuario LIKE ?", ResponsibleAccount.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Input":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                        "WHERE em.input LIKE ?", ResponsibleAccount.class);
                query1.setParameter(1, value.toUpperCase());

                list= query1.getResultList();
                break;
            case "Componente":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                        "WHERE em.componente LIKE ?", ResponsibleAccount.class);
                query3.setParameter(1, value.toUpperCase());

                list= query3.getResultList();
                break;
            case "Aplica SICC":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                        "WHERE em.aplica_sicc LIKE ?", ResponsibleAccount.class);
                if(value.toUpperCase(Locale.ROOT).equals("TRUE")||value.toUpperCase(Locale.ROOT).equals("VERDADERO"))
                {
                    value="1";
                }
                else if(value.toUpperCase(Locale.ROOT).equals("FALSE")||value.toUpperCase(Locale.ROOT).equals("FALSO"))
                {
                    value="0";
                }
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Aplica Base Fiscal":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                        "WHERE em.aplica_base_fiscal LIKE ?", ResponsibleAccount.class);
                if(value.toUpperCase(Locale.ROOT).equals("TRUE")||value.toUpperCase(Locale.ROOT).equals("VERDADERO"))
                {
                    value="1";
                }
                else if(value.toUpperCase(Locale.ROOT).equals("FALSE")||value.toUpperCase(Locale.ROOT).equals("FALSO"))
                {
                    value="0";
                }
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Aplica Metodología":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                        "WHERE em.aplica_metodologia LIKE ?", ResponsibleAccount.class);
                if(value.toUpperCase(Locale.ROOT).equals("TRUE")||value.toUpperCase(Locale.ROOT).equals("VERDADERO"))
                {
                    value="1";
                }
                else if(value.toUpperCase(Locale.ROOT).equals("FALSE")||value.toUpperCase(Locale.ROOT).equals("FALSO"))
                {
                    value="0";
                }
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
        case "Aplica MIS":
            Query query9 = entityManager.createNativeQuery("SELECT em.* FROM preciso_cuentas_responsables as em " +
                    "WHERE em.aplica_mis LIKE ?", ResponsibleAccount.class);
            if(value.toUpperCase(Locale.ROOT).equals("TRUE")||value.toUpperCase(Locale.ROOT).equals("VERDADERO"))
            {
                value="1";
            }
            else if(value.toUpperCase(Locale.ROOT).equals("FALSE")||value.toUpperCase(Locale.ROOT).equals("FALSO"))
            {
                value="0";
            }
            query9.setParameter(1, value);

            list= query9.getResultList();
            break;
            case "Centro":
                Query query10 = entityManager.createNativeQuery("SELECT ncr.* FROM preciso_cuentas_responsables as ncr " +
                        "WHERE ncr.centro like ?", ResponsibleAccount.class);
                query10.setParameter(1, value);
                list= query10.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public List<String> getComponents(){
        Query query = entityManager.createNativeQuery("SELECT em.componente FROM preciso_cuentas_responsables as em " +
                "group by em.componente");

        return query.getResultList();
    }

    public List<String> getInputs(){
        Query query = entityManager.createNativeQuery("SELECT em.input FROM preciso_cuentas_responsables as em " +
                "group by em.input");

        return query.getResultList();
    }
}
