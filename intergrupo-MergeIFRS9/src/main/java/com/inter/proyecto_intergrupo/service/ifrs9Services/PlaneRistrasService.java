package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.PlaneRistras;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.PlaneRistrasRepository;
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
public class PlaneRistrasService {

    @Autowired
    private PlaneRistrasRepository planeRistrasRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public PlaneRistrasService(PlaneRistrasRepository planeRistrasRepository, AuditRepository auditRepository) {
        this.planeRistrasRepository = planeRistrasRepository;
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
                auditCode("Inserción archivo Plano Ristras",user);
            }else{
                auditCode("Falla inserción archivo Plano Ristras",user);
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
                String cellCuentaDefinitiva = formatter.formatCellValue(row.getCell(2)).trim();

                log[0]=String.valueOf(row.getRowNum()+1);

                if(cellCuentaDefinitiva.length()==0)
                {
                    log[1]=CellReference.convertNumToColString(0)+" - (1)";
                    log[2]="false";
                    log[3]="Falló Cuenta Definitiva no puede ir vacía";
                    fail++;
                    lista.add(log);
                }
                else if(log[2].equals("true")){
                    success++;
                }
            }
        }
        String[] logFinal=new String[4];
        logFinal[0]="PLANO";
        logFinal[1]=String.valueOf(success);
        logFinal[2]=String.valueOf(fail);
        logFinal[3]="true";
        lista.add(logFinal);

        if(fail>0)
        {
            auditCode("Fallá carga masiva apartado Plano Ristras",user);
        }
        return lista;
    }

    public void getRows(Iterator<Row> rows,User user) {
        XSSFRow row;
        int firstRow=1;
        Query clear = entityManager.createNativeQuery("DELETE FROM nexco_plano_ristras");
        clear.executeUpdate();

        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellBanco = formatter.formatCellValue(row.getCell(0)).trim();
                String cellInterfaz= formatter.formatCellValue(row.getCell(1)).trim();
                String cellCuentaDefinitiva = formatter.formatCellValue(row.getCell(2)).trim();
                String cellProducto = formatter.formatCellValue(row.getCell(3)).trim();
                String cellTipoCartera = formatter.formatCellValue(row.getCell(4)).trim();
                String cellCampo12= formatter.formatCellValue(row.getCell(5)).trim();
                String cellCalificación= formatter.formatCellValue(row.getCell(6)).trim();
                String cellCampo14= formatter.formatCellValue(row.getCell(7)).trim();
                String cellCodigoSector= formatter.formatCellValue(row.getCell(8)).trim();
                String cellCodigoSubSector= formatter.formatCellValue(row.getCell(9)).trim();
                String cellFormaPago= formatter.formatCellValue(row.getCell(10)).trim();
                String cellLineaCredito= formatter.formatCellValue(row.getCell(11)).trim();
                String cellEntidRedescuento= formatter.formatCellValue(row.getCell(12)).trim();
                String cellMorosidad= formatter.formatCellValue(row.getCell(13)).trim();
                String cellTipoInversion= formatter.formatCellValue(row.getCell(14)).trim();
                String cellTipoGasto= formatter.formatCellValue(row.getCell(15)).trim();
                String cellConceptoContable= formatter.formatCellValue(row.getCell(16)).trim();
                String cellDivisa= formatter.formatCellValue(row.getCell(17)).trim();
                String cellTipoMoneda= formatter.formatCellValue(row.getCell(18)).trim();
                String cellFiller= formatter.formatCellValue(row.getCell(19)).trim();
                String cellVarios= formatter.formatCellValue(row.getCell(20)).trim();
                String cellValor= formatter.formatCellValue(row.getCell(21)).trim();
                String cellSagrupas= formatter.formatCellValue(row.getCell(22)).trim();

                PlaneRistras accountControl = new PlaneRistras();
                accountControl.setBanco(cellBanco);
                accountControl.setInterfaz(cellInterfaz);
                accountControl.setCuentaDefinitiva(cellCuentaDefinitiva);
                accountControl.setProducto(cellProducto);
                accountControl.setTipoDeCartera(cellTipoCartera);
                accountControl.setCampo12(cellCampo12);
                accountControl.setCalificacion(cellCalificación);
                accountControl.setCampo14(cellCampo14);
                accountControl.setCodigoSector(cellCodigoSector);
                accountControl.setCodigoSubsector(cellCodigoSubSector);
                accountControl.setFormaDePago(cellFormaPago);
                accountControl.setLineaDeCredito(cellLineaCredito);
                accountControl.setEntidRedescuento(cellEntidRedescuento);
                accountControl.setMorosidad(cellMorosidad);
                accountControl.setTipoInversion(cellTipoInversion);
                accountControl.setTipoDeGasto(cellTipoGasto);
                accountControl.setConceptoContable(cellConceptoContable);
                accountControl.setDivisa(cellDivisa);
                accountControl.setTipoMoneda(cellTipoMoneda);
                accountControl.setFiller(cellFiller);
                accountControl.setVarios(cellVarios);
                accountControl.setValor(cellValor);
                accountControl.setSagrupas(cellSagrupas);
                planeRistrasRepository.save(accountControl);

            }
        }
        auditCode("Carga masiva apartado Plano Ristras realizada exitosamente",user);
    }

    public List<PlaneRistras> findAll(){
        return planeRistrasRepository.findAll();
    }

    public boolean insertPlaneRistras(PlaneRistras toInsert){

        boolean state = false;

        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ristras WHERE id_plano = ?");
        verify.setParameter(1,toInsert.getIdPlano());

        if(verify.getResultList().isEmpty()){
            try {
                planeRistrasRepository.save(toInsert);
                state = true;
            } catch (Exception e){
                e.printStackTrace();
            }

        }
        return state;
    }

    public boolean planeRistrasLoad(User user){

        Query clear = entityManager.createNativeQuery("DELETE FROM nexco_plano_ristras");
        clear.executeUpdate();

        Query load = entityManager.createNativeQuery("INSERT INTO nexco_plano_ristras(banco,interfaz,cuenta_definitiva,producto,tipo_de_cartera,campo12,calificacion,campo14,codigo_sector,codigo_subsector,\n" +
                "forma_de_pago,linea_de_credito,entid_redescuento,morosidad,tipo_inversion,tipo_de_gasto,varios,valor,sagrupas,concepto_contable,divisa,tipo_moneda)\n" +
                "SELECT '0064','P00',A.numero_cuenta,'--','----','---','-','---','-','--','-----','----','--','-','-','---',A.numero_cuenta,'999999999999999','NNNNNNNNN','P0RP','COP',A.mon \n" +
                "FROM nexco_creacion_cuentas AS A");
        load.executeUpdate();

        auditCode("Cargue información Plano ristras",user);
        Query verify = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ristras");
        if(verify.getResultList().isEmpty()){
            return false;
        }else{
            return true;
        }
    }

    public void auditCode (String info,User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setCentro(user.getCentro());
        insert.setComponente("Paramétricas IFRS9");
        insert.setFecha(today);
        insert.setInput("Plano Ristras");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public PlaneRistras findByIdPlano(Long id){
        return planeRistrasRepository.findByIdPlano(id);
    }

    public PlaneRistras modifyPlaneRistras(PlaneRistras toModify,Long id, User user){
        if(toModify.getIdPlano()!=id)
            planeRistrasRepository.deleteById(id);
        auditCode("Modificacion registro tabla Plano Ristras",user);
        return planeRistrasRepository.save(toModify);
    }

    public PlaneRistras savePlaneRistras(PlaneRistras accountControl){
        return planeRistrasRepository.save(accountControl);
    }

    public void removePlaneRistras(Long id, User user){
        planeRistrasRepository.deleteById(id);
        auditCode("Eliminar registro tabla Plano Ristras",user);
    }

    public void clearPlaneRistras(User user){
        planeRistrasRepository.deleteAll();
        auditCode("Limpiar tabla Plano Ristras",user);
    }

    public Page<PlaneRistras> getAll(Pageable pageable){
        return planeRistrasRepository.findAll(pageable);
    }

    public List<PlaneRistras> findByFilter(String value, String filter) {
        List<PlaneRistras> list=new ArrayList<PlaneRistras>();
        switch (filter)
        {
            case "Banco":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.banco LIKE ?", PlaneRistras.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Interfaz":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.interfaz LIKE ?", PlaneRistras.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Cuenta Definitiva":
                Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.cuenta_definitiva LIKE ?", PlaneRistras.class);
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Producto":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.producto LIKE ?", PlaneRistras.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Tipo De Cartera":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.tipo_de_cartera LIKE ?", PlaneRistras.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Campo12":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.campo12 LIKE ?", PlaneRistras.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Calificacion":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.calificacion LIKE ?", PlaneRistras.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Campo14":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.campo14 LIKE ?", PlaneRistras.class);
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
            case "Código Sector":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.codigo_sector LIKE ?", PlaneRistras.class);
                query7.setParameter(1, value);

                list= query7.getResultList();
                break;
            case "Código Subsector":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.codigo_subsector LIKE ?", PlaneRistras.class);
                query8.setParameter(1, value);

                list= query8.getResultList();
                break;
            case "Forma De Pago":
                Query query9 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.forma_de_pago LIKE ?", PlaneRistras.class);
                query9.setParameter(1, value);

                list= query9.getResultList();
                break;
            case "Línea De Crédito":
                Query query10 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.linea_de_credito LIKE ?", PlaneRistras.class);
                query10.setParameter(1, value);

                list= query10.getResultList();
                break;
            case "Entid Redescuento":
                Query query11 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.entid_redescuento LIKE ?", PlaneRistras.class);
                query11.setParameter(1, value);

                list= query11.getResultList();
                break;
            case "Morosidad":
                Query query12 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.morosidad LIKE ?", PlaneRistras.class);
                query12.setParameter(1, value);

                list= query12.getResultList();
                break;
            case "Tipo Inversión":
                Query query13 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.tipo_inversion LIKE ?", PlaneRistras.class);
                query13.setParameter(1, value);

                list= query13.getResultList();
                break;
            case "Tipo De Gasto":
                Query query14 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.tipo_de_gasto LIKE ?", PlaneRistras.class);
                query14.setParameter(1, value);

                list= query14.getResultList();
                break;
            case "Concepto Contable":
                Query query15 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.concepto_contable LIKE ?", PlaneRistras.class);
                query15.setParameter(1, value);

                list= query15.getResultList();
                break;
            case "Divisa":
                Query query16 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.divisa LIKE ?", PlaneRistras.class);
                query16.setParameter(1, value);

                list= query16.getResultList();
                break;
            case "Tipo Moneda":
                Query query17 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.tipo_moneda LIKE ?", PlaneRistras.class);
                query17.setParameter(1, value);

                list= query17.getResultList();
                break;
            case "Filler":
                Query query18 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.filler LIKE ?", PlaneRistras.class);
                query18.setParameter(1, value);

                list= query18.getResultList();
                break;
            case "Varios":
                Query query19 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.varios LIKE ?", PlaneRistras.class);
                query19.setParameter(1, value);

                list= query19.getResultList();
                break;
            case "Valor":
                Query query20 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.valor LIKE ?", PlaneRistras.class);
                query20.setParameter(1, value);

                list= query20.getResultList();
                break;
            case "Sagrupas":
                Query query21 = entityManager.createNativeQuery("SELECT em.* FROM nexco_plano_ristras as em " +
                        "WHERE em.sagrupas LIKE ?", PlaneRistras.class);
                query21.setParameter(1, value);

                list= query21.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
