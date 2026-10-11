package com.proyecto.controlador;

import com.proyecto.modelo.Credito;
import java.util.ArrayList;
import java.util.List;

public class CreditoCRUD {
    // Lista en memoria para almacenar los créditos temporalmente
    private List<Credito> listaCreditos;

    public CreditoCRUD() {
        this.listaCreditos = new ArrayList<Credito>();
    }

    // C - CREATE (Agregar crédito)
    public boolean crearCredito(Credito credito) {
        if (buscarPorCedula(credito.getCedula()) != null) {
            System.out.println("Error: Ya existe un crédito con la cédula " + credito.getCedula());
            return false;
        }
        listaCreditos.add(credito);
        return true;
    }

    // R - READ (Buscar crédito por cédula)
    public Credito buscarPorCedula(String cedula) {
        for (Credito c : listaCreditos) {
            if (c.getCedula().equals(cedula)) {
                return c;
            }
        }
        return null; // No encontrado
    }

    // R - READ ALL (Obtener todos los registros)
    public List<Credito> obtenerTodos() {
        return listaCreditos;
    }

    // U - UPDATE (Editar datos específicos de un crédito)
    public boolean actualizarCredito(String cedula, String nuevoNombre, long nuevoMonto, boolean nuevoEstado) {
        Credito credito = buscarPorCedula(cedula);
        if (credito != null) {
            // Cambiados a minúsculas para acoplarse de forma exacta a tu modelo
            if (!nuevoNombre.isEmpty()) credito.setNombrecliente(nuevoNombre);
            if (nuevoMonto > 0) credito.setMontosolicitado(nuevoMonto);
            credito.setAprobado(nuevoEstado);
            return true;
        }
        return false;
    }

    // D - DELETE (Eliminar registro por cédula)
    public boolean eliminarCredito(String cedula) {
        Credito credito = buscarPorCedula(cedula);
        if (credito != null) {
            listaCreditos.remove(credito);
            return true;
        }
        return false;
    }
}
