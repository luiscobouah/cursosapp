package com.uah.es.service;


import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.model.Matricula;


public interface IMatriculasService {

    Matricula[] buscarTodas();

    Matricula buscarMatriculaPorId(Integer idMatricula);

    Matricula buscarMatriculaPorIdCursoIdUsuario(Integer idCurso, Integer idUsuario);

    boolean guardarMatricula(Matricula matricula);

    boolean eliminarMatricula(Curso curso, Alumno alumno);

}
