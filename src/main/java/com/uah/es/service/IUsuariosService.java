package com.uah.es.service;

import com.uah.es.model.Usuario;


public interface IUsuariosService {

    //Page<Usuario> buscarTodos(Pageable pageable);

    Usuario buscarUsuarioPorId(Integer idUsuario);

    Usuario buscarUsuarioPorNombre(String nombre);

    Usuario buscarUsuarioPorCorreo(String correo);

    Usuario login(String correo, String clave);

    void guardarUsuario(Usuario usuario);

    void eliminarUsuario(Integer idUsuario);

}
