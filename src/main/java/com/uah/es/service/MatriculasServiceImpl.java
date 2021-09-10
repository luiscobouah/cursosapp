package com.uah.es.service;

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.model.Matricula;
import com.uah.es.model.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

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

    @Override
    public Matricula[] buscarTodas() {
        return template.getForObject(url, Matricula[].class);
    }

    @Override
    public Matricula buscarMatriculaPorId(Integer idMatricula) {
        Matricula matricula = template.getForObject(url+"/"+idMatricula, Matricula.class);
        return matricula;
    }

    @Override
    public Matricula buscarMatriculaPorIdCursoIdUsuario(Integer idCurso, Integer idUsuario) {
        Matricula matricula = template.getForObject(url+"/cursousuario/"+idCurso+"/"+idUsuario, Matricula.class);
        return matricula;
    }

    @Override
    public boolean guardarMatricula(Matricula matricula) {
        boolean resultado= false;

        if (matricula.getIdMatricula() != null && matricula.getIdMatricula() > 0) {
            //template.put(url, matricula); //peligroso, habría que comprobar la inscripción anterior
            //return "No se puede modificar una matrícula.";
            return false;
        } else {
            //Inscribimos al alumno en el curso
            Usuario usuario = usuariosService.buscarUsuarioPorId(matricula.getUsuario().getIdUsuario());
            Alumno alumno = alumnosService.buscarAlumnoPorCorreo(usuario.getCorreo());
            Curso curso = cursosService.buscarCursoPorId(matricula.getIdCurso());
            try {
                //si no existe el alumno, lo creamos
                if(alumno == null) {
                    alumno = new Alumno(usuario.getNombre(), usuario.getCorreo());
                    alumnosService.guardarAlumno(alumno);

                    //si existe, comprobamos que no se matricula dos veces en el mismo curso
                } else {
                    List<Curso> cursos = alumno.getCursos();
                    //el alumno ya existe en el curso.
                    if (cursos.contains(curso)) {
                        //El alumno ya existe en el curso.
                        resultado = false;
                    //el alumno no existe en el curso.
                    } else {
                        // inscribimos el curso
                        alumnosService.inscribirCurso(alumno.getIdAlumno(), matricula.getIdCurso());
                        //Guardamos la matrícula
                        matricula.setPrecio(curso.getPrecio());
                        matricula.setIdMatricula(0);
                        matricula.setFecha(new Date());
                        template.postForObject(url, matricula, String.class);
                        resultado = true;
                    }
                }
            } catch (HttpClientErrorException ex){
                resultado = false;
            }
            return resultado;
        }
    }

    @Override
    public boolean eliminarMatricula(Curso curso, Alumno alumno) {
        Usuario usuario =  usuariosService.buscarUsuarioPorCorreo(alumno.getCorreo());
        Matricula matricula = buscarMatriculaPorIdCursoIdUsuario(curso.getIdCurso(),usuario.getIdUsuario());

        boolean resultado =  false;
        try {
            template.delete(url+ "/" +  matricula.getIdMatricula());
            alumnosService.desinscribirCurso(alumno.getIdAlumno(),curso.getIdCurso());
            resultado = true;
        } catch (HttpClientErrorException ex){
            resultado = false;
        }

        return resultado;
    }

}
