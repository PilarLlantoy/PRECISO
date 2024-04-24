package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationOther;
import com.inter.proyecto_intergrupo.model.ifrs9.AccountCreationOtherPlane;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.reportNIC34.ParamNIC34;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.AccountCreationOtherListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.AccountCreationOtherService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class AccountCreationOtherController {

    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private AccountCreationOtherService accountCreateOtherService;

    @Autowired
    private SignatureService signatureService;

    @GetMapping(value="/ifrs/accountCreationOther")
    public ModelAndView showAccountCreateOther(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String todayString = "";
        if (params.get("period") == null || params.get("period").toString() == "") {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if (String.valueOf(calendar.get(Calendar.MONTH)+1).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH)+1);
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH)+1);
            }
            if (String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)).length() == 2) {
                todayString = todayString + "-" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            } else {
                todayString = todayString + "-0" + String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            }
        } else {
            todayString = params.get("period").toString();
        }
        modelAndView.addObject("period", todayString);
        modelAndView.addObject("vFilter", todayString);
        modelAndView.addObject("directory", "accountCreationOther");

        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas Otros (General)")) {

            List<AccountCreationOther> listData2 = accountCreateOtherService.getAllData2(todayString);
            boolean cargar = accountCreateOtherService.validarCargar(todayString);

            modelAndView.addObject("allRulesData2", listData2);
            modelAndView.addObject("cargar", cargar);
            modelAndView.addObject("rol", "GENERAL");
            modelAndView.setViewName("ifrs/accountCreationOther");
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas Otros (Consolidación)")) {

            List<AccountCreationOther> listData2 = accountCreateOtherService.getAllDataConsol(todayString);
            modelAndView.addObject("allRulesData2", listData2);
            modelAndView.addObject("cargar", !listData2.isEmpty());
            modelAndView.addObject("rol", "CONSOLIDACION");
            modelAndView.setViewName("ifrs/accountCreationOther");
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas Otros (Control Contable)")) {

            List<AccountCreationOther> listData2 = accountCreateOtherService.getAllDataControl(todayString);
            modelAndView.addObject("allRulesData2", listData2);
            modelAndView.addObject("cargar", !listData2.isEmpty());
            modelAndView.addObject("rol", "CONTROL CONTABLE");
            modelAndView.setViewName("ifrs/accountCreationOther");
        }
        else if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas Otros (Gestión)")) {

            List<AccountCreationOther> listData2 = accountCreateOtherService.getAllDataGestion(todayString);
            modelAndView.addObject("allRulesData2", listData2);
            modelAndView.addObject("cargar", !listData2.isEmpty());
            modelAndView.addObject("rol", "GESTION");
            modelAndView.setViewName("ifrs/accountCreationOther");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value="/ifrs/accountCreationOtherPlane")
    public ModelAndView showAccountCreateOtherPlane(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if(userService.validateEndpointVer(user.getId(),"Ver Creación de Cuentas Otros Planos")) {

            String todayString = "";
            if (params.get("period") == null || ( params.get("period") != null && params.get("period").toString() == "")) {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if (String.valueOf(calendar.get(Calendar.MONTH)+1).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf((calendar.get(Calendar.MONTH)+1));
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf((calendar.get(Calendar.MONTH)+1));
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<AccountCreationOtherPlane> listData = accountCreateOtherService.getAllDataPlane(todayString);
            List<Object[]> listData1 = accountCreateOtherService.getAllDataPlaneResume();
            List<Object[]> listData2 = accountCreateOtherService.getAllDataPlaneResume2(todayString);
            boolean cargar = accountCreateOtherService.validarPlano(todayString);
            modelAndView.addObject("allData", listData);
            modelAndView.addObject("allData1", listData1);
            modelAndView.addObject("allData2", listData2);
            modelAndView.addObject("validar", cargar);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("directory", "accountCreationOtherPlane");
            modelAndView.setViewName("ifrs/accountCreationOtherPlane");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/ifrs/accountCreationOther")
    @ResponseBody
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOther");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = accountCreateOtherService.saveFileBD(fileContent,params.get("period").toString(),user,params.get("rol").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                accountCreateOtherService.loadAudit(user, "Cargue exitoso plantilla Creación de Cuentas");
                modelAndView.addObject("resp", "AddRep1");
            }
            else{
                response.setContentType("application/octet-stream");
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
                String currentDateTime = dateFormatter.format(new Date());
                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
                response.setHeader(headerKey, headerValue);
                accountCreateOtherService.loadAudit(user, "Cargue fallido plantilla Creación de Cuentas");
                RulesDQListReport rulesDQListReport = new RulesDQListReport(list,null);
                rulesDQListReport.exportLog(response);
            }

            modelAndView.addObject("period",params.get("period").toString());
            modelAndView.addObject("vFilter",params.get("period").toString());

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @PostMapping(value="/ifrs/accountCreationOtherPlane")
    @ResponseBody
    public ModelAndView uploadProcess(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOtherPlane");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            modelAndView.addObject("period",params.get("period").toString());
            List<AccountCreationOther> list = accountCreateOtherService.getAllDataForPeriod(params.get("fechaInicio").toString(),params.get("fechaFin").toString());
            if(!list.isEmpty()) {
                accountCreateOtherService.generatePlane(params.get("fechaInicio").toString(),params.get("fechaFin").toString(),list,user);
                accountCreateOtherService.loadAudit(user, "Generación de plano exitoso");
                modelAndView.addObject("resp", "CA11");
            }
            else {
                accountCreateOtherService.loadAudit(user, "Generación de plano fallido");
                modelAndView.addObject("resp", "CA-11");
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @PostMapping(value="/ifrs/accountCreationOther/loadGuia")
    @ResponseBody
    public ModelAndView uploadFileGuia(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOther");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            accountCreateOtherService.saveFileGuia(fileContent,params.get("period").toString(),filePart.getSubmittedFileName());
            accountCreateOtherService.loadAudit(user, "Actualizar documento guía");
            modelAndView.addObject("period",params.get("period").toString());
            modelAndView.addObject("vFilter",params.get("period").toString());
            modelAndView.addObject("resp", "CA10");

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/accountCreationOtherPlane/download/{id}")
    @ResponseBody
    public ModelAndView downloadFile(HttpServletResponse response,@PathVariable String id,@RequestParam Map<String, Object> params) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOtherPlane");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        response.setContentType("application/octet-stream");
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=" + params.get("nom").toString();
        response.setHeader(headerKey, headerValue);
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }

        ServletOutputStream outputStream = response.getOutputStream();
        outputStream.write(accountCreateOtherService.getAccount(idTemp).get(0).getArchivo());
        outputStream.close();
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/accountCreationOther/anulAccount/{id}")
    @ResponseBody
    public ModelAndView anularInvoiceCc(@PathVariable String id,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOther");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        accountCreateOtherService.anularRegistro(idTemp);
        accountCreateOtherService.loadAudit(user, "Anulación de Registro "+idTemp);
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("resp", "AC1");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/accountCreationOther/downloadPending")
    @ResponseBody
    public void exportToExcelPending(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_Pendientes_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountCreationOther> templateList = accountCreateOtherService.getDataPending(params.get("rol").toString());
        AccountCreationOtherListReport listReport = new AccountCreationOtherListReport(null,templateList);
        listReport.export(response);
    }
    @GetMapping(value = "/ifrs/accountCreationOther/downloadGuia")
    @ResponseBody
    public void exportGuia(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException, IOException {
        response.setContentType("application/octet-stream");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        accountCreateOtherService.generateGuia(response);
    }

    @GetMapping(value = "/ifrs/accountCreationOther/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_Actual_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountCreationOther> templateList = accountCreateOtherService.getDataActual(params.get("period").toString());
        AccountCreationOtherListReport listReport = new AccountCreationOtherListReport(null,templateList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/accountCreationOther/downloadPre")
    @ResponseBody
    public void exportToExcelPre(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_Actual_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountCreationOther> templateList = accountCreateOtherService.getDataActual(params.get("period").toString(),params.get("rol").toString());
        AccountCreationOtherListReport listReport = new AccountCreationOtherListReport(null,templateList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/accountCreationOther/downloadAll")
    @ResponseBody
    public void exportToExcelAll(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_Confirmadas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<AccountCreationOther> templateList = accountCreateOtherService.getDataConfirmated();
        AccountCreationOtherListReport listReport = new AccountCreationOtherListReport(null,templateList);
        listReport.export(response);
    }

    @GetMapping(value = "/ifrs/accountCreationOther/proccess")
    @ResponseBody
    public ModelAndView exportMasivaCsv(HttpServletResponse response, @RequestParam Map<String, Object> params,@RequestParam(value = "opciones",required = false) String[] opcionesLista) throws IOException {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOther");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if (opcionesLista != null)
        {
            accountCreateOtherService.generateProcess(params.get("period").toString(), opcionesLista,params.get("rol").toString(),user);
            modelAndView.addObject("resp", "InvCC1");
            modelAndView.addObject("period", params.get("period").toString());
            return modelAndView;
        }
        else
        {
            modelAndView.addObject("resp", "InvCC-2");
            modelAndView.addObject("period", params.get("period").toString());
            return modelAndView;
        }
    }

    @GetMapping(value = "/ifrs/accountCreationOther/modify/{id}")
    @ResponseBody
    public ModelAndView modifyAccount (@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        AccountCreationOther toModify = accountCreateOtherService.findByIdCuenta(idTemp,user);
        modelAndView.addObject("accountModify",toModify);
        modelAndView.setViewName("ifrs/modifyAccountCreationOther");
        return modelAndView;
    }

    @GetMapping(value = "/ifrs/accountCreationOther/respAccount/{id}")
    @ResponseBody
    public ModelAndView respAccountView (@PathVariable String id,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        AccountCreationOther toModify = accountCreateOtherService.findByIdCuenta(idTemp,user);
        modelAndView.addObject("accountModify",toModify);
        modelAndView.addObject("period",params.get("period").toString());
        modelAndView.addObject("rol",params.get("rol").toString());
        modelAndView.setViewName("ifrs/respAccountCreationOther");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/accountCreationOther/modify")
    @ResponseBody
    public ModelAndView updateAccount(@ModelAttribute AccountCreationOther paramAccount){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOther");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCreateOtherService.modifyAccount(paramAccount, user);
            modelAndView.addObject("resp", "Modify1");
            modelAndView.addObject("period", paramAccount.getPeriodo());
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @PostMapping(value = "/ifrs/accountCreationOther/respAccount")
    @ResponseBody
    public ModelAndView updateAccountResp(@ModelAttribute AccountCreationOther paramAccount,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/accountCreationOther");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            String cambio = "";
            AccountCreationOther tempo = accountCreateOtherService.findByIdCuenta(paramAccount.getIdCuentas(),user);
            if(paramAccount.getComentarioControl() != null && !paramAccount.getComentarioControl().equals(tempo.getComentarioControl()))
                cambio = cambio + " CONTROL CONTABLE";
            if(paramAccount.getComentarioGestion() != null && !paramAccount.getComentarioGestion().equals(tempo.getComentarioGestion()))
                cambio = cambio + " GESTION";
            if(paramAccount.getComentarioConsolidacion() != null && !paramAccount.getComentarioConsolidacion().equals(tempo.getComentarioConsolidacion()))
                cambio = cambio + " CONSOLIDACION";

            if(params.get("rol").toString().equals("CONSOLIDACION"))
                paramAccount.setUsuarioConsolidacion(user.getUsuario());
            else if(params.get("rol").toString().equals("CONTROL CONTABLE"))
                paramAccount.setUsuarioControl(user.getUsuario());
            else if(params.get("rol").toString().equals("GESTION"))
                paramAccount.setUsuarioGestion(user.getUsuario());

            accountCreateOtherService.modifyAccount(paramAccount, user);
            accountCreateOtherService.generateProcessResp(paramAccount.getPeriodo(),paramAccount.getIdCuentas(), params.get("rol").toString(),user,cambio);
            modelAndView.addObject("resp", "Modify1");
            modelAndView.addObject("period", paramAccount.getPeriodo());
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

}
