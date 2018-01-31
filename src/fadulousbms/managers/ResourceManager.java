package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/13.
 */
public class ResourceManager extends BusinessObjectManager
{
    private HashMap<String, Resource> resources;//resources that have been approved/acquired/delivered
    private HashMap<String, Resource> all_resources;
    private Resource selected;
    private Gson gson;
    private static ResourceManager resource_manager = new ResourceManager();
    private HashMap<String, ResourceType> resource_types;
    public static final String TAG = "ResourceManager";
    public static final String ROOT_PATH = "cache/resources/";
    public String filename = "";
    private long timestamp;

    private ResourceManager()
    {
    }

    public static ResourceManager getInstance()
    {
        return resource_manager;
    }

    /**
     *
     * @return Approved Resource objects.
     */
    public HashMap<String, Resource> getResources()
    {
        return resources;
    }

    public HashMap<String, Resource> getAll_resources()
    {
        return all_resources;
    }

    public void setAll_resources(HashMap<String, Resource> all_resources)
    {
        this.all_resources = all_resources;
    }

    public void setSelected(Resource resource)
    {
        this.selected=resource;
    }

    public Resource getSelected()
    {
        return this.selected;
    }

    public HashMap<String, ResourceType> getResource_types()
    {
        return resource_types;
    }

    @Override
    public void initialize()
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        try
        {
            if(resources==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "clients object has already been set.");
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
    }

