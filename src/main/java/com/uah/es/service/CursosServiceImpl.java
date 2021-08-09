package com.uah.es.service;

import com.uah.es.model.Curso;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CursosServiceImpl implements ICursosService {

    @Autowired
    RestTemplate template;

    String url = "http://localhost:8090/api/zcursos/cursos";

    /*@Override
    public Page<Curso> buscarTodos(Pageable pageable) {
        Curso[] cursos = template.getForObject(url, Curso[].class);
        List<Curso> cursosList = Arrays.asList(cursos);

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Curso> list;

        if (cursosList.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, cursosList.size());
            list = cursosList.subList(startItem, toIndex);
        }

        Page<Curso> page = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), cursosList.size());
        return page;
    }*/

    @Override
    public Curso buscarCursoPorId(Integer idCurso) {
        Curso curso = template.getForObject(url + "/" + idCurso, Curso.class);
        return curso;
    }

   /*@Override
    public Page<Curso> buscarCursosPorNombre(String nombre, Pageable pageable) {
        Curso[] cursos = template.getForObject(url + "/nombre/" + nombre, Curso[].class);
        List<Curso> lista = Arrays.asList(cursos);
        Page<Curso> page = new PageImpl<>(lista, pageable, lista.size());
        return page;
    }*/

    /*@Override
    public Page<Curso> buscarCursosPorCategoria(String categoria, Pageable pageable) {
        Curso[] cursos = template.getForObject(url + "/categoria/" + categoria, Curso[].class);
        List<Curso> lista = Arrays.asList(cursos);
        Page<Curso> page = new PageImpl<>(lista, pageable, lista.size());
        return page;
    }*/

   /*@Override
    public Page<Curso> buscarCursosPorProfesor(String profesor, Pageable pageable) {
        Curso[] cursos = template.getForObject(url + "/profesor/" + profesor, Curso[].class);
        List<Curso> lista = Arrays.asList(cursos);
        Page<Curso> page = new PageImpl<>(lista, pageable, lista.size());
        return page;
    }*/

    @Override
    public void guardarCurso(Curso curso) {
        if (curso.getIdCurso() != null && curso.getIdCurso() > 0) {
            template.put(url, curso);
        } else {
            curso.setIdCurso(0);
            template.postForObject(url, curso, String.class);
        }
    }

    @Override
    public void eliminarCurso(Integer idCurso) {
        template.delete(url + "/" + idCurso);
    }
}
