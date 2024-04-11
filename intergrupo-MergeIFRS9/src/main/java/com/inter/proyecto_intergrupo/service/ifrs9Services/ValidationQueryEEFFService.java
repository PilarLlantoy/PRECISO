package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ValQueryEEFF;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class ValidationQueryEEFFService {

    @PersistenceContext
    EntityManager entityManager;

    public List<ValQueryEEFF> calcularCuadreQueryEeff(String periodo, String opcionQ, String opcionE, String empresa) {
        List<ValQueryEEFF> list = new ArrayList<ValQueryEEFF>();
        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];
        String fechaProceso1;
        String fechaProceso2;

        try {


            Query query01 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_query_eeff");
            query01.executeUpdate();

            Query query02 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_query_aux_local_temp");
            query02.executeUpdate();

            Query query03 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_query_aux_ifrs9_temp");
            query03.executeUpdate();

            Query query04 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_eeff_aux_local_temp");
            query04.executeUpdate();

            Query query05 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_eeff_aux_ifrs9_temp");
            query05.executeUpdate();

            Query query06 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_inter_aux_local_temp");
            query06.executeUpdate();

            Query query07 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_inter_aux_ifrs9_temp");
            query07.executeUpdate();

            Query query08 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_ajus_hom_aux_temp");
            query08.executeUpdate();

            Query query80 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_ajus_man_aux_temp");
            query80.executeUpdate();

            Query query11 = entityManager.createNativeQuery("insert into nexco_eeff_aux_local_temp (codigo_consolidacion, tipo_cuenta, divisa, descripcion_ic, sociedad_ic, saldo) \n" +
                    "SELECT cuenta, tipo_cuenta, divisa, descripcion_ic, sociedad_ic, SUM(saldo) AS saldo \n" +
                    "FROM (SELECT C.cuenta, c.tipo_cuenta, C.Divisa_espana as divisa, c.descripcion_ic, c.sociedad_ic, CASE  \n" +
                    "WHEN LTRIM(RTRIM(C.saldo)) = '' THEN \n" +
                    "CAST(0.00 AS DECIMAL) \n" +
                    "ELSE \n" +
                    "CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                    "FROM nexco_eeff AS C \n" +
                    "LEFT JOIN nexco_cuentas_neocon D\n" +
                    "ON c.cuenta = d.cuenta \n" +
                    "WHERE saldo <> '' \n" +
                    "--and descripcion_ic = '' \n" +
                    "and upper(tipo) = 'LOCAL' \n" +
                    "AND period = ? \n" +
                    "and d.entrada = 'S') AS A \n" +
                    "GROUP BY cuenta, tipo_cuenta, divisa, descripcion_ic, sociedad_ic\n" +
                    ";");
            query11.setParameter(1,periodo);
            query11.executeUpdate();

            Query query12 = entityManager.createNativeQuery("insert into nexco_eeff_aux_ifrs9_temp (codigo_consolidacion, tipo_cuenta, divisa, descripcion_ic, sociedad_ic, saldo) \n" +
                    "SELECT cuenta, tipo_cuenta, divisa, descripcion_ic, sociedad_ic, SUM(saldo) AS saldo \n" +
                    "FROM (SELECT C.cuenta, c.tipo_cuenta, C.Divisa_espana as divisa, c.descripcion_ic, c.sociedad_ic, CASE  \n" +
                    "WHEN LTRIM(RTRIM(C.saldo)) = '' THEN \n" +
                    "CAST(0.00 AS DECIMAL) \n" +
                    "ELSE \n" +
                    "CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                    "FROM nexco_eeff AS C \n" +
                    "LEFT JOIN nexco_cuentas_neocon D\n" +
                    "ON c.cuenta = d.cuenta \n" +
                    "WHERE saldo <> '' \n" +
                    "--and descripcion_ic = '' \n" +
                    "and upper(tipo) = 'IFRS9' \n" +
                    "AND period = ? \n" +
                    "and d.entrada = 'S') AS A \n" +
                    "GROUP BY cuenta, tipo_cuenta, divisa, descripcion_ic, sociedad_ic\n" +
                    ";");
            query12.setParameter(1,periodo);
            query12.executeUpdate();

            Query query21 = entityManager.createNativeQuery("insert into nexco_query_aux_local_temp (codigo_consolidacion, divisa, saldo, fecha_proceso) \n" +
                    "SELECT q.codicons as cuenta, q.divisa as divisa, case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo, q.fecha_proceso \n" +
                    "FROM (SELECT a.codicons, a.divisa, \n" +
                    "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                    "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                    "FROM nexco_query_marcados a \n" +
                    "inner join (select * from nexco_cuentas_neocon) c \n" +
                    "on a.codicons = c.cuenta \n" +
                    "WHERE origen = 'LOCAL' AND YEAR(fecont) = ? AND MONTH(fecont) = ? \n" +
                    "AND empresa = '0060' and codicons <> '00000' and salmes <> 0) AS q \n" +
                    "left join nexco_indicadores i\n" +
                    "on q.codicons = i.cuenta_neocon\n" +
                    "GROUP BY q.codicons, q.divisa, q.fecha_proceso, i.signo\n" +
                    ";");
            query21.setParameter(1,anio);
            query21.setParameter(2,mes);
            query21.executeUpdate();

            Query queryFec1 = entityManager.createNativeQuery("SELECT top 1 convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                    "FROM nexco_query_marcados a \n" +
                    "WHERE origen = 'IFRS9' AND YEAR(fecont) = ? AND MONTH(fecont) = ? AND empresa = '0060' \n" +
                    ";");
            queryFec1.setParameter(1,anio);
            queryFec1.setParameter(2,mes);
            fechaProceso1 = queryFec1.getResultList().get(0).toString();

            Query query22 = entityManager.createNativeQuery("insert into nexco_query_aux_ifrs9_temp (codigo_consolidacion, divisa, saldo, fecha_proceso) \n" +
                    "SELECT q.codicons as cuenta, q.divisa as divisa, case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo, q.fecha_proceso \n" +
                    "FROM (SELECT a.codicons, a.divisa, \n" +
                    "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                    "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                    "FROM nexco_query_marcados a \n" +
                    "inner join (select * from nexco_cuentas_neocon) c \n" +
                    "on a.codicons = c.cuenta \n" +
                    "WHERE origen = 'IFRS9' AND YEAR(fecont) = ? AND MONTH(fecont) = ? \n" +
                    "AND empresa = '0060' and codicons <> '00000' and salmes <> 0) AS q \n" +
                    "left join nexco_indicadores i\n" +
                    "on q.codicons = i.cuenta_neocon\n" +
                    "GROUP BY q.codicons, q.divisa, q.fecha_proceso, i.signo\n" +
                    ";");
            query22.setParameter(1,anio);
            query22.setParameter(2,mes);
            query22.executeUpdate();

            Query queryFec2 = entityManager.createNativeQuery("SELECT top 1 convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                    "FROM nexco_query_marcados a \n" +
                    "WHERE origen = 'IFRS9' AND YEAR(fecont) = ? AND MONTH(fecont) = ? AND empresa = '0060' \n" +
                    ";");
            queryFec2.setParameter(1,anio);
            queryFec2.setParameter(2,mes);
            fechaProceso2 = queryFec2.getResultList().get(0).toString();

            Query query31 = entityManager.createNativeQuery("insert into nexco_inter_aux_local_temp (codigo_consolidacion, sociedad_ic, descripcion_ic, divisa, saldo) \n" +
                    "select cod_neocon, yntp, sociedad_yntp, divisa_neocon, abs(round(cast(sum(valor)/1000 as decimal(18,2)), 0)) saldo\n" +
                    "from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def a\n" +
                    "left join nexco_divisas b\n" +
                    "on a.divisa = b.id_divisa\n" +
                    "where periodo = ?\n" +
                    "group by cod_neocon, yntp, sociedad_yntp, divisa_neocon\n" +
                    ";");
            query31.setParameter(1,periodo);
            query31.executeUpdate();

            Query query32 = entityManager.createNativeQuery("insert into nexco_inter_aux_ifrs9_temp (codigo_consolidacion, sociedad_ic, descripcion_ic, divisa, saldo) \n" +
                    "select cod_neocon, yntp, sociedad_yntp, divisa_neocon, abs(round(cast(sum(valor)/1000 as decimal(18,2)), 0)) saldo\n" +
                    "from /*nexco_intergrupo_v3*/ nexco_intergrupo_v3_def a\n" +
                    "left join nexco_divisas b\n" +
                    "on a.divisa = b.id_divisa\n" +
                    "where periodo = ?\n" +
                    "group by cod_neocon, yntp, sociedad_yntp, divisa_neocon\n" +
                    ";");
            query32.setParameter(1,periodo);
            query32.executeUpdate();

            Query query41 = entityManager.createNativeQuery("insert into nexco_ajus_hom_aux_temp (codigo_consolidacion, sociedad_ic, descripcion_ic, divisa, saldo) \n" +
                    "select codicons, sociedad_ic, descripcion_ic, divisa, sum((saldo_debe1+saldo_debe2)-(saldo_haber1+saldo_haber2)) saldo from nexco_ajustes_hom \n" +
                    "where periodo = ?\n" +
                    "group by codicons, divisa, sociedad_ic, descripcion_ic\n" +
                    ";");
            query41.setParameter(1,periodo);
            query41.executeUpdate();

            Query query42 = entityManager.createNativeQuery("insert into nexco_ajus_man_aux_temp (codigo_consolidacion, divisa, saldo) \n" +
                    "select codicons, divisa_espana, sum(saldo) saldo\n" +
                    "from nexco_ajustes_manuales\n" +
                    "where periodo = ? \n" +
                    "group by codicons, divisa_espana\n" +
                    ";");
            query42.setParameter(1,periodo);
            query42.executeUpdate();

            Query query5 = entityManager.createNativeQuery("INSERT INTO nexco_query_eeff (fecha_proceso_local, fecha_proceso_ifrs9, codigo_consolidacion, descripcion, " +
                    "tipo_cuenta, perimetro, intergrupo, sociedad_ic, descripcion_ic, divisa, saldo_eeff_local, saldo_eeff_ifrs9, saldo_query_local, saldo_query_ifrs9, " +
                    "saldo_inter_local, saldo_inter_ifrs9, saldo_dif_local, saldo_dif_ifrs9, saldo_dif_eeff_inter_local, saldo_dif_eeff_inter_ifrs9, saldo_ajuste_hom, saldo_ajuste_man, periodo) \n" +
                    "SELECT ? as fecha_query_local, ? as fecha_query_ifrs9, \n" +
                    "coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion, e.codigo_consolidacion, f.codigo_consolidacion, g.codigo_consolidacion) as codigo_consolidacion, \n" +
                    "z.descripcion, \n" +
                    "isnull(b.tipo_cuenta, e.tipo_cuenta) as tipo_cuenta, \n" +
                    "case when y.cuenta_neocon is not null then y.ifrs9 else '' end as perimetro,\n" +
                    "case when (coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) is null or coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) = '') then 'No' else 'Sí' end as intergrupo,\n" +
                    "coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) sociedad, coalesce(b.descripcion_ic, c.descripcion_ic, e.descripcion_ic, f.descripcion_ic, g.descripcion_ic) descripcion_ic, \n" +
                    "coalesce(a.divisa, b.divisa, c.divisa, d.divisa, e.divisa, f.divisa, g.divisa) as divisa, \n" +
                    "abs(isnull(b.saldo,0)) as eeff_local, \n" +
                    "abs(isnull(e.saldo,0)) as eeff_ifrs9, \n" +
                    "abs(isnull(a.saldo,0)) as query_local, \n" +
                    "abs(isnull(d.saldo,0)) as query_ifrs9, \n" +
                    "abs(isnull(c.saldo, 0)) as saldo_ic,\n" +
                    "abs(isnull(f.saldo, 0)) as saldo_ic_ifrs9,\n" +
                    "case when (coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) is null or coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) = '') then abs(isnull(a.saldo,0)) - abs(isnull(b.saldo,0)) else 0 end as dif_local, \n" +
                    "case when (coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) is null or coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) = '') then abs(isnull(d.saldo,0)) - abs(isnull(e.saldo,0)) else 0 end as dif_ifrs9, \n" +
                    "case when (coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) is null or coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) = '') then 0 else abs(isnull(b.saldo,0)) - abs(isnull(c.saldo, 0)) end as dif_eeff_inter_local, \n" +
                    "case when (coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) is null or coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, g.sociedad_ic) = '') then 0 else abs(isnull(e.saldo,0)) - abs(isnull(f.saldo, 0)) end as dif_eeff_inter_ifrs9, \n" +
                    "abs(isnull(g.saldo,0)) as ajustes,\n" +
                    "abs(isnull(h.saldo,0)) as ajustes_manuales,\n" +
                    "? periodo\n" +
                    "FROM (select * from nexco_query_aux_local_temp where codigo_consolidacion <> '' and divisa <> '' and saldo <> 0) AS a\n" +
                    "FULL JOIN (select * from nexco_eeff_aux_local_temp where codigo_consolidacion <> '' and divisa <> '') AS b\n" +
                    "ON a.codigo_consolidacion = b.codigo_consolidacion and a.divisa = b.divisa and sociedad_ic = ''\n" +
                    "FULL JOIN (select * from nexco_inter_aux_local_temp where saldo <> 0) c\n" +
                    "ON isnull(a.codigo_consolidacion, b.codigo_consolidacion) = c.codigo_consolidacion and isnull(A.divisa, B.divisa) = c.divisa and b.sociedad_ic = c.sociedad_ic\n" +
                    "FULL JOIN (select * from nexco_query_aux_ifrs9_temp where codigo_consolidacion <> '' and divisa <> '' and saldo <> 0) d\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion) = d.codigo_consolidacion and (coalesce(b.sociedad_ic, c.sociedad_ic) = '' or coalesce(b.sociedad_ic, c.sociedad_ic) is null) \n" +
                    "and coalesce(a.divisa, b.divisa, c.divisa) = d.divisa\n" +
                    "FULL JOIN (select * from nexco_eeff_aux_ifrs9_temp where codigo_consolidacion <> '' and divisa <> '') e\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion) = e.codigo_consolidacion \n" +
                    "and coalesce(a.divisa, b.divisa, c.divisa, d.divisa) = e.divisa and coalesce(b.sociedad_ic, c.sociedad_ic,'') = e.sociedad_ic \n" +
                    "FULL JOIN (select * from nexco_inter_aux_ifrs9_temp where saldo <> 0) f\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion, e.codigo_consolidacion) = f.codigo_consolidacion \n" +
                    "and coalesce(a.divisa, b.divisa, c.divisa, d.divisa, e.divisa) = f.divisa and coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic,'') = f.sociedad_ic\n" +
                    "FULL JOIN (select * from nexco_ajus_hom_aux_temp where saldo <> 0) g\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion, e.codigo_consolidacion, f.codigo_consolidacion) = g.codigo_consolidacion\n" +
                    "and coalesce(a.divisa, b.divisa, c.divisa, d.divisa, e.divisa, f.divisa) = g.divisa and coalesce(b.sociedad_ic, c.sociedad_ic, e.sociedad_ic, f.sociedad_ic, '') = g.sociedad_ic\n" +
                    "FULL JOIN (select * from nexco_ajus_man_aux_temp where saldo <> 0) h\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion, e.codigo_consolidacion, f.codigo_consolidacion, g.codigo_consolidacion) = h.codigo_consolidacion\n" +
                    "and coalesce(a.divisa, b.divisa, c.divisa, d.divisa, e.divisa, f.divisa, g.divisa) = h.divisa\n" +
                    "LEFT JOIN nexco_provisiones y\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion, e.codigo_consolidacion, f.codigo_consolidacion, g.codigo_consolidacion, h.codigo_consolidacion) = y.cuenta_neocon \n" +
                    "LEFT JOIN nexco_cuentas_neocon z\n" +
                    "ON coalesce(a.codigo_consolidacion, b.codigo_consolidacion, c.codigo_consolidacion, d.codigo_consolidacion, e.codigo_consolidacion, f.codigo_consolidacion, g.codigo_consolidacion, h.codigo_consolidacion) = z.cuenta  \n" +
                    "order by 3\n" +
                    ";");
            query5.setParameter(1,fechaProceso1);
            query5.setParameter(2,fechaProceso2);
            query5.setParameter(3,periodo);
            query5.executeUpdate();

            Query queryF = entityManager.createNativeQuery("SELECT em.* FROM nexco_query_eeff AS em " +
                    "WHERE periodo = ?",ValQueryEEFF.class);
            queryF.setParameter(1,periodo);
            list= queryF.getResultList();

            return list;

        } catch (NoResultException e) {

            return list;
        }
    }

    public List<Object[]> getEeffLocal(String periodo) {

        Query query11 = entityManager.createNativeQuery("SELECT descripcion, denominacion_cuenta, tipo_cuenta, cuenta, sociedad_ic, descripcion_ic, desgloces, divisa, saldo AS saldo \n" +
                "FROM (SELECT c.descripcion, c.cuenta, c.tipo_cuenta, C.Divisa_espana as divisa, c.descripcion_ic, c.sociedad_ic, c.denominacion_cuenta, c.desgloces, CASE  \n" +
                "WHEN LTRIM(RTRIM(C.saldo)) = '' THEN \n" +
                "CAST(0.00 AS DECIMAL) \n" +
                "ELSE \n" +
                "CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                "FROM nexco_eeff AS C \n" +
                "LEFT JOIN nexco_cuentas_neocon D\n" +
                "ON c.cuenta = d.cuenta \n" +
                "WHERE saldo <> '' \n" +
                "and upper(tipo) = 'LOCAL' \n" +
                "AND period = ?\n" +
                "and d.entrada = 'S') AS A \n" +
                ";");
        query11.setParameter(1, periodo);
        return query11.getResultList();

    }

    public List<Object[]> getEeffIfrs9(String periodo){

            Query query12 = entityManager.createNativeQuery("SELECT descripcion, denominacion_cuenta, tipo_cuenta, cuenta, sociedad_ic, descripcion_ic, desgloces, divisa, saldo AS saldo \n" +
                    "FROM (SELECT c.descripcion, c.cuenta, c.tipo_cuenta, C.Divisa_espana as divisa, c.descripcion_ic, c.sociedad_ic, c.denominacion_cuenta, c.desgloces, CASE  \n" +
                    "WHEN LTRIM(RTRIM(C.saldo)) = '' THEN \n" +
                    "CAST(0.00 AS DECIMAL) \n" +
                    "ELSE \n" +
                    "CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                    "FROM nexco_eeff AS C \n" +
                    "LEFT JOIN nexco_cuentas_neocon D\n" +
                    "ON c.cuenta = d.cuenta \n" +
                    "WHERE saldo <> '' \n" +
                    "and upper(tipo) = 'IFRS9' \n" +
                    "AND period = ?\n" +
                    "and d.entrada = 'S') AS A \n" +
                    ";");
        query12.setParameter(1,periodo);
        return query12.getResultList();

    }

    public List<Object[]> getQueryLocal(String periodo){

        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        Query query21 = entityManager.createNativeQuery("SELECT q.empresa, q.nucta cuenta_local, q.codicons as codicons, fecont, coddiv, q.divisa as divisa, case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo, q.fecha_proceso \n" +
                "FROM (SELECT a.empresa, a.nucta, a.fecont, a.coddiv, a.codicons, a.divisa, \n" +
                "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                "FROM nexco_query_marcados a \n" +
                "inner join (select * from nexco_cuentas_neocon) c \n" +
                "on a.codicons = c.cuenta \n" +
                "WHERE origen = 'LOCAL' AND YEAR(fecont) = ? AND MONTH(fecont) = ?\n" +
                "AND empresa = '0060' and codicons <> '00000' and salmes <> 0) AS q \n" +
                "left join nexco_indicadores i\n" +
                "on q.codicons = i.cuenta_neocon\n" +
                "GROUP BY q.empresa, q.nucta, fecont, coddiv, q.codicons, q.divisa, q.fecha_proceso, i.signo\n" +
                ";");
        query21.setParameter(1,anio);
        query21.setParameter(2,mes);
        return query21.getResultList();

    }

    public List<Object[]> getQueryIfrs9(String periodo){

        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        Query query22 = entityManager.createNativeQuery("SELECT q.empresa, q.nucta cuenta_local, q.codicons as codicons, fecont, coddiv, q.divisa as divisa, case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo, q.fecha_proceso \n" +
                "FROM (SELECT a.empresa, a.nucta, a.fecont, a.coddiv, a.codicons, a.divisa, \n" +
                "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                "FROM nexco_query_marcados a \n" +
                "inner join (select * from nexco_cuentas_neocon) c \n" +
                "on a.codicons = c.cuenta \n" +
                "WHERE origen = 'IFRS9' AND YEAR(fecont) = ? AND MONTH(fecont) = ?\n" +
                "AND empresa = '0060' and codicons <> '00000' and salmes <> 0) AS q \n" +
                "left join nexco_indicadores i\n" +
                "on q.codicons = i.cuenta_neocon\n" +
                "GROUP BY q.empresa, q.nucta, fecont, coddiv, q.codicons, q.divisa, q.fecha_proceso, i.signo\n" +
                ";");
        query22.setParameter(1,anio);
        query22.setParameter(2,mes);
        return query22.getResultList();

    }

    public List<Object[]> getIntLocal(String periodo){

        Query query31 = entityManager.createNativeQuery("select yntp_empresa_reportante, cod_neocon, divisa, divisa_neocon, yntp, sociedad_yntp, contrato, nit, cod_pais, pais, cuenta_local, abs(round(cast(sum(valor)/1000 as decimal(18,2)), 0)) saldo\n" +
                "from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def a\n" +
                "left join nexco_divisas b\n" +
                "on a.divisa = b.id_divisa\n" +
                "where periodo = ?\n" +
                "group by yntp_empresa_reportante, cod_neocon, divisa, divisa_neocon, yntp, sociedad_yntp, contrato, nit, cod_pais, pais, cuenta_local\n" +
                ";");
        query31.setParameter(1,periodo);
        return query31.getResultList();

    }

    public List<Object[]> getIntIfrs9(String periodo){

        Query query32 = entityManager.createNativeQuery("select yntp_empresa_reportante, cod_neocon, divisa, divisa_neocon, yntp, sociedad_yntp, contrato, nit, cod_pais, pais, cuenta_local, abs(round(cast(sum(valor)/1000 as decimal(18,2)), 0)) saldo\n" +
                "from /*nexco_intergrupo_v3*/ nexco_intergrupo_v3_def a\n" +
                "left join nexco_divisas b\n" +
                "on a.divisa = b.id_divisa\n" +
                "where periodo = ?\n" +
                "group by yntp_empresa_reportante, cod_neocon, divisa, divisa_neocon, yntp, sociedad_yntp, contrato, nit, cod_pais, pais, cuenta_local\n" +
                ";");
        query32.setParameter(1,periodo);
        return query32.getResultList();

    }

    public List<Object[]> getAdjHom(String periodo){

        Query query41 = entityManager.createNativeQuery("select tipo_asiento, descripcion_asiento, codicons, sociedad_ic, descripcion_ic, divisa, \n" +
                "saldo_debe1,saldo_debe2,saldo_haber1,saldo_haber2, (saldo_debe1+saldo_debe2)-(saldo_haber1+saldo_haber2) saldo \n" +
                "from nexco_ajustes_hom \n" +
                "where periodo = ?\n" +
                ";");
        query41.setParameter(1,periodo);
        return query41.getResultList();
    }

    public List<Object[]> getAdjMan(String periodo){

        Query query42 = entityManager.createNativeQuery("select codicons, divisa_espana, saldo\n" +
                "from nexco_ajustes_manuales\n" +
                "where periodo = ? \n" +
                ";");
        query42.setParameter(1,periodo);
        return query42.getResultList();
    }

    public List<Object[]> validateAccounts(String periodo, String opcionQ, String empresa) {

        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        Query query = entityManager.createNativeQuery(
        "select distinct codicons, 'La Cuenta Neocon no se encuentra en la parametría de Indicadores de Cuenta' mensaje from nexco_query_marcados a\n" +
                "inner join (select * from nexco_cuentas_neocon) b \n" +
                "on a.codicons = b.cuenta\n" +
                "where YEAR(fecont) = ? AND MONTH(fecont) = ? AND empresa = '0060'\n" +
                "and codicons not in (select distinct cuenta_neocon from nexco_indicadores) and codicons <> '00000' and salmes <> 0\n" +
                ";");

        query.setParameter(1,anio);
        query.setParameter(2,mes);

        return query.getResultList();

    }

    public List<Object[]> getCompany(String periodo){

        Query companies = entityManager.createNativeQuery(
                "select distinct empresa from nexco_query_marcados where substring(fecont, 1, 7) = ? \n");
        companies.setParameter(1,periodo);
        List<Object[]> companiesList = companies.getResultList();

        if(companiesList.isEmpty()){

            Query companies2 = entityManager.createNativeQuery(
                    "select distinct empresa from nexco_query where substring(fecont, 1, 7) = ? \n");
            companies.setParameter(1,periodo);
            List<Object[]> companiesList2 = companies.getResultList();

            return companiesList2;

        } else {

            return companiesList;

        }
    }

    public List<ValQueryEEFF> getCuadreQueryEEFF(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_query_eeff a where periodo = ? ",ValQueryEEFF.class);
        getQuery.setParameter(1, periodo);
        return getQuery.getResultList();
    }

    public List<Object[]> getCuadreQueryEEFFGroup(String periodo){
        Query getQuery = entityManager.createNativeQuery("select a.codigo_consolidacion, a.descripcion, a.descripcion_ic, a.divisa,a.fecha_proceso_ifrs9,a.fecha_proceso_local,a.intergrupo,a.perimetro,a.saldo_dif_eeff_inter_ifrs9,a.saldo_dif_eeff_inter_local,a.saldo_dif_ifrs9,a.saldo_dif_local,a.saldo_eeff_ifrs9,a.saldo_eeff_local,a.saldo_inter_ifrs9,a.saldo_inter_local,saldo_query_ifrs9,a.saldo_query_local,a.sociedad_ic,a.tipo_cuenta,a.saldo_ajuste_hom,a.saldo_ajuste_man, a.periodo from nexco_query_eeff a where periodo = ? ");
        getQuery.setParameter(1, periodo);
        return getQuery.getResultList();
    }

    public void clearCuadreQueryEEFF(User user, String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_query_eeff where periodo = ?", ValQueryEEFF.class);
        query.setParameter(1, periodo);
        query.executeUpdate();
    }

}
