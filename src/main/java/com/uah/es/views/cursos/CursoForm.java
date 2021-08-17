package com.uah.es.views.cursos;

import com.uah.es.model.Curso;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.shared.Registration;

public class CursoForm extends FormLayout {

    // Inputs y btns del formulario
    TextField nombre= new TextField("Nombre");
    TextField duracion = new TextField ("Duración");
    TextField profesor = new TextField ("Profesor");
    TextField precio = new TextField ("Precio");
    TextField categoria = new TextField ("Categoría");
    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");

    Binder<Curso> binder = new BeanValidationBinder<>(Curso.class);
    private Curso curso = new Curso();

    public CursoForm(){

        // Relacionamos los atributos del objeto Alumno con los campos del formulario
        binder.forField(nombre)
                .asRequired("Campo requerido")
                .bind(Curso::getNombre,Curso::setNombre);
        binder.forField(profesor)
                .asRequired("Campo requerido")
                .bind(Curso::getProfesor,Curso::setProfesor);
        binder.forField(categoria)
                .asRequired("Campo requerido")
                .bind(Curso::getCategoria,Curso::setCategoria);

        add(nombre,profesor,categoria,configurarBtnsLayout());
    }

    private Component configurarBtnsLayout() {

        //Se configura los btns del formulario
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        guardarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        guardarBtn.addClickListener(click -> validarYGuardar());
        cancelarBtn.addClickListener(click -> fireEvent(new CerrarEvent(this)));

        binder.addStatusChangeListener(evt -> guardarBtn.setEnabled(binder.isValid()));

        return new HorizontalLayout(guardarBtn,cancelarBtn);
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
        binder.readBean(curso);
    }

    private void validarYGuardar() {
        try {
            binder.writeBean(curso);
            fireEvent(new CursoForm.GuardarEvent(this, curso));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eventos de los botones del formulario.
     *
     */
    public static abstract class CursoFormEvent extends ComponentEvent<CursoForm> {
        private Curso curso;

        protected CursoFormEvent(CursoForm source, Curso curso) {
            super(source, false);
            this.curso = curso;
        }

        public Curso getCurso() {
            return curso;
        }
    }

    public static class GuardarEvent extends CursoForm.CursoFormEvent {
        GuardarEvent(CursoForm source, Curso curso) {
            super(source, curso);
        }
    }

    public static class CerrarEvent extends CursoForm.CursoFormEvent {
        CerrarEvent(CursoForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }



}
