package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.model.ifrs9.Eeff;

import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.repository.ifrs9.EeffRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ContractRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CountryRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CurrencyRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
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
import java.util.*;

@Service
@Transactional
public class EeffService {

    @Autowired
    private final EeffRepository eeffRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public EeffService(EeffRepository eeffRepository) {
        this.eeffRepository = eeffRepository;
    }


    public ArrayList<String[]> saveFileBD(InputStream file,String month, String tipo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list = validarPlantilla(rows);
            String[] temporal = list.get(0);
            if (temporal[2].equals("true")) {
                Query deleteInfo = entityManager.createNativeQuery("delete from nexco_eeff where period = ? and tipo = ?;");
                deleteInfo.setParameter(1,month);
                deleteInfo.setParameter(2,tipo);
                deleteInfo.executeUpdate();

                list = getRows(rows1,month,tipo);
                completeTable(month);
            }
        }
        return list;
    }

    public void completeTable(String period) {
        Query query = entityManager.createNativeQuery("UPDATE nexco_eeff\n" +
                "set entrada = t2.entrada, intergrupo= t2.intergupo\n" +
                "from nexco_eeff t1, nexco_cuentas_neocon t2\n" +
                "where t1.cuenta = t2.cuenta AND period = ? ");
        query.setParameter(1, period);
        query.executeUpdate();
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
            if (firstRow == 6) {
                DataFormatter formatter = new DataFormatter();
                String cellCodigoSocInformante = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                String cellDenominacionCuenta = formatter.formatCellValue(row.getCell(2));
                String cellTipoCuenta = formatter.formatCellValue(row.getCell(3));
                String cellCuenta = formatter.formatCellValue(row.getCell(4));
                String cellSocIC = formatter.formatCellValue(row.getCell(5));
                String cellDescripcionIC = formatter.formatCellValue(row.getCell(6));
                String cellDesgloces = formatter.formatCellValue(row.getCell(7));
                String cellEur = formatter.formatCellValue(row.getCell(8)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellUsd = formatter.formatCellValue(row.getCell(9)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellMll = formatter.formatCellValue(row.getCell(10)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellRst = formatter.formatCellValue(row.getCell(11)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellTotal = formatter.formatCellValue(row.getCell(12)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellDivisa = formatter.formatCellValue(row.getCell(13));
                log[0] = String.valueOf(row.getRowNum());
                if ((cellCodigoSocInformante.isEmpty() || cellCodigoSocInformante.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellDenominacionCuenta.isEmpty() || cellDenominacionCuenta.isBlank()) && (cellTipoCuenta.isEmpty() || cellTipoCuenta.isBlank())
                        && (cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellSocIC.isEmpty() || cellSocIC.isBlank()) && (cellDescripcionIC.isEmpty() || cellDescripcionIC.isBlank())
                        && (cellDesgloces.isEmpty() || cellDesgloces.isBlank()) && (cellEur.isEmpty() || cellEur.isBlank()) && (cellUsd.isEmpty() || cellUsd.isBlank())
                        && (cellMll.isEmpty() || cellMll.isBlank()) && (cellRst.isEmpty() || cellRst.isBlank()) && (cellTotal.isEmpty() || cellTotal.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank())
                ) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = String.valueOf(row.getRowNum());
                    log[2] = "true";
                    break;
                } else if (cellCodigoSocInformante.isEmpty() || cellCodigoSocInformante.isBlank()) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "1";
                    log[2] = "false";
                    break;
                } else if (cellDescripcion.isEmpty() || cellDescripcion.isBlank()) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "2";
                    log[2] = "false";
                    break;
                } else if (cellDenominacionCuenta.isEmpty() || cellDenominacionCuenta.isBlank()) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "3";
                    log[2] = "false";
                    break;
                } else if (cellTipoCuenta.isEmpty() || cellTipoCuenta.isBlank()) {
                    log[0] = String.valueOf(row.getRowNum());
                    log[1] = "4";
                    log[2] = "false";
                    break;
                } else {
                    try {
                        log[0] = String.valueOf(row.getRowNum());
                        log[1] = "9";
                        if(!cellEur.isBlank())
                            Double.parseDouble(cellEur);
                        log[1] = "10";
                        if(!cellUsd.isBlank())
                            Double.parseDouble(cellUsd);
                        log[1] = "11";
                        if(!cellMll.isBlank())
                            Double.parseDouble(cellMll);
                        log[1] = "12";
                        if(!cellRst.isBlank())
                            Double.parseDouble(cellRst);
                        log[1] = "13";
                        if(!cellTotal.isBlank())
                            Double.parseDouble(cellTotal);
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

    public ArrayList getRows(Iterator<Row> rows,String month, String tipo) {
        XSSFRow row;
        ArrayList lista = new ArrayList();
        int firstRow = 1;

        String cellCuentaPrev = "";

        while (rows.hasNext()) {
            String[] log = new String[3];
            log[2] = "false";
            row = (XSSFRow) rows.next();

            if (firstRow == 6 && row.getCell(0) != null) {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCodigoSocInformante = formatter.formatCellValue(row.getCell(0));
                String cellDescripcion = formatter.formatCellValue(row.getCell(1));
                String cellDenominacionCuenta = formatter.formatCellValue(row.getCell(2));
                String cellTipoCuenta = formatter.formatCellValue(row.getCell(3));
                String cellCuenta = formatter.formatCellValue(row.getCell(4));
                String cellSocIC = formatter.formatCellValue(row.getCell(5));
                String cellDescripcionIC = formatter.formatCellValue(row.getCell(6));
                String cellDesgloces = formatter.formatCellValue(row.getCell(7));
                String cellEur = formatter.formatCellValue(row.getCell(8)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellUsd = formatter.formatCellValue(row.getCell(9)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellMll = formatter.formatCellValue(row.getCell(10)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellRst = formatter.formatCellValue(row.getCell(11)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellTotal = formatter.formatCellValue(row.getCell(12)).replace(".", "").replace(",", ".").replace(" ", "");
                String cellDivisa = formatter.formatCellValue(row.getCell(13));


                if ((cellCodigoSocInformante.isEmpty() || cellCodigoSocInformante.isBlank()) && (cellDescripcion.isEmpty() || cellDescripcion.isBlank())
                        && (cellDenominacionCuenta.isEmpty() || cellDenominacionCuenta.isBlank()) && (cellTipoCuenta.isEmpty() || cellTipoCuenta.isBlank())
                        && (cellCuenta.isEmpty() || cellCuenta.isBlank()) && (cellSocIC.isEmpty() || cellSocIC.isBlank()) && (cellDescripcionIC.isEmpty() || cellDescripcionIC.isBlank())
                        && (cellDesgloces.isEmpty() || cellDesgloces.isBlank()) && (cellEur.isEmpty() || cellEur.isBlank()) && (cellUsd.isEmpty() || cellUsd.isBlank())
                        && (cellMll.isEmpty() || cellMll.isBlank()) && (cellRst.isEmpty() || cellRst.isBlank()) && (cellTotal.isEmpty() || cellTotal.isBlank())
                        && (cellDivisa.isEmpty() || cellDivisa.isBlank())
                ) {
                    log[0] = cellDenominacionCuenta;
                    log[1] = "Fallo al ingresar registro";
                    break;
                } else {

                    if((cellCuenta.isEmpty() || cellCuenta.isBlank())){
                        cellCuenta = cellCuentaPrev;
                    }else{
                        cellCuentaPrev = cellCuenta;
                    }

                    if (!cellEur.isEmpty() && !cellEur.isBlank()) {
                        Eeff eeff = new Eeff();
                        eeff.setCodigosSocInformante(cellCodigoSocInformante);
                        eeff.setDescripcion(cellDescripcion);
                        eeff.setId(cellDenominacionCuenta);
                        eeff.setTipoCuenta(cellTipoCuenta);
                        if (!cellCuenta.isEmpty() && !cellCuenta.isBlank()) { eeff.setCuenta(cellCuenta); } else { eeff.setCuenta(""); }
                        if (!cellSocIC.isEmpty() && !cellSocIC.isBlank()) { eeff.setSocIC(cellSocIC); } else { eeff.setSocIC(""); }
                        if (!cellDescripcionIC.isEmpty() && !cellDescripcionIC.isBlank()) { eeff.setDescripcionIC(cellDescripcionIC); } else { eeff.setDescripcionIC(""); }
                        if (!cellDesgloces.isEmpty() && !cellDesgloces.isBlank()) { eeff.setDesgloces(cellDesgloces); } else { eeff.setDesgloces(""); }
                        eeff.setDivisaespana("EUR");
                        eeff.setSaldo(cellEur);
                        eeff.setIntergrupo("");
                        eeff.setEntrada("");
                        eeff.setPeriodo(month);
                        eeff.setTipo(tipo);
                        eeffRepository.save(eeff);
                        log[1] = "Registro actualizado exitosamente.";
                        log[2] = "true";
                    }

                    if (!cellUsd.isEmpty() && !cellUsd.isBlank()) {
                        Eeff eeff = new Eeff();
                        eeff.setCodigosSocInformante(cellCodigoSocInformante);
                        eeff.setDescripcion(cellDescripcion);
                        eeff.setId(cellDenominacionCuenta);
                        eeff.setTipoCuenta(cellTipoCuenta);
                        if (!cellCuenta.isEmpty() && !cellCuenta.isBlank()) { eeff.setCuenta(cellCuenta); } else { eeff.setCuenta(""); }
                        if (!cellSocIC.isEmpty() && !cellSocIC.isBlank()) { eeff.setSocIC(cellSocIC); } else { eeff.setSocIC(""); }
                        if (!cellDescripcionIC.isEmpty() && !cellDescripcionIC.isBlank()) { eeff.setDescripcionIC(cellDescripcionIC); } else { eeff.setDescripcionIC(""); }
                        if (!cellDesgloces.isEmpty() && !cellDesgloces.isBlank()) { eeff.setDesgloces(cellDesgloces); } else { eeff.setDesgloces(""); }
                        eeff.setDivisaespana("USD");
                        eeff.setSaldo(cellUsd);
                        eeff.setIntergrupo("");
                        eeff.setEntrada("");
                        eeff.setPeriodo(month);
                        eeff.setTipo(tipo);
                        eeffRepository.save(eeff);
                        log[1] = "Registro actualizado exitosamente.";
                        log[2] = "true";
                    }
                    if (!cellMll.isEmpty() && !cellMll.isBlank()) {
                        Eeff eeff = new Eeff();
                        eeff.setCodigosSocInformante(cellCodigoSocInformante);
                        eeff.setDescripcion(cellDescripcion);
                        eeff.setId(cellDenominacionCuenta);
                        eeff.setTipoCuenta(cellTipoCuenta);
                        if (!cellCuenta.isEmpty() && !cellCuenta.isBlank()) { eeff.setCuenta(cellCuenta); } else { eeff.setCuenta(""); }
                        if (!cellSocIC.isEmpty() && !cellSocIC.isBlank()) { eeff.setSocIC(cellSocIC); } else { eeff.setSocIC(""); }
                        if (!cellDescripcionIC.isEmpty() && !cellDescripcionIC.isBlank()) { eeff.setDescripcionIC(cellDescripcionIC); } else { eeff.setDescripcionIC(""); }
                        if (!cellDesgloces.isEmpty() && !cellDesgloces.isBlank()) { eeff.setDesgloces(cellDesgloces); } else { eeff.setDesgloces(""); }
                        eeff.setDivisaespana("MLL");
                        eeff.setSaldo(cellMll);
                        eeff.setIntergrupo("");
                        eeff.setEntrada("");
                        eeff.setPeriodo(month);
                        eeff.setTipo(tipo);
                        eeffRepository.save(eeff);
                        log[1] = "Registro actualizado exitosamente.";
                        log[2] = "true";
                    }
                    if (!cellRst.isEmpty() && !cellRst.isBlank()) {
                        Eeff eeff = new Eeff();
                        eeff.setCodigosSocInformante(cellCodigoSocInformante);
                        eeff.setDescripcion(cellDescripcion);
                        eeff.setId(cellDenominacionCuenta);
                        eeff.setTipoCuenta(cellTipoCuenta);
                        if (!cellCuenta.isEmpty() && !cellCuenta.isBlank()) { eeff.setCuenta(cellCuenta); } else { eeff.setCuenta(""); }
                        if (!cellSocIC.isEmpty() && !cellSocIC.isBlank()) { eeff.setSocIC(cellSocIC); } else { eeff.setSocIC(""); }
                        if (!cellDescripcionIC.isEmpty() && !cellDescripcionIC.isBlank()) { eeff.setDescripcionIC(cellDescripcionIC); } else { eeff.setDescripcionIC(""); }
                        if (!cellDesgloces.isEmpty() && !cellDesgloces.isBlank()) { eeff.setDesgloces(cellDesgloces); } else { eeff.setDesgloces(""); }
                        eeff.setDivisaespana("RST");
                        eeff.setSaldo(cellRst);
                        eeff.setIntergrupo("");
                        eeff.setEntrada("");
                        eeff.setPeriodo(month);
                        eeff.setTipo(tipo);
                        eeffRepository.save(eeff);
                        log[1] = "Registro actualizado exitosamente.";
                        log[2] = "true";
                    }
                    if((cellEur.isEmpty() || cellEur.isBlank()) && (cellUsd.isEmpty() || cellUsd.isBlank()) && (cellMll.isEmpty() || cellMll.isBlank()) && (cellRst.isEmpty() || cellRst.isBlank()))
                    {
                        Eeff eeff = new Eeff();
                        eeff.setCodigosSocInformante(cellCodigoSocInformante);
                        eeff.setDescripcion(cellDescripcion);
                        eeff.setId(cellDenominacionCuenta);
                        eeff.setTipoCuenta(cellTipoCuenta);
                        if (!cellCuenta.isEmpty() && !cellCuenta.isBlank()) { eeff.setCuenta(cellCuenta); } else { eeff.setCuenta(""); }
                        if (!cellSocIC.isEmpty() && !cellSocIC.isBlank()) { eeff.setSocIC(cellSocIC); } else { eeff.setSocIC(""); }
                        if (!cellDescripcionIC.isEmpty() && !cellDescripcionIC.isBlank()) { eeff.setDescripcionIC(cellDescripcionIC); } else { eeff.setDescripcionIC(""); }
                        if (!cellDesgloces.isEmpty() && !cellDesgloces.isBlank()) { eeff.setDesgloces(cellDesgloces); } else { eeff.setDesgloces(""); }
                        eeff.setDivisaespana("");
                        eeff.setSaldo("");
                        eeff.setIntergrupo("");
                        eeff.setEntrada("");
                        eeff.setPeriodo(month);
                        eeff.setTipo(tipo);
                        eeffRepository.save(eeff);
                        log[1] = "Registro actualizado exitosamente.";
                        log[2] = "true";
                    }

                }
                lista.add(log);
            } else {
                firstRow ++;
            }
        }
        return lista;
    }

    public List<Eeff> findAll() {
        return eeffRepository.findAll();
    }

    public   List<Eeff> findAllEeff(String periodo, String tipo){
        List<Eeff> alleeff = new ArrayList<Eeff>();
        javax.persistence.Query query = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo LIKE ? and saldo <> ''", Eeff.class);
        query.setParameter(1, periodo);
        query.setParameter(2, tipo);
        alleeff.addAll(query.getResultList());
        return alleeff;
    }

    public List<Eeff> findByFilter(String valor,String filtro,String periodo,String tipo){
        ArrayList<Eeff> toReturn;
        switch (filtro)
        {
            case "Código Sociedad Informante":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND sociedad_informante LIKE ?",Eeff.class);
                query.setParameter(3, valor);
                query.setParameter(1, periodo);
                query.setParameter(2, tipo);
                if(query.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query.getResultList();
                }
                break;

            case "Descripción":
                Query query0 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND descripcion LIKE ?",Eeff.class);
                query0.setParameter(3, valor);
                query0.setParameter(1, periodo);
                query0.setParameter(2, tipo);
                if(query0.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query0.getResultList();
                }
                break;

            case "Denominación de la cuenta":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND id LIKE ?",Eeff.class);
                query1.setParameter(3, valor);
                query1.setParameter(1, periodo);
                query1.setParameter(2, tipo);
                if(query1.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query1.getResultList();
                }
                break;

            case "Tipo de cuenta":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND tipo_cuenta LIKE ?",Eeff.class);
                query2.setParameter(3, valor);
                query2.setParameter(1, periodo);
                query2.setParameter(2, tipo);
                if(query2.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query2.getResultList();
                }
                break;

            case "Cuenta":
                Query query3 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND cuenta LIKE ?",Eeff.class);
                query3.setParameter(3, valor);
                query3.setParameter(1, periodo);
                query3.setParameter(2, tipo);
                if(query3.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query3.getResultList();
                }
                break;

            case "Soc. IC":
                Query query4 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND sociedad_IC LIKE ?",Eeff.class);
                query4.setParameter(3, valor);
                query4.setParameter(1, periodo);
                query4.setParameter(2, tipo);
                if(query4.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query4.getResultList();
                }
                break;

            case "Descripcion IC":
                Query query5 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND descripcion_IC LIKE ?",Eeff.class);
                query5.setParameter(3, valor);
                query5.setParameter(1, periodo);
                query5.setParameter(2, tipo);
                if(query5.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query5.getResultList();
                }
                break;

            case "Desgloses":
                Query query6 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND desgloces LIKE ?",Eeff.class);
                query6.setParameter(3, valor);
                query6.setParameter(1, periodo);
                query6.setParameter(2, tipo);
                if(query6.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query6.getResultList();
                }
                break;

            case "Divisa España":
                Query query7 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND Divisa_espana LIKE ?",Eeff.class);
                query7.setParameter(3, valor);
                query7.setParameter(1, periodo);
                query7.setParameter(2, tipo);
                if(query7.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query7.getResultList();
                }
                break;

            case "Saldo":
                Query query8 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND saldo LIKE ?",Eeff.class);
                query8.setParameter(3, valor);
                query8.setParameter(1, periodo);
                query8.setParameter(2, tipo);
                if(query8.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query8.getResultList();
                }
                break;

            case "Intergrupo":
                Query query9 = entityManager.createNativeQuery("SELECT * FROM nexco_eeff WHERE period = ? AND tipo= ? AND intergrupo LIKE ?",Eeff.class);
                query9.setParameter(3, valor);
                query9.setParameter(1, periodo);
                query9.setParameter(2, tipo);
                if(query9.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query9.getResultList();
                }
                break;

            case "Entrada":
                Query query10 = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal WHERE period = ? AND tipo= ? AND entrada LIKE ?",Eeff.class);
                query10.setParameter(3, valor);
                query10.setParameter(1, periodo);
                query10.setParameter(2, tipo);
                if(query10.getResultList().isEmpty()){
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Eeff>) query10.getResultList();
                }
                break;

            default :
                toReturn = new ArrayList<>();
        }

        return toReturn;
    }
}