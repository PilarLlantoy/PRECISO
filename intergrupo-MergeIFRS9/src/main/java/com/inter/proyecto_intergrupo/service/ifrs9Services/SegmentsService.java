package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Anexo;
import com.inter.proyecto_intergrupo.model.ifrs9.RiskAccount;
import com.inter.proyecto_intergrupo.model.ifrs9.SegmentosFinalTemp;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.temporal.SubsidiariesTemplateTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.SegmentFinalTempRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.SegmentRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SegmentDecisionTreeRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
@Transactional
public class SegmentsService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private SegmentDecisionTreeRepository segmentDecisionTreeRepository;

    @Autowired
    private SegmentFinalTempRepository segmentFinalTempRepository;

    @Autowired
    private SegmentRepository segmentRepository;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    public List<Object[]> validateSegments(String period) {

        Query truncateInfo = entityManager.createNativeQuery("truncate table nexco_segmentos_temp;");
        truncateInfo.executeUpdate();

        Query deleteInfo = entityManager.createNativeQuery("delete from nexco_segmentos where periodo = ?;");
        deleteInfo.setParameter(1,period);
        deleteInfo.executeUpdate();

        Query insertInfoSeg = entityManager.createNativeQuery("insert into nexco_segmentos_temp (identificacion, numero_cliente, nombre_cliente, tipo_persona, segmento_finrep, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion) \n" +
                "select trim(IDENTIFICACION), trim(NUMCLIEN), trim(CLIENTE), trim(TIPO_PERSONA), trim(CODIGO_LIQUIDEZ), \n" +
                "case when tipo_institucion <> '' then substring(tipo_institucion, 1, 1) else trim(corazu) end, \n" +
                "case when tipo_institucion <> '' then substring(tipo_institucion, 2, 2) else trim(sub_corazu) end, \n" +
                "trim(CIIU), CONVERT(INTEGER, EMPLEADOS_PERSONAS), CONVERT(DECIMAL, ACTIVOS), CONVERT(FLOAT, VOLUMEN_VTA), trim(tipo_institucion) \n" +
                "from [82.255.50.134].DB_FINAN_NUEVA.dbo.IFRS9_FINREP_CALCULADA_"+period.replace("-", "")+";");
        insertInfoSeg.executeUpdate();

        Query validateHistSeg = entityManager.createNativeQuery("update a\n" +
                "set valida = 'HIST'\n" +
                "FROM nexco_segmentos_temp a\n" +
                "inner join (select * from nexco_segmentos_hist where periodo <> ?) b\n" +
                "on a.numero_cliente = b.numero_cliente\n" +
                ";");
        validateHistSeg.setParameter(1,period);
        validateHistSeg.executeUpdate();

        Query validateHistSeg2 = entityManager.createNativeQuery("insert into nexco_segmentos (identificacion, numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, valida)\n" +
                "select a.identificacion, a.numero_cliente, a.nombre_cliente, a.tipo_persona, a.segmento_finrep, b.segmento_finrep_new, a.corasu, a.subcorasu, a.ciiu, a.numero_empleados, a.total_activos, a.total_ventas, a.tipo_institucion, ?, 'HIST'\n" +
                "FROM nexco_segmentos_temp a\n" +
                "inner join (select * from nexco_segmentos_hist where periodo <> ?) b\n" +
                "on a.numero_cliente = b.numero_cliente\n" +
                ";");
        validateHistSeg2.setParameter(1,period);
        validateHistSeg2.setParameter(2,period);
        validateHistSeg2.executeUpdate();

        List<SegmentDecisionTree> listTree = segmentDecisionTreeRepository.findAll();

        for(SegmentDecisionTree tree: listTree){

            String where = "1=1";
            //corasu
            if(tree.getCorasuOp() != null && tree.getCorasu() != null) {
                if (tree.getCorasuOp().equals("=") || tree.getCorasuOp().equals("<>") || tree.getCorasuOp().equals("<") || tree.getCorasuOp().equals(">")) {
                    where = where += " and corasu " + tree.getCorasuOp() + " '" + tree.getCorasu() + "'";
                } else if (tree.getCorasuOp().equals("IN") || tree.getCorasuOp().equals("NOT IN")) {
                    if (tree.getCorasu().contains(",")) {
                        String in = "";
                        in = tree.getCorasu().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and corasu " + tree.getCorasuOp() + " " + in;
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
                            where = where += " and corasu " + tree.getCorasuOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and corasu " + tree.getCorasuOp() + " ('" + tree.getCorasu() + "')";
                    }
                }
            }
            //subcorasu
            if(tree.getSubCorasuOp() != null && tree.getSubCorasu() != null) {
                if (tree.getSubCorasuOp().equals("=") || tree.getSubCorasuOp().equals("<>") || tree.getSubCorasuOp().equals("<") || tree.getSubCorasuOp().equals(">")) {
                    where = where += " and subcorasu " + tree.getSubCorasuOp() + " '" + tree.getSubCorasu() + "'";
                } else if (tree.getSubCorasuOp().equals("IN") || tree.getSubCorasuOp().equals("NOT IN")) {
                    if (tree.getSubCorasu().contains(",")) {
                        String in = "";
                        in = tree.getSubCorasu().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and subcorasu " + tree.getSubCorasuOp() + " " + in;
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
                            where = where += " and subcorasu " + tree.getSubCorasuOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and subcorasu " + tree.getSubCorasuOp() + " ('" + tree.getSubCorasu() + "')";
                    }
                }
            }
            //ciiu
            if(tree.getCiiuOp() != null && tree.getCiiu() != null) {
                if (tree.getCiiuOp().equals("=") || tree.getCiiuOp().equals("<>") || tree.getCiiuOp().equals("<") || tree.getCiiuOp().equals(">")) {
                    where = where += " and RIGHT(ciiu, 4) " + tree.getCiiuOp() + " '" + tree.getCiiu() + "'";
                } else if (tree.getCiiuOp().equals("IN") || tree.getCiiuOp().equals("NOT IN")) {
                    if (tree.getCiiu().contains(",")) {
                        String in = "";
                        in = tree.getCiiu().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and RIGHT(ciiu, 4) " + tree.getCiiuOp() + " " + in;
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
                            where = where += " and RIGHT(ciiu, 4) " + tree.getCiiuOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (tree.getCiiu().equals("IF")) {
                        where = where += " and RIGHT(ciiu, 4) " + tree.getCiiuOp() + "  (select distinct ciiu from nexco_ciiu)";
                    } else {
                        where = where += " and RIGHT(ciiu, 4) " + tree.getCiiuOp() + " ('" + tree.getCiiu() + "')";
                    }
                }
            }
            //empleados
            if(tree.getNumeroEmpleadosOp() != null && tree.getNumeroEmpleados() != null) {
                if (tree.getNumeroEmpleadosOp().equals("=") || tree.getNumeroEmpleadosOp().equals("<>") || tree.getNumeroEmpleadosOp().equals("<") || tree.getNumeroEmpleadosOp().equals(">")) {
                    where = where += " and numero_empleados " + tree.getNumeroEmpleadosOp() + " " + tree.getNumeroEmpleados();
                } else if (tree.getNumeroEmpleadosOp().equals("IN") || tree.getNumeroEmpleadosOp().equals("NOT IN")) {
                    if (tree.getNumeroEmpleados().contains(",")) {
                        String in = "";
                        in = tree.getNumeroEmpleados().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and numero_empleados " + tree.getNumeroEmpleadosOp() + " " + in;
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
                            where = where += " and numero_empleados " + tree.getNumeroEmpleadosOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and numero_empleados " + tree.getNumeroEmpleadosOp() + " ('" + tree.getNumeroEmpleados() + "')";
                    }
                }
            }
            //activos
            if(tree.getTotalActivosOp() != null && tree.getTotalActivos() != null) {
                if (tree.getTotalActivosOp().equals("=") || tree.getTotalActivosOp().equals("<>") || tree.getTotalActivosOp().equals("<") || tree.getTotalActivosOp().equals(">")) {
                    where = where += " and total_activos " + tree.getTotalActivosOp() + " " + tree.getTotalActivos();
                } else if (tree.getTotalActivosOp().equals("IN") || tree.getTotalActivosOp().equals("NOT IN")) {
                    if (tree.getTotalActivos().contains(",")) {
                        String in = "";
                        in = tree.getTotalActivos().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and total_activos " + tree.getTotalActivosOp() + " " + in;
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
                            where = where += " and total_activos " + tree.getTotalActivosOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and total_activos " + tree.getTotalActivosOp() + " ('" + tree.getTotalActivos() + "')";
                    }
                }
            }
            //ventas
            if(tree.getTotalVentasOp() != null && tree.getTotalVentas() != null) {
                if (tree.getTotalVentasOp().equals("=") || tree.getTotalVentasOp().equals("<>") || tree.getTotalVentasOp().equals("<") || tree.getTotalVentasOp().equals(">")) {
                    where = where += " and total_ventas " + tree.getTotalVentasOp() + " " + tree.getTotalVentas();
                } else if (tree.getTotalVentasOp().equals("IN") || tree.getTotalVentasOp().equals("NOT IN")) {
                    if (tree.getTotalVentas().contains(",")) {
                        String in = "";
                        in = tree.getTotalVentas().replace(",", "','");
                        in = "('" + in.replace(" ", "") + "')";
                        where = where += " and total_ventas " + tree.getTotalVentasOp() + " " + in;
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
                            where = where += " and total_ventas " + tree.getTotalVentasOp() + " " + in;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        where = where += " and total_ventas " + tree.getTotalVentasOp() + " ('" + tree.getTotalVentas() + "')";
                    }
                }
            }

            Query insertSeg = entityManager.createNativeQuery("insert into nexco_segmentos (identificacion, numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, valida)\n" +
                    "select identificacion, numero_cliente, nombre_cliente, tipo_persona, segmento_finrep, ?, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, ?, ? \n" +
                    "from nexco_segmentos_temp " +
                    "where "+where+" and valida is null and segmento_finrep <> ?\n" +
                    ";");
            insertSeg.setParameter(1,tree.getCodigoIFRS9());
            insertSeg.setParameter(2,period);
            insertSeg.setParameter(3,tree.getCodigoIFRS9());
            insertSeg.setParameter(4,tree.getCodigoIFRS9());
            insertSeg.executeUpdate();

            Query udpateSegVal = entityManager.createNativeQuery("update nexco_segmentos_temp " +
                    "set valida = ? \n" +
                    "where "+where+" and valida is null\n" +
                    ";");
            udpateSegVal.setParameter(1,tree.getCodigoIFRS9());
            udpateSegVal.executeUpdate();
        }

        //status

        Date today = new Date();
        String input = "SEGMENTOS-FINREP";

        StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, period);

        if (validateStatus == null) {
            StatusInfo status = new StatusInfo();
            status.setInput(input);
            status.setPeriodo(period);
            status.setFecha(today);
            StatusInfoRepository.save(status);
        } else {
            validateStatus.setFecha(today);
            StatusInfoRepository.save(validateStatus);
        }


        Query insertFil = entityManager.createNativeQuery("select numero_cliente, nombre_cliente, tipo_persona, \n" +
                "segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, \n" +
                "total_activos, total_ventas, tipo_institucion, periodo, identificacion \n" +
                "from nexco_segmentos_final where periodo = ? \n" +
                ";");
        insertFil.setParameter(1,period);

        return insertFil.getResultList();
    }

    public List<Object[]> findAll(String period) {

        Query insertFil = entityManager.createNativeQuery("select top 100000 numero_cliente, nombre_cliente, tipo_persona, \n" +
                "segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, \n" +
                "total_activos, total_ventas, tipo_institucion, periodo, '' observaciones, identificacion \n" +
                "from nexco_segmentos where periodo = ? and valida <> 'HIST' \n" +
                ";");
        insertFil.setParameter(1,period);

        return insertFil.getResultList();
    }

    public List<Object[]> findAllHist(String valor, String item) {

        String variable = item;
        if(item.equals("Número Cliente")){
            variable = "numero_cliente";
        } else if(item.equals("Nombre Cliente")){
            variable = "nombre_cliente";
        } else if(item.equals("Tipo Persona")){
            variable = "tipo_persona";
        } else if(item.equals("Segmento Viejo")){
            variable = "segmento_finrep";
        } else if(item.equals("Corasu")){
            variable = "corasu";
        } else if(item.equals("Subcorasu")){
            variable = "subcorasu";
        } else if(item.equals("CIIU")){
            variable = "ciiu";
        } else if(item.equals("Segmento Nuevo")){
            variable = "segmento_finrep_new";
        }
        Query insertFil = entityManager.createNativeQuery("select numero_cliente, nombre_cliente, tipo_persona, \n" +
                "segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, \n" +
                "total_activos, total_ventas, tipo_institucion, periodo, observaciones, identificacion \n" +
                "from nexco_segmentos_hist \n" +
                "where "+variable+" = ?;");
        insertFil.setParameter(1,valor);
        return insertFil.getResultList();
    }

    public List<Object[]> findAllFinal(String period) {

        Query insertFil = entityManager.createNativeQuery("select numero_cliente, nombre_cliente, tipo_persona, \n" +
                "segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, \n" +
                "total_activos, total_ventas, tipo_institucion, periodo, observaciones, identificacion \n" +
                "from nexco_segmentos_final where periodo = ? \n" +
                ";");
        insertFil.setParameter(1,period);

        return insertFil.getResultList();
    }
    public List<Object[]> findByFilter(String value, String filter, String period){
        List<Object[]> list = new ArrayList<>();
        switch (filter){
            case "Número Cliente":
                Query identificacion = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE numero_cliente LIKE ? AND periodo = ? ");
                identificacion.setParameter(1,value);
                identificacion.setParameter(2, period);
                list = identificacion.getResultList();
                break;
            case "Nombre Cliente":
                Query divisa = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE nombre_cliente LIKE ? AND periodo = ?");
                divisa.setParameter(1,value);
                divisa.setParameter(2, period);
                list = divisa.getResultList();
                break;
            case "Tipo Persona":
                Query cuenta = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE tipo_persona LIKE ? AND periodo = ?");
                cuenta.setParameter(1,value);
                cuenta.setParameter(2, period);
                list = cuenta.getResultList();
                break;
            case "Segmento Viejo":
                Query empresa = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE segmento_finrep_old LIKE ? AND periodo = ?");
                empresa.setParameter(1,value);
                empresa.setParameter(2, period);
                list = empresa.getResultList();
                break;
            case "Segmento Nuevo":
                Query contrato = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE segmento_finrep_new LIKE ? AND periodo = ?");
                contrato.setParameter(1,value);
                contrato.setParameter(2, period);
                list = contrato.getResultList();
                break;
            case "Corasu":
                Query centro = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE corasu LIKE ? AND periodo = ?");
                centro.setParameter(1,value);
                centro.setParameter(2, period);
                list = centro.getResultList();
                break;
            case "Subcorasu":
                Query tipo = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE subcorasu LIKE ? AND periodo = ?");
                tipo.setParameter(1,value);
                tipo.setParameter(2, period);
                list = tipo.getResultList();
                break;
            case "CIIU":
                Query digito_verif = entityManager.createNativeQuery("SELECT numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, identificacion FROM nexco_segmentos_final WHERE ciiu LIKE ? AND periodo = ?");
                digito_verif.setParameter(1,value);
                digito_verif.setParameter(2, period);
                list = digito_verif.getResultList();
                break;

        }
        return list;
    }


    public ArrayList<String[]> saveFileBD(InputStream file, User user, String period) throws IOException {
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
                error[0] = "Fallo Estructura";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<SegmentosFinalTemp> segList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";

        ArrayList<SubsidiariesTemplateTemporal> toInsert = new ArrayList<>();

        Query deleteSeg1 = entityManager.createNativeQuery("truncate table nexco_segmentos_final_temp ;");
        deleteSeg1.executeUpdate();

        Query deleteSeg = entityManager.createNativeQuery("delete from nexco_segmentos_final where periodo = ? ;");
        deleteSeg.setParameter(1,period);
        deleteSeg.executeUpdate();

        /*Query deleteSegHist = entityManager.createNativeQuery("delete from nexco_segmentos_hist where periodo = ? ;");
        deleteSegHist.setParameter(1,period);
        deleteSegHist.executeUpdate();*/

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellCliente = formatter.formatCellValue(row.getCell(0));
                String cellId = formatter.formatCellValue(row.getCell(1));
                String cellTipoPer = formatter.formatCellValue(row.getCell(3));
                String cellSegmento = formatter.formatCellValue(row.getCell(5));
                String cellobservacion = formatter.formatCellValue(row.getCell(14));


                if (cellCliente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Número de Cliente no puede estar vacío";
                    lista.add(log1);
                }
                try {
                    Double conv = Double.parseDouble(cellCliente.toString());
                } catch(Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Número de Cliente no puede contener letras";
                    lista.add(log1);
                }
                if (cellCliente.trim().length() != 8) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Número de Cliente excede el máximo de longitud permitido (8)";
                    lista.add(log1);
                }
                if (cellId.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Identificación no puede estar vacía";
                    lista.add(log1);
                }
                try {
                    Double conv = Double.parseDouble(cellId.toString());
                } catch(Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Identificación no puede contener letras";
                    lista.add(log1);
                }
                if (cellId.trim().length() != 15) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "La Identificación no tiene la longitud permitida  (15)";
                    lista.add(log1);
                }
                if (cellTipoPer.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El Tipo de Persona no puede estar vacía";
                    lista.add(log1);
                }
                if (cellTipoPer.trim().length() != 1) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El Tipo de Persona no tiene la longitud permitida (1)";
                    lista.add(log1);
                }
                if (cellSegmento.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Segmento no puede estar vacío";
                    lista.add(log1);
                }
                if (cellSegmento.trim().length() != 3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Segmento no tiene la longitud permitida (3)";
                    lista.add(log1);
                }
                if (cellobservacion.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(15);
                    log1[2] = "Las observaciones no pueden estar vacías";
                    lista.add(log1);
                }
                if (cellobservacion.trim().length() > 50) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(15);
                    log1[2] = "Las observaciones superan la longitud permitida (50)";
                    lista.add(log1);
                }

                SegmentosFinalTemp segL = new SegmentosFinalTemp();
                segL.setSegmentoFinrepNew(cellSegmento.toString());
                segL.setNumeroCliente(cellCliente.toString());
                segL.setTipoPersona(cellTipoPer.toString());
                segL.setIdentificacion(cellId.toString());
                segL.setObservaciones(cellobservacion.toString());
                segList.add(segL);


            } else {
                firstRow = 0;
            }
        }

        segmentFinalTempRepository.saveAll(segList);

        Query validate = entityManager.createNativeQuery("select COUNT(1), numero_cliente \n" +
                "from nexco_segmentos_final_temp \n" +
                "group by numero_cliente \n" +
                "having COUNT(1) > 1 \n" +
                ";");
        List<Object[]> val = validate.getResultList();

        if(val.size()>0){

            String[] error = new String[1];
            error[0] = "duplicados";
            lista.add(error);

        }else {

            Query insertSeg = entityManager.createNativeQuery("insert into nexco_segmentos_final (identificacion, numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, observaciones)\n" +
                    "select distinct isnull(a.identificacion, b.identificacion) identificacion, isnull(a.numero_cliente, b.numero_cliente) numero_cliente, a.nombre_cliente, isnull(a.tipo_persona, b.tipo_persona) tipo_persona, a.segmento_finrep, b.segmento_finrep_new, a.corasu, a.subcorasu, a.ciiu, a.numero_empleados, a.total_activos, a.total_ventas, a.tipo_institucion, ?, b.observaciones \n" +
                    "from nexco_segmentos_temp a \n" +
                    "right join nexco_segmentos_final_temp b \n" +
                    "on a.numero_cliente = b.numero_cliente \n" +
                    ";");
            insertSeg.setParameter(1, period);
            insertSeg.executeUpdate();

            Query delSeg = entityManager.createNativeQuery("delete from nexco_segmentos_hist where numero_cliente in (select numero_cliente from nexco_segmentos_final_temp);");
            delSeg.executeUpdate();

            Query insertSeg2 = entityManager.createNativeQuery("insert into nexco_segmentos_hist (identificacion, numero_cliente, nombre_cliente, tipo_persona, segmento_finrep_old, segmento_finrep_new, corasu, subcorasu, ciiu, numero_empleados, total_activos, total_ventas, tipo_institucion, periodo, observaciones)\n" +
                    "select distinct isnull(a.identificacion, b.identificacion) identificacion, isnull(a.numero_cliente, b.numero_cliente) numero_cliente, a.nombre_cliente, isnull(a.tipo_persona, b.tipo_persona) tipo_persona, a.segmento_finrep, b.segmento_finrep_new, a.corasu, a.subcorasu, a.ciiu, a.numero_empleados, a.total_activos, a.total_ventas, a.tipo_institucion, ?, b.observaciones \n" +
                    "from nexco_segmentos_temp a \n" +
                    "right join nexco_segmentos_final_temp b \n" +
                    "on a.numero_cliente = b.numero_cliente \n" +
                    ";");
            insertSeg2.setParameter(1, period);
            insertSeg2.executeUpdate();

            String[] log = new String[3];
            log[2] = stateFinal;
            lista.add(log);

            log[2] = stateFinal;
            lista.add(log);
        }

        return lista;
    }

    public ArrayList<String[]> saveFileBDHist(InputStream file, User user, String period) throws IOException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            Iterator<Row> rows;
            try {
                XSSFWorkbook wb = new XSSFWorkbook(file);
                XSSFSheet sheet = wb.getSheetAt(0);
                rows = sheet.iterator();
                list = validarPlantillaHist(rows, user, period);
            }catch (Exception e){
                String[] error = new String[1];
                error[0] = "Fallo Estructura";
                list.add(error);
                e.printStackTrace();
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaHist(Iterator<Row> rows, User user, String period) {

        ArrayList<String[]> lista = new ArrayList();
        ArrayList<SegmentosFinalTemp> segList = new ArrayList();
        XSSFRow row;
        int firstRow = 1;
        String stateFinal = "true";

        ArrayList<SubsidiariesTemplateTemporal> toInsert = new ArrayList<>();

        Query deleteSeg1 = entityManager.createNativeQuery("truncate table nexco_segmentos_final_temp ;");
        deleteSeg1.executeUpdate();

        while (rows.hasNext()) {
            //String[] log = new String[3];
            row = (XSSFRow) rows.next();
            if (firstRow != 1 && row != null) {
                DataFormatter formatter = new DataFormatter();

                String cellCliente = formatter.formatCellValue(row.getCell(0));
                String cellSegmento = formatter.formatCellValue(row.getCell(5));
                String cellobservacion = formatter.formatCellValue(row.getCell(14));



                if (cellCliente.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Número de Cliente no puede estar vacío";
                    lista.add(log1);
                }
                if (cellCliente.trim().length() > 20) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Número de Cliente excede el máximo de longitud permitido";
                    lista.add(log1);
                }
                if (cellSegmento.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El Segmento no puede estar vacío";
                    lista.add(log1);
                }
                if (cellSegmento.trim().length() != 3) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El Segmento no tiene la longitud permitida (3)";
                    lista.add(log1);
                }
                if (cellobservacion.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "Las observaciones no pueden estar vacías";
                    lista.add(log1);
                }
                if (cellobservacion.trim().length() > 50) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "Las observaciones superan la longitud permitida (50)";
                    lista.add(log1);
                }

                SegmentosFinalTemp segL = new SegmentosFinalTemp();
                segL.setSegmentoFinrepNew(cellSegmento.toString());
                segL.setNumeroCliente(cellCliente.toString());
                segList.add(segL);

                Query queryValidate = entityManager.createNativeQuery("SELECT top 1 numero_cliente FROM nexco_segmentos_hist WHERE numero_cliente = ?;");
                queryValidate.setParameter(1, cellCliente);

                List<Object[]> valSegHist = queryValidate.getResultList();

                if(!valSegHist.isEmpty()){
                    Query queryUdpate = entityManager.createNativeQuery("update nexco_segmentos_hist \n" +
                            "SET segmento_finrep_new = ?, observaciones = ? \n" +
                            "WHERE numero_cliente = ?;");
                    queryUdpate.setParameter(1, cellSegmento);
                    queryUdpate.setParameter(2, cellobservacion);
                    queryUdpate.setParameter(3, cellCliente);
                    queryUdpate.executeUpdate();
                }else{
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "Registro no encontrado por Número de Cliente";
                    lista.add(log1);
                }


            } else {
                firstRow = 0;
            }
        }

        String[] log = new String[3];
        log[2] = stateFinal;
        lista.add(log);

        log[2] = stateFinal;
        lista.add(log);

        return lista;
    }

    public List validateTableSeg(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 numero_cliente FROM nexco_segmentos WHERE periodo = ?;");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List<Object[]> getAllSegmentsCustomers(String periodo,String valor,String item) {

        String variable = item;
        if(item.equals("Número Cliente")){
            variable = "a.numero_cliente";
        } else if(item.equals("Nombre Cliente")){
            variable = "a.nombre_cliente";
        } else if(item.equals("Tipo Persona")){
            variable = "a.tipo_persona";
        } else if(item.equals("Segmento Viejo")){
            variable = "a.segmento_finrep";
        } else if(item.equals("Corasu")){
            variable = "a.corasu";
        } else if(item.equals("Subcorasu")){
            variable = "a.subcorasu";
        } else if(item.equals("CIIU")){
            variable = "a.ciiu";
        }

        Query insertFil = entityManager.createNativeQuery("select 'identificacion;corasu;subcorasu;ciiu;segmento_finrep;tipo_institucion;tipo_persona;numero_cliente;nombre_cliente;numero_empleados;total_activos;total_ventas;segmento_finrep_new;observaciones' plano\n" +
                "union all \n" +
                "select isnull(a.identificacion, '')+';'+isnull(a.corasu, '')+';'+isnull(a.subcorasu, '')+';'+isnull(a.ciiu, '')+';'+isnull(a.segmento_finrep, '')+';'+isnull(a.tipo_institucion, '')+';'+isnull(a.tipo_persona, '')+';'\n" +
                "+isnull(a.numero_cliente, '')+';'+isnull(a.nombre_cliente, '')+';'+isnull(convert(varchar, abs(round(CONVERT(numeric(20),a.numero_empleados), 0))), 0)+';'\n" +
                "+isnull(convert(varchar, abs(round(CONVERT(numeric(20),a.total_activos), 0))), 0)+';'+isnull(convert(varchar, abs(round(CONVERT(float(20),a.total_ventas), 0))), 0)+';'" +
                "+coalesce(b.segmento_finrep_new, c.segmento_finrep_new, '')+';'+isnull(b.observaciones, '') plano\n" +
                "from nexco_segmentos_temp a \n" +
                "left join nexco_segmentos_hist b \n" +
                "on a.numero_cliente = b.numero_cliente \n" +
                "left join (select * from nexco_segmentos where periodo = ?) c \n" +
                "on a.numero_cliente = c.numero_cliente \n" +
                "where "+variable+" = ? ;");
        insertFil.setParameter(1, periodo);
        insertFil.setParameter(2, valor);
        return insertFil.getResultList();
    }

}


