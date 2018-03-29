package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
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
public class NewAssetController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TextField txtName,txtDescription,txtSerial,txtValue,txtUnit,txtQuantity;
    @FXML
    private DatePicker dateAcquired,dateExhausted;
    @FXML
    private ComboBox<AssetType> cbxAssetType;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        new Thread(() ->
                refreshModel(param ->
                {
                    if(AssetManager.getInstance().getDataset()!=null)
                        Platform.runLater(() -> refreshView());
                    return null;
                })).start();
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading asset creation view.");

        if(AssetManager.getInstance().getAsset_types()==null)
        {
            IO.logAndAlert(getClass().getName(), "no asset types found in database.", IO.TAG_ERROR);
            return;
        }

        cbxAssetType.setItems(FXCollections.observableArrayList(AssetManager.getInstance().getAsset_types().values()));
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading asset model's data-set.");

        AssetManager.getInstance().initialize();
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        AssetManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
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
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        if(!Validators.isValidNode(txtName, txtName.getText(), 1, ".+"))
        {
            txtName.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtDescription, txtDescription.getText(), 1, ".+"))
        {
            txtDescription.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtSerial, txtSerial.getText(), 1, ".+"))
        {
            txtSerial.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtValue, txtValue.getText(), 1, ".+"))
        {
            txtValue.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
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
            txtQuantity.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtUnit, txtUnit.getText(), 1, ".+"))
        {
            txtUnit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
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
        asset.setCreator(SessionManager.getInstance().getActive().getUsr());
        if(dateExhausted.getValue()!=null)
            asset.setDate_exhausted(dateExhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        //if(str_extra!=null)
        //    quote.setExtra(str_extra);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));

            //create new supplier on database
            HttpURLConnection connection = RemoteComms.put("/asset", asset.getJSONString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());

                    if(response==null)
                    {
                        IO.logAndAlert("Asset Creation Failure", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("Asset Creation Failure", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }

                    String new_asset_id = response.replaceAll("\"","");//strip inverted commas around asset_id
                    new_asset_id = new_asset_id.replaceAll("\n","");//strip new line chars
                    new_asset_id = new_asset_id.replaceAll(" ","");//strip whitespace chars

                    AssetManager.getInstance().setSelected(asset);
                    IO.logAndAlert("Asset Creation Success", "Successfully created new Asset ["+new_asset_id+"]", IO.TAG_INFO);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            } else IO.logAndAlert("Asset Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    public void createAssetType()
    {
        AssetManager.getInstance().createNewAssetType(param ->
        {
            new Thread(() ->
                    refreshModel(param1 ->
                    {
                        if(AssetManager.getInstance().getDataset()!=null)
                            Platform.runLater(() -> refreshView());
                        return null;
                    })).start();
            return null;
        });
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
