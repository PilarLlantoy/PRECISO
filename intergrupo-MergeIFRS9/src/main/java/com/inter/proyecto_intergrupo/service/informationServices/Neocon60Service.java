package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Neocon60;
import com.inter.proyecto_intergrupo.model.information.Neocon60Ajuste;
import com.inter.proyecto_intergrupo.model.information.Neocon60Carga;
import com.inter.proyecto_intergrupo.model.information.Neocon60Cuadre;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamMDA;
import com.inter.proyecto_intergrupo.model.reportNIC34.QueryNIC34;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.Neocon60AjusteRepository;
import com.inter.proyecto_intergrupo.repository.ifrs9.Neocon60CargaRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.repository.reportNIC34.ParamMDARepository;
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
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Transactional
public class Neocon60Service{

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private Neocon60AjusteRepository neocon60AjusteRepository;

    @Autowired
    private Neocon60CargaRepository neocon60CargaRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Informacionales");
        insert.setFecha(today);
        insert.setInput("Neocon 60");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<Neocon60> findAll(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_neocon60 where periodo = ? order by codicons, divisa,yntp_intergrupo", Neocon60.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<Neocon60Cuadre> findAllCuadre(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_neocon60_cuadre where periodo = ? order by codicons,divisa", Neocon60Cuadre.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<Neocon60> findAllBase(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_neocon60_base where periodo = ? order by codicons", Neocon60.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<Neocon60Ajuste> findAllAjuste(String periodo){
        Query consulta = entityManager.createNativeQuery("select * from nexco_neocon60_ajuste where periodo = ? order by codicons", Neocon60Ajuste.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<Object[]> findAllCarga(String periodo){
        Query consulta = entityManager.createNativeQuery("select ano,mes,codicons,divisa_espana,sum(saldo) as saldo from nexco_neocon60_carga_masiva where periodo = ? group by ano,mes,codicons,divisa_espana order by codicons");
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public void cleanData(String periodo){
        Query consulta = entityManager.createNativeQuery("delete from nexco_neocon60 where periodo = ?");
        consulta.setParameter(1,periodo);
        consulta.executeUpdate();
    }

    public void generateSubContingentes(String periodo){

        String nameTable ="";
        String tempValue ="";
        Query verify = entityManager.createNativeQuery("SELECT TOP 1 * FROM nexco_query_marcados WHERE origen = 'LOCAL' AND fecont LIKE ? ");
        verify.setParameter(1,periodo+"%");

        if(verify.getResultList().isEmpty())
        {
            nameTable ="nexco_query";
            tempValue ="";
        }
        else
        {
            nameTable ="nexco_query_marcados";
            tempValue ="origen = 'LOCAL' AND";
        }

        Query consulta = entityManager.createNativeQuery("drop table nexco_neocon60_contingentes; " +
                "SELECT a.cuenta_local, ISNULL(ISNULL(b.id_divisa, C.coddiv),'-') divisa, ABS(CAST(ISNULL(b.saldo_divisa, 0) AS FLOAT)) saldo_divisa,ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT)) saldo_pesos,\n" +
                "abs(CAST(ISNULL(c.salmesd, 0) AS FLOAT)) salmesd, abs(CAST(ISNULL(c.salmes, 0) AS FLOAT)) salmes, \n" +
                "CAST(ABS(ISNULL(b.saldo_divisa, 0))-abs(isnull(c.salmesd, 0)) AS FLOAT) diferencia_divisa, \n" +
                "CAST(ABS(CAST(ISNULL(b.saldo_pesos, 0) AS FLOAT))-abs(isnull(c.salmes, 0)) AS FLOAT) diferencia_pesos,c.fechproce into nexco_neocon60_contingentes \n" +
                "FROM nexco_cuentas_responsables a\n" +
                "LEFT JOIN (SELECT cta_contable_60 cuenta_contable, divisa id_divisa, sum(saldo_divisa) saldo_divisa,sum(saldo_pesos) saldo_pesos\n" +
                "FROM nexco_reclasificacion_contingentes_com \n" +
                "WHERE cta_contable_60 != '' \n" +
                "AND periodo_origen = ? \n" +
                "group by cta_contable_60, divisa) b \n" +
                "on a.cuenta_local = b.cuenta_contable\n" +
                "LEFT JOIN (SELECT fecont,fechproce, nucta, coddiv, sum(salmesd) salmesd,sum(salmes) salmes\n" +
                "FROM "+nameTable+" \n" +
                "where "+tempValue+" \n" +
                "substring(fecont, 1, 7) = ? and empresa = '0060' \n" +
                "group by fecont,fechproce, nucta, coddiv) c\n" +
                "on a.cuenta_local = c.nucta AND b.id_divisa = c.coddiv \n" +
                "WHERE a.componente='CONTINGENTES' and c.fechproce is not null\n" +
                "ORDER BY 1 asc; ");
        consulta.setParameter(1,periodo);
        consulta.setParameter(2,periodo);
        consulta.executeUpdate();
    }

    public void cleanDataBase(String periodo){
        Query consulta = entityManager.createNativeQuery("delete from nexco_neocon60_base where periodo = ?");
        consulta.setParameter(1,periodo);
        consulta.executeUpdate();
    }

    public void cleanDataAjuste(String periodo){
        Query consulta = entityManager.createNativeQuery("delete from nexco_neocon60_ajuste where periodo = ?");
        consulta.setParameter(1,periodo);
        consulta.executeUpdate();
    }

    public void cleanDataCarga(String periodo){
        Query consulta = entityManager.createNativeQuery("delete from nexco_neocon60_carga_masiva where periodo = ?");
        consulta.setParameter(1,periodo);
        consulta.executeUpdate();
    }

    public void processAjuste(String periodo){
        cleanData(periodo);
        generateSubContingentes(periodo);
        String[] parte = periodo.split("-");
        Query consulta = entityManager.createNativeQuery("insert into nexco_neocon60 (ano,mes,codicons,divisa,naturaleza,pais,pais_contraparte,periodo,saldo,yntp_intergrupo,yntp_reportante)\n" +
                "(select z.ano,z.mes,z.codicons,z.divisa,case when sum(z.saldo)<0 then '-' else '+' end as naturaleza,z.pais,z.pais_contraparte,z.periodo,abs(round(sum(z.saldo),0)) as saldo1,z.yntp_intergrupo,z.yntp_reportante from\n" +
                "(select ano,mes,codicons,divisa,pais,pais_contraparte,periodo,case when naturaleza = '-' then saldo *-1 else saldo end as saldo,yntp_intergrupo,yntp_reportante \n" +
                "from nexco_neocon60_base where periodo = :periodo\n" +
                "union all\n" +
                "select ano,mes,codicons,divisa_espana,'XX' as pais,'' as pais_contraparte,periodo,saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_ajuste where periodo = :periodo\n" +
                "union all\n" +
                "select '"+parte[0]+"','"+parte[1]+"',j.codicons46,i.divisa_neocon,'XX' as pais,'' as pais_contraparte, :periodo as periodo , saldo_pesos/1000 as saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_contingentes h\n" +
                "left join (select distinct nucta, codicons46 from cuentas_puc where empresa = '0060') j on h.cuenta_local = j.NUCTA\n" +
                "left join nexco_divisas i on h.divisa = i.id_divisa\n" +
                "union all\n" +
                "select ano,mes,codicons,divisa_espana,'XX' as pais,'' as pais_contraparte,periodo,saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_carga_masiva where periodo = :periodo\n" +
                "union all\n" +
                "(select substring(a.periodo_origen,1,4) as ano, substring(a.periodo_origen,6,2) as mes, b.CODICONS46, c.divisa_neocon,'XX' as pais,'' as pais_contraparte,  a.periodo_origen, a.importe/1000 as importe,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_carga_masiva_intergrupo_v2 a \n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where EMPRESA = '0060') b on a.cuenta = b.NUCTA \n" +
                "left join nexco_divisas c on a.divisa = c.id_divisa \n" +
                "where a.periodo_origen = :periodo and tipo_info='REC' and origen_info in ('CAR', 'GAR','DEP') and b.CODICONS46 !='00000' )" +
                "union all\n" +
                "select t.ano,t.mes, '37501' as codicons,t.divisa,t.pais,t.pais_contraparte,t.periodo,case when t.saldo1 < 0 then abs(t.saldo1) else abs(t.saldo1) * -1 end as saldo,t.yntp_intergrupo,t.yntp_reportante from\n" +
                "(select w.ano,w.mes,w.divisa,w.pais,w.pais_contraparte,w.periodo,sum(w.saldo) as saldo1,w.yntp_intergrupo,w.yntp_reportante from\n" +
                "(select ano,mes,divisa_espana as divisa,'XX' as pais,'' as pais_contraparte,periodo,saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_ajuste where periodo = :periodo and SUBSTRING(codicons,1,1) IN ('3','4')\n" +
                "union all\n" +
                "select ano,mes,divisa_espana,'XX' as pais,'' as pais_contraparte,periodo,saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_carga_masiva where periodo = :periodo and SUBSTRING(codicons,1,1) IN ('3','4')\n" +
                "union all\n" +
                "(select substring(a.periodo_origen,1,4) as ano, substring(a.periodo_origen,6,2) as mes, c.divisa_neocon,'XX' as pais,'' as pais_contraparte,  a.periodo_origen, a.importe/1000 as importe,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_carga_masiva_intergrupo_v2 a \n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where EMPRESA = '0060') b on a.cuenta = b.NUCTA \n" +
                "left join nexco_divisas c on a.divisa = c.id_divisa \n" +
                "where a.periodo_origen = :periodo and tipo_info='REC' and origen_info in ('CAR', 'GAR','DEP') and b.CODICONS46 !='00000' and SUBSTRING(b.CODICONS46,1,1) IN ('3','4') )) w\n" +
                "group by ano,mes,divisa,pais,pais_contraparte,periodo,yntp_intergrupo,yntp_reportante) t\n" +
                "union all\n" +
                "select t.ano,t.mes, '28001' as codicons,t.divisa,t.pais,t.pais_contraparte,t.periodo,case when t.saldo1 > 0 then abs(t.saldo1) else abs(t.saldo1) * -1 end as saldo,t.yntp_intergrupo,t.yntp_reportante from\n" +
                "(select w.ano,w.mes,w.divisa,w.pais,w.pais_contraparte,w.periodo,sum(w.saldo) as saldo1,w.yntp_intergrupo,w.yntp_reportante from\n" +
                "(select ano,mes,divisa_espana as divisa,'XX' as pais,'' as pais_contraparte,periodo,saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_ajuste where periodo = :periodo and SUBSTRING(codicons,1,1) IN ('3','4')\n" +
                "union all\n" +
                "select ano,mes,divisa_espana,'XX' as pais,'' as pais_contraparte,periodo,saldo,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_neocon60_carga_masiva where periodo = :periodo and SUBSTRING(codicons,1,1) IN ('3','4')\n" +
                "union all\n" +
                "(select substring(a.periodo_origen,1,4) as ano, substring(a.periodo_origen,6,2) as mes, c.divisa_neocon,'XX' as pais,'' as pais_contraparte,  a.periodo_origen, a.importe/1000 AS importe,'' as yntp_intergrupo,'00548' as yntp_reportante \n" +
                "from nexco_carga_masiva_intergrupo_v2 a \n" +
                "left join (select NUCTA, CODICONS46 from CUENTAS_PUC where EMPRESA = '0060') b on a.cuenta = b.NUCTA \n" +
                "left join nexco_divisas c on a.divisa = c.id_divisa \n" +
                "where a.periodo_origen = :periodo and tipo_info='REC' and origen_info in ('CAR', 'GAR','DEP') and b.CODICONS46 !='00000' and SUBSTRING(b.CODICONS46,1,1) IN ('3','4') )) w\n" +
                "group by ano,mes,divisa,pais,pais_contraparte,periodo,yntp_intergrupo,yntp_reportante) t ) z\n" +
                "group by ano,mes,codicons,divisa,pais,pais_contraparte,periodo,yntp_intergrupo,yntp_reportante\n)");
        consulta.setParameter("periodo",periodo);
        consulta.executeUpdate();

        Query consulta1 = entityManager.createNativeQuery("drop table nexco_cuentas_neocon60_temp;\n" +
                "select z.codicons,z.divisa into nexco_cuentas_neocon60_temp from (select codicons,divisa from nexco_neocon60_base\n" +
                "union all\n" +
                "select codicons,divisa_espana from nexco_neocon60_ajuste\n" +
                "union all\n" +
                "select codicons,divisa_espana from nexco_neocon60_carga_masiva\n" +
                "union all\n" +
                "(select b.CODICONS46 as codicons, c.divisa_neocon as divisa_espana from nexco_neocon60_contingentes a\n" +
                "left join (select nucta, codicons46 from cuentas_puc where empresa  ='0060') b on a.cuenta_local = b.NUCTA\n" +
                "left join nexco_divisas c on a.divisa= c.id_divisa)\n" +
                "union all\n" +
                "select b.CODICONS46,c.divisa_neocon from (select cuenta,divisa from nexco_carga_masiva_intergrupo_v2 where periodo_origen = ? and tipo_info='REC' and origen_info in ('CAR', 'GAR','DEP') ) a\n" +
                "inner join (select NUCTA, CODICONS46 from CUENTAS_PUC where EMPRESA = '0060') b on a.cuenta = b.NUCTA\n" +
                "inner join nexco_divisas c on a.divisa = c.id_divisa where  b.CODICONS46 !='00000') z group by z.codicons,z.divisa");
        consulta1.setParameter(1,periodo);
        consulta1.executeUpdate();

        Query consulta2 = entityManager.createNativeQuery("delete from nexco_neocon60_cuadre where periodo = :periodo ;\n" +
                "insert into nexco_neocon60_cuadre (codicons,divisa,base,ajuste,ajuste2,carga,intergrupo,contingentes,total,plano,estado,periodo)\n" +
                "(select a.codicons,a.divisa, isnull(b.saldo,0) as base, isnull(c.saldo,0) as ajuste_C, isnull(g.saldo,0) as ajuste_N, isnull(d.saldo,0) as carga, isnull(e.saldo,0) as intergrupo, isnull(h.saldo,0) as contingentes, isnull(b.saldo,0)+isnull(c.saldo,0)+isnull(d.saldo,0)+isnull(e.saldo,0)+isnull(g.saldo,0)+isnull(h.saldo,0) as total,isnull(f.saldo,0) as plano,\n" +
                "case when isnull(f.saldo,0) > (isnull(b.saldo,0)+isnull(c.saldo,0)+isnull(d.saldo,0)+isnull(g.saldo,0)+isnull(e.saldo,0)+isnull(h.saldo,0)) then 'Incumple' else 'Cumple' end as estado,:periodo from nexco_cuentas_neocon60_temp a\n" +
                "left join (select codicons, divisa,sum(case when naturaleza = '-' then saldo *-1 else saldo end) as saldo from nexco_neocon60_base where periodo = :periodo group by codicons, divisa) b on a.codicons = b.codicons and a.divisa = b.divisa\n" +
                "left join (select codicons, divisa_espana,sum(saldo) as saldo from nexco_neocon60_ajuste where periodo = :periodo and aplica = 'CONTABILIDAD' group by codicons, divisa_espana) c on a.codicons = c.codicons and a.divisa = c.divisa_espana\n" +
                "left join (select codicons, divisa_espana,sum(saldo) as saldo from nexco_neocon60_ajuste where periodo = :periodo and aplica = 'NEOCON' group by codicons, divisa_espana) g on a.codicons = g.codicons and a.divisa = g.divisa_espana\n" +
                "left join (select codicons, divisa_espana,sum(saldo) as saldo from nexco_neocon60_carga_masiva where periodo = :periodo group by codicons, divisa_espana) d on a.codicons = d.codicons and a.divisa = d.divisa_espana\n" +
                "left join (select b.CODICONS46 as codicons, c.divisa_neocon as divisa_espana,sum(a.saldo_pesos) as saldo from nexco_neocon60_contingentes a\n" +
                "left join (select nucta, codicons46 from cuentas_puc where empresa  ='0060') b on a.cuenta_local = b.NUCTA\n" +
                "left join nexco_divisas c on a.divisa= c.id_divisa group by b.CODICONS46, c.divisa_neocon) h on a.codicons = h.codicons and a.divisa = h.divisa_espana\n" +
                "left join (select b.CODICONS46 as codicons,c.divisa_neocon as divisa_espana,sum(saldo)/1000 as saldo from (select cuenta,divisa,sum(importe) as saldo from nexco_carga_masiva_intergrupo_v2 where periodo_origen = :periodo and tipo_info='REC' and origen_info in ('CAR', 'GAR','DEP') group by cuenta,divisa) a\n" +
                "inner join (select NUCTA, CODICONS46 from CUENTAS_PUC where EMPRESA = '0060') b on a.cuenta = b.NUCTA\n" +
                "inner join nexco_divisas c on a.divisa = c.id_divisa where  b.CODICONS46 !='00000' group by b.CODICONS46,c.divisa_neocon) e on a.codicons = e.codicons and a.divisa = e.divisa_espana\n" +
                "left join (select isnull(b.CODICONS46, '') as codicons, isnull(d.divisa_neocon,'') as divisa, case when e.signo = '-' then abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) * -1 else abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) end as saldo\n" +
                "from (select * from nexco_intergrupo_v2_def where periodo = :periodo ) a\n" +
                "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "left join nexco_sociedades_yntp c\n" +
                "on a.yntp = c.yntp\n" +
                "left join nexco_divisas d\n" +
                "on a.divisa = d.id_divisa\n" +
                "left join nexco_indicadores e\n" +
                "on a.cod_neocon = e.cuenta_neocon\n" +
                "left join nexco_cuentas_neocon f \n" +
                "on a.cod_neocon = f.cuenta \n" +
                "where f.grscing not in ('', 'NA') \n" +
                "group by b.CODICONS46, d.divisa_neocon, e.signo\n" +
                "having abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) != 0) f on a.codicons = f.codicons and a.divisa = f.divisa)");
        consulta2.setParameter("periodo",periodo);
        consulta2.executeUpdate();

        Query consulta3 = entityManager.createNativeQuery("insert into nexco_neocon60 (ano,mes,yntp_reportante,codicons,divisa,pais,yntp_intergrupo,saldo,naturaleza,pais_contraparte,periodo)\n" +
                "select substring(A.periodo, 1, 4) as ano,substring(A.periodo, 6, 2) as mes,isnull(a.yntp_empresa_reportante, '00548') as yntp_reportante, isnull(b.CODICONS46, '') as codicons,\n" +
                "isnull(d.divisa_neocon,'')as divisa,'XX' as pais,isnull(a.yntp, '') as YNTP_Intergrupo, abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) as saldo,\n" +
                "isnull(e.signo, '') as\tnaturaleza, case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end as pais_contraparte, :periodo \n" +
                "from (select * from nexco_intergrupo_v2_def where periodo = :periodo ) a\n" +
                "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                "on a.cuenta_local = b.NUCTA\n" +
                "left join nexco_sociedades_yntp c\n" +
                "on a.yntp = c.yntp\n" +
                "left join nexco_divisas d\n" +
                "on a.divisa = d.id_divisa\n" +
                "left join nexco_indicadores e\n" +
                "on a.cod_neocon = e.cuenta_neocon\n" +
                "left join nexco_cuentas_neocon f \n" +
                "on a.cod_neocon = f.cuenta \n" +
                "where f.grscing not in ('', 'NA') \n" +
                "group by a.yntp_empresa_reportante, b.CODICONS46, \n" +
                "d.divisa_neocon, a.yntp, e.signo, case when a.cod_pais = 'ES' and a.yntp IN ('00010','82543') then 'ES' else 'XX' end, a.periodo \n" +
                "having abs(sum(round(CONVERT(numeric(20), a.valor/1000), 0))) != 0");
        consulta3.setParameter("periodo",periodo);
        consulta3.executeUpdate();
    }

    public ArrayList<String[]> saveFileBD(InputStream  file, User user,String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            list=validarPlantilla(file,periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso fichero Neocon 60");
            else
                loadAudit(user,"Cargue Fallido fichero Neocon 60");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(InputStream file, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        List<Neocon60> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        int filaCount = 1;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file)))
        {
            String linea;
            while ((linea = reader.readLine()) != null)
            {
                String cellAno = linea.substring(0,4);
                String cellMes = linea.substring(4,6);
                String cellYntpReportante = linea.substring(6,11);
                String cellCodicons = linea.substring(11,16);
                String cellDivisa = linea.substring(16,19).toUpperCase();
                String cellPais = linea.substring(19,21).toUpperCase();
                String cellYntpIntergrupo = linea.substring(21,26);
                String cellSaldo= linea.substring(26,44);
                String cellNaturaleza= linea.substring(44,45);
                String cellPaisContraparte= linea.substring(45,47);

                try {
                    Integer.parseInt(cellAno);
                }
                catch (Exception e)
                {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(0);
                    log[2] = "El campo Año debe ser númerico.";
                    lista.add(log);
                }
                try {
                    Integer.parseInt(cellMes);
                }
                catch (Exception e)
                {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(1);
                    log[2] = "El campo Mes debe ser númerico.";
                    lista.add(log);
                }
                if(!periodo.equals(cellAno+"-"+cellMes)) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(1);
                    log[2] = "El periodo ingresado no corresponde con el seleccionado.";
                    lista.add(log);
                }
                if (!validatorPatter(cellDivisa.trim(), "Divisa")) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "La Divisa ingresada no es valida (ABC).";
                    lista.add(log);
                }
                if (!validatorPatter(cellPais.trim(), "Pais")) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(4);
                    log[2] = "El campo Pais ingresada no es valido (AB).";
                    lista.add(log);
                }
                try {
                    Double.parseDouble(cellSaldo);
                }
                catch (Exception e)
                {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(7);
                    log[2] = "El campo Saldo debe ser númerico.";
                    lista.add(log);
                }
                if (!cellNaturaleza.equals("+") && !cellNaturaleza.equals("-")) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(filaCount);
                    log[1] = CellReference.convertNumToColString(8);
                    log[2] = "El campo naturaleza ingresada debe ser + o - .";
                    lista.add(log);
                }

                Neocon60 neocon60 = new Neocon60();
                neocon60.setAno(cellAno);
                neocon60.setMes(cellMes);
                neocon60.setYntpReportante(cellYntpReportante);
                neocon60.setCodicons(cellCodicons);
                neocon60.setDivisa(cellDivisa);
                neocon60.setPais(cellPais);
                neocon60.setYntpIntergrupo(cellYntpIntergrupo);
                try {
                    neocon60.setSaldo(Double.valueOf(cellSaldo));
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
                neocon60.setNaturaleza(cellNaturaleza);
                neocon60.setPaisContraparte(cellPaisContraparte);
                neocon60.setPeriodo(periodo);
                toInsert.add(neocon60);
                filaCount++;
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            String[] log = new String[3];
            log[0] = String.valueOf(filaCount);
            log[1] = CellReference.convertNumToColString(0);
            log[2] = "La estrcutura de la fila no se puede procesar.";
            lista.add(log);
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 5) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")){
            cleanData(periodo);
            insertQuery(toInsert);
            cleanDataBase(periodo);
            insertQueryBase(toInsert);
            generateDate(periodo,"NEOCON 60 BASE");
        }
        toInsert.clear();
        return lista;
    }

