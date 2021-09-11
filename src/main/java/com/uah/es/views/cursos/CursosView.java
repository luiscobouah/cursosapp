package com.uah.es.views.cursos;

//https://vaadin.com/components/vaadin-ordered-layout/java-examples
//https://vaadin.com/directory/component/tooltips4vaadin/samples

import com.helger.commons.csv.CSVWriter;
import com.uah.es.model.*;
import com.uah.es.service.IAlumnosService;
import com.uah.es.service.ICursosService;
import com.uah.es.service.IMatriculasService;
import com.uah.es.service.IUsuariosService;
import com.uah.es.views.MainLayout;
import com.uah.es.views.alumnos.AlumnosView;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
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
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.StreamResource;
import dev.mett.vaadin.tooltip.Tooltips;
import org.apache.commons.io.IOUtils;
import org.vaadin.klaudeta.PaginatedGrid;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;

import static com.uah.es.security.SecurityUtils.getEmailUser;
import static com.uah.es.security.SecurityUtils.userHasRole;

@PageTitle("Cursos")
@Route(value = "cursos", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class CursosView extends Div {

    //Servicios para comunicación con el backend
    ICursosService cursosService;
    IMatriculasService matriculasService;
    IUsuariosService usuariosService;
    IAlumnosService alumnosService;

    //Componentes visuales
    CursoForm cursoForm;
    AlumnosView alumnosView;
    Anchor linkDescargaCsv;
    PaginatedGrid<Curso> grid = new PaginatedGrid<>();
    TextField nombreFiltro = new TextField();
    TextField profesorFiltro = new TextField();
    Select<String> categoriaFiltro = new Select<>();
    Button buscarBtn = new Button("Buscar");
    Button mostrarTodosBtn = new Button("Mostrar todos");
    Button mostrarMisCurosBtn = new Button("Mostrar mis cursos");
    Button nuevoCursoBtn = new Button("Nuevo curso",new Icon(VaadinIcon.PLUS));
    Dialog formularioDg = new Dialog();
    Notification notificacionOK = new Notification("", 3000);
    Notification notificacionKO = new Notification("", 3000);

    List<Curso> listaMisCursos = new ArrayList<Curso>();
    List<Curso> listaCursos = new ArrayList<Curso>();
    Alumno alumno = new Alumno();
    Usuario usuario = new Usuario();
    boolean isListadoMisCursos = false;

    public CursosView(ICursosService cursosService, IMatriculasService matriculasService, IUsuariosService usuariosService, IAlumnosService alumnosService) {

        this.cursosService = cursosService;
        this.matriculasService =  matriculasService;
        this.usuariosService =  usuariosService;
        this.alumnosService = alumnosService;

        notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);

        if(userHasRole(Collections.singletonList(Rol.ROL_ALUMNO))){
            alumno = alumnosService.buscarAlumnoPorCorreo(getEmailUser());
            listaMisCursos = alumno.getCursos();
        }

        if(userHasRole(Collections.singletonList(Rol.ROL_PROFESOR))){
            usuario = usuariosService.buscarUsuarioPorCorreo(getEmailUser());
            listaMisCursos = Arrays.asList(cursosService.buscarCursosPorProfesor(usuario.getNombre()));
        }

        addClassName("cursos-view");
        HorizontalLayout superiorLayout = new HorizontalLayout(configurarBuscador(),configurarFormulario());
        superiorLayout.setPadding(true);
        add(superiorLayout,configurarGrid(),configurarExportarExcel());
    }

    /**
     * Configuración del grid y sus columnas.
     *
     */
    private Component configurarGrid() {

        VerticalLayout layoutGrid = new VerticalLayout();
        // Se añaden las columnas al grid
        grid.addColumn(Curso::getIdCurso).setHeader("ID").setKey("id").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getDuracion).setHeader("Duración (H)").setKey("duracion").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getProfesor).setHeader("Profesor").setKey("profesor").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getPrecio).setHeader("Precio (€)").setKey("precio").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getCategoria).setHeader("Categoría").setKey("categoria").setSortable(true).setAutoWidth(true);
        grid.addComponentColumn(item -> {
            Icon alumnosBtn = new Icon(VaadinIcon.EYE);
            alumnosBtn.setColor("#1B4F72");
            alumnosBtn.getStyle().set("cursor", "pointer");
            alumnosBtn.setSize("18px");
            alumnosBtn.addClickListener(e -> verListadoAlumnos(item));
            return alumnosBtn;
        })
        .setKey("alumnos")
        .setHeader("Alumnos")
        .setTextAlign(ColumnTextAlign.CENTER)
        .setVisible(userHasRole(Collections.singletonList(Rol.ROL_ADMIN))||userHasRole(Collections.singletonList(Rol.ROL_PROFESOR)));
        grid.addComponentColumn(item -> {
            Icon editarIcon = new Icon(VaadinIcon.EDIT);
            editarIcon.setColor("green");
            editarIcon.getStyle().set("cursor", "pointer");
            editarIcon.setSize("18px");
            editarIcon.addClickListener(e -> editarCurso(item));
            return editarIcon;
        })
        .setKey("editar")
        .setHeader("Editar")
        .setTextAlign(ColumnTextAlign.CENTER)
        .setAutoWidth(true)
        .setVisible(userHasRole(Collections.singletonList(Rol.ROL_ADMIN)));
        grid.addComponentColumn(item -> {
            Icon editarIcon = new Icon(VaadinIcon.TRASH);
            editarIcon.setColor("red");
            editarIcon.getStyle().set("cursor", "pointer");
            editarIcon.setSize("18px");
            editarIcon.addClickListener(e -> eliminarCurso(item));
            return editarIcon;
        })
        .setKey("eliminar")
        .setHeader("Eliminar")
        .setTextAlign(ColumnTextAlign.CENTER)
        .setAutoWidth(true)
        .setVisible(userHasRole(Collections.singletonList(Rol.ROL_ADMIN)));
        grid.addComponentColumn(item -> {
            Icon editarIcon = new Icon(VaadinIcon.OPEN_BOOK);
            if (listaMisCursos.contains(item)){
                Tooltips.getCurrent().setTooltip(editarIcon, "Eliminar matrícula");
                editarIcon.setColor("red");
            } else {
                Tooltips.getCurrent().setTooltip(editarIcon, "Matricular");
                editarIcon.setColor("green");
            }
            editarIcon.getStyle().set("cursor", "pointer");
            editarIcon.setSize("18px");
            editarIcon.addClickListener(e -> {
                if(listaMisCursos.contains(item)){
                    eliminarMatricula(item);
                } else {
                    matricularCurso(item);
                }
            }
            );
            return editarIcon;
        })
        .setKey("matricular")
        .setHeader("Matrícula")
        .setTextAlign(ColumnTextAlign.CENTER)
        .setAutoWidth(true)
        .setVisible(userHasRole(Collections.singletonList(Rol.ROL_ALUMNO)));

        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setColumnReorderingAllowed(true);
        // Número max de elementos a visualizar en cada página del grid
        grid.setPageSize(10);
        obtenerTodosCursos();
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
        profesorFiltro.setLabel("Profesor");
        profesorFiltro.setWidth("30%");
        profesorFiltro.setClearButtonVisible(true);
        profesorFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        categoriaFiltro.setLabel("Categoría");
        categoriaFiltro.setItems("","Desarrollo", "Educación","Finanzas");

        buscarBtn.setEnabled(false);

        // Se habilita el btn buscar solo cuando el nombre tenga valor
        nombreFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(nombreFiltro.getValue(), ""));
            profesorFiltro.setEnabled(Objects.equals(nombreFiltro.getValue(), ""));
            categoriaFiltro.setEnabled(Objects.equals(nombreFiltro.getValue(), ""));
        });
        profesorFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(profesorFiltro.getValue(), ""));
            nombreFiltro.setEnabled(Objects.equals(profesorFiltro.getValue(), ""));
            categoriaFiltro.setEnabled(Objects.equals(profesorFiltro.getValue(), ""));
        });
        categoriaFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!Objects.equals(categoriaFiltro.getValue(), ""));
            nombreFiltro.setEnabled(Objects.equals(categoriaFiltro.getValue(), ""));
            profesorFiltro.setEnabled(Objects.equals(categoriaFiltro.getValue(), ""));
        });

        buscarBtn.getStyle().set("cursor", "pointer");
        buscarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        buscarBtn.addClickListener(e -> filtrar());

        mostrarTodosBtn.getStyle().set("cursor", "pointer");
        mostrarTodosBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mostrarTodosBtn.addClickListener(e -> {
            //Limpiar los buscadores
            nombreFiltro.clear();
            profesorFiltro.clear();
            categoriaFiltro.setValue("");
            // Habilitar los buscadores
            nombreFiltro.setEnabled(true);
            profesorFiltro.setEnabled(true);
            categoriaFiltro.setEnabled(true);
            obtenerTodosCursos();
        });

        mostrarMisCurosBtn.setVisible(userHasRole(Collections.singletonList(Rol.ROL_ALUMNO))||userHasRole(Collections.singletonList(Rol.ROL_PROFESOR)));
        mostrarMisCurosBtn.getStyle().set("cursor", "pointer");
        mostrarMisCurosBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        mostrarMisCurosBtn.addClickListener(e -> {
            // Limpiar los buscadores
            nombreFiltro.clear();
            profesorFiltro.clear();
            categoriaFiltro.setValue("");
            // Deshabilitar los buscadores
            nombreFiltro.setEnabled(false);
            profesorFiltro.setEnabled(false);
            categoriaFiltro.setEnabled(false);
            obtenerMisCursos();
        });

        HorizontalLayout layoutBtns = new HorizontalLayout();
        layoutBtns.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtns.add(buscarBtn,mostrarTodosBtn,mostrarMisCurosBtn);

        buscadorLayout.add(nombreFiltro,profesorFiltro,categoriaFiltro,layoutBtns);
        return buscadorLayout;
    }

    /**
     * Configuracion del link para la descarga del Csv
     *
     */
    private Component configurarExportarExcel() {

        HorizontalLayout layoutLink = new HorizontalLayout();
        linkDescargaCsv = new Anchor(new StreamResource("Cursos.csv", this::generarCsv), "Descargar");
        layoutLink.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        layoutLink.add(linkDescargaCsv);
        return layoutLink;
    }

    /**
     * Configuracion del formulario para el alta de un nuevo alumno.
     *
     */
    private Component configurarFormulario(){

        cursoForm = new CursoForm(usuariosService);
        cursoForm.addListener(CursoForm.GuardarEvent.class, this::guardarCurso);
        cursoForm.addListener(CursoForm.CerrarEvent.class, e -> cerrarFormulario());
        formularioDg.add(cursoForm);

        nuevoCursoBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        nuevoCursoBtn.addClickListener(event -> {
            formularioDg.open();
        });

        HorizontalLayout layoutBtn = new HorizontalLayout();
        layoutBtn.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtn.getElement().getStyle().set("margin-left", "auto");
        layoutBtn.add(nuevoCursoBtn);
        nuevoCursoBtn.setVisible(userHasRole(Collections.singletonList(Rol.ROL_ADMIN)));

        return layoutBtn;
    }

    /**
     * Función para buscar cursos.
     *
     */
    private void filtrar() {

        String nombre = nombreFiltro.getValue();
        String profesor = profesorFiltro.getValue();
        String categoria = categoriaFiltro.getValue();

        if(!Objects.equals(nombre, "")){
            listaCursos = Arrays.asList(cursosService.buscarCursosPorNombre(nombre));
        }
        if(!Objects.equals(profesor, "")){
            listaCursos = Arrays.asList(cursosService.buscarCursosPorProfesor(profesor));
        }
        if(!Objects.equals(categoria, "") && categoria!=null){
            listaCursos = Arrays.asList(cursosService.buscarCursosPorCategoria(categoria));
        }
        if (listaCursos!=null){
            grid.setItems(listaCursos);
        }
    }

    /**
     * Función para actualizar el grid con todos los cursos que se han dado de alta.
     *
     */
    private void obtenerTodosCursos() {
        isListadoMisCursos = false;
        listaCursos = Arrays.asList(cursosService.buscarTodos());
        grid.setItems(listaCursos);
    }

    /**
     * Función para actualizar el grid con todos los cursos del alumno.
     *
     */
    private void obtenerMisCursos() {
        isListadoMisCursos = true;
        if(userHasRole(Collections.singletonList(Rol.ROL_ALUMNO))){
            alumno = alumnosService.buscarAlumnoPorCorreo(getEmailUser());
            listaMisCursos = alumno.getCursos();
        }

        if(userHasRole(Collections.singletonList(Rol.ROL_PROFESOR))){
            usuario = usuariosService.buscarUsuarioPorCorreo(getEmailUser());
            listaMisCursos = Arrays.asList(cursosService.buscarCursosPorProfesor(usuario.getNombre()));
        }
        grid.setItems(listaMisCursos);
    }

    /**
     * Función para crear o actualizar los datos de un curso.
     *
     */
    private void guardarCurso(CursoForm.GuardarEvent evt) {
        boolean resultado;
        Curso curso = evt.getCurso();

        // Se crea un nuevo alumno o se actualiza uno existente
        if(curso.getIdCurso() != null && curso.getIdCurso() > 0) {
            resultado = cursosService.actualizarCurso(curso);
        } else {
            resultado = cursosService.guardarCurso(curso);
        }

        if(resultado){
            notificacionOK.setText("Se ha guardado correctamente el curso");
            notificacionOK.open();
        } else {
            notificacionKO.setText("Error al guardar el curso");
            notificacionKO.open();
        }
        obtenerTodosCursos();
        cerrarFormulario();
    }

    /**
     * Función para editar los datos de un curso.
     *
     */
    private void editarCurso(Curso curso) {
        cursoForm.setCurso(curso);
        formularioDg.open();
    }

    /**
     * Función para eliminar un alumno.
     *
     */
    private void eliminarCurso(Curso curso) {

        // Se configura el Dialog para confirmar la eliminación
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea eliminar el curso: "+curso.getNombre()+"?");

        HorizontalLayout btnsLayout = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {

            if(cursosService.eliminarCurso(curso.getIdCurso())){
                notificacionOK.setText("Se ha eliminado correctamente el curso");
                notificacionOK.open();
            } else {
                notificacionKO.setText("Error al eliminar el curso");
                notificacionKO.open();
            }
            confirmacionDg.close();
            obtenerTodosCursos();

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
     * Función para matricular el alumno en el curso.
     *
     */
    private void matricularCurso(Curso curso) {

        // Se configura el Dialog para confirmar la eliminación
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea matricularse en el curso: "+curso.getNombre()+"?");

        HorizontalLayout btnsLayout = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button matricularBtn = new Button("Matricular");
        matricularBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        matricularBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        matricularBtn.addClickListener(click -> {
            Usuario usuario =  usuariosService.buscarUsuarioPorCorreo(getEmailUser());
            usuario.setRoles(null);
            Matricula matricula = new Matricula(curso.getIdCurso(),usuario);

            if(matriculasService.guardarMatricula(matricula)){
                notificacionOK.setText("Se ha matriculado correctamente en el curso");
                notificacionOK.open();
            } else {
                notificacionKO.setText("Error al matricularse en el curso");
                notificacionKO.open();
            }
            listaMisCursos = alumnosService.buscarAlumnoPorCorreo(getEmailUser()).getCursos();
            confirmacionDg.close();
            obtenerTodosCursos();
        });
        cancelarBtn.addClickListener(click -> {
            confirmacionDg.close();
        });

        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.add(cancelarBtn,matricularBtn);
        confirmacionDg.add(msjConfirmacion,btnsLayout);
        confirmacionDg.open();
    }

    /**
     * Función para eliminar la matricula del alumno en el curso.
     *
     */
    private void eliminarMatricula(Curso curso) {
        // Se configura el Dialog para confirmar la matricula
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea eliminar la matrícula del curso: "+curso.getNombre());

        HorizontalLayout btnsLayout = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Eliminar matrícula");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {
            if(matriculasService.eliminarMatricula(curso,alumno)){
                notificacionOK.setText("Se ha eliminado la matrícula correctamente");
                notificacionOK.open();
                //inicializarViews();
            } else {
                notificacionKO.setText("Error al eliminar la matrícula del curso");
                notificacionKO.open();
            }
            listaMisCursos = alumnosService.buscarAlumnoPorCorreo(getEmailUser()).getCursos();
            confirmacionDg.close();
            obtenerTodosCursos();

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
     * Función para visualizar el listado de cursos que tiene un alumno.
     *
     */
    private void verListadoAlumnos(Curso curso) {

        // Se configura el Dialog para visualizar el listado de cursos de un alumno
        Dialog listadoAlumnosDg = new Dialog();
        H2 titulo = new H2("Alumnos en " + curso.getNombre());
        Grid<Alumno> gridAlumnos= new Grid<>();
        gridAlumnos.addColumn(Alumno::getIdAlumno).setHeader("ID").setKey("id").setAutoWidth(true);
        gridAlumnos.addColumn(Alumno::getNombre).setHeader("Nombre").setKey("nombre").setAutoWidth(true);
        gridAlumnos.addColumn(Alumno::getCorreo).setHeader("Correo").setKey("correo").setAutoWidth(true);
        gridAlumnos.setItems(curso.getAlumnos());
        HorizontalLayout btns = new HorizontalLayout();
        Button cerrarBtn = new Button("Cerrar");
        cerrarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        cerrarBtn.addClickShortcut(Key.ESCAPE);

        cerrarBtn.addClickListener(click -> {
            listadoAlumnosDg.close();
        });

        btns.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btns.add(cerrarBtn);
        listadoAlumnosDg.add(titulo,gridAlumnos,btns);
        listadoAlumnosDg.setWidth("600px");
        listadoAlumnosDg.open();
    }

    /**
     * Función para generar el Csv con los datos del curso
     *
     */
    private InputStream generarCsv() {
        try {

            List<Curso> cursosCsv = new ArrayList<Curso>();
            if(isListadoMisCursos){
                cursosCsv = listaMisCursos;
            } else {
                cursosCsv = listaCursos;
            }
            StringWriter stringWriter = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(stringWriter);
            csvWriter.writeNext("id", "Nombre", "Duración (H)", "Profesor", "Precio (€)", "Categoría");
            cursosCsv.forEach(c -> csvWriter.writeNext("" + c.getIdCurso(), c.getNombre(),c.getDuracion().toString(),c.getProfesor(),c.getPrecio().toString(),c.getCategoria())
            );
            return IOUtils.toInputStream(stringWriter.toString(), "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Función para cerrar el formulario de curso.
     *
     */
    private void cerrarFormulario() {
        Curso curso = new Curso();
        cursoForm.setCurso(curso);
        formularioDg.close();
    }

}
