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
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ghost on 2017/01/13.
 */
public class PurchaseOrderManager extends BusinessObjectManager
{
    private HashMap<String, PurchaseOrder> purchaseOrders;
    private PurchaseOrder selected;
    private Gson gson;
    private static PurchaseOrderManager po_manager = new PurchaseOrderManager();
    public static final String TAG = "PurchaseOrderManager";
    public static final String ROOT_PATH = "cache/purchase_orders/";
    public String filename = "";
    private long timestamp;
    public static final int PO_STATUS_PENDING=0;
    public static final int PO_STATUS_APPROVED=1;
    public static final int PO_STATUS_ARCHIVED=2;

    private PurchaseOrderManager()
    {
    }

    public static PurchaseOrderManager getInstance()
    {
        return po_manager;
    }

    public HashMap<String, PurchaseOrder> getPurchaseOrders()
    {
        return purchaseOrders;
    }

    public void setSelected(PurchaseOrder purchaseOrder)
    {
        this.selected=purchaseOrder;
    }

    public PurchaseOrder getSelected()
    {
        return this.selected;
    }

    public static double computePurchaseOrderTotal(List<PurchaseOrderItem> poItems)
    {
        //compute total
        double total=0;
        for(PurchaseOrderItem item: poItems)
            total += item.getTotal();
        return total;
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

    public void loadDataFromServer()
    {
        try
        {
            if(purchaseOrders==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "purchase orders object has already been set.");
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
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                //Get Timestamp
                String timestamp_json = RemoteComms
                        .sendGetRequest("/api/timestamp/purchase_orders_timestamp", headers);
                Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                if (cntr_timestamp != null)
                {
                    timestamp = cntr_timestamp.getCount();
                    filename = "purchase_order_" + timestamp + ".dat";
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                }
                else
                {
                    IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                    return;
                }

                if (!isSerialized(ROOT_PATH + filename))
                {
                    String purchaseorders_json = RemoteComms.sendGetRequest("/api/purchaseorders", headers);
                    PurchaseOrder[] purchase_orders_arr = gson
                            .fromJson(purchaseorders_json, PurchaseOrder[].class);

                    purchaseOrders = new HashMap();
                    for (PurchaseOrder po : purchase_orders_arr)
                        purchaseOrders.put(po.get_id(), po);
                    if (purchaseOrders != null)
                    {
                        for (PurchaseOrder po : purchaseOrders.values())
                        {
                            //get po items from server and set them to local object
                            String purchase_order_items_json = RemoteComms
                                    .sendGetRequest("/api/purchaseorder/items/" + po.get_id(), headers);
                            PurchaseOrderResource[] purchaseOrderResources = gson
                                    .fromJson(purchase_order_items_json, PurchaseOrderResource[].class);

                            String purchase_order_assets_json = RemoteComms
                                    .sendGetRequest("/api/purchaseorder/assets/" + po.get_id(), headers);
                            PurchaseOrderAsset[] purchaseOrderAssets = gson
                                    .fromJson(purchase_order_assets_json, PurchaseOrderAsset[].class);

                            PurchaseOrderItem[] purchaseOrderItems = new PurchaseOrderItem[purchaseOrderResources.length + purchaseOrderAssets.length];
                            int i = 0;

                            for (PurchaseOrderResource item : purchaseOrderResources)
                            {
                                //String resource_json = RemoteComms.sendGetRequest("/api/resource/" + item.getItem_id(), headers);
                                //System.out.println("po resource json: " + resource_json);
                                //item.setItem(gson.fromJson(resource_json, Resource.class));
                                //item.setType(Resource.class.getName());
                                purchaseOrderItems[i] = item;
                                i++;
                            }

                            for (PurchaseOrderItem item : purchaseOrderAssets)
                            {
                                //String asset_json = RemoteComms.sendGetRequest("/api/asset/" + item.getItem_id(), headers);
                                //item.setItem(gson.fromJson(asset_json, Asset.class));
                                purchaseOrderItems[i] = item;
                                i++;
                            }
                            po.setItems(purchaseOrderItems);
                        }
                    }
                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of purchase orders.");

                    this.serialize(ROOT_PATH + filename, purchaseOrders);
                }
                else
                {
                    IO.log(this.getClass()
                            .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                    purchaseOrders = (HashMap<String, PurchaseOrder>) this.deserialize(ROOT_PATH + filename);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void emailPurchaseOrder(PurchaseOrder purchaseOrder, Callback callback)
    {
        System.out.println("PurchaseOrderManager>emailPurchaseOrder: not implemented.");
    }

    public void requestPOApproval(PurchaseOrder po, Callback callback)
    {
        if(po==null)
        {
            IO.logAndAlert("Error", "Invalid Purchase Order.", IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert("Error", "Could not find any employees in the system.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error: Invalid Session", "Could not find any valid sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error: Session Expired", "The active session has expired.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActiveEmployee()==null)
        {
            IO.logAndAlert("Error: Invalid Employee Session", "Could not find any active employee sessions.", IO.TAG_ERROR);
            return;
        }

        //upload Quote PDF to server
        uploadPurchaseOrderPDF(po);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Purchase Order ["+po.get_id()+"]");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        //gather list of Employees with enough clearance to approve quotes
        ArrayList<Employee> lst_auth_employees = new ArrayList<>();
        for(Employee employee: EmployeeManager.getInstance().getEmployees().values())
            if(employee.getAccessLevel()>=Employee.ACCESS_LEVEL_SUPER)
                lst_auth_employees.add(employee);

        if(lst_auth_employees==null)
        {
            IO.logAndAlert("Error", "Could not find any employee with the required access rights to approve documents.", IO.TAG_ERROR);
            return;
        }

        final ComboBox<Employee> cbx_destination = new ComboBox(FXCollections.observableArrayList(lst_auth_employees));
        cbx_destination.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>()
        {
            @Override
            public ListCell<Employee> call(ListView<Employee> param)
            {
                return new ListCell<Employee>()
                {
                    @Override
                    protected void updateItem(Employee employee, boolean empty)
                    {
                        if(employee!=null && !empty)
                        {
                            super.updateItem(employee, empty);
                            setText(employee.toString() + " <" + employee.getEmail() + ">");
                        }
                    }
                };
            }
        });
        cbx_destination.setMinWidth(200);
        cbx_destination.setMaxWidth(Double.MAX_VALUE);
        cbx_destination.setPromptText("Pick a recipient");
        HBox destination = CustomTableViewControls.getLabelledNode("To: ", 200, cbx_destination);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText("PURCHASE ORDER ["+po.get_id()+"] APPROVAL REQUEST");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        /*final TextField txt_job_id = new TextField();
        txt_job_id.setMinWidth(200);
        txt_job_id.setMaxWidth(Double.MAX_VALUE);
        txt_job_id.setPromptText("Type in a message");
        txt_job_id.setEditable(false);
        txt_job_id.setText(String.valueOf(quote.get_id()));
        HBox hbox_job_id = CustomTableViewControls.getLabelledNode("Quote ID: ", 200, txt_job_id);*/

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            //TODO: check this
            if(!Validators.isValidNode(cbx_destination, cbx_destination.getValue()==null?"":cbx_destination.getValue().getEmail(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("purchaseorder_id", po.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("to_email", cbx_destination.getValue().getEmail()));
            params.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));
            params.add(new AbstractMap.SimpleEntry<>("message", msg));

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive()
                            .getSessionId()));
                    params.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().toString()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/purchaseorder/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully emailed purchase order to ["+cbx_destination.getValue()+"]!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        cbx_destination.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue==null)
            {
                IO.log(getClass().getName(), "invalid destination address.", IO.TAG_ERROR);
                return;
            }
            Employee sender = SessionManager.getInstance().getActiveEmployee();
            String title = null;
            if(newValue.getGender()!=null)
                title = newValue.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";
            String msg = "Good day " + title + " " + newValue.getLastname() + ",\n\nCould you please assist me" +
                    " by approving this purchase order to be issued to "  + po.getSupplier().getSupplier_name() + ".\nThank you.\n\nBest Regards,\n"
                    + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
            txt_message.setText(msg);
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        //vbox.getChildren().add(hbox_job_id);
        vbox.getChildren().add(message);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    public void uploadPurchaseOrderPDF(PurchaseOrder purchaseOrder)
    {
        if(purchaseOrder==null)
        {
            IO.logAndAlert("Error", "Invalid purchase order object passed.", IO.TAG_ERROR);
            return;
        }
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    String path = PDF.createPurchaseOrderPdf(purchaseOrder);
                    if(path!=null)
                    {
                        File f = new File(path);
                        if (f != null)
                        {
                            if (f.exists())
                            {
                                FileInputStream in = new FileInputStream(f);
                                byte[] buffer = new byte[(int) f.length()];
                                in.read(buffer, 0, buffer.length);
                                in.close();

                                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance()
                                        .getActive().getSessionId()));
                                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                                RemoteComms.uploadFile("/api/purchaseorder/upload/" + purchaseOrder.get_id(), headers, buffer);
                                IO.log(getClass().getName(), IO.TAG_INFO, "\n uploaded purchase order [#" + purchaseOrder.get_id()
                                        + "], file size: [" + buffer.length + "] bytes.");
                            }
                            else
                            {
                                IO.logAndAlert(getClass().getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                            }
                        }
                        else
                        {
                            IO.log(getClass().getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
                        }
                    } else IO.log(getClass().getName(), "Could not get valid path for created purchase order pdf.", IO.TAG_ERROR);
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }
}