    public List<Neocon60Ajuste> findAjusteExist(String periodo,String ano,String mes,String codicons,String divisa){
        Query consulta = entityManager.createNativeQuery("select * from nexco_neocon60_ajuste where periodo = ? and ano = ? and codicons = ? and mes = ? and divisa = ?", Neocon60Ajuste.class);
        consulta.setParameter(1,periodo);
        consulta.setParameter(2,ano);
        consulta.setParameter(3,codicons);
        consulta.setParameter(4,mes);
        consulta.setParameter(5,divisa);
        return consulta.getResultList();
    }

    public ArrayList<String[]> saveFileBDAjuste(InputStream  file, User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantillaAjuste(rows,periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso ajuste Neocon60");
            else
                loadAudit(user,"Cargue Fallido ajuste Neocon60");
        }
        return list;
    }

    public ArrayList<String[]> saveFileBDCargaMasiva(InputStream  file, User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantillaCargaMasiva(rows,periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Carga Masiva Neocon60");
            else
                loadAudit(user,"Cargue Fallido Carga Masiva Neocon60");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantillaAjuste(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Neocon60Ajuste> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    String cellAno = formatter.formatCellValue(row.getCell(0)).trim();
                    String cellMes = formatter.formatCellValue(row.getCell(1)).trim();
                    String cellCodicons = formatter.formatCellValue(row.getCell(2)).trim();
                    String cellDivisa = formatter.formatCellValue(row.getCell(3)).trim().toUpperCase();
                    String cellSaldo = "";
                    XSSFCell cell0= row.getCell(4);
                    cell0.setCellType(CellType.STRING);
                    cellSaldo = formatter.formatCellValue(cell0).replace(" ", "");
                    String cellObservacion = formatter.formatCellValue(row.getCell(5)).trim();
                    String cellAplica = formatter.formatCellValue(row.getCell(6)).trim().toUpperCase();

                    if (cellAno.length() != 4) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Año debe ser de 4 carcrateres..";
                        lista.add(log);
                    } else {
                        try
                        {
                            Integer.parseInt(cellAno);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(0);
                            log[2] = "El campo Año debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellMes.length() != 2) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "El campo Mes debe ser de 2 carcrateres.";
                        lista.add(log);
                    } else {
                        try
                        {
                            Integer.parseInt(cellMes);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(1);
                            log[2] = "El campo Mes debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (!periodo.equals(cellAno+"-"+cellMes)) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(1);
                        log[2] = "Los datos ingresados no corresponden al seleccionado";
                        lista.add(log);
                    }
                    if (cellCodicons.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo Codicons no puede estar vacio";
                        lista.add(log);
                    }
                    if (cellDivisa.length() != 3) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo Divisa debe tener 3 caracteres";
                        lista.add(log);
                    } else if (!validatorPatter(cellDivisa, "Divisa")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "La Divisa ingresada no es valida (ABC).";
                        lista.add(log);
                    }
                    if (cellSaldo.length()==0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo Saldo no puede estar vacio";
                        lista.add(log);
                    }
                    else {
                        try
                        {
                            Double.parseDouble(cellSaldo);
                        }
                        catch (Exception e) {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(4);
                            log[2] = "El campo Saldo debe ser númerico.";
                            lista.add(log);
                        }
                    }
                    if (cellObservacion.length()==0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Observación no puede estar vacio.";
                        lista.add(log);
                    }
                    if (!cellAplica.equals("CONTABILIDAD") && !cellAplica.equals("NEOCON")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(6);
                        log[2] = "El campo Aplica debe ser Contabilidad o Neocon.";
                        lista.add(log);
                    }

                    Neocon60Ajuste neocon60Ajuste = new Neocon60Ajuste();
                    try
                    {
                        neocon60Ajuste.setSaldo(Double.parseDouble(cellSaldo));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    neocon60Ajuste.setAno(cellAno);
                    neocon60Ajuste.setMes(cellMes);
                    neocon60Ajuste.setCodicons(cellCodicons);
                    neocon60Ajuste.setDivisa(cellDivisa);
                    neocon60Ajuste.setObservacion(cellObservacion);
                    neocon60Ajuste.setPeriodo(periodo);
                    neocon60Ajuste.setAplica(cellAplica);
                    toInsert.add(neocon60Ajuste);
                }
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 6) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")){
            cleanDataAjuste(periodo);
            neocon60AjusteRepository.saveAll(toInsert);
            completeDataAjuste(periodo);
            generateDate(periodo,"NEOCON 60 AJUSTE");
        }
        toInsert.clear();
        return lista;
    }

