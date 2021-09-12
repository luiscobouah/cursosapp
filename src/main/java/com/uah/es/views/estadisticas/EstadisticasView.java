package com.uah.es.views.estadisticas;

import com.uah.es.model.Curso;
import com.uah.es.model.Matricula;
import com.uah.es.model.Rol;
import com.uah.es.model.Usuario;
import com.uah.es.service.ICursosService;
import com.uah.es.service.IMatriculasService;
import com.uah.es.service.IRolesService;
import com.uah.es.service.IUsuariosService;
import com.uah.es.utils.Configuracion;
import com.uah.es.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.board.Board;
import com.vaadin.flow.component.charts.Chart;
import com.vaadin.flow.component.charts.model.*;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.security.access.annotation.Secured;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.uah.es.security.SecurityUtils.getEmailUser;
import static com.uah.es.security.SecurityUtils.userHasRole;

@PageTitle("Estadísticas")
@Secured({"Admin","Profesor"})
@Route(value = "estadisticas", layout = MainLayout.class)

public class EstadisticasView extends Div {

    // Servicio para comunicación con el backend
    ICursosService cursosService;
    IUsuariosService usuariosService;
    IRolesService rolesService;
    IMatriculasService matriculasService;

    List<Curso> listaCursos = new ArrayList<Curso>();
    List<Usuario> listaUsuarios = new ArrayList<Usuario>();
    List<Rol> listaRoles = new ArrayList<Rol>();
    List<Matricula> listaMatriculas = new ArrayList<Matricula>();

    EstadisticasView(ICursosService cursosService, IUsuariosService usuariosService, IRolesService rolesService, IMatriculasService matriculasService){

        //Se inicializan los servicios
        this.cursosService = cursosService;
        this.usuariosService = usuariosService;
        this.rolesService =  rolesService;
        this.matriculasService = matriculasService;

        //Configuracion de componente Board para visualizar los distintos gráficos
        Board board = new Board();
        // Se configuran las filas del Board dependiendo del Rol de usuario
        if(userHasRole(Collections.singletonList(Configuracion.ROL_ADMIN))){
            board.addRow(graficoCursosAlumnos(Configuracion.ROL_ADMIN),graficoRolesUsuarios());
            board.addRow(graficoMatriculasPorMes());
        }
        if(userHasRole(Collections.singletonList(Configuracion.ROL_PROFESOR))){
            board.addRow(graficoCursosAlumnos(Configuracion.ROL_PROFESOR)).setHeight("70%");
            board.setHeightFull();
            setHeightFull();
        }
        add(board);
    }

    /**
     *  Func para configurar el grafico de Cursos.
     *
     */
    private Component graficoCursosAlumnos(String rol) {

        Chart graficoCursos = new Chart(ChartType.PIE);
        Configuration conf = graficoCursos.getConfiguration();

        // Configuracion del titulo y subtitulo
        conf.setTitle("Cursos");
        conf.setSubTitle("Número de alumnos por curso");
        //Configuracion del tooltip para visualizar el numero de alumnos
        Tooltip tooltip = new Tooltip();
        conf.setTooltip(tooltip);

        if (rol.equals(Configuracion.ROL_PROFESOR)) {
            PlotOptionsPie plotOptions = new PlotOptionsPie();
            plotOptions.setAllowPointSelect(true);
            plotOptions.setCursor(Cursor.POINTER);
            plotOptions.setShowInLegend(true);
            conf.setPlotOptions(plotOptions);
        }

        //Datos a visualizar
        DataSeries cursos = new DataSeries("Alumnos");
        //Se obtienen todos los cursos dependiendo del ROL.
        if (rol.equals(Configuracion.ROL_ADMIN)){
            listaCursos = Arrays.asList(cursosService.buscarTodos());
        }else {
            Usuario usuario = usuariosService.buscarUsuarioPorCorreo(getEmailUser());
            listaCursos = Arrays.asList(cursosService.buscarCursosPorProfesor(usuario.getNombre()));
        }

        listaCursos.forEach(curso -> {
            //Para cada curso se recupera el nombre y el total de alumnos que tiene
            DataSeriesItem dato = new DataSeriesItem(curso.getNombre(),curso.getAlumnos().size());
            cursos.add(dato);
        });
        conf.setSeries(cursos);
        graficoCursos.setVisibilityTogglingDisabled(true);
        return  graficoCursos;
    }

