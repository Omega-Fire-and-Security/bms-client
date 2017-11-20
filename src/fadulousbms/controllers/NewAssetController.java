package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

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
public class NewAssetController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TextField txtName,txtDescription,txtSerial,txtValue,txtUnit,txtQuantity;
    @FXML
    private DatePicker dateAcquired,dateExhausted;
    @FXML
    private ComboBox<AssetType> cbxAssetType;

    @Override
    public void refreshView()
    {
        if(AssetManager.getInstance().getAsset_types()==null)
        {
            IO.logAndAlert(getClass().getName(), "no asset types found in database.", IO.TAG_ERROR);
            return;
        }

        cbxAssetType.setItems(FXCollections.observableArrayList(AssetManager.getInstance().getAsset_types().values()));
    }

    @Override
    public void refreshModel()
    {
        AssetManager.getInstance().initialize();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        new Thread(() ->
        {
            refreshModel();
            if(PurchaseOrderManager.getInstance().getPurchaseOrders()!=null)
                Platform.runLater(() -> refreshView());
        }).start();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void createAsset()
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
        if(!Validators.isValidNode(txtDescription, txtDescription.getText(), 1, ".+"))
        {
            txtDescription.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtSerial, txtSerial.getText(), 1, ".+"))
        {
            txtSerial.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtValue, txtValue.getText(), 1, ".+"))
        {
            txtValue.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(cbxAssetType.getSelectionModel().getSelectedItem()==null)
        {
            //cbxResourceType.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            IO.logAndAlert("Validation Error", "Please select a valid asset type.", IO.TAG_ERROR);
            return;
        }
        if(dateAcquired.getValue()==null)
        {
            //dateAcquired.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            IO.logAndAlert("Validation Error", "Please choose a valid acquisition date.", IO.TAG_ERROR);
            return;
        }
        //TODO: date must be present/past not future?
        /*if(dateAcquired.getValue())
        {
            //dateAcquired.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            IO.logAndAlert("Validation Error", "Please choose a valid acquisition date.", IO.TAG_ERROR);
            return;
        }*/
        if(!Validators.isValidNode(txtQuantity, txtQuantity.getText(), 1, ".+"))
        {
            txtQuantity.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtUnit, txtUnit.getText(), 1, ".+"))
        {
            txtUnit.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }

        //prepare supplier parameters
        Asset asset = new Asset();
        asset.setAsset_name(txtName.getText());
        asset.setAsset_description(txtDescription.getText());
        asset.setAsset_serial(txtSerial.getText());
        asset.setAsset_value(Double.valueOf(txtValue.getText()));
        asset.setAsset_type(cbxAssetType.getSelectionModel().getSelectedItem().get_id());
        asset.setUnit(txtUnit.getText());
        asset.setQuantity(Long.valueOf(txtQuantity.getText()));
        asset.setDate_acquired(dateAcquired.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        if(dateExhausted.getValue()!=null)
            asset.setDate_exhausted(dateExhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new supplier on database
            HttpURLConnection connection = RemoteComms.postData("/api/asset/add", asset.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created asset["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Asset Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Asset Creation Failure", "Invalid response.", IO.TAG_ERROR);
                        return;
                    }
                    AssetManager.getInstance().setSelected(asset);
                    IO.logAndAlert("New Asset Creation Success", "Successfully created a new asset.", IO.TAG_INFO);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            } else IO.logAndAlert("New Asset Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void createAssetType()
    {
        AssetManager.getInstance().createNewAssetType(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                if(PurchaseOrderManager.getInstance().getPurchaseOrders()!=null)
                    Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }
}
