package com.inter.proyecto_intergrupo.controller.eeffController;

import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.DatesLoadEeFF;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.QueryBanco;
import com.inter.proyecto_intergrupo.model.eeffConsolidated.ValoreseeffFiliales;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.DatesLoadEeffRepository;
import com.inter.proyecto_intergrupo.repository.eeffConsolidatedRepository.QueryBancoRepository;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import com.inter.proyecto_intergrupo.service.eeffconsolidated.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class VistaBancoEeffConsolidatedController {

    private static final int PAGINATIONCOUNT = 12;

    @Autowired
    private ValoresEeffConsolidatedService valoresEeffConsolidatedService;

    @Autowired
    private BancoSFCconsolidatedService bancoSFCconsolidatedService;

    @Autowired
    private QueryBancoService queryBancoService;

    @Autowired
    private DatesLoadEeffService datesLoadEeffService;

    @Autowired
    private DatesLoadEeffRepository datesLoadEeffRepository;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/eeffConsolidated/filialesBanco")
    public ModelAndView showTemplateEEFF(@RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        if (userService.validateEndpoint(user.getUsuario(), "Ver EEFF Consolidado Banco")) {

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

            int page1 = params.get("page") != null ? (Integer.valueOf(params.get("page").toString()) - 1) : 0;
            PageRequest pageRequest2 = PageRequest.of(page1, PAGINATIONCOUNT);
            Page<QueryBanco> pageType = queryBancoService.getAll(pageRequest2,todayString);
            int totalPage = pageType.getTotalPages();
            if (totalPage > 0) {
                List<Integer> pages = IntStream.rangeClosed(1, totalPage).boxed().collect(Collectors.toList());
                modelAndView.addObject("pages", pages);
            }

            modelAndView.addObject("current", page1 + 1);
            modelAndView.addObject("next", page + 2);
            modelAndView.addObject("prev", page);
            modelAndView.addObject("period", todayString);
            modelAndView.addObject("rol", "Banco");
            modelAndView.addObject("directory", "filialesBanco");
            modelAndView.addObject("last", totalPage);
            modelAndView.addObject("registers",pageType.getTotalElements());
            modelAndView.addObject("listaDeDatosBanco", pageType.getContent());

            DatesLoadEeFF registro = datesLoadEeffRepository.findByEntidadAndPeriodo("Banco", todayString);
            String estadoDelRegistro = registro != null ? registro.getEstado() : "PENDING";

            modelAndView.addObject("estadoDelRegistro", estadoDelRegistro);
            modelAndView.setViewName("eeffConsolidated/filialesBanco");
        } else {
            modelAndView.addObject("anexo", "/home");
            modelAndView.setViewName("admin/errorMenu");
        }

        return modelAndView;
    }

    @PostMapping(value = "/eeffConsolidated/CargarSFCBanco")
    public ModelAndView uploadFile(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesBanco");
        response.setContentType("application/octet-stream");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();
            InputStream fileContent1 = filePart.getInputStream();

            boolean respuesta = bancoSFCconsolidatedService.ValidarFechaBanco(fileContent,params.get("period").toString());

            if (respuesta == true) {
                Date fechaCargue = new Date();

                datesLoadEeffService.guardarFechasEnTabla("Banco", params.get("period").toString(), "SFC", fechaCargue);
                bancoSFCconsolidatedService.guardarSoporteEnBD(fileContent1, params.get("period").toString(), "Banco", user);
                bancoSFCconsolidatedService.loadAudit(user, "Cargue exitoso del archivo SFC");

                modelAndView.addObject("resp", "AddRepFiliales");
            } else {
                bancoSFCconsolidatedService.loadAudit(user, "¡Falla al cargar el Archivo");
                modelAndView.addObject("resp", "AddRepFilialesFallido");
            }

            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("vFilter", params.get("period").toString());

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @GetMapping(value = "/eeffConsolidated/descargarSfcBanco")
    public ModelAndView DescargarSFC(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesBanco");
        response.setContentType("application/octet-stream");
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
        String currentDateTime = dateFormatter.format(new Date());

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        String headerKey = "Content-Disposition";
        String headerValue = "attachment; filename=CopiaSoporteSFCBanco" + currentDateTime + ".txt";
        response.setHeader(headerKey, headerValue);
        try {
            DatesLoadEeFF archivoDesdeBD = bancoSFCconsolidatedService.obtenerSoporteDesdeBD(params.get("rol").toString(),params.get("period").toString());
            ServletOutputStream outputStream = response.getOutputStream();
            outputStream.write(archivoDesdeBD.getSoporteSfcDescarga());
            outputStream.close();

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @PostMapping("/eeffConsolidated/CargarQueryBanco")
    public ModelAndView uploadFileBanco(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesBanco");
        response.setContentType("application/octet-stream");

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findUserByUserName(auth.getName());

        try {
            Part filePart = request.getPart("file");
            InputStream fileContent = filePart.getInputStream();

            boolean archivoValido = queryBancoService.procesarArchivoTXT(fileContent, params.get("period").toString());

            if (archivoValido) {
                Date fechaCargue = new Date();

                bancoSFCconsolidatedService.loadAudit(user, "Cargue exitoso del archivo EEFF Banco");

                modelAndView.addObject("resp", "rol-pucBancoBien");

                datesLoadEeffService.guardarFechasEnTabla("Banco", params.get("period").toString(), "Eeff", fechaCargue);
                modelAndView.addObject("period", params.get("period").toString());
            } else {

                modelAndView.addObject("resp", "rol-pucBanco");
            }

        } catch (Exception e) {
            e.printStackTrace();

        }
        return modelAndView;
    }

  @GetMapping(value = "/eeffConsolidated/DescargarEeffBanco")
  public void descargarEeffBanco(HttpServletResponse response, @RequestParam Map<String, Object> params) {
      try {
          response.setContentType("application/vnd.ms-excel");
          DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
          String currentDateTime = dateFormatter.format(new Date());
          String headerKey = "Content-Disposition";
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

          queryBancoService.descargarEeffBanco(response, todayString);

      } catch (Exception e) {
          e.printStackTrace();
      }
  }

    @GetMapping(value = "/eeffConsolidated/DescargarPlantillaBanco")
    public void descargarEeffAjustePlantillaBanco(HttpServletResponse response, @RequestParam Map<String, Object> params) {
        try {
            response.setContentType("application/vnd.ms-excel");
            DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
            String currentDateTime = dateFormatter.format(new Date());
            String headerKey = "Content-Disposition";
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
            queryBancoService.descargarEeffBancoPlantilla(response, todayString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PostMapping(value = "/eeffConsolidated/CargarQueryAjusteBanco")
    public ModelAndView uploadFileBanco1(HttpServletRequest request, HttpServletResponse response, @RequestParam Map<String, Object> params) {
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesBanco");
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

            ArrayList<String[]> list = queryBancoService.saveFileBDBanco(fileContent, params.get("period").toString());
            String[] part = list.get(0);

            if (part[2].equals("SUCCESS")) {

                queryBancoService.loadAudit(user, "Cargue exitoso plantilla ajuste EEFF Banco");
                Date fechaCargue = new Date(); // Obtener la fecha actual

                datesLoadEeffService.guardarFechasEnTabla("Banco", params.get("period").toString(), "Eeff", fechaCargue);

                modelAndView.addObject("resp", "AddRep1");
                modelAndView.addObject("row", part[0]);
                modelAndView.addObject("colum", part[1]);
            } else {
                queryBancoService.loadAudit(user, "Cargue fallido plantilla ajuste EEFF Banco");
                FiduciariaEeffConsolidatedListReport rulesDQListReport = new FiduciariaEeffConsolidatedListReport(list, null);
                rulesDQListReport.exportLog(response);
            }

            modelAndView.addObject("period", params.get("period").toString());
            modelAndView.addObject("vFilter", params.get("period").toString());

        } catch (Exception e) {
            e.printStackTrace();
            modelAndView.addObject("resp", "PC-1");
        }
        return modelAndView;
    }

    @GetMapping(value="/eeffConsolidated/processQueryBanco")
    public ModelAndView concilFilialesEeff(@RequestParam Map<String, Object> params){
        ModelAndView modelAndView = new ModelAndView("redirect:/eeffConsolidated/filialesBanco");
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

            modelAndView.addObject("resp","manualsCorrect");

        }catch (Exception e){
            e.printStackTrace();
            modelAndView.addObject("resp","PC-1");
        }

        queryBancoService.procesoAjuste(todayString);

        modelAndView.addObject("period",todayString);
        return  modelAndView;
    }
}