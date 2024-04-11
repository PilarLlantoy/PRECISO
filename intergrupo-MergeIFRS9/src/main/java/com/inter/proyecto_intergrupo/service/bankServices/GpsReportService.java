package com.inter.proyecto_intergrupo.service.bankServices;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.model.parametric.ComerParametric;
import com.inter.proyecto_intergrupo.model.parametric.ResponsibleAccount;
import com.inter.proyecto_intergrupo.repository.bank.GpsReportRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ResponsibleAccountRepository;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class GpsReportService {
    @Autowired
    private final GpsReportRepository gpsReportRepository;
    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private ResponsibleAccountRepository responsibleAccountRepository;

    @Autowired
    public GpsReportService(GpsReportRepository gpsReportRepository) {
        this.gpsReportRepository = gpsReportRepository;
    }


    public List<String[]> validarPlantilla(Iterator<Row> rows) {
        List<String[]> lista = new ArrayList<String[]>();
        XSSFRow row;
        int firstRow = 1;
        String[] log = new String[3];
        log[0] = "0";
        log[1] = "0";
        log[2] = "false";
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                DataFormatter formatter = new DataFormatter();
                String cellCuenta = formatter.formatCellValue(row.getCell(14)).trim();
                String cellClase = formatter.formatCellValue(row.getCell(7)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(10)).trim();
                String cellfecha = formatter.formatCellValue(row.getCell(4)).trim();
                String cellImporte = formatter.formatCellValue(row.getCell(11)).trim();
                log[0] = String.valueOf(row.getRowNum());

                if (cellCuenta.isEmpty() || cellCuenta.isBlank() || validatePUCAccount(cellCuenta)) {
                    log[1] = CellReference.convertNumToColString(14) + " - (15)";
                    log[2] = "false";
                } else if (cellImporte.isEmpty() || cellImporte.isBlank()) {
                    log[1] = CellReference.convertNumToColString(11) + " - (12)";
                    log[2] = "false";
                } else {
                    try {
                        log[1] = CellReference.convertNumToColString(14) + " - (15)";
                        Long.parseLong(cellCuenta);
                        log[2] = "true";
                    } catch (Exception e) {
                        log[2] = "falseFormat";
                        lista.add(log);
                    }
                }
            }
        }
        lista.add(log);
        return lista;
    }

    public List<GpsReport> findAll() {
        return gpsReportRepository.findAll();
    }

    public List<GpsReport> findByPeriodo(String period) {
        return gpsReportRepository.findByEjercicioMes(period);
    }

    public boolean validatePUCAccount(String cuenta) {
        Query neoconQuery = entityManager.createNativeQuery("SELECT NUCTA FROM CUENTAS_PUC WHERE NUCTA = ?");
        neoconQuery.setParameter(1, cuenta);
        List neoconResult = neoconQuery.getResultList();

        return !neoconResult.isEmpty();
    }

    public List<String[]> loadQueryDatabase(InputStream file, String periodo) {

        List<String[]> lista = new ArrayList<String[]>();
        int success = 0;
        int fail = 0;

        Scanner scan = new Scanner(file);
        int cont = 0;
        int correctFile = 0;
        String cabecera = "Nombre1|Razónsocial|Nºident.fis.1|NIFdelproveedor|Soc.|C";
        HashMap<String, Boolean> map = getCuentas();
        HashMap<String, ComerParametric> mapComers = getComers();
        List<GpsReport> listGps = new ArrayList<GpsReport>();

        //validar doc
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            line = line.replaceAll("\\s+", "");

            if (line.contains(cabecera)) {
                correctFile++;
                break;
            }
        }

        if (correctFile > 0) {

            Query deleteFromGps = entityManager.createNativeQuery("DELETE FROM nexco_gpsreport  WHERE ejercicio_mes = ?");
            deleteFromGps.setParameter(1, periodo);
            deleteFromGps.executeUpdate();

            while (scan.hasNextLine()) {
                String line = scan.nextLine();
                line = line.replaceAll("\\s+", "");

                if (StringUtils.countMatches(line, "|") == 27 && !line.contains(cabecera) && !line.contains("|*|||||")) {
                    String[] data = line.split("\\|");
                    try {
                        String[] fecont = data[13].split("/");
                        String periodoLine = fecont[0] + "-" + fecont[1];
                        if (map.get(data[8]) != null && map.get(data[8]) && periodoLine.equals(periodo)) {
                            GpsReport gpsReport = new GpsReport();
                            gpsReport.setNombre1(data[1]);
                            gpsReport.setRazon_social(data[2]);
                            gpsReport.setIdent_fis(data[3]);
                            gpsReport.setNif(data[4]);
                            gpsReport.setSoc(data[5]);
                            gpsReport.setClase(data[6]);
                            gpsReport.setCuenta(data[7]);
                            gpsReport.setCuenta_local(data[8]);
                            if (data[9].contains("-")) {
                                String valor = "-" + data[9].replaceAll("\\s+", "").replace("-", "");
                                gpsReport.setImporte_md(valor);
                            } else {
                                String valor = data[9].replaceAll("\\s+", "");
                                gpsReport.setImporte_md(valor);
                            }
                            gpsReport.setTipo_cambio(data[10]);
                            gpsReport.setMon1(data[11]);
                            gpsReport.setFecont(data[12]);
                            gpsReport.setEjercicioMes(fecont[0] + "-" + fecont[1]);
                            gpsReport.setCe_coste(data[14]);
                            gpsReport.setFecha_doc(data[15]);
                            gpsReport.setTexto(data[16]);
                            gpsReport.setTexto_camb(data[17]);
                            gpsReport.setDivisa(data[18]);
                            if (data[19].contains("-")) {
                                String valor = "-" + data[19].replaceAll("\\s+", "").replace("-", "");
                                gpsReport.setImporte_ml(valor);
                            } else {
                                String valor = data[19].replaceAll("\\s+", "");
                                gpsReport.setImporte_ml(valor);
                            }
                            gpsReport.setReferencia(data[20]);
                            if (data.length < 22)
                                gpsReport.setNumero_doc("");
                            else
                                gpsReport.setNumero_doc(data[21]);
                            if (data.length < 23)
                                gpsReport.setClave_3("");
                            else
                                gpsReport.setClave_3(data[22]);
                            if (data.length < 24)
                                gpsReport.setDoc_comp("");
                            else
                                gpsReport.setDoc_comp(data[23]);
                            if (data.length < 25)
                                gpsReport.setArchv_fijo("");
                            else
                                gpsReport.setArchv_fijo(data[24]);
                            if (data.length < 26)
                                gpsReport.setElemento_pep("");
                            else
                                gpsReport.setElemento_pep(data[25]);
                            if (data.length < 27) {
                                gpsReport.setUsuario_em(" ");
                            } else {
                                gpsReport.setUsuario_em(data[26]);
                            }
                            if (mapComers.get(data[8]) != null && mapComers.get(data[8]).getClase() == gpsReport.getClase() && mapComers.get(data[8]).getDocCompr() == gpsReport.getDoc_comp()) {
                                gpsReport.setProrrata_iva(mapComers.get(data[8]).getProIva());
                                gpsReport.setTipo_importe(mapComers.get(data[8]).getImporte());
                            }
                            success++;
                            listGps.add(gpsReport);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                cont++;
            }
        }
        if (correctFile > 0){
            gpsReportRepository.saveAll(listGps);
            String[] logFinal = new String[4];
            logFinal[0] = String.valueOf(success);
            logFinal[1] = String.valueOf(fail);
            lista.add(logFinal);
        } else{
            String[] logFinal = new String[4];
            logFinal[0] = String.valueOf(success);
            logFinal[1] = String.valueOf(1);
            lista.add(logFinal);
        }



        return lista;
    }

    public HashMap<String, ComerParametric> getComers() {
        HashMap<String, ComerParametric> map = new HashMap<String, ComerParametric>();
        Query getInfo = entityManager.createNativeQuery("SELECT * FROM nexco_parametrica_metodo_comer", ComerParametric.class);
        List<ComerParametric> comers = getInfo.getResultList();
        for (ComerParametric comer : comers)
            map.put(comer.getCuentaLocal(), comer);
        return map;
    }

    public HashMap<String, Boolean> getCuentas() {
        List<ResponsibleAccount> responsibleAccount = responsibleAccountRepository.findAll();
        HashMap<String, Boolean> map = new HashMap<String, Boolean>();
        for (ResponsibleAccount responsibleAccount1 : responsibleAccount) {
            map.put(responsibleAccount1.getCuentaLocal().toString(), responsibleAccount1.getMetodologia());
        }
        return map;
    }

    public List<GpsReport> matchGPSForm(String fecont) {
        List<GpsReport> toReturn;

        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_gpsreport as gps WHERE gps.perimetro_comer = 'Sí' AND ejercicio_mes = ?", GpsReport.class);
        result.setParameter(1, fecont);

        if (!result.getResultList().isEmpty()) {
            toReturn = result.getResultList();
        } else {
            toReturn = new ArrayList<>();
        }

        return toReturn;
    }

    public void valdatePreComerAndInsert(String periodo, User user) {

        Query insertParametric = entityManager.createNativeQuery("IF EXISTS \n" +
                "(SELECT gps.clase, gps.cuenta_local,gps.doc_comp FROM \n" +
                "nexco_parametrica_metodo_comer as comer,\n" +
                "(SELECT gps.doc_comp, gps.cuenta_local, gps.clase FROM nexco_gpsreport AS gps WHERE \n" +
                "gps.doc_comp IN(SELECT doc_comp FROM nexco_gpsreport WHERE ident_fis = '9002492975' AND doc_comp != '')  \n" +
                "OR gps.doc_comp IN(SELECT doc_comp FROM nexco_gpsreport WHERE nif = '9002492975'  AND doc_comp != '')\n" +
                "AND ejercicio_mes = '" + periodo + "') as gps\n" +
                "WHERE gps.cuenta_local NOT IN (SELECT com.cuenta_local FROM nexco_parametrica_metodo_comer as com) AND gps.clase NOT IN (SELECT com.clase FROM nexco_parametrica_metodo_comer as com) AND gps.doc_comp NOT IN (SELECT com.doc_compr FROM nexco_parametrica_metodo_comer as com) \n" +
                "GROUP BY gps.clase, gps.cuenta_local,gps.doc_comp)\n" +
                "INSERT INTO nexco_parametrica_metodo_comer(cuenta_local,clase,nombre_clase,doc_compr,prorrata_iva,tipo_importe)\n" +
                "(SELECT gps.cuenta_local, gps.clase, CASE WHEN gps.clase = 'S2' THEN 'Provisiones' ELSE 'Pagos Reales' END ,CASE WHEN gps.doc_comp = '' THEN 'General' ELSE gps.doc_comp END, CASE WHEN gps.prorrata_iva is null THEN '0.15' ELSE gps.prorrata_iva END, CASE WHEN gps.clase = 'S2' THEN 'P' ELSE 'R' END \n" +
                "FROM  nexco_parametrica_metodo_comer as comer,\n" +
                "(SELECT * FROM nexco_gpsreport AS gps WHERE \n" +
                "gps.doc_comp IN(SELECT doc_comp FROM nexco_gpsreport WHERE ident_fis = '9002492975' AND doc_comp != '')  \n" +
                "OR gps.doc_comp IN(SELECT doc_comp FROM nexco_gpsreport WHERE nif = '9002492975'  AND doc_comp != '')\n" +
                "AND ejercicio_mes = '" + periodo + "') as gps\n" +
                "WHERE gps.cuenta_local NOT IN (SELECT com.cuenta_local FROM nexco_parametrica_metodo_comer as com) AND gps.clase NOT IN (SELECT com.clase FROM nexco_parametrica_metodo_comer as com) AND gps.doc_comp NOT IN (SELECT com.doc_compr FROM nexco_parametrica_metodo_comer as com) \n" +
                "GROUP BY gps.clase, gps.cuenta_local,gps.doc_comp, gps.prorrata_iva)");

        insertParametric.executeUpdate();

        //Marcar como precarga
        Query markAsPre = entityManager.createNativeQuery("BEGIN \n" +

                "UPDATE nexco_gpsreport SET perimetro_comer = 'Sí' \n" +
                "FROM nexco_gpsreport as gps \n" +
                "INNER JOIN nexco_parametrica_metodo_comer as comer ON gps.cuenta_local = comer.cuenta_local \n" +
                "WHERE  ejercicio_mes = '" + periodo + "' AND gps.clase= comer.clase AND comer.tipo_importe = 'P' \n" +
                "\n" +
                "UPDATE nexco_gpsreport SET perimetro_comer = 'Sí'\n" +
                "WHERE ident_fis = '9002492975'\n" +
                "AND ejercicio_mes = '" + periodo + "' AND cuenta_local IN (SELECT cuenta_local FROM nexco_cuentas_responsables where aplica_metodologia = 1)\n" +
                "\n" +
                "UPDATE nexco_gpsreport SET perimetro_comer = 'Sí'\n" +
                "WHERE nif = '9002492975'\n" +
                "AND ejercicio_mes = '" + periodo + "' AND cuenta_local IN (SELECT cuenta_local FROM nexco_cuentas_responsables where aplica_metodologia = 1)\n" +
                "\n" +
                "UPDATE nexco_gpsreport SET perimetro_comer = 'Sí'\n" +
                "WHERE \n" +
                "doc_comp IN(SELECT doc_comp FROM nexco_gpsreport WHERE ident_fis = '9002492975' AND doc_comp != '') \n" +
                "OR\n" +
                "doc_comp IN(SELECT doc_comp FROM nexco_gpsreport WHERE nif = '9002492975' AND doc_comp != '')\n" +
                "AND ejercicio_mes = '" + periodo + "' AND cuenta_local IN (SELECT cuenta_local FROM nexco_cuentas_responsables where aplica_metodologia = 1)\n" +
                "\n" +
                "UPDATE nexco_gpsreport SET perimetro_comer = 'Sí'\n" +
                "WHERE doc_comp IN (SELECT doc_compr FROM nexco_parametrica_metodo_comer as comer WHERE comer.doc_compr != 'General')\n" +
                "\n" +
                "END");
        markAsPre.executeUpdate();

        String empresa = user.getEmpresa();

        Query deleteComer = entityManager.createNativeQuery("DELETE FROM nexco_precarga_comer WHERE periodo = ?");
        deleteComer.setParameter(1, periodo);
        deleteComer.executeUpdate();

        Query insertIntoPre = entityManager.createNativeQuery("INSERT INTO nexco_precarga_comer (yntp_empresa_reportante,cod_neocon,divisa,yntp,sociedad_yntp,contrato,nit,valor,cod_pais,pais,cuenta_local,observaciones,periodo)\n" +
                "SELECT '" + empresa + "',b.CODICONS46, a.divisa,yntp.yntp, yntp.sociedad_corta ,a.numero_doc,ter.nit_contraparte,a.importe_ml,p.id_pais,p.nombre_pais, a.cuenta_local, a.nombre_clase, '" + periodo + "'  FROM \n" +
                "(SELECT gps.divisa,gps.numero_doc, CONVERT(numeric, gps.importe_ml) as importe_ml, gps.cuenta_local, comer.clase, comer.nombre_clase\n" +
                "FROM nexco_gpsreport as gps \n" +
                "INNER JOIN (SELECT clase, nombre_clase FROM nexco_parametrica_metodo_comer GROUP BY clase, nombre_clase) as comer on comer.clase = gps.clase\n" +
                "WHERE gps.perimetro_comer = 'Sí' AND gps.ejercicio_mes = '" + periodo + "') as a\n" +
                "INNER JOIN (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013' GROUP BY puc.CODICONS46 , puc.NUCTA) b ON b.NUCTA = a.cuenta_local\n" +
                "INNER JOIN nexco_terceros as ter ON ter.nit_contraparte = '900249297'\n" +
                "INNER JOIN nexco_sociedades_yntp as yntp ON  yntp.yntp = '00570'\n" +
                "INNER JOIN nexco_paises as p ON p.id_pais = yntp.id_pais ORDER BY cuenta_local");

        insertIntoPre.executeUpdate();
    }

}
