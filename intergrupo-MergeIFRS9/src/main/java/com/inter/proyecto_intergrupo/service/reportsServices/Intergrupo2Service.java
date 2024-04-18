package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.bank.IntergrupoV1FinalRepository;
import com.inter.proyecto_intergrupo.repository.bank.IntergrupoV2FinalRepository;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import com.inter.proyecto_intergrupo.repository.reports.ContingentTemplateRepository;
import com.inter.proyecto_intergrupo.repository.temporal.ReclassificationInterDepRepository;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.Part;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional
public class Intergrupo2Service {

    @Autowired
    private ContingentTemplateRepository contingentTemplateRepository;

    @Autowired
    private ReclassificationInterDepRepository reclassificationInterDepRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private ThirdRepository thirdRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private GarantBankRepository garantBankRepository;

    @Autowired
    private YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @Autowired
    IntergrupoV2FinalRepository intergrupoV2FinalRepository;

    @PersistenceContext
    EntityManager entityManager;

    public List<IntergrupoV2> getAllFromV2(String periodo) {
        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE periodo = ?", IntergrupoV2.class);
        result.setParameter(1, periodo);
        List<IntergrupoV2> data = result.getResultList();

        return data;
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Versión 2");
        if(user!=null)
        {
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            insert.setCentro(user.getCentro());
        }
        else
        {
            insert.setNombre("SYSTEM JOB");
        }
        auditRepository.save(insert);
    }

    public void processAjuInterV2(String period){
        Query queryValidate = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v2_def \n" +
                "WHERE periodo = ? ;");
        queryValidate.setParameter(1, period);
        queryValidate.executeUpdate();

        Query result = entityManager.createNativeQuery("Insert into nexco_intergrupo_v2_def (yntp_empresa_reportante,cod_neocon,divisa,yntp,sociedad_yntp,contrato,\n" +
                "nit,valor,cod_pais,pais,cuenta_local,periodo,fuente,input,componente) SELECT base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,case when ajuste.valor is null then base.valor else (base.valor+ajuste.valor) end as valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM (SELECT * FROM nexco_intergrupo_v2 WHERE periodo = ?) AS base LEFT JOIN\n" +
                "(SELECT cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante,sum(valor) as valor FROM nexco_intergrupo_v2_final WHERE periodo = ? \n" +
                "group by cod_neocon,cod_pais,componente,contrato,cuenta_local,divisa,fuente,input,nit,pais,periodo,sociedad_yntp,yntp,yntp_empresa_reportante) AS ajuste ON  \n" +
                "base.cod_neocon=ajuste.cod_neocon and base.cod_pais=ajuste.cod_pais and\n" +
                "base.contrato=ajuste.contrato and base.cuenta_local=ajuste.cuenta_local and base.divisa=ajuste.divisa and\n" +
                "base.nit=ajuste.nit and\n" +
                "base.pais=ajuste.pais and base.periodo=ajuste.periodo and base.sociedad_yntp=ajuste.sociedad_yntp and\n" +
                "base.yntp=ajuste.yntp and base.yntp_empresa_reportante=ajuste.yntp_empresa_reportante ");
        result.setParameter(1, period);
        result.setParameter(2, period);
        result.executeUpdate();

