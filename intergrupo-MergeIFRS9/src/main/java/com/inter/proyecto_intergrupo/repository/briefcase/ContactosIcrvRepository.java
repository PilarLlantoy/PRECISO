package com.inter.proyecto_intergrupo.repository.briefcase;

import com.inter.proyecto_intergrupo.model.briefcase.BaseIcrv;
import com.inter.proyecto_intergrupo.model.briefcase.ContactosIcrv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContactosIcrvRepository extends JpaRepository<ContactosIcrv,Long> {
    List<ContactosIcrv> findAll();
    ContactosIcrv findByIdContacto(Long idContacto);
    void deleteByIdContacto(Long idContacto);
    List<ContactosIcrv> findByProcesoContainingIgnoreCase(String proceso);
    List<ContactosIcrv> findByNombreContainingIgnoreCase(String nombre);
    List<ContactosIcrv> findByEmpresaContainingIgnoreCase(String empresa);
    List<ContactosIcrv> findByCorreoPrincipalContainingIgnoreCase(String correoPrincipal);
    List<ContactosIcrv> findByCorreoSecundarioContainingIgnoreCase(String correoSecundario);
    List<ContactosIcrv> findBySuperiorContainingIgnoreCase(String superior);
    List<ContactosIcrv> findBySuperior1ContainingIgnoreCase(String superior1);
    List<ContactosIcrv> findByExtensionContainingIgnoreCase(String extension);
    List<ContactosIcrv> findByPaginaContainingIgnoreCase(String pagina);
}
