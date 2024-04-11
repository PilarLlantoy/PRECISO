package com.inter.proyecto_intergrupo.service.parametricServices;

import com.inter.proyecto_intergrupo.model.parametric.SubProduct;
import com.inter.proyecto_intergrupo.repository.parametric.SubProductRepository;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

@Service
public class SubProductService {

    @Autowired
    private SubProductRepository subProductRepository;

    Logger log = LoggerFactory.getLogger(SubProductService.class);

    //@Scheduled(fixedRate = 300000)
    public void loadFile() throws FileNotFoundException {
        Path path = Paths.get("C:/Users/fryda/Documents/Subproductos.txt");
        if (Files.exists(path)) {
            File file = new File("C:/Users/fryda/Documents/Subproductos.txt");
            loadDatabase(file);
        }
    }

    public void loadDatabase(File file) throws FileNotFoundException {
        Scanner scan = new Scanner(file);
        int count = 0;
        while (scan.hasNextLine()) {
            String line = scan.nextLine();
            line = line.replaceAll("\\s+", ";");
            if (line.indexOf(';') >= 0 && count > 0) {
                String[] data = line.split(";");
                SubProduct subProduct = new SubProduct();
                subProduct.setCta(data[0]);
                subProduct.setSubproducto(data[1]);
                subProductRepository.save(subProduct);
            }
            count ++;
        }
    }

    public SubProduct getOne(String cta){
        return subProductRepository.findByCta(cta);
    }

    /*Con file excel*/
    public ArrayList<String[]> saveFileBDSubProduct(InputStream file) throws IOException {
        ArrayList<String[]> list=new ArrayList<String[]>();
        if (file!=null)
        {
            Iterator<Row> rows = null;
            Iterator<Row> rows1 = null;
            XSSFWorkbook wb = new XSSFWorkbook(file);
            XSSFSheet sheet = wb.getSheetAt(0);
            rows = sheet.iterator();
            rows1 = sheet.iterator();
            list = validarPlantilla(rows);
            String[] temporal = list.get(0);
            if(temporal[2].equals("true"))
            {
                list=getRows(rows1);
            }
        }
        return list;
    }

    public ArrayList<String[]> validarPlantilla(Iterator<Row> rows){
        ArrayList lista= new ArrayList();
        XSSFRow row;
        int firstRow=1;
        String[] log=new String[3];
        log[0]="0";
        log[1]="0";
        log[2]="false";
        while (rows.hasNext())
        {
            row = (XSSFRow) rows.next();
            if(firstRow!=1)
            {
                DataFormatter formatter = new DataFormatter();
                String cellCta = formatter.formatCellValue(row.getCell(0));
                String cellSubproducto = formatter.formatCellValue(row.getCell(1));

                log[0]=String.valueOf(row.getRowNum());

                if ((cellCta.isEmpty() || cellCta.isBlank()) &&(cellSubproducto.isEmpty() || cellSubproducto.isBlank()))
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]=String.valueOf(row.getRowNum());
                    log[2]="true";
                    break;
                }
                else if (cellCta.isEmpty() || cellCta.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="1";
                    log[2]="false";
                    break;
                }
                else if (cellSubproducto.isEmpty() || cellSubproducto.isBlank())
                {
                    log[0]=String.valueOf(row.getRowNum());
                    log[1]="2";
                    log[2]="false";
                    break;
                }
                else
                {
                    try
                    {
                        log[0]=String.valueOf(row.getRowNum());
                        log[1]="1";
                        log[2]="true";
                    }
                    catch(Exception e){
                        log[2]="falseFormat";
                        lista.add(log);
                        return lista;
                    }
                }
            }else
            {
                firstRow=0;
            }
        }
        lista.add(log);
        return lista;
    }

    public ArrayList getRows(Iterator<Row> rows) {
        XSSFRow row;
        ArrayList lista = new ArrayList();
        int firstRow = 1;
        while (rows.hasNext()) {
            String[] log=new String[3];
            log[2]="false";
            row = (XSSFRow) rows.next();
            if(firstRow!=1 && row.getCell(0)!=null) {
                DataFormatter formatter = new DataFormatter();
                String cellCta = formatter.formatCellValue(row.getCell(0));
                String cellSubproducto = formatter.formatCellValue(row.getCell(1));
                if ((cellCta.isEmpty() || cellCta.isBlank()) &&(cellSubproducto.isEmpty() || cellSubproducto.isBlank()))
                {
                    break;
                }
                else{
                    SubProduct subProduct = new SubProduct();
                    subProduct.setCta(cellCta);
                    subProduct.setSubproducto(cellSubproducto);
                    subProductRepository.save(subProduct);
                    log[2]="true";
                    log[1] = "Registro insertado exitosamente.";
                }
                lista.add(log);
            }
            else {
                firstRow=0;
            }
        }
        return lista;
    }
}
