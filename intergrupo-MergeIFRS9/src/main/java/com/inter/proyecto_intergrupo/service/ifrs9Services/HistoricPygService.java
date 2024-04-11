package com.inter.proyecto_intergrupo.service.ifrs9Services;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.ParametersPYG;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9Intergroup;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class HistoricPygService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    public List<PlainIFRS9> getPlainIFRS9Impuestos(String periodo){
        Query getQuery = entityManager.createNativeQuery("select * from nexco_plano_ifrs9_neocon where /*periodo = ? and*/ tipo_registro IN ('IMPUE','PYG','PYG REPOS') order by periodo desc,tipo_registro, codicons, divisa", PlainIFRS9.class);
        //getQuery.setParameter(1, periodo);
        return getQuery.getResultList();
    }

    public List<Object[]> getValuesImpu(String periodo){

        Query validate = entityManager.createNativeQuery(
                "select top 1 * from nexco_plano_ifrs9_neocon where periodo = ? AND tipo_registro = 'IMPUE';");
        validate.setParameter(1,periodo);
        List<Object[]> validateData = validate.getResultList();
        return validateData;

    }

    public List<ParametersPYG> getParam(){

        Query validate = entityManager.createNativeQuery(
                "select top 1 * from nexco_parametros_pyg",ParametersPYG.class);

        return validate.getResultList();

    }

    public boolean setDataParameter(String centro, String tercero, String cuentaD ,String cuentaH,String codiconsD,String codicons,String tpD,String dvD){

        try{
            Query validate1 = entityManager.createNativeQuery("select nucta from cuentas_puc where nucta in (?,?) group by nucta");
            validate1.setParameter(1, cuentaD);
            validate1.setParameter(2, cuentaH);
            if(validate1.getResultList().size()==2)
            {
                Query validate = entityManager.createNativeQuery("UPDATE nexco_parametros_pyg\n" +
                        "SET centro = ? ,  tercero = ? , cuenta_local_pasivo = ? , cuenta_local_pyg = ? , cuenta_espana_pasivo = ? , cuenta_espana_pyg = ? , tp = ? , dv = ?");
                validate.setParameter(1, centro);
                validate.setParameter(2, tercero);
                validate.setParameter(3, cuentaD);
                validate.setParameter(4, cuentaH);
                validate.setParameter(5, codiconsD);
                validate.setParameter(6, codicons);
                validate.setParameter(7, tpD);
                validate.setParameter(8, dvD);
                validate.executeUpdate();
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<Object[]> getPlain(String period, String type, boolean reversionAplica) throws ParseException {

        List<Object[]> listaFinal = new ArrayList<>();
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM");
        Date dataFormateada = formato.parse(period);
        Calendar c = Calendar.getInstance();
        c.setTime(dataFormateada);
        Calendar t = Calendar.getInstance();
        t.setTime(dataFormateada);
        t.add(Calendar.MONTH,2);
        String periodBefore = "0";
        String periodAfter = "0";
        if(reversionAplica)
        {
            periodAfter = t.get(Calendar.YEAR) + "-" + t.get(Calendar.MONTH);
            if (t.get(Calendar.MONTH) == 0)
                periodAfter = (t.get(Calendar.YEAR) - 1) + "-12";
            else if (t.get(Calendar.MONTH) < 10)
                periodAfter = t.get(Calendar.YEAR) + "-0" + t.get(Calendar.MONTH);
        }

        Query plain1 = entityManager.createNativeQuery("select case when sociedad is null then '00548' else sociedad end sociedadA\n" +
                ", tipocons tipoA\n" +
                ", replicate(' ', 9 - LEN(isnull(tipo_asiento, '')))+isnull(tipo_asiento, '') tipo_asientoA\n" +
                ", isnull(descripcion, '') + REPLICATE(' ', 2000 - LEN(isnull(descripcion, ''))) descripcionA\n" +
                ", movimiento mA\n" +
                ", '' rA\n" +
                ", modo_ejecucion meA\n" +
                ", usuario uA\n" +
                ", codicons cA\n" +
                ", naturaleza_total ntA\n" +
                ", saldo *1000\n" +
                ", divisa dA\n" +
                ", pais_negocio pA\n" +
                ", '   ' cod_desgloseA\n" +
                ", case when intergrupo is null then '     ' else intergrupo end planoA, \n" +
                "(SELECT REPLACE(MAX(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE ? AND DiaHabil <> 0) per \n" +
                ", periodo, " +
                "  (SELECT top 1 ISNULL(aplica_reversion,0) FROM nexco_parametros_pyg) reversionP\n" +
                ", (SELECT top 1 ISNULL(centro,'Sin Parametrizar') FROM nexco_parametros_pyg) centroP\n" +
                ", (SELECT top 1 ISNULL(tp,'Sin Parametrizar') FROM nexco_parametros_pyg) tpP\n" +
                ", (SELECT top 1 ISNULL(tercero,'Sin Parametrizar') FROM nexco_parametros_pyg) terceroP\n" +
                ", (SELECT top 1 ISNULL(dv,'Sin Parametrizar') FROM nexco_parametros_pyg) dvP\n" +
                ", (SELECT top 1 ISNULL(cuenta_espana_pasivo,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaEspaPasivoP\n" +
                ", (SELECT top 1 ISNULL(cuenta_local_pasivo,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaLocalPasivoP\n" +
                ", (SELECT top 1 ISNULL(cuenta_espana_pyg,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaEspaPygP\n" +
                ", (SELECT top 1 ISNULL(cuenta_local_pyg,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaLocalPygP " +
                "from nexco_plano_ifrs9_neocon\n" +
                "where periodo IN (?) \n" +
                "and  round(abs(saldo), 0) <> 0  \n" +
                "and tipo_registro IN ("+type+")");
        plain1.setParameter(1,period+"%");
        plain1.setParameter(2,period);
        listaFinal.addAll(plain1.getResultList());

        Query plain = entityManager.createNativeQuery("select case when sociedad is null then '00548' else sociedad end sociedadA\n" +
                ", tipocons tipoA\n" +
                ", replicate(' ', 9 - LEN(isnull(tipo_asiento, '')))+isnull(tipo_asiento, '') tipo_asientoA\n" +
                ", isnull(descripcion, '') + REPLICATE(' ', 2000 - LEN(isnull(descripcion, ''))) descripcionA\n" +
                ", movimiento mA\n" +
                ", '' rA\n" +
                ", modo_ejecucion meA\n" +
                ", usuario uA\n" +
                ", codicons cA\n" +
                ", naturaleza_total ntA\n" +
                ", saldo *-1 *1000 \n" +
                ", divisa dA\n" +
                ", pais_negocio pA\n" +
                ", '   ' cod_desgloseA\n" +
                ", case when intergrupo is null then '     ' else intergrupo end planoA, \n" +
                "(SELECT REPLACE(MIN(FechaHabil),'-','') FROM FECHAS_HABILES WHERE FechaHabil LIKE ? AND DiaHabil <> 0) per \n" +
                ", periodo, " +
                "  (SELECT top 1 ISNULL(aplica_reversion,0) FROM nexco_parametros_pyg) reversionP\n" +
                ", (SELECT top 1 ISNULL(centro,'Sin Parametrizar') FROM nexco_parametros_pyg) centroP\n" +
                ", (SELECT top 1 ISNULL(tp,'Sin Parametrizar') FROM nexco_parametros_pyg) tpP\n" +
                ", (SELECT top 1 ISNULL(tercero,'Sin Parametrizar') FROM nexco_parametros_pyg) terceroP\n" +
                ", (SELECT top 1 ISNULL(dv,'Sin Parametrizar') FROM nexco_parametros_pyg) dvP\n" +
                ", (SELECT top 1 ISNULL(cuenta_espana_pasivo,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaEspaPasivoP\n" +
                ", (SELECT top 1 ISNULL(cuenta_local_pasivo,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaLocalPasivoP\n" +
                ", (SELECT top 1 ISNULL(cuenta_espana_pyg,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaEspaPygP\n" +
                ", (SELECT top 1 ISNULL(cuenta_local_pyg,'Sin Parametrizar') FROM nexco_parametros_pyg) cuentaLocalPygP " +
                "from nexco_plano_ifrs9_neocon\n" +
                "where periodo IN (?) \n" +
                "and  round(abs(saldo), 0) <> 0  \n" +
                "and tipo_registro IN ("+type+")");
        plain.setParameter(1,periodAfter+"%");
        plain.setParameter(2,period);

        if(!periodAfter.equals("0"))
            listaFinal.addAll(plain.getResultList());

        return listaFinal;
    }

}