    /**
     *  Func para configurar el grafico de Usuarios.
     *
     */
    private Component graficoRolesUsuarios() {

        Chart graficoRoles = new Chart(ChartType.COLUMN);
        Configuration conf = graficoRoles.getConfiguration();

        //Configuracion del grafico
        conf.setTitle("Roles");
        conf.setSubTitle("Número de usuarios por rol");
        conf.getLegend().setEnabled(false);
        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        conf.addxAxis(x);
        YAxis y = new YAxis();
        y.setTitle("Total usuarios");
        conf.addyAxis(y);

        PlotOptionsColumn column = new PlotOptionsColumn();
        column.setCursor(Cursor.POINTER);
        column.setDataLabels(new DataLabels(true));
        conf.setPlotOptions(column);

        // Datos a visualizar
        DataSeries roles = new DataSeries("Roles");
        PlotOptionsColumn plotOptionsColumn = new PlotOptionsColumn();
        plotOptionsColumn.setColorByPoint(true);
        roles.setPlotOptions(plotOptionsColumn);
        //Se obtienen todos los usuarios y roles
        listaUsuarios = Arrays.asList(usuariosService.buscarTodos());
        listaRoles = Arrays.asList(rolesService.buscarTodos());
        listaRoles.forEach(rol -> {
            //Para cada rol recuperamos su nombre y el número de usuarios
            DataSeriesItem dato = new DataSeriesItem(rol.getAuthority(),obtenerNumeroUsuariosRol(listaUsuarios,rol));
            roles.add(dato);
        });

        conf.setSeries(roles);
        return graficoRoles;
    }

    /**
     *  Func para configurar el grafico de Matriculas.
     *
     */
    private Component graficoMatriculasPorMes(){

        //Se obtienen todas las matriculas
        listaMatriculas = Arrays.asList(matriculasService.buscarTodas());
        Chart graficoMatriculas = new Chart(ChartType.AREASPLINE);
        Configuration conf = graficoMatriculas.getConfiguration();

        //Configuracion del grafico
        conf.setTitle("Matrículas");
        conf.setSubTitle("Número matrículas por mes");
        XAxis x = new XAxis();
        x.setType(AxisType.CATEGORY);
        x.setCategories("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic");;
        conf.addxAxis(x);
        YAxis y = new YAxis();
        y.setTitle("Número matrículas");
        conf.addyAxis(y);

        //Para cada mes se recuperan las matriculas
        DataSeries matriculas = new DataSeries("Matriculas");
        matriculas.setData(
            obtenerNumeroMatriculasMes(0),//Enero
            obtenerNumeroMatriculasMes(1),
            obtenerNumeroMatriculasMes(2),
            obtenerNumeroMatriculasMes(3),
            obtenerNumeroMatriculasMes(4),
            obtenerNumeroMatriculasMes(5),
            obtenerNumeroMatriculasMes(6),
            obtenerNumeroMatriculasMes(7),
            obtenerNumeroMatriculasMes(8),
            obtenerNumeroMatriculasMes(9),
            obtenerNumeroMatriculasMes(10),
            obtenerNumeroMatriculasMes(11)//Diciembre
        );

        conf.addSeries(matriculas);
        return graficoMatriculas;
    }

    /**
     * Func para obtener el número de usuarios de un determinado rol.
     *
     */
    private int obtenerNumeroUsuariosRol(List<Usuario> usuarios, Rol rol) {
        AtomicInteger numeroUsuario = new AtomicInteger();
        usuarios.forEach( usuario -> {
            if (usuario.getRoles().contains(rol)){
               numeroUsuario.getAndIncrement();
            }
        });
        return numeroUsuario.get();
    }

    /**
     * Func para obtener el número de matriculas por mes.
     *
     */
    private int obtenerNumeroMatriculasMes(int mes) {
        AtomicInteger numeroMatriculas = new AtomicInteger();
        listaMatriculas.forEach( matricula -> {
            if (matricula.getFecha().getMonth()==mes){
                numeroMatriculas.getAndIncrement();
            }
        });
        return numeroMatriculas.get();
    }
}
