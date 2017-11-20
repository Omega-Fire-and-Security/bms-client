/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SettingsController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtIP = new TextField();
    @FXML
    private TextField txtPort = new TextField();
    @FXML
    private ImageView img_logo;

    @Override
    public void refreshView()
    {
        txtIP.setText("127.0.0.1");
        txtPort.setText("9000");
        RemoteComms.host = "http://127.0.0.1:9000";
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
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void changeLogo()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    FileChooser fileChooser = new FileChooser();
                    File f = fileChooser.showOpenDialog(txtIP.getScene().getWindow());
                    if (f != null)
                    {
                        if (f.exists())
                        {
                            FileInputStream in = new FileInputStream(f);
                            byte[] buffer = new byte[(int) f.length()];
                            in.read(buffer, 0, buffer.length);
                            in.close();

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "image/" + f.getName().split("\\.")[1]));
                            headers.add(new AbstractMap.SimpleEntry<>("File-Type", f.getName().split("\\.")[1]));
                            RemoteComms.uploadFile("/api/upload/logo", headers, buffer);
                            IO.log(getClass().getName(), IO.TAG_ERROR, "\n File size: " + buffer.length + " bytes.");
                        } else
                        {
                            IO.logAndAlert(getClass().getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, "File object is null.");
                    }
                } catch (FileNotFoundException e)
                {
                    IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                } catch (IOException e)
                {
                    IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else{
                IO.logAndAlert(getClass().getName(), "Active session has expired.", IO.TAG_ERROR);
            }
        }else{
            IO.logAndAlert(getClass().getName(), "Active session is invalid.", IO.TAG_ERROR);
        }
    }

    @FXML
    public void applySettings()
    {
        if(txtPort.getText()!=null && txtIP.getText()!=null)
        {
            RemoteComms.setHost("http://" + txtIP.getText() + ":" + txtPort.getText());
            IO.logAndAlert(getClass().getName(), "successfully updated system configuration.", IO.TAG_INFO);
        } else IO.logAndAlert(SettingsController.class.getName(), "Empty entries are not allowed for required fields.", IO.TAG_ERROR);
    }
    
}
