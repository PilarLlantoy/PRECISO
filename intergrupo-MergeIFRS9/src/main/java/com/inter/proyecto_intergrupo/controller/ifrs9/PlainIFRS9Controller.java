package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9;
import com.inter.proyecto_intergrupo.model.ifrs9.PlainIFRS9Intergroup;
import com.inter.proyecto_intergrupo.model.ifrs9.ValIFRS9;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PlainIFRS9ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.PlainIFRS9Service;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ValidationIFRS9ListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ValidationIFRS9Service;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo3PListReport;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class PlainIFRS9Controller {

    private static final int PAGINATIONCOUNT=10;
    private List<String> listColumns = List.of("Tipo Registro", "Código Consolidación", "Divisa");

    @Autowired
    PlainIFRS9Service validationIFRS9;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value="/ifrs9/plainIFRS9")
    public ModelAndView showTemplateIFRS9(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Plantillas Neocon")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

        String todayString="";

        String Id;
        String Vf;
        if (params.get("vId") == null) {
            Id = "";
        } else {
            Id = params.get("vId").toString();
        }
        if (params.get("vFilter") == null) {
            Vf = "";
        } else {
            Vf = params.get("vFilter").toString();
        }

        if(params.get("period")==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }

        List<Object[]> validateIFRS9 = validationIFRS9.getValues(todayString);
        boolean noData = false;
        if (validateIFRS9.size() == 0) {
            noData = false;
        } else {
            noData = true;
        }

        List<Object[]> validateDataPyg = validationIFRS9.getValuesPyg(todayString);
        boolean pygData = false;
        if (validateDataPyg.size() == 0) {
            pygData = false;
        } else {
            pygData = true;
        }

        List<Object[]> validateDataPyg2 = validationIFRS9.getValuesPyg2(todayString);
        boolean pygData2 = false;
        if (validateDataPyg2.size() == 0) {
            pygData2 = false;
        } else {
            pygData2 = true;
        }

        List<Object[]> validateDataImpu = validationIFRS9.getValuesImpu(todayString);
        boolean pygImpu = false;
        if (validateDataImpu.size() == 0) {
            pygImpu = false;
        } else {
            pygImpu = true;
        }

        List<PlainIFRS9> valIFRS9;

        if (Id == "" | Vf == "") {
            valIFRS9 = validationIFRS9.getPlainIFRS9(todayString);
        } else {
            valIFRS9 = validationIFRS9.findByFilter(Id, Vf, todayString);
        }



        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), valIFRS9.size());
        Page<PlainIFRS9> pagePlain = new PageImpl<>(valIFRS9.subList(start, end), pageRequest, valIFRS9.size());

        int totalPage=pagePlain.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        List<Object[]> validateProv = validationIFRS9.validateTableIntProv(todayString);
        List<Object[]> validateRec = validationIFRS9.validateTableIntRec(todayString);

        if (!validateProv.isEmpty()) {
            modelAndView.addObject("validateInterProv", true);
            Date dateProv = new Date();
            try {
                dateProv = StatusInfoRepository.findByInputAndPeriodo("PLANOINTERGRUPO-PROV", todayString).getFecha();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateProv);
            modelAndView.addObject("dateProv", dateAsString);
        } else {
            modelAndView.addObject("validateInterProv", false);
        }

        if (!validateRec.isEmpty()) {
            modelAndView.addObject("validateInterRec", true);
            Date dateRec = new Date();
            try {
                dateRec = StatusInfoRepository.findByInputAndPeriodo("PLANOINTERGRUPO-REC", todayString).getFecha();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateRec);
            modelAndView.addObject("dateRec", dateAsString);
        } else {
            modelAndView.addObject("validateInterRec", false);
        }

        if (valIFRS9 != null){
            modelAndView.addObject("allValidation",pagePlain.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("directory","plainIFRS9");
            modelAndView.addObject("registers",valIFRS9.size());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
        }
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("noData", noData);
        modelAndView.addObject("pygData", pygData);
        modelAndView.addObject("pygData2", pygData2);
        modelAndView.addObject("pygImpu", pygImpu);
        modelAndView.setViewName("/ifrs/plainIFRS9");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;

    }

    @GetMapping(value="/ifrs9/CalPlainIFRS9")
    public ModelAndView CalPlainIFRS9(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
        response.setContentType("application/octet-stream");

        String todayString = "";

        if(params.get("period")==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }

        boolean estado = validationIFRS9.insertPlainIFRS9(todayString);
        if(estado)
            modelAndView.addObject("resp","ejecucionCorrectaPlanos");
        else
            modelAndView.addObject("resp","ejecucionCorrectaPlanos-1");
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("row",todayString);

        return  modelAndView;
    }

    @GetMapping(value="/ifrs9/CalPlainIFRS9PYG")
    public ModelAndView CalPlainIFRS9PYG(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
        response.setContentType("application/octet-stream");

        String todayString = "";

        if(params.get("period")==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        String responseView = params.get("pygData").toString();
        validationIFRS9.insertPlainIFRS9PYG(todayString,responseView);
        modelAndView.addObject("period",todayString);
        if(responseView.equals("true"))
            modelAndView.addObject("resp","ejecucionCorrectaPlanosPYG-1");
        else
            modelAndView.addObject("resp","ejecucionCorrectaPlanosPYG");

        return  modelAndView;
    }

    @GetMapping(value="/ifrs9/CalPlainIFRS9PYG2")
    public ModelAndView CalPlainIFRS9PYG2(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
        response.setContentType("application/octet-stream");

        String todayString = "";

        if(params.get("period")==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        String responseView = params.get("pygData2").toString();
        validationIFRS9.insertPlainIFRS9PYG2(todayString,responseView);
        modelAndView.addObject("period",todayString);
        if(responseView.equals("true"))
            modelAndView.addObject("resp","ejecucionCorrectaPlanosPYG2-1");
        else
            modelAndView.addObject("resp","ejecucionCorrectaPlanosPYG2");

        return  modelAndView;
    }

    @GetMapping(value="/ifrs9/CalPlainIFRS9Impuestos")
    public ModelAndView CalPlainIFRS9Impue(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
        response.setContentType("application/octet-stream");

        String todayString = "";

        if(params.get("period")==null || params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }
        String responseView = params.get("pygImpu").toString();
        validationIFRS9.insertPlainIFRS9Impu(todayString,responseView);
        modelAndView.addObject("period",todayString);
        if(responseView.equals("true"))
            modelAndView.addObject("resp","ejecucionCorrectaPlanosImpu-1");
        else
            modelAndView.addObject("resp","ejecucionCorrectaPlanosImpu");

        return  modelAndView;
    }

    @GetMapping(value = "/ifrs9/plainIFRS9/modify/{id}")
    @ResponseBody
    public ModelAndView modifyPlain(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        PlainIFRS9 toModify = validationIFRS9.findIFRS9(id).get(0);

        modelAndView.addObject("IFRS9Modify",toModify);
        modelAndView.setViewName("ifrs/modifyPlainIFRS9");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs9/plainIFRS9/modify")
    @ResponseBody
    public ModelAndView updatePlain(@ModelAttribute PlainIFRS9 ifrs9,@Param(value = "id") Integer id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            PlainIFRS9 search = validationIFRS9.findIFRS9(id).get(0);
            if (search!=null)
            {
                validationIFRS9.modifyPlain(ifrs9, id);
                ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
                modelAndView.addObject("resp", "Modify1");

                Date today = new Date();
                Audit insert = new Audit();
                insert.setAccion("Edición Manual Registros Plano IFRS9");
                insert.setCentro(user.getCentro());
                insert.setComponente("Plantillas IFRS9");
                insert.setFecha(today);
                insert.setInput("Plantillas IFRS9");
                insert.setNombre(user.getPrimerNombre());
                insert.setUsuario(user.getUsuario());
                auditRepository.save(insert);
                return  modelAndView;

            }
            else
            {
                ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
                modelAndView.addObject("resp", "Modify0");
                return  modelAndView;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
            modelAndView.addObject("resp", "General-1");
            return  modelAndView;

        }

    }

    @GetMapping(value = "/ifrs9/clearPlainIFRS9")
    public ModelAndView clearCuadreConcilifrs9(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params){

        String todayString = "";

        if(params.get("period")!=null && params.get("period").toString()=="") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);

            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            }
            else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        }
        else {
            todayString=params.get("period").toString();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        validationIFRS9.clearPlainIFRS9(user, todayString);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs9/plainIFRS9/download")
    @ResponseBody
    public void exportAllCuadre(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";

            String todayString = "";

            if(params.get("period")!=null && params.get("period").toString()=="") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);

                if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                }
                else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            }
            else {
                todayString=params.get("period").toString();
            }

            String headerValue = "attachment; filename=PlanoIFRS9_" + todayString + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<PlainIFRS9> cuadreValList = validationIFRS9.getPlainIFRS9(todayString);
            PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(cuadreValList,null);
            listReport.export(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/ifrs9/plainIFRS9/downloadpi")
    public void downloadPlanos(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= PlanosIntergrupo_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        //List<PlainIFRS9Intergroup> intList = validationIFRS9.getAllFromPlanos(params.get("period").toString());
        List<Object[]> intList = validationIFRS9.getAllFromPlanosObject(params.get("period").toString());

        Intergrupo3PListReport listReport = new Intergrupo3PListReport(intList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs9/plainIFRS9/generatePYG")
    public void downloadPyg(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= PYG_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> intList = validationIFRS9.getAllFromPYG(params.get("period").toString());
        List<Object[]> intListFuente = validationIFRS9.getAllFromPYGFuente(params.get("period").toString());
        PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(null,intList);
        listReport.exportPyg(response,null,intListFuente);
    }

    @GetMapping(value = "/ifrs9/plainIFRS9/listNeoconValidation")
    public void downloadValidation(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Listado" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<String> intList = validationIFRS9.listValidationNeocon(params.get("period").toString());
        PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(null,null);
        listReport.exportList(response,intList);
    }

    @GetMapping(value = "/ifrs9/plainIFRS9/generateRepos")
    public void downloadRepos(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Repos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> intList = validationIFRS9.getAllFromRepos(params.get("period").toString());
        List<String> intListType = validationIFRS9.getAllFromResposType(params.get("period").toString());
        PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(null,intList);
        listReport.exportPyg(response,intListType,null);
    }

    @GetMapping(value = "/ifrs9/plainIFRS9/generateImpuestos")
    public void downloadImpuestos(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Impuestos_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Object[]> intList = validationIFRS9.getAllFromImpuestos(params.get("period").toString());
        PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(null,intList);
        listReport.exportPyg(response,null,null);
    }

    @RequestMapping(value = "/ifrs9/plainIFRS9/downloadp")
    @ResponseBody
    public void exportNeocon(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        try {
            if(params.get("type")!=null && params.get("period")!=null) {
                response.setContentType("text/plain");

                String period = params.get("period").toString();

                String[] periodSep = period.split("-");

                String type = "";
                String type2 = "";

                if(params.get("type").equals("PROV")){
                    type = "PROV";
                    type2 = "'PROV','PYG','IMPUE','IMPUEP'";
                }else if(params.get("type").equals("REC")){
                    type = "RECLA";
                    type2 = "'REC'";
                }else if(params.get("type").equals("PYG")){
                    type = "REPOS";
                    type2 = "'PYG REPOS','REPOS'";
                }else{
                    type = "INTER";
                    type2 = "'INTER'";
                }

                response.setHeader("Content-Disposition", "attachment;  filename="+periodSep[1]+"-"+periodSep[0]+" PLANTILLA " +type+ ".txt");

                List<Object[]> plainList = validationIFRS9.getPlain(period, type2);
                ArrayList<String> plain = new ArrayList<>();
                for (Object row : plainList) {
                    plain.add(row.toString());
                }

                CsvService.downloadCsvCargaMasiva(response.getWriter(), plain);
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @RequestMapping(value = "/ifrs9/intergroupUpload/upload")
    public ModelAndView uploadData(HttpServletRequest request, @RequestParam(value = "file") MultipartFile[] files){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");

        String period = request.getParameter("period");

        String todayString="";
        if (period == null || Objects.equals(period, "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        } else {
            todayString = period;
        }

        try{

            List<Object[]> currents = validationIFRS9.validateCurrents(period);

            if(!currents.isEmpty()) {
                boolean result = validationIFRS9.uploadFilesIntergroup(todayString, files);
                if(result) {
                    modelAndView.addObject("resp", "correctPlainInter");
                }else{
                    modelAndView.addObject("resp", "errorDatePlainInter");
                }
            }else{
                modelAndView.addObject("resp", "errorCurrPlainInter");
            }
        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","fail");
        }

        modelAndView.addObject("period", todayString);

        return modelAndView;
    }

    @GetMapping(value = "/ifrs9/clearInterPlaneP")
    public ModelAndView clearInterPlane(@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String period = params.get("period").toString();

        validationIFRS9.clearInterPlane(period);
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs9/plainIFRS9");
        return  modelAndView;
    }

    @RequestMapping(value = "/ifrs9/plainIFRS9/downloadResumen")
    @ResponseBody
    public void exportAllCuadreResumen(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/octet-stream");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";

            String todayString = "";
            String typeData = params.get("type").toString();

            if(params.get("period")!=null && params.get("period").toString()=="") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);

                if(calendar.get(Calendar.MONTH)==0)
                {
                    calendar.add(Calendar.YEAR,-1);
                    todayString = calendar.get(Calendar.YEAR) + "-12";
                }
                else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                }
                else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            }
            else {
                todayString=params.get("period").toString();
            }

            String headerValue = "attachment; filename=Resumen_IFRS9_" + typeData + "_" + todayString + "_" + currentDateTime + ".xlsx";
            response.setHeader(headerKey, headerValue);
            List<Object[]> cuadreValList = validationIFRS9.getResumenIFRS9(todayString,typeData);
            PlainIFRS9ListReport listReport = new PlainIFRS9ListReport(null,cuadreValList);
            listReport.exportResumen(response);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
}