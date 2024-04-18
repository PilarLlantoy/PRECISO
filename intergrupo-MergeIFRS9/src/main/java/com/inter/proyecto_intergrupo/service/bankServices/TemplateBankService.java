package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.TemplateBank;
import com.inter.proyecto_intergrupo.model.parametric.UserAccount;
import com.inter.proyecto_intergrupo.model.temporal.TemplateBankTemporal;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelService;
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
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class TemplateBankService {

    @Autowired
    public ContractRepository contractRepository;

    @Autowired
    public CurrencyRepository currencyRepository;

    @Autowired
    public CountryRepository countryRepository;

    @Autowired
    public YntpSocietyRepository yntpRepository;

    @Autowired
    public ThirdRepository thirdRepository;

    @Autowired
    public ControlPanelService controlPanelService;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    public TemplateBankService(CurrencyRepository currencyRepository, CountryRepository countryRepository, YntpSocietyRepository yntpRepository, ThirdRepository thirdRepository) {
        this.currencyRepository = currencyRepository;
        this.countryRepository = countryRepository;
        this.yntpRepository = yntpRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user, String period) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();

        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? AND estado = 1", ControlPanel.class);
        query1.setParameter(1, "PLANTILLA BANCO");
        query1.setParameter(2, period);
        query1.setParameter(3, user.getCentro());
        List<ControlPanel> listValidate = query1.getResultList();

        if (listValidate.size() > 0) {
            if (listValidate.get(0).getEstado()) {
                if (file != null) {
                    Iterator<Row> rows = null;
                    Iterator<Row> rows1 = null;
                    XSSFWorkbook wb = new XSSFWorkbook(file);
                    XSSFSheet sheet = wb.getSheetAt(0);
                    rows = sheet.iterator();
                    rows1 = sheet.iterator();
                    list = validarPlantilla(rows, rows1, user, period);
                }
            } else {
                String[] log = new String[3];
                log[2] = "PERMISO";
                list.add(log);
            }
        } else {
            String[] log = new String[3];
            log[2] = "PERMISO";
            list.add(log);
        }

        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, Iterator<Row> rows1, User user, String period) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";

        Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_plantilla_carga_temporal");
        delete.executeUpdate();

        ArrayList<TemplateBankTemporal> toInsert = new ArrayList<>();

        while (rows.hasNext()) {
            String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellNeocon = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim();
                String cellyntp = formatter.formatCellValue(row.getCell(3)).trim();
                String cellSociedadYntp = formatter.formatCellValue(row.getCell(4)).trim();
                String cellCONTRATO = formatter.formatCellValue(row.getCell(5)).trim();
                String cellnit = formatter.formatCellValue(row.getCell(6)).trim();
                String cellvalor = formatter.formatCellValue(row.getCell(7)).trim();
                String cellCOD_PAIS = formatter.formatCellValue(row.getCell(8)).trim();
                String cellPais = formatter.formatCellValue(row.getCell(9)).trim();
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(10)).trim();
                String cellObservaciones = formatter.formatCellValue(row.getCell(11)).trim();

                log[0] = String.valueOf(row.getRowNum() + 1);


                try {
                    log[1] = CellReference.convertNumToColString(7) + " - (8)";
                    XSSFCell cell0 = row.getCell(7);
                    cell0.setCellType(CellType.STRING);
                    cellvalor = formatter.formatCellValue(cell0).replace(" ", "");
                    Double.parseDouble(cellvalor);
                    log[1] = CellReference.convertNumToColString(10) + " - (11)";
                    XSSFCell cell1 = row.getCell(10);
                    cell1.setCellType(CellType.STRING);
                    cellCuentaLocal = formatter.formatCellValue(cell1).replace(" ", "");
                    Long.parseLong(cellCuentaLocal);
                    if (cellyntp.trim().length() != 0) {
                        log[1] = CellReference.convertNumToColString(3) + " - (4)";
                        Long.parseLong(cellyntp);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    log[2] = "Tipo dato incorrecto, debe ser númerico";
                    lista.add(log);
                }
                if (cellDivisa.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El campo Divisa no puede estar vacío";
                    lista.add(log1);
                }

                if (cellyntp.trim().length() == 0 && cellnit.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6) + " - " + CellReference.convertNumToColString(2);
                    log1[2] = "Los campos YNTP y NIT no pueden ingresar vacios al tiempo";
                    lista.add(log1);
                }

                if (cellCuentaLocal.trim().length() == 0 || (cellCuentaLocal.length() != 0 && validateCuentaLocal(user, cellCuentaLocal) == 0)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "El campo Cuenta Local no pertenece al centro de su usuario";
                    lista.add(log1);
                }

                if (cellObservaciones.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El campo Observaciones está vacío";
                    lista.add(log1);
                }

                if (cellvalor.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El campo Valor se encuentra vacio";
                    lista.add(log1);
                }

                try{
                if (Double.parseDouble(cellvalor) == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El campo valor no puede ser 0";
                    lista.add(log1);
                }} catch (Exception e){

                }

                TemplateBankTemporal templateBank = new TemplateBankTemporal();

                templateBank.setYntpEmpresa(user.getEmpresa());
                templateBank.setNeocon(cellNeocon);
                templateBank.setDivisa(cellDivisa);
                templateBank.setYntp(cellyntp);
                templateBank.setSociedad_yntp(cellSociedadYntp);
                templateBank.setCONTRATO(cellCONTRATO);
                templateBank.setNit(cellnit);
                try {
                    templateBank.setValor(Double.parseDouble(cellvalor));
                }catch (Exception e){

                }
                templateBank.setCOD_PAIS(cellCOD_PAIS);
                templateBank.setPAIS(cellPais);
                templateBank.setCuentaLocal(cellCuentaLocal);
                templateBank.setObservaciones(cellObservaciones);
                templateBank.setUsuario(user.getUsuario());
                templateBank.setPeriodo(period);
                templateBank.setCentro(user.getCentro());

                toInsert.add(templateBank);

            }

        }
        String[] log2 = new String[3];
        log2[2] = stateFinal;
        lista.add(log2);

        String[] temp = lista.get(0);
        if (temp[2].equals("true")) {
            insertTemplateBankTemp(toInsert);
        }

        toInsert.clear();
        return lista;
    }

    public List<Object[]> validateCodicons(String period, User user) {
        Query validateData = entityManager.createNativeQuery("select a.cod_neocon, case when b.cuenta is null then 'NO EXIST' when b.naturaleza = a.naturaleza then 'SUCCESS' else 'FAILED'  end as estado \n" +
                "from (select cod_neocon,case when valor < 0 then '-' else '+' end as naturaleza from nexco_plantilla_carga_temporal where periodo = ? and usuario = ? group by cod_neocon,case when valor < 0 then '-' else '+' end) a\n" +
                "left join nexco_param_cuenta_banco b on a.cod_neocon = b.cuenta where a.naturaleza != b.naturaleza or b.naturaleza is null ");
        validateData.setParameter(1, period);
        validateData.setParameter(2, user.getUsuario());
        return validateData.getResultList();
    }

    public ArrayList<String[]> validateTemporalAndQuery(String period, User user) {
        ArrayList<String[]> lista = new ArrayList();
        String stateFinal = "true";

        Query validateData = entityManager.createNativeQuery("SELECT a.cuenta_local as cl, ISNULL(a.yntp_empresa,'VACIO') as ynt_emp, ISNULL(a.CODICONS46,'VACIO') as neocon, ISNULL(ter.nit_contraparte,'VACIO') as nit,  ISNULL(yntp.yntp,'VACIO') as yntp, ISNULL(yntp.id_pais,'VACIO')as pais FROM \n" +
                "(SELECT pc.cuenta_local, pc.yntp_empresa, pc.nit , pc.periodo ,puc.CODICONS46 FROM nexco_plantilla_carga_temporal as pc \n" +
                "LEFT JOIN (SELECT puc.NUCTA, puc.CODICONS46 FROM CUENTAS_PUC  as puc WHERE puc.EMPRESA = '0013') as puc ON puc.NUCTA = pc.cuenta_local) as a \n" +
                "LEFT JOIN nexco_terceros as ter ON ter.nit_contraparte = a.nit \n" +
                "LEFT JOIN nexco_sociedades_yntp as yntp ON ter.yntp = yntp.yntp \n" +
                "WHERE a.periodo = ? ");
        validateData.setParameter(1, period);

        List<Object[]> validateResult = validateData.getResultList();

        if (!validateResult.isEmpty()) {
            for (Object[] res : validateResult) {
                if (res[1].toString().equals("VACIO")) {
                    String[] log1 = new String[3];
                    log1[0] = res[0].toString();
                    log1[1] = "El campo Yntp empresa reportante está vacio";
                    log1[2] = "false";
                    lista.add(log1);
                }
                if (res[2].toString().equals("VACIO")) {
                    String[] log1 = new String[3];
                    log1[0] = res[0].toString();
                    log1[1] = "Codigo neocón no encontrado en cuentas PUC";
                    log1[2] = "false";
                    lista.add(log1);
                }
                if (res[3].toString().equals("VACIO")) {
                    String[] log1 = new String[3];
                    log1[0] = res[0].toString();
                    log1[1] = "Nit contraparte no encontrado en Terceros";
                    log1[2] = "false";
                    lista.add(log1);
                }
                if (res[4].toString().equals("VACIO")) {
                    String[] log1 = new String[3];
                    log1[0] = res[0].toString();
                    log1[1] = "Yntp no encontrado en Sociedades YNTP";
                    log1[2] = "false";
                    lista.add(log1);
                }
                if (res[5].toString().equals("VACIO")) {
                    String[] log1 = new String[3];
                    log1[0] = res[0].toString();
                    log1[1] = "País no encontrado en la tabla de Países";
                    log1[2] = "false";
                    lista.add(log1);
                }
            }
        }
        String[] log2 = new String[3];
        log2[2] = stateFinal;
        lista.add(log2);

        String[] temp = lista.get(0);
        if (temp[2].equals("true")) {
            lista.clear();
            try {
                List<Object[]> queryValidation = validateQuery(period);
                for (Object[] val : queryValidation) {
                    try {
                        if (val[3].toString().equals("0")) {
                            String[] log1 = new String[4];
                            log1[0] = val[0].toString();
                            log1[1] = val[1].toString();
                            log1[2] = val[2].toString();
                            log1[3] = "El valor supera el saldo del Query";

                            lista.add(log1);
                        }
                    } catch (Exception e) {
                        String[] log1 = new String[4];
                        log1[0] = val[0].toString();
                        log1[1] = val[1].toString();
                        log1[2] = "No hay valor";
                        log1[3] = "No se encuentra saldo para la cuenta. Revise que la cuenta y la divisa coincidan con el Query";

                        lista.add(log1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log2[2] = stateFinal;
        lista.add(log2);

        Query queryUpload = entityManager.createNativeQuery("SELECT * FROM nexco_plantilla_carga_temporal", TemplateBank.class);
        List<String> listQuery = queryUpload.getResultList();

        temp = lista.get(0);

        if (temp[2].equals("true")) {
            clearRegister(user, period);
            Query getFromTemporal = entityManager.createNativeQuery("SELECT * FROM nexco_plantilla_carga_temporal", TemplateBank.class);

            if (!getFromTemporal.getResultList().isEmpty()) {
                insertTemplateBank(getFromTemporal.getResultList(),user);
            }
        }

        return lista;
    }

    public void insertTemplateBankTemp(List<TemplateBankTemporal> bankList) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_plantilla_carga_temporal (yntp_empresa,cod_neocon, divisa, yntp, sociedad_yntp, contrato,nit,valor,cod_pais,pais,cuenta_local, observaciones, periodo,usuario, centro ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, bankList.get(i).getYntpEmpresa());
                        ps.setString(2, bankList.get(i).getNeocon());
                        ps.setString(3, bankList.get(i).getDivisa());
                        ps.setString(4, bankList.get(i).getYntp());
                        ps.setString(5, bankList.get(i).getSociedad_yntp());
                        ps.setString(6, bankList.get(i).getCONTRATO());
                        ps.setString(7, bankList.get(i).getNit());
                        ps.setDouble(8, bankList.get(i).getValor());
                        ps.setString(9, bankList.get(i).getCOD_PAIS());
                        ps.setString(10, bankList.get(i).getPAIS());
                        ps.setString(11, bankList.get(i).getCuentaLocal());
                        ps.setString(12, bankList.get(i).getObservaciones());
                        ps.setString(13, bankList.get(i).getPeriodo());
                        ps.setString(14, bankList.get(i).getUsuario());
                        ps.setString(15, bankList.get(i).getCentro());
                    }

                    public int getBatchSize() {
                        return bankList.size();
                    }
                });

        /*Query updateTemp = entityManager.createNativeQuery("UPDATE nexco_plantilla_carga_temporal\n" +
                "SET cod_neocon = puc.CODICONS46\n" +
                "FROM\n" +
                "(SELECT puc.NUCTA, puc.CODICONS46 FROM CUENTAS_PUC  as puc WHERE puc.EMPRESA = '0013') AS puc\n" +
                "INNER JOIN nexco_plantilla_carga_temporal as temp ON temp.cuenta_local = puc.NUCTA\n" +
                "\n" +
                "IF EXISTS (SELECT * FROM nexco_plantilla_carga_temporal WHERE yntp = '')\n" +
                "UPDATE nexco_plantilla_carga_temporal\n" +
                "SET yntp = ter.yntp\n" +
                "FROM\n" +
                "nexco_plantilla_carga_temporal as temp\n" +
                "INNER JOIN nexco_terceros as ter ON temp.nit = ter.nit_contraparte\n" +
                "\n" +
                "IF EXISTS (SELECT * FROM nexco_plantilla_carga_temporal WHERE nit = '')\n" +
                "UPDATE nexco_plantilla_carga_temporal\n" +
                "SET nit = ter.nit_contraparte\n" +
                "FROM\n" +
                "nexco_plantilla_carga_temporal as temp\n" +
                "INNER JOIN nexco_terceros as ter ON temp.yntp = ter.yntp");*/

        Query updateTemp = entityManager.createNativeQuery("UPDATE nexco_plantilla_carga_temporal\n" +
                "SET cod_neocon = puc.CODICONS46\n" +
                "FROM\n" +
                "(SELECT puc.NUCTA, puc.CODICONS46 FROM CUENTAS_PUC  as puc WHERE puc.EMPRESA = '0013') AS puc\n" +
                "INNER JOIN nexco_plantilla_carga_temporal as temp ON temp.cuenta_local = puc.NUCTA ;\n" +
                "\n" +
                "UPDATE nexco_plantilla_carga_temporal\n" +
                "SET yntp = ter.yntp\n" +
                "FROM\n" +
                "nexco_plantilla_carga_temporal as temp\n" +
                "INNER JOIN nexco_terceros as ter ON temp.nit = ter.nit_contraparte ;");

        updateTemp.executeUpdate();

    }

    public void insertTemplateBank(List<TemplateBank> bankList,User user) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_plantilla_carga (yntp_empresa,cod_neocon, divisa, yntp, sociedad_yntp, contrato,nit,valor,cod_pais,pais,cuenta_local, observaciones, periodo,usuario, centro ) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, bankList.get(i).getYntpEmpresa());
                        ps.setString(2, bankList.get(i).getNeocon());
                        ps.setString(3, bankList.get(i).getDivisa());
                        ps.setString(4, bankList.get(i).getYntp());
                        ps.setString(5, bankList.get(i).getSociedad_yntp());
                        ps.setString(6, bankList.get(i).getCONTRATO());
                        ps.setString(7, bankList.get(i).getNit());
                        ps.setDouble(8, bankList.get(i).getValor());
                        ps.setString(9, bankList.get(i).getCOD_PAIS());
                        ps.setString(10, bankList.get(i).getPAIS());
                        ps.setString(11, bankList.get(i).getCuentaLocal());
                        ps.setString(12, bankList.get(i).getObservaciones());
                        ps.setString(13, bankList.get(i).getPeriodo());
                        ps.setString(14, bankList.get(i).getUsuario());
                        ps.setString(15, bankList.get(i).getCentro());
                    }

                    public int getBatchSize() {
                        return bankList.size();
                    }
                });

        updateCuadroMando(user,bankList.get(0).getPeriodo());
    }

    public void updateCuadroMando(User user,String period)
    {
        Query updateMando = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando " +
                "SET semaforo_input = 'FULL', usuario_carga = ?, fecha_carga = ? " +
                "WHERE input = 'PLANTILLA BANCO' AND fecha_reporte = ? " +
                "AND responsable IN (SELECT centro FROM nexco_usuarios where usuario = ? " +
                "GROUP BY centro)");
        updateMando.setParameter(1,user.getPrimerNombre());
        updateMando.setParameter(2,new Date());
        updateMando.setParameter(3,period);
        updateMando.setParameter(4,user.getUsuario());
        updateMando.executeUpdate();

        Query selectMandoComp = entityManager.createNativeQuery("SELECT componente FROM nexco_cuadro_mando WHERE input = 'PLANTILLA BANCO' AND fecha_reporte = ? AND responsable = ? GROUP BY componente");
        selectMandoComp.setParameter(1,period);
        selectMandoComp.setParameter(2,user.getCentro());
        List<String> listP = selectMandoComp.getResultList();

        for (String part:listP)
        {

            Query selectMando = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando WHERE input = 'PLANTILLA BANCO' AND semaforo_input != 'FULL' AND fecha_reporte = ? AND componente = ?");
            selectMando.setParameter(1,period);
            selectMando.setParameter(2,part);

            if(selectMando.getResultList().size()==0)
            {
                Query updateMando1 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = 'FULL' " +
                        "WHERE input = 'PLANTILLA BANCO' AND fecha_reporte = ? AND componente = ?");
                updateMando1.setParameter(1,period);
                updateMando1.setParameter(2,part);
                updateMando1.executeUpdate();
            }
            else {
                Query updateMando1 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = 'EMPTY' " +
                        "WHERE input = 'PLANTILLA BANCO' AND fecha_reporte = ? AND componente = ?");
                updateMando1.setParameter(1,period);
                updateMando1.setParameter(2,part);
                updateMando1.executeUpdate();
            }
        }
    }

    public void clearRegister(User user, String period) {

        javax.persistence.Query query = entityManager.createNativeQuery("DELETE FROM nexco_plantilla_carga" +
                " WHERE periodo = ? AND centro = ? ");
        query.setParameter(2, user.getCentro());
        query.setParameter(1, period);
        query.executeUpdate();
    }

    public boolean clearRegisterFront(User user, String period) {
        Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                "WHERE em.input = ? AND em.fecha_reporte = ? AND em.responsable = ? AND estado = 1", ControlPanel.class);
        query1.setParameter(1, "PLANTILLA BANCO");
        query1.setParameter(2, period);
        query1.setParameter(3, user.getCentro());

        List<ControlPanel> listValidate = query1.getResultList();
        if (listValidate.size() > 0) {
            javax.persistence.Query query = entityManager.createNativeQuery("DELETE FROM nexco_plantilla_carga" +
                    " WHERE periodo = ? AND centro = ? ");
            query.setParameter(2, user.getCentro());
            query.setParameter(1, period);
            query.executeUpdate();
            return true;
        }
        return false;
    }

    public int validateCuentaLocal(User user, String cuenta_local) {
        javax.persistence.Query query = entityManager.createNativeQuery("SELECT * FROM nexco_user_account" +
                " WHERE id_usuario = ? AND SUBSTRING(CAST(cuenta_local AS varchar),1,4) = SUBSTRING(?,1,4) ", UserAccount.class);
        query.setParameter(1, user.getUsuario());
        query.setParameter(2, cuenta_local);
        List result = query.getResultList();
        return result.size();
    }

    public List<String[]> findAllTemplates(String id, String period, User user) {

        List<String[]> allTemplates = new ArrayList<>();
        ArrayList<String> months = new ArrayList<>();

        int month = Integer.parseInt(period.substring(5,7)) < 10 ? Integer.parseInt(period.substring(5,7).replace("0","")) :  Integer.parseInt(period.substring(5,7));
        String year = period.substring(0, 4);
        for (int i = month; i > 0; i--) {
            String m;
            if (i < 10) {
                m = year + "-0" + i;
            } else {
                m = year + "-" + i;
            }
            months.add(m);
        }

        String userId = user.getUsuario();
        String userEmpresa = user.getEmpresa();

        Query query = entityManager.createNativeQuery("SELECT '" + userEmpresa + "',nbf.cod_neocon, nbf.divisa, nbf.yntp, nbf.sociedad_yntp, nbf.contrato, nbf.nit_contraparte, SUM(nbf.valor) AS mtm, \n" +
                "nbf.cod_pais, nbf.pais, nbf.cuenta_local,:base AS ob \n" +
                "FROM nexco_base_fiscal AS nbf \n" +
                "INNER JOIN nexco_user_account AS ua\n" +
                "ON nbf.cuenta_local LIKE CAST(ua.cuenta_local AS varchar)+'%' \n" +
                "INNER JOIN (SELECT cuenta_local from nexco_cuentas_responsables WHERE aplica_base_fiscal = 1 AND aplica_mis = 0) as cr \n" +
                "ON nbf.cuenta_local LIKE CAST(cr.cuenta_local AS varchar)+'%'\n" +
                "WHERE ua.id_usuario = :idUsuario AND nbf.periodo IN (:months) \n" +
                "GROUP BY  nbf.cuenta_local, nbf.cod_neocon,nbf.divisa, nbf.yntp, nbf.sociedad_yntp, nbf.contrato, nbf.nit_contraparte, nbf.cod_pais, nbf.pais\n" +
                "ORDER BY cuenta_local");

        query.setParameter("base", "Base Fiscal");
        query.setParameter("idUsuario", userId);
        query.setParameter("months", months);

        if(!query.getResultList().isEmpty()){
            allTemplates.addAll((List<String[]>)query.getResultList());
        }

        Query query2 = entityManager.createNativeQuery("SELECT :empresa,sicc.cod_neocon, sicc.divisa, sicc.yntp, sicc.sociedad_yntp, sicc.contrato, sicc.nit_contraparte, CAST(SUM(CAST(sicc.valor as numeric)) AS varchar) AS mtm, \n" +
                "sicc.cod_pais, sicc.pais, sicc.cuenta_local, :sicc as ob\n" +
                "FROM nexco_sicc AS sicc\n" +
                "INNER JOIN nexco_user_account AS ua\n" +
                "ON sicc.cuenta_local LIKE CAST(ua.cuenta_local AS varchar)+'%' \n" +
                "RIGHT JOIN (SELECT cuenta_local from nexco_cuentas_responsables WHERE aplica_sicc = 1) as cr \n" +
                "ON sicc.cuenta_local LIKE CAST(cr.cuenta_local AS varchar)+'%' \n" +
                "WHERE ua.id_usuario = :user AND sicc.periodo_contable = :period \n" +
                "GROUP BY sicc.cuenta_local,sicc.cod_neocon,sicc.divisa, sicc.yntp, sicc.sociedad_yntp, sicc.contrato, sicc.nit_contraparte, sicc.cod_pais,  sicc.pais");

        query2.setParameter("empresa", userEmpresa);
        query2.setParameter("sicc","SICC");
        query2.setParameter("user",userId);
        query2.setParameter("period",period.replace("-",""));


        if(!query2.getResultList().isEmpty()){
            allTemplates.addAll((List<String[]>)query2.getResultList());
        }


        Query query3 = entityManager.createNativeQuery("SELECT :empresa, mis.cod_neocon, mis.divisa, mis.yntp, mis.sociedad_yntp, mis.contrato, mis.nit_contraparte, SUM(mis.valor) AS mtm, \n" +
                "mis.cod_pais, mis.pais, mis.cuenta_local, :mis AS ob \n" +
                "FROM nexco_mis as mis\n" +
                "INNER JOIN nexco_user_account AS ua\n" +
                "ON mis.cuenta_local LIKE CAST(ua.cuenta_local AS varchar)+'%' \n" +
                "RIGHT JOIN (SELECT cuenta_local from nexco_cuentas_responsables WHERE aplica_mis = 1) as cr \n" +
                "ON mis.cuenta_local LIKE CAST(cr.cuenta_local AS varchar)+'%' \n" +
                "WHERE ua.id_usuario = :user AND mis.periodo_contable = :period \n" +
                "GROUP BY mis.cuenta_local,mis.cod_neocon,mis.divisa, mis.yntp, mis.sociedad_yntp, mis.contrato, mis.nit_contraparte, mis.cod_pais, mis.pais");

        query3.setParameter("empresa", userEmpresa);
        query3.setParameter("mis","MIS");
        query3.setParameter("user",userId);
        query3.setParameter("period",period);

        if(!query3.getResultList().isEmpty()){
            allTemplates.addAll(query3.getResultList());
        }

        SimpleDateFormat formatter6 = new SimpleDateFormat("yyyy-MM-dd");
        Date period2 = null;
        try {
            period2 = formatter6.parse(period + "-01");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return allTemplates;
    }

    public List<String[]> findAllTemplatesPantilla(String id, String period, User user) {

        List<String[]> allTemplates = new ArrayList<String[]>();
        List<String> cuentas_locales_user = obtenerCuentasFiltrado(id);

        for (String cuenta : cuentas_locales_user) {
            javax.persistence.Query query = entityManager.createNativeQuery("SELECT npc.yntp_empresa, npc.cod_neocon, npc.divisa, npc.yntp, npc.sociedad_yntp, npc.contrato, npc.nit, CONVERT(numeric(30,2),npc.valor), npc.cod_pais, npc.pais, npc.cuenta_local, npc.observaciones FROM nexco_plantilla_carga AS npc" +
                    " WHERE SUBSTRING(CAST(npc.cuenta_local AS varchar),1,4) = ? AND npc.periodo = ? AND npc.centro = ?");
            query.setParameter(1, cuenta);
            query.setParameter(2, period);
            query.setParameter(3, user.getCentro());
            allTemplates.addAll(query.getResultList());
        }

        return allTemplates;
    }

    public List<String> obtenerCuentasFiltrado(String id) {
        List<String> cuentas = new ArrayList<String>();
        javax.persistence.Query query = entityManager.createNativeQuery("SELECT * FROM nexco_user_account" +
                " WHERE id_usuario = ? ", UserAccount.class);
        query.setParameter(1, id);
        List<UserAccount> result = query.getResultList();
        for (UserAccount user : result) {
            if (!cuentas.contains(user.getIdCuentaLocal().getCuentaLocal().toString().substring(0, 4)))
                cuentas.add(user.getIdCuentaLocal().getCuentaLocal().toString().substring(0, 4));
        }
        return cuentas;
    }

    public List<Object[]> validateQuery(String fecont) {

        List<Object[]> resultList = new ArrayList<>();

        Query verify = entityManager.createNativeQuery("SELECT TOP 10 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,fecont+"%");

        if(verify.getResultList().isEmpty()){
            Query result = entityManager.createNativeQuery("SELECT banco.cuenta_local , banco.valor as 'Valor Banco', query.valor as 'Valor Query', \n" +
                    "CASE WHEN query.valor <0 and banco.valor >=0\n" +
                    "THEN\n" +
                    "0 " +
                    "WHEN query.valor >= 0 \n" +
                    "THEN CASE \n" +
                    "WHEN (banco.valor+5 < query.valor) OR (banco.valor-5 < query.valor)\n" +
                    "THEN 1 ELSE 0 END\n" +
                    "ELSE CASE\n" +
                    "WHEN (banco.valor+5 >= query.valor) OR (banco.valor-5 >= query.valor)\n" +
                    "THEN 1 ELSE 0\n" +
                    "END\n" +
                    "END AS result FROM" +
                    "(SELECT cuenta_local, SUM(valor) AS valor, divisa FROM nexco_plantilla_carga_temporal where periodo = :fecont \n" +
                    "GROUP BY cuenta_local, divisa) AS banco\n" +
                    "LEFT JOIN (SELECT nucta, saldoquery AS valor, coddiv FROM nexco_query as query WHERE SUBSTRING(fecont,0,8) = :fecont AND empresa = '0013') as query \n" +
                    "ON query.nucta = banco.cuenta_local AND query.coddiv = banco.divisa");

            result.setParameter("fecont", fecont);

            if (!result.getResultList().isEmpty()) {
                resultList = result.getResultList();
            }
        } else{
            Query result = entityManager.createNativeQuery("SELECT banco.cuenta_local , banco.valor as 'Valor Banco', query.valor as 'Valor Query', \n" +
                    "CASE WHEN query.valor <0 and banco.valor >=0\n" +
                    "THEN\n" +
                    "0 " +
                    "WHEN query.valor >= 0 \n" +
                    "THEN CASE \n" +
                    "WHEN (banco.valor+5 < query.valor) OR (banco.valor-5 < query.valor)\n" +
                    "THEN 1 ELSE 0 END\n" +
                    "ELSE CASE\n" +
                    "WHEN (banco.valor+5 >= query.valor) OR (banco.valor-5 >= query.valor)\n" +
                    "THEN 1 ELSE 0\n" +
                    "END\n" +
                    "END AS result FROM" +
                    "(SELECT cuenta_local, SUM(valor) AS valor, divisa FROM nexco_plantilla_carga_temporal where periodo = :fecont \n" +
                    "GROUP BY cuenta_local, divisa) AS banco\n" +
                    "LEFT JOIN (SELECT nucta, saldoquery AS valor, coddiv FROM nexco_query_marcados as query WHERE SUBSTRING(fecont,0,8) = :fecont AND empresa = '0013' AND origen = 'LOCAL') as query \n" +
                    "ON query.nucta = banco.cuenta_local AND query.coddiv = banco.divisa");

            result.setParameter("fecont", fecont);

            if (!result.getResultList().isEmpty()) {
                resultList = result.getResultList();
            }
        }

        return resultList;
    }


}