    public ArrayList<String[]> validarPlantillaCargaMasiva(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<Neocon60Carga> toInsert = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0)
            {
                DataFormatter formatter = new DataFormatter();
                String cellCuenta = formatter.formatCellValue(row.getCell(1)).trim();
                String cellDivisa = formatter.formatCellValue(row.getCell(2)).trim().toUpperCase();
                String cellPeriodo = formatter.formatCellValue(row.getCell(7)).trim();
                String cellSaldo = "";
                XSSFCell cell0= row.getCell(5);
                cell0.setCellType(CellType.STRING);
                cellSaldo = formatter.formatCellValue(cell0).replace(" ", "");
                String cellMes = "";
                String cellAno = "";

                if (cellPeriodo.length() < 6)
                {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(7);
                    log[2] = "El campo Fecha debe estar informado.";
                    lista.add(log);
                }
                else
                {
                    try
                    {
                        cellAno = cellPeriodo.substring(0,4) ;
                        cellMes = cellPeriodo.substring(4,6) ;
                    }
                    catch (Exception e) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "El campo Periodo debe ser númerico.";
                        lista.add(log);
                    }

                    if (!periodo.equals(cellAno+"-"+cellMes))
                    {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(7);
                        log[2] = "La fecha no coincide con el periodo de cargue";
                        lista.add(log);
                    }
                }

