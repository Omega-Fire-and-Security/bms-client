/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.EmployeeManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ResetPwdController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtUsr,txtPwd,txtCode;

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
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading reset password view..");

        //TODO
        txtUsr.setText("ghost");
        txtPwd.setText("abc");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading employee data model..");

        EmployeeManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        EmployeeManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void sendCode()
    {
        ArrayList<AbstractMap.SimpleEntry<String,String>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<>("usr", txtUsr.getText()));
        try
        {
            HttpURLConnection connection = RemoteComms.post("/vericode", "", null);
            if(connection!=null)
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    IO.logAndAlert("Reset Code Sent", IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                else IO.logAndAlert("Reset Code Error", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
            else IO.log(getClass().getName(), IO.TAG_ERROR, "sendCode()> Could not get a valid response from the server.");

        } catch (IOException e)
        {
            IO.logAndAlert("Password Reset Error", e.getMessage(), IO.TAG_ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void resetPassword()
    {
        ArrayList<AbstractMap.SimpleEntry<String,String>> params = new ArrayList<>();
        params.add(new AbstractMap.SimpleEntry<>("usr", txtUsr.getText()));
        params.add(new AbstractMap.SimpleEntry<>("code", txtCode.getText()));
        params.add(new AbstractMap.SimpleEntry<>("pwd", txtPwd.getText()));

        try
        {
            HttpURLConnection connection = RemoteComms.post("/employee/pwdreset", "", null);
            if(connection!=null)
            {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.logAndAlert("Password Successfully Reset", IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                } else IO.logAndAlert("Password Reset Error", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "resetPassword()> Could not get a valid response from the server.");

        } catch (IOException e)
        {
            IO.logAndAlert("Password Reset Error", e.getMessage(), IO.TAG_ERROR);
            e.printStackTrace();
        }
    }
}
