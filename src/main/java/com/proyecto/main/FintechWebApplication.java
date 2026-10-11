package com.proyecto.main;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import java.util.Collections;

@SpringBootApplication
@ComponentScan(basePackages = "com.proyecto")
public class FintechWebApplication {
    public static void main(String[] args) {
        // Inicializa la conexión de Firebase al arrancar el servidor
        com.proyecto.servicio.FirebaseService.inicializar();
        
        // FORZADO ARQUITECTÓNICO: Obliga a Spring Boot a ignorar el puerto 8080 y usar el 8085
        SpringApplication app = new SpringApplication(FintechWebApplication.class);
        app.setDefaultProperties(Collections.singletonMap("server.port", "8085"));
        app.run(args);
        
        System.out.println("--> [SERVIDOR WEB]: Corriendo exitosamente en http://localhost:8085/creditos");
    }
}
