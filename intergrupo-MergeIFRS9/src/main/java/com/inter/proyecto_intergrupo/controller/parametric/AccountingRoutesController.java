package com.inter.proyecto_intergrupo.controller.parametric;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import static com.inter.proyecto_intergrupo.controller.parametric.AccountingLoadController.rutaArchivoFormato1;

@Controller
public class AccountingRoutesController {
    private static final int PAGINATIONCOUNT=12;

    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private List<String> listColumns=List.of("Nombre", "Archivo", "Ruta de Acceso", "Tipo de Archivo", "Estado");

    @Autowired
    private UserService userService;

    @Autowired
    private CampoRCService campoRCService;

    @Autowired
    private AccountingRouteService conciliationService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/accountingRoutes")
    public ModelAndView showAccountingRoutes(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        if(userService.validateEndpoint(user.getId(),"Ver Rutas Contables")) {

            int page=(params.get("page")!=null && !params.get("page").toString().equalsIgnoreCase(""))?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<AccountingRoute> aroutes = conciliationService.findAll();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), aroutes.size());
            Page<AccountingRoute> pageAR= new PageImpl<>(aroutes.subList(start, end), pageRequest, aroutes.size());

            int totalPage=pageAR.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allRCs",pageAR.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("columns",listColumns);
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","accountingRoutes");
            modelAndView.addObject("registers",aroutes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/accountingRoutes");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/accountingRoutes")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/fieldLoadingAccountingRoute/{id}");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = accountingRouteService.saveFileBD(fileContent,user,params.get("id").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                GeneralListReport generalListReport = new GeneralListReport(list);
                generalListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        modelAndView.addObject("page1", params.get("page1").toString());
        modelAndView.addObject("id", params.get("id").toString());
        return  modelAndView;
    }


    @GetMapping(value = "/parametric/createAccountingRoute")
    public ModelAndView showCreateAccountingRoute(){
        ModelAndView modelAndView = new ModelAndView();
        AccountingRoute aroute = new AccountingRoute();
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        modelAndView.setViewName("/parametric/createAccountingRoute");

        return modelAndView;
    }


    @GetMapping("/leer-archiva")
    public ResponseEntity<InputStreamResource> leerArchivoTXTs(@RequestParam String id) throws IOException {
        AccountingRoute ac = accountingRouteService.findById(Integer.valueOf(id));
        String filePath = ac.getRuta();
        List<CampoRC> campos = ac.getCampos();
        List<CondicionRC> condiciones = ac.getCondiciones();
        List<ValidationRC> validaciones = ac.getValidaciones();

        List<Map<String, String>> lineasMap = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                Map<String, String> lineaMap = new HashMap<>();
                int offset = 0;

                for (CampoRC campo : campos) {
                    String campoNombre = campo.getNombre();
                    int longitud = Integer.valueOf(campo.getLongitud());
                    String valorCampo = line.substring(offset, Math.min(offset + longitud, line.length())).trim();
                    lineaMap.put(campoNombre, valorCampo);
                    offset += longitud;
                }

                boolean cumpleCondiciones = true;
                for (CondicionRC condicion : condiciones) {
                    String valorCampo = lineaMap.get(condicion.getCampo().getNombre());
                    if (valorCampo == null || !valorCampo.equals(condicion.getValorCondicion())) {
                        cumpleCondiciones = false;
                        break;
                    }
                }

                if (cumpleCondiciones) {
                    lineasMap.add(lineaMap);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (ValidationRC validacion : validaciones) {
            String campoRef = validacion.getCampoRef().getNombre();
            String campoVal = validacion.getCampoVal().getNombre();
            String valorValidacion = validacion.getValorValidacion();
            String operacion = validacion.getOperacion();
            double valorOperacion = Double.parseDouble(validacion.getValorOperacion());

            for (Map<String, String> lineaMap : lineasMap) {
                String valorCampoRef = lineaMap.get(campoRef);
                String valorCampoVal = lineaMap.get(campoVal);

                if (valorCampoVal != null && valorCampoRef != null && valorCampoVal.equals(valorValidacion)) {
                    double valorCampoRefNum = Double.parseDouble(valorCampoRef);

                    switch (operacion) {
                        case "Suma":
                            valorCampoRefNum += valorOperacion;
                            break;
                        case "Resta":
                            valorCampoRefNum -= valorOperacion;
                            break;
                        case "Multiplica":
                            valorCampoRefNum *= valorOperacion;
                            break;
                        case "Divida":
                            if (valorOperacion != 0) {
                                valorCampoRefNum /= valorOperacion;
                            }
                            break;
                        default:
                            break;
                    }

                    lineaMap.put(campoRef, String.valueOf(valorCampoRefNum));
                }
            }
        }

        /* Imprimir los datos filtrados
        System.out.println("Contenido del archivo procesado y filtrado:");
        for (Map<String, String> mapa : lineasMap) {
            System.out.println("Línea:");
            for (Map.Entry<String, String> entry : mapa.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            System.out.println();
        }*/

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Datos");
        CellStyle numericStyle = workbook.createCellStyle();
        DataFormat format = workbook.createDataFormat();
        numericStyle.setDataFormat(format.getFormat("0"));
        if (!lineasMap.isEmpty()) {
            Map<String, String> firstLine = lineasMap.get(0);
            Row headerRow = sheet.createRow(0);
            int headerCellIndex = 0;
            for (String header : firstLine.keySet()) {
                headerRow.createCell(headerCellIndex++).setCellValue(header);
            }

            int rowIndex = 1;
            for (Map<String, String> lineMap : lineasMap) {
                Row row = sheet.createRow(rowIndex++);
                int cellIndex = 0;
                for (String header : firstLine.keySet()) {
                    String cellValue = lineMap.get(header);
                    try {
                        double numericValue = Double.parseDouble(cellValue);
                        Cell cell = row.createCell(cellIndex++);
                        cell.setCellValue(numericValue);
                        cell.setCellStyle(numericStyle);
                    } catch (NumberFormatException e) {
                        row.createCell(cellIndex++).setCellValue(cellValue);
                    }
                }
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        workbook.write(baos);
        workbook.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=datos.xlsx");

        return new ResponseEntity<>(new InputStreamResource(bais), headers, HttpStatus.OK);
    }

    public static void leerArchivoXLSX(String ruta) {
        String excelFilePath  = rutaArchivoFormato1+"DATOS_OPERACIONES.xlsx";
        try (FileInputStream fis = new FileInputStream(new File(excelFilePath));
             Workbook workbook = new XSSFWorkbook(fis)) {

            // Obtener la primera hoja del archivo Excel
            Sheet sheet = workbook.getSheetAt(0);

            // Recorrer las filas
            for (Row row : sheet) {
                // Comprobar si la fila está vacía
                if (isRowEmpty(row)) {
                    continue; // Saltar filas vacías
                }

                // Recorrer las celdas de cada fila
                for (Cell cell : row) {
                    switch (cell.getCellType()) {
                        case STRING:
                            System.out.print(cell.getStringCellValue() + "\t");
                            break;
                        case NUMERIC:
                            if (DateUtil.isCellDateFormatted(cell)) {
                                System.out.print(cell.getDateCellValue() + "\t");
                            } else {
                                System.out.print(cell.getNumericCellValue() + "\t");
                            }
                            break;
                        case BOOLEAN:
                            System.out.print(cell.getBooleanCellValue() + "\t");
                            break;
                        case FORMULA:
                            System.out.print(cell.getCellFormula() + "\t");
                            break;
                        default:
                            System.out.print(" \t");
                    }
                }
                System.out.println(); // Nueva línea después de cada fila
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isRowEmpty(Row row) {
        if (row == null) {
            return true;
        }
        for (int cellNum = row.getFirstCellNum(); cellNum < row.getLastCellNum(); cellNum++) {
            Cell cell = row.getCell(cellNum);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    @PostMapping(value = "/parametric/createAccountingRoute")
    public ModelAndView createAccountingRoute(@ModelAttribute AccountingRoute aroute, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountingRoutes");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        aroute.setHoraCargue(LocalTime.now());
        /*SourceSystem SF = sourceSystemService.findByNombre(sistFuente);
        aroute.setSfrc(SF);*/
        accountingRouteService.modificar(aroute);
        modelAndView.addObject("resp", "Add1");
        modelAndView.addObject("data", aroute.getNombre());
        return modelAndView;

    }

    @GetMapping(value = "/parametric/modifyAccountingRoute/{id}")
    public ModelAndView modifyAccountingRoute(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            String selecthoraCargue = aroute.getHoraCargue().format(formatter);
            modelAndView.addObject("selecthoraCargue", selecthoraCargue);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);
        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1", params.get("page1").toString());
        modelAndView.setViewName("parametric/modifyAccountingRoute");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyAccountingRoute")
    public ModelAndView modifyAccountingRouteCompl(@ModelAttribute AccountingRoute aroute, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/accountingRoutes");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        try {
            aroute.setHoraCargue(LocalTime.parse(params.get("selecthoraCargue").toString(), formatter));
            aroute.setSfrc(sourceSystemService.findByNombre(params.get("selectedSF").toString()));
            accountingRouteService.modificar(aroute);
        } catch (Exception e) {
            System.out.println("Error parsing time: " + e.getMessage());
        }
        modelAndView.addObject("resp", "Modify1");
        modelAndView.addObject("data", aroute.getNombre());
        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page", params.get("page1").toString());
        return modelAndView;
    }

    @GetMapping(value = "/parametric/fieldLoadingAccountingRoute/{id}")
    public ModelAndView cargueCampos(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        List<SourceSystem> allSFs = sourceSystemService.findAll();
        modelAndView.addObject("allSFs", allSFs);
        modelAndView.addObject("aroute",aroute);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), aroute.getCampos().size());
        List<CampoRC> loscampos = aroute.getCampos();
        Page<CampoRC> pageConciliation = new PageImpl<>(loscampos.subList(start, end), pageRequest, loscampos.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","fieldLoadingAccountingRoute/"+id);
        modelAndView.addObject("allCampos",pageConciliation.getContent());
        modelAndView.addObject("registers",loscampos.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);
        CampoRC campoRC = new CampoRC();
        modelAndView.addObject("campoRC",campoRC);

        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1", params.get("page1").toString());

        modelAndView.setViewName("parametric/fieldLoadingAccountingRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/conditionLoadingAccountingRoute/{id}")
    public ModelAndView cargueCondiciones(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        modelAndView.addObject("aroute",aroute);
        List<CondicionRC> condiciones = aroute.getCondiciones();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), condiciones.size());

        Page<CondicionRC> pageConciliation = new PageImpl<>(condiciones.subList(start, end), pageRequest, condiciones.size());
        List<CampoRC> campos = aroute.getCampos();
        modelAndView.addObject("campos",campos);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","conditionLoadingAccountingRoute/"+id);
        modelAndView.addObject("allCondiciones",pageConciliation.getContent());
        modelAndView.addObject("registers",condiciones.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);
        CondicionRC condicionRC = new CondicionRC();
        modelAndView.addObject("condicionRC",condicionRC);

        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1", params.get("page1").toString());

        modelAndView.setViewName("parametric/conditionLoadingAccountingRoute");
        return modelAndView;
    }


    @GetMapping(value = "/parametric/validationLoadingAccountingRoute/{id}")
    public ModelAndView cargueValidaciones(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        AccountingRoute aroute = accountingRouteService.findById(id);
        modelAndView.addObject("aroute",aroute);
        List<ValidationRC> validaciones = aroute.getValidaciones();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), validaciones.size());

        Page<ValidationRC> pageConciliation = new PageImpl<>(validaciones.subList(start, end), pageRequest, validaciones.size());
        List<CampoRC> campos = aroute.getCampos();
        modelAndView.addObject("campos",campos);
        List<CampoRC> camposRef = aroute.getCampos();
        modelAndView.addObject("camposRef",camposRef);

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","validationLoadingAccountingRoute/"+id);
        modelAndView.addObject("allValidaciones",pageConciliation.getContent());
        modelAndView.addObject("registers",validaciones.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");
        modelAndView.addObject("p_modificar", p_modificar);
        ValidationRC validationRC = new ValidationRC();
        modelAndView.addObject("validationRC",validationRC);

        if(params.get("page1")!=null && !params.get("page1").toString().equalsIgnoreCase(""))
            modelAndView.addObject("page1", params.get("page1").toString());

        modelAndView.setViewName("parametric/validationLoadingAccountingRoute");
        return modelAndView;
    }

    @GetMapping(value = "/parametric/searchAccountingRoutes")
    @ResponseBody
    public ModelAndView searchCountry(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<AccountingRoute> list;
        if(params==null)
            list=accountingRouteService.findByFilter("inactivo", "Estado");
        else
            list=accountingRouteService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<AccountingRoute> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allRCs",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchAccountingRoutes");
        modelAndView.addObject("registers",list.size());
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Rutas Contables");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/accountingRoutes");
        return modelAndView;
    }

    @GetMapping("/parametric/obtenerRutasCont/{sfcID}")
    @ResponseBody
    public List<Object[]> obtenerRutasCont(@PathVariable("sfcID") Integer sfcID) {
        List<Object[]> campos = accountingRouteService.findRutasBySFC(sfcID);
        return campos;
    }

    @GetMapping(value = "/parametric/accountingRoutes/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Rutas_Contables_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> accountRoute = accountingRouteService.findByEncabezados();
        List<Object[]> accountRouteCam = accountingRouteService.findByCampos();
        List<Object[]> accountRouteCon = accountingRouteService.findByCondiciones();
        List<Object[]> accountRouteVal = accountingRouteService.findByValidaciones();
        AccountingRoutesListReport listReport = new AccountingRoutesListReport(accountRoute,accountRouteVal,accountRouteCon,accountRouteCam);
        listReport.export(response);
    }

}
