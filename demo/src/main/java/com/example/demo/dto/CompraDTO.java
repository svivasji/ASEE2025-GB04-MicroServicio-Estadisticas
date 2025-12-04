package com.example.demo.dto;

public class CompraDTO {
    
    private Integer idCancion; 
    private Integer idAlbum;
    private Double precio;

    // SOLUCIÓN: Añadir comentario explicativo
    public CompraDTO() {
        // Constructor vacío necesario para la deserialización de JSON (Jackson/Spring)
    }

    public Integer getIdCancion() { return idCancion; }
    public void setIdCancion(Integer idCancion) { this.idCancion = idCancion; }

    public Integer getIdAlbum() { return idAlbum; }
    public void setIdAlbum(Integer idAlbum) { this.idAlbum = idAlbum; }

    public Double getPrecio() { return precio; }
    public void setPrecio(Double precio) { this.precio = precio; }
}