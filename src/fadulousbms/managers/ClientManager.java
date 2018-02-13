package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 */
public class ClientManager extends BusinessObjectManager
{
    private HashMap<String, Client> clients;
    private Gson gson;
    private static ClientManager clientManager = new ClientManager();
    public static final String TAG = "ClientManager";
    public static final String ROOT_PATH = "cache/clients/";
    public String filename = "";
    private long timestamp;

    private ClientManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static BusinessObjectManager getInstance()
    {
        return clientManager;
    }

    @Override
    public HashMap<String, Client> getDataset(){return clients;}

    public static String createNewClient(Client client, Callback callback)
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
            return null;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log in.", IO.TAG_ERROR);
            return null;
        }

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

            //create new client on database
            HttpURLConnection connection = RemoteComms.putJSON("/clients", client.getJSONString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());

                    //server will return message object in format "<client_id>"
                    String new_client_id = response.replaceAll("\"","");//strip inverted commas around client_id
                    new_client_id = new_client_id.replaceAll("\n","");//strip new line chars
                    new_client_id = new_client_id.replaceAll(" ","");//strip whitespace chars

                    IO.log(ClientManager.class.getName(), IO.TAG_INFO, "successfully created a new client: " + new_client_id);

                    SupplierManager.getInstance().synchroniseDataset();

                    if(callback!=null)
                        callback.call(new_client_id);

                    if(connection!=null)
                        connection.disconnect();
                    return new_client_id;
                } else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);

                    if(callback!=null)
                        callback.call(null);

                    if(connection!=null)
                        connection.disconnect();
                    return null;
                }

            } else IO.logAndAlert("Client Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.log(ClientManager.class.getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
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
                    if(smgr.getActive()!=null)
                    {
                        if(!smgr.getActive().isExpired())
                        {
                            gson = new GsonBuilder().create();
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = null;

                            timestamp_json = RemoteComms.sendGetRequest("/timestamp/clients_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "clients_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                String clients_json_object = RemoteComms.sendGetRequest("/clients", headers);
                                ClientServerObject clientServerObject = gson.fromJson(clients_json_object, ClientServerObject.class);
                                if(clientServerObject!=null)
                                {
                                    if(clientServerObject.get_embedded()!=null)
                                    {
                                        Client[] clients_arr = clientServerObject.get_embedded().getClients();

                                        clients = new HashMap<>();
                                        for (Client client : clients_arr)
                                        {
                                            clients.put(client.get_id(), client);
                                        }
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Clients in database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ClientServerObject (containing Client objects & other metadata) is null");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of clients.");
                                serialize(ROOT_PATH + filename, clients);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                clients = (HashMap<String, Client>) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Active session has expired.", "Session Expired", IO.TAG_ERROR);
                    } else IO.logAndAlert("No active sessions.", "Session Expired", IO.TAG_ERROR);
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

    public static void newClientWindow(String title, Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - " + title);
        stage.setMinWidth(320);
        stage.setHeight(500);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_client_name = new TextField();
        txt_client_name.setMinWidth(200);
        txt_client_name.setMaxWidth(Double.MAX_VALUE);
        HBox client_name = CustomTableViewControls.getLabelledNode("Client Name", 200, txt_client_name);

        final TextArea txt_physical_address = new TextArea();
        txt_physical_address.setMinWidth(200);
        txt_physical_address.setMaxWidth(Double.MAX_VALUE);
        HBox physical_address = CustomTableViewControls.getLabelledNode("Physical Address", 200, txt_physical_address);

        final TextArea txt_postal_address = new TextArea();
        txt_postal_address.setMinWidth(200);
        txt_postal_address.setMaxWidth(Double.MAX_VALUE);
        HBox postal_address = CustomTableViewControls.getLabelledNode("Postal Address", 200, txt_postal_address);

        final TextField txt_tel = new TextField();
        txt_tel.setMinWidth(200);
        txt_tel.setMaxWidth(Double.MAX_VALUE);
        HBox tel = CustomTableViewControls.getLabelledNode("Tel Number", 200, txt_tel);

        final TextField txt_contact_email = new TextField();
        txt_contact_email.setMinWidth(200);
        txt_contact_email.setMaxWidth(Double.MAX_VALUE);
        HBox contact_email = CustomTableViewControls.getLabelledNode("eMail Address", 200, txt_contact_email);

        final TextField txt_client_reg = new TextField();
        txt_client_reg.setMinWidth(200);
        txt_client_reg.setMaxWidth(Double.MAX_VALUE);
        HBox client_reg = CustomTableViewControls.getLabelledNode("Registration Number", 200, txt_client_reg);

        final TextField txt_client_vat = new TextField();
        txt_client_vat.setMinWidth(200);
        txt_client_vat.setMaxWidth(Double.MAX_VALUE);
        HBox client_vat = CustomTableViewControls.getLabelledNode("VAT Number", 200, txt_client_vat);

        final TextField txt_client_account = new TextField();
        txt_client_account.setMinWidth(200);
        txt_client_account.setMaxWidth(Double.MAX_VALUE);
        HBox client_account = CustomTableViewControls.getLabelledNode("Account Name", 200, txt_client_account);

        final DatePicker dpk_date_partnered = new DatePicker();
        dpk_date_partnered.setMinWidth(200);
        dpk_date_partnered.setMaxWidth(Double.MAX_VALUE);
        HBox date_partnered = CustomTableViewControls.getLabelledNode("Date Partnered", 200, dpk_date_partnered);

        final TextField txt_website = new TextField();
        txt_website.setMinWidth(200);
        txt_website.setMaxWidth(Double.MAX_VALUE);
        HBox website = CustomTableViewControls.getLabelledNode("Website", 200, txt_website);

        final CheckBox chbx_active = new CheckBox();
        chbx_active.setMinWidth(200);
        chbx_active.setMaxWidth(Double.MAX_VALUE);
        chbx_active.setSelected(true);
        HBox active = CustomTableViewControls.getLabelledNode("Active", 200, chbx_active);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_client_name, txt_client_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_physical_address, txt_physical_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_postal_address, txt_postal_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_tel, txt_tel.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_contact_email, txt_contact_email.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_client_reg, txt_client_reg.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_client_vat, txt_client_vat.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_client_account, txt_client_account.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_partnered, dpk_date_partnered.getValue()==null?"":dpk_date_partnered.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                return;

            long date_partnered_in_sec = dpk_date_partnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("client_name", txt_client_name.getText()));
            params.add(new AbstractMap.SimpleEntry<>("physical_address", txt_physical_address.getText()));
            params.add(new AbstractMap.SimpleEntry<>("postal_address", txt_postal_address.getText()));
            params.add(new AbstractMap.SimpleEntry<>("tel", txt_tel.getText()));
            params.add(new AbstractMap.SimpleEntry<>("contact_email", txt_contact_email.getText()));
            params.add(new AbstractMap.SimpleEntry<>("registration", txt_client_reg.getText()));
            params.add(new AbstractMap.SimpleEntry<>("vat", txt_client_vat.getText()));
            params.add(new AbstractMap.SimpleEntry<>("account_name", txt_client_account.getText()));
            params.add(new AbstractMap.SimpleEntry<>("date_partnered", String.valueOf(date_partnered_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("website", txt_website.getText()));
            params.add(new AbstractMap.SimpleEntry<>("active", String.valueOf(chbx_active.isSelected())));
            params.add(new AbstractMap.SimpleEntry<>("other", txt_other.getText()));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                else
                {
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/client/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully created a new client!", IO.TAG_INFO);
                        callback.call(null);
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

        //populate clients combobox

        //Add form controls vertically on the stage
        vbox.getChildren().add(client_name);
        vbox.getChildren().add(physical_address);
        vbox.getChildren().add(postal_address);
        vbox.getChildren().add(tel);
        vbox.getChildren().add(contact_email);
        vbox.getChildren().add(client_reg);
        vbox.getChildren().add(client_vat);
        vbox.getChildren().add(client_account);
        vbox.getChildren().add(date_partnered);
        vbox.getChildren().add(website);
        vbox.getChildren().add(active);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        /*stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                synchroniseDataset());*/

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    class ClientServerObject extends ServerObject
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
            private Client[] clients;

            public Client[] getClients()
            {
                return clients;
            }

            public void setClients(Client[] clients)
            {
                this.clients = clients;
            }
        }
    }
}