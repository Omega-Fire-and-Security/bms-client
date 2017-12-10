package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewSupplierController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TextField txtName,txtSpeciality,txtTel,txtFax,txtEmail,txtWebsite,txtRegistration,txtVat,txtAccount;
    @FXML
    private CheckBox cbxActive;
    @FXML
    private DatePicker datePartnered;
    @FXML
    private TextArea txtPhysical,txtPostal;

    @Override
    public void refreshView()
    {
        txtName.textProperty().addListener((observable, oldValue, newValue) ->
        {
            String account_name = newValue.replaceAll(" ","-");
            txtAccount.setText(account_name.toLowerCase());
        });
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
    public void createSupplier()
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
        if(!Validators.isValidNode(txtName, txtName.getText(), 1, ".+"))
        {
            txtName.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtPhysical, txtPhysical.getText(), 1, ".+"))
        {
            txtPhysical.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtPostal, txtPostal.getText(), 1, ".+"))
        {
            txtPostal.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtFax, txtFax.getText(), 1, ".+"))
        {
            txtFax.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
        {
            txtEmail.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtSpeciality, txtSpeciality.getText(), 1, ".+"))
        {
            txtSpeciality.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtRegistration, txtRegistration.getText(), 1, ".+"))
        {
            txtRegistration.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(datePartnered, datePartnered.getValue()==null?"":datePartnered.getValue().toString(), 4, date_regex))
        {
            datePartnered.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtWebsite, txtWebsite.getText(), 1, ".+"))
        {
            txtWebsite.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            return;
        }

        //prepare supplier parameters
        Supplier supplier = new Supplier();
        supplier.setSupplier_name(txtName.getText());
        supplier.setPhysical_address(txtPhysical.getText());
        supplier.setPostal_address(txtPostal.getText());
        supplier.setTel(txtTel.getText());
        supplier.setFax(txtFax.getText());
        supplier.setContact_email(txtEmail.getText());
        supplier.setSpeciality(txtSpeciality.getText());
        supplier.setRegistration(txtRegistration.getText());
        supplier.setVat(txtVat.getText());
        supplier.setAccount_name(txtAccount.getText());
        supplier.setDate_partnered(datePartnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        supplier.setWebsite(txtWebsite.getText());
        supplier.setActive(cbxActive.isSelected());

        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new supplier on database
            HttpURLConnection connection = RemoteComms.postData("/api/supplier/add", supplier.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created supplier["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Supplier Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Supplier Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    IO.logAndAlert("New Supplier Creation Success", "Successfully created a new supplier ["+txtName.getText()+"].", IO.TAG_INFO);
                    SupplierManager.getInstance().initialize();
                    SupplierManager.getInstance().setSelected(supplier);
                    itemsModified = false;
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
