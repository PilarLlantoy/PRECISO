package com.inter.proyecto_intergrupo.controller.parametric;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.*;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.CloseDateSourceSystemService;
import com.inter.proyecto_intergrupo.service.parametricServices.SourceSystemService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class CloseDateSourceSystemController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private SourceSystemService sourceSystemService;

    @Autowired
    private UserService userService;

    @Autowired
    private CloseDateSourceSystemService closeDateSourceSystemService;

    @GetMapping(value = "/parametric/closeDateSF/{id}")
    public ModelAndView closeDateSF(@PathVariable int id, @RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView();

        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        SourceSystem sistemaFuente = sourceSystemService.findById(id);
        List<CloseDateSourceSystem> fechasCierre = closeDateSourceSystemService.findByEstadoAndSistemaFuente(sistemaFuente);

        int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        int start = (int) pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()),fechasCierre.size());

        Page<CloseDateSourceSystem> pageConciliation = new PageImpl<>(fechasCierre.subList(start, end), pageRequest, fechasCierre.size());

        int totalPage=pageConciliation.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("directory","closeDateSourceSystem/"+id);
        modelAndView.addObject("allFechas",pageConciliation.getContent());
        modelAndView.addObject("registers",fechasCierre.size());
        modelAndView.addObject("filterExport","Original");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        modelAndView.addObject("p_modificar", p_modificar);

        CloseDateSourceSystem fecha = new CloseDateSourceSystem();
        modelAndView.addObject("fecha",fecha);
        modelAndView.addObject("sistemaFuente",sistemaFuente);

        modelAndView.setViewName("parametric/closeDateSourceSystem");
        return modelAndView;
    }


    @PostMapping(value = "/parametric/createFechaCierre")
    public ModelAndView createFechaCierre(@ModelAttribute CloseDateSourceSystem fecha,
                                           @RequestParam(name = "sfId") String sfId,
                                           BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/closeDateSF/" + sfId);
        SourceSystem sistemaFuente= sourceSystemService.findById(Integer.valueOf(sfId));

        boolean exists = sistemaFuente.getFechasCierre().stream()
                .anyMatch(closeDate -> closeDate.getValor().equals(fecha.getValor()));
        if(!exists)
        {
            fecha.setSistemaFuente(sistemaFuente);
            closeDateSourceSystemService.modificar(fecha);
        }
        else{
            modelAndView.addObject("resp", "Date-1");
        }
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteCloseDate/{sfId}/{fechaId}")
    public ModelAndView deleteAccountConcil(@PathVariable int sfId, @PathVariable int fechaId){

        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/closeDateSF/"+sfId);
        try {
            CloseDateSourceSystem fecha = closeDateSourceSystemService.findById(fechaId);
            fecha.setEstado(false);
            closeDateSourceSystemService.deleteById(fecha.getId());
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }



}
