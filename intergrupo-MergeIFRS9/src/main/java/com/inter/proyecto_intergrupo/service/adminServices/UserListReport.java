package com.inter.proyecto_intergrupo.service.adminServices;

import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class UserListReport {
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    private List<User> userList;

    public UserListReport(List<User> userList){
        this.userList = userList;
        workbook = new XSSFWorkbook();
    }

    private void writeHeaderLine(){
        sheet = workbook.createSheet("Usuarios");
        Row row = sheet.createRow(0);

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(10);
        style.setFont(font);

        createCell(row, 0, "Usuario", style);
        createCell(row, 2, "Correo", style);
        createCell(row, 1, "Nombre", style);
        createCell(row, 3, "Centro", style);
        createCell(row, 4, "Fecha Alta", style);
        createCell(row, 5, "Estado", style);
        createCell(row, 6, "Roles", style);
        createCell(row, 7, "Yntp Empresa", style);
    }

    private void createCell(Row row, int columCount, Object value, CellStyle style){
        sheet.autoSizeColumn(columCount);
        Cell cell = row.createCell(columCount);

        if(value instanceof Integer){
            cell.setCellValue((Integer) value);
        } else if(value instanceof Boolean){
            cell.setCellValue((Boolean) value);
        } else if(value instanceof String){
            cell.setCellValue((String) value);
        } else if(value instanceof Date){
            cell.setCellValue((Date) value);
        }else if(value instanceof Double){
            cell.setCellValue((Double) value);
        }

        cell.setCellStyle(style);
    }

    private void writeDataLines(){
        int rowCount = 1;

        CellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setFontHeight(11);
        style.setFont(font);

        for(User user: userList){
            Row row = sheet.createRow(rowCount++);
            int columnCount = 0;

            String listRoles ="";
            for (Role rol: user.getRoles()) {
                listRoles = listRoles+ rol.getNombre()+",";
            }
            if(listRoles.length()>1)
                listRoles = listRoles.substring(0,listRoles.length()-1);

            createCell(row,columnCount++,user.getUsuario(),style);
            createCell(row,columnCount++,user.getNombre(),style);
            createCell(row,columnCount++,user.getCorreo(),style);
            createCell(row,columnCount++,user.getCentro(),style);
            createCell(row,columnCount++,user.getCreacion().toString(),style);
            if(user.getEstado()==true)
            {
                createCell(row,columnCount++,"Activo",style);
            }
            else{
                createCell(row,columnCount++,"Inactivo",style);
            }
            createCell(row,columnCount++, listRoles,style);
            createCell(row,columnCount++,user.getEmpresa(),style);
        }
    }

    public void export(HttpServletResponse response) throws IOException {
        writeHeaderLine();
        writeDataLines();

        ServletOutputStream outputStream = response.getOutputStream();
        workbook.write(outputStream);
        workbook.close();

        outputStream.close();
    }
}
