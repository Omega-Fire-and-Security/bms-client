package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import fadulousbms.model.Job;
import fadulousbms.model.Screens;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import jfxtras.scene.control.agenda.Agenda;

import javax.swing.*;
import java.io.IOException;
import java.net.URL;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ghost on 2017/02/02.
 */
public class AgendaController extends ScreenController implements Initializable
{
    @FXML
    private Agenda projectsAgenda;

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        EmployeeManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        QuoteManager.getInstance().initialize();
        JobManager.getInstance().initialize();

        for(Job job_appointment: JobManager.getInstance().getDataset().values())
            projectsAgenda.appointments().add(job_appointment);
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading calender[agenda] view..");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading homescreen data model..");
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void forceSynchronise()
    {
        refreshModel(param ->
        {
            Platform.runLater(() -> refreshView());
            return null;
        });

    }

    /*public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }*/
}
