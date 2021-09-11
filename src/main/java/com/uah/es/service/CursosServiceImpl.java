package com.uah.es.service;

import com.uah.es.model.Curso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CursosServiceImpl implements ICursosService {

    String url = "http://localhost:8002/cursos";

    @Autowired
    RestTemplate template;

    @Override
    public Curso[] buscarTodos() {
        return template.getForObject(url, Curso[].class);
    }

    @Override
    public Curso buscarCursoPorId(Integer idCurso) {
        return template.getForObject(url + "/" + idCurso, Curso.class);
    }

   @Override
    public Curso[] buscarCursosPorNombre(String nombre) {
       return template.getForObject(url + "/nombre/" + nombre, Curso[].class);
    }

    @Override
    public Curso[] buscarCursosPorCategoria(String categoria) {
        return template.getForObject(url + "/categoria/" + categoria, Curso[].class);
    }

   @Override
    public Curso[] buscarCursosPorProfesor(String profesor) {
        return template.getForObject(url + "/profesor/" + profesor, Curso[].class);
    }

    @Override
    public boolean guardarCurso(Curso curso) {

        boolean result = false;
        curso.setIdCurso(0);
        try {
            template.postForEntity(url, curso, String.class);
            result = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }

    @Override
    public boolean actualizarCurso(Curso curso) {

        boolean result = false;
        try {
            template.put(url, curso);
            result = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }

    @Override
    public boolean eliminarCurso(Integer idCurso) {
        boolean result = false;

        try {
            template.delete(url + "/" + idCurso);
            result = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }

}
