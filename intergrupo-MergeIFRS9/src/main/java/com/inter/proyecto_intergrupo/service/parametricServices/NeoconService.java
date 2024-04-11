package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Neocon;
import com.inter.proyecto_intergrupo.model.parametric.Third;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.NeoconRepository;
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
import java.util.*;

@Service
@Transactional
public class NeoconService {

    @Autowired
    private NeoconRepository neoconRepository;

    @Autowired
    private AuditRepository auditRepository;

    @PersistenceContext
    EntityManager entityManager;

    public NeoconService(NeoconRepository neoconRepository) {
        this.neoconRepository = neoconRepository;
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
                insert.setAccion("Inserción archivo Cuentas Neocon");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Cuentas Neocon");
                insert.setNombre(user.getNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo inserción archivo Cuentas Neocon");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Cuentas Neocon");
                insert.setNombre(user.getNombre());
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
            if(firstRow!=1 && row.getRowNum()>3)
            {
                boolean responseValidate = true;
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellPlanCuentas = formatter.formatCellValue(row.getCell(0));
                String cellCodigoJerarquico = formatter.formatCellValue(row.getCell(1));
                String cellCuenta = formatter.formatCellValue(row.getCell(2));
                String cellDescripcion = formatter.formatCellValue(row.getCell(3));
                String cellEntrada = formatter.formatCellValue(row.getCell(4));
                String cellMinimo = formatter.formatCellValue(row.getCell(5));
                String cellNaturaleza = formatter.formatCellValue(row.getCell(6));
                String cellIntergurpo = formatter.formatCellValue(row.getCell(7));
                String cellGrScIng = formatter.formatCellValue(row.getCell(8));
                String cellEpigraf = formatter.formatCellValue(row.getCell(9));
                String cellResidencia = formatter.formatCellValue(row.getCell(10));
                String cellBancaria = formatter.formatCellValue(row.getCell(11));
                String cellForm = formatter.formatCellValue(row.getCell(12));
                String cellTdes = formatter.formatCellValue(row.getCell(13));
                String cellSoporteDerivada = formatter.formatCellValue(row.getCell(14));
                String cellUnid = formatter.formatCellValue(row.getCell(15));
                String cellTipoCambio = formatter.formatCellValue(row.getCell(16));
                String cellAgregacion = formatter.formatCellValue(row.getCell(17));
                String cellTipoDivisa = formatter.formatCellValue(row.getCell(18));
                String cellTipoPais= formatter.formatCellValue(row.getCell(19));
                String cellContrap = formatter.formatCellValue(row.getCell(20));
                String cellTimp = formatter.formatCellValue(row.getCell(21));
                String cellConciliacion = formatter.formatCellValue(row.getCell(22));
                log[0]=String.valueOf(row.getRowNum());
                if((cellPlanCuentas.isEmpty() || cellPlanCuentas.isBlank()) && (cellCodigoJerarquico.isEmpty() || cellCodigoJerarquico.isBlank()) && (cellCuenta.isEmpty() || cellCuenta.isBlank()) &&
                        (cellDescripcion.isEmpty() || cellDescripcion.isBlank()) && (cellEntrada.isEmpty() || cellEntrada.isBlank()) && (cellMinimo.isEmpty() || cellMinimo.isBlank()) &&
                        (cellNaturaleza.isEmpty() || cellNaturaleza.isBlank()) && (cellIntergurpo.isEmpty() || cellIntergurpo.isBlank()) && (cellGrScIng.isEmpty() || cellGrScIng.isBlank()) &&
                        (cellEpigraf.isEmpty() || cellEpigraf.isBlank()) && (cellResidencia.isEmpty() || cellResidencia.isBlank()) && (cellBancaria.isEmpty() || cellBancaria.isBlank()) &&
                        (cellForm.isEmpty() || cellForm.isBlank()) && (cellTdes.isEmpty() || cellTdes.isBlank()) && (cellSoporteDerivada.isEmpty() || cellSoporteDerivada.isBlank()) && (cellUnid.isEmpty() || cellUnid.isBlank()) &&
                        (cellTipoCambio.isEmpty() || cellTipoCambio.isBlank()) && (cellAgregacion.isEmpty() || cellAgregacion.isBlank()) && (cellTipoDivisa.isEmpty() || cellTipoDivisa.isBlank()) && (cellTipoPais.isEmpty() || cellTipoPais.isBlank()) &&
                        (cellContrap.isEmpty() || cellContrap.isBlank()) && (cellTimp.isEmpty() || cellTimp.isBlank()) && (cellConciliacion.isEmpty() || cellConciliacion.isBlank()))
                {
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }
                else if(cellCuenta.isEmpty() || cellCuenta.isBlank() ||cellCuenta.length()<4)
                {
                    log[1]="3";
                    log[2]="false";
                    break;
                }
                else if(cellNaturaleza.isEmpty() || cellNaturaleza.isBlank() ||cellNaturaleza.length()!=1)
                {
                    log[1]="7";
                    log[2]="false";
                    break;
                }
                else
                {
                    for (int i=0;i<=22;i++)
                    {
                        String cellTemporal = formatter.formatCellValue(row.getCell(i));
                        if((cellTemporal.isEmpty() || cellTemporal.isBlank() ||cellTemporal.length()>254) && i!=12)
                        {
                            log[1]=String.valueOf(i+1);
                            log[2]="false";
                            responseValidate = false;
                            break;
                        }
                    }
                    if(responseValidate==true) {
                        try {
                            log[1] = "3";
                            Long cuenta = Long.parseLong(cellCuenta);
                            log[1] = "7";
                            int naturaleza = Integer.parseInt(cellNaturaleza);
                            log[2] = "true";
                        } catch (Exception e) {
                            log[2] = "falseFormat";
                            lista.add(log);
                            return lista;
                        }
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

            if(firstRow!=1 && row.getRowNum()>3)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellPlanCuentas = formatter.formatCellValue(row.getCell(0));
                String cellCodigoJerarquico = formatter.formatCellValue(row.getCell(1));
                String cellCuenta = formatter.formatCellValue(row.getCell(2));
                String cellDescripcion = formatter.formatCellValue(row.getCell(3));
                String cellEntrada = formatter.formatCellValue(row.getCell(4));
                String cellMinimo = formatter.formatCellValue(row.getCell(5));
                String cellNaturaleza = formatter.formatCellValue(row.getCell(6));
                String cellIntergurpo = formatter.formatCellValue(row.getCell(7));
                String cellGrScIng = formatter.formatCellValue(row.getCell(8));
                String cellEpigraf = formatter.formatCellValue(row.getCell(9));
                String cellResidencia = formatter.formatCellValue(row.getCell(10));
                String cellBancaria = formatter.formatCellValue(row.getCell(11));
                String cellForm = formatter.formatCellValue(row.getCell(12));
                String cellTdes = formatter.formatCellValue(row.getCell(13));
                String cellSoporteDerivada = formatter.formatCellValue(row.getCell(14));
                String cellUnid = formatter.formatCellValue(row.getCell(15));
                String cellTipoCambio = formatter.formatCellValue(row.getCell(16));
                String cellAgregacion = formatter.formatCellValue(row.getCell(17));
                String cellTipoDivisa = formatter.formatCellValue(row.getCell(18));
                String cellTipoPais= formatter.formatCellValue(row.getCell(19));
                String cellContrap = formatter.formatCellValue(row.getCell(20));
                String cellTimp = formatter.formatCellValue(row.getCell(21));
                String cellConciliacion = formatter.formatCellValue(row.getCell(22));
                log[0] = cellCuenta;

                if((cellPlanCuentas.isEmpty() || cellPlanCuentas.isBlank()) && (cellCodigoJerarquico.isEmpty() || cellCodigoJerarquico.isBlank()) && (cellCuenta.isEmpty() || cellCuenta.isBlank()) &&
                        (cellDescripcion.isEmpty() || cellDescripcion.isBlank()) && (cellEntrada.isEmpty() || cellEntrada.isBlank()) && (cellMinimo.isEmpty() || cellMinimo.isBlank()) &&
                        (cellNaturaleza.isEmpty() || cellNaturaleza.isBlank()) && (cellIntergurpo.isEmpty() || cellIntergurpo.isBlank()) && (cellGrScIng.isEmpty() || cellGrScIng.isBlank()) &&
                        (cellEpigraf.isEmpty() || cellEpigraf.isBlank()) && (cellResidencia.isEmpty() || cellResidencia.isBlank()) && (cellBancaria.isEmpty() || cellBancaria.isBlank()) &&
                        (cellForm.isEmpty() || cellForm.isBlank()) && (cellTdes.isEmpty() || cellTdes.isBlank()) && (cellSoporteDerivada.isEmpty() || cellSoporteDerivada.isBlank()) && (cellUnid.isEmpty() || cellUnid.isBlank()) &&
                        (cellTipoCambio.isEmpty() || cellTipoCambio.isBlank()) && (cellAgregacion.isEmpty() || cellAgregacion.isBlank()) && (cellTipoDivisa.isEmpty() || cellTipoDivisa.isBlank()) && (cellTipoPais.isEmpty() || cellTipoPais.isBlank()) &&
                        (cellContrap.isEmpty() || cellContrap.isBlank()) && (cellTimp.isEmpty() || cellTimp.isBlank()) && (cellConciliacion.isEmpty() || cellConciliacion.isBlank()))
                {
                    break;
                }
                else if(neoconRepository.findByCuenta(Long.parseLong(cellCuenta))==null)
                {
                    Neocon neocon = new Neocon();
                    neocon.setPlanDeCuentas(cellPlanCuentas);
                    neocon.setCodigoJerarquico(cellCodigoJerarquico);
                    neocon.setCuenta(Long.parseLong(cellCuenta));
                    neocon.setDescripcion(cellDescripcion);
                    neocon.setEntrada(cellEntrada);
                    neocon.setMinimo(cellMinimo);
                    neocon.setNaturaleza(Integer.parseInt(cellNaturaleza));
                    neocon.setIntergrupo(cellIntergurpo);
                    neocon.setGrScIng(cellGrScIng);
                    neocon.setEpigraf(cellEpigraf);
                    neocon.setResidencia(cellResidencia);
                    neocon.setBancaria(cellBancaria);
                    neocon.setForm(cellForm);
                    neocon.setTdes(cellTdes);
                    neocon.setSoporteDerivada(cellSoporteDerivada);
                    neocon.setUnid(cellUnid);
                    neocon.setTipoCambio(cellTipoCambio);
                    neocon.setAgregacion(cellAgregacion);
                    neocon.setTipoDivisa(cellTipoDivisa);
                    neocon.setTipoPais(cellTipoPais);
                    neocon.setContrap(cellContrap);
                    neocon.setTimp(cellTimp);
                    neocon.setConciliacion(cellConciliacion);
                    neoconRepository.save(neocon);
                    log[1] = "Registro insertado exitosamente.";
                    lista.add(log);
                }
                else{
                    Neocon neocon = neoconRepository.findByCuenta(Long.parseLong(cellCuenta));
                    neocon.setPlanDeCuentas(cellPlanCuentas);
                    neocon.setCodigoJerarquico(cellCodigoJerarquico);
                    neocon.setDescripcion(cellDescripcion);
                    neocon.setEntrada(cellEntrada);
                    neocon.setMinimo(cellMinimo);
                    neocon.setNaturaleza(Integer.parseInt(cellNaturaleza));
                    neocon.setIntergrupo(cellIntergurpo);
                    neocon.setGrScIng(cellGrScIng);
                    neocon.setEpigraf(cellEpigraf);
                    neocon.setResidencia(cellResidencia);
                    neocon.setBancaria(cellBancaria);
                    neocon.setForm(cellForm);
                    neocon.setTdes(cellTdes);
                    neocon.setSoporteDerivada(cellSoporteDerivada);
                    neocon.setUnid(cellUnid);
                    neocon.setTipoCambio(cellTipoCambio);
                    neocon.setAgregacion(cellAgregacion);
                    neocon.setTipoDivisa(cellTipoDivisa);
                    neocon.setTipoPais(cellTipoPais);
                    neocon.setContrap(cellContrap);
                    neocon.setTimp(cellTimp);
                    neocon.setConciliacion(cellConciliacion);
                    log[1] = "Registro actualizado exitosamente.";
                    lista.add(log);
                }
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Neocon> findAll(){
        return neoconRepository.findAll();
    }

    public Neocon findNeoconByCuenta(Long id){
        return neoconRepository.findByCuenta(id);
    }

    public Neocon modifyNeocon(Neocon toModify,Long id, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificar registro tabla Cuentas Neocon");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuentas Neocon");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

        if(toModify.getCuenta()!=id)
            neoconRepository.deleteById(id);
        return neoconRepository.save(toModify);
    }

    public Neocon saveNeocon(Neocon neocon, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Guardar registro tabla Cuentas Neocon");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuentas Neocon");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        return neoconRepository.save(neocon);
    }

    public void removeNeocon(Long id, User user){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro tabla Cuentas Neocon");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuentas Neocon");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        neoconRepository.deleteById(id);
    }

    public void clearNeocon(User user ){
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Cuentas Neocon");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Cuentas Neocon");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
        neoconRepository.deleteAll();
    }

