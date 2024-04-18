package com.inter.proyecto_intergrupo.service.reportsServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanelIfrs;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.model.reports.Anexo8Finrep;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamMDARepository;
import com.inter.proyecto_intergrupo.repository.reports.Anexo8FinrepRepository;
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
public class Anexo8FinrepService {

    @Autowired
    private Anexo8FinrepRepository anexo8FinrepRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public Anexo8FinrepService(Anexo8FinrepRepository anexo8FinrepRepository) {
        this.anexo8FinrepRepository = anexo8FinrepRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Anexo 8");
        insert.setFecha(today);
        insert.setInput("FINREP");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Anexo8Finrep findByCuenta(String id){
        return anexo8FinrepRepository.findByCuenta(id);
    }

    public List<Anexo8Finrep> findAll()
    {
        return anexo8FinrepRepository.findAll();
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
                loadAudit(user,"Cargue Exitoso parametrica Anexo 8 FINREP");
            else
                loadAudit(user,"Cargue Fallido parametrica Anexo 8 FINREP");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Anexo8Finrep> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellCuenta = formatter.formatCellValue(row.getCell(0)).trim();

                    if (cellCuenta.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Cuenta no puede estar vacio.";
                        lista.add(log);
                    }
                    Anexo8Finrep anexo8Finrep = new Anexo8Finrep();
                    anexo8Finrep.setCuenta(cellCuenta);
                    toInsert.add(anexo8Finrep);
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
            anexo8FinrepRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public Anexo8Finrep saveAnexo8(Anexo8Finrep toSave, User user){
        loadAudit(user,"Adición Exitosa registro cuenta");
        return anexo8FinrepRepository.save(toSave);
    }

    public void removeAnexo8(String id, User user){
        loadAudit(user,"Eliminación Exitosa registro cuenta");
        anexo8FinrepRepository.deleteByCuenta(id);
    }

    public void clearAnexo8(User user){
        loadAudit(user,"Limpieza de tabla Exitosa moneda");
        anexo8FinrepRepository.deleteAll();
    }

    public Page<Anexo8Finrep> getAll(Pageable pageable){
        return anexo8FinrepRepository.findAll(pageable);
    }

    public List<Anexo8Finrep> findByFilter(String value, String filter) {
        List<Anexo8Finrep> list=new ArrayList<Anexo8Finrep>();
        switch (filter)
        {
            case "Cuenta":
                list=anexo8FinrepRepository.findByCuentaContainingIgnoreCase(value);
                break;
            default:
                break;
        }
        return list;
    }

    public List<Object[]> findDataFinrep(String periodo){
        /*Query query = entityManager.createNativeQuery("SELECT A.CENTRO,D.DescripcionCentro AS DESCRIPCION_CENTRO, A.CUENTA, C.DERECTA AS DESCRIPCION_CUENTA_PUC, DIVISA, IMPORTE, FECHA_ORIGEN, FECHA_CIERRE, TP, IDENTIFICACION,\n" +
                "DV, NOMBRE, CONTRATO, OBSERVACION, CUENTA_PROV, IMPORTE_PROV, IMPORTE_ORIGINAL, PROBABILIDAD_RECUPERACION, '1' AS ALTURA \n" +
                "FROM Cargas_Anexos_SICC_"+periodo.replace("-","")+" A \n" +
                "INNER JOIN nexco_anexo8_finrep B ON A.CUENTA = B.CUENTA\n" +
                "LEFT JOIN (select NUCTA, DERECTA from cuentas_puc where empresa = '0013') C ON B.CUENTA = C.NUCTA\n" +
                "LEFT JOIN CENTROS D ON A.centro = D.centro");
        return query.getResultList();*/

        Query query = entityManager.createNativeQuery("SELECT A.CENTRO,D.DescripcionCentro AS DESCRIPCION_CENTRO, A.CUENTA, C.DERECTA AS DESCRIPCION_CUENTA_PUC, DIVISA, IMPORTE, FECHA_ORIGEN, FECHA_CIERRE, TP, IDENTIFICACION,\n" +
                "DV, NOMBRE, CONTRATO, OBSERVACION, CUENTA_PROV, IMPORTE_PROV, IMPORTE_ORIGINAL, PROBABILIDAD_RECUPERACION, '1' AS ALTURA \n" +
                "FROM nexco_anexo8_finrep B \n" +
                "INNER JOIN (select distinct NUCTA, CODICONS46,DERECTA from cuentas_puc where empresa = '0013') C ON B.CUENTA = C.CODICONS46\n" +
                "INNER JOIN Cargas_Anexos_SICC_"+periodo.replace("-","")+" A ON C.NUCTA = A.CUENTA\n" +
                "LEFT JOIN CENTROS D ON A.centro = D.centro");
        return query.getResultList();
    }
}
