package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMayoresEEFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.tablaUnificadaEliminacionesPatrimoniales;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ParametricEEFFAjustesMayoresRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class ParametricAjustesMayoresEEFFService {

    @Autowired
    private ParametricEEFFAjustesMayoresRepository parametricEEFFAjustesMayoresRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ParametricAjustesMayoresEEFFService(ParametricEEFFAjustesMayoresRepository parametricEEFFAjustesMayoresRepository) {
        this.parametricEEFFAjustesMayoresRepository = parametricEEFFAjustesMayoresRepository;
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Cuentas por Cobrar");
        insert.setFecha(today);
        insert.setInput("Cuentas");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParametricAjustesMayoresEEFF findByIdTipoParametro(Long id){
        return parametricEEFFAjustesMayoresRepository.findByIdTipoParametro(id);
    }

    public List<ParametricAjustesMayoresEEFF> findByPeriodo(String periodo)
    {
        return parametricEEFFAjustesMayoresRepository.findByPeriodo(periodo);
    }



    public ArrayList<String[]> saveFileBDAjustesMayores(InputStream  file, String periodo, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantillaAjustes(rows, periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue exitoso Parametria Ajustes Minimos");
            else
                loadAudit(user,"Cargue Fallido Ajustes Minimos");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaAjustes(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ParametricAjustesMayoresEEFF> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();

                    String cellL4 = formatter.formatCellValue(row.getCell(0));
                    String cellL9 = formatter.formatCellValue(row.getCell(1));
                    String cellEntidad = formatter.formatCellValue(row.getCell(2));
                    String cellCuenta = formatter.formatCellValue(row.getCell(3));
                    String cellMoneda = formatter.formatCellValue(row.getCell(4));

                    XSSFCell cell1= row.getCell(5);
                    cell1.setCellType(CellType.STRING);
                    String cellDebe = formatter.formatCellValue(cell1).replace(" ", "");

                    XSSFCell cell2= row.getCell(6);
                    cell2.setCellType(CellType.STRING);
                    String cellHaber = formatter.formatCellValue(cell2).replace(" ", "");

                    XSSFCell cell3= row.getCell(7);
                    cell3.setCellType(CellType.STRING);
                    String cellSaldo = formatter.formatCellValue(cell3).replace(" ", "");

                    String cellConcepto = formatter.formatCellValue(row.getCell(8));


                    if (cellL4.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo L4 no puede estar vacio.";
                    }
                    if (cellL9.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo L9 no puede estar vacio.";
                    }
                    if (cellEntidad.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Entidad no puede estar vacio.";
                    }
                    if (cellCuenta.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Cuenta no puede estar vacio.";
                    }
                    if (cellMoneda.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Moneda no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellDebe.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Debe no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try {
                            Double.parseDouble(cellDebe);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(5);
                            log[2] = "El campo Debe debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellHaber.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Haber no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try {
                            Double.parseDouble(cellHaber);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(6);
                            log[2] = "El campo Haber debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellSaldo.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Saldo no puede estar vacio.";
                        lista.add(log);
                    }
                    else {
                        try {
                            Double.parseDouble(cellSaldo);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(7);
                            log[2] = "El campo Saldo debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellConcepto.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(8);
                        log[2] = "El campo Concepto no puede estar vacio.";
                        lista.add(log);
                    }

                    ParametricAjustesMayoresEEFF parametriceeff = new ParametricAjustesMayoresEEFF();
                    parametriceeff.setL_4(cellL4);
                    parametriceeff.setL_9(cellL9);
                    parametriceeff.setCuenta(cellCuenta);
                    parametriceeff.setEntidad(cellEntidad);
                    parametriceeff.setMoneda(cellMoneda);
                    try {
                        parametriceeff.setDebe(Double.parseDouble(cellDebe));
                        parametriceeff.setHaber(Double.parseDouble(cellHaber));
                        parametriceeff.setSaldo(Double.parseDouble(cellSaldo));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    parametriceeff.setConcepto(cellConcepto);
                    parametriceeff.setPeriodo(periodo);
                    toInsert.add(parametriceeff);
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
            parametricEEFFAjustesMayoresRepository.deleteAll();
            parametricEEFFAjustesMayoresRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ParametricAjustesMayoresEEFF modifyAccount1(ParametricAjustesMayoresEEFF toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro Cuentas");
        return parametricEEFFAjustesMayoresRepository.save(toModify);
    }

    public ParametricAjustesMayoresEEFF saveAccount1(ParametricAjustesMayoresEEFF toSave, User user){
        loadAudit(user,"Adición Exitosa registro Cuentas");
        return parametricEEFFAjustesMayoresRepository.save(toSave);
    }

    public void removeAccount1(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro Cuentas");
        parametricEEFFAjustesMayoresRepository.deleteByIdTipoParametro(id);
    }

    public void clearAccount1(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Cuentas");
        parametricEEFFAjustesMayoresRepository.deleteAll();
    }

    public Page<ParametricAjustesMayoresEEFF> getAll1(Pageable pageable, String period){
        List<ParametricAjustesMayoresEEFF> list = parametricEEFFAjustesMayoresRepository.findByPeriodo(period);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ParametricAjustesMayoresEEFF> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public void ProcesarAjustesMayores(String periodo) {

        Query update = entityManager.createNativeQuery("UPDATE nexco_concil_filiales set debe_ajustes_mayores = NULL, haber_ajustes_mayores = NULL where periodo = :periodo ; ");
        update.setParameter("periodo", periodo);
        update.executeUpdate();

        Query insert4 = entityManager.createNativeQuery("insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons)\n" +
                "select distinct SUBSTRING(a.cuenta,1,1) as l_1, SUBSTRING(a.cuenta,1,2) as l_2, SUBSTRING(a.cuenta,1,4) as l_4,SUBSTRING(a.cuenta,1,6) as l_6, SUBSTRING(a.cuenta,1,9) as l_9, \n" +
                "a.cuenta,a.entidad,0 as banco,0 as fiduciaria,0 as valores,a.moneda as moneda,coalesce(b.nombre_cuenta,c.nombre_cuenta,d.derecta,'') as nombre,:periodo ,0 as total,coalesce(b.cod_cons,c.cod_cons,d.codicons46,'') as codicons\n" +
                "from (select cuenta,entidad,moneda,sum(saldo) as saldo from nexco_parametria_ajustes_mayores_eeff where periodo = :periodo  and cuenta not in (select distinct cuenta from nexco_concil_filiales where periodo =:periodo ) group by cuenta,entidad,moneda) a\n" +
                "inner join (select distinct cuenta,moneda from nexco_concil_filiales where periodo =:periodo ) e on a.cuenta!=e.cuenta and a.moneda != e.moneda\n" +
                "left join (select * from nexco_puc_fiduciaria_filiales) b on a.cuenta = b.cuenta \n" +
                "left join (select * from nexco_puc_valores_filiales) c on a.cuenta = c.id_cuenta \n" +
                "left join (select * from cuentas_puc where empresa = '0013') d on a.cuenta = d.nucta ");
        insert4.setParameter("periodo", periodo);
        insert4.executeUpdate();

        Query update1 = entityManager.createNativeQuery("UPDATE a set a.debe_ajustes_mayores = case when b.saldo > 0 then b.saldo else 0  end, a.haber_ajustes_mayores = case when b.saldo < 0 then b.saldo ELSE 0 end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo1 ) a, (select cuenta, moneda,periodo,sum(saldo) as saldo from nexco_parametria_ajustes_mayores_eeff where periodo = :periodo1 group by cuenta, moneda,periodo ) b\n" +
                "where a.cuenta= b.cuenta AND a.moneda = b.moneda and a.periodo = b.periodo;");
        update1.setParameter("periodo1", periodo);
        update1.executeUpdate();


        Query insert = entityManager.createNativeQuery("UPDATE nexco_concil_filiales set debe_ajustes_mayores = 0, haber_ajustes_mayores = 0 where periodo = :periodo and debe_ajustes_mayores IS NULL AND haber_ajustes_mayores IS NULL; ");
        insert.setParameter("periodo", periodo);
        insert.executeUpdate();

        Query update2 = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        update2.setParameter("periodo", periodo);
        update2.executeUpdate();


        Query insert1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales where periodo = :periodo) a");
        insert1.setParameter("periodo", periodo);
        insert1.executeUpdate();

    }
}