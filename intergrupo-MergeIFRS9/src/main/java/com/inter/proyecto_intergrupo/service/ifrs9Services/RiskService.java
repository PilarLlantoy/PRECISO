package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.TemplateBank;
import com.inter.proyecto_intergrupo.model.ifrs9.Risk;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.ifrs9.RiskRepository;
import org.apache.poi.hssf.usermodel.HSSFFormulaEvaluator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RiskService {

    @Autowired
    private RiskRepository riskRepository;

    public List<Risk> getAllRisksByPeriodo(){
        return riskRepository.findAll();
    }

    public ArrayList<String[]> saveFileBD(InputStream file, String period) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=getRows(rows1,period);
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista= new ArrayList();
        XSSFRow row;
        int firstRow=1;
        String[] log=new String[3];
        log[0]="0";
        log[1]="0";
        log[2]="false";
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row!=null)
            {
                DataFormatter formatter = new DataFormatter();
                String cellContrato = formatter.formatCellValue(row.getCell(0));
                String cellFamiliaInicial = formatter.formatCellValue(row.getCell(1));
                String cellFamiliaFinal = formatter.formatCellValue(row.getCell(2));
                String cellCliente = formatter.formatCellValue(row.getCell(3));
                String cellStageIncial = formatter.formatCellValue(row.getCell(4));
                String cellStageFinal = formatter.formatCellValue(row.getCell(5));
                String cellEdadInicial = formatter.formatCellValue(row.getCell(6)).replace(",",".").replace(" ","");
                String cellEdadFinal = formatter.formatCellValue(row.getCell(7));
                String cellY01Inicial = formatter.formatCellValue(row.getCell(8));
                String cellY01Final = formatter.formatCellValue(row.getCell(9));
                String cellProvInicial = formatter.formatCellValue(row.getCell(10));
                String cellProvFinal = formatter.formatCellValue(row.getCell(11));
                String cellAjusteProvision = formatter.formatCellValue(row.getCell(12));
                String cellNumeroCaso = formatter.formatCellValue(row.getCell(13));
                String cellSdfuba = formatter.formatCellValue(row.getCell(14));
                String cellRacreg = formatter.formatCellValue(row.getCell(15));
                String cellFamilia = formatter.formatCellValue(row.getCell(16));

                log[0]=String.valueOf(row.getRowNum() +1);

                if((cellContrato.isEmpty() || cellContrato.isBlank()) && (cellFamiliaInicial.isEmpty() || cellFamiliaInicial.isBlank())
                        && (cellCliente.isEmpty() || cellCliente.isBlank()) && (cellStageIncial.isEmpty() || cellStageIncial.isBlank()) && (cellEdadInicial.isEmpty() || cellEdadInicial.isBlank()) &&
                        (cellY01Inicial.isEmpty() || cellY01Inicial.isBlank()) && (cellY01Final.isEmpty() || cellY01Final.isBlank()) && (cellEdadFinal.isEmpty() || cellEdadFinal.isBlank())
                        && (cellProvInicial.isEmpty() || cellProvInicial.isBlank()) && (cellFamiliaFinal.isEmpty() || cellFamiliaFinal.isBlank())
                        && (cellStageFinal.isEmpty() || cellStageFinal.isBlank()) && (cellProvFinal.isEmpty() || cellProvFinal.isBlank()) && (cellAjusteProvision.isEmpty() || cellAjusteProvision.isBlank())
                        && (cellNumeroCaso.isEmpty() || cellNumeroCaso.isBlank()) && (cellSdfuba.isEmpty() || cellSdfuba.isBlank())
                        && (cellRacreg.isEmpty() || cellRacreg.isBlank()) && (cellFamilia.isEmpty() || cellFamilia.isBlank()))
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }else if(cellContrato.isEmpty() || cellContrato.isBlank()){
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="false";
                    break;
                } else if (cellFamiliaInicial.isEmpty() || cellFamiliaInicial.isBlank()){
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="2";
                    log[2]="false";
                    break;
                } else if (cellFamiliaFinal.isEmpty() || cellFamiliaFinal.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="3";
                    log[2]="false";
                    break;
                } else if (cellCliente.isEmpty() || cellCliente.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="4";
                    log[2]="false";
                    break;
                } else if (cellStageIncial.isEmpty() || cellStageIncial.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="5";
                    log[2]="false";
                    break;
                } else if (cellStageFinal.isEmpty() || cellStageFinal.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="6";
                    log[2]="false";
                    break;
                } else if (cellEdadInicial.isEmpty() || cellEdadInicial.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="7";
                    log[2]="false";
                    break;
                } else if (cellEdadFinal.isEmpty() || cellEdadFinal.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="8";
                    log[2]="false";
                    break;
                } else if (cellY01Inicial.isEmpty() || cellY01Inicial.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="9";
                    log[2]="false";
                    break;
                } else if (cellY01Final.isEmpty() || cellY01Final.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="10";
                    log[2]="false";
                    break;
                } else if (cellProvInicial.isEmpty() || cellProvInicial.isBlank()) {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="11";
                    log[2]="false";
                    break;
                }
            } else {
                firstRow=0;
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
                String cellContrato = formatter.formatCellValue(row.getCell(0));
                String cellFamiliaInicial = formatter.formatCellValue(row.getCell(1));
                String cellFamiliaFinal = formatter.formatCellValue(row.getCell(2));
                String cellCliente = formatter.formatCellValue(row.getCell(3));
                String cellStageIncial = formatter.formatCellValue(row.getCell(4));
                String cellStageFinal = formatter.formatCellValue(row.getCell(5));
                String cellEdadInicial = formatter.formatCellValue(row.getCell(6)).replace(",",".").replace(" ","");
                String cellEdadFinal = formatter.formatCellValue(row.getCell(7));
                String cellY01Inicial = formatter.formatCellValue(row.getCell(8));
                String cellY01Final = formatter.formatCellValue(row.getCell(9));
                String cellProvInicial = formatter.formatCellValue(row.getCell(10));
                String cellProvFinal = formatter.formatCellValue(row.getCell(11));
                String cellAjusteProvision = formatter.formatCellValue(row.getCell(12));
                String cellNumeroCaso = formatter.formatCellValue(row.getCell(13));
                String cellSdfuba = formatter.formatCellValue(row.getCell(14));;
                String cellRacreg = formatter.formatCellValue(row.getCell(15));
                String cellFamilia = formatter.formatCellValue(row.getCell(16));

                if((cellContrato.isEmpty() || cellContrato.isBlank()) && (cellFamiliaInicial.isEmpty() || cellFamiliaInicial.isBlank())
                        && (cellCliente.isEmpty() || cellCliente.isBlank()) && (cellStageIncial.isEmpty() || cellStageIncial.isBlank()) && (cellEdadInicial.isEmpty() || cellEdadInicial.isBlank()) &&
                        (cellY01Inicial.isEmpty() || cellY01Inicial.isBlank()) && (cellY01Final.isEmpty() || cellY01Final.isBlank()) && (cellEdadFinal.isEmpty() || cellEdadFinal.isBlank())
                        && (cellProvInicial.isEmpty() || cellProvInicial.isBlank()) && (cellFamiliaFinal.isEmpty() || cellFamiliaFinal.isBlank())
                        && (cellStageFinal.isEmpty() || cellStageFinal.isBlank()) && (cellProvFinal.isEmpty() || cellProvFinal.isBlank()) && (cellAjusteProvision.isEmpty() || cellAjusteProvision.isBlank())
                        && (cellNumeroCaso.isEmpty() || cellNumeroCaso.isBlank()) && (cellSdfuba.isEmpty() || cellSdfuba.isBlank())
                        && (cellRacreg.isEmpty() || cellRacreg.isBlank()) && (cellFamilia.isEmpty() || cellFamilia.isBlank()))
                {
                    break;
                } else {
                    Risk risk = new Risk();
                    risk.setCMCO_COD_CCONTR(cellContrato);
                    risk.setCMCO_COD_FAMILIA_FINAL(cellFamiliaFinal);
                    risk.setCMCO_COD_FAMILIA_FINAL(cellFamiliaFinal);
                    risk.setCMCO_COD_CLIENT(cellCliente);
                    risk.setCMCO_IND_STAGE_INICIAL(cellStageIncial);
                    risk.setCMCO_IND_STAGE_FINAL(cellStageFinal);
                    risk.setEAD_INICIAL(cellEdadInicial);
                    risk.setEAD_FINAL(cellEdadFinal);
                    risk.setEAD_Y01_INICIAL(cellY01Inicial);
                    risk.setEAD_Y01_FINAL(cellY01Final);
                    risk.setCMCO_IMP_PROV_INICIAL(cellProvInicial);
                    risk.setCMCO_IMP_PROV_FINAL(cellProvFinal);
                    risk.setVALOR_AJUSTE_PROVISION(cellAjusteProvision.replace(".",""));
                    risk.setNumero_caso(cellNumeroCaso.substring(2));
                    risk.setCMCO_IMP_SDFUBA_CON(cellSdfuba);
                    risk.setCMCO_IMP_RACREG_CON(cellRacreg);
                    risk.setFAMILIA(cellFamilia);
                    riskRepository.save(risk);
                    log[0] = cellContrato;
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

    public void removeThird(String id){
        riskRepository.deleteById(id);
    }

    public Optional<Risk> findById(String id){
        return riskRepository.findById(id);
    }
}
