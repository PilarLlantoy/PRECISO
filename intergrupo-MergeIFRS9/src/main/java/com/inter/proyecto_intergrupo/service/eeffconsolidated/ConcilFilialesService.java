package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.eeffConsolidated.ConcilFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.FiduciariaeeffFiliales;
import com.inter.proyecto_intergrupo.model.ifrs9.IFRS9ConcilAccount;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ConcilFilialaesRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.FiduciariaEeffConsolidatedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
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
public class ConcilFilialesService {
    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private ConcilFilialaesRepository concilFilialaesRepository;
    public List<ConcilFiliales> getEeffConsolidatedDataByPeriod(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_concil_filiales where periodo = ? order by cuenta;",ConcilFiliales.class);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<Object[]> getEeffConsolidatedDataByPeriod2(String periodo, String level,String level_general) {
        Query consulta = entityManager.createNativeQuery("select "+level+" /*,coalesce(b.cod_cons,c.cod_cons,d.codicons46,'') as codicons1*/,coalesce(b.nombre_cuenta,c.nombre_cuenta,d.derecta,'') as nombre,a.moneda,sum(banco) as banco,sum(fiduciaria) as fiduciaria,sum(valores) as valores,sum(total) as total,sum(debe_ajustes_mayores) as debe_ajustes_mayores, sum(haber_ajustes_mayores) as haber_ajustes_mayores, sum(total_ifrs_2) as total_ifrs,\n" +
                "sum(debe_total) as debe_total,sum(haber_total) as haber_total,sum(total_consol_2) as total_consol,a.periodo\n" +
                "from nexco_concil_filiales a\n" +
                "left join (select * from nexco_puc_fiduciaria_filiales) b on a."+level_general+" = b.cuenta\n" +
                "left join (select * from nexco_puc_valores_filiales) c on a."+level_general+" = c.id_cuenta\n" +
                "left join (select * from cuentas_puc where empresa='0013') d on a."+level_general+" = d.nucta\n" +
                "where a.periodo = ? group by "+level+" /*,coalesce(b.cod_cons,c.cod_cons,d.codicons46,'')*/,coalesce(b.nombre_cuenta,c.nombre_cuenta,d.derecta,''),a.moneda,a.periodo order by "+level);
        consulta.setParameter(1,periodo);
        return consulta.getResultList();
    }

    public List<Object[]> getDetalleEliminaciones(String periodo) {
        Query consulta = entityManager.createNativeQuery("select l_4,l_9,empresa,cuenta as cuenta1 ,moneda,isnull(debe,0) as debe,isnull(haber,0) as haber, isnull(debe,0)+isnull(haber,0) as saldo, 'ELIMINACIONES INTERGRUPO' as concepto \n" +
                "from nexco_concil_filiales where periodo = :periodo  and ((debe is not null and debe != 0) or (haber is not null and haber != 0))\n" +
                "union all\n" +
                "select l_4,l_9,empresa,cuenta as cuenta1,moneda,isnull(debe_patrimonio,0) as debe,isnull(haber_patrimonio,0) as haber, isnull(debe_patrimonio,0)+isnull(haber_patrimonio,0) as saldo, 'ELIMINACIONES PATRIMONIO' as concepto \n" +
                "from nexco_concil_filiales where periodo = :periodo  and ((debe_patrimonio is not null and debe_patrimonio != 0) or (haber_patrimonio is not null and haber_patrimonio != 0))\n" +
                "union all\n" +
                "select l_4,l_9,empresa,cuenta as cuenta1,moneda,isnull(debe_ajustes_minimos,0) as debe,isnull(haber_ajustes_minimos,0) as haber, isnull(debe_ajustes_minimos,0)+isnull(haber_ajustes_minimos,0) as saldo, 'AJUSTES' as concepto \n" +
                "from nexco_concil_filiales where periodo = :periodo  and ((debe_ajustes_minimos is not null and debe_ajustes_minimos != 0) or (haber_ajustes_minimos is not null and haber_ajustes_minimos != 0))\n" +
                "union all\n" +
                "select l_4,l_9,empresa,cuenta as cuenta1,moneda,isnull(debe_ajustes_mayores,0) as debe,isnull(haber_ajustes_mayores,0) as haber, isnull(debe_ajustes_mayores,0)+isnull(haber_ajustes_mayores,0) as saldo, 'AJUSTES IFRS' as concepto \n" +
                "from nexco_concil_filiales where periodo = :periodo  and ((debe_ajustes_mayores is not null and debe_ajustes_mayores != 0) or (haber_ajustes_mayores is not null and haber_ajustes_mayores != 0))\n" +
                "union all \n" +
                "select l_4,l_9,empresa,cuenta as cuenta1,moneda,isnull(debe_ver_pt,0) as debe,isnull(haber_ver_pt,0) as haber, isnull(debe_ver_pt,0)+isnull(haber_ver_pt,0) as saldo, 'ELIMINACIONES VER PT' as concepto \n" +
                "from nexco_concil_filiales where periodo = :periodo  and ((debe_ver_pt is not null and debe_ver_pt != 0) or (haber_ver_pt is not null and haber_ver_pt != 0))\n" +
                "\n");
        consulta.setParameter("periodo",periodo);
        return consulta.getResultList();
    }

