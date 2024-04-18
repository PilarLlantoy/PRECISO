package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.ThirdRepository;
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

@Service
@Transactional
public class ThirdService {

    @Autowired
    private ThirdRepository thirdRepository;

    @Autowired
    private YntpSocietyRepository yntpRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ThirdService(ThirdRepository thirdRepository, YntpSocietyRepository yntpRepository) {
        this.thirdRepository = thirdRepository;
        this.yntpRepository = yntpRepository;
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
                insert.setAccion("Se carga tabla Historico de Terceros");
                insert.setCentro(user.getCentro());
                insert.setComponente("Historico de Terceros");
                insert.setFecha(today);
                insert.setInput("Historico de Terceros");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
            else
            {
                Audit insert = new Audit();
                Date today = new Date();
                insert.setAccion("Se carga tabla Historico de Terceros");
                insert.setCentro(user.getCentro());
                insert.setComponente("Historico de Terceros");
                insert.setFecha(today);
                insert.setInput("Historico de Terceros");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows) {
        ArrayList lista= new ArrayList();
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
                String cellNit = formatter.formatCellValue(row.getCell(0));
                String cellContraparte = formatter.formatCellValue(row.getCell(1));
                String cellCodigoCliente = formatter.formatCellValue(row.getCell(2));
                String cellTipo = formatter.formatCellValue(row.getCell(3));
                String cellDv = formatter.formatCellValue(row.getCell(4));
                String cellYntp = formatter.formatCellValue(row.getCell(5));
                String cellMarca = formatter.formatCellValue(row.getCell(6));
                if((cellNit.isEmpty() || cellNit.isBlank()) && (cellContraparte.isEmpty() || cellContraparte.isBlank()) && (cellCodigoCliente.isEmpty() || cellCodigoCliente.isBlank()) &&
                        (cellTipo.isEmpty() || cellTipo.isBlank()) && (cellDv.isEmpty() || cellDv.isBlank()) && (cellYntp.isEmpty() || cellYntp.isBlank()) &&
                        (cellMarca.isEmpty() || cellMarca.isBlank()))
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }
                else if(cellNit.isEmpty() || cellNit.isBlank() ||cellNit.length()>14)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if(cellContraparte.isEmpty() || cellContraparte.isBlank() ||cellContraparte.length()>254)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else if(cellCodigoCliente.isEmpty() || cellCodigoCliente.isBlank() ||cellCodigoCliente.length()>14)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="3";
                    log[2]="false";
                    break;
                }
                else if(cellTipo.isEmpty() || cellTipo.isBlank() ||cellTipo.length()>2)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="4";
                    log[2]="false";
                    break;
                }
                else if(cellDv.isEmpty() || cellDv.isBlank() ||cellDv.length()>1)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="5";
                    log[2]="false";
                    break;
                }
                else if(cellYntp.isEmpty() || cellYntp.isBlank() ||cellYntp.length()!=5)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="6";
                    log[2]="false";
                    break;
                }
                else if(cellMarca.isEmpty() || cellMarca.isBlank() ||cellMarca.length()>5)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="7";
                    log[2]="false";
                    break;
                }
                else
                {
                    try
                    {
                        log[0]=String.valueOf(row.getRowNum());log[1]="3";
                        int tipo = Integer.parseInt(cellTipo);log[1]="5";
                        int dv = Integer.parseInt(cellDv);log[1]="6";
                        int yntp = Integer.parseInt(cellYntp);log[1]="7";
                        int marca = Integer.parseInt(cellMarca);log[2]="true";
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
                String cellNit = formatter.formatCellValue(row.getCell(0));
                String cellContraparte = formatter.formatCellValue(row.getCell(1));
                String cellCodigoCliente = formatter.formatCellValue(row.getCell(2));
                String cellTipo = formatter.formatCellValue(row.getCell(3));
                String cellDv = formatter.formatCellValue(row.getCell(4));
                String cellYntp = formatter.formatCellValue(row.getCell(5));
                String cellMarca = formatter.formatCellValue(row.getCell(6));

                if((cellNit.isEmpty() || cellNit.isBlank()) && (cellContraparte.isEmpty() || cellContraparte.isBlank()) && (cellCodigoCliente.isEmpty() || cellCodigoCliente.isBlank()) &&
                        (cellTipo.isEmpty() || cellTipo.isBlank()) && (cellDv.isEmpty() || cellDv.isBlank()) && (cellYntp.isEmpty() || cellYntp.isBlank()) &&
                        (cellMarca.isEmpty() || cellMarca.isBlank()))
                {
                    break;
                }
                else if(thirdRepository.findByNit(cellNit)==null)
                {

                    Query query0 = entityManager.createNativeQuery("SELECT em.yntp FROM nexco_sociedades_yntp as em " +
                            "WHERE em.yntp = ?");
                    query0.setParameter(1, cellYntp);

                    log[0] = cellNit;
                    if(!query0.getResultList().isEmpty()) {
                        Third third = new Third();
                        third.setNit(cellNit);
                        third.setContraparte(cellContraparte);
                        third.setCodigoCliente(cellCodigoCliente);
                        third.setTipo(Integer.parseInt(cellTipo));
                        third.setDv(Integer.parseInt(cellDv));
                        third.setFecha(today);
                        third.setYntp(cellYntp);
                        third.setMarcaTipoInstitucion(Integer.parseInt(cellMarca));
                        thirdRepository.save(third);
                        log[1] = "Registro insertado exitosamente.";
                    }
                    else {
                        log[1] = "Yntp no existe en tabla de Sociedades";
                    }
                    lista.add(log);
                }
                else if(thirdRepository.findByNit(cellNit)!=null)
                {

                    Query query0 = entityManager.createNativeQuery("SELECT em.yntp FROM nexco_sociedades_yntp as em " +
                            "WHERE em.yntp = ?");
                    query0.setParameter(1, cellYntp);

                    log[0] = cellNit;
                    if(!query0.getResultList().isEmpty()) {
                        Third third = thirdRepository.findByNit(cellNit);
                        third.setNit(cellNit);
                        third.setContraparte(cellContraparte);
                        third.setCodigoCliente(cellCodigoCliente);
                        third.setTipo(Integer.parseInt(cellTipo));
                        third.setDv(Integer.parseInt(cellDv));
                        third.setFecha(today);
                        third.setYntp(cellYntp);
                        third.setMarcaTipoInstitucion(Integer.parseInt(cellMarca));
                        thirdRepository.save(third);
                        log[1] = "Registro actualizado exitosamente.";
                    }
                    else {
                        log[1] = "Yntp no existe en tabla de Sociedades";
                    }
                    lista.add(log);
                }
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Third> findAll(){
        return thirdRepository.findAll();
    }

    public Third findThirdByNit(String id){
        return thirdRepository.findByNit(id);
    }

    public Third modifyThird(Third toModify,String id,String yntp, User user){
        Date date=new Date();
        Third toInsert = new Third();

        toInsert.setNit(toModify.getNit());
        toInsert.setCodigoCliente(toModify.getCodigoCliente());
        toInsert.setContraparte(toModify.getContraparte());
        toInsert.setDv(toModify.getDv());
        toInsert.setMarcaTipoInstitucion(toModify.getMarcaTipoInstitucion());
        toInsert.setTipo(toModify.getTipo());
        toInsert.setYntp(yntp);
        toInsert.setFecha(date);
        if(!toModify.getNit().equals(id))
            thirdRepository.deleteById(id);

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se modifica un registro Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return thirdRepository.save(toInsert);
    }

    public Third saveThird(Third third,String yntp, User user){
        third.setCodigoCliente(third.getCodigoCliente());
        third.setFecha(new Date());
        third.setContraparte(third.getContraparte());
        third.setDv(third.getDv());
        third.setMarcaTipoInstitucion(third.getMarcaTipoInstitucion());
        third.setNit(third.getNit());
        third.setTipo(third.getTipo());
        third.setYntp(yntp);

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se agrega un registro Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return thirdRepository.save(third);
    }

    public void removeThird(String id, User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se elimina un registro Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        thirdRepository.deleteById(id);
    }

    public void clearThird(User user){

        Audit insert = new Audit();
        Date today = new Date();
        insert.setAccion("Se eliminan los registros Historico de Terceros");
        insert.setCentro(user.getCentro());
        insert.setComponente("Historico de Terceros");
        insert.setFecha(today);
        insert.setInput("Historico de Terceros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        thirdRepository.deleteAll();
    }

    public Page<Third> getAll(Pageable pageable){
        return thirdRepository.findAll(pageable);
    }

    public List<Third> findByFilter(String value, String filter) {
        List<Third> list=new ArrayList<Third>();
        switch (filter)
        {
            case "NIT":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.nit_contraparte LIKE ?", Third.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Contraparte":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.contraparte LIKE ?", Third.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Código Cliente":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.codigo_cliente LIKE ?", Third.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Tipo":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.tipo LIKE ?", Third.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "DV":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.dv LIKE ?", Third.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "YNTP":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.yntp LIKE ?", Third.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Marca Tipo de Institución":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_terceros as em " +
                        "WHERE em.marca_tipo_institucion LIKE ?", Third.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
