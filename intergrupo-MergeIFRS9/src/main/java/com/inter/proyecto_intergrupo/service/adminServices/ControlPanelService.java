package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.ControlPanel;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.MessagingException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class ControlPanelService {

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public ControlPanelService() {

    }

    public List<ControlPanel> findAll()
    {
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em", ControlPanel.class);
        return query.getResultList();
    }

    public List<ControlPanel> findByIdCuadroMando(String centro, String input, String componente, String fecha){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em WHERE em.responsable = ? AND" +
                " em.input = ? AND em.componente = ? AND em.fecha_reporte = ?", ControlPanel.class);
        query.setParameter(1, centro);
        query.setParameter(2, input);
        query.setParameter(3, componente);
        query.setParameter(4,fecha);
        return query.getResultList();
    }

    public List<ControlPanel> findByFechaReporte(String id,String filtro,String value){
        List<ControlPanel> list=new ArrayList<ControlPanel>();

        if(!filtro.equals("VACIO"))
        {

            switch (filtro)
            {
                case "Componente":
                    Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                            "WHERE em.componente LIKE ? AND em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
                    query.setParameter(1, value );
                    query.setParameter(2, id );

                    list= query.getResultList();

                    break;
                case "Input":
                    Query query0 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                            "WHERE em.input LIKE ? AND em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
                    query0.setParameter(1, value);
                    query0.setParameter(2, id );

                    list= query0.getResultList();
                    break;
                case "Centro Costos":
                    Query query1 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                            "WHERE em.responsable LIKE ? AND em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
                    query1.setParameter(1, value);
                    query1.setParameter(2, id );

                    list= query1.getResultList();
                    break;
                case "Empresa":
                    Query query2 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                            "WHERE em.empresa LIKE ? AND em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
                    query2.setParameter(1, value);
                    query2.setParameter(2, id );

                    list= query2.getResultList();
                    break;
                case "Usuario Carga":
                    Query query3 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                            "WHERE em.usuario_carga LIKE ? AND em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
                    query3.setParameter(1, value);
                    query3.setParameter(2, id );

                    list= query3.getResultList();
                    break;
                case "Estado Carga":
                    Query query4 = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                            "WHERE em.estado LIKE ? AND em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
                    query4.setParameter(1, value);
                    query4.setParameter(2, id );

                    list= query4.getResultList();
                    break;
                default:
                    break;
            }
        }
        else
        {
            Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em " +
                    "WHERE em.fecha_reporte = ? ORDER BY componente,input,responsable", ControlPanel.class);
            query.setParameter(1, id );

            list= query.getResultList();
        }

        return list;
    }
    public List<ControlPanel> findByFechaReporteRP21(String fecha, User user){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em WHERE em.fecha_reporte = ? AND em.componente = ? ORDER BY em.input", ControlPanel.class);
        query.setParameter(1, fecha);
        query.setParameter(2, "DERIVADOS");
        return query.getResultList();
    }

    public List<ControlPanel> findByFechaReporteContingentes(String fecha, User user){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em WHERE em.fecha_reporte = ? AND em.componente = ? AND em.responsable = ? ORDER BY em.input", ControlPanel.class);
        query.setParameter(1, fecha);
        query.setParameter(2, "CONTINGENTES");
        query.setParameter(3, user.getCentro());
        return query.getResultList();
    }

    public boolean findByRysFechaReporte(String periodo){
        Query query = entityManager.createNativeQuery("SELECT em.* FROM nexco_cuadro_mando as em WHERE em.fecha_reporte = ? AND em.componente = ? AND em.input = ? AND semaforo_input ='FULL'", ControlPanel.class);
        query.setParameter(1, periodo);
        query.setParameter(2, "DERIVADOS");
        query.setParameter(3, "RYS");
        if(!query.getResultList().isEmpty())
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public void changeState(ControlPanel control, User user){
        String datoEspe = "Habilitado";
        if(control.getEstado()==true){
            control.setEstado(false);
            datoEspe = "Inhabilitado";
        }
        else{
            control.setEstado(true);
        }
        Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET estado = ? " +
                "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
        query.setParameter(1, control.getEstado());
        query.setParameter(2, control.getResponsable());
        query.setParameter(3, control.getInput());
        query.setParameter(4, control.getComponente());
        query.setParameter(5, control.getFechaReporte());
        query.executeUpdate();
        auditCode("Cambio estado a "+datoEspe +" del Componente "+control.getComponente() +" e Input "+ control.getInput() + " en el periodo " + control.getFechaReporte(),user);
    }

    public void changeSemaforoInput(ControlPanel control){
        Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? " +
                "WHERE responsable = ? AND input = ? AND componente = ?", ControlPanel.class);
        query.setParameter(1, control.getSemaforoInput());
        query.setParameter(2, control.getResponsable());
        query.setParameter(3, control.getInput());
        query.setParameter(4, control.getComponente());
        query.executeUpdate();
    }

    public void changeSemaforoComponente(ControlPanel control){
        Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = ? " +
                "WHERE componente = ?", ControlPanel.class);
        query.setParameter(1, control.getSemaforoComponente());
        query.setParameter(2, control.getComponente());
        query.executeUpdate();
    }

    public void sendEmail(ControlPanel panel, User usuario) {

        panel.setSemaforoInput("PENDING");

        Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? " +
                "WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
        query.setParameter(1, panel.getSemaforoInput());
        query.setParameter(2, panel.getResponsable());
        query.setParameter(3, panel.getInput());
        query.setParameter(4, panel.getComponente());
        query.setParameter(5, panel.getFechaReporte());
        query.executeUpdate();
    }

    public void validateComponent(String component,String period)
    {
        Query queryFinal = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando " +
                "WHERE componente = ? AND semaforo_input = 'PENDING' AND fecha_reporte = ?", ControlPanel.class);
        queryFinal.setParameter(1, component);
        queryFinal.setParameter(2, period);

        Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = ? " +
                "WHERE componente = ? AND fecha_reporte = ?", ControlPanel.class);
        query5.setParameter(2, component);
        query5.setParameter(3, period);

        if(queryFinal.getResultList().size()==0)
        {
            query5.setParameter(1, "FULL");
        }
        else
        {
            query5.setParameter(1, "EMPTY");
        }
        query5.executeUpdate();

    }
    public void validateComponentByInput(String input, String periodo)
    {
        Query queryFinal1 = entityManager.createNativeQuery("SELECT componente FROM nexco_cuadro_mando " +
                "WHERE input = :input AND semaforo_input != :state AND fecha_reporte = :periodo");
        queryFinal1.setParameter("input", input);
        queryFinal1.setParameter("state", "PENDING");
        queryFinal1.setParameter("periodo", periodo);
        List<String> lista1=queryFinal1.getResultList();


        Query queryFinal = entityManager.createNativeQuery("SELECT componente FROM nexco_cuadro_mando " +
                "WHERE input = :input AND semaforo_input = :state AND fecha_reporte= :periodo AND componente NOT IN :comp");
        queryFinal.setParameter("input", input);
        queryFinal.setParameter("periodo", periodo);
        queryFinal.setParameter("state", "PENDING");
        queryFinal.setParameter("comp",lista1 );
        List<String> lista2=queryFinal.getResultList();

        Query query5 = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = :estado " +
                "WHERE input = :input AND fecha_reporte = :periodo AND componente IN :comp");
        query5.setParameter("estado", "PENDING");
        query5.setParameter("input", input);
        query5.setParameter("periodo", periodo);
        query5.setParameter("comp", lista2);
        query5.executeUpdate();


    }

    public List<Object> findByAccounts(String centro,String componente,String input){
        Query query = entityManager.createNativeQuery("SELECT ncr.centro, ncr.cuenta_local FROM nexco_cuentas_responsables as ncr \n" +
                "WHERE ncr.centro = ? AND ncr.componente = ? AND ncr.input = ? ");
        query.setParameter(1, centro);
        query.setParameter(2, componente);
        query.setParameter(3, input);
        return query.getResultList();
    }

    public void validateAccountsGlobal(List<String> listAccountsCompl, String centro,String input,String usuario,String periodo)
    {

        Query queryS = entityManager.createNativeQuery("SELECT SUBSTRING(CAST(ncr.cuenta_local AS varchar),1,4) FROM nexco_cuentas_responsables as ncr \n" +
                "WHERE ncr.centro = :centro AND ncr.input = :input AND SUBSTRING(CAST(ncr.cuenta_local AS varchar),1,4) IN :lista GROUP BY SUBSTRING(CAST(ncr.cuenta_local AS varchar),1,4)");
        queryS.setParameter("centro", centro);
        queryS.setParameter("input", input);
        queryS.setParameter("lista", listAccountsCompl);
        List<String> listAccounts = queryS.getResultList();

        if(listAccounts.size()>0)
        {
            Query query = entityManager.createNativeQuery("UPDATE ncm SET semaforo_input = 'PENDING', usuario_carga = :usuario \n" +
                    "FROM nexco_cuadro_mando as ncm \n" +
                    "INNER JOIN  nexco_cuentas_responsables AS ncr \n" +
                    "ON ncm.componente= ncr.componente AND ncr.input = ncm.input AND ncr.centro = ncm.responsable\n" +
                    "WHERE ncr.centro = :centro AND ncr.input = :input AND SUBSTRING(CAST(ncr.cuenta_local AS varchar),1,4) IN :listAccounts AND ncm.fecha_reporte = :periodo");
            query.setParameter("usuario", usuario);
            query.setParameter("centro", centro);
            query.setParameter("input", input);
            query.setParameter("listAccounts", listAccounts);
            query.setParameter("periodo", periodo);
            query.executeUpdate();
        }
    }

    public List<ControlPanel> validateQueryAndVertical(String periodo, String responsable){
        Query query = entityManager.createNativeQuery("SELECT * FROM nexco_cuadro_mando WHERE fecha_reporte = ? AND responsable = ? AND componente = ?;", ControlPanel.class);
        query.setParameter(1,periodo);
        query.setParameter(2,responsable);
        query.setParameter(3,"DERIVADOS");
        List<ControlPanel> listControlPanel = query.getResultList();
        return listControlPanel;
    }

    public List<Object[]> getListRp21(String periodo){
        Query query = entityManager.createNativeQuery("SELECT local_rp21, div_rp21, SUM(vr_nominal_cop) AS suma " +
                "FROM nexco_reporte_rp21 WHERE CONVERT(varchar(7),fecont,120) = ? GROUP BY local_rp21, div_rp21;");
        query.setParameter(1,periodo);
        List<Object[]> listRp21 = query.getResultList();
        return listRp21;
    }

    public List<Object[]> getListQuery(String periodo){
        Query query = entityManager.createNativeQuery("SELECT nucta,coddiv, SUM(salmes) AS suma FROM nexco_query WHERE fecont = ? GROUP BY nucta,coddiv;");
        query.setParameter(1,periodo);
        List<Object[]> listQuery = query.getResultList();
        return listQuery;
    }

    public List<String> validateRp21AndQuery(String perido, String responsable){
        int validate = 1;
        int validatePanel = 1;
        List<String> accounts = new ArrayList<>();
        List<ControlPanel> controlPanelList = validateQueryAndVertical(perido,responsable);
        for (ControlPanel panel:controlPanelList) {
            if(panel.getSemaforoComponente().equals("PENDING"))
                validatePanel = validatePanel & 1;
            else
                validatePanel = validatePanel & 0;
        }
        if (validatePanel == 1) {
          List<Object[]> rp21s = getListRp21(perido);
          List<Object[]> querys = getListQuery(perido);
          for(int i = 0; i<rp21s.size(); i++){
              for(int j=0; j< querys.size(); j++){
                  if(rp21s.get(i)[0].toString().equals(querys.get(j)[0].toString())){
                      if(rp21s.get(i)[1].toString().equals(querys.get(j)[1].toString())
                              && rp21s.get(i)[2].toString().equals(querys.get(j)[2].toString())){
                          validate = validate & 1;
                          querys.remove(i);
                          break;
                      } else {
                          validate = validate & 0;
                          accounts.add(rp21s.get(i)[0].toString());
                      }
                  }
              }
          }
          if(validate == 1){
              updateState(controlPanelList);
              accounts.add("Validación exitosa");
          }
          if(querys.size() == 0) {
              rp21s.stream().forEach(e -> accounts.add(e[0].toString()));
          }
        }
        return accounts;
    }

    public void updateState(List<ControlPanel> controlPanelList){
        for (ControlPanel panel: controlPanelList) {
            Query query = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_input = ? , semaforo_componente = ?" +
                    " WHERE responsable = ? AND input = ? AND componente = ? AND fecha_reporte = ?", ControlPanel.class);
            query.setParameter(1, "FULL");
            query.setParameter(2, "FULL");
            query.setParameter(3, panel.getResponsable());
            query.setParameter(4, panel.getInput());
            query.setParameter(5, panel.getComponente());
            query.setParameter(6, panel.getFechaReporte());
            query.executeUpdate();
        }
    }

    public void save(ControlPanel control) {
        Query query = entityManager.createNativeQuery("INSERT INTO nexco_cuadro_mando(responsable, input, fecha_reporte, componente, empresa, estado, semaforo_componente, semaforo_input) VALUES (?,?,?,?,?,?,?,?)", ControlPanel.class);
        query.setParameter(1, control.getResponsable());
        query.setParameter(2, control.getInput());
        query.setParameter(3, control.getFechaReporte());
        query.setParameter(4, control.getComponente());
        query.setParameter(5, control.getEmpresa());
        query.setParameter(6, false);
        query.setParameter(7, "EMPTY");
        query.setParameter(8, "EMPTY");
        query.executeUpdate();
    }

    public void changeAllStates(String period, String action,User user){
        if(action.equals("Enable")){
            Query enable = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando " +
                    "SET estado = 1 " +
                    "WHERE fecha_reporte = ?");

            enable.setParameter(1,period);
            enable.executeUpdate();
            auditCode("Cambio general de estados a Habilitado en el periodo " + period ,user);
        } else {
            Query disable = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando " +
                    "SET estado = 0 " +
                    "WHERE fecha_reporte = ?");

            disable.setParameter(1,period);
            disable.executeUpdate();
            auditCode("Cambio general de estados a Inhabilitado en el periodo " + period ,user);
        }
    }

    public void deleteIntergrupo(String period, String inter){
        if(inter.equals("v1")){
            Query delete = entityManager.createNativeQuery("DELETE FROM /*nexco_intergrupo_v1*/ nexco_intergrupo_v1_def WHERE periodo = ?");
            delete.setParameter(1,period);
            delete.executeUpdate();

            Query deleteControl = entityManager.createNativeQuery("UPDATE nexco_cuadro_mando SET semaforo_componente = 'EMPTY', semaforo_input = 'EMPTY' WHERE fecha_reporte = ?");
            deleteControl.setParameter(1,period);
            deleteControl.executeUpdate();
        }else{
            Query delete = entityManager.createNativeQuery("DELETE FROM /*nexco_intergrupo_v2*/ nexco_intergrupo_v2_def WHERE periodo = ?");
            delete.setParameter(1,period);
            delete.executeUpdate();
        }
    }

    public void auditCode (String info, User user){
        Date today=new Date();
        Audit insert = new Audit();
        insert.setAccion(info);
        insert.setComponente("Intergrupo");
        insert.setFecha(today);
        insert.setInput("Cuadro de Mando");
        insert.setNombre(user.getNombre());
        insert.setUsuario(user.getUsuario());
        insert.setCentro(user.getCentro());
        auditRepository.save(insert);
    }
}
