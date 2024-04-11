package com.inter.proyecto_intergrupo.service.eeffconsolidated;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucFiliales;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoresPucTemporalFiliales;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ValoresPucConsolidatedRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.ValoresPucTemporalConsolidatedRepository;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Service
@Transactional

public class BancoPucConsolidatedService {
    @Autowired
    private ValoresPucConsolidatedRepository valoresPucConsolidatedRepository;

    @Autowired
    private ValoresPucTemporalConsolidatedRepository valoresPucTemporalConsolidatedRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    private AuditRepository auditRepository;

    public BancoPucConsolidatedService(ValoresPucConsolidatedRepository valoresPucConsolidatedRepository) {
        this.valoresPucConsolidatedRepository = valoresPucConsolidatedRepository;
    }

    public List<Object[]> getPucDataByPeriod(String periodo) {

        Query update = entityManager.createNativeQuery("select '00548' as empresa, a.nucta, substring(a.nucta,1,1) as clase, a.derecta,case when a.incie = '-' then 'C'when a.incie = '+' then 'D' END as inci, a.indic, a.estacta, b.moneda , a.codicons46,  ? as periodo from (select * from cuentas_puc where empresa = '0013') a\n" +
                "LEFT JOIN (select * from nexco_query_banco where periodo = ? and empresa = '0013') b on a.nucta = b.nucta");
        update.setParameter(1,periodo);
        update.setParameter(2,periodo);
        List<Object[]> pucData = update.getResultList();

        return pucData;
    }

    public void downloadPucBanco(HttpServletResponse response, String period) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment; filename=PUC_Banco.xlsx");
        List<Object[]> pucData = getPucDataByPeriod(period);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("PUCBanco");

        Row headerRow = sheet.createRow(0);
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat(workbook.createDataFormat().getFormat("#,##0.00"));

        headerRow.createCell(0).setCellValue("Empresa");
        headerRow.createCell(1).setCellValue("Cuenta");
        headerRow.createCell(2).setCellValue("Clase Cuenta");
        headerRow.createCell(3).setCellValue("Nombre Cuenta");
        headerRow.createCell(4).setCellValue("Naturaleza");
        headerRow.createCell(5).setCellValue("Indic");
        headerRow.createCell(6).setCellValue("Estado de Cuenta");
        headerRow.createCell(7).setCellValue("Moneda");
        headerRow.createCell(8).setCellValue("Cod Cons");
        headerRow.createCell(9).setCellValue("Periodo");
        int rowNum = 1;
        for (Object[] pucItem : pucData) {
            Row row = sheet.createRow(rowNum++);

            row.createCell(0).setCellValue(pucItem[0].toString());
            row.createCell(1).setCellValue(pucItem[1].toString());
            row.createCell(2).setCellValue(pucItem[2].toString());
            row.createCell(3).setCellValue(pucItem[3].toString());
            if(pucItem[4]!= null)
                row.createCell(4).setCellValue(pucItem[4].toString());
            else
                row.createCell(4).setCellValue("");
            row.createCell(5).setCellValue(pucItem[5].toString());
            row.createCell(6).setCellValue(pucItem[6].toString());
            if(pucItem[7]!= null)
                row.createCell(7).setCellValue(pucItem[7].toString());
            else
                row.createCell(7).setCellValue("");
            row.createCell(8).setCellValue(pucItem[8].toString());
            row.createCell(9).setCellValue(pucItem[9].toString());
        }
        workbook.write(response.getOutputStream());
        response.flushBuffer();
    }


    public List<ValoresPucFiliales> getAllPucData() {
        return valoresPucConsolidatedRepository.findAll();
    }

}

