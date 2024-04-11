package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationOther;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationOtherPlane;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import com.inter.proyecto_intergrupo.repository.accountsReceivable.InvoicesCcRepository;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.AccountCreationOtherPlaneRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.AccountCreationOtherRepository;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.inter.proyecto_intergrupo.service.resourcesServices.SendEmailService;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STMerge;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional
public class AccountCreationOtherService {

    @Autowired
    private AccountCreationOtherRepository accountCreationOtherRepository;

    @Autowired
    private AccountCreationOtherPlaneRepository accountCreationOtherPlaneRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private SendEmailService sendEmailService;

    public AccountCreationOtherService(AccountCreationOtherRepository accountCreationOtherRepository) {
        this.accountCreationOtherRepository = accountCreationOtherRepository;
    }

    public boolean validarCargar(String periodo)
    {
        LocalDate fechaHoy = LocalDate.now();
        LocalDate fechaCadena = LocalDate.parse(periodo,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        if(fechaCadena.equals(fechaHoy))
            return true;
        else
            return false;
    }

    public boolean validarPlano(String periodo)
    {
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where estado_plano = 'PENDING' and periodo like ? ", AccountCreationOther.class);
        consulta.setParameter(1,periodo+"%");
        if(!consulta.getResultList().isEmpty())
            return true;
        else
            return false;
    }

    public List<AccountCreationOther> getAllData2(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo = ? ", AccountCreationOther.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<AccountCreationOtherPlane> getAccount(Long id){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros_planos where id_plano = ? ", AccountCreationOtherPlane.class);
        consulta.setParameter(1,id);
        return consulta.getResultList();
    }

