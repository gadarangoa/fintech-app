package com.proyecto.servicio;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.proyecto.modelo.Credito;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CreditoDAO {

    // --- MÉTODO 1: BUSCAR UN SOLO DOCUMENTO ---
	public DocumentSnapshot procesarBusqueda(String cedula) throws Exception {
        Firestore db = FirebaseService.getFirestoreInstance();
        
        // Hacemos un Query para buscar en la colección el documento donde el campo "cedula" coincida exactamente
        com.google.api.core.ApiFuture<QuerySnapshot> apiFuture = db.collection("solicitudes_credito")
                .whereEqualTo("cedula", cedula.trim())
                .get();
        
        // Esperamos la respuesta del servidor con un tiempo límite de seguridad de 5 segundos
        QuerySnapshot querySnapshot = apiFuture.get(5, java.util.concurrent.TimeUnit.SECONDS);
        List<QueryDocumentSnapshot> documentos = querySnapshot.getDocuments();
        
        // Si la consulta encontró al menos un documento que coincida con esa cédula, lo retorna
        if (!documentos.isEmpty()) {
            return documentos.get(0);
        }
        
        // Si no hay coincidencias, retorna null de forma segura
        return null;
    }

    // --- MÉTODO 2: LECTURA GENERAL PARA PINTAR LA TABLA WEB ---
    public List<Credito> obtenerTodosLosCreditos() {
        List<Credito> lista = new ArrayList<Credito>();
        try {
            Firestore db = FirebaseService.getFirestoreInstance();
            ApiFuture<QuerySnapshot> future = db.collection("solicitudes_credito").get();
            QuerySnapshot querySnapshot = future.get(5, TimeUnit.SECONDS);
            List<QueryDocumentSnapshot> documentos = querySnapshot.getDocuments();

            for (int i = 0; i < documentos.size(); i++) {
                QueryDocumentSnapshot doc = documentos.get(i);
                
                String cedula = doc.getString("cedula");
                String nombre = doc.getString("nombreCliente");
                String correo = doc.getString("correo");
                String ciudad = doc.getString("ciudad");
                
                // Forzamos conversión segura de tipos desde Cloud Firestore
                long monto = doc.contains("montoSolicitado") && doc.getLong("montoSolicitado") != null ? doc.getLong("montoSolicitado") : 0L;
                int plazo = doc.contains("plazoMeses") && doc.getLong("plazoMeses") != null ? doc.getLong("plazoMeses").intValue() : 0;
                double tasa = doc.contains("tasaInteres") && doc.getDouble("tasaInteres") != null ? doc.getDouble("tasaInteres") : 0.0;
                long ingresos = doc.contains("ingresosMensuales") && doc.getLong("ingresosMensuales") != null ? doc.getLong("ingresosMensuales") : 0L;
                int score = doc.contains("scoreCrediticio") && doc.getLong("scoreCrediticio") != null ? doc.getLong("scoreCrediticio").intValue() : 0;
                boolean aprobado = doc.contains("aprobado") && doc.getBoolean("aprobado") != null && doc.getBoolean("aprobado");

                if (cedula != null) {
                    // LLAMADA AL CONSTRUCTOR ALINEADO AL MODELO
                    Credito c = new Credito(cedula, nombre, correo, ciudad, monto, plazo, tasa, ingresos, score, aprobado);
                    lista.add(c);
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ [WARN]: Error al jalar registros de Firebase: " + e.getMessage());
        }
        return lista;
    }

    // --- MÉTODOS TRANSACCIONALES DE ESCRITURA ---
    public void insertarCredito(Credito credito) throws Exception {
        Firestore db = FirebaseService.getFirestoreInstance();
        Map<String, Object> datos = new HashMap<String, Object>();
        datos.put("cedula", credito.getCedula());
        datos.put("nombreCliente", credito.getNombrecliente());
        datos.put("correo", credito.getCorreo());
        datos.put("ciudad", credito.getCiudad());
        datos.put("montoSolicitado", credito.getMontosolicitado());
        datos.put("plazoMeses", credito.getPlazomeses());
        datos.put("tasaInteres", credito.getTasainteres());
        datos.put("ingresosMensuales", credito.getIngresosmensuales());
        datos.put("scoreCrediticio", credito.getScorecrediticio());
        datos.put("aprobado", credito.isAprobado());

        db.collection("solicitudes_credito").document(credito.getCedula()).set(datos).get(5, TimeUnit.SECONDS);
    }
    
    // =========================================================================
    // ACTUALIZAR SEGÚN DOCUMENT ID DINÁMICO
    // =========================================================================
    public void actualizarCredito(String cedula, String nombre, long monto, boolean aprobado) throws Exception {
        Firestore db = FirebaseService.getFirestoreInstance();
        
        // Buscamos el documento para conocer su ID real en Firestore
        ApiFuture<QuerySnapshot> future = db.collection("solicitudes_credito").whereEqualTo("cedula", cedula.trim()).get();
        List<QueryDocumentSnapshot> documentos = future.get().getDocuments();
        
        if (!documentos.isEmpty()) {
            String documentIdReal = documentos.get(0).getId(); // Captura el ID aleatorio (Ej: DErMGcDS...)
            
            Map<String, Object> actualizaciones = new HashMap<String, Object>();
            if (nombre != null && !nombre.isEmpty()) actualizaciones.put("nombreCliente", nombre);
            if (monto > 0) actualizaciones.put("montoSolicitado", monto);
            actualizaciones.put("aprobado", aprobado);

            db.collection("solicitudes_credito").document(documentIdReal).update(actualizaciones).get(5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }
 // =========================================================================
    // ELIMINAR SEGÚN DOCUMENT ID DINÁMICO
    // =========================================================================
    public void eliminarCredito(String cedula) throws Exception {
        Firestore db = FirebaseService.getFirestoreInstance();
        
        // Buscamos el documento para conocer su ID real en Firestore
        ApiFuture<QuerySnapshot> future = db.collection("solicitudes_credito").whereEqualTo("cedula", cedula.trim()).get();
        List<QueryDocumentSnapshot> documentos = future.get().getDocuments();
        
        if (!documentos.isEmpty()) {
            String documentIdReal = documentos.get(0).getId();
            db.collection("solicitudes_credito").document(documentIdReal).delete().get(5, java.util.concurrent.TimeUnit.SECONDS);
        }
    }
}
