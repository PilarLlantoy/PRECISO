package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.CalculoIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ReportIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportIcrvRepository extends JpaRepository<ReportIcrv,Long> {
    List<ReportIcrv> findAll();
    ReportIcrv findByIdReport(Long id);
    void deleteByPeriodo(String periodo);
    List<ReportIcrv> findByEntidadAndPeriodo(String entidad, String periodo);
    List<ReportIcrv> findByCodPeriodoAndPeriodo(String codPeriodo, String periodo);
    List<ReportIcrv> findByCodSociinfoAndPeriodo(String codSociinfo, String periodo);
    List<ReportIcrv> findByXtiCarteraAndPeriodo(String xtiCartera, String periodo);
    List<ReportIcrv> findByCodSocipartAndPeriodo(String codSocipart, String periodo);
    List<ReportIcrv> findByCodIsinAndPeriodo(String codIsin, String periodo);
    List<ReportIcrv> findByCosteValorAndPeriodo(Double costeValor, String periodo);
    List<ReportIcrv> findByAjusteValorRazonableAndPeriodo(Double ajusteValorRazonable, String periodo);
    List<ReportIcrv> findByMicrocoberturasAndPeriodo(Double microcoberturas, String periodo);
    List<ReportIcrv> findByCorreccionesPorDeterioroAndPeriodo(Double correccionesPorDeterioro, String periodo);
    List<ReportIcrv> findByValorCotizadoAndPeriodo(Double valorCotizado, String periodo);
    List<ReportIcrv> findByDesembolsoPdteAndPeriodo(Double desembolsoPdte, String periodo);
    List<ReportIcrv> findByNumTitulosAndPeriodo(Double numTitulos, String periodo);
    List<ReportIcrv> findByCapitalSocialAndPeriodo(Double capitalSocial, String periodo);
    List<ReportIcrv> findByCosteAdquisicionAndPeriodo(Double costeAdquisicion, String periodo);
    List<ReportIcrv> findBySignoValorContableAndPeriodo(String signoValorContable, String periodo);
    List<ReportIcrv> findBySignoMicrocoberturaAndPeriodo(String signoMicrocobertura, String periodo);
    List<ReportIcrv> findByPeriodo(String periodo);
}
