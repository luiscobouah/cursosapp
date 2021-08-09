package com.uah.es.service;


import com.uah.es.model.Matricula;


public interface IMatriculasService {

    //Page<Matricula> buscarTodas(Pageable pageable);

    //Page<Matricula> buscarMatriculasPorIdCurso(Integer idCurso, Pageable pageable);

    Matricula buscarMatriculaPorId(Integer idMatricula);

    String guardarMatricula(Matricula matricula);

    void eliminarMatricula(Integer idMatricula);

}
