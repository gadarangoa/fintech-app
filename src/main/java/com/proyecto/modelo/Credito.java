package com.proyecto.modelo;

public class Credito {
    private String cedula;
    private String nombrecliente;
    private String correo;
    private String ciudad;
    private long montosolicitado;
    private int plazomeses;
    private double tasainteres;
    private long ingresosmensuales;
    private int scorecrediticio;
    private boolean aprobado;

    // Constructor vacío obligatorio
    public Credito() {}

    // CONSTRUCTOR EXPLICITO REQUERIDO POR EL CONTROLADOR WEB
    public Credito(String cedula, String nombrecliente, String correo, String ciudad, 
                   long montosolicitado, int plazomeses, double tasainteres, 
                   long ingresosmensuales, int scorecrediticio, boolean aprobado) {
        this.cedula = cedula;
        this.nombrecliente = nombrecliente;
        this.correo = correo;
        this.ciudad = ciudad;
        this.montosolicitado = montosolicitado;
        this.plazomeses = plazomeses;
        this.tasainteres = tasainteres;
        this.ingresosmensuales = ingresosmensuales;
        this.scorecrediticio = scorecrediticio;
        this.aprobado = aprobado;
    }

    // Getters y Setters
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getNombrecliente() { return nombrecliente; }
    public void setNombrecliente(String nombrecliente) { this.nombrecliente = nombrecliente; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getCiudad() { return ciudad; }
    public void setCiudad(String ciudad) { this.ciudad = ciudad; }

    public long getMontosolicitado() { return montosolicitado; }
    public void setMontosolicitado(long montosolicitado) { this.montosolicitado = montosolicitado; }

    public int getPlazomeses() { return plazomeses; }
    public void setPlazomeses(int plazomeses) { this.plazomeses = plazomeses; }

    public double getTasainteres() { return tasainteres; }
    public void setTasainteres(double tasainteres) { this.tasainteres = tasainteres; }

    public long getIngresosmensuales() { return ingresosmensuales; }
    public void setIngresosmensuales(long ingresosmensuales) { this.ingresosmensuales = ingresosmensuales; }

    public int getScorecrediticio() { return scorecrediticio; }
    public void setScorecrediticio(int scorecrediticio) { this.scorecrediticio = scorecrediticio; }

    public boolean isAprobado() { return aprobado; }
    public void setAprobado(boolean aprobado) { this.aprobado = aprobado; }
}
