package com.inter.proyecto_intergrupo.controller.ifrs9;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.ifrs9.Manuals;
import com.inter.proyecto_intergrupo.model.parametric.ChangeCurrency;
import com.inter.proyecto_intergrupo.repository.parametric.statusInfoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ManualsListReport;
import com.inter.proyecto_intergrupo.service.ifrs9Services.ManualsService;
import com.inter.proyecto_intergrupo.service.parametricServices.ChangeCurrencyListReport;
import com.inter.proyecto_intergrupo.service.parametricServices.ChangeCurrencyService;
import com.inter.proyecto_intergrupo.service.resourcesServices.CsvService;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ManualsController {
    private static final int PAGINATIONCOUNT=12;
    Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    @Autowired
    private UserService userService;

    @Autowired
    private ManualsService manualsService;

    @Autowired
    private statusInfoRepository statusInfoRepositoryL;

    private List<String> listColumns=List.of("Centro","Cuenta PUC","Divisa","Importe","Fuente Información");

    @GetMapping(value="/ifrs/manuals")
    public ModelAndView showManualsR(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        if(userService.validateEndpointVer(user.getId(),"Ver Manuales (ANEXO8 - SICC)")) {

            int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
            PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);

            String todayString = "";
            if (params.get("period") == null || params.get("period").toString() == "") {
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
                } else {
                    todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
                }
            } else {
                todayString = params.get("period").toString();
            }

            List<Object[]> validateSicc = manualsService.validateLoad(todayString);
            if (validateSicc.size() !=0) {
                modelAndView.addObject("validateSicc", true);
                Date dateSicc = new Date();
                try {
                    dateSicc = statusInfoRepositoryL.findByInputAndPeriodo("MANUALS", todayString).getFecha();
                }catch (Exception e){
                    e.printStackTrace();
                }
                String pattern = "dd/MM/yyyy HH:mm:ss";
                DateFormat df = new SimpleDateFormat(pattern);
                String dateAsString = df.format(dateSicc);
                modelAndView.addObject("dateSicc", dateAsString);
            } else {
                modelAndView.addObject("validateSicc", false);
            }

            List<Object[]> list= new ArrayList<Object[]>();
            if((params.get("vFilter") !=null && params.get("vFilter").toString().equals("Original")) ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
                list = manualsService.findAllByPeriodResume(todayString);
                modelAndView.addObject("vFilter", "Original");
            }
            else{
                list = manualsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);
            }

            int start = (int)pageRequest.getOffset();
            int end = Math.min((start + pageRequest.getPageSize()), list.size());
            Page<Object[]> pageManuals = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

            int totalPage = pageManuals.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("allManuals", pageManuals.getContent());
            modelAndView.addObject("current", page + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("columns", listColumns);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("directory", "manuals");
            modelAndView.addObject("registers",pageManuals.getTotalElements());

            modelAndView.addObject("userName", user.getPrimerNombre());
            modelAndView.addObject("userEmail", user.getCorreo());
            modelAndView.setViewName("ifrs/manuals");
        }
        else
        {
            modelAndView.addObject("anexo","/home");
            modelAndView.setViewName("admin/errorMenu");
        }
        return modelAndView;
    }

    @GetMapping(value="/ifrs/anexoLoadM")
    public ModelAndView loadManuals(HttpServletRequest request, HttpServletResponse response,@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/manuals");
        response.setContentType("application/octet-stream");
        String todayString = "";
        if (params.get("period") == null || Objects.equals(params.get("period").toString(), "")) {
            Date today = new Date();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(today);
            if(calendar.get(Calendar.MONTH)==0)
            {
                calendar.add(Calendar.YEAR,-1);
                todayString = calendar.get(Calendar.YEAR) + "-12";
            }
            else if (String.valueOf(calendar.get(Calendar.MONTH)).length() == 2) {
                todayString = calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH);
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + calendar.get(Calendar.MONTH);
            }
        } else {
            todayString = params.get("period").toString();
        }

        boolean respuesta = false;
        try {
            respuesta = manualsService.loadData(todayString);
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        List<Object[]> validateSicc = manualsService.validateLoad(todayString);
        if (validateSicc.size() !=0) {
            modelAndView.addObject("validateSicc", true);
            Date dateSicc = new Date();
            try {
                dateSicc = statusInfoRepositoryL.findByInputAndPeriodo("MANUALS", todayString).getFecha();
            }catch (Exception e){
                e.printStackTrace();
            }
            String pattern = "dd/MM/yyyy HH:mm:ss";
            DateFormat df = new SimpleDateFormat(pattern);
            String dateAsString = df.format(dateSicc);
            modelAndView.addObject("dateSicc", dateAsString);
        } else {
            modelAndView.addObject("validateSicc", false);
        }

        if(respuesta==true) {
            modelAndView.addObject("resp", "ANXM1");
        }
        else
        {
            modelAndView.addObject("resp", "ANXM-1");
        }
        modelAndView.addObject("period",todayString);

        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/manuals/downloadNotApply")
    @ResponseBody
    public void exportToExcelNotApply(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString = "";
        if (params.get("period") != null && params.get("period").toString() == "") {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        } else {
            todayString = params.get("period").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ManualesAnexo8PUC" + currentDateTime + ".csv";
        response.setHeader(headerKey, headerValue);
        List<Object[]> manualsList = manualsService.getManualsNoApply(todayString);
        /*ManualsListReport listReport = new ManualsListReport(null,manualsList);
        listReport.exportExtra(response);*/
        //CsvService.downloadManuals(response.getWriter(), manualsList);
    }

    /*@GetMapping(value = "/ifrs/modifyManuals/{id}")
    @ResponseBody
    public ModelAndView modifyManuals(@PathVariable String id,@PathVariable String di) throws ParseException {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        Date fecha = formato.parse(id);
        Manuals toModify = manualsService.findManualsByCCC(null,null,di).get(0);
        modelAndView.addObject("changeCurrencyModify",toModify);
        modelAndView.addObject("manualObject",toModify);
        modelAndView.addObject("divisaId",toModify.getDivisa());
        modelAndView.setViewName("ifrs/modifyManuals");
        return modelAndView;
    }

    @PostMapping(value = "/ifrs/modifyManuals")
    @ResponseBody
    public ModelAndView updateManuals(@ModelAttribute ChangeCurrency changeCurrency,@Param(value = "fecha") String fecha,@RequestParam("centro") String centro,@Param(value = "cuenta") String cuenta,@RequestParam("contrato") String contrato){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        ModelAndView modelAndView = new ModelAndView("redirect:/ifrs/manuals");
        try {
            Manuals searchManuals = manualsService.findManualsByCCC().get(0);
            //if (searchGarantBank==null||fecha.equals(searchGarantBank.getFecha()))
            {
                manualsService.modifyManuals(searchManuals,divisa,user,fecha);
                modelAndView.addObject("resp", "Modify1");
            }
            //else
            {
              //  modelAndView.addObject("resp", "Modify0");
            }
        }
        catch(Exception e){
            modelAndView.addObject("resp", "General-1");
        }
        return  modelAndView;

    }*/

    /*@GetMapping(value = "/parametric/validatePalabra")
    @ResponseBody
    public String validateNitGarantBanks(@RequestParam String idNew,@RequestParam String id){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String result = "invalid";
        if(garantBankService.findGarantBankByNombreSmiliar(idNew)==null||idNew.equals(id))
            result="valid";
        return  result;
    }

    @GetMapping(value="/parametric/addChangeCurrency")
    public ModelAndView showAddChangeCurrency(){
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("parametric/addChangeCurrency");
        return modelAndView;
    }

    @PostMapping(value = "/parametric/addChangeCurrency/add")
    public ModelAndView addChangeCurrency(@RequestParam Map<String, Object> params) throws ParseException {
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        ChangeCurrency insert= new ChangeCurrency();
        SimpleDateFormat formato = new SimpleDateFormat("dd-MM-yyyy");
        formato.applyPattern("yyyy-MM-dd");
        Date fecha = formato.parse(params.get("fecha").toString()+"-01");
        insert.setFecha(fecha);
        insert.setDivisa(params.get("divisa").toString().toUpperCase(Locale.ROOT));
        insert.setValor(Double.parseDouble(params.get("valor").toString()));
        boolean result = changeCurrencyService.insertChangeCurrency(insert);

        if(result){
            modelAndView.addObject("resp", "Add1");
        }else {
            modelAndView.addObject("resp","Add0");
        }

        return modelAndView;
    }

    @GetMapping(value = "/parametric/removeChangeCurrency/{id}")
    @ResponseBody
    public ModelAndView removeChangeCurrency(@PathVariable String id) throws ParseException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        String[] partsId= id.split("00:00:00.0");
        String[] partsDate = partsId[0].split("-");
        ChangeCurrency toRemove = changeCurrencyService.findChangeCurrencyByDivisaYFecha(partsDate[0],partsDate[1],partsId[1]).get(0);
        changeCurrencyService.removeChangeCurrency(toRemove.getFecha(),toRemove.getDivisa(),user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        return  modelAndView;
    }

    @GetMapping(value = "/ifrs/clearManuals")
    public ModelAndView clearManuals(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());
        manualsService.clearManuals(user);
        ModelAndView modelAndView = new ModelAndView("redirect:/parametric/changeCurrency");
        return  modelAndView;
    }*/

    @GetMapping(value = "/ifrs/manuals/download")
    @ResponseBody
    public void exportToExcel(HttpServletResponse response, RedirectAttributes redirectAttrs,@RequestParam Map<String, Object> params) throws IOException {
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        String todayString = "";
        if (params.get("period") != null && params.get("period").toString() == "") {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        } else {
            todayString = params.get("period").toString();
        }

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=ManualesAnexo8" + currentDateTime + ".xlsx";
        response.setHeader(headerKey, headerValue);
        List<Manuals> manualsList= new ArrayList<Manuals>();
        manualsList = manualsService.findAllByPeriod(todayString);
        List<Object[]> manualsListNot = manualsService.getManualsNoApply(todayString);
        List<Object[]> manualsQuery = manualsService.validateQuery("0013",todayString);
        /*if((params.get("vFilter").toString()).equals("Original") ||params.get("vFilter")==null||(params.get("vFilter").toString()).equals("")) {
            manualsList = manualsService.findAllByPeriod(todayString);
        }
        else{
            manualsList = manualsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);
        }*/
        ManualsListReport listReport = new ManualsListReport(manualsList,null,manualsListNot,manualsQuery);
        listReport.export(response);
    }



    /*@GetMapping(value = "/ifrs/searchManuals")
    @ResponseBody
    public ModelAndView searchChangeCurrency(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        String todayString = "";
        if (params.get("period") != null && params.get("period").toString() == "") {
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
            } else {
                todayString = calendar.get(Calendar.YEAR) + "-0" + String.valueOf(calendar.get(Calendar.MONTH));
            }
        } else {
            todayString = params.get("period").toString();
        }

        int page=params.get("page")==null?0:(Integer.valueOf(params.get("page").toString())-1);
        PageRequest pageRequest=PageRequest.of(page,PAGINATIONCOUNT);
        List<Manuals> list=manualsService.findByFilter(params.get("vId").toString(),params.get("vFilter").toString(),todayString);

        int start = (int)pageRequest.getOffset();
        int end = Math.min((start + pageRequest.getPageSize()), list.size());
        Page<Manuals> pageManuals = new PageImpl<>(list.subList(start, end), pageRequest, list.size());

        int totalPage=pageManuals.getTotalPages();
        if(totalPage>0){
            List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
            modelAndView.addObject("pages",pages);
        }
        modelAndView.addObject("allManuals",pageManuals.getContent());
        modelAndView.addObject("current",page+1);
        modelAndView.addObject("next",page+2);
        modelAndView.addObject("prev",page);
        modelAndView.addObject("vId",params.get("vId").toString());
        modelAndView.addObject("last",totalPage);
        modelAndView.addObject("vFilter",params.get("vFilter").toString());
        modelAndView.addObject("columns",listColumns);
        modelAndView.addObject("directory","searchManuals");
        modelAndView.addObject("registers",list.size());

        User user = userService.findUserByUserName(auth.getName());
        modelAndView.addObject("userName",user.getPrimerNombre());
        modelAndView.addObject("userEmail",user.getCorreo());
        modelAndView.setViewName("ifrs/manuals");
        return modelAndView;
    }*/
}
