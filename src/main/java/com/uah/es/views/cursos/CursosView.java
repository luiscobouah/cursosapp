package com.uah.es.views.cursos;

//https://vaadin.com/components/vaadin-ordered-layout/java-examples

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.model.Matricula;
import com.uah.es.model.Usuario;
import com.uah.es.service.IAlumnosService;
import com.uah.es.service.ICursosService;
import com.uah.es.service.IMatriculasService;
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
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    List<Curso> listaMisCursos;
    Alumno alumno;

    public CursosView(
            ICursosService cursosService,
            IMatriculasService matriculasService,
            IUsuariosService usuariosService,
            IAlumnosService alumnosService) {

        this.cursosService = cursosService;
        this.matriculasService =  matriculasService;
        this.usuariosService =  usuariosService;
        this.alumnosService = alumnosService;

        notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);

        alumno = alumnosService.buscarAlumnoPorCorreo(getEmailUser());
        listaMisCursos = alumno.getCursos();

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
        grid.addColumn(Curso::getIdCurso).setHeader("ID").setKey("id").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getDuracion).setHeader("Duración").setKey("duracion").setSortable(true);
        grid.addColumn(Curso::getProfesor).setHeader("Profesor").setKey("profesor").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getPrecio).setHeader("Precio").setKey("precio").setSortable(true).setAutoWidth(true);;
        grid.addColumn(Curso::getCategoria).setHeader("Categoría").setKey("categoria").setSortable(true).setAutoWidth(true);;
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
        .setVisible(userHasRole(Collections.singletonList("Admin")));
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
        .setVisible(userHasRole(Collections.singletonList("Admin")));
        grid.addComponentColumn(item -> {
            Icon editarIcon = new Icon(VaadinIcon.OPEN_BOOK);
            if (listaMisCursos.contains(item)){
                editarIcon.setColor("gray");
                editarIcon.setVisible(false);
            } else {
                editarIcon.setColor("blue");
            }
            editarIcon.getStyle().set("cursor", "pointer");
            editarIcon.setSize("18px");
            editarIcon.addClickListener(e -> matricularCurso(item));
            return editarIcon;
        })
        .setKey("matricular")
        .setHeader("Matricular")
        .setTextAlign(ColumnTextAlign.CENTER)
        .setAutoWidth(true)
        .setVisible(userHasRole(Collections.singletonList("Alumno")));

        // Número max de elementos a visualizar en cada página del grid
        grid.setPageSize(10);
        //grid.setHeightByRows(true);
        grid.setColumnReorderingAllowed(true);
        //grid.setSelectionMode(Grid.SelectionMode.MULTI);
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
            // Mostrar la columna Matricular
            if(userHasRole(Collections.singletonList("Alumno"))){
                grid.getColumnByKey("matricular").setVisible(true);
            }
            obtenerTodosCursos();
        });

        mostrarMisCurosBtn.setVisible(userHasRole(Collections.singletonList("Alumno")));
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
            // Mostrar la columna Matricula
            grid.getColumnByKey("matricular").setVisible(false);

            obtenerMisCursos();
        });

        HorizontalLayout layoutBtns = new HorizontalLayout();
        layoutBtns.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.END);
        layoutBtns.add(buscarBtn,mostrarTodosBtn,mostrarMisCurosBtn);

        buscadorLayout.add(nombreFiltro,profesorFiltro,categoriaFiltro,layoutBtns);
        return buscadorLayout;
    }

    /**
     * Configuracion del formulario para el alta de un nuevo alumno.
     *
     */
    private Component configurarFormulario(){

        cursoForm = new CursoForm();
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
        nuevoCursoBtn.setVisible(userHasRole(Collections.singletonList("Admin")));

        return layoutBtn;
    }

    /**
     * Función para buscar cursos.
     *
     */
    private void filtrar() {
        Curso[] cursos = new Curso[0];

        String nombre = nombreFiltro.getValue();
        String profesor = profesorFiltro.getValue();
        String categoria = categoriaFiltro.getValue();

        if(!Objects.equals(nombre, "")){
            cursos =  cursosService.buscarCursosPorNombre(nombre);
        }
        if(!Objects.equals(profesor, "")){
            cursos =  cursosService.buscarCursosPorProfesor(profesor);
        }
        if(!Objects.equals(categoria, "") && categoria!=null){
            cursos =  cursosService.buscarCursosPorCategoria(categoria);
        }
        if (cursos!=null){
            grid.setItems(cursos);
        }
    }

    /**
     * Función para actualizar el grid con todos los cursos que se han dado de alta.
     *
     */
    private void obtenerTodosCursos() {
        Curso[] listaCursos = cursosService.buscarTodos();
        grid.setItems(listaCursos);
    }

    /**
     * Función para actualizar el grid con todos los cursos del alumno.
     *
     */
    private void obtenerMisCursos() {
        listaMisCursos = alumno.getCursos();
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
