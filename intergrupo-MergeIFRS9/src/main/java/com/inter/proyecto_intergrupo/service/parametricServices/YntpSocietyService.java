package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.model.temporal.YntpSocietyTemporal;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import com.inter.proyecto_intergrupo.repository.temporal.YntpSocietyTemporalRepository;
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
public class YntpSocietyService {

    @Autowired
    private final YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    private YntpSocietyTemporalRepository yntpSocietyTemporalRepository;

    @Autowired
    private final CountryRepository countryRepository;

    @Autowired
    private final CurrencyRepository currencyRepository;

    @Autowired
    private final ConsolidationMethodRepository consolidationMethodRepository;

    @Autowired
    private final ConsolidationGroupRepository consolidationGroupRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public YntpSocietyService(ConsolidationGroupRepository consolidationGroupRepository, CurrencyRepository currencyRepository, YntpSocietyRepository yntpSocietyRepository, CountryRepository countryRepository, ConsolidationMethodRepository consolidationMethodRepository) {
        this.yntpSocietyRepository = yntpSocietyRepository;
        this.countryRepository = countryRepository;
        this.consolidationMethodRepository = consolidationMethodRepository;
        this.currencyRepository = currencyRepository;
        this.consolidationGroupRepository = consolidationGroupRepository;
    }

