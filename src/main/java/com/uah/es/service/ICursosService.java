package com.uah.es.service;

import com.uah.es.model.Curso;

public interface ICursosService {

    Curso[] buscarTodos();

    Curso buscarCursoPorId(Integer idCurso);

    Curso[] buscarCursosPorNombre(String nombre);

    Curso[] buscarCursosPorCategoria(String categoria);

    Curso[] buscarCursosPorProfesor(String profesor);

    boolean guardarCurso(Curso curso);

    boolean actualizarCurso(Curso curso);

    void eliminarCurso(Integer idCurso);

}
