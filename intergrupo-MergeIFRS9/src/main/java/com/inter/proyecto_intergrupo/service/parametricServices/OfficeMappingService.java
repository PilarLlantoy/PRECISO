package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.io.InputStream;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional
public class OfficeMappingService {

    @Autowired
    private OfficeMappingRepository officeMappingRepository;
    @Autowired
    private ParamOfficeMappingRepository paramOfficeMappingRepository;

    @Autowired
    private CampoRCRepository campoRCRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PersistenceContext
    EntityManager entityManager;
    @Autowired
    private AuditRepository auditRepository;

    public List<OfficeMapping> findAll(){return officeMappingRepository.findAll();}

    public List<Object[]> findAccountRoute(int id){
        Query query = entityManager.createNativeQuery("select id_campo,nombre from preciso_campos_rc where id_rc = ? ");
        query.setParameter(1,id);
        return query.getResultList();
    }

    public void saveParam(String rc, String campoCentroOrigen, String campoNombreCentro, String campoCentroDestino){
         ParamOfficeMapping param = new ParamOfficeMapping();
        if(!paramOfficeMappingRepository.findAll().isEmpty())
            param=paramOfficeMappingRepository.findAll().get(0);
        param.setIdRcOrigen(Long.parseLong(rc));
        param.setCampoRcCentroOrigen(Long.parseLong(campoCentroOrigen));
        param.setCampoRcCentroOrigenN(campoRCRepository.findAllById(Integer.parseInt(campoCentroOrigen)).getNombre());
        param.setCampoRc1DetalleOrigen(Long.parseLong(campoNombreCentro));
        param.setCampoRc1DetalleOrigenN(campoRCRepository.findAllById(Integer.parseInt(campoNombreCentro)).getNombre());
        param.setCampoRcCentroResultado(Long.parseLong(campoCentroDestino));
        param.setCampoRcCentroResultadoN(campoRCRepository.findAllById(Integer.parseInt(campoCentroDestino)).getNombre());
        paramOfficeMappingRepository.save(param);
    }

    public void saveData(){
        ParamOfficeMapping param = new ParamOfficeMapping();
        if(!paramOfficeMappingRepository.findAll().isEmpty())
            param=paramOfficeMappingRepository.findAll().get(0);
        Query query = entityManager.createNativeQuery("Delete from preciso_homologacion_centros;\n" +
                "Insert into preciso_homologacion_centros (centro_origen,nombre_centro,centro_destino)\n" +
                "Select distinct top 2000 "+param.getCampoRcCentroOrigenN()+" as cam_1 ,"+param.getCampoRc1DetalleOrigenN()+" as cam_2 ,"+param.getCampoRcCentroResultadoN()+" as cam_3 from preciso_rc_"+param.getIdRcOrigen());
        query.executeUpdate();
    }

    public List<ParamOfficeMapping> getParam(){
        return paramOfficeMappingRepository.findAll();
    }

    public void loadAudit(User user, String mensaje)
    {
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("Procesos Contables");
        insert.setFecha(today);
        insert.setInput("Homologación Centros");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }

    public List<Object[]> findByFilter(String value, String filter, Object mes) {
        List<Object[]> list=new ArrayList<Object[]>();
        String dataMes ="";
        if(mes!=null)
            dataMes =" and a.fecha_conciliacion like '"+mes.toString()+"%' ";
        switch (filter)
        {
            case "Conciliación":
                Query quer = entityManager.createNativeQuery("select b.nombre as codigo_concil,a.fecha_conciliacion,c.nombre as codigo_conta,a.fecha_cargue_contable,a.estado_cargue_conciliacion,a.estado_cargue_cargue_contable,a.aplica_semana, a.id\n" +
                        "from preciso_maestro_inventarios a\n" +
                        "left join preciso_conciliaciones b on a.codigo_conciliacion=b.id\n" +
                        "left join preciso_rutas_contables c on a.codigo_cargue_contable=c.id_rc where b.nombre like ? "+dataMes+" order by b.nombre, a.fecha_conciliacion,c.nombre,a.fecha_cargue_contable ");
                quer.setParameter(1,value);
                list = quer.getResultList();
                break;
            case "Contable":
                Query query = entityManager.createNativeQuery("select b.nombre as codigo_concil,a.fecha_conciliacion,c.nombre as codigo_conta,a.fecha_cargue_contable,a.estado_cargue_conciliacion,a.estado_cargue_cargue_contable,a.aplica_semana, a.id\n" +
                        "from preciso_maestro_inventarios a\n" +
                        "left join preciso_conciliaciones b on a.codigo_conciliacion=b.id\n" +
                        "left join preciso_rutas_contables c on a.codigo_cargue_contable=c.id_rc where c.nombre like ? "+dataMes+" order by b.nombre, a.fecha_conciliacion,c.nombre,a.fecha_cargue_contable ");
                query.setParameter(1,value);
                list= query.getResultList();
                break;
            default:
                break;
        }
        return list;
    }
}
