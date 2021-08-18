package com.uah.es.views.cursos;

import com.uah.es.model.Curso;
import com.uah.es.service.ICursosService;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.uah.es.views.MainLayout;
import com.vaadin.flow.router.RouteAlias;
import org.vaadin.klaudeta.PaginatedGrid;

@PageTitle("Cursos")
@Route(value = "cursos", layout = MainLayout.class)
@RouteAlias(value = "", layout = MainLayout.class)
public class CursosView extends Div {

    //Servicio para comunicación con el backend
    ICursosService cursosService;

    //Componentes visuales
    CursoForm cursoForm;
    PaginatedGrid<Curso> grid = new PaginatedGrid<>();
    TextField nombreFiltro = new TextField();
    TextField profesorFiltro = new TextField();
    Select<String> categoriaFiltro = new Select<>();
    Button buscarBtn = new Button(new Icon(VaadinIcon.SEARCH));
    Button nuevoCursoBtn = new Button("Nuevo curso");
    Dialog formularioDg = new Dialog();

    Notification notificationOk = new Notification("Se han guardado correctamente los datos de curso", 3000);
    Notification notificationKo = new Notification("Error al guardar el curso", 3000);

    String filtro;

    public CursosView(ICursosService cursosService) {

        this.cursosService = cursosService;
        addClassName("cursos-view");
        configurarGrid();
        configurarBuscador();
        configurarFormulario();
        add(nombreFiltro,profesorFiltro,categoriaFiltro,buscarBtn,nuevoCursoBtn,grid);
    }

    /**
     * Configuracion del grid y sus columnas.
     *
     */
    private void configurarGrid() {

        //Se obtienen todos los alumnos
        Curso[] listaCursos = cursosService.buscarTodos();

        // Se añaden las columnas al grid
        grid.addColumn(Curso::getIdCurso).setHeader("ID").setKey("id").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getDuracion).setHeader("Duración").setKey("duracion").setSortable(true);
        grid.addColumn(Curso::getProfesor).setHeader("Profesor").setKey("profesor").setSortable(true).setAutoWidth(true);
        grid.addColumn(Curso::getPrecio).setHeader("Precio").setKey("precio").setSortable(true);
        grid.addColumn(Curso::getCategoria).setHeader("Categoría").setKey("categoria").setSortable(true);
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
        .setTextAlign(ColumnTextAlign.CENTER);
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
        .setTextAlign(ColumnTextAlign.CENTER);
        grid.addComponentColumn(item -> {
            Icon editarIcon = new Icon(VaadinIcon.OPEN_BOOK);
            editarIcon.setColor("blue");
            editarIcon.getStyle().set("cursor", "pointer");
            editarIcon.setSize("18px");
            editarIcon.addClickListener(e -> matricularCurso(item));
            return editarIcon;
        })
        .setKey("matricular")
        .setHeader("Matricular")
        .setTextAlign(ColumnTextAlign.CENTER);

