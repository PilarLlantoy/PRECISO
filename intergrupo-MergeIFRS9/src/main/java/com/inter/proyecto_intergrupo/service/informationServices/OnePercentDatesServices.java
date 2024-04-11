package com.inter.proyecto_intergrupo.service.informationServices;

import com.inter.proyecto_intergrupo.model.information.OnePercentDates;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Service
@Transactional
public class OnePercentDatesServices {

    @Autowired
    EntityManager entityManager;

    public ArrayList<OnePercentDates> getDates(String period){
        ArrayList<OnePercentDates> toReturn = new ArrayList<>();

        Query getData = entityManager.createNativeQuery("SELECT * FROM nexco_fechas_porc WHERE fecha_corte LIKE ?", OnePercentDates.class);
        getData.setParameter(1,period+"%");

        if(!getData.getResultList().isEmpty()){
            toReturn = (ArrayList<OnePercentDates>) getData.getResultList();
        }

        return toReturn;

    }

    public void generateDates(String period) throws ParseException {
        Query getMaxDate = entityManager.createNativeQuery("SELECT MAX(FechaHabil) FROM FECHAS_HABILES WHERE FechaHabil LIKE ? AND DiaHabil = 1");
        getMaxDate.setParameter(1,period+"%");

        if(!getMaxDate.getResultList().isEmpty()){
            Query delete = entityManager.createNativeQuery("DELETE FROM nexco_fechas_porc WHERE fecha_corte LIKE ?");
            delete.setParameter(1,period+"%");
            delete.executeUpdate();
        }

        String date = getMaxDate.getSingleResult().toString();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd",new Locale("es", "ES"));

        Date originalDate = formatter.parse(date.trim());

        Calendar c = Calendar.getInstance();
        c.setTime(originalDate);

        String month = c.getDisplayName(Calendar.MONTH, Calendar.LONG, new Locale("es", "ES"));

        ArrayList<String[]> toInsert = new ArrayList<>();

        Calendar auxCal = Calendar.getInstance();
        auxCal.setTime(originalDate);

        for(int i = 1; i<=10; i++){
            String[] data = new String[4];
            auxCal.add(Calendar.DATE,1);
            Date auxDate = auxCal.getTime();
            String fecha = formatter.format(auxDate);

            data[0] = month;
            data[1] = fecha;
            data[2] = formatter.format(originalDate);
            data[3] = String.valueOf(i);

            toInsert.add(data);
        }

        for(String[] data : toInsert){
            Query insert = entityManager.createNativeQuery("INSERT INTO nexco_fechas_porc (mes_contable,fechas, fecha_corte, version) VALUES (?,?,?,?)");
            insert.setParameter(1,data[0]);
            insert.setParameter(2,data[1]);
            insert.setParameter(3,data[2]);
            insert.setParameter(4,data[3]);
            insert.executeUpdate();
        }

    }


}