    public List<Object[]> getMayorizaAccion(String periodo) {
        Query consulta = entityManager.createNativeQuery("select z.cuenta,COALESCE(w.nombre_cuenta,x.nombre_cuenta,y.derecta,'') as descripcion,z.banco,z.fiduciaria,z.valores,z.eliminacion,z.ML,z.ME,z.MT,z.nivel\n" +
                "from (select l_1 as cuenta, sum(banco) as banco, sum(valores) as valores, sum(fiduciaria) as fiduciaria, sum(total_ifrs)-sum(total) as eliminacion,sum(case when moneda = 'ML' then total_ifrs else 0 end) as ML, \n" +
                "sum(case when moneda = 'ME' then total_ifrs else 0 end) as ME,sum(total_ifrs) as MT, len(l_1) as nivel from nexco_concil_filiales where periodo = :periodo  group by l_1\n" +
                "union\n" +
                "select l_2 as cuenta, sum(banco) as banco, sum(valores) as valores, sum(fiduciaria) as fiduciaria, sum(total_ifrs)-sum(total) as eliminacion,sum(case when moneda = 'ML' then total_ifrs else 0 end) as ML, \n" +
                "sum(case when moneda = 'ME' then total_ifrs else 0 end) as ME,sum(total_ifrs) as MT, len(l_2) as nivel from nexco_concil_filiales where periodo = :periodo  group by l_2\n" +
                "union\n" +
                "select l_4 as cuenta, sum(banco) as banco, sum(valores) as valores, sum(fiduciaria) as fiduciaria, sum(total_ifrs)-sum(total) as eliminacion,sum(case when moneda = 'ML' then total_ifrs else 0 end) as ML, \n" +
                "sum(case when moneda = 'ME' then total_ifrs else 0 end) as ME,sum(total_ifrs) as MT, len(l_4) as nivel from nexco_concil_filiales where periodo = :periodo  group by l_4\n" +
                "union\n" +
                "select l_6 as cuenta, sum(banco) as banco, sum(valores) as valores, sum(fiduciaria) as fiduciaria, sum(total_ifrs)-sum(total) as eliminacion,sum(case when moneda = 'ML' then total_ifrs else 0 end) as ML, \n" +
                "sum(case when moneda = 'ME' then total_ifrs else 0 end) as ME,sum(total_ifrs) as MT, len(l_6) as nivel from nexco_concil_filiales where periodo = :periodo  group by l_6\n" +
                "union\n" +
                "select l_9 as cuenta, sum(banco) as banco, sum(valores) as valores, sum(fiduciaria) as fiduciaria, sum(total_ifrs)-sum(total) as eliminacion,sum(case when moneda = 'ML' then total_ifrs else 0 end) as ML, \n" +
                "sum(case when moneda = 'ME' then total_ifrs else 0 end) as ME,sum(total_ifrs) as MT, len(l_9) as nivel from nexco_concil_filiales where periodo = :periodo  group by l_9) z\n" +
                "left join nexco_puc_fiduciaria_filiales w on z.cuenta=w.cuenta\n" +
                "left join nexco_puc_fiduciaria_filiales x on z.cuenta=x.cuenta \n" +
                "left join (select nucta,derecta from cuentas_puc where empresa= '0013') y on z.cuenta=y.nucta ORDER BY 1 ");
        consulta.setParameter("periodo",periodo);
        return consulta.getResultList();
    }

    public List<Object[]> getLevel1(String periodo) {
        Query consulta = entityManager.createNativeQuery("select l_1, sum (Total) as total from nexco_concil_filiales where periodo = ? group by L_1 \n" +
                "union all\n" +
                "select 'Total' as L_1, sum (Total) as total from nexco_concil_filiales where periodo = ? order by 1");
        consulta.setParameter(1,periodo);
        consulta.setParameter(2,periodo);
        return consulta.getResultList();
    }

