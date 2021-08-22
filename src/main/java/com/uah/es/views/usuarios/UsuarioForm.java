package com.uah.es.views.usuarios;


import com.uah.es.model.Rol;
import com.uah.es.model.Usuario;
import com.uah.es.service.ICursosService;
import com.uah.es.service.IRolesService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.checkbox.CheckboxGroupVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.shared.Registration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UsuarioForm extends FormLayout {

    // Inputs y btns del formulario
    TextField nombre= new TextField("Nombre");
    EmailField correo = new EmailField ("Correo");
    TextField clave = new TextField ("Clave");
    Checkbox estado = new Checkbox();
    CheckboxGroup<String> roles = new CheckboxGroup<>();

    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");

    Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);
    Usuario usuario = new Usuario();


    public UsuarioForm(){

        List<Rol> rolesLista = new ArrayList<>();

        Rol rolAdmin = new Rol(1,"Administrador");
        Rol rolAlumno = new Rol(2,"Alumno");
        Rol rolProfesor = new Rol(3,"Profesor");

        rolesLista.add(rolAdmin);
        rolesLista.add(rolAlumno);
        rolesLista.add(rolProfesor);

        //usuario.setRoles(rolesLista);

        estado.setLabel("Activo");
        estado.setValue(false);

        roles.setLabel("Rol");
        roles.setItems("Administrador", "Alumno", "Profesor");
        roles.addThemeVariants(CheckboxGroupVariant.LUMO_VERTICAL);
        roles.addValueChangeListener(
                e -> {
                    usuario.setRoles(null);
                    List<Rol> rolesSeleccionados = new ArrayList<>();
                    for(int i = 0; i < e.getValue().size(); i++) {
                        rolesSeleccionados.add(rolesLista.get(i));
                    }
                    usuario.setRoles(rolesSeleccionados);
                }
        );

        // Relacionamos los atributos del objeto Alumno con los campos del formulario
        binder.forField(nombre)
                .asRequired("Campo requerido")
                .bind(Usuario::getNombre,Usuario::setNombre);
        binder.forField(correo)
                .withValidator(new EmailValidator("Correo no válido"))
                .asRequired("Campo requerido")
                .bind(Usuario::getCorreo,Usuario::setCorreo);
        binder.forField(clave)
                .asRequired("Campo requerido")
                .bind(Usuario::getClave,Usuario::setClave);
        /*binder.forField(precio)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("No es un precio válido"))
                .asRequired("Campo requerido")
                .bind(Curso::getPrecio,Curso::setPrecio);*/
        binder.forField(estado)
                .bind(Usuario::isEnable,Usuario::setEnable);

        setMaxWidth("600px");
        add(nombre,correo,clave,roles,estado,configurarBtnsLayout());
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

        HorizontalLayout btnsLayout =  new HorizontalLayout();
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.add(cancelarBtn,guardarBtn);

        return btnsLayout;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;

        System.out.println(usuario.getRoles().size());

        if (usuario.getRoles() != null){
            for(int i = 0; i < usuario.getRoles().size(); i++) {
                String rol = usuario.getRoles().get(i).getAuthority();
                System.out.println(rol);
                roles.setValue(Collections.singleton(rol));
            }
        }

        binder.readBean(usuario);
    }

    private void validarYGuardar() {
        try {
            binder.writeBean(usuario);
            fireEvent(new GuardarEvent(this, usuario));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eventos de los botones del formulario.
     *
     */
    public static abstract class CursoFormEvent extends ComponentEvent<UsuarioForm> {
        private Usuario usuario;

        protected CursoFormEvent(UsuarioForm source, Usuario usuario) {
            super(source, false);
            this.usuario = usuario;
        }

        public Usuario getUsuario() {
            return usuario;
        }
    }

    public static class GuardarEvent extends UsuarioForm.CursoFormEvent {
        GuardarEvent(UsuarioForm source, Usuario usuario) {
            super(source, usuario);
        }
    }

    public static class CerrarEvent extends UsuarioForm.CursoFormEvent {
        CerrarEvent(UsuarioForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }

}
