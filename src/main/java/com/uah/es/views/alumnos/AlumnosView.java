package com.uah.es.views.alumnos;

import com.uah.es.model.Alumno;
import com.uah.es.service.IAlumnosService;
import com.uah.es.views.MainLayout;
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
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

    IAlumnosService alumnosService;
    AlumnoForm alumnoForm;


    PaginatedGrid<Alumno> grid = new PaginatedGrid<>();
    TextField correoFiltro = new TextField();
    Button buscarBtn = new Button(new Icon(VaadinIcon.SEARCH));
    Button nuevoAlumnoBtn = new Button(new Icon(VaadinIcon.NEWSPAPER));
    Dialog formularioDg = new Dialog();


    Notification notificationOk = new Notification("Se guardado correctamente el alumno", 3000);
    Notification notificationKo = new Notification("Error al guardar el alumno", 3000);



    public AlumnosView(IAlumnosService alumnosService) {

        addClassName("usuarios-view");
        this.alumnosService = alumnosService;
        configurarGrid();
        configurarFiltros();
        configurarFormulario();
        add(correoFiltro,buscarBtn,nuevoAlumnoBtn,grid);
    }

    private void configurarGrid() {
        Alumno[] listaAlumnos = alumnosService.buscarTodos();

        /*TemplateRenderer<Alumno> importantRenderer = TemplateRenderer.<Alumno>of(
                "<iron-icon icon='vaadin:check' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: var(--lumo-primary-text-color);'>");*/
        grid.addColumn(Alumno::getIdAlumno).setHeader("ID").setKey("id");
        grid.addColumn(Alumno::getNombre).setHeader("Nombre").setKey("nombre").setSortable(true);
        grid.addColumn(Alumno::getCorreo).setHeader("Correo").setKey("correo").setSortable(true);
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

        // Sets the max number of items to be rendered on the grid for each page
        grid.setPageSize(12);
        // Sets how many pages should be visible on the pagination before and/or after the current selected page
        grid.setPaginatorSize(3);
        grid.setItems(listaAlumnos);

    }

    private void configurarFiltros() {
        correoFiltro.setLabel("Correo electrónico");
        correoFiltro.setWidth("30%");
        correoFiltro.setClearButtonVisible(true);
        correoFiltro.setValueChangeMode(ValueChangeMode.EAGER);
        buscarBtn.setEnabled(false);

        // Habilitamos el boton buscar solo cuando el correo tenga valor
        correoFiltro.addValueChangeListener(e -> {
            buscarBtn.setEnabled(!correoFiltro.getValue().isEmpty());
        });

        buscarBtn.addClickListener(e -> filtrarPorCorreo());
    }

    private void configurarFormulario(){

        alumnoForm = new AlumnoForm();
        alumnoForm.addListener(AlumnoForm.SaveEvent.class, this::guardarAlumno);
        alumnoForm.addListener(AlumnoForm.CloseEvent.class, e -> cerrar());
        formularioDg.add(alumnoForm);

        nuevoAlumnoBtn.addClickListener(event -> {
            formularioDg.open();
        });

    }

    private void filtrarPorCorreo() {
        System.out.println(correoFiltro.getValue());
        Alumno alumno = alumnosService.buscarAlumnoPorCorreo(correoFiltro.getValue());
        if (alumno!=null){
            grid.setItems(alumno);
        }
    }

    private void updateList() {
        Alumno[] listaAlumnos = alumnosService.buscarTodos();
        grid.setItems(listaAlumnos);
    }

    private void guardarAlumno(AlumnoForm.SaveEvent evt) {

        Boolean result;
        Alumno alumno = evt.getAlumno();

        if(alumno.getIdAlumno() != null && alumno.getIdAlumno() > 0) {
            result = alumnosService.actualizarAlumno(alumno);
        } else {
            result = alumnosService.guardarAlumno(alumno);
        }

        if(result){
            notificationOk.open();
        } else {
            notificationKo.open();
        }
        updateList();
        cerrar();
    }

    private void editarAlumno(Alumno alumno) {
        System.out.println("Editar"+alumno.getNombre());
        alumnoForm.setAlumno(alumno);
        formularioDg.open();
    }

    private void elimarAlumno(Alumno alumno) {
        System.out.println("Eliminar alumno"+alumno.getNombre());
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("Desea eliminar el alumno: "+ alumno.getNombre());

        HorizontalLayout btns = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Eliminar");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {
            alumnosService.eliminarAlumno(alumno.getIdAlumno());
            confirmacionDg.close();
            updateList();

        });
        cancelarBtn.addClickListener(click -> {
            confirmacionDg.close();
        });

        btns.add(eliminarBtn,cancelarBtn);
        confirmacionDg.add(msjConfirmacion,btns);
        confirmacionDg.open();
    }

    private void cerrar() {
        Alumno alumno = new Alumno();
        alumnoForm.setAlumno(alumno);
        formularioDg.close();
        //form.setVisible(false);
        //removeClassName("editing");
    }

}
