package com.uah.es.service;


import com.uah.es.model.Matricula;


public interface IMatriculasService {

    Matricula[] buscarTodas();

    //Page<Matricula> buscarMatriculasPorIdCurso(Integer idCurso, Pageable pageable);

    Matricula buscarMatriculaPorId(Integer idMatricula);

    boolean guardarMatricula(Matricula matricula);

    void eliminarMatricula(Integer idMatricula);

}
