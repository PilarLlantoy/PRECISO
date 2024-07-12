package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SignatureRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdsCcRepository;
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
import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class SignatureService {

    @Autowired
    private SignatureRepository signatureRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public SignatureService(SignatureRepository signatureRepository) {
        this.signatureRepository = signatureRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Cuentas por Cobrar");
        insert.setFecha(today);
        insert.setInput("Firmas");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Signature findByIdFirma(Long id){
        return signatureRepository.findByIdFirma(id);
    }

    public List<Signature> findAllFirma(){
        Query consulta = entityManager.createNativeQuery("select * from preciso_firmas where firma is not null",Signature.class);
        return consulta.getResultList();
    }

    public List<Signature> findAll()
    {
        return signatureRepository.findAll();
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
                loadAudit(user,"Cargue exitoso plantilla Firmas");
            else
                loadAudit(user,"Cargue Fallido plantilla Firmas");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Signature> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();

                    String cellNombre = formatter.formatCellValue(row.getCell(0));
                    String cellCargo = formatter.formatCellValue(row.getCell(1));
                    String cellTelefono = formatter.formatCellValue(row.getCell(2));
                    String cellCorreo = formatter.formatCellValue(row.getCell(3));
                    String cellDireccion = formatter.formatCellValue(row.getCell(4));

                    if (cellNombre.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Nombre no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCargo.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Cargo no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellTelefono.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Teléfono no puede estar vacio.";
                        lista.add(log);
                    }
                    else if (!validatorPatter(cellTelefono.trim(),"Phone")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El Telefono ingresado no es valido.";
                        lista.add(log);
                    }
                    if (cellCorreo.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Correo no puede estar vacio.";
                        lista.add(log);
                    } else if (!validatorPatter(cellCorreo.trim(),"Mail")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El Correo ingresado no es valido.";
                        lista.add(log);
                    }
                    if (cellDireccion.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Dirección no puede estar vacio.";
                        lista.add(log);
                    }

                    Signature signature = new Signature();
                    signature.setCargo(cellCargo);
                    signature.setNombre(cellNombre);
                    signature.setCorreo(cellCorreo);
                    signature.setTelefono(cellTelefono);
                    signature.setDireccion(cellDireccion);
                    toInsert.add(signature);
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
            signatureRepository.deleteAll();
            signatureRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public Signature modifySignature(Signature toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro Firma");
        return signatureRepository.save(toModify);
    }

    public Signature saveSignature(Signature toSave, User user){
        loadAudit(user,"Adición Exitosa registro Firma");
        return signatureRepository.save(toSave);
    }

    public void removeSignature(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro Firma");
        signatureRepository.deleteByIdFirma(id);
    }

    public void clearSignature(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Firmas");
        signatureRepository.deleteAll();
    }

    public Page<Signature> getAll(Pageable pageable){
        return signatureRepository.findAll(pageable);
    }

    public List<Signature> findByFilter(String value, String filter) {
        List<Signature> list=new ArrayList<Signature>();
        switch (filter)
        {
            case "Cargo":
                list=signatureRepository.findByCargoContainingIgnoreCase(value);
                break;
            case "Nombre":
                list=signatureRepository.findByNombreContainingIgnoreCase(value);
                break;
            case "Teléfono":
                list=signatureRepository.findByTelefonoContainingIgnoreCase(value);
                break;
            case "Correo":
                list=signatureRepository.findByCorreoContainingIgnoreCase(value);
                break;
            case "Dirección":
                list=signatureRepository.findByDireccionContainingIgnoreCase(value);
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

    public void saveSignatureImage(InputStream  inputStream, User user,String id)
    {
        try
        {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] byteArray = outputStream.toByteArray();

            Signature signature = signatureRepository.findByIdFirma(Long.parseLong(id));
            signature.setFirma(byteArray);
            entityManager.persist(signature);
            loadAudit(user,"Cargue imagen firma Exitosa");
            outputStream.close();
        }
        catch (Exception e)
        {
            loadAudit(user,"Cargue imagen firma Fallida");
            e.printStackTrace();
        }
    }

}
