package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamMDARepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamNIC34Repository;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ParamNIC34Service {

    @Autowired
    private ParamNIC34Repository paramNIC34Repository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SendEmailService sendEmailService;

    public ParamNIC34Service(ParamNIC34Repository paramNIC34Repository) {
        this.paramNIC34Repository = paramNIC34Repository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("NIC34");
        insert.setFecha(today);
        insert.setInput("Parametrica NIC34");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParamNIC34 findByIdNic34(Long id){
        return paramNIC34Repository.findByIdNic34(id);
    }

    public List<ParamNIC34> findAll()
    {
        return paramNIC34Repository.findAll();
    }

    public ParamNIC34 findObject(String cuenta, String idGrupo, String aplica, String idNota, String idSubNota, String idCampo, String moneda){
        Query validateFecont = entityManager.createNativeQuery("select * from nexco_param_nic34 where cuenta = ? and id_grupo = ? and aplica = ? and id_nota = ? and id_subnota = ? and id_campo = ? and moneda = ?",ParamNIC34.class);
        validateFecont.setParameter(1,cuenta);
        validateFecont.setParameter(2,idGrupo);
        validateFecont.setParameter(3,aplica);
        validateFecont.setParameter(4,idNota);
        validateFecont.setParameter(5,idSubNota);
        validateFecont.setParameter(6,idCampo);
        validateFecont.setParameter(7,moneda);
        List<ParamNIC34> list = validateFecont.getResultList();
        if(!list.isEmpty())
            return list.get(0);
        else
            return null;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows,user);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Parametrica NIC34");
            else
                loadAudit(user,"Cargue Fallido Parametrica NIC34");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows,User user) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<String[]> listaMail = new ArrayList();
        ArrayList<ParamNIC34> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int columnCount = 0;
                    DataFormatter formatter = new DataFormatter();
                    String cellL6 = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellCuenta = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellIdGrupo = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellGrupo = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellAplica = formatter.formatCellValue(row.getCell(columnCount++)).trim().toUpperCase();
                    String cellIdNota = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellNota = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellIdSubNota = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellSubNota = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellIdCampo = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellCampo = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellMoneda = formatter.formatCellValue(row.getCell(columnCount++)).trim().toUpperCase();
                    String cellSigno = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellResponsable = formatter.formatCellValue(row.getCell(columnCount++)).trim();

                    if (cellL6.length() != 6) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo L6 debe contener 6 números.";
                        lista.add(log);
                    }
                    if (cellCuenta.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Cuenta no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellIdGrupo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Id Grupo no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellGrupo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Grupo no puede estar vacío.";
                        lista.add(log);
                    }
                    if (!cellAplica.equals("T") && !cellAplica.equals("A")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Aplica debe ser T o A.";
                        lista.add(log);
                    }
                    if (cellIdNota.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Id Nota no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellNota.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Nota no puede estar vacío.";
                        lista.add(log);
                    }
                    /*if (cellIdSubNota.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Id SubNota no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellSubNota.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
                        log[2] = "El campo SubNota no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellIdCampo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(9);
                        log[2] = "El campo Id Campo no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellCampo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(10);
                        log[2] = "El campo Campo no puede estar vacío.";
                        lista.add(log);
                    }*/
                    if (!cellMoneda.equals("ML") && !cellMoneda.equals("ME")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(11);
                        log[2] = "El campo Moneda debe ser ML o ME.";
                        lista.add(log);
                    }
                    if (!cellSigno.equals("-1") && !cellSigno.equals("1")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(12);
                        log[2] = "El campo Signo debe ser -1 o 1.";
                        lista.add(log);
                    }
                    if (cellResponsable.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(13);
                        log[2] = "El campo Responsable no puede estar vacío.";
                        lista.add(log);
                    }

                    ParamNIC34 paramNIC34 = new ParamNIC34();
                    ParamNIC34 temp = findObject(cellCuenta,cellIdGrupo,cellAplica,cellIdNota,cellIdSubNota,cellIdCampo,cellMoneda);
                    if(temp!=null)
                    {
                        paramNIC34=temp;
                    }
                    else if(listaMail.size()<=20 && !cellResponsable.equals(user.getUsuario()))
                    {
                        String[] logData = new String[2];
                        logData[1] = cellCuenta;
                        logData[0] = cellResponsable;
                        listaMail.add(logData);
                    }
                    paramNIC34.setL6(cellL6);
                    paramNIC34.setCuenta(cellCuenta);
                    paramNIC34.setIdGrupo(cellIdGrupo);
                    paramNIC34.setGrupo(cellGrupo);
                    paramNIC34.setAplica(cellAplica);
                    paramNIC34.setIdNota(cellIdNota);
                    paramNIC34.setNota(cellNota);
                    paramNIC34.setIdSubnota(cellIdSubNota);
                    paramNIC34.setSubnota(cellSubNota);
                    paramNIC34.setIdCampo(cellIdCampo);
                    paramNIC34.setCampo(cellCampo);
                    paramNIC34.setMoneda(cellMoneda);
                    paramNIC34.setResponsable(cellResponsable);
                    try {
                        paramNIC34.setSigno(Double.parseDouble(cellSigno));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    toInsert.add(paramNIC34);

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
        if (temp[2].equals("SUCCESS")){
            paramNIC34Repository.saveAll(toInsert);
            sendEmailPrev(listaMail,user);
        }
        toInsert.clear();
        return lista;
    }

    public ParamNIC34 modifyNic34(ParamNIC34 toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro parametrica NIC34");
        return paramNIC34Repository.save(toModify);
    }

    public ParamNIC34 saveNic34(ParamNIC34 toSave, User user){
        loadAudit(user,"Adición Exitosa registro parametrica NIC34");
        return paramNIC34Repository.save(toSave);
    }

    public void removeNic34(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro parametrica NIC34");
        paramNIC34Repository.deleteByIdNic34(id);
    }

    public void clearNic34(User user){
        loadAudit(user,"Limpieza de tabla Exitosa parametrica NIC34");
        paramNIC34Repository.deleteAll();
    }

    public Page<ParamNIC34> getAll(Pageable pageable){
        return paramNIC34Repository.findAll(pageable);
    }

    public List<ParamNIC34> findByFilter(String value, String filter) {
        List<ParamNIC34> list=new ArrayList<ParamNIC34>();
        switch (filter)
        {
            case "L6":
                list=paramNIC34Repository.findByL6Like(value);
                break;
            case "Cuenta":
                list=paramNIC34Repository.findByCuentaLike(value);
                break;
            case "ID Grupo":
                list=paramNIC34Repository.findByIdGrupoLike(value);
                break;
            case "Grupo":
                list=paramNIC34Repository.findByGrupoLike(value);
                break;
            case "Aplica":
                list=paramNIC34Repository.findByAplicaLike(value);
                break;
            case "ID Nota":
                list=paramNIC34Repository.findByIdNotaLike(value);
                break;
            case "Nota":
                list=paramNIC34Repository.findByNotaLike(value);
                break;
            case "ID Subnota":
                list=paramNIC34Repository.findByIdSubnotaLike(value);
                break;
            case "Subnota":
                list=paramNIC34Repository.findBySubnotaLike(value);
                break;
            case "ID Campo":
                list=paramNIC34Repository.findByIdCampoLike(value);
                break;
            case "Campo":
                list=paramNIC34Repository.findByCampoLike(value);
                break;
            case "Moneda":
                list=paramNIC34Repository.findByMonedaLike(value);
                break;
            case "Responsable":
                list=paramNIC34Repository.findByResponsableLike(value);
                break;
            default:
                break;
        }
        return list;
    }

    public void sendEmail(String recipientEmail,String recipientCopyEmail,String data) {
        String subject = "Notificaciones Nexco NIC34";

        String content = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title></title>\n" +
                "    <style>\n" +
                "        \n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f0f0f0;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "            text-align: center;\n" +
                "        }\n" +
                "\n" +
                "        \n" +
                "        header {\n" +
                "            background-color: #004481;\n" +
                "            color: white;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "\n" +
                "        \n" +
                "        .content {\n" +
                "            background-color: white;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0 0 10px rgba(0, 0, 0, 0.2);\n" +
                "            margin: 20px;\n" +
                "            padding: 20px;\n" +
                "        }\n" +
                "\n" +
                "        \n" +
                "        .btn {\n" +
                "            display: inline-block;\n" +
                "            background-color: #004481;\n" +
                "            color: white;\n" +
                "            padding: 10px 20px;\n" +
                "            text-decoration: none;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "\n" +
                "        footer {\n" +
                "            background-color: #004481;\n" +
                "            color: white;\n" +
                "            padding: 10px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <h1>¡Notificación Parametrización Pendiente!</h1>\n" +
                "    </header>\n" +
                "    <div class=\"content\">\n" +
                "        <h2>Se ha realizado el cargue de cuentas en la Parametrica NIC34, y se le asignaron las siguientes cuentas como responsable:</h2>\n" +
                data+
                "        <p>Por favor ingrese al aplicativo Nexco en el apartado de NIC34 -> Parametricas -> NIC34 Cuentas Generales, y parametrice los datos faltantes de las cuentas asociada. </p>\n" +
                "        <a href='https://82.255.1.245:8443/parametric/nic34'>Link Acceso</a>\n" +
                "    </div>\n" +
                "    <footer>\n" +
                "        Nexco Reporte NIC34.\n" +
                "    </footer>\n" +
                "</body>\n" +
                "</html>";;

        sendEmailService.sendEmailCopAd(recipientEmail,recipientCopyEmail, subject, content);
    }

    public void sendEmailPrev(ArrayList<String[]> lista,User user)
    {
        lista.sort(Comparator.comparing(arr -> arr[0]));

        for (String[] arr:lista)
        {
            System.out.println(Arrays.toString(arr));
        }

        String actual="";
        String cuentas="";
        int posicion = 1;
        for (int i =0;i<lista.size();i++)
        {
            cuentas = cuentas + "<p>"+posicion+") "+lista.get(i)[1] +"</p></br>";
            posicion++;
            if(actual.length()==0)
            {
                actual=lista.get(i)[0];
            }
            if(lista.size()-1 == i || ( lista.size()-1 != i && !actual.equals(lista.get(i+1)[0])))
            {
                Query cTercero = entityManager.createNativeQuery("select * from nexco_usuarios where usuario = ?",User.class);
                cTercero.setParameter(1,actual.trim().toUpperCase());
                List<User> tempThird = cTercero.getResultList();
                if(!tempThird.isEmpty())
                    sendEmail(tempThird.get(0).getCorreo(),user.getCorreo(),cuentas);
                if( lista.size()-1 != i && !actual.equals(lista.get(i+1)[0]))
                    actual=lista.get(i+1)[0];
                cuentas="";
                posicion = 1;
            }
        }
    }
}
