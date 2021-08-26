package com.uah.es.views.alumnos;

import com.uah.es.model.Alumno;
import com.uah.es.service.IAlumnosService;
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
import org.vaadin.klaudeta.PaginatedGrid;

/*https://vaadin.com/directory/component/grid-pagination/samples*/
//https://vaadin.com/components/vaadin-button/java-install

@PageTitle("Alumnos")
@Route(value = "alumnos", layout = MainLayout.class)
public class AlumnosView extends Div {

    //Servicio para comunicación con el backend
    IAlumnosService alumnosService;

    //Componentes visuales
    AlumnoForm alumnoForm;
    PaginatedGrid<Alumno> grid = new PaginatedGrid<>();
    TextField correoFiltro = new TextField();
    Button buscarBtn = new Button("Buscar");
    Button mostrarTodosBtn = new Button("Mostrar todos");
    Button nuevoAlumnoBtn = new Button("Nuevo Alumno");
    Dialog formularioDg = new Dialog();
    Notification notificacion = new Notification("", 3000);

    public AlumnosView(IAlumnosService alumnosService) {

        this.alumnosService = alumnosService;

        addClassName("usuarios-view");
        configurarFormulario();
        //HorizontalLayout superiorLayout = new HorizontalLayout(configurarBuscador(),configurarFormulario());
        add(configurarBuscador(),configurarGrid());
    }

