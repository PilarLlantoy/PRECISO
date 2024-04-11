package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.CalculoIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaCalculoIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaPrecioIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PrecioIcrvRepository;
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
public class CalculoIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private PlantillaCalculoIcrvRepository plantillaCalculoIcrvRepository;

    @Autowired
    private CalculoIcrvRepository calculoIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Cálculo ICRV");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public CalculoIcrv findByIdCalculo(Long id){
        return calculoIcrvRepository.findByIdCalculo(id);
    }

    public CalculoIcrv modifyCalculo(CalculoIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdCalculo()+") Cálculo ICRV");
        return calculoIcrvRepository.save(toModify);
    }

    public List<PlantillaCalculoIcrv> findAllPlantilla()
    {
        return plantillaCalculoIcrvRepository.findAll();
    }
    public List<CalculoIcrv> findAllCalculo(String periodo)
    {
        return calculoIcrvRepository.findByPeriodo(periodo);
    }
    public List<Object[]> findAllNota(String periodo)
    {
        Query query = entityManager.createNativeQuery("select cuenta,case when naturaleza = 'H' then ajuste*-1 else ajuste end as ajuste,a.empresa,a.periodo,nit,dv,naturaleza from nexco_calculo_icrv a left join (select * from nexco_base_icrv where naturaleza != '' ) b on a.empresa=b.empresa where a.periodo = :periodo and a.ajuste != 0 ");
        query.setParameter("periodo",periodo);
        return query.getResultList();
    }

    public void completeTable(String periodo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);

        Query query = entityManager.createNativeQuery("delete from nexco_query_icrv where fecont_icrv = :periodo ;\n" +
                "insert into nexco_query_icrv (empresa,nucta,fecont,coddiv,codicons,codigest,fechproce,salmed,salmedd,salmes,salmesd,divisa,saldoquery,saldoquerydivisa,fecont_icrv, empresa_icrv,evento,n2) " +
                "(select a.*, :periodo as fecont_icrv, b.empresa as empresa_icrv,b.evento as evento, substring(a.nucta,1,2) as n2 \n" +
                "from nexco_query a left join nexco_base_icrv b on a.nucta = b.cuenta);");
        query.setParameter("periodo",ultimoDiaDelMes);
        query.executeUpdate();

        Query limpieza = entityManager.createNativeQuery("delete from nexco_calculo_icrv where periodo = :periodo ;\n" +
                "insert into nexco_calculo_icrv (valoracion, empresa, nit, dv, isin, participacion, vr_accion, no_acciones, periodo) \n" +
                "select valoracion, empresa, nit, dv, isin, participacion, vr_accion, no_acciones, :periodo from nexco_calculo_plantilla_icrv;");
        limpieza.setParameter("periodo",periodo);
        limpieza.executeUpdate();

        Query validate = entityManager.createNativeQuery("update nexco_calculo_icrv set valor_nominal=isnull(vr_accion,0)*isnull(no_acciones,0) where periodo = :periodo ;\n" +
                "update a set a.precio = isnull(b.precio_valoracion,0), a.vr_patrimonio = isnull(b.patrimonio,0) * isnull(a.participacion,0) from (select * from nexco_calculo_icrv where periodo = :periodo ) as a, (select * from nexco_precio_icrv where periodo = :periodo ) as b where a.empresa = b.empresa;\n" +
                "update nexco_calculo_icrv set vr_mercado=case when valoracion = 'MERCADO' then isnull(no_acciones,0) * isnull(precio,0) else isnull(vr_patrimonio,0) end where periodo = :periodo ;\n" +
                "update a set a.saldo_libros_valoracion = isnull(b.salmes,0) from (select * from nexco_calculo_icrv where periodo = :periodo ) as a, (select empresa_icrv,sum(salmes) as salmes from nexco_query_icrv where evento = 'INV' group by empresa_icrv) as b where a.empresa = b.empresa_icrv;\n" +
                "update nexco_calculo_icrv set ajuste = isnull(vr_patrimonio,0)-isnull(saldo_libros_valoracion,0) where periodo = :periodo ;\n" +
                "update b set b.dividendos_pagados_acciones = isnull(a.accion,0) from (select * from nexco_pdu_icrv where periodo = :periodo ) as a, (select * from nexco_calculo_icrv where periodo = :periodo ) as b where a.entidad = b.empresa ;");
        validate.setParameter("periodo",periodo);
        validate.executeUpdate();
    }

    public void clearCalculo(User user, String periodo){
        completeTable(periodo);
        loadAudit(user,"Generación Exitosa de Cálculo para "+periodo);
    }

    @Scheduled(cron = "0 5 2 1 * ?")
    public void jobCalculo(){
        validateCalculo();
    }
    @Scheduled(cron = "0 5 7 1 * ?")
    public void jobCalculo2(){
        validateCalculo();
    }

    public void validateCalculo(){
        Date today = new Date();
        String todayString="";
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        if(calendar.get(Calendar.MONTH)==0)
        {
            calendar.add(Calendar.YEAR,-1);
            todayString = calendar.get(Calendar.YEAR) + "-12";
        }
        else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
            todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
        } else {
            todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
        }
        User user = new User();
        user.setCentro("");
        user.setNombre("SYSTEM JOB");
        user.setUsuario("");

        if(findAllCalculo(todayString).size()==0)
        {
            completeTable(todayString);
            loadAudit(user,"Ejecución Exitosa JOB Cálculo ICRV");
        }
        else{
            loadAudit(user,"Ejecución Anulada JOB Cálculo ICRV");
        }
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
                loadAudit(user,"Cargue Exitoso Plantilla Cálculo ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla Cálculo ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<PlantillaCalculoIcrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellValoracion = formatter.formatCellValue(row.getCell(0)).trim().toUpperCase();
                    String cellEmpresa = formatter.formatCellValue(row.getCell(1)).trim().toUpperCase();
                    String cellNit = formatter.formatCellValue(row.getCell(2)).trim().replace(".","");
                    String cellDv = formatter.formatCellValue(row.getCell(3)).trim();
                    String cellIsin = formatter.formatCellValue(row.getCell(4)).trim();

                    XSSFCell cell2= row.getCell(5);
                    cell2.setCellType(CellType.STRING);
                    String cellParticipacion = formatter.formatCellValue(cell2).replace(" ", "").replace("%","");

                    XSSFCell cell0= row.getCell(6);
                    cell0.setCellType(CellType.STRING);
                    String cellVrAccion = formatter.formatCellValue(cell0).replace(" ", "");

                    XSSFCell cell1= row.getCell(7);
                    cell1.setCellType(CellType.STRING);
                    String cellNoAcciones = formatter.formatCellValue(cell1).replace(" ", "");

                    if (cellValoracion.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Valoración no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellEmpresa.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Empresa no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellNit.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo NIT no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellDv.length() != 1) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo DV debe tener 1 caracter.";
                        lista.add(log);
                    }
                    if (cellIsin.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo ISIN no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellParticipacion.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Participación no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Double.parseDouble(cellParticipacion);
                        }
                        catch (Exception e){
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(5);
                            log[2] = "El campo Participación debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellVrAccion.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo VR Acción no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Double.parseDouble(cellVrAccion);
                        }
                        catch (Exception e){
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(6);
                            log[2] = "El campo VR Acción debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellNoAcciones.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo No Acciones no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Double.parseDouble(cellNoAcciones);
                        }
                        catch (Exception e){
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(7);
                            log[2] = "El campo No Acciones debe ser númerico.";
                            lista.add(log);
                        }
                    }

                    if(lista.size() == 0) {
                        PlantillaCalculoIcrv data = new PlantillaCalculoIcrv();
                        data.setValoracion(cellValoracion);
                        data.setEmpresa(cellEmpresa);
                        data.setNit(cellNit);
                        data.setDv(cellDv);
                        data.setIsin(cellIsin);
                        data.setParticipacion(Double.parseDouble(cellParticipacion)/100);
                        data.setVrAccion(Double.parseDouble(cellVrAccion));
                        data.setNoAcciones(Double.parseDouble(cellNoAcciones));
                        toInsert.add(data);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 5) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            plantillaCalculoIcrvRepository.deleteAll();
            plantillaCalculoIcrvRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public List<CalculoIcrv> findByFilter(String value, String filter, String period) {
        List<CalculoIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Valoración":
                    list = calculoIcrvRepository.findByValoracionAndPeriodo(value, period);
                    break;
                case "Empresa":
                    list = calculoIcrvRepository.findByEmpresaAndPeriodo(value, period);
                    break;
                case "NIT":
                    list = calculoIcrvRepository.findByNitAndPeriodo(value, period);
                    break;
                case "DV":
                    list = calculoIcrvRepository.findByDvAndPeriodo(value, period);
                    break;
                case "ISIN":
                    list = calculoIcrvRepository.findByIsinAndPeriodo(value, period);
                    break;
                case "%Participación":
                    list = calculoIcrvRepository.findByParticipacionAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "VR Acción":
                    list = calculoIcrvRepository.findByVrAccionAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "No Acciones":
                    list = calculoIcrvRepository.findByNoAccionesAndPeriodo(Integer.parseInt(value), period);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            return list;
        }
        return list;
    }

}


