package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.*;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class PlainIFRS9Service {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    public boolean insertPlainIFRS9(String periodo) {

        boolean valueReturn = false;
        try {

            Query delete9 = entityManager.createNativeQuery("delete from nexco_plano_ifrs9_neocon where periodo = ? and tipo_registro in ('PROV', 'REC'); ");
            delete9.setParameter(1,periodo);
            delete9.executeUpdate();

            Query deleteTemp = entityManager.createNativeQuery("TRUNCATE TABLE nexco_plano_ifrs9_neocon_temp_d");
            deleteTemp.executeUpdate();

            //Provisiones y reclasificaciones
            Query insert1 = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon_temp_d (sociedad, tipocons, tipo_asiento, descripcion, movimiento, referencia, modo_ejecucion, usuario, codicons, naturaleza_total, saldo, divisa, pais_negocio, periodo, intergrupo, tipo_registro)\n" +
                    "select '00548' sociedad_informante \n" +
                    ", '0' tipo_cons\n" +
                    ", isnull((SELECT TOP 1 TIPO_ASIENTO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'PROV'), '') tipo_asiento\n" +
                    ", isnull((SELECT TOP 1 TIPO_PROCESO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'PROV'), '') descripcion\n" +
                    ", 'M' movimiento\n" +
                    ", isnull((SELECT TOP 1 referencia FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'PROV'), '') referencia\n" +
                    ", 'P' modo_ejecucion\n" +
                    ", 'C388386' usuario\n" +
                    ", a.codicons\n" +
                    ", CASE WHEN a.naturaleza_total = 'D' and isnull(round(abs(sdo_total_plantilla), 0), 0)-isnull(b.saldo, 0) <0 then 'H' WHEN a.naturaleza_total = 'H' and isnull(round(abs(sdo_total_plantilla), 0), 0)-isnull(b.saldo, 0) <0 then 'D' else a.naturaleza_total END naturaleza_total\n" +
                    ", CASE WHEN b.naturaleza_total = 'D' THEN isnull(round(sdo_total_plantilla, 0), 0)-isnull(b.saldo, 0) ELSE isnull(round(sdo_total_plantilla, 0), 0)+isnull(b.saldo, 0) END saldo\n" +
                    ", a.divisa\n" +
                    ", 'XX' pais_negocio\n" +
                    ", a.periodo\n" +
                    ", null intergrupo\n" +
                    ", 'PROV' tipo_registro \n" +
                    "from (select * from nexco_val_ifrs9 where sdo_prov <> 0 and periodo = ?) a\n" +
                    "left join (select b.CODICONS46 codicons, round(sum(importe_total/1000), 0) saldo, c.divisa_neocon divisa, " +
                    "CASE WHEN (case when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) >= 0 then 'D' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) >=0 then 'D' else case when round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(b.CODICONS46,1,1) = '5' THEN 'D' WHEN (case when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) >= 0 then 'D' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) >=0 then 'D' else case when round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(b.CODICONS46,1,1) = '5' THEN 'H' ELSE (case when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) >= 0 then 'D' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) >=0 then 'D' else case when round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' else 'D' end end) END naturaleza_total, \n" +
                    "'PROV' tipo_registro\n" +
                    "from nexco_plano_ifrs9_intergrupo a\n" +
                    "left join (SELECT puc.CODICONS46, puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join nexco_divisas c\n" +
                    "on a.divisa = c.id_divisa\n" +
                    "left join (select distinct contrato, yntp from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def where periodo = ?) d\n" +
                    "on a.contrato = d.contrato\n" +
                    "left join nexco_cuentas_neocon e\n" +
                    "on b.CODICONS46 = e.cuenta\n" +
                    "where periodo = ? and origen = 'INTERGRUPO_PROVIS.TXT'\n" +
                    "GROUP BY b.CODICONS46, c.divisa_neocon, origen, e.naturaleza) b\n" +
                    "on a.codicons = b.codicons and a.divisa = b.divisa\n" +
                    "union all --intergrupo provisiones\n" +
                    "select '00548' sociedad_informante\n" +
                    ", '0' tipo_cons\n" +
                    ", isnull((SELECT TOP 1 TIPO_ASIENTO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'PROV'), '') tipo_asiento\n" +
                    ", isnull((SELECT TOP 1 TIPO_PROCESO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'PROV'), '') descripcion\n" +
                    ", 'M' movimiento\n" +
                    ", isnull((SELECT TOP 1 referencia FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'PROV'), '') referencia\n" +
                    ", 'P' modo_ejecucion\n" +
                    ", 'C388386' usuario\n" +
                    ", b.codicons\n" +
                    ", CASE WHEN (case when round(sum(a.importe_total/1000), 0) <0 then 'H' else 'D' end) = 'H' AND SUBSTRING(b.codicons,1,1) = '5' THEN 'D' WHEN (case when round(sum(a.importe_total/1000), 0) <0 then 'H' else 'D' end) = 'D' AND SUBSTRING(b.codicons,1,1) = '5' THEN 'H' ELSE (case when round(sum(a.importe_total/1000), 0) <0 then 'H' else 'D' end) END naturaleza_total\n" +
                    ", CASE WHEN SUBSTRING(b.codicons,1,1) = '5' THEN round(sum((a.importe_total*-1)/1000), 0) ELSE round(sum(a.importe_total/1000), 0) END saldo\n" +
                    ", c.divisa_neocon divisa\n" +
                    ", 'XX' pais_negocio\n" +
                    ", a.periodo\n" +
                    ", d.yntp intergrupo\n" +
                    ", 'PROV' tipo_registro\n" +
                    "from nexco_plano_ifrs9_intergrupo a\n" +
                    "left join (SELECT puc.CODICONS46 codicons, puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join nexco_divisas c\n" +
                    "on a.divisa = c.id_divisa\n" +
                    "left join (select distinct contrato, yntp from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def where periodo = ?) d\n" +
                    "on a.contrato = d.contrato\n" +
                    "left join nexco_cuentas_neocon e\n" +
                    "on b.codicons = e.cuenta\n" +
                    "where periodo = ? and origen = 'INTERGRUPO_PROVIS.TXT'\n" +
                    "GROUP BY b.codicons, c.divisa_neocon, origen, periodo, d.yntp, e.naturaleza\n" +
                    "union all --Reclasificaciones\n" +
                    "select '00548' sociedad_informante\n" +
                    ", '0' tipo_cons\n" +
                    ", isnull((SELECT TOP 1 TIPO_ASIENTO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'REC'), '') tipo_asiento\n" +
                    ", isnull((SELECT TOP 1 TIPO_PROCESO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'REC'), '') descripcion\n" +
                    ", 'M' movimiento\n" +
                    ", isnull((SELECT TOP 1 referencia FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'REC'), '') referencia\n" +
                    ", 'P' modo_ejecucion\n" +
                    ", 'C388386' usuario\n" +
                    ", a.codicons\n" +
                    ", CASE WHEN a.naturaleza_total = 'D' and isnull(round(abs(sdo_total_plantilla), 0), 0)-isnull(b.saldo, 0) <0 then 'H' WHEN a.naturaleza_total = 'H' and isnull(round(abs(sdo_total_plantilla), 0), 0)-isnull(b.saldo, 0) <0 then 'D' else a.naturaleza_total END naturaleza_total\n" +
                    ", CASE WHEN SUBSTRING(a.codicons,1,1) = '1' THEN (CASE WHEN b.naturaleza_total = 'D' THEN isnull(round(abs(sdo_total_plantilla), 0), 0)-isnull(b.saldo, 0) ELSE isnull(round(abs(sdo_total_plantilla), 0), 0)+isnull(b.saldo, 0) END) ELSE (CASE WHEN b.naturaleza_total = 'D' THEN isnull(round(abs(sdo_total_plantilla), 0), 0)+isnull(b.saldo, 0) ELSE isnull(round(abs(sdo_total_plantilla), 0), 0)-isnull(b.saldo, 0) END) END saldo\n" +
                    ", a.divisa\n" +
                    ", 'XX' pais_negocio\n" +
                    ", a.periodo\n" +
                    ", null intergrupo\n" +
                    ", 'REC' tipo_registro\n" +
                    "from (select * from nexco_val_ifrs9 where sdo_rec <> 0 and periodo = ?) a\n" +
                    "left join (select b.CODICONS46 codicons, round(abs(sum(importe_total/1000)), 0) saldo, c.divisa_neocon divisa, " +
                    " CASE WHEN (case when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) >= 0 then 'D' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) >=0 then 'D' else case when round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' else 'D' end end) = 'H' AND SUBSTRING(b.CODICONS46,1,1) = '5' THEN 'D' WHEN (case when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) >= 0 then 'D' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) >=0 then 'D' else case when round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' else 'D' end end) = 'D' AND SUBSTRING(b.CODICONS46,1,1) = '5' THEN 'H' ELSE (case when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '0' and round(abs(sum(a.importe_total/1000)), 0) >= 0 then 'D' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' when e.naturaleza = '1' and round(abs(sum(a.importe_total/1000)), 0) >=0 then 'D' else case when round(abs(sum(a.importe_total/1000)), 0) <0 then 'H' else 'D' end end) END naturaleza_total,\n" +
                    "'REC' tipo_registro\n" +
                    "from nexco_plano_ifrs9_intergrupo a\n" +
                    "left join (SELECT puc.CODICONS46, puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join nexco_divisas c\n" +
                    "on a.divisa = c.id_divisa\n" +
                    "left join (select distinct contrato, yntp from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def where periodo = ?) d\n" +
                    "on a.contrato = d.contrato\n" +
                    "left join nexco_cuentas_neocon e\n" +
                    "on b.CODICONS46 = e.cuenta\n" +
                    "where periodo = ? and origen = 'INTERGRUPO_RECLAS.TXT'\n" +
                    "GROUP BY b.CODICONS46, c.divisa_neocon, origen, e.naturaleza) b\n" +
                    "on a.codicons = b.codicons and a.divisa = b.divisa\n" +
                    "union all --intergrupo reclasificaciones\n" +
                    "select '00548' sociedad_informante\n" +
                    ", '0' tipo_cons\n" +
                    ", isnull((SELECT TOP 1 TIPO_ASIENTO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'REC'), '') tipo_asiento\n" +
                    ", isnull((SELECT TOP 1 TIPO_PROCESO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'REC'), '') descripcion\n" +
                    ", 'M' movimiento\n" +
                    ", isnull((SELECT TOP 1 referencia FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'REC'), '') referencia\n" +
                    ", 'P' modo_ejecucion\n" +
                    ", 'C388386' usuario\n" +
                    ", b.codicons\n" +
                    ", CASE WHEN b.codicons='50129' and round(sum(a.importe_total/1000), 0) <0 THEN 'H' WHEN b.codicons='50129' and round(sum(a.importe_total/1000), 0) >=0 THEN 'D' WHEN (case when round(sum(a.importe_total/1000), 0) <0 then 'H' else 'D' end) = 'H' AND SUBSTRING(b.codicons,1,1) = '5' THEN 'D' WHEN (case when round(sum(a.importe_total/1000), 0) <0 then 'H' else 'D' end) = 'D' AND SUBSTRING(b.codicons,1,1) = '5' THEN 'H' ELSE (case when round(sum(a.importe_total/1000), 0) <0 then 'H' else 'D' end) END naturaleza_total\n" +
                    ", CASE WHEN SUBSTRING(b.codicons,1,1) = '5' THEN round(sum((a.importe_total*-1)/1000), 0) ELSE round(sum(a.importe_total/1000), 0) END saldo\n" +
                    ", c.divisa_neocon divisa\n" +
                    ", 'XX' pais_negocio\n" +
                    ", a.periodo\n" +
                    ", d.yntp intergrupo\n" +
                    ", 'REC' tipo_registro\n" +
                    "from nexco_plano_ifrs9_intergrupo a\n" +
                    "left join (SELECT puc.CODICONS46 codicons, puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                    "on a.cuenta = b.NUCTA\n" +
                    "left join nexco_divisas c\n" +
                    "on a.divisa = c.id_divisa\n" +
                    "left join (select distinct contrato, yntp from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def where periodo = ?) d\n" +
                    "on a.contrato = d.contrato\n" +
                    "left join nexco_cuentas_neocon e\n" +
                    "on b.codicons = e.cuenta\n" +
                    "where periodo = ? and origen = 'INTERGRUPO_RECLAS.TXT'\n" +
                    "GROUP BY b.codicons, c.divisa_neocon, origen, periodo, d.yntp, e.naturaleza\n" +
                    ";");
            insert1.setParameter(1,periodo);
            insert1.setParameter(2,periodo);
            insert1.setParameter(3,periodo);
            insert1.setParameter(4,periodo);
            insert1.setParameter(5,periodo);
            insert1.setParameter(6,periodo);
            insert1.setParameter(7,periodo);
            insert1.setParameter(8,periodo);
            insert1.setParameter(9,periodo);
            insert1.setParameter(10,periodo);
            insert1.executeUpdate();

            Query insert2 = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon_temp_d (sociedad, tipocons, tipo_asiento, descripcion, movimiento, referencia, modo_ejecucion, usuario, codicons, naturaleza_total, saldo, divisa, pais_negocio, periodo, intergrupo, tipo_registro)\n" +
                    "select '00548' sociedad_informante \n" +
                    ", '0' tipo_cons\n" +
                    ", isnull((SELECT TOP 1 TIPO_ASIENTO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = a.fuenteF), '') tipo_asiento\n" +
                    ", isnull((SELECT TOP 1 TIPO_PROCESO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = a.fuenteF), '') descripcion\n" +
                    ", 'M' movimiento\n" +
                    ", isnull((SELECT TOP 1 referencia FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = a.fuenteF), '') referencia\n" +
                    ", 'P' modo_ejecucion\n" +
                    ", 'C388386' usuario\n" +
                    ", a.codicons\n" +
                    ", a.naturaleza_total \n" +
                    ", isnull(round(abs(a.sdo_total_plantilla), 0), 0) saldo\n" +
                    ", a.divisa\n" +
                    ", 'XX' pais_negocio\n" +
                    ", a.periodo\n" +
                    ", null intergrupo\n" +
                    ", a.fuenteF tipo_registro \n" +
                    "from (select nvi.*,nam.fuente fuenteF from (select * from nexco_val_ifrs9 where sdo_prov = 0 and sdo_rec = 0 and periodo = ?) nvi\n" +
                    "INNER JOIN (SELECT * FROM nexco_ajustes_manuales WHERE periodo = ?) nam ON nvi.codicons = nam.codicons) a");
            insert2.setParameter(1,periodo);
            insert2.setParameter(2,periodo);
            insert2.executeUpdate();

            Query insert3 = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon (sociedad, tipocons, tipo_asiento, descripcion, movimiento, referencia, modo_ejecucion, usuario, codicons, naturaleza_total, saldo, divisa, pais_negocio, periodo, intergrupo, tipo_registro)\n" +
            "SELECT distinct sociedad, tipocons, tipo_asiento, descripcion, movimiento, referencia, modo_ejecucion, usuario, codicons, naturaleza_total, saldo, divisa, pais_negocio, periodo, intergrupo, tipo_registro FROM nexco_plano_ifrs9_neocon_temp_d ");
            insert3.executeUpdate();


            if(listValidationNeocon(periodo).isEmpty())
                valueReturn = true;

            return valueReturn;

        } catch (NoResultException e) {
            e.printStackTrace();
            return valueReturn;
        }
    }

    public List<String> listValidationNeocon(String periodo)
    {
        Query validate0 = entityManager.createNativeQuery("SELECT A.codicons \n" +
                "FROM (SELECT codicons FROM nexco_plano_ifrs9_neocon WHERE periodo = ? and intergrupo is not null and intergrupo != '' GROUP BY codicons) A\n" +
                "INNER JOIN (SELECT cuenta FROM nexco_cuentas_neocon WHERE grscing = 'NA' group by cuenta) B ON A.codicons =B.cuenta\n");
        validate0.setParameter(1,periodo);
        return validate0.getResultList();
    }

    public void insertPlainIFRS9Impu(String periodo, String pygImpu) {

        try {

            Query delete9 = entityManager.createNativeQuery("delete from nexco_plano_ifrs9_neocon where periodo = ? and tipo_registro in ('IMPUE','IMPUEP'); ");
            delete9.setParameter(1,periodo);
            delete9.executeUpdate();

            if(pygImpu.equals("false")) {

                String[] partsPeriodo = periodo.split("-");
                int anioNumber = Integer.parseInt(partsPeriodo[0]);
                anioNumber = anioNumber - 1;

                Query resultF = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon (tipo_registro,sociedad,tipocons,tipo_asiento,descripcion,movimiento,referencia,modo_ejecucion,usuario,codicons,naturaleza_total,saldo,divisa,pais_negocio,periodo)\n" +
                        "select W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.total_saldo,W.divisa,W.pais_negocio,W.periodo \n" +
                        "FROM (\n" +
                        "SELECT 'IMPUE' tipo_registro,\n" +
                        "'00548' Sociedad,\n" +
                        "'0' tipocons,\n" +
                        "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_asiento, \n" +
                        "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_proceso,\n" +
                        "'M' movimiento,\n" +
                        "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') referencia,\n" +
                        "'P' modo_ejecucion,\n" +
                        "'C388386' usuario,\n" +
                        "(SELECT top 1 B.cuenta FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) cuentaC,\n" +
                        "CASE WHEN (A.saldo*-1 * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100< 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                        "(A.saldo*-1 * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100 total_saldo,\n" +
                        "(SELECT top 1 B.divisa FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) divisa,\n" +
                        "'XX' pais_negocio,\n" +
                        ":period periodo \n" +
                        "FROM (SELECT sum(Z.saldo) saldo FROM nexco_plano_ifrs9_neocon Z WHERE Z.tipo_registro = 'PYG' AND Z.periodo = :period) A\n" +
                        "UNION ALL\n" +
                        "SELECT 'IMPUE' tipo_registro,\n" +
                        "'00548' Sociedad,\n" +
                        "'0' tipocons,\n" +
                        "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_asiento, \n" +
                        "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_proceso,\n" +
                        "'M' movimiento,\n" +
                        "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') referencia,\n" +
                        "'P' modo_ejecucion,\n" +
                        "'C388386' usuario,\n" +
                        "(SELECT top 1 B.cuenta_h FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) cuentaC,\n" +
                        "CASE WHEN (A.saldo * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100< 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                        "(A.saldo * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100 total_saldo,\n" +
                        "(SELECT top 1 B.divisa FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) divisa,\n" +
                        "'XX' pais_negocio,\n" +
                        ":period periodo \n" +
                        "FROM (SELECT sum(Z.saldo) saldo FROM nexco_plano_ifrs9_neocon Z WHERE Z.tipo_registro = 'PYG' AND Z.periodo = :period) A\n" +
                        "UNION ALL\n" +
                        "SELECT 'IMPUEP' tipo_registro,\n" +
                        "'00548' Sociedad,\n" +
                        "'0' tipocons,\n" +
                        "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_asiento, \n" +
                        "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_proceso,\n" +
                        "'M' movimiento,\n" +
                        "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') referencia,\n" +
                        "'P' modo_ejecucion,\n" +
                        "'C388386' usuario,\n" +
                        "(SELECT top 1 B.cuenta FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) cuentaC,\n" +
                        "CASE WHEN (SELECT top 1 B.valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio)< 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                        "(SELECT top 1 B.valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) total_saldo,\n" +
                        "(SELECT top 1 B.divisa FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) divisa,\n" +
                        "'XX' pais_negocio,\n" +
                        ":period periodo \n" +
                        "FROM (SELECT sum(Z.saldo) saldo FROM nexco_plano_ifrs9_neocon Z WHERE Z.tipo_registro = 'PYG' AND Z.periodo=:period) A\n" +
                        ") W\n" +
                        "GROUP BY W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.total_saldo,W.divisa,W.pais_negocio,W.periodo ");
                resultF.setParameter("period",periodo);
                resultF.setParameter("anio",anioNumber);
                resultF.executeUpdate();
            }

        } catch (NoResultException e) {
            e.printStackTrace();
        }
    }

    public void insertPlainIFRS9PYG(String periodo, String pygData) {

        try {

            Query delete9 = entityManager.createNativeQuery("delete from nexco_plano_ifrs9_neocon where periodo = ? and tipo_registro in ('PYG'); ");
            delete9.setParameter(1,periodo);
            delete9.executeUpdate();

            if(pygData.equals("false")) {

                String[] partsPeriodo = periodo.split("-");
                int anioNumber = Integer.parseInt(partsPeriodo[0]);
                anioNumber = anioNumber - 1;

                Query resultF = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon (tipo_registro,sociedad,tipocons,tipo_asiento,descripcion,movimiento,referencia,modo_ejecucion,usuario,codicons,naturaleza_total,saldo,divisa,pais_negocio,periodo) " +
                        "select W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,sum(W.total_saldo),W.divisa,W.pais_negocio,W.periodo \n" +
                        "FROM (select 'PYG' tipo_registro,'00548' Sociedad,'0' tipocons,C.tipo_asiento,C.tipo_proceso,'M' movimiento,C.referencia,'P' modo_ejecucion,'C388386' usuario,CASE WHEN (A.saldo-B.valor) < 0 THEN B.cuenta_h ELSE B.cuenta END cuentaC,CASE WHEN (A.saldo-B.valor) < 0 THEN 'H' ELSE 'D' END naturaleza,ISNULL(A.saldo-B.valor,0) total_saldo,A.divisa,'XX' pais_negocio,? periodo \n" +
                        "from (SELECT X.instrumento, X.stage_espana, Z.divisa, Z.tipo_registro,((sum(Z.saldo))*-1) saldo FROM nexco_plano_ifrs9_neocon Z\n" +
                        "INNER JOIN nexco_provisiones X ON Z.codicons = X.cuenta_neocon\n" +
                        "WHERE tipo_registro = 'PROV' and periodo = ? GROUP BY X.instrumento, X.stage_espana, Z.divisa, Z.tipo_registro) A\n" +
                        "INNER JOIN (SELECT anio,tipo,stage,divisa,cuenta,cuenta_h,sum(valor) valor FROM nexco_parametria_pyg GROUP BY anio,tipo,stage,divisa,cuenta,cuenta_h) B ON A.instrumento = UPPER(B.tipo) AND A.stage_espana = B.stage AND A.divisa = B.divisa and anio = ? \n" +
                        "LEFT JOIN nexco_tipo_plantilla_esp C ON C.descripcion = 'PYG'\n" +
                        "GROUP BY CASE WHEN (A.saldo-B.valor) < 0 THEN B.cuenta_h ELSE B.cuenta END, C.tipo_proceso, A.divisa,CASE WHEN (A.saldo-B.valor) < 0 THEN 'H' ELSE 'D' END,C.referencia,ISNULL(A.saldo-B.valor,0),C.tipo_asiento\n" +
                        ")W\n" +
                        "GROUP BY W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.divisa,W.pais_negocio,W.periodo ");

                resultF.setParameter(1,periodo);
                resultF.setParameter(2,periodo);
                resultF.setParameter(3,String.valueOf(anioNumber));
                resultF.executeUpdate();

            }

        } catch (NoResultException e) {
            e.printStackTrace();
        }
    }
    public void insertPlainIFRS9PYG2(String periodo, String pygData) {

        try {

            Query delete9 = entityManager.createNativeQuery("delete from nexco_plano_ifrs9_neocon where periodo = ? and tipo_registro in ('PYG REPOS','REPOS'); ");
            delete9.setParameter(1,periodo);
            delete9.executeUpdate();

            if(pygData.equals("false")) {

                String[] partsPeriodo = periodo.split("-");
                int anioNumber = Integer.parseInt(partsPeriodo[0]);
                anioNumber = anioNumber - 1;

                Query resultF = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon (tipo_registro,sociedad,tipocons,tipo_asiento,descripcion,movimiento,referencia,modo_ejecucion,usuario,codicons,naturaleza_total,saldo,divisa,pais_negocio,periodo) " +
                        "select W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,sum(W.total_saldo),W.divisa,W.pais_negocio,W.periodo \n" +
                        "FROM (select 'REPOS' tipo_registro,'00548' Sociedad,'0' tipocons,C.tipo_asiento,C.tipo_proceso,'M' movimiento,C.referencia,'P' modo_ejecucion,'C388386' usuario, A.cta_neocon cuentaC,CASE WHEN (A.AJ/1000) < 0 THEN 'H' ELSE 'D' END naturaleza,ISNULL(A.AJ/1000,0) total_saldo,H.divisa_neocon divisa,'XX' pais_negocio,? periodo \n" +
                        "from (select T.cta_neocon,T.dvsa_cntble_1_conciliacion, SUM(T.ajuste)AJ \n" +
                        "FROM nexco_rys_conciliacion T WHERE T.periodo = ?\n" +
                        "GROUP BY T.cta_neocon,T.dvsa_cntble_1_conciliacion) A\n" +
                        "LEFT JOIN nexco_tipo_plantilla_esp C ON C.descripcion = 'REPOS'\n" +
                        "LEFT JOIN nexco_divisas H ON A.dvsa_cntble_1_conciliacion = H.id_divisa\n" +
                        "GROUP BY A.cta_neocon, C.tipo_proceso, H.divisa_neocon,CASE WHEN (A.AJ/1000) < 0 THEN 'H' ELSE 'D' END,C.referencia,ISNULL(A.AJ/1000,0),C.tipo_asiento \n" +
                        "UNION ALL\n" +//PYG REPOS
                        "SELECT 'PYG REPOS' tipo_registro,\n" +
                        "'00548' Sociedad,\n" +
                        "'0' tipocons,\n" +
                        "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_asiento, \n" +
                        "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_proceso,\n" +
                        "'M' movimiento,\n" +
                        "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') referencia,\n" +
                        "'P' modo_ejecucion,\n" +
                        "'C388386' usuario,\n" +
                        "CASE WHEN Z.ajuste-X.valor < 0 THEN X.cuenta_h ELSE X.cuenta END cuentaC,\n" +
                        "CASE WHEN Z.ajuste-X.valor < 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                        "SUM(Z.ajuste-X.valor) total_saldo,\n" +
                        "X.divisa,\n" +
                        "'XX' pais_negocio,\n" +
                        "? periodo \n" +
                        "FROM (SELECT A.cta_neocon,A.tipo_entidad,SUM(A.ajuste_pyg)/1000 ajuste FROM nexco_rys_conciliacion A WHERE A.periodo = ? GROUP BY A.cta_neocon,A.tipo_entidad) Z\n" +
                        "INNER JOIN (SELECT B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h,sum(B.valor) valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'REPOS' and B.anio = ? GROUP BY B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h) X ON UPPER(Z.tipo_entidad) = UPPER(X.tipo) AND substring(Z.cta_neocon,1,1) = X.stage\n" +
                        "GROUP BY\n" +
                        "CASE WHEN Z.ajuste-X.valor < 0 THEN X.cuenta_h ELSE X.cuenta END ,\n" +
                        "CASE WHEN Z.ajuste-X.valor < 0 THEN 'H' ELSE 'D' END ,\n" +
                        "X.divisa " +
                        "UNION ALL \n" +
                        "SELECT 'PYG REPOS' tipo_registro,\n" +
                        "'00548' Sociedad,\n" +
                        "'0' tipocons,\n" +
                        "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_asiento, \n" +
                        "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_proceso,\n" +
                        "'M' movimiento,\n" +
                        "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') referencia,\n" +
                        "'P' modo_ejecucion,\n" +
                        "'C388386' usuario,\n" +
                        "CASE WHEN 0-Z.valor < 0 THEN Z.cuenta_h ELSE Z.cuenta END cuentaC,\n" +
                        "CASE WHEN 0-Z.valor < 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                        "SUM(0-Z.valor) total_saldo,\n" +
                        "Z.divisa,\n" +
                        "'XX' pais_negocio,\n" +
                        "? periodo\n" +
                        "FROM nexco_parametria_pyg Z WHERE Z.descripcion = 'REPOS' and Z.anio = ? and Z.tipo NOT IN (\n" +
                        "select tipo_entidad\n" +
                        "FROM (SELECT A.cta_neocon,A.tipo_entidad,SUM(A.ajuste_pyg)/1000 ajuste FROM nexco_rys_conciliacion A WHERE A.periodo = ? GROUP BY A.cta_neocon,A.tipo_entidad) Z\n" +
                        "INNER JOIN (SELECT B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h,sum(B.valor) valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'REPOS' and B.anio = ? GROUP BY B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h) X ON UPPER(Z.tipo_entidad) = UPPER(X.tipo) AND substring(Z.cta_neocon,1,1) = X.stage\n" +
                        "GROUP BY tipo_entidad)\n" +
                        "GROUP BY\n" +
                        "CASE WHEN 0-Z.valor < 0 THEN Z.cuenta_h ELSE Z.cuenta END ,\n" +
                        "CASE WHEN 0-Z.valor < 0 THEN 'H' ELSE 'D' END ,\n" +
                        "Z.divisa)W\n" +
                        "GROUP BY W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.divisa,W.pais_negocio,W.periodo ");

                resultF.setParameter(1,periodo);
                resultF.setParameter(2,periodo);

                resultF.setParameter(3,periodo);
                resultF.setParameter(4,periodo);
                resultF.setParameter(5,String.valueOf(anioNumber));

                resultF.setParameter(6,periodo);
                resultF.setParameter(7,String.valueOf(anioNumber));
                resultF.setParameter(8,periodo);
                resultF.setParameter(9,String.valueOf(anioNumber));
                resultF.executeUpdate();

            }

        } catch (NoResultException e) {
            e.printStackTrace();
        }
    }

    public List<PlainIFRS9> findIFRS9(Integer id){
        Query query = entityManager.createNativeQuery("SELECT id, sociedad, tipocons, tipo_asiento, descripcion, movimiento, referencia, " +
                "modo_ejecucion, usuario, codicons, naturaleza_total, saldo, divisa, pais_negocio, periodo, tipo_registro, cod_desglose, intergrupo \n" +
                "FROM nexco_plano_ifrs9_neocon \n" +
                "WHERE id = ?",PlainIFRS9.class);

        query.setParameter(1, id);
        return query.getResultList();
    }

    public void modifyPlain(PlainIFRS9 toModify,Integer id){

        PlainIFRS9 toInsert = new PlainIFRS9();
        toInsert.setSociedad(toModify.getSociedad());
        toInsert.setTipocons(toModify.getTipocons());
        toInsert.setTipoAsiento(toModify.getTipoAsiento());
        toInsert.setDescripcion(toModify.getDescripcion());
        toInsert.setMovimiento(toModify.getMovimiento());
        toInsert.setReferencia(toModify.getReferencia());
        toInsert.setModoEjecucion(toModify.getModoEjecucion());
        toInsert.setUsuario(toModify.getUsuario());
        toInsert.setCodicons(toModify.getCodicons());
        toInsert.setNaturalezaTotal(toModify.getNaturalezaTotal());
        toInsert.setSaldo(toModify.getSaldo());
        toInsert.setDivisa(toModify.getDivisa());
        toInsert.setPaisNegocio(toModify.getPaisNegocio());

        Query query = entityManager.createNativeQuery("UPDATE nexco_plano_ifrs9_neocon SET sociedad = ? , \n" +
                "tipocons = ? , tipo_asiento = ? , descripcion = ? , movimiento = ? , referencia = ? ,\n" +
                "modo_ejecucion = ? , usuario = ? , codicons = ? , naturaleza_total = ? , saldo = ? , divisa = ? , pais_negocio = ? \n" +
                "WHERE id = ? ", PlainIFRS9.class);
        query.setParameter(1, toInsert.getSociedad());
        query.setParameter(2, toInsert.getTipocons());
        query.setParameter(3, toInsert.getTipoAsiento());
        query.setParameter(4, toInsert.getDescripcion());
        query.setParameter(5, toInsert.getMovimiento());
        query.setParameter(6, toInsert.getReferencia());
        query.setParameter(7, toInsert.getModoEjecucion());
        query.setParameter(8, toInsert.getUsuario());
        query.setParameter(9, toInsert.getCodicons());
        query.setParameter(10, toInsert.getNaturalezaTotal());
        query.setParameter(11, toInsert.getSaldo());
        query.setParameter(12, toInsert.getDivisa());
        query.setParameter(13, toInsert.getPaisNegocio());
        query.setParameter(14, id);

        try {
            query.executeUpdate();
        }catch(Exception e){

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
    public void clearInterPlane(String periodo)
    {
        Query validate = entityManager.createNativeQuery("delete from nexco_plano_ifrs9_intergrupo where periodo = ? ;");
        validate.setParameter(1,periodo);
        validate.executeUpdate();
    }

    public List<Object[]> getValues(String periodo){

        Query validate = entityManager.createNativeQuery(
                "select top 1 * from nexco_val_ifrs9 where periodo = ? ;");
        validate.setParameter(1,periodo);
        List<Object[]> validateData = validate.getResultList();

        return validateData;

    }

    public List<Object[]> getValuesPyg(String periodo){

        Query validate = entityManager.createNativeQuery(
                "select top 1 * from nexco_plano_ifrs9_neocon where periodo = ? AND tipo_registro IN ('PYG');");
        validate.setParameter(1,periodo);
        List<Object[]> validateData = validate.getResultList();
        return validateData;

    }
    public List<Object[]> getValuesPyg2(String periodo){

        Query validate = entityManager.createNativeQuery(
                "select top 1 * from nexco_plano_ifrs9_neocon where periodo = ? AND tipo_registro IN ('PYG REPOS','REPOS');");
        validate.setParameter(1,periodo);
        List<Object[]> validateData = validate.getResultList();
        return validateData;

    }

    public List<Object[]> getValuesImpu(String periodo){

        Query validate = entityManager.createNativeQuery(
                "select top 1 * from nexco_plano_ifrs9_neocon where periodo = ? AND tipo_registro IN ('IMPUE','IMPUEP');");
        validate.setParameter(1,periodo);
        List<Object[]> validateData = validate.getResultList();
        return validateData;

    }

    public List<PlainIFRS9> getPlainIFRS9(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_plano_ifrs9_neocon where periodo = ? order by tipo_registro, codicons, divisa", PlainIFRS9.class);
        getQuery.setParameter(1, periodo);
        return getQuery.getResultList();
    }

    public List<PlainIFRS9> findByFilter(String value, String filter, String period) {

        ArrayList<PlainIFRS9> toReturn;

        switch (filter) {

            case "Tipo Registro":
                Query query = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ifrs9_neocon WHERE tipo_registro LIKE ? and periodo LIKE ? order by tipo_registro, codicons, divisa", PlainIFRS9.class);
                query.setParameter(1, value);
                query.setParameter(2, period);
                if (query.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<PlainIFRS9>) query.getResultList();
                }
                break;

            case "Código Consolidación":
                Query query1 = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ifrs9_neocon WHERE codicons LIKE ? and periodo LIKE ? order by tipo_registro, codicons, divisa", PlainIFRS9.class);
                query1.setParameter(1, value);
                query1.setParameter(2, period);
                if (query1.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<PlainIFRS9>) query1.getResultList();
                }
                break;

            case "Divisa":
                Query query2 = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ifrs9_neocon WHERE divisa LIKE ? and periodo LIKE ? order by tipo_registro, codicons, divisa", PlainIFRS9.class);
                query2.setParameter(1, value);
                query2.setParameter(2, period);
                if (query2.getResultList().isEmpty()) {
                    toReturn = new ArrayList<>();
                } else {
                    toReturn = (ArrayList<PlainIFRS9>) query2.getResultList();
                }
                break;

            default:
                toReturn = new ArrayList<>();
        }


        return toReturn;
    }


    public void clearPlainIFRS9(User user, String periodo){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_plano_ifrs9_neocon where periodo = ?", PlainIFRS9.class);
        query.setParameter(1, periodo);
        query.executeUpdate();
    }

    public boolean uploadFilesIntergroup(String period, MultipartFile[] files){

        ArrayList<PlainIFRS9Intergroup> toInsert = new ArrayList<>();
        ArrayList<PlainIFRS9IntergroupOrigin> toInsertOrigin = new ArrayList<>();

        Arrays.stream(files).forEach(multipartFile -> {

            if(multipartFile.getOriginalFilename().equals("INTERGRUPO_PROVIS.TXT") || multipartFile.getOriginalFilename().equals("INTERGRUPO_RECLAS.TXT") || multipartFile.getOriginalFilename().equals("INTERGRUPO_IMPUES.TXT")){
                BufferedReader br;
                String line;
                try {
                    InputStream is = multipartFile.getInputStream();
                    br = new BufferedReader(new InputStreamReader(is));
                    while ((line = br.readLine()) != null){
                        PlainIFRS9Intergroup upload = new PlainIFRS9Intergroup();

                        upload.setEmpresa(line.substring(0, 4).trim());
                        upload.setFechaContable(line.substring(7, 15).trim());
                        upload.setFecha(line.substring(15, 23).trim());
                        upload.setDivisa(line.substring(60, 63).trim());
                        upload.setCuenta(line.substring(74, 92).trim());
                        upload.setImporteD(Double.parseDouble(line.substring(124, 139).trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setImporteC(Double.parseDouble(line.substring(139, 154).trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setImporteDD(Double.parseDouble(line.substring(154, 169).trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setImporteCD(Double.parseDouble(line.substring(169, 184).trim()
                                .replace("+","")
                                .replace(",","")));
                        upload.setObservacion(line.substring(215, 245).trim());
                        upload.setContrato(line.substring(245, 263).trim());
                        upload.setOrigen(multipartFile.getOriginalFilename());
                        upload.setPeriodo(period);

                        if(!upload.getCuenta().substring(0,4).equals("1960") && !upload.getCuenta().substring(0,1).equals("4") && !upload.getCuenta().substring(0,1).equals("5"))
                            toInsert.add(upload);

                        PlainIFRS9IntergroupOrigin uploadO = new PlainIFRS9IntergroupOrigin();

                        uploadO.setEmpresa(line.substring(0, 4).trim());
                        uploadO.setFechaContable(line.substring(7, 15).trim());
                        uploadO.setFecha(line.substring(15, 23).trim());
                        uploadO.setDivisa(line.substring(60, 63).trim());
                        uploadO.setCuenta(line.substring(74, 92).trim());
                        uploadO.setImporteD(Double.parseDouble(line.substring(124, 139).trim()
                                .replace("+","")
                                .replace(",","")));
                        uploadO.setImporteC(Double.parseDouble(line.substring(139, 154).trim()
                                .replace("+","")
                                .replace(",","")));
                        uploadO.setImporteDD(Double.parseDouble(line.substring(154, 169).trim()
                                .replace("+","")
                                .replace(",","")));
                        uploadO.setImporteCD(Double.parseDouble(line.substring(169, 184).trim()
                                .replace("+","")
                                .replace(",","")));
                        uploadO.setObservacion(line.substring(215, 245).trim());
                        uploadO.setContrato(line.substring(245, 263).trim());
                        uploadO.setOrigen(multipartFile.getOriginalFilename());
                        uploadO.setPeriodo(period);

                        toInsertOrigin.add(uploadO);

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        if(!toInsert.isEmpty()){
            Query delete = entityManager.createNativeQuery("DELETE FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ?");
            delete.setParameter(1,period);
            delete.executeUpdate();

            Query deleteClone = entityManager.createNativeQuery("Delete from nexco_plano_ifrs9_intergrupo_origin where periodo = ? ");
            deleteClone.setParameter(1,period);
            deleteClone.executeUpdate();

            batchInsert(toInsert);
            batchInsertO(toInsertOrigin);

            Query updateO = entityManager.createNativeQuery("UPDATE nexco_plano_ifrs9_intergrupo_origin\n" +
                    "SET importe_c = importe_c/100, importe_d = importe_d/100, importe_dd = importe_dd/100, importe_cd = importe_cd/100 \n" +
                    "WHERE periodo = ?");
            updateO.setParameter(1, period);
            updateO.executeUpdate();

            Query updateO2 = entityManager.createNativeQuery("update a\n" +
                    "set importe_cd_exp = importe_cd*b.valor, importe_dd_exp = importe_dd*b.valor\n" +
                    "from nexco_plano_ifrs9_intergrupo_origin a\n" +
                    "left join (select * from nexco_divisas_valor where substring(convert(varchar, fecha, 23), 1, 7) = ?) b\n" +
                    "on a.divisa = b.divisa\n" +
                    "where periodo = ?");
            updateO2.setParameter(1, period);
            updateO2.setParameter(2, period);
            updateO2.executeUpdate();


            Query updateO3 = entityManager.createNativeQuery("UPDATE nexco_plano_ifrs9_intergrupo_origin\n" +
                    "SET importe_total = (importe_d - importe_c)+(importe_dd_exp - importe_cd_exp) \n" +
                    "WHERE periodo = ?");
            updateO3.setParameter(1, period);
            updateO3.executeUpdate();

            updateInsertAccount(period);

            Query select = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ? AND substring(fecha_contable, 1, 6) <> replace(periodo, '-', '');");
            select.setParameter(1,period);
            select.getResultList();

            if(select.getResultList().isEmpty()) {

                Query update = entityManager.createNativeQuery("UPDATE nexco_plano_ifrs9_intergrupo\n" +
                        "SET importe_c = importe_c/100, importe_d = importe_d/100, importe_dd = importe_dd/100, importe_cd = importe_cd/100 \n" +
                        "WHERE periodo = ?");
                update.setParameter(1, period);
                update.executeUpdate();

                Query update2 = entityManager.createNativeQuery("update a\n" +
                        "set importe_cd_exp = importe_cd*b.valor, importe_dd_exp = importe_dd*b.valor\n" +
                        "from nexco_plano_ifrs9_intergrupo a\n" +
                        "left join (select * from nexco_divisas_valor where substring(convert(varchar, fecha, 23), 1, 7) = ?) b\n" +
                        "on a.divisa = b.divisa\n" +
                        "where periodo = ?");
                update2.setParameter(1, period);
                update2.setParameter(2, period);
                update2.executeUpdate();


                Query update3 = entityManager.createNativeQuery("UPDATE nexco_plano_ifrs9_intergrupo\n" +
                        "SET importe_total = (importe_d - importe_c)+(importe_dd_exp - importe_cd_exp) \n" +
                        "WHERE periodo = ?");
                update3.setParameter(1, period);
                update3.executeUpdate();


                Query delete9 = entityManager.createNativeQuery("delete from nexco_plano_ifrs9_neocon where periodo = ? and tipo_registro = 'INTER'; ");
                delete9.setParameter(1,period);
                delete9.executeUpdate();
                /*
                Query insert = entityManager.createNativeQuery("insert into nexco_plano_ifrs9_neocon (sociedad, tipocons, tipo_asiento, descripcion, movimiento, referencia, modo_ejecucion, usuario, codicons, naturaleza_total, saldo, divisa, pais_negocio, periodo, tipo_registro)\n" +
                        "select '00548' sociedad_informante\n" +
                        ", '0' tipo_cons\n" +
                        ", isnull((SELECT TOP 1 TIPO_ASIENTO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'INTER'), '') tipo_asiento\n" +
                        ", isnull((SELECT TOP 1 TIPO_PROCESO FROM nexco_tipo_plantilla_esp WHERE DESCRIPCION = 'INTER'), '') descripcion\n" +
                        ", 'M' movimiento\n" +
                        ", '000000000464' referencia\n" +
                        ", 'P' modo_ejecucion\n" +
                        ", 'C388386' usuario\n" +
                        ", b.CODICONS46 codicons\n" +
                        ", case when sum(importe_total) < 0 then 'H' else 'D' end naturaleza_total\n" +
                        ", round(abs(sum(importe_total)), 0) saldo\n" +
                        ", c.divisa_neocon divisa\n" +
                        ", 'XX' pais_negocio\n" +
                        ", periodo\n" +
                        ", 'INTER' tipo_registro\n" +
                        "from nexco_plano_ifrs9_intergrupo a\n" +
                        "left join (SELECT puc.CODICONS46 , puc.NUCTA FROM CUENTAS_PUC as puc WHERE puc.EMPRESA = '0013') b\n" +
                        "on a.cuenta = b.NUCTA\n" +
                        "left join nexco_divisas c\n" +
                        "on a.divisa = c.id_divisa\n" +
                        "where periodo = ? \n" +
                        "GROUP BY b.CODICONS46, c.divisa_neocon, case when importe_total < 0 then 'H' else 'D' end, periodo\n" +
                        ";");
                insert.setParameter(1, period);
                insert.executeUpdate();
                */
                //status
                Query selectVal1 = entityManager.createNativeQuery("SELECT top 1 * FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ? AND origen = 'INTERGRUPO_PROVIS.TXT'");
                selectVal1.setParameter(1,period);
                selectVal1.getResultList();


                if(!selectVal1.getResultList().isEmpty()){
                    Date today = new Date();
                    String input = "PLANOINTERGRUPO-PROV";

                    StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo(input, period);

                    if (validateStatus == null) {
                        StatusInfo status = new StatusInfo();
                        status.setInput(input);
                        status.setPeriodo(period);
                        status.setFecha(today);
                        StatusInfoRepository.save(status);
                    } else {
                        validateStatus.setFecha(today);
                        StatusInfoRepository.save(validateStatus);
                    }
                }

                Query selectVal2 = entityManager.createNativeQuery("SELECT top 1  * FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ? AND origen = 'INTERGRUPO_RECLAS.TXT'");
                selectVal2.setParameter(1,period);
                selectVal2.getResultList();

                if(!selectVal2.getResultList().isEmpty()){
                    Date today2 = new Date();
                    String input2 = "PLANOINTERGRUPO-REC";

                    StatusInfo validateStatus2 = StatusInfoRepository.findByInputAndPeriodo(input2, period);

                    if (validateStatus2 == null) {
                        StatusInfo status2 = new StatusInfo();
                        status2.setInput(input2);
                        status2.setPeriodo(period);
                        status2.setFecha(today2);
                        StatusInfoRepository.save(status2);
                    } else {
                        validateStatus2.setFecha(today2);
                        StatusInfoRepository.save(validateStatus2);
                    }
                }

                return true;

            }else{

                Query delete1 = entityManager.createNativeQuery("DELETE FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ?");
                delete1.setParameter(1,period);
                delete1.executeUpdate();

                return false;
            }

        }else{

            return false;

        }
    }

    public void updateInsertAccount(String period)
    {
        Query delete0 = entityManager.createNativeQuery("update a\n" +
                "set a.cuenta = b.cuenta_local \n" +
                "from nexco_plano_ifrs9_intergrupo a\n" +
                "inner join (SELECT distinct contrato,cuenta_local,divisa from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def z\n" +
                "inner join (SELECT CODICONS46, NUCTA FROM CUENTAS_PUC WHERE EMPRESA='0013' GROUP BY CODICONS46, NUCTA) c ON z.cuenta_local = c.NUCTA\n" +
                "inner join (SELECT * FROM nexco_provisiones WHERE ifrs9 = 'CV') d ON c.CODICONS46 = d.cuenta_neocon\n" +
                "where periodo = ?) b ON a.contrato=b.contrato and a.divisa=b.divisa and Substring(a.cuenta,1,2) = Substring(b.cuenta_local ,1,2)\n" +
                "where a.cuenta != b.cuenta_local and a.periodo = ? and (LEN(a.cuenta) <= 12 or (LEN(a.cuenta) = 15 and Substring(a.cuenta ,LEN(a.cuenta)-2,3) NOT LIKE '99%')) and origen='INTERGRUPO_PROVIS.TXT'");
        delete0.setParameter(1,period);
        delete0.setParameter(2,period);
        delete0.executeUpdate();

        Query delete1 = entityManager.createNativeQuery("update a\n" +
                "set a.cuenta = b.cuenta_local \n" +
                "from nexco_plano_ifrs9_intergrupo a\n" +
                "inner join (SELECT distinct contrato,cuenta_local,divisa from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def z\n" +
                "inner join (SELECT CODICONS46, NUCTA FROM CUENTAS_PUC WHERE EMPRESA='0013' GROUP BY CODICONS46, NUCTA) c ON z.cuenta_local = c.NUCTA\n" +
                "inner join (SELECT * FROM nexco_provisiones WHERE ifrs9 = 'PR') d ON c.CODICONS46 = d.cuenta_neocon\n" +
                "where periodo = ?) b ON a.contrato=b.contrato and a.divisa=b.divisa and Substring(a.cuenta,1,2) = Substring(b.cuenta_local ,1,2)\n" +
                "where a.cuenta != b.cuenta_local and a.periodo = ? and LEN(a.cuenta) <= 12 and origen='INTERGRUPO_RECLAS.TXT'");
        delete1.setParameter(1,period);
        delete1.setParameter(2,period);
        delete1.executeUpdate();
    }

    public void batchInsert(List<PlainIFRS9Intergroup> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_plano_ifrs9_intergrupo (empresa, fecha_contable, fecha, divisa, cuenta, importe_d, importe_c, importe_dd, importe_cd, observacion, contrato, origen, periodo) \n" +
                        "values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getFechaContable());
                        ps.setString(3, temporal.get(i).getFecha());
                        ps.setString(4, temporal.get(i).getDivisa());
                        ps.setString(5, temporal.get(i).getCuenta());
                        ps.setDouble(6, temporal.get(i).getImporteD());
                        ps.setDouble(7, temporal.get(i).getImporteC());
                        ps.setDouble(8, temporal.get(i).getImporteDD());
                        ps.setDouble(9, temporal.get(i).getImporteCD());
                        ps.setString(10, temporal.get(i).getObservacion());
                        ps.setString(11, temporal.get(i).getContrato());
                        ps.setString(12, temporal.get(i).getOrigen());
                        ps.setString(13, temporal.get(i).getPeriodo());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public void batchInsertO(List<PlainIFRS9IntergroupOrigin> temporal) {

        jdbcTemplate.batchUpdate(
                "insert into nexco_plano_ifrs9_intergrupo_origin (empresa, fecha_contable, fecha, divisa, cuenta, importe_d, importe_c, importe_dd, importe_cd, observacion, contrato, origen, periodo) \n" +
                        "values (?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getEmpresa());
                        ps.setString(2, temporal.get(i).getFechaContable());
                        ps.setString(3, temporal.get(i).getFecha());
                        ps.setString(4, temporal.get(i).getDivisa());
                        ps.setString(5, temporal.get(i).getCuenta());
                        ps.setDouble(6, temporal.get(i).getImporteD());
                        ps.setDouble(7, temporal.get(i).getImporteC());
                        ps.setDouble(8, temporal.get(i).getImporteDD());
                        ps.setDouble(9, temporal.get(i).getImporteCD());
                        ps.setString(10, temporal.get(i).getObservacion());
                        ps.setString(11, temporal.get(i).getContrato());
                        ps.setString(12, temporal.get(i).getOrigen());
                        ps.setString(13, temporal.get(i).getPeriodo());
                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public List<Object[]> validateCurrents(String periodo){

        Query validate = entityManager.createNativeQuery(
                "select * from nexco_divisas_valor where substring(convert(varchar, fecha, 23), 1, 7) = ?;");
        validate.setParameter(1,periodo);
        List<Object[]> validateData = validate.getResultList();

        return validateData;

    }

    public List<Object[]> getPlain(String period, String type){

        Query plain = entityManager.createNativeQuery("select case when sociedad is null then '00548' else sociedad end \n" +
                "+ tipocons\n" +
                "+ replicate(' ', 9 - LEN(isnull(tipo_asiento, '')))+isnull(tipo_asiento, '') --tipo_asiento\n" +
                "+ isnull(descripcion, '') + REPLICATE(' ', 2000 - LEN(isnull(descripcion, ''))) --descripcion\n" +
                "+ movimiento\n" +
                "+ referencia\n" +
                "+ modo_ejecucion\n" +
                "+ usuario\n" +
                "+ codicons\n" +
                "+ naturaleza_total\n" +
                "+ replicate('0', 18 - len(convert(varchar, convert(numeric(18), round(abs(saldo), 0))))) + convert(varchar, convert(numeric(18), round(abs(saldo), 0))) --saldo\n" +
                "+ case when divisa is null then 'MLL' else divisa end \n" +
                "+ pais_negocio\n" +
                "+ '   ' --cod_desglose\n" +
                "+ case when intergrupo is null then '     ' else intergrupo end \n" +
                "plano\n" +
                "FROM\n" +
                "(SELECT codicons,descripcion,divisa,modo_ejecucion,movimiento,naturaleza_total,pais_negocio,periodo,referencia,sum(saldo) saldo,intergrupo,sociedad,tipo_asiento,tipocons,usuario FROM nexco_plano_ifrs9_neocon\n" +
                "where periodo = ? \n" +
                "and  round(abs(saldo), 0) <> 0  \n" +
                "and tipo_registro IN ("+type+")\n" +
                "GROUP BY codicons,descripcion,divisa,modo_ejecucion,movimiento,naturaleza_total,pais_negocio,periodo,referencia,intergrupo,sociedad,tipo_asiento,tipocons,usuario) A GROUP BY " +
                "case when sociedad is null then '00548' else sociedad end \n" +
                "+ tipocons\n" +
                "+ replicate(' ', 9 - LEN(isnull(tipo_asiento, '')))+isnull(tipo_asiento, '') --tipo_asiento\n" +
                "+ isnull(descripcion, '') + REPLICATE(' ', 2000 - LEN(isnull(descripcion, ''))) --descripcion\n" +
                "+ movimiento\n" +
                "+ referencia\n" +
                "+ modo_ejecucion\n" +
                "+ usuario\n" +
                "+ codicons\n" +
                "+ naturaleza_total\n" +
                "+ replicate('0', 18 - len(convert(varchar, convert(numeric(18), round(abs(saldo), 0))))) + convert(varchar, convert(numeric(18), round(abs(saldo), 0))) --saldo\n" +
                "+ case when divisa is null then 'MLL' else divisa end \n" +
                "+ pais_negocio\n" +
                "+ '   ' --cod_desglose\n" +
                "+ case when intergrupo is null then '     ' else intergrupo end \n" +
                ";");
        plain.setParameter(1,period);
        return plain.getResultList();
    }

    public List<Object[]> getResumenIFRS9(String period, String type){

        String typeText="'"+type+"'";
        if(type.equals("PYG"))
            typeText="'PYG REPOS','REPOS'";
        else if(type.equals("PROV"))
            typeText="'PROV','PYG','IMPUE','IMPUEP'";

        Query plain = entityManager.createNativeQuery("select Z.descripcion,\n" +
                "Z.digito,\n" +
                "Z.divisa ,\n" +
                "CASE WHEN a.naturaleza_total = 'D' THEN sum(convert(numeric(18), round(abs(a.VND), 0))) ELSE 0 END D,\n" +
                "CASE WHEN b.naturaleza_total = 'H' THEN sum(convert(numeric(18), round(abs(b.VNH), 0))) ELSE 0 END H,\n" +
                "CASE WHEN a.naturaleza_total = 'D' THEN sum(convert(numeric(18), round(abs(a.VND), 0))) ELSE 0 END -\n" +
                "CASE WHEN b.naturaleza_total = 'H' THEN sum(convert(numeric(18), round(abs(b.VNH), 0))) ELSE 0 END Diferencia \n" +
                "FROM (select descripcion,SUBSTRING(codicons,1,1) digito,divisa from nexco_plano_ifrs9_neocon where periodo = ? and  round(abs(saldo), 0) <> 0  and tipo_registro IN ("+typeText+") group by descripcion,SUBSTRING(codicons,1,1),divisa) Z\n" +
                "left join(select descripcion,SUBSTRING(codicons,1,1) digito, naturaleza_total,divisa,sum(convert(numeric(18), round(abs(saldo), 0))) VND\n" +
                "from nexco_plano_ifrs9_neocon where periodo = ? AND naturaleza_total = 'D' and  round(abs(saldo), 0) <> 0  and tipo_registro IN ("+typeText+") \n" +
                "group by descripcion,SUBSTRING(codicons,1,1), naturaleza_total,divisa) a\n" +
                "ON z.digito = a.digito and z.divisa = a.divisa\n" +
                "left join\n" +
                "(select descripcion,SUBSTRING(codicons,1,1) digito, naturaleza_total,divisa,sum(convert(numeric(18), round(abs(saldo), 0))) VNH\n" +
                "from nexco_plano_ifrs9_neocon where periodo = ? AND naturaleza_total = 'H' and  round(abs(saldo), 0) <> 0  and tipo_registro IN ("+typeText+") \n" +
                "group by descripcion,SUBSTRING(codicons,1,1), naturaleza_total,divisa)b\n" +
                "ON z.digito = b.digito and z.divisa = b.divisa\n" +
                "GROUP BY Z.descripcion,Z.digito,Z.divisa,a.naturaleza_total,b.naturaleza_total");
        plain.setParameter(1,period);
        plain.setParameter(2,period);
        plain.setParameter(3,period);
        return plain.getResultList();
    }

    public List validateTableIntProv(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 origen FROM nexco_plano_ifrs9_intergrupo \n" +
                " WHERE periodo = ? and origen = 'INTERGRUPO_PROVIS.TXT';");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List validateTableIntRec(String period){
        Query queryValidate = entityManager.createNativeQuery("SELECT top 1 origen FROM nexco_plano_ifrs9_intergrupo \n" +
                "WHERE periodo = ? and origen = 'INTERGRUPO_RECLAS.TXT';");
        queryValidate.setParameter(1, period);

        return queryValidate.getResultList();
    }

    public List<PlainIFRS9Intergroup> getAllFromPlanos(String periodo) {
        Query result = entityManager.createNativeQuery("SELECT * FROM nexco_plano_ifrs9_intergrupo WHERE periodo = ?", PlainIFRS9Intergroup.class);
        result.setParameter(1, periodo);
        List<PlainIFRS9Intergroup> data = result.getResultList();

        return data;
    }

    public List<Object[]> getAllFromPlanosObject(String periodo) {
            Query result = entityManager.createNativeQuery("SELECT a.empresa,a.fecha_contable,a.cuenta,a.contrato,a.divisa,a.importe_c,a.importe_d,a.importe_cd,a.importe_dd,a.importe_cd_exp,a.importe_dd_exp,a.importe_total,a.observacion,a.origen,a.periodo,b.CODICONS46,ISNULL(c.yntp,' ') YNTP FROM nexco_plano_ifrs9_intergrupo_origin a\n" +
                    "LEFT JOIN (SELECT NUCTA,CODICONS46 FROM CUENTAS_PUC WHERE empresa= '0013') b ON a.cuenta = b.NUCTA\n" +
                    "LEFT JOIN (select contrato, yntp from /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def where periodo = ? group by contrato, yntp) c ON a.contrato=c.contrato\n" +
                    "WHERE a.periodo = ?");
        result.setParameter(1, periodo);
        result.setParameter(2, periodo);
        return  result.getResultList();
    }

    public List<Object[]> getAllFromPYG(String periodo) {

        String[] partsPeriodo = periodo.split("-");
        int anioNumber = Integer.parseInt(partsPeriodo[0]);
        anioNumber = anioNumber-1;

        Query resultF = entityManager.createNativeQuery("select W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,sum(W.total_saldo),W.divisa,W.pais_negocio,W.periodo,sum(W.valor) WV,sum(W.saldo) WS \n" +
                "FROM (select 'PYG' tipo_registro,'00548' Sociedad,'0' tipocons,C.tipo_asiento,C.tipo_proceso,'M' movimiento,C.referencia,'P' modo_ejecucion,'C388386' usuario,CASE WHEN (A.saldo-B.valor) < 0 THEN B.cuenta_h ELSE B.cuenta END cuentaC,CASE WHEN (A.saldo-B.valor) < 0 THEN 'H' ELSE 'D' END naturaleza,ISNULL(A.saldo-B.valor,0) total_saldo,A.divisa,'XX' pais_negocio,? periodo, B.valor,A.saldo \n" +
                "from (SELECT X.instrumento, X.stage_espana, Z.divisa, Z.tipo_registro,((sum(Z.saldo))*-1) saldo FROM nexco_plano_ifrs9_neocon Z\n" +
                "INNER JOIN nexco_provisiones X ON Z.codicons = X.cuenta_neocon\n" +
                "WHERE tipo_registro = 'PROV' and periodo = ? GROUP BY X.instrumento, X.stage_espana, Z.divisa, Z.tipo_registro) A\n" +
                "INNER JOIN (SELECT anio,tipo,stage,divisa,cuenta,cuenta_h,sum(valor) valor FROM nexco_parametria_pyg GROUP BY anio,tipo,stage,divisa,cuenta,cuenta_h) B ON A.instrumento = UPPER(B.tipo) AND A.stage_espana = B.stage AND A.divisa = B.divisa and anio = ? \n" +
                "LEFT JOIN nexco_tipo_plantilla_esp C ON C.descripcion = 'PYG'\n" +
                "GROUP BY CASE WHEN (A.saldo-B.valor) < 0 THEN B.cuenta_h ELSE B.cuenta END, C.tipo_proceso, A.divisa,CASE WHEN (A.saldo-B.valor) < 0 THEN 'H' ELSE 'D' END,C.referencia,ISNULL(A.saldo-B.valor,0),C.tipo_asiento,B.valor,A.saldo)W\n" +
                "GROUP BY W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.divisa,W.pais_negocio,W.periodo ");

        resultF.setParameter(1,periodo);
        resultF.setParameter(2,periodo);
        resultF.setParameter(3,String.valueOf(anioNumber));
        return resultF.getResultList();
    }

    public List<Object[]> getAllFromPYGFuente(String periodo) {

        Query resultF = entityManager.createNativeQuery("SELECT Z.codicons, Z.divisa, ISNULL(X.instrumento,'Sin Parametrizar') INS, ISNULL(X.stage_espana,'Sin Parametrizar') STA,((sum(Z.saldo))*-1) saldo FROM nexco_plano_ifrs9_neocon Z\n" +
                "left JOIN nexco_provisiones X ON Z.codicons = X.cuenta_neocon\n" +
                "WHERE tipo_registro = 'PROV' and periodo = ? GROUP BY Z.codicons, Z.divisa, X.instrumento, X.stage_espana");

        resultF.setParameter(1,periodo);
        return resultF.getResultList();
    }

    public List<Object[]> getAllFromImpuestos(String periodo) {

        String[] partsPeriodo = periodo.split("-");
        int anioNumber = Integer.parseInt(partsPeriodo[0]);
        anioNumber = anioNumber-1;

        Query resultF = entityManager.createNativeQuery("select W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.total_saldo,W.divisa,W.pais_negocio,W.periodo,sum(W.Saldo_Param)SP, sum(W.Saldo_Fuente) SF \n" +
                "FROM (\n" +
                "SELECT 'IMPUE' tipo_registro,\n" +
                "'00548' Sociedad,\n" +
                "'0' tipocons,\n" +
                "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_asiento, \n" +
                "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_proceso,\n" +
                "'M' movimiento,\n" +
                "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') referencia,\n" +
                "'P' modo_ejecucion,\n" +
                "'C388386' usuario,\n" +
                "(SELECT top 1 B.cuenta FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) cuentaC,\n" +
                "CASE WHEN (A.saldo*-1 * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100< 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                "(A.saldo*-1 * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100 total_saldo,\n" +
                "(SELECT top 1 B.divisa FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) divisa,\n" +
                "'XX' pais_negocio,\n" +
                ":period periodo,\n" +
                "(SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) Saldo_Param,\n" +
                "A.saldo*-1 Saldo_Fuente \n" +
                "FROM (SELECT sum(Z.saldo) saldo FROM nexco_plano_ifrs9_neocon Z WHERE Z.tipo_registro = 'PYG' AND Z.periodo = :period) A\n" +
                "UNION ALL\n" +
                "SELECT 'IMPUE' tipo_registro,\n" +
                "'00548' Sociedad,\n" +
                "'0' tipocons,\n" +
                "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_asiento, \n" +
                "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_proceso,\n" +
                "'M' movimiento,\n" +
                "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') referencia,\n" +
                "'P' modo_ejecucion,\n" +
                "'C388386' usuario,\n" +
                "(SELECT top 1 B.cuenta_h FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) cuentaC,\n" +
                "CASE WHEN (A.saldo * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100< 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                "(A.saldo * (SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio))/100 total_saldo,\n" +
                "(SELECT top 1 B.divisa FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) divisa,\n" +
                "'XX' pais_negocio,\n" +
                ":period periodo,\n" +
                "(SELECT top 1 B.stage FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) Saldo_Param,\n" +
                "A.saldo Saldo_Fuente \n" +
                "FROM (SELECT sum(Z.saldo) saldo FROM nexco_plano_ifrs9_neocon Z WHERE Z.tipo_registro = 'PYG' AND Z.periodo = :period) A\n" +
                "UNION ALL\n" +
                "SELECT 'IMPUEP' tipo_registro,\n" +
                "'00548' Sociedad,\n" +
                "'0' tipocons,\n" +
                "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_asiento, \n" +
                "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') tipo_proceso,\n" +
                "'M' movimiento,\n" +
                "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PROV'),'S/R') referencia,\n" +
                "'P' modo_ejecucion,\n" +
                "'C388386' usuario,\n" +
                "(SELECT top 1 B.cuenta FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) cuentaC,\n" +
                "CASE WHEN (SELECT top 1 B.valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio)< 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                "(SELECT top 1 B.valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) total_saldo,\n" +
                "(SELECT top 1 B.divisa FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) divisa,\n" +
                "'XX' pais_negocio,\n" +
                ":period periodo,\n" +
                "(SELECT top 1 B.valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'IMPUESTO' and B.anio = :anio) Saldo_Param,\n" +
                "0 Saldo_Fuente \n" +
                "FROM (SELECT sum(Z.saldo) saldo FROM nexco_plano_ifrs9_neocon Z WHERE Z.tipo_registro = 'PYG' AND Z.periodo=:period) A\n" +
                ") W\n" +
                "GROUP BY W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.total_saldo,W.divisa,W.pais_negocio,W.periodo ");
        resultF.setParameter("period",periodo);
        resultF.setParameter("anio",anioNumber);
        return resultF.getResultList();
    }

    public List<Object[]> getAllFromRepos(String periodo) {

        String[] partsPeriodo = periodo.split("-");
        int anioNumber = Integer.parseInt(partsPeriodo[0]);
        anioNumber = anioNumber-1;

        Query resultF = entityManager.createNativeQuery("select W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,sum(W.total_saldo),W.divisa,W.pais_negocio,W.periodo,sum(W.Saldo_Param) SP,sum(W.Saldo_Fuente) SF\n" +
                "FROM (\n" +
                "select 'REPOS' tipo_registro," +
                "'00548' Sociedad," +
                "'0' tipocons," +
                "C.tipo_asiento," +
                "C.tipo_proceso," +
                "'M' movimiento," +
                "C.referencia," +
                "'P' modo_ejecucion," +
                "'C388386' usuario," +
                " A.cta_neocon cuentaC," +
                "CASE WHEN (A.AJ/1000) < 0 THEN 'H' ELSE 'D' END naturaleza," +
                "ISNULL(A.AJ/1000,0) total_saldo," +
                "H.divisa_neocon divisa," +
                "'XX' pais_negocio," +
                "? periodo, \n" +
                "0 Saldo_Param,\n" +
                "A.AJ/1000 Saldo_Fuente \n" +
                "from (select T.cta_neocon,T.dvsa_cntble_1_conciliacion, SUM(T.ajuste)AJ \n" +
                "FROM nexco_rys_conciliacion T WHERE T.periodo = ?\n" +
                "GROUP BY T.cta_neocon,T.dvsa_cntble_1_conciliacion) A\n" +
                "LEFT JOIN nexco_tipo_plantilla_esp C ON C.descripcion = 'REPOS'\n" +
                "LEFT JOIN nexco_divisas H ON A.dvsa_cntble_1_conciliacion = H.id_divisa\n" +
                "GROUP BY A.cta_neocon, C.tipo_proceso, H.divisa_neocon,CASE WHEN (A.AJ/1000) < 0 THEN 'H' ELSE 'D' END,C.referencia,ISNULL(A.AJ/1000,0),C.tipo_asiento,A.AJ/1000 \n" +
                "UNION ALL\n" +//PYG REPOS
                "SELECT 'PYG REPOS' tipo_registro,\n" +
                "'00548' Sociedad,\n" +
                "'0' tipocons,\n" +
                "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_asiento, \n" +
                "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_proceso,\n" +
                "'M' movimiento,\n" +
                "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') referencia,\n" +
                "'P' modo_ejecucion,\n" +
                "'C388386' usuario,\n" +
                "CASE WHEN Z.ajuste-X.valor < 0 THEN X.cuenta_h ELSE X.cuenta END cuentaC,\n" +
                "CASE WHEN Z.ajuste-X.valor < 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                "SUM(Z.ajuste-X.valor) total_saldo,\n" +
                "X.divisa,\n" +
                "'XX' pais_negocio,\n" +
                "? periodo, " +
                "X.valor Saldo_Param,\n" +
                "Z.ajuste Saldo_Fuente\n" +
                "FROM (SELECT A.cta_neocon,A.tipo_entidad,SUM(A.ajuste_pyg)/1000 ajuste FROM nexco_rys_conciliacion A WHERE A.periodo = ? GROUP BY A.cta_neocon,A.tipo_entidad) Z\n" +
                "INNER JOIN (SELECT B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h,sum(B.valor) valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'REPOS' and B.anio = ? GROUP BY B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h) X ON UPPER(Z.tipo_entidad) = UPPER(X.tipo) AND substring(Z.cta_neocon,1,1) = X.stage\n" +
                "GROUP BY\n" +
                "CASE WHEN Z.ajuste-X.valor < 0 THEN X.cuenta_h ELSE X.cuenta END ,\n" +
                "CASE WHEN Z.ajuste-X.valor < 0 THEN 'H' ELSE 'D' END ,\n" +
                "X.divisa,X.valor,Z.ajuste \n" +
                "UNION ALL\n" +
                "SELECT 'PYG REPOS' tipo_registro,\n" +
                "'00548' Sociedad,\n" +
                "'0' tipocons,\n" +
                "ISNULL((SELECT top 1 tipo_asiento FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_asiento, \n" +
                "ISNULL((SELECT top 1 tipo_proceso FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') tipo_proceso,\n" +
                "'M' movimiento,\n" +
                "ISNULL((SELECT top 1 referencia FROM nexco_tipo_plantilla_esp WHERE descripcion = 'PYG REPOS'),'S/R') referencia,\n" +
                "'P' modo_ejecucion,\n" +
                "'C388386' usuario,\n" +
                "CASE WHEN 0-Z.valor < 0 THEN Z.cuenta_h ELSE Z.cuenta END cuentaC,\n" +
                "CASE WHEN 0-Z.valor < 0 THEN 'H' ELSE 'D' END naturaleza,\n" +
                "SUM(0-Z.valor) total_saldo,\n" +
                "Z.divisa,\n" +
                "'XX' pais_negocio,\n" +
                "? periodo,\n" +
                "Z.valor Saldo_Param,\n" +
                "0 Saldo_Fuente\n" +
                "FROM nexco_parametria_pyg Z WHERE Z.descripcion = 'REPOS' and Z.anio = ? and Z.tipo NOT IN (\n" +
                "select tipo_entidad\n" +
                "FROM (SELECT A.cta_neocon,A.tipo_entidad,SUM(A.ajuste_pyg)/1000 ajuste FROM nexco_rys_conciliacion A WHERE A.periodo = ? GROUP BY A.cta_neocon,A.tipo_entidad) Z\n" +
                "INNER JOIN (SELECT B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h,sum(B.valor) valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'REPOS' and B.anio = ? GROUP BY B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h) X ON UPPER(Z.tipo_entidad) = UPPER(X.tipo) AND substring(Z.cta_neocon,1,1) = X.stage\n" +
                "GROUP BY tipo_entidad)\n" +
                "GROUP BY\n" +
                "CASE WHEN 0-Z.valor < 0 THEN Z.cuenta_h ELSE Z.cuenta END ,\n" +
                "CASE WHEN 0-Z.valor < 0 THEN 'H' ELSE 'D' END ,\n" +
                "Z.divisa,Z.valor)W\n" +
                "GROUP BY W.tipo_registro,W.Sociedad,W.tipocons,W.tipo_asiento,W.tipo_proceso,W.movimiento,W.referencia,W.modo_ejecucion,W.usuario,W.cuentaC,W.naturaleza,W.divisa,W.pais_negocio,W.periodo \n" +
                "\n");

        resultF.setParameter(1,periodo);
        resultF.setParameter(2,periodo);
        resultF.setParameter(3,periodo);
        resultF.setParameter(4,periodo);
        resultF.setParameter(5,String.valueOf(anioNumber));
        resultF.setParameter(6,periodo);
        resultF.setParameter(7,String.valueOf(anioNumber));
        resultF.setParameter(8,periodo);
        resultF.setParameter(9,String.valueOf(anioNumber));
        return resultF.getResultList();
    }

    public List<String> getAllFromResposType(String periodo) {

        String[] partsPeriodo = periodo.split("-");
        int anioNumber = Integer.parseInt(partsPeriodo[0]);
        anioNumber = anioNumber - 1;

        Query resultF = entityManager.createNativeQuery("SELECT Z.tipo_entidad\n" +
                "FROM (SELECT A.cta_neocon,A.tipo_entidad,SUM(A.ajuste_pyg)/1000 ajuste FROM nexco_rys_conciliacion A WHERE A.periodo = ? GROUP BY A.cta_neocon,A.tipo_entidad) Z\n" +
                "LEFT JOIN (SELECT B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h,sum(B.valor) valor FROM nexco_parametria_pyg B WHERE B.descripcion = 'REPOS' and B.anio = ? GROUP BY B.anio,B.tipo,B.stage,B.divisa,B.cuenta,B.cuenta_h) X ON UPPER(Z.tipo_entidad) = UPPER(X.tipo) AND substring(Z.cta_neocon,1,1) = X.stage\n" +
                "WHERE Z.tipo_entidad NOT IN (SELECT tipo FROM nexco_parametria_pyg WHERE descripcion = 'REPOS' and anio = ? GROUP BY tipo)\n" +
                "GROUP BY Z.tipo_entidad");
        resultF.setParameter(1, periodo);
        resultF.setParameter(2, String.valueOf(anioNumber));
        resultF.setParameter(3, String.valueOf(anioNumber));
        return resultF.getResultList();
    }
}
