package com.uah.es.views.matriculas;

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.service.IAlumnosService;
import com.uah.es.service.ICursosService;
import com.uah.es.service.IMatriculasService;
import com.uah.es.service.IUsuariosService;
import com.uah.es.views.MainLayout;
import com.uah.es.views.alumnos.AlumnosView;
import com.uah.es.views.cursos.CursosView;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

@PageTitle("Matriculas")
@Route(value = "matriculas", layout = MainLayout.class)
@Secured("Admin")
public class MatriculasView extends VerticalLayout {

    //Servicios para comunicación con el backend
    ICursosService cursosService;
    IMatriculasService matriculasService;
    IUsuariosService usuariosService;
    IAlumnosService alumnosService;

    //Componentes visuales
    CursosView cursosView;
    AlumnosView alumnosView;
    Notification notificacionOK = new Notification("", 3000);
    Notification notificacionKO = new Notification("", 3000);
    Button eliminarMatricula = new Button("Eliminar matrícula",new Icon(VaadinIcon.OPEN_BOOK));

    Alumno alumnoSeleccionado = new Alumno();
    Curso cursoSeleccionado = new Curso();

    public MatriculasView(ICursosService cursosService, IMatriculasService matriculasService, IUsuariosService usuariosService, IAlumnosService alumnosService) {

        this.cursosService = cursosService;
        this.matriculasService =  matriculasService;
        this.usuariosService = usuariosService;
        this.alumnosService = alumnosService;

        notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);

        inicializarViews();

        eliminarMatricula.setEnabled(false);

        HorizontalLayout gridsLayout = new HorizontalLayout();
        gridsLayout.setWidth("100%");
        gridsLayout.add(configurarCursosView(),configurarAlumnosView());

        HorizontalLayout btnsLayout = new HorizontalLayout();
        eliminarMatricula.addThemeVariants(ButtonVariant.LUMO_ERROR);
        eliminarMatricula.addClickListener( e -> eliminarMatricula(cursoSeleccionado,alumnoSeleccionado));
        btnsLayout.getElement().getStyle().set("margin-left", "auto");
        btnsLayout.add(eliminarMatricula);

        add(gridsLayout,btnsLayout);

    }

    private void inicializarViews() {


    }

    private VerticalLayout configurarCursosView() {
        cursosView = new CursosView(cursosService, matriculasService, usuariosService, alumnosService);

        VerticalLayout cursosLayout =  new VerticalLayout();
        cursosView.configuracionMatriculasView();
        cursosView.setWidth("100%");
        cursosView.addClickListener( e -> {
            cursoSeleccionado =  cursosView.cursoSeleccionado;
            eliminarMatricula.setEnabled(cursoSeleccionado.getIdCurso()!= null && alumnoSeleccionado.getIdAlumno()!= null);
            alumnosView.setVisible(cursoSeleccionado.getIdCurso()!= null);
            alumnosView.configuracionMatriculasView(cursoSeleccionado);
        });
        cursosLayout.setPadding(false);
        cursosLayout.setMargin(false);
        cursosLayout.add(new H2("Cursos"),cursosView);
        return cursosLayout;
    }

    private VerticalLayout configurarAlumnosView() {
        alumnosView = new AlumnosView(alumnosService);

        VerticalLayout alumnosLayout =  new VerticalLayout();
        alumnosView.setWidth("100%");
        alumnosView.setVisible(false);
        alumnosView.addClickListener( e -> {
            alumnoSeleccionado =  alumnosView.alumnoSeleccionado;
            eliminarMatricula.setEnabled(cursoSeleccionado.getIdCurso()!= null && alumnoSeleccionado.getIdAlumno()!= null);
        });
        alumnosLayout.setPadding(false);
        alumnosLayout.setMargin(false);
        alumnosLayout.add(new H2("Alumnos"),alumnosView);
        return alumnosLayout;
    }

    /**
     * Función para matricular el alumno en el curso.
     *
     */
    private void eliminarMatricula(Curso curso, Alumno alumno) {
        // Se configura el Dialog para confirmar la matricula
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea eliminar la matrícula del Alumno: "+alumno.getNombre()+" del curso: "+curso.getNombre());

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
                inicializarViews();
            } else {
                notificacionKO.setText("Error al eliminar la matrícula del curso");
                notificacionKO.open();
            }
            confirmacionDg.close();

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

}
