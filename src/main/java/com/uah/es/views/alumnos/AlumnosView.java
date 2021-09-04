package com.uah.es.views.alumnos;

import com.helger.commons.csv.CSVWriter;
import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.service.IAlumnosService;
import com.uah.es.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*https://vaadin.com/directory/component/grid-pagination/samples*/
//https://vaadin.com/components/vaadin-button/java-install

@PageTitle("Alumnos")
@Route(value = "alumnos", layout = MainLayout.class)
@Secured("Admin")
public class AlumnosView extends Div {

    //Servicio para comunicación con el backend
    IAlumnosService alumnosService;

    //Componentes visuales
    AlumnoForm alumnoForm;
    Anchor linkDescargaCsv;
    PaginatedGrid<Alumno> grid = new PaginatedGrid<>();
    TextField correoFiltro = new TextField();
    Button buscarBtn = new Button("Buscar");
    Button mostrarTodosBtn = new Button("Mostrar todos");
    Button nuevoAlumnoBtn = new Button("Nuevo Alumno");
    Dialog formularioDg = new Dialog();
    Notification notificacionOK = new Notification("", 3000);
    Notification notificacionKO = new Notification("", 3000);

    List<Alumno> listaAlumnos = new ArrayList<Alumno>();
    boolean mostrarAcciones = true;
    public  Alumno alumnoSeleccionado = new Alumno();

    public AlumnosView(IAlumnosService alumnosService) {

        this.alumnosService = alumnosService;

        notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);

        addClassName("usuarios-view");
        configurarFormulario();
        add(configurarBuscador(),configurarGrid(),configurarExportarExcel());
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
        .setKey("cursos")
        .setHeader("Cursos")
        .setVisible(mostrarAcciones);
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
        .setTextAlign(ColumnTextAlign.CENTER)
        .setVisible(mostrarAcciones);;
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
        .setTextAlign(ColumnTextAlign.CENTER)
        .setVisible(mostrarAcciones);

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
     * Configuracion del link para la descarga del Csv
     *
     */
    private Component configurarExportarExcel() {

        HorizontalLayout layoutLink = new HorizontalLayout();
        linkDescargaCsv = new Anchor(new StreamResource("Alumnos.csv", this::generarCsv), "Descargar");
        layoutLink.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layoutLink.add(linkDescargaCsv);
        return layoutLink;
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
    }

    /**
     * Función para buscar un alumno por el correo.
     *
     */
    private void filtrar() {
        listaAlumnos = Collections.singletonList(alumnosService.buscarAlumnoPorCorreo(correoFiltro.getValue()));
        grid.setItems(listaAlumnos);
    }

    /**
     * Función para actualizar el grid con todos los alumnos que se han dado de alta.
     *
     */
    private void obtenerTodosAlumnos() {
        listaAlumnos = Arrays.asList(alumnosService.buscarTodos());
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
            notificacionOK.setText("Se ha guardado correctamente el alumno");
            notificacionOK.open();
        } else {
            notificacionKO.setText("Error al guardar el curso");
            notificacionKO.open();
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
                notificacionOK.setText("Se ha eliminado correctamente el alumno");
                notificacionOK.open();
            } else {
                notificacionKO.setText("Error al eliminar el alumno");
                notificacionKO.open();
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
     * Función para generar el Csv con los datos del curso
     *
     */
    private InputStream generarCsv() {
        try {

            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext("id", "Nombre", "Correo","Cursos");
            listaAlumnos.forEach(a -> csvWriter.writeNext("" + a.getIdAlumno(), a.getNombre(),a.getCorreo(),a.getStringCursos())
            );
            return IOUtils.toInputStream(stringWriter.toString(), "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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

    /**
     * Función para actualizar el grid con todos los alumnos que se han dado de alta.
     *
     */
    private void obtenerAlumnoNoMatriculados(Curso curso) {
        ArrayList<Alumno> alumnos = new ArrayList(listaAlumnos);
        alumnos.removeAll(curso.getAlumnos());
        grid.setItems(alumnos);
    }

    /**
     * Función para ocular las acciones cuando se llama desde MatriculasView.
     *
     */
    public void ocultarAcciones(Curso curso) {
        obtenerAlumnoNoMatriculados(curso);
        grid.getColumnByKey("cursos").setVisible(false);
        grid.getColumnByKey("editar").setVisible(false);
        grid.getColumnByKey("eliminar").setVisible(false);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.getSelectedItems();
        linkDescargaCsv.setVisible(false);
        grid.setPageSize(5);

        grid.addSelectionListener(e ->{
            if(e.getFirstSelectedItem().isPresent()){
                alumnoSeleccionado = e.getFirstSelectedItem().get();
            } else {
                alumnoSeleccionado = new Alumno();
            }
        });


    }

}
