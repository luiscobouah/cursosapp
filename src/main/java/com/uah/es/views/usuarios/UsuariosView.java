package com.uah.es.views.usuarios;


import com.uah.es.model.Usuario;
import com.uah.es.service.IRolesService;
import com.uah.es.service.IUsuariosService;
import com.uah.es.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Objects;

@PageTitle("Usuarios")
@Route(value = "usuarios", layout = MainLayout.class)
@Secured("ROLE_Administrador")
public class UsuariosView extends Div {

    IUsuariosService usuariosService;

    //Componentes visuales
    UsuarioForm usuarioForm;
    PaginatedGrid<Usuario> grid = new PaginatedGrid<>();
    TextField nombreFiltro = new TextField();
    TextField correoFiltro = new TextField();

    Button buscarBtn = new Button("Buscar");
    Button mostrarTodosBtn = new Button("Mostrar todos");
    Button nuevoUsuarioBtn = new Button("Nuevo usuario",new Icon(VaadinIcon.PLUS));
    Dialog formularioDg = new Dialog();
    Notification notificacion = new Notification("", 3000);

    public UsuariosView(IUsuariosService usuariosService) {

        this.usuariosService = usuariosService;

        addClassName("cursos-view");
        HorizontalLayout superiorLayout = new HorizontalLayout(configurarBuscador(),configurarFormulario());
        superiorLayout.setPadding(true);
        add(superiorLayout,configurarGrid());
    }

    /**
     * Configuración del grid y sus columnas.
     *
     */
    private Component configurarGrid() {

        VerticalLayout layoutGrid = new VerticalLayout();

        // Se añaden las columnas al grid
        grid.addColumn(Usuario::getIdUsuario).setHeader("ID").setKey("id").setSortable(true).setAutoWidth(true);
        grid.addColumn(Usuario::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(Usuario::getCorreo).setHeader("Correo").setKey("correo").setSortable(false).setAutoWidth(true);
        grid.addColumn(Usuario::getClave).setHeader("Clave").setKey("clave").setSortable(false).setAutoWidth(true);
        grid.addColumn(Usuario::getStringRoles).setHeader("Roles").setKey("roles").setSortable(false).setAutoWidth(true);
        grid.addComponentColumn(item -> {
                    Icon editarIcon = new Icon(VaadinIcon.CHECK_CIRCLE_O);
                    if(item.isEnable()){
                        editarIcon.setColor("blue");
                    } else {
                        editarIcon.setColor("gray");
                    }
                    editarIcon.setSize("18px");
                    return editarIcon;
                })
                .setKey("estado")
                .setHeader("Estado")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);
        grid.addComponentColumn(item -> {
                    Icon editarIcon = new Icon(VaadinIcon.EDIT);
                    editarIcon.setColor("green");
                    editarIcon.getStyle().set("cursor", "pointer");
                    editarIcon.setSize("18px");
                    editarIcon.addClickListener(e -> editarUsuario(item));
                    return editarIcon;
                })
                .setKey("editar")
                .setHeader("Editar")
                .setTextAlign(ColumnTextAlign.CENTER);
        grid.addComponentColumn(item -> {
                    Icon editarIcon = new Icon(VaadinIcon.TRASH);
                    editarIcon.setColor("red");
                    editarIcon.getStyle().set("cursor", "pointer");
                    editarIcon.setSize("18px");
                    editarIcon.addClickListener(e -> eliminarUsuario(item));
                    return editarIcon;
                })
                .setKey("eliminar")
                .setHeader("Eliminar")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        // Número max de elementos a visualizar en cada página del grid
        grid.setPageSize(10);
        //grid.setHeightByRows(true);
        grid.setColumnReorderingAllowed(true);
        //grid.setSelectionMode(Grid.SelectionMode.MULTI);
        obtenerTodosUsuarios();
        layoutGrid.add(grid);

        return layoutGrid;
    }

