package com.uah.es.views.alumnos;

import com.uah.es.filter.AlumnoFilter;
import com.uah.es.model.Alumno;
import com.uah.es.service.IAlumnosService;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.uah.es.views.MainLayout;
import org.vaadin.klaudeta.PaginatedGrid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*https://vaadin.com/directory/component/grid-pagination/samples*/

@PageTitle("Alumnos")
@Route(value = "alumnos", layout = MainLayout.class)
public class AlumnosView extends Div {

    private IAlumnosService alumnosService;
    private AlumnoFilter filterObject;
    private ListDataProvider<Alumno> dataProvider;
    private HeaderRow filterRow;

    PaginatedGrid<Alumno> grid = new PaginatedGrid<>();
    private TextField filterText = new TextField();

    public AlumnosView(IAlumnosService alumnosService) {
        this.alumnosService = alumnosService;
        List<Alumno> listaAlumnos = Arrays.asList(alumnosService.buscarTodos());

        dataProvider = new ListDataProvider<>(listaAlumnos);


        grid.addColumn(Alumno::getIdAlumno).setHeader("ID");
        grid.addColumn(Alumno::getNombre).setHeader("Nombre").setSortable(true);
        grid.addColumn(Alumno::getCorreo).setHeader("Correo").setKey("correo").setSortable(true);

        filterRow = grid.appendHeaderRow();

        filterObject = new AlumnoFilter();
        dataProvider.setFilter(alumno -> filterObject.test(alumno));


        //grid.setItems(alumnosService.buscarTodos());
        // Sets the max number of items to be rendered on the grid for each page
        grid.setPageSize(4);
        // Sets how many pages should be visible on the pagination before and/or after the current selected page
        grid.setPaginatorSize(3);

        grid.setItems();

        addClassName("usuarios-view");
        configureFilter();
        add(filterText,grid);
    }

    private void configureFilter() {
        filterText.setLabel("Correo");
        //filterText.setPlaceholder("Filtrar por correo...");
        filterText.setClearButtonVisible(true);
        filterText.setValueChangeMode(ValueChangeMode.LAZY);
        filterText.addValueChangeListener(e -> {
            filterObject.setCorreo(e.getValue());
            dataProvider.refreshAll();
        });

        filterText.setValueChangeMode(ValueChangeMode.EAGER);

        //filterRow.getCell(grid.getColumnByKey("correo")).setComponent(filterText);
        filterText.setSizeFull();
        filterText.setPlaceholder("Filter");
        filterText.getElement().setAttribute("focus-target", "");

    }

    private void updateList() {
        System.out.println(filterText.getValue());
        Alumno alumno = alumnosService.buscarAlumnoPorCorreo(filterText.getValue());

        if (alumno!=null){
            grid.setItems(alumno);
        } else {

        }
    }

}
