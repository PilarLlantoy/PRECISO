package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Contract;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.GarantBank;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.ContingentTemplate;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import com.inter.proyecto_intergrupo.repository.reports.ContingentTemplateRepository;
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
public class ContractService {

    @Autowired
    private final ContractRepository contractRepository;

    @Autowired
    private final CurrencyRepository currencyRepository;

    @Autowired
    private final ContingentTemplateRepository contingentTemplateRepository;

    @Autowired
    private final GarantBankRepository garantBankRepository;

    @Autowired
    private final YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    private final CountryRepository countryRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    public ContractService(ContractRepository contractRepository, CurrencyRepository currencyRepository, ContingentTemplateRepository contingentTemplateRepository, GarantBankRepository garantBankRepository, CountryRepository countryRepository, YntpSocietyRepository yntpSocietyRepository) {
        this.contractRepository = contractRepository;
        this.currencyRepository = currencyRepository;
        this.contingentTemplateRepository = contingentTemplateRepository;
        this.garantBankRepository = garantBankRepository;
        this.countryRepository = countryRepository;
        this.yntpSocietyRepository = yntpSocietyRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user, List<Object[]> yntpList, List<GarantBank> bankList, List<Country> countryList) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list=validarPlantilla(rows, yntpList, bankList, countryList);
            String[] temporal= list.get(0);
            if(temporal[2].equals("true"))
            {

                list=getRows(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo Contratos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Contratos");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }else{
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Fallo en inserción archivo Contratos");
                insert.setCentro(user.getCentro());
                insert.setComponente("Parametricas");
                insert.setFecha(today);
                insert.setInput("Contratos");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);

            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, List<Object[]> yntpList, List<GarantBank> bankList, List<Country> countryList) {
        ArrayList lista= new ArrayList();
        XSSFRow row;
        String[] log=new String[4];
        log[2]="false";

        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter();
                String cellContrato = formatter.formatCellValue(row.getCell(0)).replace(" ", "");
                String cellArchivoEntrada = formatter.formatCellValue(row.getCell(1));
                String cellBanco = formatter.formatCellValue(row.getCell(2)).trim();
                String cellTipoAval = formatter.formatCellValue(row.getCell(3)).trim();
                String cellTipoProceso = formatter.formatCellValue(row.getCell(4));
                String cellTipoAvalOrigen = formatter.formatCellValue(row.getCell(5));
                //String cellPais = formatter.formatCellValue(row.getCell(6));
                log[0]=String.valueOf(row.getRowNum()+1);

                if(cellContrato.isEmpty())
                {
                    log[1]="1";
                    log[2]="fail";
                    log[3]="El campo Contrato no puede estar vacío";
                    break;
                }
                else if(cellContrato.isBlank())
                {
                    log[1]="1";
                    log[2]="fail";
                    log[3]="El campo Contrato no puede estar vacío";
                    break;
                }
                else if(cellContrato.trim().length()!=18)
                {
                    log[1]="1";
                    log[2]="fail";
                    log[3]="El campo Contrato debe tener una longitud de 18 posiciones";
                    break;
                }/*
                else if(cellArchivoEntrada.isEmpty() || cellArchivoEntrada.isBlank() ||cellArchivoEntrada.length()>254)
                {
                    log[1]="2";
                    log[2]="false";
                    break;
                }*/
                else if(cellBanco.isEmpty() || cellBanco.isBlank() || cellBanco.length()>255)
                {
                    log[1]="3";
                    log[2]="fail";
                    break;
                }

                else if(yntpSocietyRepository.findByYntp(cellBanco)==null)
                {
                    if(garantBankRepository.findAllByNit(cellBanco)==null) {
                        log[1]="3";
                        log[2]="fail";
                        log[3]="El campo YNTP no cruza con Sociedades YNTP ni Bancos Garantes";
                        break;
                    }
                }
                else if(cellTipoAval.length() == 0 || cellTipoAval.trim().isBlank() || cellTipoAval.length()>254)
                {
                    log[1]="4";
                    log[2]="fail";
                    break;
                }
                /*else if(cellTipoProceso.isEmpty() || cellTipoProceso.isBlank() ||cellTipoProceso.length()>254)
                {
                    log[1]="5";
                    log[2]="false";
                    break;
                }
                else if((!cellTipoAvalOrigen.isEmpty() || !cellTipoAvalOrigen.isBlank()) && cellTipoAvalOrigen.length()>254)
                {
                    log[1]="6";
                    log[2]="false";
                    break;
                }
                else if((cellPais.isEmpty() || cellPais.isBlank()) && (cellPais.length()>254||countryRepository.findAllById(cellPais)==null))
                {
                    log[1]="7";
                    log[2]="false";
                    break;
                }*/
                else{
                    try
                    {
                        log[2]="true";
                    }
                    catch(Exception e){
                        log[2]="falseFormat";
                        e.printStackTrace();
                        lista.add(log);
                        return lista;
                    }
                }
            }
        }
        if(lista.size()==0) {
            String[] log1 = new String[4];
            log1[2] = "true";
            lista.add(log1);
        }
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

            if(row.getRowNum()>0)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellContrato = formatter.formatCellValue(row.getCell(0));
                String cellArchivoEntrada = formatter.formatCellValue(row.getCell(1));
                String cellBanco = formatter.formatCellValue(row.getCell(2)).trim();
                String cellTipoAval = formatter.formatCellValue(row.getCell(3));
                String cellTipoProceso = formatter.formatCellValue(row.getCell(4));
                //String cellTipoAvalOrigen = formatter.formatCellValue(row.getCell(5));
                //String cellPais = formatter.formatCellValue(row.getCell(6));
                log[0]=cellContrato;
                if((cellContrato.isEmpty() || cellContrato.isBlank()) && (cellBanco.isEmpty() || cellBanco.isBlank())/* && (cellTipoProceso.isEmpty() || cellTipoProceso.isBlank())*/)
                {
                    break;
                }
                else if(contractRepository.findByContrato(cellContrato)!=null)
                {
                    if(findYntpByFilter(cellBanco, "yntp").size()!=0)
                    {
                        YntpSociety sociedad=yntpSocietyRepository.findByYntp(cellBanco);
                        if(sociedad!=null && sociedad.getPais()!=null)
                        {
                            Contract contract = contractRepository.findByContrato(cellContrato);
                            contract.setContrato(cellContrato);
                            contract.setBanco(cellBanco);
                            contract.setPaisContrato(sociedad.getPais());
                            if(cellTipoProceso.trim().length() >0){
                                contract.setTipoProceso(cellTipoProceso);
                            }
                            if(cellArchivoEntrada.trim().length() >0){
                                contract.setArchivoEntrada(cellArchivoEntrada);
                            }
                            Query aval = entityManager.createNativeQuery("SELECT id_tipo_aval, aval_origen FROM nexco_tipo_aval WHERE id_tipo_aval = ? GROUP BY id_tipo_aval, aval_origen");
                            aval.setParameter(1,cellTipoAval.trim());
                            List<Object[]> avalRes = aval.getResultList();
                            if(!avalRes.isEmpty()){
                                contract.setTipoAval(cellTipoAval);
                                contract.setTipoAvalOrigen(avalRes.get(0)[1].toString());
                            }

                            /*if (!cellTipoAval.isBlank() || !cellTipoAval.isEmpty()) {
                                contract.setTipoAval(cellTipoAval);
                            } else {
                                contract.setTipoAval("");
                            }
                                contract.setTipoProceso(cellTipoProceso);
                            if (!cellTipoAvalOrigen.isBlank() || !cellTipoAvalOrigen.isEmpty()) {
                                contract.setTipoAvalOrigen(cellTipoAvalOrigen);
                            } else {
                                contract.setTipoAvalOrigen("");
                            }*/
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";


                            List<ContingentTemplate> ajuste = contingentTemplateRepository.findAllByContrato(cellContrato);
                            for (ContingentTemplate template:ajuste) {
                                template.setPaisBanco(sociedad.getPais().getNombre());
                                template.setNombreBanco(cellBanco);
                                contingentTemplateRepository.save(template);
                            }

                        }
                        else
                        {
                            log[1] = "Registro no Insertado. País de banco "+cellBanco+" en parametrica de Sociedades Yntp no tiene un país asignado";
                        }
                    }
                    else if(garantBankRepository.findAllByNit(cellBanco)!=null)
                    {
                        List<GarantBank> bancoGrante=garantBankRepository.findAllByNit(cellBanco);
                        if(bancoGrante.size() > 0 && bancoGrante.get(0).getPais()!=null)
                        {
                            Contract contract = contractRepository.findByContrato(cellContrato);
                            contract.setContrato(cellContrato);
                            contract.setBanco(cellBanco);
                            contract.setPaisContrato(countryRepository.findAllById(bancoGrante.get(0).getPais()));
                            if(cellTipoProceso.trim().length() >0){
                                contract.setTipoProceso(cellTipoProceso);
                            }

                            if(cellArchivoEntrada.trim().length() >0){
                                contract.setArchivoEntrada(cellArchivoEntrada);
                            }

                            Query aval = entityManager.createNativeQuery("SELECT id_tipo_aval, aval_origen FROM nexco_tipo_aval WHERE id_tipo_aval = ? GROUP BY id_tipo_aval, aval_origen");
                            aval.setParameter(1,cellTipoAval.trim());
                            List<Object[]> avalRes = aval.getResultList();
                            if(!avalRes.isEmpty()){
                                contract.setTipoAval(cellTipoAval);
                                contract.setTipoAvalOrigen(avalRes.get(0)[1].toString());
                            }
                            /*if (!cellTipoAval.isBlank() || !cellTipoAval.isEmpty()) {
                                contract.setTipoAval(cellTipoAval);
                            } else {
                                contract.setTipoAval("");
                            }
                                contract.setTipoProceso(cellTipoProceso);
                            if (!cellTipoAvalOrigen.isBlank() || !cellTipoAvalOrigen.isEmpty()) {
                                contract.setTipoAvalOrigen(cellTipoAvalOrigen);
                            } else {
                                contract.setTipoAvalOrigen("");
                            }*/
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";

                            List<ContingentTemplate> ajuste = contingentTemplateRepository.findAllByContrato(cellContrato);
                            for (ContingentTemplate template:ajuste) {
                                template.setPaisBanco(countryRepository.findAllById(bancoGrante.get(0).getPais()).getId());
                                template.setNombreBanco(cellBanco);
                                contingentTemplateRepository.save(template);
                            }

                        }
                        else
                        {
                            log[1] = "Registro no Insertado. País de banco "+cellBanco+" en parametrica de Banco Garante no tiene un país asignado";
                        }
                    }
                    else
                    {
                        log[1] = "Registro no Insertado. Banco "+cellBanco+" se debe encontrar en parametrica de Sociedades Yntp o Banco Garante";
                    }
                }
                else if(contractRepository.findByContrato(cellContrato)==null)
                {
                    if(findYntpByFilter(cellBanco, "yntp").size()!=0)
                    {
                        YntpSociety sociedad=yntpSocietyRepository.findByYntp(cellBanco);
                        if(sociedad!=null && sociedad.getPais()!=null)
                        {
                            Contract contract = new Contract();
                            contract.setContrato(cellContrato);
                            contract.setBanco(cellBanco);
                            contract.setPaisContrato(sociedad.getPais());
                            contract.setTipoProceso(cellTipoProceso);
                            contract.setArchivoEntrada(cellArchivoEntrada);

                            Query aval = entityManager.createNativeQuery("SELECT id_tipo_aval, aval_origen FROM nexco_tipo_aval WHERE id_tipo_aval = ? GROUP BY id_tipo_aval, aval_origen");
                            aval.setParameter(1,cellTipoAval.trim());
                            List<Object[]> avalRes = aval.getResultList();
                            if(!avalRes.isEmpty()){
                                contract.setTipoAval(cellTipoAval);
                                contract.setTipoAvalOrigen(avalRes.get(0)[1].toString());
                            }
                            /*if (!cellTipoAval.isBlank() || !cellTipoAval.isEmpty()) {
                                contract.setTipoAval(cellTipoAval);
                            } else {
                                contract.setTipoAval("");
                            }
                                contract.setTipoProceso(cellTipoProceso);
                            if (!cellTipoAvalOrigen.isBlank() || !cellTipoAvalOrigen.isEmpty()) {
                                contract.setTipoAvalOrigen(cellTipoAvalOrigen);
                            } else {
                                contract.setTipoAvalOrigen("");
                            }*/
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";


                            List<ContingentTemplate> ajuste = contingentTemplateRepository.findAllByContrato(cellContrato);
                            for (ContingentTemplate template:ajuste) {
                                template.setPaisBanco(sociedad.getPais().getNombre());
                                template.setNombreBanco(cellBanco);
                                contingentTemplateRepository.save(template);
                            }

                        }
                        else
                        {
                            log[1] = "Registro no Insertado. País de banco "+cellBanco+" en parametrica de Sociedades Yntp no tiene un país asignado";
                        }
                    }
                    else if(garantBankRepository.findAllByNit(cellBanco)!=null)
                    {
                        List<GarantBank> bancoGrante=garantBankRepository.findAllByNit(cellBanco);
                        if(bancoGrante.size() > 0 && bancoGrante.get(0).getPais()!=null)
                        {
                            Contract contract = new Contract();
                            contract.setContrato(cellContrato);
                            contract.setBanco(cellBanco);
                            contract.setPaisContrato(countryRepository.findAllById(bancoGrante.get(0).getPais()));
                            contract.setTipoProceso(cellTipoProceso);
                            contract.setArchivoEntrada(cellArchivoEntrada);

                            Query aval = entityManager.createNativeQuery("SELECT id_tipo_aval, aval_origen FROM nexco_tipo_aval WHERE id_tipo_aval = ? GROUP BY id_tipo_aval, aval_origen");
                            aval.setParameter(1,cellTipoAval.trim());
                            List<Object[]> avalRes = aval.getResultList();
                            if(!avalRes.isEmpty()){
                                contract.setTipoAval(cellTipoAval);
                                contract.setTipoAvalOrigen(avalRes.get(0)[1].toString());
                            }
                            /*if (!cellTipoAval.isBlank() || !cellTipoAval.isEmpty()) {
                                contract.setTipoAval(cellTipoAval);
                            } else {
                                contract.setTipoAval("");
                            }
                                contract.setTipoProceso(cellTipoProceso);
                            if (!cellTipoAvalOrigen.isBlank() || !cellTipoAvalOrigen.isEmpty()) {
                                contract.setTipoAvalOrigen(cellTipoAvalOrigen);
                            } else {
                                contract.setTipoAvalOrigen("");
                            }*/
                            contractRepository.save(contract);
                            log[1] = "Registro actualizado exitosamente";

                            List<ContingentTemplate> ajuste = contingentTemplateRepository.findAllByContrato(cellContrato);
                            for (ContingentTemplate template:ajuste) {
                                template.setPaisBanco(countryRepository.findAllById(bancoGrante.get(0).getPais()).getId());
                                template.setNombreBanco(cellBanco);
                                contingentTemplateRepository.save(template);
                            }

                        }
                        else
                        {
                            log[1] = "Registro no Insertado. País de banco "+cellBanco+" en parametrica de Banco Garante no tiene un país asignado";
                        }
                    }
                    else
                    {
                        log[1] = "Registro no Insertado. Banco "+cellBanco+" se debe encontrar en parametrica de Sociedades Yntp o Banco Garante";
                    }
                }
                lista.add(log);
            }
            else{
                firstRow=0;
            }
        }
        return lista;
    }

    public List<Contract> findAll(){

        return contractRepository.findAll();

    }

    public List<Object[]> findAllJoin(){

        Query query = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada origen, d.sociedad_corta, \n" +
                "c.aval_origen, a.tipo_proceso, a.tipo_aval_origen, b.nombre_pais \n" +
                "from nexco_contratos a \n" +
                "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                "left join nexco_sociedades_yntp d on convert(int, a.banco) = d.yntp ");

        return query.getResultList();

        //return contractRepository.findAll();

    }

    public Contract findContract(String contrato){
        return contractRepository.findAllByContrato(contrato);
    }

    public List<Object[]> findContractJoin(String contrato){

        Query query = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada origen, a.tipo_aval_origen, \n" +
                "a.tipo_aval id_tipo_aval, c.aval_origen, a.tipo_proceso, a.banco id_sociedad, d.nit, a.id_pais, b.nombre_pais \n" +
                "from nexco_contratos a \n" +
                "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                "left join nexco_banco_garante as d on a.banco = d.nit \n" +
                "where a.id_contrato = ?");

        query.setParameter(1,contrato);

        return query.getResultList();

        //return contractRepository.findAllByContrato(contrato);
    }

    public void modifyContract(Contract toModify,String id,String pais,User user){

        Contract toInsert = new Contract();
        toInsert.setContrato(toModify.getContrato());
        toInsert.setArchivoEntrada(toModify.getArchivoEntrada());
        toInsert.setBanco(toModify.getBanco());
        toInsert.setTipoAval(toModify.getTipoAval());
        toInsert.setTipoAvalOrigen(toModify.getTipoAvalOrigen());
        toInsert.setTipoProceso(toModify.getTipoProceso());
        toInsert.setPaisContrato(countryRepository.findAllById(pais));
        Query query = entityManager.createNativeQuery("UPDATE nexco_contratos SET id_contrato = ? , archivo_entrada = ? , " +
                "banco = ? , tipo_aval = ? , tipo_aval_origen = ? , id_pais = ? WHERE id_contrato = ?", Contract.class);
        query.setParameter(1, toInsert.getContrato());
        query.setParameter(2, toInsert.getArchivoEntrada());
        if(yntpSocietyRepository.findByYntp(toInsert.getBanco())!=null && yntpSocietyRepository.findByYntp(toInsert.getBanco()).getPais()!=null)
        {
            query.setParameter(3, toInsert.getBanco());
            query.setParameter(6, yntpSocietyRepository.findBySociedadDescripcionCorta(toInsert.getBanco()).getPais().getId());
        }
        else if(garantBankRepository.findByNombreSimilar(toInsert.getBanco())!=null && garantBankRepository.findByNombreSimilar(toInsert.getBanco()).getPais()!=null)
        {
            query.setParameter(3, toInsert.getBanco());
            query.setParameter(6, garantBankRepository.findByNombreSimilar(toInsert.getBanco()).getPais());
        }
        query.setParameter(4, toInsert.getTipoAval());
        query.setParameter(5, toInsert.getTipoAvalOrigen());
        query.setParameter(7,toInsert.getContrato());
        query.executeUpdate();

        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro archivo Contratos");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Contratos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public void modifyContractN(String id_contrato_old, String id_contrato, String origen, String tipo_aval, String yntp, String tipo_proceso, String pais, String id, User user){

        Query query1 = entityManager.createNativeQuery("SELECT aval_origen FROM nexco_tipo_aval where id_tipo_Aval = ?");
        query1.setParameter(1,tipo_aval);
        List<String> list=query1.getResultList();

        Query query = entityManager.createNativeQuery("UPDATE nexco_contratos SET id_contrato = ?, archivo_entrada = ? , " +
                "tipo_aval = ?, banco = case when ? = '' then null else ? end, tipo_proceso = ?, id_pais = ?, tipo_aval_origen = ? WHERE id_contrato = ?");
        query.setParameter(1, id_contrato);
        query.setParameter(2, origen);
        query.setParameter(3, tipo_aval);
        query.setParameter(4, yntp);
        query.setParameter(5, yntp);
        query.setParameter(6, tipo_proceso);
        query.setParameter(7, pais);
        if(list.size()>0)
            query.setParameter(8, list.get(0).toString());
        else
            query.setParameter(8, "");
        query.setParameter(9, id_contrato_old);

        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Modificacion registro archivo Contratos");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Contratos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);

    }

    public void removeContract(String id, User user){
        Query query = entityManager.createNativeQuery("DELETE from nexco_contratos " +
                "WHERE id_contrato = ?", Contract.class);
        query.setParameter(1, Long.parseLong(id));
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Eliminar registro archivo Contratos");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Contratos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public void clearContract(User user){
        Query query = entityManager.createNativeQuery("DELETE from nexco_contratos", Contract.class);
        query.executeUpdate();
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion("Limpiar tabla Contratos");
        insert.setCentro(user.getCentro());
        insert.setComponente("Parametricas");
        insert.setFecha(today);
        insert.setInput("Contratos");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public Page<Contract> getAll(Pageable pageable){

        return contractRepository.findAll(pageable);
    }

    public List<Object> getAllJoin(){

        Query query = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada origen, ISNULL(f.yntp,d.nit), \n" +
                "c.id_tipo_aval, a.tipo_proceso, c.aval_origen, b.id_pais \n" +
                "from nexco_contratos a \n" +
                "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                "left join nexco_banco_garante as d on a.banco = d.nit GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                "c.id_tipo_aval, a.tipo_proceso, c.aval_origen, b.id_pais order by a.id_contrato");

        return query.getResultList();

    }

    public List<Object[]> findByFilter(String value, String filter) {
        List<Object[]> list=new ArrayList<Object[]>();
        switch (filter)
        {
            case "Contrato":
                Query query = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.id_contrato LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Origen":
                Query query0 = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.archivo_entrada LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            case "Banco":
                Query query1 = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada , ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.banco LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query1.setParameter(1, value);

                list= query1.getResultList();
                break;
            case "Tipo Aval":
                Query query2 = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada , ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.tipo_aval LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query2.setParameter(1, value);

                list= query2.getResultList();
                break;
            case "Tipo Aval Origen":
                Query query3 = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada , ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.tipo_aval_origen LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query3.setParameter(1, value);

                list= query3.getResultList();
                break;
            case "Tipo Proceso":
                Query query4 = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada , ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.tipo_proceso LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query4.setParameter(1, value);

                list= query4.getResultList();
                break;
            case "País":
                Query query6 = entityManager.createNativeQuery("select a.id_contrato, a.archivo_entrada , ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais \n" +
                        "from nexco_contratos a \n" +
                        "left join nexco_paises b on a.id_pais = b.id_pais \n" +
                        "left join nexco_tipo_aval c on a.tipo_aval = c.id_tipo_aval \n" +
                        "left join nexco_sociedades_yntp as f on f.yntp = a.banco \n" +
                        "left join nexco_banco_garante as d on a.banco = d.nit "+
                        "WHERE a.id_pais LIKE ? GROUP BY a.id_contrato, a.archivo_entrada, ISNULL(f.yntp,d.nit), \n" +
                        "a.tipo_aval, a.tipo_proceso, a.tipo_aval_origen, b.id_pais order by a.id_contrato");
                query6.setParameter(1, value);
                list= query6.getResultList();
                break;
            default:
                break;
        }
        return list;
    }

    public List<YntpSociety> findYntpByFilter(String value, String filter) {
        List<YntpSociety> list=new ArrayList<YntpSociety>();
        switch (filter)
        {
            case "yntp":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_sociedades_yntp as em " +
                        "WHERE RIGHT('00000' + Ltrim(Rtrim(em.yntp)),5) LIKE ?", YntpSociety.class);
                query.setParameter(1, value);

                list= query.getResultList();

                break;
            default:
                break;
        }
        return list;
    }
}
