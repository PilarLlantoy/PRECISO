package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.F351Icrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaCalculoIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.CalculoIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.F351IcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaCalculoIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaF351IcrvRepository;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
@Transactional
public class F351IcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private F351IcrvRepository f351IcrvRepository;

    private PlantillaF351IcrvRepository plantillaF351IcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("F351 ICRV");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public F351Icrv findByIdF(Long id){
        return f351IcrvRepository.findByIdF(id);
    }

    public List<F351Icrv> findAllF351(String periodo)
    {
        return f351IcrvRepository.findByPeriodo(periodo);
    }

    public void completeTable(String periodo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);

        Query query = entityManager.createNativeQuery("delete from nexco_f351_icrv where periodo = :periodo  ;\n" +
                "insert into nexco_f351_icrv (fecha_proceso,nro_asignado,codigo_puc,nit,documento_emisor,razon_social_emisor,vinculado,aval,tipo_identificacion_aval,identificacion_aval,razon_social_aval,periodo) \n" +
                "(select :ultimoDiaDelMes as fecha_proceso,nro_asignado,codigo_puc,nit,documento_emisor,razon_social_emisor,vinculado,aval,tipo_identificacion_aval,identificacion_aval,razon_social_aval, :periodo from nexco_plantilla_f351_icrv);");
        query.setParameter("periodo",periodo);
        query.setParameter("ultimoDiaDelMes",ultimoDiaDelMes);
        query.executeUpdate();

    }

    public void clearF351(User user, String periodo){
        completeTable(periodo);
        loadAudit(user,"Generación Exitosa de F531 para "+periodo);
    }

    public ArrayList<String[]> saveFileBDPlantilla(InputStream file, User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows, periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Plantilla F531 ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla F531 ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<F351Icrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";

        DateTimeFormatter formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter1);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter1);

        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    int count = 1;
                    String cellNroAsignado = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodigoPuc = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNit = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellDocumentoEmisor = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRazonSocialEmisor = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellVinculado = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellAval = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTipoIdentificacionAval = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellIdentificacionAval = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRazonSocialAval = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellIdentifacionAdministrador = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRazonSocialAdministrador = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellClaseInversion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNemotecnico = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCuponPrincipal = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaEmite = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaVcto = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaVctoCupon = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaCompra = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodMoneda = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorNominal = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellAmortizaciones = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorNominalCapitalizado = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNumeroAcciones = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellClaseAccion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorCompra = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorCompraPesos = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTasaFacial = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorTasa = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCalculoInteres = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellPeriodicidadPago = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellModalidad = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellIndTasaReferencia = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorMercado1316 = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorPresentePesos = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorMercadoDifPeso = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTasaNegociacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellDiasVcto = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTasaReferencia = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorTasaReferencia = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorTasaPrimerFlujo = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellMargenValora = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTasaDescuento = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellPrecio = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellMetodoValora = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaUltimoReprecio = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorPresenteReprecio = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellIndBursatibilidad = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellInteresVencidos = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellPucProvision = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellBaseProvision = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorProvision = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCalificacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellEntidadCalificadora = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCalificacionRiesgo = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCalificacionAvalista = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCalificacionSoberania = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellEntidadCalificadoraSoberania = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCustodio = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNumeroIdentificacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFungible = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellMontoEmision = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellPorcentajeParticipacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRamo = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRelacionMatrix = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellConcentracionPropiedad = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRelacionVinculacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodigoPucCausacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCausacionValoracion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodigoPucCausaPat = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCausaValoracionPat = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaCorte = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellUnidadCaptura = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodEmp = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellPortafolio = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTipoEvaluacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTipoFideicomiso = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodFideicomiso = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTipoEntidadVig = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCodEntidadVig = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorValorizacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValorDesvaloriza = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellFechaTasaRef = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNumeroAsignado = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellVlrMercadoInv = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNegocioDn02 = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellOperacionFuturo = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellValoracionPortafolio = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellTipoTitulo = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellIsinStar = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellRegistroManual = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellCiiu = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellNaturalezaJuridica = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellVinculacion = formatter.formatCellValue(row.getCell(count++)).trim();
                    String cellProveedorDePrecios = formatter.formatCellValue(row.getCell(count++)).trim();

                    if (cellNroAsignado.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Nro Asignado no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodigoPuc.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Código PUC no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellNit.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo NIT no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellDocumentoEmisor.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Documento Emisor no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellRazonSocialEmisor.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Razón Social Emiasor no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellValorNominal.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(21);
                            cell.setCellType(CellType.STRING);
                            cellValorNominal = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorNominal);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(21);
                            log[2] = "El campo Valor Nominal debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorNominalCapitalizado.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(23);
                            cell.setCellType(CellType.STRING);
                            cellValorNominalCapitalizado = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorNominalCapitalizado);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(23);
                            log[2] = "El campo Valor Nominal Capitalizado debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellNumeroAcciones.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(24);
                            cell.setCellType(CellType.STRING);
                            cellNumeroAcciones = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellNumeroAcciones);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(24);
                            log[2] = "El campo Número Acciones debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorCompra.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(26);
                            cell.setCellType(CellType.STRING);
                            cellValorCompra = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorCompra);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(26);
                            log[2] = "El campo Valor Compra debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorCompraPesos.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(27);
                            cell.setCellType(CellType.STRING);
                            cellValorCompraPesos = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorCompraPesos);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(27);
                            log[2] = "El campo Valor Compra Pesos debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorTasa.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(29);
                            cell.setCellType(CellType.STRING);
                            cellValorTasa = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorTasa);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(29);
                            log[2] = "El campo Valor Tasa debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorMercado1316.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(34);
                            cell.setCellType(CellType.STRING);
                            cellValorMercado1316 = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorMercado1316);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(34);
                            log[2] = "El campo Valor Mercado 1316 debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorPresentePesos.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(35);
                            cell.setCellType(CellType.STRING);
                            cellValorPresentePesos = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorPresentePesos);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(35);
                            log[2] = "El campo Valor Presente Pesos debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorMercadoDifPeso.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(36);
                            cell.setCellType(CellType.STRING);
                            cellValorMercadoDifPeso = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorMercadoDifPeso);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(36);
                            log[2] = "El campo Valor Mercado Dif Pesos debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorTasaReferencia.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(40);
                            cell.setCellType(CellType.STRING);
                            cellValorTasaReferencia = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorTasaReferencia);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(40);
                            log[2] = "El campo Valor Tasa Referencia debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorTasaPrimerFlujo.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(41);
                            cell.setCellType(CellType.STRING);
                            cellValorTasaPrimerFlujo = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorTasaPrimerFlujo);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(41);
                            log[2] = "El campo Valor Tasa Primer Flujo debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellMetodoValora.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(45);
                            cell.setCellType(CellType.STRING);
                            cellMetodoValora = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellMetodoValora);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(45);
                            log[2] = "El campo Método Valora debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorPresenteReprecio.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(47);
                            cell.setCellType(CellType.STRING);
                            cellValorPresenteReprecio = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorPresenteReprecio);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(47);
                            log[2] = "El campo Valor Presente Reprecio debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellValorProvision.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(52);
                            cell.setCellType(CellType.STRING);
                            cellValorProvision = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellValorProvision);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(52);
                            log[2] = "El campo Valor Provisión debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellRelacionMatrix.length() != 0) {
                        try
                        {
                            XSSFCell cell= row.getCell(65);
                            cell.setCellType(CellType.STRING);
                            cellRelacionMatrix = formatter.formatCellValue(cell).replace(" ", "").replace("%","").replace(",", "");
                            Double.parseDouble(cellRelacionMatrix);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(65);
                            log[2] = "El campo Relación Matrix debe ser numérico.";
                            lista.add(log);
                        }
                    }

                    if(lista.size() == 0) {
                        F351Icrv data = new F351Icrv();
                        data.setFechaProceso(ultimoDiaDelMes);
                        data.setNroAsignado(cellNroAsignado);
                        data.setCodigoPuc(cellCodigoPuc);
                        data.setNit(cellNit);
                        data.setDocumentoEmisor(cellDocumentoEmisor);
                        data.setRazonSocialEmisor(cellRazonSocialEmisor);
                        data.setVinculado(cellVinculado);
                        data.setAval(cellAval);
                        data.setTipoIdentificacionAval(cellTipoIdentificacionAval);
                        data.setIdentificacionAval(cellIdentificacionAval);
                        data.setRazonSocialAval(cellRazonSocialAval);
                        data.setIdentifacionAdministrador(cellIdentifacionAdministrador);
                        data.setRazonSocialAdministrador(cellRazonSocialAdministrador);
                        data.setClaseInversion(cellClaseInversion);
                        data.setNemotecnico(cellNemotecnico);
                        data.setCuponPrincipal(cellCuponPrincipal);
                        data.setFechaEmite(cellFechaEmite);
                        data.setFechaVcto(cellFechaVcto);
                        data.setFechaVctoCupon(cellFechaVctoCupon);
                        data.setFechaCompra(cellFechaCompra);
                        data.setCodMoneda(cellCodMoneda);
                        data.setAmortizaciones(cellAmortizaciones);
                        data.setClaseAccion(cellClaseAccion);
                        data.setTasaFacial(cellTasaFacial);
                        data.setCalculoInteres(cellCalculoInteres);
                        data.setPeriodicidadPago(cellPeriodicidadPago);
                        data.setModalidad(cellModalidad);
                        data.setIndTasaReferencia(cellIndTasaReferencia);
                        data.setTasaNegociacion(cellTasaNegociacion);
                        data.setDiasVcto(cellDiasVcto);
                        data.setTasaReferencia(cellTasaReferencia);
                        data.setMargenValora(cellMargenValora);
                        data.setTasaDescuento(cellTasaDescuento);
                        data.setPrecio(cellPrecio);
                        data.setFechaUltimoReprecio(cellFechaUltimoReprecio);
                        data.setIndBursatibilidad(cellIndBursatibilidad);
                        data.setInteresVencidos(cellInteresVencidos);
                        data.setPucProvision(cellPucProvision);
                        data.setBaseProvision(cellBaseProvision);
                        data.setCalificacion(cellCalificacion);
                        data.setEntidadCalificadora(cellEntidadCalificadora);
                        data.setCalificacionRiesgo(cellCalificacionRiesgo);
                        data.setCalificacionAvalista(cellCalificacionAvalista);
                        data.setCalificacionSoberania(cellCalificacionSoberania);
                        data.setEntidadCalificadoraSoberania(cellEntidadCalificadoraSoberania);
                        data.setCustodio(cellCustodio);
                        data.setNumeroIdentificacion(cellNumeroIdentificacion);
                        data.setFungible(cellFungible);
                        data.setMontoEmision(cellMontoEmision);
                        data.setPorcentajeParticipacion(cellPorcentajeParticipacion);
                        data.setRamo(cellRamo);
                        data.setConcentracionPropiedad(cellConcentracionPropiedad);
                        data.setRelacionVinculacion(cellRelacionVinculacion);
                        data.setCodigoPucCausacion(cellCodigoPucCausacion);
                        data.setCausacionValoracion(cellCausacionValoracion);
                        data.setCodigoPucCausaPat(cellCodigoPucCausaPat);
                        data.setCausaValoracionPat(cellCausaValoracionPat);
                        data.setFechaCorte(cellFechaCorte);
                        data.setUnidadCaptura(cellUnidadCaptura);
                        data.setCodEmp(cellCodEmp);
                        data.setPortafolio(cellPortafolio);
                        data.setTipoEvaluacion(cellTipoEvaluacion);
                        data.setTipoFideicomiso(cellTipoFideicomiso);
                        data.setCodFideicomiso(cellCodFideicomiso);
                        data.setTipoEntidadVig(cellTipoEntidadVig);
                        data.setCodEntidadVig(cellCodEntidadVig);
                        data.setValorValorizacion(cellValorValorizacion);
                        data.setValorDesvaloriza(cellValorDesvaloriza);
                        data.setFechaTasaRef(cellFechaTasaRef);
                        data.setNumeroAsignado(cellNumeroAsignado);
                        data.setVlrMercadoInv(cellVlrMercadoInv);
                        data.setNegocioDn02(cellNegocioDn02);
                        data.setOperacionFuturo(cellOperacionFuturo);
                        data.setValoracionPortafolio(cellValoracionPortafolio);
                        data.setTipoTitulo(cellTipoTitulo);
                        data.setIsinStar(cellIsinStar);
                        data.setRegistroManual(cellRegistroManual);
                        data.setCiiu(cellCiiu);
                        data.setNaturalezaJuridica(cellNaturalezaJuridica);
                        data.setVinculacion(cellVinculacion);
                        data.setProveedorDePrecios(cellProveedorDePrecios);
                        data.setPeriodo(periodo);

                        if (cellValorNominal.length() != 0)
                            data.setValorNominal(Double.parseDouble(cellValorNominal));
                        else
                            data.setValorNominal(0.00);

                        if (cellValorNominalCapitalizado.length() != 0)
                            data.setValorNominalCapitalizado(Double.parseDouble(cellValorNominalCapitalizado));
                        else
                            data.setValorNominalCapitalizado(0.00);

                        if (cellNumeroAcciones.length() != 0)
                            data.setNumeroAcciones(Double.parseDouble(cellNumeroAcciones));
                        else
                            data.setNumeroAcciones(0.00);

                        if (cellValorCompra.length() != 0)
                            data.setValorCompra(Double.parseDouble(cellValorCompra));
                        else
                            data.setValorCompra(0.00);

                        if (cellValorCompraPesos.length() != 0)
                            data.setValorCompraPesos(Double.parseDouble(cellValorCompraPesos));
                        else
                            data.setValorCompraPesos(0.00);

                        if (cellValorTasa.length() != 0)
                            data.setValorTasa(Double.parseDouble(cellValorTasa));
                        else
                            data.setValorTasa(0.00);

                        if (cellValorMercado1316.length() != 0)
                            data.setValorMercado1316(Double.parseDouble(cellValorMercado1316));
                        else
                            data.setValorMercado1316(0.00);

                        if (cellValorPresentePesos.length() != 0)
                            data.setValorPresentePesos(Double.parseDouble(cellValorPresentePesos));
                        else
                            data.setValorPresentePesos(0.00);

                        if (cellValorMercadoDifPeso.length() != 0)
                            data.setValorMercadoDifPeso(Double.parseDouble(cellValorMercadoDifPeso));
                        else
                            data.setValorMercadoDifPeso(0.00);

                        if (cellValorTasaReferencia.length() != 0)
                            data.setValorTasaReferencia(Double.parseDouble(cellValorTasaReferencia));
                        else
                            data.setValorTasaReferencia(0.00);

                        if (cellValorTasaPrimerFlujo.length() != 0)
                            data.setValorTasaPrimerFlujo(Double.parseDouble(cellValorTasaPrimerFlujo));
                        else
                            data.setValorTasaPrimerFlujo(0.00);

                        if (cellMetodoValora.length() != 0)
                            data.setMetodoValora(Double.parseDouble(cellMetodoValora));
                        else
                            data.setMetodoValora(0.00);

                        if (cellValorPresenteReprecio.length() != 0)
                            data.setValorPresenteReprecio(Double.parseDouble(cellValorPresenteReprecio));
                        else
                            data.setValorPresenteReprecio(0.00);

                        if (cellValorProvision.length() != 0)
                            data.setValorProvision(Double.parseDouble(cellValorProvision));
                        else
                            data.setValorProvision(0.00);

                        if (cellRelacionMatrix.length() != 0)
                            data.setRelacionMatrix(Double.parseDouble(cellRelacionMatrix));
                        else
                            data.setRelacionMatrix(0.00);

                        toInsert.add(data);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 96) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            f351IcrvRepository.deleteByPeriodo(periodo);
            f351IcrvRepository.saveAll(toInsert);

            Query query = entityManager.createNativeQuery("delete from nexco_plantilla_f351_icrv;\n" +
                    "insert into nexco_plantilla_f351_icrv (fecha_proceso,nro_asignado,codigo_puc,nit,documento_emisor,razon_social_emisor,vinculado,aval,tipo_identificacion_aval,identificacion_aval,razon_social_aval) \n" +
                    "(select fecha_proceso,nro_asignado,codigo_puc,nit,documento_emisor,razon_social_emisor,vinculado,aval,tipo_identificacion_aval,identificacion_aval,razon_social_aval from nexco_f351_icrv where periodo = :periodo );");
            query.setParameter("periodo",periodo);
            query.executeUpdate();

            /*Query query1 = entityManager.createNativeQuery("");
            query1.setParameter("periodo",periodo);
            query1.setParameter("ultimoDiaDelMes",ultimoDiaDelMes);
            query1.executeUpdate();*/
        }
        toInsert.clear();
        return lista;
    }

}


