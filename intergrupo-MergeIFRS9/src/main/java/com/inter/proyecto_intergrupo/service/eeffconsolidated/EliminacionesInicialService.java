package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialCuadreGeneral;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.EliminacionesVersionInicialDetalle;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.EliminacionesVersionIncialDetalleRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.EliminacionesVersionIncialRepository;
import javax.persistence.*;
import java.util.List;

import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.Date;
@Service
@Transactional
public class EliminacionesInicialService {

    @Autowired
    private EliminacionesVersionIncialRepository eliminacionesVersionIncialRepository;

    @Autowired
    private EliminacionesVersionIncialDetalleRepository eliminacionesVersionIncialDetalleRepository;

    @Autowired
    private statusInfoRepository statusInfoRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public EliminacionesInicialService(EliminacionesVersionIncialRepository eliminacionesVersionIncialRepository) {
        this.eliminacionesVersionIncialRepository = eliminacionesVersionIncialRepository;
    }

    public List<EliminacionesVersionInicialCuadreGeneral> getCuadreGeneral(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_eliminaciones_version_inicial_cuadre_general where periodo = ?;", EliminacionesVersionInicialCuadreGeneral.class);
        consulta.setParameter(1, periodo);
        return consulta.getResultList();
    }
    public Page getAllCuadre(Pageable pageable, String periodo, List<EliminacionesVersionInicialCuadreGeneral> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<EliminacionesVersionInicialCuadreGeneral> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }
    /*******************************Separador********************/

