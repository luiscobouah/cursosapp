package com.uah.es.service;

import com.uah.es.model.Alumno;


public interface IAlumnosService {

    Alumno[] buscarTodos();

    Alumno buscarAlumnoPorId(Integer idAlumno);

    Alumno buscarAlumnoPorCorreo(String correo);

    Alumno[] buscarAlumnosPorNombre(String nombre);

    boolean guardarAlumno(Alumno alumno);

    boolean actualizarAlumno(Alumno alumno);

    boolean eliminarAlumno(Integer idAlumno);

    boolean inscribirCurso(Integer idAlumno, Integer idCurso);

    boolean desinscribirCurso(Integer idAlumno, Integer idCurso);
}
