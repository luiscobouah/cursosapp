package com.uah.es.views.cursos;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.uah.es.views.MainLayout;
import com.vaadin.flow.router.RouteAlias;

@PageTitle("Cursos")
@Route(value = "cursos", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class CursosView extends Div {

    public CursosView() {
        addClassName("cursos-view");
        add(new Text("Content placeholder"));
    }

}
