package com.uah.es.model;

import java.util.ArrayList;
import java.util.List;

public class Alumno {

    private Integer idAlumno;
    private String nombre;
    private String correo;
    private List<Curso> cursos;

    public Alumno(String nombre, String correo) {
        this.idAlumno = 0;
        this.nombre = nombre;
        this.correo = correo;
        this.cursos = new ArrayList<>();
    }

    public Alumno() {
    }

    public Integer getIdAlumno() {
        return idAlumno;
    }

    public void setIdAlumno(Integer idAlumno) {
        this.idAlumno = idAlumno;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public List<Curso> getCursos() {
        return cursos;
    }

    public void setCursos(List<Curso> cursos) {
        this.cursos = cursos;
    }

    public String getStringCursos() {

        return this.cursos.toString().replace("[", "").replace("]", "");
    }

    @Override
    public String toString() {
        return this.nombre;
    }


}
