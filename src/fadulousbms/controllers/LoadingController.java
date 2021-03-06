/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.ScreenManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class LoadingController extends ScreenController implements Initializable
{
    @FXML
    private Label lblLoading;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //Loading Animation
        Thread t = new Thread(() ->
        {
            while(true)
            {
                Platform.runLater(() ->
                {
                    if(lblLoading.getText().length()>14)
                        lblLoading.setText("Loading");
                    lblLoading.setText(lblLoading.getText()+".");
                });
                try
                {
                    Thread.sleep(100);
                } catch (InterruptedException e)
                {
                    IO.log("LoadingScreenController", IO.TAG_ERROR, e.getMessage());
                }
            }
        });
        t.start();
    }

    @Override
    public void refreshView()
    {
    }

    @Override
    public void refreshModel(Callback callback)
    {
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
}