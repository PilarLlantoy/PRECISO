package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreation;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationPlane;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.AccountCreationRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.GarantBankRepository;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import com.inter.proyecto_intergrupo.utility.Utility;
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
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class AccountCreationService {

    @Autowired
    private AccountCreationRepository accountCreationRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    SendEmailService sendEmailService;

    @PersistenceContext
    EntityManager entityManager;

    public AccountCreationService(AccountCreationRepository accountCreationRepository, AuditRepository auditRepository) {
        this.accountCreationRepository = accountCreationRepository;
        this.auditRepository = auditRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file,User user,String perfil) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            if(perfil.equals("GENERAL")) {
                list = validarPlantillaGeneral(rows, user, perfil);
            }
            else if(perfil.equals("GESTION")) {
                list = validarPlantillaGestion(rows, user, perfil);
            }
            else if(perfil.equals("CONSOLIDACION")) {
                list = validarPlantillaConsolidacion(rows, user, perfil);
            }
            else if(perfil.equals("CONTROL CONTABLE")) {
                list = validarPlantillaControl(rows, user, perfil);
            }
            String[] temporal= list.get(0);
            if(temporal[2].equals("0"))
            {
                //getRows(rows1,user);
                auditCode("Cargue datos actualizados de archivo Rechazos "+ perfil,user,perfil);
            }
        }
        return list;
    }

    public void auditCode (String info,User user, String perfil){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setCentro(user.getCentro());
        insert.setComponente("CREACIÓN DE CUENTAS");
        insert.setFecha(today);
        insert.setInput("ACTUALIZACIÓN DATA "+perfil);
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ArrayList<String[]> validarPlantillaGestion(Iterator<Row> rows, User user, String perfil) {
        ArrayList lista= new ArrayList();
        List<AccountCreation> listAdd= new ArrayList<AccountCreation>();
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
                String cellEmpresa = formatter.formatCellValue(row.getCell(0)).trim();
                String cellNumeroCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodigoGestion = formatter.formatCellValue(row.getCell(6)).trim();
                String cellEpigrafe = formatter.formatCellValue(row.getCell(7)).trim();

                log[0]=String.valueOf(row.getRowNum());
                List<AccountCreation> listSearch = getAccount(cellEmpresa,cellNumeroCuenta);

                if(cellEmpresa.length()==0 || cellEmpresa.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Empresa Vacía";
                    fail++;
                    lista.add(log);
                }
                else if(cellNumeroCuenta.length()==0 || cellNumeroCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Cuenta Vacía";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoGestion.length()==0 ||cellCodigoGestion.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló Código Gestión Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellEpigrafe.length()!=7)
                {
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló Epigrafe debe tener 7 caracteres";
                    fail++;
                    lista.add(log);
                }
                else if(listSearch.size()==0)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Cuenta no encontrada en listado, no modifique la cuenta";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    listSearch.get(0).setCODIGOGESTION(cellCodigoGestion);
                    listSearch.get(0).setEPIGRAFE(cellEpigrafe);
                    listAdd.add(listSearch.get(0));
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RECHAZOS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Falla al cargar datos actualizados de archivo Rechazos "+ perfil,user,perfil);
        }
        else
        {
            accountCreationRepository.saveAll(listAdd);
        }
        return lista;
    }

    public ArrayList<String[]> validarPlantillaConsolidacion(Iterator<Row> rows, User user, String perfil) {
        ArrayList lista= new ArrayList();
        List<AccountCreation> listAdd= new ArrayList<AccountCreation>();
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
                String cellEmpresa = formatter.formatCellValue(row.getCell(0)).trim();
                String cellNumeroCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodicons = formatter.formatCellValue(row.getCell(6)).trim();

                log[0]=String.valueOf(row.getRowNum());
                List<AccountCreation> listSearch = getAccount(cellEmpresa,cellNumeroCuenta);

                if(cellEmpresa.length()==0 || cellEmpresa.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Empresa Vacía";
                    fail++;
                    lista.add(log);
                }
                else if(cellNumeroCuenta.length()==0 || cellNumeroCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Cuenta Vacía";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodicons.length()==0 ||cellCodicons.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló Código Gestión Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(listSearch.size()==0)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Cuenta no encontrada en listado, no modifique la cuenta";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    listSearch.get(0).setCONSOLID(cellCodicons);
                    listAdd.add(listSearch.get(0));
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RECHAZOS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Falla al cargar datos actualizados de archivo Rechazos "+ perfil,user,perfil);
        }
        else
        {
            accountCreationRepository.saveAll(listAdd);
        }
        return lista;
    }

    public ArrayList<String[]> validarPlantillaControl(Iterator<Row> rows, User user, String perfil) {
        ArrayList lista= new ArrayList();
        List<AccountCreation> listAdd= new ArrayList<AccountCreation>();
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
                String cellEmpresa = formatter.formatCellValue(row.getCell(0)).trim();
                String cellNumeroCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(6)).trim();
                String cellDias = formatter.formatCellValue(row.getCell(7)).trim();
                String cellIndicador = formatter.formatCellValue(row.getCell(8)).trim();
                String cellApunte = formatter.formatCellValue(row.getCell(9)).trim();
                String cellInventarable = formatter.formatCellValue(row.getCell(10)).trim();

                log[0]=String.valueOf(row.getRowNum());
                List<AccountCreation> listSearch = getAccount(cellEmpresa,cellNumeroCuenta);

                if(cellEmpresa.length()==0 || cellEmpresa.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Empresa Vacía";
                    fail++;
                    lista.add(log);
                }
                else if(cellNumeroCuenta.length()==0 || cellNumeroCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Cuenta Vacía";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoControl.length()==0 ||cellCodigoControl.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló Código Control Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellDias.length()==0 ||cellDias.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló Días de Plazo Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellIndicador.length()==0 ||cellIndicador.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(8)+" - (9)";
                    log[2]="false";
                    log[3]="Falló Indicador de la Cuenta Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellApunte.length()==0 ||cellApunte.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(9)+" - (10)";
                    log[2]="false";
                    log[3]="Falló Tipo de Apunte Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellInventarable.length()==0 ||cellInventarable.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(10)+" - (11)";
                    log[2]="false";
                    log[3]="Falló Campo Inventariable Vacío";
                    fail++;
                    lista.add(log);
                }
                else if(listSearch.size()==0)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Cuenta no encontrada en listado, no modifique la cuenta";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    listSearch.get(0).setCODIGODECONTROL(cellCodigoControl);
                    listSearch.get(0).setDIASDEPLAZO(cellDias);
                    listSearch.get(0).setINDICADORDELACUENTA(cellIndicador);
                    listSearch.get(0).setTIPODEAPUNTE(cellApunte);
                    listSearch.get(0).setINVENTARIABLE(cellInventarable);
                    listAdd.add(listSearch.get(0));
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RECHAZOS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Falla al cargar datos actualizados de archivo Rechazos "+ perfil,user,perfil);
        }
        else
        {
            accountCreationRepository.saveAll(listAdd);
        }
        return lista;
    }

    public ArrayList<String[]> validarPlantillaGeneral(Iterator<Row> rows, User user, String perfil) {
        ArrayList lista= new ArrayList();
        List<AccountCreation> listAdd= new ArrayList<AccountCreation>();
        XSSFRow row;
        int fail=0;
        int success =0;
        int failList = lista.size();
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0) {
                DataFormatter formatter = new DataFormatter();
                String cellEmpresa = formatter.formatCellValue(row.getCell(0)).trim();
                String cellNumeroCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCuenta4 = "";
                String cellSubCuenta2 = "";
                String cellSub = "";
                String cellSeg = "";
                String cellStag = "";
                String cellNombreCuenta = formatter.formatCellValue(row.getCell(7)).trim();
                String cellNombreCortoCuenta = formatter.formatCellValue(row.getCell(8)).trim();
                String cellTipoCta = formatter.formatCellValue(row.getCell(9)).trim();
                String cellIndic = formatter.formatCellValue(row.getCell(10)).trim();
                String cellClaveAcceso = formatter.formatCellValue(row.getCell(11)).trim();
                String cellMon = formatter.formatCellValue(row.getCell(12)).trim();
                String cellTICTOO1 = formatter.formatCellValue(row.getCell(13)).trim();
                String cellTICTOO2 = formatter.formatCellValue(row.getCell(14)).trim();
                String cellTICTOO3 = formatter.formatCellValue(row.getCell(15)).trim();
                String cellTICTOO4 = formatter.formatCellValue(row.getCell(16)).trim();
                String cellTICTOO5 = formatter.formatCellValue(row.getCell(17)).trim();
                String cellTICENAO = formatter.formatCellValue(row.getCell(18)).trim();
                String cellCENAUO01 = formatter.formatCellValue(row.getCell(19)).trim();
                String cellCENAUO02 = formatter.formatCellValue(row.getCell(20)).trim();
                String cellCENAUO03 = formatter.formatCellValue(row.getCell(21)).trim();
                String cellCENAUO04 = formatter.formatCellValue(row.getCell(22)).trim();
                String cellCENAUO05 = formatter.formatCellValue(row.getCell(23)).trim();
                String cellCENAUO06 = formatter.formatCellValue(row.getCell(24)).trim();
                String cellCENAUO07 = formatter.formatCellValue(row.getCell(25)).trim();
                String cellCENAUO08 = formatter.formatCellValue(row.getCell(26)).trim();
                String cellCENAUO09 = formatter.formatCellValue(row.getCell(27)).trim();
                String cellTICTOD1 = formatter.formatCellValue(row.getCell(28)).trim();
                String cellTICTOD2 = formatter.formatCellValue(row.getCell(29)).trim();
                String cellTICTOD3 = formatter.formatCellValue(row.getCell(30)).trim();
                String cellTICTOD4 = formatter.formatCellValue(row.getCell(31)).trim();
                String cellTICTOD5 = formatter.formatCellValue(row.getCell(32)).trim();
                String cellTICENAD = formatter.formatCellValue(row.getCell(33)).trim();
                String cellCENAUD01 = formatter.formatCellValue(row.getCell(34)).trim();
                String cellCENAUD02 = formatter.formatCellValue(row.getCell(35)).trim();
                String cellCENAUD03 = formatter.formatCellValue(row.getCell(36)).trim();
                String cellCENAUD04 = formatter.formatCellValue(row.getCell(37)).trim();
                String cellCENAUD05 = formatter.formatCellValue(row.getCell(38)).trim();
                String cellCENAUD06 = formatter.formatCellValue(row.getCell(39)).trim();
                String cellCENAUD07 = formatter.formatCellValue(row.getCell(40)).trim();
                String cellCENAUD08 = formatter.formatCellValue(row.getCell(41)).trim();
                String cellCENAUD09 = formatter.formatCellValue(row.getCell(42)).trim();
                String cellTIPAPUN = formatter.formatCellValue(row.getCell(43)).trim();
                String cellSIGINIC = formatter.formatCellValue(row.getCell(44)).trim();
                String cellINDICADORPROCESOBAJA = formatter.formatCellValue(row.getCell(45)).trim();
                String cellINDCUENTAINVENTARIABLE = formatter.formatCellValue(row.getCell(46)).trim();
                String cellINDCUENTAOPERACIONAL = formatter.formatCellValue(row.getCell(47)).trim();
                String cellCODCDCONMEX = formatter.formatCellValue(row.getCell(48)).trim();
                String cellCODREPREGUL = formatter.formatCellValue(row.getCell(49)).trim();
                String cellINTERFAZ = formatter.formatCellValue(row.getCell(50)).trim();
                String cellRESPONSABLEOPERATIVONIVEL1 = formatter.formatCellValue(row.getCell(51)).trim();
                String cellRESPONSABLEOPERATIVOCENOPERES1 = formatter.formatCellValue(row.getCell(52)).trim();
                String cellRESPONSABLEOPERATIVOCENOPERES2 = formatter.formatCellValue(row.getCell(53)).trim();
                String cellRESPONSABLEOPERATIVOCENOPERES3 = formatter.formatCellValue(row.getCell(54)).trim();
                String cellRESPONSABLEOPERATIVOCENOPERES4 = formatter.formatCellValue(row.getCell(55)).trim();
                String cellRESPONSABLEOPERATIVOCENOPERES5 = formatter.formatCellValue(row.getCell(56)).trim();
                String cellRESPONSABLEOPERATIVOCENOPERES6 = formatter.formatCellValue(row.getCell(57)).trim();
                String cellRESPONSABLEGESTIONNIVEL2 = formatter.formatCellValue(row.getCell(58)).trim();
                String cellRESPONSABLEGESTIONCENGESRES1 = formatter.formatCellValue(row.getCell(59)).trim();
                String cellRESPONSABLEGESTIONCENGESRES2 = formatter.formatCellValue(row.getCell(60)).trim();
                String cellRESPONSABLEGESTIONCENGESRES3 = formatter.formatCellValue(row.getCell(61)).trim();
                String cellRESPONSABLEGESTIONCENGESRES4 = formatter.formatCellValue(row.getCell(62)).trim();
                String cellRESPONSABLEGESTIONCENGESRES5 = formatter.formatCellValue(row.getCell(63)).trim();
                String cellRESPONSABLEGESTIONCENGESRES6 = formatter.formatCellValue(row.getCell(64)).trim();
                String cellRESPONSABLEADMINISTRATIVONIVEL3 = formatter.formatCellValue(row.getCell(65)).trim();
                String cellRESPONSABLEADMINISTRATIVOCENADMRES1 = formatter.formatCellValue(row.getCell(66)).trim();
                String cellRESPONSABLEADMINISTRATIVOCENADMRES2 = formatter.formatCellValue(row.getCell(67)).trim();
                String cellRESPONSABLEADMINISTRATIVOCENADMRES3 = formatter.formatCellValue(row.getCell(68)).trim();
                String cellRESPONSABLEADMINISTRATIVOCENADMRES4 = formatter.formatCellValue(row.getCell(69)).trim();
                String cellRESPONSABLEADMINISTRATIVOCENADMRES5 = formatter.formatCellValue(row.getCell(70)).trim();
                String cellRESPONSABLEADMINISTRATIVOCENADMRES6 = formatter.formatCellValue(row.getCell(71)).trim();
                String cellCONTRAPARTIDAORDEN = formatter.formatCellValue(row.getCell(72)).trim();
                String cellCONTRAPARTIDARESULTADOSDH = formatter.formatCellValue(row.getCell(73)).trim();
                String cellCodigoGestion = formatter.formatCellValue(row.getCell(74)).trim();
                String cellEpigrafe = formatter.formatCellValue(row.getCell(75)).trim();
                String cellConsolid = formatter.formatCellValue(row.getCell(76)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(77)).trim();
                String cellDias = formatter.formatCellValue(row.getCell(78)).trim();
                String cellIndicador = formatter.formatCellValue(row.getCell(79)).trim();
                String cellApunte = formatter.formatCellValue(row.getCell(80)).trim();
                String cellInventarable = formatter.formatCellValue(row.getCell(81)).trim();

                List<AccountCreation> listSearch = getAccount(cellEmpresa, cellNumeroCuenta);

                try {
                    Long.parseLong(cellNumeroCuenta);
                    if(cellNumeroCuenta.length()>=4)
                        cellCuenta4 = cellNumeroCuenta.substring(0,4);
                    if(cellNumeroCuenta.length()>=6)
                        cellSubCuenta2 = cellNumeroCuenta.substring(4,6);
                    if(cellNumeroCuenta.length()>=9)
                        cellSub = cellNumeroCuenta.substring(6,9);
                    if(cellNumeroCuenta.length()>=12)
                        cellSeg = cellNumeroCuenta.substring(9,12);
                    if(cellNumeroCuenta.length()>=15)
                        cellStag = cellNumeroCuenta.substring(12,15);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    String[] log0=new String[4];
                    log0[0]=String.valueOf(row.getRowNum()+1);
                    log0[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log0[2]="false";
                    log0[3]="Falló Número de cuenta debe ser tipo de dato Númerico";
                    fail++;
                    lista.add(log0);
                    continue;
                }

                if(cellEmpresa.length()==0 || cellEmpresa.length()>255)
                {
                    String[] log1=new String[4];
                    log1[0]=String.valueOf(row.getRowNum()+1);
                    log1[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log1[2]="false";
                    log1[3]="Falló Empresa Vacía";
                    fail++;
                    lista.add(log1);
                }
                if(cellIndic.length()==0 ||cellIndic.length()>255)
                {
                    String[] log2=new String[4];
                    log2[0]=String.valueOf(row.getRowNum()+1);
                    log2[1]=CellReference.convertNumToColString(10)+" - (11)";
                    log2[2]="false";
                    log2[3]="Falló Indicador Cuenta Vacío";
                    fail++;
                    lista.add(log2);
                }
                if((cellNumeroCuenta.length()==0 || cellNumeroCuenta.length()>255) && cellIndic.equals("I"))
                {
                    String[] log3=new String[4];
                    log3[0]=String.valueOf(row.getRowNum()+1);
                    log3[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log3[2]="false";
                    log3[3]="Falló Cuenta Vacía";
                    fail++;
                    lista.add(log3);
                }
                if((cellNumeroCuenta.length()==0 || cellNumeroCuenta.length()>255) && cellIndic.equals("L"))
                {
                    String[] log4=new String[4];
                    log4[0]=String.valueOf(row.getRowNum()+1);
                    log4[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log4[2]="false";
                    log4[3]="Falló Cuenta Vacía";
                    fail++;
                    lista.add(log4);
                }
                if(cellNumeroCuenta.length()!=6 && cellIndic.equals("L"))
                {
                    if(cellNumeroCuenta.length()!=9 )
                    {
                        if(cellNumeroCuenta.length()!=12) {
                            String[] log5=new String[4];
                            log5[0]=String.valueOf(row.getRowNum()+1);
                            log5[1] = CellReference.convertNumToColString(1) + " - (2)";
                            log5[2] = "false";
                            log5[3] = "Falló Cuenta debe ser de nivel 6, 9 o 12 para literal";
                            fail++;
                            lista.add(log5);
                        }
                    }
                }
                if(cellNombreCuenta.length()==0 ||cellNombreCuenta.length()>65)
                {
                    String[] log11=new String[4];
                    log11[0]=String.valueOf(row.getRowNum()+1);
                    log11[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log11[2]="false";
                    log11[3]="Falló Nombre Cuenta Vacío o tiene mas de 65 caracteres";
                    fail++;
                    lista.add(log11);
                }
                if(cellNombreCortoCuenta.length()==0 ||cellNombreCortoCuenta.length()>35)
                {
                    String[] log12=new String[4];
                    log12[0]=String.valueOf(row.getRowNum()+1);
                    log12[1]=CellReference.convertNumToColString(8)+" - (9)";
                    log12[2]="false";
                    log12[3]="Falló Nombre Corto Cuenta Vacío o tiene mas de 35 caracteres";
                    fail++;
                    lista.add(log12);
                }
                if(cellTipoCta.length()==0 ||cellTipoCta.length()>255)
                {
                    String[] log13=new String[4];
                    log13[0]=String.valueOf(row.getRowNum()+1);
                    log13[1]=CellReference.convertNumToColString(9)+" - (10)";
                    log13[2]="false";
                    log13[3]="Falló Tipo CTA Vacío";
                    fail++;
                    lista.add(log13);
                }
                if((cellClaveAcceso.length()==0 ||cellClaveAcceso.length()>255) && cellIndic.equals("I"))
                {
                    String[] log14=new String[4];
                    log14[0]=String.valueOf(row.getRowNum()+1);
                    log14[1]=CellReference.convertNumToColString(11)+" - (12)";
                    log14[2]="false";
                    log14[3]="Falló Clave Acceso Vacío";
                    fail++;
                    lista.add(log14);
                }
                if(cellMon.length()==0 ||cellMon.length()>255)
                {
                    String[] log15=new String[4];
                    log15[0]=String.valueOf(row.getRowNum()+1);
                    log15[1]=CellReference.convertNumToColString(11)+" - (12)";
                    log15[2]="false";
                    log15[3]="Falló Mon Vacío";
                    fail++;
                    lista.add(log15);
                }
                if((cellTICENAO.length()==0 ||cellTICENAO.length()>255) && cellIndic.equals("I"))
                {
                    String[] log16=new String[4];
                    log16[0]=String.valueOf(row.getRowNum()+1);
                    log16[1]=CellReference.convertNumToColString(18)+" - (19)";
                    log16[2]="false";
                    log16[3]="Falló TICENAO Vacío";
                    fail++;
                    lista.add(log16);
                }
                if((cellCENAUO01.length()==0 ||cellCENAUO01.length()>255) && cellIndic.equals("I"))
                {
                    String[] log17=new String[4];
                    log17[0]=String.valueOf(row.getRowNum()+1);
                    log17[1]=CellReference.convertNumToColString(19)+" - (20)";
                    log17[2]="false";
                    log17[3]="Falló CENAUO01 Vacío";
                    fail++;
                    lista.add(log17);
                }
                if((cellCODREPREGUL.length()==0 ||cellCODREPREGUL.length()>255) && cellIndic.equals("I"))
                {
                    String[] log18=new String[4];
                    log18[0]=String.valueOf(row.getRowNum()+1);
                    log18[1]=CellReference.convertNumToColString(49)+" - (50)";
                    log18[2]="false";
                    log18[3]="Falló COD-REPREGUL Vacío";
                    fail++;
                    lista.add(log18);
                }
                if((cellRESPONSABLEOPERATIVONIVEL1.length()==0 ||cellRESPONSABLEOPERATIVONIVEL1.length()>255) && cellIndic.equals("I"))
                {
                    String[] log19=new String[4];
                    log19[0]=String.valueOf(row.getRowNum()+1);
                    log19[1]=CellReference.convertNumToColString(51)+" - (52)";
                    log19[2]="false";
                    log19[3]="Falló RESPONSABLE CONTROL OPERATIVO NIVEL1 Vacío";
                    fail++;
                    lista.add(log19);
                }
                if((cellRESPONSABLEOPERATIVOCENOPERES1.length()==0 ||cellRESPONSABLEOPERATIVOCENOPERES1.length()>255) && cellIndic.equals("I"))
                {
                    String[] log20=new String[4];
                    log20[0]=String.valueOf(row.getRowNum()+1);
                    log20[1]=CellReference.convertNumToColString(52)+" - (53)";
                    log20[2]="false";
                    log20[3]="Falló RESPONSABLE CONTROL OPERATIVO CEN-OPE-RES1 Vacío";
                    fail++;
                    lista.add(log20);
                }
                if((cellRESPONSABLEGESTIONNIVEL2.length()==0 ||cellRESPONSABLEGESTIONNIVEL2.length()>255) && cellIndic.equals("I"))
                {
                    String[] log21=new String[4];
                    log21[0]=String.valueOf(row.getRowNum()+1);
                    log21[1]=CellReference.convertNumToColString(58)+" - (59)";
                    log21[2]="false";
                    log21[3]="Falló RESPONSABLE CONTROL DE GESTION NIVEL2 Vacío";
                    fail++;
                    lista.add(log21);
                }
                if((cellRESPONSABLEGESTIONCENGESRES1.length()==0 ||cellRESPONSABLEGESTIONCENGESRES1.length()>255) && cellIndic.equals("I"))
                {
                    String[] log22=new String[4];
                    log22[0]=String.valueOf(row.getRowNum()+1);
                    log22[1]=CellReference.convertNumToColString(59)+" - (60)";
                    log22[2]="false";
                    log22[3]="Falló RESPONSABLE CONTROL DE GESTION CEN-GES-RES1 Vacío";
                    fail++;
                    lista.add(log22);
                }
                if((cellRESPONSABLEADMINISTRATIVONIVEL3.length()==0 ||cellRESPONSABLEADMINISTRATIVONIVEL3.length()>255) && cellIndic.equals("I"))
                {
                    String[] log23=new String[4];
                    log23[0]=String.valueOf(row.getRowNum()+1);
                    log23[1]=CellReference.convertNumToColString(65)+" - (66)";
                    log23[2]="false";
                    log23[3]="Falló RESPONSABLE CONTROL ADMINISTRATIVO NIVEL3 Vacío";
                    fail++;
                    lista.add(log23);
                }
                if((cellRESPONSABLEADMINISTRATIVOCENADMRES1.length()==0 ||cellRESPONSABLEADMINISTRATIVOCENADMRES1.length()>255) && cellIndic.equals("I"))
                {
                    String[] log24=new String[4];
                    log24[0]=String.valueOf(row.getRowNum()+1);
                    log24[1]=CellReference.convertNumToColString(66)+" - (67)";
                    log24[2]="false";
                    log24[3]="Falló RESPONSABLE CONTROL ADMINISTRATIVO CEN-ADM-RES1 Vacío";
                    fail++;
                    lista.add(log24);
                }
                if(lista.size()==failList)
                {
                    if(listSearch.size()==0)
                    {
                        listSearch.add(new AccountCreation());
                        listSearch.get(0).setOrigen("CUENTAS");
                    }
                    listSearch.get(0).setEMPRESA(cellEmpresa);
                    listSearch.get(0).setNUMEROCUENTA(cellNumeroCuenta);
                    listSearch.get(0).setCUENTA4(cellCuenta4);
                    listSearch.get(0).setSUBCUENTA2(cellSubCuenta2);
                    listSearch.get(0).setSUB(cellSub);
                    listSearch.get(0).setSEG(cellSeg);
                    listSearch.get(0).setSTAG(cellStag);
                    listSearch.get(0).setNOMBRECUENTA(cellNombreCuenta);
                    listSearch.get(0).setNOMBRECORTOCUENTA(cellNombreCortoCuenta);
                    listSearch.get(0).setTIPOCTA(cellTipoCta);
                    listSearch.get(0).setINDICLI(cellIndic);
                    listSearch.get(0).setCLAVEACCESO(cellClaveAcceso);
                    listSearch.get(0).setMON(cellMon);
                    listSearch.get(0).setTICTOO1(cellTICTOO1);
                    listSearch.get(0).setTICTOO2(cellTICTOO2);
                    listSearch.get(0).setTICTOO3(cellTICTOO3);
                    listSearch.get(0).setTICTOO4(cellTICTOO4);
                    listSearch.get(0).setTICTOO5(cellTICTOO5);
                    listSearch.get(0).setTICENAO(cellTICENAO);
                    listSearch.get(0).setCENAUO01(cellCENAUO01);
                    listSearch.get(0).setCENAUO02(cellCENAUO02);
                    listSearch.get(0).setCENAUO03(cellCENAUO03);
                    listSearch.get(0).setCENAUO04(cellCENAUO04);
                    listSearch.get(0).setCENAUO05(cellCENAUO05);
                    listSearch.get(0).setCENAUO06(cellCENAUO06);
                    listSearch.get(0).setCENAUO07(cellCENAUO07);
                    listSearch.get(0).setCENAUO08(cellCENAUO08);
                    listSearch.get(0).setCENAUO09(cellCENAUO09);
                    listSearch.get(0).setTICTOD1(cellTICTOD1);
                    listSearch.get(0).setTICTOD2(cellTICTOD2);
                    listSearch.get(0).setTICTOD3(cellTICTOD3);
                    listSearch.get(0).setTICTOD4(cellTICTOD4);
                    listSearch.get(0).setTICTOD5(cellTICTOD5);
                    listSearch.get(0).setTICENAD(cellTICENAD);
                    listSearch.get(0).setCENAUD01(cellCENAUD01);
                    listSearch.get(0).setCENAUD02(cellCENAUD02);
                    listSearch.get(0).setCENAUD03(cellCENAUD03);
                    listSearch.get(0).setCENAUD04(cellCENAUD04);
                    listSearch.get(0).setCENAUD05(cellCENAUD05);
                    listSearch.get(0).setCENAUD06(cellCENAUD06);
                    listSearch.get(0).setCENAUD07(cellCENAUD07);
                    listSearch.get(0).setCENAUD08(cellCENAUD08);
                    listSearch.get(0).setCENAUD09(cellCENAUD09);
                    listSearch.get(0).setTIPAPUN(cellTIPAPUN);
                    listSearch.get(0).setSIGINIC(cellSIGINIC);
                    listSearch.get(0).setINDICADORPROCESODEBAJA(cellINDICADORPROCESOBAJA);
                    listSearch.get(0).setINDCUENTAINVENTARIABLE(cellINDCUENTAINVENTARIABLE);
                    listSearch.get(0).setINDCUENTAOPERACIONAL(cellINDCUENTAOPERACIONAL);
                    listSearch.get(0).setCODCDCONMEX(cellCODCDCONMEX);
                    listSearch.get(0).setCODREPREGUL(cellCODREPREGUL);
                    listSearch.get(0).setINTERFAZ(cellINTERFAZ);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVONIVEL1(cellRESPONSABLEOPERATIVONIVEL1);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVOCENOPERES1(cellRESPONSABLEOPERATIVOCENOPERES1);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVOCENOPERES2(cellRESPONSABLEOPERATIVOCENOPERES2);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVOCENOPERES3(cellRESPONSABLEOPERATIVOCENOPERES3);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVOCENOPERES4(cellRESPONSABLEOPERATIVOCENOPERES4);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVOCENOPERES5(cellRESPONSABLEOPERATIVOCENOPERES5);
                    listSearch.get(0).setRESPONSABLECONTROLOPERATIVOCENOPERES6(cellRESPONSABLEOPERATIVOCENOPERES6);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONNIVEL2(cellRESPONSABLEGESTIONNIVEL2);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONCENGESRES1(cellRESPONSABLEGESTIONCENGESRES1);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONCENGESRES2(cellRESPONSABLEGESTIONCENGESRES2);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONCENGESRES3(cellRESPONSABLEGESTIONCENGESRES3);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONCENGESRES4(cellRESPONSABLEGESTIONCENGESRES4);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONCENGESRES5(cellRESPONSABLEGESTIONCENGESRES5);
                    listSearch.get(0).setRESPONSABLECONTROLDEGESTIONCENGESRES6(cellRESPONSABLEGESTIONCENGESRES6);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVONIVEL3(cellRESPONSABLEADMINISTRATIVONIVEL3);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVOCENADMRES1(cellRESPONSABLEADMINISTRATIVOCENADMRES1);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVOCENADMRES2(cellRESPONSABLEADMINISTRATIVOCENADMRES2);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVOCENADMRES3(cellRESPONSABLEADMINISTRATIVOCENADMRES3);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVOCENADMRES4(cellRESPONSABLEADMINISTRATIVOCENADMRES4);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVOCENADMRES5(cellRESPONSABLEADMINISTRATIVOCENADMRES5);
                    listSearch.get(0).setRESPONSABLECONTROLADMINISTRATIVOCENADMRES6(cellRESPONSABLEADMINISTRATIVOCENADMRES6);
                    listSearch.get(0).setCONTRAPARTIDADEORDEN(cellCONTRAPARTIDAORDEN);
                    listSearch.get(0).setCONTRAPARTIDADERESULTADOSDH(cellCONTRAPARTIDARESULTADOSDH);
                    listSearch.get(0).setCODIGOGESTION(cellCodigoGestion);
                    listSearch.get(0).setEPIGRAFE(cellEpigrafe);
                    listSearch.get(0).setCONSOLID(cellConsolid);
                    listSearch.get(0).setCODIGODECONTROL(cellCodigoControl);
                    listSearch.get(0).setDIASDEPLAZO(cellDias);
                    listSearch.get(0).setINDICADORDELACUENTA(cellIndicador);
                    listSearch.get(0).setTIPODEAPUNTE(cellApunte);
                    listSearch.get(0).setINVENTARIABLE(cellInventarable);
                    listAdd.add(listSearch.get(0));
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RECHAZOS";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Falla al cargar datos actualizados de archivo Rechazos "+ perfil,user,perfil);
        }
        else
        {
            accountCreationRepository.saveAll(listAdd);

            Query updateAll3 = entityManager.createNativeQuery("update preciso_administracion_cuadro_mando_ifrs \n" +
                    "set semaforo_componente = 'PENDING' \n" +
                    "where orden > 1");
            updateAll3.executeUpdate();
        }
        return lista;
    }

    public boolean creationStagesR(String perfil, User user, HttpServletRequest request)
    {
        if(perfil.equals("GENERAL"))
        {
            Query updateAll3 = entityManager.createNativeQuery("update preciso_administracion_cuadro_mando_ifrs \n" +
                    "set semaforo_componente = 'PENDING' \n" +
                    "where orden > 1");
            updateAll3.executeUpdate();

            clearAccountCreation("RECHAZOS");
            insertIntoAccountCreation("RECHAZOS");

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean creationStagesC(String perfil, User user, HttpServletRequest request)
    {
        if(perfil.equals("GENERAL"))
        {
            Query updateAll3 = entityManager.createNativeQuery("update preciso_administracion_cuadro_mando_ifrs \n" +
                    "set semaforo_componente = 'PENDING' \n" +
                    "where orden > 1");
            updateAll3.executeUpdate();

            clearAccountCreation("CUENTAS");
            insertIntoAccountCreation("CUENTAS");

            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean confirmData(String perfil, User user, HttpServletRequest request)
    {
        try{
            List<AccountCreation> list = new ArrayList<AccountCreation>();
            if(perfil.equals("GESTION"))
            {
                Query validate = entityManager.createNativeQuery("SELECT * FROM nexco_creacion_cuentas WHERE (codigo_gestion = '' OR epigrafe = '' OR \n" +
                        "codigo_gestion IS NULL OR epigrafe IS NULL) AND indic_li = 'I'", AccountCreation.class);
                list = validate.getResultList();
            }
            else if(perfil.equals("GENERAL"))
            {
                Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_creacion_cuentas WHERE empresa = '' OR numero_cuenta = '' OR nombre_corto_cuenta = '' OR nombre_cuenta = '' OR tipocta = '' OR indic_li = '' OR mon = '' OR " +
                        "empresa IS NULL OR numero_cuenta IS NULL OR nombre_corto_cuenta IS NULL OR tipocta IS NULL OR indic_li IS NULL OR mon IS NULL OR nombre_cuenta IS NULL", AccountCreation.class);
                list = verify.getResultList();
            }
            else if(perfil.equals("CONSOLIDACION")) {
                Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_creacion_cuentas WHERE (consolid = '' OR \n" +
                        "consolid IS NULL) AND indic_li = 'I'", AccountCreation.class);
                list = verify.getResultList();
            }
            else if(perfil.equals("CONTROL CONTABLE")) {
                Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_creacion_cuentas WHERE (codigo_de_control = '' OR dias_de_plazo = '' OR indicador_de_la_cuenta = '' OR tipo_de_apunte = '' OR inventariable = '' OR \n" +
                        " codigo_de_control IS NULL OR dias_de_plazo IS NULL OR indicador_de_la_cuenta IS NULL OR tipo_de_apunte IS NULL OR inventariable IS NULL) AND indic_li = 'I'", AccountCreation.class);
                list = verify.getResultList();
            }
            if(list.size()==0) {
                Date hoy = new Date();
                Query updateAll2 = entityManager.createNativeQuery("update preciso_administracion_cuadro_mando_ifrs \n" +
                        "set semaforo_componente = 'FULL', usuario_carga = ?  , fecha_cargue = ?\n" +
                        "where componente = ? AND input = ?");
                if (perfil.equals("GENERAL")) {
                    updateAll2.setParameter(3, "STAGES");
                    updateAll2.setParameter(1, user.getPrimerNombre());
                    updateAll2.setParameter(2, hoy);
                    Query updateAll3 = entityManager.createNativeQuery("update preciso_administracion_cuadro_mando_ifrs \n" +
                            "set semaforo_componente = 'PENDING' \n" +
                            "where orden > 2");
                    updateAll3.executeUpdate();

                    String resetPasswordLink = Utility.getSiteURL(request) + "/ifrs/accountCreation";
                    Query emails = entityManager.createNativeQuery("SELECT distinct A.* FROM preciso_administracion_usuarios A, preciso_administracion_rol_vista B, preciso_administracion_vistas C, preciso_administracion_user_rol D WHERE A.usuario = D.usuario AND D.id_perfil = B.id_perfil AND B.id_vista = C.id_vista \n" +
                            "AND (C.nombre = 'Ver Creación de Cuentas (Gestión)' OR C.nombre = 'Ver Creación de Cuentas (Control Contable)' OR C.nombre = 'Ver Creación de Cuentas (Consolidación)')", User.class);
                    List<User>listEmails = emails.getResultList();
                    for (User u : listEmails)
                    {
                        sendEmailC(u.getCorreo(), resetPasswordLink);
                    }

                } else {
                    updateAll2.setParameter(3, "CREACION");
                    updateAll2.setParameter(1, user.getPrimerNombre());
                    updateAll2.setParameter(2, hoy);
                }
                updateAll2.setParameter(4, perfil);
                updateAll2.executeUpdate();
            }
            else
            {
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public String validateNext(String perfil)
    {
        String resp = "PENDING";
        if(perfil.equals("GENERAL"))
        {

            Query search2 = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_cuadro_mando_ifrs \n" +
                    "WHERE componente = ? AND semaforo_componente = 'FULL'");
            search2.setParameter(1,"STAGES");

            Query search3 = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_cuadro_mando_ifrs \n" +
                    "WHERE componente = ? AND semaforo_componente != 'FULL'");
            search3.setParameter(1,"CREACION");

            if(search2.getResultList().size()!=0)
            {
                resp="OK";
                if(search3.getResultList().size()==0)
                {
                    resp="DOWN";
                }
            }
            else if(search2.getResultList().size()==0)
            {
                resp="NEXT";
            }

        }
        else
        {

            Query search1 = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_cuadro_mando_ifrs \n" +
                    "WHERE componente = ? AND semaforo_componente = 'FULL'");
            search1.setParameter(1,"STAGES");

            Query search2 = entityManager.createNativeQuery("SELECT * FROM preciso_administracion_cuadro_mando_ifrs \n" +
                    "WHERE componente = ? AND input = ? AND semaforo_componente = 'FULL'");
            search2.setParameter(1,"CREACION");
            search2.setParameter(2,perfil);

            if(search1.getResultList().size()!=0)
            {
                if(search2.getResultList().size()!=0)
                {
                    resp="OK";
                }
                else if(search2.getResultList().size()==0)
                {
                    resp="NEXT";
                }
            }
        }
        return resp;
    }

    public void clearAccountCreation( String point)
    {
        if( point.equals("ALL")) {
            Query insertData = entityManager.createNativeQuery("DELETE FROM nexco_creacion_cuentas");
            insertData.executeUpdate();
        }
        else
        {
            if(point.equals("CUENTAS"))
            {
                Query insertData1 = entityManager.createNativeQuery("SELECT * INTO nexco_creacion_cuentas_sub FROM nexco_creacion_cuentas WHERE origen = ?");
                insertData1.setParameter(1,point);
                insertData1.executeUpdate();
            }
            Query insertData = entityManager.createNativeQuery("DELETE FROM nexco_creacion_cuentas WHERE origen = ?");
            insertData.setParameter(1,point);
            insertData.executeUpdate();
        }

        Query updateAll3 = entityManager.createNativeQuery("update preciso_administracion_cuadro_mando_ifrs \n" +
                "set semaforo_componente = 'PENDING' \n" +
                "where orden > 1");
        updateAll3.executeUpdate();
    }

    public void insertIntoAccountCreation(String point)
    {
        if(point.equals("RECHAZOS"))
        {
            for (int i =1;i<=3;i++)
            {
                Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_creacion_cuentas (empresa,numero_cuenta,cuenta4,subcuenta2,sub,seg,stag,tipocta,indic_li,clave_acceso,mon,ticenao,cenauo01,tictod1,tictod2,tictod3,ticenad,cod_repregul,responsable_control_operativo_nivel1,responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_de_gestion_nivel2,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2,responsable_control_de_gestion_cen_ges_res3,responsable_control_administrativo_nivel3,responsable_control_administrativo_cen_adm_res1,contrapartida_de_orden,contrapartida_de_resultados_dh,origen) \n" +
                        "SELECT distinct '0013'empresa, cu.corte,SUBSTRING(cu.corte,1,4),SUBSTRING(cu.corte,5,2),SUBSTRING(cu.corte,7,3),SUBSTRING(cu.corte,10,3),SUBSTRING(cu.corte,13,3),\n" +
                        "cu.tipo_cta,'I','B',CASE WHEN cu.divisa='COP' THEN '0' ELSE '1' END,'I','0460','O','S','C','T',SUBSTRING(cu.corte,1,6)+'001','01','00431','01584',\n" +
                        "'00460','00088','02','00431','01584','00088','03','00460',C.contrapartida,C.contrapartida, ? \n" +
                        "FROM (SELECT SUBSTRING(A.cuenta,0,B.stage_inicial)+'"+i+"'+SUBSTRING(A.cuenta,B.stage_inicial+B.stage_cantidad,LEN(A.cuenta)-B.stage_inicial+B.stage_cantidad) corte, A.* FROM (SELECT nrc.* FROM nexco_rechazos_cc AS nrc WHERE nrc.tipo_rechazo_real = 'Creación de cuenta')AS A\n" +
                        "LEFT JOIN nexco_identificacion_rechazos_p1 B ON SUBSTRING(A.cuenta,1,1) = B.inicial_cuenta) AS cu \n" +
                        "LEFT JOIN nexco_equivalencias_ifrs C ON SUBSTRING(cu.corte,1,4) = C.cuenta_contable WHERE cu.corte NOT IN (SELECT NUCTA FROM CUENTAS_PUC) AND cu.corte NOT IN (SELECT numero_cuenta FROM  nexco_creacion_cuentas)");
                insertData.setParameter(1,point);
                insertData.executeUpdate();
            }
        }
        else
        {
            for (int i =1;i<=3;i++)
            {
                Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_creacion_cuentas (empresa,numero_cuenta,cuenta4,subcuenta2,sub,seg,stag,tipocta,indic_li,clave_acceso,mon,ticenao,cenauo01,tictod1,tictod2,tictod3,ticenad,cod_repregul,responsable_control_operativo_nivel1,responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_de_gestion_nivel2,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2,responsable_control_de_gestion_cen_ges_res3,responsable_control_administrativo_nivel3,responsable_control_administrativo_cen_adm_res1,contrapartida_de_orden,contrapartida_de_resultados_dh,origen,nombre_cuenta,nombre_corto_cuenta ) \n" +
                        "SELECT distinct '0013'empresa, cu.corte,SUBSTRING(cu.corte,1,4),SUBSTRING(cu.corte,5,2),SUBSTRING(cu.corte,7,3),SUBSTRING(cu.corte,10,3),SUBSTRING(cu.corte,13,3),\n" +
                        "cu.tipocta,'I','B',CASE WHEN cu.mon='COP' THEN '0' ELSE '1' END,'I','0460','O','S','C','T',SUBSTRING(cu.corte,1,6)+'001','01','00431','01584',\n" +
                        "'00460','00088','02','00431','01584','00088','03','00460',C.contrapartida,C.contrapartida, ? ,cu.nombre_cuenta,cu.nombre_corto_cuenta \n" +
                        "FROM (SELECT SUBSTRING(A.numero_cuenta,0,B.stage_inicial)+'"+i+"'+SUBSTRING(A.numero_cuenta,B.stage_inicial+B.stage_cantidad,LEN(A.numero_cuenta)-B.stage_inicial+B.stage_cantidad) corte, A.* FROM nexco_creacion_cuentas_sub AS A\n" +
                        "LEFT JOIN nexco_identificacion_rechazos_p1 B ON SUBSTRING(A.numero_cuenta,1,1) = B.inicial_cuenta WHERE A.indic_li = 'I') AS cu \n" +
                        "LEFT JOIN nexco_equivalencias_ifrs C ON SUBSTRING(cu.corte,1,4) = C.cuenta_contable WHERE cu.corte NOT IN (SELECT NUCTA FROM CUENTAS_PUC) AND cu.corte NOT IN (SELECT numero_cuenta FROM  nexco_creacion_cuentas)");
                insertData.setParameter(1,point);
                insertData.executeUpdate();
            }

            Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_creacion_cuentas (empresa,numero_cuenta,cuenta4,subcuenta2,sub,seg,stag,tipocta,indic_li,clave_acceso,mon,origen,nombre_cuenta,nombre_corto_cuenta) \n" +
                    "SELECT distinct '0013'empresa, cu.corte,SUBSTRING(cu.corte,1,4),SUBSTRING(cu.corte,5,2),SUBSTRING(cu.corte,7,3),SUBSTRING(cu.corte,10,3),SUBSTRING(cu.corte,13,3),\n" +
                    "cu.tipocta,'L','B',CASE WHEN cu.mon='COP' THEN '0' ELSE '1' END, ? , cu.nombre_cuenta,cu.nombre_corto_cuenta \n" +
                    "FROM (SELECT A.numero_cuenta corte, A.* FROM nexco_creacion_cuentas_sub AS A WHERE A.indic_li = 'L') AS cu \n" +
                    "LEFT JOIN nexco_equivalencias_ifrs C ON SUBSTRING(cu.corte,1,4) = C.cuenta_contable WHERE cu.corte NOT IN (SELECT NUCTA FROM CUENTAS_PUC) AND cu.corte NOT IN (SELECT numero_cuenta FROM  nexco_creacion_cuentas)");
            insertData.setParameter(1,point);
            insertData.executeUpdate();

            Query insertData1 = entityManager.createNativeQuery("DROP TABLE nexco_creacion_cuentas_sub");
            insertData1.executeUpdate();
        }
    }

    public List<String> listAccountCreateNOTIN()
    {
        List<String> listFinal = new ArrayList<>();
        for (int i =1;i<=3;i++)
        {
            Query insertData = entityManager.createNativeQuery("SELECT cu.corte \n" +
                    "FROM (SELECT SUBSTRING(A.cuenta,0,B.stage_inicial)+'"+i+"'+SUBSTRING(A.cuenta,B.stage_inicial+B.stage_cantidad,LEN(A.cuenta)-B.stage_inicial+B.stage_cantidad) corte, A.* FROM (SELECT nrc.* FROM nexco_rechazos_cc AS nrc WHERE nrc.tipo_rechazo_real = 'Creación de cuenta')AS A\n" +
                    "LEFT JOIN nexco_identificacion_rechazos_p1 B ON SUBSTRING(A.cuenta,1,1) = B.inicial_cuenta) AS cu \n" +
                    "LEFT JOIN nexco_equivalencias_ifrs C ON SUBSTRING(cu.corte,1,4) = C.cuenta_contable " +
                    "INNER JOIN (SELECT NUCTA FROM CUENTAS_PUC GROUP BY NUCTA) D ON cu.cuenta = D.NUCTA " +
                    "--WHERE cu.corte NOT IN (SELECT NUCTA FROM CUENTAS_PUC GROUP BY NUCTA) GROUP BY cu.corte");
            listFinal.addAll(insertData.getResultList());
        }

        Query insertData1 = entityManager.createNativeQuery("SELECT A.numero_cuenta \n" +
                "FROM nexco_creacion_cuentas AS A WHERE A.numero_cuenta NOT IN (SELECT NUCTA FROM CUENTAS_PUC GROUP BY NUCTA) AND A.origen ='CUENTAS' GROUP BY A.numero_cuenta");
        listFinal.addAll(insertData1.getResultList());

        return listFinal;
    }

    public List<String> listAccountCreateIN()
    {
        List<String> listFinal = new ArrayList<>();
        for (int i =1;i<=3;i++)
        {
            Query insertData = entityManager.createNativeQuery("SELECT cu.corte \n" +
                    "FROM (SELECT SUBSTRING(A.cuenta,0,B.stage_inicial)+'"+i+"'+SUBSTRING(A.cuenta,B.stage_inicial+B.stage_cantidad,LEN(A.cuenta)-B.stage_inicial+B.stage_cantidad) corte, A.* FROM (SELECT nrc.* FROM nexco_rechazos_cc AS nrc WHERE nrc.tipo_rechazo_real = 'Creación de cuenta')AS A\n" +
                    "LEFT JOIN nexco_identificacion_rechazos_p1 B ON SUBSTRING(A.cuenta,1,1) = B.inicial_cuenta) AS cu \n" +
                    "LEFT JOIN nexco_equivalencias_ifrs C ON SUBSTRING(cu.corte,1,4) = C.cuenta_contable " +
                    "INNER JOIN (SELECT NUCTA FROM CUENTAS_PUC GROUP BY NUCTA) D ON cu.cuenta = D.NUCTA " +
                    "--WHERE cu.corte IN (SELECT NUCTA FROM CUENTAS_PUC GROUP BY NUCTA) GROUP BY cu.corte");
            listFinal.addAll(insertData.getResultList());
        }

        Query insertData1 = entityManager.createNativeQuery("SELECT A.numero_cuenta \n" +
                "FROM nexco_creacion_cuentas AS A WHERE A.numero_cuenta IN (SELECT NUCTA FROM CUENTAS_PUC GROUP BY NUCTA) AND A.origen ='CUENTAS' GROUP BY A.numero_cuenta");
        listFinal.addAll(insertData1.getResultList());

        return listFinal;
    }

    public boolean updateDataControl()
    {
        try {
            Query insertData = entityManager.createNativeQuery("UPDATE nexco_creacion_cuentas\n" +
                    "set codigo_de_control = t2.codigo_de_control, dias_de_plazo = t2.dias_de_plazo, indicador_de_la_cuenta = t2.indicador_de_la_cuenta, tipo_de_apunte = t2.tipo_de_apunte,inventariable = t2.inventariable\n" +
                    "from nexco_creacion_cuentas t1, nexco_control_contable_ifrs t2\n" +
                    "where SUBSTRING(t1.numero_cuenta,1,4) = t2.cuenta AND t1.indic_li = 'I'");
            insertData.executeUpdate();
            return true;
        }
        catch (Exception e)
        {
            return false;
        }
    }

    public List<AccountCreationPlane> findAllBusiness()
    {
        Query deleteData1 = entityManager.createNativeQuery("delete from nexco_creacion_plano");
        deleteData1.executeUpdate();

        Query insertData1 = entityManager.createNativeQuery("INSERT INTO nexco_creacion_plano\n" +
                "(cenaud01,cenaud02,cenaud03,cenaud04,cenaud05,cenaud06,cenaud07,cenaud08,cenaud09,cenauo01,cenauo02,cenauo03,cenauo04\n" +
                ",cenauo05,cenauo06,cenauo07,cenauo08,cenauo09,clave_acceso,cod_cdconmex,codigo_de_control,codigo_gestion,cod_repregul,consolid\n" +
                ",contrapartida_de_orden,contrapartida_de_resultados_dh,cuenta4,dias_de_plazo,empresa,epigrafe,ind_cuenta_inventariable,ind_cuenta_operacional\n" +
                ",indicador_de_la_cuenta,indicador_proceso_de_baja,indic_li,interfaz,inventariable,mon,nombre_corto_cuenta,nombre_cuenta,numero_cuenta,responsable_control_administrativo_cen_adm_res1\n" +
                ",responsable_control_administrativo_cen_adm_res2,responsable_control_administrativo_cen_adm_res3,responsable_control_administrativo_cen_adm_res4,responsable_control_administrativo_cen_adm_res5\n" +
                ",responsable_control_administrativo_cen_adm_res6,responsable_control_administrativo_nivel3,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2\n" +
                ",responsable_control_de_gestion_cen_ges_res3,responsable_control_de_gestion_cen_ges_res4,responsable_control_de_gestion_cen_ges_res5,responsable_control_de_gestion_cen_ges_res6,responsable_control_de_gestion_nivel2,\n" +
                "responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_operativo_cen_ope_res5\n" +
                ",responsable_control_operativo_cen_ope_res6,responsable_control_operativo_nivel1,seg,siginic,stag,sub,subcuenta2,ticenad,ticenao,tictod1,tictod2,tictod3,tictod4,tictod5,tictoo1,tictoo2\n" +
                ",tictoo3,tictoo4,tictoo5,tipapun,tipocta,tipo_de_apunte)" +
                "SELECT cenaud01,cenaud02,cenaud03,cenaud04,cenaud05,cenaud06,cenaud07,cenaud08,cenaud09,cenauo01,cenauo02,cenauo03,cenauo04\n" +
                ",cenauo05,cenauo06,cenauo07,cenauo08,cenauo09,clave_acceso,cod_cdconmex,codigo_de_control,codigo_gestion,cod_repregul,consolid\n" +
                ",contrapartida_de_orden,contrapartida_de_resultados_dh,cuenta4,dias_de_plazo,empresa,epigrafe,ind_cuenta_inventariable,ind_cuenta_operacional\n" +
                ",indicador_de_la_cuenta,indicador_proceso_de_baja,indic_li,interfaz,inventariable,mon,nombre_corto_cuenta,nombre_cuenta,numero_cuenta,responsable_control_administrativo_cen_adm_res1\n" +
                ",responsable_control_administrativo_cen_adm_res2,responsable_control_administrativo_cen_adm_res3,responsable_control_administrativo_cen_adm_res4,responsable_control_administrativo_cen_adm_res5\n" +
                ",responsable_control_administrativo_cen_adm_res6,responsable_control_administrativo_nivel3,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2\n" +
                ",responsable_control_de_gestion_cen_ges_res3,responsable_control_de_gestion_cen_ges_res4,responsable_control_de_gestion_cen_ges_res5,responsable_control_de_gestion_cen_ges_res6,responsable_control_de_gestion_nivel2,\n" +
                "responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_operativo_cen_ope_res5\n" +
                ",responsable_control_operativo_cen_ope_res6,responsable_control_operativo_nivel1,seg,siginic,stag,sub,subcuenta2,ticenad,ticenao,tictod1,tictod2,tictod3,tictod4,tictod5,tictoo1,tictoo2\n" +
                ",tictoo3,tictoo4,tictoo5,tipapun,tipocta,tipo_de_apunte " +
                "FROM nexco_creacion_cuentas WHERE empresa = '0013' AND numero_cuenta NOT IN (SELECT numero_cuenta FROM nexco_creacion_cuentas WHERE empresa = '0064')");
        insertData1.executeUpdate();

        Query insertData2 = entityManager.createNativeQuery("INSERT INTO nexco_creacion_plano\n" +
                "(cenaud01,cenaud02,cenaud03,cenaud04,cenaud05,cenaud06,cenaud07,cenaud08,cenaud09,cenauo01,cenauo02,cenauo03,cenauo04\n" +
                ",cenauo05,cenauo06,cenauo07,cenauo08,cenauo09,clave_acceso,cod_cdconmex,codigo_de_control,codigo_gestion,cod_repregul,consolid\n" +
                ",contrapartida_de_orden,contrapartida_de_resultados_dh,cuenta4,dias_de_plazo,empresa,epigrafe,ind_cuenta_inventariable,ind_cuenta_operacional\n" +
                ",indicador_de_la_cuenta,indicador_proceso_de_baja,indic_li,interfaz,inventariable,mon,nombre_corto_cuenta,nombre_cuenta,numero_cuenta,responsable_control_administrativo_cen_adm_res1\n" +
                ",responsable_control_administrativo_cen_adm_res2,responsable_control_administrativo_cen_adm_res3,responsable_control_administrativo_cen_adm_res4,responsable_control_administrativo_cen_adm_res5\n" +
                ",responsable_control_administrativo_cen_adm_res6,responsable_control_administrativo_nivel3,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2\n" +
                ",responsable_control_de_gestion_cen_ges_res3,responsable_control_de_gestion_cen_ges_res4,responsable_control_de_gestion_cen_ges_res5,responsable_control_de_gestion_cen_ges_res6,responsable_control_de_gestion_nivel2,\n" +
                "responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_operativo_cen_ope_res5\n" +
                ",responsable_control_operativo_cen_ope_res6,responsable_control_operativo_nivel1,seg,siginic,stag,sub,subcuenta2,ticenad,ticenao,tictod1,tictod2,tictod3,tictod4,tictod5,tictoo1,tictoo2\n" +
                ",tictoo3,tictoo4,tictoo5,tipapun,tipocta,tipo_de_apunte)" +
                "SELECT cenaud01,cenaud02,cenaud03,cenaud04,cenaud05,cenaud06,cenaud07,cenaud08,cenaud09,cenauo01,cenauo02,cenauo03,cenauo04\n" +
                ",cenauo05,cenauo06,cenauo07,cenauo08,cenauo09,clave_acceso,cod_cdconmex,codigo_de_control,codigo_gestion,cod_repregul,consolid\n" +
                ",contrapartida_de_orden,contrapartida_de_resultados_dh,cuenta4,dias_de_plazo,empresa,epigrafe,ind_cuenta_inventariable,ind_cuenta_operacional\n" +
                ",indicador_de_la_cuenta,indicador_proceso_de_baja,indic_li,interfaz,inventariable,mon,nombre_corto_cuenta,nombre_cuenta,numero_cuenta,responsable_control_administrativo_cen_adm_res1\n" +
                ",responsable_control_administrativo_cen_adm_res2,responsable_control_administrativo_cen_adm_res3,responsable_control_administrativo_cen_adm_res4,responsable_control_administrativo_cen_adm_res5\n" +
                ",responsable_control_administrativo_cen_adm_res6,responsable_control_administrativo_nivel3,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2\n" +
                ",responsable_control_de_gestion_cen_ges_res3,responsable_control_de_gestion_cen_ges_res4,responsable_control_de_gestion_cen_ges_res5,responsable_control_de_gestion_cen_ges_res6,responsable_control_de_gestion_nivel2,\n" +
                "responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_operativo_cen_ope_res5\n" +
                ",responsable_control_operativo_cen_ope_res6,responsable_control_operativo_nivel1,seg,siginic,stag,sub,subcuenta2,ticenad,ticenao,tictod1,tictod2,tictod3,tictod4,tictod5,tictoo1,tictoo2\n" +
                ",tictoo3,tictoo4,tictoo5,tipapun,tipocta,tipo_de_apunte " +
                "FROM nexco_creacion_cuentas WHERE empresa = '0064' AND numero_cuenta NOT IN (SELECT numero_cuenta FROM nexco_creacion_cuentas WHERE empresa = '0013')");
        insertData2.executeUpdate();

        Query updateData1 = entityManager.createNativeQuery("UPDATE nexco_creacion_plano SET empresa = '0064-' WHERE empresa = '0013'");
        updateData1.executeUpdate();

        Query updateData2 = entityManager.createNativeQuery("UPDATE nexco_creacion_plano SET empresa = '0013' WHERE empresa = '0064'");
        updateData2.executeUpdate();

        Query updateData3 = entityManager.createNativeQuery("UPDATE nexco_creacion_plano SET empresa = '0064' WHERE empresa = '0064-'");
        updateData3.executeUpdate();

        Query insertData = entityManager.createNativeQuery("INSERT INTO nexco_creacion_plano\n" +
                "(cenaud01,cenaud02,cenaud03,cenaud04,cenaud05,cenaud06,cenaud07,cenaud08,cenaud09,cenauo01,cenauo02,cenauo03,cenauo04\n" +
                ",cenauo05,cenauo06,cenauo07,cenauo08,cenauo09,clave_acceso,cod_cdconmex,codigo_de_control,codigo_gestion,cod_repregul,consolid\n" +
                ",contrapartida_de_orden,contrapartida_de_resultados_dh,cuenta4,dias_de_plazo,empresa,epigrafe,ind_cuenta_inventariable,ind_cuenta_operacional\n" +
                ",indicador_de_la_cuenta,indicador_proceso_de_baja,indic_li,interfaz,inventariable,mon,nombre_corto_cuenta,nombre_cuenta,numero_cuenta,responsable_control_administrativo_cen_adm_res1\n" +
                ",responsable_control_administrativo_cen_adm_res2,responsable_control_administrativo_cen_adm_res3,responsable_control_administrativo_cen_adm_res4,responsable_control_administrativo_cen_adm_res5\n" +
                ",responsable_control_administrativo_cen_adm_res6,responsable_control_administrativo_nivel3,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2\n" +
                ",responsable_control_de_gestion_cen_ges_res3,responsable_control_de_gestion_cen_ges_res4,responsable_control_de_gestion_cen_ges_res5,responsable_control_de_gestion_cen_ges_res6,responsable_control_de_gestion_nivel2,\n" +
                "responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_operativo_cen_ope_res5\n" +
                ",responsable_control_operativo_cen_ope_res6,responsable_control_operativo_nivel1,seg,siginic,stag,sub,subcuenta2,ticenad,ticenao,tictod1,tictod2,tictod3,tictod4,tictod5,tictoo1,tictoo2\n" +
                ",tictoo3,tictoo4,tictoo5,tipapun,tipocta,tipo_de_apunte)" +
                "SELECT cenaud01,cenaud02,cenaud03,cenaud04,cenaud05,cenaud06,cenaud07,cenaud08,cenaud09,cenauo01,cenauo02,cenauo03,cenauo04\n" +
                ",cenauo05,cenauo06,cenauo07,cenauo08,cenauo09,clave_acceso,cod_cdconmex,codigo_de_control,codigo_gestion,cod_repregul,consolid\n" +
                ",contrapartida_de_orden,contrapartida_de_resultados_dh,cuenta4,dias_de_plazo,empresa,epigrafe,ind_cuenta_inventariable,ind_cuenta_operacional\n" +
                ",indicador_de_la_cuenta,indicador_proceso_de_baja,indic_li,interfaz,inventariable,mon,nombre_corto_cuenta,nombre_cuenta,numero_cuenta,responsable_control_administrativo_cen_adm_res1\n" +
                ",responsable_control_administrativo_cen_adm_res2,responsable_control_administrativo_cen_adm_res3,responsable_control_administrativo_cen_adm_res4,responsable_control_administrativo_cen_adm_res5\n" +
                ",responsable_control_administrativo_cen_adm_res6,responsable_control_administrativo_nivel3,responsable_control_de_gestion_cen_ges_res1,responsable_control_de_gestion_cen_ges_res2\n" +
                ",responsable_control_de_gestion_cen_ges_res3,responsable_control_de_gestion_cen_ges_res4,responsable_control_de_gestion_cen_ges_res5,responsable_control_de_gestion_cen_ges_res6,responsable_control_de_gestion_nivel2,\n" +
                "responsable_control_operativo_cen_ope_res1,responsable_control_operativo_cen_ope_res2,responsable_control_operativo_cen_ope_res3,responsable_control_operativo_cen_ope_res4,responsable_control_operativo_cen_ope_res5\n" +
                ",responsable_control_operativo_cen_ope_res6,responsable_control_operativo_nivel1,seg,siginic,stag,sub,subcuenta2,ticenad,ticenao,tictod1,tictod2,tictod3,tictod4,tictod5,tictoo1,tictoo2\n" +
                ",tictoo3,tictoo4,tictoo5,tipapun,tipocta,tipo_de_apunte " +
                "FROM nexco_creacion_cuentas");
        insertData.executeUpdate();

        Query list = entityManager.createNativeQuery("SELECT * FROM nexco_creacion_plano order by empresa,numero_cuenta", AccountCreationPlane.class);
        return list.getResultList();
    }

    public List<AccountCreation> findAll(){
        return accountCreationRepository.findAll();
    }

    public List<Object[]> findAllResumeView( String perfil){
        List<Object[]> list = new ArrayList<Object[]>();
        if(perfil.equals("GENERAL"))
        {
            Query verify = entityManager.createNativeQuery("SELECT empresa,numero_cuenta,nombre_corto_cuenta,tipocta,indic_li,mon,codigo_gestion,epigrafe,consolid,codigo_de_control,indicador_de_la_cuenta,tipo_de_apunte FROM nexco_creacion_cuentas ORDER BY empresa,numero_cuenta");
            list = verify.getResultList();
        }
        else if(perfil.equals("GESTION"))
        {
            Query verify = entityManager.createNativeQuery("SELECT empresa,numero_cuenta,nombre_corto_cuenta,tipocta,indic_li,mon,codigo_gestion,epigrafe FROM nexco_creacion_cuentas WHERE indic_li = 'I' ORDER BY empresa,numero_cuenta" +
                    "--WHERE codigo_gestion IS NULL OR codigo_gestion = '' OR epigrafe IS NULL OR epigrafe = '' ");
            list = verify.getResultList();
        }
        else if(perfil.equals("CONSOLIDACION")) {
            Query verify = entityManager.createNativeQuery("SELECT empresa,numero_cuenta,nombre_corto_cuenta,tipocta,indic_li,mon,consolid FROM nexco_creacion_cuentas WHERE indic_li = 'I' ORDER BY empresa,numero_cuenta" +
                    "--WHERE consolid IS NULL OR consolid = '' ");
            list = verify.getResultList();
        }
        else if(perfil.equals("CONTROL CONTABLE")) {
            Query verify = entityManager.createNativeQuery("SELECT empresa,numero_cuenta,nombre_corto_cuenta,tipocta,indic_li,mon,codigo_de_control,dias_de_plazo,indicador_de_la_cuenta,tipo_de_apunte,inventariable FROM nexco_creacion_cuentas WHERE indic_li = 'I' ORDER BY empresa,numero_cuenta" +
                    "--WHERE codigo_de_control IS NULL OR codigo_de_control = '' OR dias_de_plazo IS NULL OR dias_de_plazo = '' OR indicador_de_la_cuenta IS NULL OR indicador_de_la_cuenta = '' OR tipo_de_apunte IS NULL OR tipo_de_apunte = '' OR inventariable IS NULL OR inventariable = '' ");
            list = verify.getResultList();
        }
        return list;
    }

    public List<AccountCreation> getAccount(String empresa, String cuenta) {

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_creacion_cuentas WHERE empresa = ? AND numero_cuenta = ?", AccountCreation.class);
        verify.setParameter(1, empresa);
        verify.setParameter(2, cuenta);

        return verify.getResultList();
    }

    public void removeData() {

        Query verify = entityManager.createNativeQuery("DELETE FROM nexco_creacion_cuentas");
        verify.executeUpdate();
    }

    public AccountCreation saveAccountCreation(AccountCreation garantBank){
        return accountCreationRepository.save(garantBank);
    }


    public Page<AccountCreation> getAll(Pageable pageable){
        return accountCreationRepository.findAll(pageable);
    }

    public List<AccountCreation> findByFilter(String value, String filter) {
        List<AccountCreation> list=new ArrayList<AccountCreation>();
        switch (filter)
        {
            case "NIT":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.nit LIKE ?", AccountCreation.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Nombre Banco Real":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.nombre_banco_real LIKE ?", AccountCreation.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Nombre Similar":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.nombre_similar LIKE ?", AccountCreation.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "País":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_banco_garante as em " +
                        "WHERE em.pais LIKE ?", AccountCreation.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

    public void sendEmailC(String recipientEmail, String link) {
        String subject = "Nexco - Habilitación Creación de cuentas";

        String content = "<p>Se ha habilitado el proceso de creación de cuentas.</p>"
                + "<p>Por favor siga los siguientes pasos:</p>"
                + "<p>1) Descargue la precarga.</p>"
                + "<p>2) Diligencie la información respectiva en su totalidad en la plantilla formato Excel</p>"
                + "<p>3) Realice su validación y confirme con el bóton.</p>"

                + "<p>Ingrese al siguiente enlace</p>"
                + "<p><a href=\"" + link + "\">Click Aquí</a></p>"
                + "<br>"
                + "<p>Ignore este correo si ya ha realizado el proceso.</p>";

        sendEmailService.sendEmail(recipientEmail, subject, content);
    }

}
