package com.uah.es.service;

import com.uah.es.model.Alumno;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AlumnosServiceImpl implements IAlumnosService {

    @Autowired
    RestTemplate template;

    String url = "http://localhost:8001/alumnos";

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
    public void guardarAlumno(Alumno alumno) {
        if (alumno.getIdAlumno() != null && alumno.getIdAlumno() > 0) {
            template.put(url, alumno);
        } else {
            alumno.setIdAlumno(0);
            template.postForObject(url, alumno, String.class);
        }
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