    public List<AccountCreationOtherPlane> getAllDataPlane(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros_planos where fecha_creacion like ? order by fecha_creacion desc ", AccountCreationOtherPlane.class);
        consulta.setParameter(1,periodo+"%");
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getAllDataForPeriod(String inicio, String fin){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo >= ? and periodo <= ? and estado_plano = 'PENDING' ",AccountCreationOther.class);
        consulta.setParameter(1,inicio);
        consulta.setParameter(2,fin);
        return consulta.getResultList();
    }

    public void generatePlane(String inicio, String fin,List<AccountCreationOther>  lista, User user) throws ParseException, IOException {
        Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_plano = 'COMPLETED' where id_cuentas in (:valores) ;");
        consulta.setParameter("valores",lista);
        consulta.executeUpdate();

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formateador = DateTimeFormatter.ofPattern("yyMMdd");

        SimpleDateFormat formato1= new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formato2= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat formato3= new DecimalFormat("0.00");
        DecimalFormat formato4= new DecimalFormat("0");
        String nombreArchivo = "GOF.S0011.PHA.T001.ACUENTAS.F"+fechaActual.format(formateador)+".txt";

        File archivo = new File("C:\\Users\\CE66916\\Documents\\Mario\\"+nombreArchivo);

        BufferedWriter writer = new BufferedWriter(new FileWriter(archivo));
        String contenido = "";
        List<String> listado = new ArrayList<>();
        for (AccountCreationOther account : lista) {
            listado.add(account.getNucta());
            if(account.getIndic().equals("L")) {
                contenido = contenido + String.format("%-" + 4 + "s", account.getEmpresa()) +
                        String.format("%-" + 15 + "s", account.getNucta()) +
                        String.format("%-" + 65 + "s", account.getNombreCuentaLarga()) +
                        String.format("%-" + 35 + "s", account.getNombreCuentaCorta()) +
                        String.format("%-" + 4 + "s", "0008") +
                        String.format("%-" + 1 + "s", account.getTipoCta()) +
                        String.format("%-" + 1 + "s", account.getTipoCuentaOrden()) +
                        String.format("%-" + 1 + "s", account.getIndic()) +
                        String.format("%-" + 15 + "s", account.getContrapartidaResultados()) +
                        String.format("%-" + 1 + "s", account.getIndicadorCierre()) +
                        String.format("%-" + 15 + "s", account.getContrapartidaResultados()) +
                        String.format("%-" + 15 + "s", account.getContrapartidaOrden()) +
                        String.format("%-" + 1 + "s", account.getClaveAcceso()) +
                        String.format("%-" + 1 + "s", account.getTipoApunte()) +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", account.getMon());
            }
            else {
                contenido = contenido + String.format("%-" + 4 + "s", account.getEmpresa()) +
                        String.format("%-" + 15 + "s", account.getNucta()) +
                        String.format("%-" + 65 + "s", account.getNombreCuentaLarga()) +
                        String.format("%-" + 35 + "s", account.getNombreCuentaCorta()) +
                        String.format("%-" + 4 + "s", "0008") +
                        String.format("%-" + 1 + "s", account.getTipoCta()) +
                        String.format("%-" + 1 + "s", account.getTipoCuentaOrden()) +
                        String.format("%-" + 1 + "s", account.getIndic()) +
                        String.format("%-" + 15 + "s", account.getContrapartidaResultados()) +
                        String.format("%-" + 1 + "s", account.getIndicadorCierre()) +
                        String.format("%-" + 15 + "s", account.getContrapartidaResultados()) +
                        String.format("%-" + 15 + "s", account.getContrapartidaOrden()) +
                        String.format("%-" + 1 + "s", account.getClaveAcceso()) +
                        String.format("%-" + 1 + "s", account.getTipoApunte()) +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", account.getMon()) +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", account.getIndic().replace("L", " ")) +
                        String.format("%-" + 4 + "s", account.getIndic().replace("L", "").replace("I", "0460")) +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 1 + "s", account.getIndic().replace("I", "O").replace("L", "")) +
                        String.format("%-" + 1 + "s", account.getIndic().replace("I", "S").replace("L", "")) +
                        String.format("%-" + 1 + "s", account.getIndic().replace("I", "C").replace("L", "")) +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", "") +
                        String.format("%-" + 1 + "s", account.getIndic().replace("I", "T").replace("L", "")) +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 4 + "s", "") +
                        String.format("%-" + 5 + "s", account.getCodigoGestion()) +
                        String.format("%-" + 5 + "s", account.getConsolid()) +
                        String.format("%-" + 2 + "s", account.getCodigoControl()) +
                        String.format("%-" + 3 + "s", account.getDiasPlazo()) +
                        String.format("%-" + 9 + "s", "") +
                        String.format("%-" + 10 + "s", account.getNucta().substring(0, 6) + "0001") +
                        String.format("%-" + 2 + "s", "01") +
                        String.format("%-" + 5 + "s", account.getResponsablecontroloperativocenoperes1()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroloperativocenoperes2()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroloperativocenoperes3()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroloperativocenoperes4()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroloperativocenoperes5()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroloperativocenoperes6()) +
                        String.format("%-" + 2 + "s", "02") +
                        String.format("%-" + 5 + "s", account.getResponsablecontroldegestioncengesres1()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroldegestioncengesres2()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroldegestioncengesres3()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroldegestioncengesres4()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroldegestioncengesres5()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroldegestioncengesres6()) +
                        String.format("%-" + 2 + "s", "03") +
                        String.format("%-" + 5 + "s", account.getResponsablecontroladministrativocenadmres1()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroladministrativocenadmres2()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroladministrativocenadmres3()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroladministrativocenadmres4()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroladministrativocenadmres5()) +
                        String.format("%-" + 5 + "s", account.getResponsablecontroladministrativocenadmres6()) +
                        String.format("%-" + 9 + "s", account.getEpigrafe()) +
                        "\n";
            }
        }
        writer.write(contenido);
        writer.close();

        byte[] bytesArray = convertirFileABytes(archivo);

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");
        AccountCreationOtherPlane accountCreationOtherPlane = new AccountCreationOtherPlane();
        accountCreationOtherPlane.setFechaCreacion(new Date());
        accountCreationOtherPlane.setFechaInicio(formato.parse(inicio));
        accountCreationOtherPlane.setFechaFin(formato.parse(fin));
        accountCreationOtherPlane.setNombreArchivo(nombreArchivo);
        accountCreationOtherPlane.setUsuarioGenerador(user.getUsuario());
        accountCreationOtherPlane.setArchivo(bytesArray);
        accountCreationOtherPlaneRepository.save(accountCreationOtherPlane);

        Query consulta1 = entityManager.createNativeQuery("select a.* from nexco_usuarios a\n" +
                "inner join nexco_user_rol b on a.usuario = b.usuario\n" +
                "inner join nexco_rol_vista c on b.id_perfil=c.id_perfil\n" +
                "inner join nexco_vistas d on c.id_vista =d.id_vista where d.nombre in ('Ver Creación de Cuentas Otros (Control Contable)' ,'Ver Creación de Cuentas Otros (Consolidación)','Ver Creación de Cuentas Otros (Gestión)')", User.class);
        List<User> tempThird = consulta1.getResultList();

        String usuarios = "";
        String cuentas = "<table>\n" +
                "<tr>\n" +
                " <th>Cuenta</th>\n" +
                "</tr>\n" ;
        for (String part: listado) {
            cuentas = cuentas +
                    "<tr>\n" +
                    "<td>"+part+"</td>\n" +
                    "</tr>\n";
        }
        for (User part: tempThird) {
            usuarios = usuarios + part.getCorreo()+";";
        }
        cuentas = cuentas +"</table>";

        sendEmailConfirm(usuarios,user.getCorreo()+";con.group@bbva.com",cuentas);
    }

    public byte[] convertirFileABytes(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            long longitudArchivo = file.length();

            // Verificar si el archivo es demasiado grande para ser almacenado en un array de bytes
            if (longitudArchivo > Integer.MAX_VALUE) {
                throw new IOException("El archivo es demasiado grande para ser almacenado en un array de bytes.");
            }

            // Crear un array de bytes del tamaño del archivo
            byte[] bytesArray = new byte[(int) longitudArchivo];

            // Leer los bytes del archivo y almacenarlos en el array
            int offset = 0;
            int bytesRead;
            while (offset < bytesArray.length && (bytesRead = is.read(bytesArray, offset, bytesArray.length - offset)) >= 0) {
                offset += bytesRead;
            }

            // Verificar que se hayan leído todos los bytes
            if (offset < bytesArray.length) {
                throw new IOException("No se pudieron leer todos los bytes del archivo.");
            }

            return bytesArray;
        }
    }

    public List<Object[]> getAllDataPlaneResume(){
        Query consulta = entityManager.createNativeQuery("select estado_plano, count(estado_plano) as conteo from nexco_creacion_cuentas_otros where estado_plano is not null group by estado_plano ");
        return consulta.getResultList();
    }

    public List<Object[]> getAllDataPlaneResume2(String periodo){
        Query consulta = entityManager.createNativeQuery("select estado_plano, count(estado_plano) as conteo from nexco_creacion_cuentas_otros where estado_plano is not null and periodo like ? group by estado_plano ");
        consulta.setParameter(1,periodo+"%");
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getAllDataConsol(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo = ? and (estado_consolidacion != 'EMPTY' or comentario_consolidacion is not null or comentario_consolidacion != '' )", AccountCreationOther.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getAllDataControl(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo = ? and (estado_control != 'EMPTY' or comentario_control is not null or comentario_control != '' )", AccountCreationOther.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getAllDataGestion(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo = ? and (estado_gestion != 'EMPTY' or comentario_gestion is not null or comentario_gestion != '' )", AccountCreationOther.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public void anularRegistro(Long id){
        Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_general = 'CANCELLED', estado_gestion = 'EMPTY',estado_consolidacion = 'EMPTY',estado_control = 'EMPTY',  codigo_gestion = '-', epigrafe = '-', consolid = '-', codigo_control = '-', indicador_cuenta = '-', tipo_apunte = '-' where id_cuentas = ? ");
        consulta.setParameter(1,id);
        consulta.executeUpdate();
    }

    public List<AccountCreationOther> getDataPending(String rol){
        List<AccountCreationOther> result = new ArrayList<>();
        if(rol.equals("GENERAL")) {
            Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where estado_general in ('PENDING','EMPTY') ", AccountCreationOther.class);
            result= consulta.getResultList();
        }
        else if(rol.equals("GESTION")) {
            Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where estado_gestion = 'PENDING' ", AccountCreationOther.class);
            result= consulta.getResultList();
        }
        else if(rol.equals("CONSOLIDACION")) {
            Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where estado_consolidacion = 'PENDING' ", AccountCreationOther.class);
            result= consulta.getResultList();
        }
        else if(rol.equals("CONTROL CONTABLE")) {
            Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where estado_control = 'PENDING' ", AccountCreationOther.class);
            result= consulta.getResultList();
        }
        return result;
    }

    public List<AccountCreationOther> getDataActual(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo = ? ", AccountCreationOther.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public void generateGuia(HttpServletResponse response) throws IOException {

        File carpeta = new File("C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\guia");
        String nombreArchivo ="";
        if (carpeta.isDirectory()) {
            File[] archivos = carpeta.listFiles();
            if (archivos != null && archivos.length == 1) {
                File archivo = archivos[0];
                nombreArchivo = archivo.getName();
                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename="+nombreArchivo;
                response.setHeader(headerKey, headerValue);
                try (OutputStream out = response.getOutputStream();
                     FileInputStream fileInputStream = new FileInputStream(new File("C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\guia\\"+nombreArchivo))) {

                    // Leer desde el FileInputStream y escribir en el OutputStream de la respuesta
                    byte[] buffer = new byte[1024];
                    int longitud;
                    while ((longitud = fileInputStream.read(buffer)) != -1) {
                        out.write(buffer, 0, longitud);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<AccountCreationOther> getDataActual(String periodo,String rol){
        String campo = "";
        if(rol.equals("GENERAL"))
            campo ="estado_general";
        else if(rol.equals("CONSOLIDACION"))
            campo = "estado_consolidacion";
        else if(rol.equals("CONTROL CONTABLE"))
            campo = "estado_control";
        else if(rol.equals("GESTION"))
            campo = "estado_gestion";
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where periodo = ? and "+campo+" = 'PENDING' ", AccountCreationOther.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getDataConfirmated(){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where estado_general = 'COMPLETED' and estado_consolidacion = 'COMPLETED' and estado_control = 'COMPLETED' and estado_gestion = 'COMPLETED' ", AccountCreationOther.class);
        return consulta.getResultList();
    }
    public boolean validatePucEmpresa(String nucta, String empresa){
        Query consulta = entityManager.createNativeQuery("select top 1 * from cuentas_puc where NUCTA = ? and EMPRESA = ? ");
        consulta.setParameter(1,nucta);
        consulta.setParameter(2,empresa);
        return !consulta.getResultList().isEmpty();
    }
    public boolean validateHistoricNucta(String nucta, String empresa){
        Query consulta = entityManager.createNativeQuery("select top 1 * from nexco_creacion_cuentas_otros where nucta = ? and empresa = ? and estado_general != 'CANCELLED' ");
        consulta.setParameter(1,nucta);
        consulta.setParameter(2,empresa);
        return !consulta.getResultList().isEmpty();
    }

    public List<AccountCreationOther> getHistoricNuctaConsolidacion(String nucta, String empresa){
        Query consulta = entityManager.createNativeQuery("select top 1 * from nexco_creacion_cuentas_otros where nucta = ? and empresa = ? and estado_general = 'COMPLETED' and estado_consolidacion = 'PENDING' ", AccountCreationOther.class);
        consulta.setParameter(1,nucta);
        consulta.setParameter(2,empresa);
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getHistoricNuctaGestion(String nucta, String empresa){
        Query consulta = entityManager.createNativeQuery("select top 1 * from nexco_creacion_cuentas_otros where nucta = ? and empresa = ? and estado_general = 'COMPLETED' and estado_gestion = 'PENDING' ", AccountCreationOther.class);
        consulta.setParameter(1,nucta);
        consulta.setParameter(2,empresa);
        return consulta.getResultList();
    }

    public List<AccountCreationOther> getHistoricNuctaControl(String nucta, String empresa){
        Query consulta = entityManager.createNativeQuery("select top 1 * from nexco_creacion_cuentas_otros where nucta = ? and empresa = ? and estado_general = 'COMPLETED' and estado_control = 'PENDING' ", AccountCreationOther.class);
        consulta.setParameter(1,nucta);
        consulta.setParameter(2,empresa);
        return consulta.getResultList();
    }

    public void extractAccounts(List<String> useList,User user){
        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where id_cuentas in ( :valores ) ;", AccountCreationOther.class);
        consulta.setParameter("valores", useList);
        List<AccountCreationOther> listResult = consulta.getResultList();

        Query consulta1 = entityManager.createNativeQuery("select a.* from nexco_usuarios a\n" +
                "inner join nexco_user_rol b on a.usuario = b.usuario\n" +
                "inner join nexco_rol_vista c on b.id_perfil=c.id_perfil\n" +
                "inner join nexco_vistas d on c.id_vista =d.id_vista where d.nombre in ('Ver Creación de Cuentas Otros (Control Contable)' ,'Ver Creación de Cuentas Otros (Consolidación)','Ver Creación de Cuentas Otros (Gestión)')", User.class);
        List<User> tempThird = consulta1.getResultList();

        String usuarios = "";
        String cuentas = "<table>\n" +
                "<tr>\n" +
                " <th>Empresa</th>\n" +
                " <th>Cuenta</th>\n" +
                " <th>Nombre Corto</th>\n" +
                "</tr>\n" ;
        for (AccountCreationOther part: listResult) {
            cuentas = cuentas +
                    "<tr><td>"+part.getEmpresa()+"</td>\n" +
                    "<td>"+part.getNucta()+"</td>\n" +
                    "<td>"+part.getNombreCuentaCorta()+"</td></tr>\n";
        }
        for (User part: tempThird) {
            usuarios = usuarios + part.getCorreo()+";";
        }
        cuentas = cuentas +"</table>";

        sendEmail(usuarios,user.getCorreo()+";con.group@bbva.com",cuentas,listResult.get(0).getPeriodo());

    }

    public void generateProcess(String periodo,String[] opcionesLista, String rol,User user) {

        List<String> useList = Arrays.asList(opcionesLista);

        if(rol.equals("GENERAL")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_general = 'COMPLETED', estado_control = 'PENDING', estado_gestion = 'PENDING', estado_consolidacion = 'PENDING' where periodo = :periodo and id_cuentas in (:valores)  and indic = 'I'; \n" +
                    "update nexco_creacion_cuentas_otros set estado_general = 'COMPLETED', estado_control = 'COMPLETED', estado_gestion = 'COMPLETED', estado_consolidacion = 'COMPLETED', estado_plano = 'PENDING' where periodo = :periodo and id_cuentas in (:valores)  and indic = 'L'; ");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", useList);
            consulta.executeUpdate();

            extractAccounts(useList,user);
        }
        else if(rol.equals("GESTION")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_gestion = 'COMPLETED' where periodo = :periodo and id_cuentas in (:valores) ; ");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", useList);
            consulta.executeUpdate();
        }
        else if(rol.equals("CONTROL CONTABLE")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_control = 'COMPLETED' where periodo = :periodo and id_cuentas in (:valores) ; ");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", useList);
            consulta.executeUpdate();
        }
        else if(rol.equals("CONSOLIDACION")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_consolidacion = 'COMPLETED' where periodo = :periodo and id_cuentas in (:valores) ; ");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", useList);
            consulta.executeUpdate();
        }
        Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_plano = 'PENDING' where estado_general = 'COMPLETED' and estado_control = 'COMPLETED' and estado_gestion = 'COMPLETED' and estado_consolidacion = 'COMPLETED' and estado_plano is null ;");
        consulta.executeUpdate();
    }

    public void generateProcessResp(String periodo,Long id, String rol,User user,String cambio) {

        if(rol.equals("GENERAL")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_gestion = 'PENDING', estado_general = 'COMPLETED' where periodo = :periodo and id_cuentas = :valores and estado_gestion != 'COMPLETED';" +
                    "update nexco_creacion_cuentas_otros set estado_control = 'PENDING', estado_general = 'COMPLETED' where periodo = :periodo and id_cuentas = :valores and estado_control != 'COMPLETED' ;" +
                    "update nexco_creacion_cuentas_otros set estado_consolidacion = 'PENDING', estado_general = 'COMPLETED' where periodo = :periodo and id_cuentas = :valores and estado_consolidacion != 'COMPLETED' ;");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", id);
            consulta.executeUpdate();
            System.out.println(cambio);
            sendEmailMessage(id,user,rol,cambio);
        }
        else if(rol.equals("GESTION")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_gestion = 'EMPTY', estado_general = 'PENDING' where periodo = :periodo and id_cuentas = :valores ;");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", id);
            consulta.executeUpdate();
            sendEmailMessage(id,user,rol);
        }
        else if(rol.equals("CONTROL CONTABLE")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_control = 'EMPTY', estado_general = 'PENDING' where periodo = :periodo and id_cuentas = :valores ;");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", id);
            consulta.executeUpdate();
            sendEmailMessage(id,user,rol);
        }
        else if(rol.equals("CONSOLIDACION")) {
            Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set estado_consolidacion = 'EMPTY', estado_general = 'PENDING' where periodo = :periodo and id_cuentas = :valores ; ");
            consulta.setParameter("periodo", periodo);
            consulta.setParameter("valores", id);
            consulta.executeUpdate();
            sendEmailMessage(id,user,rol);
        }

        Query consulta = entityManager.createNativeQuery("update nexco_creacion_cuentas_otros set comentario_consolidacion = null where periodo = :periodo and comentario_consolidacion = '' ;" +
                "update nexco_creacion_cuentas_otros set comentario_gestion = null where periodo = :periodo and comentario_gestion = '' ;" +
                "update nexco_creacion_cuentas_otros set comentario_control = null where periodo = :periodo and comentario_control = '' ;" +
                "update nexco_creacion_cuentas_otros set codigo_gestion = null where periodo = :periodo and codigo_gestion = '' ;" +
                "update nexco_creacion_cuentas_otros set epigrafe = null where periodo = :periodo and epigrafe = '' ;" +
                "update nexco_creacion_cuentas_otros set consolid = null where periodo = :periodo and consolid = '' ;" +
                "update nexco_creacion_cuentas_otros set tipo_apunte = null where periodo = :periodo and tipo_apunte = '' ;" +
                "update nexco_creacion_cuentas_otros set codigo_control = null where periodo = :periodo and codigo_control = '' ;" +
                "update nexco_creacion_cuentas_otros set indicador_cuenta = null where periodo = :periodo and indicador_cuenta = '' ;");
        consulta.setParameter("periodo", periodo);
        consulta.executeUpdate();
    }

    public AccountCreationOther findByIdCuenta(Long id,User user){
        loadAudit(user,"Preliminar editar registro "+id+" de Creación de Otras Cuentas");
        return accountCreationOtherRepository.findByIdCuentas(id);
    }

    public AccountCreationOther modifyAccount(AccountCreationOther toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro "+toModify.getIdCuentas()+" en Creación de Otras Cuentas");
        return accountCreationOtherRepository.save(toModify);
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Creación de Cuentas");
        insert.setFecha(today);
        insert.setInput("Otras Cuentas");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ArrayList<String[]> saveFileBD(InputStream file, String periodo,User user,String rol) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            if(rol.equals("GENERAL"))
                list = validarPlantilla(rows,periodo,user);
            else if(rol.equals("CONSOLIDACION"))
                list = validarPlantillaConsolidacion(rows,periodo,user);
            else if(rol.equals("CONTROL CONTABLE"))
                list = validarPlantillaControlContable(rows,periodo,user);
            else if(rol.equals("GESTION"))
                list = validarPlantillaGestion(rows,periodo,user);
        }
        return list;
    }

    public void saveFileGuia(InputStream file, String periodo, String nombre) throws IOException, InvalidFormatException {
        if (file != null) {
            File carpeta = new File("C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\guia");
            if (carpeta.isDirectory()) {
                File[] archivos = carpeta.listFiles();
                if (archivos != null){
                    for (File archivo : archivos) {
                        if (archivo.isFile()) {
                            archivo.delete();
                        }
                    }
                }
            }
            Files.copy(file, Path.of("C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\guia\\"+nombre), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo,User user) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<AccountCreationOther> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                int conteo = 0;
                DataFormatter formatter = new DataFormatter();
                String cellEmpresa = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNucta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaLarga = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaCorta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellJustificacion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDinamica = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndic = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellClaveAcceso = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellMon = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroO = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroD = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoGestion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellEpigrafe = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellConsolid = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoApunte = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCierre = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDiasPlazo = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellInterfaz = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCtaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaResultados = formatter.formatCellValue(row.getCell(conteo++)).trim();

                if (cellEmpresa.length() != 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Empresa debe ser de 4 caracteres";
                    lista.add(log1);
                }
                if (cellNucta.length() < 6 || cellNucta.length() > 15) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El campo Cuenta debe contener de 6 a 15 cracteres";
                    lista.add(log1);
                }
                else if (validateHistoricNucta(cellNucta,cellEmpresa)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Cuenta "+cellNucta+" con empresa "+cellEmpresa+" ya se encuentra en registros históricos";
                    lista.add(log1);
                }
                else if (validatePucEmpresa(cellNucta,cellEmpresa)) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Cuenta "+cellNucta+" con empresa "+cellEmpresa+" ya se encuentra existente en el PUC";
                    lista.add(log1);
                }
                if (cellNombreCuentaLarga.length() > 65 || cellNombreCuentaLarga.length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El campo Nombre Cuenta Larga no puede exceder los 65 caracteres";
                    lista.add(log1);
                }
                if (cellNombreCuentaCorta.length() > 35 || cellNombreCuentaCorta.length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El campo Nombre Cuenta Corta no puede exceder los 35 caracteres";
                    lista.add(log1);
                }
                if (cellJustificacion.length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(4);
                    log1[2] = "El campo Justificación Global no puede estar vacía";
                    lista.add(log1);
                }
                if (cellDinamica.length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El campo Dinamica Cuenta no puede estar vacío";
                    lista.add(log1);
                }
                if (!cellTipoCta.equals("A") && !cellTipoCta.equals("P") && !cellTipoCta.equals("I") && !cellTipoCta.equals("G") && !cellTipoCta.equals("PT") && !cellTipoCta.equals("O") && !cellTipoCta.equals("K")) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El campo Tipo Cuenta debe estar en el siguiente catalogo: A , P , I , G , PT , O , K";
                    lista.add(log1);
                }
                if (!cellIndic.equals("L") && !cellIndic.equals("I")) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(7);
                    log1[2] = "El campo Indic debe estar en el siguiente catalogo: L , I";
                    lista.add(log1);
                }
                else {
                    if (!cellIndic.equals("L")) {
                        if (!cellClaveAcceso.equals("B") && !cellClaveAcceso.equals("A") && !cellClaveAcceso.equals("O")) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(8);
                            log1[2] = "El campo Clave Acceso debe estar en el siguiente catalogo: B , A , O";
                            lista.add(log1);
                        }
                        if (!cellMon.equals("0") && !cellMon.equals("1") && !cellMon.equals("2")) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(9);
                            log1[2] = "El campo Mon debe estar en el siguiente catalogo: 0 , 1 , 2";
                            lista.add(log1);
                        }
                        if (cellCentroO.length() == 0) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(10);
                            log1[2] = "El campo Centro Origen no puede estar vacio";
                            lista.add(log1);
                        }
                        if (cellCentroD.length() == 0) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(11);
                            log1[2] = "El campo Centro Destino no puede estar vacio";
                            lista.add(log1);
                        }
                        /*if (cellCodigoGestion.length() != 5) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(12);
                            log1[2] = "El campo Código de Gestión debe contener 5 caracteres";
                            lista.add(log1);
                        }
                        if (cellEpigrafe.length() != 9) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(13);
                            log1[2] = "El campo Epigrafe debe contener 9 caracteres";
                            lista.add(log1);
                        }
                        if (cellConsolid.length() != 5) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(14);
                            log1[2] = "El campo Consolid debe contener 5 caracteres";
                            lista.add(log1);
                        }
                        if (cellTipoApunte.length() != 1) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(15);
                            log1[2] = "El campo Tipo Apunte debe contener 1 caracteres";
                            lista.add(log1);
                        }
                        if (cellCodigoControl.length() != 2) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(16);
                            log1[2] = "El campo Código Control debe contener 2 caracteres";
                            lista.add(log1);
                        }*/
                        if (!cellIndicadorCierre.equals("+") && !cellIndicadorCierre.equals("-")) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(17);
                            log1[2] = "El campo Ind Cierre debe estar en el siguiente catalogo: + , -";
                            lista.add(log1);
                        }
                        if (cellDiasPlazo.length() != 3) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(18);
                            log1[2] = "El campo Días Plazo debe contener 3 caracteres";
                            lista.add(log1);
                        }
                        /*if (cellIndicadorCta.length() != 1) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(19);
                            log1[2] = "El campo Indicador CTA debe contener 1 caracteres";
                            lista.add(log1);
                        }
                        if (cellInterfaz.length() != 3) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(20);
                            log1[2] = "El campo Interfaz debe contener 3 caracteres";
                            lista.add(log1);
                        }*/
                        if (cellTipoCta.equals("O") && (cellNucta.substring(0,1).equals("6") || cellNucta.substring(0,1).equals("8")) && (cellContrapartidaOrden.length() > 9 || cellContrapartidaOrden.length() == 0)) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(39);
                            log1[2] = "El campo Contrapartida Orden debe contener de 1 a 9 caracteres";
                            lista.add(log1);
                        }
                        if (cellTipoCta.equals("O") && (cellNucta.substring(0,1).equals("6") || cellNucta.substring(0,1).equals("8")) && cellTipoCtaOrden.length() != 1) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(40);
                            log1[2] = "El campo Tipo Cuenta Orden debe contener 1 caracter";
                            lista.add(log1);
                        }
                        if ((cellNucta.substring(0,1).equals("4") || cellNucta.substring(0,1).equals("5")) && cellContrapartidaResultados.length() != 9) {
                            String[] log1 = new String[3];
                            log1[0] = String.valueOf(row.getRowNum() + 1);
                            log1[1] = CellReference.convertNumToColString(41);
                            log1[2] = "El campo Contrapartida de Resultados D/H debe contener 9 caracteres";
                            lista.add(log1);
                        }
                    }
                }
                /*if (cellRco1.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(21);
                    log1[2] = "El campo Responsable Operativo 1 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRco2.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(22);
                    log1[2] = "El campo Responsable Operativo 2 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRco3.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(23);
                    log1[2] = "El campo Responsable Operativo 3 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRco4.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(24);
                    log1[2] = "El campo Responsable Operativo 4 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRco5.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(25);
                    log1[2] = "El campo Responsable Operativo 5 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRco6.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(26);
                    log1[2] = "El campo Responsable Operativo 6 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRcg1.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(27);
                    log1[2] = "El campo Responsable De Gestión 1 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRcg2.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(28);
                    log1[2] = "El campo Responsable De Gestión 2 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRcg3.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(29);
                    log1[2] = "El campo Responsable De Gestión 3 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRcg4.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(30);
                    log1[2] = "El campo Responsable De Gestión 4 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRcg5.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(31);
                    log1[2] = "El campo Responsable De Gestión 5 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRcg6.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(32);
                    log1[2] = "El campo Responsable De Gestión 6 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRca1.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(33);
                    log1[2] = "El campo Responsable Administrativo 1 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRca2.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(34);
                    log1[2] = "El campo Responsable Administrativo 2 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRca3.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(35);
                    log1[2] = "El campo Responsable Administrativo 3 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRca4.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(36);
                    log1[2] = "El campo Responsable Administrativo 4 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRca5.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(37);
                    log1[2] = "El campo Responsable Administrativo 5 debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellRca6.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(38);
                    log1[2] = "El campo Responsable Administrativo 6 debe contener 5 caracteres";
                    lista.add(log1);
                }*/

                if(lista.isEmpty())
                {
                    AccountCreationOther accountCreationOther = new AccountCreationOther();
                    accountCreationOther.setEmpresa(cellEmpresa);
                    accountCreationOther.setNucta(cellNucta);
                    accountCreationOther.setNombreCuentaLarga(cellNombreCuentaLarga);
                    accountCreationOther.setNombreCuentaCorta(cellNombreCuentaCorta);
                    accountCreationOther.setJustificacionGlobal(cellJustificacion);
                    accountCreationOther.setDinamicaCuenta(cellDinamica);
                    accountCreationOther.setTipoCta(cellTipoCta);
                    accountCreationOther.setIndic(cellIndic);
                    accountCreationOther.setClaveAcceso(cellClaveAcceso);
                    accountCreationOther.setMon(cellMon);
                    accountCreationOther.setCentroOrigen(cellCentroO);
                    accountCreationOther.setCentroDestino(cellCentroD);
                    if (cellIndic.equals("L")) {
                        accountCreationOther.setCodigoGestion(cellCodigoGestion);
                        accountCreationOther.setConsolid(cellConsolid);
                        accountCreationOther.setEpigrafe(cellEpigrafe);
                        accountCreationOther.setTipoApunte(cellTipoApunte);
                        accountCreationOther.setCodigoControl(cellCodigoControl);
                        accountCreationOther.setIndicadorCuenta(cellIndicadorCta);
                    }
                    accountCreationOther.setDiasPlazo(cellDiasPlazo);
                    accountCreationOther.setIndicadorCierre(cellIndicadorCierre);
                    accountCreationOther.setInterfaz(cellInterfaz);
                    accountCreationOther.setResponsablecontroloperativocenoperes1(cellRco1);
                    accountCreationOther.setResponsablecontroloperativocenoperes2(cellRco2);
                    accountCreationOther.setResponsablecontroloperativocenoperes3(cellRco3);
                    accountCreationOther.setResponsablecontroloperativocenoperes4(cellRco4);
                    accountCreationOther.setResponsablecontroloperativocenoperes5(cellRco5);
                    accountCreationOther.setResponsablecontroloperativocenoperes6(cellRco6);
                    accountCreationOther.setResponsablecontroldegestioncengesres1(cellRcg1);
                    accountCreationOther.setResponsablecontroldegestioncengesres2(cellRcg2);
                    accountCreationOther.setResponsablecontroldegestioncengesres3(cellRcg3);
                    accountCreationOther.setResponsablecontroldegestioncengesres4(cellRcg4);
                    accountCreationOther.setResponsablecontroldegestioncengesres5(cellRcg5);
                    accountCreationOther.setResponsablecontroldegestioncengesres6(cellRcg6);
                    accountCreationOther.setResponsablecontroladministrativocenadmres1(cellRca1);
                    accountCreationOther.setResponsablecontroladministrativocenadmres2(cellRca2);
                    accountCreationOther.setResponsablecontroladministrativocenadmres3(cellRca3);
                    accountCreationOther.setResponsablecontroladministrativocenadmres4(cellRca4);
                    accountCreationOther.setResponsablecontroladministrativocenadmres5(cellRca5);
                    accountCreationOther.setResponsablecontroladministrativocenadmres6(cellRca6);
                    accountCreationOther.setContrapartidaOrden(cellContrapartidaOrden);
                    accountCreationOther.setTipoCuentaOrden(cellTipoCtaOrden);
                    accountCreationOther.setContrapartidaResultados(cellContrapartidaResultados);
                    accountCreationOther.setCreador(user.getUsuario());
                    accountCreationOther.setPeriodo(periodo);
                    accountCreationOther.setEstadoGeneral("EMPTY");
                    accountCreationOther.setEstadoConsolidacion("EMPTY");
                    accountCreationOther.setEstadoControl("EMPTY");
                    accountCreationOther.setEstadoGestion("EMPTY");
                    toInsert.add(accountCreationOther);
                }
            }
        }
        if(lista.size()!=0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size()*41)-lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS"))
        {
            accountCreationOtherRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> validarPlantillaConsolidacion(Iterator<Row> rows, String periodo,User user) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<AccountCreationOther> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                int conteo = 0;
                DataFormatter formatter = new DataFormatter();
                String cellEmpresa = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNucta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaLarga = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaCorta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellJustificacion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDinamica = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndic = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellClaveAcceso = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellMon = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroO = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroD = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoGestion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellEpigrafe = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellConsolid = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoApunte = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCierre = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDiasPlazo = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellInterfaz = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCtaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaResultados = formatter.formatCellValue(row.getCell(conteo++)).trim();

                if (cellEmpresa.length() != 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Empresa debe ser de 4 caracteres";
                    lista.add(log1);
                }
                if (cellNucta.length() < 6 || cellNucta.length() > 15) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El campo Cuenta debe contener de 6 a 15 cracteres";
                    lista.add(log1);
                }
                List<AccountCreationOther> part = getHistoricNuctaConsolidacion(cellNucta,cellEmpresa);
                if (part.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Cuenta "+cellNucta+" y Empresa "+cellEmpresa+" no se encontro dentro de los registros.";
                    lista.add(log1);
                }
                if (cellConsolid.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(14);
                    log1[2] = "El campo Consolid debe contener 5 caracteres";
                    lista.add(log1);
                }


                if(lista.isEmpty()) {
                    AccountCreationOther accountCreationOther = part.get(0);
                    accountCreationOther.setConsolid(cellConsolid);
                    toInsert.add(accountCreationOther);
                }

            }
        }
        if(lista.size()!=0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size()*41)-lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS"))
        {
            accountCreationOtherRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> validarPlantillaGestion(Iterator<Row> rows, String periodo,User user) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<AccountCreationOther> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                int conteo = 0;
                DataFormatter formatter = new DataFormatter();
                String cellEmpresa = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNucta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaLarga = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaCorta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellJustificacion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDinamica = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndic = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellClaveAcceso = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellMon = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroO = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroD = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoGestion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellEpigrafe = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellConsolid = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoApunte = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCierre = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDiasPlazo = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellInterfaz = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCtaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaResultados = formatter.formatCellValue(row.getCell(conteo++)).trim();

                if (cellEmpresa.length() != 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Empresa debe ser de 4 caracteres";
                    lista.add(log1);
                }
                if (cellNucta.length() < 6 || cellNucta.length() > 15) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El campo Cuenta debe contener de 6 a 15 cracteres";
                    lista.add(log1);
                }
                List<AccountCreationOther> part = getHistoricNuctaGestion(cellNucta,cellEmpresa);
                if (part.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Cuenta "+cellNucta+" y Empresa "+cellEmpresa+" no se encontro dentro de los registros.";
                    lista.add(log1);
                }
                if (cellCodigoGestion.length() != 5) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(12);
                    log1[2] = "El campo Código de Gestión debe contener 5 caracteres";
                    lista.add(log1);
                }
                if (cellEpigrafe.length() != 9) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(13);
                    log1[2] = "El campo Epigrafe debe contener 9 caracteres";
                    lista.add(log1);
                }

                if(lista.isEmpty()) {
                    AccountCreationOther accountCreationOther = part.get(0);
                    accountCreationOther.setCodigoGestion(cellCodigoGestion);
                    accountCreationOther.setEpigrafe(cellEpigrafe);
                    toInsert.add(accountCreationOther);
                }

            }
        }
        if(lista.size()!=0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size()*4)-lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS"))
        {
            accountCreationOtherRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> validarPlantillaControlContable(Iterator<Row> rows, String periodo,User user) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";
        ArrayList<AccountCreationOther> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                int conteo = 0;
                DataFormatter formatter = new DataFormatter();
                String cellEmpresa = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNucta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaLarga = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellNombreCuentaCorta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellJustificacion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDinamica = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndic = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellClaveAcceso = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellMon = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroO = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCentroD = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoGestion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellEpigrafe = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellConsolid = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoApunte = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellCodigoControl = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCierre = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellDiasPlazo = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellIndicadorCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellInterfaz = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRco6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRcg6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca1 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca2 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca3 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca4 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca5 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellRca6 = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellTipoCtaOrden = formatter.formatCellValue(row.getCell(conteo++)).trim();
                String cellContrapartidaResultados = formatter.formatCellValue(row.getCell(conteo++)).trim();

                if (cellEmpresa.length() != 4) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El campo Empresa debe ser de 4 caracteres";
                    lista.add(log1);
                }
                if (cellNucta.length() < 6 || cellNucta.length() > 15) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El campo Cuenta debe contener de 6 a 15 cracteres";
                    lista.add(log1);
                }
                List<AccountCreationOther> part = getHistoricNuctaControl(cellNucta,cellEmpresa);
                if (part.isEmpty()) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El Cuenta "+cellNucta+" y Empresa "+cellEmpresa+" no se encontro dentro de los registros.";
                    lista.add(log1);
                }
                if (cellCodigoControl.length() != 2) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(16);
                    log1[2] = "El campo Código Control debe contener 2 caracteres";
                    lista.add(log1);
                }
                if (cellIndicadorCta.length() != 1) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(19);
                    log1[2] = "El campo Indicador CTA debe contener 1 caracteres";
                    lista.add(log1);
                }
                if (cellTipoApunte.length() == 0) {
                    cellTipoApunte=" ";
                }

                if(lista.isEmpty()) {
                    AccountCreationOther accountCreationOther = part.get(0);
                    accountCreationOther.setCodigoControl(cellCodigoControl);
                    accountCreationOther.setIndicadorCuenta(cellIndicadorCta);
                    accountCreationOther.setTipoApunte(cellTipoApunte);
                    toInsert.add(accountCreationOther);
                }

            }
        }
        if(lista.size()!=0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size()*41)-lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS"))
        {
            accountCreationOtherRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public void sendEmailConfirm(String recipientEmail,String recipientCopyEmail,String data) {
        String subject = "[NEXCO] Confirmación Creación Otras Cuentas";

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
                "        <h1>¡Confirmación Creación de Otras Cuentas!</h1>\n" +
                "    </header>\n" +
                "    <div class=\"content\">\n" +
                "        <h2>Se ha realizado la creación de las siguientes cuentas:</h2>\n" +
                data+
                "    </div>\n" +
                "    <footer>\n" +
                "        Nexco Creación de Otras Cuentas.\n" +
                "    </footer>\n" +
                "</body>\n" +
                "</html>";;

        sendEmailService.sendEmailCopAd(recipientEmail,recipientCopyEmail, subject, content);
    }

    public void sendEmail(String recipientEmail,String recipientCopyEmail,String data, String periodo) {
        String subject = "[NEXCO] Notificaciones Creación Otras Cuentas";

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
                "        <h1>¡Notificación Creación de Otras Cuentas!</h1>\n" +
                "    </header>\n" +
                "    <div class=\"content\">\n" +
                "        <h2>Se ha realizado el cargue de las siguientes cuentas en el modulo de Creación de Otras Cuentas:</h2>\n" +
                data+
                "        <p>Por favor ingrese al aplicativo Nexco en el apartado de Creación de Cuentas -> Otras Cuentas -> Creación de Cuentas, descargue la pre-carga en el periodo "+periodo+" y complete las columnas de acuerdo a su area: </p>\n" +
                "        <p>1) Area Gestión: Código Gestion y Epigrafe.</p>\n" +
                "        <p>2) Area Consolidación: Consolid.</p>\n" +
                "        <p>3) Area Control Contable: Tipo Apunte, Cod Control y Ind Cta Inv.</p>\n" +
                "        <p>Por último confirme los datos cargados.</p>\n" +
                "        <a href='https://82.255.1.245:8443/ifrs/accountCreationOther'>Link Acceso Creación de Otras Cuentas</a>\n" +
                "    </div>\n" +
                "    <footer>\n" +
                "        Nexco Creación de Otras Cuentas.\n" +
                "    </footer>\n" +
                "</body>\n" +
                "</html>";;

        sendEmailService.sendEmailCopAd(recipientEmail,recipientCopyEmail, subject, content);
    }

    public void sendEmailMessage(Long id, User user,String rol) {

        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where id_cuentas = :valores ;", AccountCreationOther.class);
        consulta.setParameter("valores", id);
        AccountCreationOther data = (AccountCreationOther) consulta.getResultList().get(0);

        Query consulta1 = entityManager.createNativeQuery("select a.* from nexco_usuarios a where a.usuario = ? ", User.class);
        consulta1.setParameter(1,data.getCreador());
        List<User> tempThird = consulta1.getResultList();

        String mensaje = "Revisa la bandeja en vista";
        String usuarios = "";
        if(rol.equals("CONSOLIDACION"))
        {
            mensaje = data.getComentarioConsolidacion();
        }
        else if(rol.equals("GESTION")) {
            mensaje = data.getComentarioGestion();
        }
        else if(rol.equals("CONTROL CONTABLE")) {
            mensaje = data.getComentarioControl();
        }

        for (User part: tempThird) {
            usuarios = usuarios + part.getCorreo()+";";
        }

        String subject = "[NEXCO] Notificación Mensaje Creación Otras Cuentas";
        String content = processMessage(mensaje,user,data,rol);
        sendEmailService.sendEmailCopAd(usuarios,user.getCorreo()+";con.group@bbva.com", subject, content);
    }

    public void sendEmailMessage(Long id, User user,String rol, String cambio) {

        Query consulta = entityManager.createNativeQuery("select * from nexco_creacion_cuentas_otros where id_cuentas = :valores ;", AccountCreationOther.class);
        consulta.setParameter("valores", id);
        AccountCreationOther data = (AccountCreationOther) consulta.getResultList().get(0);
        String subject = "[NEXCO] Notificación Mensaje Creación Otras Cuentas";

        if(cambio.contains("CONSOLIDACION")) {
            Query consulta1 = entityManager.createNativeQuery("select a.* from nexco_usuarios a where a.usuario = ? ", User.class);
            consulta1.setParameter(1,data.getUsuarioConsolidacion());
            User tempThird = (User) consulta1.getResultList().get(0);
            String mensaje = data.getComentarioConsolidacion();
            String content = processMessage(mensaje,user,data,rol);
            sendEmailService.sendEmailCopAd(tempThird.getCorreo(),user.getCorreo()+";con.group@bbva.com", subject, content);
        }
        if(cambio.contains("GESTION")) {
            Query consulta1 = entityManager.createNativeQuery("select a.* from nexco_usuarios a where a.usuario = ? ", User.class);
            consulta1.setParameter(1,data.getUsuarioGestion());
            User tempThird = (User) consulta1.getResultList().get(0);
            String mensaje = data.getComentarioGestion();
            String content = processMessage(mensaje,user,data,rol);
            sendEmailService.sendEmailCopAd(tempThird.getCorreo(),user.getCorreo()+";con.group@bbva.com", subject, content);
        }
        if(cambio.contains("CONTROL CONTABLE")) {
            Query consulta1 = entityManager.createNativeQuery("select a.* from nexco_usuarios a where a.usuario = ? ", User.class);
            consulta1.setParameter(1,data.getUsuarioControl());
            User tempThird = (User) consulta1.getResultList().get(0);
            String mensaje = data.getComentarioControl();
            String content = processMessage(mensaje,user,data,rol);
            sendEmailService.sendEmailCopAd(tempThird.getCorreo(),user.getCorreo()+";con.group@bbva.com", subject, content);
        }
    }

    public String processMessage (String mensaje,User user, AccountCreationOther data, String rol)
    {
        return"<!DOCTYPE html>\n" +
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
                "        <h1>¡[NEXCO] Notificación Mensaje Creación de Otras Cuentas!</h1>\n" +
                "    </header>\n" +
                "    <div class=\"content\">\n" +
                "        <h2>Mensaje Nuevo Recibido</h2>\n" +
                "        <p>Se ha recibido un mensaje del Area "+rol+" con la sigueinte información:</p>\n" +
                "        <p>Remitente: "+user.getNombre()+".</p>\n" +
                "        <p>Cuenta: "+data.getNucta()+".</p>\n" +
                "        <p>Empresa: "+data.getEmpresa()+".</p>\n" +
                "        <p>Periodo: "+data.getPeriodo()+".</p>\n" +
                "        <p>Mensaje: "+mensaje+".</p><br/>\n" +
                "        <p>Para dar respuesta al mensaje de ser necesario ingrese al apartado de Mensajes de la respectiva cuenta dentro del modulo de Creación de Otras Cuentas.</p>\n" +
                "        <a href='https://82.255.1.245:8443/ifrs/accountCreationOther'>Link Acceso Creación de Otras Cuentas</a>\n" +
                "    </div>\n" +
                "    <footer>\n" +
                "        Nexco Creación de Otras Cuentas.\n" +
                "    </footer>\n" +
                "</body>\n" +
                "</html>";
    }
}
