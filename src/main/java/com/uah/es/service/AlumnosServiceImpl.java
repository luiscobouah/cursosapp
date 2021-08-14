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

    /*@Override
    public Page<Alumno> buscarTodos(Pageable pageable) {
        Alumno[] alumnos = template.getForObject(url, Alumno[].class);
        List<Alumno> alumnosList = Arrays.asList(alumnos);

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Alumno> list;

        if(alumnosList.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, alumnosList.size());
            list = alumnosList.subList(startItem, toIndex);
        }
        Page<Alumno> page = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), alumnosList.size());
        return page;
    }*/

    @Override
    public Alumno[] buscarTodos() {
        return template.getForObject(url, Alumno[].class);
    }

    @Override
    public Alumno buscarAlumnoPorId(Integer idAlumno) {
        Alumno alumno = template.getForObject(url+"/"+idAlumno, Alumno.class);
        return alumno;
    }

    @Override
    public Alumno buscarAlumnoPorCorreo(String correo) {
        Alumno alumno = template.getForObject(url+"/correo/"+correo, Alumno.class);
        return alumno;
    }

    @Override
    public Boolean guardarAlumno(Alumno alumno) {

        Boolean result = false;

        alumno.setIdAlumno(0);
        ResponseEntity<String> response = template.postForEntity(url, alumno, String.class);

        // Verificar la respuesta de la petición
        if (response.getStatusCode() == HttpStatus.OK) {
            result = true;
            System.out.println("Request Successful");
        } else {
            result = false;
            System.out.println("Request Failed");
        }

        return result;

        /*  if (alumno.getIdAlumno() != null && alumno.getIdAlumno() > 0) {
            template.put(url, alumno);
        } else {*/

       /* } */
    }

    @Override
    public Boolean actualizarAlumno(Alumno alumno) {

        Boolean result = false;

        if (alumno.getIdAlumno() != null && alumno.getIdAlumno() > 0) {
            template.put(url, alumno);
            result=true;
        } else {
            result = false;
        }

        return result;


        //ResponseEntity<String> response = template.postForEntity(url, alumno, String.class);

        // Verificar la respuesta de la petición
        /*if (response.getStatusCode() == HttpStatus.OK) {
            result = true;
            System.out.println("Request Successful");
        } else {
            result = false;
            System.out.println("Request Failed");
        }

        return result;*/


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
