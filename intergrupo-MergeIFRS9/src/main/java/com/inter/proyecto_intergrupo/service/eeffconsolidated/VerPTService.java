package com.inter.proyecto_intergrupo.service.eeffconsolidated;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.*;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.VerPT1PatrimonioTecnicoRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.VerPTValorRiesgoRepository;
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
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VerPTService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private statusInfoRepository statusInfoRepository;

    @Autowired
    private VerPTValorRiesgoRepository verPTValorRiesgoRepository;

    @Autowired
    private VerPT1PatrimonioTecnicoRepository verPT1PatrimonioTecnicoRepository;

    /***************************************** TABLA VERIFICACION EN RIESGO ********************************************************/
    public List<tablaVerPTunificada> getDataVerPT(String periodo, String fuente) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_ver_pt_tabla_unificada where periodo = ? and fuente = ?;", tablaVerPTunificada.class);
        consulta.setParameter(1, periodo);
        consulta.setParameter(2, fuente);
        return consulta.getResultList();
    }

    public Page getAllVerPT(Pageable pageable, String periodo,List<tablaVerPTunificada> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<tablaVerPTunificada> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    /***************************************** VALOR EN RIESGO TOTAL ********************************************************/
    public List<VerPT> getDataValorRiesgo(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_valor_riesgo_total_verpt where periodo = ? ;", VerPT.class);
        consulta.setParameter(1, periodo);
        return consulta.getResultList();
    }

    public Page getAllValorRiesgo(Pageable pageable, String periodo, List<VerPT> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<VerPT> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }

    /***************************************** PATRIMONIO TECNICO ********************************************************/
    public List<VerPT1> getDataPatrimonioTecnico(String periodo) {
        Query consulta = entityManager.createNativeQuery("select * from nexco_valor_patrimonio_tecnico_verpt where periodo = ? ;", VerPT1.class);
        consulta.setParameter(1, periodo);
        return consulta.getResultList();
    }

    public Page getAllPatrimonioTecnico(Pageable pageable, String periodo, List<VerPT1> list) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<VerPT1> pageAval = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageAval;
    }


    public void confirmarValorEnRiesgo(String periodo) {
        Date fechaActual = new Date();
        String input = "VALOR EN RIESGO TOTAL";
        String status = "CONFIRMADO";

        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setFecha(fechaActual);
        statusInfo.setInput(input);
        statusInfo.setPeriodo(periodo);
        statusInfo.setStatus(status);
        statusInfoRepository.save(statusInfo);
    }

    public void confirmarPatrimonioTecnico(String periodo) {
        Date fechaActual = new Date();
        String input = "PATRIMONIO TECNICO";
        String status = "CONFIRMADO";

        StatusInfo statusInfo = new StatusInfo();
        statusInfo.setFecha(fechaActual);
        statusInfo.setInput(input);
        statusInfo.setPeriodo(periodo);
        statusInfo.setStatus(status);
        statusInfoRepository.save(statusInfo);
    }

    public void ProcesarVerPT(String periodo) {

        /***********************************VER PT********************************************************************/


        Query consultaDelete = entityManager.createNativeQuery("DELETE FROM nexco_status_info\n" +
                "WHERE input IN ('PATRIMONIO TECNICO', 'VALOR EN RIESGO TOTAL') \n" +
                "AND periodo = :periodo ");
        consultaDelete.setParameter("periodo", periodo);
        consultaDelete.executeUpdate();



        Query insert1 = entityManager.createNativeQuery("delete nexco_ver_pt_tabla_unificada where fuente = 'VER - PT' AND periodo = :periodo ");
        insert1.setParameter("periodo", periodo);
        insert1.executeUpdate();

        Query insert = entityManager.createNativeQuery("insert into nexco_ver_pt_tabla_unificada(concepto,cuenta,saldo_local,requerido,ajuste,fuente,periodo,moneda)\n" +
                "SELECT 'Renglon 645 Formato 110' AS Concepto, z.cuenta AS Cuenta,  SUM(ISNULL(b.banco,0)) AS saldo_local, SUM(ISNULL(c.valor_riesgo_total,0)) AS requerido, SUM(ISNULL(c.valor_riesgo_total,0)) - SUM(ISNULL(b.banco,0)) AS Ajuste,'VER - PT' AS fuente,:periodo  AS periodo, c.moneda\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'VER - PT' AND concepto = 'Renglon 645 Formato 110') z \n" +
                "LEFT JOIN (SELECT * FROM nexco_concil_filiales WHERE empresa = 'Banco' AND periodo =:periodo ) b ON b.cuenta = z.cuenta \n" +
                "LEFT JOIN (SELECT * FROM nexco_valor_riesgo_total_verpt WHERE periodo =:periodo ) c ON z.cuenta = z.cuenta GROUP BY z.cuenta,c.moneda\n" +
                "UNION ALL\n" +
                "SELECT 'Deudoras Por Contra' AS Concepto, z.cuenta AS Cuenta,  SUM(ISNULL(b.banco,0)) * -1 AS saldo_local, SUM(ISNULL(c.valor_riesgo_total,0)) * -1 AS requerido, (SUM(ISNULL(c.valor_riesgo_total,0))*-1) - (SUM(ISNULL(b.banco,0))*-1) AS Ajuste,'VER - PT' AS fuente,:periodo  AS periodo,c.moneda\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'VER - PT' AND concepto = 'Deudoras Por Contra') z \n" +
                "LEFT JOIN (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'VER - PT' AND concepto = 'Renglon 645 Formato 110') d on d.parametro = z.parametro\n" +
                "LEFT JOIN (SELECT * FROM nexco_concil_filiales WHERE empresa = 'Banco' AND periodo =:periodo ) b ON b.cuenta = d.cuenta \n" +
                "LEFT JOIN (SELECT * FROM nexco_valor_riesgo_total_verpt WHERE periodo =:periodo ) c ON z.cuenta = z.cuenta GROUP BY z.cuenta,c.moneda\n" +
                "UNION ALL\n" +
                "SELECT 'Patrimonio Técnico Back Office' AS Concepto, z.cuenta AS Cuenta,  SUM(ISNULL(b.banco,0)) AS saldo_local, SUM(ISNULL(c.valor_patrimonio_tecnico,0))*-1 AS requerido, (SUM(ISNULL(c.valor_patrimonio_tecnico,0))*-1) - SUM(ISNULL(b.banco,0)) AS Ajuste,'VER - PT' AS fuente,:periodo  AS periodo,c.moneda\n" +
                "FROM (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'VER - PT' AND concepto = 'Patrimonio Tecnico Back Office') z \n" +
                "LEFT JOIN (SELECT * FROM nexco_concil_filiales WHERE empresa = 'Banco' AND periodo =:periodo ) b ON b.cuenta = z.cuenta \n" +
                "LEFT JOIN (SELECT * FROM nexco_valor_patrimonio_tecnico_verpt WHERE periodo =:periodo ) c ON z.cuenta = z.cuenta GROUP BY z.cuenta,c.moneda\n" +
                "UNION ALL\n" +
                "select 'Acreedoras Por Contra (DB) - BG1' as Concepto, z.cuenta as Cuenta,  SUM(ISNULL(b.banco,0))*-1 as saldo_local, SUM(ISNULL(c.valor_patrimonio_tecnico,0)) AS requerido, (SUM(ISNULL(c.valor_patrimonio_tecnico,0))) - (SUM(ISNULL(b.banco,0))*-1) AS Ajuste,'VER - PT' as fuente,:periodo  as periodo,c.moneda\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'VER - PT' and concepto = 'Acreedoras Por Contra (DB) - BG1') z \n" +
                "LEFT JOIN (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'VER - PT' AND concepto = 'Patrimonio Tecnico Back Office') d on d.parametro = z.parametro\n" +
                "LEFT JOIN (select * from nexco_concil_filiales where empresa = 'Banco' and periodo =:periodo ) b on b.cuenta = d.cuenta \n" +
                "LEFT JOIN (SELECT * FROM nexco_valor_patrimonio_tecnico_verpt WHERE periodo =:periodo ) c ON z.cuenta = z.cuenta GROUP BY z.cuenta,c.moneda\n" +
                "UNION ALL\n" +
                "select 'Patrimonio Técnico Mensual' as Concepto, z.cuenta as Cuenta, SUM(ISNULL(valores,0)) as saldo_local, 0.00 as requerido , (0.00) - SUM(b.valores) AS Ajuste,'VER - PT' as fuente, :periodo  as periodo,c.moneda\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'VER - PT' and concepto = 'Patrimonio Técnico Mensual') z \n" +
                "LEFT JOIN (select * from nexco_concil_filiales where empresa = 'valores' and periodo =:periodo ) b on b.cuenta = z.cuenta \n" +
                "LEFT JOIN (SELECT * FROM nexco_valor_patrimonio_tecnico_verpt WHERE periodo =:periodo ) c ON z.cuenta = z.cuenta GROUP BY z.cuenta,c.moneda\n" +
                "UNION ALL\n" +
                "select 'Deudoras de control por contra (cr)' as Concepto, z.cuenta as Cuenta,  SUM(valores)*-1 as saldo_local, 0.00 as requerido, (0.00) - (SUM(b.valores)*-1) AS Ajuste, 'VER - PT' as fuente,:periodo  as periodo,c.moneda\n" +
                "from (select * from nexco_parametria_eeff where parametro = 'VER - PT' and concepto = 'Deudoras de control por contra (cr)') z \n" +
                "LEFT JOIN (SELECT * FROM nexco_parametria_eeff WHERE parametro = 'VER - PT' AND concepto = 'Patrimonio Técnico Mensual') d on d.parametro = z.parametro\n" +
                "LEFT JOIN (select * from nexco_concil_filiales where empresa = 'valores' and periodo =:periodo ) b on b.cuenta = d.cuenta \n" +
                "LEFT JOIN (SELECT * FROM nexco_valor_patrimonio_tecnico_verpt WHERE periodo =:periodo ) c ON z.cuenta = z.cuenta GROUP BY z.cuenta,c.moneda; ");
        insert.setParameter("periodo", periodo);
        insert.executeUpdate();

        Query insert2 = entityManager.createNativeQuery("UPDATE a set a.debe_ver_pt = 0, a.haber_ver_pt = 0 from (select * from nexco_concil_filiales where periodo = :periodo) a; \n" +
                "UPDATE a set a.debe_ver_pt = case when b.ajuste < 0 then 0 else b.ajuste end, a.haber_ver_pt = case when b.ajuste > 0 then 0 else b.ajuste end\n" +
                "from (select * from nexco_concil_filiales where periodo = :periodo) a, (select * from nexco_ver_pt_tabla_unificada where periodo = :periodo) b\n" +
                "where a.cuenta= b.cuenta and a.moneda = b.moneda; ");
        insert2.setParameter("periodo", periodo);
        insert2.executeUpdate();

        Query UpdatesEliminaciones = entityManager.createNativeQuery("UPDATE a set haber_total = ISNULL(haber,0) + ISNULL(haber_patrimonio,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(haber_ver_pt,0) ,debe_total = ISNULL(debe,0) + ISNULL(debe_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) , eliminacion = ISNULL(debe,0) + ISNULL(haber,0) + ISNULL(debe_patrimonio,0) + ISNULL(haber_patrimonio,0) + ISNULL(debe_ajustes_minimos,0) + ISNULL(haber_ajustes_minimos,0) + ISNULL(debe_ver_pt,0) + ISNULL(haber_ver_pt,0) + ISNULL(total,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones.setParameter("periodo", periodo);
        UpdatesEliminaciones.executeUpdate();

        Query UpdatesEliminaciones1 = entityManager.createNativeQuery("UPDATE a set total_ifrs = ISNULL(eliminacion,0) + ISNULL(debe_ajustes_mayores,0) + ISNULL(haber_ajustes_mayores,0) from (select * from nexco_concil_filiales where periodo = :periodo) a\n");
        UpdatesEliminaciones1.setParameter("periodo", periodo);
        UpdatesEliminaciones1.executeUpdate();

    }
//********************************************VALOR RIESGO TOTAL********************************///
    public VerPT findByIdDato(Long id){
        return verPTValorRiesgoRepository.findByIdDato(id);
    }

    public VerPT modifyAccountValorEnRiesgo(VerPT toModify, User user) {
        return verPTValorRiesgoRepository.save(toModify);
    }

    public VerPT saveValorEnRiesgo(VerPT toSave, User user){
        return verPTValorRiesgoRepository.save(toSave);
    }

    //********************************************PATRIMONIO TECNICO********************************///

    public VerPT1 findByIdDato1(Long id){
        return verPT1PatrimonioTecnicoRepository.findByIdDato1(id);
    }

    public VerPT1 modifyAccountPatrimonioTecnico(VerPT1 toModify, User user) {
        return verPT1PatrimonioTecnicoRepository.save(toModify);
    }

    public VerPT1 savePatrimonioTecnico(VerPT1 toSave, User user){
        return verPT1PatrimonioTecnicoRepository.save(toSave);
    }

}

