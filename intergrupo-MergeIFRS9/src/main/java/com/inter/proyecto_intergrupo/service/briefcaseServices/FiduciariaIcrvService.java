package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.FiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ValoresIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.FiduciariaIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.ValoresIcrvRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class FiduciariaIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private FiduciariaIcrvRepository fiduciariaIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Fiduciaria ICRV");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public FiduciariaIcrv findByIdFiduciaria(Long id){
        return fiduciariaIcrvRepository.findByIdFiduciaria(id);
    }

    public List<FiduciariaIcrv> findAllByPeriod(String periodo)
    {
        return fiduciariaIcrvRepository.findByPeriodo(periodo);
    }

    public boolean proccessData(User user,String periodo){
        fiduciariaIcrvRepository.deleteByPeriodo(periodo);
        Query query = entityManager.createNativeQuery("insert into nexco_fiduciaria_icrv (descripcion, periodo) select a.corte, a.periodo from nexco_balfiduciaria_icrv  a where a.periodo = :periodo   group by a.corte , a.periodo;\n" +
                "update a set a.capital_autorizado = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Capital Autorizado') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.capital_por_suscribir = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Capital Por Suscribir') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.reserva_legal = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Reserva Legal') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.apropiacion_de_utilidades = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Apropiación De Utilidades') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.readquisicion_de_acciones = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Para Readquisicion De Acciones') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.acciones_propias_readquiridas = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Acciones Propias Readquiridas') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.instrumentos_financieros_valor_razonable = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Instrumentos Financieros Valor Razonab') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.instrumentos_financieros_valor_razonable_cambios_ori = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Instrumentos Financieros Valor Razonable Cambios ORI') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.titulos_de_tesoreria = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Titulos De Tesoreria') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.impto_diferido_valor_inv_disponible_vta = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Impto Diferido X Dif En Valor Inv Disponible Vta') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.resultado_ejercicios_anteriores = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Resultado De Ejercicios Anteriores') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.resultado_ejercicios_anteriores2 = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Resultado De Ejercicios Anteriores 2') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update a set a.resultados_del_ejercicio = b.valor from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select periodo,corte,sum(saldo_final)as valor from nexco_balfiduciaria_icrv  where periodo = :periodo   and codigo_cuenta_niif = (select valor2 from nexco_param_icrv where categoria = 'Resultados Del Ejercicio') group by periodo,corte) b where a.periodo = b.periodo and a.descripcion = b.corte;\n" +
                "update nexco_fiduciaria_icrv set patrimonio_total = isnull(capital_autorizado,0) + isnull(capital_por_suscribir,0) + isnull(reserva_legal,0) + isnull(apropiacion_de_utilidades,0) + isnull(readquisicion_de_acciones,0) + isnull(acciones_propias_readquiridas,0) + isnull(instrumentos_financieros_valor_razonable,0) + isnull(instrumentos_financieros_valor_razonable_cambios_ori,0) + isnull(titulos_de_tesoreria,0) + isnull(impto_diferido_valor_inv_disponible_vta,0) + isnull(resultado_ejercicios_anteriores,0) + isnull(resultado_ejercicios_anteriores2,0) + isnull(resultados_del_ejercicio,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set ori = isnull(instrumentos_financieros_valor_razonable,0) + isnull(instrumentos_financieros_valor_razonable_cambios_ori,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set patrimonio_sin_ori = isnull(patrimonio_total,0) + isnull(ori,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set k_reservas = isnull(capital_autorizado,0) + isnull(capital_por_suscribir,0) + isnull(reserva_legal,0) + isnull(apropiacion_de_utilidades,0) + isnull(readquisicion_de_acciones,0) + isnull(acciones_propias_readquiridas,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set otras = isnull(resultado_ejercicios_anteriores,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set ori_calculado = (isnull(ori,0))* (select valor2 from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set variacion_ori_calculdo = isnull(isnull(ori_calculado,0) - (select isnull(t2.ori_calculado,0) from nexco_fiduciaria_icrv t2 where t2.periodo = :periodo   and t2.id_fiduciaria = nexco_fiduciaria_icrv.id_fiduciaria -1),0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set pyg_calculado = (isnull(resultados_del_ejercicio,0))* (select valor2 from nexco_param_icrv where categoria = 'Por Valores')  where periodo = :periodo  ;\n" +
                "update a set a.saldo_ori_contable = b.salmes *-1 from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select * from nexco_query_icrv where nucta in ('382005001','382005771')) b where a.descripcion = b.fecont_icrv;\n" +
                "update nexco_fiduciaria_icrv set variacion_ori_registrado = isnull(isnull(saldo_ori_contable,0) - (select isnull(t2.saldo_ori_contable,0) from nexco_fiduciaria_icrv t2 where t2.periodo = :periodo   and t2.id_fiduciaria = nexco_fiduciaria_icrv.id_fiduciaria -1),0) where periodo = :periodo  ;\n" +
                "update a set a.pyg = b.salmes * -1 from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select * from nexco_query_icrv where nucta in ('415005775')) b where a.descripcion = b.fecont_icrv;\n" +
                "update nexco_fiduciaria_icrv set dif_pyg_ok = isnull(pyg_calculado,0) + isnull(pyg,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set dif_ori_ok = isnull(variacion_ori_calculdo,0) + isnull(variacion_ori_registrado,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set borrar = isnull(ori_calculado,0) + isnull(saldo_ori_contable,0) where periodo = :periodo  ;\n" +
                "update a set a.nominal_001 = b.salmes from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select * from nexco_query_icrv where nucta in ('131505001')) b where a.descripcion = b.fecont_icrv;\n" +
                "update a set a.nominal_771 = b.salmes from (select * from nexco_fiduciaria_icrv where periodo = :periodo  ) a, (select * from nexco_query_icrv where nucta in ('131505771')) b where a.descripcion = b.fecont_icrv;\n" +
                "update nexco_fiduciaria_icrv set nominal_1315 = (isnull(patrimonio_total,0)*-1) * (select valor2 from nexco_param_icrv where categoria = 'Por Valores') - isnull(nominal_001,0) - isnull(nominal_771,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set pyg_t = isnull(pyg_calculado,0) + isnull(pyg,0) where periodo = :periodo  ;\n" +
                "update nexco_fiduciaria_icrv set ori_t = (isnull(nominal_1315,0) + isnull(pyg_t,0))*-1 where periodo = :periodo  ; ");
        query.setParameter("periodo",periodo);
        query.executeUpdate();
        if(findAllByPeriod(periodo).isEmpty()) {
            loadAudit(user, "Generación de Resumen Fiduciaria fallido.");
            return false;
        }
        else {
            loadAudit(user, "Generación de Resumen Fiduciaria exitoso.");
            return true;
        }
    }

}


