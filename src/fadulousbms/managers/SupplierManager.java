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
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 */
public class SupplierManager extends BusinessObjectManager
{
    private Gson gson;
    //private Supplier[] suppliers;
    private HashMap<String, Supplier> suppliers;
    private Supplier selected;
    private TableView tblSuppliers;
    private static SupplierManager supplierManager = new SupplierManager();
    public static final String TAG = "SupplierManager";
    public static final String ROOT_PATH = "cache/suppliers/";
    public String filename = "";
    private long timestamp;

    private SupplierManager()
    {
    }

    public static SupplierManager getInstance()
    {
        return supplierManager;
    }

    public HashMap<String, Supplier> getSuppliers(){return suppliers;}

    public void setSelected(Supplier supplier)
    {
        this.selected=supplier;
    }

    public Supplier getSelected()
    {
        return this.selected;
    }

    @Override
    public void initialize()
    {
        try
        {
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
    }

    public void newSupplierWindow(Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Supplier");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        stage.setHeight(700);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_supplier_name = new TextField();
        txt_supplier_name.setMinWidth(200);
        txt_supplier_name.setMaxWidth(Double.MAX_VALUE);
        HBox supplier_name = CustomTableViewControls.getLabelledNode("Supplier name", 200, txt_supplier_name);

        final TextArea txt_physical_address = new TextArea();
        txt_physical_address.setMinWidth(200);
        txt_physical_address.setMaxWidth(Double.MAX_VALUE);
        HBox physical_address = CustomTableViewControls.getLabelledNode("Supplier physical address", 200, txt_physical_address);

        final TextArea txt_postal_address = new TextArea();
        txt_postal_address.setMinWidth(200);
        txt_postal_address.setMaxWidth(Double.MAX_VALUE);
        HBox postal_address = CustomTableViewControls.getLabelledNode("Supplier postal address", 200, txt_postal_address);

        final TextField txt_tel = new TextField();
        txt_tel.setMinWidth(200);
        txt_tel.setMaxWidth(Double.MAX_VALUE);
        HBox tel = CustomTableViewControls.getLabelledNode("Supplier tel number", 200, txt_tel);

        final TextField txt_contact_email = new TextField();
        txt_contact_email.setMinWidth(200);
        txt_contact_email.setMaxWidth(Double.MAX_VALUE);
        HBox contact_email = CustomTableViewControls.getLabelledNode("eMail Address", 200, txt_contact_email);

        final TextField txt_speciality = new TextField();
        txt_speciality.setMinWidth(200);
        txt_speciality.setMaxWidth(Double.MAX_VALUE);
        HBox speciality = CustomTableViewControls.getLabelledNode("Supplier speciality", 200, txt_speciality);

        final TextField txt_supplier_reg = new TextField();
        txt_supplier_reg.setMinWidth(200);
        txt_supplier_reg.setMaxWidth(Double.MAX_VALUE);
        HBox supplier_reg = CustomTableViewControls.getLabelledNode("Registration Number", 200, txt_supplier_reg);

        final TextField txt_supplier_vat = new TextField();
        txt_supplier_vat.setMinWidth(200);
        txt_supplier_vat.setMaxWidth(Double.MAX_VALUE);
        HBox supplier_vat = CustomTableViewControls.getLabelledNode("VAT Number", 200, txt_supplier_vat);

        final TextField txt_supplier_account = new TextField();
        txt_supplier_account.setMinWidth(200);
        txt_supplier_account.setMaxWidth(Double.MAX_VALUE);
        HBox supplier_account = CustomTableViewControls.getLabelledNode("Account Name", 200, txt_supplier_account);

        final DatePicker dpk_date_partnered = new DatePicker();
        dpk_date_partnered.setMinWidth(200);
        dpk_date_partnered.setMaxWidth(Double.MAX_VALUE);
        HBox date_partnered = CustomTableViewControls.getLabelledNode("Date partnered", 200, dpk_date_partnered);

        final TextField txt_website = new TextField();
        txt_website.setMinWidth(200);
        txt_website.setMaxWidth(Double.MAX_VALUE);
        HBox website = CustomTableViewControls.getLabelledNode("Supplier website", 200, txt_website);

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
        submit = CustomTableViewControls.getSpacedButton("Create Supplier", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_supplier_name, txt_supplier_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_physical_address, txt_physical_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_postal_address, txt_postal_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_tel, txt_tel.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_contact_email, txt_contact_email.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_speciality, txt_speciality.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_supplier_reg, txt_supplier_reg.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_supplier_vat, txt_supplier_vat.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_supplier_account, txt_supplier_account.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_partnered, dpk_date_partnered.getValue()==null?"":dpk_date_partnered.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                return;

            String str_supplier_name = txt_supplier_name.getText();
            String str_physical_address = txt_physical_address.getText();
            String str_postal_address = txt_postal_address.getText();
            String str_tel = txt_tel.getText();
            String str_contact_email = txt_contact_email.getText();
            String str_speciality = txt_speciality.getText();
            String str_website = txt_website.getText();
            String str_reg = txt_supplier_reg.getText();
            String str_vat = txt_supplier_vat.getText();
            long date_partnered_in_sec = dpk_date_partnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();
            boolean is_active = chbx_active.selectedProperty().get();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("supplier_name", str_supplier_name));
            params.add(new AbstractMap.SimpleEntry<>("physical_address", str_physical_address));
            params.add(new AbstractMap.SimpleEntry<>("postal_address", str_postal_address));
            params.add(new AbstractMap.SimpleEntry<>("tel", str_tel));
            params.add(new AbstractMap.SimpleEntry<>("contact_email", str_contact_email));
            params.add(new AbstractMap.SimpleEntry<>("speciality", str_speciality));
            params.add(new AbstractMap.SimpleEntry<>("registration", str_reg));
            params.add(new AbstractMap.SimpleEntry<>("vat", str_vat));
            params.add(new AbstractMap.SimpleEntry<>("account_name", txt_supplier_account.getText()));
            params.add(new AbstractMap.SimpleEntry<>("date_partnered", String.valueOf(date_partnered_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("website", str_website));
            params.add(new AbstractMap.SimpleEntry<>("other", str_other));
            params.add(new AbstractMap.SimpleEntry<>("active", String.valueOf(is_active)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                else
                {
                    IO.logAndAlert("No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/supplier/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully created a new supplier!", IO.TAG_INFO);
                        SupplierManager.getInstance().initialize();
                        //TODO: SupplierManager.getInstance().setSelected(supplier); why??
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

        //populate clients combobox

        //Add form controls vertically on the stage
        vbox.getChildren().add(supplier_name);
        vbox.getChildren().add(physical_address);
        vbox.getChildren().add(postal_address);
        vbox.getChildren().add(tel);
        vbox.getChildren().add(contact_email);
        vbox.getChildren().add(speciality);
        vbox.getChildren().add(supplier_reg);
        vbox.getChildren().add(supplier_vat);
        vbox.getChildren().add(supplier_account);
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

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
        {
            try
            {
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
        });

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    public void loadDataFromServer()
    {
        try
        {
            if(suppliers==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "suppliers object has already been set.");
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
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                gson = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                //Get Timestamp
                String timestamp_json = RemoteComms
                        .sendGetRequest("/timestamp/suppliers_timestamp", headers);
                Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                if (cntr_timestamp != null)
                {
                    timestamp = cntr_timestamp.getCount();
                    filename = "suppliers_" + timestamp + ".dat";
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                }
                else
                {
                    IO.log(this.getClass().getName(), IO.TAG_ERROR, "could not get valid timestamp");
                    return;
                }

                if (!isSerialized(ROOT_PATH + filename))
                {
                    String suppliers_json = RemoteComms.sendGetRequest("/suppliers", headers);
                    SupplierServerObject supplierServerObject= gson.fromJson(suppliers_json, SupplierServerObject.class);
                    if(supplierServerObject!=null)
                    {
                        if(supplierServerObject.get_embedded()!=null)
                        {
                            Supplier[] suppliers_arr = supplierServerObject.get_embedded().getSuppliers();
                            suppliers = new HashMap<>();
                            for (Supplier supplier : suppliers_arr)
                                suppliers.put(supplier.get_id(), supplier);
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Suppliers in the database");
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "SupplierServerObject (containing Supplier objects & other metadata) is null");

                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of suppliers.");
                    this.serialize(ROOT_PATH + filename, suppliers);
                } else
                {
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                    suppliers = (HashMap) this.deserialize(ROOT_PATH + filename);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.",  IO.TAG_ERROR);
        } else IO.logAndAlert("Invalid Session.", "Active Session is invalid", IO.TAG_ERROR);
    }

    class SupplierServerObject extends ServerObject
    {
        private SupplierServerObject.Embedded _embedded;

        SupplierServerObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(SupplierServerObject.Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private Supplier[] suppliers;

            public Supplier[] getSuppliers()
            {
                return suppliers;
            }

            public void setSuppliers(Supplier[] suppliers)
            {
                this.suppliers = suppliers;
            }
        }
    }
}
