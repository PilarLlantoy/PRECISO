package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdsCcRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ThirdsCcService {

    @Autowired
    private ThirdsCcRepository thirdsCcRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ThirdsCcService(ThirdsCcRepository thirdsCcRepository) {
        this.thirdsCcRepository = thirdsCcRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Cuentas por Cobrar");
        insert.setFecha(today);
        insert.setInput("Terceros");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ThirdsCc findByNit(String nit){
        return thirdsCcRepository.findByNit(nit);
    }

    public List<ThirdsCc> findAll()
    {
        return thirdsCcRepository.findAll();
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue exitoso plantilla Terceros CC");
            else
                loadAudit(user,"Cargue Fallido plantilla Terceros CC");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ThirdsCc> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellNit = formatter.formatCellValue(row.getCell(0)).trim();
                    String cellNombre = formatter.formatCellValue(row.getCell(1)).trim();
                    String cellImpuesto = formatter.formatCellValue(row.getCell(2)).trim();
                    String cellTelefono = formatter.formatCellValue(row.getCell(3)).trim();
                    String cellCorreo = formatter.formatCellValue(row.getCell(4)).trim();
                    String cellCorreoAlterno = formatter.formatCellValue(row.getCell(5)).trim();
                    String cellCorreoAlterno2 = formatter.formatCellValue(row.getCell(6)).trim();
                    String cellDireccion = formatter.formatCellValue(row.getCell(7)).trim();
                    String cellCorreoCopia1 = formatter.formatCellValue(row.getCell(8)).trim();
                    String cellCorreoCopia2 = formatter.formatCellValue(row.getCell(9)).trim();

                    if (cellNit.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo NIT no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellNombre.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellImpuesto.trim().toUpperCase().equals("SIN IMPUESTOS") && !cellImpuesto.trim().toUpperCase().equals("RETEFUENTE") && !cellImpuesto.trim().toUpperCase().equals("IVA")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Impuesto debe ser SIN IMPUESTOS, RETEFUENTE o IVA.";
                        lista.add(log);
                    }
                    if (cellTelefono.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Teléfono no puede estar vacio.";
                        lista.add(log);
                    }
                    else if (!validatorPatter(cellTelefono.trim(),"Phone")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El Telefono ingresado no es valido.";
                        lista.add(log);
                    }
                    if (cellCorreo.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Correo no puede estar vacio.";
                        lista.add(log);
                    } else if (!validatorPatter(cellCorreo.trim(),"Mail")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El Correo ingresado no es valido.";
                        lista.add(log);
                    }

                    if (cellCorreoAlterno.trim().length() != 0) {
                        if (!validatorPatter(cellCorreoAlterno.trim(),"Mail")) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(5);
                            log[2] = "El Correo Alterno ingresado no es valido.";
                            lista.add(log);
                        }
                    }
                    if (cellCorreoAlterno2.trim().length() != 0) {
                        if (!validatorPatter(cellCorreoAlterno2.trim(),"Mail")) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(6);
                            log[2] = "El Correo Alterno 2 ingresado no es valido.";
                            lista.add(log);
                        }
                    }
                    if (cellDireccion.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Dirección no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCorreoCopia1.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
                        log[2] = "El campo Correo Copia 1 no puede estar vacio.";
                        lista.add(log);
                    } else if (!validatorPatter(cellCorreoCopia1.trim(),"Mail")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
                        log[2] = "El Correo Copia 1 ingresado no es valido.";
                        lista.add(log);
                    }
                    if (cellCorreoCopia2.trim().length() != 0) {
                        if (!validatorPatter(cellCorreoCopia2.trim(),"Mail")) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(9);
                            log[2] = "El Correo Copia 2 ingresado no es valido.";
                            lista.add(log);
                        }
                    }

                    ThirdsCc thirdsCc = new ThirdsCc();
                    thirdsCc.setNit(cellNit);
                    thirdsCc.setNombre(cellNombre);
                    thirdsCc.setImpuesto(cellImpuesto.toUpperCase());
                    thirdsCc.setCorreo(cellCorreo);
                    thirdsCc.setCorreoAlterno(cellCorreoAlterno);
                    thirdsCc.setCorreoAlterno2(cellCorreoAlterno2);
                    thirdsCc.setCorreoCopia1(cellCorreoCopia1);
                    thirdsCc.setCorreoCopia2(cellCorreoCopia2);
                    thirdsCc.setTelefono(cellTelefono);
                    thirdsCc.setDireccion(cellDireccion);
                    toInsert.add(thirdsCc);
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
            thirdsCcRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ThirdsCc modifyThird(ThirdsCc toModify,String nit, User user)
    {
        if(!toModify.getNit().equals(nit))
            thirdsCcRepository.deleteByNit(nit);
        loadAudit(user,"Modificación Exitosa registro Tercero CC");
        return thirdsCcRepository.save(toModify);
    }

    public ThirdsCc saveThird(ThirdsCc third, User user){
        loadAudit(user,"Adición Exitosa registro Tercero CC");
        return thirdsCcRepository.save(third);
    }

    public void removeThird(String id, User user){
        loadAudit(user,"Eliminación Exitosa registro Tercero CC");
        thirdsCcRepository.deleteById(id);
    }

    public void clearThird(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Terceros CC");
        thirdsCcRepository.deleteAll();
    }

    public Page<ThirdsCc> getAll(Pageable pageable){
        return thirdsCcRepository.findAll(pageable);
    }

    public List<ThirdsCc> findByFilter(String value, String filter) {
        List<ThirdsCc> list=new ArrayList<ThirdsCc>();
        switch (filter)
        {
            case "NIT":
                list=thirdsCcRepository.findByNitContainingIgnoreCase(value);
                break;
            case "Nombre":
                list=thirdsCcRepository.findByNombreContainingIgnoreCase(value);
                break;
            case "Impuesto":
                list=thirdsCcRepository.findByImpuestoContainingIgnoreCase(value);
                break;
            case "Teléfono":
                list=thirdsCcRepository.findByTelefonoContainingIgnoreCase(value);
                break;
            case "Correo":
                list=thirdsCcRepository.findByCorreoContainingIgnoreCase(value);
                break;
            case "Correo Alterno":
                list=thirdsCcRepository.findByCorreoAlternoContainingIgnoreCase(value);
                break;
            case "Correo Alterno 2":
                list=thirdsCcRepository.findByCorreoAlterno2ContainingIgnoreCase(value);
                break;
            case "Dirección":
                list=thirdsCcRepository.findByDireccionContainingIgnoreCase(value);
                break;
            case "Correo Copia 1":
                list=thirdsCcRepository.findByCorreoCopia1ContainingIgnoreCase(value);
                break;
            case "Correo Copia 2":
                list=thirdsCcRepository.findByCorreoCopia2ContainingIgnoreCase(value);
                break;
            default:
                break;
        }
        return list;
    }

    public boolean validatorPatter(String dato,String tipo){
        String patron ="";
        if(tipo.equals("Mail"))
        {
            patron="^[A-Za-z0-9+_.-]+@\\w+\\.\\w+$";
        }
        else if(tipo.equals("Phone")){
            patron="^(\\+\\d{2})?\\d{10}$";
        }
        Pattern pattern= Pattern.compile(patron);
        Matcher matcher = pattern.matcher(dato);
        return matcher.matches();
    }
}
