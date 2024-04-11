package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.*;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.*;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
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

@Service
@Transactional
public class EliminacionesAjustadaService {
    @Autowired
    private EliminacionesVersionAjustadaRepository eliminacionesVersionAjustadaRepository;

    @Autowired
    private EliminacionesVersionCuadreGeneralAjustadaRepository eliminacionesVersionCuadreGeneralAjustadaRepository;
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private statusInfoRepository statusInfoRepository;

    @Autowired
    private AuditRepository auditRepository;

    public EliminacionesAjustadaService(EliminacionesVersionAjustadaRepository eliminacionesVersionAjustadaRepository) {
        this.eliminacionesVersionAjustadaRepository = eliminacionesVersionAjustadaRepository;
    }

    public List<EliminacionesVersionAjustadaRepository> getEeffConsolidatedDataByPeriod(String periodo) {
        return eliminacionesVersionAjustadaRepository.findByPeriodo(periodo);
    }

    public List<EliminacionesVersionAjustada> getCuadreEliminacionDetalle(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_eliminaciones_version_ajustada_detalle  where periodo = ?;", EliminacionesVersionAjustada.class);
        consulta.setParameter(1, periodo);
        return consulta.getResultList();
    }
    public Page getAllEliminacionDetalle(Pageable pageable, String periodo, List<EliminacionesVersionAjustada> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<EliminacionesVersionAjustada> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    /********************************Separador********************/

    public List<EliminacionesDetalleVersionAjustada> getCuadreEliminacionDetalleCuadreGeneral(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_eliminaciones_version_ajustada_detalle_cuadre_general where periodo = ?;", EliminacionesDetalleVersionAjustada.class);
        consulta.setParameter(1, periodo);
        return consulta.getResultList();
    }
    public Page getAllEliminacionDetalleCuadreGeneral(Pageable pageable, String periodo, List<EliminacionesDetalleVersionAjustada> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<EliminacionesDetalleVersionAjustada> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        XSSFRow row;
        String stateFinal = "SUCCESS";

        ArrayList<EliminacionesVersionAjustada> toInsert = new ArrayList<>();
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {

                DataFormatter formatter = new DataFormatter();
                String cellid = formatter.formatCellValue(row.getCell(0));
                String cellnombre = formatter.formatCellValue(row.getCell(1));
                String cellconcepto = formatter.formatCellValue(row.getCell(2));
                String cellcuentaLocal = formatter.formatCellValue(row.getCell(3));
                String cellvalor = formatter.formatCellValue(row.getCell(4));
                String cellYntp = formatter.formatCellValue(row.getCell(5));
                String celll = formatter.formatCellValue(row.getCell(6));
                String cellabs = formatter.formatCellValue(row.getCell(7));
                String cellnat = formatter.formatCellValue(row.getCell(8));


                if (cellid.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(0);
                    log1[2] = "El Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (cellnombre.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(1);
                    log1[2] = "El nombre Cuenta no puede estar vacio";
                    lista.add(log1);
                }

                if (cellconcepto.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(2);
                    log1[2] = "El nombre Concepto no puede estar vacio";
                    lista.add(log1);
                }

                if (cellcuentaLocal.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(3);
                    log1[2] = "El nombre Naturaleza no puede estar vacio";
                    lista.add(log1);
                }

                try {
                    XSSFCell cell1 = row.getCell(4);
                    cell1.setCellType(CellType.STRING);
                    cellvalor = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellvalor);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                if (cellYntp.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(5);
                    log1[2] = "El nombre Naturaleza no puede estar vacio";
                    lista.add(log1);
                }

                if (celll.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(6);
                    log1[2] = "El nombre Naturaleza no puede estar vacio";
                    lista.add(log1);
                }

                try {
                    XSSFCell cell1 = row.getCell(7);
                    cell1.setCellType(CellType.STRING);
                    cellabs = formatter.formatCellValue(cell1).replace(" ", "");
                    Double.parseDouble(cellabs);
                } catch (Exception e) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "Tipo dato incorrecto, debe ser un númerico decimal";
                    lista.add(log);
                }

                if (cellnat.trim().length() == 0) {
                    String[] log1 = new String[3];
                    log1[0] = String.valueOf(row.getRowNum() + 1);
                    log1[1] = CellReference.convertNumToColString(8);
                    log1[2] = "El campo Nivel no debe estar vacio";
                    lista.add(log1);
                }

                EliminacionesVersionAjustada Eliminaciones = new EliminacionesVersionAjustada();

                Eliminaciones.setId(cellid);
                Eliminaciones.setNombre(cellnombre);
                Eliminaciones.setConcepto(cellconcepto);
                Eliminaciones.setCuentaLocal(cellcuentaLocal);
                Eliminaciones.setYntp(cellYntp);
                Eliminaciones.setL(celll);
                Eliminaciones.setNat(cellnat);
                Eliminaciones.setPeriodo(periodo);

                try {
                    Eliminaciones.setValor(Double.parseDouble(cellvalor));
                    Eliminaciones.setAbs(Math.abs(Double.parseDouble(cellvalor)));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (cellconcepto.trim().length()!=0 && !cellconcepto.trim().substring(0,1).equals("9")) {
                    toInsert.add(Eliminaciones);
                }
            }
        }

        if (lista.size() != 0) {
            stateFinal = "FAILED";

        }
        else {
            eliminacionesVersionAjustadaRepository.deleteByPeriodo(periodo);
            eliminacionesVersionAjustadaRepository.saveAll(toInsert);

            eliminacionesVersionCuadreGeneralAjustadaRepository.deleteByPeriodo(periodo);
            procesarCuadreGeneralAjuste(periodo);
            EliminarByPeriod(periodo);

        }
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 13) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);

        toInsert.clear();
        return lista;
    }
    public void procesarCuadreGeneralAjuste (String periodo){
        Query consulta = entityManager.createNativeQuery("insert into nexco_eliminaciones_version_ajustada_detalle_cuadre_general ( nombre,concepto,periodo,plantilla_banco,plantilla_filial,ajuste,total_general)\n" +
                "SELECT nombre,concepto,periodo, SUM(CASE WHEN id = 'PLANTILLA BANCO'  then isnull(valor,0) else 0 end) as plantilla_banco,  \n" +
                "SUM(CASE WHEN id = 'PLANTILLA FILIAL'  then isnull(valor,0) else 0 end) as plantilla_filial, \n" +
                "SUM(CASE WHEN id = 'AJUSTE'  then isnull(valor,0) else 0 end) as ajuste,  \n" +
                "SUM(CASE WHEN id = 'PLANTILLA BANCO'  then isnull(valor,0) else 0 end + CASE WHEN id = 'PLANTILLA FILIAL' then isnull(valor,0) else 0 end + CASE WHEN id = 'AJUSTE'  then isnull(valor,0) else 0 end) AS total_general\n" +
                "FROM nexco_eliminaciones_version_ajustada_detalle where periodo = ? \n" +
                "GROUP BY  nombre,concepto,periodo");
        consulta.setParameter(1,periodo);
        consulta.executeUpdate();

        Query UpdatesEliminaciones = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones.setParameter("periodo", periodo);
        UpdatesEliminaciones.executeUpdate();

        Query UpdatesEliminaciones1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones1.setParameter("periodo", periodo);
        UpdatesEliminaciones1.executeUpdate();

    }

    public void EliminarByPeriod (String periodo){
        Query consulta2 = entityManager.createNativeQuery("delete  from nexco_status_info where periodo = ?");
        consulta2.setParameter(1,periodo);
        consulta2.executeUpdate();
    }
    public ArrayList<String[]>   saveFileBD(InputStream file, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list = new ArrayList<String[]>();
        if (file != null) {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list = validarPlantilla(rows, periodo);
        }
        return list;
    }

    public void loadAudit(User user, String mensaje) {
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("EEFF Consolidado");
        insert.setFecha(today);
        insert.setInput("Filiales");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

}





