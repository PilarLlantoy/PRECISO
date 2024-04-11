package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PlantillaPrecioIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.PrecioIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.BalvaloresIcrvRepository;
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
public class BalvaloresIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private BalvaloresIcrvRepository balvaloresIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Bal Valores ICRV");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public BalvaloresIcrv findByIdBal(Long id){
        return balvaloresIcrvRepository.findByIdBal(id);
    }

    public BalvaloresIcrv modifyBal(BalvaloresIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdBal()+") Bal Valores ICRV");
        return balvaloresIcrvRepository.save(toModify);
    }

    public List<BalvaloresIcrv> findAllBal(String periodo)
    {
        return balvaloresIcrvRepository.findByPeriodo(periodo);
    }


    public void clearBalvaloresIcrv(User user, String periodo){
        balvaloresIcrvRepository.deleteByPeriodo(periodo);
        loadAudit(user,"Limpieza Exitosa de Bal Valores para "+periodo);
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
                loadAudit(user,"Cargue Exitoso Bal Valores ICRV");
            else
                loadAudit(user,"Cargue Fallido Bal Valores ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<BalvaloresIcrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellMes = formatter.formatCellValue(row.getCell(0)).trim();
                    String cellCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                    String cellDescripcion = formatter.formatCellValue(row.getCell(2)).trim();

                    XSSFCell cell0= row.getCell(3);
                    cell0.setCellType(CellType.STRING);
                    String cellMonedaTotal = formatter.formatCellValue(cell0).replace(" ", "").replace(",","");
                    if (cellMes.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Mes no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCuenta.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Cuenta NIIF no puede estar vacio.";
                        lista.add(log);
                    }
                    else if (!cellCuenta.substring(0,1).equals("3")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Cuenta NIIF solo permite cuentas que inicien por 3.";
                        lista.add(log);
                    }
                    if (cellDescripcion.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Descripción no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellMonedaTotal.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Moneda Total no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Double.parseDouble((cellMonedaTotal));
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(3);
                            log[2] = "El campo Moneda Total debe ser numérico.";
                            lista.add(log);
                        }
                    }

                    if(lista.size() == 0) {
                        BalvaloresIcrv data = new BalvaloresIcrv();
                        data.setMes(cellMes);
                        data.setCuentaNiif(cellCuenta);
                        data.setDescripcion(cellDescripcion);
                        data.setMonedaTotal(Double.parseDouble(cellMonedaTotal));
                        data.setPeriodo(periodo);
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
            balvaloresIcrvRepository.deleteByPeriodo(periodo);
            balvaloresIcrvRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public List<BalvaloresIcrv> findByFilter(String value, String filter, String period) {
        List<BalvaloresIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Mes":
                    list = balvaloresIcrvRepository.findByMesAndPeriodo(value, period);
                    break;
                case "Cuenta NIIF":
                    list = balvaloresIcrvRepository.findByCuentaNiifAndPeriodo(value, period);
                    break;
                case "Descripción":
                    list = balvaloresIcrvRepository.findByDescripcionAndPeriodo(value, period);
                    break;
                case "Moneda Total":
                    list = balvaloresIcrvRepository.findByMonedaTotalAndPeriodo(Double.parseDouble(value.replace(".","")), period);
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


