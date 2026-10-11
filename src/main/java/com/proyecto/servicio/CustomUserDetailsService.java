package com.proyecto.servicio;

import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            // 1. Obtenemos la conexión activa a Firebase
            Firestore db = FirebaseService.getFirestoreInstance();
            
            // 2. CORREGIDO: Removemos el timeout de adentro de los paréntesis para máxima compatibilidad
            DocumentSnapshot doc = db.collection("analistas")
                                     .document(username.trim())
                                     .get()
                                     .get(); // El segundo .get() espera la respuesta de forma síncrona limpia

            // 3. Validamos si el analista existe en tu base de datos cloud
            if (doc != null && doc.exists()) {
                String claveNube = doc.getString("password");
                String rolNube = doc.contains("rol") ? doc.getString("rol") : "ROLE_ANALISTA";

                // 4. Retornamos las credenciales reales de la nube a Spring Security
                return User.withDefaultPasswordEncoder()
                           .username(username)
                           .password(claveNube)
                           .authorities(rolNube)
                           .build();
            } else {
                throw new UsernameNotFoundException("El analista no existe en Firestore.");
            }
        } catch (Exception e) {
            throw new UsernameNotFoundException("Error conectando con la colección analistas: " + e.getMessage());
        }
    }
}
