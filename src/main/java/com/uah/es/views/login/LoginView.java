package com.uah.es.views.login;

import com.uah.es.model.Rol;
import com.uah.es.model.Usuario;
import com.uah.es.service.IRolesService;
import com.uah.es.service.IUsuariosService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("login")
@PageTitle("Login | CursosApp")

//https://vaadin.com/components/vaadin-login/java-examples
//https://vaadin.com/forum/thread/12793447/how-to-implement-user-registration-sign-up-in-a-vaadin-app

public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private LoginForm login = new LoginForm();
    FormLayout registroForm = new FormLayout();
    Binder<Usuario> binder = new Binder<>();

    Button registrarseBtn = new Button("Registrarse");
    Dialog registroDg = new Dialog();
    Notification notificacionOK = new Notification("", 3000);
    Notification notificacionKO = new Notification("", 3000);
    H2 tituloFormulario = new H2("Registro Usuario");

    // Inputs y btns del formulario de registro
    TextField nombre = new TextField("Nombre");
    EmailField correo = new EmailField ("Correo");
    PasswordField contrasena = new PasswordField("Contraseña");
    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");

    IUsuariosService usuariosService;
    IRolesService rolesService;

    Usuario usuario;

    public LoginView(IUsuariosService usuariosService, IRolesService rolesService) {

        this.usuariosService = usuariosService;
        this.rolesService = rolesService;

        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        login.setI18n(configurarLoginEsp());
        login.setAction("login");

        confirgurarFormRegistro();

        registrarseBtn.getStyle().set("cursor", "pointer");
        registrarseBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        registrarseBtn.addClickListener(e -> {
            registroDg.open();
        });

        add(new H1("Cursos App"), login,registrarseBtn);
    }

    private LoginI18n configurarLoginEsp() {

        final LoginI18n i18n = LoginI18n.createDefault();

        i18n.setHeader(new LoginI18n.Header());
        i18n.getHeader().setTitle("Cursos App");
        i18n.getForm().setUsername("Usuario");
        i18n.getForm().setTitle("Inicio de sesión");
        i18n.getForm().setSubmit("Entrar");
        i18n.getForm().setPassword("Contraseña");
        i18n.getForm().setForgotPassword("Recordar contraseña");
        i18n.getErrorMessage().setTitle("Usuario/contraseña incorrecta");
        i18n.getErrorMessage().setMessage("Por favor vuelva a intentarlo");
        return i18n;
    }

    private void confirgurarFormRegistro () {

        usuario = new Usuario();
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        // Relacionamos los atributos del objeto Usuario con los campos del formulario
        binder.forField(nombre)
                .asRequired("Campo requerido")
                .bind(Usuario::getNombre,Usuario::setNombre);
        binder.forField(correo)
                .withValidator(new EmailValidator("Correo no válido"))
                .asRequired("Campo requerido")
                .bind(Usuario::getCorreo,Usuario::setCorreo);
        binder.forField(contrasena)
                .asRequired("Campo requerido")
                .bind(Usuario::getClave,Usuario::setClave);
        // Click listeners for the buttons
        guardarBtn.addClickListener(event -> {
            if (binder.writeBeanIfValid(usuario)) {
                guardarUsuario(usuario);
            }
        });
        cancelarBtn.addClickListener(event -> {
            // clear fields by setting null
            setUsuario();
            registroDg.close();
            //infoLabel.setText("");
        });

        registroForm.add(nombre,correo,contrasena);
        registroForm.setResponsiveSteps(new FormLayout.ResponsiveStep("1px", 1));

        HorizontalLayout btnsLayout = new HorizontalLayout();
        btnsLayout.add(cancelarBtn, guardarBtn);
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        registroDg.add(tituloFormulario,registroForm,btnsLayout);
        registroDg.setMaxWidth("600px");

    }

    public void guardarUsuario(Usuario usuario) {

        List<Rol> rolesLista = new ArrayList<>();
        rolesLista.add(new Rol(2,"ROLE_USER"));
        usuario.setRoles(rolesLista);
        usuario.setEnable(true);

        if(usuariosService.guardarUsuario(usuario)){
            registroDg.close();
            notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notificacionOK.setText("Se ha creado correctamente el usuario");
            notificacionOK.open();
            setUsuario();
        } else {
            notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notificacionKO.setText("Error al crear el usuario");
            notificacionKO.open();
        }
    }
    public void setUsuario() {

        Usuario usuario = new Usuario();
        this.usuario = usuario;
        binder.readBean(usuario);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if(beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            login.setError(true);
        }
    }
}