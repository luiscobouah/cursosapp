package com.uah.es.service;

import com.uah.es.model.Rol;


public interface IRolesService {

    Rol[] buscarTodos();

    Rol buscarRolPorId(Integer idRol);

    void guardarRol(Rol rol);

    void eliminarRol(Integer idRol);

}
