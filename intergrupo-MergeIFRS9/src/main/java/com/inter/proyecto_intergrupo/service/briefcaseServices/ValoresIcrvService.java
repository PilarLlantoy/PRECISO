package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.BalvaloresIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ValoresIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.BalvaloresIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.ValoresIcrvRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;


@Service
@Transactional
public class ValoresIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private ValoresIcrvRepository valoresIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Valores ICRV");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public ValoresIcrv findByIdValores(Long id){
        return valoresIcrvRepository.findByIdValores(id);
    }

    public List<ValoresIcrv> findAllByPeriod(String periodo)
    {
        return valoresIcrvRepository.findByPeriodo(periodo);
    }

    public boolean proccessData(User user,String periodo){
        valoresIcrvRepository.deleteByPeriodo(periodo);
        Query query = entityManager.createNativeQuery("delete from nexco_valores_icrv where periodo = :periodo ;\n" +
                "insert into nexco_valores_icrv (descripcion, periodo) select a.mes, a.periodo from nexco_balvalores_icrv  a where a.periodo = :periodo  group by a.mes , a.periodo;\n" +
                "update a set a.capital_autorizado = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Capital Autorizado') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.capital_por_suscribir = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Capital Por Suscribir') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.reserva_legal = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Reserva Legal') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.reservas_ocasionales = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Reservas Ocacionales') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.acciones_bvc_voluntarias = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Acciones BVC Voluntarias') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.pa_fab_asobolsa = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'PA FAB Asobolsa') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.acciones_bvc_obligatorias = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Acciones BVC Obligatorias') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.impuesto_diferido = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Impuesto Diferido') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.provision_de_cartera = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Provision De Cartera') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.revalorizacion_del_patrimonio = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Revalorizacio Del Patrimonio') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.utilidades_acumuladas_ea = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Utilidades Aacumuladas EA') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.perdidas_acumuladas_ea = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Perdidas Acumuladas EA') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.utilidad_del_ejercicio = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Utilidad Del Ejercicio') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update a set a.perdida_del_ejercicio = b.valor from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select periodo,mes,sum(moneda_total)as valor from nexco_balvalores_icrv  where periodo = :periodo  and cuenta_niif = (select valor from nexco_param_icrv where categoria = 'Perdida Del Ejercicio') group by periodo,mes) b where a.periodo = b.periodo and a.descripcion = b.mes;\n" +
                "update nexco_valores_icrv set patrimonio_total = isnull(capital_autorizado,0) + isnull(capital_por_suscribir,0) + isnull(reserva_legal,0) + isnull(reservas_ocasionales,0) + isnull(acciones_bvc_voluntarias,0) + isnull(pa_fab_asobolsa,0) + isnull(acciones_bvc_obligatorias,0) + isnull(impuesto_diferido,0) + isnull(provision_de_cartera,0) + isnull(revalorizacion_del_patrimonio,0) + isnull(utilidades_acumuladas_ea,0) + isnull(perdidas_acumuladas_ea,0) + isnull(utilidad_del_ejercicio,0) + isnull(perdida_del_ejercicio,0) where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set ori = isnull(acciones_bvc_voluntarias,0) + isnull(pa_fab_asobolsa,0) + isnull(acciones_bvc_obligatorias,0) + isnull(impuesto_diferido,0) + isnull(provision_de_cartera,0) where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set patrimonio_sin_ori = isnull(patrimonio_total,0) - isnull(ori,0) where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set krl = (isnull(capital_autorizado,0) + isnull(capital_por_suscribir,0) + isnull(reserva_legal,0))* (select valor from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set perdidas_acumuladas_ea_t = (isnull(revalorizacion_del_patrimonio,0) + isnull(utilidades_acumuladas_ea,0) + isnull(perdidas_acumuladas_ea,0))* (select valor from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set variacion_otras = isnull(isnull(perdidas_acumuladas_ea_t,0) - (select isnull(t2.perdidas_acumuladas_ea_t,0) from nexco_valores_icrv t2 where t2.periodo = :periodo  and t2.id_valores = nexco_valores_icrv.id_valores -1),0) where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set ori_calculado = isnull(ori,0) * (select valor from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set variacion_ori_calculdo = isnull(isnull(ori_calculado,0) - (select isnull(t2.ori_calculado,0) from nexco_valores_icrv t2 where t2.periodo = :periodo  and t2.id_valores = nexco_valores_icrv.id_valores -1),0) where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set pyg_calculado = (isnull(utilidad_del_ejercicio,0) + isnull(perdida_del_ejercicio,0))* (select valor from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set inversion = (isnull(ori_calculado,0) + isnull(pyg_calculado,0)) *-1 where periodo = :periodo ;\n" +
                "update a set a.saldo_ori_contable = b.salmes from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select * from nexco_query_icrv where nucta in ('382005772','382005002')) b where a.descripcion = b.fecont_icrv;\n" +
                "update nexco_valores_icrv set variacion_ori_registrado = isnull(isnull(saldo_ori_contable,0) - (select isnull(t2.saldo_ori_contable,0) from nexco_valores_icrv t2 where t2.periodo = :periodo  and t2.id_valores = nexco_valores_icrv.id_valores -1),0) where periodo = :periodo ;\n" +
                "update a set a.pyg = b.salmes from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select * from nexco_query_icrv where nucta in ('514195771','415005776')) b where a.descripcion = b.fecont_icrv;\n" +
                "update nexco_valores_icrv set dif_pyg_ok = (isnull(pyg_calculado,0) - isnull(pyg,0))  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set dif_ori_ok = (isnull(variacion_ori_calculdo,0) - isnull(variacion_ori_registrado,0))  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set borrar = (isnull(ori_calculado,0) - isnull(saldo_ori_contable,0))  where periodo = :periodo ;\n" +
                "update a set a.nominal_002 = b.salmes from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select * from nexco_query_icrv where nucta in ('131505002')) b where a.descripcion = b.fecont_icrv;\n" +
                "update a set a.nominal_772 = b.salmes from (select * from nexco_valores_icrv where periodo = :periodo ) a, (select * from nexco_query_icrv where nucta in ('131505772')) b where a.descripcion = b.fecont_icrv;\n" +
                "update nexco_valores_icrv set variacion_inv = isnull(isnull(nominal_772,0) - (select isnull(t2.nominal_772,0) from nexco_valores_icrv t2 where t2.periodo = :periodo  and t2.id_valores = nexco_valores_icrv.id_valores -1),0) where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set nominal_var_inv = (isnull(nominal_002,0) + isnull(variacion_inv,0))  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set patrimonio_total_porcentaje = isnull(patrimonio_total,0)* (select valor from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set diferencia = isnull(nominal_002,0) + isnull(nominal_772,0)+isnull(patrimonio_total_porcentaje,0)  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set nominal_1315 = isnull(patrimonio_total,0) * (select valor from nexco_param_icrv where categoria = 'Por Valores') - isnull(nominal_772,0) - isnull(nominal_002,0)  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set pyg_t = (isnull(utilidad_del_ejercicio,0) + isnull(perdida_del_ejercicio,0)) * (select valor from nexco_param_icrv where categoria = 'Por Valores') - isnull(pyg,0)  where periodo = :periodo ;\n" +
                "update nexco_valores_icrv set ori_t = (isnull(nominal_1315,0) - isnull(pyg_t,0))  where periodo = :periodo ;");
        query.setParameter("periodo",periodo);
        query.executeUpdate();
        if(findAllByPeriod(periodo).isEmpty()) {
            loadAudit(user, "Generación de Resumen Valores fallido.");
            return false;
        }
        else {
            loadAudit(user, "Generación de Resumen Valores exitoso.");
            return true;
        }
    }

}


