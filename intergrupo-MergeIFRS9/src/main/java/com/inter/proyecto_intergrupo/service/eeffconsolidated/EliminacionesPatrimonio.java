package com.inter.proyecto_intergrupo.service.eeffconsolidated;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.tablaUnificadaEliminacionesPatrimoniales;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.List;

@Service
@Transactional
public class EliminacionesPatrimonio {

    @Autowired
    EntityManager entityManager;

    public List<tablaUnificadaEliminacionesPatrimoniales> getDataPatrimonio(String periodo, String fuente) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_eliminaciones_tabla_unificada where periodo = ? and fuente = ?;", tablaUnificadaEliminacionesPatrimoniales.class);
        consulta.setParameter(1, periodo);
        consulta.setParameter(2, fuente);
        return consulta.getResultList();
    }

    public Page getAllEliminacion(Pageable pageable, String periodo,List<tablaUnificadaEliminacionesPatrimoniales> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<tablaUnificadaEliminacionesPatrimoniales> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public void ProcesarEliminacion(String periodo) {

        /***********************************FIDUCIARIA********************************************************************/

        Query insert = entityManager.createNativeQuery("delete from nexco_eliminaciones_tabla_unificada where periodo = :periodo ;\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,saldo,fuente,periodo)\n" +
                "(select b.concepto, b.cuenta, sum(isnull(a.total,0)) as saldo, 'Verificación Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Fiduciaria' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto, b.cuenta)\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,saldo,fuente,periodo)\n" +
                "(select 'Total' as concepto, 'Total' as cuenta, sum(saldo) as saldo, fuente, periodo from nexco_eliminaciones_tabla_unificada \n" +
                "where fuente = 'Verificación Patrimonio Fiduciaria' and periodo = :periodo group by fuente, periodo)\n");
        insert.setParameter("periodo", periodo);
        insert.executeUpdate();


        Query insert1 = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo)\n" +
                "(select b.concepto, sum(a.total) as saldo, 'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto = '01. Capital y resto') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Fiduciaria' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "(select b.concepto, sum(a.total) as saldo, 'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto = '02. Utilidad') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Fiduciaria' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "(select b.concepto, sum(a.total) as saldo, 'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto = '02. ORI') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Fiduciaria' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "(select 'Total Patrimonio', sum(a.total) as saldo, 'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto IN ('02. ORI','01. Capital y resto','02. Utilidad')) b\n" +
                "INNER JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Fiduciaria' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta)\n" +
                "UNION ALL\n" +
                "(select 'Porcentaje Participación', b.porcentaje as saldo, 'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select top 1 * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria') b )\n" +
                "UNION ALL\n" +
                "(select 'Minoritario', 1 - b.porcentaje as saldo, 'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select top 1 * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria') b )\n" +
                "UNION ALL\n" +
                "select z.concepto,z.saldo * (1-(select top 1 porcentaje from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria')) as saldo, z.fuente, z.periodo from \n" +
                "(select 'Patrimonio x%Participación' as concepto, sum(a.total) as saldo,\n" +
                "'Total Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto IN ('02. ORI','01. Capital y resto','02. Utilidad')) b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Fiduciaria' and periodo = :periodo group by cuenta) a  ON  a.cuenta = b.cuenta) z");
        insert1.setParameter("periodo", periodo);
        insert1.executeUpdate();

        Query insert2 = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,saldo,fuente,periodo)\n" +
                "(select case when b.concepto = 'Ingreso/Gasto' THEN CASE WHEN sum(a.banco) >= 0 then 'Gasto' when sum(a.banco) < 0 then 'Ingreso' end ELSE b.concepto END as concepto, b.cuenta, sum(isnull(a.banco,0)) as saldo, \n" +
                "'Contabilidad Eliminación Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Fiduciaria') b\n" +
                "LEFT JOIN (select cuenta,sum(banco) as banco from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto, b.cuenta)");
        insert2.setParameter("periodo", periodo);
        insert2.executeUpdate();

        Query insert3 = entityManager.createNativeQuery("INSERT INTO nexco_eliminaciones_tabla_unificada (concepto, saldo, fuente, periodo)\n" +
                "SELECT 'Banco' as concepto, SUM(a.total)* b.porcentaje as saldo, 'Validación Contabilidad Eliminación' as fuente, :periodo as periodo\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto IN ('02. Utilidad','02. Perdida')) b\n" +
                "LEFT JOIN (SELECT cuenta,sum(total) as total FROM nexco_concil_filiales WHERE empresa = 'Fiduciaria' AND periodo = :periodo group by cuenta) a ON a.cuenta = b.cuenta group by b.porcentaje\n" +
                "UNION ALL\n" +
                "SELECT 'Validación Banco' as concepto, y.saldo - (select sum(saldo) from nexco_eliminaciones_tabla_unificada where fuente = 'Contabilidad Eliminación Fiduciaria' and concepto IN ('Ingreso','Gasto') and periodo = :periodo ) as saldo, 'Validación Contabilidad Eliminación' as fuente, :periodo as periodo \n" +
                "from (SELECT 'Banco' as concepto, SUM(a.total)* b.porcentaje as saldo, 'Validación Contabilidad Eliminación' as fuente, :periodo as periodo\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'Patrimonio Fiduciaria' AND concepto IN ('02. Utilidad','02. Perdida')) b\n" +
                "LEFT JOIN (SELECT cuenta,sum(total) as total FROM nexco_concil_filiales WHERE empresa = 'Fiduciaria' AND periodo = :periodo group by cuenta) a ON a.cuenta = b.cuenta group by b.porcentaje) y\n");
        insert3.setParameter("periodo", periodo);
        insert3.executeUpdate();

        Query insert4 = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo)\n" +
                "select 'Inversión Contable Banco' as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Fiduciaria' and concepto = 'Inversión Contable Banco') b ON  a.cuenta = b.cuenta group by b.concepto\n" +
                "UNION ALL\n" +
                "(select case when b.concepto = 'Ingreso/Gasto' THEN CASE WHEN sum(a.banco) >= 0 then 'Gasto' when sum(a.banco) <= 0 then 'Ingreso' end ELSE b.concepto END as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Fiduciaria' AND concepto = 'Ingreso/Gasto' ) b ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "select 'ORI' as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Fiduciaria' and concepto = 'ORI') b ON  a.cuenta = b.cuenta group by b.concepto\n" +
                "UNION ALL \n" +
                "Select 'Validación Inversión Contable Banco' as concepto, a.saldo * b.saldo as saldo, 'Validacion Patrimonio Fiduciaria' as fuente, a.periodo from (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Total Patrimonio' AND PERIODO = :periodo) a\n" +
                "LEFT JOIN (Select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Porcentaje Participación' AND PERIODO = :periodo) b on a.fuente = b.fuente\n" +
                "UNION ALL\n" +
                "select 'Validación %Participacion Inversión Contable' as concepto, a.saldo / b.saldo as saldo, a.fuente , a.periodo from (select 'Inversión Contable Banco' as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo ) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Fiduciaria' and concepto = 'Inversión Contable Banco') b ON  a.cuenta = b.cuenta group by b.concepto) a\n" +
                "LEFT JOIN (Select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Total Patrimonio' AND PERIODO = :periodo) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'ORI REAL' as concepto, a.saldo * b.saldo as saldo, 'Validacion Patrimonio Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = '02. ORI' and periodo = :periodo ) a\n" +
                "INNER JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Porcentaje Participación' and periodo = :periodo ) b ON  a.fuente = b.fuente");
        insert4.setParameter("periodo", periodo);
        insert4.executeUpdate();

        Query insert5 = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo) \n" +
                "select 'Inversión' as concepto, ISNULL(a.saldo,0)*-1 as saldo, 'Validacion Final Fiduciaria' as fuente, :periodo as periodo from (select sum(saldo) as saldo from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Validacion Patrimonio Fiduciaria' and concepto in ('ORI REAL','Ingreso','Inversión Contable Banco') and periodo = :periodo ) a\n" +
                "UNION ALL \n" +
                "select 'Patrimonio' as concepto, a.saldo * -1 as saldo, 'Validacion Final Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' AND concepto = '01. Capital y resto' and periodo = :periodo )a\n" +
                "UNION ALL\n" +
                "select 'Interés Min Cap' as concepto, a.saldo * b.saldo as saldo, 'Validacion Final Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = '01. Capital y resto' and periodo = :periodo ) a\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Minoritario' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Interés Min Útil' as concepto, a.saldo * b.saldo as saldo, 'Validacion Final Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = '02. Utilidad' and periodo = :periodo ) a\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Minoritario' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Interés Min ORI' as concepto, a.saldo * b.saldo as saldo, 'Validacion Final Fiduciaria' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = '02. Ori' and periodo = :periodo ) a\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Minoritario' and periodo = :periodo ) b on 1 = 1;\n" +
                "\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo) \n" +
                "select 'Total' as concepto, SUM(saldo) as saldo, 'Validacion Final Fiduciaria' as fuente, :periodo as periodo\n" +
                "from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Fiduciaria' and concepto in ('Inversión', 'Patrimonio', 'Interés Min Cap', 'Interés Min Útil', 'Interés Min ORI') and periodo = :periodo ;");
        insert5.setParameter("periodo", periodo);
        insert5.executeUpdate();

        /***********************************VALORES********************************************************************/

        Query insertValores = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,saldo,fuente,periodo)\n" +
                "(select b.concepto, b.cuenta, sum(isnull(a.total,0)) as saldo, 'Verificación Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Valores' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto, b.cuenta)\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,saldo,fuente,periodo)\n" +
                "(select 'Total' as concepto, 'Total' as cuenta, sum(saldo) as saldo, fuente, periodo from nexco_eliminaciones_tabla_unificada \n" +
                "where fuente = 'Verificación Patrimonio Valores' and periodo = :periodo group by fuente, periodo)\n");
        insertValores.setParameter("periodo", periodo);
        insertValores.executeUpdate();


        Query insert1Valores = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo)\n" +
                "(select b.concepto, sum(a.total) as saldo, 'Total Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto = '01. Capital y resto') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Valores' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "(select b.concepto, sum(a.total) as saldo, 'Total Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto = '02. Utilidad') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Valores' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "(select b.concepto, sum(a.total) as saldo, 'Total Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto = '02. ORI') b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Valores' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "(select 'Total Patrimonio', sum(a.total) as saldo, 'Total Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto IN ('02. ORI','01. Capital y resto','02. Utilidad')) b\n" +
                "INNER JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Valores' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta)\n" +
                "UNION ALL\n" +
                "(select 'Porcentaje Participación', b.porcentaje as saldo, 'Total Patrimonio Valores' as fuente, :periodo as periodo from (select top 1 * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores') b )\n" +
                "UNION ALL\n" +
                "(select 'Minoritario', 1 - b.porcentaje as saldo, 'Total Patrimonio Valores' as fuente, :periodo as periodo from (select top 1 * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores') b )\n" +
                "UNION ALL\n" +
                "select z.concepto,z.saldo * (1-(select top 1 porcentaje from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores')) as saldo, z.fuente, z.periodo from \n" +
                "(select 'Patrimonio x%Participación' as concepto, sum(a.total) as saldo,\n" +
                "'Total Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto IN ('02. ORI','01. Capital y resto','02. Utilidad')) b\n" +
                "LEFT JOIN (select cuenta, sum(total) as total from nexco_concil_filiales where empresa = 'Valores' and periodo = :periodo group by cuenta) a  ON  a.cuenta = b.cuenta) z");
        insert1Valores.setParameter("periodo", periodo);
        insert1Valores.executeUpdate();

        Query insert2Valores = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,saldo,fuente,periodo)\n" +
                "(select case when b.concepto = 'Ingreso/Gasto' THEN CASE WHEN sum(a.banco) >= 0 then 'Gasto' when sum(a.banco) < 0 then 'Ingreso' end ELSE b.concepto END as concepto, b.cuenta, sum(isnull(a.banco,0)) as saldo, \n" +
                "'Contabilidad Eliminación Valores' as fuente, :periodo as periodo from (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Valores') b\n" +
                "LEFT JOIN (select cuenta,sum(banco) as banco from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo group by cuenta) a ON  a.cuenta = b.cuenta group by b.concepto, b.cuenta)");
        insert2Valores.setParameter("periodo", periodo);
        insert2Valores.executeUpdate();

        Query insert3Valores = entityManager.createNativeQuery("INSERT INTO nexco_eliminaciones_tabla_unificada (concepto, saldo, fuente, periodo)\n" +
                "SELECT 'Banco' as concepto, SUM(a.total)* b.porcentaje as saldo, 'Validación Contabilidad Eliminación Valores' as fuente, :periodo as periodo\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto IN ('02. Utilidad','02. Perdida')) b\n" +
                "LEFT JOIN (SELECT cuenta,sum(total) as total FROM nexco_concil_filiales WHERE empresa = 'Valores' AND periodo = :periodo group by cuenta) a ON a.cuenta = b.cuenta group by b.porcentaje\n" +
                "UNION ALL\n" +
                "SELECT 'Validación Banco' as concepto, y.saldo - (select sum(saldo) from nexco_eliminaciones_tabla_unificada where fuente = 'Contabilidad Eliminación Valores' and concepto IN ('Ingreso','Gasto') and periodo = :periodo ) as saldo, 'Validación Contabilidad Eliminación Valores' as fuente, :periodo as periodo \n" +
                "from (SELECT 'Banco' as concepto, SUM(a.total)* b.porcentaje as saldo, 'Validación Contabilidad Eliminación Valores' as fuente, :periodo as periodo\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'Patrimonio Valores' AND concepto IN ('02. Utilidad','02. Perdida')) b\n" +
                "LEFT JOIN (SELECT cuenta,sum(total) as total FROM nexco_concil_filiales WHERE empresa = 'Valores' AND periodo = :periodo group by cuenta) a ON a.cuenta = b.cuenta group by b.porcentaje) y\n");
        insert3Valores.setParameter("periodo", periodo);
        insert3Valores.executeUpdate();

        Query insert4Valores = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo)\n" +
                "select 'Inversión Contable Banco' as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Valores' and concepto = 'Inversión Contable Banco') b ON  a.cuenta = b.cuenta group by b.concepto\n" +
                "UNION ALL\n" +
                "(select case when b.concepto = 'Ingreso/Gasto' THEN CASE WHEN sum(a.banco) >= 0 then 'Gasto' when sum(a.banco) <= 0 then 'Ingreso' end ELSE b.concepto END as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Valores' AND concepto = 'Ingreso/Gasto' ) b ON  a.cuenta = b.cuenta group by b.concepto)\n" +
                "UNION ALL\n" +
                "select 'ORI' as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Valores' and concepto = 'ORI') b ON  a.cuenta = b.cuenta group by b.concepto\n" +
                "UNION ALL \n" +
                "Select 'Validación Inversión Contable Banco' as concepto, a.saldo * b.saldo as saldo, 'Validacion Patrimonio Valores' as fuente, a.periodo from (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Valores' and concepto = 'Total Patrimonio' AND PERIODO = :periodo) a\n" +
                "LEFT JOIN (Select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Valores' and concepto = 'Porcentaje Participación' AND PERIODO = :periodo) b on a.fuente = b.fuente\n" +
                "UNION ALL\n" +
                "select 'Validación %Participacion Inversión Contable' as concepto, a.saldo / b.saldo as saldo, a.fuente , a.periodo from (select 'Inversión Contable Banco' as concepto, sum(a.banco) as saldo, 'Validacion Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_concil_filiales where empresa = 'banco' and periodo = :periodo ) a\n" +
                "INNER JOIN (select * from nexco_parametria_eeff WHERE parametro = 'Eliminación Banco Valores' and concepto = 'Inversión Contable Banco') b ON  a.cuenta = b.cuenta group by b.concepto) a\n" +
                "LEFT JOIN (Select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Valores' and concepto = 'Total Patrimonio' AND PERIODO = :periodo) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'ORI REAL' as concepto, a.saldo * b.saldo as saldo, 'Validacion Patrimonio Valores' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = '02. ORI' and periodo = :periodo ) a\n" +
                "INNER JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = 'Porcentaje Participación' and periodo = :periodo ) b ON  a.fuente = b.fuente");
        insert4Valores.setParameter("periodo", periodo);
        insert4Valores.executeUpdate();

        Query insert5Valores = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo) \n" +
                "select 'Inversión' as concepto, ISNULL(a.saldo,0)*-1 as saldo, 'Validacion Final Valores' as fuente, :periodo as periodo from (select sum(saldo) as saldo from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Validacion Patrimonio Valores' and concepto in ('ORI REAL','Ingreso','Inversión Contable Banco') and periodo = :periodo ) a\n" +
                "UNION ALL \n" +
                "select 'Patrimonio' as concepto, a.saldo * -1 as saldo, 'Validacion Final Valores' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' AND concepto = '01. Capital y resto' and periodo = :periodo )a\n" +
                "UNION ALL\n" +
                "select 'Interés Min Cap' as concepto, a.saldo * b.saldo as saldo, 'Validacion Final Valores' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = '01. Capital y resto' and periodo = :periodo ) a\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = 'Minoritario' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Interés Min Útil' as concepto, a.saldo * b.saldo as saldo, 'Validacion Final Valores' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = '02. Utilidad' and periodo = :periodo ) a\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = 'Minoritario' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Interés Min ORI' as concepto, a.saldo * b.saldo as saldo, 'Validacion Final Valores' as fuente, :periodo as periodo from (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = '02. Ori' and periodo = :periodo ) a\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada WHERE fuente = 'Total Patrimonio Valores' and concepto = 'Minoritario' and periodo = :periodo ) b on 1 = 1;\n" +
                "\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,saldo,fuente,periodo) \n" +
                "select 'Total' as concepto, SUM(saldo) as saldo, 'Validacion Final Valores' as fuente, :periodo as periodo\n" +
                "from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Valores' and concepto in ('Inversión', 'Patrimonio', 'Interés Min Cap', 'Interés Min Útil', 'Interés Min ORI') and periodo = :periodo ;");
        insert5Valores.setParameter("periodo", periodo);
        insert5Valores.executeUpdate();

        /***********************************ASIENTO ELIMINACIONES********************************************************************/

        Query insertAsiento = entityManager.createNativeQuery("insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,fiduciaria,valores,fuente,periodo)\n" +
                "select 'Ingreso' as Concepto, substring(z.cuenta,1,4) as Cuenta, case when a.saldo < 0 then a.saldo*-1 else 0 end as Fiduciaria, CASE  WHEN b.saldo < 0 THEN b.saldo * -1 ELSE 0 END as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Ingreso') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Fiduciaria' and concepto = 'Ingreso' and periodo = :periodo ) a on z.concepto = a.concepto\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Valores' and concepto = 'Ingreso' and periodo = :periodo ) b on z.concepto = b.concepto\n" +
                "UNION ALL\n" +
                "select 'Gasto' as Concepto, substring(z.cuenta,1,4) as Cuenta, case when a.saldo < 0 then a.saldo*-1 else 0 end as Fiduciaria, CASE  WHEN b.saldo < 0 THEN b.saldo * -1 ELSE 0 END as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Gasto') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Fiduciaria' and concepto = 'Gasto' and periodo = :periodo) a on z.concepto = a.concepto\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Valores' and concepto = 'Gasto' and periodo = :periodo ) b on z.concepto = b.concepto\n" +
                "UNION ALL\n" +
                "select 'Ori Real' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo * -1 as Fiduciaria, b.saldo * -1 as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'ORI REAL') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Fiduciaria' and concepto = 'ORI REAL' and periodo = :periodo ) a on z.concepto = a.concepto\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Valores' and concepto = 'ORI REAL' and periodo = :periodo ) b on z.concepto = b.concepto\n" +
                "\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,fiduciaria,valores,fuente,periodo)\n" +
                "select 'Inversión Filiales Parcial' as Concepto, substring(z.cuenta,1,4) as Cuenta, (a.fiduciaria + b.fiduciaria + c.fiduciaria) * -1 as Fiduciaria, (a.valores + b.valores + c.valores) * -1 as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Inversión Filiales Parcial') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Ingreso' and periodo = :periodo ) a on z.parametro = a.fuente\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Gasto' and periodo = :periodo ) b on z.parametro = b.fuente\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'ORI REAL' and periodo = :periodo ) c on z.parametro = c.fuente\n" +
                "UNION ALL\n" +
                "select 'Ganancia del ejercicio' as Concepto, substring(z.cuenta,1,4) as Cuenta, (a.fiduciaria + b.fiduciaria) as Fiduciaria, (a.valores + b.valores) as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Ganancia del ejercicio') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Ingreso' and periodo = :periodo ) a on z.parametro = a.fuente\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Gasto' and periodo = :periodo ) b on z.parametro = b.fuente\n" +
                "UNION ALL\n" +
                "select 'Ganancias y Pérdidas' as Concepto, substring(z.cuenta,1,4) as Cuenta, (a.fiduciaria + b.fiduciaria) * -1 as Fiduciaria, (a.valores + b.valores) * -1 as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Ganancias y Pérdidas') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Ingreso' and periodo = :periodo ) a on z.parametro = a.fuente\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Gasto' and periodo = :periodo ) b on z.parametro = b.fuente\n" +
                "\n" +
                "\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,fiduciaria,valores,fuente,periodo)\n" +
                "select 'Inversión Filiales' as Concepto, substring(z.cuenta,1,4) as Cuenta, (c.fiduciaria + a.saldo)*-1 as Fiduciaria, (c.valores + b.saldo)*-1 as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Inversión Filiales') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Fiduciaria' and concepto = 'Inversión Contable Banco' and periodo = :periodo ) a on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Patrimonio Valores' and concepto = 'Inversión Contable Banco' and periodo = :periodo ) b on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Inversión Filiales Parcial' and periodo = :periodo ) c on z.parametro = c.fuente\n" +
                "UNION ALL\n" +
                "select 'Capital y resto' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo *-1 as Fiduciaria, b.saldo * -1 as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Capital y resto') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Fiduciaria' and concepto = '01. Capital y resto' and periodo = :periodo ) a on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Valores' and concepto = '01. Capital y resto' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Patrimonio x%Participación' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo as Fiduciaria, b.saldo  as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Patrimonio x%Participación') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Fiduciaria' and concepto = 'Patrimonio x%Participación' and periodo = :periodo ) a on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Total Patrimonio Valores' and concepto = 'Patrimonio x%Participación' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Participación No Controladora' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo*-1 as Fiduciaria, b.saldo*-1  as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Participación No Controladora') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Fiduciaria' and concepto = 'Interés Min Útil' and periodo = :periodo ) a on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Valores' and concepto = 'Interés Min Útil' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Interés Ganancias y Pérdidas' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo as Fiduciaria, b.saldo  as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Interés Ganancias y Pérdidas') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Fiduciaria' and concepto = 'Interés Min Útil' and periodo = :periodo ) a on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Valores' and concepto = 'Interés Min Útil' and periodo = :periodo ) b on 1 = 1\n" +
                "UNION ALL\n" +
                "select 'Interés Ganancia del ejercicio' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo*-1 as Fiduciaria, b.saldo*-1  as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Interés Ganancia del ejercicio') z \n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Fiduciaria' and concepto = 'Interés Min Útil' and periodo = :periodo ) a on 1 = 1\n" +
                "LEFT JOIN (select * from nexco_eliminaciones_tabla_unificada where fuente = 'Validacion Final Valores' and concepto = 'Interés Min Útil' and periodo = :periodo ) b on 1 = 1\n" +
                "\n" +
                "insert into nexco_eliminaciones_tabla_unificada (concepto,cuenta,fiduciaria,valores,fuente,periodo)\n" +
                "select 'Superávit Participación Patrimonial' as Concepto, substring(z.cuenta,1,4) as Cuenta, (a.fiduciaria + b.fiduciaria + c.fiduciaria + d.fiduciaria + e.fiduciaria + f.fiduciaria)*-1 as Fiduciaria, (a.valores + b.valores + c.valores + d.valores + e.valores + f.valores)*-1 as Valores, 'Asiento de Eliminación' as fuente, :periodo  as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Superávit Participación Patrimonial') z \n" +
                "LEFT JOIN (select isnull(fiduciaria,0) as fiduciaria, isnull(valores,0) as valores, fuente from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Inversión Filiales' and periodo = :periodo ) a on z.parametro = a.fuente\n" +
                "LEFT JOIN (select isnull(fiduciaria,0) as fiduciaria, isnull(valores,0) as valores, fuente from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Capital y resto' and periodo = :periodo ) b on z.parametro = b.fuente\n" +
                "LEFT JOIN (select isnull(fiduciaria,0) as fiduciaria, isnull(valores,0) as valores, fuente from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Patrimonio x%Participación' and periodo = :periodo ) c on z.parametro = c.fuente\n" +
                "LEFT JOIN (select isnull(fiduciaria,0) as fiduciaria, isnull(valores,0) as valores, fuente from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Participación No Controladora' and periodo = :periodo ) d on z.parametro = d.fuente\n" +
                "LEFT JOIN (select isnull(fiduciaria,0) as fiduciaria, isnull(valores,0) as valores, fuente from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Interés Ganancias y Pérdidas' and periodo = :periodo ) e on z.parametro = e.fuente\n" +
                "LEFT JOIN (select isnull(fiduciaria,0) as fiduciaria, isnull(valores,0) as valores, fuente from nexco_eliminaciones_tabla_unificada where fuente = 'Asiento de Eliminación' and concepto = 'Interés Ganancia del ejercicio' and periodo = :periodo ) f on z.parametro = f.fuente\n" +
                "UNION ALL\n" +
                "select 'Inversión Contable Banco Principal' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo*-1 as Fiduciaria, b.saldo*-1 as Valores, 'Asiento de Eliminación' as fuente, :periodo   as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Inversión Contable Banco Principal') z\n" +
                "LEFT JOIN (select top 1 * from nexco_eliminaciones_tabla_unificada where periodo = :periodo  and fuente = 'Contabilidad Eliminación Fiduciaria' and concepto ='Inversión Contable Banco' order by cuenta asc) a on 1=1\n" +
                "LEFT JOIN (select top 1 * from nexco_eliminaciones_tabla_unificada where periodo = :periodo  and fuente = 'Contabilidad Eliminación Valores' and concepto ='Inversión Contable Banco' order by cuenta asc) b on 1=1\n" +
                "UNION ALL\n" +
                "select 'Inversión Contable Banco Aux' as Concepto, substring(z.cuenta,1,4) as Cuenta, a.saldo as Fiduciaria, b.saldo as Valores, 'Asiento de Eliminación' as fuente, :periodo   as periodo\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'Asiento de Eliminación' and concepto = 'Inversión Contable Banco Aux') z\n" +
                "LEFT JOIN (select top 1 * from nexco_eliminaciones_tabla_unificada where periodo = :periodo  and fuente = 'Contabilidad Eliminación Fiduciaria' and concepto ='Inversión Contable Banco' order by cuenta asc) a on 1=1\n" +
                "LEFT JOIN (select top 1 * from nexco_eliminaciones_tabla_unificada where periodo = :periodo  and fuente = 'Contabilidad Eliminación Valores' and concepto ='Inversión Contable Banco' order by cuenta asc) b on 1=1");
        insertAsiento.setParameter("periodo", periodo);
        insertAsiento.executeUpdate();

        /**********************************************ACTUALIZAR ELIMINACIONES********************************************************************/

        Query updateConsolInsert = entityManager.createNativeQuery("delete from nexco_concil_filiales where periodo = :periodo and cuenta in ('590501901','31500501001','31500501002','515805001','515805002'); \n"+
                "insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons) \n" +
                "values ('5','59','5905','590501','590501901','590501901','Banco',0,0,0,'ML','Utilidad Consolidado', :periodo ,0,'37501');\n" +
                "insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons) \n" +
                "values ('3','31','3150','315005','315005010','31500501001','Banco',0,0,0,'ML','Participación No Controladora Fiduciaria', :periodo ,0,'26103');\n" +
                "insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons) \n" +
                "values ('3','31','3150','315005','315005010','31500501002','Banco',0,0,0,'ML','Participación No Controladora Valores', :periodo ,0,'26103');\n" +
                "insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons) \n" +
                "values ('5','51','5158','515805','515805001','515805001','Banco',0,0,0,'ML','Participación No Controladora Fiduciaria', :periodo ,0,'29008');\n" +
                "insert into nexco_concil_filiales (l_1,l_2,l_4,l_6,l_9,cuenta,empresa,banco,valores,fiduciaria,moneda,nombre_cuenta,periodo,total,codicons) \n" +
                "values ('5','51','5158','515805','515805002','515805002','Banco',0,0,0,'ML','Participación No Controladora Valores', :periodo ,0,'29008');");
        updateConsolInsert.setParameter("periodo", periodo);
        updateConsolInsert.executeUpdate();

        Query updateConsol = entityManager.createNativeQuery("update a set a.debe_patrimonio = case when b.saldo<0 then abs(b.saldo) else 0 end, a.haber_patrimonio = case when b.saldo>0 then abs(b.saldo)*-1 else 0 end, \n" +
                "a.eliminacion_patrimonio = a.eliminacion + case when b.saldo<0 then abs(b.saldo) else 0 end + case when b.saldo>0 then abs(b.saldo)*-1 else 0 end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo ) a, \n" +
                "(select * from nexco_eliminaciones_tabla_unificada where periodo = :periodo and fuente = 'Verificación Patrimonio Fiduciaria' and concepto= '01. Capital y resto')b\n" +
                "where a.cuenta=b.cuenta");
        updateConsol.setParameter("periodo", periodo);
        updateConsol.executeUpdate();

        Query updateConsol1 = entityManager.createNativeQuery("update a set a.debe_patrimonio = case when b.saldo<0 then abs(b.saldo) else 0 end, a.haber_patrimonio = case when b.saldo>0 then abs(b.saldo)*-1 else 0 end, \n" +
                "a.eliminacion_patrimonio = a.eliminacion + case when b.saldo<0 then abs(b.saldo) else 0 end + case when b.saldo>0 then abs(b.saldo)*-1 else 0 end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo ) a, \n" +
                "(select * from nexco_eliminaciones_tabla_unificada where periodo = :periodo and fuente = 'Verificación Patrimonio Valores' and concepto= '01. Capital y resto')b\n" +
                "where a.cuenta=b.cuenta");
        updateConsol1.setParameter("periodo", periodo);
        updateConsol1.executeUpdate();

        Query updateConsol2 = entityManager.createNativeQuery("update z set z.debe_patrimonio = case when x.saldo>0 then abs(x.saldo) else 0 end, z.haber_patrimonio = case when x.saldo<0 then abs(x.saldo)*-1 else 0 end, \n" +
                "z.eliminacion_patrimonio = z.eliminacion + case when x.saldo>0 then abs(x.saldo) else 0 end + case when x.saldo<0 then abs(x.saldo)*-1 else 0 end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo ) z\n" +
                "inner join (select b.cuenta,sum(a.fiduciaria) as saldo from (select * from nexco_eliminaciones_tabla_unificada where periodo = :periodo and fuente = 'Asiento de Eliminación') a\n" +
                "left join (select concepto,cuenta,cuenta2 from nexco_parametria_eeff where parametro = 'Asiento de Eliminación')b on a.concepto=b.concepto where b.cuenta is not null and b.cuenta != '' and b.cuenta != b.cuenta2 group by b.cuenta) x on z.cuenta = x.cuenta");
        updateConsol2.setParameter("periodo", periodo);
        updateConsol2.executeUpdate();

        Query updateConsol3 = entityManager.createNativeQuery("update z set z.debe_patrimonio = case when x.saldo>0 then abs(x.saldo) else 0 end, z.haber_patrimonio = case when x.saldo<0 then abs(x.saldo)*-1 else 0 end, \n" +
                "z.eliminacion_patrimonio = z.eliminacion + case when x.saldo>0 then abs(x.saldo) else 0 end + case when x.saldo<0 then abs(x.saldo)*-1 else 0 end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo ) z\n" +
                "inner join (select b.cuenta2,sum(a.valores) as saldo from (select * from nexco_eliminaciones_tabla_unificada where periodo = :periodo and fuente = 'Asiento de Eliminación') a\n" +
                "left join (select concepto,cuenta,cuenta2 from nexco_parametria_eeff where parametro = 'Asiento de Eliminación')b on a.concepto=b.concepto where b.cuenta is not null and b.cuenta2 != '' and b.cuenta != b.cuenta2 group by b.cuenta2) x on z.cuenta = x.cuenta2");
        updateConsol3.setParameter("periodo", periodo);
        updateConsol3.executeUpdate();

        Query updateConsol4 = entityManager.createNativeQuery("update z set z.debe_patrimonio = z.debe_patrimonio+ case when x.saldo>0 then abs(x.saldo) else 0 end, z.haber_patrimonio = z.haber_patrimonio + case when x.saldo<0 then abs(x.saldo)*-1 else 0 end,\n" +
                "z.eliminacion_patrimonio = z.eliminacion_patrimonio + z.eliminacion + case when x.saldo>0 then abs(x.saldo) else 0 end + case when x.saldo<0 then abs(x.saldo)*-1 else 0 end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo ) z\n" +
                "inner join (select b.cuenta, sum(a.fiduciaria+a.valores) as saldo from (select * from nexco_eliminaciones_tabla_unificada where periodo = :periodo and fuente = 'Asiento de Eliminación') a\n" +
                "left join (select concepto,cuenta,cuenta2 from nexco_parametria_eeff where parametro = 'Asiento de Eliminación')b on a.concepto=b.concepto where b.cuenta is not null and b.cuenta != '' and b.cuenta = b.cuenta2 group by b.cuenta) x on z.cuenta = x.cuenta\n");
        updateConsol4.setParameter("periodo", periodo);
        updateConsol4.executeUpdate();

        /*Query updateConsol5 = entityManager.createNativeQuery("");
        updateConsol5.setParameter("periodo", periodo);
        updateConsol5.executeUpdate();*/

        Query UpdatesEliminaciones = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones.setParameter("periodo", periodo);
        UpdatesEliminaciones.executeUpdate();

        Query UpdatesEliminaciones1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones1.setParameter("periodo", periodo);
        UpdatesEliminaciones1.executeUpdate();

    }

}

