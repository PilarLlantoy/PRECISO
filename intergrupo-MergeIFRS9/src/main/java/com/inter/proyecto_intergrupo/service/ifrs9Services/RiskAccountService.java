package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccount;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccountFinal;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.temporal.RiskAccountTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RiskAccountRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RiskAccountRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SegmentDecisionTreeRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.repository.temporal.RiskAccountTemporalRepository;
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
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class RiskAccountService {

    @Autowired
    private RiskAccountRepository riskAccountRepository;

    @Autowired
    private RiskAccountTemporalRepository riskAccountTemporalRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SegmentDecisionTreeRepository segmentDecisionTreeRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @PersistenceContext
    EntityManager entityManager;

    public RiskAccountService(RiskAccountRepository riskAccountRepository, AuditRepository auditRepository) {
        this.riskAccountRepository = riskAccountRepository;
        this.auditRepository = auditRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file,User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows,user,periodo);
            String[] temporal= list.get(0);
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user,String periodo) {
        ArrayList lista= new ArrayList();
        List<RiskAccountTemporal> listAdd= new ArrayList<RiskAccountTemporal>();
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
                String cellContrato = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodigoFamiliaInicial = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCodigoFamiliaFinal = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCodigoCliente = formatter.formatCellValue(row.getCell(3)).trim();
                String cellStageInicial = formatter.formatCellValue(row.getCell(4)).trim();
                String cellStageFinal = formatter.formatCellValue(row.getCell(5)).trim();
                String cellNumeroCaso = formatter.formatCellValue(row.getCell(13)).trim();
                String cellFamilia = formatter.formatCellValue(row.getCell(16)).trim();

                String cellEADInicial = "";
                String cellEADFinal = "";
                String cellEADY01Inicial = "";
                String cellEADY01Final = "";
                String cellImporteInicial = "";
                String cellImporteFinal = "";
                String cellValorAjusteProvision = "";
                String cellIMPSDFUBA = "";
                String cellIMPRACREG = "";

                log[0]=String.valueOf(row.getRowNum()+1);
                try
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    XSSFCell cell6= row.getCell(6);
                    cell6.setCellType(CellType.STRING);
                    cellEADInicial = formatter.formatCellValue(cell6).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    XSSFCell cell7= row.getCell(7);
                    cell7.setCellType(CellType.STRING);
                    cellEADFinal = formatter.formatCellValue(cell7).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(9)+" - (10)";
                    XSSFCell cell9= row.getCell(9);
                    cell9.setCellType(CellType.STRING);
                    cellEADY01Final = formatter.formatCellValue(cell9).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(10)+" - 11)";
                    XSSFCell cell10= row.getCell(10);
                    cell10.setCellType(CellType.STRING);
                    cellImporteInicial = formatter.formatCellValue(cell10).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(11)+" - 12)";
                    XSSFCell cell11= row.getCell(11);
                    cell11.setCellType(CellType.STRING);
                    cellImporteFinal = formatter.formatCellValue(cell11).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(12)+" - 13)";
                    XSSFCell cell12= row.getCell(12);
                    cell12.setCellType(CellType.STRING);
                    cellValorAjusteProvision = formatter.formatCellValue(cell12).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(14)+" - (15)";
                    XSSFCell cell14= row.getCell(14);
                    cell14.setCellType(CellType.STRING);
                    cellIMPSDFUBA = formatter.formatCellValue(cell14).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(15)+" - (16)";
                    XSSFCell cell15= row.getCell(15);
                    cell15.setCellType(CellType.STRING);
                    cellIMPRACREG = formatter.formatCellValue(cell15).replace(" ", "");

                    log[1]=CellReference.convertNumToColString(6)+" - (7)";Double.parseDouble(cellEADInicial);
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";Double.parseDouble(cellEADFinal);
                    //log[1]=CellReference.convertNumToColString(8)+" - (9)";Double.parseDouble(cellEADY01Inicial);
                    log[1]=CellReference.convertNumToColString(9)+" - (10)";Double.parseDouble(cellEADY01Final);
                    log[1]=CellReference.convertNumToColString(10)+" - (11)";Double.parseDouble(cellImporteInicial);
                    log[1]=CellReference.convertNumToColString(11)+" - (12)";Double.parseDouble(cellImporteFinal);
                    log[1]=CellReference.convertNumToColString(12)+" - (13)";Double.parseDouble(cellValorAjusteProvision);
                    log[1]=CellReference.convertNumToColString(14)+" - (15)";Double.parseDouble(cellIMPSDFUBA);
                    log[1]=CellReference.convertNumToColString(15)+" - (16)";Double.parseDouble(cellIMPRACREG);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                    log[2]="false";
                    log[3]="Falló, se esperaba un número";
                    fail++;
                    lista.add(log);
                    continue;
                }

                if(cellContrato.length()==0 || cellContrato.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Contrato no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoFamiliaInicial.length()==0 || cellCodigoFamiliaInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Código Familia Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoFamiliaFinal.length()==0 || cellCodigoFamiliaFinal.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Código Familia Final no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoCliente.length()==0 || cellCodigoCliente.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló Código Cliente no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellStageInicial.length()==0 || cellStageInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló Stage Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellStageFinal.length()==0 || cellStageFinal.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló Stage Final no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellEADInicial.length()==0 || cellEADInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(6)+" - (7)";
                    log[2]="false";
                    log[3]="Falló EAD Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellEADFinal.length()==0 || cellEADFinal.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(7)+" - (8)";
                    log[2]="false";
                    log[3]="Falló EAD Final no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellEADY01Final.length()==0 || cellEADY01Final.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(9)+" - (10)";
                    log[2]="false";
                    log[3]="Falló EAD Y01 Final no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellImporteInicial.length()==0 || cellImporteInicial.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(10)+" - (11)";
                    log[2]="false";
                    log[3]="Falló Importe Inicial no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellImporteFinal.length()==0 || cellImporteFinal.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(11)+" - (12)";
                    log[2]="false";
                    log[3]="Falló Importe Final no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellValorAjusteProvision.length()==0 || cellValorAjusteProvision.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(12)+" - (13)";
                    log[2]="false";
                    log[3]="Falló Valor Ajuste Provisión no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellNumeroCaso.length()==0 || cellNumeroCaso.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(13)+" - (14)";
                    log[2]="false";
                    log[3]="Falló Número Caso no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellIMPSDFUBA.length()==0 || cellIMPSDFUBA.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(14)+" - (15)";
                    log[2]="false";
                    log[3]="Falló IMP SDFUBA no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellIMPRACREG.length()==0 || cellIMPRACREG.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(15)+" - (16)";
                    log[2]="false";
                    log[3]="Falló IMP RACREG no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true"))
                {
                    String validaText = "";
                    if(Double.parseDouble(cellImporteInicial) != (Math.round((Double.parseDouble(cellImporteFinal.isEmpty()?"0":cellImporteFinal))*100.0)/100.0))
                    {
                        validaText="cambia provisión";
                    }
                    if(!cellStageInicial.equals(cellStageFinal))
                    {
                        if(validaText.length()>0)
                        {
                            validaText = validaText + " - cambia stage";
                        }
                        else
                        {
                            validaText = validaText + "cambia stage";
                        }
                    }

                    RiskAccountTemporal riskAccount = new RiskAccountTemporal();
                    riskAccount.setContrato(cellContrato);
                    riskAccount.setCodigoFamiliaInicial(cellCodigoFamiliaInicial);
                    riskAccount.setCodigoFamiliaFinal(cellCodigoFamiliaFinal);
                    riskAccount.setCodigoCliente(cellCodigoCliente);
                    riskAccount.setStageInicial(cellStageInicial);
                    riskAccount.setStageFinal(cellStageFinal);
                    riskAccount.setEadInicial(Double.parseDouble(cellEADInicial.isEmpty()?"0":cellEADInicial));
                    riskAccount.setEadFinal(Double.parseDouble(cellEADFinal.isEmpty()?"0":cellEADFinal));
                    riskAccount.setEadY01Inicial(Double.parseDouble(cellEADY01Inicial.isEmpty()?"0":cellEADY01Inicial));
                    riskAccount.setEadY01Final(Double.parseDouble(cellEADY01Final.isEmpty()?"0":cellEADY01Final));
                    riskAccount.setImporteInicial(Double.parseDouble(cellImporteInicial.isEmpty()?"0":cellImporteInicial));
                    riskAccount.setImporteFinal(Math.round((Double.parseDouble(cellImporteFinal.isEmpty()?"0":cellImporteFinal))*100.0)/100.0);
                    riskAccount.setValorAjusteProvisión(Math.round((Double.parseDouble(cellValorAjusteProvision.isEmpty()?"0":cellValorAjusteProvision))*100.0)/100.0);
                    riskAccount.setNumeroCaso(cellNumeroCaso);
                    riskAccount.setImpSdfuba(Double.parseDouble(cellIMPSDFUBA.isEmpty()?"0":cellIMPSDFUBA));
                    riskAccount.setImpRacreg(Double.parseDouble(cellIMPRACREG.isEmpty()?"0":cellIMPRACREG));
                    riskAccount.setFamilia("");
                    if(validaText.contains("cambia provisión"))
                    {
                        riskAccount.setCambiaProvision(Double.parseDouble(cellValorAjusteProvision));
                    }
                    else
                    {
                        riskAccount.setCambiaProvision(Double.parseDouble(cellImporteFinal));
                    }
                    riskAccount.setValida(validaText);
                    riskAccount.setPeriodo(periodo);
                    listAdd.add(riskAccount);
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="RIESGO";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Contabilización Riesgos",user);
        }
        else
        {
            removeDataPeriodTemporal(periodo);
            riskAccountTemporalRepository.saveAll(listAdd);
            replicateData(periodo);

            auditCode("Carga masiva apartado Contabilización Riesgos realizada exitosamente",user);
        }
        return lista;
    }

    public List<RiskAccount> findAll(String periodo){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_contabilizacion_riesgos WHERE periodo = ? ",RiskAccount.class);
        query.setParameter(1,periodo);
        return query.getResultList();
    }

    public void replicateData(String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_contabilizacion_riesgos WHERE periodo = ?");
        query.setParameter(1,periodo);
        query.executeUpdate();

        Query query3 = entityManager.createNativeQuery("INSERT INTO nexco_contabilizacion_riesgos (cambia_provision,cambio_de_segmento,codigo_cliente,codigo_familia_final,codigo_familia_inicial,contrato,ead_final,ead_inicial,ead_y01_final,ead_y01_inicial,familia,imp_racreg,imp_sdfuba,importe_final,importe_inicial,numero_caso,stage_final,stage_inicial,valida,valor_ajuste_provisión,periodo) \n" +
                "SELECT cambia_provision,cambio_de_segmento,codigo_cliente,codigo_familia_final,codigo_familia_inicial,contrato,ead_final,ead_inicial,ead_y01_final,ead_y01_inicial,familia,imp_racreg,imp_sdfuba,importe_final,importe_inicial,numero_caso,stage_final,stage_inicial,valida,valor_ajuste_provisión,periodo \n" +
                "FROM nexco_contabilizacion_riesgos_temp WHERE periodo = ?");
        query3.setParameter(1,periodo);
        query3.executeUpdate();
    }

    public void loadSegmentosRechazos(String periodo)
    {
        loadDataContract(periodo);
        removeDataPeriod(periodo);

        Date today = new Date();
        String input = "SEGMENTOS-RECHAZOS";

        StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, periodo);

        if (validateStatus == null) {
            StatusInfo status = new StatusInfo();
            status.setInput(input);
            status.setPeriodo(periodo);
            status.setFecha(today);
            StatusInfoRepository.save(status);
        } else {
            validateStatus.setFecha(today);
            StatusInfoRepository.save(validateStatus);
        }
    }

    public void loadDataFinal(String periodo){

    }

    public void loadDataUpdate(String periodo){

        loadSegmentosRechazos(periodo);

        Query queryC = entityManager.createNativeQuery("CREATE TABLE nexco_contabilizacion_riesgos_temp_arb \n" +
                "(id_criesgos BIGINT IDENTITY(1,1) PRIMARY KEY,\n" +
                "cambia_provision FLOAT,\n" +
                "cambio_de_segmento VARCHAR(255),\n" +
                "codigo_cliente VARCHAR(255),\n" +
                "codigo_familia_final VARCHAR(255),\n" +
                "codigo_familia_inicial VARCHAR(255),\n" +
                "contrato VARCHAR(255),\n" +
                "ead_final FLOAT,\n" +
                "ead_inicial FLOAT,\n" +
                "ead_y01_final FLOAT,\n" +
                "ead_y01_inicial FLOAT,\n" +
                "familia VARCHAR(255),\n" +
                "imp_racreg FLOAT,\n" +
                "imp_sdfuba FLOAT,\n" +
                "importe_final FLOAT,\n" +
                "importe_inicial FLOAT,\n" +
                "numero_caso VARCHAR(255),\n" +
                "stage_final VARCHAR(255),\n" +
                "stage_inicial VARCHAR(255),\n" +
                "valida VARCHAR(255),\n" +
                "valor_ajuste_provisión FLOAT,\n" +
                "periodo VARCHAR(255))");
        queryC.executeUpdate();

        Query query = entityManager.createNativeQuery("INSERT INTO nexco_contabilizacion_riesgos_temp_arb (cambia_provision,cambio_de_segmento,codigo_cliente,codigo_familia_final,codigo_familia_inicial,contrato,ead_final,ead_inicial,ead_y01_final,ead_y01_inicial,familia,imp_racreg,imp_sdfuba,importe_final,importe_inicial,numero_caso,stage_final,stage_inicial,valida,valor_ajuste_provisión,periodo) \n" +
                "SELECT A.cambia_provision,B.segmento_real,A.codigo_cliente,A.codigo_familia_final,A.codigo_familia_inicial,A.contrato,A.ead_final,A.ead_inicial,A.ead_y01_final,A.ead_y01_inicial,A.familia,A.imp_racreg,A.imp_sdfuba,A.importe_final,A.importe_inicial,A.numero_caso,A.stage_final,A.stage_inicial,A.valida,A.valor_ajuste_provisión,A.periodo \n" +
                "FROM nexco_contabilizacion_riesgos_temp A \n" +
                "LEFT JOIN nexco_cambio_segementos_rechazos B ON A.codigo_cliente = B.codigo_cliente WHERE B.segmento_real IS NULL");
        query.executeUpdate();

        validateSegments(periodo);

        Query query2 = entityManager.createNativeQuery("DROP TABLE nexco_contabilizacion_riesgos_temp_arb");
        query2.executeUpdate();

        Query query3 = entityManager.createNativeQuery("INSERT INTO nexco_contabilizacion_riesgos (cambia_provision,cambio_de_segmento,codigo_cliente,codigo_familia_final,codigo_familia_inicial,contrato,ead_final,ead_inicial,ead_y01_final,ead_y01_inicial,familia,imp_racreg,imp_sdfuba,importe_final,importe_inicial,numero_caso,stage_final,stage_inicial,valida,valor_ajuste_provisión,periodo) \n" +
                "SELECT A.cambia_provision,B.segmento_real,A.codigo_cliente,A.codigo_familia_final,A.codigo_familia_inicial,A.contrato,A.ead_final,A.ead_inicial,A.ead_y01_final,A.ead_y01_inicial,A.familia,A.imp_racreg,A.imp_sdfuba,Round(A.importe_final, 2, 0),Round(A.importe_inicial, 2, 0),A.numero_caso,A.stage_final,A.stage_inicial,A.valida,Round(A.valor_ajuste_provisión, 2, 0),A.periodo \n" +
                "FROM nexco_contabilizacion_riesgos_temp A \n" +
                "LEFT JOIN nexco_cambio_segementos_rechazos B ON A.codigo_cliente = B.codigo_cliente WHERE B.segmento_real IS NOT NULL");
        query3.executeUpdate();

    }

    public void validateSegments(String period) {

        List<SegmentDecisionTree> listTree = segmentDecisionTreeRepository.findAll();

        for(SegmentDecisionTree tree: listTree){

            String where = "1=1";
            //corasu
            if(tree.getCorasuOp() != null && tree.getCorasu() != null) {
                if (tree.getCorasuOp().equals("=") || tree.getCorasuOp().equals("<>") || tree.getCorasuOp().equals("<") || tree.getCorasuOp().equals(">")) {
                    where = where += " and B.corazu " + tree.getCorasuOp() + " '" + tree.getCorasu() + "'";
                } else if (tree.getCorasuOp().equals("IN") || tree.getCorasuOp().equals("NOT IN")) {
                    if (tree.getCorasu().contains(",")) {
                        String in = "";
                        in = tree.getCorasu().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and B.corazu " + tree.getCorasuOp() + " " + in;
                    } else if (tree.getCorasu().contains("-")) {
                        try {
                            String[] ArrayIn = tree.getCorasu().replace(" ", "").split("-");
                            int max = Integer.parseInt(ArrayIn[1]);
                            int min = Integer.parseInt(ArrayIn[0]);
                            String in = "(";
                            if (max > min) {
                                for (int i = min; i < max + 1; ++i) {
                                    if (i == min) {
                                        in = in + "'" + i + "'";
                                    } else {
                                        in = in + ",'" + i + "'";
                                    }
                                }
                                in = in + ")";
                            }
                            where = where += " and B.corazu " + tree.getCorasuOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and B.corazu " + tree.getCorasuOp() + " ('" + tree.getCorasu() + "')";
                    }
                }
            }
            //subcorasu
            if(tree.getSubCorasuOp() != null && tree.getSubCorasu() != null) {
                if (tree.getSubCorasuOp().equals("=") || tree.getSubCorasuOp().equals("<>") || tree.getSubCorasuOp().equals("<") || tree.getSubCorasuOp().equals(">")) {
                    where = where += " and B.sub_corazu " + tree.getSubCorasuOp() + " '" + tree.getSubCorasu() + "'";
                } else if (tree.getSubCorasuOp().equals("IN") || tree.getSubCorasuOp().equals("NOT IN")) {
                    if (tree.getSubCorasu().contains(",")) {
                        String in = "";
                        in = tree.getSubCorasu().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and B.sub_corazu " + tree.getSubCorasuOp() + " " + in;
                    } else if (tree.getSubCorasu().contains("-")) {
                        try {
                            String[] ArrayIn = tree.getSubCorasu().replace(" ", "").split("-");
                            int max = Integer.parseInt(ArrayIn[1]);
                            int min = Integer.parseInt(ArrayIn[0]);
                            String in = "(";
                            if (max > min) {
                                for (int i = min; i < max + 1; ++i) {
                                    if (i == min) {
                                        in = in + "'" + i + "'";
                                    } else {
                                        in = in + ",'" + i + "'";
                                    }
                                }
                                in = in + ")";
                            }
                            where = where += " and B.sub_corazu " + tree.getSubCorasuOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and B.sub_corazu " + tree.getSubCorasuOp() + " ('" + tree.getSubCorasu() + "')";
                    }
                }
            }
            //ciiu
            if(tree.getCiiuOp() != null && tree.getCiiu() != null) {
                if (tree.getCiiuOp().equals("=") || tree.getCiiuOp().equals("<>") || tree.getCiiuOp().equals("<") || tree.getCiiuOp().equals(">")) {
                    where = where += " and RIGHT(B.ciuu, 4) " + tree.getCiiuOp() + " '" + tree.getCiiu() + "'";
                } else if (tree.getCiiuOp().equals("IN") || tree.getCiiuOp().equals("NOT IN")) {
                    if (tree.getCiiu().contains(",")) {
                        String in = "";
                        in = tree.getCiiu().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and RIGHT(B.ciuu, 4) " + tree.getCiiuOp() + " " + in;
                    } else if (tree.getCiiu().contains("-")) {
                        try {
                            String[] ArrayIn = tree.getCiiu().replace(" ", "").split("-");
                            int max = Integer.parseInt(ArrayIn[1]);
                            int min = Integer.parseInt(ArrayIn[0]);
                            String in = "(";
                            if (max > min) {
                                for (int i = min; i < max + 1; ++i) {
                                    if (i == min) {
                                        in = in + "'" + i + "'";
                                    } else {
                                        in = in + ",'" + i + "'";
                                    }
                                }
                                in = in + ")";
                            }
                            where = where += " and RIGHT(B.ciuu, 4) " + tree.getCiiuOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (tree.getCiiu().equals("IF")) {
                        where = where += " and RIGHT(B.ciuu, 4) " + tree.getCiiuOp() + "  (select distinct ciiu from nexco_ciiu)";
                    } else {
                        where = where += " and RIGHT(B.ciuu, 4) " + tree.getCiiuOp() + " ('" + tree.getCiiu() + "')";
                    }
                }
            }
            //empleados
            if(tree.getNumeroEmpleadosOp() != null && tree.getNumeroEmpleados() != null) {
                if (tree.getNumeroEmpleadosOp().equals("=") || tree.getNumeroEmpleadosOp().equals("<>") || tree.getNumeroEmpleadosOp().equals("<") || tree.getNumeroEmpleadosOp().equals(">")) {
                    where = where += " and B.numero_empleados " + tree.getNumeroEmpleadosOp() + " " + tree.getNumeroEmpleados();
                } else if (tree.getNumeroEmpleadosOp().equals("IN") || tree.getNumeroEmpleadosOp().equals("NOT IN")) {
                    if (tree.getNumeroEmpleados().contains(",")) {
                        String in = "";
                        in = tree.getNumeroEmpleados().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and B.numero_empleados " + tree.getNumeroEmpleadosOp() + " " + in;
                    } else if (tree.getNumeroEmpleados().contains("-")) {
                        try {
                            String[] ArrayIn = tree.getNumeroEmpleados().replace(" ", "").split("-");
                            int max = Integer.parseInt(ArrayIn[1]);
                            int min = Integer.parseInt(ArrayIn[0]);
                            String in = "(";
                            if (max > min) {
                                for (int i = min; i < max + 1; ++i) {
                                    if (i == min) {
                                        in = in + "'" + i + "'";
                                    } else {
                                        in = in + ",'" + i + "'";
                                    }
                                }
                                in = in + ")";
                            }
                            where = where += " and B.numero_empleados " + tree.getNumeroEmpleadosOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and B.numero_empleados " + tree.getNumeroEmpleadosOp() + " ('" + tree.getNumeroEmpleados() + "')";
                    }
                }
            }
            //activos
            if(tree.getTotalActivosOp() != null && tree.getTotalActivos() != null) {
                if (tree.getTotalActivosOp().equals("=") || tree.getTotalActivosOp().equals("<>") || tree.getTotalActivosOp().equals("<") || tree.getTotalActivosOp().equals(">")) {
                    where = where += " and B.total_activos " + tree.getTotalActivosOp() + " " + tree.getTotalActivos();
                } else if (tree.getTotalActivosOp().equals("IN") || tree.getTotalActivosOp().equals("NOT IN")) {
                    if (tree.getTotalActivos().contains(",")) {
                        String in = "";
                        in = tree.getTotalActivos().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and B.total_activos " + tree.getTotalActivosOp() + " " + in;
                    } else if (tree.getTotalActivos().contains("-")) {
                        try {
                            String[] ArrayIn = tree.getTotalActivos().replace(" ", "").split("-");
                            int max = Integer.parseInt(ArrayIn[1]);
                            int min = Integer.parseInt(ArrayIn[0]);
                            String in = "(";
                            if (max > min) {
                                for (int i = min; i < max + 1; ++i) {
                                    if (i == min) {
                                        in = in + "'" + i + "'";
                                    } else {
                                        in = in + ",'" + i + "'";
                                    }
                                }
                                in = in + ")";
                            }
                            where = where += " and B.total_activos " + tree.getTotalActivosOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and B.total_activos " + tree.getTotalActivosOp() + " ('" + tree.getTotalActivos() + "')";
                    }
                }
            }
            //ventas
            if(tree.getTotalVentasOp() != null && tree.getTotalVentas() != null) {
                if (tree.getTotalVentasOp().equals("=") || tree.getTotalVentasOp().equals("<>") || tree.getTotalVentasOp().equals("<") || tree.getTotalVentasOp().equals(">")) {
                    where = where += " and B.total_ventas " + tree.getTotalVentasOp() + " " + tree.getTotalVentas();
                } else if (tree.getTotalVentasOp().equals("IN") || tree.getTotalVentasOp().equals("NOT IN")) {
                    if (tree.getTotalVentas().contains(",")) {
                        String in = "";
                        in = tree.getTotalVentas().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and B.total_ventas " + tree.getTotalVentasOp() + " " + in;
                    } else if (tree.getTotalVentas().contains("-")) {
                        try {
                            String[] ArrayIn = tree.getTotalVentas().replace(" ", "").split("-");
                            int max = Integer.parseInt(ArrayIn[1]);
                            int min = Integer.parseInt(ArrayIn[0]);
                            String in = "(";
                            if (max > min) {
                                for (int i = min; i < max + 1; ++i) {
                                    if (i == min) {
                                        in = in + "'" + i + "'";
                                    } else {
                                        in = in + ",'" + i + "'";
                                    }
                                }
                                in = in + ")";
                            }
                            where = where += " and B.total_ventas " + tree.getTotalVentasOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and B.total_ventas " + tree.getTotalVentasOp() + " ('" + tree.getTotalVentas() + "')";
                    }
                }
            }

            Query insertSeg = entityManager.createNativeQuery("INSERT INTO nexco_contabilizacion_riesgos (cambia_provision,cambio_de_segmento,codigo_cliente,codigo_familia_final,codigo_familia_inicial,contrato,ead_final,ead_inicial,ead_y01_final,ead_y01_inicial,familia,imp_racreg,imp_sdfuba,importe_final,importe_inicial,numero_caso,stage_final,stage_inicial,valida,valor_ajuste_provisión,periodo) \n" +
                    "SELECT A.cambia_provision,?,A.codigo_cliente,A.codigo_familia_final,A.codigo_familia_inicial,A.contrato,A.ead_final,A.ead_inicial,A.ead_y01_final,A.ead_y01_inicial,A.familia,A.imp_racreg,A.imp_sdfuba,A.importe_final,A.importe_inicial,A.numero_caso,A.stage_final,A.stage_inicial,A.valida,A.valor_ajuste_provisión,? \n" +
                    "FROM nexco_contabilizacion_riesgos_temp_arb A \n" +
                    "LEFT JOIN nexco_cambio_segementos_rechazos B ON A.codigo_cliente = B.codigo_cliente \n" +
                    "WHERE "+where+" AND (A.cambio_de_segmento IS NULL OR A.cambio_de_segmento = '') ");

            insertSeg.setParameter(1,tree.getCodigoIFRS9());
            insertSeg.setParameter(2,period);
            insertSeg.executeUpdate();

            Query udpateSegVal = entityManager.createNativeQuery("UPDATE nexco_cambio_segementos_rechazos " +
                    "SET segmento_real = ? \n" +
                    "WHERE "+where.replace("B.","")+" AND (segmento_real IS NULL OR segmento_real = '')\n" +
                    ";");
            udpateSegVal.setParameter(1,tree.getCodigoIFRS9());
            udpateSegVal.executeUpdate();
        }
    }

    public void loadDataContract(String periodo){
        removeDataContract(periodo);

        /*Query query = entityManager.createNativeQuery("INSERT INTO nexco_cambio_segementos_rechazos (contrato,codigo_cliente,segmento_actual,cuenta_rechazo,segmento_rechazo,codigo_tipo_inst,nombre_cliente,corazu,sub_corazu,segmento_real,ciuu,tercero,numero_empleados,total_activos,total_ventas,origen,periodo, tipo_cliente)\n" +
                "SELECT A.cmco_cod_ccontr, A.cmco_cod_client, A.cmco_cod_segm_finrep, D.cuenta,D.segemento_proceso,A.cmco_cod_tipoinst, B.nombres+B.apellido1+B.apellido2 cliente, B.corazu, B.sub_corazu, C.segmento_finrep_new,B.CIIU,B.identificacion, CONVERT(INTEGER, B.No_EMPLEADOS_CLIENTE), CONVERT(DECIMAL, A.CMCO_IMP_EAD_ACTUAL), CONVERT(DECIMAL, A.CMCO_IMP_VOLVEN_CLI) , CASE WHEN C.segmento_finrep_new IS NOT NULL THEN 'HIST' ELSE 'ARBL' END, ?, B.tipo_id \n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_"+periodo.replace("-","")+" A \n" +
                //"FROM SIMUL.dbo.ifrs9_maestro_contratos_"+periodo.replace("-","")+" A \n" +
                "LEFT JOIN personas B on A.cmco_cod_client COLLATE SQL_Latin1_General_CP1_CI_AS = B.numclien \n" +
                "LEFT JOIN (SELECT * FROM nexco_segmentos_hist WHERE periodo = ?) C ON A.CMCO_COD_CLIENT COLLATE SQL_Latin1_General_CP1_CI_AS = C.numero_cliente\n" +
                "LEFT JOIN nexco_rechazos_cc D ON A.CMCO_COD_CCONTR COLLATE SQL_Latin1_General_CP1_CI_AS = D.contrato \n" +
                "WHERE a.cmco_cod_ccontr COLLATE SQL_Latin1_General_CP1_CI_AS IN (SELECT contrato from nexco_rechazos_cc WHERE (tipo_rechazo_real = 'Cambio de segmento' or tipo_rechazo_real = 'Error de armado') GROUP BY contrato)");*/

        Query query = entityManager.createNativeQuery("INSERT INTO nexco_cambio_segementos_rechazos (contrato,codigo_cliente,segmento_actual,cuenta_rechazo,segmento_rechazo,codigo_tipo_inst,nombre_cliente,corazu,sub_corazu,segmento_real,ciuu,tercero,numero_empleados,total_activos,total_ventas,origen,periodo, tipo_cliente)\n" +
                "SELECT A.cmco_cod_ccontr, A.cmco_cod_client, A.cmco_cod_segm_finrep, D.cuenta,D.segemento_proceso,A.cmco_cod_tipoinst, B.nombres+B.apellido1+B.apellido2 cliente, B.corazu, B.sub_corazu, C.segmento_finrep_new,B.CIIU,B.identificacion, CONVERT(INTEGER, B.No_EMPLEADOS_CLIENTE), CONVERT(DECIMAL, A.CMCO_IMP_EAD_ACTUAL), CONVERT(DECIMAL, A.CMCO_IMP_VOLVEN_CLI) , CASE WHEN C.segmento_finrep_new IS NOT NULL THEN 'HIST' ELSE 'ARBL' END, ?, B.tipo_id \n" +
                "FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.ifrs9_maestro_contratos_"+periodo.replace("-","")+" A \n" +
                "LEFT JOIN personas B on A.cmco_cod_client = B.numclien COLLATE Modern_Spanish_CI_AS\n" +
                "LEFT JOIN (SELECT * FROM nexco_segmentos_hist WHERE periodo = ?) C ON A.CMCO_COD_CLIENT = C.numero_cliente COLLATE Modern_Spanish_CI_AS\n" +
                "INNER JOIN (SELECT * from nexco_rechazos_cc WHERE (tipo_rechazo_real = 'Cambio de segmento' or tipo_rechazo_real = 'Error de armado')) D ON A.CMCO_COD_CCONTR = D.contrato COLLATE Modern_Spanish_CI_AS\n");
        query.setParameter(1,periodo);
        query.setParameter(2,periodo);
        query.executeUpdate();

    }

    public void auditCode (String info,User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setCentro(user.getCentro());
        insert.setComponente("Riesgos IFRS9");
        insert.setFecha(today);
        insert.setInput("Contabilización Riesgos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public RiskAccount findByIdCriesgos(Long inicial){
        return riskAccountRepository.findByIdCriesgos(inicial);
    }

    public RiskAccount saveRiskAccount(RiskAccount riskAccount){
        return riskAccountRepository.save(riskAccount);
    }

    public void removeRiskAccount(Long id, User user){
        riskAccountRepository.deleteById(id);
        auditCode("Eliminar registro tabla Contabilización Riesgos",user);
    }

    public void removeDataPeriodTemporal(String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_contabilizacion_riesgos_temp WHERE periodo = ?");
        query.setParameter(1,periodo);
        query.executeUpdate();
    }

    public void removeDataPeriod(String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_contabilizacion_riesgos WHERE periodo = ?");
        query.setParameter(1,periodo);
        query.executeUpdate();
    }

    public void removeDataContract(String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_cambio_segementos_rechazos WHERE periodo = ?");
        query.setParameter(1,periodo);
        query.executeUpdate();
    }

    public List<Object[]> findAllResume(String periodo){
        Query query = entityManager.createNativeQuery("SELECT ISNULL(A.valida,'NA') VA, ISNULL(A.stage,'NA') IMF, ISNULL(A.segmento,'NA') VAP,ISNULL(SUM(A.provision),0) PRV, A.contrato\n" +
                "FROM nexco_contabilizacion_riesgos_final AS A WHERE A.periodo = ? AND A.valida IS NOT NULL AND A.valida !='' GROUP BY ISNULL(A.valida,'NA'), ISNULL(A.stage,'NA'), ISNULL(A.segmento,'NA'),A.contrato ORDER BY ISNULL(A.valida,'NA'),A.contrato");
        query.setParameter(1,periodo);
        return query.getResultList();
    }

    public List<Object[]> findAllResumeLoad(String periodo){
        List<Object[]> finalList = new ArrayList<Object[]>();

        Query select0 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and (valida = '' OR valida IS NULL) GROUP BY valida");
        select0.setParameter(1, periodo);
        select0.setParameter(2, periodo);
        finalList.addAll(select0.getResultList());

        Query select1 = entityManager.createNativeQuery("SELECT ? PER,REPLACE(UPPER(valida),'- ',''),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida NOT LIKE ? and valida NOT LIKE ? and valida LIKE ? GROUP BY valida");
        select1.setParameter(1, periodo);
        select1.setParameter(2, periodo);
        select1.setParameter(3, "%cambia provisión%");
        select1.setParameter(4, "%cambia stage%");
        select1.setParameter(5, "%cambia segmento%");
        finalList.addAll(select1.getResultList());

        Query select2 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida NOT LIKE ? and valida LIKE ? and valida LIKE ? GROUP BY valida");
        select2.setParameter(1, periodo);
        select2.setParameter(2, periodo);
        select2.setParameter(3, "%cambia provisión%");
        select2.setParameter(4, "%cambia stage%");
        select2.setParameter(5, "%cambia segmento%");
        finalList.addAll(select2.getResultList());

        Query select3 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida LIKE ? and valida NOT LIKE ? and valida LIKE ? GROUP BY valida");
        select3.setParameter(1, periodo);
        select3.setParameter(2, periodo);
        select3.setParameter(3, "%cambia provisión%");
        select3.setParameter(4, "%cambia stage%");
        select3.setParameter(5, "%cambia segmento%");
        finalList.addAll(select3.getResultList());

        Query select4 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida LIKE ? and valida LIKE ? and valida LIKE ? GROUP BY valida");
        select4.setParameter(1, periodo);
        select4.setParameter(2, periodo);
        select4.setParameter(3, "%cambia provisión%");
        select4.setParameter(4, "%cambia stage%");
        select4.setParameter(5, "%cambia segmento%");
        finalList.addAll(select4.getResultList());

        Query select5 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida NOT LIKE ? and valida LIKE ? and valida NOT LIKE ? GROUP BY valida");
        select5.setParameter(1, periodo);
        select5.setParameter(2, periodo);
        select5.setParameter(3, "%cambia provisión%");
        select5.setParameter(4, "%cambia stage%");
        select5.setParameter(5, "%cambia segmento%");
        finalList.addAll(select5.getResultList());

        Query select6 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida LIKE ? and valida LIKE ? and valida NOT LIKE ? GROUP BY valida");
        select6.setParameter(1, periodo);
        select6.setParameter(2, periodo);
        select6.setParameter(3, "%cambia provisión%");
        select6.setParameter(4, "%cambia stage%");
        select6.setParameter(5, "%cambia segmento%");
        finalList.addAll(select6.getResultList());

        Query select7 = entityManager.createNativeQuery("SELECT ? PER,UPPER(valida),ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? and valida LIKE ? and valida NOT LIKE ? and valida NOT LIKE ? GROUP BY valida");
        select7.setParameter(1, periodo);
        select7.setParameter(2, periodo);
        select7.setParameter(3, "%cambia provisión%");
        select7.setParameter(4, "%cambia stage%");
        select7.setParameter(5, "%cambia segmento%");
        finalList.addAll(select7.getResultList());

        /*Query query = entityManager.createNativeQuery("SELECT ? PER,valida,ISNULL(SUM(A.importe_inicial),0) IMI, ISNULL(SUM(A.importe_final),0) IMF,ISNULL(SUM(A.valor_ajuste_provisión),0) VAP \n" +
                "FROM nexco_contabilizacion_riesgos AS A WHERE A.periodo = ? GROUP BY valida");
        query.setParameter(1,periodo);
        query.setParameter(2,periodo);
        finalList.addAll(query.getResultList());*/


        return finalList;
    }

    public void clearRiskAccount(User user){
        riskAccountRepository.deleteAll();
        auditCode("Limpiar tabla Contabilización Riesgos",user);
    }

    public Page<RiskAccount> getAll(Pageable pageable){
        return riskAccountRepository.findAll(pageable);
    }

    public List<RiskAccount> getAllData(String periodo){
        Query selectS = entityManager.createNativeQuery("SELECT * FROM nexco_contabilizacion_riesgos WHERE periodo = ?", RiskAccount.class);
        selectS.setParameter(1, periodo);
        return selectS.getResultList();
    }

    public List<RiskAccountFinal> getDataCSV(String periodo){

        List<RiskAccountFinal> list =new ArrayList<>();
        Query selectS = entityManager.createNativeQuery("SELECT * FROM nexco_contabilizacion_riesgos_final WHERE periodo = ? and (Round(provision, 2, 0) != 0 or provision is null) and valida = ?  ORDER BY contrato", RiskAccountFinal.class);
        selectS.setParameter(1, periodo);
        selectS.setParameter(2, "cambia segmento");
        list.addAll(selectS.getResultList());

        Query select1 = entityManager.createNativeQuery("SELECT * FROM nexco_contabilizacion_riesgos_final WHERE periodo = ? and (Round(provision, 2, 0) != 0 or provision is null) and valida = ?  ORDER BY contrato", RiskAccountFinal.class);
        select1.setParameter(1, periodo);
        select1.setParameter(2, "cambia stage");
        list.addAll(select1.getResultList());

        Query select2 = entityManager.createNativeQuery("SELECT * FROM nexco_contabilizacion_riesgos_final WHERE periodo = ? and (Round(provision, 2, 0) != 0 or provision is null) and valida = ?  ORDER BY contrato", RiskAccountFinal.class);
        select2.setParameter(1, periodo);
        select2.setParameter(2, "cambia provisión");
        list.addAll(select2.getResultList());

        Query select3 = entityManager.createNativeQuery("SELECT * FROM nexco_contabilizacion_riesgos_final WHERE periodo = ? and (Round(provision, 2, 0) != 0 or provision is null) and valida NOT IN (?,?,?)  ORDER BY contrato", RiskAccountFinal.class);
        select3.setParameter(1, periodo);
        select3.setParameter(2, "cambia provisión");
        select3.setParameter(3, "cambia stage");
        select3.setParameter(4, "cambia segmento");
        list.addAll(select3.getResultList());

        return list;
    }

    public void updateDataFinal(String periodo){
        Query selectS = entityManager.createNativeQuery("delete from nexco_contabilizacion_riesgos_final where periodo = ?");
        selectS.setParameter(1, periodo);
        selectS.executeUpdate();

        Query updateData = entityManager.createNativeQuery("update nexco_contabilizacion_riesgos set cambio_de_segmento = b.segmento_real, valida = CONCAT(REPLACE(valida,' - cambia segmento',''),' - cambia segmento') \n" +
                "from nexco_contabilizacion_riesgos a, nexco_cambio_segementos_rechazos_final b \n" +
                "where a.codigo_cliente= b.codigo_cliente");
        updateData.executeUpdate();

        /*Query updateData1 = entityManager.createNativeQuery("update nexco_contabilizacion_riesgos set valida = CONCAT(REPLACE(valida,' - cambia segmento',''),' - cambia segmento') \n" +
                "from nexco_contabilizacion_riesgos a, nexco_cambio_segementos_rechazos_final b\n" +
                "where a.codigo_cliente= b.codigo_cliente and origen ='SUGERENCIA ÁRBOL'");
        updateData1.executeUpdate();*/

        Query insertDS = entityManager.createNativeQuery("INSERT INTO nexco_contabilizacion_riesgos_final (contrato,provision,stage,segmento,valida,periodo)\n" +
                "SELECT contrato, SUM(importe_final),stage_final,cambio_de_segmento,valida, ? FROM nexco_contabilizacion_riesgos WHERE periodo = ? and valida is not null and valida != '' group by contrato,stage_final,cambio_de_segmento,valida order by valida desc");
        insertDS.setParameter(1, periodo);
        insertDS.setParameter(2, periodo);
        insertDS.executeUpdate();

        Query insertDS1 = entityManager.createNativeQuery("INSERT INTO nexco_contabilizacion_riesgos_final (contrato,provision,stage,segmento,valida,periodo)\n" +
                "select contrato,null,null,segmento_real,'cambia segmento',? from nexco_cambio_segementos_rechazos_final where periodo = ? and contrato not in(select contrato from nexco_contabilizacion_riesgos_final where periodo = ?)");
        insertDS1.setParameter(1, periodo);
        insertDS1.setParameter(2, periodo);
        insertDS1.setParameter(3, periodo);
        insertDS1.executeUpdate();
    }

    public List<RiskAccount> findByFilter(String value, String filter) {
        List<RiskAccount> list=new ArrayList<RiskAccount>();
        switch (filter)
        {
            case "Código cliente":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_contabilizacion_riesgos as em " +
                        "WHERE em.codigo_cliente LIKE ?", RiskAccount.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Stage Final":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_contabilizacion_riesgos as em " +
                        "WHERE em.stage_final LIKE ?", RiskAccount.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Cambio Segmento":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_contabilizacion_riesgos as em " +
                        "WHERE em.cambio_de_segmento LIKE ?", RiskAccount.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Válida":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_contabilizacion_riesgos as em " +
                        "WHERE em.valida LIKE ?", RiskAccount.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