                if (cellCuenta.length() == 0) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(1);
                    log[2] = "El campo Cuenta no puede estar vacio";
                    lista.add(log);
                }
                if (cellDivisa.length() != 3) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(2);
                    log[2] = "El campo Divisa debe tener 3 caracteres";
                    lista.add(log);
                } else if (!validatorPatter(cellDivisa, "Divisa")) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(2);
                    log[2] = "La Divisa ingresada no es valida (ABC).";
                    lista.add(log);
                }
                if (cellSaldo.length()==0) {
                    String[] log = new String[3];
                    log[0] = String.valueOf(row.getRowNum() + 1);
                    log[1] = CellReference.convertNumToColString(5);
                    log[2] = "El campo Saldo no puede estar vacio";
                    lista.add(log);
                }
                else
                {
                    try
                    {
                        Double.parseDouble(cellSaldo);
                    }
                    catch (Exception e) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(5);
                        log[2] = "El campo Saldo debe ser númerico.";
                        lista.add(log);
                    }
                }

                Neocon60Carga neocon60Carga = new Neocon60Carga();
                neocon60Carga.setAno(cellAno);
                neocon60Carga.setMes(cellMes);
                neocon60Carga.setNucta(cellCuenta);
                neocon60Carga.setPeriodo(periodo);
                neocon60Carga.setDivisa(cellDivisa);
                try{
                    neocon60Carga.setSaldo(Double.parseDouble(cellSaldo));
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                toInsert.add(neocon60Carga);
            }
        }

        if (lista.size() != 0)
            stateFinal = "FAILED";
        String[] log2 = new String[3];
        log2[0] = String.valueOf((toInsert.size() * 4) - lista.size());
        log2[1] = String.valueOf(lista.size());
        log2[2] = stateFinal;
        lista.add(log2);
        String[] temp = lista.get(0);
        if (temp[2].equals("SUCCESS")){
            cleanDataCarga(periodo);
            neocon60CargaRepository.saveAll(toInsert);
            completeDataCarga(periodo);
            generateDate(periodo,"NEOCON 60 CARGA");
        }
        toInsert.clear();
        return lista;
    }

    public void insertQuery(List<Neocon60> temporal) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_neocon60(ano,mes,yntp_reportante,codicons,divisa,pais,yntp_intergrupo,saldo,naturaleza,pais_contraparte,periodo) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getAno());
                        ps.setString(2, temporal.get(i).getMes());
                        ps.setString(3, temporal.get(i).getYntpReportante());
                        ps.setString(4, temporal.get(i).getCodicons());
                        ps.setString(5, temporal.get(i).getDivisa());
                        ps.setString(6, temporal.get(i).getPais());
                        ps.setString(7, temporal.get(i).getYntpIntergrupo());
                        ps.setDouble(8, temporal.get(i).getSaldo());
                        ps.setString(9, temporal.get(i).getNaturaleza());
                        ps.setString(10, temporal.get(i).getPaisContraparte());
                        ps.setString(11, temporal.get(i).getPeriodo());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public void insertQueryBase(List<Neocon60> temporal) {

        jdbcTemplate.batchUpdate(
                "INSERT INTO nexco_neocon60_base(ano,mes,yntp_reportante,codicons,divisa,pais,yntp_intergrupo,saldo,naturaleza,pais_contraparte,periodo) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getAno());
                        ps.setString(2, temporal.get(i).getMes());
                        ps.setString(3, temporal.get(i).getYntpReportante());
                        ps.setString(4, temporal.get(i).getCodicons());
                        ps.setString(5, temporal.get(i).getDivisa());
                        ps.setString(6, temporal.get(i).getPais());
                        ps.setString(7, temporal.get(i).getYntpIntergrupo());
                        ps.setDouble(8, temporal.get(i).getSaldo());
                        ps.setString(9, temporal.get(i).getNaturaleza());
                        ps.setString(10, temporal.get(i).getPaisContraparte());
                        ps.setString(11, temporal.get(i).getPeriodo());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public boolean validatorPatter(String dato,String tipo){
        String patron ="";
        if(tipo.equals("Pais"))
        {
            patron="^[a-zA-Z]{2}$";
        }
        else if(tipo.equals("Divisa")){
            patron="^[a-zA-Z]{3}$";
        }
        Pattern pattern= Pattern.compile(patron);
        Matcher matcher = pattern.matcher(dato);
        return matcher.matches();
    }

    public void completeDataCarga(String periodo){
        Query consulta = entityManager.createNativeQuery("update tabla1 set tabla1.divisa_espana = tabla2.divisa_neocon \n" +
                "from (select * from nexco_neocon60_carga_masiva where periodo = ?) tabla1 , nexco_divisas tabla2\n" +
                "where tabla1.divisa = tabla2.id_divisa;\n" +
                "\n" +
                "update tabla1 set tabla1.codicons = tabla2.codicons46 \n" +
                "from (select * from nexco_neocon60_carga_masiva where periodo = ?) tabla1 , (select nucta, codicons46 from cuentas_puc where empresa = '0060') tabla2\n" +
                "where tabla1.nucta = tabla2.nucta;");
        consulta.setParameter(1,periodo);
        consulta.setParameter(2,periodo);
        consulta.executeUpdate();
    }

    public void completeDataAjuste(String periodo){
        Query consulta = entityManager.createNativeQuery("update tabla1 set tabla1.divisa_espana = tabla2.divisa_neocon \n" +
                "from (select * from nexco_neocon60_ajuste where periodo = ?) tabla1 , nexco_divisas tabla2\n" +
                "where tabla1.divisa = tabla2.id_divisa;");
        consulta.setParameter(1,periodo);
        consulta.executeUpdate();
    }

    public void generateDate(String periodo,String input)
    {
        Date today = new Date();

        StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, periodo);

        if (validateStatus == null) {
            StatusInfo status = new StatusInfo();
            status.setInput(input);
            status.setPeriodo(periodo);
            status.setFecha(today);
            StatusInfoRepository.save(status);
        } else {
            validateStatus.setFecha(today);
            StatusInfoRepository.save(validateStatus);
        }
    }

}
