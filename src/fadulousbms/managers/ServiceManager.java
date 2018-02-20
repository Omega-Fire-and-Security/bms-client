package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
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
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/13.
 */
public class ServiceManager extends BusinessObjectManager
{
    private HashMap<String, Service> services;//resources that have been approved/acquired/delivered
    private HashMap<String, ServiceItem> service_items;
    private Gson gson;
    private static ServiceManager service_manager = new ServiceManager();
    public static final String TAG = "ServiceManager";
    public static final String ROOT_PATH = "cache/services/";
    public String filename = "";
    private long timestamp;

    private ServiceManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static ServiceManager getInstance()
    {
        return service_manager;
    }

    /**
     *
     * @return Approved Resource objects.
     */
    @Override
    public HashMap<String, Service> getDataset()
    {
        return services;
    }

    public HashMap<String, ServiceItem> getService_items()
    {
        return service_items;
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms
                                    .sendGetRequest("/timestamp/services_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "services_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp for services data-set.");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                //load services
                                String resources_json = RemoteComms.sendGetRequest("/services", headers);
                                ServiceServerObject services_server_object = gson.fromJson(resources_json, ServiceServerObject.class);

                                if(services_server_object!=null)
                                {
                                    if(services_server_object.get_embedded()!=null)
                                    {
                                        Service[] services_arr = services_server_object.get_embedded().getServices();

                                        services = new HashMap();
                                        if (services_arr != null)
                                        {
                                            for (Service service : services_arr)
                                            {
                                                services.put(service.get_id(), service);
                                            }
                                        } else IO.log(getClass().getName(), IO.TAG_WARN, "no services found in database.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Services in database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ServiceServerObject (containing Services objects & other metadata) is null");

                                //load service items
                                String service_items_json = RemoteComms.sendGetRequest("/services/items", headers);
                                ServiceItemServerObject serviceItemServerObject = gson.fromJson(service_items_json, ServiceItemServerObject.class);
                                if(serviceItemServerObject!=null)
                                {
                                    if(serviceItemServerObject.get_embedded()!=null)
                                    {
                                        ServiceItem[] service_items_arr = serviceItemServerObject.get_embedded().getService_items();

                                        if(service_items_arr!=null)
                                        {
                                            service_items = new HashMap<>();
                                            for (ServiceItem serviceItem : service_items_arr)
                                                service_items.put(serviceItem.get_id(), serviceItem);
                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any ServiceItems in the database.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any ServiceItems in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ServiceItemServerObject (containing ServiceItem objects & other metadata) is null");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of services.");

                                serialize(ROOT_PATH + filename, services);
                                Files.delete(new File(ROOT_PATH + "service_items.dat").toPath());
                                serialize(ROOT_PATH + "service_items.dat", service_items);
                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                services = (HashMap<String, Service>) deserialize(ROOT_PATH + filename);
                                service_items = (HashMap<String, ServiceItem>) deserialize(ROOT_PATH + "service_items.dat");
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

    public void createService(Service service, Callback callback) throws IOException
    {
        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        if(SessionManager.getInstance().getActive()!=null)
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }

        //create new quote on database
        //ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        HttpURLConnection connection = RemoteComms.putJSON(service.apiEndpoint(), service.getJSONString(), headers);
        if(connection!=null)
        {
            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                String response = IO.readStream(connection.getInputStream());

                if(response==null)
                {
                    IO.logAndAlert("Service Creation Error", "Invalid server response.", IO.TAG_ERROR);
                    return;
                }
                if(response.isEmpty())
                {
                    IO.logAndAlert("Service Creation Error", "Invalid server response.", IO.TAG_ERROR);
                    return;
                }

                //server will return message object in format "<quote_id>"
                String new_service_id = response.replaceAll("\"","");//strip inverted commas around quote_id
                new_service_id = new_service_id.replaceAll("\n","");//strip new line chars
                new_service_id = new_service_id.replaceAll(" ","");//strip whitespace chars
                IO.log(getClass().getName(), IO.TAG_INFO, "created Service["+new_service_id+"].");

                //Close connection
                if(connection!=null)
                    connection.disconnect();

                ServiceManager.getInstance().forceSynchronise();

                if(ServiceManager.getInstance().getDataset()!=null && new_service_id!=null)
                    ServiceManager.getInstance().setSelected(ServiceManager.getInstance().getDataset().get(new_service_id));

                IO.logAndAlert("Service Creation Success", "Successfully created a new Service: " + service.getService_title(), IO.TAG_INFO);
                //execute callback w/ args
                if(callback!=null)
                    if(new_service_id!=null)
                        callback.call(new_service_id);
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                //execute callback w/ args
                if(callback!=null)
                        callback.call(null);
            }
            if(connection!=null)
                connection.disconnect();
        } else IO.logAndAlert("Service Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
    }

    public void createServiceItem(ServiceItem serviceItem, Callback callback) throws IOException
    {
        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        if(SessionManager.getInstance().getActive()!=null)
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }

        //create new quote on database
        //ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        HttpURLConnection connection = RemoteComms.putJSON(serviceItem.apiEndpoint(), serviceItem.getJSONString(), headers);
        if(connection!=null)
        {
            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                String response = IO.readStream(connection.getInputStream());

                if(response==null)
                {
                    IO.logAndAlert("Service Item Creation Error", "Invalid server response.", IO.TAG_ERROR);
                    return;
                }
                if(response.isEmpty())
                {
                    IO.logAndAlert("Service Item Creation Error", "Invalid server response.", IO.TAG_ERROR);
                    return;
                }

                //server will return message object in format "<quote_id>"
                String new_service_item_id = response.replaceAll("\"","");//strip inverted commas around quote_id
                new_service_item_id = new_service_item_id.replaceAll("\n","");//strip new line chars
                new_service_item_id = new_service_item_id.replaceAll(" ","");//strip whitespace chars
                IO.log(getClass().getName(), IO.TAG_INFO, "created ServiceItem["+new_service_item_id+"].");

                //Close connection
                if(connection!=null)
                    connection.disconnect();

                ServiceManager.getInstance().forceSynchronise();

                if(ServiceManager.getInstance().getService_items()!=null && new_service_item_id!=null)
                    ServiceManager.getInstance().setSelected(ServiceManager.getInstance().getService_items().get(new_service_item_id));

                IO.logAndAlert("Service Item Creation Success", "Successfully created a new service item: " + serviceItem.getItem_name(), IO.TAG_INFO);
                //execute callback w/ args
                if(callback!=null)
                    if(new_service_item_id!=null)
                        callback.call(new_service_item_id);
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                //execute callback w/ args
                if(callback!=null)
                    callback.call(null);
            }
            if(connection!=null)
                connection.disconnect();
        } else IO.logAndAlert("Service Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
    }

    public  void resourceCreationWindow(Node parent_node, Callback callback)
    {
        IO.showPopOver("New Resource", Screens.NEW_RESOURCE.getScreen(), parent_node);
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
                        //refresh model & view when material type has been created.
                        forceSynchronise();

                        if(callback!=null)
                            callback.call(null);
                        stage.close();
                    } else
                    {
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
                forceSynchronise());

        stage.setScene(scene);
        stage.show();
    }

    class ServiceServerObject extends ServerObject
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
            private Service[] services;

            public Service[] getServices()
            {
                return services;
            }

            public void setServices(Service[] services)
            {
                this.services = services;
            }
        }
    }

    class ServiceItemServerObject extends ServerObject
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
            private ServiceItem[] service_items;

            public ServiceItem[] getService_items()
            {
                return service_items;
            }

            public void setService_items(ServiceItem[] service_items)
            {
                this.service_items = service_items;
            }
        }
    }
}
