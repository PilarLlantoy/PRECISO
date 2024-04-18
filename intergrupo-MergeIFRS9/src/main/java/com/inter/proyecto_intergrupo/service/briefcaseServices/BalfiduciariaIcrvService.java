package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BalfiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.BalfiduciariaIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.BalvaloresIcrvRepository;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Service
@Transactional
public class BalfiduciariaIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private BalfiduciariaIcrvRepository balfiduciariaIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Bal Fiduciaria ICRV");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public BalfiduciariaIcrv findByIdBal(Long id){
        return balfiduciariaIcrvRepository.findByIdBal(id);
    }

    public BalfiduciariaIcrv modifyBal(BalfiduciariaIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdBal()+") Bal Fiduciaria ICRV");
        return balfiduciariaIcrvRepository.save(toModify);
    }

    public List<BalfiduciariaIcrv> findAllBal(String periodo)
    {
        return balfiduciariaIcrvRepository.findByPeriodo(periodo);
    }


    public void clearBalfiduciariaIcrv(User user, String periodo){
        balfiduciariaIcrvRepository.deleteByPeriodo(periodo);
        loadAudit(user,"Limpieza Exitosa de Bal Fiduciaria para "+periodo);
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
                loadAudit(user,"Cargue Exitoso Bal Fiduciaria ICRV");
            else
                loadAudit(user,"Cargue Fallido Bal Fiduciaria ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<BalfiduciariaIcrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int consecutive = 0;
                    DataFormatter formatter = new DataFormatter();
                    String cellCorte = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodigoContabilidad = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellNombreFideicomiso = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellAno = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellMes = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodigoPuc = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCuenta = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodigoPucLocal = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellNombre = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    XSSFCell cell0= row.getCell(consecutive++);
                    cell0.setCellType(CellType.STRING);
                    String cellSaldoFinal = formatter.formatCellValue(cell0).replace(" ", "").replace(",","");

                    String cellNivel = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodicons = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodigest = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellL4 = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    if (cellCorte.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Corte no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodigoContabilidad.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Código Contabilidad no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellNombreFideicomiso.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Nombre Fideicomiso no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellAno.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Año no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellMes.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Mes no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodigoPuc.length() == 0) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(5);
                    log[2] = "El campo Código PUC no puede estar vacio.";
                    lista.add(log);
                    }
                    if (cellCuenta.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Código Cuenta NIIF no puede estar vacio.";
                        lista.add(log);
                    }
                    else if (!cellCuenta.substring(0,1).equals("3")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Cuenta NIIF solo permite cuentas que inicien por 3.";
                        lista.add(log);
                    }
                    if (cellNombre.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellSaldoFinal.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(9);
                        log[2] = "El campo Saldo Final no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try{
                            Double.parseDouble((cellSaldoFinal));
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(9);
                            log[2] = "El campo Saldo Final debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    if (cellNivel.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(10);
                        log[2] = "El campo Nivel no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodicons.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(11);
                        log[2] = "El campo Código Consolidación no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodigest.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(12);
                        log[2] = "El campo Código Gestión no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellL4.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(13);
                        log[2] = "El campo L4 no puede estar vacio.";
                        lista.add(log);
                    }

                    if(lista.size() == 0) {
                        BalfiduciariaIcrv data = new BalfiduciariaIcrv();
                        data.setCorte(cellCorte);
                        data.setCodigoContabilidad(cellCodigoContabilidad);
                        data.setNombreFideicomiso(cellNombreFideicomiso);
                        data.setAno(cellAno);
                        data.setMes(cellMes);
                        data.setCodigoPuc(cellCodigoPuc);
                        data.setCodigoCuentaNiif(cellCuenta);
                        data.setCodigoPucLocal(cellCodigoPucLocal);
                        data.setNombre(cellNombre);
                        data.setSaldoFinal(Double.parseDouble(cellSaldoFinal));
                        data.setNivel(cellNivel);
                        data.setCodicons(cellCodicons);
                        data.setCodigest(cellCodigest);
                        data.setL4(cellL4);
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
            balfiduciariaIcrvRepository.deleteByPeriodo(periodo);
            balfiduciariaIcrvRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public List<BalfiduciariaIcrv> findByFilter(String value, String filter, String period) {
        List<BalfiduciariaIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Corte":
                    list = balfiduciariaIcrvRepository.findByCorteAndPeriodo(value, period);
                    break;
                case "Código Contabilidad":
                    list = balfiduciariaIcrvRepository.findByCodigoContabilidadAndPeriodo(value, period);
                    break;
                case "Nombre Fideicomiso":
                    list = balfiduciariaIcrvRepository.findByNombreFideicomisoAndPeriodo(value, period);
                    break;
                case "Año":
                    list = balfiduciariaIcrvRepository.findByAnoAndPeriodo(value, period);
                    break;
                case "Mes":
                    list = balfiduciariaIcrvRepository.findByMesAndPeriodo(value, period);
                    break;
                case "Código PUC":
                    list = balfiduciariaIcrvRepository.findByCodigoPucAndPeriodo(value, period);
                    break;
                case "Código Cuenta NIIF":
                    list = balfiduciariaIcrvRepository.findByCodigoCuentaNiifAndPeriodo(value, period);
                    break;
                case "Código PUC Local":
                    list = balfiduciariaIcrvRepository.findByCodigoPucLocalAndPeriodo(value, period);
                    break;
                case "Nombre":
                    list = balfiduciariaIcrvRepository.findByNombreAndPeriodo(value, period);
                    break;
                case "Saldo Final":
                    list = balfiduciariaIcrvRepository.findBySaldoFinalAndPeriodo(Double.parseDouble(value.replace(".","")), period);
                    break;
                case "Nivel":
                    list = balfiduciariaIcrvRepository.findByNivelAndPeriodo(value, period);
                    break;
                case "Código Consolidación":
                    list = balfiduciariaIcrvRepository.findByCodiconsAndPeriodo(value, period);
                    break;
                case "Código Gestión":
                    list = balfiduciariaIcrvRepository.findByCodigestAndPeriodo(value, period);
                    break;
                case "L4":
                    list = balfiduciariaIcrvRepository.findByL4AndPeriodo(value, period);
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


