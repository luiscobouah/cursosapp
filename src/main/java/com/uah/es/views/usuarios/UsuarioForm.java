package com.uah.es.views.usuarios;


import com.uah.es.model.Rol;
import com.uah.es.model.Usuario;
import com.uah.es.service.IRolesService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
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

import java.util.*;

public class UsuarioForm extends FormLayout {

    // Inputs y btns del formulario
    TextField nombre= new TextField("Nombre");
    EmailField correo = new EmailField ("Correo");
    TextField clave = new TextField ("Clave");
    Checkbox estado = new Checkbox();
    RadioButtonGroup<Rol> roles = new RadioButtonGroup<>();
    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");
    H2 titulo = new H2("Usuario");

    Binder<Usuario> binder = new BeanValidationBinder<>(Usuario.class);
    Usuario usuario = new Usuario();
    List<Rol> rolesLista = new ArrayList<Rol>();

    public UsuarioForm(IRolesService rolesService){

        // Se configura componente CheckBox para el estado
        estado.setLabel("Activo");
        estado.setValue(false);

        // Se configura el listado de roles en el componente RadioButtonGroup
        rolesLista = Arrays.asList(rolesService.buscarTodos());
        roles.setLabel("Rol");
        roles.setItems(rolesLista);
        //Se asigna por defecto el rol Alumno
        roles.setValue(rolesLista.get(1));
        roles.isRequired();
        roles.addValueChangeListener(
                e -> {
                    //El rol seleccionado se guarda en el usuario
                    Rol rolesSeleccionados = e.getValue();
                    List<Rol> roles = new ArrayList<Rol>();
                    roles.add(rolesSeleccionados);
                    usuario.setRoles(roles);
                }
        );

        // Relacionamos los atributos del objeto Usuario con los campos del formulario
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
        binder.forField(estado)
                .bind(Usuario::isEnable,Usuario::setEnable);

        setMaxWidth("600px");
        //Se añaden los componentes a la vista
        add(titulo,nombre,correo,clave,estado,roles,configurarBtnsLayout());
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
     * Func para asignar el objeto Usuario al formulario.
     *
     */
    public void setUsuario(Usuario usuario) {

        roles.clear();
        List<Rol> rolesUsuario = usuario.getRoles();

        if (rolesUsuario != null){
            //Se asigna el rol del usuario en el formulario
            roles.setValue(rolesUsuario.get(0));
        } else {
            //Se asigna por defecto el rol Alumno en el formulario
            roles.setValue(rolesLista.get(1));
        }

        this.usuario = usuario;
        binder.readBean(usuario);
    }

    /**
     * Func para validar y guardar el objeto Usuario del formulario.
     *
     */
    private void validarYGuardar() {
        try {
            binder.writeBean(usuario);
            fireEvent(new GuardarEvent(this, usuario));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Func para modificar la visibilidad del RadioButtonGroup de roles cuando se modifica un usuario.
     *
     */
    public  void modificarVisibilidaRoles(boolean estado) {
        roles.setReadOnly(estado);
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
