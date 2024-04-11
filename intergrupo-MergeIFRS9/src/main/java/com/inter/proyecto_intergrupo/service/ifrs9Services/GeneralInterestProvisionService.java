package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.ifrs9.GeneralInterestProvision;
import com.inter.proyecto_intergrupo.model.temporal.GeneralInterestProvisionTemporal;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class GeneralInterestProvisionService {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager entityManager;

    public ArrayList<String[]> validateTemplateAndInsert(InputStream file, String period) throws ParseException {
        ArrayList<String[]> validation = new ArrayList<>();

        loadTemporalFile(file,period);

        Query validate = entityManager.createNativeQuery("SELECT temp.id_prov,temp.califica,temp.cartera,temp.codigo_ifrs9,ISNULL(gen.cartera,'no') as vali from nexco_provision_general_interes_temp as temp\n" +
                "LEFT JOIN nexco_parametrica_genericas as gen ON gen.cartera = temp.cartera AND gen.calificacion = temp.califica AND gen.codigo_ifrs9 = temp.codigo_ifrs9 where gen.cartera is null");

        List<Object[]> result = validate.getResultList();

        for(Object[] res : result){
            if(res[4].toString().equals("no")){
                String[] log = new String[4];
                log[0] = res[0].toString();
                log[1] = res[1].toString();
                log[2] = "La  combinación cartera ("+res[2].toString()+"), calificación ("+res[1].toString()+") y código IFRS9 ("+res[3].toString()+") no se encuentra en la paramétrica de genéricas";
                log[3] = "false";
                validation.add(log);
            }
        }
        String[] log = new String[4];
        log[3] = "true";
        validation.add(log);

        String[] temp = validation.get(0);

        if(temp[3].equals("true")){

            ArrayList<String[]> validateEmpty = validateEmpty();

            if(validateEmpty.isEmpty()){
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
                Date currentDate = formatter.parse(period);

                Calendar c = Calendar.getInstance();
                c.setTime(currentDate);
                c.add(Calendar.MONTH,-1);
                Date resultAfter  = c.getTime();


                String currentMinusMonth = formatter.format(resultAfter);

                Query delete = entityManager.createNativeQuery("DELETE FROM nexco_provision_general_interes WHERE periodo = ?");
                delete.setParameter(1,period);
                delete.executeUpdate();

                Query insertFinal = entityManager.createNativeQuery("INSERT INTO nexco_provision_general_interes (tp, nit, tp_car, suc_obl_al, numero_op, sucursal, lin_subpro, cartera, prod_alta, alivios, alivios_covid, \n" +
                        "capital,intereses,vr_int_cte,vr_mora,cxc,provis_cap,provis_int,califica_7_ini,califica_ini,fec_vto_i,fecha_ini_mora,lineas_sfc,nueva_califica,nueva_califica_7,califica,\n" +
                        "califica_7,aceptado,aplicado,estado,fecha_vcto_alivio,fecha_vcto_pad,periodo_gracia_int,tipo,bolsillos,filtro,interes,prov_interes_ini,prov_gral_int_fm,\n" +
                        "califica_7_pda,marca,califica_pda,prov_gral_int_100,marca_mrco,check_int,saldo_intereses_bolsillo,intereses_bolsillos,prov_gral_int,check_bolsillo,\n" +
                        "check_prov_int,tp_fidei,nit_fidei,tp_def,nit_def,ciiu,nombre_2,corazu,sector,codigo_ifrs9,nit_sin_dv,dv,banca,nivel_ventas_2,periodo,empresa,valor,mes_anterior, diferencia_meses,cuenta_balance,cuenta_pyg,cuenta_pyg_valor,contrato,fuente_info)\n" +
                        "SELECT ISNULL(temp.tp, mesAnt.tp),ISNULL(temp.nit,mesAnt.nit),ISNULL(temp.tp_car,mesAnt.tp_car), \n" +
                        "ISNULL(temp.suc_obl_al,mesAnt.suc_obl_al),ISNULL(temp.numero_op,mesAnt.numero_op),ISNULL(temp.sucursal,mesAnt.sucursal),ISNULL(temp.lin_subpro,mesAnt.lin_subpro),\n" +
                        "ISNULL(temp.cartera,mesAnt.cartera),ISNULL(temp.prod_alta, mesAnt.prod_alta),ISNULL(temp.alivios, mesAnt.alivios),ISNULL(temp.alivios_covid, mesAnt.alivios_covid),\n" +
                        "ISNULL(temp.capital, mesAnt.capital),ISNULL(temp.intereses, mesAnt.intereses),ISNULL(temp.vr_int_cte,mesAnt.vr_int_cte),ISNULL(temp.vr_mora, mesAnt.vr_mora), \n" +
                        "ISNULL(temp.cxc,mesAnt.cxc),ISNULL(temp.provis_cap, mesAnt.provis_cap),ISNULL(temp.provis_int,mesAnt.provis_int),ISNULL(temp.califica_7_ini,mesAnt.califica_7_ini), \n" +
                        "ISNULL(temp.califica_ini,mesAnt.califica_ini),ISNULL(temp.fec_vto_i,mesAnt.fec_vto_i),ISNULL(temp.fecha_ini_mora,mesAnt.fecha_ini_mora),ISNULL(temp.lineas_sfc,mesAnt.lineas_sfc),\n" +
                        "ISNULL(temp.nueva_califica,mesAnt.nueva_califica),ISNULL(temp.nueva_califica_7, mesAnt.nueva_califica_7),ISNULL(temp.califica,mesAnt.califica),\n" +
                        "ISNULL(temp.califica_7,mesAnt.califica_7),ISNULL(temp.aceptado,mesAnt.aceptado),ISNULL(temp.aplicado,mesAnt.aplicado),ISNULL(temp.estado,mesAnt.estado),\n" +
                        "ISNULL(temp.fecha_vcto_alivio,mesAnt.fecha_vcto_alivio),ISNULL(temp.fecha_vcto_pad,mesAnt.fecha_vcto_pad),ISNULL(temp.periodo_gracia_int,mesAnt.periodo_gracia_int),\n" +
                        "ISNULL(temp.tipo, mesAnt.tipo),ISNULL(temp.bolsillos, mesAnt.bolsillos),ISNULL(temp.filtro,mesAnt.filtro),ISNULL(temp.interes,mesAnt.interes),\n" +
                        "ISNULL(temp.prov_interes_ini,mesAnt.prov_interes_ini),ISNULL(temp.prov_gral_int_fm,mesAnt.prov_gral_int_fm), \n" +
                        "ISNULL(temp.califica_7_pda,mesAnt.califica_7_pda),ISNULL(temp.marca,mesAnt.marca),ISNULL(temp.califica_pda,mesAnt.califica_pda),ISNULL(temp.prov_gral_int_100, mesAnt.prov_gral_int_100),\n" +
                        "ISNULL(temp.marca_mrco,mesAnt.marca_mrco),ISNULL(temp.check_int,mesAnt.check_int),ISNULL(temp.saldo_intereses_bolsillo,mesAnt.saldo_intereses_bolsillo),\n" +
                        "ISNULL(temp.intereses_bolsillos,mesAnt.intereses_bolsillos),ISNULL(temp.prov_gral_int,mesAnt.prov_gral_int),ISNULL(temp.check_bolsillo,mesAnt.check_bolsillo),\n" +
                        "ISNULL(temp.check_prov_int,mesAnt.check_prov_int),ISNULL(temp.tp_fidei,mesAnt.tp_fidei),ISNULL(temp.nit_fidei,mesAnt.nit_fidei),ISNULL(temp.tp_def,mesAnt.tp_def),\n" +
                        "ISNULL(temp.nit_def,mesAnt.nit_def),ISNULL(temp.ciiu,mesAnt.ciiu),ISNULL(temp.nombre_2,mesAnt.nombre_2),ISNULL(temp.corazu, mesAnt.corazu),ISNULL(temp.sector,mesAnt.sector),\n" +
                        "ISNULL(temp.codigo_ifrs9,mesAnt.codigo_ifrs9),ISNULL(temp.nit_sin_dv,mesAnt.nit_sin_dv),ISNULL(temp.dv,mesAnt.dv),ISNULL(temp.banca,mesAnt.banca), \n" +
                        "ISNULL(temp.nivel_ventas_2,mesAnt.nivel_ventas_2),ISNULL(temp.periodo,?),\n" +
                        "ISNULL(genBal.empresa,mesAnt.empresa) as empresa, ISNULL(CONVERT(numeric(18,2),temp.prov_gral_int),0) as valor, ISNULL(mesAnt.valor,0) as 'Mes Anterior', CONVERT(numeric(18,2),(ISNULL(temp.prov_gral_int,0) - ISNULL(mesAnt.valor,0))) as 'Diferencia Meses',\n" +
                        "ISNULL(genBal.cuenta,mesAnt.cuenta_balance) 'Cuenta Balance',\n" +
                        "CASE WHEN (ISNULL(temp.prov_gral_int,0) - ISNULL(mesAnt.valor,0)) >= 0 THEN ISNULL(genProvPos.cuenta,mesAnt.cuenta_pyg) ELSE ISNULL(genProvNeg.cuenta,mesAnt.cuenta_pyg) END 'cuenta PYG', \n" +
                        "CASE WHEN ISNULL(temp.prov_gral_int,0) >= 0 THEN ISNULL(genProvPos.cuenta,mesAnt.cuenta_pyg) ELSE ISNULL(genProvNeg.cuenta,mesAnt.cuenta_pyg) END 'cuenta PYG Valor',\n" +
                        "CONCAT(ISNULL(genBal.empresa,mesAnt.empresa),ISNULL(temp.sucursal,mesAnt.sucursal),ISNULL(temp.numero_op,mesAnt.numero_op)),\n" +
                        "'Provision General de Intereses'\n" +
                        "FROM\n" +
                        "(SELECT * FROM nexco_provision_general_interes_temp) as temp\n" +
                        "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'BALANCE' AND gen.fuente_info = 'PROV_022') as genBal  ON temp.cartera = genBal.cartera AND temp.califica = genBal.calificacion AND temp.codigo_ifrs9 = genBal.codigo_ifrs9 \n" +
                        "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='+' AND gen.fuente_info = 'PROV_022') as genProvPos  ON temp.cartera = genProvPos.cartera AND temp.califica = genProvPos.calificacion AND temp.codigo_ifrs9 = genProvPos.codigo_ifrs9 \n" +
                        "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='-' AND gen.fuente_info = 'PROV_022') as genProvNeg  ON temp.cartera = genProvNeg.cartera AND temp.califica = genProvNeg.calificacion AND temp.codigo_ifrs9 = genProvNeg.codigo_ifrs9 \n" +
                        "FULL JOIN (select * FROM nexco_provision_general_interes where periodo = ?) as mesAnt ON CONCAT(genBal.empresa,temp.sucursal,temp.numero_op) = mesAnt.contrato AND mesAnt.nit = temp.nit");
                insertFinal.setParameter(1,period);
                insertFinal.setParameter(2,currentMinusMonth);
                insertFinal.executeUpdate();

            } else {
                validation.clear();
                validation = validateEmpty;
            }
        }

        return validation;
    }

    public ArrayList<String[]> validateEmpty (){
        ArrayList<String[]> toReturn = new ArrayList<>();

        Query validate = entityManager.createNativeQuery("SELECT DISTINCT CASE WHEN genBal.cuenta is null AND (genProvPos.cuenta is null OR genProvNeg.cuenta is null) THEN 'PYG Y BALANCE' ELSE CASE WHEN genBal.cuenta is null THEN 'BALANCE' ELSE 'PYG' END END\n" +
                ",temp.cartera, temp.califica, temp.codigo_ifrs9\n" +
                "FROM\n" +
                "(SELECT * FROM nexco_provision_general_interes_temp) as temp\n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'BALANCE') as genBal  ON temp.cartera = genBal.cartera AND temp.califica = genBal.calificacion AND temp.codigo_ifrs9 = genBal.codigo_ifrs9 \n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='+') as genProvPos  ON temp.cartera = genProvPos.cartera AND temp.califica = genProvPos.calificacion AND temp.codigo_ifrs9 = genProvPos.codigo_ifrs9 \n" +
                "LEFT JOIN (select distinct empresa, cuenta, calificacion, cartera, codigo_ifrs9 from nexco_parametrica_genericas as gen WHERE tp = 'PYG' AND indicador ='-') as genProvNeg  ON temp.cartera = genProvNeg.cartera AND temp.califica = genProvNeg.calificacion AND temp.codigo_ifrs9 = genProvNeg.codigo_ifrs9 \n" +
                "WHERE genBal.cuenta is null OR (genProvPos.cuenta is null OR genProvNeg.cuenta is null)");

        List<Object[]> result = validate.getResultList();

        if(!result.isEmpty()){
            for(Object[] res : result){
                String[] log = new String[5];
                log[0] = res[0].toString();
                log[1] = res[1].toString();
                log[2] = res[2].toString();
                log[3] = res[3].toString();
                log[4] = "Los campos no se encuentran en la parametría de genéricas";
                toReturn.add(log);
            }
        }

        return toReturn;
    }

    public void loadTemporalFile(InputStream file, String periodo){

        Query delete = entityManager.createNativeQuery("TRUNCATE TABLE nexco_provision_general_interes_temp");
        delete.executeUpdate();

        Scanner scanner = new Scanner(file);
        List<GeneralInterestProvisionTemporal> listGp = new ArrayList<>();

        while (scanner.hasNextLine()){
            String line = scanner.nextLine();
            String[] data = line.split(";");

            if(data.length > 0 && !data[0].trim().equals("TP")){
                GeneralInterestProvisionTemporal gp = new GeneralInterestProvisionTemporal();
                gp.setTp(data[0].trim());
                gp.setNit(data[1].trim());
                gp.setTpCar(data[2].trim());
                gp.setSucOblAl(data[3].trim());
                gp.setNumeroOp(data[4].trim());
                gp.setSucursal(StringUtils.leftPad(data[5].trim(),4,"0"));
                gp.setLinSubpro(data[6].trim());
                gp.setCartera(data[7].trim());
                gp.setProdAlta(data[8].trim());
                gp.setAlivios(data[9].trim());
                gp.setAliviosCovid(data[10].trim());
                gp.setCapital(ParseDouble(data[11].trim()));
                gp.setIntereses(ParseDouble(data[12].trim()));
                gp.setVrIntCte(ParseDouble(data[13].trim()));
                gp.setVrMora(ParseDouble(data[14].trim()));
                gp.setCxc(ParseDouble(data[15].trim()));
                gp.setProvisCap(ParseDouble(data[16].trim()));
                gp.setProvisInt(ParseDouble(data[17].trim()));
                gp.setCalifica7Ini(data[18].trim());
                gp.setCalificaIni(data[19].trim());
                gp.setFecVtoI(data[20].trim());
                gp.setFechaIniMora(data[21].trim());
                gp.setLineasSfc(data[22].trim());
                gp.setNuevaCalifica(data[23].trim());
                gp.setNuevaCalifica7(data[24].trim());
                gp.setCalifica(data[25].trim());
                gp.setCalifica7(data[26].trim());
                gp.setAceptado(data[27].trim());
                gp.setAplicado(data[28].trim());
                gp.setEstado(data[29].trim());
                gp.setFechaVctoAlivio(data[30].trim());
                gp.setFechaVctoPad(data[31].trim());
                gp.setPeriodoGraciaInt(data[32].trim());
                gp.setTipo(data[33].trim());
                gp.setBolsillos(data[34].trim());
                gp.setFiltro(data[35].trim());
                gp.setInteres(ParseDouble(data[36].trim()));
                gp.setProvInteresIni(ParseDouble(data[37].trim()));
                gp.setProvGralIntFm(ParseDouble(data[38].trim()));
                gp.setCalifica7Pda(data[39].trim());
                gp.setMarca(data[40].trim());
                gp.setCalificaPda(data[41].trim());
                gp.setProvGralInt100(ParseDouble(data[42].trim()));
                gp.setMarcaMrco(data[43].trim());
                gp.setCheckInt(data[44].trim());
                gp.setSaldoInteresesBolsillo(ParseDouble(data[45].trim()));
                gp.setInteresesBolsillo(ParseDouble(data[46].trim()));
                gp.setProvGralInt(ParseDouble(data[47].trim()));
                gp.setCheckBolsillo(data[48].trim());
                gp.setCheckProvInt(data[49].trim());
                gp.setTpFidei(data[50].trim());
                gp.setNitFidei(data[51].trim());
                gp.setTpDef(data[52].trim());
                gp.setNitDef(data[53].trim());
                gp.setCiiu(data[54].trim());
                gp.setNombre2(data[55].trim());
                gp.setCorazu(data[56].trim());
                gp.setSector(data[57].trim());
                gp.setCodigoIfrs9(data[58].trim());
                gp.setNitSinDv(data[59].trim());
                gp.setDv(data[60].trim());
                gp.setBanca(data[61].trim());
                if(data.length != 63){
                    gp.setNivelVentas2("");
                }else {
                    gp.setNivelVentas2(data[62].trim());
                }
                gp.setPeriodo(periodo);

                listGp.add(gp);

                if (listGp.size() == 100000) {
                    batchInsert(listGp);
                    listGp.clear();
                }
            }
        }
        batchInsert(listGp);
        listGp.clear();
    }

    public double ParseDouble(String number){
        double toReturn;
        String fixedNum = number.replace(",","");
        if(!number.isEmpty()){
            toReturn = Double.parseDouble(fixedNum);
        } else{
            toReturn = 0;
        }

        return toReturn;
    }

    public void batchInsert(List<GeneralInterestProvisionTemporal> temporal) {


        jdbcTemplate.batchUpdate(
                "insert into nexco_provision_general_interes_temp (tp, nit, tp_car, suc_obl_al, numero_op, sucursal, lin_subpro, cartera, prod_alta, alivios, alivios_covid," +
                        "capital,intereses,vr_int_cte,vr_mora,cxc,provis_cap,provis_int,califica_7_ini,califica_ini,fec_vto_i,fecha_ini_mora,lineas_sfc,nueva_califica,nueva_califica_7,califica," +
                        "califica_7,aceptado,aplicado,estado,fecha_vcto_alivio,fecha_vcto_pad,periodo_gracia_int,tipo,bolsillos,filtro,interes,prov_interes_ini,prov_gral_int_fm," +
                        "califica_7_pda,marca,califica_pda,prov_gral_int_100,marca_mrco,check_int,saldo_intereses_bolsillo,intereses_bolsillos,prov_gral_int,check_bolsillo," +
                        "check_prov_int,tp_fidei,nit_fidei,tp_def,nit_def,ciiu,nombre_2,corazu,sector,codigo_ifrs9,nit_sin_dv,dv,banca,nivel_ventas_2,periodo) " +
                        "values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)",
                new BatchPreparedStatementSetter() {

                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setString(1, temporal.get(i).getTp());
                        ps.setString(2, temporal.get(i).getNit());
                        ps.setString(3, temporal.get(i).getTpCar());
                        ps.setString(4, temporal.get(i).getSucOblAl());
                        ps.setString(5, temporal.get(i).getNumeroOp());
                        ps.setString(6, temporal.get(i).getSucursal());
                        ps.setString(7, temporal.get(i).getLinSubpro());
                        ps.setString(8, temporal.get(i).getCartera());
                        ps.setString(9, temporal.get(i).getProdAlta());
                        ps.setString(10, temporal.get(i).getAlivios());
                        ps.setString(11, temporal.get(i).getAliviosCovid());
                        ps.setDouble(12, temporal.get(i).getCapital());
                        ps.setDouble(13, temporal.get(i).getIntereses());
                        ps.setDouble(14, temporal.get(i).getVrIntCte());
                        ps.setDouble(15, temporal.get(i).getVrMora());
                        ps.setDouble(16, temporal.get(i).getCxc());
                        ps.setDouble(17, temporal.get(i).getProvisCap());
                        ps.setDouble(18, temporal.get(i).getProvisInt());
                        ps.setString(19, temporal.get(i).getCalifica7Ini());
                        ps.setString(20, temporal.get(i).getCalificaIni());
                        ps.setString(21, temporal.get(i).getFecVtoI());
                        ps.setString(22, temporal.get(i).getFechaIniMora());
                        ps.setString(23, temporal.get(i).getLineasSfc());
                        ps.setString(24, temporal.get(i).getNuevaCalifica());
                        ps.setString(25, temporal.get(i).getNuevaCalifica7());
                        ps.setString(26, temporal.get(i).getCalifica());
                        ps.setString(27, temporal.get(i).getCalifica7());
                        ps.setString(28, temporal.get(i).getAceptado());
                        ps.setString(29, temporal.get(i).getAplicado());
                        ps.setString(30, temporal.get(i).getEstado());
                        ps.setString(31, temporal.get(i).getFechaVctoAlivio());
                        ps.setString(32, temporal.get(i).getFechaVctoPad());
                        ps.setString(33, temporal.get(i).getPeriodoGraciaInt());
                        ps.setString(34, temporal.get(i).getTipo());
                        ps.setString(35, temporal.get(i).getBolsillos());
                        ps.setString(36, temporal.get(i).getFiltro());
                        ps.setDouble(37, temporal.get(i).getInteres());
                        ps.setDouble(38, temporal.get(i).getProvInteresIni());
                        ps.setDouble(39, temporal.get(i).getProvGralIntFm());
                        ps.setString(40, temporal.get(i).getCalifica7Pda());
                        ps.setString(41, temporal.get(i).getMarca());
                        ps.setString(42, temporal.get(i).getCalificaPda());
                        ps.setDouble(43, temporal.get(i).getProvGralInt100());
                        ps.setString(44, temporal.get(i).getMarcaMrco());
                        ps.setString(45, temporal.get(i).getCheckInt());
                        ps.setDouble(46, temporal.get(i).getSaldoInteresesBolsillo());
                        ps.setDouble(47, temporal.get(i).getInteresesBolsillo());
                        ps.setDouble(48, temporal.get(i).getProvGralInt());
                        ps.setString(49, temporal.get(i).getCheckBolsillo());
                        ps.setString(50, temporal.get(i).getCheckProvInt());
                        ps.setString(51, temporal.get(i).getTpFidei());
                        ps.setString(52, temporal.get(i).getNitFidei());
                        ps.setString(53, temporal.get(i).getTpDef());
                        ps.setString(54, temporal.get(i).getNitDef());
                        ps.setString(55, temporal.get(i).getCiiu());
                        ps.setString(56, temporal.get(i).getNombre2());
                        ps.setString(57, temporal.get(i).getCorazu());
                        ps.setString(58, temporal.get(i).getSector());
                        ps.setString(59, temporal.get(i).getCodigoIfrs9());
                        ps.setString(60, temporal.get(i).getNitSinDv());
                        ps.setString(61, temporal.get(i).getDv());
                        ps.setString(62, temporal.get(i).getBanca());
                        ps.setString(63, temporal.get(i).getNivelVentas2());
                        ps.setString(64, temporal.get(i).getPeriodo());

                    }

                    public int getBatchSize() {
                        return temporal.size();
                    }
                });
    }

    public ArrayList<Object[]> getGeneralInterest(String periodo){
        ArrayList<Object[]> result = new ArrayList<>();

        Query data = entityManager.createNativeQuery("select empresa, valor, mes_anterior, diferencia_meses, cuenta_balance, cuenta_pyg, contrato, fuente_info from nexco_provision_general_interes where periodo = ?");
        data.setParameter(1,periodo);

        if(!data.getResultList().isEmpty()){
            result = (ArrayList<Object[]>) data.getResultList();
        }

        return result;
    }

    public ArrayList<GeneralInterestProvision> getGeneralInterestComplete(String periodo){
        ArrayList<GeneralInterestProvision> result = new ArrayList<>();

        Query data = entityManager.createNativeQuery("select * from nexco_provision_general_interes where periodo = ?", GeneralInterestProvision.class);
        data.setParameter(1,periodo);

        if(!data.getResultList().isEmpty()){
            result = (ArrayList<GeneralInterestProvision>) data.getResultList();
        }

        return result;
    }

    public List<Object[]> generateMassiveCharge(String period) throws ParseException {
        List<Object[]> result = new ArrayList<>();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM");
        Date currentDate = formatter.parse(period);

        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);
        c.add(Calendar.MONTH,-1);
        Date resultAfter  = c.getTime();


        String currentMinusMonth = formatter.format(resultAfter);


        Query getData = entityManager.createNativeQuery("SELECT ISNULL(A.OFINEGOCIO, J.sucursal) sucursalA,cuenta_balance, divisa, contrato, REFERENCIA, valor, DESCRIPCION,PERIODO, TP, numero, dv, TIPOPERDIDA,CLASERIESGO,TIPOMOVIMIENTO,PRODUCTO, PROCESO,LINEAOPERATIVA \n" +
                "FROM\n" +
                "(" +
                "SELECT sucursal,cuenta_balance, 'COP' as divisa, contrato, '' as REFERENCIA, \n" +
                "FORMAT(ROUND(prov.valor*-1,2),'#,##0.00') AS valor, UPPER(prov.cartera+'-'+prov.Califica+'-prov gral inte') AS DESCRIPCION,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as PERIODO, prov.tp_def as TP, prov.nit_def as numero, prov.dv as dv, \n" +
                "' ' as TIPOPERDIDA,' ' as CLASERIESGO,' ' as TIPOMOVIMIENTO,' ' as PRODUCTO,' ' as PROCESO,' ' as LINEAOPERATIVA \n" +
                "FROM nexco_provision_general_interes as prov\n" +
                "WHERE ROUND(prov.valor*-1,2)<>0 AND prov.periodo = :fecha \n" +
                "UNION ALL \n" +
                "SELECT distinct sucursal,cuenta_pyg, 'COP' as divisa, contrato, '' as REFERENCIA, \n" +
                "FORMAT(ROUND(prov.valor,2),'#,##0.00') AS valor, UPPER(prov.cartera+'-'+prov.Califica+'-prov gral inte') AS DESCRIPCION,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as PERIODO, prov.tp_def as TP, prov.nit_def as numero, prov.dv as dv, \n" +
                "' ' TIPOPERDIDA,' ' CLASERIESGO,' 'TIPOMOVIMIENTO,' 'PRODUCTO,' 'PROCESO,' 'LINEAOPERATIVA \n" +
                "FROM nexco_provision_general_interes as prov\n" +
                "WHERE ROUND(prov.valor,2)<>0 AND prov.periodo = :fecha\n" +
                "UNION ALL\n" +
                "SELECT sucursal,cuenta_balance, 'COP' as divisa, contrato, '' as REFERENCIA, \n" +
                "FORMAT(ROUND(prov.valor,2),'#,##0.00') AS valor, 'REVERSION CIRCULAR O22 '+:lastMonth AS DESCRIPCION,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as PERIODO, prov.tp_def as TP, prov.nit_def as numero, prov.dv as dv, \n" +
                "' ' as TIPOPERDIDA,' ' as CLASERIESGO,' ' as TIPOMOVIMIENTO,' ' as PRODUCTO,' ' as PROCESO,' ' as LINEAOPERATIVA \n" +
                "FROM nexco_provision_general_interes as prov\n" +
                "WHERE ROUND(prov.valor,2)<>0 AND prov.periodo = :lastMonth \n" +
                "UNION ALL \n" +
                "SELECT distinct sucursal,cuenta_pyg, 'COP' as divisa, contrato, '' as REFERENCIA, \n" +
                "FORMAT(ROUND(prov.valor*-1,2),'#,##0.00') AS valor, 'REVERSION CIRCULAR O22 '+:lastMonth AS DESCRIPCION,\n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as PERIODO, prov.tp_def as TP, prov.nit_def as numero, prov.dv as dv, \n" +
                "' ' TIPOPERDIDA,' ' CLASERIESGO,' 'TIPOMOVIMIENTO,' 'PRODUCTO,' 'PROCESO,' 'LINEAOPERATIVA \n" +
                "FROM nexco_provision_general_interes as prov\n" +
                "WHERE ROUND(prov.valor*-1,2)<>0 AND prov.periodo = :lastMonth " +
                ")J\n" +
                "LEFT JOIN (SELECT * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.OFI_GRUPO b WHERE b.FECHA_CIERRE is not null and b.FECHA_CIERRE!='' and b.OFINEGOCIO !='0000') A ON J.sucursal= A.OFICINA collate SQL_Latin1_General_CP1_CI_AS ");

        getData.setParameter("fecha", period);
        getData.setParameter("fecha2", period+"%");
        getData.setParameter("lastMonth", currentMinusMonth);

        if(!getData.getResultList().isEmpty()){
            result = getData.getResultList();
        }

        return result;
    }

    public List<Object[]> generateAnexo8(String period){
        List<Object[]> result = new ArrayList<>();

        insertIntoAnexo(period);

        Query getData = entityManager.createNativeQuery("SELECT ISNULL(A.OFINEGOCIO, prov.sucursal) sucursalA,cuenta_balance, 'COP' as divisa,  \n" +
                "FORMAT(SUM(prov.valor*-1),'#,##0.00') AS valor, \n" +
                "CONVERT(VARCHAR,CONVERT(datetime,(SELECT REPLACE(MAX(FechaHabil),'-','/') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0)),103) as 'Fecha Origen',\n" +
                "'' as 'Fecha Cierre',\n" +
                "prov.tp_def as TP, \n" +
                "prov.nit_def as identi, \n" +
                "prov.dv as dv, \n" +
                "SUBSTRING(prov.nombre_2,1,35) AS 'Razon Social',\n" +
                "contrato,\n" +
                "UPPER(prov.cartera+'-'+prov.Califica+'-prov gral inte') AS Observacion,\n" +
                "'' as 'Cuenta Provision',\n" +
                "'' as 'Valor Provision',\n" +
                "'' as 'Importe Original',\n" +
                "'NO'\n" +
                "FROM nexco_provision_general_interes as prov \n" +
                "LEFT JOIN (SELECT * FROM [82.255.50.134].DB_FINAN_NUEVA.dbo.OFI_GRUPO b WHERE b.FECHA_CIERRE is not null and b.FECHA_CIERRE!='' and b.OFINEGOCIO !='0000') A ON prov.sucursal= A.OFICINA collate SQL_Latin1_General_CP1_CI_AS \n" +
                "WHERE ROUND(prov.valor*-1,2)<>0 AND prov.periodo = :fecha \n" +
                "GROUP BY ISNULL(A.OFINEGOCIO, prov.sucursal),cuenta_balance,cartera,califica,contrato,nombre_2,dv,nit_def,tp_def");
        getData.setParameter("fecha", period);
        getData.setParameter("fecha2", period+"%");

        if(!getData.getResultList().isEmpty()){
            result = getData.getResultList();
        }

        return result;
    }

    public void insertIntoAnexo(String period){

        Query delete = entityManager.createNativeQuery("DELETE FROM nexco_anexo_8_prov_gen_int WHERE fecha_origen LIKE ?");
        delete.setParameter(1,period.replace("-","/")+"%");
        delete.executeUpdate();

        Query insert = entityManager.createNativeQuery("INSERT INTO nexco_anexo_8_prov_gen_int(centro,cuenta,divisa,importe,fecha_origen,fecha_cierre,tp,identificacion,dv,razon_social,contrato,observacion,\n" +
                "cuenta_provision,valor_provision,importe_moneda_original, prob_recup)\n" +
                "SELECT sucursal,cuenta_balance, 'COP' as divisa,  \n" +
                "SUM(prov.valor*-1) AS valor, \n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','/') FROM FECHAS_HABILES WHERE FechaHabil LIKE :fecha2 AND DiaHabil <> 0) as 'Fecha Origen',\n" +
                "'' as 'Fecha Cierre',\n" +
                "prov.tp_def as TP, \n" +
                "prov.nit_def as identi, \n" +
                "prov.dv as dv, \n" +
                "SUBSTRING(prov.nombre_2,1,35) AS 'Razon Social',\n" +
                "contrato,\n" +
                "UPPER(prov.cartera+'-'+prov.Califica+'-prov gral inte') AS Observacion,\n" +
                "'' as 'Cuenta Provision',\n" +
                "'' as 'Valor Provision',\n" +
                "'' as 'Importe Original',\n" +
                "'NO'\n" +
                "FROM nexco_provision_general_interes as prov\n" +
                "WHERE ROUND(prov.valor*-1,2)<>0 AND prov.periodo = :fecha \n" +
                "GROUP BY sucursal,cuenta_balance,cartera,califica,contrato,nombre_2,dv,nit_def,tp_def");
        insert.setParameter("fecha", period);
        insert.setParameter("fecha2", period+"%");
        insert.executeUpdate();
    }





}
