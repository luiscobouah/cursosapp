package com.uah.es.service;

import com.uah.es.model.Alumno;


public interface IAlumnosService {
    //Page<Alumno> buscarTodos(Pageable pageable);

    Alumno[] buscarTodos();

    Alumno buscarAlumnoPorId(Integer idAlumno);

    Alumno buscarAlumnoPorCorreo(String correo);

    boolean guardarAlumno(Alumno alumno);

    boolean actualizarAlumno(Alumno alumno);

    void eliminarAlumno(Integer idAlumno);

    void inscribirCurso(Integer idAlumno, Integer idCurso);
}
