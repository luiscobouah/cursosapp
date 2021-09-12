package com.uah.es.views.usuarios;


import com.helger.commons.csv.CSVWriter;
import com.uah.es.model.Rol;
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
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.apache.commons.io.IOUtils;
import org.springframework.security.access.annotation.Secured;
import org.vaadin.klaudeta.PaginatedGrid;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


@PageTitle("Usuarios")
@Route(value = "usuarios", layout = MainLayout.class)
@Secured("Admin")
public class UsuariosView extends Div {

    //Servicios
    IUsuariosService usuariosService;
    IRolesService rolesService;

    //Componentes visuales
    UsuarioForm usuarioForm;
    PaginatedGrid<Usuario> grid = new PaginatedGrid<>();
    TextField nombreFiltro = new TextField();
    TextField correoFiltro = new TextField();
    Select<String> rolFiltro = new Select<>();
    Button buscarBtn = new Button("Buscar");
    Button mostrarTodosBtn = new Button("Mostrar todos");
    Button nuevoUsuarioBtn = new Button("Nuevo usuario",new Icon(VaadinIcon.PLUS));
    Dialog formularioDg = new Dialog();
    Notification notificacionOK = new Notification("", 3000);
    Notification notificacionKO = new Notification("", 3000);

    List<Usuario> listaUsuarios = new ArrayList<Usuario>();
    List<Rol> listaRoles = new ArrayList<>();

    public UsuariosView(IUsuariosService usuariosService, IRolesService rolesService) {

        //Se inicializan los servicios
        this.usuariosService = usuariosService;
        this.rolesService =  rolesService;

        //Se configura el color de de la notificaciones
        notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);

