package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.GarantiasActivasIcrf;
import com.inter.proyecto_intergrupo.model.briefcase.ReposIcrf;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.GarantiasActivasIcrfRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.ReposIcrfRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class VentaEnCortoIcrfService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private GarantiasActivasIcrfRepository garantiasActivasIcrfRepository;

    @Autowired
    private ReposIcrfRepository reposIcrfRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Venta En Corto ICRF");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }
    public String generateDatecalendar(String periodo)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate firstDayOfMonth1 = firstDayOfMonth.minusMonths(1);
        LocalDate lastDayOfMonth = firstDayOfMonth1.withDayOfMonth(firstDayOfMonth1.lengthOfMonth());
        return lastDayOfMonth.format(formatter);
    }

    public void insertFileGarantiasActivas(String periodo)
    {
        LocalDate fecha = LocalDate.parse(generateDatecalendar(periodo));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy").withLocale(java.util.Locale.ENGLISH);
        String fechaFormateada = fecha.format(formatter);

        Query queryCreateTemp = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_garantias_activas_icrf_bulk;" +
                "CREATE TABLE [dbo].[nexco_garantias_activas_icrf_bulk](\n" +
                "\t[cod_emp] [varchar](255) NULL,\n" +
                "\t[portafolio] [varchar](255) NULL,\n" +
                "\t[negocio] [varchar](255) NULL,\n" +
                "\t[fecha] [varchar](max) ,\n" +
                "\t[fecha_final] [varchar](max) ,\n" +
                "\t[estado] [varchar](255) NULL,\n" +
                "\t[operacion] [varchar](255) NULL,\n" +
                "\t[nro_papeleta] [varchar](255) NULL,\n" +
                "\t[seguimiento] [varchar](255) NULL,\n" +
                "\t[acceso] [varchar](255) NULL,\n" +
                "\t[origen] [varchar](255) NULL,\n" +
                "\t[moneda] [varchar](255) NULL,\n" +
                "\t[emisor] [varchar](255) NULL,\n" +
                "\t[tipo] [varchar](255) NULL,\n" +
                "\t[nro_titulo] [varchar](255) NULL,\n" +
                "\t[valor_nominal] [varchar](255) NULL,\n" +
                "\t[tasa_nominal] [varchar](255) NULL,\n" +
                "\t[pago_interes] [varchar](255) NULL,\n" +
                "\t[modalidad] [varchar](255) NULL,\n" +
                "\t[tipo_interes] [varchar](255) NULL,\n" +
                "\t[pago_capital] [varchar](255) NULL,\n" +
                "\t[pago_dia] [varchar](255) NULL,\n" +
                "\t[fecha_emision] [varchar](255) NULL,\n" +
                "\t[fecha_vcto] [varchar](255) NULL,\n" +
                "\t[fecha_compra] [varchar](255) NULL,\n" +
                "\t[fecha_val] [varchar](255) NULL,\n" +
                "\t[sw_val_tir] [varchar](255) NULL,\n" +
                "\t[sw_360_comp] [varchar](255) NULL,\n" +
                "\t[sw_360_vlr] [varchar](255) NULL,\n" +
                "\t[sw_met_lin] [varchar](255) NULL,\n" +
                "\t[sw_met_mar] [varchar](255) NULL,\n" +
                "\t[tasa_basica] [varchar](255) NULL,\n" +
                "\t[tipo_emisor] [varchar](255) NULL,\n" +
                "\t[sw_margen] [varchar](255) NULL,\n" +
                "\t[nemo_bvc] [varchar](255) NULL,\n" +
                "\t[valor_compra] [varchar](255) NULL,\n" +
                "\t[valor_dia] [varchar](255) NULL,\n" +
                "\t[real_360] [varchar](255) NULL,\n" +
                "\t[real_365] [varchar](255) NULL,\n" +
                "\t[tir_360] [varchar](255) NULL,\n" +
                "\t[tir_365] [varchar](255) NULL,\n" +
                "\t[margen] [varchar](255) NULL,\n" +
                "\t[t360_tre] [varchar](255) NULL,\n" +
                "\t[t365_tre] [varchar](255) NULL,\n" +
                "\t[valor_mercado] [varchar](255) NULL,\n" +
                "\t[valor_vcto] [varchar](255) NULL,\n" +
                "\t[plazo] [varchar](255) NULL,\n" +
                "\t[dias_vcto] [varchar](255) NULL,\n" +
                "\t[dias_int] [varchar](255) NULL,\n" +
                "\t[intereses_cobrar] [varchar](255) NULL,\n" +
                "\t[plaza_expedicion] [varchar](255) NULL,\n" +
                "\t[plazo_rango] [varchar](255) NULL,\n" +
                "\t[cod_cli] [varchar](255) NULL,\n" +
                "\t[aleatorio] [varchar](255) NULL,\n" +
                "\t[tir_margen] [varchar](255) NULL,\n" +
                "\t[tasa_basica_marg] [varchar](255) NULL,\n" +
                "\t[estado_anterior] [varchar](255) NULL,\n" +
                "\t[autorizacion] [varchar](255) NULL,\n" +
                "\t[pap_ant] [varchar](255) NULL,\n" +
                "\t[cod_titulo] [varchar](255) NULL,\n" +
                "\t[nro_derecho] [varchar](255) NULL,\n" +
                "\t[tipo_derecho] [varchar](255) NULL,\n" +
                "\t[llave_titulo] [varchar](255) NULL,\n" +
                "\t[mercado_rtefte] [varchar](255) NULL,\n" +
                "\t[inicio_flujo] [varchar](255) NULL,\n" +
                "\t[fin_flujo] [varchar](255) NULL,\n" +
                "\t[valor_flujo] [varchar](255) NULL,\n" +
                "\t[valor_autoretencion] [varchar](255) NULL,\n" +
                "\t[valor_ret_trasladada] [varchar](255) NULL,\n" +
                "\t[valor_ret_por_pagar] [varchar](255) NULL,\n" +
                "\t[valor_ret_comision] [varchar](255) NULL,\n" +
                "\t[sw_genera_plano] [varchar](255) NULL,\n" +
                "\t[moneda_compra] [varchar](255) NULL,\n" +
                "\t[moneda_emision] [varchar](255) NULL,\n" +
                "\t[fec_cumplimiento] [varchar](255) NULL,\n" +
                "\t[vlr_futuro] [varchar](255) NULL,\n" +
                "\t[intereses_futuros] [varchar](255) NULL,\n" +
                "\t[vlr_compromiso] [varchar](255) NULL,\n" +
                "\t[oper_cubierta] [varchar](255) NULL,\n" +
                "\t[rend_papel_fut] [varchar](255) NULL,\n" +
                "\t[vlr_riesgo_ayer] [varchar](255) NULL,\n" +
                "\t[vlr_riesgo_hoy] [varchar](255) NULL,\n" +
                "\t[tir_papel] [varchar](255) NULL,\n" +
                "\t[fec_val_riesgo] [varchar](255) NULL,\n" +
                "\t[dias_futuros] [varchar](255) NULL,\n" +
                "\t[ajuste_indice] [varchar](255) NULL,\n" +
                "\t[vp_titulo] [varchar](255) NULL,\n" +
                "\t[vp_compromiso] [varchar](255) NULL,\n" +
                "\t[vp_titulo_ayer] [varchar](255) NULL,\n" +
                "\t[vp_compromiso_ayer] [varchar](255) NULL,\n" +
                "\t[pap_cruzada] [varchar](255) NULL,\n" +
                "\t[fecha1] [varchar](255) NULL,\n" +
                "\t[negocio1] [varchar](255) NULL,\n" +
                "\t[carrusel] [varchar](255) NULL,\n" +
                "\t[usr_actualiza_remate] [varchar](255) NULL,\n" +
                "\t[valor_total] [varchar](255) NULL,\n" +
                "\t[causacion_neta] [varchar](255) NULL,\n" +
                "\t[tipo_op_mdo] [varchar](255) NULL,\n" +
                "\t[fec_val_riesgo1] [varchar](255) NULL,\n" +
                "\t[isin_star] [varchar](255) NULL,\n" +
                "\t[nombre] [varchar](255) NULL,\n" +
                "\t[nombre_portafolio] [varchar](255) NULL,\n" +
                "\t[nro_identificacion] [varchar](255) NULL,\n" +
                "\t[ctro_contable_alt] [varchar](255) NULL,\n" +
                "\t[tipo_entidad] [varchar](255) NULL,\n" +
                "\t[nro_ope_origen] [varchar](255) NULL,\n" +
                "\t[c] [varchar](255) NULL,\n" +
                "\t[n] [varchar](255) NULL,\n" +
                "\t[isin_star1] [varchar](255) NULL,\n" +
                "\t[nom_emisor] [varchar](255) NULL,\n" +
                "\t[cta_orden] [varchar](255) NULL,\n" +
                "\t[valor_cupon] [varchar](255) NULL\n" +
                "\t);");
        queryCreateTemp.executeUpdate();

        //Query queryBulk = entityManager.createNativeQuery("BULK INSERT nexco_garantias_activas_icrf_bulk FROM 'C:\\Program Files\\Microsoft SQL Server\\MSSQL15.SQLEXPRESS\\MSSQL\\DATA\\Fichero\\Garantias_Activas_"+fechaFormateada+".csv' WITH (FIELDTERMINATOR= ';',ROWTERMINATOR = '\\n', FIRSTROW = 2)");
        Query queryBulk = entityManager.createNativeQuery("BULK INSERT nexco_garantias_activas_icrf_bulk FROM '\\\\co.igrupobbva\\svrfilesystem\\ME_APPS\\RCREDITO\\FICHEROS\\Dialogo\\Garantias_Activas_"+fechaFormateada+".txt' WITH (FIELDTERMINATOR= ';',ROWTERMINATOR = '\\n', FIRSTROW = 1)");
        queryBulk.executeUpdate();

        Query queryTransformData = entityManager.createNativeQuery("DELETE FROM nexco_garantias_activas_icrf WHERE periodo = :periodo ;\n" +
                "INSERT INTO nexco_garantias_activas_icrf (acceso,ajuste_indice,aleatorio,autorizacion,c,carrusel,causacion_neta,cod_cli,cod_emp,cod_titulo,cta_orden,ctro_contable_alt,dias_futuros,dias_int,dias_vcto,emisor,estado,estado_anterior,fec_cumplimiento,fec_val_riesgo,fec_val_riesgo1,fecha,fecha1,fecha_compra,fecha_emision,fecha_final,fecha_val,fecha_vcto,fin_flujo,inicio_flujo,intereses_cobrar,intereses_futuros,isin_star,isin_star1,llave_titulo,margen,mercado_rtefte\n" +
                ",modalidad,moneda,moneda_compra,moneda_emision,n,negocio,negocio1,nemo_bvc,nom_emisor,nombre,nombre_portafolio,nro_derecho,nro_identificacion,nro_ope_origen,nro_papeleta,nro_titulo,oper_cubierta,operacion,origen,pago_capital,pago_dia,pago_interes,pap_ant,pap_cruzada,plaza_expedicion,plazo,plazo_rango,portafolio,real_360,real_365,rend_papel_fut,seguimiento,sw_360_comp,sw_360_vlr,sw_genera_plano,sw_margen,sw_met_lin,sw_met_mar,sw_val_tir,t360_tre,t365_tre,tasa_basica,tasa_basica_marg,tasa_nominal\n" +
                ",tipo,tipo_derecho,tipo_emisor,tipo_entidad,tipo_interes,tipo_op_mdo,tir_360,tir_365,tir_margen,tir_papel,usr_actualiza_remate,valor_autoretencion,valor_compra,valor_dia,valor_flujo,valor_mercado,valor_nominal,valor_ret_comision,valor_ret_por_pagar,valor_ret_trasladada,valor_total,valor_vcto,vlr_compromiso,vlr_futuro,vlr_riesgo_ayer,vlr_riesgo_hoy,vp_compromiso,vp_compromiso_ayer,vp_titulo,vp_titulo_ayer,valor_cupon,periodo)\n" +
                "(SELECT acceso, CONVERT(FLOAT, ajuste_indice),aleatorio, autorizacion, c, carrusel, CONVERT(FLOAT, causacion_neta),cod_cli, cod_emp, cod_titulo, cta_orden, ctro_contable_alt, dias_futuros, dias_int, dias_vcto, emisor, estado, estado_anterior, CONVERT(DATETIME, fec_cumplimiento, 103),CONVERT(DATETIME, fec_val_riesgo, 103),CONVERT(DATETIME, fec_val_riesgo1, 103),CONVERT(DATETIME, fecha, 103),CONVERT(DATETIME, fecha1, 103),CONVERT(DATETIME, fecha_compra, 103),CONVERT(DATETIME, fecha_emision, 103),\n" +
                "CONVERT(DATETIME, fecha_final, 103) ,CONVERT(DATETIME, fecha_val, 103), CONVERT(DATETIME, fecha_vcto, 103)fecha_vcto, CONVERT(DATETIME, fin_flujo, 103)fin_flujo, CONVERT(DATETIME, inicio_flujo, 103)inicio_flujo, intereses_cobrar, intereses_futuros, isin_star, isin_star1, llave_titulo, CONVERT(FLOAT, margen),mercado_rtefte, modalidad, moneda, CONVERT(FLOAT, moneda_compra),CONVERT(FLOAT, moneda_emision), n, negocio, negocio1, nemo_bvc, nom_emisor, nombre, nombre_portafolio, nro_derecho, nro_identificacion, \n" +
                "nro_ope_origen, nro_papeleta, nro_titulo, oper_cubierta, operacion, origen, pago_capital, pago_dia, pago_interes, pap_ant, pap_cruzada, plaza_expedicion, plazo, plazo_rango, portafolio, CONVERT(FLOAT, real_360),CONVERT(FLOAT, real_365),CONVERT(FLOAT, rend_papel_fut),seguimiento, sw_360_comp, sw_360_vlr, sw_genera_plano, sw_margen, sw_met_lin, sw_met_mar, sw_val_tir, CONVERT(FLOAT, t360_tre), CONVERT(FLOAT, t365_tre), tasa_basica, tasa_basica_marg, CONVERT(FLOAT, tasa_nominal),tipo, tipo_derecho, \n" +
                "tipo_emisor, tipo_entidad, tipo_interes, tipo_op_mdo, CONVERT(FLOAT, tir_360), CONVERT(FLOAT, tir_365), tir_margen, CONVERT(FLOAT, tir_papel), usr_actualiza_remate, valor_autoretencion, CONVERT(FLOAT, valor_compra), CONVERT(FLOAT, valor_dia), CONVERT(FLOAT, valor_flujo), CONVERT(FLOAT, valor_mercado), CONVERT(FLOAT, valor_nominal), valor_ret_comision, valor_ret_por_pagar, valor_ret_trasladada, CONVERT(FLOAT, valor_total), CONVERT(FLOAT, valor_vcto), vlr_compromiso, CONVERT(FLOAT, vlr_futuro), CONVERT(FLOAT, vlr_riesgo_ayer),\n" +
                "CONVERT(FLOAT, vlr_riesgo_hoy), CONVERT(FLOAT, vp_compromiso), CONVERT(FLOAT, vp_compromiso_ayer), CONVERT(FLOAT, vp_titulo), CONVERT(FLOAT, vp_titulo_ayer),valor_cupon, :periodo FROM nexco_garantias_activas_icrf_bulk);");
        queryTransformData.setParameter("periodo",periodo);
        queryTransformData.executeUpdate();
    }

    public void insertFileRepos(String periodo)
    {
        LocalDate fecha = LocalDate.parse(generateDatecalendar(periodo));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy").withLocale(java.util.Locale.ENGLISH);
        String fechaFormateada = fecha.format(formatter);

        Query queryCreateTemp = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_repos_e_inter_icrf_bulk ;" +
                "CREATE TABLE nexco_repos_e_inter_icrf_bulk (\n" +
                "\tcodigo varchar(255) NULL,\n" +
                "\tnombre varchar(255) NULL,\n" +
                "\tnegociador varchar(255) NULL,\n" +
                "\tcod_cli varchar(255) NULL,\n" +
                "\tfecha varchar(255) NULL,\n" +
                "\tfecha_final varchar(255) NULL,\n" +
                "\tvalor_total varchar(255) NULL,\n" +
                "\tintereses varchar(255) NULL,\n" +
                "\ttasa varchar(255) NULL,\n" +
                "\tnegocio varchar(255) NULL,\n" +
                "\testado varchar(255) NULL,\n" +
                "\ttipo_mov varchar(255) NULL,\n" +
                "\tnro_papeleta varchar(255) NULL,\n" +
                "\ttipo_op_mdo varchar(255) NULL,\n" +
                "\tcausacion_hoy varchar(255) NULL,\n" +
                "\tcausacion_ayer varchar(255) NULL,\n" +
                "\tportafolio varchar(255) NULL,\n" +
                "\tcontabilidad varchar(255) NULL,\n" +
                "\ttasa_mesa varchar(255) NULL,\n" +
                "\tcausacion_neta varchar(255) NULL,\n" +
                "\tnombre1 varchar(255) NULL,\n" +
                "\tnro_identificacion varchar(255) NULL,\n" +
                "\tduracion_modificada_anual varchar(255) NULL,\n" +
                "\tmoneda varchar(255) NULL,\n" +
                "\tctro_contable_alt varchar(255) NULL,\n" +
                "\ttipo_entidad varchar(255) NULL,\n" +
                "\tvlr_libros varchar(255) NULL,\n" +
                "\tvlr_libros_intereses_hoy varchar(255) NULL,\n" +
                "\tvlr_mdo_gtia_act varchar(255) NULL,\n" +
                "\tvlr_mdo_gtia_pas varchar(255) NULL,\n" +
                "\tpap_reemplaza varchar(255) NULL,\n" +
                "\tcalif_contraparte varchar(255) NULL,\n" +
                "\tcod_puc varchar(255) NULL,\n" +
                "\tcodpuc_incumpl varchar(255) NULL,\n" +
                "\tmonto_prov varchar(255) NULL,\n" +
                "\tmonto_ext varchar(255) NULL,\n" +
                "\tmonto_fin_ext varchar(255) NULL,\n" +
                "\tmonto_legal_f varchar(255) NULL,\n" +
                "\tpuc_rend varchar(255) NULL,\n" +
                "\tmonto_rend varchar(255) NULL,\n" +
                "\tcod_puccupon_ar varchar(255) NULL,\n" +
                "\tmonto_cupon_ar varchar(255) NULL,\n" +
                "\tcodpuc_cupon varchar(255) NULL,\n" +
                "\tmonto_cupon varchar(255) NULL,\n" +
                "\tllamado_margen varchar(255) NULL,\n" +
                "\ttp_llamado_margen varchar(255) NULL,\n" +
                "\tcodpuc_llamado_margend varchar(255) NULL,\n" +
                "\tcodpuc_llamado_margenv varchar(255) NULL,\n" +
                "\tmnto_acum_llamado_margen varchar(255) NULL,\n" +
                "\texposicon_neta varchar(255) NULL,\n" +
                "\ttipo_vinculacion varchar(255) NULL,\n" +
                "\tcodigo_normalizado varchar(255) NULL,\n" +
                "\tportafolio_front varchar(255) NULL,\n" +
                "\tcta_balance varchar(255) NULL,\n" +
                "\tcta_balance_interes varchar(255) NULL,\n" +
                "\tcta_pyg varchar(255) NULL,\n" +
                "\tisin_cdd varchar(255) NULL,\n" +
                "\tcod_emisor varchar(255) NULL,\n" +
                "\tnom_emisor varchar(255) NULL\n" +
                "\t);");
        queryCreateTemp.executeUpdate();

        //Query queryBulk = entityManager.createNativeQuery("BULK INSERT nexco_repos_e_inter_icrf_bulk FROM 'C:\\Program Files\\Microsoft SQL Server\\MSSQL15.SQLEXPRESS\\MSSQL\\DATA\\Fichero\\Repos_e_Interbancarios_"+fechaFormateada+".csv' WITH (FIELDTERMINATOR= ';',ROWTERMINATOR = '\\n', FIRSTROW = 2)");
        Query queryBulk = entityManager.createNativeQuery("BULK INSERT nexco_repos_e_inter_icrf_bulk FROM '\\\\co.igrupobbva\\svrfilesystem\\ME_APPS\\RCREDITO\\FICHEROS\\Dialogo\\Repos_e_Interbancarios_"+fechaFormateada+".txt' WITH (FIELDTERMINATOR= ';',ROWTERMINATOR = '\\n', FIRSTROW = 1)");
        queryBulk.executeUpdate();

        Query queryTransformData = entityManager.createNativeQuery("DELETE FROM nexco_repos_e_inter_icrf WHERE periodo = :periodo ;\n" +
                "INSERT INTO nexco_repos_e_inter_icrf (calif_contraparte,causacion_ayer,causacion_hoy,causacion_neta,cod_cli,cod_emisor,cod_puc,cod_puccupon_ar,codigo,codigo_normalizado,codpuc_cupon,codpuc_incumpl,codpuc_llamado_margend,codpuc_llamado_margenv,contabilidad,cta_balance,cta_balance_interes,cta_pyg,ctro_contable_alt,duracion_modificada_anual,estado,exposicon_neta,fecha,fecha_final,intereses,isin_cdd,llamado_margen,\n" +
                "mnto_acum_llamado_margen,moneda,monto_cupon,monto_cupon_ar,monto_ext,monto_fin_ext,monto_legal_f,monto_prov,monto_rend,negociador,negocio,nom_emisor,nombre,nombre1,nro_identificacion,nro_papeleta,pap_reemplaza,portafolio,portafolio_front,puc_rend,tasa,tasa_mesa,tipo_entidad,tipo_mov,tipo_op_mdo,tipo_vinculacion,tp_llamado_margen,valor_total,vlr_libros,vlr_libros_intereses_hoy,vlr_mdo_gtia_act,vlr_mdo_gtia_pas,periodo)\n" +
                "(SELECT calif_contraparte,CONVERT(FLOAT, causacion_ayer),CONVERT(FLOAT, causacion_hoy),causacion_neta,cod_cli,cod_emisor,cod_puc, cod_puccupon_ar, codigo, codigo_normalizado, codpuc_cupon, codpuc_incumpl, codpuc_llamado_margend, codpuc_llamado_margenv, contabilidad, cta_balance, cta_balance_interes, cta_pyg, ctro_contable_alt, duracion_modificada_anual, estado, exposicon_neta, CONVERT(DATETIME, fecha, 103),\n" +
                "CONVERT(DATETIME, fecha_final, 103),intereses, isin_cdd, llamado_margen, mnto_acum_llamado_margen, moneda, monto_cupon, monto_cupon_ar, CONVERT(FLOAT, monto_ext),CONVERT(FLOAT, monto_fin_ext),CONVERT(FLOAT, monto_legal_f),CONVERT(FLOAT, monto_prov),monto_rend, negociador, negocio, nom_emisor, nombre, nombre1, nro_identificacion, nro_papeleta, pap_reemplaza, portafolio, portafolio_front, puc_rend, tasa, tasa_mesa, \n" +
                "tipo_entidad, tipo_mov, tipo_op_mdo, tipo_vinculacion, tp_llamado_margen, CONVERT(FLOAT, valor_total),CONVERT(FLOAT, REPLACE(vlr_libros, ',', '')),vlr_libros_intereses_hoy, vlr_mdo_gtia_act, vlr_mdo_gtia_pas, :periodo FROM nexco_repos_e_inter_icrf_bulk);\n" +
                "UPDATE nexco_repos_e_inter_icrf SET contabilidad = '20153' WHERE nombre = 'VENTA EN CORTO' and periodo = :periodo and (contabilidad is null or contabilidad = '');");
        queryTransformData.setParameter("periodo",periodo);
        queryTransformData.executeUpdate();

        Query queryUpdateIsin = entityManager.createNativeQuery("UPDATE a SET a.isin_new = b.dn03por1_isin_bvc FROM (select * from nexco_repos_e_inter_icrf where periodo = :periodo ) AS a,\n" +
                "(select distinct nemo_bvc,dn03por1_isin_bvc from nexco_portafolio_diario_icrf where periodo = :periodo and nemo_bvc is not null) AS b\n" +
                "WHERE a.isin_cdd=b.nemo_bvc");
        queryUpdateIsin.setParameter("periodo",periodo);
        queryUpdateIsin.executeUpdate();
    }

    public void insertFilePortafolioDiario(String periodo)
    {
        LocalDate fecha = LocalDate.parse(generateDatecalendar(periodo));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMMyyyy").withLocale(java.util.Locale.ENGLISH);
        String fechaFormateada = fecha.format(formatter);

        Query queryCreateTemp = entityManager.createNativeQuery("DROP TABLE IF EXISTS nexco_portafolio_diario_icrf_bulk ;" +
                "CREATE TABLE nexco_portafolio_diario_icrf_bulk (\n" +
                "origen varchar(255),\n" +
                "portafolio varchar(255),\n" +
                "nombre_portafolio varchar(255),\n" +
                "fecha_val varchar(255),\n" +
                "nro_titulo varchar(255),\n" +
                "isin_star varchar(255),\n" +
                "emisor varchar(255),\n" +
                "nombre_emisor varchar(255),\n" +
                "fecha_vcto varchar(255),\n" +
                "inicio_flujo varchar(255),\n" +
                "fin_flujo varchar(255),\n" +
                "tasa_cupon varchar(255),\n" +
                "tipo_papel varchar(255),\n" +
                "tipo varchar(255),\n" +
                "valor_nominal varchar(255),\n" +
                "saldo_capital varchar(255),\n" +
                "moneda varchar(255),\n" +
                "causados varchar(255),\n" +
                "precio_limpio varchar(255),\n" +
                "valor_compra varchar(255),\n" +
                "interes_lineal_compra varchar(255),\n" +
                "intereses_lineal_hoy varchar(255),\n" +
                "valor_mercado2 varchar(255),\n" +
                "fecha_compra varchar(255),\n" +
                "tir_compra varchar(255),\n" +
                "tir_365 varchar(255),\n" +
                "tir_365_1 varchar(255),\n" +
                "precio_sucio_asset varchar(255),\n" +
                "estado_ varchar(255),\n" +
                "tvariable varchar(255),\n" +
                "tvariable1 varchar(255),\n" +
                "tasa_nominal varchar(255),\n" +
                "spread varchar(255),\n" +
                "dn03por1_duracion_br varchar(255),\n" +
                "prima_descto_total varchar(255),\n" +
                "fecha_emision varchar(255),\n" +
                "dias_vcto varchar(255),\n" +
                "amortiza varchar(255),\n" +
                "mod_interes varchar(255),\n" +
                "pago_interes varchar(255),\n" +
                "tipo_custodio varchar(255),\n" +
                "custodio varchar(255),\n" +
                "base_interes varchar(255),\n" +
                "caculint varchar(255),\n" +
                "cod_tipo_interes varchar(255),\n" +
                "tipo_interes varchar(255),\n" +
                "puntos_adicionales_spead varchar(255),\n" +
                "nro_total_dias_cupon varchar(255),\n" +
                "intereses_cobrar varchar(255),\n" +
                "cta_contab_concil varchar(255),\n" +
                "cod_emp varchar(255),\n" +
                "nemo_bvc varchar(255),\n" +
                "valor_mercado2_a varchar(255),\n" +
                "valor_causacion_hoy varchar(255),\n" +
                "valor_causacion_ayer varchar(255),\n" +
                "valor_en_mda_hoy varchar(255),\n" +
                "valor_en_mda_ayer varchar(255),\n" +
                "prima_descto_hoy varchar(255),\n" +
                "prima_descto_ayer varchar(255),\n" +
                "intereses_lineal_ayer varchar(255),\n" +
                "dn03por1_valor_infoval varchar(255),\n" +
                "nit_emisor varchar(255),\n" +
                "nominal_residual varchar(255),\n" +
                "duracion_modificada varchar(255),\n" +
                "estado_garantia varchar(255),\n" +
                "ctro_contable_alt varchar(255),\n" +
                "vr_patrimonio_hoy varchar(255),\n" +
                "vr_patrimonio_ayer varchar(255),\n" +
                "dn03por1_isin_bvc varchar(255),\n" +
                "num_acc varchar(255),\n" +
                "clas_acc varchar(255),\n" +
                "vlr_cmp_pesos varchar(255),\n" +
                "modalidad varchar(255),\n" +
                "tasvar_1erflujo varchar(255),\n" +
                "fec_ult_rep varchar(255),\n" +
                "vlr_presult_rep varchar(255),\n" +
                "calificacion varchar(255),\n" +
                "ent_calificadora varchar(255),\n" +
                "cal_riesgo_emi varchar(255),\n" +
                "cal_avalista varchar(255),\n" +
                "nro_ident varchar(255),\n" +
                "codcat_unicausa_val varchar(255),\n" +
                "caus_val varchar(255),\n" +
                "codcat_unicausa_pat varchar(255),\n" +
                "causa_pat varchar(255),\n" +
                "vlr_raz_hv varchar(255),\n" +
                "nro_asignado varchar(255),\n" +
                "tip_id_emisor varchar(255),\n" +
                "id_emisor varchar(255),\n" +
                "nat_jur_emi varchar(255),\n" +
                "ciiu_emi varchar(255),\n" +
                "vinc_emisor varchar(255),\n" +
                "prov_precios varchar(255),\n" +
                "mod_orig varchar(255),\n" +
                "codigo_normalizado varchar(255),\n" +
                "cod_cli varchar(255),\n" +
                "portafolio_front varchar(255),\n" +
                "cta_pyg varchar(255),\n" +
                "cta_pyg_prima varchar(255),\n" +
                "cta_pyg_rof varchar(255),\n" +
                "correccion_monetaria varchar(255),\n" +
                "cta_orden_pp varchar(255),\n" +
                "titulo_fraccionado_padre varchar(255),\n" +
                "titulo_fraccionado_original varchar(255)\n"+
                ");");
        queryCreateTemp.executeUpdate();

        //Query queryBulk = entityManager.createNativeQuery("BULK INSERT nexco_portafolio_diario_icrf_bulk FROM 'C:\\Program Files\\Microsoft SQL Server\\MSSQL15.SQLEXPRESS\\MSSQL\\DATA\\Fichero\\Portafolio_Diario_"+fechaFormateada+".csv' WITH (FIELDTERMINATOR= ';',ROWTERMINATOR = '\\n', FIRSTROW = 2)");
        Query queryBulk = entityManager.createNativeQuery("BULK INSERT nexco_portafolio_diario_icrf_bulk FROM '\\\\co.igrupobbva\\svrfilesystem\\ME_APPS\\RCREDITO\\FICHEROS\\Dialogo\\Portafolio_Diario_"+fechaFormateada+".txt' WITH (FIELDTERMINATOR= ';',ROWTERMINATOR = '\\n', FIRSTROW = 1)");
        queryBulk.executeUpdate();

        Query queryTransformData = entityManager.createNativeQuery("DELETE FROM nexco_portafolio_diario_icrf WHERE periodo = :periodo  ;\n" +
                "INSERT INTO nexco_portafolio_diario_icrf (origen, portafolio, nombre_portafolio, fecha_val, nro_titulo, isin_star, emisor, nombre_emisor, fecha_vcto, inicio_flujo, fin_flujo, tasa_cupon, tipo_papel, tipo, valor_nominal, saldo_capital, moneda, causados, precio_limpio, valor_compra, interes_lineal_compra, intereses_lineal_hoy, valor_mercado2, fecha_compra, tir_compra, tir_365, tir_365_1, precio_sucio_asset, estado_, tvariable, tvariable1, tasa_nominal, spread, dn03por1_duracion_br, \n" +
                "prima_descto_total, fecha_emision, dias_vcto, amortiza, mod_interes, pago_interes, tipo_custodio, custodio, base_interes, caculint, cod_tipo_interes, tipo_interes, puntos_adicionales_spead, nro_total_dias_cupon, intereses_cobrar, cta_contab_concil, cod_emp, nemo_bvc, valor_mercado2_a, valor_causacion_hoy, valor_causacion_ayer, valor_en_mda_hoy, valor_en_mda_ayer, prima_descto_hoy, prima_descto_ayer, intereses_lineal_ayer, dn03por1_valor_infoval, nit_emisor, nominal_residual, \n" +
                "duracion_modificada, estado_garantia, ctro_contable_alt, vr_patrimonio_hoy, vr_patrimonio_ayer, dn03por1_isin_bvc, num_acc, clas_acc, vlr_cmp_pesos, modalidad, tasvar_1erflujo, fec_ult_rep, vlr_presult_rep, calificacion, ent_calificadora, cal_riesgo_emi, cal_avalista, nro_ident, codcat_unicausa_val, caus_val, codcat_unicausa_pat, causa_pat, vlr_raz_hv, nro_asignado, tip_id_emisor, id_emisor, nat_jur_emi, ciiu_emi, vinc_emisor, prov_precios, mod_orig, codigo_normalizado, cod_cli, portafolio_front, cta_pyg, cta_pyg_prima, cta_pyg_rof, correccion_monetaria, cta_orden_pp, titulo_fraccionado_padre, titulo_fraccionado_original,periodo)\n" +
                "(SELECT origen, portafolio, nombre_portafolio, CONVERT(DATETIME, fecha_val, 103), nro_titulo, isin_star, emisor, nombre_emisor, CONVERT(DATETIME, fecha_vcto, 103), CONVERT(DATETIME, inicio_flujo, 103), CONVERT(DATETIME, fin_flujo, 103), CONVERT(FLOAT, tasa_cupon), tipo_papel, tipo,  CONVERT(FLOAT, valor_nominal), CONVERT(FLOAT, saldo_capital), moneda, causados, CONVERT(FLOAT, precio_limpio), CONVERT(FLOAT, valor_compra),  CONVERT(FLOAT, interes_lineal_compra),  CONVERT(FLOAT, intereses_lineal_hoy),  CONVERT(FLOAT, valor_mercado2),  CONVERT(DATETIME, fecha_compra, 103), tir_compra, tir_365, tir_365_1, precio_sucio_asset, estado_, \n" +
                "tvariable, tvariable1, tasa_nominal, spread, dn03por1_duracion_br, prima_descto_total,  CONVERT(DATETIME, fecha_emision, 103), dias_vcto, amortiza, mod_interes, pago_interes, tipo_custodio, custodio, base_interes, caculint, cod_tipo_interes, tipo_interes, puntos_adicionales_spead, nro_total_dias_cupon, CONVERT(FLOAT, intereses_cobrar),   cta_contab_concil, cod_emp, nemo_bvc, CONVERT(FLOAT, valor_mercado2_a), CONVERT(FLOAT, valor_causacion_hoy),  CONVERT(FLOAT, valor_causacion_ayer),  CONVERT(FLOAT, valor_en_mda_hoy),  CONVERT(FLOAT, valor_en_mda_ayer),  CONVERT(FLOAT, prima_descto_hoy),  CONVERT(FLOAT, prima_descto_ayer),  \n" +
                "CONVERT(FLOAT, intereses_lineal_ayer),   dn03por1_valor_infoval, nit_emisor, nominal_residual, duracion_modificada, estado_garantia, ctro_contable_alt, CONVERT(FLOAT, vr_patrimonio_hoy),  CONVERT(FLOAT, vr_patrimonio_ayer),  dn03por1_isin_bvc, num_acc, clas_acc, CONVERT(FLOAT, vlr_cmp_pesos),  modalidad, tasvar_1erflujo, CONVERT(DATETIME, fec_ult_rep, 103), CONVERT(FLOAT, vlr_presult_rep),  calificacion, ent_calificadora, cal_riesgo_emi, cal_avalista, nro_ident, codcat_unicausa_val, CONVERT(FLOAT, caus_val),  codcat_unicausa_pat, causa_pat,  CONVERT(FLOAT, vlr_raz_hv),  nro_asignado, tip_id_emisor, id_emisor, nat_jur_emi, ciiu_emi, \n" +
                "vinc_emisor, prov_precios, mod_orig, codigo_normalizado, cod_cli, portafolio_front, cta_pyg, cta_pyg_prima, cta_pyg_rof, correccion_monetaria, cta_orden_pp, titulo_fraccionado_padre, titulo_fraccionado_original, :periodo  FROM nexco_portafolio_diario_icrf_bulk);");
        queryTransformData.setParameter("periodo",periodo);
        queryTransformData.executeUpdate();
    }

    public List<ReposIcrf> getDataRepos(String periodo){
        return reposIcrfRepository.findByPeriodo(periodo);
    }

    public List<GarantiasActivasIcrf> getDataGarantias(String periodo){
        return garantiasActivasIcrfRepository.findByPeriodo(periodo);
    }

    public List<Object[]> getDataResumeTotal(String periodo){
        String[] parts =periodo.split("-");
        Query querySelectData = entityManager.createNativeQuery("SELECT contabilidad,isin_star,isin_new,vlr_libros,conteo,coste_excupon,valor_nominal,valor_compra,valor_mercado,convertido_nominal,convertido_compra,reporte_nominal,reporte_compra,reporte_mercado FROM nexco_resumen_vc_icrf WHERE periodo = :periodo \n" +
                "UNION ALL\n" +
                "SELECT 'Total General','','',sum(vlr_libros),sum(conteo),sum(coste_excupon),sum(valor_nominal),sum(valor_compra),sum(valor_mercado),sum(convertido_nominal),sum(convertido_compra),sum(reporte_nominal),sum(reporte_compra),sum(reporte_mercado) FROM nexco_resumen_vc_icrf WHERE periodo = :periodo ");
        querySelectData.setParameter("periodo",periodo);
        return querySelectData.getResultList();
    }

    public void generateResume(String periodo){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate fecha = LocalDate.parse(periodo + "-01", formatter);
        LocalDate fechaResultado = fecha.minusMonths(1);
        String fechaResultadoString = fechaResultado.format(formatter);
        String[] parts =fechaResultadoString.split("-");
        Query querySelectData = entityManager.createNativeQuery("DELETE FROM nexco_resumen_vc_icrf WHERE periodo = :periodo ;" +
                "INSERT INTO nexco_resumen_vc_icrf (contabilidad,isin_star,isin_new,vlr_libros,conteo,coste_excupon,valor_nominal,valor_compra,valor_mercado,convertido_nominal,convertido_compra,reporte_nominal,reporte_compra,reporte_mercado,periodo)\n" +
                "(SELECT c.contabilidad,a.isin_star,b.dn03por1_isin_bvc,c.vlr_libros,c.conteo,c.coste_excupon,d.valor_nominal,d.valor_compra,d.valor_mercado,d.nominal1,d.compra1,ROUND(d.nominal1/1000, 0) AS nominal2,ROUND(d.compra1, 0) AS compra2,ROUND(d.valor_mercado/1000, 0) AS mercado1, :periodo  FROM (SELECT isin_star FROM nexco_garantias_activas_icrf WHERE periodo = :periodo  and negocio1 = 16 and operacion = 16 GROUP BY isin_star UNION SELECT isin_cdd AS isin_star FROM nexco_repos_e_inter_icrf WHERE periodo = :periodo  AND nombre = 'VENTA EN CORTO' GROUP BY isin_cdd)a\n" +
                "LEFT JOIN (SELECT nemo_bvc,dn03por1_isin_bvc FROM nexco_portafolio_diario_icrf WHERE periodo = :periodo  AND nemo_bvc IS NOT NULL GROUP BY nemo_bvc,dn03por1_isin_bvc) b ON a.isin_star=b.nemo_bvc\n" +
                "LEFT JOIN (SELECT contabilidad,isin_cdd,isin_new,sum(vlr_libros) AS vlr_libros, count(isin_cdd) AS conteo, ROUND(sum(vlr_libros)/1000, 0) AS coste_excupon FROM nexco_repos_e_inter_icrf WHERE periodo = :periodo  AND nombre = 'VENTA EN CORTO' GROUP BY contabilidad,isin_cdd,isin_new) c ON a.isin_star=c.isin_cdd\n" +
                "LEFT JOIN (SELECT isin_star,sum(valor_nominal) AS valor_nominal, sum(valor_compra) AS valor_compra, sum(valor_mercado) AS valor_mercado, CASE WHEN SUBSTRING(isin_star,1,3)='TUV' THEN sum(valor_nominal)*(SELECT top 1 peso_cop_uvr FROM nexco_uvr_icrf WHERE MONTH(fecha) = :mes  AND YEAR(fecha)= :ano ) ELSE sum(valor_nominal) END AS nominal1,CASE WHEN SUBSTRING(isin_star,1,3)='TUV' THEN sum(valor_compra)*(SELECT top 1 peso_cop_uvr FROM nexco_uvr_icrf WHERE MONTH(fecha) = :mes  AND YEAR(fecha)= :ano ) ELSE sum(valor_compra) END AS compra1 FROM nexco_garantias_activas_icrf WHERE periodo = :periodo  and negocio1 = 16 and operacion = 16 GROUP BY isin_star) d ON a.isin_star=d.isin_star);");
        querySelectData.setParameter("periodo",periodo);
        querySelectData.setParameter("ano",parts[0]);
        querySelectData.setParameter("mes",parts[1]);
        querySelectData.executeUpdate();
    }

    /*public ReportIcrv modifyReport(ReportIcrv toModify, User user)
    {
        loadAudit(user,"Modificación Exitosa Registro ("+toModify.getIdReport()+") Venta en Corto ICRF");
        return reportIcrvRepository.save(toModify);
    }
    public List<VentasEnCortoIcrf> findAllReport(String periodo)
    {
        return reportIcrvRepository.findByPeriodo(periodo);
    }

    public void completeTable(String periodo) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);

        Query query = entityManager.createNativeQuery("delete from nexco_report_icrv where periodo = :periodo ;\n" +
                "insert into nexco_report_icrv (entidad,cod_periodo,cod_sociinfo,xti_cartera,cod_socipart,signo_valor_contable,signo_microcobertura,periodo)\n" +
                "(select entidad,cod_periodo,cod_sociinfo,xti_cartera,cod_socipart,signo_valor_contable,signo_microcobertura, :periodo as periodo from nexco_plantilla_report_icrv);");
        query.setParameter("periodo",periodo);
        query.executeUpdate();

        updateTable(periodo);
    }

    public void updateTable(String periodo) {

        Query validate = entityManager.createNativeQuery("update a set a.cod_isin=b.isin,a.coste_valor=round(isnull(b.saldo_libros_valoracion,0)/1000,0) from (select * from nexco_report_icrv where periodo = :periodo ) a, (select * from nexco_calculo_icrv where periodo = :periodo ) b where a.entidad=b.empresa;\n" +
                "update a set a.ajuste_valor_razonable=round(isnull(b.saldo_libros_valoracion,0)/1000,0)-isnull(a.coste_valor,0) from (select * from nexco_report_icrv where periodo = :periodo ) a, (select * from nexco_calculo_icrv where periodo = :periodo ) b where a.entidad=b.empresa;\n" +
                "update a set a.desembolso_pdte=isnull(a.coste_valor,0)+isnull(a.ajuste_valor_razonable,0)-isnull(a.correcciones_por_deterioro,0) from (select * from nexco_report_icrv where periodo = :periodo ) a, (select * from nexco_calculo_icrv where periodo = :periodo ) b where a.entidad=b.empresa;" +
                "");
        validate.setParameter("periodo",periodo);
        validate.executeUpdate();
    }

    public void clearReport(User user, String periodo){
        completeTable(periodo);
        loadAudit(user,"Generación Exitosa de Reporte para "+periodo);
    }

    public ArrayList<String[]> saveFileBDPlantilla(InputStream file, User user, String periodo) throws IOException, InvalidFormatException {
        ArrayList<String[]> list=new ArrayList<>();
        if (file!=null)
        {
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            Iterator<Row> rows = sheet.iterator();
            list=validarPlantilla(rows, periodo);
            if(list.get(0)[2].equals("SUCCESS"))
                loadAudit(user,"Cargue Exitoso Plantilla Reporte ICRV");
            else
                loadAudit(user,"Cargue Fallido Plantilla Reporte ICRV");
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows, String periodo) {
        ArrayList<String[]> lista = new ArrayList();
        ArrayList<ReportIcrv> toInsert = new ArrayList<>();
        ArrayList<PlantillaReportIcrv> toInsertPre = new ArrayList<>();
        String stateFinal = "SUCCESS";
        XSSFRow row;
        while (rows.hasNext()) {
            row = (XSSFRow) rows.next();
            if (row.getRowNum() > 0) {
                {
                    DataFormatter formatter = new DataFormatter();
                    int consecutive = 0;
                    String cellEntidad = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodPeriodo = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodSociinfo = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellXtiCartera = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodSocipart = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellCodIsin = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    XSSFCell cell0= row.getCell(consecutive++);
                    if(cell0!=null)
                        cell0.setCellType(CellType.STRING);
                    String cellCosteValor = formatter.formatCellValue(cell0).replace(" ", "").replace(",","");

                    XSSFCell cell1= row.getCell(consecutive++);
                    if(cell1!=null)
                        cell1.setCellType(CellType.STRING);
                    String cellAjusteValorRazonable = formatter.formatCellValue(cell1).replace(" ", "").replace(",","");

                    XSSFCell cell2= row.getCell(consecutive++);
                    if(cell2!=null)
                        cell2.setCellType(CellType.STRING);
                    String cellMicrocoberturas = formatter.formatCellValue(cell2).replace(" ", "").replace(",","");

                    XSSFCell cell3= row.getCell(consecutive++);
                    if(cell3!=null)
                        cell3.setCellType(CellType.STRING);
                    String cellCorreccionesPorDeterioro = formatter.formatCellValue(cell3).replace(" ", "").replace(",","");

                    XSSFCell cell4= row.getCell(consecutive++);
                    if(cell4!=null)
                        cell4.setCellType(CellType.STRING);
                    String cellValorCotizado = formatter.formatCellValue(cell4).replace(" ", "").replace(",","");

                    XSSFCell cell5= row.getCell(consecutive++);
                    if(cell5!=null)
                        cell5.setCellType(CellType.STRING);
                    String cellDesembolsoPdte = formatter.formatCellValue(cell5).replace(" ", "").replace(",","");

                    XSSFCell cell6= row.getCell(consecutive++);
                    if(cell6!=null)
                        cell6.setCellType(CellType.STRING);
                    String cellNumTitulos = formatter.formatCellValue(cell6).replace(" ", "").replace(",","");

                    XSSFCell cell7= row.getCell(consecutive++);
                    if(cell7!=null)
                        cell7.setCellType(CellType.STRING);
                    String cellCapitalSocial = formatter.formatCellValue(cell7).replace(" ", "").replace(",","");

                    XSSFCell cell8= row.getCell(consecutive++);
                    if(cell8!=null)
                        cell8.setCellType(CellType.STRING);
                    String cellCosteAdquisicion = formatter.formatCellValue(cell8).replace(" ", "");

                    String cellSignoValorContable = formatter.formatCellValue(row.getCell(consecutive++)).trim();
                    String cellSignoMicrocobertura = formatter.formatCellValue(row.getCell(consecutive++)).trim();

                    if (cellEntidad.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(0);
                        log[2] = "El campo Entidad no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodSociinfo.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(2);
                        log[2] = "El campo CodSociinfo no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellXtiCartera.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(3);
                        log[2] = "El campo XtiCartera no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellCodSocipart.length() == 0) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(4);
                        log[2] = "El campo CodSocipart no puede estar vacio.";
                        lista.add(log);
                    }
                    if (cellMicrocoberturas.length()!=0) {
                        try{
                            Double.parseDouble(cellMicrocoberturas);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(8);
                            log[2] = "El campo Microcoberturas debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellMicrocoberturas="0";
                    }
                    if (cellCorreccionesPorDeterioro.length()!=0) {
                        try{
                            Double.parseDouble(cellCorreccionesPorDeterioro);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(9);
                            log[2] = "El campo Correcciones Por Deterioro debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellCorreccionesPorDeterioro="0";
                    }
                    if (cellValorCotizado.length()!=0) {
                        try{
                            Double.parseDouble(cellValorCotizado);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(10);
                            log[2] = "El campo Valor Cotizado debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellValorCotizado="0";
                    }
                    if (cellCosteAdquisicion.length()!=0) {
                        try{
                            Double.parseDouble(cellCosteAdquisicion);
                        }
                        catch (Exception e)
                        {
                            String[] log = new String[3];
                            log[0] = String.valueOf(row.getRowNum() + 1);
                            log[1] = CellReference.convertNumToColString(11);
                            log[2] = "El campo Coste Adquisicion debe ser numérico.";
                            lista.add(log);
                        }
                    }
                    else {
                        cellCosteAdquisicion="0";
                    }
                    if (!cellSignoValorContable.equals("+") && !cellSignoValorContable.equals("-")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(24);
                        log[2] = "El campo Signo Valor Contable debe ser + o -.";
                        lista.add(log);
                    }
                    if (!cellSignoMicrocobertura.equals("+") && !cellSignoMicrocobertura.equals("-")) {
                        String[] log = new String[3];
                        log[0] = String.valueOf(row.getRowNum() + 1);
                        log[1] = CellReference.convertNumToColString(25);
                        log[2] = "El campo Signo Valor Contable debe ser + o -.";
                        lista.add(log);
                    }

                    if(lista.size() == 0) {
                        PlantillaReportIcrv data0 = new PlantillaReportIcrv();
                        data0.setEntidad(cellEntidad);
                        data0.setCodPeriodo(periodo.replace("-",""));
                        data0.setCodSociinfo(cellCodSociinfo);
                        data0.setXtiCartera(cellXtiCartera);
                        data0.setCodSocipart(cellCodSocipart);
                        data0.setSignoValorContable(cellSignoValorContable);
                        data0.setSignoMicrocobertura(cellSignoMicrocobertura);
                        data0.setPeriodo(periodo);
                        toInsertPre.add(data0);

                        ReportIcrv data = new ReportIcrv();
                        data.setEntidad(cellEntidad);
                        data.setCodPeriodo(periodo.replace("-",""));
                        data.setCodSociinfo(cellCodSociinfo);
                        data.setXtiCartera(cellXtiCartera);
                        data.setCodSocipart(cellCodSocipart);
                        data.setMicrocoberturas(Double.parseDouble(cellMicrocoberturas));
                        data.setCorreccionesPorDeterioro(Math.abs(Double.parseDouble(cellCorreccionesPorDeterioro)));
                        data.setValorCotizado(Double.parseDouble(cellValorCotizado));
                        data.setCosteAdquisicion(Double.parseDouble(cellCosteAdquisicion));
                        data.setSignoValorContable(cellSignoValorContable);
                        data.setSignoMicrocobertura(cellSignoMicrocobertura);
                        data.setPeriodo(periodo);
                        toInsert.add(data);
                    }
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
        if (temp[2].equals("SUCCESS")) {
            plantillaReportIcrvRepository.deleteAll();
            plantillaReportIcrvRepository.saveAll(toInsertPre);
            reportIcrvRepository.deleteByPeriodo(periodo);
            reportIcrvRepository.saveAll(toInsert);
            updateTable(periodo);
        }
        toInsert.clear();
        return lista;
    }

    public List<ReportIcrv> findByFilter(String value, String filter, String period) {
        List<ReportIcrv> list = new ArrayList<>();
        try {
            switch (filter) {
                case "Entidad":
                    list = reportIcrvRepository.findByEntidadAndPeriodo(value, period);
                    break;
                case "Código Periodo":
                    list = reportIcrvRepository.findByCodPeriodoAndPeriodo(value, period);
                    break;
                case "CodSociinfo":
                    list = reportIcrvRepository.findByCodSociinfoAndPeriodo(value, period);
                    break;
                case "XtiCartera":
                    list = reportIcrvRepository.findByXtiCarteraAndPeriodo(value, period);
                    break;
                case "CodSocipart":
                    list = reportIcrvRepository.findByCodSocipartAndPeriodo(value, period);
                    break;
                case "CodISIN":
                    list = reportIcrvRepository.findByCodIsinAndPeriodo(value, period);
                    break;
                case "Signo Valor Contable":
                    list = reportIcrvRepository.findBySignoValorContableAndPeriodo(value, period);
                    break;
                case "Signo Microcobertura":
                    list = reportIcrvRepository.findBySignoMicrocoberturaAndPeriodo(value, period);
                    break;
                default:
                    break;
            }
        }
        catch (Exception e)
        {
            return list;
        }
        return list;
    }*/

}