    /**
     * Configuracion del buscador.
     *
     */
    private Component configurarBuscador() {

        HorizontalLayout buscadorLayout = new HorizontalLayout();

        // Configuracion del filtro para buscar por nombre
        nombreFiltro.setLabel("Nombre");
        nombreFiltro.setWidth("30%");
        nombreFiltro.setClearButtonVisible(true);
        nombreFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        // Configuracion del filtro para buscar por profesor
        correoFiltro.setLabel("Correo");
        correoFiltro.setWidth("30%");
        correoFiltro.setClearButtonVisible(true);
        correoFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        buscarBtn.setEnabled(false);

        // Se habilita el btn buscar solo cuando el nombre tenga valor
        nombreFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(nombreFiltro.getValue(), ""));
            correoFiltro.setEnabled(Objects.equals(nombreFiltro.getValue(), ""));
        });
        correoFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(correoFiltro.getValue(), ""));
            nombreFiltro.setEnabled(Objects.equals(correoFiltro.getValue(), ""));
        });

        buscarBtn.getStyle().set("cursor", "pointer");
        buscarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        buscarBtn.addClickListener(e -> filtrar());

        mostrarTodosBtn.getStyle().set("cursor", "pointer");
        mostrarTodosBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mostrarTodosBtn.addClickListener(e -> {
            nombreFiltro.clear();
            correoFiltro.clear();
            obtenerTodosUsuarios();
        });

        HorizontalLayout layoutBtns = new HorizontalLayout();
        layoutBtns.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtns.add(buscarBtn,mostrarTodosBtn);

        buscadorLayout.add(nombreFiltro, correoFiltro,layoutBtns);
        return buscadorLayout;
    }

    /**
     * Configuracion del formulario para el alta de un nuevo alumno.
     *
     */
    private Component configurarFormulario(){

        usuarioForm = new UsuarioForm();
        usuarioForm.addListener(UsuarioForm.GuardarEvent.class, this::guardarCurso);
        usuarioForm.addListener(UsuarioForm.CerrarEvent.class, e -> cerrarFormulario());
        formularioDg.add(usuarioForm);

        nuevoUsuarioBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevoUsuarioBtn.addClickListener(event -> {
            formularioDg.open();
        });

        HorizontalLayout layoutBtn = new HorizontalLayout();
        layoutBtn.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtn.getElement().getStyle().set("margin-left", "auto");
        layoutBtn.add(nuevoUsuarioBtn);

        return layoutBtn;
    }

    /**
     * Función para buscar cursos.
     *
     */
    private void filtrar() {
        Usuario usuario = new Usuario();

        String nombre = nombreFiltro.getValue();
        String profesor = correoFiltro.getValue();

        if(!Objects.equals(nombre, "")){
            usuario =  usuariosService.buscarUsuarioPorNombre(nombre);
        }
        if(!Objects.equals(profesor, "")){
            usuario =  usuariosService.buscarUsuarioPorCorreo(profesor);
        }

        if (usuario!=null){
            grid.setItems(usuario);
        }
    }

    /**
     * Función para actualizar el grid con todos los cursos que se han dado de alta.
     *
     */
    private void obtenerTodosUsuarios() {
        Usuario[] listaUsuarios = usuariosService.buscarTodos();
        grid.setItems(listaUsuarios);
    }

    /**
     * Función para crear o actualizar los datos de un curso.
     *
     */
    private void guardarCurso(UsuarioForm.GuardarEvent evt) {
        boolean resultado;
        Usuario usuario = evt.getUsuario();

        // Se crea un nuevo alumno o se actualiza uno existente
        if(usuario.getIdUsuario() != null && usuario.getIdUsuario() > 0) {
            resultado = usuariosService.actualizarUsuario(usuario);
        } else {
            resultado = usuariosService.guardarUsuario(usuario);
        }

        if(resultado){
            notificacion.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notificacion.setText("Se ha guardado correctamente el usuario");
            notificacion.open();
        } else {
            notificacion.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notificacion.setText("Error al guardar el usuario");
            notificacion.open();
        }
        obtenerTodosUsuarios();
        cerrarFormulario();
    }

    /**
     * Función para editar los datos de un curso.
     *
     */
    private void editarUsuario(Usuario usuario) {
        usuarioForm.setUsuario(usuario);
        formularioDg.open();
    }

    /**
     * Función para eliminar un alumno.
     *
     */
    private void eliminarUsuario(Usuario usuario) {

        // Se configura el Dialog para confirmar la eliminación
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea eliminar el usuario: "+usuario.getNombre()+"?");

        HorizontalLayout btnsLayout = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {

            if(usuariosService.eliminarUsuario(usuario.getIdUsuario())){
                notificacion.setText("Se ha eliminado correctamente el usuario");
                notificacion.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notificacion.open();
            } else {
                notificacion.setText("Error al eliminar el usuario");
                notificacion.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notificacion.open();
            }

            confirmacionDg.close();
            obtenerTodosUsuarios();

        });
        cancelarBtn.addClickListener(click -> {
            confirmacionDg.close();
        });
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.add(cancelarBtn,eliminarBtn);
        confirmacionDg.add(msjConfirmacion,btnsLayout);
        confirmacionDg.open();
    }

    /**
     * Función para cerrar el formulario de curso.
     *
     */
    private void cerrarFormulario() {
        Usuario usuario = new Usuario();
        usuarioForm.setUsuario(usuario);
        formularioDg.close();
    }


}
