package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.ReposYSimultaneas;
import com.inter.proyecto_intergrupo.model.parametric.GenericAccount;
import com.inter.proyecto_intergrupo.model.parametric.ReposYSimultaneas;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ReposYSimultaneasListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ReposYSimultaneasService;
import com.inter.proyecto_intergrupo.service.parametricServices.ReposYSimultaneasListReport;
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
public class ReposYSimultaneasController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ReposYSimultaneasService reposYSimultaneasService;


    private List<String> listColumns=List.of("Cuenta", "CodNombre", "Codigo", "Descripcion", "CuentaPyG", "DescripcionCuentaPyG");

    @GetMapping(value="/parametric/reposYSimultaneas")
    public ModelAndView showReposYSimultaneas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<ReposYSimultaneas> pageReposYSimultaneas=reposYSimultaneasService.getAll(pageRequest);
        int totalPage=pageReposYSimultaneas.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allReposYSimultaneas",pageReposYSimultaneas.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","reposYSimultaneas");
        List<ReposYSimultaneas> list = reposYSimultaneasService.findAll();
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/reposYSimultaneas");
        return modelAndView;
    }

    @PostMapping(value="/parametric/reposYSimultaneas")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reposYSimultaneas");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ReposYSimultaneas> reposYSimultaneasList = reposYSimultaneasService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            ReposYSimultaneasListReport listReport = new ReposYSimultaneasListReport(reposYSimultaneasList);
            ArrayList<String[]> list=reposYSimultaneasService.saveFileBD(fileContent,user);
            String[] part=list.get(0);
            if(part[2].equals("true"))
            {
                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                listReport.exportLog(response,list);
            }
            else if(part[2].equals("falseFormat"))
            {
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep-1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            }
            else
            {
                int rowTemp=(Integer.parseInt(part[0])+1);
                part[0]=String.valueOf(rowTemp);
                modelAndView.addObject("resp", "AddRep0");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
                return  modelAndView;
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        finally {
            return  modelAndView;
        }
    }

    @GetMapping(value = "/parametric/modifyReposYSimultaneas/{id}")
    @ResponseBody
    public ModelAndView modifyReposYSimultaneas(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ReposYSimultaneas toModify = reposYSimultaneasService.findReposYSimultaneasbyId(id).get(0);
        modelAndView.addObject("reposYSimultaneasModify",toModify);
        modelAndView.setViewName("parametric/modifyReposYSimultaneas");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyReposYSimultaneas")
    @ResponseBody
    public ModelAndView updateReposYSimultaneas(@ModelAttribute ReposYSimultaneas reposYSimultaneas,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reposYSimultaneas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            ReposYSimultaneas searchReposYSimultaneas = reposYSimultaneasService.findReposYSimultaneasbyId(id).get(0);
            if (searchReposYSimultaneas!=null)
            {
                if (validarRegistro(modelAndView,reposYSimultaneas)){
                    reposYSimultaneasService.modifyReposYSimultaneas(reposYSimultaneas, id);
                    modelAndView.addObject("resp", "Modify1");
                }
            }
            else
            {
                modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }


    @GetMapping(value="/parametric/addReposYSimultaneas")
    public ModelAndView showAddReposYSimultaneas(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        ReposYSimultaneas reposYSimultaneas = new ReposYSimultaneas();
        modelAndView.addObject("reposYSimultaneas", reposYSimultaneas);
        modelAndView.setViewName("parametric/addReposYSimultaneas");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addReposYSimultaneas")
    public ModelAndView createNewReposYSimultaneas(@ModelAttribute ReposYSimultaneas reposYSimultaneas) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reposYSimultaneas");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (reposYSimultaneasService.findReposYSimultaneasbyId(reposYSimultaneas.getId()).size() == 0) {
                if (validarRegistro(modelAndView, reposYSimultaneas)){
                    reposYSimultaneasService.saveReposYSimultaneas(reposYSimultaneas);
                    modelAndView.addObject("resp", "Add1");
                }
            } else {
                modelAndView.addObject("resp", "Add0");
            }
        }
        catch (Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/removeReposYSimultaneas/{id}")
    @ResponseBody
    public boolean removeReposYSimultaneas(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reposYSimultaneas");
        boolean response=false;
        try {
            ReposYSimultaneas toRemove = reposYSimultaneasService.findReposYSimultaneasbyId(id).get(0);
            reposYSimultaneasService.removeReposYSimultaneas(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearReposYSimultaneas")
    public ModelAndView clearReposYSimultaneas(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        reposYSimultaneasService.clearReposYSimultaneas(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/reposYSimultaneas");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/reposYSimultaneas/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ReposYSimultaneas_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<ReposYSimultaneas> reposYSimultaneasList= new ArrayList<ReposYSimultaneas>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            reposYSimultaneasList = reposYSimultaneasService.findAll();
        }
        else{
            reposYSimultaneasList = reposYSimultaneasService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        ReposYSimultaneasListReport listReport = new ReposYSimultaneasListReport(reposYSimultaneasList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchReposYSimultaneas")
    @ResponseBody
    public ModelAndView searchReposYSimultaneas(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<ReposYSimultaneas> list=reposYSimultaneasService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<ReposYSimultaneas> pageReposYSimultaneas = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageReposYSimultaneas.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allReposYSimultaneas",pageReposYSimultaneas.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchReposYSimultaneas");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/reposYSimultaneas");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, ReposYSimultaneas reposYSimultaneas){
        boolean salida = true;
        if (reposYSimultaneas.getCuenta().length() < 4 || reposYSimultaneas.getCuenta().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        if (reposYSimultaneas.getCuentaPyG().length() < 4 || reposYSimultaneas.getCuentaPyG().length() > 18){
            modelAndView.addObject("resp", "ErrorCuentaTamanio9");
            salida = false;
        }
        try{
            Double.parseDouble(reposYSimultaneas.getCodigo());
        } catch (Exception e){
            modelAndView.addObject("resp", "ErrorCodigoCaracteres");
            salida = false;
        }
        try{
            Double.parseDouble(reposYSimultaneas.getCuenta());
        } catch (Exception e){
            modelAndView.addObject("resp", "ErrorCuentaCaracteres");
            salida = false;
        }
        try{
            Double.parseDouble(reposYSimultaneas.getCuentaPyG());
        } catch (Exception e){
            modelAndView.addObject("resp", "ErrorCuentaPyGCaracteres");
            salida = false;
        }
        return salida;
    }
}