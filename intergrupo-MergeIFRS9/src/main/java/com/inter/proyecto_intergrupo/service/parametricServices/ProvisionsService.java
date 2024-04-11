package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Provisions;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ProvisionsRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ProvisionsService {

    @Autowired
    private ProvisionsRepository provisionsRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public ProvisionsService(ProvisionsRepository provisionsRepository) {
        this.provisionsRepository = provisionsRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
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
                insert.setAccion("Inserción archivo Provisiones");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Provisiones");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else {
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Provisiones");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Provisiones");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);


            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList<String[]> lista= new ArrayList<String[]>();
        XSSFRow row;
        int firstRow=1;
        String[] log=new String[3];
        log[0]="0";
        log[1]="0";
        log[2]="false";
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellInstrumento = formatter.formatCellValue(row.getCell(0)).trim();
                String cellJerarquia = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDescripcion = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCuentaNeocon = formatter.formatCellValue(row.getCell(3)).trim();
                String cellMinimo = formatter.formatCellValue(row.getCell(4)).trim();
                String cellIfrs9 = formatter.formatCellValue(row.getCell(5)).trim();
                String cellStage = formatter.formatCellValue(row.getCell(6)).trim();
                String cellProducto = formatter.formatCellValue(row.getCell(7)).trim();
                String cellSector = formatter.formatCellValue(row.getCell(8)).trim();
                String cellSigno = formatter.formatCellValue(row.getCell(9)).trim();
                ArrayList<String> listValidation =new ArrayList<String>(Arrays.asList("PR","CV"));
                log[0]=String.valueOf(row.getRowNum());
                if((cellCuentaNeocon.isEmpty() || cellCuentaNeocon.isBlank()) && (cellInstrumento.isEmpty() || cellInstrumento.isBlank()) && (cellJerarquia.isEmpty() || cellJerarquia.isBlank()) &&
                        (cellDescripcion.isEmpty() || cellDescripcion.isBlank()) && (cellMinimo.isEmpty() || cellMinimo.isBlank()) && (cellIfrs9.isEmpty() || cellIfrs9.isBlank()) && (cellStage.isEmpty() || cellStage.isBlank())
                        && (cellProducto.isEmpty() || cellProducto.isBlank()) && (cellSector.isEmpty() || cellSector.isBlank()) && (cellSigno.isEmpty() || cellSigno.isBlank()))
                {
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }
                else if(cellCuentaNeocon.length()==0 || cellCuentaNeocon.length()<5 ||cellCuentaNeocon.length()>18)
                {
                    log[1]="4";
                    log[2]="false";
                    break;
                }
                else if(cellInstrumento.length()==0 ||cellInstrumento.length()>254)
                {
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellJerarquia.length()==0 || cellJerarquia.length()>254)
                {
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else if(cellDescripcion.length()==0 || cellDescripcion.length()>254)
                {
                    log[1]="3";
                    log[2]="false";
                    break;
                }
                else if(cellMinimo.length()==0 || cellMinimo.length()!=1)
                {
                    log[1]="5";
                    log[2]="false";
                    break;
                }
                else if(cellIfrs9.length()==0 || (!listValidation.contains(cellIfrs9.toUpperCase())))
                {
                    log[1]="6";
                    log[2]="false";
                    break;
                }
                else if(cellStage.length()==0 || cellStage.length()>255)
                {
                    log[1]="7";
                    log[2]="false";
                    break;
                }
                else if(cellProducto.length()==0 || cellProducto.length()>255)
                {
                    log[1]="8";
                    log[2]="false";
                    break;
                }
                else if(cellSector.length()==0 || cellSector.length()>255)
                {
                    log[1]="9";
                    log[2]="false";
                    break;
                }
                else if(cellSigno.length()==0 || cellSigno.length()!=1)
                {
                    log[1]="10";
                    log[2]="false";
                    break;
                }
                else
                {
                    try
                    {
                        log[1]="1"; Long cuentaNeocon = Long.parseLong(cellCuentaNeocon);
                        log[2]="true";
                    }
                    catch(Exception e){
                        log[2]="falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            }
            else
            {
                firstRow=0;
            }
        }
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
                String cellInstrumento = formatter.formatCellValue(row.getCell(0)).trim();
                String cellJerarquia = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDescripcion = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCuentaNeocon = formatter.formatCellValue(row.getCell(3)).trim();
                String cellMinimo = formatter.formatCellValue(row.getCell(4)).trim();
                String cellIfrs9 = formatter.formatCellValue(row.getCell(5)).trim();
                String cellStage = formatter.formatCellValue(row.getCell(6)).trim();
                String cellProducto = formatter.formatCellValue(row.getCell(7)).trim();
                String cellSector = formatter.formatCellValue(row.getCell(8)).trim();
                String cellSigno = formatter.formatCellValue(row.getCell(9)).trim();
                log[0] = cellCuentaNeocon;
                if((cellCuentaNeocon.isEmpty() || cellCuentaNeocon.isBlank()) && (cellInstrumento.isEmpty() || cellInstrumento.isBlank()) && (cellJerarquia.isEmpty() || cellJerarquia.isBlank()) &&
                        (cellDescripcion.isEmpty() || cellDescripcion.isBlank()) && (cellMinimo.isEmpty() || cellMinimo.isBlank()) && (cellIfrs9.isEmpty() || cellIfrs9.isBlank()) && (cellStage.isEmpty() || cellStage.isBlank())
                        && (cellProducto.isEmpty() || cellProducto.isBlank()) && (cellSector.isEmpty() || cellSector.isBlank()) && (cellSigno.isEmpty() || cellSigno.isBlank()))
                {
                    break;
                }
                else if(provisionsRepository.findByCuentaNeocon(cellCuentaNeocon)==null)
                {
                    Provisions provisions = new Provisions();
                    provisions.setCuentaNeocon(cellCuentaNeocon);
                    provisions.setInstrumento(cellInstrumento.toUpperCase());
                    provisions.setJerarquia(cellJerarquia);
                    provisions.setMinimo(cellMinimo.toUpperCase());
                    provisions.setDescripcion(cellDescripcion);
                    provisions.setIfrs9(cellIfrs9.toUpperCase());
                    provisions.setStageEspana(cellStage);
                    provisions.setProductoEspana(cellProducto.toUpperCase());
                    provisions.setSector(cellSector.toUpperCase());
                    provisions.setSigno(cellSigno);
                    provisionsRepository.save(provisions);
                    log[1] = "Provisión insertada exitosamente.";
                    lista.add(log);
                }
                else if(provisionsRepository.findByCuentaNeocon(cellCuentaNeocon)!=null)
                {
                    Provisions provisions = provisionsRepository.findByCuentaNeocon(cellCuentaNeocon);
                    provisions.setInstrumento(cellInstrumento.toUpperCase());
                    provisions.setJerarquia(cellJerarquia);
                    provisions.setMinimo(cellMinimo.toUpperCase());
                    provisions.setDescripcion(cellDescripcion);
                    provisions.setIfrs9(cellIfrs9.toUpperCase());
                    provisions.setStageEspana(cellStage);
                    provisions.setProductoEspana(cellProducto.toUpperCase());
                    provisions.setSector(cellSector.toUpperCase());
                    provisions.setSigno(cellSigno);
                    provisionsRepository.save(provisions);
                    log[1] = "Provisión actualizada exitosamente.";
                    lista.add(log);
                }
                else{
                    log[1]="Fallo al ingresar registro, Fallo sistema al insertar";
                    lista.add(log);
                }
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Provisions> findAll(){
        return provisionsRepository.findAll();
    }

    public Provisions findProvisionsByCuentaNeocon(String id){
        return provisionsRepository.findByCuentaNeocon(id);
    }

    public Provisions modifyProvisions(Provisions toModify,String id, User user){
        Provisions toInsert = new Provisions();
        toInsert.setCuentaNeocon(toModify.getCuentaNeocon());
        toInsert.setInstrumento(toModify.getInstrumento());
        toInsert.setMinimo(toModify.getMinimo().toUpperCase());
        toInsert.setDescripcion(toModify.getDescripcion());
        toInsert.setJerarquia(toModify.getJerarquia());
        toInsert.setIfrs9(toModify.getIfrs9());
        toInsert.setStageEspana(toModify.getStageEspana());
        toInsert.setProductoEspana(toModify.getProductoEspana());
        toInsert.setSector(toModify.getSector());
        toInsert.setSigno(toModify.getSigno());
        if(toModify.getCuentaNeocon()!=id)
            provisionsRepository.deleteById(id);

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro tabla de Provisiones");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Provisiones");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return provisionsRepository.save(toInsert);
    }

    public Provisions saveProvisions(Provisions provisions, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Ingresar registro tabla de Provisiones");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Provisiones");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return provisionsRepository.save(provisions);
    }

    public void removeProvisions(String id, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla de Provisiones");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Provisiones");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        provisionsRepository.deleteById(id);
    }

    public void clearProvisions(User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla de Provisiones");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Provisiones");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        provisionsRepository.deleteAll();
    }

    public Page<Provisions> getAll(Pageable pageable){
        return provisionsRepository.findAll(pageable);
    }

    public List<Provisions> findByFilter(String value, String filter) {
        List<Provisions> list=new ArrayList<Provisions>();
        switch (filter)
        {
            case "Cuenta Neocon":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.cuenta_neocon LIKE ?", Provisions.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Instrumento":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.instrumento LIKE ?", Provisions.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Jerarquía":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.jerarquia LIKE ?", Provisions.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Descripción":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.descripcion LIKE ?", Provisions.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Mínimo":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.minimo LIKE ?", Provisions.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Perímetro IFRS9":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.ifrs9 LIKE ?", Provisions.class);
                query4.setParameter(1, value.toUpperCase());

                list= query4.getResultList();
                break;
            case "Stage España":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.stage_espana LIKE ?", Provisions.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Producto España":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.producto_espana LIKE ?", Provisions.class);
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
            case "Sector":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.sector LIKE ?", Provisions.class);
                query7.setParameter(1, value);

                list= query7.getResultList();
                break;
            case "Signo":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_provisiones as em " +
                        "WHERE em.signo LIKE ?", Provisions.class);
                query8.setParameter(1, value);

                list= query8.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

}
