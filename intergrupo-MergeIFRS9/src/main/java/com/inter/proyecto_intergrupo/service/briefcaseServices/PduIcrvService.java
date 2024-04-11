package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.PduIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPduIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PduIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.PlantillaPduIcrvRepository;
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
public class PduIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private PlantillaPduIcrvRepository plantillaPduIcrvRepository;

    @Autowired
    private PduIcrvRepository pduIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("PDU ICRV");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public PduIcrv findByIdPdu(Long id){
        return pduIcrvRepository.findByIdPdu(id);
    }

    public PduIcrv modifyPdu(PduIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdPdu()+") PDU ICRV");
        return pduIcrvRepository.save(toModify);
    }

    public List<PlantillaPduIcrv> findAllPlantilla()
    {
        return plantillaPduIcrvRepository.findAll();
    }
    public List<PduIcrv> findAllPdu(String periodo)
    {
        return pduIcrvRepository.findByPeriodo(periodo);
    }

    public void completeTable(String periodo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);


        Query limpieza = entityManager.createNativeQuery("delete from nexco_pdu_icrv where periodo = :periodo ;\n" +
                "insert into nexco_pdu_icrv (noisin, grupo, entidad, periodo) \n" +
                "select noisin, grupo, entidad, :periodo from nexco_pdu_plantilla_icrv;");
        limpieza.setParameter("periodo",periodo);
        limpieza.executeUpdate();

        completeDataTable(periodo);

    }

    public void completeDataTable(String periodo)
    {
        Query validate = entityManager.createNativeQuery("update a set a.porcentaje = isnull(b.participacion,0) from (select * from nexco_pdu_icrv where periodo = :periodo ) as a, (select * from nexco_calculo_icrv where periodo = :periodo ) as b where a.entidad = b.empresa ;\n" +
                "update nexco_pdu_icrv set utilidad_distribuir=isnull(utilidad_del_ejercicio,0)-isnull(reserva_no_distribuida,0) where periodo = :periodo ;\n" +
                "update nexco_pdu_icrv set dividendos_recibidos=isnull(utilidad_distribuir,0)*isnull(porcentaje,0) where periodo = :periodo ;\n" +
                "update nexco_pdu_icrv set efectivo=isnull(dividendos_recibidos,0)*(isnull(porcentaje_efectivo,0)/100),accion=isnull(dividendos_recibidos,0)*(isnull(porcentaje_accion,0)/100) where periodo = :periodo ;\n" +
                "update nexco_pdu_icrv set total=isnull(efectivo,0)+isnull(accion,0) where periodo = :periodo ;\n" +
                "update nexco_pdu_icrv set validacion=isnull(dividendos_recibidos,0)-isnull(total,0), retencion_en_fuente=case when aplica_retfuente = 'SI' then isnull(total,0)*0.1 else 0 end where periodo = :periodo ;\n" +
                "update nexco_pdu_icrv set valor_recibir=isnull(total,0)-isnull(retencion_en_fuente,0) where periodo = :periodo ;\n" +
                "update nexco_pdu_icrv set correo=isnull(valor_recibir,0)-isnull(valor_dividendos_pago1,0)-isnull(valor_dividendos_pago2,0)-isnull(valor_dividendos_pago3,0) where periodo = :periodo ;\n" +
                "update b set b.dividendos_pagados_acciones = isnull(a.accion,0) from (select * from nexco_pdu_icrv where periodo = :periodo ) as a, (select * from nexco_calculo_icrv where periodo = :periodo ) as b where a.entidad = b.empresa ;");
        validate.setParameter("periodo",periodo);
        validate.executeUpdate();
    }

    public void clearPdu(User user, String periodo){
        completeTable(periodo);
        loadAudit(user,"Generación Exitosa de PDU para "+periodo);
    }

    @Scheduled(cron = "0 3 2 1 * ?")
    public void jobPdu(){
        validatePdu();
    }
    @Scheduled(cron = "0 3 7 1 * ?")
    public void jobPdu2(){
        validatePdu();
    }

    public void validatePdu(){
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

        if(findAllPdu(todayString).size()==0)
        {
            completeTable(todayString);
            loadAudit(user,"Ejecución Exitosa JOB PDU ICRV");
        }
        else{
            loadAudit(user,"Ejecución Anulada JOB PDU ICRV");
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
                loadAudit(user,"Cargue Exitoso Plantilla PDU ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla PDU ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<PlantillaPduIcrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellNoisin = formatter.formatCellValue(row.getCell(0)).trim();
                    String cellGrupo = formatter.formatCellValue(row.getCell(1)).trim();
                    String cellEntidad = formatter.formatCellValue(row.getCell(2)).trim().toUpperCase();

                    if (cellNoisin.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Noisin no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellGrupo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Grupo no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellEntidad.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Entidad no puede estar vacio.";
                        lista.add(log);
                    }

                    if(lista.size() == 0) {
                        PlantillaPduIcrv data = new PlantillaPduIcrv();
                        data.setNoisin(cellNoisin);
                        data.setGrupo(cellGrupo);
                        data.setEntidad(cellEntidad);
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
            plantillaPduIcrvRepository.deleteAll();
            plantillaPduIcrvRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public List<PduIcrv> findByFilter(String value, String filter, String period) {
        List<PduIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "NOISIN":
                    list = pduIcrvRepository.findByNoisinAndPeriodo(value, period);
                    break;
                case "GRUPO":
                    list = pduIcrvRepository.findByGrupoAndPeriodo(value, period);
                    break;
                case "ENTIDAD":
                    list = pduIcrvRepository.findByEntidadAndPeriodo(value, period);
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


