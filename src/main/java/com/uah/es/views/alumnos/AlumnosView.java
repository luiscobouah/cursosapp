package com.uah.es.views.alumnos;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.uah.es.views.MainLayout;

@PageTitle("Alumnos")
@Route(value = "alumnos", layout = MainLayout.class)
public class AlumnosView extends Div {

    public AlumnosView() {
        addClassName("alumnos-view");
        add(new Text("Content placeholder"));
    }

}
