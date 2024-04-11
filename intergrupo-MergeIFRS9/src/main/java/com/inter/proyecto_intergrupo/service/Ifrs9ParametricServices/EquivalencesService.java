package com.inter.proyecto_intergrupo.service.Ifrs9ParametricServices;

import com.inter.proyecto_intergrupo.model.Ifrs9Parametrics.Equivalences;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.EquivalencesRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.EquivalencesRepository;
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
public class EquivalencesService {

    @Autowired
    private EquivalencesRepository equivalencesRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public EquivalencesService(EquivalencesRepository equivalencesRepository, AuditRepository auditRepository) {
        this.equivalencesRepository = equivalencesRepository;
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
                auditCode("Inserción archivo Equivalencias",user);
            }else{
                auditCode("Falla inserción archivo Equivalencias",user);
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
                String cellCuenta = formatter.formatCellValue(row.getCell(0)).trim();
                String cellContrapartida = formatter.formatCellValue(row.getCell(1)).trim();

                log[0]=String.valueOf(row.getRowNum()+1);

                if(cellCuenta.length()!=4)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Cuenta Contabñe debe tener 4 carcateres";
                    fail++;
                    lista.add(log);
                }
                else if(cellContrapartida.length()==0 || cellContrapartida.length()>255)
                {
                    log[1]=CellReference.convertNumToColString(1)+" - (2)";
                    log[2]="false";
                    log[3]="Falló Contrapartida no puede ir vacío";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="EQUI";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Equivalencias",user);
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
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellCuenta = formatter.formatCellValue(row.getCell(0)).trim();
                String cellContrapartida = formatter.formatCellValue(row.getCell(1)).trim();

                Equivalences equivalences = new Equivalences();
                equivalences.setCuentaContable(cellCuenta);
                equivalences.setContrapartida(cellContrapartida);
                equivalencesRepository.save(equivalences);

            }
        }
        auditCode("Carga masiva apartado Equivalencias realizada exitosamente",user);
    }

    public List<Equivalences> findAll(){
        return equivalencesRepository.findAll();
    }

    public boolean insertEquivalences(Equivalences toInsert){

        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_equivalencias_ifrs WHERE cuenta_contable = ?");
        verify.setParameter(1,toInsert.getCuentaContable());

        if(verify.getResultList().isEmpty()){
            try {
                equivalencesRepository.save(toInsert);
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
        insert.setInput("Equivalencias");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Equivalences findByCuentaContable(String inicial){
        return equivalencesRepository.findByCuentaContable(inicial);
    }

    public Equivalences modifyEquivalences(Equivalences toModify,String id, User user){
        if(toModify.getCuentaContable()!=id)
            equivalencesRepository.deleteById(id);
        auditCode("Modificacion registro tabla Equivalencias",user);
        return equivalencesRepository.save(toModify);
    }

    public Equivalences saveEquivalences(Equivalences rejectionIdP2){
        return equivalencesRepository.save(rejectionIdP2);
    }

    public void removeEquivalences(String id, User user){
        equivalencesRepository.deleteById(id);
        auditCode("Eliminar registro tabla Equivalencias",user);
    }

    public void clearEquivalences(User user){
        equivalencesRepository.deleteAll();
        auditCode("Limpiar tabla Equivalencias",user);
    }

    public Page<Equivalences> getAll(Pageable pageable){
        return equivalencesRepository.findAll(pageable);
    }

    public List<Equivalences> findByFilter(String value, String filter) {
        List<Equivalences> list=new ArrayList<Equivalences>();
        switch (filter)
        {
            case "Cuenta Contable":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_equivalencias_ifrs as em " +
                        "WHERE em.cuenta_contable LIKE ?", Equivalences.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Contrapartida":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_equivalencias_ifrs as em " +
                        "WHERE em.contrapartida LIKE ?", Equivalences.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