    public List<EliminacionesVersionInicialDetalle> getCuadreDetalle(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_eliminaciones_version_inicial_detalle where periodo = ?;", EliminacionesVersionInicialDetalle.class);
        consulta.setParameter(1, periodo);
        return consulta.getResultList();
    }
    public Page getAllDetalle(Pageable pageable, String periodo, List<EliminacionesVersionInicialDetalle> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<EliminacionesVersionInicialDetalle> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    public void loadAudit(User user, String mensaje) {
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("EEFF Consolidado");
        insert.setFecha(today);
        insert.setInput("Filiales");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }



    public void obtenerDiferenciasPorConcepto(String periodo) {

        Query resultFinal = entityManager.createNativeQuery(
                "drop TABLE nexco_eliminaciones_version_inicial_temporal;\n" +
                        "select a.yntp_empresa, a.concepto, a.cuenta_local, a.cuenta_filial, a.contrato_banco, a.valor_banco valor_banco,\n" +
                        "a.valor_filial valor_filial, abs(a.valor_banco)-abs(a.valor_filial) diferencia, a.tipo_registro, a.id, a.justificacion\n" +
                        ", b.CODICONS46 cod_neocon_banco, c.cod_neocon cod_neocon_filial\n" +
                        "into nexco_eliminaciones_version_inicial_temporal from nexco_intergrupo_recon_just a \n" +
                        "left join (select nucta, CODICONS46 from CUENTAS_PUC where empresa = '0013') b\n" +
                        "on a.cuenta_local = b.NUCTA\n" +
                        "left join (select distinct cod_neocon, cuenta_local, yntp_reportante from nexco_filiales_intergrupo where periodo = :periodo) c\n" +
                        "on a.cuenta_filial = c.cuenta_local and a.yntp_empresa = c.yntp_reportante\n" +
                        "where periodo =:periodo AND (a.yntp_empresa = '00560' OR a.yntp_empresa = '00561') and (valor_banco <> 0 or valor_filial <> 0) and a.tipo_registro <> 'TOTAL' \n" +
                        "order by 1, 2, 5, 3, 4 ; ");

        resultFinal.setParameter("periodo", periodo);
        resultFinal.executeUpdate();


        Query Update = entityManager.createNativeQuery(
                "delete from nexco_eliminaciones_version_inicial_cuadre_general where periodo = :periodo ; " +
                        "insert into nexco_eliminaciones_version_inicial_cuadre_general (nombre,concepto,plantilla_banco,plantilla_filial,ajuste,total_general,periodo)\n" +
                        "(SELECT case when yntp_empresa = '00560' then 'BBVA VALORES' when yntp_empresa = '00561' then 'BBVA FIDUCIARIA' end as empresa, concepto, sum(valor_banco) as PLANTILLA_BANCO, sum(valor_filial) as PLANTILLA_FILIAL, sum(diferencia) AS AJUSTE, sum(diferencia) + (sum(diferencia)*-1) AS TOTAL_GENERAL , :periodo\n" +
                        "FROM nexco_eliminaciones_version_inicial_temporal group by case when yntp_empresa = '00560' then 'BBVA VALORES' when yntp_empresa = '00561' then 'BBVA FIDUCIARIA' end, concepto) ");
        Update.setParameter("periodo", periodo);
        Update.executeUpdate();

        Query Update1 = entityManager.createNativeQuery(
                "delete from nexco_eliminaciones_version_inicial_detalle where periodo = :periodo ; " +
                        "INSERT INTO nexco_eliminaciones_version_inicial_detalle (id, nombre, concepto, NAT, yntp, cuenta_local, periodo, valor, abs, l)\n" +
                        "SELECT CASE WHEN (CASE WHEN cuenta_local IS NOT NULL and cuenta_local <> '' THEN 1 ELSE 0 END) = 1 THEN 'PLANTILLA BANCO' WHEN (CASE WHEN cuenta_filial IS NOT NULL and cuenta_filial <> '' THEN 1 ELSE 0 END) = 1 THEN 'PLANTILLA FILIAL' ELSE NULL END AS ID,\n" +
                        "CASE WHEN yntp_empresa = '00560' THEN 'BBVA VALORES' WHEN yntp_empresa = '00561' THEN 'BBVA FIDUCIARIA' END AS empresa, concepto, \n" +
                        "CASE WHEN (valor_banco) < 0 THEN 'C' WHEN (valor_banco) > 0 THEN 'D' WHEN (valor_filial) < 0 THEN 'C' WHEN (valor_filial) > 0 THEN 'D' ELSE 'D' END AS NAT,\n" +
                        "yntp_empresa as Yntp, case when cuenta_local = '' then cuenta_filial else cuenta_local end as cuenta_local,:periodo as periodo,\n" +
                        "CASE WHEN valor_banco = 0 then valor_filial else valor_banco end as valor, CASE WHEN valor_banco = 0 then ABS(valor_filial) else ABS(valor_banco) end as Valor_ABS,\n" +
                        "case when cuenta_local = '' then SUBSTRING(cuenta_filial,1,1) else SUBSTRING(cuenta_local,1,1) end as l\n" +
                        "FROM nexco_eliminaciones_version_inicial_temporal");
        Update1.setParameter("periodo", periodo);
        Update1.executeUpdate();
    }

    public void confirmarInfo(String periodo) {
        Date fechaActual = new Date();
        String input = "ELIMINACIONES CARGUE CONFIRMADO";
        String status = "CONFIRMADO";

        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setFecha(fechaActual);
        statusInfo.setInput(input);
        statusInfo.setPeriodo(periodo);
        statusInfo.setStatus(status);

        statusInfoRepository.save(statusInfo);
    }

    public void confirmarInfoLuzMa(String periodo) {
        Date fechaActual = new Date();
        String input = "ELIMINACIONES AUTORIZACION";
        String status = "CONFIRMADO";

        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setFecha(fechaActual);
        statusInfo.setInput(input);
        statusInfo.setPeriodo(periodo);
        statusInfo.setStatus(status);

        statusInfoRepository.save(statusInfo);
    }

    public void ProcesarEliminaciones(String periodo) {

        Query resultFinal = entityManager.createNativeQuery(
              "update a set a.debe = 0, a.haber = 0 from (select * from nexco_concil_filiales WHERE PERIODO = :periodo ) a; \n" +
                "update a set a.debe = case when b.valor < 0 THEN b.valor*-1 ELSE 0 end, a.haber = case when b.valor > 0 THEN b.valor*-1 ELSE 0 end from (select * from nexco_concil_filiales WHERE PERIODO = :periodo) a, (select cuenta_local, sum(valor) as valor from nexco_eliminaciones_version_ajustada_detalle WHERE PERIODO = :periodo group by cuenta_local) b where a.cuenta = b.cuenta_local;");
        resultFinal.setParameter("periodo", periodo);
        resultFinal.executeUpdate();

        Query UpdatesEliminaciones = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones.setParameter("periodo", periodo);
        UpdatesEliminaciones.executeUpdate();

        Query UpdatesEliminaciones1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones1.setParameter("periodo", periodo);
        UpdatesEliminaciones1.executeUpdate();
    }

    public void confirmarInfoPatrimoniales(String periodo) {
        Date fechaActual = new Date();
        String input = "ELIMINACIONES PATRIMONIALES CONFIRMADO";
        String status = "CONFIRMADO";

        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setFecha(fechaActual);
        statusInfo.setInput(input);
        statusInfo.setPeriodo(periodo);
        statusInfo.setStatus(status);

        statusInfoRepository.save(statusInfo);
    }

    public void autorizarInfoPatrimoniales(String periodo) {
        Date fechaActual = new Date();
        String input = "AUTORIZACIÓN ELIMINACIONES PATRIMONIALES";
        String status = "CONFIRMADO";

        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setFecha(fechaActual);
        statusInfo.setInput(input);
        statusInfo.setPeriodo(periodo);
        statusInfo.setStatus(status);

        statusInfoRepository.save(statusInfo);
    }
}





