/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

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

    @Override
    public void refreshView()
    {
    }

    @Override
    public void refreshModel()
    {
    }

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
}