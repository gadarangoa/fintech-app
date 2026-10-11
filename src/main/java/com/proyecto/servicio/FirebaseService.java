package com.proyecto.servicio;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.proyecto.modelo.Credito;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

public class FirebaseService {

    private static Firestore db;

    public static void inicializar() {
        try {
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream("firebase-key.json");

                FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

                FirebaseApp.initializeApp(options);
            }
            db = FirestoreClient.getFirestore();
            System.out.println("--> [FIREBASE FIRESTORE]: Conectado exitosamente.");
        } catch (Exception e) {
            System.err.println("Error al conectar con Firestore: " + e.getMessage());
        }
    }

    // Retorna la instancia de la base de datos para consultas directas desde la pantalla
    public static Firestore getFirestoreInstance() {
        if (db == null) inicializar();
        return db;
    }

    // Guarda o actualiza un registro directamente en la nube utilizando la cédula como ID del documento
    public static void guardarOCambiarCredito(Credito credito) {
        try {
            if (db == null) inicializar();

            Map<String, Object> datos = new HashMap<String, Object>();
            datos.put("cedula", credito.getCedula());
            datos.put("nombreCliente", credito.getNombrecliente()); // <-- Corregido a minúscula
            datos.put("correo", credito.getCorreo());
            datos.put("ciudad", credito.getCiudad());               // <-- Corregido a minúscula
            datos.put("montoSolicitado", credito.getMontosolicitado()); // <-- Corregido a minúscula
            datos.put("plazoMeses", credito.getPlazomeses());       // <-- Corregido a minúscula
            datos.put("tasaInteres", credito.getTasainteres());     // <-- Corregido a minúscula
            datos.put("ingresosMensuales", credito.getIngresosmensuales()); // <-- Corregido a minúscula
            datos.put("scoreCrediticio", credito.getScorecrediticio()); // <-- Corregido a minúscula
            datos.put("aprobado", credito.isAprobado());

            db.collection("creditos").document(credito.getCedula()).set(datos);
        } catch (Exception e) {
            System.err.println("Error al guardar en Firestore: " + e.getMessage());
        }
    }

    public static void actualizarCampos(String cedula, String nuevoNombre, double nuevoMonto, boolean nuevoEstado) {
        try {
            if (db == null) inicializar();

            Map<String, Object> actualizaciones = new HashMap<String, Object>();
            if (!nuevoNombre.isEmpty()) actualizaciones.put("nombreCliente", nuevoNombre);
            if (nuevoMonto > 0) actualizaciones.put("montoSolicitado", nuevoMonto);
            actualizaciones.put("aprobado", nuevoEstado);

            if (!actualizaciones.isEmpty()) {
                db.collection("creditos").document(cedula).update(actualizaciones);
            }
        } catch (Exception e) {
            System.err.println("Error al actualizar en Firestore: " + e.getMessage());
        }
    }

    public static void eliminarDeFirebase(String cedula) {
        try {
            if (db == null) inicializar();
            db.collection("creditos").document(cedula).delete();
        } catch (Exception e) {
            System.err.println("Error al eliminar en Firestore: " + e.getMessage());
        }
    }
}
