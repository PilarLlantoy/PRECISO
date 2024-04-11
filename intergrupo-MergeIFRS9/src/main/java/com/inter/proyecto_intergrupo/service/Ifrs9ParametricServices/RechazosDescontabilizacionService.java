package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RechazosDescontabilizacion;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RechazosDescontabilizacionPreCarga;
import com.inter.proyecto_intergrupo.model.ifrs9.FirstAdjustment;
import com.inter.proyecto_intergrupo.model.ifrs9.RechazosDesconAutoTemp;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RechazosDesconAutoTempRepository;
import org.apache.commons.collections.ArrayStack;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xerces.impl.io.ASCIIReader;
import org.bouncycastle.crypto.modes.gcm.GCMExponentiator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.*;
import java.text.ParseException;
import java.util.*;

@Service
@Transactional
public class RechazosDescontabilizacionService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private RechazosDesconAutoTempRepository rechazosDesconAutoTempRepository;

    public boolean cargarRechazosDescontabilizacionPre(String periodo, String fuente,InputStream fileContent, String ruta) {
        try {
            String nameDirectory="\\\\co.igrupobbva\\svrfilesystem\\TX\\ENVIO_HOST\\XC\\CONSOLIDACION\\";
            if(!ruta.equals("NO APLICA"))
            {
                System.out.println(ruta);
                nameDirectory=ruta;
            }
            /*if(fuente.equals("FILE"))
            {
                nameDirectory="\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\PLAN_00\\";
            }*/
            //File f = new File("\\\\co.igrupobbva\\svrfilesystem\\TX\\Recepcion_host\\Financiera\\PLAN_00\\RECHAZOS_DESCON_PROV_PLAN00.TXT");
            File f = new File(nameDirectory+"RECHAZOS_DESCON_PROV_PLAN00.TXT");
            /*if(fuente.equals("FILE")) {
                try {
                    FileOutputStream fileI = new FileOutputStream(f, false);
                    int read;
                    byte[] bytes = new byte[ASCIIReader.DEFAULT_BUFFER_SIZE];
                    while ((read = fileContent.read(bytes)) != -1) {
                        fileI.write(bytes, 0, read);
                    }
                    fileI.close();
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }*/

            if(!f.exists() || f.isDirectory()) {
                return false;
            }

            Query query0 = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_descontabilizacion_pre WHERE periodo = ?");
            query0.setParameter(1,periodo);
            query0.executeUpdate();

            Query query1 = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_rechazos_descon_auto_temp ");
            query1.executeUpdate();

            Query query2 = entityManager.createNativeQuery("CREATE TABLE nexco_rechazos_descon_auto_temp( \n" +
                    " \n" +
                    "REC_COD_PROCESO varchar(255), \n" +
                    "REC_COD_CCONTR varchar(255), \n" +
                    "REC_COD_CTACONT varchar(255), \n" +
                    "REC_COD_SALDO varchar(255), \n" +
                    "REC_IND_STAGE_FINAL varchar(255), \n" +
                    "REC_COD_SEGM_FINREP varchar(255), \n" +
                    "REC_RECHAZOS varchar(255))");
            query2.executeUpdate();

            Query query1D = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_rechazos_descon_auto_temp_compl ");
            query1D.executeUpdate();

            Query query2D = entityManager.createNativeQuery("CREATE TABLE nexco_rechazos_descon_auto_temp_compl( \n" +
                    " \n" +
                    "REC_COD_PROCESO varchar(255), \n" +
                    "REC_COD_CCONTR varchar(255), \n" +
                    "REC_COD_CTACONT varchar(255), \n" +
                    "REC_COD_SALDO varchar(255), \n" +
                    "REC_IND_STAGE_FINAL varchar(255), \n" +
                    "REC_COD_SEGM_FINREP varchar(255), \n" +
                    "REC_RECHAZOS varchar(255))");
            query2D.executeUpdate();

            /*if(fuente.equals("FILE"))
            {
                loadFileDatabase(fileContent);
            }
            else
            {*/
                Query query3 = entityManager.createNativeQuery("BULK INSERT nexco_rechazos_descon_auto_temp \n" +
                        "FROM '"+nameDirectory+"RECHAZOS_DESCON_PROV_PLAN00.TXT' WITH (FIELDTERMINATOR= ';')");
                query3.executeUpdate();

                Query query3D = entityManager.createNativeQuery("BULK INSERT nexco_rechazos_descon_auto_temp_compl \n" +
                        "FROM '"+nameDirectory+"RECHAZOS_DESCON_PROV_PLAN00.TXT' WITH (FIELDTERMINATOR= ';')");
                query3D.executeUpdate();
            //}


            Query query3Du = entityManager.createNativeQuery("UPDATE nexco_rechazos_descon_auto_temp_compl\n" +
                    "SET REC_COD_CTACONT = TRIM(REC_COD_CTACONT)\n" +
                    "\n" +
                    "UPDATE nexco_rechazos_descon_auto_temp_compl\n" +
                    "SET REC_COD_SALDO = CASE WHEN TRIM(REC_COD_SALDO) LIKE '%-%' THEN '-'+TRIM(REPLACE(REPLACE(REC_COD_SALDO,'-',''),',','')) ELSE REPLACE(TRIM(REC_COD_SALDO),',','') END \n" +
                    "\n" +
                    "UPDATE nexco_rechazos_descon_auto_temp_compl\n" +
                    "SET REC_COD_CCONTR = TRIM(REC_COD_CCONTR)");
            query3Du.executeUpdate();

            Query query4 = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_descon_auto_temp\n" +
                    "WHERE TRIM(REC_COD_CTACONT) = '' ");
            query4.executeUpdate();

            Query query5 = entityManager.createNativeQuery("UPDATE nexco_rechazos_descon_auto_temp\n" +
                    "SET REC_RECHAZOS = REC_COD_SEGM_FINREP,\n" +
                    "REC_COD_SEGM_FINREP = ''\n" +
                    "WHERE REC_RECHAZOS IS NULL\n" +
                    "\n" +
                    "UPDATE nexco_rechazos_descon_auto_temp\n" +
                    "SET REC_COD_CTACONT = TRIM(REC_COD_CTACONT)\n" +
                    "\n" +
                    "UPDATE nexco_rechazos_descon_auto_temp\n" +
                    "SET REC_COD_SALDO = CASE WHEN TRIM(REC_COD_SALDO) LIKE '%-%' THEN '-'+TRIM(REPLACE(REPLACE(REC_COD_SALDO,'-',''),',','')) ELSE REPLACE(TRIM(REC_COD_SALDO),',','') END \n" +
                    "\n" +
                    "UPDATE nexco_rechazos_descon_auto_temp\n" +
                    "SET REC_COD_CCONTR = TRIM(REC_COD_CCONTR)");
            query5.executeUpdate();

            Query query6 = entityManager.createNativeQuery("INSERT INTO nexco_rechazos_descontabilizacion_pre(cuenta,centro,divisa,contrato,saldo,periodo,filtro,descripcion)\n" +
                    "SELECT rec.REC_COD_CTACONT , '', 'COP', rec.REC_COD_CCONTR, convert(numeric(18,2),rec.REC_COD_SALDO), ?,\n" +
                    "CASE WHEN rec.REC_RECHAZOS LIKE 'CUENTA NO HOMOLOGADA%' THEN 1 \n" +
                    "WHEN REC_RECHAZOS LIKE 'CONTRATO EXISTE EN EL VERTICAL PERO NO EN LA CONCILIACION' THEN 2\n" +
                    "ELSE 3 END, rec.REC_RECHAZOS\n" +
                    "FROM nexco_rechazos_descon_auto_temp rec \n"/* +
                    "(SELECT DISTINCT Contrato2 as contrato, Oficina as oficina FROM [82.255.50.134].DB_FINAN_NUEVA.DBO.PERSONAS2 WHERE Estado <> 'B' AND Estado <> 'C') as per\n" +
                    "LEFT JOIN nexco_rechazos_descon_auto_temp as rec ON rec.REC_COD_CCONTR = per.Contrato COLLATE Modern_Spanish_CI_AS"*/);
            query6.setParameter(1,periodo);
            query6.executeUpdate();

            Query createTmpCentros = entityManager.createNativeQuery("SELECT distinct centro, contrato INTO nexco_temp_h141_p FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.cont_h141mes_"+periodo.replace("-","_")+" where contrato in (SELECT distinct Z.contrato COLLATE Modern_Spanish_CI_AS FROM nexco_rechazos_descontabilizacion_pre Z where Z.periodo = ? and Z.cuenta in (SELECT Y.NUCTA FROM nexco_provisiones X\n" +
                    "INNER JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') Y ON X.cuenta_neocon = Y.CODICONS46))");
            createTmpCentros.setParameter(1,periodo);
            createTmpCentros.executeUpdate();

            Query updateCentros = entityManager.createNativeQuery("update A\n" +
                    "SET A.centro = B.centro\n" +
                    "FROM (SELECT Z.centro,Z.contrato FROM nexco_rechazos_descontabilizacion_pre Z where Z.periodo = ? and Z.cuenta in (SELECT distinct Y.NUCTA FROM nexco_provisiones X\n" +
                    "INNER JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA = '0013') Y ON X.cuenta_neocon = Y.CODICONS46)) A\n" +
                    "INNER JOIN nexco_temp_h141_p B ON A.contrato COLLATE Modern_Spanish_CI_AS = B.contrato and A.centro COLLATE Modern_Spanish_CI_AS <> B.centro");
            updateCentros.setParameter(1,periodo);
            updateCentros.executeUpdate();

            Query deletempCentros = entityManager.createNativeQuery("drop table nexco_temp_h141_p");
            deletempCentros.executeUpdate();

            return true;

        } catch (NoResultException e) {

            return false;
        }
    }

    public void loadFileDatabase(InputStream file) {
        try {
            XSSFRow row;
            Scanner scan = new Scanner(file);
            List<RechazosDesconAutoTemp> tempListAdd = new ArrayList<RechazosDesconAutoTemp>();

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                String[] partLines = line.split(";");
                RechazosDesconAutoTemp insert = new RechazosDesconAutoTemp();
                insert.setREC_COD_PROCESO(partLines[0]);
                insert.setREC_COD_CCONTR(partLines[1]);
                insert.setREC_COD_CTACONT(partLines[2]);
                insert.setREC_COD_SALDO(partLines[3]);

                if(partLines.length==7)
                {
                    insert.setREC_IND_STAGE_FINAL(partLines[4]);
                    insert.setREC_COD_SEGM_FINREP(partLines[5]);
                    insert.setREC_RECHAZOS(partLines[6]);
                }
                else
                {
                    insert.setREC_IND_STAGE_FINAL("");
                    insert.setREC_COD_SEGM_FINREP(partLines[4]);
                    insert.setREC_RECHAZOS(partLines[5]);
                }
                if(partLines[2].trim().length()>0)
                    tempListAdd.add(insert);
            }

            rechazosDesconAutoTempRepository.saveAll(tempListAdd);

            Query getQuery1 = entityManager.createNativeQuery("INSERT INTO nexco_rechazos_descon_auto_temp_compl( REC_COD_PROCESO,REC_COD_CCONTR,REC_COD_CTACONT,REC_COD_SALDO,REC_IND_STAGE_FINAL,REC_COD_SEGM_FINREP,REC_RECHAZOS) \n" +
                    "select REC_COD_PROCESO,REC_COD_CCONTR,REC_COD_CTACONT,REC_COD_SALDO,REC_IND_STAGE_FINAL,REC_COD_SEGM_FINREP,REC_RECHAZOS from nexco_rechazos_descon_auto_temp");
            getQuery1.executeUpdate();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public List<Object[]> getRechazosDescontabilizacionPre(String periodo, String filtro){
        List<Object[]> toReturn = new ArrayList<>();
        if(filtro.equals("%")){
            Query getQuery = entityManager.createNativeQuery("select REC_COD_PROCESO,'' CENTRO,REC_COD_CCONTR,REC_COD_CTACONT,'' DIVISA,'' A,''B,REC_COD_SALDO,CASE WHEN REC_RECHAZOS IS NULL THEN REC_COD_SEGM_FINREP ELSE REC_RECHAZOS END from nexco_rechazos_descon_auto_temp_compl ORDER BY REC_COD_CTACONT desc");
            //getQuery.setParameter(1,periodo);
            //getQuery.setParameter(2,filtro);
            if(!getQuery.getResultList().isEmpty()){
                toReturn = getQuery.getResultList();
            }
        }
        else if(filtro.equals("2")){
            Query getQuery = entityManager.createNativeQuery("select * from nexco_rechazos_descontabilizacion_pre where periodo = ? and filtro <> 1");
            getQuery.setParameter(1,periodo);
            if(!getQuery.getResultList().isEmpty()){
                toReturn = getQuery.getResultList();
            }
        }else{
            Query getQuery = entityManager.createNativeQuery("select * from nexco_rechazos_descontabilizacion_pre where periodo = ?");
            getQuery.setParameter(1,periodo);
            //getQuery.setParameter(2,filtro);
            if(!getQuery.getResultList().isEmpty()){
                toReturn = getQuery.getResultList();
            }
        }
        return toReturn;
    }

    public List<Object[]> getRechazosDescontabilizacion(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_rechazos_descontabilizacion where periodo = ?");
        getQuery.setParameter(1,periodo);
        return getQuery.getResultList();
    }

    public List<Object[]> getRechazosDescontabilizacionCV(String periodo){
        Query getQuery = entityManager.createNativeQuery("select a.cuenta, a.diferencias, b.REC_COD_PROCESO, b.REC_COD_CCONTR, b.REC_COD_CTACONT,b.REC_COD_SALDO,CASE WHEN b.REC_RECHAZOS IS NULL THEN b.REC_COD_SEGM_FINREP ELSE b.REC_RECHAZOS END as SEGMENTO\n" +
                "from (SELECT * FROM nexco_conciliacion_scope_ifrs9 WHERE periodo = ? and SUBSTRING(descripcion,1,1) = '2' and SUBSTRING(descripcion,len(descripcion)-2,2) = 'RI' and validacion = 'REVISAR') a\n" +
                "left join (select * from nexco_rechazos_descon_auto_temp_compl) b on a.cuenta = b.REC_COD_CTACONT");
        getQuery.setParameter(1,periodo);
        return getQuery.getResultList();
    }

    public void clearRechazosDescontabilizacion(User user, String periodo){
        Query query1 = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_descontabilizacion_pre where periodo = ?", RechazosDescontabilizacionPreCarga.class);
        query1.setParameter(1,periodo);
        query1.executeUpdate();

        Query query2 = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_descontabilizacion where periodo = ?", RechazosDescontabilizacion.class);
        query2.setParameter(1,periodo);
        query2.executeUpdate();
    }

    public void clearRechazosDescontabilizacionPre(User user, String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_rechazos_descontabilizacion where periodo = ?", RechazosDescontabilizacion.class);
        query.setParameter(1,periodo);
        query.executeUpdate();
    }

    private void insertarRegistro(RechazosDescontabilizacion rechazosDescontabilizacion){
        Query query1 = entityManager.createNativeQuery("INSERT INTO nexco_rechazos_descontabilizacion(cuenta,centro,divisa,contrato,saldo,periodo,descripcion) VALUES(?,?,?,?,?,?,?)", RechazosDescontabilizacion.class);
        query1.setParameter(1,rechazosDescontabilizacion.getCuenta());
        query1.setParameter(2,rechazosDescontabilizacion.getCentro());
        query1.setParameter(3,rechazosDescontabilizacion.getDivisa());
        query1.setParameter(4,rechazosDescontabilizacion.getContrato());
        query1.setParameter(5,rechazosDescontabilizacion.getSaldo());
        query1.setParameter(6,rechazosDescontabilizacion.getPeriodo());
        query1.setParameter(7,rechazosDescontabilizacion.getDescripcion());
        query1.executeUpdate();
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user, String period) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            clearRechazosDescontabilizacionPre(user,period);

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1,period);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Rechazos Descontabilización");
                insert.setCentro(user.getCentro());
                insert.setComponente("IFRS9");
                insert.setFecha(today);
                insert.setInput("Rechazos Descontabilización");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Rechazos Descontabilización");
                insert.setCentro(user.getCentro());
                insert.setComponente("IFRS9");
                insert.setFecha(today);
                insert.setInput("Rechazos Descontabilización");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow == 2) {
                DataFormatter formatter = new DataFormatter();
                String cellCuenta= formatter.formatCellValue(row.getCell(0));
                String cellCentro = formatter.formatCellValue(row.getCell(1));
                String cellDivisa= formatter.formatCellValue(row.getCell(2));
                String cellContrato = formatter.formatCellValue(row.getCell(3));
                String cellSaldo= formatter.formatCellValue(row.getCell(4));
                String cellPeriodo= formatter.formatCellValue(row.getCell(5));
                //String cellSeleccionar = formatter.formatCellValue(row.getCell(6));
                String cellDescripcion = formatter.formatCellValue(row.getCell(6));
                log[0] = String.valueOf(row.getRowNum());
                if((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellCentro.isEmpty() || cellCentro.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank()) && (cellContrato.isEmpty() || cellContrato.isBlank())
                        && (cellSaldo.isEmpty() || cellSaldo.isBlank()) && (cellPeriodo.isEmpty() || cellPeriodo.isBlank())) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank() || cellCuenta.length() > 50) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellCentro.isEmpty() || cellCentro.isBlank() || cellCentro.length() > 50) {
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellDivisa.isEmpty() || cellDivisa.isBlank() || cellDivisa.length() > 50) {
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellContrato.isEmpty() || cellContrato.isBlank() || cellContrato.length() > 50) {
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else if (cellSaldo.isEmpty() || cellSaldo.isBlank() || cellSaldo.length() > 50) {
                    log[1] = "5";
                    log[2] = "false";
                    break;
                } else if (cellPeriodo.isEmpty() || cellPeriodo.isBlank() || cellPeriodo.length() > 50) {
                    log[1] = "6";
                    log[2] = "false";
                    break;
                }
                else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        Long contrato = Long.parseLong(cellContrato);log[1]="1";
                        log[1] = "1";
                        log[2] = "true";
                    } catch (Exception e) {
                        log[2] = "falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            } else {
                firstRow ++;
            }
        }
        lista.add(log);
        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows, String period) {
        XSSFRow row;
        ArrayList lista= new ArrayList();
        int firstRow = 1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter();
                String cellCuenta= formatter.formatCellValue(row.getCell(0));
                String cellCentro = formatter.formatCellValue(row.getCell(1));
                String cellDivisa= formatter.formatCellValue(row.getCell(2));
                String cellContrato = formatter.formatCellValue(row.getCell(3));
                String cellSaldo= formatter.formatCellValue(row.getCell(4)).replace(".","").replace(",",".");
                String cellPeriodo= formatter.formatCellValue(row.getCell(5));
                String cellDescripcion= formatter.formatCellValue(row.getCell(6));

                if((cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellCentro.isEmpty() || cellCentro.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank()) && (cellContrato.isEmpty() || cellContrato.isBlank())
                        && (cellSaldo.isEmpty() || cellSaldo.isBlank()) && (cellPeriodo.isEmpty() || cellPeriodo.isBlank()))
                {
                    break;
                } else {
                    RechazosDescontabilizacion rechazosDescontabilizacion = new RechazosDescontabilizacion();
                    rechazosDescontabilizacion.setCuenta(cellCuenta);
                    rechazosDescontabilizacion.setCentro(cellCentro);
                    rechazosDescontabilizacion.setDivisa(cellDivisa);
                    rechazosDescontabilizacion.setContrato(cellContrato);
                    rechazosDescontabilizacion.setSaldo(Double.parseDouble(cellSaldo));
                    rechazosDescontabilizacion.setPeriodo(cellPeriodo);
                    rechazosDescontabilizacion.setDescripcion(cellDescripcion);
                    insertarRegistro(rechazosDescontabilizacion);
                    log[0] = cellCuenta;
                    log[1] = "Registro actualizado exitosamente.";
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    
}
