package com.inter.proyecto_intergrupo.service.reportNIC34;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.reportNIC34.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.*;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

@Service
@Transactional
public class ReportNIC34ConsolService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private PygVistaNIC34ConsolRepository pygVistaNIC34Repository;

    @Autowired
    private BalanceVistaNIC34ConsolRepository balanceVistaNIC34Repository;

    @Autowired
    private NotasVistaNIC34ConsolRepository notasVistaNIC34Repository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("NIC34");
        insert.setFecha(today);
        insert.setInput("Reporte NIC34 Conslidado");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows,user);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Cargue Plantilla Query NIC34");
            else
                loadAudit(user,"Cargue Fallido Cargue Plantilla Query NIC34");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows,User user) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<QueryNIC34Consol> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        String validaFecont = null;
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    int columnCount = 0;
                    DataFormatter formatter = new DataFormatter();
                    String cellFecont = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellNucta = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    String cellMoneda = formatter.formatCellValue(row.getCell(columnCount++)).trim().toUpperCase();
                    String cellSaldoQuery = formatter.formatCellValue(row.getCell(columnCount++)).trim();
                    if(validaFecont == null)
                        validaFecont=cellFecont;

                    if (cellFecont.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Fecont debe estar en el formato yyyy-mm-dd. ";
                        lista.add(log);
                    }
                    if (!cellFecont.equals(validaFecont)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Fecont no corresponde con registros anteriores";
                        lista.add(log);
                    }
                    if (cellNucta.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Nucta no puede estar vacío.";
                        lista.add(log);
                    }
                    if (cellSaldoQuery.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo saldo query no puede estar vacío.";
                        lista.add(log);
                    }
                    else
                    {
                        try {
                            new BigDecimal(cellSaldoQuery.replaceAll(",", ""));
                        } catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(2);
                            log[2] = "El campo saldo query debe ser númerico decimal.";
                            lista.add(log);
                        }
                    }
                    if (!cellMoneda.equals("ML") && !cellMoneda.equals("ME")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(11);
                        log[2] = "El campo Moneda debe ser ML o ME.";
                        lista.add(log);
                    }

                    QueryNIC34Consol queryNIC34Consol = new QueryNIC34Consol();
                    queryNIC34Consol.setFECONT(cellFecont);
                    queryNIC34Consol.setNUCTA(cellNucta);
                    queryNIC34Consol.setMONEDA(cellMoneda);
                    try {
                        queryNIC34Consol.setSALDOQUERY(new BigDecimal(cellSaldoQuery.replaceAll(",", "")));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    toInsert.add(queryNIC34Consol);

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
        if (temp[2].equals("SUCCESS") && !toInsert.isEmpty()){
            deleteData(toInsert.get(0).getFECONT());
            insertQuery(toInsert);
        }
        toInsert.clear();
        return lista;
    }

    public String generatePeriodo(Object period)
    {
        if (period == null || period.toString() == "") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
            }
            return String.valueOf(calendar.get(Calendar.YEAR));

        }
        else {
            return period.toString();
        }
    }

    public String generateCorte(Object corte)
    {
        if (corte == null || corte.toString() == "") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);

            List<String> q1 = Arrays.asList("0", "1", "2");
            List<String> q2 = Arrays.asList("3", "4", "5");
            List<String> q3 = Arrays.asList("6", "7", "8");

            if(q1.contains(String.valueOf(calendar.get(Calendar.MONTH))))
            {
                return  "4Q";
            }
            else if(q2.contains(String.valueOf(calendar.get(Calendar.MONTH))))
            {
                return "1Q";
            }
            else if(q3.contains(String.valueOf(calendar.get(Calendar.MONTH))))
            {
                return "2Q";
            }
            else
            {
                return "3Q";
            }
        }
        else {
            return corte.toString();
        }
    }

    public void deleteData(String finalFecont)
    {
        Query validateFecont = entityManager.createNativeQuery("delete from nexco_query_nic34_consol where fecont = ? ");
        validateFecont.setParameter(1,finalFecont);
        validateFecont.executeUpdate();
    }

    public List<ParamFechas> getAllData(String corte, String periodo)
    {
        Query validateFecont = entityManager.createNativeQuery("select * from nexco_nic_fechas where q_aplica = ? and estado_consol ='CARGADO' and balance = 'X'",ParamFechas.class);
        validateFecont.setParameter(1,corte+"-"+periodo);
        return validateFecont.getResultList();
    }

    public List<BaseNIC34Consol> getData(String corte, String periodo)
    {
        Query validateFecont = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where q_aplica = ? order by nucta",BaseNIC34Consol.class);
        validateFecont.setParameter(1,corte+"-"+periodo);
        return validateFecont.getResultList();
    }

    public List<Object[]> getDataBaseBalance(String corte, String periodo)
    {
        Query validateFecont = entityManager.createNativeQuery("select fecont from nexco_base_nic34_consol where q_aplica = ? and balance='X' group by fecont");
        validateFecont.setParameter(1,corte+"-"+periodo);
        return validateFecont.getResultList();
    }

    public List<Object[]> getDataBasePyg(String corte, String periodo)
    {
        Query validateFecont = entityManager.createNativeQuery("select fecont from nexco_base_nic34_consol where q_aplica = ? and pyg='X' group by fecont");
        validateFecont.setParameter(1,corte+"-"+periodo);
        return validateFecont.getResultList();
    }

    public List<Object[]> getDataBaseGroup(String corte, String periodo,String aplica)
    {
        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        Query validateFecont = entityManager.createNativeQuery("select fecont,nucta,id_grupo,grupo,id_nota,nota,id_subnota,subnota,saldoquery,moneda from nexco_base_nic34_consol \n" +
                "where q_aplica = ? and balance = 'X' and id_grupo in (select id_g from nexco_nic34_informes where agrupa_2='EEFF' and id_g <> '' and aplica_query = ? and aplica = ? group by id_g) order by id_grupo");
        validateFecont.setParameter(1,corte+"-"+periodo);
        validateFecont.setParameter(2,aplica);
        validateFecont.setParameter(3,aplicaC);
        return validateFecont.getResultList();
    }

    public List<Object[]> getDataBaseGroupNotas(String corte, String periodo)
    {
        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";
        Query validateFecont = entityManager.createNativeQuery("select fecont,nucta,id_grupo,grupo,id_nota,nota,id_subnota,subnota,saldoquery,moneda from nexco_base_nic34_consol \n" +
                "where q_aplica = ? and balance = 'X' and id_nota in (select id_g from nexco_nic34_informes_notas where id_g <> '' and aplica = ? group by id_g) order by id_nota");
        validateFecont.setParameter(1,corte+"-"+periodo);
        validateFecont.setParameter(2,aplicaC);
        return validateFecont.getResultList();
    }

    public List<Object[]> getDataBalanceVista(String corte, String periodo)
    {
        Query data1 = entityManager.createNativeQuery("select id_nic34,id_grupo,moneda,signo,grupo,nota,saldo1,saldo2,variacion,total from nexco_balance_vista_nic34_consol where q_aplica = ? ");
        data1.setParameter(1,corte+"-"+periodo);
        return data1.getResultList();
    }

    public void getDataFilBalance(String corte, String periodo)
    {
        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        String qa=corte+"-"+periodo;
        Query data1 = entityManager.createNativeQuery("select a.id_nic34, a.id_g,a.moneda,a.signo,a.concepto,isnull(a.notas,'') as notas,case when isnull(a.signo,0)<0 then abs(isnull(b.saldo1,0))*-1 else abs(isnull(b.saldo1,0)) end as s1,\n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.saldo2,0))*-1 else abs(isnull(b.saldo2,0)) end as s2, case when isnull(a.signo,0)<0 then abs(isnull(b.variacion,0))*-1 else abs(isnull(b.variacion,0)) end as va\n" +
                "from (select * from nexco_nic34_informes where aplica_query='BALANCE' and agrupa_2='EEFF' and aplica = ?) a\n" +
                "left join (select *from nexco_balance_nic34_consol where q_aplica = ? ) b on a.id_g =b.id_grupo");
        data1.setParameter(1,aplicaC);
        data1.setParameter(2,qa);
        List<Object[]> listaTemp = data1.getResultList();

        List<BalanceVistaNIC34Consol> listInsert = new ArrayList<>();
        Double valor1 = 0.0;
        Double valor2 = 0.0;
        Double vari = 0.0;

        Double cvalor1 = 0.0;
        Double cvalor2 = 0.0;
        Double cvari = 0.0;

        int conteo = 0;

        for (Object[] temp: listaTemp)
        {
            try {
                BalanceVistaNIC34Consol toInsert = new BalanceVistaNIC34Consol();
                toInsert.setIdGrupo(temp[1].toString());
                toInsert.setMoneda(temp[2].toString());
                toInsert.setSigno(Double.parseDouble(temp[3].toString()));
                toInsert.setGrupo(temp[4].toString());
                toInsert.setQaplica(qa);
                toInsert.setNota(temp[5].toString());
                if(temp[1]!=null && temp[1].toString().length()!=1)
                {
                    toInsert.setSaldo1(Double.parseDouble(temp[6].toString()));
                    toInsert.setSaldo2(Double.parseDouble(temp[7].toString()));
                    toInsert.setVariacion(Double.parseDouble(temp[8].toString()));
                    toInsert.setTotal("0");

                    valor1 = valor1 + Double.parseDouble(temp[6].toString());
                    valor2 = valor2 + Double.parseDouble(temp[7].toString());
                    vari = vari + Double.parseDouble(temp[8].toString());
                }
                else
                {
                    if(conteo==3)
                    {
                        toInsert.setSaldo1(cvalor1);
                        toInsert.setSaldo2(cvalor2);
                        toInsert.setVariacion(cvari);
                        toInsert.setTotal("1");
                    }
                    else
                    {
                        toInsert.setSaldo1(valor1);
                        toInsert.setSaldo2(valor2);
                        toInsert.setVariacion(vari);
                        toInsert.setTotal("1");

                        if(conteo>0) {
                            cvalor1 = cvalor1 + valor1;
                            cvalor2 = cvalor2 + valor2;
                            cvari = cvari + vari;
                        }
                    }

                    valor1 = 0.0;
                    valor2 = 0.0;
                    vari = 0.0;
                    conteo++;
                }
                listInsert.add(toInsert);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Query feconts = entityManager.createNativeQuery("delete from nexco_balance_vista_nic34_consol where q_aplica = ? ");
        feconts.setParameter(1,corte+"-"+periodo);
        feconts.executeUpdate();

        balanceVistaNIC34Repository.saveAll(listInsert);
    }

    public void getDataFilNotas(String corte, String periodo, String aplica,String id, String filtro)
    {
        String columna ="1";
        String dataFiltro ="1";
        if(id!=null)
        {
            columna=id;
            dataFiltro=filtro;
        }

        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        String qa=corte+"-"+periodo;
        Query data1 = entityManager.createNativeQuery("select a.id_nic34,a.id_g,a.moneda,a.signo,a.concepto,\n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.saldo1,0))*-1 else abs(isnull(b.saldo1,0)) end as s1,\n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.saldo2,0))*-1 else abs(isnull(b.saldo2,0)) end as s2, \n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.variacion1,0))*-1 else abs(isnull(b.variacion1,0)) end as va1,\n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.variacion2,0))*-1 else abs(isnull(b.variacion2,0)) end as va2,\n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.saldo3,0))*-1 else abs(isnull(b.saldo3,0)) end as s3,\n" +
                "case when isnull(a.signo,0)<0 then abs(isnull(b.saldo4,0))*-1 else abs(isnull(b.saldo4,0)) end as s4,notas\n" +
                "from (select id_nic34,id_g,concepto, moneda,signo,notas from nexco_nic34_informes_notas where aplica_query = :aplica and aplica = :aplicaC and "+columna+" like :filtro group by id_nic34,id_g,concepto, moneda,signo,notas) a\n" +
                "left join (select id_nota,moneda,sum(saldo1) as saldo1,sum(saldo2) as saldo2,sum(variacion1) as variacion1,sum(variacion2) as variacion2,sum(saldo3) as saldo3,sum(saldo4) as saldo4 from nexco_notas_nic34_consol where q_aplica = :qa group by id_nota,moneda\n" +
                "union all\n" +
                "select id_nota,'MT' as moneda,sum(saldo1) as saldo1,sum(saldo2) as saldo2,sum(variacion1) as variacion1,sum(variacion2) as variacion2,sum(saldo3) as saldo3,sum(saldo4) as saldo4  from nexco_notas_nic34_consol where q_aplica = :qa group by id_nota) b \n" +
                "on a.id_g = b.id_nota and a.moneda = b.moneda");
        data1.setParameter("qa",qa);
        data1.setParameter("aplica",aplica);
        data1.setParameter("aplicaC",aplicaC);
        data1.setParameter("filtro",dataFiltro);
        List<Object[]> listaTemp = data1.getResultList();

        List<NotasVistaNIC34Consol> listInsert = new ArrayList<>();
        Double valor1 = 0.0;
        Double valor2 = 0.0;
        Double vari1 = 0.0;
        Double vari2 = 0.0;
        Double valor3 = 0.0;
        Double valor4 = 0.0;

        Double cvalor1 = 0.0;
        Double cvalor2 = 0.0;
        Double cvari1 = 0.0;
        Double cvari2 = 0.0;
        Double cvalor3 = 0.0;
        Double cvalor4 = 0.0;

        for (Object[] temp: listaTemp)
        {
            try {
                NotasVistaNIC34Consol toInsert = new NotasVistaNIC34Consol();
                toInsert.setIdNota(temp[1].toString());
                toInsert.setMoneda(temp[2].toString());
                toInsert.setSigno(Double.parseDouble(temp[3].toString()));
                toInsert.setNota(temp[4].toString());
                toInsert.setQaplica(qa);
                if(temp[11]!=null)
                    toInsert.setNotas(temp[11].toString());
                else
                    toInsert.setNotas("");
                if(temp[1]!=null && !temp[4].toString().contains("TOTAL") && temp[1].toString().equals(""))
                {
                    toInsert.setSaldo1(valor1);
                    toInsert.setSaldo2(valor2);
                    toInsert.setVariacion1(vari1);
                    toInsert.setVariacion2(vari2);
                    toInsert.setSaldo3(valor3);
                    toInsert.setSaldo4(valor4);
                    toInsert.setTotal("1");

                    cvalor1 = cvalor1 + valor1;
                    cvalor2 = cvalor2 + valor2;
                    cvari1 = cvari1 + vari1;
                    cvari2 = cvari2 + vari2;
                    cvalor3 = cvalor3 + valor4;
                    cvalor4 = cvalor4 + valor4;

                    valor1 = 0.0;
                    valor2 = 0.0;
                    vari1 = 0.0;
                    vari2 = 0.0;
                    valor3 = 0.0;
                    valor4 = 0.0;
                }
                else if(temp[4]!=null && temp[4].toString().contains("TOTAL"))
                {
                    toInsert.setSaldo1(cvalor1);
                    toInsert.setSaldo2(cvalor2);
                    toInsert.setVariacion1(cvari1);
                    toInsert.setVariacion2(cvari2);
                    toInsert.setSaldo3(cvalor3);
                    toInsert.setSaldo4(cvalor4);
                    toInsert.setTotal("1");

                    cvalor1 = 0.0;
                    cvalor2 = 0.0;
                    cvari1 = 0.0;
                    cvari2 = 0.0;
                    cvalor3 = 0.0;
                    cvalor4 = 0.0;
                }
                else
                {
                    toInsert.setSaldo1(Double.parseDouble(temp[5].toString()));
                    toInsert.setSaldo2(Double.parseDouble(temp[6].toString()));
                    toInsert.setVariacion1(Double.parseDouble(temp[7].toString()));
                    toInsert.setVariacion2(Double.parseDouble(temp[8].toString()));
                    toInsert.setSaldo3(Double.parseDouble(temp[9].toString()));
                    toInsert.setSaldo4(Double.parseDouble(temp[10].toString()));
                    toInsert.setTotal("0");

                    valor1 = valor1 + Double.parseDouble(temp[5].toString());
                    valor2 = valor2 + Double.parseDouble(temp[6].toString());
                    vari1 = vari1 + Double.parseDouble(temp[7].toString());
                    vari2 = vari2 + Double.parseDouble(temp[8].toString());
                    valor3 = valor3 + Double.parseDouble(temp[9].toString());
                    valor4 = valor4 + Double.parseDouble(temp[10].toString());
                }
                listInsert.add(toInsert);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Query feconts = entityManager.createNativeQuery("delete from nexco_notas_vista_nic34_consol where q_aplica = ? ");
        feconts.setParameter(1,corte+"-"+periodo);
        feconts.executeUpdate();

        notasVistaNIC34Repository.saveAll(listInsert);
    }

    public List<Object[]> getDataPygVista(String corte, String periodo)
    {
        Query data1 = entityManager.createNativeQuery("select id_nic34,id_grupo,moneda,signo,grupo,nota,saldo1,saldo2,variacion1,variacion2,saldo3,saldo4,total from nexco_pyg_vista_nic34_consol where q_aplica = ? ");
        data1.setParameter(1,corte+"-"+periodo);
        return data1.getResultList();
    }

    public List<Object[]> getDataNotasVista(String corte, String periodo)
    {
        Query data1 = entityManager.createNativeQuery("select id_nic34,id_nota,moneda,signo,nota,saldo1,saldo2,variacion1,total,variacion2,saldo3,saldo4,notas from nexco_notas_vista_nic34_consol where q_aplica = ? ");
        data1.setParameter(1,corte+"-"+periodo);
        return data1.getResultList();
    }

    public List<Object[]> getDataNotasVistaFiltro(String corte, String periodo,String id, String filtro)
    {
        String column = "1";
        switch (filtro)
        {
            case "ID":
                column = "id_nota";
                break;
            case "Moneda":
                column = "moneda";
                break;
            case "Signo":
                column = "signo";
                break;
            case "Grupo":
                column = "nota";
                break;
            case "Nota":
                column = "notas";
                break;
        }
        Query data1 = entityManager.createNativeQuery("select id_nic34,id_nota,moneda,signo,nota,saldo1,saldo2,variacion1,total,variacion2,saldo3,saldo4,notas from nexco_notas_vista_nic34_consol where q_aplica = ? and "+column+" like ?");
        data1.setParameter(1,corte+"-"+periodo);
        data1.setParameter(2,id);
        return data1.getResultList();
    }

    public void getDataFilPyg(String corte, String periodo)
    {
        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        String qa=corte+"-"+periodo;
        Query data1 = entityManager.createNativeQuery("select a.id_nic34, a.id_g,a.moneda,a.signo,a.concepto,a.notas,case when a.signo<0 then abs(isnull(b.saldo1,0))*-1 else abs(isnull(b.saldo1,0)) end as s1,\n" +
                "case when a.signo<0 then abs(isnull(b.saldo2,0))*-1 else abs(isnull(b.saldo2,0)) end as s2, case when a.signo<0 then abs(isnull(b.variacion1,0))*-1 else abs(isnull(b.variacion1,0)) end as v1,\n" +
                "case when a.signo<0 then abs(isnull(b.variacion2,0))*-1 else abs(isnull(b.variacion2,0)) end as v2,case when a.signo<0 then abs(isnull(b.saldo3,0))*-1 else abs(isnull(b.saldo3,0)) end as s3,\n" +
                "case when a.signo<0 then abs(isnull(b.saldo4,0))*-1 else abs(isnull(b.saldo4,0)) end as s4\n" +
                "from (select id_nic34,id_g,concepto,signo,moneda,isnull(notas,'') as notas from nexco_nic34_informes where agrupa_2='EEFF' and aplica_query='PYG' and aplica = ? ) a\n" +
                "left join (select * from nexco_pyg_nic34_consol where q_aplica = ?) b on a.id_g=b.id_grupo order by a.id_nic34");
        data1.setParameter(1,aplicaC);
        data1.setParameter(2,qa);
        List<Object[]> listaTemp = data1.getResultList();

        List<PygVistaNIC34Consol> listInsert = new ArrayList<>();

        Double valor1= 0.0,cvalor1= 0.0,c2valor1 = 0.0;
        Double valor2= 0.0,cvalor2= 0.0,c2valor2 = 0.0;
        Double vari1 = 0.0,cvari1 = 0.0,c2vari1 = 0.0;
        Double vari2 = 0.0,cvari2 = 0.0,c2vari2 = 0.0;
        Double valor3= 0.0,cvalor3= 0.0,c2valor3 = 0.0;
        Double valor4= 0.0,cvalor4= 0.0,c2valor4 = 0.0;

        int conteo = 0;

        for (Object[] temp: listaTemp)
        {
            try {
                PygVistaNIC34Consol toInsert = new PygVistaNIC34Consol();
                toInsert.setIdGrupo(temp[1].toString());
                toInsert.setMoneda(temp[2].toString());
                toInsert.setSigno(Double.parseDouble(temp[3].toString()));
                toInsert.setGrupo(temp[4].toString());
                toInsert.setQaplica(qa);
                toInsert.setNota(temp[5].toString());
                if(temp[1]!=null && !temp[1].toString().equals(""))
                {
                    toInsert.setSaldo1(Double.parseDouble(temp[6].toString()));
                    toInsert.setSaldo2(Double.parseDouble(temp[7].toString()));
                    toInsert.setVariacion1(Double.parseDouble(temp[8].toString()));
                    toInsert.setVariacion2(Double.parseDouble(temp[9].toString()));
                    toInsert.setSaldo3(Double.parseDouble(temp[10].toString()));
                    toInsert.setSaldo4(Double.parseDouble(temp[11].toString()));
                    toInsert.setTotal("0");

                    valor1 = valor1 + Double.parseDouble(temp[6].toString());
                    valor2 = valor2 + Double.parseDouble(temp[7].toString());
                    vari1 = vari1 + Double.parseDouble(temp[8].toString());
                    vari2 = vari2 + Double.parseDouble(temp[9].toString());
                    valor3 = valor3 + Double.parseDouble(temp[10].toString());
                    valor4 = valor4 + Double.parseDouble(temp[11].toString());
                }
                else
                {
                    if(conteo==2)
                    {
                        toInsert.setSaldo1(cvalor1+valor1);
                        toInsert.setSaldo2(cvalor2+valor2);
                        toInsert.setVariacion1(cvari1+vari1);
                        toInsert.setVariacion2(cvari2+vari2);
                        toInsert.setSaldo3(cvalor3+valor3);
                        toInsert.setSaldo4(cvalor4+valor4);
                        toInsert.setTotal("1");

                        cvalor1 = cvalor1 + valor1;
                        cvalor2 = cvalor2 + valor2;
                        cvari1 = cvari1 + vari1;
                        cvari2 = cvari2 + vari2;
                        cvalor3 = cvalor3 + valor3;
                        cvalor4 = cvalor4 + valor4;
                    }
                    else if(conteo==3)
                    {
                        toInsert.setSaldo1(cvalor1+valor1);
                        toInsert.setSaldo2(cvalor2+valor2);
                        toInsert.setVariacion1(cvari1+vari1);
                        toInsert.setVariacion2(cvari2+vari2);
                        toInsert.setSaldo3(cvalor3+valor3);
                        toInsert.setSaldo4(cvalor4+valor4);
                        toInsert.setTotal("1");

                        c2valor1 = cvalor1 + valor1;
                        c2valor2 = cvalor2 + valor2;
                        c2vari1 = cvari1 + vari1;
                        c2vari2 = cvari2 + vari2;
                        c2valor3 = cvalor3 + valor3;
                        c2valor4 = cvalor4 + valor4;
                    }
                    else if(conteo==5)
                    {
                        toInsert.setSaldo1(c2valor1 + valor1);
                        toInsert.setSaldo2(c2valor2 + valor2);
                        toInsert.setVariacion1(c2vari1 + vari1);
                        toInsert.setVariacion2(c2vari2 + vari2);
                        toInsert.setSaldo3(c2valor3 + valor3);
                        toInsert.setSaldo4(c2valor4 + valor4);
                        toInsert.setTotal("1");

                        c2valor1 = c2valor1 + valor1;
                        c2valor2 = c2valor2 + valor2;
                        c2vari1 = c2vari1 + vari1;
                        c2vari2 = c2vari2 + vari2;
                        c2valor3 = c2valor3 + valor3;
                        c2valor4 = c2valor4 + valor4;
                    }
                    else if(conteo==6)
                    {
                        toInsert.setSaldo1(c2valor1 + valor1);
                        toInsert.setSaldo2(c2valor2 + valor2);
                        toInsert.setVariacion1(c2vari1 + vari1);
                        toInsert.setVariacion2(c2vari2 + vari2);
                        toInsert.setSaldo3(c2valor3 + valor3);
                        toInsert.setSaldo4(c2valor4 + valor4);
                        toInsert.setTotal("1");
                    }
                    else
                    {
                        toInsert.setSaldo1(valor1);
                        toInsert.setSaldo2(valor2);
                        toInsert.setVariacion1(vari1);
                        toInsert.setVariacion2(vari2);
                        toInsert.setSaldo3(valor3);
                        toInsert.setSaldo4(valor4);
                        toInsert.setTotal("1");

                        cvalor1 = cvalor1 + valor1;
                        cvalor2 = cvalor2 + valor2;
                        cvari1 = cvari1 + vari1;
                        cvari2 = cvari2 + vari2;
                        cvalor3 = cvalor3 + valor3;
                        cvalor4 = cvalor4 + valor4;

                        if(conteo==4)
                        {
                            c2valor1 = c2valor1 + valor1;
                            c2valor2 = c2valor2 + valor2;
                            c2vari1 = c2vari1 + vari1;
                            c2vari2 = c2vari2 + vari2;
                            c2valor3 = c2valor3 + valor3;
                            c2valor4 = c2valor4 + valor4;
                        }
                    }

                    valor1 = 0.0;
                    valor2 = 0.0;
                    vari1 = 0.0;
                    vari2 = 0.0;
                    valor3 = 0.0;
                    valor4 = 0.0;
                    conteo++;
                }
                listInsert.add(toInsert);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        Query feconts = entityManager.createNativeQuery("delete from nexco_pyg_vista_nic34_consol where q_aplica = ? ");
        feconts.setParameter(1,corte+"-"+periodo);
        feconts.executeUpdate();

        pygVistaNIC34Repository.saveAll(listInsert);
    }

    public List<String> getDataFecontBalance(String corte, String periodo)
    {
        Query feconts = entityManager.createNativeQuery("select fecont from nexco_base_nic34_consol where q_aplica = ? and balance='X' group by fecont order by 1 desc");
        feconts.setParameter(1,corte+"-"+periodo);
        return feconts.getResultList();
    }

    public List<String> getDataFecontNotas(String corte, String periodo)
    {
        Query resultado = entityManager.createNativeQuery("select case when sum(saldo3) = 0 then 'SI' else 'NO' end as result from nexco_notas_vista_nic34_consol where q_aplica = ? ");
        resultado.setParameter(1,corte+"-"+periodo);
        List<String> lista =  resultado.getResultList();

        if(lista.get(0).equals("NO")){
            Query feconts = entityManager.createNativeQuery("select fecont from nexco_base_nic34_consol where q_aplica = ? and pyg='X' group by fecont order by 1 desc");
            feconts.setParameter(1, corte + "-" + periodo);
            return feconts.getResultList();
        }
        else{
            Query feconts = entityManager.createNativeQuery("select fecont from nexco_base_nic34_consol where q_aplica = ? and balance='X' group by fecont order by 1 desc");
            feconts.setParameter(1, corte + "-" + periodo);
            return feconts.getResultList();
        }
    }

    public List<String> getDataFecontPyg(String corte, String periodo)
    {
        Query feconts = entityManager.createNativeQuery("select fecont from nexco_base_nic34_consol where q_aplica = ? and pyg='X' group by fecont order by 1 desc");
        feconts.setParameter(1,corte+"-"+periodo);
        return feconts.getResultList();
    }

    public void generateDataBalance(String corte, String periodo, String puntaje)
    {
        String valor="1000000";
        if(puntaje.equals("Miles"))
            valor="1000";
        else if(puntaje.equals("Pesos"))
            valor="1";

        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        Query deletes = entityManager.createNativeQuery("delete from nexco_balance_nic34_consol where q_aplica = ?");
        deletes.setParameter(1,corte+"-"+periodo);
        deletes.executeUpdate();

        List<String> list = getDataFecontBalance(corte,periodo);

        Query validateFecont = entityManager.createNativeQuery("insert into nexco_balance_nic34_consol (id_grupo,grupo,saldo1,saldo2,variacion,q_aplica) \n" +
                "(select z.id_grupo, z.grupo, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(b.saldo,0)))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_grupo,grupo from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_grupo,grupo) z\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' group by id_grupo,grupo) a \n" +
                "on z.id_grupo=a.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' group by id_grupo,grupo) b \n" +
                "on a.id_grupo=b.id_grupo where z.id_grupo in (select id_g from nexco_nic34_informes where agrupa_2='EEFF' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' and aplica_query = 'BALANCE' group by id_g)\n" +
                "group by z.id_grupo, z.grupo\n" +
                "union all\n" +
                "select z.id_grupo, z.grupo, case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) else 0 end  as saldo1, \n" +
                " case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) else 0 end as saldo2,\n" +
                "round(((case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)) else 0 end)-(case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)) else 0 end))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_grupo,grupo from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' and id_grupo in ('A13','B13') group by id_grupo,grupo) z\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'A13' and balance = 'X' group by id_grupo,grupo) a \n" +
                "on z.id_grupo=a.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'A13' and balance = 'X' group by id_grupo,grupo) b \n" +
                "on z.id_grupo=b.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'B13' and balance = 'X' group by id_grupo,grupo) c\n" +
                "on z.id_grupo!=c.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'B13' and balance = 'X' group by id_grupo,grupo) d\n" +
                "on z.id_grupo!=d.id_grupo where z.id_grupo = 'A13'\n" +
                "group by z.id_grupo, z.grupo\n" +
                "union all\n" +
                "select z.id_grupo, z.grupo, case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then 0 else round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) end  as saldo1, \n" +
                "case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then 0 else round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) end as saldo2,\n" +
                "round(((case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then 0 else sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)) end)-( case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then 0 else sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)) end))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_grupo,grupo from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' and id_grupo in ('A13','B13') group by id_grupo,grupo) z\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'A13' and balance = 'X' group by id_grupo,grupo) a \n" +
                "on z.id_grupo!=a.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'A13' and balance = 'X' group by id_grupo,grupo) b \n" +
                "on z.id_grupo!=b.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'B13' and balance = 'X' group by id_grupo,grupo) c\n" +
                "on z.id_grupo=c.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'B13' and balance = 'X' group by id_grupo,grupo) d\n" +
                "on z.id_grupo=d.id_grupo where z.id_grupo = 'B13'\n" +
                "group by z.id_grupo, z.grupo\n" +
                "union all\n" +
                "select z.id_grupo, z.grupo, case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) else 0 end  as saldo1, \n" +
                " case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) else 0 end as saldo2,\n" +
                "round(((case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)) else 0 end)-(case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)) else 0 end))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_grupo,grupo from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' and id_grupo in ('A14','B14') group by id_grupo,grupo) z\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'A14' and balance = 'X' group by id_grupo,grupo) a \n" +
                "on z.id_grupo=a.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'A14' and balance = 'X' group by id_grupo,grupo) b \n" +
                "on z.id_grupo=b.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'B14' and balance = 'X' group by id_grupo,grupo) c\n" +
                "on z.id_grupo!=c.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'B14' and balance = 'X' group by id_grupo,grupo) d\n" +
                "on z.id_grupo!=d.id_grupo where z.id_grupo = 'A14'\n" +
                "group by z.id_grupo, z.grupo\n" +
                "union all\n" +
                "select z.id_grupo, z.grupo, case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then 0 else round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) end  as saldo1, \n" +
                "case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then 0 else round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) end as saldo2,\n" +
                "round(((case when sum(isnull(a.saldo,0)) > sum(isnull(c.saldo,0)) then 0 else sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)) end)-( case when sum(isnull(b.saldo,0)) > sum(isnull(d.saldo,0)) then 0 else sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)) end))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_grupo,grupo from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' and id_grupo in ('A14','B14') group by id_grupo,grupo) z\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'A14' and balance = 'X' group by id_grupo,grupo) a \n" +
                "on z.id_grupo!=a.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'A14' and balance = 'X' group by id_grupo,grupo) b \n" +
                "on z.id_grupo!=b.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and id_grupo = 'B14' and balance = 'X' group by id_grupo,grupo) c\n" +
                "on z.id_grupo=c.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and id_grupo = 'B14' and balance = 'X' group by id_grupo,grupo) d\n" +
                "on z.id_grupo=d.id_grupo where z.id_grupo = 'B14'\n" +
                "group by z.id_grupo, z.grupo\n" +
                "union all\n" +
                "select 'C6' as id_grupo, 'Resultado del ejercicio' as grupo, round((select sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' and substring(nucta,1,1) in ('4','5'))/"+valor+",0) as saldo1,\n" +
                "round((select sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' and substring(nucta,1,1) in ('4','5'))/"+valor+",0) as saldo2, \n" +
                "round(((select sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' and substring(nucta,1,1) in ('4','5'))-(select sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' and substring(nucta,1,1) in ('4','5')))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select top 1 * from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X') z)");
        validateFecont.setParameter("qa",corte+"-"+periodo);
        validateFecont.setParameter("periodo1",list.get(0));
        validateFecont.setParameter("periodo2",list.get(1));
        validateFecont.setParameter("aplicaC",aplicaC);
        validateFecont.executeUpdate();
    }

    public void generateDataNotas(String corte, String periodo, String puntaje, String aplica)
    {
        String valor="1000000";
        if(puntaje.equals("Miles"))
            valor="1000";
        else if(puntaje.equals("Pesos"))
            valor="1";

        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        Query deletes = entityManager.createNativeQuery("delete from nexco_notas_nic34_consol where q_aplica = ?");
        deletes.setParameter(1,corte+"-"+periodo);
        deletes.executeUpdate();

        List<String> list = getDataFecontBalance(corte,periodo);
        List<String> list2 = getDataFecontPyg(corte,periodo);

        Query validateFecont = entityManager.createNativeQuery("insert into nexco_notas_nic34_consol (id_nota,nota,moneda,saldo1,saldo2,variacion1,q_aplica) \n" +
                "(select z.id_nota,z.nota,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(b.saldo,0)))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_nota,nota,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_nota,nota,id_subnota,subnota,moneda) z\n" +
                "left join (select id_nota,nota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' group by id_nota,nota,moneda) a \n" +
                "on z.id_nota=a.id_nota and z.moneda=a.moneda\n" +
                "left join (select id_nota,nota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' group by id_nota,nota,moneda) b \n" +
                "on z.id_nota=b.id_nota and z.moneda=b.moneda where z.id_nota in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_NOTA' and aplica = :aplicaC and id_g <> '' and condicion = 'SALDO' group by id_g)\n" +
                "group by z.id_nota,z.nota,z.moneda\n" +
                "union all\n" +
                "select z.id_subnota,z.subnota,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(b.saldo,0)))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_subnota,subnota,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_subnota,subnota,id_subnota,subnota,moneda) z\n" +
                "left join (select id_subnota,subnota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' group by id_subnota,subnota,moneda) a \n" +
                "on z.id_subnota=a.id_subnota and z.moneda=a.moneda\n" +
                "left join (select id_subnota,subnota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' group by id_subnota,subnota,moneda) b \n" +
                "on z.id_subnota=b.id_subnota and z.moneda=b.moneda where z.id_subnota in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_SUBNOTA' and aplica = :aplicaC and id_g <> '' and condicion = 'SALDO' group by id_g)\n" +
                "group by z.id_subnota,z.subnota,z.moneda\n" +
                "union all\n" +
                "select z.id_campo,z.campo,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(b.saldo,0)))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_campo,campo,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_campo,campo,id_campo,campo,moneda) z\n" +
                "left join (select id_campo,campo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' group by id_campo,campo,moneda) a \n" +
                "on z.id_campo=a.id_campo and z.moneda=a.moneda\n" +
                "left join (select id_campo,campo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' group by id_campo,campo,moneda) b \n" +
                "on z.id_campo=b.id_campo and z.moneda=b.moneda where z.id_campo in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_CAMPO' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' group by id_g)\n" +
                "group by z.id_campo,z.campo,z.moneda\n" +
                "union all\n" +
                "select z.id_grupo,z.grupo,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(b.saldo,0)))/"+valor+",0) as vari, :qa as qa from \n" +
                "(select id_grupo,grupo,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_grupo,grupo,id_grupo,grupo,moneda) z\n" +
                "left join (select id_grupo,grupo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and balance = 'X' group by id_grupo,grupo,moneda) a \n" +
                "on z.id_grupo=a.id_grupo and z.moneda=a.moneda\n" +
                "left join (select id_grupo,grupo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and balance = 'X' group by id_grupo,grupo,moneda) b \n" +
                "on z.id_grupo=b.id_grupo and z.moneda=b.moneda where z.id_grupo in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_GRUPO' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' group by id_g)\n" +
                "group by z.id_grupo,z.grupo,z.moneda)");
        validateFecont.setParameter("qa",corte+"-"+periodo);
        validateFecont.setParameter("periodo1",list.get(0));
        validateFecont.setParameter("periodo2",list.get(1));
        validateFecont.setParameter("aplicaC",aplicaC);

        Query validateFecont2 = entityManager.createNativeQuery("insert into nexco_notas_nic34_consol (id_nota,nota,moneda,saldo1,saldo2,variacion1,variacion2,saldo3,saldo4,q_aplica) \n" +
                "(select z.id_nota,z.nota,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) as vari1, \n" +
                "round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) as vari2, round(sum(isnull(c.saldo,0))/"+valor+",0) as saldo3, round(sum(isnull(d.saldo,0))/"+valor+",0) as saldo4, :qa as qa from \n" +
                "(select id_nota,nota,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_nota,nota,id_subnota,subnota,moneda) z \n" +
                "left join (select id_nota,nota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and pyg = 'X' group by id_nota,nota,moneda) a \n" +
                "on z.id_nota=a.id_nota and z.moneda=a.moneda \n" +
                "left join (select id_nota,nota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and pyg = 'X' group by id_nota,nota,moneda) b \n" +
                "on z.id_nota=b.id_nota and z.moneda=b.moneda \n" +
                "left join (select id_nota,nota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo3 and pyg = 'X' group by id_nota,nota,moneda) c \n" +
                "on z.id_nota=c.id_nota and z.moneda=c.moneda \n" +
                "left join (select id_nota,nota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo4 and pyg = 'X' group by id_nota,nota,moneda) d \n" +
                "on z.id_nota=d.id_nota and z.moneda=d.moneda \n" +
                "where z.id_nota in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_NOTA' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' group by id_g) \n" +
                "group by z.id_nota,z.nota,z.moneda \n" +
                "union all \n" +
                "select z.id_subnota,z.subnota,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) as vari1, \n" +
                "round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) as vari2, round(sum(isnull(c.saldo,0))/"+valor+",0) as saldo3, round(sum(isnull(d.saldo,0))/"+valor+",0) as saldo4, :qa as qa from \n" +
                "(select id_subnota,subnota,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_subnota,subnota,id_subnota,subnota,moneda) z \n" +
                "left join (select id_subnota,subnota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and pyg = 'X' group by id_subnota,subnota,moneda) a \n" +
                "on z.id_subnota=a.id_subnota and z.moneda=a.moneda \n" +
                "left join (select id_subnota,subnota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and pyg = 'X' group by id_subnota,subnota,moneda) b \n" +
                "on z.id_subnota=b.id_subnota and z.moneda=b.moneda \n" +
                "left join (select id_subnota,subnota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo3 and pyg = 'X' group by id_subnota,subnota,moneda) c \n" +
                "on z.id_subnota=c.id_subnota and z.moneda=c.moneda \n" +
                "left join (select id_subnota,subnota,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo4 and pyg = 'X' group by id_subnota,subnota,moneda) d \n" +
                "on z.id_subnota=d.id_subnota and z.moneda=d.moneda \n" +
                "where z.id_subnota in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_SUBNOTA' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' group by id_g) \n" +
                "group by z.id_subnota,z.subnota,z.moneda \n" +
                "union all \n" +
                "select z.id_campo,z.campo,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) as vari1, \n" +
                "round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) as vari2, round(sum(isnull(c.saldo,0))/"+valor+",0) as saldo3, round(sum(isnull(d.saldo,0))/"+valor+",0) as saldo4, :qa as qa from \n" +
                "(select id_campo,campo,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_campo,campo,id_campo,campo,moneda) z \n" +
                "left join (select id_campo,campo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and pyg = 'X' group by id_campo,campo,moneda) a \n" +
                "on z.id_campo=a.id_campo and z.moneda=a.moneda \n" +
                "left join (select id_campo,campo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and pyg = 'X' group by id_campo,campo,moneda) b \n" +
                "on z.id_campo=b.id_campo and z.moneda=b.moneda \n" +
                "left join (select id_campo,campo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo3 and pyg = 'X' group by id_campo,campo,moneda) c \n" +
                "on z.id_campo=c.id_campo and z.moneda=c.moneda \n" +
                "left join (select id_campo,campo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo4 and pyg = 'X' group by id_campo,campo,moneda) d \n" +
                "on z.id_campo=d.id_campo and z.moneda=d.moneda \n" +
                "where z.id_campo in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_CAMPO' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' group by id_g) \n" +
                "group by z.id_campo,z.campo,z.moneda \n" +
                "union all \n" +
                "select z.id_grupo,z.grupo,z.moneda, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo2,round((sum(isnull(a.saldo,0))-sum(isnull(c.saldo,0)))/"+valor+",0) as vari1, \n" +
                "round((sum(isnull(b.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) as vari2, round(sum(isnull(c.saldo,0))/"+valor+",0) as saldo3, round(sum(isnull(d.saldo,0))/"+valor+",0) as saldo4, :qa as qa from \n" +
                "(select id_grupo,grupo,moneda from nexco_base_nic34_consol where q_aplica = :qa and balance = 'X' group by id_grupo,grupo,id_grupo,grupo,moneda) z \n" +
                "left join (select id_grupo,grupo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and pyg = 'X' group by id_grupo,grupo,moneda) a \n" +
                "on z.id_grupo=a.id_grupo and z.moneda=a.moneda \n" +
                "left join (select id_grupo,grupo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and pyg = 'X' group by id_grupo,grupo,moneda) b \n" +
                "on z.id_grupo=b.id_grupo and z.moneda=b.moneda \n" +
                "left join (select id_grupo,grupo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo3 and pyg = 'X' group by id_grupo,grupo,moneda) c \n" +
                "on z.id_grupo=c.id_grupo and z.moneda=c.moneda \n" +
                "left join (select id_grupo,grupo,moneda,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo4 and pyg = 'X' group by id_grupo,grupo,moneda) d \n" +
                "on z.id_grupo=d.id_grupo and z.moneda=d.moneda \n" +
                "where z.id_grupo in (select id_g from nexco_nic34_informes_notas where agrupa_1='ID_GRUPO' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' group by id_g) \n" +
                "group by z.id_grupo,z.grupo,z.moneda)");
        validateFecont2.setParameter("qa",corte+"-"+periodo);
        validateFecont2.setParameter("periodo1",list2.get(0));
        validateFecont2.setParameter("periodo2",list2.get(1));
        validateFecont2.setParameter("periodo3",list2.get(2));
        validateFecont2.setParameter("periodo4",list2.get(3));
        validateFecont2.setParameter("aplicaC",aplicaC);

        if(aplica.equals("BALANCE"))
            validateFecont.executeUpdate();
        else if (aplica.equals("PYG"))
            validateFecont2.executeUpdate();
    }

    public void generateDataPyg(String corte, String periodo, String puntaje)
    {
        String valor="1000000";
        if(puntaje.equals("Miles"))
            valor="1000";
        else if(puntaje.equals("Pesos"))
            valor="1";

        String aplicaC="T";
        if(corte.equals("4Q"))
            aplicaC="A";

        Query deletes = entityManager.createNativeQuery("delete from nexco_pyg_nic34_consol where q_aplica = ?");
        deletes.setParameter(1,corte+"-"+periodo);
        deletes.executeUpdate();

        List<String> list = getDataFecontPyg(corte,periodo);

        Query validateFecont = entityManager.createNativeQuery("insert into nexco_pyg_nic34_consol (id_grupo,grupo,saldo1,saldo2,variacion1,variacion2,saldo3,saldo4,q_aplica) \n" +
                "(select z.id_grupo, z.grupo, round(sum(isnull(a.saldo,0))/"+valor+",0) as saldo1, round(sum(isnull(c.saldo,0))/"+valor+",0) as saldo2,\n" +
                "round((sum(isnull(a.saldo,0))-sum(isnull(b.saldo,0)))/"+valor+",0) as vari1,round((sum(isnull(c.saldo,0))-sum(isnull(d.saldo,0)))/"+valor+",0) as vari1,\n" +
                "round(sum(isnull(b.saldo,0))/"+valor+",0) as saldo3,round(sum(isnull(d.saldo,0))/"+valor+",0) as saldo4, :qa as qa from \n" +
                "(select id_grupo,grupo from nexco_base_nic34_consol where q_aplica = :qa and pyg = 'X' group by id_grupo,grupo) z\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo1 and pyg = 'X' group by id_grupo,grupo) a \n" +
                "on z.id_grupo=a.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo2 and pyg = 'X' group by id_grupo,grupo) b \n" +
                "on a.id_grupo=b.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo3 and pyg = 'X' group by id_grupo,grupo) c \n" +
                "on a.id_grupo=c.id_grupo\n" +
                "left join (select id_grupo,grupo,sum(saldoquery) as saldo from nexco_base_nic34_consol where q_aplica = :qa and fecont = :periodo4 and pyg = 'X' group by id_grupo,grupo) d \n" +
                "on a.id_grupo=d.id_grupo\n" +
                "where z.id_grupo in (select id_g from nexco_nic34_informes where agrupa_2='EEFF' and id_g <> '' and aplica = :aplicaC and condicion = 'SALDO' and aplica_query = 'PYG' group by id_g)\n" +
                "group by z.id_grupo,z.grupo)");
        validateFecont.setParameter("qa",corte+"-"+periodo);
        validateFecont.setParameter("periodo1",list.get(0));
        validateFecont.setParameter("periodo2",list.get(1));
        validateFecont.setParameter("periodo3",list.get(2));
        validateFecont.setParameter("periodo4",list.get(3));
        validateFecont.setParameter("aplicaC",aplicaC);
        validateFecont.executeUpdate();
    }

    public List<Object[]> validateData(String corte, String periodo) {
        Query validateFecont = entityManager.createNativeQuery("select distinct z.nucta,x.responsable from (select a.* from nexco_query_nic34_consol a where saldoquery <> 0 ) z\n" +
                "inner join (select * from nexco_nic_fechas where q_aplica = ? and estado_consol ='CARGADO') y on z.fecont = y.fecont\n" +
                "left join (select cuenta,moneda,id_grupo,responsable from nexco_param_nic34 union select cuenta,moneda,id_grupo,responsable from nexco_param_nic34_consol) x on z.nucta = x.cuenta and z.moneda = x.moneda where x.id_grupo is null");
        validateFecont.setParameter(1, corte + "-" + periodo);
        return validateFecont.getResultList();
    }

    public void processData(String corte, String periodo)
    {
        Query validateFecont = entityManager.createNativeQuery("delete from nexco_base_nic34_consol where q_aplica = ? ;\n" +
                "insert into nexco_base_nic34_consol (fecont,l6,nucta,id_grupo,grupo,aplica,signo,id_nota,nota,id_subnota,subnota,id_campo,campo,moneda,saldoquery,balance,pyg,q_aplica)\n" +
                "(select z.fecont,x.l6,z.nucta,x.id_grupo,x.grupo,x.aplica,x.signo,x.id_nota,x.nota,x.id_subnota,x.subnota,x.id_campo,x.campo,z.moneda,(z.saldoquery * x.signo) as saldoquery,y.balance,y.pyg,y.q_aplica from (select a.* from nexco_query_nic34_consol a where saldoquery <> 0 ) z\n" +
                "inner join (select * from nexco_nic_fechas where q_aplica = ? and estado_consol ='CARGADO') y on z.fecont = y.fecont\n" +
                "inner join (select l6,grupo,aplica,signo,id_nota,nota,id_subnota,subnota,id_campo,campo,cuenta,moneda,id_grupo,responsable from \n" +
                "(select l6,grupo,aplica,signo,id_nota,nota,id_subnota,subnota,id_campo,campo,cuenta,moneda,id_grupo,responsable from nexco_param_nic34 \n" +
                "union \n" +
                "select l6,grupo,aplica,signo,id_nota,nota,id_subnota,subnota,id_campo,campo,cuenta,moneda,id_grupo,responsable from nexco_param_nic34_consol) y\n" +
                "group by l6,grupo,aplica,signo,id_nota,nota,id_subnota,subnota,id_campo,campo,cuenta,moneda,id_grupo,responsable) x on z.nucta = x.cuenta and z.moneda = x.moneda\n" +
                "group by z.fecont,x.l6,z.nucta,x.id_grupo,x.grupo,x.aplica,x.signo,x.id_nota,x.nota,x.id_subnota,x.subnota,x.id_campo,x.campo,z.moneda,(z.saldoquery * x.signo),y.balance,y.pyg,y.q_aplica);");
        validateFecont.setParameter(1,corte+"-"+periodo);
        validateFecont.setParameter(2,corte+"-"+periodo);
        validateFecont.executeUpdate();
    }

    public void insertQuery(List<QueryNIC34Consol> temporal) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_query_nic34_consol(NUCTA,FECONT,SALDOQUERY,MONEDA) VALUES (?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getNUCTA());
                        ps.setString(2, temporal.get(i).getFECONT());
                        ps.setBigDecimal(3, temporal.get(i).getSALDOQUERY());
                        ps.setString(4, temporal.get(i).getMONEDA());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public List<BaseNIC34Consol> findByFilter(String value, String filter, String corte, String periodo) {
        List<BaseNIC34Consol> list=new ArrayList<BaseNIC34Consol>();
        switch (filter)
        {
            case "Fecont":
                Query validate0 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where fecont like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate0.setParameter(1,"%"+value+"%");
                validate0.setParameter(2,corte+"-"+periodo);
                list=validate0.getResultList();
                break;
            case "L6":
                Query validate1 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where l6 like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate1.setParameter(1,"%"+value+"%");
                validate1.setParameter(2,corte+"-"+periodo);
                list=validate1.getResultList();
                break;
            case "Nucta":
                Query validate2 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where nucta like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate2.setParameter(1,"%"+value+"%");
                validate2.setParameter(2,corte+"-"+periodo);
                list=validate2.getResultList();
                break;
            case "ID Grupo":
                Query validate3 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where id_grupo like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate3.setParameter(1,"%"+value+"%");
                validate3.setParameter(2,corte+"-"+periodo);
                list=validate3.getResultList();
                break;
            case "Grupo":
                Query validate4 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where grupo like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate4.setParameter(1,"%"+value+"%");
                validate4.setParameter(2,corte+"-"+periodo);
                list=validate4.getResultList();
                break;
            case "Aplica":
                Query validate5 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where aplica like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate5.setParameter(1,"%"+value+"%");
                validate5.setParameter(2,corte+"-"+periodo);
                list=validate5.getResultList();
                break;
            case "Signo":
                Query validate6 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where signo like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate6.setParameter(1,"%"+value+"%");
                validate6.setParameter(2,corte+"-"+periodo);
                list=validate6.getResultList();
                break;
            case "ID Nota":
                Query validate7 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where id_nota like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate7.setParameter(1,"%"+value+"%");
                validate7.setParameter(2,corte+"-"+periodo);
                list=validate7.getResultList();
                break;
            case "Nota":
                Query validate8 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where nota like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate8.setParameter(1,"%"+value+"%");
                validate8.setParameter(2,corte+"-"+periodo);
                list=validate8.getResultList();
                break;
            case "ID SubNota":
                Query validate9 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where id_subnota like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate9.setParameter(1,"%"+value+"%");
                validate9.setParameter(2,corte+"-"+periodo);
                list=validate9.getResultList();
                break;
            case "SubNota":
                Query validate10 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where subnota like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate10.setParameter(1,"%"+value+"%");
                validate10.setParameter(2,corte+"-"+periodo);
                list=validate10.getResultList();
                break;
            case "Moneda":
                Query validate11 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where moneda like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate11.setParameter(1,"%"+value+"%");
                validate11.setParameter(2,corte+"-"+periodo);
                list=validate11.getResultList();
                break;
            case "Saldo":
                Query validate12 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where saldoquery like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate12.setParameter(1,"%"+value+"%");
                validate12.setParameter(2,corte+"-"+periodo);
                list=validate12.getResultList();
                break;
            case "Balance":
                Query validate13 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where balance like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate13.setParameter(1,"%"+value+"%");
                validate13.setParameter(2,corte+"-"+periodo);
                list=validate13.getResultList();
                break;
            case "PYG":
                Query validate14 = entityManager.createNativeQuery("select * from nexco_base_nic34_consol where pyg like ? and q_aplica = ?",BaseNIC34Consol.class);
                validate14.setParameter(1,"%"+value+"%");
                validate14.setParameter(2,corte+"-"+periodo);
                list=validate14.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}