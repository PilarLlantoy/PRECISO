package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.information.ChangeAccountQuery;
import com.inter.proyecto_intergrupo.model.parametric.MarcacionConcil;
import com.inter.proyecto_intergrupo.model.parametric.Query;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.QueryService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class QueryController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private QueryService queryService;

    @GetMapping(value="/parametric/query")
    public ModelAndView uploadFileProvisions(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Query")) {

            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            String todayString = "";
            String todayStringHoy = "";

            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayStringHoy = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayStringHoy = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
            } else {
                todayStringHoy = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }

            if (params.get("period") == null || params.get("period").toString() == "") {
                if(calendar.get(Calendar.MONTH)==0)
            {
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

            if (todayStringHoy.equals(todayString)) {
                modelAndView.addObject("apply", true);
            } else {
                modelAndView.addObject("apply", false);
            }

            List<String[]> list = queryService.getFechas(todayString);
            List<String> listConcil = queryService.getFechasConcil(todayString);

            List<String[]> listProceso = queryService.getFechasProceso(todayString);
            List<String> listProcesoConcil = queryService.getFechasProcesoConcil(todayString);

            String dataLC = "Sin Asignación";
            String dataLPC = "Sin Asignación";

            if(listProcesoConcil.isEmpty() == false) {
                dataLC = listConcil.get(0).toString();
                dataLPC = listProcesoConcil.get(0).toString();
            }

            List<String> nombreGof = queryService.getNombreGof(todayString);
            modelAndView.addObject("listGof", nombreGof);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("listDates", list);
            modelAndView.addObject("listDatesCon", dataLC);
            modelAndView.addObject("listDatesaP", listProceso);
            modelAndView.addObject("listDatesaPCon", dataLPC);

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/query");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/query/queryCheckLocal")
    public ModelAndView marcarLocal(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (params.get("type") != null)
        {
            queryService.markLocal(params.get("type").toString(),params.get("period").toString());
        }
        String todayString =params.get("period").toString();
        List<String[]> list = queryService.getFechas(todayString);
        List<String> listConcil = queryService.getFechasConcil(todayString);

        List<String[]> listProceso = queryService.getFechasProceso(todayString);
        List<String> listProcesoConcil = queryService.getFechasProcesoConcil(todayString);

        String dataLC = "Sin Asignación";
        String dataLPC = "Sin Asignación";

        if(listProcesoConcil.isEmpty() == false) {
            dataLC = listConcil.get(0).toString();
            dataLPC = listProcesoConcil.get(0).toString();
        }

        List<String> nombreGof = queryService.getNombreGof(todayString);
        modelAndView.addObject("listGof", nombreGof);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("listDates", list);
        modelAndView.addObject("listDatesCon", dataLC);
        modelAndView.addObject("listDatesaP", listProceso);
        modelAndView.addObject("listDatesaPCon", dataLPC);
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/query");
        return  modelAndView;
    }

    @GetMapping(value="/parametric/query/queryCheckIfrs9")
    public ModelAndView marcarIfrs9(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (params.get("type") != null) {
            queryService.markIfrs9(params.get("type").toString(),params.get("period").toString());
        }
        String todayString =params.get("period").toString();
        List<String[]> list = queryService.getFechas(todayString);
        List<String> listConcil = queryService.getFechasConcil(todayString);

        List<String[]> listProceso = queryService.getFechasProceso(todayString);
        List<String> listProcesoConcil = queryService.getFechasProcesoConcil(todayString);

        String dataLC = "Sin Asignación";
        String dataLPC = "Sin Asignación";

        if(listProcesoConcil.isEmpty() == false) {
            dataLC = listConcil.get(0).toString();
            dataLPC = listProcesoConcil.get(0).toString();
        }

        List<String> nombreGof = queryService.getNombreGof(todayString);
        modelAndView.addObject("listGof", nombreGof);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("listDates", list);
        modelAndView.addObject("listDatesCon", dataLC);
        modelAndView.addObject("listDatesaP", listProceso);
        modelAndView.addObject("listDatesaPCon", dataLPC);
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/query");
        return  modelAndView;
    }

    @GetMapping(value="/parametric/query/clearMarcadoRP")
    public ModelAndView desmarcarIfrs9(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (params.get("type") != null)
            queryService.desmarkIfrs9(params.get("type").toString(),params.get("period").toString());
        String todayString =params.get("period").toString();
        List<String[]> list = queryService.getFechas(todayString);
        List<String> listConcil = queryService.getFechasConcil(todayString);

        List<String[]> listProceso = queryService.getFechasProceso(todayString);
        List<String> listProcesoConcil = queryService.getFechasProcesoConcil(todayString);

        String dataLC = "Sin Asignación";
        String dataLPC = "Sin Asignación";

        if(listProcesoConcil.isEmpty() == false) {
            dataLC = listConcil.get(0).toString();
            dataLPC = listProcesoConcil.get(0).toString();
        }

        List<String> nombreGof = queryService.getNombreGof(todayString);
        modelAndView.addObject("listGof", nombreGof);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("listDates", list);
        modelAndView.addObject("listDatesCon", dataLC);
        modelAndView.addObject("listDatesaP", listProceso);
        modelAndView.addObject("listDatesaPCon", dataLPC);
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/query");
        return  modelAndView;
    }

    @GetMapping(value="/parametric/query/clearMarcadoRPCon")
    public ModelAndView desmarcarIfrs9Concil(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (params.get("type").equals("1"))
            queryService.desmarkIfrs9Concil(params.get("period").toString());
        else
            queryService.markIfrs9Concil(params.get("period").toString(),queryService.getUltimoGof());
        String todayString =params.get("period").toString();
        List<String[]> list = queryService.getFechas(todayString);
        List<String> listConcil = queryService.getFechasConcil(todayString);

        List<String[]> listProceso = queryService.getFechasProceso(todayString);
        List<String> listProcesoConcil = queryService.getFechasProcesoConcil(todayString);

        String dataLC = "Sin Asignación";
        String dataLPC = "Sin Asignación";

        if(listProcesoConcil.isEmpty() == false) {
            dataLC = listConcil.get(0).toString();
            dataLPC = listProcesoConcil.get(0).toString();
        }

        List<String> nombreGof = queryService.getNombreGof(todayString);
        modelAndView.addObject("listGof", nombreGof);
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("listDates", list);
        modelAndView.addObject("listDatesCon", dataLC);
        modelAndView.addObject("listDatesaP", listProceso);
        modelAndView.addObject("listDatesaPCon", dataLPC);
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/query");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/query/downloadAyer")
    @ResponseBody
    public void exportToExcelAyer(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Query_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Query> queryList= queryService.getAllAyer();
        QueryListReport listReport = new QueryListReport(queryList,null);
        listReport.export(response);
    }

    @GetMapping(value = "/reports/validateCodicons")
    @ResponseBody
    public void validateCodes(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Codicons_"+params.get("periodValidate").toString()+"_VS_"+params.get("period").toString()+"_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        queryService.validateCodicons(params.get("periodValidate").toString(),params.get("period").toString());
        List<ChangeAccountQuery> queryList = queryService.getAccountsCodicons(params.get("period").toString());
        QueryListReport listReport = new QueryListReport(null,queryList);
        listReport.exportCodicons(response);
    }

    @GetMapping(value = "/parametric/query/downloadHoy")
    @ResponseBody
    public void exportToExcelToday(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Query_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Query> queryList= queryService.getAllHoy();
        QueryListReport listReport = new QueryListReport(queryList,null);
        listReport.export(response);
    }


    @GetMapping(value = "/parametric/query/downloadMarcadoLocal")
    @ResponseBody
    public void exportToExcelMarcadoLocal(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Query_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Query> queryList= queryService.getAllMarcado("LOCAL",params.get("period").toString());
        QueryListReport listReport = new QueryListReport(queryList,null);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/query/downloadMarcadoRP")
    @ResponseBody
    public void exportToExcelMarcadoRP(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Query_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);

        List<Query> queryList= queryService.getAllMarcado("IFRS9",params.get("period").toString());
        QueryListReport listReport = new QueryListReport(queryList,null);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/query/downloadMarcadoRPCon")
    @ResponseBody
    public void exportToExcelMarcadoRPCon(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Conci_Marcacion_" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);

        List<MarcacionConcil> concilList= queryService.getAllConciliacionMarcado(params.get("period").toString());
        CsvService.downloadTxtMarcacion(response.getWriter(), concilList);
        /*QueryListReport listReport = new QueryListReport(null,null);
        listReport.exportConcil(response,concilList);*/
    }

    @PostMapping(value="/parametric/query/load")
    public ModelAndView uploadFileCred(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/query");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        String todayStringHoy="";

        if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayStringHoy = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
            todayStringHoy = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
        }
        else {
            todayStringHoy = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
        }

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            try {
                String[] resp = queryService.saveFileBDManual(fileContent, params.get("typemark").toString(),params.get("period").toString(),user);
                if(resp[0].equals("true") && resp[1].equals("true")){
                    modelAndView.addObject("resp","true");
                } else if(!resp[0].equals("true")){
                    modelAndView.addObject("resp","false");
                    modelAndView.addObject("mess",resp[0]);
                    modelAndView.addObject("cause","fecha");
                }
                else if(!resp[1].equals("true")){
                    modelAndView.addObject("resp","false");
                    modelAndView.addObject("cause","div");
                    modelAndView.addObject("mess",resp[1]);
                }

                String todayString =params.get("period").toString();
                List<String[]> list = queryService.getFechas(todayString);
                List<String> listConcil = queryService.getFechasConcil(todayString);

                List<String[]> listProceso = queryService.getFechasProceso(todayString);
                List<String> listProcesoConcil = queryService.getFechasProcesoConcil(todayString);

                String dataLC = "Sin Asignación";
                String dataLPC = "Sin Asignación";

                if(listProcesoConcil.isEmpty() == false) {
                    dataLC = listConcil.get(0).toString();
                    dataLPC = listProcesoConcil.get(0).toString();
                }

                List<String> nombreGof = queryService.getNombreGof(todayString);
                modelAndView.addObject("listGof", nombreGof);
                modelAndView.addObject("period", todayString);
                modelAndView.addObject("listDates", list);
                modelAndView.addObject("listDatesCon", dataLC);
                modelAndView.addObject("listDatesaP", listProceso);
                modelAndView.addObject("listDatesaPCon", dataLPC);

                if(todayStringHoy.equals(params.get("period").toString())){
                    modelAndView.addObject("apply",true);
                }
                else
                {
                    modelAndView.addObject("apply",false);
                }
            }catch (Exception e)
            {
                e.printStackTrace();

            }

        }catch(Exception e){
            e.printStackTrace();
            return  modelAndView;
        }
        //modelAndView.addObject("period",params.get("period").toString());
        return  modelAndView;
    }
}
