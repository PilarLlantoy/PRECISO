package com.inter.proyecto_intergrupo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ProyectoIntergrupoApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProyectoIntergrupoApplication.class, args);
    }

}
