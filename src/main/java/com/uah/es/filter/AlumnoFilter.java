package com.uah.es.filter;

import com.uah.es.model.Alumno;
import org.apache.commons.lang3.StringUtils;

public class AlumnoFilter {
    String correo = "";

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean test(Alumno alumno) {
        if (correo.length() > 0 && !StringUtils.containsIgnoreCase(String.valueOf(alumno.getCorreo()),correo)) {
            return false;
        }

        return true;
    }
}
