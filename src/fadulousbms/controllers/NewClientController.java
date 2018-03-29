package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Client;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
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
    private TextField txtName,txtTel,txtFax,txtEmail,txtWebsite, txtRegistration,txtVat, txtAccount;
    @FXML
    private CheckBox cbxActive;
    @FXML
    private DatePicker datePartnered;
    @FXML
    private TextArea txtPhysical,txtPostal;

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
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading client creation view.");

        txtName.textProperty().addListener((observable, oldValue, newValue) ->
        {
            String account_name = newValue.replaceAll(" ","-");
            txtAccount.setText(account_name.toLowerCase());
        });
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients data-set.");
        ClientManager.getInstance().initialize();
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        ClientManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void createClient()
    {
        //String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";
        String date_regex=".+";

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
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        if(!Validators.isValidNode(txtName, txtName.getText(), 1, ".+"))
        {
            txtName.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtPhysical, txtPhysical.getText(), 1, ".+"))
        {
            txtPhysical.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtPostal, txtPostal.getText(), 1, ".+"))
        {
            txtPostal.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtFax, txtFax.getText(), 1, ".+"))
        {
            txtFax.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
        {
            txtEmail.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtRegistration, txtRegistration.getText(), 1, ".+"))
        {
            txtRegistration.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(datePartnered, datePartnered.getValue()==null?"":datePartnered.getValue().toString(), 4, date_regex))
        {
            datePartnered.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtWebsite, txtWebsite.getText(), 1, ".+"))
        {
            txtWebsite.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }

        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error", "Invalid session. Please log in.", IO.TAG_ERROR);
            return;
        }

        //prepare client parameters
        Client client = new Client();
        client.setClient_name(txtName.getText());
        client.setPhysical_address(txtPhysical.getText());
        client.setPostal_address(txtPostal.getText());
        client.setTel(txtTel.getText());
        client.setFax(txtFax.getText());
        client.setContact_email(txtEmail.getText());
        client.setDate_partnered(datePartnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        client.setWebsite(txtWebsite.getText());
        client.setRegistration_number(txtRegistration.getText());
        client.setVat_number(txtVat.getText());
        client.setAccount_name(txtAccount.getText());
        client.setActive(cbxActive.isSelected());
        client.setCreator(SessionManager.getInstance().getActive().getUsr());

        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));

            //create new supplier on database
            HttpURLConnection connection = RemoteComms.put("/client", client.getJSONString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());

                    if(response==null)
                    {
                        IO.logAndAlert("Client Creation Failure", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("Client Creation Failure", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    IO.logAndAlert("Client Creation Success", "Successfully created new Client ["+client.getClient_name()+"]", IO.TAG_INFO);

                    String new_client_id = response.replaceAll("\"","");//strip inverted commas around client_id
                    new_client_id = new_client_id.replaceAll("\n","");//strip new line chars
                    new_client_id = new_client_id.replaceAll(" ","");//strip whitespace chars

                    ClientManager.getInstance().forceSynchronise();

                    ClientManager.getInstance().setSelected(client);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            } else IO.logAndAlert("New Generic Quote Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void back()
    {
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.OPERATIONS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            ScreenManager.getInstance().setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
    }
}
