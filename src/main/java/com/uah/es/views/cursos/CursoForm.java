package com.uah.es.views.cursos;

import com.uah.es.model.Curso;
import com.uah.es.model.Usuario;
import com.uah.es.service.IUsuariosService;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToDoubleConverter;
import com.vaadin.flow.shared.Registration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CursoForm extends FormLayout {

    //Servicios para comunicación con el backend
    IUsuariosService usuariosService;
    MemoryBuffer buffer = new MemoryBuffer();

    // Inputs y btns del formulario
    TextField nombre= new TextField("Nombre");
    IntegerField duracion = new IntegerField("Duración (H)");
    //TextField profesor = new TextField ("Profesor");
    TextField precio = new TextField ("Precio (€)");
    Select<String> categoria = new Select<>();
    Select<String> profesor = new Select<>();
    Upload imagen = new Upload(buffer);

    Button cancelarBtn = new Button("Cancelar");
    Button guardarBtn = new Button("Guardar");

    H2 titulo = new H2("Curso");

    Binder<Curso> binder = new BeanValidationBinder<>(Curso.class);
    private Curso curso = new Curso();

    public CursoForm(IUsuariosService usuariosService){
        this.usuariosService = usuariosService;

        categoria.setLabel("Categoría");
        categoria.setItems("Desarrollo", "Educación","Finanzas");

        profesor.setLabel("Profesor");
        List<Usuario> usuariosProfesor = Arrays.asList(usuariosService.buscarUsuariosPorRol(3));
        List<String> nombresProfesores = new ArrayList<>();
        usuariosProfesor.forEach(u ->{
            nombresProfesores.add(u.getNombre());
        });
        profesor.setItems(nombresProfesores);

        // Relacionamos los atributos del objeto Alumno con los campos del formulario
        binder.forField(nombre)
                .asRequired("Campo requerido")
                .bind(Curso::getNombre,Curso::setNombre);
        binder.forField(duracion)
                .asRequired("Campo requerido")
                .bind(Curso::getDuracion,Curso::setDuracion);
        binder.forField(profesor)
                .asRequired("Campo requerido")
                .bind(Curso::getProfesor,Curso::setProfesor);
        binder.forField(precio)
                .withNullRepresentation("")
                .withConverter(new StringToDoubleConverter("No es un precio válido"))
                .asRequired("Campo requerido")
                .bind(Curso::getPrecio,Curso::setPrecio);
        binder.forField(categoria)
                .asRequired("Campo requerido")
                .bind(Curso::getCategoria,Curso::setCategoria);

        setMaxWidth("600px");
        add(titulo,nombre,duracion,profesor,precio,categoria,configurarCargaImagen(),configurarBtnsLayout());
    }

    private Component configurarBtnsLayout() {

        //Se configura los btns del formulario
        guardarBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        cancelarBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        guardarBtn.addClickShortcut(Key.ENTER);
        cancelarBtn.addClickShortcut(Key.ESCAPE);

        guardarBtn.addClickListener(click -> validarYGuardar());
        cancelarBtn.addClickListener(click -> fireEvent(new CerrarEvent(this)));

        binder.addStatusChangeListener(evt -> guardarBtn.setEnabled(binder.isValid()));

        HorizontalLayout btnsLayout =  new HorizontalLayout();
        btnsLayout.setPadding(true);
        btnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        btnsLayout.add(cancelarBtn,guardarBtn);

        return btnsLayout;
    }

    private Component configurarCargaImagen() {
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setPadding(false);

        Label nombreCampoImagen = new Label("Imagen");
        imagen.setAcceptedFileTypes("image/jpeg", "image/png", "image/gif");
        imagen.addSucceededListener(event -> {
            curso.setImagen(event.getFileName());
        });

        verticalLayout.add(nombreCampoImagen,imagen);
        return verticalLayout;
    }

    public void setCurso(Curso curso) {
        this.curso = curso;
        binder.readBean(curso);
    }

    private void validarYGuardar() {
        try {
            binder.writeBean(curso);
            fireEvent(new CursoForm.GuardarEvent(this, curso));
        } catch (ValidationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Eventos de los botones del formulario.
     *
     */
    public static abstract class CursoFormEvent extends ComponentEvent<CursoForm> {
        private Curso curso;

        protected CursoFormEvent(CursoForm source, Curso curso) {
            super(source, false);
            this.curso = curso;
        }

        public Curso getCurso() {
            return curso;
        }
    }

    public static class GuardarEvent extends CursoForm.CursoFormEvent {
        GuardarEvent(CursoForm source, Curso curso) {
            super(source, curso);
        }
    }

    public static class CerrarEvent extends CursoForm.CursoFormEvent {
        CerrarEvent(CursoForm source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
                                                                  ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }



}
