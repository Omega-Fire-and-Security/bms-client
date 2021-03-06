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

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import org.controlsfx.control.MaskerPane;

/**
 * Login Controller class
 * Created by th3gh0st 2016/12/14
 * @author ghost
 */
public class LoginController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtUsr;
    @FXML
    private TextField txtPwd;
    @FXML
    private Button btnShow;
    private String pwd="";
    private boolean pwd_masked = true;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        refreshView();
    }

    @Override
    public void refreshView()
    {
        //TODO: remove this
        txtUsr.setText("jivesh");
        txtPwd.setText("#Robertsham1234");

        txtPwd.setOnKeyReleased(event ->
        {
            if(event.getCode() == KeyCode.ENTER)
            {
                login();
            } else
            {
                if(txtPwd.getText()!=null)
                {
                    //pwd += txtPwd.getText().charAt(txtPwd.getText().length()-1);
                    //System.out.println("pwd so far: " + pwd + " >> " + pwd.replaceAll(".", "\u2022"));
                    /*if (pwd_masked)
                        txtPwd.setText(txtPwd.getText().replaceAll(".", "\u2022"));//replace all text with bullets
                    else txtPwd.setText(pwd);*/
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "empty password");
            }
        });

        /*txtPwd.textProperty().addListener(new ChangeListener<String>()
        {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                System.out.println("text changed from: " + oldValue + " to " + newValue);
            }
        });*/

        txtUsr.setOnKeyPressed(event ->
        {
            if(event.getCode() == KeyCode.ENTER)
            {
                login();
            }
        });
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

    @FXML
    public void togglePasswordMask()
    {
        if(txtPwd.getText()!=null)
        {
            if (txtPwd.getText().contains("\u2022"))
            {
                //is masked, unmask
                txtPwd.setText(pwd);
                btnShow.setText("hide");
                pwd_masked = false;
            }
            else//is not masked
            {
                pwd_masked = true;
                pwd = txtPwd.getText();
                txtPwd.setText(pwd.replaceAll(".", "\u2022"));
                btnShow.setText("show");
            }
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "empty password");
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
        //Load HomeScreen
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
                            if(session != null)
                            {
                                //SessionManager ssn_mgr = SessionManager.getInstance();
                                //ssn_mgr.getSessions().removeAll(ssn_mgr.getSessions());
                                SessionManager.getInstance().clearSessions();
                                SessionManager.getInstance().addSession(session);
                                SessionManager.getInstance().setActive(session);
                                /*ssn_mgr.getActive().setDate(session.getDate());
                                ssn_mgr.getActive().setSession_id(session.getSession_id());
                                ssn_mgr.getActive().setTtl(session.getTtl());
                                ssn_mgr.getActive().setUsr(session.getUsr());*/
                                //load User data to memory
                                EmployeeManager.getInstance().initialize();
                                if (ScreenManager.getInstance().loadScreen(Screens.HOME.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/" + Screens.HOME.getScreen())))
                                    ScreenManager.getInstance().setScreen(Screens.HOME.getScreen());
                                else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load home screen.");
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid user session.");
                        }else IO.logAndAlert("Login failure", "Invalid input.", IO.TAG_ERROR);
                    } catch (ClassNotFoundException e)
                    {
                        IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    } catch(ConnectException ex)
                    {
                        IO.logAndAlert("Login failure", ex.getMessage() + ", \nis the server up? are you connected to the network?", IO.TAG_ERROR);
                        ex.printStackTrace();
                    } catch (LoginException ex)
                    {
                        IO.logAndAlert("Login failure", ex.getMessage(), IO.TAG_ERROR);
                        ex.printStackTrace();
                    } catch (IOException e)
                    {
                        IO.logAndAlert("IO Error", e.getMessage(), IO.TAG_ERROR);
                        e.printStackTrace();
                    } catch (Exception e)
                    {
                        IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                        e.printStackTrace();
                    }
                    //hide loading pane
                    if(param!=null)
                    {
                        if (param instanceof MaskerPane)
                        {
                            MaskerPane loadingPane = (MaskerPane) param;
                            loadingPane.setVisible(false);
                        }
                    }
                }
            }).start();
            return null;
        });
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