        Query queryValidate2 = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v2_def \n" +
                "WHERE periodo = ? and valor=0;");
        queryValidate2.setParameter(1, period);
        queryValidate2.executeUpdate();
    }

    public List<IntergrupoV2> findByFilter(String value, String filter, String period) {

        ArrayList<IntergrupoV2> toReturn;

        switch (filter) {

            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE cuenta_local LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query.setParameter(1, value);
                query.setParameter(2, period);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query.getResultList();
                }
                break;

            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE contrato LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query1.getResultList();
                }
                break;

            case "Nit":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE nit LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query2.getResultList();
                }
                break;

            case "Cod Neocon":
                Query query3 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE cod_neocon LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query3.setParameter(1, value);
                query3.setParameter(2, period);
                if (query3.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query3.getResultList();
                }
                break;

            case "Divisa":
                Query query4 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE divisa LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period);
                if (query4.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query4.getResultList();
                }
                break;

            case "YNTP":
                Query query5 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE yntp LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query5.setParameter(1, value);
                query5.setParameter(2, period);
                if (query5.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query5.getResultList();
                }
                break;

            case "Sociedad":
                Query query6 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE sociedad_yntp LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query6.setParameter(1, value);
                query6.setParameter(2, period);
                if (query6.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query6.getResultList();
                }
                break;

            case "Cod Pais":
                Query query7 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE cod_pais LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query7.setParameter(1, value);
                query7.setParameter(2, period);
                if (query7.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query7.getResultList();
                }
                break;

            case "Pais":
                Query query8 = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2 WHERE pais LIKE ? and periodo LIKE ?", IntergrupoV2.class);
                query8.setParameter(1, value);
                query8.setParameter(2, period);
                if (query8.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<IntergrupoV2>) query8.getResultList();
                }
                break;

            default:
                toReturn = new ArrayList<>();
        }


        return toReturn;
    }

    public List<String[]> saveFileBD(Collection<Part> parts, User user, String period) throws IOException, InvalidFormatException {

        List<String[]> list = new ArrayList<String[]>();
        List<String[]> finalList = new ArrayList<String[]>();
        List<String> names = new ArrayList<String>();
        String[] listReportNames = new String[4];

        Iterator<Row> rows = null;
        Iterator<Row> rows1 = null;

        for (Part part : parts) {
            InputStream file = part.getInputStream();
            if (part.getSubmittedFileName() != null && file != null) {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                try {
                    XSSFSheet sheet = wb.getSheetAt(1);
                    rows = sheet.iterator();
                    rows1 = sheet.iterator();
                    list = validarPlantilla(rows, user);
                } catch (Exception e) {
                    String[] temp = new String[4];
                    temp[0] = "ERROR_PAGINA";
                    finalList.add(temp);
                    return finalList;
                }

            }
        }
        if (!list.isEmpty()) {
            finalList = Stream.concat(finalList.stream(), list.stream()).collect(Collectors.toList());
            if (list.get(list.size() - 1)[2].equals("0")) {
                //getRows(rows1, user);

                List<Object[]> diff = validateVsIntergroup(period);

                if (diff.size() == 0) {

                    Date today = new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Inserción Documento Depósitos para Intergrupo V2");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("Intergrupo V2");
                    insert.setFecha(today);
                    insert.setInput("Depósitos - Intergrupo V2");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                }
            }
        }
        finalList.add(listReportNames);

        return finalList;
    }

    public List<String[]> validarPlantilla(Iterator<Row> rows, User user) {
        ArrayList<String[]> lista = new ArrayList<String[]>();
        ArrayList<ReclassificationInterDepTemp> listTemp = new ArrayList<ReclassificationInterDepTemp>();
        XSSFRow row;
        int fail = 0;
        int success = 0;
        boolean validation = false;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                String[] log = new String[4];
                log[2] = "true";
                DataFormatter formatter = new DataFormatter();
                String cellContrato = formatter.formatCellValue(row.getCell(0));
                String cellCentro = formatter.formatCellValue(row.getCell(1));
                String cellNit = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCuenta = formatter.formatCellValue(row.getCell(3));
                String cellCuentaNueva = formatter.formatCellValue(row.getCell(4));
                //String cellProducto = formatter.formatCellValue(row.getCell(5));
                //String cellSectorActual = formatter.formatCellValue(row.getCell(6)).trim();
                String cellSaldo = formatter.formatCellValue(row.getCell(7)).replace(" ", "");
                //String cellProductoNuevo = formatter.formatCellValue(row.getCell(8));
                //String cellSectorNuevo = formatter.formatCellValue(row.getCell(9));

                log[0] = String.valueOf(row.getRowNum() + 1);

                try {
                    log[1] = CellReference.convertNumToColString(7) + " - (8)";
                    XSSFCell cell0 = row.getCell(7);
                    cell0.setCellType(CellType.STRING);
                    cellSaldo = formatter.formatCellValue(cell0).replace(" ", "");
                    Double.parseDouble(cellSaldo);
                } catch (Exception e) {
                    fail++;
                    log[2] = "false";
                    log[3] = "El saldo debe ser númerico";
                    lista.add(log);
                    continue;
                }

                if (cellContrato.isEmpty() || cellContrato.isBlank() || cellContrato.length() > 30) {
                    log[1] = CellReference.convertNumToColString(0) + " - (1)";
                    log[2] = "false";
                    log[3] = "El contrato no puede estar vacío";
                    fail++;
                    lista.add(log);
                } else if (cellCentro.isEmpty() || cellCentro.isBlank()) {
                    log[1] = CellReference.convertNumToColString(1) + " - (2)";
                    log[2] = "false";
                    log[3] = "El campo Centro no puede ir vacío";
                    fail++;
                    lista.add(log);
                } else if (cellNit.isEmpty() || cellNit.isBlank()) {
                    log[1] = CellReference.convertNumToColString(2) + " - (3)";
                    log[2] = "false";
                    log[3] = "El campo NIT no puede ir vacío";
                    fail++;
                    lista.add(log);
                } else if (cellCuenta.isEmpty() || cellCuenta.isBlank()) {
                    log[1] = CellReference.convertNumToColString(3) + " - (4)";
                    log[2] = "false";
                    log[3] = "La Cuenta Contable no puede estar vacía";
                    fail++;
                    lista.add(log);
                } else if (cellCuentaNueva.isEmpty() || cellCuentaNueva.isBlank()) {
                    log[1] = CellReference.convertNumToColString(4) + " - (5)";
                    log[2] = "false";
                    log[3] = "La Cuenta Contable Nueva no puede estar vacía";
                    fail++;
                    lista.add(log);
                } else {

                    success++;
                    ReclassificationInterDepTemp RecDep = new ReclassificationInterDepTemp();
                    RecDep.setContrato(cellContrato);
                    RecDep.setCentro(cellCentro);
                    RecDep.setNit(cellNit);
                    RecDep.setCuenta(cellCuenta);
                    RecDep.setCuentaNueva(cellCuentaNueva);
                    RecDep.setSaldo(Double.parseDouble(cellSaldo));

                    listTemp.add(RecDep);
                    log[2] = "true";
                }
            }
        }
        String[] logFinal = new String[4];
        logFinal[0] = "Plantilla";
        logFinal[1] = String.valueOf(success);
        logFinal[2] = String.valueOf(fail);
        logFinal[3] = "true";
        lista.add(logFinal);

        if (fail > 0) {
            Date today = new Date();
            Audit insert = new Audit();
            insert.setAccion("Falló Inserción Documento Depósitos para Intergrupo V2");
            insert.setCentro(user.getCentro());
            insert.setComponente("Intergrupo V2");
            insert.setFecha(today);
            insert.setInput("Depositos - Intergrupo V2");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        } else {
            Query query2 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_reclasificacion_dep_temp;");
            query2.executeUpdate();

            reclassificationInterDepRepository.saveAll(listTemp);
        }

        return lista;
    }

    public void getRows(Iterator<Row> rows, User user) {
        XSSFRow row;
        Date today = new Date();

        Query query2 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_reclasificacion_dep_temp;");
        //query2.setParameter(1, period);
        query2.executeUpdate();

        List<ReclassificationInterDepTemp> listaRec = new ArrayList<ReclassificationInterDepTemp>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellContrato = formatter.formatCellValue(row.getCell(0));
                //String cellCentro = formatter.formatCellValue(row.getCell(1));
                String cellNit = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCuenta = formatter.formatCellValue(row.getCell(3));
                String cellCuentaNueva = formatter.formatCellValue(row.getCell(4));
                //String cellProducto = formatter.formatCellValue(row.getCell(5));
                //String cellSectorActual = formatter.formatCellValue(row.getCell(6)).trim();
                String cellSaldo = formatter.formatCellValue(row.getCell(7)).replace(" ", "");
                //String cellProductoNuevo = formatter.formatCellValue(row.getCell(8));
                //String cellSectorNuevo = formatter.formatCellValue(row.getCell(9));

                ReclassificationInterDepTemp RecDepTemplate = new ReclassificationInterDepTemp();
                RecDepTemplate.setContrato(cellContrato);
                RecDepTemplate.setNit(cellNit);
                RecDepTemplate.setCuenta(cellCuenta);
                RecDepTemplate.setCuentaNueva(cellCuentaNueva);
                RecDepTemplate.setSaldo(Double.parseDouble(cellSaldo));
                listaRec.add(RecDepTemplate);

            }
        }
        reclassificationInterDepRepository.saveAll(listaRec);
    }

    public List<Object[]> validateVsIntergroup(String period) {

        Query query = entityManager.createNativeQuery("select a.contrato, a.cuenta cuenta_mis, b.cuenta_local cuenta_intergrupo, abs(isnull(a.saldo, 0)) valor_mis, \n" +
                "abs(isnull(b.valor, 0)) valor, abs(isnull(b.valor, 0))-abs(isnull(a.saldo, 0)) diferencia \n" +
                "from nexco_reclasificacion_dep_temp a\n" +
                "inner join (select contrato, cuenta_local, valor from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ? \n" +
                "and substring(cuenta_local, 1, 1) = '2') b \n" +
                "on a.contrato = b.contrato and a.cuenta = b.cuenta_local \n" +
                "union all\n" +
                "select a.contrato, a.cuenta cuenta_mis, '' cuenta_intergrupo, isnull(a.saldo, 0) valor_mis, \n" +
                "0 valor, 0-isnull(a.saldo, 0) diferencia \n" +
                "from nexco_reclasificacion_dep_temp a\n" +
                "inner join (select contrato, cuenta_local, valor from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?) b \n" +
                "on a.contrato = b.contrato\n" +
                "where a.contrato not in (select distinct a.contrato\n" +
                "from nexco_reclasificacion_dep_temp a\n" +
                "inner join (select contrato, cuenta_local, valor from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?\n" +
                "and substring(cuenta_local, 1, 1) = '2') b \n" +
                "on a.contrato = b.contrato and a.cuenta = b.cuenta_local)\n" +
                "group by a.contrato, a.cuenta, isnull(a.saldo, 0)\n" +
                ";");
        query.setParameter(1, period);
        query.setParameter(2, period);
        query.setParameter(3, period);
        return query.getResultList();

    }

    public List<Object[]> validateVsIntergroupRest(String period) {

        Query query = entityManager.createNativeQuery("select a.contrato, b.cuenta_local cuenta_intergrupo, \n" +
                "isnull(b.valor, 0) valor \n" +
                "from nexco_reclasificacion_dep_temp a\n" +
                "inner join (select contrato, cuenta_local, valor from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?) b \n" +
                "on a.contrato = b.contrato\n" +
                "where a.contrato not in (select distinct a.contrato\n" +
                "from nexco_reclasificacion_dep_temp a\n" +
                "inner join (select contrato, cuenta_local, valor from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?\n" +
                "and substring(cuenta_local, 1, 1) = '2') b \n" +
                "on a.contrato = b.contrato and a.cuenta = b.cuenta_local)\n" +
                "group by a.contrato, b.cuenta_local, isnull(b.valor, 0)\n" +
                "order by 1, 2\n" +
                ";");
        query.setParameter(1, period);
        query.setParameter(2, period);
        return query.getResultList();

    }

    public void insertfromTempDep(String period) {

        Query deleteInfo = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_dep where periodo = ?");
        deleteInfo.setParameter(1, period);
        deleteInfo.executeUpdate();

        Query query = entityManager.createNativeQuery("insert into nexco_reclasificacion_dep (centro, contrato, cuenta, cuenta_nueva, nit, producto, producto_nuevo, saldo, sector_actual, sector_nuevo, periodo)\n" +
                "select centro, contrato, cuenta, cuenta_nueva, nit, producto, producto_nuevo, saldo, sector_actual, sector_nuevo, ?\n" +
                "from nexco_reclasificacion_dep_temp\n" +
                ";");
        query.setParameter(1, period);
        query.executeUpdate();

    }

    public void insertCargaMasiva(String period) {

        List<Object> days = getHabilDay(period);

        String day1 = days.get(0).toString();
        String day2 = days.get(1).toString();

        Query deleteInfo = entityManager.createNativeQuery("DELETE FROM nexco_carga_masiva_intergrupo_v2 where origen_info = 'DEP' and periodo_origen = ?");
        deleteInfo.setParameter(1, period);
        deleteInfo.executeUpdate();

        Query deleteInfo1 = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_all where origen_info = 'DEP' and periodo = ?");
        deleteInfo1.setParameter(1, period);
        deleteInfo1.executeUpdate();

        Query insertRecla0 = entityManager.createNativeQuery("insert into nexco_reclasificacion_all (centro, contrato, cuenta, cuenta_nueva, nit, origen_info, saldo, yntp, periodo)\n" +
                "select centro, contrato, cuenta, cuenta_nueva, nit, 'DEP', saldo, '' yntp, periodo\n" +
                "from nexco_reclasificacion_dep where periodo = ? \n" +
                ";");
        insertRecla0.setParameter(1, period);
        insertRecla0.executeUpdate();

        Query insertRecla = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select a.centro, a.cuenta, 'COP' divisa, a.contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(a.saldo, 0) importe, 'AJT_REC_DEPOSITOS' descripcion, \n" +
                "? fecha, \n" +
                "'3' td, left(a.nit, 9) nit, right(a.nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, ? periodo_origen, 'REC' tipo_info, 'DEP' origen_info\n" +
                "from nexco_reclasificacion_dep a \n" +
                "where a.periodo = ?;");

        insertRecla.setParameter(1, day1);
        insertRecla.setParameter(2, day1);
        insertRecla.setParameter(3, period);
        insertRecla.setParameter(4, period);
        insertRecla.executeUpdate();

        Query insertRecla2 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select a.centro, a.cuenta_nueva, 'COP' divisa, a.contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(a.saldo, 0)*(-1) importe, 'REC_DEPOSITOS' descripcion, \n" +
                "? fecha, \n" +
                "'3' td, left(a.nit, 9) nit, right(a.nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, ? periodo_origen, 'REC' tipo_info, 'DEP' origen_info\n" +
                "from nexco_reclasificacion_dep a\n" +
                "where a.periodo = ?;");

        insertRecla2.setParameter(1, day1);
        insertRecla2.setParameter(2, day1);
        insertRecla2.setParameter(3, period);
        insertRecla2.setParameter(4, period);
        insertRecla2.executeUpdate();

        Query insertCMRev = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "SELECT centro_costos, cuenta, divisa, contrato, referencia_cruce, importe*(-1) importe, 'DEV_REC_DEPOSITOS' descripcion, ?, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, ?, 'REV', origen_info\n" +
                "FROM nexco_carga_masiva_intergrupo_v2 where origen_info = 'DEP' and periodo_origen = ? \n" +
                ";");
        insertCMRev.setParameter(1, day2);
        insertCMRev.setParameter(2, period);
        insertCMRev.setParameter(3, period);
        insertCMRev.executeUpdate();

    }

    public void insertCargaMasivaCartera(String period) {

        List<Object> days = getHabilDay(period);

        String day1 = days.get(0).toString();
        String day2 = days.get(1).toString();

        Query deleteInfo = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_car where periodo = ?");
        deleteInfo.setParameter(1, period);
        deleteInfo.executeUpdate();

        Query deleteInfo4 = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_gar where periodo = ?");
        deleteInfo4.setParameter(1, period);
        deleteInfo4.executeUpdate();

        Query deleteInfo2 = entityManager.createNativeQuery("DELETE FROM nexco_carga_masiva_intergrupo_v2 where origen_info = 'CAR' and periodo_origen = ?");
        deleteInfo2.setParameter(1, period);
        deleteInfo2.executeUpdate();

        Query deleteInfo1 = entityManager.createNativeQuery("DELETE FROM nexco_reclasificacion_all where origen_info IN ('CAR','GAR') and periodo = ?");
        deleteInfo1.setParameter(1, period);
        deleteInfo1.executeUpdate();

        Query insertCM = entityManager.createNativeQuery("insert into nexco_reclasificacion_car (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, tipo_sociedad, tipo_sociedad_par, producto, tipo, stage, cuenta_nueva)\n" +
                "select x.*, w.cuenta cuenta_nueva from \n" +
                "(select cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, \n" +
                "yntp, yntp_empresa_reportante, tipo_entidad, z.tipo_sociedad tipo_entidad_par, \n" +
                "z.producto, z.tipo, z.stage \n" +
                "from (select a.*, b.tipo_entidad from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a \n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where lower(a.componente) in ('cartera', 'provisiones','renta fija')\n" +
                "and a.periodo = ?\n" +
                "and substring(a.contrato, 9, 2) <> '50'\n" +
                ") y\n" +
                "inner join (select distinct concepto, codicons, tipo_sociedad, producto, tipo, stage\n" +
                "from nexco_reclasificacion_intergrupo_v2 where (cuenta_contrapartida is null or cuenta_contrapartida = '')) z\n" +
                "on y.cod_neocon = z.codicons and substring(y.cuenta_local, 1, 2) = concepto\n" +
                "where y.tipo_entidad <> z.tipo_sociedad) x \n" +
                "inner join nexco_reclasificacion_intergrupo_v2 w \n" +
                "on x.producto = w.producto and x.tipo = w.tipo and x.stage = w.stage and x.tipo_entidad = w.tipo_sociedad and substring(x.cuenta_local, 1, 2) = w.concepto\n" +
                "union all\n" +
                "select x.*, w.cuenta cuenta_nueva from \n" +
                "(select cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, \n" +
                "yntp, yntp_empresa_reportante, tipo_entidad, z.tipo_sociedad tipo_entidad_par, \n" +
                "'Tarjetas de Crédito' producto, z.tipo, z.stage \n" +
                "from (select a.*, b.tipo_entidad from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a \n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where lower(a.componente) in ('cartera', 'provisiones','renta fija')\n" +
                "and a.periodo = ? \n" +
                "and substring(a.contrato, 9, 2) = '50'\n" +
                ") y\n" +
                "inner join (select distinct concepto, codicons, tipo_sociedad, producto, tipo, stage\n" +
                "from nexco_reclasificacion_intergrupo_v2 where (cuenta_contrapartida is null or cuenta_contrapartida = '')) z\n" +
                "on y.cod_neocon = z.codicons and substring(y.cuenta_local, 1, 2) = concepto\n" +
                "where y.tipo_entidad <> z.tipo_sociedad or z.producto <> 'Tarjetas de Crédito') x \n" +
                "inner join nexco_reclasificacion_intergrupo_v2 w \n" +
                "on x.producto = w.producto and x.tipo = w.tipo and x.stage = w.stage and x.tipo_entidad = w.tipo_sociedad and substring(x.cuenta_local, 1, 2) = w.concepto\n" +
                ";");
        insertCM.setParameter(1, period);
        insertCM.setParameter(2, period);
        insertCM.executeUpdate();

        Query insertRecla = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select substring(contrato, 5, 4) centro, cuenta_local, divisa, contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(valor, 0)*(-1) importe, 'REC_CARTERA' descripcion, \n" +
                "? fecha, \n" +
                "left(nit, 1) td, nit, right(nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, \n" +
                "? periodo_origen, \n" +
                "'REC' tipo_info, 'CAR' origen_info\n" +
                "from nexco_reclasificacion_car \n" +
                "where periodo = ? \n" +
                ";");
        insertRecla.setParameter(1, day1);
        insertRecla.setParameter(2, day1);
        insertRecla.setParameter(3, period);
        insertRecla.setParameter(4, period);
        insertRecla.executeUpdate();

        Query insertRecla2 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select substring(contrato, 5, 4) centro, cuenta_nueva, divisa, contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(valor, 0) importe, 'AJT_REC_CARTERA' descripcion, \n" +
                "? fecha, \n" +
                "left(nit, 1) td, nit, right(nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, \n" +
                "? periodo_origen, \n" +
                "'REC' tipo_info, 'CAR' origen_info\n" +
                "from nexco_reclasificacion_car \n" +
                "where periodo = ?\n" +
                ";");
        insertRecla2.setParameter(1, day1);
        insertRecla2.setParameter(2, day1);
        insertRecla2.setParameter(3, period);
        insertRecla2.setParameter(4, period);
        insertRecla2.executeUpdate();

        Query insertCMRev = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "SELECT centro_costos, cuenta, divisa, contrato, referencia_cruce, importe*(-1) importe, 'DEV_REC_CARTERA' descripcion, ?, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, ?, 'REV', origen_info\n" +
                "FROM nexco_carga_masiva_intergrupo_v2 where origen_info = 'CAR' and periodo_origen = ? \n" +
                ";");
        insertCMRev.setParameter(1, day2);
        insertCMRev.setParameter(2, period);
        insertCMRev.setParameter(3, period);
        insertCMRev.executeUpdate();


        // Garantias

        Query deleteInfo3 = entityManager.createNativeQuery("DELETE FROM nexco_carga_masiva_intergrupo_v2 where origen_info = 'GAR' and periodo_origen = ?");
        deleteInfo3.setParameter(1, period);
        deleteInfo3.executeUpdate();

        Query insertCM2 = entityManager.createNativeQuery("insert into nexco_reclasificacion_gar (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, tipo_sociedad, tipo_sociedad_par, producto, tipo, stage, cuenta_nueva, cuenta_contrapartida)\n" +
                "select x.*, w.cuenta cuenta_nueva, w.cuenta_contrapartida from \n" +
                "(select cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, tipo_entidad, z.tipo_sociedad tipo_entidad_par, z.producto, z.tipo, z.stage from (select a.*, b.tipo_entidad from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a \n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where lower(a.componente) in ('cartera', 'provisiones')\n" +
                "and a.periodo = ?\n" +
                ") y\n" +
                "inner join (select distinct concepto, codicons, tipo_sociedad, producto, tipo, stage\n" +
                "from nexco_reclasificacion_intergrupo_v2 where cuenta_contrapartida <> '') z\n" +
                "on y.cod_neocon = z.codicons and substring(y.cuenta_local, 1, 2) = concepto\n" +
                "where y.tipo_entidad <> z.tipo_sociedad) x\n" +
                "inner join nexco_reclasificacion_intergrupo_v2 w\n" +
                "on x.stage = w.stage and x.tipo_entidad = w.tipo_sociedad and substring(x.cuenta_local, 1, 2) = w.concepto\n" +
                ";");
        insertCM2.setParameter(1, period);
        insertCM2.executeUpdate();

        Query insertRecla4 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select substring(contrato, 5, 4) centro, cuenta_contrapartida, divisa, contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(valor, 0) importe, 'REC_GARANTIAS' descripcion, \n" +
                "? fecha, \n" +
                "left(nit, 1) td, nit, right(nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, \n" +
                "? periodo_origen, \n" +
                "'REC' tipo_info, 'GAR' origen_info\n" +
                "from nexco_reclasificacion_gar \n" +
                "where periodo = ?\n" +
                ";");
        insertRecla4.setParameter(1, day1);
        insertRecla4.setParameter(2, day1);
        insertRecla4.setParameter(3, period);
        insertRecla4.setParameter(4, period);
        insertRecla4.executeUpdate();

        Query insertRecla5 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select substring(contrato, 5, 4) centro, cuenta_local, divisa, contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(valor, 0)*(-1) importe, 'REC_GARANTIAS' descripcion, \n" +
                "? fecha, \n" +
                "left(nit, 1) td, nit, right(nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, \n" +
                "? periodo_origen, \n" +
                "'REC' tipo_info, 'GAR' origen_info\n" +
                "from nexco_reclasificacion_gar \n" +
                "where periodo = ?\n" +
                ";");
        insertRecla5.setParameter(1, day1);
        insertRecla5.setParameter(2, day1);
        insertRecla5.setParameter(3, period);
        insertRecla5.setParameter(4, period);
        insertRecla5.executeUpdate();

        Query insertRecla6 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select substring(contrato, 5, 4) centro, cuenta_nueva, divisa, contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(valor, 0) importe, 'AJT_REC_GARANTIAS' descripcion, \n" +
                "? fecha, \n" +
                "left(nit, 1) td, nit, right(nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, \n" +
                "? periodo_origen, \n" +
                "'REC' tipo_info, 'GAR' origen_info\n" +
                "from nexco_reclasificacion_gar \n" +
                "where periodo = ?\n" +
                ";");
        insertRecla6.setParameter(1, day1);
        insertRecla6.setParameter(2, day1);
        insertRecla6.setParameter(3, period);
        insertRecla6.setParameter(4, period);
        insertRecla6.executeUpdate();

        Query insertRecla7 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td, numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "select substring(contrato, 5, 4) centro, cuenta_contrapartida, divisa, contrato, \n" +
                "? referencia_de_cruce,\n" +
                "isnull(valor, 0)*(-1) importe, 'AJT_REC_GARANTIAS' descripcion, \n" +
                "? fecha, \n" +
                "left(nit, 1) td, nit, right(nit, 1) dv, '' tipo_perdida, '' clase_riesgo, '' tipo_movimiento, \n" +
                "'' producto, '' proceso, '' linea_operativa, \n" +
                "? periodo_origen, \n" +
                "'REC' tipo_info, 'GAR' origen_info\n" +
                "from nexco_reclasificacion_gar \n" +
                "where periodo = ?\n" +
                ";");
        insertRecla7.setParameter(1, day1);
        insertRecla7.setParameter(2, day1);
        insertRecla7.setParameter(3, period);
        insertRecla7.setParameter(4, period);
        insertRecla7.executeUpdate();

        Query insertCMRev1 = entityManager.createNativeQuery("insert into nexco_carga_masiva_intergrupo_v2 (centro_costos, cuenta, divisa, contrato, referencia_cruce, importe, descripcion, fecha, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, periodo_origen, tipo_info, origen_info)\n" +
                "SELECT centro_costos, cuenta, divisa, contrato, referencia_cruce, importe*(-1) importe, 'DEV_REC_GARANTIAS' descripcion, ?, td,\n" +
                "numero_documento, dv, tipo_perdida, clase_riesgo, tipo_movimiento, producto, proceso, linea_operativa, ?, 'REV', origen_info\n" +
                "FROM nexco_carga_masiva_intergrupo_v2 where origen_info = 'GAR' and periodo_origen = ? \n" +
                ";");
        insertCMRev1.setParameter(1, day2);
        insertCMRev1.setParameter(2, period);
        insertCMRev1.setParameter(3, period);
        insertCMRev1.executeUpdate();


        //Insert into all

        Query insertRecla0 = entityManager.createNativeQuery("insert into nexco_reclasificacion_all (centro, contrato, cuenta, cuenta_nueva, nit, origen_info, saldo, yntp, periodo)\n" +
                "select SUBSTRING(contrato, 5, 4) centro, contrato, cuenta_local, cuenta_nueva, nit, 'CAR', valor, yntp, periodo\n" +
                "from nexco_reclasificacion_car where periodo = ?\n" +
                ";");
        insertRecla0.setParameter(1, period);
        insertRecla0.executeUpdate();

        Query insertRecla00 = entityManager.createNativeQuery("insert into nexco_reclasificacion_all (centro, contrato, cuenta, cuenta_nueva, nit, origen_info, saldo, yntp, periodo)\n" +
                "select SUBSTRING(contrato, 5, 4) centro, contrato, cuenta_local, cuenta_nueva, nit, 'GAR', valor, yntp, periodo\n" +
                "from nexco_reclasificacion_gar where periodo = ?\n" +
                ";");
        insertRecla00.setParameter(1, period);
        insertRecla00.executeUpdate();

    }

    public List<Object[]> getReclasficacionCSV(Object rec, Object rev, Object dep, Object car, String period) {

        String where = "";
        if (rec != null && rev == null) {
            where = where + " and tipo_info='REC'";
        } else if (rec == null && rev != null) {
            where = where + " and tipo_info='REV'";
        }

        String where2 = "";
        if (dep != null && car == null) {
            where2 = where2 + " and origen_info='DEP'";
        } else if (dep == null && car != null) {
            where2 = where2 + " and origen_info in ('CAR', 'GAR')";
        }

        Query cargaMasiva = entityManager.createNativeQuery("select 'CENTRO DE COSTO;CUENTA;DIVISA;CONTRATO;REFERENCIA DE CRUCE;IMPORTE; DESCRIPCION ;FECHA;TIPO DE DOCUMENTO;NUMERO DE DOCUMENTO;DIGITO DE VERIFICACION;TIPO DE PERDIDA;CLASE DE RIESGO;TIPO DE MOVIMIENTO;PRODUCTO;PROCESO;LINEA OPERATIVA;VALOR BASE' \n" +
                "union all\n" +
                "select centro_costos+';'+cuenta+';'+divisa+';'+contrato+';'+convert(varchar, fecha, 112)+';'\n" +
                "+\n" +
                "convert(varchar, FORMAT(ROUND(importe,0),'g', 'de-de'))+ \n" +
                "';'+descripcion+';'+convert(varchar, fecha, 112)+';'+td+';'+numero_documento+';'+dv\n" +
                "+';'+''+';'+''+';'+''+';'+''+';'+''+';'+''+';'+'0,00' \n" +
                "from nexco_carga_masiva_intergrupo_v2 \n" +
                "where periodo_origen = ? and ROUND(importe,0) <> 0 \n" +
                "" + where + " " + where2 + ";");
        cargaMasiva.setParameter(1, period);
        return cargaMasiva.getResultList();
    }

    public List<Object[]> getAllReportResumen(String period, String rec, String rev, String dep, String car) {

        String where = "";
        if (rec.equals("true") && rev.equals("false")) {
            where = where + " and tipo_info='REC'";
        } else if (rec.equals("false") && rev.equals("true")) {
            where = where + " and tipo_info='REV'";
        }

        String where2 = "";
        if (dep.equals("true") && car.equals("false")) {
            where2 = where2 + " and origen_info='DEP'";
        } else if (dep.equals("false") && car.equals("true")) {
            where2 = where2 + " and origen_info in ('CAR', 'GAR')";
        }

        Query insertCM = entityManager.createNativeQuery("select a.centro_costos, b.CODICONS46, a.cuenta, a.divisa, a.contrato, a.importe, a.descripcion, a.numero_documento, substring(convert(varchar, fecha, 120), 1, 10)\n" +
                "from nexco_carga_masiva_intergrupo_v2 a \n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where EMPRESA = '0013') b \n" +
                "on a.cuenta = b.NUCTA \n" +
                "where a.periodo_origen = ? \n" +
                " " + where + " " + where2 + " ;");
        insertCM.setParameter(1, period);
        insertCM.getResultList();

        return insertCM.getResultList();

    }

    public void insertV2(Object dep, Object car, String period, String exclude, User user) {

        String where = "";
        if (dep != null && car == null) {
            where = where + " and origen_info='DEP'";
        } else if (dep == null && car != null) {
            where = where + " and origen_info in ('CAR', 'GAR')";
        } else if (dep != null && car != null) {
            where = where + " and origen_info in ('CAR', 'GAR', 'DEP')";
        } else {
            where = where + " and origen_info not in ('CAR', 'GAR', 'DEP')";
        }

        Query deleteInfo = entityManager.createNativeQuery("DELETE FROM nexco_intergrupo_v2 where periodo = ?");
        deleteInfo.setParameter(1, period);
        deleteInfo.executeUpdate();

        Query insert = entityManager.createNativeQuery("insert into nexco_intergrupo_v2 (cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, \n" +
                "nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante)\n" +
                "select isnull(c.CODICONS46, d.CODICONS46) cod_neocon, a.cod_pais, a.componente, a.contrato, isnull(b.cuenta_nueva, a.cuenta_local) cuenta_local, a.divisa, a.fuente, a.input, \n" +
                "a.nit, a.pais, a.periodo, a.sociedad_yntp, a.valor, a.yntp, a.yntp_empresa_reportante\n" +
                "from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a\n" +
                "left join (select * from nexco_reclasificacion_all where periodo = ? " + where + " ) b\n" +
                "on a.cuenta_local = b.cuenta and a.contrato = b.contrato\n" +
                "left join (select nucta, codicons46 from cuentas_puc where empresa = '0013') c\n" +
                "on b.cuenta_nueva = c.NUCTA\n" +
                "inner join (select nucta, codicons46 from cuentas_puc where empresa = '0013') d\n" +
                "on a.cuenta_local = d.NUCTA \n" +
                "left join nexco_cuentas_neocon e \n" +
                "on isnull(c.CODICONS46, d.CODICONS46) = e.cuenta \n" +
                "where a.periodo = ? and e.grscing not in (" + exclude + ") \n" +
                ";");

        insert.setParameter(1, period);
        insert.setParameter(2, period);
        insert.executeUpdate();

        saveLog(user, "Generación Intergrupo V2");
    }

    public List<Object[]> getAllIntVersions(String period, String version) {

        String table = "";
        if (version.equals("v1")) {
            table = "/*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def";
        } else {
            table = "nexco_intergrupo_v2";
        }

        Query insertCM = entityManager.createNativeQuery("select a.yntp_empresa_reportante, isnull(b.CODICONS46, a.cod_neocon) cod_neocon, a.divisa, d.divisa_neocon, a.yntp, isnull(c.sociedad_corta, a.sociedad_yntp) sociedad_yntp, \n" +
                "a.contrato, a.nit, e.signo, abs(sum(CONVERT(numeric(20),a.valor/1000))) valor_motores, sum(a.valor), a.cod_pais, a.pais, \n" +
                "case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end pais_xx, a.cuenta_local, a.periodo, a.fuente, a.input, a.componente\n" +
                "from (select * from " + table + " where periodo = ?) a\n" +
                "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "left join nexco_sociedades_yntp c\n" +
                "on a.yntp = c.yntp\n" +
                "left join nexco_divisas d\n" +
                "on a.divisa = d.id_divisa\n" +
                "left join nexco_indicadores e\n" +
                "on a.cod_neocon = e.cuenta_neocon\n" +
                "group by a.yntp_empresa_reportante, isnull(b.CODICONS46, a.cod_neocon), a.divisa, d.divisa_neocon, a.yntp, \n" +
                "isnull(c.sociedad_corta, a.sociedad_yntp), a.contrato, a.nit, e.signo, a.cod_pais, a.pais,\n" +
                "a.cuenta_local, a.periodo, a.fuente, a.input, a.componente;");
        insertCM.setParameter(1, period);
        insertCM.getResultList();

        return insertCM.getResultList();

    }

    public List<Object[]> getAllContractVersion(String period) {

        Query queryV1 = entityManager.createNativeQuery("SELECT a.contrato FROM nexco_intergrupo_v2 a \n" +
                "INNER JOIN (SELECT * FROM nexco_provisiones)b ON a.cod_neocon = b.cuenta_neocon \n" +
                "WHERE a.periodo = ? AND a.contrato !='' GROUP BY a.contrato ORDER BY a.contrato");
        queryV1.setParameter(1, period);
        return queryV1.getResultList();
    }

    public List<Object[]> getAllIntVersionsPlain(String version, String period) {

        if (version.equals("v1")) {

            Query queryV1 = entityManager.createNativeQuery("select replace(A.periodo, '-', '')+isnull(a.yntp_empresa_reportante, '00548')+isnull(b.CODICONS46, '     ')+isnull(d.divisa_neocon, '   ')+'XX'+isnull(a.yntp, '     ')\n" +
                    "+replicate('0', 18 - len(convert(varchar, abs(sum(round(CONVERT(numeric(20),a.valor/1000), 0)))))) + convert(varchar, abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))))\n" +
                    "+isnull(e.signo, ' ')+case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end\n" +
                    "from (select * from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?) a\n" +
                    "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                    "on a.cuenta_local = b.NUCTA\n" +
                    "left join nexco_sociedades_yntp c\n" +
                    "on a.yntp = c.yntp\n" +
                    "left join nexco_divisas d\n" +
                    "on a.divisa = d.id_divisa\n" +
                    "left join nexco_indicadores e\n" +
                    "on a.cod_neocon = e.cuenta_neocon\n" +
                    "left join nexco_cuentas_neocon f \n" +
                    "on a.cod_neocon = f.cuenta \n" +
                    "where f.grscing not in ('', 'NA') \n" +
                    "group by a.yntp_empresa_reportante, b.CODICONS46, \n" +
                    "d.divisa_neocon, a.yntp, e.signo, case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end, a.periodo \n" +
                    "having abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) != 0");
            queryV1.setParameter(1, period);
            queryV1.getResultList();

            return queryV1.getResultList();

        } else {
            /*Query queryV2 = entityManager.createNativeQuery("select replace(A.periodo, '-', '')+isnull(a.yntp_empresa_reportante, '00548')+isnull(b.CODICONS46, '     ')+isnull(d.divisa_neocon, '   ')+'XX'+isnull(a.yntp, '     ')\n" +
                    "+replicate('0', 18 - len(convert(varchar, abs(sum(round(CONVERT(numeric(20),a.valor/1000), 0)))))) + convert(varchar, abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))))\n" +
                    "+isnull(e.signo, ' ')+case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end\n" +
                    "from (select * from nexco_intergrupo_v2_def where periodo = ?) a\n" +
                    "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                    "on a.cuenta_local = b.NUCTA\n" +
                    "left join nexco_sociedades_yntp c\n" +
                    "on a.yntp = c.yntp\n" +
                    "left join nexco_divisas d\n" +
                    "on a.divisa = d.id_divisa\n" +
                    "left join nexco_indicadores e\n" +
                    "on a.cod_neocon = e.cuenta_neocon\n" +
                    "left join nexco_cuentas_neocon f \n" +
                    "on a.cod_neocon = f.cuenta \n" +
                    "where f.grscing not in ('', 'NA') \n" +
                    "group by a.yntp_empresa_reportante, b.CODICONS46, \n" +
                    "d.divisa_neocon, a.yntp, e.signo, case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end, a.periodo \n" +
                    "having abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) != 0");*/
            Query queryV2 = entityManager.createNativeQuery("select replace(A.periodo, '-', '')+isnull(a.yntp_empresa_reportante, '00548')+isnull(b.CODICONS46, '     ')+isnull(d.divisa_neocon, '   ')+'XX'+isnull(a.yntp, '     ')\n" +
                    "+replicate('0', 18 - len(convert(varchar, abs(round(CONVERT(numeric(20),sum(a.valor)/1000), 0))))) + convert(varchar, abs(round(CONVERT(numeric(20), sum(a.valor)/1000), 0)))\n" +
                    "+isnull(e.signo, ' ')+case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end\n" +
                    "from (select * from nexco_intergrupo_v2_def where periodo = ?) a\n" +
                    "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                    "on a.cuenta_local = b.NUCTA\n" +
                    "left join nexco_sociedades_yntp c\n" +
                    "on a.yntp = c.yntp\n" +
                    "left join nexco_divisas d\n" +
                    "on a.divisa = d.id_divisa\n" +
                    "left join nexco_indicadores e\n" +
                    "on a.cod_neocon = e.cuenta_neocon\n" +
                    "left join nexco_cuentas_neocon f \n" +
                    "on a.cod_neocon = f.cuenta \n" +
                    "where f.grscing not in ('', 'NA') \n" +
                    "group by a.yntp_empresa_reportante, b.CODICONS46, \n" +
                    "d.divisa_neocon, a.yntp, e.signo, case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end, a.periodo \n" +
                    "having abs(round(CONVERT(numeric(20), sum(a.valor)/1000), 0)) != 0");
            queryV2.setParameter(1, period);
            queryV2.getResultList();

            return queryV2.getResultList();

        }

    }

    public List<Object[]> getErrorsIntVersionsPlain(String period) {

        Query queryV1 = entityManager.createNativeQuery("select distinct a.yntp codigo, 'El YNTP debe ser dado de alta en parametría de Sociedades' mensaje\n" +
                "from (select * from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?) a\n" +
                "left join nexco_sociedades_yntp c\n" +
                "on a.yntp = c.yntp\n" +
                "where c.yntp is null\n" +
                "union all\n" +
                "select a.divisa codigo, 'La Divisa debe ser dada de alta en parametría de Divisas' mensaje\n" +
                "from (select * from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def where periodo = ?) a\n" +
                "left join nexco_divisas d\n" +
                "on a.divisa = d.id_divisa\n" +
                "where d.id_divisa is null\n" +
                "union all\n" +
                "select distinct a.cod_neocon codigo, 'El Código Neocon debe ser dado de alta en parametría de Indicadores de Cuentas' mensaje\n" +
                "from (select * from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a inner join nexco_cuentas_neocon b on a.cod_neocon = b.cuenta where a.periodo = ? and b.grscing not in ('', 'NA')) a\n" +
                "left join nexco_indicadores e\n" +
                "on a.cod_neocon = e.cuenta_neocon\n" +
                "where e.cuenta_neocon is null\n" +
                ";");
        queryV1.setParameter(1, period);
        queryV1.setParameter(2, period);
        queryV1.setParameter(3, period);
        queryV1.getResultList();

        return queryV1.getResultList();

    }

    public List<Object[]> findByFilterVersions(String value, String filter, String period, String version) {

        ArrayList<Object[]> toReturn;

        String table = "";
        if (version == "v1") {
            table = "/*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def";
        } else {
            table = "nexco_intergrupo_v2";
        }

        switch (filter) {

            case "Cuenta":
                Query query = entityManager.createNativeQuery("select isnull(b.CODICONS46, a.cod_neocon) cod_neocon, a.cod_pais, a.componente, a.contrato, a.cuenta_local, a.divisa, d.divisa_neocon, a.fuente, a.input, a.nit, a.pais, isnull(c.sociedad_corta, a.sociedad_yntp) sociedad_yntp, e.signo, CONVERT(numeric(20),abs(a.valor)/1000), a.yntp, a.yntp_empresa_reportante \n" +
                        "from (select * from " + table + " where periodo = ?) a\n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join nexco_sociedades_yntp c\n" +
                        "on a.yntp = c.yntp\n" +
                        "left join nexco_divisas d\n" +
                        "on a.divisa = d.id_divisa\n" +
                        "left join nexco_indicadores e\n" +
                        "on a.cod_neocon = e.cuenta_neocon\n" +
                        "where a.cuenta_local like ?\n" +
                        ";");
                query.setParameter(1, period);
                query.setParameter(2, value);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Object[]>) query.getResultList();
                }
                break;

            case "Contrato":
                Query query1 = entityManager.createNativeQuery("select isnull(b.CODICONS46, a.cod_neocon) cod_neocon, a.cod_pais, a.componente, a.contrato, a.cuenta_local, a.divisa, d.divisa_neocon, a.fuente, a.input, a.nit, a.pais, isnull(c.sociedad_corta, a.sociedad_yntp) sociedad_yntp, e.signo, CONVERT(numeric(20),abs(a.valor)/1000), a.yntp, a.yntp_empresa_reportante \n" +
                        "from (select * from " + table + " where periodo = ?) a\n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join nexco_sociedades_yntp c\n" +
                        "on a.yntp = c.yntp\n" +
                        "left join nexco_divisas d\n" +
                        "on a.divisa = d.id_divisa\n" +
                        "left join nexco_indicadores e\n" +
                        "on a.cod_neocon = e.cuenta_neocon\n" +
                        "where a.contrato like ?;");
                query1.setParameter(1, period);
                query1.setParameter(2, value);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Object[]>) query1.getResultList();
                }
                break;

            case "Nit":
                Query query2 = entityManager.createNativeQuery("select isnull(b.CODICONS46, a.cod_neocon) cod_neocon, a.cod_pais, a.componente, a.contrato, a.cuenta_local, a.divisa, d.divisa_neocon, a.fuente, a.input, a.nit, a.pais, isnull(c.sociedad_corta, a.sociedad_yntp) sociedad_yntp, e.signo, CONVERT(numeric(20),abs(a.valor)/1000), a.yntp, a.yntp_empresa_reportante \n" +
                        "from (select * from " + table + " where periodo = ?) a\n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join nexco_sociedades_yntp c\n" +
                        "on a.yntp = c.yntp\n" +
                        "left join nexco_divisas d\n" +
                        "on a.divisa = d.id_divisa\n" +
                        "left join nexco_indicadores e\n" +
                        "on a.cod_neocon = e.cuenta_neocon\n" +
                        "where a.nit like ?;");
                query2.setParameter(1, period);
                query2.setParameter(2, value);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Object[]>) query2.getResultList();
                }
                break;

            case "Cod Neocon":
                Query query3 = entityManager.createNativeQuery("select isnull(b.CODICONS46, a.cod_neocon) cod_neocon, a.cod_pais, a.componente, a.contrato, a.cuenta_local, a.divisa, d.divisa_neocon, a.fuente, a.input, a.nit, a.pais, isnull(c.sociedad_corta, a.sociedad_yntp) sociedad_yntp, e.signo, CONVERT(numeric(20),abs(a.valor)/1000), a.yntp, a.yntp_empresa_reportante \n" +
                        "from (select * from " + table + " where periodo = ?) a\n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join nexco_sociedades_yntp c\n" +
                        "on a.yntp = c.yntp\n" +
                        "left join nexco_divisas d\n" +
                        "on a.divisa = d.id_divisa\n" +
                        "left join nexco_indicadores e\n" +
                        "on a.cod_neocon = e.cuenta_neocon\n" +
                        "where isnull(b.CODICONS46, a.cod_neocon) like ?;");
                query3.setParameter(1, period);
                query3.setParameter(2, value);
                if (query3.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Object[]>) query3.getResultList();
                }
                break;

            case "Divisa":
                Query query4 = entityManager.createNativeQuery("select isnull(b.CODICONS46, a.cod_neocon) cod_neocon, a.cod_pais, a.componente, a.contrato, a.cuenta_local, a.divisa, d.divisa_neocon, a.fuente, a.input, a.nit, a.pais, isnull(c.sociedad_corta, a.sociedad_yntp) sociedad_yntp, e.signo, CONVERT(numeric(20),abs(a.valor)/1000), a.yntp, a.yntp_empresa_reportante\n" +
                        "from (select * from " + table + " where periodo = ?) a\n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join nexco_sociedades_yntp c\n" +
                        "on a.yntp = c.yntp\n" +
                        "left join nexco_divisas d\n" +
                        "on a.divisa = d.id_divisa\n" +
                        "left join nexco_indicadores e\n" +
                        "on a.cod_neocon = e.cuenta_neocon\n" +
                        "where a.divisa like ?;");
                query4.setParameter(1, period);
                query4.setParameter(2, value);
                if (query4.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<Object[]>) query4.getResultList();
                }
                break;

            default:
                toReturn = new ArrayList<>();
        }


        return toReturn;
    }

    public List<Object[]> getErrorRecon1(String period) {
        List<Object[]> toReturn = new ArrayList<>();

        Query resultRec = entityManager.createNativeQuery("select cod_neocon, substring(cuenta_local, 1, 2) cuenta_local, 'El Código de Consolidación y Cuenta nivel 2 no están creados en la parametría de Reclasificación' mensaje\n" +
                "from (select a.*, b.tipo_entidad from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a \n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where lower(a.componente) in ('cartera', 'provisiones','renta fija') and a.periodo = ?) y\n" +
                "left join (select distinct concepto, codicons, tipo_sociedad, producto, tipo, stage\n" +
                "from nexco_reclasificacion_intergrupo_v2) z\n" +
                "on y.cod_neocon = z.codicons and substring(y.cuenta_local, 1, 2) = concepto\n" +
                "where z.codicons is null\n" +
                "group by cod_neocon, substring(cuenta_local, 1, 2)\n" +
                "order by 2, 1");

        resultRec.setParameter(1, period);

        if (!resultRec.getResultList().isEmpty()) {
            toReturn = resultRec.getResultList();
        }

        return toReturn;
    }

    public List<Object[]> getErrorRecon2(String period) {
        List<Object[]> toReturn = new ArrayList<>();

        Query resultRec = entityManager.createNativeQuery("select x.cod_neocon, x.cuenta_local, x.tipo_entidad, x.tipo_entidad_par, x.producto, x.tipo, x.stage, 'Combinación faltante en parametría de Reclasificaciones' mensaje\n" +
                "from \n" +
                "(select cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, \n" +
                "yntp, yntp_empresa_reportante, tipo_entidad, z.tipo_sociedad tipo_entidad_par, \n" +
                "case when substring(contrato, 9, 2) = '50' then 'Tarjetas de Crédito' else z.producto end producto, \n" +
                "z.tipo, z.stage \n" +
                "from (select a.*, b.tipo_entidad from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a \n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where lower(a.componente) in ('cartera', 'provisiones','renta fija')\n" +
                "and a.periodo = ? \n" +
                ") y\n" +
                "inner join (select distinct concepto, codicons, tipo_sociedad, producto, tipo, stage\n" +
                "from nexco_reclasificacion_intergrupo_v2 where (cuenta_contrapartida is null or cuenta_contrapartida = '')) z\n" +
                "on y.cod_neocon = z.codicons and substring(y.cuenta_local, 1, 2) = concepto\n" +
                "where y.tipo_entidad <> z.tipo_sociedad) x\n" +
                "left join nexco_reclasificacion_intergrupo_v2 w\n" +
                "on x.producto = w.producto and x.tipo = w.tipo and x.stage = w.stage and x.tipo_entidad = w.tipo_sociedad and substring(x.cuenta_local, 1, 2) = w.concepto\n" +
                "where w.codicons is null\n" +
                "union\n" +
                "select x.cod_neocon, x.cuenta_local, x.tipo_entidad, x.tipo_entidad_par, x.producto, x.tipo, x.stage, 'Combinación faltante en parametría de Reclasificaciones' mensaje\n" +
                "from \n" +
                "(select cod_neocon, cod_pais, componente, contrato, cuenta_local, divisa, fuente, input, nit, pais, periodo, sociedad_yntp, valor, yntp, yntp_empresa_reportante, tipo_entidad, z.tipo_sociedad tipo_entidad_par, z.producto, z.tipo, z.stage from (select a.*, b.tipo_entidad from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a \n" +
                "inner join nexco_sociedades_yntp b\n" +
                "on a.yntp = b.yntp\n" +
                "where lower(a.componente) in ('cartera', 'provisiones','renta fija')\n" +
                "and a.periodo = ? \n" +
                ") y\n" +
                "inner join (select distinct concepto, codicons, tipo_sociedad, producto, tipo, stage\n" +
                "from nexco_reclasificacion_intergrupo_v2 where cuenta_contrapartida <> '') z\n" +
                "on y.cod_neocon = z.codicons and substring(y.cuenta_local, 1, 2) = concepto\n" +
                "where y.tipo_entidad <> z.tipo_sociedad) x\n" +
                "left join nexco_reclasificacion_intergrupo_v2 w\n" +
                "on x.stage = w.stage and x.tipo_entidad = w.tipo_sociedad and substring(x.cuenta_local, 1, 2) = w.concepto\n" +
                "where w.codicons is null\n" +
                ";");

        resultRec.setParameter(1, period);
        resultRec.setParameter(2, period);

        if (!resultRec.getResultList().isEmpty()) {
            toReturn = resultRec.getResultList();
        }

        return toReturn;
    }

    public List validateTableInterV2Aju(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 * FROM nexco_intergrupo_v2_final \n" +
                "WHERE periodo = ? ;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List<IntergrupoV2> getAllFromV2FinalAju(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT base.id_reporte,base.yntp_empresa_reportante,base.cod_neocon,base.divisa,base.yntp,base.sociedad_yntp,base.contrato,\n" +
                "base.nit,base.valor,base.cod_pais,\n" +
                "base.pais,base.cuenta_local,base.periodo,base.fuente,base.input,base.componente\n" +
                "FROM nexco_intergrupo_v2_def as base WHERE periodo = ?", IntergrupoV2.class);
        result.setParameter(1, periodo);
        List<IntergrupoV2> data = result.getResultList();

        return data;
    }

    public List<Object[]> getNeoconInter(String period) {

        Query resultRec = entityManager.createNativeQuery("select distinct b.grscing from /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def a\n" +
                "left join nexco_cuentas_neocon b\n" +
                "on a.cod_neocon = b.cuenta\n" +
                "where a.periodo = ?");
        resultRec.setParameter(1, period);

        return resultRec.getResultList();
    }

    public List<Object> getHabilDay(String period) {

        Query result = entityManager.createNativeQuery("select * from\n" +
                "(select top 1 FechaHabil\n" +
                "from fechas_habiles\n" +
                "where FechaHabil like ?+'%' and DiaHabil <> '0'\n" +
                "order by NumColumn desc) a\n" +
                "union all\n" +
                "select * from\n" +
                "(select top 1 FechaHabil\n" +
                "from fechas_habiles\n" +
                "where FechaHabil like substring(convert(varchar, DATEADD(d, 1, EOMONTH(convert(date, ?+'-01'))), 23), 1, 7)+'%' and DiaHabil <> '0'\n" +
                "order by NumColumn asc) b");
        result.setParameter(1, period);
        result.setParameter(2, period);

        return result.getResultList();
    }

    public List<IntergrupoV2> findIntergrupo2(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT em.id_reporte, em.cod_neocon, em.cod_pais, em.componente, em.contrato, \n" +
                "em.cuenta_local, em.divisa, em.fuente, em.input, em.nit, em.pais, em.periodo, em.sociedad_yntp, convert(numeric(20,2), em.valor) valor, em.yntp, em.yntp_empresa_reportante \n" +
                "FROM nexco_intergrupo_v2 as em \n" +
                "WHERE em.id_reporte = ?", IntergrupoV2.class);

        query.setParameter(1, id);
        return query.getResultList();
    }

    public List<IntergrupoV2> findIntergrupo2Aju(Integer id) {
        Query query = entityManager.createNativeQuery("SELECT em.id_reporte, em.cod_neocon, em.cod_pais, em.componente, em.contrato, \n" +
                "em.cuenta_local, em.divisa, em.fuente, em.input, em.nit, em.pais, em.periodo, em.sociedad_yntp, convert(numeric(20,2), em.valor) valor, em.yntp, em.yntp_empresa_reportante \n" +
                "FROM nexco_intergrupo_v2_def as em \n" +
                "WHERE em.id_reporte = ?", IntergrupoV2.class);

        query.setParameter(1, id);
        return query.getResultList();
    }

    public List<Currency> getDivisas() {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_divisas as em ", Currency.class);

        return query.getResultList();
    }

    public List<Country> getPaises() {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_paises as em ", Country.class);

        return query.getResultList();
    }

    public List<YntpSociety> getYntps() {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_sociedades_yntp as em ", YntpSociety.class);

        return query.getResultList();
    }

    public List<Country> getPais(String id) {
        Query query = entityManager.createNativeQuery("SELECT top 1 em.* FROM nexco_paises as em where em.id_pais = ?", Country.class);
        query.setParameter(1, id);
        return query.getResultList();
    }

    public void modifyIntergrupoV2(IntergrupoV2 toModify, Integer id) {
        IntergrupoV2 toInsert = new IntergrupoV2();
        toInsert.setCodNeocon(toModify.getCodNeocon());
        toInsert.setCodPais(toModify.getCodPais());
        toInsert.setContrato(toModify.getContrato());
        toInsert.setCuentaLocal(toModify.getCuentaLocal());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setNit(toModify.getNit());
        toInsert.setYntp(toModify.getYntp());
        toInsert.setValor(toModify.getValor());

        Country country = countryRepository.findAllById(toInsert.getCodPais());
        YntpSociety yntp = yntpSocietyRepository.findByYntp(toInsert.getYntp());

        Query query = entityManager.createNativeQuery("UPDATE nexco_intergrupo_v2 SET cod_neocon = ? , cod_pais = ? , contrato = ? , cuenta_local = ? , divisa = ? , nit = ? ,\n" +
                "yntp = ? , valor = ? , pais = ? , sociedad_yntp = ? \n" +
                "WHERE id_reporte = ? ", IntergrupoV2.class);
        query.setParameter(1, toInsert.getCodNeocon());
        query.setParameter(2, toInsert.getCodPais());
        query.setParameter(3, toInsert.getContrato());
        query.setParameter(4, toInsert.getCuentaLocal());
        query.setParameter(5, toInsert.getDivisa());
        query.setParameter(6, toInsert.getNit());
        query.setParameter(7, toInsert.getYntp());
        query.setParameter(8, toInsert.getValor());
        query.setParameter(9, country.getNombre());
        query.setParameter(10, yntp.getSociedadDescripcionCorta());
        query.setParameter(11, id);

        try {
            query.executeUpdate();
        } catch (Exception e) {

        }
    }

    public List<Object[]> getCuenta(String cuenta) {
        Query query = entityManager.createNativeQuery("select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013' and nucta = ?");
        query.setParameter(1, cuenta);
        return query.getResultList();
    }

    public List<Object[]> getCodiCons(String codicons) {
        Query query = entityManager.createNativeQuery("select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013' and CODICONS46 = ?");
        query.setParameter(1, codicons);
        return query.getResultList();
    }

    public void addInfo(IntergrupoV2 inter) {
        Query insert = entityManager.createNativeQuery("INSERT INTO nexco_intergrupo_v2 (yntp_empresa_reportante,yntp,periodo,nit,divisa,cuenta_local,contrato,cod_neocon,cod_pais,pais,sociedad_yntp,valor,fuente) " +
                "VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)");

        insert.setParameter(1, inter.getYntpReportante());
        insert.setParameter(2, inter.getYntp());
        insert.setParameter(3, inter.getPeriodo());
        insert.setParameter(4, inter.getNit());
        insert.setParameter(5, inter.getDivisa());
        insert.setParameter(6, inter.getCuentaLocal());
        insert.setParameter(7, inter.getContrato());
        insert.setParameter(8, inter.getCodNeocon());
        insert.setParameter(9, inter.getCodPais());
        insert.setParameter(10, inter.getPais());
        insert.setParameter(11, inter.getSociedadYntp());
        insert.setParameter(12, inter.getValor());
        insert.setParameter(13, inter.getFuente());

        insert.executeUpdate();
    }

    public void saveLog(User user, String accion) {
        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion(accion);
        insert.setCentro(user.getCentro());
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Intergrupo V2 Banco");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<IntergrupoV2Final> getAllFromV2Final(String periodo) {

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_intergrupo_v2_final WHERE periodo = ?", IntergrupoV2Final.class);
        result.setParameter(1, periodo);
        List<IntergrupoV2Final> data = result.getResultList();

        return data;
    }

    public ArrayList<String[]> validarPlantillaFinal(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<IntergrupoV2Final> interList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;

        Query deleteAp1 = entityManager.createNativeQuery("delete from nexco_intergrupo_v2_final where periodo = ?;");
        deleteAp1.setParameter(1,period);
        deleteAp1.executeUpdate();

        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellYNTPEmpresaReportante = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodNeocon = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim();
                String cellYntp = formatter.formatCellValue(row.getCell(3)).trim();
                String cellSociedadYNTP = formatter.formatCellValue(row.getCell(4)).trim();
                String cellContrato = formatter.formatCellValue(row.getCell(5)).trim();
                String cellNITContraparte = formatter.formatCellValue(row.getCell(6)).trim();
                String cellValor = formatter.formatCellValue(row.getCell(7)).trim();
                String cellCodPaís = formatter.formatCellValue(row.getCell(8)).trim();
                String cellPaís = formatter.formatCellValue(row.getCell(9)).trim();
                String cellCuentaLocal = formatter.formatCellValue(row.getCell(10)).trim();
                String cellPeriodo = formatter.formatCellValue(row.getCell(11)).trim();
                String cellFuente = formatter.formatCellValue(row.getCell(12)).trim();
                String cellInput = formatter.formatCellValue(row.getCell(13)).trim();
                String cellComponente = formatter.formatCellValue(row.getCell(14)).trim();

                if (cellYNTPEmpresaReportante.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El YNTP Empresa Reportante debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellCodNeocon.trim().length() !=5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El CodNeocon debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellDivisa.trim().length() !=3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "La Divisa debe estar diligenciado a 3 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellYntp.trim().length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El YNTP debe estar diligenciado a 5 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellSociedadYNTP.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "La Sociedad YNTP no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellContrato.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Contrato no puede estar vacío.";
                    lista.add(log1);
                }*/
                if (cellNITContraparte.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El NIT Contraparte no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCodPaís.trim().length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El Cod País debe estar diligenciado a 2 caracteres de texto.";
                    lista.add(log1);
                }
                if (cellPaís.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(9);
                    log1[2] = "El País no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellCuentaLocal.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(10);
                    log1[2] = "La Cuenta Local no puede estar vacía.";
                    lista.add(log1);
                }
                if (!cellPeriodo.trim().equals(period)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(11);
                    log1[2] = "El periodo reportado no es el mismo al cual se desea cargar.";
                    lista.add(log1);
                }
                if (cellFuente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El Fuente reportada no puede estar vacía.";
                    lista.add(log1);
                }
                /*if (cellInput.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El Input reportado no puede estar vacío.";
                    lista.add(log1);
                }
                if (cellComponente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El Componente reportado no puede estar vacío.";
                    lista.add(log1);
                }*/

                Double valapp;
                try{
                    valapp = !cellValor.isEmpty()?Double.parseDouble(cellValor.replace(" ","").replace(".","").replace(",",".").replace("(","-").replace(")","")):.0;
                }catch (Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El Valor debe ser informado númerico";
                    lista.add(log1);
                    valapp = .0;
                }

                IntergrupoV2Final temporalInter = new IntergrupoV2Final();
                temporalInter.setYntpReportante(cellYNTPEmpresaReportante);
                temporalInter.setCodNeocon(cellCodNeocon);
                temporalInter.setDivisa(cellDivisa);
                temporalInter.setYntp(cellYntp);
                temporalInter.setSociedadYntp(cellSociedadYNTP);
                temporalInter.setContrato(cellContrato);
                temporalInter.setNit(cellNITContraparte);
                temporalInter.setValor(valapp);
                temporalInter.setCodPais(cellCodPaís);
                temporalInter.setPais(cellPaís);
                temporalInter.setCuentaLocal(cellCuentaLocal);
                temporalInter.setPeriodo(period);
                temporalInter.setFuente(cellFuente);
                temporalInter.setInput(cellInput);
                temporalInter.setComponente(cellComponente);
                interList.add(temporalInter);

            } else {
                firstRow = 0;
            }
        }
        String[] log = new String[3];
        log[0] = String.valueOf(interList.size()-lista.size());
        log[1] = String.valueOf(lista.size());
        if(lista.isEmpty())
        {
            intergrupoV2FinalRepository.saveAll(interList);
            log[2] = "COMPLETE";
        }
        else
        {
            log[2] = "FAILED";
        }
        lista.add(log);

        return lista;
    }

    public ArrayList<String[]> saveFileFinalIntergrupo(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantillaFinal(rows, user, period);
            }
            catch (Exception e) {
                String[] error = new String[3];
                error[0] = "0";
                error[1] = "1";
                error[2] = "FAIL";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }
}
