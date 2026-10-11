package com.proyecto.main;

import com.proyecto.servicio.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService = new CustomUserDetailsService();

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable() // Desactivamos protección básica para acoplarnos a tus formularios actuales
            .authorizeRequests()
                .antMatchers("/login", "/estilos.css").permitAll() // PERMITE ACCESO PÚBLICO INTEGRAL AL LOGIN
                .anyRequest().authenticated() // Bloquea perimetralmente el core de la app (/creditos)
            .and()
            .formLogin()
                .loginPage("/login") // Llama a la ruta que acabamos de corregir
                .loginProcessingUrl("/login") // Gatillo que procesa el POST del formulario
                .defaultSuccessUrl("/creditos", true) // Al autenticarse con éxito, da acceso
                .failureUrl("/login?error=true") // Redirección si las credenciales fallan
                .permitAll()
            .and()
            .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login")
                .permitAll();
                
        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(customUserDetailsService);
        authProvider.setPasswordEncoder(PasswordEncoderFactories.createDelegatingPasswordEncoder());
        return authProvider;
    }
}
