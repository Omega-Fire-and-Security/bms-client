/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
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

    @Override
    public void refreshView()
    {
        //TODO
        txtUsr.setText("ghost");
        txtPwd.setText("abc");
    }

    @Override
    public void refreshModel()
    {
        EmployeeManager.getInstance().initialize();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
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
            HttpURLConnection connection = RemoteComms.postData("/api/vericode/add", params, null);
            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                IO.logAndAlert("Reset Code Sent", IO.readStream(connection.getInputStream()), IO.TAG_INFO);
            }else IO.logAndAlert("Reset Code Error", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

        } catch (IOException e)
        {
            IO.logAndAlert("Password Reset Error", e.getMessage(), IO.TAG_ERROR);
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
            HttpURLConnection connection = RemoteComms.postData("/api/employee/pwdreset", params, null);
            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                IO.logAndAlert("Password Successfully Reset", IO.readStream(connection.getInputStream()), IO.TAG_INFO);
            }else IO.logAndAlert("Password Reset Error", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

        } catch (IOException e)
        {
            IO.logAndAlert("Password Reset Error", e.getMessage(), IO.TAG_ERROR);
        }
    }
}
