package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/02/01.
 */
public class AssetManager extends BusinessObjectManager
{
    private HashMap<String, Asset> assets;//assets that have been approved/acquired/delivered
    private HashMap<String, Asset> all_assets;
    private HashMap<String, AssetType> asset_types;
    private static AssetManager asset_manager = new AssetManager();
    private Gson gson;
    public static final String ROOT_PATH = "cache/assets/";
    public String filename = "";
    private long timestamp;
    public static final String TAG = "AssetManager";

    private AssetManager()
    {
    }

    @Override
    public void initialize()
    {
        loadDataFromServer();
    }

    public static AssetManager getInstance()
    {
        return asset_manager;
    }

    @Override
    public HashMap<String, Asset> getDataset()
    {
        return assets;
    }

    public HashMap<String, AssetType> getAsset_types()
    {
        return asset_types;
    }

    public HashMap<String, Asset> getAll_assets()
    {
        return all_assets;
    }

    //TODO: implement private void reloadDataFromServer()
    public void loadDataFromServer()
    {
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if (smgr.getActive() != null)
            {
                if (!smgr.getActive().isExpired())
                {
                    gson = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));

                    //Get Timestamp
                    String timestamp_json = RemoteComms.get("/timestamp/assets_timestamp", headers);
                    Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if(cntr_timestamp!=null)
                    {
                        timestamp = cntr_timestamp.getCount();
                        filename = "assets_"+timestamp+".dat";
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+timestamp);
                    } else
                    {
                        IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                        return;
                    }

