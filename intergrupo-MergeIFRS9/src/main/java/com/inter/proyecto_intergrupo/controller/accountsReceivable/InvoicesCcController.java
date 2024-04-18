package com.inter.proyecto_intergrupo.controller.accountsReceivable;

import com.inter.proyecto_intergrupo.model.accountsReceivable.AccountCc;
import com.inter.proyecto_intergrupo.model.accountsReceivable.InvoicesCc;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.dataquality.PointRulesDQ;
import com.inter.proyecto_intergrupo.model.dataquality.RulesDQ;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.service.accountsReceivable.InvoicesCcListReport;
import com.inter.proyecto_intergrupo.service.accountsReceivable.InvoicesCcService;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.dataqualityServices.PointRulesDQListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQListReport;
import com.inter.proyecto_intergrupo.service.dataqualityServices.RulesDQService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class InvoicesCcController {

    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private InvoicesCcService invoicesCcService;

    @Autowired
    private SignatureService signatureService;

    @GetMapping(value="/accountsReceivable/invoicesCc")
    public ModelAndView showInvoicesCc(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Informe Cuentas Por Cobrar")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
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

            List<String[]> listData1 = invoicesCcService.getAllData1(todayString);
            List<String[]> listData3 = invoicesCcService.getAllData3(todayString);
            List<InvoicesCc> listData2 = invoicesCcService.getAllData2(todayString);
            List<Signature> listSignatures = signatureService.findAllFirma();
            boolean cargar = invoicesCcService.validarCargar(todayString);

            modelAndView.addObject("allRulesData1", listData1);
            modelAndView.addObject("allRulesData2", listData2);
            modelAndView.addObject("allRulesData3", listData3);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("vFilter", todayString);
            modelAndView.addObject("sequenceData", Integer.parseInt(invoicesCcService.sequenceMax()) +1);
            modelAndView.addObject("cargar", cargar);
            modelAndView.addObject("signatures", listSignatures);
            modelAndView.addObject("directory", "invoicesCc");

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("accountsReceivable/invoicesCc");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/accountsReceivable/invoicesCc")
    @ResponseBody
    public void uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/invoicesCc");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ArrayList<String[]> list = invoicesCcService.saveFileBD(fileContent,params.get("period").toString());
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                invoicesCcService.loadAudit(user, "Cargue exitoso plantilla Cuentas Por Cobrar");
                modelAndView.addObject("resp", "AddRep1");
                response.setHeader("Content-Disposition", "attachment;  filename = Carga Masiva Cuentas_" + currentDateTime + ".csv");
                List<Object[]> result = invoicesCcService.generateMassiveCharge(params.get("period").toString());
                CsvService.downloadMassiveChargeCC(response.getWriter(), result);
            }
            else{
                String headerKey = "Content-Disposition";
                String headerValue = "attachment; filename=Log_Cargue_" + currentDateTime + ".xlsx";
                response.setHeader(headerKey, headerValue);
                invoicesCcService.loadAudit(user, "Cargue fallido plantilla Cuentas Por Cobrar");
                RulesDQListReport rulesDQListReport = new RulesDQListReport(list,null);
                rulesDQListReport.exportLog(response);
            }

            modelAndView.addObject("period",params.get("period").toString());
            modelAndView.addObject("vFilter",params.get("period").toString());

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        //return  modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/invoicesCc/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CuentasPorCobrar_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<InvoicesCc> templateList = invoicesCcService.getAllData2(params.get("period").toString());
        InvoicesCcListReport listReport = new InvoicesCcListReport(null,templateList);
        listReport.export(response);
    }

    @GetMapping(value = "/accountsReceivable/invoicesCc/downloadPending")
    @ResponseBody
    public void exportToExcelPending(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Cuentas_Pendientes_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<InvoicesCc> templateList = invoicesCcService.getInvoicePending();
        InvoicesCcListReport listReport = new InvoicesCcListReport(null,templateList);
        listReport.export(response);
    }

    @GetMapping(value = "/accountsReceivable/anulInvoicesCc/{id}")
    @ResponseBody
    public ModelAndView anularInvoiceCc(@PathVariable String id,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/invoicesCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        invoicesCcService.anularFactura(idTemp);
        invoicesCcService.loadAudit(user, "Anulación de Registro "+idTemp);
        return modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/showInvoicesCc/{id}")
    @ResponseBody
    public ModelAndView downloadWordInvoiceCc(HttpServletResponse response,@PathVariable String id,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/invoicesCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());

        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Invoice_" + currentDateTime + ".docx";
        response.setHeader(headerKey, headerValue);

        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        invoicesCcService.downloadFactura(idTemp,response);
        return modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/proccessInvoicesCc")
    @ResponseBody
    public ModelAndView processInvoiceCc(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/invoicesCc");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("period",params.get("period").toString());
        try {
            boolean result = invoicesCcService.procesarData(params.get("period").toString(), Integer.parseInt(params.get("secuence").toString()),params.get("firmaSelect").toString());
            if(result) {
                modelAndView.addObject("resp", "InvCC1");
                invoicesCcService.loadAudit(user, "Procesamiento exitoso de lote "+params.get("secuence").toString());
            }
            else {
                modelAndView.addObject("resp", "InvCC-1");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/accountsReceivable/proccessInvoicesCcMassive")
    @ResponseBody
    public ModelAndView exportMasivaCsv(HttpServletResponse response, @RequestParam Map<String, Object> params,@RequestParam(value = "opciones",required = false) String[] opcionesLista) throws IOException, ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/accountsReceivable/invoicesCc");
        response.setContentType("text/plain");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        if (opcionesLista != null)
        {
            response.setHeader("Content-Disposition", "attachment;  filename = Carga Masiva Cuentas_" + currentDateTime + ".csv");
            List<Object[]> result = invoicesCcService.generateMassiveCharge(params.get("period").toString(), opcionesLista);
            CsvService.downloadMassiveChargeCC(response.getWriter(), result);
            return null;
        }
        else
        {
            modelAndView.addObject("resp", "InvCC-2");
            modelAndView.addObject("period", params.get("period").toString());
            return modelAndView;
        }
    }

}
