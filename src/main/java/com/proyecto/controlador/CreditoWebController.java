package com.proyecto.controlador;

import com.google.cloud.firestore.DocumentSnapshot;
import com.proyecto.modelo.Credito;
import com.proyecto.servicio.CreditoDAO;
import com.proyecto.util.CSVReaderUtil;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;

@Controller
@RequestMapping("/creditos")
public class CreditoWebController {

    private CreditoDAO dao = new CreditoDAO();

    // =========================================================================
    // METODO 1: PINTAR LA PAGINA PRINCIPAL (Corrige el error 404 al iniciar o volver)
    // =========================================================================
    @GetMapping
    public String paginaPrincipal(Model model) {
        try {
            // Pasamos las solicitudes para flujos de control secundarios si se requieren
            model.addAttribute("solicitudes", dao.obtenerTodosLosCreditos());
        } catch (Exception e) {
            model.addAttribute("solicitudes", new ArrayList<Credito>());
        }
        // Al cargar la vista de inicio por primera vez no hay búsqueda activa
        model.addAttribute("resultadoBusqueda", null);
        return "index"; // Carga estrictamente el archivo index.html de la carpeta templates
    }

    // =========================================================================
    // METODO 1.1: RUTA EXCLUSIVA PARA DESPLEGAR EL HISTORIAL EN LA OTRA VISTA
    // =========================================================================
    @GetMapping("/ver-tabla")
    public String desplegarVistaTabla(Model model) {
        try {
            model.addAttribute("solicitudes", dao.obtenerTodosLosCreditos());
        } catch (Exception e) {
            model.addAttribute("solicitudes", new ArrayList<Credito>());
        }
        return "tabla"; // Carga estrictamente el archivo tabla.html de la carpeta templates
    }

    // =========================================================================
    // METODO 2: PROCESAR LA BÚSQUEDA PROTEGIDA CON POST
    // =========================================================================
    @PostMapping("/buscar-solicitud")
    public String realizarBusquedaWeb(@RequestParam(value = "txtCedulaBuscar", required = false) String txtCedulaBuscar, Model model) {
        try {
            if (txtCedulaBuscar != null && !txtCedulaBuscar.trim().isEmpty()) {
                DocumentSnapshot doc = dao.procesarBusqueda(txtCedulaBuscar.trim());
                
                if (doc != null && doc.exists()) {
                    Credito c = new Credito(
                        doc.getString("cedula"),
                        doc.getString("nombreCliente"),
                        doc.getString("correo"),
                        doc.getString("ciudad"),
                        doc.contains("montoSolicitado") ? doc.getLong("montoSolicitado") : 0L,
                        doc.contains("plazoMeses") ? doc.getLong("plazoMeses").intValue() : 0,
                        doc.contains("tasaInteres") ? doc.getDouble("tasaInteres") : 0.0,
                        doc.contains("ingresosMensuales") ? doc.getLong("ingresosMensuales") : 0L,
                        doc.contains("scoreCrediticio") ? doc.getLong("scoreCrediticio").intValue() : 0,
                        doc.contains("aprobado") && doc.getBoolean("aprobado")
                    );
                    model.addAttribute("resultadoBusqueda", c);
                } else {
                    model.addAttribute("resultadoBusqueda", null);
                }
            } else {
                model.addAttribute("resultadoBusqueda", null);
            }
            model.addAttribute("solicitudes", dao.obtenerTodosLosCreditos());
        } catch (Exception e) {
            System.err.println("Error en proceso de busqueda: " + e.getMessage());
        }
        return "index";
    }

    // =========================================================================
    // METODO 3: MITIGACIÓN DE INYECCIONES Y ACCIÓN GUARDAR / EDITAR
    // =========================================================================
    @PostMapping("/guardar")
    public String accionGuardarOEditar(@ModelAttribute Credito credito, @RequestParam(required = false) String operacion) {
        try {
            if ("editar".equals(operacion)) {
                String cedulaSegura = credito.getCedula().trim();
                String nombreSanitizado = credito.getNombrecliente().replaceAll("<[^>]*>", "").replaceAll("[(/\\\\*';)]", "").trim();
                dao.actualizarCredito(cedulaSegura, nombreSanitizado, credito.getMontosolicitado(), credito.isAprobado());
            } else {
                String nombreLimpio = credito.getNombrecliente().replaceAll("<[^>]*>", "").trim();
                credito.setNombrecliente(nombreLimpio);
                dao.insertarCredito(credito);
            }
        } catch (Exception e) {
            System.err.println("Error al procesar transaccion: " + e.getMessage());
        }
        return "redirect:/creditos"; // Al guardar, nos redirige de forma limpia a la vista principal
    }
    // METODO 4: ELIMINAR Y REDIRECCIONAR AL HISTORIAL DE LA TABLA
    @GetMapping("/eliminar/{cedula}")
    public String accionEliminar(@PathVariable String cedula) {
        try {
            dao.eliminarCredito(cedula);
        } catch (Exception e) {
            System.err.println("Error al eliminar: " + e.getMessage());
        }
        return "redirect:/creditos/ver-tabla"; // Al borrar de la tabla, nos mantiene en la tabla
    }
    // METODO 5: PROCESAR ARCHIVO CSV DESDE EL EXPLORADOR WEB
    @PostMapping("/cargar-csv")
    public String accionCargarCSV(@RequestParam("archivoCsv") org.springframework.web.multipart.MultipartFile archivoCsv) {
        try {
            if (archivoCsv != null && !archivoCsv.isEmpty()) {
                CSVReaderUtil.cargarDatosDesdeCSVFlujoWeb(archivoCsv);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar archivo: " + e.getMessage());
        }
        return "redirect:/creditos"; // Al cargar archivo nos muestra el formulario limpio
    }
}