    public void updateColumns(String periodo) {
        System.out.println(periodo);
        Query consulta = entityManager.createNativeQuery("update nexco_concil_filiales set total_ifrs_2 = isnull(total,0) + isnull(debe_ajustes_mayores,0) + isnull(haber_ajustes_mayores,0) where periodo = :periodo ;\n" +
                "update nexco_concil_filiales set total_consol_2 = isnull(total_ifrs_2,0) + isnull(debe_total,0) + isnull(haber_total,0) where periodo = :periodo ;");
        consulta.setParameter("periodo",periodo);
        consulta.executeUpdate();
    }

    public List<Object[]> getLevel2(String periodo) {
        Query consulta39 = entityManager.createNativeQuery("select l_2, sum (Total) as total from nexco_concil_filiales where periodo = ? and L_2 in ('39','59') group by L_2 \n" +
                "union all\n" +
                "select 'Total' as L_2, sum (Total) as total from nexco_concil_filiales where periodo = ? and L_2 in ('39','59') order by 1\n");
        consulta39.setParameter(1,periodo);
        consulta39.setParameter(2,periodo);
        return consulta39.getResultList();
    }

    public Page getAll(Pageable pageable, String periodo) {
        List<ConcilFiliales> list = getEeffConsolidatedDataByPeriod(periodo);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<ConcilFiliales> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public void concilFilialesEeff(String period) {
        Query delete = entityManager.createNativeQuery("drop table t_eeff_cuentas_unificadas");
        delete.executeUpdate();

        Query insert = entityManager.createNativeQuery("select * into t_eeff_cuentas_unificadas from (SELECT cuenta, nombre_cuenta, 'Fiduciaria' as empresa FROM nexco_puc_fiduciaria_filiales where len (cuenta) >= 13\n" +
                "UNION SELECT id_cuenta, nombre_cuenta , 'Valores' as empresa FROM nexco_puc_valores_filiales where maneja_movimientos = 1 and substring(id_cuenta,1,1) != '7' \n" +
                "UNION SELECT nucta, derecta as nombre_cuenta, 'Banco' as empresa FROM cuentas_puc where empresa = '0013') a;");

        insert.executeUpdate();

        Query insert12 = entityManager.createNativeQuery("DELETE FROM t_eeff_cuentas_unificadas where cuenta in (select distinct b.id_cuenta from (SELECT id_cuenta FROM nexco_puc_valores_filiales where periodo = ? and len (id_cuenta) = 9 ) as a\n" +
                "INNER JOIN (SELECT id_cuenta FROM nexco_puc_valores_filiales where periodo = ? and len (id_cuenta) = 8) as b \n" +
                "ON SUBSTRING(a.id_cuenta,1,8) = b.id_cuenta)");

        insert12.setParameter(1, period);
        insert12.setParameter(2, period);
        insert12.executeUpdate();

        Query delete1 = entityManager.createNativeQuery("delete from  nexco_concil_filiales where periodo = ?");
        delete1.setParameter(1, period);
        delete1.executeUpdate();

        /*Query update = entityManager.createNativeQuery("insert into nexco_concil_filiales (L_1, L_2, L_4, L_6, L_9, cuenta, nombre_cuenta, empresa, moneda, banco, fiduciaria, valores, total, periodo)  \n" +
                "(select e.L_1, e.L_2, e.L_4, e.L_6, e.L_9, e.cuenta, e.nombre_cuenta, e.empresa, e.moneda, e.saldo_banco, e.saldo_fiduciaria, e.saldo_valores, e.total, ?  from (select SUBSTRING(a.cuenta,1,1) as L_1, SUBSTRING(a.cuenta,1,2) as L_2,SUBSTRING(a.cuenta,1,4) as L_4, SUBSTRING(a.cuenta,1,6) as L_6, SUBSTRING(a.cuenta,1,9) as L_9, a.cuenta, a.nombre_cuenta, a.empresa, ISNULL(b.moneda,ISNULL( c.moneda,ISNULL(d.moneda, 'NA'))) AS moneda, ISNULL(b.saldo_final_export,0) as saldo_fiduciaria, ISNULL(c.saldo_final,0) as saldo_valores, ISNULL(d.salmes*-1,0) as saldo_banco, ISNULL(b.saldo_final_export,0)  + ISNULL(c.saldo_final,0) + ISNULL(d.salmes*-1,0) as total from t_eeff_cuentas_unificadas as a\n" +
                "LEFT JOIN (select * from nexco_eeff_fiduciaria_filiales where periodo = ? and saldo_final_export <>0) as b\n" +
                "ON a.cuenta = b.cuenta and a.empresa = 'Fiduciaria'\n" +
                "LEFT JOIN (select * from nexco_eeff_valores_filiales where periodo = ? and saldo_final <>0) as c\n" +
                "ON a.cuenta = c.cuenta and a.empresa = 'Valores'\n" +
                "LEFT JOIN (select * from nexco_query_banco_def where periodo = ? and salmes <>0) as d\n" +
                "ON a.cuenta = d.nucta and a.empresa = 'Banco') AS e WHERE e.moneda <> 'NA')");

        update.setParameter(1, period);
        update.setParameter(2, period);
        update.setParameter(3, period);
        update.setParameter(4, period);
        update.executeUpdate();*/

        Query update = entityManager.createNativeQuery("insert into nexco_concil_filiales (L_1, L_2, L_4, L_6, L_9, cuenta,codicons, nombre_cuenta, empresa, moneda, banco, fiduciaria, valores, total, periodo)  \n" +
                "(select e.L_1, e.L_2, e.L_4, e.L_6, e.L_9, e.cuenta,e.codicons, e.nombre_cuenta, e.empresa, e.moneda, e.saldo_banco, e.saldo_fiduciaria, e.saldo_valores, e.total, ?  \n" +
                "from (select SUBSTRING(a.cuenta,1,1) as L_1, SUBSTRING(a.cuenta,1,2) as L_2,SUBSTRING(a.cuenta,1,4) as L_4, SUBSTRING(a.cuenta,1,6) as L_6, SUBSTRING(a.cuenta,1,9) as L_9, a.cuenta,COALESCE(b.cod_cons,c.cod_cons,d.cod_cons) as codicons, a.nombre_cuenta, a.empresa, ISNULL(b.moneda,ISNULL( c.moneda,ISNULL(d.moneda, 'NA'))) AS moneda, case when b.naturaleza ='C' then ISNULL(b.saldo_final_export,0)*-1 else ISNULL(b.saldo_final_export,0) end as saldo_fiduciaria, ISNULL(c.saldo_final,0)  as saldo_valores, ISNULL(d.salmes*-1,0) as saldo_banco, case when b.naturaleza='C' then ISNULL(b.saldo_final_export,0)*-1 else ISNULL(b.saldo_final_export,0) end  + ISNULL(c.saldo_final,0) + ISNULL(d.salmes*-1,0) as total \n" +
                "from t_eeff_cuentas_unificadas as a\n" +
                "LEFT JOIN (select * from nexco_eeff_fiduciaria_filiales where periodo = ?  and saldo_final_export <>0) as b\n" +
                "ON a.cuenta = b.cuenta and a.empresa = 'Fiduciaria'\n" +
                "LEFT JOIN (select * from nexco_eeff_valores_filiales where periodo = ?  and saldo_final <>0) as c\n" +
                "ON a.cuenta = c.cuenta and a.empresa = 'Valores'\n" +
                "LEFT JOIN (select x.nucta,x.moneda, z.CODICONS46 as cod_cons, sum(x.salmes) as salmes from nexco_query_banco_def x \n" +
                "LEFT JOIN (select * from cuentas_puc where empresa = '0013') z on x.nucta = z.NUCTA\n" +
                "where periodo = ?  and x.salmes <>0 \n" +
                "group by x.nucta,x.moneda,z.CODICONS46) as d\n" +
                "ON a.cuenta = d.nucta and a.empresa = 'Banco') AS e WHERE e.moneda <> 'NA')");

        update.setParameter(1, period);
        update.setParameter(2, period);
        update.setParameter(3, period);
        update.setParameter(4, period);
        update.executeUpdate();


        Query UpdatesEliminaciones = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones.setParameter("periodo", period);
        UpdatesEliminaciones.executeUpdate();

        Query UpdatesEliminaciones1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones1.setParameter("periodo", period);
        UpdatesEliminaciones1.executeUpdate();
    }
    public List<ConcilFiliales> obtenerDatosTabla() {

        return concilFilialaesRepository.findAll();
    }

    public boolean validarConfirmado(String periodo) {
        Query consulta = entityManager.createNativeQuery(
                "SELECT COUNT(*) FROM nexco_fecha_cargue_eeff WHERE periodo = ? AND estado = 'CONFIRMADO'");
        consulta.setParameter(1, periodo);

        try {
            int count = ((Number) consulta.getSingleResult()).intValue();

            if (count == 3) {
                return true;
            } else {
                return false;
            }
        } catch (NoResultException e) {
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}