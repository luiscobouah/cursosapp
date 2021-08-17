package com.uah.es.service;

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CursosServiceImpl implements ICursosService {

    @Autowired
    RestTemplate template;

    String url = "http://localhost:8002/cursos";

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
        ResponseEntity<String> response = template.postForEntity(url, curso, String.class);

        // Verificar la respuesta de la petición
        if (response.getStatusCode() == HttpStatus.OK) {
            result = true;
            System.out.println("Request Successful");
        }
        return result;
    }

    @Override
    public boolean actualizarCurso(Curso curso) {

        boolean result = false;
        if (curso.getIdCurso() != null && curso.getIdCurso() > 0) {
            template.put(url, curso);
            result=true;
        }
        return result;
    }

    @Override
    public void eliminarCurso(Integer idCurso) {
        template.delete(url + "/" + idCurso);
    }
}
