package com.inter.proyecto_intergrupo.controller.reports;

import com.inter.proyecto_intergrupo.model.admin.Audit;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Country;
import com.inter.proyecto_intergrupo.model.parametric.Currency;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.model.parametric.YntpSociety;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV1Final;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV2;
import com.inter.proyecto_intergrupo.model.reports.IntergrupoV3;
import com.inter.proyecto_intergrupo.repository.admin.AuditRepository;
import com.inter.proyecto_intergrupo.repository.parametric.YntpSocietyRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ConciliacionDiffListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ConciliacionListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo1ListReport;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo1Service;
import com.inter.proyecto_intergrupo.service.reportsServices.Intergrupo3ListReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class Intergrupo1Controller {
    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns = List.of("Cuenta", "Contrato", "Nit", "Cod Neocon", "Divisa", "YNTP", "Sociedad", "Cod Pais", "Pais");

    @Autowired
    Intergrupo1Service intergrupo;

    @Autowired
    private UserService userService;

    @Autowired
    private AuditRepository auditRepository;

    @Autowired
    private YntpSocietyRepository yntpSocietyRepository;

    @Autowired
    private statusInfoRepository StatusInfoRepository;

    @GetMapping(value = "/reports/intergroup1")
    public ModelAndView getIntergrupo1(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Intergrupo 1 Banco")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            String todayString;
            String Id;
            String Vf;
            if (params.get("vId") == null) {
                Id = "";
            } else {
                Id = params.get("vId").toString();
            }
            if (params.get("vFilter") == null || (params.get("vFilter") != null && params.get("vFilter").toString() != null && params.get("vFilter").toString().equals(""))) {
                Vf = "Seleccione un filtro";
            } else {
                Vf = params.get("vFilter").toString();
            }
            if (params.get("page") != null && params.get("page").toString() != null) {
                modelAndView.addObject("page", params.get("page").toString());
            }

            if (params.get("period") == null || (params.get("period") != null  && params.get("period").toString() == "")) {
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
                todayString = params.get("period").toString();
            }
            intergrupo.getAllFromV1(todayString);
            List<IntergrupoV1> list;
            List<IntergrupoV1> listDef =intergrupo.getAllFromV1FinalAju(todayString);
            if(listDef.isEmpty())
            {
                if (Id.equals("") | Vf.equals("Seleccione un filtro")) {
                    list = intergrupo.getAllFromV1(todayString);
                } else {
                    list = intergrupo.findByFilter(Id, Vf, todayString);
                }
            }
            else
            {
                if (Id.equals("") | Vf.equals("Seleccione un filtro")) {
                    list = listDef;
                } else {
                    list = intergrupo.findByFilterDef(Id, Vf, todayString);
                }
            }

            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<IntergrupoV1> pageInter = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageInter.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            List<Object[]> validateInterAjuV1 = intergrupo.validateTableInterV1Aju(todayString);

            if (!validateInterAjuV1.isEmpty()) {
                modelAndView.addObject("validateInterAjuV1", true);
                Date dateInterAjuV1 = new Date();
                try {
                    dateInterAjuV1 = StatusInfoRepository.findByInputAndPeriodo("INTER_V1_AJU", todayString).getFecha();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateInterAjuV1);
                modelAndView.addObject("dateInterAjuV1", dateAsString);
            } else {
                modelAndView.addObject("validateInterAjuV1", false);
            }

            modelAndView.addObject("intergroup", pageInter.getContent());
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("vId", Id);
            modelAndView.addObject("vFilter", Vf);
            modelAndView.addObject("directory", "intergroup1");
            modelAndView.addObject("columns", listColumns);

            modelAndView.setViewName("reports/intergroup1");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/reports/intergroup1/update")
    public ModelAndView updateParametric(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1");

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        String todayString="";
        if(params.get("period") ==null || params.get("period").toString()=="") {
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

        intergrupo.getFromRP21(todayString);

        List<IntergrupoV1> result = intergrupo.getAllFromV1(todayString);


        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), result.size());
        Page<IntergrupoV1> pageInter = new PageImpl<>(result.subList(start, end), pageRequest, result.size());

        int totalPage=pageInter.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }

        modelAndView.addObject("intergroup",pageInter.getContent());
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",todayString);
        modelAndView.addObject("directory","intergroup1");

        return modelAndView;

    }

    @GetMapping(value = "/reports/intergroup1/download")
    public void downloadIntergrupoV1(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV1> intList = intergrupo.getAllFromV1(params.get("period").toString());

        Intergrupo1ListReport listReport = new Intergrupo1ListReport(intList);
        listReport.export(response,null);
    }

    @GetMapping(value = "/reports/intergroup1/downloadFinalAjustes")
    public void downloadIntergrupoV1FAju(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= IntergrupoV1ConAjustes_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV1> intList = intergrupo.getAllFromV1FinalAju(params.get("period").toString());
        List<IntergrupoV1> intListN = intergrupo.getAllFromV1FinalAjuN(params.get("period").toString());

        Intergrupo1ListReport listReport = new Intergrupo1ListReport(intList);
        listReport.export(response,intListN);
    }

    @GetMapping(value = "/reports/intergroup1/downloadFinal")
    public void downloadIntergrupoV1F(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, ParseException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename= Ajustes_IntergrupoV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<IntergrupoV1Final> intList = intergrupo.getAllFromV1Final(params.get("period").toString());

        Intergrupo1ListReport listReport = new Intergrupo1ListReport(null);
        listReport.exportFinal(response, intList);
    }

    @GetMapping(value = "/reports/intergroup1/modify")
    @ResponseBody
    public ModelAndView modifyInt(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());

        List<IntergrupoV1> listDef = intergrupo.getAllFromV1FinalAju(params.get("period").toString());
        IntergrupoV1 toModify = new IntergrupoV1();
        if (listDef.isEmpty())
        {
            toModify = intergrupo.findIntergrupo1(Integer.parseInt(params.get("id").toString())).get(0);
        }
        else
        {
            toModify = intergrupo.findIntergrupo1Aju(Integer.parseInt(params.get("id").toString())).get(0);
        }
        List<Currency> divisas = intergrupo.getDivisas();
        List<Country> paises = intergrupo.getPaises();

        modelAndView.addObject("divisas",divisas);
        modelAndView.addObject("paises",paises);
        if(params.get("vId") != null && params.get("vId").toString() != null)
            modelAndView.addObject("vId", params.get("vId").toString());
        if(params.get("vFilter") != null && params.get("vFilter").toString() != null)
            modelAndView.addObject("vFilter", params.get("vFilter").toString());
        if(params.get("page") != null && params.get("page").toString() != null)
            modelAndView.addObject("page", params.get("page").toString());
        modelAndView.addObject("intModify",toModify);
        modelAndView.setViewName("reports/modifyIntergroup1");
        return modelAndView;
    }

    @GetMapping(value = "/reports/intergroup1/processInterV1")
    public ModelAndView processIntergroupV1(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            intergrupo.processAjuInterV1(params.get("period").toString());
            intergrupo.auditCode("Procesamiento Exitoso Ajustes Intergrupo V1", user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            intergrupo.auditCode("Procesamiento Fallido Ajustes Intergrupo V1", user);
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @PostMapping(value = "/reports/intergroup1/modify")
    @ResponseBody
    public ModelAndView updateInt(@ModelAttribute IntergrupoV1 intergrupoV1,@Param(value = "id") Integer id,@Param(value = "vId") String vId,@Param(value = "vFilter") String vFilter,@Param(value = "page") String page){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            IntergrupoV1 temp = intergrupo.findIntergrupo1(id).get(0);
            List<IntergrupoV1> listDef = intergrupo.getAllFromV1FinalAju(temp.getPeriodo());
            IntergrupoV1 search = new IntergrupoV1();
            if (listDef.isEmpty())
            {
                search = intergrupo.findIntergrupo1(id).get(0);
            }
            else
            {
                search = intergrupo.findIntergrupo1Aju(id).get(0);
            }
            if (search!=null)
            {
                YntpSociety yntp = yntpSocietyRepository.findByYntp(intergrupoV1.getYntp());
                List<Object[]> cuenta = intergrupo.getCuenta(intergrupoV1.getCuentaLocal());
                List<Object[]> codicons = intergrupo.getCodiCons(intergrupoV1.getCodNeocon());

                if(yntp==null){

                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1/modify/"+id);
                    modelAndView.addObject("resp", "ErrorIntV2-0");
                    modelAndView.addObject("intModify", intergrupoV1);
                    return  modelAndView;

                } else if(cuenta.isEmpty()){

                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1/modify/"+id);
                    modelAndView.addObject("resp", "ErrorIntV2-1");
                    modelAndView.addObject("intModify", intergrupoV1);
                    return  modelAndView;

                }else if(codicons.isEmpty()){

                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1/modify/"+id);
                    modelAndView.addObject("resp", "ErrorIntV2-2");
                    modelAndView.addObject("intModify", intergrupoV1);
                    return  modelAndView;

                } else {

                    if(listDef.isEmpty())
                        intergrupo.modifyIntergrupoV1(intergrupoV1, id);
                    else
                        intergrupo.modifyIntergrupoV1Aju(intergrupoV1, id);
                    ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1");

                    if(vId != null && !vId.equals(""))
                        modelAndView.addObject("vId", vId);
                    if(vFilter != null && !vFilter.equals(""))
                        modelAndView.addObject("vFilter", vFilter);
                    if(page != null && !page.equals("") && !page.equals("0")) {
                        System.out.println(page);
                        modelAndView.addObject("page", page);
                    }

                    modelAndView.addObject("resp", "Modify1");

                    Date today = new Date();
                    Audit insert = new Audit();
                    insert.setAccion("Edición Manual Registros Intergrupo V1");
                    insert.setCentro(user.getCentro());
                    insert.setComponente("INTERGRUPO V1");
                    insert.setFecha(today);
                    insert.setInput("INTERGRUPO V1");
                    insert.setNombre(user.getPrimerNombre());
                    insert.setUsuario(user.getUsuario());
                    auditRepository.save(insert);
                    return  modelAndView;
                }
            }
            else
            {
                ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1");
                modelAndView.addObject("resp", "Modify0");
                return  modelAndView;
            }
        }
        catch(Exception e){
            e.printStackTrace();
            ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup2");
            modelAndView.addObject("resp", "General-1");
            return  modelAndView;
        }
    }

    @GetMapping(value="/reports/intergroup1/addIntergurpo1")
    public ModelAndView showAddIntergurpoV1(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        IntergrupoV1 intergrupoV1 = new IntergrupoV1();
        intergrupoV1.setPeriodo(params.get("period").toString());
        intergrupoV1.setFuente("MANUAL");
        intergrupoV1.setComponente("INTERAZ MANUAL");
        intergrupoV1.setInput("INTERAZ MANUAL");

        List<Currency> divisas = intergrupo.getDivisas();
        List<Country> paises = intergrupo.getPaises();
        List<YntpSociety> yntps = intergrupo.getYntps();

        modelAndView.addObject("divisas",divisas);
        modelAndView.addObject("paises",paises);
        modelAndView.addObject("yntps",yntps);
        modelAndView.addObject("intergrupoV1", intergrupoV1);
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.setViewName("reports/addIntergroup1");
        return modelAndView;
    }
    @PostMapping(value = "/reports/intergroup1/addIntergrupo1")
    public ModelAndView createNewIntergurpoV1(@ModelAttribute IntergrupoV1 intergrupoV1) {
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try{
            intergrupoV1.setPais(intergrupo.getPais(intergrupoV1.getCodPais()).get(0).getNombre());
            intergrupo.addInfo(intergrupoV1,user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @PostMapping(value="/reports/intergroup1/loadFinal")
    public ModelAndView uploadFileFinal(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/reports/intergroup1");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString = "";

        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=logIntergrupoFinalV1_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            List<String[]> list = intergrupo.saveFileFinalIntergrupo(fileContent, user, todayString);
            String[] part=list.get(list.size()-1);
            if(part[2].equals("COMPLETE"))
            {
                Date today = new Date();
                StatusInfo validateStatus = StatusInfoRepository.findByInputAndPeriodo("INTER_V1_AJU", todayString);

                if (validateStatus == null) {
                    StatusInfo status = new StatusInfo();
                    status.setInput("INTER_V1_AJU");
                    status.setPeriodo(todayString);
                    status.setFecha(today);
                    StatusInfoRepository.save(status);
                } else {
                    validateStatus.setFecha(today);
                    StatusInfoRepository.save(validateStatus);
                }
                intergrupo.auditCode("Cargue Exitoso Ajustes Intergrupo V1", user);

                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                intergrupo.auditCode("Cargue Fallido Ajustes Intergrupo V1", user);
                Intergrupo1ListReport report = new Intergrupo1ListReport(null);
                report.exportLog(response, list);
            }

        }catch(Exception e){
            intergrupo.auditCode("Cargue Fallido Ajustes Intergrupo V1", user);
            modelAndView.addObject("resp", "Descon-3");
            e.printStackTrace();
        }
        return  modelAndView;
    }

}
