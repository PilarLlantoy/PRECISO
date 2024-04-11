package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelJobs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.TaxBase;
import com.inter.proyecto_intergrupo.model.bank.TaxBaseComplete;
import com.inter.proyecto_intergrupo.model.bank.TaxBaseLoad;
import com.inter.proyecto_intergrupo.model.temporal.TaxBaseTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.admin.ControlPanelJobsRepository;
import com.inter.proyecto_intergrupo.repository.bank.TaxBaseLoadRepository;
import com.inter.proyecto_intergrupo.repository.bank.TaxBaseRepository;
import com.inter.proyecto_intergrupo.repository.temporal.TaxBaseTemporalRepository;
import com.inter.proyecto_intergrupo.service.adminServices.ControlPanelJobsService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class TaxBaseService {

    public static final String PRIMARY = "\\\\co.igrupobbva\\svrfilesystem\\TX\\RECEPCION_HOST\\XC\\ASES_FISCAL_IMPUESTOS\\";
    public static final String SECONDARY = "\\\\co.igrupobbva\\svrfilesystem\\TX\\ENVIO_HOST\\XC\\CONSOLIDACION\\";
    final String PRIMARY_SC = "C:\\Users\\CE66390\\Documents\\BBVA Intergrupo\\Pantallazos Base Fiscal\\Resultados - pantallazos base fiscal.txt";
    final String SEC_SC = "C:\\Users\\ce66390\\Documents\\Prueba No borrar\\Resultados - pantallazos base fiscal.txt";
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Autowired
    private TaxBaseRepository taxBaseRepository;
    @Autowired
    private TaxBaseTemporalRepository taxBaseTemporalRepository;
    @Autowired
    private TaxBaseLoadRepository taxBaseLoadRepository;
    @Autowired
    private ControlPanelJobsRepository controlPanelJobsRepository;
    //public static final String DIRGOF = "C:\\Users\\ce66390\\Documents\\Prueba No borrar\\Base\\GOF\\";
    @Autowired
    private ControlPanelJobsService controlPanelJobsService;
    @Autowired
    private AuditRepository auditRepository;

    public TaxBaseService(TaxBaseRepository taxBaseRepository, TaxBaseLoadRepository taxBaseLoadRepository, ControlPanelJobsRepository controlPanelJobsRepository, AuditRepository auditRepository, JdbcTemplate jdbcTemplate) {
        this.taxBaseRepository = taxBaseRepository;
        this.taxBaseLoadRepository = taxBaseLoadRepository;
        this.controlPanelJobsRepository = controlPanelJobsRepository;
        this.auditRepository = auditRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Scheduled(cron = "0 2 0,7 * * ?")
    //@Scheduled(cron = "0 22 11 * * * ")
    public void validateRoutineTax() throws IOException, InvalidFormatException {

        Query validate = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando_jobs WHERE estado = 1 AND nombre = 'GOF Base Fiscal'");

        if (!validate.getResultList().isEmpty()) {
            ControlPanelJobs job = controlPanelJobsService.findByIdJob(1);

            if (job.getNombre().equals("GOF Base Fiscal")) {

                boolean state = loadData(PRIMARY);

                boolean state2 = loadData(SECONDARY);
                job.setFechaEjecucion(new Date());
                if (state || state2)
                    job.setFechaEjecucionExitosa(new Date());
            }
            controlPanelJobsService.save(job);
        }
    }


    public boolean loadData(String dir) throws IOException, InvalidFormatException {

        String line = "";
        boolean cargoJob = false;

        List<TaxBaseTemporal> toInsert = new ArrayList<>();

        List<TaxBaseLoad> temporalList = new ArrayList<>();
        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal_carga WHERE estado = 'PENDIENTE'", TaxBaseLoad.class );

        if(!getData.getResultList().isEmpty()){
            temporalList = getData.getResultList();
        }

        ZoneId defaultZoneId = ZoneId.systemDefault();
        LocalDate date = LocalDate.now().minusDays(1);
        Date correctDate = Date.from(date.atStartOfDay(defaultZoneId).toInstant());
        SimpleDateFormat formatter = new SimpleDateFormat("yyMMdd");
        String today = formatter.format(correctDate);

        String peticion = "";

        if(!temporalList.isEmpty()) {
            for (TaxBaseLoad taxBaseLoad : temporalList) {

                Query truncateTemp = entityManager.createNativeQuery("TRUNCATE TABLE nexco_base_fiscal_temporal");
                truncateTemp.executeUpdate();

                String regex = "GOF.S0203.HAD"+"+\\d+"+".F"+taxBaseLoad.getFechaFichero()+".P"+taxBaseLoad.getPeticion()+".TD+\\d+.MI+\\d+.TXT";
                String realName = dir;

                File path = new File(dir);


                File[] listFiles = path.listFiles(file -> {
                    String name = file.getName();
                    return name.matches(regex);
                });

                if (listFiles != null) {
                    if (listFiles.length > 0) {
                        realName = listFiles[0].toString();
                    }

                }

                peticion = taxBaseLoad.getNit().replace("000000", "");

                try {
                    BufferedReader readerValid = new BufferedReader(new FileReader(realName));
                    if (readerValid.readLine() == null) {
                        TaxBaseLoad temporal = taxBaseLoadRepository.findByIdCarga(taxBaseLoad.getIdCarga());
                        temporal.setEstado("El documento de petición " + taxBaseLoad.getPeticion() + " se encuentra vacío");
                        taxBaseLoadRepository.save(temporal);
                    }
                    readerValid.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                boolean pathFound = true;

                try {

                    boolean cargo = true;
                    BufferedReader reader = new BufferedReader(new FileReader(realName));

                    Query query1 = entityManager.createNativeQuery("SELECT TOP 10 em.* FROM nexco_base_fiscal as em WHERE em.nit_contraparte = ? AND em.periodo = ?");
                    query1.setParameter(1, taxBaseLoad.getNit().replace("000000", ""));
                    query1.setParameter(2, taxBaseLoad.getFecha());

                    if (!query1.getResultList().isEmpty()) {

                        Query query2 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal WHERE nit_contraparte = ? AND periodo = ?");
                        query2.setParameter(1, taxBaseLoad.getNit().replace("000000", ""));
                        query2.setParameter(2, taxBaseLoad.getFecha());
                        query2.executeUpdate();

                        Query query4 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal_completa WHERE nit_contraparte = ? AND periodo = ?");
                        query4.setParameter(1, taxBaseLoad.getNit().replace("000000", ""));
                        query4.setParameter(2, taxBaseLoad.getFecha());
                        query4.executeUpdate();

                        Query query6 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal_no_aplica WHERE nit_contraparte = ? ");
                        query6.setParameter(1, taxBaseLoad.getNit().replace("000000", ""));
                        query6.executeUpdate();

                        Query query7 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal_no_aplica_completa WHERE nit_contraparte = ? ");
                        query7.setParameter(1, taxBaseLoad.getNit().replace("000000", ""));
                        query7.executeUpdate();
                    }

                    while ((line = reader.readLine()) != null && cargo) {
                        String[] parts = line.split(";");
                        if (parts.length == 21) {

                            TaxBaseTemporal insert = new TaxBaseTemporal();
                            if (parts[17] != null && parts[17].trim().length() != 0) {
                                insert.setDivisa(parts[17].trim());
                            } else {
                                insert.setDivisa("COP");
                            }
                            insert.setContrato(parts[5].trim() + parts[6].trim() + parts[7].trim() + parts[8].trim());
                            insert.setNitContraparte(parts[0].trim().replaceAll("[^a-zA-Z0-9]", "").replaceFirst("^0+(?!$)", ""));
                            if (parts[16].charAt(0) == '+') {
                                Double value = Double.parseDouble(parts[16].trim().replace("+", ""));
                                String sign = String.format("%.2f", value).replace(",", ".");
                                double finalV = Double.parseDouble(sign);
                                insert.setValor(finalV);
                            } else {
                                Double value = Double.parseDouble(parts[16].trim());
                                String sign = String.format("%.2f", value).replace(",", ".");
                                double finalV = Double.parseDouble(sign);
                                insert.setValor(finalV);
                            }
                            insert.setCuentaLocal(parts[3].trim());
                            insert.setPeriodo(taxBaseLoad.getFecha());
                            insert.setCodNeocon(parts[3].trim().replaceAll("[^a-zA-Z0-9]", ""));
                            insert.setPeticion(taxBaseLoad.getPeticion());

                            toInsert.add(insert);
                            cargoJob = true;

                            if (toInsert.size() == 1500000) {
                                batchInsert(toInsert);
                                toInsert.clear();
                            }
                        } else {
                            cargo = false;
                        }
                    }
                    reader.close();

                } catch (Exception e) {
                    pathFound = false;
                }
                batchInsert(toInsert);
                toInsert.clear();

                if (pathFound) {

                    Query update = entityManager.createNativeQuery("UPDATE nexco_base_fiscal_temporal \n" +
                            "SET valor = valor * -1 \n" +
                            "WHERE (cuenta_local like '2%' or cuenta_local like '3%' or cuenta_local like '4%' or cuenta_local like '62%' or cuenta_local like '63%' or cuenta_local like '82%' or cuenta_local like '83%')");
                    update.executeUpdate();

                    if (peticion.equals("800226098") || peticion.equals("800240882")) {

                        Query insertToBF = entityManager.createNativeQuery("INSERT INTO nexco_base_fiscal (cod_neocon, divisa,yntp,sociedad_yntp,contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local, periodo) \n" +
                                "SELECT b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, SUM(a.valor/100), p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo FROM \n" +
                                "(SELECT temp.id_base_fiscal,temp.cod_neocon, temp.contrato, temp.cuenta_local, temp.divisa, temp.nit_contraparte, temp.periodo, temp.valor FROM nexco_base_fiscal_temporal as temp \n" +
                                "INNER JOIN (SELECT cuenta_local from nexco_cuentas_responsables where aplica_base_fiscal = 1 OR aplica_metodologia = 1) as resp \n" +
                                "ON temp.cuenta_local LIKE CAST(resp.cuenta_local AS varchar)+'%') as a \n" +
                                "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.cuenta_local = b.NUCTA \n" +
                                "INNER JOIN nexco_terceros AS ter ON ter.nit_contraparte = a.nit_contraparte \n" +
                                "INNER JOIN nexco_sociedades_yntp AS yntp ON ter.yntp = yntp.yntp \n" +
                                "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais " +
                                "GROUP BY b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo");

                        insertToBF.executeUpdate();

                        Query insertIntoBFComplete = entityManager.createNativeQuery("INSERT INTO nexco_base_fiscal_completa (cod_neocon, divisa,yntp,sociedad_yntp,contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local, periodo) " +
                                "SELECT b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, a.valor/100, p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo FROM " +
                                "(SELECT temp.id_base_fiscal,temp.cod_neocon, temp.contrato, temp.cuenta_local, temp.divisa, temp.nit_contraparte, temp.periodo, temp.valor FROM nexco_base_fiscal_temporal as temp " +
                                "INNER JOIN (SELECT cuenta_local from nexco_cuentas_responsables where aplica_base_fiscal = 1 OR aplica_metodologia = 1) as resp " +
                                "ON temp.cuenta_local LIKE CAST(resp.cuenta_local AS varchar)+'%') as a " +
                                "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.cuenta_local = b.NUCTA " +
                                "INNER JOIN nexco_terceros AS ter ON ter.nit_contraparte = a.nit_contraparte " +
                                "INNER JOIN nexco_sociedades_yntp AS yntp ON ter.yntp = yntp.yntp " +
                                "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais ");

                        insertIntoBFComplete.executeUpdate();

                    } else {

                        Query insertToBF = entityManager.createNativeQuery("INSERT INTO nexco_base_fiscal (cod_neocon, divisa,yntp,sociedad_yntp,contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local, periodo) \n" +
                                "SELECT b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, SUM(a.valor/100), p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo FROM \n" +
                                "(SELECT temp.id_base_fiscal,temp.cod_neocon, temp.contrato, temp.cuenta_local, temp.divisa, temp.nit_contraparte, temp.periodo, temp.valor FROM nexco_base_fiscal_temporal as temp \n" +
                                "INNER JOIN (SELECT cuenta_local from nexco_cuentas_responsables where aplica_base_fiscal = 1 OR aplica_metodologia = 1) as resp \n" +
                                "ON temp.cuenta_local LIKE CAST(resp.cuenta_local AS varchar)+'%') as a \n" +
                                "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.cuenta_local = b.NUCTA \n" +
                                "INNER JOIN nexco_terceros AS ter ON ter.nit_contraparte = a.nit_contraparte \n" +
                                "INNER JOIN nexco_sociedades_yntp AS yntp ON ter.yntp = yntp.yntp \n" +
                                "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais " +
                                "INNER JOIN (SELECT v FROM (SELECT '' as v UNION ALL SELECT id_contrato FROM nexco_contratos) as u) as con ON con.v = a.contrato " +
                                "GROUP BY b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo");

                        insertToBF.executeUpdate();

                        Query insertIntoBFComplete = entityManager.createNativeQuery("INSERT INTO nexco_base_fiscal_completa (cod_neocon, divisa,yntp,sociedad_yntp,contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local, periodo) " +
                                "SELECT b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, a.valor/100, p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo FROM " +
                                "(SELECT temp.id_base_fiscal,temp.cod_neocon, temp.contrato, temp.cuenta_local, temp.divisa, temp.nit_contraparte, temp.periodo, temp.valor FROM nexco_base_fiscal_temporal as temp " +
                                "INNER JOIN (SELECT cuenta_local from nexco_cuentas_responsables where aplica_base_fiscal = 1 OR aplica_metodologia = 1) as resp " +
                                "ON temp.cuenta_local LIKE CAST(resp.cuenta_local AS varchar)+'%') as a " +
                                "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.cuenta_local = b.NUCTA " +
                                "INNER JOIN nexco_terceros AS ter ON ter.nit_contraparte = a.nit_contraparte " +
                                "INNER JOIN nexco_sociedades_yntp AS yntp ON ter.yntp = yntp.yntp " +
                                "INNER JOIN (SELECT v FROM (SELECT '' as v UNION ALL SELECT id_contrato FROM nexco_contratos) as u) as con ON con.v = a.contrato " +
                                "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais ");

                        insertIntoBFComplete.executeUpdate();
                    }

                    Query insertNoApply = entityManager.createNativeQuery("INSERT INTO nexco_base_fiscal_no_aplica_completa (cod_neocon, divisa,yntp,sociedad_yntp,contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local, periodo)\n" +
                            "SELECT b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, a.valor, p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo FROM\n" +
                            "(SELECT temp.cod_neocon, temp.contrato, temp.cuenta_local, temp.divisa, ter.nit_contraparte, temp.periodo, temp.valor/100 as valor, ter.yntp FROM nexco_base_fiscal_temporal as temp\n" +
                            "INNER JOIN nexco_terceros as ter ON temp.nit_contraparte = ter.nit_contraparte) AS a\n" +
                            "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.cuenta_local = b.NUCTA \n" +
                            "INNER JOIN nexco_sociedades_yntp AS yntp ON a.yntp = yntp.yntp\n" +
                            "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais\n" +
                            "WHERE a.cuenta_local NOT IN (SELECT CAST(cuenta_local as varchar) FROM nexco_cuentas_responsables)");

                    insertNoApply.executeUpdate();

                    Query insertNoApplyComplete = entityManager.createNativeQuery("INSERT INTO nexco_base_fiscal_no_aplica (cod_neocon, divisa,yntp,sociedad_yntp,contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local, periodo)\n" +
                            "SELECT b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, SUM(a.valor), p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo FROM \n" +
                            "(SELECT temp.cod_neocon, temp.contrato, temp.cuenta_local, temp.divisa, ter.nit_contraparte, temp.periodo, temp.valor/100 as valor, ter.yntp FROM nexco_base_fiscal_temporal as temp \n" +
                            "INNER JOIN nexco_terceros as ter ON temp.nit_contraparte = ter.nit_contraparte) AS a \n" +
                            "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b ON a.cuenta_local = b.NUCTA \n" +
                            "INNER JOIN nexco_sociedades_yntp AS yntp ON a.yntp = yntp.yntp \n" +
                            "INNER JOIN nexco_paises AS p ON yntp.id_pais = p.id_pais \n" +
                            "WHERE a.cuenta_local NOT IN (SELECT CAST(cuenta_local as varchar) FROM nexco_cuentas_responsables) \n" +
                            "GROUP BY b.CODICONS46, a.divisa, yntp.yntp,yntp.sociedad_corta, a.contrato, a.nit_contraparte, p.id_pais, p.nombre_pais, a.cuenta_local, a.periodo ");

                    insertNoApplyComplete.executeUpdate();

                }

                Query inserted = entityManager.createNativeQuery("SELECT nexco_base_fiscal_temporal.peticion FROM nexco_base_fiscal_temporal\n" +
                        "LEFT JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') as p \n" +
                        "ON cuenta_local = p.NUCTA where p.NUCTA is not null \n" +
                        "GROUP BY nexco_base_fiscal_temporal.peticion");

                List<String> isInserted = inserted.getResultList();

                if (!isInserted.isEmpty()) {
                    String lista = "";

                    for (int i = 0; i < isInserted.size(); i++) {
                        if (isInserted.size() - 1 == i) {
                            lista = lista + "'" + isInserted.get(i) + "'";
                        } else {
                            lista = lista + "'" + isInserted.get(i) + "',";
                        }
                    }

                    Query updateBF = entityManager.createNativeQuery("UPDATE nexco_base_fiscal_carga SET estado = 'CARGADO' WHERE peticion IN (" + lista + ") ");
                    updateBF.executeUpdate();
                }

                Query notInserted = entityManager.createNativeQuery("SELECT nexco_base_fiscal_temporal.peticion FROM nexco_base_fiscal_temporal\n" +
                        "LEFT JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') as p \n" +
                        "ON cuenta_local = p.NUCTA where p.NUCTA is null\n" +
                        "GROUP BY nexco_base_fiscal_temporal.peticion");

                List<String> isNotInserted = notInserted.getResultList();

                if (!isNotInserted.isEmpty()) {
                    String lista = "";

                    for (int i = 0; i < isNotInserted.size(); i++) {
                        if (isNotInserted.size() - 1 == i) {
                            lista = lista + "'" + isNotInserted.get(i) + "'";
                        } else {
                            lista = lista + "'" + isNotInserted.get(i) + "',";
                        }
                    }
                }


            }
        }

        return cargoJob;
    }

    public boolean getGOFCode(InputStream file, String month, User user) throws IOException, ParseException {
        // Declaracion de variables
        // Ruta del fichuero que vamos a procesar

        File f1 = new File(PRIMARY_SC);

        try (OutputStream output = new FileOutputStream(f1)) {
            file.transferTo(output);
            taxBaseLoadRepository.deleteAll();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        // Numero de consecutivos
        Integer num = 0;
        // Inicio y final del texto donde tomaremos la informacion
        Integer numInicio, numFin = 0;

        FileReader fr = new FileReader(f1);
        BufferedReader br = new BufferedReader(fr);
        BufferedReader br0 = new BufferedReader(fr);
        String s;

        // Delimitacion inicial para rastrear el numero de consecutivo
        String inputInicio = "CONSECUTIVO";
        // Delimitacion final para rastrear el numero de consecutivo
        String inputFin = "OFICINA";
        // Delimitacion inicial para rastrear el numero de consecutivo
        String inputInicioNIT = "NUMERO DE IDENTIFICACION";
        String inputInicioFecha = "FECHA INICIO";
        String inputInicioFechaFichero = "CONTABILIDAD GENERAL";
        String mustInclude = "CONSULTA DE MOVIMIENTOS (HADTMI1)";

        int count = 0;

        TaxBaseLoad load = new TaxBaseLoad();
        while ((s = br.readLine()) != null) {

            if (count == 1) {
                if (!s.contains(mustInclude)) {
                    return false;
                }
            }

            if (s.contains(inputInicioNIT)) {
                String temp = s.split("-")[1].trim();
                if (!temp.isBlank() && temp != null) {
                    load.setNit(temp);
                }
            }
            if (s.contains(inputInicioFecha)) {
                String[] temp = s.split(":")[1].split("-");
                if (temp.length != 0) {
                    load.setFecha(temp[0].trim() + "-" + temp[1].trim());
                }
            }
            if (s.contains(inputInicioFechaFichero)) {
                String temp = s.split(inputInicioFechaFichero)[1].trim().substring(5, 13);
                DateFormat tempDate = new SimpleDateFormat("dd/MM/yy");
                Date tempDate2 = tempDate.parse(temp);
                DateFormat tempDate3 = new SimpleDateFormat("yyMMdd");
                String tempDateString = tempDate3.format(tempDate2);

                if (!tempDateString.isBlank() && tempDateString != null) {
                    load.setFechaFichero(tempDateString);
                }
            }
            if (s.contains(inputInicio)) {
                numInicio = s.indexOf(inputInicio);
                numFin = s.indexOf(inputFin);
                String temp = s.substring(numInicio + 18, numFin - 6);
                if (!temp.isBlank() && temp != null) {
                    load.setPeticion(temp);
                    num = num + 1;
                    if (month.equals(load.getFecha())) {
                        load.setEstado("PENDIENTE");
                    } else {
                        load.setEstado("Fecha no coincide con la de petición");
                    }
                    taxBaseLoadRepository.save(load);
                    load = new TaxBaseLoad();
                }
            }
            count++;
        }
        fr.close();

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Cargue documento Peticiones por NIT");
        insert.setCentro(user.getCentro());
        insert.setComponente("BASE FISCAL");
        insert.setFecha(today);
        insert.setInput("PETICIONES");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        return true;
    }

    public ArrayList<TaxBase> getTaxBaseAccum(String periodo) {

        ArrayList<TaxBase> toReturn;

        ArrayList<String> months = new ArrayList<>();

        int month = Integer.parseInt(periodo.substring(5, 7)) < 10 ? Integer.parseInt(periodo.substring(5, 7).replace("0", "")) : Integer.parseInt(periodo.substring(5, 7));

        String year = periodo.substring(0, 4);
        for (int i = month; i > 0; i--) {
            String m;
            if (i < 10) {
                m = year + "-0" + i;
            } else {
                m = year + "-" + i;
            }
            months.add(m);
        }


        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal WHERE periodo IN (:months) " +
                "ORDER BY periodo desc", TaxBase.class);
        getData.setParameter("months", months);

        if (!getData.getResultList().isEmpty()) {
            toReturn = (ArrayList<TaxBase>) getData.getResultList();
        } else {
            toReturn = new ArrayList<>();
        }

        return toReturn;
    }

    public ArrayList<TaxBase> getTaxBase(String fecont) {

        ArrayList<TaxBase> toReturn;

        Query toShow = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal where periodo = ?", TaxBase.class);
        toShow.setParameter(1, fecont);

        if (toShow.getResultList().isEmpty()) {
            toReturn = new ArrayList<>();
        } else {
            toReturn = (ArrayList<TaxBase>) toShow.getResultList();
        }

        return toReturn;
    }

    public ArrayList<TaxBaseComplete> getTaxBaseComplete(String fecont) {

        ArrayList<TaxBaseComplete> toReturn;

        Query toShow = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal_completa where periodo = ?", TaxBaseComplete.class);
        toShow.setParameter(1, fecont);

        if (toShow.getResultList().isEmpty()) {
            toReturn = new ArrayList<>();
        } else {
            toReturn = (ArrayList<TaxBaseComplete>) toShow.getResultList();
        }

        return toReturn;
    }

    public ArrayList<Object[]> getNoMatchBF(String fecont) {

        ArrayList<Object[]> toReturn;
        Query getNoMatch = entityManager.createNativeQuery("SELECT cod_neocon, divisa ,yntp,sociedad_yntp, contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local " +
                "FROM nexco_base_fiscal_no_aplica WHERE periodo = ?");
        getNoMatch.setParameter(1, fecont);

        if (getNoMatch.getResultList().isEmpty()) {
            toReturn = new ArrayList<>();
        } else {
            toReturn = (ArrayList<Object[]>) getNoMatch.getResultList();
        }

        return toReturn;
    }

    public ArrayList<Object[]> getNoMatchBFComplete(String fecont) {

        ArrayList<Object[]> toReturn;
        Query getNoMatch = entityManager.createNativeQuery("SELECT cod_neocon, divisa ,yntp,sociedad_yntp, contrato, nit_contraparte, valor, cod_pais, pais, cuenta_local " +
                "FROM nexco_base_fiscal_no_aplica_completa WHERE periodo = ?");
        getNoMatch.setParameter(1, fecont);

        if (getNoMatch.getResultList().isEmpty()) {
            toReturn = new ArrayList<>();
        } else {
            toReturn = (ArrayList<Object[]>) getNoMatch.getResultList();
        }

        return toReturn;
    }

    public void deleteFromBaseFiscal(String periodo) {
        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal WHERE periodo = :period");
        delete.setParameter("period", periodo);
        delete.executeUpdate();

        Query delete1 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal_completa WHERE periodo = :period");
        delete1.setParameter("period", periodo);
        delete1.executeUpdate();

        Query delete2 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal_no_aplica WHERE periodo = :period");
        delete2.setParameter("period", periodo);
        delete2.executeUpdate();

        Query delete3 = entityManager.createNativeQuery("DELETE FROM nexco_base_fiscal_no_aplica_completa WHERE periodo = :period");
        delete3.setParameter("period", periodo);
        delete3.executeUpdate();

    }

    public List<TaxBaseLoad> findAllLoad() {
        return taxBaseLoadRepository.findAll();
    }

    public Page<TaxBase> getAll(Pageable pageable) {
        return taxBaseRepository.findAll(pageable);
    }

    public void batchInsert(List<TaxBaseTemporal> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_base_fiscal_temporal (cod_neocon, contrato, cuenta_local, divisa, nit_contraparte, periodo, valor, peticion) values (?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getCodNeocon());
                        ps.setString(2, temporal.get(i).getContrato());
                        ps.setString(3, temporal.get(i).getCuentaLocal());
                        ps.setString(4, temporal.get(i).getDivisa());
                        ps.setString(5, temporal.get(i).getNitContraparte().replace("000000", ""));
                        ps.setString(6, temporal.get(i).getPeriodo());
                        ps.setDouble(7, temporal.get(i).getValor());
                        ps.setString(8, temporal.get(i).getPeticion());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public ArrayList<TaxBase> findByFilter(String value, String filter, String period) {
        ArrayList<TaxBase> toReturn;
        switch (filter) {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal WHERE cuenta_local LIKE ? and periodo LIKE ?", TaxBase.class);
                query.setParameter(1, value);
                query.setParameter(2, period);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<TaxBase>) query.getResultList();
                }
                break;
            case "NIT":

                Query query0 = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal WHERE nit_contraparte LIKE ? and periodo LIKE ? ", TaxBase.class);
                query0.setParameter(1, value);
                query0.setParameter(2, period);
                if (query0.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<TaxBase>) query0.getResultList();
                }
                break;
            case "Contrato":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal as nsy WHERE contrato LIKE ? and periodo LIKE ? ", TaxBase.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<TaxBase>) query1.getResultList();
                }
                break;
            case "Divisa":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal as nsy WHERE divisa LIKE ? and periodo LIKE ? ", TaxBase.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<TaxBase>) query2.getResultList();
                }
                break;

            case "Importe":
                Query query4 = entityManager.createNativeQuery("SELECT * FROM nexco_base_fiscal WHERE valor LIKE ? and periodo LIKE ? ", TaxBase.class);
                query4.setParameter(1, value);
                query4.setParameter(2, period);
                if (query4.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<TaxBase>) query4.getResultList();
                }
                break;
            default:
                toReturn = new ArrayList<>();
        }

        return toReturn;
    }

    public TaxBaseLoad saveLoad(TaxBaseLoad load, User user) {
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Guardar registro petición estado carga");
        insert.setCentro(user.getCentro());
        insert.setComponente("Base Fiscal");
        insert.setFecha(today);
        insert.setInput("Adición de petición");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        load.setNit(String.format("%15s", load.getNit()).replace(' ', '0'));
        load.setPeticion(String.format("%7s", load.getPeticion()).replace(' ', '0'));
        load.setEstado("PENDIENTE");
        return taxBaseLoadRepository.save(load);
    }
}