    public Page<Neocon> getAll(Pageable pageable){
        return neoconRepository.findAll(pageable);
    }

    public List<Neocon> findByFilter(String value, String filter) {
        List<Neocon> list=new ArrayList<Neocon>();
        switch (filter)
        {
            case "Cuenta Neocon":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.cuenta LIKE ?", Neocon.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Plan De Cuentas":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.plan_cuentas LIKE ?", Neocon.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Código Jerarquico":
                Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.codigo_jerarquico LIKE ?", Neocon.class);
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Descripción":
                Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.descripcion LIKE ?", Neocon.class);
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Entrada":
                Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.entrada LIKE ?", Neocon.class);
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "Mínimo":
                Query query5 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.minimo LIKE ?", Neocon.class);
                query5.setParameter(1, value);

                list= query5.getResultList();
                break;
            case "Naturaleza":
                Query query6 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.naturaleza LIKE ?", Neocon.class);
                query6.setParameter(1, value);

                list= query6.getResultList();
                break;
            case "Intergrupo":
                Query query7 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.intergupo LIKE ?", Neocon.class);
                query7.setParameter(1, value);

                list= query7.getResultList();
                break;
            case "GrScIng":
                Query query8 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.grscing LIKE ?", Neocon.class);
                query8.setParameter(1, value);

                list= query8.getResultList();
                break;
            case "Epigraf":
                Query query9 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.epigraf LIKE ?", Neocon.class);
                query9.setParameter(1, value);

                list= query9.getResultList();
                break;
            case "Residencia":
                Query query10 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.residencia LIKE ?", Neocon.class);
                query10.setParameter(1, value);

                list= query10.getResultList();
                break;
            case "Bancaria":
                Query query11 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.bancaria LIKE ?", Neocon.class);
                query11.setParameter(1, value);

                list= query11.getResultList();
                break;
            case "Form":
                Query query12 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.form LIKE ?", Neocon.class);
                query12.setParameter(1, value);

                list= query12.getResultList();
                break;
            case "Tdes":
                Query query13 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.tdes LIKE ?", Neocon.class);
                query13.setParameter(1, value);

                list= query13.getResultList();
                break;
            case "Soporte/Derivada":
                Query query14 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.soporte_derivada LIKE ?", Neocon.class);
                query14.setParameter(1, value);

                list= query14.getResultList();
                break;
            case "Unid":
                Query query15 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.unid LIKE ?", Neocon.class);
                query15.setParameter(1, value);

                list= query15.getResultList();
                break;
            case "Tipo Cambio":
                Query query16 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.tipo_cambio LIKE ?", Neocon.class);
                query16.setParameter(1, value);

                list= query16.getResultList();
                break;
            case "Agregación":
                Query query17 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.agregacion LIKE ?", Neocon.class);
                query17.setParameter(1, value);

                list= query17.getResultList();
                break;
            case "Tipo Divisa":
                Query query18 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.tipo_divisa LIKE ?", Neocon.class);
                query18.setParameter(1, value);

                list= query18.getResultList();
                break;
            case "Tipo País":
                Query query19 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.tipo_pais LIKE ?", Neocon.class);
                query19.setParameter(1, value);

                list= query19.getResultList();
                break;
            case "Contrap":
                Query query20 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.contrap LIKE ?", Neocon.class);
                query20.setParameter(1, value);

                list= query20.getResultList();
                break;
            case "Timp":
                Query query21 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.timp LIKE ?", Neocon.class);
                query21.setParameter(1, value);

                list= query21.getResultList();
                break;
            case "Conciliación":
                Query query22 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuentas_neocon as em " +
                        "WHERE em.conciliacion LIKE ?", Neocon.class);
                query22.setParameter(1, value);

                list= query22.getResultList();
                break;
            default:
                break;
        }

        return list;
    }

}
