package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Client;
import fadulousbms.model.Screens;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewClientController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TextField txtName,txtTel,txtFax,txtWebsite, txtRegistration,txtVat;
    @FXML
    private CheckBox cbxActive;
    @FXML
    private DatePicker datePartnered;
    @FXML
    private TextArea txtPhysical,txtPostal;

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
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void createClient()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if(!Validators.isValidNode(txtName, txtName.getText(), 1, ".+"))
        {
            txtName.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtPhysical, txtPhysical.getText(), 1, ".+"))
        {
            txtPhysical.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtPostal, txtPostal.getText(), 1, ".+"))
        {
            txtPostal.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtWebsite, txtWebsite.getText(), 1, ".+"))
        {
            txtWebsite.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtRegistration, txtRegistration.getText(), 1, ".+"))
        {
            txtRegistration.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtFax, txtFax.getText(), 1, ".+"))
        {
            txtFax.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }

        //prepare client parameters
        Client client = new Client();
        client.setClient_name(txtName.getText());
        client.setPhysical_address(txtPhysical.getText());
        client.setPostal_address(txtPostal.getText());
        client.setTel(txtTel.getText());
        client.setFax(txtFax.getText());
        client.setDate_partnered(datePartnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        client.setWebsite(txtWebsite.getText());
        client.setRegistration(txtRegistration.getText());
        client.setVat(txtVat.getText());
        client.setActive(cbxActive.isSelected());

        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new supplier on database
            HttpURLConnection connection = RemoteComms.postData("/api/client/add", client.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created client["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Client Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Client Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    try
                    {
                        ClientManager.getInstance().reloadDataFromServer();
                        ClientManager.getInstance().setSelected(client);
                        IO.logAndAlert("New Client Creation Success", "Successfully created a new client.", IO.TAG_INFO);
                        itemsModified = false;
                    }catch (MalformedURLException ex)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                        IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
                    }catch (ClassNotFoundException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
                    }catch (IOException ex)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                        IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
                    }
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Generic Quote Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void back()
    {
        try
        {
            ScreenManager.getInstance().setPreviousScreen();
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
