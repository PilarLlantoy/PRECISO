package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Ciiu;
import com.inter.proyecto_intergrupo.model.parametric.TypeTemplate;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.CiiuRepository;
import com.inter.proyecto_intergrupo.repository.parametric.TypeTemplateRepository;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class TypeTemplateService {


    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private final TypeTemplateRepository typeTemplateRepository;

    public TypeTemplateService(TypeTemplateRepository typeTemplateRepository) {
        this.typeTemplateRepository = typeTemplateRepository;
    }

    public List<TypeTemplate> findAll(){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_plantilla_esp as em ", TypeTemplate.class);
        return query.getResultList();
    }

    public List<TypeTemplate> findTypebyId(Integer id){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_plantilla_esp as em " +
                "WHERE em.id = ?",TypeTemplate.class);

        query.setParameter(1, id);
        return query.getResultList();
    }


    public void modifyType(TypeTemplate toModify,Integer id){
        TypeTemplate toInsert = new TypeTemplate();
        toInsert.setNombreArchivo(toModify.getNombreArchivo());
        toInsert.setTipoProceso(toModify.getTipoProceso());
        toInsert.setDescripcion(toModify.getDescripcion());
        toInsert.setTipoAsiento(toModify.getTipoAsiento());
        toInsert.setReferencia(toModify.getReferencia());
        Query query = entityManager.createNativeQuery("UPDATE nexco_tipo_plantilla_esp SET nombre_archivo = ? , tipo_proceso = ?, descripcion = ?, tipo_asiento = ?, referencia = ? " +
                "WHERE id = ? ", TypeTemplate.class);
        query.setParameter(1,toInsert.getNombreArchivo());
        query.setParameter(2,toInsert.getTipoProceso());
        query.setParameter(3, toInsert.getDescripcion());
        query.setParameter(4, toInsert.getTipoAsiento());
        query.setParameter(5, toInsert.getReferencia());
        query.setParameter(6, id );
        try {
            query.executeUpdate();
        }catch(Exception e){

        }
    }

    public void saveType(TypeTemplate type){
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_tipo_plantilla_esp (nombre_archivo,tipo_proceso,descripcion, tipo_asiento, referencia) VALUES (?,?,?,?,?)", TypeTemplate.class);
        query.setParameter(1, type.getNombreArchivo());
        query.setParameter(2, type.getTipoProceso());
        query.setParameter(3, type.getDescripcion() );
        query.setParameter(4, type.getTipoAsiento() );
        query.setParameter(5, type.getReferencia() );
        query.executeUpdate();
    }

    public void removeType(Integer id){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_tipo_plantilla_esp WHERE id = ? ", TypeTemplate.class);
        query.setParameter(1, id);
        query.executeUpdate();
    }

    public void clearType(User user){
        Query query = entityManager.createNativeQuery("DELETE FROM nexco_tipo_plantilla_esp", TypeTemplate.class);
        query.executeUpdate();
    }

    public Page<TypeTemplate> getAll(Pageable pageable){
        List<TypeTemplate> list = findAll();
        int start = (int)pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());
        Page<TypeTemplate> pageType = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return pageType;
    }

    public List<TypeTemplate> findByFilter(String value, String filter) {
        List<TypeTemplate> list=new ArrayList<TypeTemplate>();
        switch (filter)
        {
            case "Nombre Archivo":
                Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_plantilla_esp as em " +
                        "WHERE em.nombre_archivo LIKE ?", TypeTemplate.class);
                query.setParameter(1, value );

                list= query.getResultList();

                break;
            case "Tipo Proceso":
                Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_tipo_plantilla_esp as em " +
                        "WHERE em.tipo_proceso LIKE ?", TypeTemplate.class);
                query0.setParameter(1, value);

                list= query0.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}