    /**
     * Configuracion del grid y sus columnas.
     *
     */
    private Component configurarGrid() {

        VerticalLayout layoutGrid = new VerticalLayout();
        // Se añaden las columnas al grid
        grid.addColumn(Alumno::getIdAlumno).setHeader("ID").setKey("id").setAutoWidth(true);
        grid.addColumn(Alumno::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(Alumno::getCorreo).setHeader("Correo").setKey("correo").setSortable(true).setAutoWidth(true);
        //grid.addColumn(Alumno::getStringCursos).setHeader("Cursos").setKey("curso").setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Button cursosBtn = new Button();
            cursosBtn.setText(item.getStringCursos());
            cursosBtn.getStyle().set("cursor", "pointer");
            cursosBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
            cursosBtn.addClickListener(e -> verListadoCursos(item));
            return cursosBtn;
        })
        .setHeader("Cursos")
        .setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Icon editarIcon = new Icon(VaadinIcon.EDIT);
            editarIcon.setColor("green");
            editarIcon.getStyle().set("cursor", "pointer");
            editarIcon.setSize("18px");
            editarIcon.addClickListener(e -> editarAlumno(item));
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
            editarIcon.addClickListener(e -> elimarAlumno(item));
            return editarIcon;
        })
        .setKey("eliminar")
        .setHeader("Eliminar")
        .setTextAlign(ColumnTextAlign.CENTER);

        // Número max de elementos a visualizar en cada página del grid
        grid.setPageSize(10);
        obtenerTodosAlumnos();
        layoutGrid.add(grid);

        return layoutGrid;
    }

    /**
     * Configuracion del buscador.
     *
     */
    private Component configurarBuscador() {

        HorizontalLayout buscadorLayout = new HorizontalLayout();

        // Configuracion del filtro para buscar por correo
        correoFiltro.setLabel("Correo");
        correoFiltro.setWidth("30%");
        correoFiltro.setClearButtonVisible(true);
        correoFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        // Se habilita el btn buscar solo cuando el correo tenga valor
        correoFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!correoFiltro.getValue().isEmpty());
        });

        buscarBtn.setEnabled(false);
        buscarBtn.getStyle().set("cursor", "pointer");
        buscarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        buscarBtn.addClickListener(e -> filtrar());

        mostrarTodosBtn.getStyle().set("cursor", "pointer");
        mostrarTodosBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mostrarTodosBtn.addClickListener(e -> {
            correoFiltro.clear();
            obtenerTodosAlumnos();
        });

        HorizontalLayout layoutBtns = new HorizontalLayout();
        layoutBtns.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtns.add(buscarBtn,mostrarTodosBtn);

        buscadorLayout.setPadding(true);
        buscadorLayout.add(correoFiltro,layoutBtns);
        return buscadorLayout;

    }

    /**
     * Configuracion del formulario para el alta de un nuevo alumno.
     *
     */
    private void configurarFormulario(){

        alumnoForm = new AlumnoForm();
        alumnoForm.addListener(AlumnoForm.GuardarEvent.class, this::guardarAlumno);
        alumnoForm.addListener(AlumnoForm.CerrarEvent.class, e -> cerrarFormulario());
        formularioDg.add(alumnoForm);

        /*nuevoAlumnoBtn.addClickListener(event -> {
            formularioDg.open();
        });*/
    }

    /**
     * Función para buscar un alumno por el correo.
     *
     */
    private void filtrar() {
        Alumno alumno = alumnosService.buscarAlumnoPorCorreo(correoFiltro.getValue());
        if (alumno!=null){
            grid.setItems(alumno);
        }
    }

    /**
     * Función para actualizar el grid con todos los alumnos que se han dado de alta.
     *
     */
    private void obtenerTodosAlumnos() {
        Alumno[] listaAlumnos = alumnosService.buscarTodos();
        grid.setItems(listaAlumnos);
    }

    /**
     * Función para crear o actulizar los datos de un alumno.
     *
     */
    private void guardarAlumno(AlumnoForm.GuardarEvent evt) {
        boolean result;
        Alumno alumno = evt.getAlumno();

        // Se crea un nuevo alumno o se actualiza uno existente
        if(alumno.getIdAlumno() != null && alumno.getIdAlumno() > 0) {
            result = alumnosService.actualizarAlumno(alumno);
        } else {
            result = alumnosService.guardarAlumno(alumno);
        }

        if(result){
            notificacion.setText("Se ha guardado correctamente el alumno");
            notificacion.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notificacion.open();
        } else {
            notificacion.setText("Error al guardar el curso");
            notificacion.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notificacion.open();
        }
        obtenerTodosAlumnos();
        cerrarFormulario();
    }

    /**
     * Función para editar los datos de un alumno.
     *
     */
    private void editarAlumno(Alumno alumno) {
        alumnoForm.setAlumno(alumno);
        formularioDg.open();
    }

    /**
     * Función para eliminar un alumno.
     *
     */
    private void elimarAlumno(Alumno alumno) {

        // Se configura el Dialog para confirmar la eliminación
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea eliminar el alumno: "+alumno.getNombre()+"?");

        HorizontalLayout btnsLayout = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {
            if(alumnosService.eliminarAlumno(alumno.getIdAlumno())){
                notificacion.setText("Se ha eliminado correctamente el alumno");
                notificacion.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
                notificacion.open();
            } else {
                notificacion.setText("Error al eliminar el alumno");
                notificacion.addThemeVariants(NotificationVariant.LUMO_ERROR);
                notificacion.open();
            }

            confirmacionDg.close();
            obtenerTodosAlumnos();

        });
        cancelarBtn.addClickListener(click -> {
            confirmacionDg.close();
        });

        btnsLayout.add(cancelarBtn,eliminarBtn);
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        confirmacionDg.add(msjConfirmacion,btnsLayout);
        confirmacionDg.open();
    }

    /**
     * Función para visualizar el listado de cursos que tiene un alumno.
     *
     */
    private void verListadoCursos(Alumno alumno) {

        // Se configura el Dialog para visualizar el listado de cursos de un alumno
        Dialog listadoCursosDg = new Dialog();
        Label listadoCursos = new Label();
        listadoCursos.setText( alumno.getStringCursos());

        HorizontalLayout btns = new HorizontalLayout();
        Button cerrarBtn = new Button("Cerrar");
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cerrarBtn.addClickShortcut(Key.ESCAPE);

        cerrarBtn.addClickListener(click -> {
            listadoCursosDg.close();
        });

        btns.add(cerrarBtn);
        listadoCursosDg.add(listadoCursos,btns);
        listadoCursosDg.open();
    }

    /**
     * Función para cerrar el formulario de alumno.
     *
     */
    private void cerrarFormulario() {
        Alumno alumno = new Alumno();
        alumnoForm.setAlumno(alumno);
        formularioDg.close();
    }

}
