package com.uah.es.views.matriculas;

import com.uah.es.model.Alumno;
import com.uah.es.model.Curso;
import com.uah.es.model.Matricula;
import com.uah.es.model.Usuario;
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


    Alumno alumnoSeleccionado = new Alumno();
    Curso cursoSeleccionado = new Curso();

    Button matricularBtn = new Button("Matricular",new Icon(VaadinIcon.OPEN_BOOK));

    public MatriculasView(ICursosService cursosService, IMatriculasService matriculasService, IUsuariosService usuariosService, IAlumnosService alumnosService) {

        this.cursosService = cursosService;
        this.matriculasService =  matriculasService;
        this.usuariosService = usuariosService;
        this.alumnosService = alumnosService;

        notificacionOK.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
        notificacionKO.addThemeVariants(NotificationVariant.LUMO_ERROR);

        matricularBtn.setEnabled(false);

        cursosView = new CursosView(cursosService,matriculasService,usuariosService,alumnosService);
        cursosView.ocultarAcciones();
        cursosView.setWidth("100%");
        alumnosView = new AlumnosView(alumnosService);
        alumnosView.setWidth("100%");
        alumnosView.setVisible(false);

        cursosView.addClickListener( e -> {
            cursoSeleccionado =  cursosView.cursoSeleccionado;
            matricularBtn.setEnabled(cursoSeleccionado.getIdCurso()!= null && alumnoSeleccionado.getIdAlumno()!= null);
            alumnosView.setVisible(cursoSeleccionado.getIdCurso()!= null);
            alumnosView.ocultarAcciones(cursoSeleccionado);

        });
        alumnosView.addClickListener( e -> {
            alumnoSeleccionado =  alumnosView.alumnoSeleccionado;
            matricularBtn.setEnabled(cursoSeleccionado.getIdCurso()!= null && alumnoSeleccionado.getIdAlumno()!= null);
        });

        VerticalLayout cursosLayout =  new VerticalLayout();
        cursosLayout.add(new Label("Cursos"),cursosView);


        VerticalLayout alumnosLayout =  new VerticalLayout();
        alumnosLayout.add(new Label("Alumnos"),alumnosView);


        HorizontalLayout gridsLayout = new HorizontalLayout();
        gridsLayout.setWidth("100%");
        gridsLayout.add(cursosLayout,alumnosLayout);


        HorizontalLayout btnsLayout = new HorizontalLayout();
        matricularBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        matricularBtn.addClickListener( e -> matricularAlumno(cursoSeleccionado,alumnoSeleccionado));
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.getElement().getStyle().set("margin-left", "auto");
        btnsLayout.add(matricularBtn);

        add(gridsLayout,btnsLayout);

    }/**
     * Función para matricular el alumno en el curso.
     *
     */
    private void matricularAlumno(Curso curso, Alumno alumno) {

        // Se configura el Dialog para confirmar la matricula
        Dialog confirmacionDg = new Dialog();
        Label msjConfirmacion = new Label();
        msjConfirmacion.setText("¿Desea matricular el Alumno: "+alumno.getNombre()+" en el curso: "+curso.getNombre());


        Usuario usuario =  usuariosService.buscarUsuarioPorCorreo(alumno.getCorreo());
        usuario.setRoles(null);
        Matricula matricula = new Matricula(curso.getIdCurso(),usuario);

        HorizontalLayout btnsLayout = new HorizontalLayout();
        Button cancelarBtn = new Button("Cancelar");
        Button eliminarBtn = new Button("Matricular");
        eliminarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        eliminarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        eliminarBtn.addClickListener(click -> {

            if(matriculasService.guardarMatricula(matricula)){
                notificacionOK.setText("Se ha matriculado correctamente en el curso");
                notificacionOK.open();
            } else {
                notificacionKO.setText("Error al matricularse en el curso");
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
