package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.IOException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 * @author ghost
 */
public class ClientManager extends ApplicationObjectManager
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

    public static ClientManager getInstance()
    {
        return clientManager;
    }

    @Override
    public Client getSelected()
    {
        return (Client) super.getSelected();
    }

    @Override
    public HashMap<String, Client> getDataset(){return clients;}

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
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms.get("/timestamp/clients_timestamp", headers);
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
                                String clients_json_object = RemoteComms.get("/clients", headers);
                                ClientServerResponseObject clientServerObject = (ClientServerResponseObject) ClientManager.getInstance().parseJSONobject(clients_json_object, new ClientServerResponseObject());
                                if(clientServerObject!=null)
                                {
                                    if(clientServerObject.get_embedded()!=null)
                                    {
                                        Client[] clients_arr = clientServerObject.get_embedded().getClients();

                                        clients = new HashMap<>();
                                        for (Client client : clients_arr)
                                            clients.put(client.get_id(), client);
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Clients in database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ClientServerResponseObject (containing Client objects & other metadata) is null");

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

    public void newClientPopOver(Node parent, Callback callback)
    {
        setSelected(null);

        final TextField txt_client_name = new TextField();
        txt_client_name.setMinWidth(200);
        txt_client_name.setMaxWidth(Double.MAX_VALUE);
        //HBox client_name = CustomTableViewControls.getLabelledNode("Client Name", 200, txt_client_name);

        final TextArea txt_physical_address = new TextArea();
        txt_physical_address.setMinWidth(200);
        txt_physical_address.setMaxWidth(Double.MAX_VALUE);
        txt_physical_address.setPrefHeight(70);
        //HBox physical_address = CustomTableViewControls.getLabelledNode("Physical Address", 200, txt_physical_address);

        final TextArea txt_postal_address = new TextArea();
        txt_postal_address.setMinWidth(200);
        txt_postal_address.setMaxWidth(Double.MAX_VALUE);
        txt_postal_address.setPrefHeight(70);
        //HBox postal_address = CustomTableViewControls.getLabelledNode("Postal Address", 200, txt_postal_address);

        final TextField txt_tel = new TextField();
        txt_tel.setMinWidth(200);
        txt_tel.setMaxWidth(Double.MAX_VALUE);
        //HBox tel = CustomTableViewControls.getLabelledNode("Tel Number", 200, txt_tel);

        final TextField txt_contact_email = new TextField();
        txt_contact_email.setMinWidth(200);
        txt_contact_email.setMaxWidth(Double.MAX_VALUE);
        //HBox contact_email = CustomTableViewControls.getLabelledNode("eMail Address", 200, txt_contact_email);

        final TextField txt_client_reg = new TextField("N/A");
        txt_client_reg.setMinWidth(200);
        txt_client_reg.setMaxWidth(Double.MAX_VALUE);
        //HBox client_reg = CustomTableViewControls.getLabelledNode("Registration Number", 200, txt_client_reg);

        final TextField txt_client_vat = new TextField("N/A");
        txt_client_vat.setMinWidth(200);
        txt_client_vat.setMaxWidth(Double.MAX_VALUE);
        //HBox client_vat = CustomTableViewControls.getLabelledNode("VAT Number", 200, txt_client_vat);

        final TextField txt_client_account = new TextField();
        txt_client_account.setMinWidth(200);
        txt_client_account.setMaxWidth(Double.MAX_VALUE);
        //HBox client_account = CustomTableViewControls.getLabelledNode("Account Name", 200, txt_client_account);

        txt_client_name.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if(txt_client_name.getText()!=null)
                txt_client_account.setText(txt_client_name.getText().toLowerCase().replaceAll(" ", "-"));
        });

        final DatePicker dpk_date_partnered = new DatePicker();
        dpk_date_partnered.setMinWidth(200);
        dpk_date_partnered.setMaxWidth(Double.MAX_VALUE);
        //HBox date_partnered = CustomTableViewControls.getLabelledNode("Date Partnered", 200, dpk_date_partnered);

        final TextField txt_website = new TextField();
        txt_website.setMinWidth(200);
        txt_website.setMaxWidth(Double.MAX_VALUE);
        //HBox website = CustomTableViewControls.getLabelledNode("Website", 200, txt_website);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        txt_other.setPrefHeight(70);
        //HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        Button btnSubmit = new Button("Create New Client");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(45);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(new Label("Client Name: "), 0, 0);
        page.add(txt_client_name, 1, 0);

        page.add(new Label("Physical Address: "), 0, 1);
        page.add(txt_physical_address, 1, 1);

        page.add(new Label("Postal Address: "), 0, 2);
        page.add(txt_postal_address, 1, 2);

        page.add(new Label("Tel No.: "), 0, 3);
        page.add(txt_tel, 1, 3);

        page.add(new Label("eMail address: "), 0, 4);
        page.add(txt_contact_email, 1, 4);

        page.add(new Label("Registration Number: "), 0, 5);
        page.add(txt_client_reg, 1, 5);

        page.add(new Label("Tax Number"), 0, 6);
        page.add(txt_client_vat, 1, 6);

        page.add(new Label("Credit account name: "), 0, 7);
        page.add(txt_client_account, 1, 7);

        page.add(new Label("Website: "), 0, 8);
        page.add(txt_website, 1, 8);

        page.add(new Label("Other Info: "), 0, 9);
        page.add(txt_other, 1, 9);

        page.add(btnSubmit, 1, 10);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create new Client");
        popover.setDetached(true);
        popover.show(parent);

        TextFields.bindAutoCompletion(txt_client_name, ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    setSelected(event.getCompletion());

                    if(((Client)getSelected()).getPhysical_address()!=null)
                        txt_physical_address.setText(((Client)getSelected()).getPhysical_address());
                    if(((Client)getSelected()).getPostal_address()!=null)
                        txt_postal_address.setText(((Client)getSelected()).getRegistration_number());
                    if(((Client)getSelected()).getRegistration_number()!=null)
                        txt_client_reg.setText(((Client)getSelected()).getRegistration_number());
                    if(((Client)getSelected()).getTax_number()!=null)
                        txt_client_vat.setText(((Client)getSelected()).getRegistration_number());
                    if(((Client)getSelected()).getAccount_name()!=null)
                        txt_client_account.setText(((Client)getSelected()).getAccount_name());
                    if(((Client)getSelected()).getTel()!=null)
                        txt_tel.setText(((Client)getSelected()).getTel());
                    if(((Client)getSelected()).getWebsite()!=null)
                        txt_website.setText(((Client)getSelected()).getWebsite());
                    if(((Client)getSelected()).getContact_email()!=null)
                        txt_contact_email.setText(((Client)getSelected()).getContact_email());
                    if((getSelected()).getOther()!=null)
                        txt_other.setText(getSelected().getOther());
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
                /*if(!Validators.isValidNode(txt_client_reg, txt_client_reg.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_client_vat, txt_client_vat.getText(), 1, ".+"))
                    return;*/
                if(!Validators.isValidNode(txt_client_account, txt_client_account.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(dpk_date_partnered, dpk_date_partnered.getValue()==null?"":dpk_date_partnered.getValue().toString(), 4, date_regex))
                    return;
                /*if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                    return;*/

                //if txt_client_name matches selected_client's client_name ask if they want to make a duplicate record
                String proceed = IO.OK;
                if(((Client)getSelected())!=null)
                    if(txt_client_name.getText().equals(((Client)getSelected()).getClient_name()))
                        proceed = IO.showConfirm("Found duplicate client, continue?", "Found client with the name ["+txt_client_name.getText()+"], add another record?");

                //did they choose to continue with the creation or cancel?
                if(!proceed.equals(IO.OK))
                {
                    IO.log(getClass().getName(), "aborting new Client creation.", IO.TAG_VERBOSE);
                    return;
                }

                long date_partnered_in_sec = dpk_date_partnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

                Client client = new Client();
                client.setClient_name(txt_client_name.getText());
                client.setPhysical_address(txt_physical_address.getText());
                client.setPostal_address(txt_postal_address.getText());
                client.setTel(txt_tel.getText());
                client.setContact_email(txt_contact_email.getText());
                client.setRegistration_number(txt_client_reg.getText());
                client.setTax_number(txt_client_vat.getText());
                client.setAccount_name(txt_client_account.getText());
                client.setDate_partnered(date_partnered_in_sec);
                client.setWebsite(txt_website.getText());
                client.setActive(true);
                client.setCreator(SessionManager.getInstance().getActive().getUsr());
                if(txt_other.getText()!=null)
                    client.setOther(txt_other.getText());

                try
                {
                    ClientManager.getInstance().putObject(client, new_client_id ->
                    {
                        if(new_client_id!=null)
                        {
                            setSelected(ClientManager.getInstance().getDataset().get(new_client_id));
                        } else IO.logAndAlert("Error", "Could not create new client ["+txt_client_name.getText()+"]", IO.TAG_ERROR);
                        return null;
                    });
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
        });
    }

    class ClientServerResponseObject extends ServerResponseObject
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