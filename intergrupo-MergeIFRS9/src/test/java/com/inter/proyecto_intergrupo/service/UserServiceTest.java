package com.inter.proyecto_intergrupo.service;


import com.inter.proyecto_intergrupo.model.admin.Role;
import com.inter.proyecto_intergrupo.model.admin.User;
import com.inter.proyecto_intergrupo.model.bank.GpsReport;
import com.inter.proyecto_intergrupo.service.adminServices.UserService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.inter.proyecto_intergrupo.repository.admin.RoleRepository;
import com.inter.proyecto_intergrupo.repository.admin.UserRepository;

import java.util.Date;
import java.util.HashSet;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

public class UserServiceTest {

    @Mock
    private UserRepository mockUserRepository;
    @Mock
    private RoleRepository mockRoleRepository;
    @Mock
    private BCryptPasswordEncoder mockBCryptPasswordEncoder;

    private UserService userServiceUnderTest;
    private User user;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        userServiceUnderTest = new UserService(mockUserRepository,
                mockRoleRepository,
                mockBCryptPasswordEncoder);
        user = User.builder()
                .usuario("juansepe")
                .contra("12345")
                .creacion(new Date())
                .centro("12345")
                .correo("juan@juan.com")
                .build();

        Mockito.when(mockUserRepository.save(any()))
                .thenReturn(user);
        Mockito.when(mockUserRepository.findByCorreo(anyString()))
                .thenReturn(user);
    }

    @Test
    public void testFindUserByEmail() {
        String  cabecera = "Nombre1|Razónsocial|Nºident.fis.1|NIFdelproveedor|Soc.|C";
        String line = "|   PROSEGUR TECNOLOGÍA S.A.S     |                                                                                                                                            |8300251047      |                    |CO11|RE      |23502117  |251105030           |            145 775 |     1,00000|COP  |04.08.2021|2021/08   |CO11000076|28.07.2021|                                                  |                         |COP  |            145 775 |1*T5973         |5600007880|202150042202950061  |8541322031|            |                        |            |";
        line = line.replaceAll("\\s+", "");

        if(StringUtils.countMatches(line, "|") == 27 && !line.contains(cabecera) && !line.contains("|  * ")) {
            String[] data = line.split("\\|");
            String[] fecont = data[13].split("/");
            String periodoLine = fecont[0] + "-" + fecont[1];
            GpsReport gpsReport = new GpsReport();
            gpsReport.setNombre1(data[1]);
            gpsReport.setRazon_social(data[2]);
            gpsReport.setIdent_fis(data[3]);
            gpsReport.setNif(data[4]);
            gpsReport.setSoc(data[5]);
            gpsReport.setClase(data[6]);
            gpsReport.setCuenta(data[7]);
            gpsReport.setCuenta_local(data[8]);
            gpsReport.setImporte_md(data[9].substring(0,data[9].length()-1));
            gpsReport.setTipo_cambio(data[10]);
            gpsReport.setMon1(data[11]);
            gpsReport.setFecont(data[12]);
            gpsReport.setEjercicioMes(fecont[0] + "-" + fecont[1]);
            gpsReport.setCe_coste(data[14]);
            gpsReport.setFecha_doc(data[15]);
            gpsReport.setTexto(data[16]);
            gpsReport.setTexto_camb(data[17]);
            gpsReport.setDivisa(data[18]);
            gpsReport.setImporte_ml(data[19].substring(0,data[19].length()-1));
            gpsReport.setReferencia(data[20]);
            if (data.length < 22)
                gpsReport.setNumero_doc("");
            else
                gpsReport.setNumero_doc(data[21]);
            if (data.length < 23)
                gpsReport.setClave_3("");
            else
                gpsReport.setClave_3(data[22]);
            if (data.length < 24)
                gpsReport.setDoc_comp("");
            else
                gpsReport.setDoc_comp(data[23]);
            if (data.length < 25)
                gpsReport.setArchv_fijo("");
            else
                gpsReport.setArchv_fijo(data[24]);
            if (data.length < 26)
                gpsReport.setElemento_pep("");
            else
                gpsReport.setElemento_pep(data[25]);
            if(data.length < 27)
            {
                gpsReport.setUsuario_em(" ");
            } else {
                gpsReport.setUsuario_em(data[26]);
            }
        }
    }

    @Test
    public void testSaveUser() {
        // Setup
        //final String email = "juan@juan.com";

        // Run the test
        //User result = userServiceUnderTest.saveUser(User.builder().build(), new HashSet<Role>());

        // Verify the results
        //assertEquals(email, result.getCorreo());
    }
}