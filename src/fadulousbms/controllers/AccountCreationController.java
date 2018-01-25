/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
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
public class AccountCreationController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtUsername;
    @FXML
    private TextField txtPassword;
    @FXML
    private TextField txtFirstname;
    @FXML
    private TextField txtLastname;
    @FXML
    private ComboBox cbxSex, cbxAccessLevel;
    @FXML
    private TextField txtEmail;
    @FXML
    private TextField txtTelephone;
    @FXML
    private TextField txtCellphone;
    @FXML
    private TextArea txtOther;// = new TextArea();
    private String[] access_levels = {"NONE", "STANDARD", "ADMIN", "SUPER"};

    @Override
    public void refreshView()
    {
        cbxAccessLevel.setItems(FXCollections.observableArrayList(access_levels));
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
    public void createAccountSubmit()
    {
        int sex_index = cbxSex.getSelectionModel().selectedIndexProperty().get();

        if(!validateFormField(txtUsername, "Invalid Username", "please enter a valid username", "^.*(?=.{5,}).*"))
            return;
        if(!validateFormField(txtPassword, "Invalid Password", "please enter a valid password", "^.*(?=.{8,}).*"))//(?=[a-zA-Z])(?=.*[0-9])(?=.*[@#!$%^&*-+=])
            return;
        if(!validateFormField(txtFirstname, "Invalid Firstname", "please enter a valid first name", "^.*(?=.{1,}).*"))
            return;
        if(!validateFormField(txtLastname, "Invalid Lastname", "please enter a valid last name", "^.*(?=.{1,}).*"))
            return;

        if(sex_index<0)
        {
            cbxSex.getStyleClass().remove("form-control-default");
            cbxSex.getStyleClass().add("control-input-error");
        }else{
            cbxSex.getStyleClass().remove("control-input-error");
            cbxSex.getStyleClass().add("form-control-default");
        }
        if(!validateFormField(txtEmail, "Invalid Email", "please enter a valid email address", "^.*(?=.{5,})(?=(.*@.*\\.)).*"))
            return;
        if(!validateFormField(txtTelephone, "Invalid Telephone Number", "please enter a valid telephone number", "^.*(?=.{10,}).*"))
            return;
        if(!validateFormField(txtCellphone, "Invalid Cellphone Number", "please enter a valid cellphone number", "^.*(?=.{10,}).*"))
            return;

        //all valid, send data to server
        int access_level_index = cbxAccessLevel.getSelectionModel().getSelectedIndex();
        if(access_level_index>=0)
        {
            Employee employee = new Employee();
            employee.setUsr(txtUsername.getText());
            try
            {
                String encrypted = IO.getEncryptedHexString(txtPassword.getText());
                employee.setPwd(encrypted);
            } catch (Exception e)
            {
                IO.logAndAlert("Error", "Could not get password hash: " + e.getMessage(), IO.TAG_ERROR);
            }
            employee.setFirstname(txtFirstname.getText());
            employee.setLastname(txtLastname.getText());
            employee.setGender(cbxSex.getItems().get(sex_index).toString());
            employee.setEmail(txtEmail.getText());
            employee.setTel(txtTelephone.getText());
            employee.setCell(txtCellphone.getText());
            if(txtOther.getText()!=null)
                if(!txtOther.getText().isEmpty())
                    employee.setOther(txtOther.getText());
            //set creator Employee if an Employee is logged in.
            if(SessionManager.getInstance().getActive()!=null)
                if(!SessionManager.getInstance().getActive().isExpired())
                    employee.setCreator(SessionManager.getInstance().getActive().getUsr());

            switch (access_level_index)//access_levels[access_level_index].toLowerCase()
            {
                case 0://none//AccessLevels.NO_ACCESS.getLevel()
                    employee.setAccessLevel(0);
                    break;
                case 1://normal
                    employee.setAccessLevel(1);
                    break;
                case 2://admin
                    employee.setAccessLevel(2);
                    break;
                case 3://super
                    employee.setAccessLevel(3);
                    break;
                default:
                    IO.logAndAlert("Error", "Unknown Employee access level ["+access_level_index+"]", IO.TAG_ERROR);
            }
            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
                HttpURLConnection connection = RemoteComms.putJSON("/employees", employee.toString(), headers);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.logAndAlert("Account Creation Success", IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                } else
                    IO.logAndAlert("Account Creation Failure", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

                connection.disconnect();
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        }
    }

    private boolean validateFormField(TextField txt, String errTitle, String errMsg, String regex)
    {
        if(!Validators.isValidNode(txt, txt.getText(), regex))
        {
            //IO.logAndAlert(errTitle, errMsg, IO.TAG_ERROR);
            IO.log(getClass().getName(), IO.TAG_ERROR, errMsg);
            return false;
        }
        return true;
    }
}
