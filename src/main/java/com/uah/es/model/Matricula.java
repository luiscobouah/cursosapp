package com.uah.es.model;

import java.util.Date;

public class Matricula {

    private Integer idMatricula;
    private Integer idCurso;
    private Double precio;
    private Date fecha;
    private Usuario usuario;

    public Matricula() {
    }

    public Matricula(Integer idCurso, Usuario usuario) {
        this.idCurso = idCurso;
        this.usuario = usuario;
    }

    public Integer getIdMatricula() {
        return idMatricula;
    }

    public void setIdMatricula(Integer idMatricula) {
        this.idMatricula = idMatricula;
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
