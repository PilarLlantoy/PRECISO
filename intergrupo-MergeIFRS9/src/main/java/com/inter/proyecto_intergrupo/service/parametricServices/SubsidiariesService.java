package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Subsidiaries;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CurrencyRepository;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
@Transactional
public class SubsidiariesService {

    @Autowired
    private YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private AuditRepository auditRepository;

    public SubsidiariesService(YntpSocietyRepository yntpSocietyRepository, CurrencyRepository currencyRepository) {
        this.yntpSocietyRepository = yntpSocietyRepository;
        this.currencyRepository = currencyRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Histórico Cruce Filiales");
                insert.setCentro(user.getCentro());
                insert.setComponente("Histórico Cruce Filiales");
                insert.setFecha(today);
                insert.setInput("Histórico Cruce Filiales");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else {
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Histórico Cruce Filiales");
                insert.setCentro(user.getCentro());
                insert.setComponente("Histórico Cruce Filiales");
                insert.setFecha(today);
                insert.setInput("Histórico Cruce Filiales");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {

        ArrayList<String[]> lista= new ArrayList();
        XSSFRow row;
        int firstRow=1;
        String stateFinal = "true";

        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellYntpEmpresa = formatter.formatCellValue(row.getCell(0));
                String cellCuentaFilial = formatter.formatCellValue(row.getCell(2));
                String cellYntpLocal = "00548";
                String cellCuentaLocal= formatter.formatCellValue(row.getCell(1));
                String cellContratoBanco = formatter.formatCellValue(row.getCell(3));
                String cellContratoFilial = formatter.formatCellValue(row.getCell(4));
                String cellConceptos= formatter.formatCellValue(row.getCell(5));

                if(cellYntpEmpresa.isEmpty() || cellYntpEmpresa.isBlank() || cellYntpEmpresa.length()>5 || cellYntpEmpresa.length() < 1) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El formato del YNTP no es válido";
                    lista.add(log1);
                }
                if(cellCuentaLocal.isEmpty() || cellCuentaLocal.isBlank() || cellCuentaLocal.length() < 1) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El formato de la Cuenta Local no es válido";
                    lista.add(log1);
                }
                if(cellCuentaFilial.isEmpty() || cellCuentaFilial.isBlank() || cellCuentaFilial.length() < 1){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El formato de la Cuenta Filial no es válido";
                    lista.add(log1);
                }
                if(cellConceptos.isEmpty() || cellConceptos.isBlank() ||cellConceptos.length()>255 || cellConceptos.length() < 1) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El Concepto es obligatorio";
                    lista.add(log1);
                }
                try
                {
                    int yntp = Integer.parseInt(cellYntpEmpresa);
                    int yntpLocal = Integer.parseInt(cellYntpLocal);
                }
                catch(Exception e){
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El formato del YNTP no es válido";
                    lista.add(log1);
                }

            }
            else
            {
                firstRow=0;
            }
        }

        String[] log = new String[3];
        log[2] = stateFinal;
        lista.add(log);

        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        ArrayList lista= new ArrayList();
        int firstRow=1;
        while (rows.hasNext())
        {
            String[] log=new String[3];
            log[2]="true";
            row = (XSSFRow) rows.next();

            if(firstRow!=1 && row.getCell(0)!=null)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale

                String cellYntpEmpresa = formatter.formatCellValue(row.getCell(0));
                String cellCuentaFilial = formatter.formatCellValue(row.getCell(2));
                String cellYntpLocal = "00548";
                String cellCuentaLocal= formatter.formatCellValue(row.getCell(1));
                String cellContratoBanco = formatter.formatCellValue(row.getCell(3));
                String cellContratoFilial = formatter.formatCellValue(row.getCell(4));
                String cellConceptos= formatter.formatCellValue(row.getCell(5));
                log[0] = String.format("%5s", cellYntpEmpresa).replace(' ', '0')+" - "+cellCuentaFilial;

                if((cellYntpEmpresa.isEmpty() || cellYntpEmpresa.isBlank()) &&
                        (cellYntpLocal.isEmpty() || cellYntpLocal.isBlank()))
                {
                    break;
                }
                else if(yntpSocietyRepository.findByYntp(cellYntpEmpresa)!=null)
                {
                    Subsidiaries subsidiaries = new Subsidiaries();
                    subsidiaries.setYntpEmpresa(String.format("%5s", cellYntpEmpresa).replace(' ', '0'));
                    subsidiaries.setCuentaFilial(cellCuentaFilial);
                    subsidiaries.setCuentaLocal(cellCuentaLocal);
                    subsidiaries.setYtnpLocal(cellYntpLocal);
                    subsidiaries.setDivisa("");
                    subsidiaries.setContratoBanco(cellContratoBanco);
                    subsidiaries.setContratoFilial(cellContratoFilial);
                    subsidiaries.setObservacionReportante("");
                    subsidiaries.setConceptos(cellConceptos);

                    Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em \n" +
                            "WHERE em.yntp_empresa = ? AND em.cuenta_filial = ? and em.cuenta_local = ? and em.yntp_local = ? and em.contrato_banco = ? \n" +
                            "and em.conceptos = ? and em.contrato_filial = ?", Subsidiaries.class);
                    query1.setParameter(1, String.format("%5s", subsidiaries.getYntpEmpresa()).replace(' ', '0'));
                    query1.setParameter(2, subsidiaries.getCuentaFilial());
                    query1.setParameter(3, subsidiaries.getCuentaLocal());
                    query1.setParameter(4, subsidiaries.getYtnpLocal());
                    query1.setParameter(5, subsidiaries.getContratoBanco());
                    query1.setParameter(6, subsidiaries.getConceptos());
                    query1.setParameter(7, subsidiaries.getContratoFilial());

                    if(query1.getResultList().size()==0) {
                        Query query = entityManager.createNativeQuery("INSERT INTO preciso_filiales (yntp_empresa,cuenta_filial,cuenta_local,yntp_local,divisa,contrato_banco,observacion_reportante,conceptos, contrato_filial) VALUES (?,?,?,?,?,?,?,?,?)", Subsidiaries.class);
                        query.setParameter(1, String.format("%5s", subsidiaries.getYntpEmpresa()).replace(' ', '0'));
                        query.setParameter(2, subsidiaries.getCuentaFilial());
                        query.setParameter(3, subsidiaries.getCuentaLocal());
                        query.setParameter(4, subsidiaries.getYtnpLocal());
                        query.setParameter(5, subsidiaries.getDivisa());
                        query.setParameter(6, subsidiaries.getContratoBanco());
                        query.setParameter(7, subsidiaries.getObservacionReportante());
                        query.setParameter(8, subsidiaries.getConceptos());
                        query.setParameter(9, subsidiaries.getContratoFilial());
                        query.executeUpdate();
                        log[1] = "Registro insertado exitosamente.";
                    }
                    else
                    {
                        Query query = entityManager.createNativeQuery("UPDATE preciso_filiales SET cuenta_local = ? , yntp_local = ?, divisa = ?, contrato_banco = ?, observacion_reportante = ?, conceptos = ?, contrato_filial = ? " +
                                "WHERE yntp_empresa = ? AND cuenta_filial = ?", Subsidiaries.class);

                        query.setParameter(1, subsidiaries.getCuentaLocal() );
                        query.setParameter(2, subsidiaries.getYtnpLocal() );
                        query.setParameter(3, subsidiaries.getDivisa());
                        query.setParameter(4, subsidiaries.getContratoBanco());
                        query.setParameter(5, subsidiaries.getObservacionReportante());
                        query.setParameter(6, subsidiaries.getConceptos());
                        query.setParameter(7, subsidiaries.getContratoFilial());
                        query.setParameter(8, String.format("%5s", subsidiaries.getYntpEmpresa()).replace(' ', '0'));
                        query.setParameter(9, subsidiaries.getCuentaFilial() );
                        query.executeUpdate();
                        log[1] = "Registro actualizado exitosamente.";
                    }
                }
                else {
                    log[1] = "Yntp no existe en tabla de Sociedades";
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Subsidiaries> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em ", Subsidiaries.class);
        return query.getResultList();
    }

    public List<Subsidiaries> findSubsidiariesByYntpCuenta(String yntp,String cuenta){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em " +
                "WHERE em.yntp_empresa = ? AND em.cuenta_filial = ?", Subsidiaries.class);

        query.setParameter(1, String.format("%5s", yntp).replace(' ', '0'));
        query.setParameter(2, cuenta );
        return query.getResultList();
    }

    public List<Subsidiaries> findSubsidiariesById(String id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em " +
                "WHERE em.id = ? ", Subsidiaries.class);
        query.setParameter(1, id );
        return query.getResultList();
    }

    public void modifySubsidiaries(Subsidiaries toModify, User user){
        /*Subsidiaries toInsert = new Subsidiaries();
        toInsert.setConceptos(toModify.getConceptos());
        toInsert.setYntpEmpresa(String.format("%5s", toModify.getYntpEmpresa()).replace(' ', '0'));
        toInsert.setCuentaFilial(toModify.getCuentaFilial());
        toInsert.setCuentaLocal(toModify.getCuentaLocal());
        toInsert.setYtnpLocal(toModify.getYtnpLocal());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setContratoBanco(toModify.getContratoBanco());
        toInsert.setContratoFilial(toModify.getContratoFilial());
        toInsert.setObservacionReportante(toModify.getObservacionReportante());*/

        Query query = entityManager.createNativeQuery("UPDATE preciso_filiales SET yntp_empresa = ? , cuenta_filial = ? , cuenta_local = ? , yntp_local = ?, divisa = ?, contrato_banco = ?, observacion_reportante = ?, conceptos = ?, contrato_filial= ? " +
                "WHERE id = ? ", Subsidiaries.class);
        query.setParameter(1, String.format("%5s", toModify.getYntpEmpresa()).replace(' ', '0'));
        query.setParameter(2, toModify.getCuentaFilial() );
        query.setParameter(3, toModify.getCuentaLocal() );
        query.setParameter(4, toModify.getYtnpLocal() );
        query.setParameter(5, toModify.getDivisa());
        query.setParameter(6, toModify.getContratoBanco());
        query.setParameter(7, toModify.getObservacionReportante());
        query.setParameter(8, toModify.getConceptos());
        query.setParameter(9, toModify.getContratoFilial());
        query.setParameter(10, toModify.getId() );
        try {
            /*int yntp = Integer.parseInt(toInsert.getYntpEmpresa());
            Long cuentaFilial = Long.parseLong(toInsert.getCuentaFilial());
            int yntpLocal = Integer.parseInt(toInsert.getYtnpLocal());
            Long cuentaLocal = Long.parseLong(toInsert.getCuentaLocal());*/
            //if (currencyRepository.findAllById(toModify.getDivisa()) != null)
            query.executeUpdate();

            Date today = new Date();
            Audit insert = new Audit();
            insert.setAccion("Modificado registro Histórico Cruce Filiales");
            insert.setCentro(user.getCentro());
            insert.setComponente("Histórico Cruce Filiales");
            insert.setFecha(today);
            insert.setInput("Histórico Cruce Filiales");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void saveSubsidiaries(Subsidiaries subsidiaries, User user){
        Query query = entityManager.createNativeQuery("INSERT INTO preciso_filiales (yntp_empresa,cuenta_filial,cuenta_local,yntp_local,divisa,contrato_banco,observacion_reportante,conceptos,contrato_filial) VALUES (?,?,?,?,?,?,?,?,?)", Subsidiaries.class);
        query.setParameter(1, String.format("%5s", subsidiaries.getYntpEmpresa()).replace(' ', '0'));
        query.setParameter(2, subsidiaries.getCuentaFilial() );
        query.setParameter(3, subsidiaries.getCuentaLocal() );
        query.setParameter(4, subsidiaries.getYtnpLocal() );
        query.setParameter(5, subsidiaries.getDivisa());
        query.setParameter(6, subsidiaries.getContratoBanco());
        query.setParameter(7, subsidiaries.getObservacionReportante());
        query.setParameter(8, subsidiaries.getConceptos());
        query.setParameter(9, subsidiaries.getContratoFilial());
        query.executeUpdate();
    }

    public void removeSubsidiaries(String id,String cuenta, User user){
        Query query = entityManager.createNativeQuery("DELETE FROM preciso_filiales WHERE yntp_empresa = ? AND cuenta_filial = ?", Subsidiaries.class);
        query.setParameter(1, String.format("%5s", id).replace(' ', '0'));
        query.setParameter(2, cuenta);

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminados registros  Histórico Cruce Filiales");
        insert.setCentro(user.getCentro());
        insert.setComponente("Histórico Cruce Filiales");
        insert.setFecha(today);
        insert.setInput("Histórico Cruce Filiales");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        query.executeUpdate();
    }

    public void clearSubsidiaries(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM preciso_filiales", Subsidiaries.class);

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminado archivo Histórico Cruce Filiales");
        insert.setCentro(user.getCentro());
        insert.setComponente("Histórico Cruce Filiales");
        insert.setFecha(today);
        insert.setInput("Histórico Cruce Filiales");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        query.executeUpdate();
    }

    public Page<Subsidiaries> getAll(Pageable pageable){
        List<Subsidiaries> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<Subsidiaries> pageSubsidiaries = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageSubsidiaries;
    }

    public List<Subsidiaries> findByFilter(String value, String filter) {
        List<Subsidiaries> list=new ArrayList<Subsidiaries>();
        switch (filter)
        {
            case "Yntp Empresa Reportante":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em " +
                        "WHERE em.yntp_empresa LIKE ?", Subsidiaries.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Cuenta Local":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em " +
                        "WHERE em.cuenta_local LIKE ?", Subsidiaries.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Cuenta Reportante":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em " +
                        "WHERE em.cuenta_filial LIKE ?", Subsidiaries.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Yntp Local":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM preciso_filiales as em " +
                        "WHERE em.yntp_local LIKE ?", Subsidiaries.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}
