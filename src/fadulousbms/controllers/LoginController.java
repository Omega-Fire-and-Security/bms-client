/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.FadulousBMS;
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
        txtUsr.setText("ghost");
        txtPwd.setText("12345678");
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

    @FXML
    public void resetPassword()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.RESET_PWD.getScreen(),getClass().getResource("../views/"+Screens.RESET_PWD.getScreen())))
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
                        String usr = txtUsr.getText(), pwd=txtPwd.getText();
                        if(usr!=null && pwd!=null)
                        {
                            Session session = RemoteComms.auth(usr, pwd);
                            SessionManager ssn_mgr = SessionManager.getInstance();
                            ssn_mgr.addSession(session);

                            //load User data to memory
                            EmployeeManager.getInstance().loadDataFromServer();

                            if (screenManager.loadScreen(Screens.HOME.getScreen(), getClass().getResource("../views/" + Screens.HOME.getScreen())))
                            {
                                screenManager.setScreen(Screens.HOME.getScreen());
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load home screen.");
                        }else{
                            JOptionPane.showMessageDialog(null, "Invalid entry.", "Login failure", JOptionPane.ERROR_MESSAGE);
                            IO.log(getClass().getName(), IO.TAG_ERROR, "invalid input.");
                        }
                    }catch(ConnectException ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.getMessage() + ", \nis the server up? are you connected to the network?", "Login failure", JOptionPane.ERROR_MESSAGE);
                        IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage() + ", \nis the server up? are you connected to the network?");
                    } catch (LoginException ex)
                    {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Login failure", JOptionPane.ERROR_MESSAGE);
                        IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }
}
