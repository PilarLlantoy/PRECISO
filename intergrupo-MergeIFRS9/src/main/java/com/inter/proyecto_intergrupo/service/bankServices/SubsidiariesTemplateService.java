package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.SubsidiariesTemplate;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

@Transactional
@Service
public class SubsidiariesTemplateService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public SubsidiariesTemplateService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user, String period) throws IOException {

        Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_plantilla_filiales_temporal");
        delete.executeUpdate();

        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantilla(rows, user, period);
            }catch (Exception e){
                String[] error = new String[1];
                error[0] = "Fallo Estrucutura";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";

        ArrayList<SubsidiariesTemplateTemporal> toInsert = new ArrayList<>();

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellYntpReportante = formatter.formatCellValue(row.getCell(0)).replaceAll("[^a-zA-Z0-9]", "");
                String cellNeocon = formatter.formatCellValue(row.getCell(1)).replaceAll("[^a-zA-Z0-9]", "");
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).replaceAll("[^a-zA-Z0-9]", "");
                String cellYntp = formatter.formatCellValue(row.getCell(3)).replaceAll("[^a-zA-Z0-9]", "");
                String cellSociedadYntp = formatter.formatCellValue(row.getCell(4)).replaceAll("[^a-zA-Z0-9]", "");
                String cellContrato = formatter.formatCellValue(row.getCell(5)).replaceAll("[^a-zA-Z0-9]", "");
                //String cellNit = formatter.formatCellValue(row.getCell(6)).replaceAll("[^a-zA-Z0-9]", "");
                String cellNit = formatter.formatCellValue(row.getCell(6));
                String cellValor = formatter.formatCellValue(row.getCell(7)).replace(".", "").replace(",", ".");
                String cellCodPais = formatter.formatCellValue(row.getCell(8)).replaceAll("[^a-zA-Z0-9]", "");
                String cellPais = formatter.formatCellValue(row.getCell(9)).replaceAll("[^a-zA-Z0-9]", "");
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(10)).replaceAll("[^a-zA-Z0-9]", "");
                String cellObservaciones = formatter.formatCellValue(row.getCell(11));

                if (!user.getEmpresa().equals(cellYntpReportante)) {
                    if(cellYntpReportante.trim().length() != 5){
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(3);
                        log1[2] = "El campo YNTP debe tener 5 posiciones";
                        lista.add(log1);
                    } else{
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(0);
                        log1[2] = "El usuario no pertenece a la empresa Reportante ";
                        lista.add(log1);
                    }
                }
                if(cellNit.trim().replaceAll("[a-zA-Z0-9]","").matches("[\\W|\\D]+")){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El Nit no debe contener caracteres especiales ni dígito de verificación";
                    lista.add(log1);
                }
                if (cellNeocon.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "Neocon no puede estar vacio";
                    lista.add(log1);
                }
                if (cellValor.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "Valor no puede estar vacio";
                    lista.add(log1);
                }
                try {
                    if (Double.parseDouble(cellValor.trim()) == 0) {
                        String[] log1 = new String[3];
                        log1[0] = String.valueOf(row.getRowNum() + 1);
                        log1[1] = CellReference.convertNumToColString(7);
                        log1[2] = "Valor debe ser diferente de 0";
                        lista.add(log1);
                    }
                } catch (Exception e) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El campo valor debe ser numérico";
                    lista.add(log1);
                }
                if (cellYntp.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "Yntp no puede estar vacio";
                    lista.add(log1);
                }
                if (cellCuentaLocal.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "Cuenta Local no puede estar vacia";
                    lista.add(log1);
                }
                if (cellObservaciones.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "Observación no puede estar vacia";
                    lista.add(log1);
                }

                SubsidiariesTemplateTemporal subsidiaries = new SubsidiariesTemplateTemporal();
                subsidiaries.setYntpReportante(user.getEmpresa());
                subsidiaries.setCodNeocon(cellNeocon);
                subsidiaries.setDivisa(cellDivisa);
                subsidiaries.setYntp(cellYntp);
                subsidiaries.setSociedadYntp(cellSociedadYntp);
                subsidiaries.setContrato(cellContrato);
                subsidiaries.setNitContraparte(cellNit);
                try {
                    subsidiaries.setValor(Double.parseDouble(cellValor));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                subsidiaries.setCodPais(cellCodPais);
                subsidiaries.setPais(cellPais);
                subsidiaries.setCuentaLocal(cellCuentaLocal);
                subsidiaries.setObservaciones(cellObservaciones);
                subsidiaries.setPeriodo(period);
                subsidiaries.setUsuario(user.getUsuario());

                toInsert.add(subsidiaries);
            } else {
                firstRow = 0;
            }
        }
        String[] log = new String[3];
        log[2] = stateFinal;
        lista.add(log);

        String[] temp = lista.get(0);

        //Validacion Divisa
        ArrayList<String[]> countryValidation = validateCurrency();
        String[] countryRes = countryValidation.get(0);

        if (temp[2].equals("true") && countryRes[2].equals("true")) {
            lista.clear();
            insertSubsidiariesTemp(toInsert);
            List<Object[]> validation = validateSubsidiaries(user.getEmpresa(), period);
            for (Object[] reg : validation) {
                try {
                    if (reg[3].toString().equals("0")) {
                        String[] log1 = new String[4];
                        log1[0] = reg[0].toString();
                        log1[1] = reg[1].toString();
                        log1[2] = reg[2].toString();
                        log1[3] = "El valor supera el saldo reportado en Fantools";
                        lista.add(log1);
                    }
                } catch (Exception e) {
                    String[] log1 = new String[4];
                    log1[0] = reg[0].toString();
                    log1[1] = reg[1].toString();
                    log1[2] = "No hay valor";
                    log1[3] = "No hay información en Fantools para el periodo " + period;

                    lista.add(log1);
                }
            }
        } else if (!countryRes[2].equals("true") && temp[2].equals("true")) {
            lista.clear();
            lista = countryValidation;
        }

        log[2] = stateFinal;
        lista.add(log);

        String[] temp2 = lista.get(0);
        if (temp2[2].equals("true")) {
            clearRegister(user, period);

            Query getFromTemporal = entityManager.createNativeQuery("SELECT * FROM nexco_plantilla_filiales_temporal", SubsidiariesTemplate.class);
            if (!getFromTemporal.getResultList().isEmpty()) {
                insertSubsidiaries(getFromTemporal.getResultList());
                Query updateTemporal = entityManager.createNativeQuery("UPDATE nexco_plantilla_filiales\n" +
                        "SET sociedad_yntp = B.sociedad_corta\n" +
                        "FROM nexco_plantilla_filiales A, nexco_sociedades_yntp B \n" +
                        "WHERE A.yntp = B.yntp and A.sociedad_yntp != B.sociedad_corta");
                updateTemporal.executeUpdate();
            }
        }

        return lista;
    }

    public ArrayList<String[]> validateCurrency() {
        ArrayList<String[]> result = new ArrayList<>();
        Query validate = entityManager.createNativeQuery("SELECT cuenta_local, fil.divisa, ISNULL(div.id_divisa,'0') AS validacion FROM \n" +
                "(SELECT cuenta_local,divisa FROM nexco_plantilla_filiales_temporal) as fil\n" +
                "LEFT JOIN\n" +
                "(SELECT id_divisa FROM nexco_divisas as div) as div\n" +
                "ON fil.divisa = div.id_divisa");

        List<Object[]> validateRes = validate.getResultList();

        for (Object[] val : validateRes) {
            if (val[2].toString().equals("0")) {
                String[] log = new String[4];
                log[0] = val[0].toString();
                log[1] = val[1].toString();
                log[2] = "El país no existe en la tabla de historico de paises";
                log[3] = "Divisa";
                result.add(log);
            }
        }

        String[] log = new String[4];
        log[2] = "true";
        result.add(log);

        return result;
    }

    public void insertSubsidiariesTemp(List<SubsidiariesTemplateTemporal> bankList) {

        Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_plantilla_filiales_temporal");
        delete.executeUpdate();

        jdbcTemplate.batchUpdate(
                "insert into nexco_plantilla_filiales_temporal (yntp_reportante,cod_neocon, divisa, yntp, sociedad_yntp, contrato,nit_contraparte,valor,cod_pais,pais,cuenta_local, observaciones, periodo,usuario ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, bankList.get(i).getYntpReportante());
                        ps.setString(2, bankList.get(i).getCodNeocon());
                        ps.setString(3, bankList.get(i).getDivisa());
                        ps.setString(4, bankList.get(i).getYntp());
                        ps.setString(5, bankList.get(i).getSociedadYntp());
                        ps.setString(6, bankList.get(i).getContrato());
                        ps.setString(7, bankList.get(i).getNitContraparte());
                        ps.setDouble(8, bankList.get(i).getValor());
                        ps.setString(9, bankList.get(i).getCodPais());
                        ps.setString(10, bankList.get(i).getPais());
                        ps.setString(11, bankList.get(i).getCuentaLocal());
                        ps.setString(12, bankList.get(i).getObservaciones());
                        ps.setString(13, bankList.get(i).getPeriodo());
                        ps.setString(14, bankList.get(i).getUsuario());
                    }

                    public int getBatchSize() {
                        return bankList.size();
                    }
                });
    }

    public void insertSubsidiaries(List<SubsidiariesTemplate> bankList) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_plantilla_filiales (yntp_reportante,cod_neocon, divisa, yntp, sociedad_yntp, contrato,nit_contraparte,valor,cod_pais,pais,cuenta_local, observaciones, periodo,usuario ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, bankList.get(i).getYntpReportante());
                        ps.setString(2, bankList.get(i).getCodNeocon());
                        ps.setString(3, bankList.get(i).getDivisa());
                        ps.setString(4, bankList.get(i).getYntp());
                        ps.setString(5, bankList.get(i).getSociedadYntp());
                        ps.setString(6, bankList.get(i).getContrato());
                        ps.setString(7, bankList.get(i).getNitContraparte());
                        ps.setDouble(8, bankList.get(i).getValor());
                        ps.setString(9, bankList.get(i).getCodPais());
                        ps.setString(10, bankList.get(i).getPais());
                        ps.setString(11, bankList.get(i).getCuentaLocal());
                        ps.setString(12, bankList.get(i).getObservaciones());
                        ps.setString(13, bankList.get(i).getPeriodo());
                        ps.setString(14, bankList.get(i).getUsuario());
                    }

                    public int getBatchSize() {
                        return bankList.size();
                    }
                });
    }

    public void clearRegister(User user, String period) {
        javax.persistence.Query query = entityManager.createNativeQuery("DELETE FROM nexco_plantilla_filiales" +
                " WHERE periodo = ? AND usuario = ?", UserAccount.class);
        query.setParameter(1, period);
        query.setParameter(2, user.getUsuario());
        query.executeUpdate();
    }

    public ArrayList<SubsidiariesTemplate> getActualSubsidiaries(String fecont, User user) {
        ArrayList<SubsidiariesTemplate> result;
        Query getByMonth = entityManager.createNativeQuery("SELECT * FROM nexco_plantilla_filiales WHERE periodo = ? AND yntp_reportante = ?", SubsidiariesTemplate.class);
        getByMonth.setParameter(1, fecont);
        getByMonth.setParameter(2, user.getEmpresa());

        if (!getByMonth.getResultList().isEmpty()) {
            result = (ArrayList<SubsidiariesTemplate>) getByMonth.getResultList();
        } else {
            result = new ArrayList<>();
        }

        return result;
    }

    public boolean sendIntergrupo(String fecont, User user) {

        boolean resp = false;

        ArrayList<SubsidiariesTemplate> result;
        Query getByMonth = entityManager.createNativeQuery("SELECT * FROM nexco_plantilla_filiales WHERE periodo = ? AND yntp_reportante = ?", SubsidiariesTemplate.class);
        getByMonth.setParameter(1, fecont);
        getByMonth.setParameter(2, user.getEmpresa());

        if (!getByMonth.getResultList().isEmpty()) {
            resp = true;
            Query delete = entityManager.createNativeQuery("DELETE FROM nexco_filiales_intergrupo WHERE periodo = ? AND yntp_reportante = ? ");
            delete.setParameter(1, fecont);
            delete.setParameter(2, user.getEmpresa());
            delete.executeUpdate();

            Query insert = entityManager.createNativeQuery("INSERT INTO nexco_filiales_intergrupo (yntp_reportante,cod_neocon,divisa,yntp,sociedad_yntp,contrato,nit_contraparte,valor,cod_pais,pais,cuenta_local,observaciones,periodo,usuario) " +
                    "SELECT yntp_reportante,cod_neocon,divisa,yntp,sociedad_yntp,contrato,nit_contraparte,valor,cod_pais,pais,cuenta_local,observaciones,periodo,usuario FROM nexco_plantilla_filiales WHERE periodo = ? AND yntp_reportante = ? ");

            insert.setParameter(1, fecont);
            insert.setParameter(2, user.getEmpresa());
            insert.executeUpdate();
        }

        return resp;
    }

    public List<Object[]> validateSubsidiaries(String empresa, String periodo) {

        List<Object[]> result;

        HashMap<String, String> centroFilial = new HashMap<>();
        centroFilial.put("00570", "COME");
        centroFilial.put("00561", "FIDU");
        centroFilial.put("00560", "VALO");
        centroFilial.put("00565", "S_GE");
        centroFilial.put("00566", "S_VI");

        String userCenter = centroFilial.get(empresa);
        String fechaMes = periodo.replace("-", "_");

        Query findMonth = entityManager.createNativeQuery("SELECT LOWER(mes) AS mes FROM [82.255.50.134].[DB_FINAN_NUEVA].[dbo].filiales_fechas \n" +
                "WHERE valor = ? AND filial = ?");
        findMonth.setParameter(1, fechaMes);
        findMonth.setParameter(2, userCenter);

        if (!findMonth.getResultList().isEmpty()) {
            String monthForQuery = findMonth.getResultList().get(0).toString();

            Query validateSubsidiaries = entityManager.createNativeQuery("SELECT filiales.cuenta_local, filiales.valor AS 'Valor Filial', s2.valor AS 'Valor S2',\n" +
                    "CASE WHEN s2.valor >= 0\n" +
                    "THEN CASE\n" +
                    "WHEN (filiales.valor+5 <= s2.valor) OR (filiales.valor-5 <= s2.valor)\n" +
                    "THEN 1 ELSE 0 END\n" +
                    "ELSE CASE\n" +
                    "WHEN (filiales.valor+5 >= s2.valor) OR (filiales.valor-5 >= s2.valor)\n" +
                    "THEN 1 ELSE 0 END\n" +
                    "END AS result FROM \n" +
                    "(SELECT cuenta_local, SUM(valor) as valor, divisa FROM nexco_plantilla_filiales_temporal \n" +
                    "WHERE periodo = '" + periodo + "' AND yntp_reportante = '" + empresa + "' \n" +
                    "GROUP BY cuenta_local, divisa) AS filiales \n" +
                    "LEFT JOIN (SELECT cuenta COLLATE SQL_Latin1_General_CP1_CI_AS as cuenta, " + monthForQuery + " as valor, divisa COLLATE SQL_Latin1_General_CP1_CI_AS as divisa FROM [82.255.50.134].[DB_FINAN_NUEVA].[dbo].filiales_s2 \n" +
                    "WHERE filial = '" + userCenter + "' AND tiporegistro IN ('005','001')) AS s2\n" +
                    "ON filiales.cuenta_local = s2.cuenta AND filiales.divisa = s2.divisa");

            if (!validateSubsidiaries.getResultList().isEmpty()) {
                result = validateSubsidiaries.getResultList();
            } else {
                result = new ArrayList<>();
            }

        } else {
            result = new ArrayList<>();
        }

        return result;
    }

}
