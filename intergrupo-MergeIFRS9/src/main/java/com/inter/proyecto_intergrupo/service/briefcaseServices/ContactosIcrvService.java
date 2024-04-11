package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.ContactosIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.ContactosIcrvRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class ContactosIcrvService {

    @Autowired
    private ContactosIcrvRepository contactosIcrvRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ContactosIcrvService(ContactosIcrvRepository contactosIcrvRepository) {
        this.contactosIcrvRepository = contactosIcrvRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Contactos ICRV");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ContactosIcrv findByIdContacto(Long id){
        return contactosIcrvRepository.findByIdContacto(id);
    }

    public List<ContactosIcrv> findAll()
    {
        return contactosIcrvRepository.findAll();
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
                loadAudit(user,"Cargue Exitoso Plantilla Contactos ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla Contactos ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ContactosIcrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    int cont = 0;
                    String cellProceso = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellNombre = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellEmpresa = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellCorreoP = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellCorreoS = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellSuperior = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellSuperior1 = formatter.formatCellValue(row.getCell(cont++)).trim();

                    XSSFCell cell0= row.getCell(cont++);
                    if(cell0 != null)
                        cell0.setCellType(CellType.STRING);
                    String cellCelular = formatter.formatCellValue(cell0).replace(" ", "").replace(",","");

                    XSSFCell cell1= row.getCell(cont++);
                    if(cell1 != null)
                        cell1.setCellType(CellType.STRING);
                    String cellTelefono = formatter.formatCellValue(cell1).replace(" ", "").replace(",","");

                    String cellExtension = formatter.formatCellValue(row.getCell(cont++)).trim();
                    String cellPagina = formatter.formatCellValue(row.getCell(cont++)).trim();

                    if (cellProceso.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Proceso no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellNombre.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellEmpresa.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Empresa no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCorreoP.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Correo Principal no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCelular.length() != 0) {
                        try{
                            Long.parseLong(cellCelular);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(7);
                            log[2] = "El campo Celular debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellTelefono.length() != 0) {
                        try{
                            Long.parseLong(cellTelefono);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(8);
                            log[2] = "El campo Teléfono debe ser númerico.";
                            lista.add(log);
                        }
                    }

                    if(lista.size() == 0) {
                        ContactosIcrv data = new ContactosIcrv();
                        data.setProceso(cellProceso);
                        data.setNombre(cellNombre);
                        data.setEmpresa(cellEmpresa);
                        data.setCorreoPrincipal(cellCorreoP);
                        data.setCorreoSecundario(cellCorreoS);
                        data.setSuperior(cellSuperior);
                        data.setSuperior1(cellSuperior1);
                        if(cellCelular.length()!=0)
                            data.setCelular(Long.parseLong(cellCelular));
                        if(cellTelefono.length()!=0)
                            data.setTelefono(Long.parseLong(cellTelefono));
                        data.setExtension(cellExtension);
                        data.setPagina(cellPagina);
                        toInsert.add(data);
                    }
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 11) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")) {
            contactosIcrvRepository.deleteAll();
            contactosIcrvRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ContactosIcrv modifyContactos(ContactosIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro Contactos");
        return contactosIcrvRepository.save(toModify);
    }

    public ContactosIcrv saveContactos(ContactosIcrv toSave, User user){
        loadAudit(user,"Adición Exitosa Registro Contactos");
        return contactosIcrvRepository.save(toSave);
    }

    public void removeContactos(Long id, User user){
        loadAudit(user,"Eliminación Exitosa Registro Contactos");
        contactosIcrvRepository.deleteByIdContacto(id);
    }

    public void clearContactos(User user){
        loadAudit(user,"Limpieza de Tabla Exitosa Contactos");
        contactosIcrvRepository.deleteAll();
    }

    public Page<ContactosIcrv> getAll(Pageable pageable){
        return contactosIcrvRepository.findAll(pageable);
    }

    public List<ContactosIcrv> findByFilter(String value, String filter) {
        List<ContactosIcrv> list=new ArrayList<ContactosIcrv>();
        try{
            switch (filter)
            {
                case "Proceso":
                    list=contactosIcrvRepository.findByProcesoContainingIgnoreCase(value);
                    break;
                case "Nombre":
                    list=contactosIcrvRepository.findByNombreContainingIgnoreCase(value);
                    break;
                case "Empresa":
                    list=contactosIcrvRepository.findByEmpresaContainingIgnoreCase(value);
                    break;
                case "Correo Principal":
                    list=contactosIcrvRepository.findByCorreoPrincipalContainingIgnoreCase(value);
                    break;
                case "Correo Secundario":
                    list=contactosIcrvRepository.findByCorreoSecundarioContainingIgnoreCase(value);
                    break;
                case "Superior":
                    list=contactosIcrvRepository.findBySuperiorContainingIgnoreCase(value);
                    break;
                case "Superior 1":
                    list=contactosIcrvRepository.findBySuperior1ContainingIgnoreCase(value);
                    break;
                case "Extensión":
                    list=contactosIcrvRepository.findByExtensionContainingIgnoreCase(value);
                    break;
                case "Página":
                    list=contactosIcrvRepository.findByPaginaContainingIgnoreCase(value);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e){
            return list;
        }
        return list;
    }
}
