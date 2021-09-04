package com.uah.es.views.usuarios;


import com.uah.es.model.Rol;
import com.uah.es.model.Usuario;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UsuarioForm extends FormLayout {

    // Inputs y btns del formulario
    TextField nombre= new TextField("Nombre");
    EmailField correo = new EmailField ("Correo");
    TextField clave = new TextField ("Clave");
    Checkbox estado = new Checkbox();
    RadioButtonGroup<Rol> roles = new RadioButtonGroup<>();

    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");

    Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);
    Usuario usuario = new Usuario();


    public UsuarioForm(){

        estado.setLabel("Activo");
        estado.setValue(false);

        List<Rol> rolesLista = new ArrayList<>();
        rolesLista.add(new Rol(1,"Admin"));
        rolesLista.add(new Rol(2,"Alumno"));
        rolesLista.add(new Rol(3,"Profesor"));
        roles.setLabel("Rol");
        roles.setItems(rolesLista);
        //roles.addThemeVariants(RadioButtonGroupV.LUMO_VERTICAL);
        roles.isRequired();
        roles.addValueChangeListener(
                e -> {
                    //usuario.setRoles(null);
                    Rol rolesSeleccionados = e.getValue();
                    List<Rol> roles = new ArrayList<Rol>();
                    roles.add(rolesSeleccionados);
                    usuario.setRoles(roles);
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
        /*binder.forField(roles)
                .asRequired("Campo requerido");*/
        binder.forField(estado)
                .bind(Usuario::isEnable,Usuario::setEnable);

        setMaxWidth("600px");
        add(nombre,correo,clave,estado,roles,configurarBtnsLayout());
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

        roles.clear();
        List<Rol> rolesUsuario = usuario.getRoles();

        if (rolesUsuario != null){
            roles.setValue(rolesUsuario.get(0));
        }

        this.usuario = usuario;
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
     * Funcion para desactivar el RadioButtonGroup de roles cuando se modifica un usuario.
     *
     */
    public  void desaactivarRoles() {
        roles.setReadOnly(true);
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
