package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.*;
import com.inter.proyecto_intergrupo.model.parametric.StatusInfo;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.VerPT1PatrimonioTecnicoRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.VerPTValorRiesgoRepository;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.EliminacionesPatrimonio;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.VerPTService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaEeffVerPTController {

    private static final int PAGINATIONCOUNT = 12;

    @Autowired
    private VerPTService verPTService;

    @Autowired
    private UserService userService;

    @Autowired
    private statusInfoRepository statusInfoRepository;

    @Autowired
    private VerPT1PatrimonioTecnicoRepository verPT1PatrimonioTecnicoRepository;


    @GetMapping(value = "/eeffConsolidated/patrimonialesVerPT")
    public ModelAndView showTemplateVerPT(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if (userService.validateEndpoint(user.getId(), "Ver EEFF Eliminaciones Ver PT")) {

            int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);

            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
                Date today = new Date();
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(today);
                if (calendar.get(Calendar.MONTH) == 0) {
                    calendar.add(Calendar.YEAR, -1);
                    todayString = calendar.get(Calendar.YEAR) + "-12";
                } else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                    todayString = calendar.get(Calendar.YEAR) + "-" + String.valueOf(calendar.get(Calendar.MONTH));
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<tablaVerPTunificada> list = verPTService.getDataVerPT(todayString, "VER - PT");
            List<VerPT> list1 = verPTService.getDataValorRiesgo(todayString);
            List<VerPT1> list2 = verPTService.getDataPatrimonioTecnico(todayString);

            int page1 = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<tablaVerPTunificada> pageType = verPTService.getAllVerPT(pageRequest2, todayString, list);
            Page<VerPT> pageType1 = verPTService.getAllValorRiesgo(pageRequest2, todayString, list1);
            Page<VerPT1> pageType2 = verPTService.getAllPatrimonioTecnico(pageRequest2, todayString, list2);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }
            modelAndView.addObject("current", page1 + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageType.getTotalElements());
            modelAndView.addObject("ListaDatosVerPT", list);
            modelAndView.addObject("ListaDatosValorRiesgo", list1);
            modelAndView.addObject("ListaDatosPatrimonioTecnico", list2);
            modelAndView.setViewName("eeffConsolidated/patrimonialesVerPT");

            StatusInfo registro = statusInfoRepository.findByInputAndPeriodo("VALOR EN RIESGO TOTAL", todayString);
            String estadoDelRegistro = registro != null ? registro.getStatus() : "NULL";
            modelAndView.addObject("estadoDelRegistro", estadoDelRegistro);

            StatusInfo registro1 = statusInfoRepository.findByInputAndPeriodo("PATRIMONIO TECNICO", todayString);
            String estadoDelRegistro1 = registro1 != null ? registro1.getStatus() : "NULL";
            modelAndView.addObject("estadoDelRegistro1", estadoDelRegistro1);


       } else {
          modelAndView.addObject("anexo", "/home");
          modelAndView.setViewName("admin/errorMenu");
        }

        return modelAndView;
    }

    @GetMapping(value="/eeffConsolidated/ConfirmarValorEnRiesgo")
    public ModelAndView confirmarValorEnRiesgo(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        String todayString="";
        System.out.println(params.get("period").toString());
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
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
        try{
            modelAndView.addObject("resp","correct");
        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","error");
        }
        verPTService.confirmarValorEnRiesgo(todayString);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("resp", "AddRep3");
        return  modelAndView;
    }

    @GetMapping(value="/eeffConsolidated/confirmarPatrimonioTecnico")
    public ModelAndView confirmarPatrimonioTecnico(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        String todayString="";
        System.out.println(params.get("period").toString());
        if(params.get("period")==null || Objects.equals(params.get("period").toString(), "")) {
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
        try{
            modelAndView.addObject("resp","correct");
        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","error");
        }
        verPTService.confirmarPatrimonioTecnico(todayString);
        modelAndView.addObject("period",todayString);
        modelAndView.addObject("resp", "AddRep3");
        return  modelAndView;
    }

    @GetMapping("/eeffConsolidated/procesarVerPT")
    public ModelAndView procesarVerPT(@RequestParam("period") String periodo)
    {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        verPTService.ProcesarVerPT(periodo);
        modelAndView.addObject("resp", "ProcessExi");
        modelAndView.addObject("period", periodo);
        return modelAndView;
    }

    //********************************************VALOR RIESGO TOTAL********************************///

    @GetMapping(value = "/eeffConsolidated/addAccountValorEnRiesgo")
    public ModelAndView showAddAccoun234(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        VerPT accountCc = new VerPT();
        modelAndView.addObject("accountCc", accountCc);
        System.out.println(params.get("period").toString());
        modelAndView.addObject("period", params.get("period").toString());
        modelAndView.setViewName("/eeffConsolidated/addAccountValorEnRiesgo");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/addAccountValorEnRiesgo")
    public ModelAndView create432(@ModelAttribute VerPT accountCc, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCc.setPeriodo(params.get("period").toString());
            verPTService.saveValorEnRiesgo(accountCc, user);
            modelAndView.addObject("resp", "Add1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
            e.printStackTrace();
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/ModifyValorEnRiesgo/{id}")
    @ResponseBody
    public ModelAndView modifyAccountValorEnRiesgo(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Long idTemp = null;
        try {
            idTemp = Long.parseLong(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        VerPT toModify = verPTService.findByIdDato(idTemp);
        modelAndView.addObject("accountCcModify22", toModify);
        DecimalFormat formato = new DecimalFormat("#,###.##");
      //  modelAndView.addObject("valorf",formato.format(toModify.getValorRiesgoTotal()));
        modelAndView.addObject("valorf", BigDecimal.valueOf(toModify.getValorRiesgoTotal()));
        modelAndView.setViewName("eeffConsolidated/ModifyValorEnRiesgo");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/ModifyValorEnRiesgo")
    @ResponseBody
    public ModelAndView updateAccountValorEnRiesgo(@ModelAttribute VerPT accountCc, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCc.setValorRiesgoTotal(Double.parseDouble(params.get("valorf").toString()));
            verPTService.modifyAccountValorEnRiesgo(accountCc, user);
            modelAndView.addObject("period", accountCc.getPeriodo());
            modelAndView.addObject("resp", "Modify1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;

    }

    //********************************************PATRIMONIO TECNICO********************************///
    @GetMapping(value = "/eeffConsolidated/addAccountPatrimonioTecnico")
    public ModelAndView showAddAccounPATRIMONIOTECNICO(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        int page = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
        PageRequest pageRequest = PageRequest.of(page, PAGINATIONCOUNT);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        VerPT1 accountCc = new VerPT1();
        modelAndView.addObject("accountCc", accountCc);
        modelAndView.addObject("period", params.get("period").toString());
        modelAndView.setViewName("/eeffConsolidated/addAccountPatrimonioTecnico");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/addAccountPatrimonioTecnico")
    public ModelAndView createPATRIMONIOTECNICO(@ModelAttribute VerPT1 accountCc, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCc.setPeriodo(params.get("period").toString());
            verPTService.savePatrimonioTecnico(accountCc, user);
            modelAndView.addObject("resp", "Add1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
            e.printStackTrace();
        }
        return modelAndView;
    }


    @GetMapping(value = "/eeffConsolidated/ModifyPatrimonioTecnico/{id}")
    @ResponseBody
    public ModelAndView modifyAccountPATRIMONIOTECNICO(@PathVariable String id) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName", user.getPrimerNombre());
        modelAndView.addObject("userEmail", user.getCorreo());
        Long idTemp1 = null;
        try {
            idTemp1 = Long.parseLong(id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        VerPT1 toModify = verPTService.findByIdDato1(idTemp1);
        modelAndView.addObject("accountCcPatrimonio", toModify);
        DecimalFormat formato = new DecimalFormat("#,###.##");
        //  modelAndView.addObject("valorf",formato.format(toModify.getValorRiesgoTotal()));
        modelAndView.addObject("valorf1", BigDecimal.valueOf(toModify.getValorPatrimonioTecnico()));
        modelAndView.setViewName("eeffConsolidated/ModifyPatrimonioTecnico");
        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/ModifyPatrimonioTecnico")
    @ResponseBody
    public ModelAndView updateAccountPATRIMONIOTECNICO(@ModelAttribute VerPT1 accountCc, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/patrimonialesVerPT");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        try {
            accountCc.setValorPatrimonioTecnico(Double.parseDouble(params.get("valorf1").toString()));
            verPTService.modifyAccountPatrimonioTecnico(accountCc, user);
            modelAndView.addObject("period", accountCc.getPeriodo());
            modelAndView.addObject("resp", "Modify1");
        } catch (Exception e) {
            modelAndView.addObject("resp", "General-1");
        }
        return modelAndView;

    }

}