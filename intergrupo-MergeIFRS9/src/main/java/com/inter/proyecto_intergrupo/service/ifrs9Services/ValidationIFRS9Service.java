package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ValIFRS9;
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
public class ValidationIFRS9Service {

    @PersistenceContext
    EntityManager entityManager;

    public void valIFRS9(String periodo) {

        String[] arrayPeriodo = periodo.split("-");
        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        try {

            Query delete1 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_provrec_temp");
            delete1.executeUpdate();

            Query delete2 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_recla_temp");
            delete2.executeUpdate();

            Query delete3 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_ajus_temp");
            delete3.executeUpdate();

            Query delete4 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_primera_temp");
            delete4.executeUpdate();

            Query delete5 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_eeffloc_temp");
            delete5.executeUpdate();

            Query delete6 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_queryloc_temp");
            delete6.executeUpdate();

            Query delete7 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_eeff_temp");
            delete7.executeUpdate();

            Query delete8 = entityManager.createNativeQuery("TRUNCATE TABLE nexco_val_ifrs9_query_temp");
            delete8.executeUpdate();

            Query delete9 = entityManager.createNativeQuery("delete from nexco_val_ifrs9 where periodo = ?");
            delete9.setParameter(1,periodo);
            delete9.executeUpdate();

            //Provisiones y reclasificaciones
            Query insert1 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_provrec_temp (codicons, divisa, sdo_prov, sdo_rec) \n" +
                    "select ISNULL(z.CONSOL, y.CONSOL) codicons, ISNULL(z.DIV_2, y.DIV_2) divisa, round(sum(ISNULL(z.vr_miles, 0)),0) sdo_prov, round(sum(ISNULL(y.vr_miles, 0)),0) sdo_rec \n" +
                    "from (select a.cuenta, ISNULL(b.CODICONS46,e.codicons) CONSOL, c.divisa_neocon DIV_2, a.vr_miles\n" +
                    "from (select * from nexco_validacion_descon_carga where periodo = ?) a\n" +
                    "left join (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join nexco_divisas c\n" +
                    "on a.div = c.id_divisa\n" +
                    "left join (select distinct nombre_archivo, tipo_proceso, descripcion from nexco_tipo_plantilla_esp) d\n" +
                    "on d.nombre_archivo = substring(a.nombre_real, 1, charindex('.', a.nombre_real)-1)\n" +
                    "left join (select nucta,codicons from nexco_query_marcados where fecont like ? and origen='IFRS9' and empresa = '0060' group by nucta,codicons) e\n" +
                    "on a.cuenta = e.nucta\n" +
                    "where d.descripcion = 'PROV' and a.cuenta not like '1960%' and (b.codicons46 <> '27508' or e.codicons <> '27508') ) z\n" +
                    "FULL join (select a.cuenta, ISNULL(b.CODICONS46,e.codicons) CONSOL, c.divisa_neocon DIV_2, a.vr_miles\n" +
                    "from (select * from nexco_validacion_descon_carga where periodo = ?) a\n" +
                    "left join (SELECT puc.CODICONS46, puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join nexco_divisas c\n" +
                    "on a.div = c.id_divisa\n" +
                    "left join (select distinct nombre_archivo, tipo_proceso, descripcion from nexco_tipo_plantilla_esp) d\n" +
                    "on d.nombre_archivo = substring(a.nombre_real, 1, charindex('.', a.nombre_real)-1)\n" +
                    "left join (select nucta,codicons from nexco_query_marcados where fecont like ? and origen='IFRS9' and empresa = '0060' group by nucta,codicons) e\n" +
                    "on a.cuenta = e.nucta\n" +
                    "where d.descripcion = 'REC') y\n" +
                    "on z.CONSOL = y.CONSOL and z.DIV_2 = y.DIV_2\n" +
                    "group by ISNULL(z.CONSOL, y.CONSOL), ISNULL(z.DIV_2, y.DIV_2) \n" +
                    ";");
            insert1.setParameter(1,periodo);
            insert1.setParameter(2,periodo+"%");
            insert1.setParameter(3,periodo);
            insert1.setParameter(4,periodo+"%");
            insert1.executeUpdate();

            //Reclasificaciones
            Query insert2 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_recla_temp (codicons, divisa, saldo) \n" +
                    "select ISNULL(b.CODICONS46,e.codicons) codicons, c.divisa_neocon divisa, round((sum(a.importe)/1000)*-1,0) saldo \n" +
                    "from ( \n" +
                    "select cuenta, divisa, importe \n" +
                    "from nexco_carga_masiva_intergrupo_v2\n" +
                    "where periodo_origen = ? and tipo_info <> 'REV') a\n" +
                    "left join (SELECT puc.CODICONS46, puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join (select nucta,codicons from nexco_query_marcados where fecont like ? and origen='IFRS9' and empresa = '0060' group by nucta,codicons) e\n" +
                    "on a.cuenta = e.nucta\n" +
                    "left join nexco_divisas c\n" +
                    "on a.divisa = c.id_divisa\n" +
                    "group by ISNULL(b.CODICONS46,e.codicons), c.divisa_neocon\n" +
                    "having sum(a.importe) not between -1 and 1 \n" +
                    ";");
            insert2.setParameter(1,periodo);
            insert2.setParameter(2,periodo+"%");
            insert2.executeUpdate();