        // Número max de elementos a visualizar en cada página del grid
        grid.setPageSize(12);
        grid.setItems(listaCursos);
    }

    /**
     * Configuracion del buscador.
     *
     */
    private void configurarBuscador() {

        // Configuracion del filtro para buscar por nombre
        nombreFiltro.setLabel("Nombre");
        nombreFiltro.setWidth("20%");
        nombreFiltro.setClearButtonVisible(true);
        nombreFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        // Configuracion del filtro para buscar por profesor
        profesorFiltro.setLabel("Profesor");
        profesorFiltro.setWidth("20%");
        profesorFiltro.setClearButtonVisible(true);
        profesorFiltro.setValueChangeMode(ValueChangeMode.EAGER);

        categoriaFiltro.setLabel("Categoría");
        categoriaFiltro.setItems("","Desarrollo", "Educación","Finanzas");

        buscarBtn.setEnabled(false);

        // Se habilita el btn buscar solo cuando el nombre tenga valor
        nombreFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!nombreFiltro.getValue().isEmpty());
            profesorFiltro.setEnabled(nombreFiltro.getValue().isEmpty());
            categoriaFiltro.setEnabled(nombreFiltro.getValue().isEmpty());
        });
        profesorFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!profesorFiltro.getValue().isEmpty());
            nombreFiltro.setEnabled(profesorFiltro.getValue().isEmpty());
            categoriaFiltro.setEnabled(profesorFiltro.getValue().isEmpty());
        });
        categoriaFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!categoriaFiltro.getValue().isEmpty());
            nombreFiltro.setEnabled(categoriaFiltro.getValue().isEmpty());
            profesorFiltro.setEnabled(categoriaFiltro.getValue().isEmpty());
        });
        buscarBtn.addClickListener(e -> filtrar());
    }

    /**
     * Configuracion del formulario para el alta de un nuevo alumno.
     *
     */
    private void configurarFormulario(){

        cursoForm = new CursoForm();
        cursoForm.addListener(CursoForm.GuardarEvent.class, this::guardarCurso);
        cursoForm.addListener(CursoForm.CerrarEvent.class, e -> cerrarFormulario());
        formularioDg.add(cursoForm);

        nuevoCursoBtn.addClickListener(event -> {
            formularioDg.open();
        });
    }

    /**
     * Funcición para buscar un curso por el nombre.
     *
     */
    private void filtrar() {
        Curso[] cursos = new Curso[0];

        System.out.println("nombre:"+nombreFiltro.getValue());
        System.out.println("profesor:"+profesorFiltro.getValue());
        System.out.println("categoria:"+categoriaFiltro.getValue());

       if(nombreFiltro.getValue() != null){
            cursos =  cursosService.buscarCursosPorNombre(nombreFiltro.getValue());
        }

        if(profesorFiltro.getValue() != null){
            cursos =  cursosService.buscarCursosPorProfesor(profesorFiltro.getValue());
        }

        if(categoriaFiltro.getValue() != null){
            cursos =  cursosService.buscarCursosPorCategoria(categoriaFiltro.getValue());
        }

        if (cursos!=null){
            grid.setItems(cursos);
        }
    }

    /**
     * Funcición para actualizar el grid con todos los cursos que se han dado de alta.
     *
     */
    private void actualizarGrid() {
        Curso[] listaCursos = cursosService.buscarTodos();
        grid.setItems(listaCursos);
    }

    /**
     * Funcición para crear o actualizar los datos de un curso.
     *
     */
    private void guardarCurso(CursoForm.GuardarEvent evt) {
        boolean result;
        Curso curso = evt.getCurso();

        // Se crea un nuevo alumno o se actualiza uno existente
        if(curso.getIdCurso() != null && curso.getIdCurso() > 0) {
            result = cursosService.actualizarCurso(curso);
        } else {
            result = cursosService.guardarCurso(curso);
        }

        if(result){
            notificationOk.open();
        } else {
            notificationKo.open();
        }
        actualizarGrid();
        cerrarFormulario();
    }

    /**
     * Funcición para editar los datos de un curso.
     *
     */
    private void editarCurso(Curso curso) {
        cursoForm.setCurso(curso);
        formularioDg.open();
    }

    /**
     * Funcición para eliminar un alumno.
     *
     */
    private void eliminarCurso(Curso curso) {

        // Se configura el Dialog para confirmar la eliminación
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("Desea eliminar el curso "+curso.getNombre()+"?");

        HorizontalLayout btns = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {
            cursosService.eliminarCurso(curso.getIdCurso());
            confirmacionDg.close();
            actualizarGrid();

        });
        cancelarBtn.addClickListener(click -> {
            confirmacionDg.close();
        });

        btns.add(eliminarBtn,cancelarBtn);
        confirmacionDg.add(msjConfirmacion,btns);
        confirmacionDg.open();
    }

    private void matricularCurso(Curso curso) {

    }

    /**
     * Funcición para cerrar el formulario de curso.
     *
     */
    private void cerrarFormulario() {
        //Curso curso = new Curso();
        //cursoForm.setCurso(curso);
        formularioDg.close();
    }

}
