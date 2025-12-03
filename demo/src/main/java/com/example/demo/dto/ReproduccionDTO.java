package com.example.demo.dto;

public class ReproduccionDTO {

    private Integer idCancion;
    private String emailUser;

    public ReproduccionDTO() {
    }

    public ReproduccionDTO(Integer idCancion, String emailUser) {
        this.idCancion = idCancion;
        this.emailUser = emailUser;
    }

    public Integer getIdCancion() {
        return idCancion;
    }

    public void setIdCancion(Integer idCancion) {
        this.idCancion = idCancion;
    }

    public String getEmailUser() {
        return emailUser;
    }

    public void setEmailUser(String emailUser) {
        this.emailUser = emailUser;
    }
}