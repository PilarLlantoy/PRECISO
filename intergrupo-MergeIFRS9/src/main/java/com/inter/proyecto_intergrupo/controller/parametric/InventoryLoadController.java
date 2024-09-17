package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.AccountingRoute;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.AccountingRouteService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class InventoryLoadController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

    @Autowired
    private UserService userService;

    @Autowired
    private AccountingRouteService conciliationService;

    @Autowired
    private AccountingRouteService accountingRouteService;

    @Autowired
    private SourceSystemService sourceSystemService;

    @GetMapping(value="/parametric/inventoryLoad")
    public ModelAndView inventoryLoad(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Países");
        if(userService.validateEndpoint(user.getId(),"Ver Países")) { //CAMBIAR A VER Conciliaciones

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<AccountingRoute> aroutes = conciliationService.findAllActive();
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
            modelAndView.addObject("filterExport","Original");
            modelAndView.addObject("directory","inventoryLoad");
            modelAndView.addObject("registers",aroutes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/inventoryLoad");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }



}