        //addClassName("cursos-view");
        //Se configuran los componentes visuales y se añaden a la vista
        HorizontalLayout superiorLayout = new HorizontalLayout(configurarBuscador(),configurarFormulario());
        superiorLayout.setPadding(true);
        add(superiorLayout,configurarGrid(),configurarExportarExcel());
    }

    /**
     * Func para configurar el grid y sus columnas.
     *
     */
    private Component configurarGrid() {

        VerticalLayout layoutGrid = new VerticalLayout();
        // Se añaden las columnas al grid
        grid.addColumn(Usuario::getIdUsuario).setHeader("ID").setKey("id").setSortable(true).setAutoWidth(true);
        grid.addColumn(Usuario::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(Usuario::getCorreo).setHeader("Correo").setKey("correo").setSortable(false).setAutoWidth(true);
        grid.addColumn(Usuario::getStringRoles).setHeader("Rol").setKey("rol").setSortable(false).setAutoWidth(true);
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

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setColumnReorderingAllowed(true);
        // Número max de elementos a visualizar en cada página del grid
        grid.setPageSize(10);
        obtenerTodosUsuarios();
        layoutGrid.add(grid);

        return layoutGrid;
    }

    /**
     *  Func para configurar el buscador.
     *
     */
    private Component configurarBuscador() {

        HorizontalLayout buscadorLayout = new HorizontalLayout();

        // Configuracion del filtro para buscar por nombre
        nombreFiltro.setLabel("Nombre");
        nombreFiltro.setWidth("30%");
        nombreFiltro.setClearButtonVisible(true);
        nombreFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        // Configuracion del filtro para buscar por correo
        correoFiltro.setLabel("Correo");
        correoFiltro.setWidth("30%");
        correoFiltro.setClearButtonVisible(true);
        correoFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        // Configuracion del filtro para buscar por rol
        rolFiltro.setLabel("Rol");
        listaRoles=Arrays.asList(rolesService.buscarTodos());
        List<String> roles = new ArrayList<>();
        //Se añade un elemento de texto vacio
        roles.add("");
        listaRoles.forEach(rol -> {
            roles.add(rol.getAuthority());
        });
        rolFiltro.setItems(roles);

        buscarBtn.setEnabled(false);

        // Se habilita el btn buscar solo cuando el nombre tenga valor y los demás filtros se inhabilitan.
        nombreFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(nombreFiltro.getValue(), ""));
            correoFiltro.setEnabled(Objects.equals(nombreFiltro.getValue(), ""));
            rolFiltro.setEnabled(Objects.equals(nombreFiltro.getValue(), ""));
        });
        // Se habilita el btn buscar solo cuando el correo tenga valor y los demás filtros se inhabilitan.
        correoFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(correoFiltro.getValue(), ""));
            nombreFiltro.setEnabled(Objects.equals(correoFiltro.getValue(), ""));
            rolFiltro.setEnabled(Objects.equals(correoFiltro.getValue(), ""));
        });
        // Se habilita el btn buscar solo cuando el rol tenga valor y los demás filtros se inhabilitan.
        rolFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(rolFiltro.getValue(), ""));
            nombreFiltro.setEnabled(Objects.equals(rolFiltro.getValue(), ""));
            correoFiltro.setEnabled(Objects.equals(rolFiltro.getValue(), ""));
        });

        //Se configuran los botones del buscador y sus listeners
        buscarBtn.getStyle().set("cursor", "pointer");
        buscarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        buscarBtn.addClickListener(e -> filtrar());

        mostrarTodosBtn.getStyle().set("cursor", "pointer");
        mostrarTodosBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mostrarTodosBtn.addClickListener(e -> {
            //Limpiar los buscadores
            nombreFiltro.clear();
            correoFiltro.clear();
            rolFiltro.setValue("");
            // Habilitar los buscadores
            nombreFiltro.setEnabled(true);
            correoFiltro.setEnabled(true);
            rolFiltro.setEnabled(true);
            obtenerTodosUsuarios();
        });

        HorizontalLayout layoutBtns = new HorizontalLayout();
        layoutBtns.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtns.add(buscarBtn,mostrarTodosBtn);
        //Se añaden los componentes visuales
        buscadorLayout.add(nombreFiltro, correoFiltro,rolFiltro,layoutBtns);
        return buscadorLayout;
    }

    /**
     * Func para configurar el link para la descarga del Csv
     *
     */
    private Component configurarExportarExcel() {

        HorizontalLayout layoutLink = new HorizontalLayout();
        Anchor linkDescargaCsv = new Anchor(new StreamResource("Usuarios.csv", this::generarCsv), "Descargar");
        layoutLink.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layoutLink.add(linkDescargaCsv);
        return layoutLink;
    }

    /**
     * Configuracion del formulario para el alta de un nuevo usuario.
     *
     */
    private Component configurarFormulario(){

        usuarioForm = new UsuarioForm(rolesService);
        usuarioForm.addListener(UsuarioForm.GuardarEvent.class, this::guardarUsuario);
        usuarioForm.addListener(UsuarioForm.CerrarEvent.class, e -> cerrarFormulario());
        formularioDg.add(usuarioForm);

        //Se configura el boton para añadir un nuevo Usuario
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
     * Func para buscar Usuarios.
     *
     */
    private void filtrar() {

        String nombre = nombreFiltro.getValue();
        String profesor = correoFiltro.getValue();
        String rol = rolFiltro.getValue();

        //Solo se filtra por un solo filtro
        if(!Objects.equals(nombre, "")){
            listaUsuarios = Collections.singletonList(usuariosService.buscarUsuarioPorNombre(nombre));
        }
        if(!Objects.equals(profesor, "")){
            listaUsuarios = Collections.singletonList(usuariosService.buscarUsuarioPorCorreo(profesor));
        }
        if(!Objects.equals(rol, "") && rol!=null){
            AtomicInteger idRol=new AtomicInteger();
            listaRoles.forEach(r ->{
                if(r.getAuthority().equals(rol)){
                    idRol.set(r.getIdRol());
                }
            });
            listaUsuarios = Arrays.asList(usuariosService.buscarUsuariosPorRol(idRol.get()));
        }
        grid.setItems(listaUsuarios);

    }

    /**
     * Func para actualizar el grid con todos los Usuarios que se han dado de alta.
     *
     */
    private void obtenerTodosUsuarios() {

        listaUsuarios = Arrays.asList(usuariosService.buscarTodos());
        grid.setItems(listaUsuarios);
    }

    /**
     * Func para crear o actualizar los datos de un Usuario.
     *
     */
    private void guardarUsuario(UsuarioForm.GuardarEvent evt) {
        boolean resultado;
        Usuario usuario = evt.getUsuario();

        // Se crea un nuevo alumno o se actualiza uno existente
        if(usuario.getIdUsuario() != null && usuario.getIdUsuario() > 0) {
            resultado = usuariosService.actualizarUsuario(usuario);
        } else {
            resultado = usuariosService.guardarUsuario(usuario);
        }

        // Se muestra la notificacion indicando el restultado
        if(resultado){
            notificacionOK.setText("Se ha guardado correctamente el usuario");
            notificacionOK.open();
        } else {
            notificacionKO.setText("Error al guardar el usuario");
            notificacionKO.open();
        }

        // Se actualiza el grid con todos los alumnos
        obtenerTodosUsuarios();
        cerrarFormulario();
    }

    /**
     * Func para editar los datos de un usuario.
     *
     */
    private void editarUsuario(Usuario usuario) {

        usuarioForm.setUsuario(usuario);
        usuarioForm.modificarVisibilidaRoles(true);
        formularioDg.open();
    }

    /**
     * Func para eliminar un usuario.
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
            // Se muestra la notificacion indicando el restultado
            if(usuariosService.eliminarUsuario(usuario.getIdUsuario())){
                notificacionOK.setText("Se ha eliminado correctamente el usuario");
                notificacionOK.open();
            } else {
                notificacionKO.setText("Error al eliminar el usuario");
                notificacionKO.open();
            }
            // Se actualiza el grid con todos los usuario
            confirmacionDg.close();
            obtenerTodosUsuarios();

        });
        cancelarBtn.addClickListener(click -> {
            confirmacionDg.close();
        });
        //Se añaden los componentes visuales al Dialog
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.add(cancelarBtn,eliminarBtn);
        confirmacionDg.add(msjConfirmacion,btnsLayout);
        confirmacionDg.open();
    }

    /**
     * Func para generar el Csv con los datos de usuarios
     *
     */
    private InputStream generarCsv() {

        try {
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);
            csvWriter.setSeparatorChar(';');
            csvWriter.writeNext("id", "Nombre", "Correo","Rol");
            listaUsuarios.forEach(u -> csvWriter.writeNext(String.valueOf(u.getIdUsuario()), u.getNombre(),u.getCorreo(),u.getStringRoles())
            );
            return IOUtils.toInputStream(stringWriter.toString(), "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Func para cerrar el formulario de usuario.
     *
     */
    private void cerrarFormulario() {

        //Se crea un Usuario vacio para resetear el formulario
        Usuario usuario = new Usuario();
        usuarioForm.setUsuario(usuario);
        usuarioForm.modificarVisibilidaRoles(false);
        formularioDg.close();
    }


}
