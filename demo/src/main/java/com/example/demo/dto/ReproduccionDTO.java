package com.example.demo.dto;

import java.time.LocalDateTime;

public class ReproduccionDTO {

    // Campos existentes
    private Integer idCancion;
    private String emailUser;
    
    // NUEVOS CAMPOS (Para la respuesta)
    private String id;
    private LocalDateTime fecha;

    // SOLUCIÓN: Añadir comentario explicativo dentro de las llaves
    public ReproduccionDTO() {
        // Constructor vacío necesario para la deserialización de JSON
    }

    // Getters y Setters necesarios
    public Integer getIdCancion() { return idCancion; }
    public void setIdCancion(Integer idCancion) { this.idCancion = idCancion; }

    public String getEmailUser() { return emailUser; }
    public void setEmailUser(String emailUser) { this.emailUser = emailUser; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public LocalDateTime getFecha() { return fecha; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
}