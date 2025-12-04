package com.example.demo.dto;

import java.util.List;

public class EstadisticasRequestDTO {
    
    private List<Integer> ids;
    private String fechaInicio; // Recibimos String para facilitar el parseo en controller o usar @DateTimeFormat
    private String fechaFin;

    // SOLUCIÓN: Añadir comentario explicativo dentro de las llaves
    public EstadisticasRequestDTO() {
        // Constructor vacío necesario para la deserialización de JSON
    }

    // Getters y Setters
    public List<Integer> getIds() { return ids; }
    public void setIds(List<Integer> ids) { this.ids = ids; }

    public String getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(String fechaInicio) { this.fechaInicio = fechaInicio; }

    public String getFechaFin() { return fechaFin; }
    public void setFechaFin(String fechaFin) { this.fechaFin = fechaFin; }
}