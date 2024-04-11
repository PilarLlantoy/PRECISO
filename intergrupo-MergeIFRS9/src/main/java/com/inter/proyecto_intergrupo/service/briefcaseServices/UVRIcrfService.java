package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.UVRIcrf;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.UVRIcrfRepository;
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

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Service
@Transactional
public class UVRIcrfService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private UVRIcrfRepository uvrIcrfRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("UVR ICRF");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public UVRIcrf findByFecha(Date fecha){
        return uvrIcrfRepository.findByFecha(fecha);
    }

    public UVRIcrf modifyUVRIcrf(UVRIcrf toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getFecha()+")");
        return uvrIcrfRepository.save(toModify);
    }

    public List<UVRIcrf> findAllUVRIcrf(String periodo)
    {
        return uvrIcrfRepository.findByPeriodo(periodo);
    }


    public void clearUVRIcrf(User user, String periodo){
        uvrIcrfRepository.deleteByPeriodo(periodo);
        loadAudit(user,"Limpieza Exitosa de UVR ICRF para "+periodo);
    }

    public ArrayList<String[]> saveFileBDPlantilla(InputStream file, User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows,periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso UVR ICRF");
            else
                loadAudit(user,"Cargue Fallido UVR ICRF");
        }
        return list;
    }

    public String generateDatecalendar(String periodo)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo, formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        return lastDayOfMonth.format(formatter);
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<UVRIcrf> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 7) {
                {
                    Date convertDate =new Date();
                    DataFormatter formatter = new DataFormatter();
                    String cellFecha = formatter.formatCellValue(row.getCell(0)).trim();
                    String cellPesosColombianos = formatter.formatCellValue(row.getCell(1)).trim();
                    String cellVariacionAnual = formatter.formatCellValue(row.getCell(2)).trim();

                    if (cellFecha.length() != 0 && cellPesosColombianos.length() != 0 && cellVariacionAnual.length() != 0) {
                        if (cellFecha.equals(generateDatecalendar(cellFecha))) {

                            XSSFCell cell0 = row.getCell(1);
                            cell0.setCellType(CellType.STRING);
                            cellPesosColombianos = formatter.formatCellValue(cell0).replace(" ", "").replace(",", "");

                            XSSFCell cell1 = row.getCell(2);
                            cell1.setCellType(CellType.STRING);
                            cellVariacionAnual = formatter.formatCellValue(cell1).replace(" ", "").replace(",", "");


                            try {
                                SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                                convertDate = formato.parse(cellFecha);
                            } catch (Exception e) {
                                String[] log = new String[3];
                                log[0] = String.valueOf(row.getRowNum() + 1);
                                log[1] = CellReference.convertNumToColString(0);
                                log[2] = "El campo Fecha (dd/mm/aaaa) debe ser Fecha en formato (dd/mm/yyyy).";
                                lista.add(log);
                            }

                            if (cellPesosColombianos.length() == 0) {
                                String[] log = new String[3];
                                log[0] = String.valueOf(row.getRowNum() + 1);
                                log[1] = CellReference.convertNumToColString(1);
                                log[2] = "El campo Pesos colombianos por UVR no puede estar vacio.";
                                lista.add(log);
                            } else {
                                try {
                                    Double.parseDouble((cellPesosColombianos));
                                } catch (Exception e) {
                                    String[] log = new String[3];
                                    log[0] = String.valueOf(row.getRowNum() + 1);
                                    log[1] = CellReference.convertNumToColString(1);
                                    log[2] = "El campo Pesos colombianos por UVR debe ser numérico.";
                                    lista.add(log);
                                }
                            }
                            if (cellVariacionAnual.length() == 0) {
                                String[] log = new String[3];
                                log[0] = String.valueOf(row.getRowNum() + 1);
                                log[1] = CellReference.convertNumToColString(2);
                                log[2] = "El campo Variación Anual Porcentual % no puede estar vacio.";
                                lista.add(log);
                            } else {
                                try {
                                    Double.parseDouble((cellVariacionAnual));
                                } catch (Exception e) {
                                    String[] log = new String[3];
                                    log[0] = String.valueOf(row.getRowNum() + 1);
                                    log[1] = CellReference.convertNumToColString(2);
                                    log[2] = "El campo Variación Anual Porcentual % debe ser numérico.";
                                    lista.add(log);
                                }
                            }

                            if (lista.size() == 0) {

                                String[] parts=cellFecha.split("/");

                                UVRIcrf data = new UVRIcrf();
                                data.setFecha(convertDate);
                                data.setPesoCopUvr(Double.parseDouble(cellPesosColombianos));
                                data.setVariacionAnual(Double.parseDouble(cellVariacionAnual));
                                data.setPeriodo(parts[2]);
                                toInsert.add(data);
                            }
                        }
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
            uvrIcrfRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public List<UVRIcrf> findByFilter(String value, String filter, String period) {
        List<UVRIcrf> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Fecha (dd/MM/yyyy)":
                    SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy");
                    list = uvrIcrfRepository.findByFechaAndPeriodo(formato.parse(value), period);
                    break;
                case "Pesos colombianos por UVR":
                    list = uvrIcrfRepository.findByPesoCopUvrAndPeriodo(Double.parseDouble(value), period);
                    break;
                case "Variación Anual Porcentual":
                    list = uvrIcrfRepository.findByVariacionAnualAndPeriodo(Double.parseDouble(value), period);
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


