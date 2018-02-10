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
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.text.Normalizer;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;

/**
 * Created by ghost on 2017/01/11.
 */
public class SupplierManager extends BusinessObjectManager
{
    private Gson gson;
    private HashMap<String, Supplier> suppliers;
    private static SupplierManager supplierManager = new SupplierManager();
    public static final String TAG = "SupplierManager";
    public static final String ROOT_PATH = "cache/suppliers/";
    public String filename = "";
    private long timestamp;

    private SupplierManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static SupplierManager getInstance()
    {
        return supplierManager;
    }

    @Override
    public HashMap<String, Supplier> getDataset(){return suppliers;}

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
                synchroniseDataset());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
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
                                return null;
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
                                serialize(ROOT_PATH + filename, suppliers);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                suppliers = (HashMap) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.",  IO.TAG_ERROR);
                    } else IO.logAndAlert("Invalid Session.", "Active Session is invalid", IO.TAG_ERROR);
                } catch (MalformedURLException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (ClassNotFoundException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
                return null;
            }
        };
    }

    public static void parsePDF()
    {
        File file = new File("suppliers.pdf");//C:/my.pdf
        try
        {
            PDDocument doc = PDDocument.load(file);
            String doc_text = new PDFTextStripper().getText(doc);
            if(doc_text!=null)
            {
                String balance_regex = "^(R\\s*(\\-|\\+)*\\s*\\d+\\,{0,1}\\d*\\.{0,1}\\d{0,2})";
                String tel_num_regex = "(\\d{3,}\\s+\\d{3,}\\s+\\d{4,})";
                String contact_regex = "(\\w+\\s*\\w*)";
                String active_regex = "(Yes|No)";
                String category_regex = "(\\w+\\s*\\w*)";
                String name_regex = "(\\w+\\s*\\w*)";

                for (String line : doc_text.split("\n"))
                {
                    String balance="", tel="", contact="", category="", org = "";
                    boolean active=false;

                    System.out.println("\n\n");
                    IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "***********Parsing Line: " + line);
                    if(line.toLowerCase().contains("yes") || line.toLowerCase().contains("no"))
                    {
                        Matcher matcher = Validators.matchRegex(balance_regex+tel_num_regex+contact_regex+active_regex+category_regex+name_regex, line);//check balance
                        if(matcher.find())
                        {
                            IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tmatches main regex, group count: " + (matcher.groupCount()));

                            balance = matcher.group(0);
                            tel = matcher.group(1);
                            contact = matcher.group(2);
                            active = matcher.group(3).toLowerCase().equals("yes");
                            category = matcher.group(4);
                            name_regex = matcher.group(5);
                        } else//line does not have all the data
                        {
                            //filter out the missing fields
                            IO.log(PDF.class
                                    .getName(), IO.TAG_VERBOSE, "\tdoes not match main regex, parsing given fields..");

                            int index = 0;
                            //check balance
                            matcher = Validators.matchRegex(balance_regex, line);
                            if (matcher.find())
                            {
                                balance = matcher.group(0);
                                index = matcher.end();//move char cursor for next search
                            }

                            //check telephone
                            matcher = Validators.matchRegex(tel_num_regex, line);
                            if (matcher.find(index))
                            {
                                tel = matcher.group(0);
                                index = matcher.end();//move char cursor for next search
                            }

                            //the remaining fields need special parsing
                            String new_line = line.substring(index);
                            String[] contact_cat_org_arr = new String[0];
                            if(line.contains("Yes"))
                            {
                                contact_cat_org_arr = new_line.split("Yes");
                                active=true;
                            }
                            if(line.contains("Yes0"))
                            {
                                contact_cat_org_arr = new_line.split("Yes0");//get rid of leading zero
                                active=true;
                            }
                            if(line.contains("No"))
                            {
                                contact_cat_org_arr = new_line.split("No");
                                active=false;
                            }
                            if(line.contains("No0"))
                            {
                                contact_cat_org_arr = new_line.split("No0");//get rid of leading zero
                                active=false;
                            }

                            //for (String s : new_line.split("(?=\\p{Upper})"))
                            if(contact_cat_org_arr!=null)
                            {
                                if(contact_cat_org_arr.length>0)
                                {
                                    contact = contact_cat_org_arr[0];//.isEmpty()?contact_cat_org_arr[1]:contact_cat_org_arr[0];

                                    if(contact_cat_org_arr.length>1)//if arr not empty, use elem [1] as category & org name
                                    {
                                        if(contact.isEmpty())
                                            contact=contact_cat_org_arr[1];//use elem [1] as contact if elem [0] is empty
                                        category = contact_cat_org_arr[1];
                                        org=contact_cat_org_arr[1];
                                    } else //if no elem [1], use same value as contact
                                    {
                                        category=contact_cat_org_arr[0];
                                        org=contact_cat_org_arr[0];
                                    }

                                    /*if(contact_cat_org_arr.length>2)//if arr len>2 use elem 2 as organisation
                                        org=contact_cat_org_arr[2];
                                    else if(contact_cat_org_arr.length>1)//if arr len>1 use elem 1 as organisation
                                        org=contact_cat_org_arr[1];
                                    else org=contact_cat_org_arr[0];//else just default to using the same value as contact*/
                                }
                            }
                        }
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tBalance: " + balance);
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tTel: " + tel);
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tContact: " + contact);
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tActive: " + active);
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tCategory: " + category);
                        IO.log(PDF.class.getName(), IO.TAG_VERBOSE, "\tOrganisation: " + org);

                        if(SessionManager.getInstance().getActive()!=null)
                        {
                            if(!SessionManager.getInstance().getActive().isExpired())
                            {
                                //create Supplier
                                Supplier supplier = new Supplier();
                                supplier.setSupplier_name(org);//.replaceAll("[^\\p{ASCII}]", "")
                                supplier.setCreator(SessionManager.getInstance().getActive().getUsr());
                                supplier.setAccount_name(supplier.getSupplier_name().toLowerCase().replaceAll("\\s", ""));
                                if(contact!=null)
                                    supplier.setContact_email(contact);//.replaceAll("[^\\p{ASCII}]", "")
                                //Normalizer.normalize(contact, Normalizer.Form.NFD)
                                supplier.setWebsite("not available");
                                supplier.setDate_partnered(System.currentTimeMillis());
                                supplier.setSpeciality(category);//.replaceAll("[^\\p{ASCII}]", "")
                                supplier.setActive(active);
                                supplier.setFax("not available");
                                supplier.setTel(tel);//.replaceAll("[^\\p{ASCII}]", "")
                                supplier.setPostal_address("not available");
                                supplier.setPhysical_address("not available");
                                supplier.setRegistration_number("not available");
                                supplier.setVat_number("not available");

                                System.out.println("############"+supplier.getJSONString());

                                SupplierManager.getInstance().createNewSupplier(supplier, null);
                            } else IO.logAndAlert("Error: Session Expired", "Active session is has expired.\nPlease log inx.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.log(PDF.class.getName(), IO.TAG_WARN, "invalid supplier, can't tell if active or not.");
                }
            }
        } catch (IOException e)
        {
            IO.log(PDF.class.getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public String createNewSupplier(Supplier supplier, Callback callback)
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

            //create new job on database
            HttpURLConnection connection = RemoteComms.putJSON("/suppliers", supplier.getJSONString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());

                    //server will return message object in format "<supplier_id>"
                    String new_supplier_id = response.replaceAll("\"","");//strip inverted commas around job_id
                    new_supplier_id = new_supplier_id.replaceAll("\n","");//strip new line chars
                    new_supplier_id = new_supplier_id.replaceAll(" ","");//strip whitespace chars

                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully created a new supplier: " + new_supplier_id);

                    SupplierManager.getInstance().synchroniseDataset();

                    if(callback!=null)
                        callback.call(new_supplier_id);

                    if(connection!=null)
                        connection.disconnect();
                    return new_supplier_id;
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

            } else IO.logAndAlert("Supplier Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
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
