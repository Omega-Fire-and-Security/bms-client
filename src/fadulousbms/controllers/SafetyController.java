/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SafetyController extends ScreenController implements Initializable
{

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading safety view..");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading safety data model..");

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        refreshModel(param ->
        {
            Platform.runLater(() -> refreshView());
            return null;
        });
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    public void safetyClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.SAFETY_FILES.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.SAFETY_FILES.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.SAFETY_FILES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load safety files screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public void riskClick()
    {
    }

    public void ohsClick()
    {
    }

    public void appointmentClick()
    {
    }

    public void showScanWindow()
    {
        /*Morena morena = new Morena();
        try
        {
            Manager manager= Manager.getInstance();
            morena.simpleScan();
            manager.close();
        } catch (Exception e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }*/
        //Scan scan = new Scan();

    }

    public void inspectionClick()
    {
    }
}