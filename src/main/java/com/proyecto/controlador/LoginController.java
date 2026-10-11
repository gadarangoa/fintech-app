package com.proyecto.controlador;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    // ALINEADO: Este método responde de forma directa y pura a http://localhost:8085/login
    @GetMapping("/login")
    public String mostrarPantallaLogin() {
        return "login"; // Abre directamente el archivo login.html ubicado en la carpeta templates
    }
}
