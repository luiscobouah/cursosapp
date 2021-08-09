package com.uah.es.service;

import com.uah.es.model.Curso;

public interface ICursosService {

    //Page<Curso> buscarTodos(Pageable pageable);

    Curso buscarCursoPorId(Integer idCurso);

    //Page<Curso> buscarCursosPorNombre(String nombre, Pageable pageable);

    //Page<Curso> buscarCursosPorCategoria(String categoria, Pageable pageable);

    //Page<Curso> buscarCursosPorProfesor(String profesor, Pageable pageable);

    void guardarCurso(Curso curso);

    void eliminarCurso(Integer idCurso);

}
