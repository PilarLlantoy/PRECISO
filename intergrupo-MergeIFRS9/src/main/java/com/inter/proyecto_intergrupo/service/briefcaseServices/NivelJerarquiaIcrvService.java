package com.inter.proyecto_intergrupo.service.briefcaseServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.briefcase.FiduciariaIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.NivelJerarquiaIcrv;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.FiduciariaIcrvRepository;
import com.inter.proyecto_intergrupo.repository.briefcase.NivelJerarquiaIcrvRepository;
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
public class NivelJerarquiaIcrvService {

    @Autowired
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private NivelJerarquiaIcrvRepository nivelJerarquiaIcrvRepository;

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Portafolio");
        insert.setFecha(today);
        insert.setInput("Nivel Jerarquia ICRV");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public NivelJerarquiaIcrv findByIdNivel(Long id){
        return nivelJerarquiaIcrvRepository.findByIdNivel(id);
    }

    public List<NivelJerarquiaIcrv> findAllByPeriod(String periodo)
    {
        return nivelJerarquiaIcrvRepository.findByPeriodo(periodo);
    }

    public boolean proccessData(User user,String periodo){

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate firstDayOfMonth = LocalDate.parse(periodo + "-01", formatter);
        LocalDate lastDayOfMonth = firstDayOfMonth.withDayOfMonth(firstDayOfMonth.lengthOfMonth());
        String ultimoDiaDelMes = lastDayOfMonth.format(formatter);

        nivelJerarquiaIcrvRepository.deleteByPeriodo(periodo);
        Query query = entityManager.createNativeQuery("insert into nexco_nivel_jerarquia_icrv (cuenta_contable_inversion,cuenta_contable_valorizacion,empresa,fecha_de_adquision,periodo,nit,isin,porcentaje_participacion,acciones_en_circulacion,acciones_que_posee_bbva,capital,val_nominal_accion,no_acciones,saldo_inversion,saldo_valoracion,vr_intrinseco_de_la_accion,corte,corte_de_eeff,metodo_de_valoracion) \n" +
                "select b.cuenta as c1, coalesce(c.cuenta,d.cuenta) as c2,a.empresa,b.fecha_adquisicion, :periodo  as periodo,e.nit,e.isin,e.participacion,f.aciones_circulacion,e.numero_acciones,e.capital,e.vr_accion,e.numero_acciones,g.salmes,coalesce(h.salmes,i.salmes) as saldo_valoracion, f.vr_intrinseco, :periodoCorte  as corte, month(f.fecha_actualizacion) as corte_de_eeff, case f.metodo when 'Mercado' then 'VALOR RAZONABLE' when 'Valor Intrinseco' then 'MÉTODO DE VARIACIÓN PATRIMONIAL' when 'MPP' then 'MÉTODO DE PARTICIPACIÓN PÁTRIMONIAL' end as mdv from\n" +
                "(select empresa from nexco_base_icrv where cuenta like '13%' and codicons is not null and codicons != '' and evento = 'INV' and concepto in ('NOMINAL','VALORACIÓN','ATENEA VALORIZACIÓN')  group by empresa) a\n" +
                "left join (select cuenta,empresa,fecha_adquisicion from nexco_base_icrv where cuenta like '13%' and codicons is not null and codicons != '' and evento = 'INV' and concepto in ('NOMINAL')) b on a.empresa = b.empresa\n" +
                "left join (select cuenta,empresa,fecha_adquisicion from nexco_base_icrv where cuenta like '13%' and codicons is not null and codicons != '' and evento = 'INV' and concepto in ('VALORACIÓN')) c on a.empresa = c.empresa\n" +
                "left join (select cuenta,empresa,fecha_adquisicion from nexco_base_icrv where cuenta like '13%' and codicons is not null and codicons != '' and evento = 'INV' and concepto in ('ATENEA VALORIZACIÓN')) d on a.empresa = d.empresa\n" +
                "inner join (select empresa,nit,isin,participacion,sum(no_acciones) as numero_acciones,sum(capital) as capital, sum(vr_accion) as vr_accion from nexco_calculo_icrv where periodo = :periodo  group by empresa,nit,isin,participacion) e on a.empresa = e.empresa\n" +
                "left join (select empresa,fecha_actualizacion,metodo,sum(acciones) as aciones_circulacion, sum(vr_intrinseco) as vr_intrinseco from nexco_precio_icrv where periodo = :periodo  group by empresa,fecha_actualizacion,metodo) f on a.empresa = f.empresa\n" +
                "left join (select nucta,sum(salmes) as salmes from nexco_query_icrv where fecont_icrv like :periodoCorte  group by nucta) g on b.cuenta = g.nucta\n" +
                "left join (select nucta,sum(salmes) as salmes from nexco_query_icrv where fecont_icrv like :periodoCorte  group by nucta) h on c.cuenta = h.nucta\n" +
                "left join (select nucta,sum(salmes) as salmes from nexco_query_icrv where fecont_icrv like :periodoCorte  group by nucta) i on d.cuenta = i.nucta;\n" +
                "\n" +
                "update nexco_nivel_jerarquia_icrv set nominal = isnull(val_nominal_accion,0)*isnull(acciones_que_posee_bbva,0),vr_mercado_inver = isnull(saldo_inversion,0)+isnull(saldo_valoracion,0),vr_mercado_inver2 = isnull(saldo_inversion,0)+isnull(saldo_valoracion,0),\n" +
                "corte_de_eeff = CASE corte_de_eeff WHEN '1' THEN 'ENERO' WHEN '2' THEN 'FEBRERO' WHEN '3' THEN 'MARZO' WHEN '4' THEN 'ABRIL' WHEN '5' THEN 'MAYO' WHEN '6' THEN 'JUNIO' WHEN '7' THEN 'JULIO' WHEN '8' THEN 'AGOSTO' WHEN '9' THEN 'SEPTIEMBRE' WHEN '10' THEN 'OCTUBRE' \n" +
                "WHEN '11' THEN 'NOVIEMBRE' WHEN '12' THEN 'DICIEMBRE' ELSE corte_de_eeff END where periodo = :periodo ;");
        query.setParameter("periodo",periodo);
        query.setParameter("periodoCorte",ultimoDiaDelMes);
        query.executeUpdate();

        if(findAllByPeriod(periodo).isEmpty()) {
            loadAudit(user, "Generación de Nivel Jerarquia fallido.");
            return false;
        }
        else {
            loadAudit(user, "Generación de Nivel Jerarquia exitoso.");
            return true;
        }
    }

}


