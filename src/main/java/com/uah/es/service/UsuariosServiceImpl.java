package com.uah.es.service;

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.model.Rol;
import com.uah.es.model.Usuario;
import com.uah.es.utils.Configuracion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class UsuariosServiceImpl implements IUsuariosService {

    @Autowired
    RestTemplate template;

    @Autowired
    AlumnosServiceImpl alumnosService;

    String url = Configuracion.URL_SERVICIO_USUARIOS;

    @Override
    public Usuario[] buscarTodos() {
        return template.getForObject(url, Usuario[].class);
    }

    @Override
    public Usuario[] buscarUsuariosPorRol(Integer idRol) {
        return template.getForObject(url+"/rol/"+idRol, Usuario[].class);
    }


    @Override
    public Usuario buscarUsuarioPorId(Integer idUsuario) {
        return template.getForObject(url + "/" + idUsuario, Usuario.class);
    }

    @Override
    public Usuario buscarUsuarioPorNombre(String nombre) {
        return template.getForObject(url+"/nombre/"+nombre, Usuario.class);
    }

    @Override
    public Usuario buscarUsuarioPorCorreo(String correo) {
        return template.getForObject(url+"/correo/"+correo, Usuario.class);
    }

    @Override
    public Usuario login(String correo, String clave) {
        return template.getForObject(url+"/login/"+correo+"/"+clave, Usuario.class);
    }

    @Override
    public boolean guardarUsuario(Usuario usuario) {

        boolean result = false;
        usuario.setIdUsuario(0);
        try {
            template.postForEntity(url, usuario, String.class);

            // Si el nuevo usuario tiene rol Alumno, entonces creamos un nuevo alumno
            if(usuario.getRoles().contains(new Rol(2,"Alumno"))){
                Alumno alumno = new Alumno(usuario.getNombre(),usuario.getCorreo());
                alumnosService.guardarAlumno(alumno);
            }
            result = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }

    @Override
    public boolean actualizarUsuario(Usuario usuario) {

        boolean result = false;
        try {
            template.put(url, usuario);
            result = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }

    @Override
    public boolean eliminarUsuario(Integer idUsuario) {
        boolean result = false;

        try {
            template.delete(url+"/"+idUsuario);
            result = true;
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        return result;
    }

 }
