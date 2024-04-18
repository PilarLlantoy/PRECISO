package com.inter.proyecto_intergrupo.service.eeffconsolidated;
import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.io.*;

@Service
@Transactional
public class FiduciariaSFCconsolidatedService {

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private DatesLoadEeffRepository datesLoadEeFFRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public boolean ValidarFecha(InputStream fileContent, String periodo) {
        try {

            String outputPath = "C:\\Users\\CE66916\\Documents\\BBVA Intergrupo\\SoporteSFCFiduciaria.pt";

            InputStreamReader inputStreamReader = new InputStreamReader(fileContent);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(outputPath));

            String linea;
            String buscarlinea = "FECHA DEL INFORME";
            boolean fechaValida = false;

            while ((linea = bufferedReader.readLine()) != null) {
                bufferedWriter.write(linea);
                bufferedWriter.newLine();

                if (linea.contains(buscarlinea)) {
                    String[] parts = linea.split(":");
                    SimpleDateFormat periodoActual = new SimpleDateFormat("yyyy-MM");
                    SimpleDateFormat formatoNuevo = new SimpleDateFormat("MMyyyy");
                    Date fechaPeriodo = periodoActual.parse(periodo);
                    String periodoNuevo = formatoNuevo.format(fechaPeriodo);

                    if (parts[1].substring(3, parts[1].length()).equals(periodoNuevo.replace("-", ""))) {
                        fechaValida = true;
                    }
                }
            }

            return fechaValida;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void loadAudit(User user, String mensaje) {
        Date today = new Date();
        Audit insert = new Audit();
        insert.setAccion(mensaje);
        insert.setCentro(user.getCentro());
        insert.setComponente("EEFF Consolidado");
        insert.setFecha(today);
        insert.setInput("Filiales");
        insert.setNombre(user.getPrimerNombre());
        insert.setUsuario(user.getUsuario());
        auditRepository.save(insert);
    }
    public void guardarSoporteEnBD(InputStream inputStream , String periodo , String entidad , User user)  {
        try {
            DatesLoadEeFF datesLoadEeFF = datesLoadEeFFRepository.findByEntidadAndPeriodo(entidad,periodo);
            System.out.println(datesLoadEeFF);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            byte[] byteArray = outputStream.toByteArray();
            datesLoadEeFF.setSoporteSfcDescarga(byteArray);
            entityManager.persist(datesLoadEeFF);
            loadAudit(user,"Cargue imagen Soporte SFC Exitosa");
            outputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public DatesLoadEeFF obtenerSoporteDesdeBD(String entidad , String periodo) {

        DatesLoadEeFF archivoDesdeBD = datesLoadEeFFRepository.findByEntidadAndPeriodo(entidad, periodo);
        if (archivoDesdeBD != null) {
            return archivoDesdeBD;
        } else {
            return null;
        }
    }
}
