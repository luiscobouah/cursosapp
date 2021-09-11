package com.uah.es.service;

import com.uah.es.model.Usuario;


public interface IUsuariosService {

    Usuario[] buscarTodos();

    Usuario[] buscarUsuariosPorRol(Integer idRol);

    Usuario buscarUsuarioPorId(Integer idUsuario);

    Usuario buscarUsuarioPorNombre(String nombre);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario login(String correo, String clave);

    boolean guardarUsuario(Usuario usuario);

    boolean actualizarUsuario(Usuario usuario);

    boolean eliminarUsuario(Integer idUsuario);

}
