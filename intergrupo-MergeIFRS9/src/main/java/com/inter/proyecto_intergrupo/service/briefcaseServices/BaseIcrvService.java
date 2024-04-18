package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.BaseIcrvRepository;
import com.inter.proyecto_intergrupo.repository.parametric.SignatureRepository;
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
import java.io.ByteArrayOutputStream;
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
public class BaseIcrvService {

    @Autowired
    private BaseIcrvRepository baseIcrvRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public BaseIcrvService(BaseIcrvRepository baseIcrvRepository) {
        this.baseIcrvRepository = baseIcrvRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Base ICRV");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public BaseIcrv findByIdBase(Long id){
        return baseIcrvRepository.findByIdBase(id);
    }

    public List<BaseIcrv> findAll()
    {
        return baseIcrvRepository.findAll();
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
                loadAudit(user,"Cargue Exitoso Plantilla Base ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla Base ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<BaseIcrv> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    int conteo=0;
                    String cellCuenta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellEmpresa = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellNaturaleza = formatter.formatCellValue(row.getCell(conteo++)).trim().toUpperCase();
                    String cellEvento = formatter.formatCellValue(row.getCell(conteo++)).trim().toUpperCase();
                    String cellFechaAdquisicion = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellConcepto = formatter.formatCellValue(row.getCell(conteo++)).trim().toUpperCase();
                    String cellNoAsignado = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellCodicons = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellEpigrafe = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellDescripcionPlano = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellCta = formatter.formatCellValue(row.getCell(conteo++)).trim();
                    String cellDescripcionCta = formatter.formatCellValue(row.getCell(conteo++)).trim();

                    if (cellCuenta.length() < 9) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Cuenta debe tener 9 o más caracteres.";
                        lista.add(log);
                    }
                    if (cellEmpresa.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Empresa no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellFechaAdquisicion.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Fecha de Adquisición no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellNaturaleza.length()!=0 &&!cellNaturaleza.equals("D")&&!cellNaturaleza.equals("H")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Naturaleza debe ser D o H.";
                        lista.add(log);
                    }
                    if (!cellEvento.equals("")&&!cellEvento.equals("DIV")&&!cellEvento.equals("INV")&&!cellEvento.equals("IMP")&&!cellEvento.equals("PAT")&&!cellEvento.equals("PYG")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Evento debe ser DIV, INV, IMP, PAT, PYG o Vacio.";
                        lista.add(log);
                    }
                    if (!cellConcepto.equals("")&&!cellConcepto.equals("VALORACIÓN")&&!cellConcepto.equals("NOMINAL")&&!cellConcepto.equals("UTILIDAD/PERDIDA")&&!cellConcepto.equals("ORI")&&!cellConcepto.equals("ATENEA VALORIZACIÓN")
                            &&!cellConcepto.equals("UTILIDAD")&&!cellConcepto.equals("PÉRDIDA")&&!cellConcepto.equals("DIVIDENDOS")&&!cellConcepto.equals("DIVIDENDOS EFECTIVO")&&!cellConcepto.equals("DIVIDENDOS ACCIONES")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Evento debe ser VALORACIÓN, NOMINAL, UTILIDAD/PERDIDA, ORI, ATENEA VALORIZACIÓN, UTILIDAD, PÉRDIDA, DIVIDENDOS, DIVIDENDOS EFECTIVO, DIVIDENDOS ACCIONES o Vacio.";
                        lista.add(log);
                    }

                    if(lista.size() == 0) {
                        BaseIcrv data = new BaseIcrv();
                        data.setCuenta(cellCuenta);
                        data.setEmpresa(cellEmpresa);
                        data.setNaturaleza(cellNaturaleza);
                        data.setEvento(cellEvento);
                        data.setFechaAdquisicion(cellFechaAdquisicion);
                        data.setConcepto(cellConcepto);
                        data.setNoAsignado(cellNoAsignado);
                        data.setCodicons(cellCodicons);
                        data.setEpigrafe(cellEpigrafe);
                        data.setDescripcionPlano(cellDescripcionPlano);
                        data.setCta(cellCta);
                        data.setDescripcionCta(cellDescripcionCta);
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
            baseIcrvRepository.deleteAll();
            baseIcrvRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public BaseIcrv modifyBase(BaseIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro Base");
        return baseIcrvRepository.save(toModify);
    }

    public BaseIcrv saveBase(BaseIcrv toSave, User user){
        loadAudit(user,"Adición Exitosa Registro Base");
        return baseIcrvRepository.save(toSave);
    }

    public void removeBase(Long id, User user){
        loadAudit(user,"Eliminación Exitosa Registro Base");
        baseIcrvRepository.deleteByIdBase(id);
    }

    public void clearBase(User user){
        loadAudit(user,"Limpieza de Tabla Exitosa Base");
        baseIcrvRepository.deleteAll();
    }

    public Page<BaseIcrv> getAll(Pageable pageable){
        return baseIcrvRepository.findAll(pageable);
    }

    public List<BaseIcrv> findByFilter(String value, String filter) {
        List<BaseIcrv> list=new ArrayList<BaseIcrv>();
        switch (filter)
        {
            case "Cuenta":
                list=baseIcrvRepository.findByCuentaContainingIgnoreCase(value);
                break;
            case "Empresa":
                list=baseIcrvRepository.findByEmpresaContainingIgnoreCase(value);
                break;
            case "Naturaleza":
                list=baseIcrvRepository.findByNaturalezaContainingIgnoreCase(value);
                break;
            case "Evento":
                list=baseIcrvRepository.findByEventoContainingIgnoreCase(value);
                break;
            case "Concepto":
                list=baseIcrvRepository.findByConceptoContainingIgnoreCase(value);
                break;
            case "No Asignado":
                list=baseIcrvRepository.findByNoAsignadoContainingIgnoreCase(value);
                break;
            case "Codicons":
                list=baseIcrvRepository.findByCodiconsContainingIgnoreCase(value);
                break;
            case "Epigrafe":
                list=baseIcrvRepository.findByEpigrafeContainingIgnoreCase(value);
                break;
            case "Descripción Plano":
                list=baseIcrvRepository.findByDescripcionPlanoContainingIgnoreCase(value);
                break;
            case "Cta":
                list=baseIcrvRepository.findByCtaContainingIgnoreCase(value);
                break;
            case "Descripción Cta":
                list=baseIcrvRepository.findByDescripcionCtaContainingIgnoreCase(value);
                break;
            default:
                break;
        }
        return list;
    }
}
