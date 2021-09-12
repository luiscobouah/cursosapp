package com.uah.es.views.alumnos;

import com.uah.es.model.Alumno;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.shared.Registration;

public class AlumnoForm extends FormLayout {

    // Inputs y btns del formulario
    TextField nombre= new TextField("Nombre");
    EmailField correo = new EmailField ("Correo");
    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");
    H2 titulo = new H2("Alumno");

    Binder<Alumno> binder = new BeanValidationBinder<>(Alumno.class);
    private Alumno alumno = new Alumno();

    public AlumnoForm(){

        // Se relacionan los atributos del objeto Alumno con los campos del formulario
        binder.forField(correo)
                .withValidator(new EmailValidator("Correo no válido"))
                .asRequired("Campo requerido")
                .bind(Alumno::getCorreo,Alumno::setCorreo);
        binder.forField(nombre)
                .asRequired("Campo requerido")
                .bind( Alumno::getNombre,Alumno::setNombre);

        setMaxWidth("600px");
        //Se añaden los componentes a la vista
        add(titulo,nombre,correo,configurarBtnsLayout());
    }

    /**
     * Func para configurar los botones del formulario.
     *
     */
    private Component configurarBtnsLayout() {

        //Se configura los btns del formulario
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        guardarBtn.addClickListener(click -> validarYGuardar());
        cancelarBtn.addClickListener(click -> fireEvent(new CerrarEvent(this)));
        binder.addStatusChangeListener(evt -> guardarBtn.setEnabled(binder.isValid()));

        HorizontalLayout btnsLayout =  new HorizontalLayout();
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.add(cancelarBtn,guardarBtn);
        return btnsLayout;
    }

    /**
     * Func para asignar el objeto Alumno al formulario.
     *
     */
    public void setAlumno(Alumno alumno) {

        this.alumno = alumno;
        binder.readBean(alumno);
    }

    /**
     * Func para validar y guardar el objeto Alumno del formulario.
     *
     */
    private void validarYGuardar() {
        try {
            binder.writeBean(alumno);
            fireEvent(new GuardarEvent(this, alumno));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eventos de los botones del formulario.
     *
     */
    public static abstract class AlumnoFormEvent extends ComponentEvent<AlumnoForm> {
        private Alumno alumno;

        protected AlumnoFormEvent(AlumnoForm source, Alumno alumno) {
            super(source, false);
            this.alumno = alumno;
        }

        public Alumno getAlumno() {
            return alumno;
        }
    }

    public static class GuardarEvent extends AlumnoFormEvent {
        GuardarEvent(AlumnoForm source, Alumno alumno) {
            super(source, alumno);
        }
    }

    public static class CerrarEvent extends AlumnoFormEvent {
        CerrarEvent(AlumnoForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }


}
