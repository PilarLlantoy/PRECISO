package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.Signature;
import com.inter.proyecto_intergrupo.model.parametric.ThirdsCc;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SignatureService;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdsCcListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ThirdsCcService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class SignatureController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private SignatureService signatureService;

    private List<String> listColumns=List.of("Nombre", "Cargo", "Teléfono","Correo","Dirección");

    @GetMapping(value="/parametric/signature")
    public ModelAndView showSignature(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getUsuario(),"Ver Parametrica Firmas")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
            Page<Signature> pageSignature = signatureService.getAll(pageRequest);
            int totalPage = pageSignature.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("allSignature", pageSignature.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("filterExport", "Original");
            modelAndView.addObject("directory", "signature");
            modelAndView.addObject("registers",pageSignature.getTotalElements());
            modelAndView.addObject("userName", user.getNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("parametric/signature");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/signature")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
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
            ArrayList<String[]> list = signatureService.saveFileBD(fileContent,user);
            String[] part = list.get(0);

            if(part[2].equals("SUCCESS")){
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else{
                SignatureListReport signatureListReport = new SignatureListReport(list,null);
                signatureListReport.exportLog(response);
            }

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/modifySignature/{id}")
    @ResponseBody
    public ModelAndView modifySignature(@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Long idTemp = null;
        try{
            idTemp=Long.parseLong(id);

        }catch (Exception e)
        {
            e.printStackTrace();
        }
        Signature toModify = signatureService.findByIdFirma(idTemp);
        modelAndView.addObject("signatureModify",toModify);
        modelAndView.setViewName("parametric/modifySignature");
        return modelAndView;
    }

    @PostMapping(value="/parametric/loadSignature/{id}")
    public ModelAndView loadFileSignature(HttpServletRequest request, HttpServletResponse response,@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            Part filePart = request.getPart("file1");
            InputStream fileContent = filePart.getInputStream();
            signatureService.saveSignatureImage(fileContent,user,id);
            modelAndView.addObject("resp", "Firma1");;
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

    @PostMapping(value = "/parametric/modifySignature")
    @ResponseBody
    public ModelAndView updateSignature(@ModelAttribute Signature signature){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            signatureService.modifySignature(signature, user);
            modelAndView.addObject("resp", "Modify1");
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }

    @GetMapping(value="/parametric/addSignature")
    public ModelAndView showAddSignature(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        Signature signature = new Signature();
        modelAndView.addObject("signature", signature);
        modelAndView.setViewName("parametric/addSignature");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addSignature")
    public ModelAndView createNewSignature(@ModelAttribute Signature signature) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            signatureService.saveSignature(signature, user);
            modelAndView.addObject("resp", "Add1");
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeSignature/{id}")
    @ResponseBody
    public ModelAndView removeSignature(@PathVariable String id){

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Signature toRemove = signatureService.findByIdFirma(Long.parseLong(id));
        signatureService.removeSignature(toRemove.getIdFirma(), user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/clearSignature")
    public ModelAndView clearSignature(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        signatureService.clearSignature(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/signature/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Firmas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Signature> signatureList= new ArrayList<Signature>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            signatureList = signatureService.findAll();
        }
        else{
            signatureList = signatureService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        SignatureListReport listReport = new SignatureListReport(null,signatureList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchSignature")
    @ResponseBody
    public ModelAndView searchSignature(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Signature> list=signatureService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Signature> pageThird = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageThird.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allSignature",pageThird.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchSignature");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/signature");
        return modelAndView;
    }

    @GetMapping(value="/parametric/downloadImage/{id}")
    public ModelAndView downloadImage(HttpServletRequest request, HttpServletResponse response,@PathVariable String id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/signature");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=Image" + currentDateTime + ".png";
        response.setHeader(headerKey, headerValue);
        try {
            Signature signature =signatureService.findByIdFirma(Long.parseLong(id));
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(signature.getFirma());
            outputStream.close();

        }catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return  modelAndView;
    }

}