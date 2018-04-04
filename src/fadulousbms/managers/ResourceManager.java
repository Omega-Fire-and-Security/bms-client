package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/13.
 */
public class ResourceManager extends ApplicationObjectManager
{
    private HashMap<String, Resource> resources;
    private HashMap<String, Resource> acquired_resources;//resources that have been approved/acquired/delivered
    private HashMap<String, ResourceType> resource_types;
    private ResourceType selectedResourceType;
    private Gson gson;
    private static ResourceManager resource_manager = new ResourceManager();
    public static final String TAG = "ResourceManager";
    public static final String ROOT_PATH = "cache/resources/";
    public String filename = "";
    private long timestamp;

    private ResourceManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static ResourceManager getInstance()
    {
        return resource_manager;
    }


    @Override
    public HashMap<String, Resource> getDataset()
    {
        return resources;
    }

    @Override
    public Resource getSelected()
    {
        return (Resource) super.getSelected();
    }

    public ResourceType getSelectedResourceType()
    {
        return selectedResourceType;
    }

    public void setSelectedResourceType(ResourceType resourceType)
    {
        this.selectedResourceType = resourceType;
    }

    /**
     *
     * @return Approved Resource objects.
     */
    public HashMap<String, Resource> getApproved_resources()
    {
        return acquired_resources;
    }

    public HashMap<String, ResourceType> getResource_types()
    {
        return resource_types;
    }

    @Override
    Callback getSynchronisationCallback()
    {
        return new Callback()
        {
            @Override
            public Object call(Object param)
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
                            String timestamp_json = RemoteComms
                                    .get("/timestamp/resources_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "resources_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            }
                            else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            //load resources and resource types if they are not on local disk
                            if (!isSerialized(ROOT_PATH + filename) || !isSerialized(ROOT_PATH + "resource_types.dat"))
                            {
                                String resources_json = RemoteComms.get("/resources", headers);
                                ResourceServerResponseObject resources_server_object = (ResourceServerResponseObject) ResourceManager.getInstance().parseJSONobject(resources_json, new ResourceServerResponseObject());

                                if(resources_server_object!=null)
                                {
                                    if(resources_server_object.get_embedded()!=null)
                                    {
                                        Resource[] resources_arr = resources_server_object.get_embedded().getResources();

                                        resources = new HashMap();
                                        acquired_resources = new HashMap();
                                        if (resources_arr != null)
                                        {
                                            for (Resource res : resources_arr)
                                            {
                                                resources.put(res.get_id(), res);
                                                if (res.getDate_acquired() > 0)
                                                    acquired_resources.put(res.get_id(), res);
                                                else IO.log(getClass().getName(), IO.TAG_WARN, "material [" + res + "] has not been approved yet. [date_acquired not set]");
                                            }
                                        } else IO.log(getClass().getName(), IO.TAG_WARN, "no resources found in database.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Resources in database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ResourceServerResponseObject (containing Resource objects & other metadata) is null");


                                String resource_types_json = RemoteComms.get("/resources/types", headers);
                                ResourceTypeServerResponseObject resourceTypeServerObject = (ResourceTypeServerResponseObject) ResourceManager.getInstance().parseJSONobject(resource_types_json, new ResourceTypeServerResponseObject());
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
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ResourceTypeServerResponseObject (containing ResourceType objects & other metadata) is null");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of materials.");

                                serialize(ROOT_PATH + filename, resources);
                                //delete resource_types.dat if it exists
                                try {
                                    Files.delete(new File(ROOT_PATH + "resource_types.dat").toPath());
                                } catch (NoSuchFileException e) {
                                    IO.log(getClass().getName(), IO.TAG_WARN, e.getMessage());
                                } catch (FileNotFoundException e) {
                                    IO.log(getClass().getName(), IO.TAG_WARN, e.getMessage());
                                } finally {
                                    serialize(ROOT_PATH + "resource_types.dat", resource_types);
                                }
                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                resources = (HashMap<String, Resource>) deserialize(ROOT_PATH + filename);
                                resource_types = (HashMap<String, ResourceType>) deserialize(ROOT_PATH + "resource_types.dat");

                                acquired_resources = new HashMap<>();
                                if (resources != null)
                                {
                                    for (Resource resource : resources.values())
                                        if(resource.getDate_acquired() > 0)
                                            acquired_resources.put(resource.get_id(), resource);
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "serialized materials are null.");
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                } catch (ClassNotFoundException e)
                {
                    e.printStackTrace();
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (IOException e)
                {
                    e.printStackTrace();
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
                return null;
            }
        };
    }

    @Override
    protected void synchroniseDataset()
    {
        /**
         * Overridden because this manager needs to check if a second internal data-set[resource_types] has been set or not.
         * The super version of this method only checks the primary data-set.
         */
        boolean dataset_empty;
        if(getDataset()==null || getResource_types()==null)
            dataset_empty=true;
        else dataset_empty=getDataset().isEmpty();

        if(dataset_empty)//TODO: improve data set timestamp checks before synchronization
            if(getRefresh_lock()<=0)//if there's no other thread synchronising the data-set, synchronise the data-set
                reloadDataFromServer(getSynchronisationCallback());
            else IO.log(getClass().getName(), IO.TAG_WARN, "can't synchronize "+getClass().getSimpleName()+" model's data-set, thread started at ["+getRefresh_lock()+"] is still busy.");
        else IO.log(getClass().getName(), IO.TAG_WARN, getClass().getSimpleName()+" model's data-set has already been set, not synchronizing.");
    }

    public void newResourcePopOver(Node parent, Callback callback)
    {
        setSelected(null);
        setSelectedResourceType(null);

        TextField txt_mat_description = new TextField("");
        txt_mat_description.setMinWidth(120);
        txt_mat_description.setPromptText("Summary of material");
        Label lbl_des = new Label("Material Description*: ");
        lbl_des.setMinWidth(160);

        TextField txt_mat_category = new TextField("");
        txt_mat_category.setMinWidth(120);
        txt_mat_category.setPromptText("Material type e.g. Access Control Hardware");
        Label lbl_cat = new Label("Material Category*: ");
        lbl_cat.setMinWidth(160);

        TextField txt_mat_value = new TextField("");
        txt_mat_value.setMinWidth(120);
        txt_mat_value.setPromptText("Material cost excl. tax");
        Label lbl_val = new Label("Material Value*: ");
        lbl_val.setMinWidth(160);

        TextField txt_mat_unit = new TextField("");
        txt_mat_unit.setMinWidth(120);
        txt_mat_unit.setPromptText("Unit of measurement");
        Label lbl_unit = new Label("Material Unit*: ");
        lbl_unit.setMinWidth(160);

        Button btnSubmit = new Button("Create & Add Material");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(35);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(lbl_des, 0, 0);
        page.add(txt_mat_description, 1, 0);

        page.add(lbl_cat, 0, 1);
        page.add(txt_mat_category, 1, 1);

        page.add(lbl_val, 0, 2);
        page.add(txt_mat_value, 1, 2);

        page.add(lbl_unit, 0, 3);
        page.add(txt_mat_unit, 1, 3);

        page.add(btnSubmit, 0, 4);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create & Add Material");
        popover.setDetached(true);
        popover.show(parent);

        TextFields.bindAutoCompletion(txt_mat_category, ResourceManager.getInstance().getResource_types().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    setSelectedResourceType(event.getCompletion());
                }
            }
        });

