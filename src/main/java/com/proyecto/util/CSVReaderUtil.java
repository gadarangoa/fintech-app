package com.proyecto.util;

import com.opencsv.CSVReader;
import com.proyecto.modelo.Credito;
import com.proyecto.servicio.CreditoDAO;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStreamReader;

public class CSVReaderUtil {

    // =========================================================================
    // MÉTODO 5 CORE: LECTURA EN MEMORIA CON DECODIFICACIÓN NATIVA DE EXCEL
    // =========================================================================
    public static int cargarDatosDesdeCSVFlujoWeb(MultipartFile archivo) {
        InputStreamReader isr = null;
        CSVReader csvReader = null;
        CreditoDAO dao = new CreditoDAO();
        int contador = 0;
        int lineaActual = 0;
        
        try {
            System.out.println("--> [RASTRADOR CLOUD]: Procesando el archivo: " + archivo.getOriginalFilename());
            
            // FILTRO DE ENTRADA: Forzamos la decodificación windows-1252 
            // Esto permite capturar las tildes y eñes directas que Microsoft Excel exporta en Windows
            isr = new InputStreamReader(archivo.getInputStream(), "windows-1252");
            csvReader = new CSVReader(isr);
            
            String[] fila;

            // OpenCSV extrae cada renglón mapeándolo en una matriz de columnas indexadas
            while ((fila = csvReader.readNext()) != null) {
                lineaActual++;
                
                // Validación de seguridad: Saltamos líneas vacías o incompletas
                if (fila == null || fila.length == 0 || (fila.length == 1 && fila[0].trim().isEmpty())) {
                    continue;
                }

                try {
                    // Si el archivo usa punto y coma (;) en lugar de coma, OpenCSV lee una sola columna larga.
                    // Aplicamos un divisor elástico de rescate para asegurar la compatibilidad:
                    String[] columnas = fila;
                    if (fila.length == 1 && fila[0].contains(";")) {
                        columnas = fila[0].split(";");
                    }

                    // SANITIZACIÓN CORE: Extraemos el texto de la celda y removemos las comillas dobles
                    String cedula = columnas[0].trim().replace("\"", "");
                    String nombre = columnas[1].trim().replace("\"", "");
                    String correo = columnas[2].trim().replace("\"", "");
                    String city = columnas[3].trim().replace("\"", "");
                    
                    // Omitir el encabezado si la primera línea contiene los títulos de las columnas
                    if (lineaActual == 1 && (cedula.equalsIgnoreCase("cedula") || nombre.toLowerCase().contains("nombre"))) {
                        System.out.println("--> [RASTRADOR CLOUD]: Fila de encabezado detectada y omitida con éxito.");
                        continue;
                    }

                    // Parseo estructurado y de tipos contables numéricos
                    long monto = Long.parseLong(columnas[4].trim().replace("\"", "").replaceAll("[^0-9]", ""));
                    int plazo = Integer.parseInt(columnas[5].trim().replace("\"", "").replaceAll("[^0-9]", ""));
                    double tasa = Double.parseDouble(columnas[6].trim().replace("\"", ""));
                    long ingresos = Long.parseLong(columnas[7].trim().replace("\"", "").replaceAll("[^0-9]", ""));
                    int score = Integer.parseInt(columnas[8].trim().replace("\"", "").replaceAll("[^0-9]", ""));
                    
                    String strAprobado = columnas[9].trim().replace("\"", "").toLowerCase();
                    boolean aprobado = strAprobado.equals("true") || strAprobado.equals("1");

                    // Instanciamos el objeto con la data sanitizada
                    Credito credito = new Credito(cedula, nombre, correo, city, monto, plazo, tasa, ingresos, score, aprobado);
                    
                    // Inyección transaccional directa a la colección solicitudes_credito en la nube
                    dao.insertarCredito(credito);
                    contador++;
                    
                } catch (Exception ex) {
                    System.err.println("⚠️ [RASTRADOR CLOUD]: Fila " + lineaActual + " saltada por error de parseo: " + ex.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [RASTRADOR CLOUD]: Error crítico al abrir el flujo web: " + e.getMessage());
        } finally {
            if (csvReader != null) { try { csvReader.close(); } catch (Exception e) {} }
            if (isr != null) { try { isr.close(); } catch (Exception e) {} }
        }
        
        System.out.println("--> [RASTRADOR CLOUD]: Proceso terminado. Total inyectados de forma íntegra: " + contador);
        return contador;
    }
}
