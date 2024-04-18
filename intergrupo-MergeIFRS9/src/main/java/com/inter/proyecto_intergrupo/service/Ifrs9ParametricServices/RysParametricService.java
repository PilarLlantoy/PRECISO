package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RysParametric;
import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.RysParametric;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RysParametricRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.RysParametricRepository;
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

@Service
@Transactional
public class RysParametricService {

    @Autowired
    private RysParametricRepository rysParametricRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public RysParametricService(RysParametricRepository rysParametricRepository, AuditRepository auditRepository) {
        this.rysParametricRepository = rysParametricRepository;
        this.auditRepository = auditRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream  file,User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows,user);
            String[] temporal= list.get(0);
            if(temporal[2].equals("0"))
            {
                getRows(rows1,user);
                auditCode("Inserción archivo Parametrica RYS",user);
            }else{
                auditCode("Falla inserción archivo Parametrica RYS",user);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, User user) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int fail=0;
        int success =0;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                String[] log=new String[4];
                log[2]="true";

                DataFormatter formatter = new DataFormatter();
                String cellCodigo = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodigoNombre = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCuenta = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCuentaPyg = formatter.formatCellValue(row.getCell(3)).trim();
                String cellCuentaNeocon = formatter.formatCellValue(row.getCell(4)).trim();
                String cellCuentaNeoconPyg = formatter.formatCellValue(row.getCell(5)).trim();

                log[0]=String.valueOf(row.getRowNum()+1);

                if(cellCodigo.length()==0 || cellCodigo.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Código no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCodigoNombre.length()==0 || cellCodigoNombre.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Código Nombre no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCuenta.length()==0 || cellCuenta.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(2)+" - (3)";
                    log[2]="false";
                    log[3]="Falló Cuenta no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCuentaPyg.length()==0 || cellCuentaPyg.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(3)+" - (4)";
                    log[2]="false";
                    log[3]="Falló Cuenta Pyg no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCuentaNeocon.length()==0 || cellCuentaNeocon.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(4)+" - (5)";
                    log[2]="false";
                    log[3]="Falló Cuenta Neocon no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(cellCuentaNeoconPyg.length()==0 || cellCuentaNeoconPyg.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(5)+" - (6)";
                    log[2]="false";
                    log[3]="Falló Cuenta Neocon Pyg no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="CUENTA";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Parametrica RYS",user);
        }
        return lista;
    }

    public void getRows(Iterator<Row> rows,User user) {
        XSSFRow row;
        int firstRow=1;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter();
                String cellCodigo = formatter.formatCellValue(row.getCell(0)).trim();
                String cellCodigoNombre = formatter.formatCellValue(row.getCell(1)).trim();
                String cellCuenta = formatter.formatCellValue(row.getCell(2)).trim();
                String cellCuentaPyg = formatter.formatCellValue(row.getCell(3)).trim();
                String cellCuentaNeocon = formatter.formatCellValue(row.getCell(4)).trim();
                String cellCuentaNeoconPyg = formatter.formatCellValue(row.getCell(5)).trim();

                RysParametric rysParametric = new RysParametric();
                rysParametric.setCodigo(cellCodigo);
                rysParametric.setCodigoNombre(cellCodigoNombre);
                rysParametric.setCuenta(cellCuenta);
                rysParametric.setCuentaPyg(cellCuentaPyg);
                rysParametric.setCuentaNeocon(cellCuentaNeocon);
                rysParametric.setCuentaNeoconPyg(cellCuentaNeoconPyg);
                rysParametricRepository.save(rysParametric);
            }
        }
        auditCode("Carga masiva apartado Parametrica RYS realizada exitosamente",user);
    }

    public List<RysParametric> findAll(){
        return rysParametricRepository.findAll();
    }

    public boolean insertRysParametric(RysParametric toInsert){

        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_rys_parametrica WHERE CUENTA = ?");
        verify.setParameter(1,toInsert.getCuenta());

        if(verify.getResultList().isEmpty()){
            try {
                rysParametricRepository.save(toInsert);
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return state;
    }

    public void auditCode (String info,User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Parametrica RYS");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public RysParametric findByIdRys(Long inicial){
        return rysParametricRepository.findByIdRys(inicial);
    }

    public RysParametric modifyRysParametric(RysParametric toModify,Long id, User user){
        if(toModify.getIdRys()!=id)
            rysParametricRepository.deleteById(id);
        auditCode("Modificacion registro tabla Parametrica RYS",user);
        return rysParametricRepository.save(toModify);
    }

    public RysParametric saveRysParametric(RysParametric accountControl){
        return rysParametricRepository.save(accountControl);
    }

    public void removeRysParametric(Long id, User user){
        rysParametricRepository.deleteById(id);
        auditCode("Eliminar registro tabla Parametrica RYS",user);
    }

    public void clearRysParametric(User user){
        rysParametricRepository.deleteAll();
        auditCode("Limpiar tabla Parametrica RYS",user);
    }

    public Page<RysParametric> getAll(Pageable pageable){
        return rysParametricRepository.findAll(pageable);
    }

    public List<RysParametric> findByFilter(String value, String filter) {
        List<RysParametric> list=new ArrayList<RysParametric>();
        switch (filter)
        {
            case "Cuenta":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_rys_parametrica as em " +
                        "WHERE em.cuenta LIKE ?", RysParametric.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Código":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_rys_parametrica as em " +
                        "WHERE em.codigo LIKE ?", RysParametric.class);
                query5.setParameter(1, value );

                list= query5.getResultList();

                break;
            case "Código Nombre":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_rys_parametrica as em " +
                        "WHERE em.codigo_nombre LIKE ?", RysParametric.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Cuenta PYG":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_rys_parametrica as em " +
                        "WHERE em.cuenta_pyg LIKE ?", RysParametric.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Cuenta Neocon":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_rys_parametrica as em " +
                        "WHERE em.cuenta_neocon LIKE ?", RysParametric.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Cuenta Neocon PYG":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_rys_parametrica as em " +
                        "WHERE em.cuenta_neocon_pyg LIKE ?", RysParametric.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
