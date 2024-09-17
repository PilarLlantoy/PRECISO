package com.inter.proyecto_intergrupo.controller.parametric;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.parametric.EventType;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.parametricServices.EventTypeService;
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
public class EventTypeController {
    private static final int PAGINATIONCOUNT=12;
    private List<String> listColumns=List.of("Código", "Nombre","Estado");
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private EventTypeService eventTypeService;

    @GetMapping(value="/parametric/eventType")
    public ModelAndView showEventType(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Tipos Eventos");
        if(userService.validateEndpoint(user.getId(),"Ver Tipos Eventos")) {

            int page=params.get("page")!=null?(Integer.valueOf(params.get("page").toString())-1):0;
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            List<EventType> eventTypes = eventTypeService.findAllActive();
            int start = (int) pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), eventTypes.size());
            Page<EventType> pageET = new PageImpl<>(eventTypes.subList(start, end), pageRequest, eventTypes.size());

            int totalPage=pageET.getTotalPages();
            if(totalPage>0){
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages",pages);
            }
            modelAndView.addObject("allEventType",pageET.getContent());
            modelAndView.addObject("current",page+1);
            modelAndView.addObject("next",page+2);
            modelAndView.addObject("prev",page);
            modelAndView.addObject("last",totalPage);
            modelAndView.addObject("registers",eventTypes.size());
            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("p_modificar", p_modificar);
            modelAndView.setViewName("parametric/EventType");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value = "/parametric/modifyEventType/{id}")
    @ResponseBody
    public ModelAndView modifyEventType(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        EventType toModify = eventTypeService.findAllById(id);
        modelAndView.addObject("objetoModify",toModify);
        modelAndView.addObject("id",toModify.getId());
        modelAndView.setViewName("parametric/modifyEventType");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/modifyEventType")
    public ModelAndView updateEventType(@ModelAttribute EventType objeto){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/eventType");
        eventTypeService.modificar(objeto);
        return modelAndView;
    }

    @PostMapping(value = "/parametric/deleteEventType/{id}")
    public ModelAndView deleteEventType(@PathVariable int id){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/eventType");
        try {
            EventType objeto = eventTypeService.findAllById(id);
            objeto.setEstado(false);
            eventTypeService.modificar(objeto);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return modelAndView;
    }


    @GetMapping(value = "/parametric/createEventType")
    public ModelAndView showCreateEventType(){
        ModelAndView modelAndView = new ModelAndView();
        EventType objeto = new EventType();
        modelAndView.addObject("objeto",objeto);
        modelAndView.setViewName("/parametric/createEventType");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/createEventType")
    public ModelAndView createEventType(@ModelAttribute EventType objeto, BindingResult bindingResult){
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/eventType");
        EventType paisExists = eventTypeService.findAllById(objeto.getId());
        if(paisExists != null){
            bindingResult
                    .rejectValue("pais", "error.pais",
                            "El pais ya se ha registrado");
        }
        if(bindingResult.hasErrors()){
            modelAndView.setViewName("parametric/createEventType");
        }else{
            eventTypeService.modificar(objeto);
        }
        return modelAndView;

    }

    @GetMapping(value = "/parametric/searchEventType")
    @ResponseBody
    public ModelAndView searchEventType(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        System.out.println("PARAMS "+params.get("vId").toString()+" "+params.get("vFilter").toString());
        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<EventType> list;
        if(params==null)
            list=eventTypeService.findByFilter("inactivo", "Estado");
        else
            list=eventTypeService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString());

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<EventType> pageTypeEntity = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageTypeEntity.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allEventType",pageTypeEntity.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchEventType");
        modelAndView.addObject("registers",list.size());
        User user = userService.findUserByUserName(auth.getName());
        Boolean p_modificar= userService.validateEndpointModificar(user.getId(),"Ver Tipos Eventos");

        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.addObject("p_modificar", p_modificar);

        modelAndView.setViewName("parametric/eventType");
        return modelAndView;
    }
}
