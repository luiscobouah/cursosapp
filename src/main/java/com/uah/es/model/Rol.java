package com.uah.es.model;

import java.util.Objects;

public class Rol {

    private Integer idRol;
    private String authority;

    public Rol() {
    }

    public Rol(Integer idRol, String authority) {
        this.idRol = idRol;
        this.authority = authority;
    }

    public Rol(String idRolAndName){
        if(idRolAndName != null && idRolAndName.length() > 0){
            String[] fieldPositions = idRolAndName.split("-");
            this.idRol = Integer.parseInt(fieldPositions[0]);
            this.authority = fieldPositions[1];
        }
    }

    public Integer getIdRol() {
        return idRol;
    }

    public void setIdRol(Integer idRol) {
        this.idRol = idRol;
    }

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String toString() {
        return ""+idRol+"-"+this.authority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rol)) return false;
        Rol rol = (Rol) o;
        return idRol.equals(rol.idRol) && authority.equals(rol.authority);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idRol, authority);
    }
}
