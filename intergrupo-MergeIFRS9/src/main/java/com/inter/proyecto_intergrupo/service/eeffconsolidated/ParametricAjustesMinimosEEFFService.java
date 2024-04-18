package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ParametricAjustesMinimosEEFF;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ParametricEEFFAjustesMinimosRepository;
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
public class ParametricAjustesMinimosEEFFService {

    @Autowired
    private ParametricEEFFAjustesMinimosRepository parametricEEFFAjustesMinimosRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ParametricAjustesMinimosEEFFService(ParametricEEFFAjustesMinimosRepository parametricEEFFAjustesMinimosRepository) {
        this.parametricEEFFAjustesMinimosRepository = parametricEEFFAjustesMinimosRepository;
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
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ParametricAjustesMinimosEEFF findByIdTipoParametro(Long id){
        return parametricEEFFAjustesMinimosRepository.findByIdTipoParametro(id);
    }

    public List<ParametricAjustesMinimosEEFF> findAll()
    {
        return parametricEEFFAjustesMinimosRepository.findAll();
    }



    public ArrayList<String[]> saveFileBDAjustesMinimos(InputStream  file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantillaAjustes(rows);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue exitoso Parametria Ajustes Minimos");
            else
                loadAudit(user,"Cargue Fallido Ajustes Minimos");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaAjustes(Iterator<Row> rows) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ParametricAjustesMinimosEEFF> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();

                    String cellCuentaOrigen = formatter.formatCellValue(row.getCell(0));
                    String cellEmpresaOrigen = formatter.formatCellValue(row.getCell(1));
                    String cellMonedaOrigen = formatter.formatCellValue(row.getCell(2));
                    String cellEmpresaDestino= formatter.formatCellValue(row.getCell(3));
                    String cellCuentaDestino = formatter.formatCellValue(row.getCell(4));
                    String cellMonedaDestino = formatter.formatCellValue(row.getCell(5));
                    String cellperiodo = formatter.formatCellValue(row.getCell(6));

                    if (cellCuentaOrigen.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Concepto no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellEmpresaOrigen.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Cuenta no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellMonedaOrigen.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Moneda no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellEmpresaDestino.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Empresa no puede estar vacio.";
                        lista.add(log);
                    }

                    if (cellCuentaDestino.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Cuenta no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellMonedaDestino.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Moneda no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellperiodo.trim().length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Periodo no puede estar vacio.";
                        lista.add(log);
                    }

                    ParametricAjustesMinimosEEFF parametriceeff = new ParametricAjustesMinimosEEFF();
                    parametriceeff.setCuentaOrigen(cellCuentaOrigen);
                    parametriceeff.setEmpresaOrigen(cellEmpresaOrigen);
                    parametriceeff.setMonedaOrigen(cellMonedaOrigen);
                    parametriceeff.setEmpresaDestino(cellEmpresaDestino);
                    parametriceeff.setCuentaDestino(cellCuentaDestino);
                    parametriceeff.setMonedaDestino(cellMonedaDestino);
                    parametriceeff.setPeriodo(cellperiodo);
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
            parametricEEFFAjustesMinimosRepository.deleteAll();
            parametricEEFFAjustesMinimosRepository.saveAll(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public ParametricAjustesMinimosEEFF modifyAccount1(ParametricAjustesMinimosEEFF toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa registro Cuentas");
        return parametricEEFFAjustesMinimosRepository.save(toModify);
    }

    public ParametricAjustesMinimosEEFF saveAccount1(ParametricAjustesMinimosEEFF toSave, User user){
        loadAudit(user,"Adición Exitosa registro Cuentas");
        return parametricEEFFAjustesMinimosRepository.save(toSave);
    }

    public void removeAccount1(Long id, User user){
        loadAudit(user,"Eliminación Exitosa registro Cuentas");
        parametricEEFFAjustesMinimosRepository.deleteByIdTipoParametro(id);
    }

    public void clearAccount1(User user){
        loadAudit(user,"Limpieza de tabla Exitosa Cuentas");
        parametricEEFFAjustesMinimosRepository.deleteAll();
    }

    public Page<ParametricAjustesMinimosEEFF> getAll1(Pageable pageable){
        return parametricEEFFAjustesMinimosRepository.findAll(pageable);
    }



    public List<ParametricAjustesMinimosEEFF> findByFilter(String value, String filter) {
        List<ParametricAjustesMinimosEEFF> list=new ArrayList<ParametricAjustesMinimosEEFF>();
        switch (filter)
        {
            case "cuenta origen":
                list=parametricEEFFAjustesMinimosRepository.findByCuentaOrigenContainingIgnoreCase(value);
                break;
            case "empresa origen":
                list=parametricEEFFAjustesMinimosRepository.findByEmpresaOrigenContainingIgnoreCase(value);
                break;
            case "empresa destino":
                list=parametricEEFFAjustesMinimosRepository.findByEmpresaDestinoContainingIgnoreCase(value);
                break;
            case "cuenta destino":
                list = parametricEEFFAjustesMinimosRepository.findByCuentaDestinoGreaterThanEqual(value);
                break;
            default:
                break;
        }
        return list;
    }


    public void ProcesarAjustesMinimos(String periodo) {

        Query UpdatesEliminaciones2 = entityManager.createNativeQuery("drop table nexco_temporal_ajustes_minimos;\n" +
                "SELECT a.cuenta_origen, a.empresa_origen, a.moneda_origen, a.cuenta_destino, a.empresa_destino, a.moneda_destino, b.total*-1 as total, a.periodo into nexco_temporal_ajustes_minimos\n" +
                "FROM nexco_parametria_ajustes_minimos_eeff a\n" +
                "INNER JOIN (select * from nexco_concil_filiales) b ON a.cuenta_origen = b.cuenta AND a.empresa_origen = b.empresa and a.periodo = b.periodo;");
        UpdatesEliminaciones2.executeUpdate();

        Query UpdatesEliminaciones3 = entityManager.createNativeQuery("insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons)\n" +
                "select distinct SUBSTRING(a.cuenta_destino,1,1) as l_1, SUBSTRING(a.cuenta_destino,1,2) as l_2, SUBSTRING(a.cuenta_destino,1,4) as l_4,SUBSTRING(a.cuenta_destino,1,6) as l_6, SUBSTRING(a.cuenta_destino,1,9) as l_9, \n" +
                "a.cuenta_destino,a.empresa_destino,0 as banco,0 as fiduciaria,0 as valores,a.moneda_destino as moneda,coalesce(b.nombre_cuenta,c.nombre_cuenta,d.derecta) as nombre,a.periodo,0 as total,coalesce(b.cod_cons,c.cod_cons,d.codicons46,'') as codicons\n" +
                "from (SELECT distinct cuenta_destino,empresa_destino,moneda_destino,periodo from nexco_parametria_ajustes_minimos_eeff where cuenta_destino not in (SELECT distinct cuenta FROM nexco_concil_filiales)) a\n" +
                "inner join (SELECT distinct cuenta,moneda,empresa,periodo FROM nexco_concil_filiales) e on a.cuenta_destino != e.cuenta and e.moneda != a.moneda_destino and a.periodo = e.periodo and e.empresa = a.empresa_destino\n" +
                "left join (select * from nexco_puc_fiduciaria_filiales) b on a.cuenta_destino = b.cuenta \n" +
                "left join (select * from nexco_puc_valores_filiales) c on a.cuenta_destino = c.id_cuenta \n" +
                "left join (select * from cuentas_puc where empresa = '0013') d on a.cuenta_destino = d.nucta ");
        UpdatesEliminaciones3.executeUpdate();

        Query insert = entityManager.createNativeQuery("UPDATE nexco_concil_filiales set debe_ajustes_minimos = null, haber_ajustes_minimos = null ; \n"+
                "UPDATE a set a.debe_ajustes_minimos = case when b.total*-1 < 0 then 0 else b.total*-1 end, a.haber_ajustes_minimos = case when b.total*-1 > 0 then 0 else b.total*-1 end  from (select * from nexco_concil_filiales) a, (select cuenta_destino,empresa_destino,moneda_destino,periodo,sum(total)as total from nexco_temporal_ajustes_minimos group by cuenta_destino,empresa_destino,moneda_destino,periodo) b\n" +
                "where a.cuenta= b.cuenta_destino AND a.empresa = b.empresa_destino and a.moneda = b.moneda_destino and a.periodo = b.periodo;");
        insert.executeUpdate();

        Query insert2 = entityManager.createNativeQuery("UPDATE a set a.debe_ajustes_minimos = case when b.total < 0 then 0 else b.total end, a.haber_ajustes_minimos = case when b.total > 0 then 0 else b.total end  \n" +
                "from (select * from nexco_concil_filiales) a, (select cuenta_origen,empresa_origen,moneda_origen,periodo,sum(total)as total from nexco_temporal_ajustes_minimos group by cuenta_origen,empresa_origen,moneda_origen,periodo) b\n" +
                "where a.cuenta= b.cuenta_origen AND a.empresa = b.empresa_origen and a.moneda = b.moneda_origen and a.periodo = b.periodo;");
        insert2.executeUpdate();

        Query UpdatesEliminaciones = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales ) a\n");
        UpdatesEliminaciones.executeUpdate();

        Query UpdatesEliminaciones1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales) a\n");
        UpdatesEliminaciones1.executeUpdate();

    }
}