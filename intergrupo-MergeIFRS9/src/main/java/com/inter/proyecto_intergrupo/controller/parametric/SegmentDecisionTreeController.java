package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountAndByProduct;
import com.inter.proyecto_intergrupo.model.parametric.ReposYSimultaneas;
import com.inter.proyecto_intergrupo.model.parametric.SegmentDecisionTree;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.ReposYSimultaneasListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.SegmentDecisionTreeService;
import com.inter.proyecto_intergrupo.service.parametricServices.SegmentDecisionTreeListReport;
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
public class SegmentDecisionTreeController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private SegmentDecisionTreeService segmentDecisionTreeService;


    private List<String> listColumns=List.of("CodigoIFRS9", "DescripcionSectorizacion", "Corasu", "SubCorasu", "CIIU", "NumeroEmpleados", "TotalActivos", "TotalVentas", "VerificacionContratos", "OtrosCriterios");

    @GetMapping(value="/parametric/segmentDecisionTree")
    public ModelAndView showSegmentDecisionTree(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpoint(user.getId(),"Ver Árbol de decisión de segmento")) {

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        Page<SegmentDecisionTree> pageSegmentDecisionTree=segmentDecisionTreeService.getAll(pageRequest);
        int totalPage=pageSegmentDecisionTree.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allSegmentDecisionTree",pageSegmentDecisionTree.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("filterExport","Original");
        modelAndView.addObject("directory","segmentDecisionTree");
        List<SegmentDecisionTree> list = segmentDecisionTreeService.findAll();
        modelAndView.addObject("registers",list.size());

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/segmentDecisionTree");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @PostMapping(value="/parametric/segmentDecisionTree")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/segmentDecisionTree");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=LogInserción_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<SegmentDecisionTree> segmentDecisionTreeList = segmentDecisionTreeService.findAll();
        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            SegmentDecisionTreeListReport listReport = new SegmentDecisionTreeListReport(segmentDecisionTreeList);
            ArrayList<String[]> list = segmentDecisionTreeService.saveFileBD(fileContent,user);
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

    @GetMapping(value = "/parametric/modifySegmentDecisionTree/{id}")
    @ResponseBody
    public ModelAndView modifySegmentDecisionTree(@PathVariable Integer id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        SegmentDecisionTree toModify = segmentDecisionTreeService.findSegmentDecisionTreebyId(id).get(0);
        modelAndView.addObject("segmentDecisionTreeModify",toModify);
        modelAndView.setViewName("parametric/modifySegmentDecisionTree");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifySegmentDecisionTree")
    @ResponseBody
    public ModelAndView updateSegmentDecisionTree(@ModelAttribute SegmentDecisionTree segmentDecisionTree,@Param(value = "id") Integer id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/segmentDecisionTree");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            SegmentDecisionTree searchSegmentDecisionTree = segmentDecisionTreeService.findSegmentDecisionTreebyId(id).get(0);
            if (searchSegmentDecisionTree!=null)
            {
                if(validarRegistro(modelAndView, segmentDecisionTree)){
                    segmentDecisionTreeService.modifySegmentDecisionTree(segmentDecisionTree, id);
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


    @GetMapping(value="/parametric/addSegmentDecisionTree")
    public ModelAndView showAddSegmentDecisionTree(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        SegmentDecisionTree segmentDecisionTree = new SegmentDecisionTree();
        modelAndView.addObject("segmentDecisionTree", segmentDecisionTree);
        modelAndView.setViewName("parametric/addSegmentDecisionTree");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addSegmentDecisionTree")
    public ModelAndView createNewSegmentDecisionTree(@ModelAttribute SegmentDecisionTree segmentDecisionTree) {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/segmentDecisionTree");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            if (segmentDecisionTreeService.findSegmentDecisionTreebyId(segmentDecisionTree.getId()).size() == 0) {
                if (validarRegistro(modelAndView, segmentDecisionTree)){
                    segmentDecisionTreeService.saveSegmentDecisionTree(segmentDecisionTree);
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

    @GetMapping(value = "/parametric/removeSegmentDecisionTree/{id}")
    @ResponseBody
    public boolean removeSegmentDecisionTree(@PathVariable Integer id,@RequestParam Map<String, Object> params){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/segmentDecisionTree");
        boolean response=false;
        try {
            SegmentDecisionTree toRemove = segmentDecisionTreeService.findSegmentDecisionTreebyId(id).get(0);
            segmentDecisionTreeService.removeSegmentDecisionTree(toRemove.getId());
            response=true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return  response;
    }

    @GetMapping(value = "/parametric/clearSegmentDecisionTree")
    public ModelAndView clearSegmentDecisionTree(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        segmentDecisionTreeService.clearSegmentDecisionTree(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/segmentDecisionTree");
        return  modelAndView;
    }

    @GetMapping(value = "/parametric/segmentDecisionTree/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs, @RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ArbolDeDecisionSegmento_" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<SegmentDecisionTree> segmentDecisionTreeList= new ArrayList<SegmentDecisionTree>();
        if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            segmentDecisionTreeList = segmentDecisionTreeService.findAll();
        }
        else{
            segmentDecisionTreeList = segmentDecisionTreeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());
        }
        SegmentDecisionTreeListReport listReport = new SegmentDecisionTreeListReport(segmentDecisionTreeList);
        listReport.export(response);
    }

    @GetMapping(value = "/parametric/searchSegmentDecisionTree")
    @ResponseBody
    public ModelAndView searchSegmentDecisionTree(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<SegmentDecisionTree> list=segmentDecisionTreeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<SegmentDecisionTree> pageSegmentDecisionTree = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageSegmentDecisionTree.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allSegmentDecisionTree",pageSegmentDecisionTree.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchSegmentDecisionTree");

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/segmentDecisionTree");
        return modelAndView;
    }

    private Boolean validarRegistro(ModelAndView modelAndView, SegmentDecisionTree segmentDecisionTree){
        boolean salida = true;
        if (segmentDecisionTree.getCodigoIFRS9().length() != 3){
            modelAndView.addObject("resp", "ErrorCodigoTamanio3");
            salida = false;
        }
        else{
            try{
                Double.parseDouble(segmentDecisionTree.getCodigoIFRS9());
            } catch (Exception e){
                modelAndView.addObject("resp", "ErrorCodigoCaracteres");
                salida = false;
            }
        }
        return salida;
    }
}