package com.uah.es.service;

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.model.Matricula;
import com.uah.es.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class MatriculasServiceImpl implements IMatriculasService {

    @Autowired
    RestTemplate template;

    @Autowired
    IAlumnosService alumnosService;

    @Autowired
    IUsuariosService usuariosService;

    @Autowired
    ICursosService cursosService;

    String url = "http://localhost:8003/matriculas";

    /*@Override
    public Page<Matricula> buscarTodas(Pageable pageable) {
        Matricula[] matriculas = template.getForObject(url, Matricula[].class);
        List<Matricula> matriculasList = Arrays.asList(matriculas);

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Matricula> list;

        if(matriculasList.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, matriculasList.size());
            list = matriculasList.subList(startItem, toIndex);
        }
        Page<Matricula> page = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), matriculasList.size());
        return page;
    }*/

    /*@Override
    public Page<Matricula> buscarMatriculasPorIdCurso(Integer idCurso, Pageable pageable) {
        Matricula[] matriculas = template.getForObject(url+"/curso/"+idCurso, Matricula[].class);
        List<Matricula> matriculasList = Arrays.asList(matriculas);

        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();
        int startItem = currentPage * pageSize;
        List<Matricula>list;

        if(matriculasList.size() <startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, matriculasList.size());
            list = matriculasList.subList(startItem, toIndex);
        }
        Page<Matricula> page = new PageImpl<>(list, PageRequest.of(currentPage, pageSize), matriculasList.size());
        return page;
    }*/

    @Override
    public Matricula buscarMatriculaPorId(Integer idMatricula) {
        Matricula matricula = template.getForObject(url+"/"+idMatricula, Matricula.class);
        return matricula;
    }

    @Override
    public boolean guardarMatricula(Matricula matricula) {
        if (matricula.getIdMatricula() != null && matricula.getIdMatricula() > 0) {
            //template.put(url, matricula); //peligroso, habría que comprobar la inscripción anterior
            //return "No se puede modificar una matrícula.";
            return false;
        } else {
            //Inscribimos al alumno en el curso
            Usuario usuario = usuariosService.buscarUsuarioPorId(matricula.getUsuario().getIdUsuario());
            Alumno alumno = alumnosService.buscarAlumnoPorCorreo(usuario.getCorreo());
            Curso curso = cursosService.buscarCursoPorId(matricula.getIdCurso());
            boolean resultado= false;
            //si no existe el alumno, lo creamos
            if(alumno == null) {
                alumno = new Alumno(usuario.getNombre(), usuario.getCorreo());
                alumnosService.guardarAlumno(alumno);
                //resultado = "Alumno creado. ";
                resultado = true;
            } else { //si existe, comprobamos que no se matricula dos veces en el mismo curso
                //resultado = "Alumno encontrado. ";
                resultado = true;
                List<Curso> cursos = alumno.getCursos();
                if (cursos.contains(curso)) {
                    resultado = true;
                    //return "El alumno ya existe en el curso!";
                }
            }
            alumnosService.inscribirCurso(alumno.getIdAlumno(), matricula.getIdCurso());

            //Guardamos la matrícula
            matricula.setPrecio(curso.getPrecio());
            matricula.setIdMatricula(0);
            matricula.setFecha(new Date());

            try {
                template.postForObject(url, matricula, String.class);

            } catch (HttpClientErrorException ex){

                System.out.println(ex);
            }


           // return resultado + "Los datos de la matricula fueron guardados!";
            return resultado;
        }
    }

    @Override
    public void eliminarMatricula(Integer idMatricula) {
        template.delete(url+ "/" +  idMatricula);
    }

}