        TextFields.bindAutoCompletion(txt_mat_description, ResourceManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    //update selected material
                    setSelected(event.getCompletion());

                    IO.log(getClass().getName(), IO.TAG_INFO, "auto-completed material: " + getSelected().getResource_description());
                    txt_mat_description.setText(getSelected().getResource_description());

                    if(ResourceManager.getInstance().getResource_types()!=null && getSelected().getResource_type()!=null)
                    {
                        setSelectedResourceType(ResourceManager.getInstance().getResource_types().get(getSelected().getResource_type()));
                        txt_mat_category.setText(getSelectedResourceType().getType_name());
                    }
                    txt_mat_value.setText(String.valueOf(getSelected().getResource_value()));
                    txt_mat_unit.setText(getSelected().getUnit());
                }
            }
        });

        btnSubmit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
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

                if(!Validators.isValidNode(txt_mat_description, txt_mat_description.getText(), 1, ".+"))
                {
                    txt_mat_description.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }

                if(txt_mat_category.getText()==null)
                {
                    IO.logAndAlert("Error", "Invalid material category.\nPlease enter a valid value.", IO.TAG_WARN);
                    return;
                }

                if(txt_mat_category.getText().isEmpty())
                {
                    IO.logAndAlert("Error", "Invalid material category.\nPlease enter a valid value.", IO.TAG_WARN);
                    return;
                }

                if(!Validators.isValidNode(txt_mat_value, txt_mat_value.getText(), 1, ".+"))
                {
                    txt_mat_value.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }
                if(!Validators.isValidNode(txt_mat_unit, txt_mat_unit.getText(), 1, ".+"))
                {
                    txt_mat_unit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }

                String resource_type_id = null;

                if(getSelectedResourceType()!=null)
                {
                    /*
                        If category text is not exactly the same as the category text inputted in the material creation
                        Form then create new category/material type.
                     */
                    if(getSelectedResourceType().getType_name().equals(txt_mat_category.getText()))
                        resource_type_id = getSelectedResourceType().get_id();
                }

                Resource resource = new Resource();
                resource.setResource_description(txt_mat_description.getText());
                resource.setUnit(txt_mat_unit.getText());
                resource.setQuantity(Long.valueOf(1));
                resource.setDate_acquired(System.currentTimeMillis());
                resource.setCreator(SessionManager.getInstance().getActive().getUsr());
                try
                {
                    resource.setResource_value(Double.valueOf(txt_mat_value.getText()));
                } catch (NumberFormatException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    return;
                }
                /*
                    If selected_material_type is null then create new material type/category using inputted
                    Text from material creation form
                 */
                if(resource_type_id==null)
                {
                    //create new resource type/category
                    ResourceType resourceType = new ResourceType(txt_mat_category.getText(), "");
                    resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
                    try
                    {
                        ResourceManager.getInstance().putObject(resourceType, material_category_id ->
                        {
                            if(material_category_id!=null)
                            {
                                ResourceManager.getInstance().setSelected(ResourceManager.getInstance().getResource_types().get(material_category_id));

                                resource.setResource_type((String) material_category_id);

                                //create new material using new category
                                createMaterial(resource, callback);
                            } else IO.logAndAlert("Error", "Could not create material category ["+txt_mat_category.getText()+"]", IO.TAG_ERROR);
                            return null;
                        });
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                } else //new material not in new category
                {
                    //create new material using selected category
                    resource.setResource_type(resource_type_id);
                    createMaterial(resource, callback);
                }
            }
        });
    }

    /**
     * Method that creates a new material record on the database but first checks if it's
     * Description is not the same as the currently selected material's description.
     * @param resource the Resource object to be created.
     * @param callback the Callback to be executed on post creation of the Resource object.
     */
    private void createMaterial(Resource resource, Callback callback)
    {
        if(resource==null)
        {
            IO.logAndAlert("Error", "Resource to be created is invalid", IO.TAG_ERROR);
            //execute callback w/o args
            if(callback!=null)
                callback.call(null);
            return;
        }

        try
        {
            String proceed = IO.OK;
            if(ResourceManager.getInstance().getSelected()!=null)
                if(resource.getResource_description().equals(ResourceManager.getInstance().getSelected().getResource_description()))
                    proceed = IO.showConfirm("Duplicate material found, proceed?", "New material's description is the same as an existing material, continue with creation of material?");

            if(proceed.equals(IO.OK))
            {
                ResourceManager.getInstance().putObject(resource, new_res_id ->
                {
                    if(new_res_id!=null)
                    {
                        //update selected material
                        ResourceManager.getInstance().setSelected(ResourceManager.getInstance().getDataset().get(new_res_id));

                        //execute callback w/ args
                        if (callback != null)
                            callback.call(new_res_id);
                    } else
                    {
                        //execute callback w/o args
                        if(callback!=null)
                            callback.call(null);
                    }
                    return null;
                });
            } else IO.log(getClass().getName(), IO.TAG_VERBOSE, "cancelled new material creation.");
        } catch (IOException e)
        {
            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
            e.printStackTrace();
        }
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
                } else
                {
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

                /*HttpURLConnection connection = RemoteComms.post("/api/resource/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //close stage
                        stage.close();

                        IO.logAndAlert("Success", "Successfully created a new resource!", IO.TAG_INFO);
                        //refresh model & view when material has been created.
                        forceSynchronise();

                        if(callback!=null)
                            callback.call(null);
                    } else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    }
                }*/
                throw new NotImplementedException();
            } catch (Exception e)
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
                forceSynchronise());

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

            ResourceType resourceType = new ResourceType(str_type_name, str_type_description);
            resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
            resourceType.setOther(str_type_other);

            try
            {
                ResourceManager.getInstance().putObject(resourceType, callback);
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
                forceSynchronise());

        stage.setScene(scene);
        stage.show();
    }

    class ResourceServerResponseObject extends ServerResponseObject
    {
        private ResourceServerResponseObject.Embedded _embedded;

        ResourceServerResponseObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(ResourceServerResponseObject.Embedded _embedded)
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

    class ResourceTypeServerResponseObject extends ServerResponseObject
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
