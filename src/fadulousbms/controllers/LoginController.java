/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.*;
import fadulousbms.auxilary.Session;
import fadulousbms.exceptions.LoginException;
import fadulousbms.model.Screens;
import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.swing.JOptionPane;

/**
 * views Controller class
 *
 * @author ghost
 */
public class LoginController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtUsr;
    @FXML
    private TextField txtPwd;

    @Override
    public void refreshView()
    {
        //TODO: remove this
        txtUsr.setText("gh0st");
        txtPwd.setText("87654321");
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
        refreshView();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void resetPassword()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.RESET_PWD.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.RESET_PWD.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.RESET_PWD.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load password reset screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void login()
    {
        try
        {
            String usr = txtUsr.getText(), pwd=txtPwd.getText();
            if(usr!=null && pwd!=null)
            {
                Session session = RemoteComms.auth(usr, pwd);
                if(session != null)
                {
                    SessionManager ssn_mgr = SessionManager.getInstance();
                    ssn_mgr.addSession(session);

                    //load User data to memory
                    EmployeeManager.getInstance().loadDataFromServer();
                    //Load HomeScreen
                    final ScreenManager screenManager = ScreenManager.getInstance();
                    ScreenManager.getInstance().showLoadingScreen(param ->
                    {
                        new Thread(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    if (screenManager.loadScreen(Screens.HOME.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/" + Screens.HOME.getScreen())))
                                        screenManager.setScreen(Screens.HOME.getScreen());
                                    else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load home screen.");
                                } catch (IOException e)
                                {
                                    IO.logAndAlert("Login failure", e.getMessage(), IO.TAG_ERROR);
                                }
                            }
                        }).start();
                        return null;
                    });
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid user session.");
            }else IO.logAndAlert("Login failure", "Invalid input.", IO.TAG_ERROR);
        }catch(ConnectException ex)
        {
            IO.logAndAlert("Login failure", ex.getMessage() + ", \nis the server up? are you connected to the network?", IO.TAG_ERROR);
        } catch (LoginException ex)
        {
            IO.logAndAlert("Login failure", ex.getMessage(), IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert("Login failure", e.getMessage(), IO.TAG_ERROR);
        } catch (Exception e)
        {
            IO.logAndAlert("Login failure", e.getMessage(), IO.TAG_ERROR);
        }
    }
}
