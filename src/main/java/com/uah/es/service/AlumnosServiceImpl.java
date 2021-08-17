package com.uah.es.service;

import com.uah.es.model.Alumno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AlumnosServiceImpl implements IAlumnosService {

    @Autowired
    RestTemplate template;

    String url = "http://localhost:8002/alumnos";

    @Override
    public Alumno[] buscarTodos() {
        return template.getForObject(url, Alumno[].class);
    }

    @Override
    public Alumno buscarAlumnoPorId(Integer idAlumno) {
        return template.getForObject(url+"/"+idAlumno, Alumno.class);
    }

    @Override
    public Alumno buscarAlumnoPorCorreo(String correo) {
        return template.getForObject(url+"/correo/"+correo, Alumno.class);
    }

    @Override
    public boolean guardarAlumno(Alumno alumno) {

        boolean result = false;
        alumno.setIdAlumno(0);
        ResponseEntity<String> response = template.postForEntity(url, alumno, String.class);
        // Verificar la respuesta de la petición
        if (response.getStatusCode() == HttpStatus.OK) {
            result = true;
        }
        return result;
    }

    @Override
    public boolean actualizarAlumno(Alumno alumno) {

        boolean result = false;
        if (alumno.getIdAlumno() != null && alumno.getIdAlumno() > 0) {
            template.put(url, alumno);
            result=true;
        }
        return result;
    }

    @Override
    public void eliminarAlumno(Integer idAlumno) {
        template.delete(url + "/" + idAlumno);
    }

    @Override
    public void inscribirCurso(Integer idAlumno, Integer idCurso) {
        template.getForObject(url+"/insc/"+idAlumno+"/"+idCurso, String.class);
    }
}
