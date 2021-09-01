package com.uah.es.security;

import com.uah.es.views.login.LoginView;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.NotFoundException;
import com.vaadin.flow.server.ServiceInitEvent;
import com.vaadin.flow.server.VaadinServiceInitListener;

import org.springframework.stereotype.Component;

@Component
public class ConfigureUIServiceInitListener implements VaadinServiceInitListener {

    @Override
    public void serviceInit(ServiceInitEvent event) {
        event.getSource().addUIInitListener(uiEvent -> {
            final UI ui = uiEvent.getUI();
            ui.addBeforeEnterListener(this::beforeEnter); // (2)
        });
    }

    /**
     * Reroutes the user if (s)he is not authorized to access the view.
     *
     * @param event
     *            before navigation event with event details
     */
    private void beforeEnter(BeforeEnterEvent event) {
        if(!SecurityUtils.isAccessGranted(event.getNavigationTarget())) { // (1)
            if(SecurityUtils.isUserLoggedIn()) { // (2)
                event.rerouteToError(NotFoundException.class); // (3)
            } else {
                event.rerouteTo(LoginView.class); // (4)
            }
        }
    }
}