    public void reloadDataFromServer() throws ClassNotFoundException, IOException
    {
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                gson = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                //Get Timestamp
                String timestamp_json = RemoteComms
                        .sendGetRequest("/timestamp/resources_timestamp", headers);
                Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                if (cntr_timestamp != null)
                {
                    timestamp = cntr_timestamp.getCount();
                    filename = "resources_" + timestamp + ".dat";
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                }
                else
                {
                    IO.log(this.getClass().getName(), IO.TAG_ERROR, "could not get valid timestamp");
                    return;
                }

                if (!isSerialized(ROOT_PATH + filename))
                {
                    String resources_json = RemoteComms.sendGetRequest("/resources", headers);
                    ResourceServerObject resources_server_object = gson.fromJson(resources_json, ResourceServerObject.class);

                    if(resources_server_object!=null)
                    {
                        if(resources_server_object.get_embedded()!=null)
                        {
                            Resource[] resources_arr = resources_server_object.get_embedded().getResources();

                            resources = new HashMap();
                            all_resources = new HashMap();
                            if (resources_arr != null)
                            {
                                for (Resource res : resources_arr)
                                {
                                    all_resources.put(res.get_id(), res);
                                    if (res.getDate_acquired() > 0)
                                        resources.put(res.get_id(), res);
                                    else IO.log(getClass().getName(), IO.TAG_WARN, "material [" + res + "] has not been approved yet. [date_acquired not set]");
                                }
                            } else IO.log(getClass().getName(), IO.TAG_WARN, "no resources found in database.");
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Resources in database.");
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "ResourceServerObject (containing Resource objects & other metadata) is null");


                    String resource_types_json = RemoteComms.sendGetRequest("/resources/types", headers);
                    ResourceTypeServerObject resourceTypeServerObject = gson.fromJson(resource_types_json, ResourceTypeServerObject.class);
                    if(resourceTypeServerObject!=null)
                    {
                        if(resourceTypeServerObject.get_embedded()!=null)
                        {
                            ResourceType[] resource_types_arr = resourceTypeServerObject.get_embedded()
                                    .getResource_types();

                            resource_types = new HashMap<>();
                            for (ResourceType resource_type : resource_types_arr)
                                resource_types.put(resource_type.get_id(), resource_type);
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Resource Types in the database.");
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "ResourceTypeServerObject (containing ResourceType objects & other metadata) is null");

                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of materials.");

                    this.serialize(ROOT_PATH + filename, all_resources);
                    this.serialize(ROOT_PATH + "resource_types.dat", resource_types);
                } else
                {
                    IO.log(this.getClass()
                            .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                    all_resources = (HashMap<String, Resource>) this.deserialize(ROOT_PATH + filename);
                    resource_types = (HashMap<String, ResourceType>) this.deserialize(ROOT_PATH + "resource_types.dat");

                    resources = new HashMap<>();
                    if (all_resources != null)
                    {
                        for (Resource resource : all_resources.values())
                            if(resource.getDate_acquired() > 0)
                                resources.put(resource.get_id(), resource);
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "serialized materials are null.");
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void newResourceWindow(Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Material");
        stage.setWidth(520);
        stage.setMaxWidth(520);
        stage.setHeight(450);
        stage.setMaxHeight(450);
        stage.setResizable(false);
        stage.setAlwaysOnTop(true);
        stage.centerOnScreen();

        VBox vbox = new VBox(10);

        final TextField txt_resource_name = new TextField();
        txt_resource_name.setMinWidth(200);
        txt_resource_name.setMaxWidth(Double.MAX_VALUE);
        HBox resource_name = CustomTableViewControls.getLabelledNode("Material name", 200, txt_resource_name);

        final TextField txt_resource_description = new TextField();
        txt_resource_description.setMinWidth(200);
        txt_resource_description.setMaxWidth(Double.MAX_VALUE);
        HBox resource_description = CustomTableViewControls.getLabelledNode("Material description", 200, txt_resource_description);

        final TextField txt_resource_serial = new TextField();
        txt_resource_serial.setMinWidth(200);
        txt_resource_serial.setMaxWidth(Double.MAX_VALUE);
        HBox resource_serial = CustomTableViewControls.getLabelledNode("Material serial", 200, txt_resource_serial);

        final ComboBox<ResourceType> cbx_resource_type = new ComboBox<>();
        cbx_resource_type.setCellFactory(new Callback<ListView<ResourceType>, ListCell<ResourceType>>()
        {
            @Override
            public ListCell<ResourceType> call(ListView<ResourceType> lst_resource_types)
            {
                return new ListCell<ResourceType>()
                {
                    @Override
                    protected void updateItem(ResourceType resource_type, boolean empty)
                    {
                        super.updateItem(resource_type, empty);
                        if(resource_type!=null && !empty)
                        {
                            setText(resource_type.getType_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_resource_type.setButtonCell(new ListCell<ResourceType>()
        {
            @Override
            protected void updateItem(ResourceType resource_type, boolean empty)
            {
                super.updateItem(resource_type, empty);
                if(resource_type!=null && !empty)
                {
                    setText(resource_type.getType_name());
                }else{
                    setText("");
                }
            }
        });
        if(resource_types!=null)
            cbx_resource_type.setItems(FXCollections.observableArrayList(resource_types.values()));
        else IO.log(getClass().getName(), IO.TAG_ERROR, "material_types map is not set.");
        cbx_resource_type.setMinWidth(200);
        cbx_resource_type.setMinHeight(35);
        cbx_resource_type.setMaxWidth(Double.MAX_VALUE);

        Button btnNewType = new Button("New");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnNewType.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnNewType.getStyleClass().add("btnAdd");
        btnNewType.setMinWidth(70);
        btnNewType.setMinHeight(35);
        HBox.setHgrow(btnNewType, Priority.ALWAYS);
        HBox resource_type = CustomTableViewControls.getLabelledNode("Resource type", 200, new HBox(cbx_resource_type, btnNewType));
        btnNewType.setOnAction(event ->
        {
            //close material creation window
            stage.close();
            ResourceManager.getInstance().newResourceTypeWindow(param ->
            {
                //show material creation window again after material type has been created.
                newResourceWindow(callback);
                return null;
            });
        });

        final TextField txt_resource_value = new TextField();
        txt_resource_value.setMinWidth(200);
        txt_resource_value.setMaxWidth(Double.MAX_VALUE);
        HBox resource_value = CustomTableViewControls.getLabelledNode("Cost", 200, txt_resource_value);

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
        submit = CustomTableViewControls.getSpacedButton("Create Resource", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_resource_name, txt_resource_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_resource_description, txt_resource_description.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_resource_serial, txt_resource_serial.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_resource_type, cbx_resource_type.getValue()==null?"":cbx_resource_type.getValue().get_id(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_resource_value, txt_resource_value.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_quantity, txt_quantity.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_unit, txt_unit.getText(), 1, ".+"))
                return;
            //if(!Validators.isValidNode(dpk_date_acquired, dpk_date_acquired.getValue()==null?"":dpk_date_acquired.getValue().toString(), 4, date_regex))
            //    return;
            //if(!Validators.isValidNode(txt_account, txt_account.getText(), 1, ".+"))
            //    return;

            long date_acquired_in_sec, date_exhausted_in_sec=0;
            String str_resource_name = txt_resource_name.getText();
            String str_resource_description = txt_resource_description.getText();
            String str_resource_serial = txt_resource_serial.getText();
            String str_resource_type = cbx_resource_type.getValue().get_id();
            String str_resource_value = txt_resource_value.getText();
            String str_quantity = txt_quantity.getText();
            //date_acquired_in_sec = dpk_date_acquired.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            //if(dpk_date_exhausted.getValue()!=null)
            //    date_exhausted_in_sec = dpk_date_exhausted.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("resource_name", str_resource_name));
            params.add(new AbstractMap.SimpleEntry<>("resource_description", str_resource_description));
            params.add(new AbstractMap.SimpleEntry<>("resource_serial", str_resource_serial));
            params.add(new AbstractMap.SimpleEntry<>("resource_type", str_resource_type));
            params.add(new AbstractMap.SimpleEntry<>("resource_value", str_resource_value));
            params.add(new AbstractMap.SimpleEntry<>("quantity", str_quantity));
            params.add(new AbstractMap.SimpleEntry<>("unit", txt_unit.getText()));
            //params.add(new AbstractMap.SimpleEntry<>("date_acquired", String.valueOf(date_acquired_in_sec)));
            //params.add(new AbstractMap.SimpleEntry<>("account", txt_account.getText()));
            if(date_exhausted_in_sec>0)
                params.add(new AbstractMap.SimpleEntry<>("date_exhausted", String.valueOf(date_exhausted_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("extra", str_other));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                else
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/resource/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //close stage
                        stage.close();

                        IO.logAndAlert("Success", "Successfully created a new resource!", IO.TAG_INFO);
                        try
                        {
                            //refresh model & view when material has been created.
                            reloadDataFromServer();
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
                        if(callback!=null)
                            callback.call(null);
                    }else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    }
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(resource_name);
        vbox.getChildren().add(resource_description);
        vbox.getChildren().add(resource_serial);
        vbox.getChildren().add(resource_type);
        vbox.getChildren().add(resource_value);
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

    public void newResourceTypeWindow(Callback callback)
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
            return;
        }
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Material Type");
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
        HBox type_description = CustomTableViewControls.getLabelledNode("Material type description", 200, txt_type_description);

        final TextField txt_other = new TextField();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Create Material Type", event ->
        {
            if(!Validators.isValidNode(txt_type_name, txt_type_name.getText(), 1, "\\w+"))
            {
                IO.logAndAlert("Error", "Please make sure that the material type name doesn't have any spaces.", IO.TAG_ERROR);
                return;
            }

            String str_type_name = txt_type_name.getText();
            String str_type_description = txt_type_description.getText();
            String str_type_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            /*params.add(new AbstractMap.SimpleEntry<>("type_name", str_type_name));
            params.add(new AbstractMap.SimpleEntry<>("type_description", str_type_description));
            params.add(new AbstractMap.SimpleEntry<>("other", String.valueOf(str_type_other)));*/

            ResourceType resourceType = new ResourceType(str_type_name, str_type_description);
            resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
            resourceType.setOther(str_type_other);

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                else
                {
                    IO.logAndAlert("Session expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.putJSON("/resources/types", resourceType.getJSONString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added new material type!", IO.TAG_INFO);
                        try
                        {
                            //refresh model & view when material type has been created.
                            reloadDataFromServer();
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

                        if(callback!=null)
                            callback.call(null);
                        stage.close();
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
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

    class ResourceServerObject extends ServerObject
    {
        private ResourceServerObject.Embedded _embedded;

        ResourceServerObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(ResourceServerObject.Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private Resource[] resources;

            public Resource[] getResources()
            {
                return resources;
            }

            public void setResources(Resource[] resources)
            {
                this.resources = resources;
            }
        }
    }

    class ResourceTypeServerObject extends ServerObject
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
            private ResourceType[] resource_types;

            public ResourceType[] getResource_types()
            {
                return resource_types;
            }

            public void setResource_types(ResourceType[] resource_types)
            {
                this.resource_types = resource_types;
            }
        }
    }
}
