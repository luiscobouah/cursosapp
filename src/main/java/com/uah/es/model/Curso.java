package com.uah.es.model;

import java.util.List;
import java.util.Objects;

public class Curso {

    private Integer idCurso;
    private String nombre;
    private Integer duracion;
    private String profesor;
    private Double precio;
    private String categoria;
    private String imagen;
    private List<Alumno> alumnos;

    public Curso(Integer idCurso, String nombre, Integer duracion, String profesor, Double precio, String categoria, String imagen, List<Alumno> alumnos) {
        this.idCurso = idCurso;
        this.nombre = nombre;
        this.duracion = duracion;
        this.profesor = profesor;
        this.precio = precio;
        this.categoria = categoria;
        this.imagen = imagen;
        this.alumnos = alumnos;
    }

    public Curso() {
    }

    public Integer getIdCurso() {
        return idCurso;
    }

    public void setIdCurso(Integer idCurso) {
        this.idCurso = idCurso;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getDuracion() {
        return duracion;
    }

    public void setDuracion(Integer duracion) {
        this.duracion = duracion;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public List<Alumno> getAlumnos() {
        return alumnos;
    }

    public void setAlumnos(List<Alumno> alumnos) {
        this.alumnos = alumnos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Curso)) return false;
        Curso curso = (Curso) o;
        return Objects.equals(idCurso, curso.idCurso);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idCurso);
    }

    public String toString() {
        return this.nombre;
    }
}
