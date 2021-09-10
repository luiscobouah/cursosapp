package com.uah.es.views;

import com.vaadin.flow.router.*;

import javax.servlet.http.HttpServletResponse;

@ParentLayout(MainLayout.class)
public class CustomNotFoundTarget extends RouteNotFoundError {

    @Override
    public int setErrorParameter(BeforeEnterEvent event, ErrorParameter<NotFoundException> parameter) {
        getElement().setText("No se encontró la página solicitada'");
        return HttpServletResponse.SC_NOT_FOUND;
    }
}