                    if(!isSerialized(ROOT_PATH+filename) || !isSerialized(ROOT_PATH+"asset_types.dat"))
                    {
                        String assets_json = RemoteComms.get("/assets", headers);
                        AssetServerObject assetServerObject = (AssetServerObject) AssetManager.getInstance().parseJSONobject(assets_json, new AssetServerObject());
                        if(assetServerObject!=null)
                        {
                            if(assetServerObject.get_embedded()!=null)
                            {
                                Asset[] assets_arr = assetServerObject.get_embedded().getAssets();

                                assets = new HashMap<>();
                                for (Asset asset : assets_arr)
                                    assets.put(asset.get_id(), asset);
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Assets in database.");
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "AssetServerObject (containing Asset objects & other metadata) is null");

                        String asset_types_json = RemoteComms.get("/assets/types", headers);
                        AssetTypeServerObject assetTypeServerObject = (AssetTypeServerObject) AssetManager.getInstance().parseJSONobject(asset_types_json, new AssetTypeServerObject());
                        if(assetTypeServerObject!=null)
                        {
                            if(assetTypeServerObject.get_embedded()!=null)
                            {
                                AssetType[] asset_types_arr = assetTypeServerObject.get_embedded().getAsset_types();

                                asset_types = new HashMap<>();
                                for (AssetType assetType : asset_types_arr)
                                    asset_types.put(assetType.get_id(), assetType);
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Asset Types in the database.");
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "AssetTypeServerObject (containing AssetType objects & other metadata) is null");

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of assets.");

                        this.serialize(ROOT_PATH+filename, all_assets);
                        //delete asset_types.dat if it exists
                        try {
                            Files.delete(new File(ROOT_PATH + "asset_types.dat").toPath());
                        } catch (NoSuchFileException e) {
                            IO.log(getClass().getName(), IO.TAG_WARN, e.getMessage());
                        } catch (FileNotFoundException e) {
                            IO.log(getClass().getName(), IO.TAG_WARN, e.getMessage());
                        } finally {
                            this.serialize(ROOT_PATH+"asset_types.dat", asset_types);
                        }
                    }else
                    {
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        all_assets = (HashMap<String, Asset>) this.deserialize(ROOT_PATH+filename);
                        asset_types = (HashMap<String, AssetType>) this.deserialize(ROOT_PATH+"asset_types.dat");

                        assets = new HashMap<>();
                        if(all_assets!=null)
                        {
                            for (Asset asset : all_assets.values())
                                if (asset.getDate_acquired() > 0)
                                    assets.put(asset.get_id(), asset);
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "serialized assets are null.");
                    }
                } else
                {
                    IO.logAndAlert("Active session has expired.", "Session Expired", IO.TAG_ERROR);
                }
            } else
            {
                IO.logAndAlert("No active sessions.", "Session Expired", IO.TAG_ERROR);
            }
        }catch (JsonSyntaxException ex)
        {
            IO.logAndAlert("JsonSyntax Error", ex.getMessage(), IO.TAG_ERROR);
        }catch (MalformedURLException ex)
        {
            IO.logAndAlert("MalformedURL Error", ex.getMessage(), IO.TAG_ERROR);
        }catch (ClassNotFoundException e)
        {
            IO.logAndAlert("ClassNotFound Error", e.getMessage(), IO.TAG_ERROR);
        }catch (IOException ex)
        {
            IO.logAndAlert("IO Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    @Override
    Callback getSynchronisationCallback()
    {
        return null;
    }

    public void newAssetWindow(Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Asset");
        stage.setWidth(520);
        stage.setMaxWidth(520);
        stage.setHeight(450);
        stage.setMaxHeight(450);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.centerOnScreen();

        VBox vbox = new VBox(10);

        final TextField txt_asset_name = new TextField();
        txt_asset_name.setMinWidth(200);
        txt_asset_name.setMaxWidth(Double.MAX_VALUE);
        HBox asset_name = CustomTableViewControls.getLabelledNode("Asset name", 200, txt_asset_name);

        final TextField txt_asset_description = new TextField();
        txt_asset_description.setMinWidth(200);
        txt_asset_description.setMaxWidth(Double.MAX_VALUE);
        HBox asset_description = CustomTableViewControls.getLabelledNode("Asset description", 200, txt_asset_description);

        final TextField txt_asset_serial = new TextField();
        txt_asset_serial.setMinWidth(200);
        txt_asset_serial.setMaxWidth(Double.MAX_VALUE);
        HBox asset_serial = CustomTableViewControls.getLabelledNode("Asset serial", 200, txt_asset_serial);

        final ComboBox<AssetType> cbx_asset_type = new ComboBox<>();
        cbx_asset_type.setCellFactory(new Callback<ListView<AssetType>, ListCell<AssetType>>()
        {
            @Override
            public ListCell<AssetType> call(ListView<AssetType> lst_asset_types)
            {
                return new ListCell<AssetType>()
                {
                    @Override
                    protected void updateItem(AssetType asset_type, boolean empty)
                    {
                        super.updateItem(asset_type, empty);
                        if(asset_type!=null && !empty)
                        {
                            setText(asset_type.getType_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_asset_type.setButtonCell(new ListCell<AssetType>()
        {
            @Override
            protected void updateItem(AssetType asset_type, boolean empty)
            {
                super.updateItem(asset_type, empty);
                if(asset_type!=null && !empty)
                {
                    setText(asset_type.getType_name());
                }else{
                    setText("");
                }
            }
        });
        if(asset_types!=null)
            cbx_asset_type.setItems(FXCollections.observableArrayList(asset_types.values()));
        else IO.log(getClass().getName(), IO.TAG_ERROR, "asset_types map is not set.");
        cbx_asset_type.setMinWidth(200);
        cbx_asset_type.setMinHeight(35);
        cbx_asset_type.setMaxWidth(Double.MAX_VALUE);

        Button btnNewType = new Button("New");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnNewType.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnNewType.getStyleClass().add("btnAdd");
        btnNewType.setMinWidth(70);
        btnNewType.setMinHeight(35);
        HBox.setHgrow(btnNewType, Priority.ALWAYS);
        HBox asset_type = CustomTableViewControls.getLabelledNode("Asset type", 200, new HBox(cbx_asset_type, btnNewType));
        btnNewType.setOnAction(event ->
        {
            stage.close();
            AssetManager.getInstance().newAssetTypeWindow(param ->
            {
                loadDataFromServer();
                return null;
            });
        });

        final TextField txt_asset_value = new TextField();
        txt_asset_value.setMinWidth(200);
        txt_asset_value.setMaxWidth(Double.MAX_VALUE);
        HBox asset_value = CustomTableViewControls.getLabelledNode("Cost ["+Globals.CURRENCY_SYMBOL.getValue()+"]", 200, txt_asset_value);

        final TextField txt_quantity = new TextField();
        txt_quantity.setMinWidth(200);
        txt_quantity.setMaxWidth(Double.MAX_VALUE);
        HBox quantity = CustomTableViewControls.getLabelledNode("Quantity", 200, txt_quantity);

        final TextField txt_unit = new TextField();
        txt_unit.setMinWidth(200);
        txt_unit.setMaxWidth(Double.MAX_VALUE);
        HBox unit = CustomTableViewControls.getLabelledNode("Unit", 200, txt_unit);

        /*DatePicker dpk_date_acquired = new DatePicker();
        dpk_date_acquired.setMinWidth(200);
        dpk_date_acquired.setMaxWidth(Double.MAX_VALUE);
        HBox date_acquired = CustomTableViewControls.getLabelledNode("Date acquired", 200, dpk_date_acquired);

        DatePicker dpk_date_exhausted = new DatePicker();
        dpk_date_exhausted.setMinWidth(200);
        dpk_date_exhausted.setMaxWidth(Double.MAX_VALUE);
        HBox date_exhausted = CustomTableViewControls.getLabelledNode("Date exhausted", 200, dpk_date_exhausted);

        final TextField txt_account = new TextField();
        txt_account.setMinWidth(200);
        txt_account.setMaxWidth(Double.MAX_VALUE);
        HBox account = CustomTableViewControls.getLabelledNode("Account", 200, txt_account);*/

        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Create Asset", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_asset_name, txt_asset_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_asset_description, txt_asset_description.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_asset_serial, txt_asset_serial.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_asset_type, cbx_asset_type.getValue()==null?"":cbx_asset_type.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_asset_value, txt_asset_value.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_quantity, txt_quantity.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_unit, txt_unit.getText(), 1, ".+"))
                return;
            //if(!Validators.isValidNode(dpk_date_acquired, dpk_date_acquired.getValue()==null?"":dpk_date_acquired.getValue().toString(), 4, date_regex))
            //    return;
            //if(!Validators.isValidNode(txt_account, txt_account.getText(), 1, ".+"))
            //    return;

            //long date_acquired_in_sec, date_exhausted_in_sec=0;
            String str_resource_name = txt_asset_name.getText();
            String str_resource_description = txt_asset_description.getText();
            String str_resource_serial = txt_asset_serial.getText();
            String str_resource_type = cbx_asset_type.getValue().get_id();
            String str_resource_value = txt_asset_value.getText();
            String str_quantity = txt_quantity.getText();
            //date_acquired_in_sec = dpk_date_acquired.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            //if(dpk_date_exhausted.getValue()!=null)
            //    date_exhausted_in_sec = dpk_date_exhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("asset_name", str_resource_name));
            params.add(new AbstractMap.SimpleEntry<>("asset_description", str_resource_description));
            params.add(new AbstractMap.SimpleEntry<>("asset_serial", str_resource_serial));
            params.add(new AbstractMap.SimpleEntry<>("asset_type", str_resource_type));
            params.add(new AbstractMap.SimpleEntry<>("asset_value", str_resource_value));
            params.add(new AbstractMap.SimpleEntry<>("quantity", str_quantity));
            params.add(new AbstractMap.SimpleEntry<>("unit", txt_unit.getText()));
            //params.add(new AbstractMap.SimpleEntry<>("date_acquired", String.valueOf(date_acquired_in_sec)));
            //params.add(new AbstractMap.SimpleEntry<>("account", txt_account.getText()));
            //if(date_exhausted_in_sec>0)
            //    params.add(new AbstractMap.SimpleEntry<>("date_exhausted", String.valueOf(date_exhausted_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("extra", str_other));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();

                //TODO:
                HttpURLConnection connection = RemoteComms.post("/asset", "", headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added a new asset!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    }
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "Could not get a valid response from the server.");
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(asset_name);
        vbox.getChildren().add(asset_description);
        vbox.getChildren().add(asset_serial);
        vbox.getChildren().add(asset_type);
        vbox.getChildren().add(asset_value);
        vbox.getChildren().add(quantity);
        vbox.getChildren().add(unit);
        //vbox.getChildren().add(date_acquired);
        //vbox.getChildren().add(date_exhausted);
        //vbox.getChildren().add(account);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display
        Scene scene = new Scene(vbox);
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
    }

    public void newAssetTypeWindow(Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Asset Type");
        stage.setWidth(520);
        stage.setMaxWidth(520);
        stage.setHeight(230);
        stage.setMaxHeight(230);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.centerOnScreen();

        VBox vbox = new VBox(10);

        final TextField txt_type_name = new TextField();
        txt_type_name.setMinWidth(200);
        txt_type_name.setMaxWidth(Double.MAX_VALUE);
        HBox type_name = CustomTableViewControls.getLabelledNode("Type name", 200, txt_type_name);

        final TextField txt_type_description = new TextField();
        txt_type_description.setMinWidth(200);
        txt_type_description.setMaxWidth(Double.MAX_VALUE);
        HBox type_description = CustomTableViewControls.getLabelledNode("Asset type description", 200, txt_type_description);

        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Create Asset Type", event ->
        {
            if(!Validators.isValidNode(txt_type_name, txt_type_name.getText(), 1, "\\w+"))
            {
                IO.logAndAlert("Error", "Invalid type name, please make sure that the asset type name doesn't have any spaces.", IO.TAG_ERROR);
                return;
            }

            String str_type_name = txt_type_name.getText();
            String str_type_description = txt_type_description.getText();
            String str_type_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("type_name", str_type_name));
            params.add(new AbstractMap.SimpleEntry<>("type_description", str_type_description));
            params.add(new AbstractMap.SimpleEntry<>("other", String.valueOf(str_type_other)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();

                //TODO:
                HttpURLConnection connection = RemoteComms.post("/asset/type", "", headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added new asset type!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "Could not get a valid response from the server.");
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }

        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(type_name);
        vbox.getChildren().add(type_description);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
    }

    public void createNewAssetType(Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Asset Type");
        stage.setMinWidth(320);
        stage.setMinHeight(200);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_type_name = new TextField();
        txt_type_name.setMinWidth(200);
        txt_type_name.setMaxWidth(Double.MAX_VALUE);
        HBox type_name = CustomTableViewControls.getLabelledNode("Asset Type Name", 200, txt_type_name);

        final TextField txt_type_description = new TextField();
        txt_type_description.setMinWidth(200);
        txt_type_description.setMaxWidth(Double.MAX_VALUE);
        HBox type_description = CustomTableViewControls.getLabelledNode("Asset Type Description", 200, txt_type_description);

        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(!Validators.isValidNode(txt_type_name, txt_type_name.getText(), 1, "\\w+"))
            {
                IO.logAndAlert("Invalid Asset Type Name", "Please enter a valid type name.", IO.TAG_ERROR);
                return;
            }
            if(!Validators.isValidNode(txt_type_description, txt_type_description.getText(), 1, ""))
            {
                IO.logAndAlert("Invalid Asset Type Description", "Please enter a valid type description.", IO.TAG_ERROR);
                return;
            }

            String str_type_name = txt_type_name.getText();
            String str_type_description = txt_type_description.getText();
            String str_type_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("type_name", str_type_name));
            params.add(new AbstractMap.SimpleEntry<>("type_description", str_type_description));
            params.add(new AbstractMap.SimpleEntry<>("other", String.valueOf(str_type_other)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();

                //TODO:
                HttpURLConnection connection = RemoteComms.put("/asset/type", "", headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added new asset type! [" + IO
                                .readStream(connection.getInputStream()) + "]", IO.TAG_INFO);
                        //execute callback w/ args
                        if(callback!=null)
                            callback.call(IO.readStream(connection.getInputStream()));
                        return;
                    }
                    else
                        IO.logAndAlert("Error "+ connection.getResponseCode(), IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "Could not get a valid response from the server.");
            } catch (IOException e)
            {
                IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                e.printStackTrace();
            }
            //execute callback w/o args
            if(callback!=null)
                callback.call(null);
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(type_name);
        vbox.getChildren().add(type_description);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    class AssetServerObject extends ServerObject
    {
        private Embedded _embedded;

        Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private Asset[] assets;

            public Asset[] getAssets()
            {
                return assets;
            }

            public void setAssets(Asset[] assets)
            {
                this.assets = assets;
            }
        }
    }

    class AssetTypeServerObject extends ServerObject
    {
        private Embedded _embedded;

        Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private AssetType[] asset_types;

            public AssetType[] getAsset_types()
            {
                return asset_types;
            }

            public void setAsset_types(AssetType[] asset_types)
            {
                this.asset_types = asset_types;
            }
        }
    }
}