            //Ajustes Manuales
            Query insert3 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_ajus_temp (codicons, divisa, saldo, perimetro) \n" +
                    "select codicons, divisa_espana divisa, round(sum(saldo),0) saldo, fuente\n" +
                    "from nexco_ajustes_manuales where periodo = ?\n" +
                    "group by fuente,codicons, divisa_espana \n" +
                    ";");
            insert3.setParameter(1,periodo);
            insert3.executeUpdate();

            //Tanque primera vez y perdida incurrida
            Query insert4 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_primera_temp (divisa, codicons, saldo) \n" +
                    "select (select TOP 1 divisa_neocon from nexco_divisas where id_divisa = 'COP'  ) divisa, ISNULL(b.CODICONS46,e.codicons) codicons, round(sum(diferencia_pesos)/1000,0) saldo\n" +
                    "from nexco_ajuste_primera_vez a\n" +
                    "left join \n" +
                    "(SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join (select nucta,codicons from nexco_query_marcados where fecont like ? and origen='IFRS9' and empresa = '0060' group by nucta,codicons) e\n" +
                    "on a.cuenta = e.nucta\n" +
                    "WHERE SUBSTRING(a.cuenta,1,4) != '1960'\n" +
                    "group by ISNULL(b.CODICONS46,e.codicons)\n" +
                    "UNION ALL\n" +
                    "select (select TOP 1 divisa_neocon from nexco_divisas where id_divisa = 'COP'  ) divisa, a.codigo_consolidacion codicons, round(sum(a.saldo*-1)/1000,0) saldo\n" +
                    "from nexco_perdidaincurrida a \n" +
                    "group by a.codigo_consolidacion\n" +
                    ";");
            insert4.setParameter(1,periodo+"%");
            insert4.executeUpdate();

            //eeff local
            Query insert5 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_eeffloc_temp (cuenta, divisa, saldo) \n" +
                    "SELECT cuenta, divisa, SUM(saldo) AS saldo \n" +
                    "FROM (SELECT C.cuenta, C.Divisa_espana as divisa, CASE  \n" +
                    "WHEN LTRIM(RTRIM(C.saldo)) = '' THEN \n" +
                    "CAST(0.00 AS DECIMAL) \n" +
                    "ELSE \n" +
                    "CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                    "FROM nexco_eeff AS C \n" +
                    "LEFT JOIN nexco_cuentas_neocon D\n" +
                    "ON c.cuenta = d.cuenta \n" +
                    "WHERE saldo <> '' and descripcion_ic = '' and upper(tipo) = 'LOCAL' AND period = ? and d.entrada = 'S') AS A \n" +
                    "GROUP BY cuenta, divisa \n" +
                    ";");
            insert5.setParameter(1,periodo);
            insert5.executeUpdate();

            //query local
            Query insert6 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_queryloc_temp (cuenta, divisa, saldo) \n" +
                    "SELECT q.codicons as cuenta, q.divisa as divisa, case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo\n" +
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
                    "GROUP BY q.codicons, q.divisa, q.fecha_proceso, i.signo \n" +
                    ";");
            insert6.setParameter(1,anio);
            insert6.setParameter(2,mes);
            insert6.executeUpdate();

            //eeff ifrs9
            Query insert7 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_eeff_temp (cuenta, divisa, saldo) \n" +
                    "SELECT cuenta, divisa, SUM(saldo) AS saldo \n" +
                    "FROM (SELECT C.cuenta, C.Divisa_espana as divisa, CASE  \n" +
                    "WHEN LTRIM(RTRIM(C.saldo)) = '' THEN \n" +
                    "CAST(0.00 AS DECIMAL) \n" +
                    "ELSE \n" +
                    "CAST(REPLACE(TRIM(C.saldo),',','.') AS DECIMAL(18,2)) END AS saldo \n" +
                    "FROM nexco_eeff AS C \n" +
                    "LEFT JOIN nexco_cuentas_neocon D\n" +
                    "ON c.cuenta = d.cuenta \n" +
                    "WHERE saldo <> '' and descripcion_ic = '' and upper(tipo) = 'IFRS9' AND period = ? and d.entrada = 'S') AS A \n" +
                    "GROUP BY cuenta, divisa  \n" +
                    ";");
            insert7.setParameter(1,periodo);
            insert7.executeUpdate();

            //query ifrs9
            Query insert8 = entityManager.createNativeQuery("insert into nexco_val_ifrs9_query_temp (cuenta, divisa, saldo) \n" +
                    /*"SELECT q.codicons as cuenta, q.divisa as divisa, case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo \n" +*/
                    "SELECT q.codicons as cuenta, q.divisa as divisa, case when q.codicons='50131' and q.divisa='USD' then ROUND(SUM(q.saldo)/1000,0)*(-1) when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end AS saldo  \n" +
                    "FROM (SELECT a.codicons, a.divisa, \n" +
                    "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                    "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                    "FROM nexco_query_marcados a \n" +
                    "inner join (select * from nexco_cuentas_neocon) c \n" +
                    "on a.codicons = c.cuenta \n" +
                    "WHERE origen = 'IFRS9' AND YEAR(fecont) = ? AND MONTH(fecont) = ? \n" +
                    "AND empresa = '0060' and codicons <> '00000' and saldoquery <> 0) AS q \n" +
                    "left join nexco_indicadores i\n" +
                    "on q.codicons = i.cuenta_neocon\n" +
                    "GROUP BY q.codicons, q.divisa, q.fecha_proceso, i.signo \n" +
                    ";");
            insert8.setParameter(1,anio);
            insert8.setParameter(2,mes);
            insert8.executeUpdate();

            Query insertFinal = entityManager.createNativeQuery("insert into nexco_val_ifrs9 (codicons, divisa, perimetro, sdo_prov, sdo_rec, sdo_pri, sdo_recla, sdo_aj, sdo_total_plantilla, naturaleza_total, sdo_eeff_loc, sdo_query_loc, sdo_diff_loc, sdo_nuevo, sdo_eeff_ifrs9, sdo_query_ifrs9, sdo_diff_query, sdo_diff_eeff, periodo) \n" +
                    "select coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta) codicons, \n" +
                    "coalesce(m.divisa, n.divisa, o.divisa, p.divisa, q.divisa, r.divisa, s.divisa, t.divisa) divisa, case when z.cuenta_neocon is not null then z.ifrs9 else '' end perimetro, \n" +
                    "isnull(m.sdo_prov, 0)/*-isnull(w.saldo, 0)*/ sdo_prov, isnull(m.sdo_rec, 0)/*-isnull(l.saldo, 0)*/ sdo_rec, isnull(p.saldo, 0) sdo_pri, isnull(n.saldo, 0) sdo_recla, CASE WHEN w.perimetro = 'PROV' THEN isnull(w.saldo, 0) ELSE isnull(l.saldo, 0) END  sdo_aj,\n" +
                    "CASE WHEN (CASE WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) = '5' THEN 'D' WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) = '5' THEN 'H' ELSE (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) END) = 'D' THEN abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0)) ELSE abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0))*-1 END  total_plantilla, \n" +
                    "CASE WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) = '5' THEN 'D' WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) = '5' THEN 'H' ELSE (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) END naturaleza_total , \n" +
                    "isnull(q.saldo, 0) eeff_local, isnull(r.saldo, 0) query_local, isnull(q.saldo, 0)-isnull(r.saldo, 0) diff_local, \n" +
                    "isnull(q.saldo, 0) + CASE WHEN (CASE WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) = '5' THEN 'D' WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) = '5' THEN 'H' ELSE (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) END) = 'D' THEN abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0)) ELSE abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0))*-1 END nuevo_saldo, \n" +
                    //"isnull(q.saldo, 0)+isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) nuevo_saldo, \n" +
                    "isnull(s.saldo, 0) eeff_ifrs9, \n" +
                    "isnull(t.saldo, 0) query_ifrs9, " +
                    "isnull(q.saldo, 0) + CASE WHEN (CASE WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) IN ('5','2') THEN 'D' WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) IN ('5','2') THEN 'H' ELSE (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) END) = 'D' THEN abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0)) ELSE abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0))*-1 END-isnull(t.saldo, 0) diff_query,\n" +
                    "isnull(q.saldo, 0) + CASE WHEN (CASE WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) IN ('5','2') THEN 'D' WHEN (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta),1,1) IN ('5','2') THEN 'H' ELSE (case when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '0' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >= 0 then 'D' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' when zz.naturaleza = '1' and isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) >=0 then 'D' else case when isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0) <0 then 'H' else 'D' end end) END) = 'D' THEN abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0)) ELSE abs(isnull(m.sdo_prov, 0)+isnull(m.sdo_rec, 0)+isnull(p.saldo, 0)+isnull(n.saldo, 0)+isnull(o.saldo, 0))*-1 END-isnull(s.saldo, 0) diff_eeff, ? \n" +
                    "from nexco_val_ifrs9_provrec_temp\n" +
                    "m full join nexco_val_ifrs9_recla_temp n\n" +
                    "on m.codicons = n.codicons and m.divisa = n.divisa\n" +
                    "full join (select codicons, divisa, round(sum(saldo),0) saldo from nexco_val_ifrs9_ajus_temp group by codicons, divisa) o\n" +
                    "on coalesce(m.codicons, n.codicons) = o.codicons \n" +
                    "and coalesce(m.divisa, n.divisa) = o.divisa\n" +
                    "full join (select * from nexco_val_ifrs9_ajus_temp where perimetro = 'PROV') w\n" +
                    "on coalesce(m.codicons, n.codicons) = w.codicons \n" +
                    "and coalesce(m.divisa, n.divisa) = w.divisa\n" +
                    "full join (select * from nexco_val_ifrs9_ajus_temp where perimetro = 'REC') l\n" +
                    "on coalesce(m.codicons, n.codicons) = l.codicons \n" +
                    "and coalesce(m.divisa, n.divisa) = l.divisa\n" +
                    "full join nexco_val_ifrs9_primera_temp p\n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons) = p.codicons\n" +
                    "and coalesce(m.divisa, n.divisa, o.divisa) = p.divisa\n" +
                    "full join nexco_val_ifrs9_eeffloc_temp q\n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons, p.codicons) = q.cuenta \n" +
                    "and coalesce(m.divisa, n.divisa, o.divisa, p.divisa) = q.divisa\n" +
                    "left join nexco_val_ifrs9_queryloc_temp r\n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta) = r.cuenta \n" +
                    "and coalesce(m.divisa, n.divisa, o.divisa, p.divisa, q.divisa) = r.divisa\n" +
                    "left join nexco_val_ifrs9_eeff_temp s\n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta) = s.cuenta \n" +
                    "and coalesce(m.divisa, n.divisa, o.divisa, p.divisa, q.divisa, r.divisa) = s.divisa\n" +
                    "left join nexco_val_ifrs9_query_temp t\n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta) = t.cuenta \n" +
                    "and coalesce(m.divisa, n.divisa, o.divisa, p.divisa, q.divisa, r.divisa, s.divisa) = t.divisa\n" +
                    "left join nexco_provisiones z\n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta) = z.cuenta_neocon \n" +
                    "left join nexco_cuentas_neocon zz \n" +
                    "on coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta) = zz.cuenta \n" +
                    "where coalesce(m.codicons, n.codicons, o.codicons, p.codicons, q.cuenta, r.cuenta, s.cuenta, t.cuenta) <> '00000'\n" +
                    ";");
            insertFinal.setParameter(1,periodo);
            insertFinal.executeUpdate();


        } catch (NoResultException e) {
            e.printStackTrace();
        }
    }

    public List<Object[]> validateAccounts(String periodo, String opcionQ, String empresa) {

        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        Query query = entityManager.createNativeQuery(
        "select distinct codicons, 'La Cuenta Neocon no se encuentra en la parametría de Indicadores de Cuenta' mensaje from nexco_query_marcados a\n" +
                "inner join (select * from nexco_cuentas_neocon) b \n" +
                "on a.codicons = b.cuenta\n" +
                "where origen = ? \n" +
                "AND YEAR(fecont) = ? AND MONTH(fecont) = ? AND empresa = ?\n" +
                "and codicons not in (select distinct cuenta_neocon from nexco_indicadores) and codicons <> '00000' and salmes <> 0\n" +
                ";");

        query.setParameter(1,opcionQ);
        query.setParameter(2,anio);
        query.setParameter(3,mes);
        query.setParameter(4,empresa);

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

    public List<Object[]> getPlanos (String periodo) {

        Query query11 = entityManager.createNativeQuery("select a.cuenta, ISNULL(ISNULL(b.CODICONS46,e.codicons),e2.codicons) CONSOL, A.div, c.divisa_neocon div_2, a.vr_miles, tasa_conversion, observacion, nombre_real\n" +
                "from (select * from nexco_validacion_descon_carga where periodo = ?) a\n" +
                "left join (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                "on a.cuenta = b.NUCTA\n" +
                "left join nexco_divisas c\n" +
                "on a.div = c.id_divisa\n" +
                "left join (select distinct nombre_archivo, tipo_proceso, descripcion from nexco_tipo_plantilla_esp) d\n" +
                "on d.nombre_archivo = substring(a.nombre_real, 1, charindex('.', a.nombre_real)-1)\n" +
                "left join (select nucta,codicons from nexco_query_marcados where fecont like ? and origen='IFRS9' and empresa = '0060' group by nucta,codicons) e\n" +
                "on a.cuenta = e.nucta\n" +
                "left join (select nucta,codicons from nexco_query where empresa = '0060' group by nucta,codicons) e2\n" +
                "on a.cuenta = e2.nucta\n"+
                "where d.descripcion in ('PROV', 'REC')\n" +
                "order by 8, 1, 2\n" +
                ";");
        query11.setParameter(1, periodo);
        query11.setParameter(2, periodo+"%");
        return query11.getResultList();

    }
    public List<Object[]> getTableIntergrupo(String periodo) {
        Query query1= entityManager.createNativeQuery("select A.contrato,A.cuenta,A.divisa,A.empresa,A.fecha_contable,A.fecha,A.importe_c,A.importe_d, A.importe_cd,A.importe_dd,A.importe_cd_exp,A.importe_dd_exp,A.importe_total, CASE WHEN A.origen = 'INTERGRUPO_PROVIS.TXT' THEN 'PROVISIONES' ELSE 'RECLASIFICACIONES' END tipoO, B.CODICONS46,C.yntp\n" +
                "FROM nexco_plano_ifrs9_intergrupo A\n" +
                "LEFT JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE EMPRESA='0013' GROUP BY NUCTA,CODICONS46) B ON A.cuenta = B.NUCTA\n" +
                "LEFT JOIN (SELECT contrato,yntp FROM nexco_intergrupo_v2 WHERE periodo = ? group by contrato,yntp) C ON A.contrato = C.contrato\n" +
                "WHERE origen IN ('INTERGRUPO_PROVIS.TXT', 'INTERGRUPO_RECLAS.TXT') AND A.periodo = ?");
        query1.setParameter(1,periodo);
        query1.setParameter(2,periodo);
        return query1.getResultList();
    }
    public List<Object[]> getDivisas(String periodo) {

        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        Query query11 = entityManager.createNativeQuery("select fecha, divisa, valor from nexco_divisas_valor \n" +
                "where YEAR(fecha) = ? AND MONTH(fecha) = ?\n" +
                "order by 2\n" +
                ";");
        query11.setParameter(1, anio);
        query11.setParameter(2, mes);
        return query11.getResultList();

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

        Query query21 = entityManager.createNativeQuery("SELECT q.empresa, q.nucta cuenta_local, q.codicons as codicons, fecont, coddiv, q.divisa as divisa,/* case when i.signo = '+' then abs(ROUND(SUM(q.saldo)/1000,0)) when i.signo = '-' then abs(ROUND(SUM(q.saldo)/1000,0))*(-1) else abs(ROUND(SUM(q.saldo)/1000,0)) end*/ sum(q.saldo) AS saldo, q.fecha_proceso \n" +
                "FROM (SELECT a.empresa, a.nucta, a.fecont, a.coddiv, a.codicons, a.divisa, \n" +
                "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                "FROM nexco_query_marcados a \n" +
                "inner join (select * from nexco_cuentas_neocon) c \n" +
                "on a.codicons = c.cuenta \n" +
                "WHERE origen = 'LOCAL' AND YEAR(fecont) = ? AND MONTH(fecont) = ?\n" +
                "AND empresa = '0060' and codicons <> '00000' and salmes <> 0) AS q \n" +
                "/*left join nexco_indicadores i\n" +
                "on q.codicons = i.cuenta_neocon\n*/" +
                "GROUP BY q.empresa, q.nucta, fecont, coddiv, q.codicons, q.divisa, q.fecha_proceso/*, i.signo*/\n" +
                ";");
        query21.setParameter(1,anio);
        query21.setParameter(2,mes);
        return query21.getResultList();

    }

    public List<Object[]> getQueryIfrs9(String periodo){

        String[] arrayPeriodo = periodo.split("-");

        String anio = arrayPeriodo[0];
        String mes = arrayPeriodo[1];

        Query query22 = entityManager.createNativeQuery("SELECT q.empresa, q.nucta cuenta_local, q.codicons as codicons, fecont, coddiv, q.divisa as divisa, sum(q.saldo) AS saldo, q.fecha_proceso \n" +
                "FROM (SELECT a.empresa, a.nucta, a.fecont, a.coddiv, a.codicons, a.divisa, \n" +
                "cast(a.saldoquery AS DECIMAL(18,2)) AS saldo, \n" +
                "convert(varchar, a.fechproce, 23) fecha_proceso \n" +
                "FROM nexco_query_marcados a \n" +
                "inner join (select * from nexco_cuentas_neocon) c \n" +
                "on a.codicons = c.cuenta \n" +
                "WHERE origen = 'IFRS9' AND YEAR(fecont) = ? AND MONTH(fecont) = ?\n" +
                "AND empresa = '0060' and codicons <> '00000' and salmes <> 0) AS q \n" +
                "GROUP BY q.empresa, q.nucta, fecont, coddiv, q.codicons, q.divisa, q.fecha_proceso\n" +
                ";");
        query22.setParameter(1,anio);
        query22.setParameter(2,mes);
        return query22.getResultList();

    }

    public List<ValIFRS9> getValIFRS9(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_val_ifrs9 where periodo = ? order by codicons, divisa",ValIFRS9.class);
        getQuery.setParameter(1, periodo);
        return getQuery.getResultList();
    }

    public List<ValIFRS9>  getValIFRS9Filter(String periodo){
        /*Query getQuery = entityManager.createNativeQuery("select * from nexco_val_ifrs9 A\n" +
                "inner join (select H.codicons, H.divisa from\n" +
                "(select codicons,divisa from nexco_val_ifrs9_ajus_temp Z \n" +
                "union all\n" +
                "select T.cod_neocon,U.divisa_neocon from nexco_validacion_descon_carga T\n" +
                "left join nexco_divisas U ON T.div = U.id_divisa WHERE periodo = ? and T.cod_neocon is not null group by T.cod_neocon,U.divisa_neocon) H \n" +
                "GROUP BY H.codicons, H.divisa) B \n" +
                "ON A.codicons=B.codicons and A.divisa = B.divisa\n" +
                "where periodo = ? AND (A.sdo_prov <> 0 OR A.sdo_recla <> 0 OR A.sdo_rec <> 0 OR sdo_aj <> 0 OR A.sdo_pri <> 0) \n" +
                "order by A.codicons, A.divisa",ValIFRS9.class);
        getQuery.setParameter(1, periodo);
        getQuery.setParameter(2, periodo);
        return getQuery.getResultList();*/

        Query getQuery = entityManager.createNativeQuery("select * from nexco_val_ifrs9 A\n" +
                "inner join (select H.codicons, H.divisa from\n" +
                "(select codicons,divisa from nexco_val_ifrs9_ajus_temp Z \n" +
                "union all\n" +
                "select ISNULL(ISNULL(b.CODICONS46,e.codicons),e2.codicons) as cod_neocon, c.divisa_neocon as divisa_neocon\n" +
                "from (select * from nexco_validacion_descon_carga where periodo = ?) a\n" +
                "left join (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                "on a.cuenta = b.NUCTA\n" +
                "left join nexco_divisas c\n" +
                "on a.div = c.id_divisa\n" +
                "left join (select distinct nombre_archivo, tipo_proceso, descripcion from nexco_tipo_plantilla_esp) d\n" +
                "on d.nombre_archivo = substring(a.nombre_real, 1, charindex('.', a.nombre_real)-1)\n" +
                "left join (select nucta,codicons from nexco_query_marcados where fecont like ? and origen='IFRS9' and empresa = '0060' group by nucta,codicons) e\n" +
                "on a.cuenta = e.nucta\n" +
                "left join (select nucta,codicons from nexco_query where empresa = '0060' group by nucta,codicons) e2\n" +
                "on a.cuenta = e2.nucta\n" +
                "group by ISNULL(ISNULL(b.CODICONS46,e.codicons),e2.codicons), c.divisa_neocon) H \n" +
                "GROUP BY H.codicons, H.divisa) B \n" +
                "ON A.codicons=B.codicons and A.divisa = B.divisa\n" +
                "where periodo = ? AND (A.sdo_prov <> 0 OR A.sdo_recla <> 0 OR A.sdo_rec <> 0 OR sdo_aj <> 0 OR A.sdo_pri <> 0) \n" +
                "order by A.codicons, A.divisa",ValIFRS9.class);
        getQuery.setParameter(1, periodo);
        getQuery.setParameter(2, periodo+"%");
        getQuery.setParameter(3, periodo);
        return getQuery.getResultList();
    }

    public void clearValIFRS9(User user, String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_val_ifrs9 where periodo = ?", ValIFRS9.class);
        query.setParameter(1, periodo);
        query.executeUpdate();
    }

}