    public ArrayList<String[]> saveFileBD(InputStream file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;

            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list = validateTemplate(rows);
            String[] temporal = list.get(0);
            if (temporal[2].equals("true"))
            {

                Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_sociedades_yntp");
                delete.executeUpdate();

                list = getRowsSql(rows1);
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Inserción archivo YNTP");
                insert.setCentro(user.getCentro());
                insert.setComponente("YNTP");
                insert.setFecha(today);
                insert.setInput("YNTP Sociedades");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            } else {
                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Error inserción archivo YNTP");
                insert.setCentro(user.getCentro());
                insert.setComponente("YNTP");
                insert.setFecha(today);
                insert.setInput("YNTP Sociedades");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
            }
        }
            return list;
        }

        public ArrayList<String[]> validateTemplate(Iterator<Row> rows) {
            ArrayList lista= new ArrayList();
            List<YntpSocietyTemporal> listInsert= new ArrayList<YntpSocietyTemporal>();
            XSSFRow row;
            String[] log=new String[3];
            log[0]="0";
            log[1]="0";
            log[2]="false";

            Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_sociedades_yntp_temporal");
            delete.executeUpdate();

            while (rows.hasNext())
            {
                row = (XSSFRow) rows.next();
                if(row.getRowNum()>13)
                {
                    log[0]=String.valueOf(row.getRowNum());
                    DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                    String cellYntp= formatter.formatCellValue(row.getCell(0));
                    String cellSociedadLarga = formatter.formatCellValue(row.getCell(1));
                    String cellSociedadCorta = formatter.formatCellValue(row.getCell(2));
                    String cellDivisa = formatter.formatCellValue(row.getCell(4));
                    String cellMetodo = formatter.formatCellValue(row.getCell(8));
                    String cellGrupo = formatter.formatCellValue(row.getCell(9));
                    String cellPais = formatter.formatCellValue(row.getCell(18));
                    String cellTipoEntidad = formatter.formatCellValue(row.getCell(7));

                    String[] partsDivisa = new String[4];
                    String[] partsPais = new String[4];
                    String[] partsGrupo = new String[4];
                    String[] partsMetodo = new String[4];

                    try
                    {
                        log[1]="1"; Integer.parseInt(cellYntp);
                        log[1]="5"; partsDivisa = cellDivisa.split("-");
                        log[1]="9"; partsMetodo = cellMetodo.split("-");
                        log[1]="10"; partsGrupo = cellGrupo.split("-");
                        log[1]="19"; partsPais = cellPais.split("-");
                        log[2]="true";
                    }
                    catch(Exception e){
                        log[2]="falseFormat";
                        lista.add(log);
                        return lista;
                    }

                    if(cellYntp.isEmpty() || cellYntp.isBlank() ||cellYntp.length()!=5)
                    {
                        log[1]="1";
                        log[2]="false";
                        break;
                    }
                    else if(cellSociedadLarga.isEmpty() || cellSociedadLarga.isBlank() ||cellSociedadLarga.length()>254)
                    {
                        log[1]="2";
                        log[2]="false";
                        break;
                    }
                    else if(cellSociedadCorta.isEmpty() || cellSociedadCorta.isBlank() ||cellSociedadCorta.length()>254)
                    {
                        log[1]="3";
                        log[2]="false";
                        break;
                    }
                    else if(cellDivisa.isEmpty() || cellDivisa.isBlank() ||partsDivisa[0].trim().length()!=3)
                    {
                        log[1]="5";
                        log[2]="false";
                        break;
                    }
                    else if(cellMetodo.isEmpty() || cellMetodo.isBlank() ||partsMetodo[0].trim().length()!=1)
                    {
                        log[1]="9";
                        log[2]="false";
                        break;
                    }
                    else if(cellGrupo.isEmpty() || cellGrupo.isBlank() ||partsGrupo[0].trim().length()!=2)
                    {
                        log[1]="10";
                        log[2]="false";
                        break;
                    }
                    else if(cellPais.isEmpty() || cellPais.isBlank() ||partsPais[0].trim().length()!=2)
                    {
                        log[0]=String.valueOf(row.getRowNum());
                        log[1]="19";
                        log[2]="false";
                        break;
                    }else if(cellTipoEntidad.isEmpty() || cellTipoEntidad.isBlank())
                    {
                        log[0]=String.valueOf(row.getRowNum());
                        log[1]="29";
                        log[2]="false";
                        break;
                    }
                    else
                    {
                        YntpSocietyTemporal insert = new YntpSocietyTemporal();
                        insert.setYntp(cellYntp);
                        insert.setSociedadDescripcionLarga(cellSociedadLarga);
                        insert.setSociedadDescripcionCorta(cellSociedadCorta);
                        insert.setDivisa(partsDivisa[0].trim());
                        insert.setMetodo(partsMetodo[0].trim());
                        insert.setGrupo(partsGrupo[0].trim());
                        insert.setPais(partsPais[0].trim());
                        insert.setTipoEntidad(cellTipoEntidad);
                        if(partsPais.length>2)
                            insert.setNombrePais(partsPais[1]+" "+partsPais[2]);
                        else
                            insert.setNombrePais(partsPais[1].trim());
                        if(partsDivisa.length>2)
                            insert.setNombreDivisa(partsDivisa[1]+" "+partsDivisa[2]);
                        else
                            insert.setNombreDivisa(partsDivisa[1].trim());
                        listInsert.add(insert);
                    }
                }
            }
            if(lista.size()==0)
            {
                yntpSocietyTemporalRepository.saveAll(listInsert);
            }
            lista.add(log);
            return lista;
        }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        ArrayList lista= new ArrayList();
        ArrayList<String[]> listaLogD= new ArrayList<String[]>();
        ArrayList<String[]> listaLogP= new ArrayList<String[]>();
        ArrayList<YntpSociety> listaYntp= new ArrayList<YntpSociety>();
        ArrayList<Currency> listaDivisa= new ArrayList<Currency>();
        ArrayList<Country> listaPais= new ArrayList<Country>();
        int firstRow=1;
        while (rows.hasNext())
        {
            String[] log=new String[4];
            String[] logD=new String[4];
            String[] logP=new String[4];
            log[2]="true";
            row = (XSSFRow) rows.next();
            if(row.getRowNum()>13)
            {
                DataFormatter formatter = new DataFormatter(); //creating formatter using the default locale
                String cellYntp = formatter.formatCellValue(row.getCell(0));

                    String cellSociedadLarga = formatter.formatCellValue(row.getCell(1));
                    String cellSociedadCorta = formatter.formatCellValue(row.getCell(2));
                    String cellDivisa = formatter.formatCellValue(row.getCell(4));
                    String cellMetodo = formatter.formatCellValue(row.getCell(8));
                    String cellGrupo = formatter.formatCellValue(row.getCell(9));
                    String cellPais = formatter.formatCellValue(row.getCell(18));
                    String cellTipoEntidad = formatter.formatCellValue(row.getCell(7));
                    ConsolidationGroup group= new ConsolidationGroup();
                    ConsolidationMethod method=new ConsolidationMethod();

                    try {
                        group = consolidationGroupRepository.findAllById(cellGrupo.split("-")[0].trim());
                        method =consolidationMethodRepository.findAllById(cellMetodo.split("-")[0].trim());
                    }
                    catch (Exception e){
                        continue;
                    }
                    if(group==null)
                    {
                        log[0]=cellYntp;
                        log[1]="Fallo al ingresar registro, No se encuentra el grupo -> "+cellGrupo.split("-")[0].trim();
                    }
                    else if(method==null)
                    {
                        log[0]=cellYntp;
                        log[1]="Fallo al ingresar registro, No se encuentra el método -> "+cellMetodo.split("-")[0].trim();
                    }
                    else
                    {
                        Country country =new Country();
                            String[] partsText = cellPais.trim().split("-");
                        country.setId(partsText[0].trim());
                        country.setNombre(partsText[1].trim());
                        if(partsText.length>2)
                            country.setNombre(country.getNombre()+" "+partsText[2]);
                        listaPais.add(country);

                    logP[0] = country.getId();
                    logP[1] = "País Insertado exitosamente.";
                    listaLogP.add(logP);


                    Currency currency =new Currency();
                    currency.setId(cellDivisa.split("-")[0].trim());
                    currency.setNombre(cellDivisa.split("-")[1].trim());

                    try {
                        Currency temp= currencyRepository.findAllById(currency.getId());
                        if(temp!=null)
                        {
                            currency.setDivisaNeocon(temp.getDivisaNeocon());
                            logD[0] = currency.getId();
                            logD[1] = "Divisa Actualizada exitosamente.";
                        }
                        else
                        {
                            logD[0] = country.getId();
                            logD[1] = "Divisa Insertada exitosamente.";
                        }
                        listaLogD.add(logD);
                    }catch (Exception e ){

                    }
                    listaDivisa.add(currency);

                    YntpSociety yntp = new YntpSociety();
                    yntp.setYntp(cellYntp);
                    yntp.setSociedadDescripcionLarga(cellSociedadLarga);
                    yntp.setSociedadDescripcionCorta(cellSociedadCorta);
                    yntp.setPais(country);
                    yntp.setDivisa(currency);
                    yntp.setGrupo(group);
                    yntp.setMetodo(method);
                    yntp.setTipoEntidad(cellTipoEntidad);
                    listaYntp.add(yntp);
                    log[0] = cellYntp;
                    log[1] = "Registro insertado exitosamente.";
                }
                lista.add(log);
            }
        }

        String[] log1=new String[4];
        log1[0]="DIVISA";
        lista.add(log1);

        lista.addAll(listaLogD);

        String[] log2=new String[4];
        log2[0]="PAIS";
        lista.add(log2);

        lista.addAll(listaLogP);

        countryRepository.saveAll(listaPais);
        currencyRepository.saveAll(listaDivisa);
        yntpSocietyRepository.saveAll(listaYntp);

            return lista;
        }

    public ArrayList getRowsSql(Iterator<Row> rows) {
        XSSFRow row;
        Date today=new Date();
        boolean success = false;
        List<String> listInsert = new ArrayList<>();
        ArrayList lista= new ArrayList();
        List<String[]> listaLogD= new ArrayList<String[]>();
        List<String[]> listaLogP= new ArrayList<String[]>();
        List<Currency> listaDivisa= new ArrayList<Currency>();
        List<Country> listaPais= new ArrayList<Country>();

        Query search = entityManager.createNativeQuery("SELECT nsyt.yntp,\n" +
                "nsyt.id_divisa,\n" +
                "nsyt.id_grupo_ifrs,\n" +
                "nsyt.id_metodo_ifrs,\n" +
                "nsyt.id_pais,\n" +
                "nsyt.sociedad_corta,\n" +
                "nsyt.sociedad_larga,\n" +
                "nsyt.tipo_entidad,\n" +
                "nd.id_divisa SD,\n" +
                "ngc.id_grupo_ifrs SG,\n" +
                "nmc.id_metodo_ifrs SM,\n" +
                "np.id_pais SP, \n"+
                "nsyt.nombre_pais, \n" +
                "nsyt.nombre_divisa \n" +
                "FROM nexco_sociedades_yntp_temporal AS nsyt\n" +
                "LEFT JOIN nexco_divisas AS nd ON nsyt.id_divisa = nd.id_divisa\n" +
                "LEFT JOIN nexco_paises AS np ON nsyt.id_pais = np.id_pais\n" +
                "LEFT JOIN nexco_metodos_consolidacion AS nmc ON nsyt.id_metodo_ifrs = nmc.id_metodo_ifrs\n" +
                "LEFT JOIN nexco_grupos_consolidacion AS ngc ON nsyt.id_grupo_ifrs = ngc.id_grupo_ifrs");

        List<Object[]> listResult = search.getResultList();

        for (Object[] yntp:listResult)
        {
            if(yntp[9]==null)
            {
                String[] log=new String[4];
                log[0]=yntp[0].toString();
                log[1]="Fallo al ingresar registro, No se encuentra el grupo -> "+yntp[2];
                log[2]="true";
                lista.add(log);
                success = true;
            }
            else if(yntp[10]==null)
            {
                String[] log=new String[4];
                log[0]=yntp[0].toString();
                log[1]="Fallo al ingresar registro, No se encuentra el método -> "+yntp[3];
                log[2]="true";
                lista.add(log);
                success = true;
            }
            else
            {
                if (yntp[11] == null && !listInsert.contains(yntp[4].toString())) {
                    Country country = new Country();
                    country.setId(yntp[4].toString());
                    country.setNombre(yntp[12].toString());
                    listaPais.add(country);
                    listInsert.add(yntp[4].toString());

                    String[] logP = new String[4];
                    logP[0] = country.getId();
                    logP[1] = "País Insertado exitosamente.";
                    listaLogP.add(logP);
                }
                if (yntp[8] == null && !listInsert.contains(yntp[1].toString())) {
                    Currency currency = new Currency();
                    currency.setId(yntp[1].toString());
                    currency.setNombre(yntp[13].toString());
                    listaDivisa.add(currency);
                    listInsert.add(yntp[1].toString());

                    String[] logD = new String[4];
                    logD[0] = currency.getId();
                    logD[1] = "Divisa Insertada exitosamente.";
                    listaLogD.add(logD);
                }

                String[] log = new String[4];
                log[0] = yntp[0].toString();
                log[1] = "Registro insertado exitosamente.";
                log[2] = "true";
                lista.add(log);

            }
        }

        String[] log1=new String[4];
        log1[0]="DIVISA";
        lista.add(log1);

        lista.addAll(listaLogD);

        String[] log2=new String[4];
        log2[0]="PAIS";
        lista.add(log2);

        lista.addAll(listaLogP);

        countryRepository.saveAll(listaPais);
        currencyRepository.saveAll(listaDivisa);

        if(success==false)
        {
            Query insertQuery = entityManager.createNativeQuery("INSERT INTO nexco_sociedades_yntp (yntp,id_divisa,id_grupo_ifrs,id_metodo_ifrs,id_pais,sociedad_corta,sociedad_larga,tipo_entidad) (SELECT nsyt.yntp,\n" +
                    "nsyt.id_divisa,\n" +
                    "nsyt.id_grupo_ifrs,\n" +
                    "nsyt.id_metodo_ifrs,\n" +
                    "nsyt.id_pais,\n" +
                    "nsyt.sociedad_corta,\n" +
                    "nsyt.sociedad_larga,\n" +
                    "nsyt.tipo_entidad\n" +
                    "FROM nexco_sociedades_yntp_temporal AS nsyt)\n");
            insertQuery.executeUpdate();
        }

        return lista;
    }

        public List<YntpSociety> findAll(){
            return yntpSocietyRepository.findAll();
        }

        public String[] findAllString(){
            Query query = entityManager.createNativeQuery("SELECT TOP 1 sociedad_corta FROM nexco_sociedades_yntp");
            String[] lista= (String[]) query.getResultList().stream().toArray(String[]::new);
            return lista;
        }

        public List<Object[]> findAllCeros(){
            Query query = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp AS nsy");
            return query.getResultList();
        }

        public YntpSociety findYntpByYntp(String id){
            return yntpSocietyRepository.findByYntp(id);
        }

        public YntpSociety modifyYntp(YntpSociety toModify, String id, String pais, String divisa, String grupo,String metodo){
            YntpSociety toInsert = new YntpSociety();
            toInsert.setYntp(toModify.getYntp());
            toInsert.setSociedadDescripcionLarga(toModify.getSociedadDescripcionLarga());
            toInsert.setSociedadDescripcionCorta(toModify.getSociedadDescripcionCorta());
            toInsert.setPais(countryRepository.findAllById(pais));
            toInsert.setDivisa(currencyRepository.findAllById(divisa));
            toInsert.setGrupo(consolidationGroupRepository.findAllById(grupo));
            toInsert.setMetodo(consolidationMethodRepository.findAllById(metodo));
            if(!toModify.getYntp().equals(id))
                yntpSocietyRepository.deleteById(id);
            return yntpSocietyRepository.save(toInsert);
        }

        public YntpSociety saveYntp(YntpSociety yntp, String pais, String divisa, String grupo,String metodo){
            yntp.setYntp(yntp.getYntp());
            yntp.setSociedadDescripcionLarga(yntp.getSociedadDescripcionLarga());
            yntp.setSociedadDescripcionCorta(yntp.getSociedadDescripcionCorta());
            yntp.setPais(countryRepository.findAllById(pais));
            yntp.setDivisa(currencyRepository.findAllById(divisa));
            yntp.setMetodo(consolidationMethodRepository.findAllById(metodo));
            yntp.setGrupo(consolidationGroupRepository.findAllById(grupo));
            return yntpSocietyRepository.save(yntp);
        }

        public void removeYntp(String id, User user){
            Query query = entityManager.createNativeQuery("DELETE from nexco_sociedades_yntp " +
                    "WHERE yntp = ?", YntpSociety.class);
            query.setParameter(1,id);
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Eliminación registro YNTP");
            insert.setCentro(user.getCentro());
            insert.setComponente("YNTP");
            insert.setFecha(today);
            insert.setInput("YNTP");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);
            query.executeUpdate();
        }

        public void clearYntp(User user){
            Query query = entityManager.createNativeQuery("DELETE from nexco_sociedades_yntp", YntpSociety.class);
            Date today=new Date();
            Audit insert = new Audit();
            insert.setAccion("Eliminación tabla YNTP");
            insert.setCentro(user.getCentro());
            insert.setComponente("YNTP");
            insert.setFecha(today);
            insert.setInput("YNTP");
            insert.setNombre(user.getPrimerNombre());
            insert.setUsuario(user.getUsuario());
            auditRepository.save(insert);

            query.executeUpdate();
        }

        public Page<YntpSociety> getAll(Pageable pageable){
            return yntpSocietyRepository.findAll(pageable);
        }

        public List<Object[]> findByFilter(String value, String filter) {
            List<Object[]> list=new ArrayList<Object[]>();
            switch (filter)
            {
                case "Código Sociedad":
                    Query query = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5),nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy " +
                            "WHERE nsy.yntp LIKE ?");
                    query.setParameter(1, value);
                    list= query.getResultList();

                    break;
                case "Descripción Larga Sociedad":
                    Query query0 = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy " +
                            "WHERE nsy.sociedad_larga LIKE ?");
                    query0.setParameter(1, value);

                    list= query0.getResultList();
                    break;
                case "Descripción Corta Sociedad":
                    Query query1 = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy " +
                            "WHERE nsy.sociedad_corta LIKE ?");
                    query1.setParameter(1, value);

                    list= query1.getResultList();
                    break;
                case "Divisa":
                    Query query2 = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy, nexco_divisas as di " +
                            "WHERE di.id_divisa=nsy.id_divisa AND di.nombre_divisa LIKE ?");
                    query2.setParameter(1, value);

                    list= query2.getResultList();
                    break;
                case "Mtdo. Consolidación IFRS":
                    Query query3 = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy, nexco_metodos_consolidacion as m " +
                            "WHERE m.id_metodo_ifrs=nsy.id_metodo_ifrs AND m.nombre_metodo_ifrs LIKE ?");
                    query3.setParameter(1, value);

                    list= query3.getResultList();
                    break;
                case "Grupo Consolidación IFRS":
                    Query query4 = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy, nexco_grupos_consolidacion as gc " +
                            "WHERE gc.id_grupo_ifrs=nsy.id_grupo_ifrs AND gc.nombre_grupo_ifrs LIKE ?");
                    query4.setParameter(1, value);

                    list= query4.getResultList();
                    break;
                case "País":
                    Query query5 = entityManager.createNativeQuery("SELECT RIGHT('00000' + Ltrim(Rtrim(nsy.yntp)),5) AS yntp,nsy.sociedad_corta,nsy.sociedad_larga,nsy.id_divisa, nsy.id_grupo_ifrs, nsy.id_metodo_ifrs, nsy.id_pais, nsy.tipo_entidad FROM nexco_sociedades_yntp as nsy, nexco_paises as p " +
                            "WHERE p.id_pais=nsy.id_pais AND p.nombre_pais LIKE ?");
                    query5.setParameter(1, value);

                    list= query5.getResultList();
                    break;
                default:
                    break;
            }

            return list;
        }
    }