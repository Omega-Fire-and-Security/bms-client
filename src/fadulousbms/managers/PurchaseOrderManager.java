package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.*;

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
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                //Get Timestamp
                String timestamp_json = RemoteComms
                        .sendGetRequest("/timestamp/purchase_orders_timestamp", headers);
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
                    String purchaseorders_json = RemoteComms.sendGetRequest("/purchaseorders", headers);
                    PurchaseOrderServerObject purchaseOrderServerObject= gson.fromJson(purchaseorders_json, PurchaseOrderServerObject.class);
                    if(purchaseOrderServerObject!=null)
                    {
                        if(purchaseOrderServerObject.get_embedded()!=null)
                        {
                            PurchaseOrder[] purchase_orders_arr = purchaseOrderServerObject.get_embedded().getPurchase_orders();

                            if (purchase_orders_arr != null)
                            {
                                purchaseOrders = new HashMap<>();
                                for (PurchaseOrder purchaseOrder : purchase_orders_arr)
                                    purchaseOrders.put(purchaseOrder.get_id(), purchaseOrder);
                            }
                            else IO.log(getClass().getName(), IO.TAG_WARN, "no purchase orders found in database.");
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Purchase Orders in database.");
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "PurchaseOrderServerObject (containing PurchaseOrder objects & other metadata) is null");

                    if (purchaseOrders != null)
                    {
                        for (PurchaseOrder po : purchaseOrders.values())
                        {
                            ArrayList<PurchaseOrderItem> purchaseOrderItems = new ArrayList<>();

                            //get po items from server and set them to local object
                            String purchase_order_items_json = RemoteComms.sendGetRequest("/purchaseorders/resources/" + po.get_id(), headers);
                            PurchaseOrderResourceServerObject purchaseOrderResourceServerObject= gson.fromJson(purchase_order_items_json, PurchaseOrderResourceServerObject.class);
                            if(purchaseOrderResourceServerObject!=null)
                            {
                                if(purchaseOrderResourceServerObject.get_embedded()!=null)
                                {
                                    PurchaseOrderResource[] purchase_order_resources_arr = purchaseOrderResourceServerObject
                                            .get_embedded().getPurchase_order_resources();
                                    purchaseOrderItems
                                            .addAll(FXCollections.observableArrayList(purchase_order_resources_arr));
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Resources for PO #"+ po.get_id());
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "PurchaseOrderResourceServerObject (containing PurchaseOrderResource objects & other metadata) is null");

                            String purchase_order_assets_json = RemoteComms
                                    .sendGetRequest("/purchaseorders/assets/" + po.get_id(), headers);
                            PurchaseOrderAssetServerObject purchaseOrderAssetServerObject= gson.fromJson(purchase_order_assets_json, PurchaseOrderAssetServerObject.class);
                            if(purchaseOrderAssetServerObject!=null)
                            {
                                if(purchaseOrderAssetServerObject.get_embedded()!=null)
                                {
                                    PurchaseOrderAsset[] purchase_order_assets_arr = purchaseOrderAssetServerObject
                                            .get_embedded().getPurchase_order_assets();
                                    purchaseOrderItems
                                            .addAll(FXCollections.observableArrayList(purchase_order_assets_arr));
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Assets for PO #"+ po.get_id());
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "PurchaseOrderAssetServerObject (containing PurchaseOrderAsset objects & other metadata) is null");


                            if(!purchaseOrderItems.isEmpty())
                            {
                                PurchaseOrderItem[] po_items_arr = new PurchaseOrderItem[purchaseOrderItems.size()];
                                purchaseOrderItems.toArray(po_items_arr);
                                po.setItems(po_items_arr);
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "PO #"+po.getNumber()+" has no items.");
                        }
                    }
                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of purchase orders.");
                    this.serialize(ROOT_PATH + filename, purchaseOrders);
                } else
                {
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                    purchaseOrders = (HashMap<String, PurchaseOrder>) this.deserialize(ROOT_PATH + filename);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void requestPOApproval(PurchaseOrder po, Callback callback) throws IOException
    {
        if(po==null)
        {
            IO.logAndAlert("Error", "Invalid Purchase Order.", IO.TAG_ERROR);
            return;
        }
        if(po.getSupplier()==null)
        {
            IO.logAndAlert("Error", "Invalid Purchase Order->Supplier.", IO.TAG_ERROR);
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
        String path = PDF.createPurchaseOrderPdf(po);
        String base64_po = null;
        if(path!=null)
        {
            File f = new File(path);
            if (f != null)
            {
                if (f.exists())
                {
                    FileInputStream in = new FileInputStream(f);
                    byte[] buffer =new byte[(int) f.length()];
                    in.read(buffer, 0, buffer.length);
                    in.close();
                    base64_po = Base64.getEncoder().encodeToString(buffer);
                } else
                {
                    IO.logAndAlert(PurchaseOrderManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                }
            } else
            {
                IO.log(PurchaseOrderManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
            }
        } else IO.log(PurchaseOrderManager.class.getName(), "Could not get valid path for created PO pdf.", IO.TAG_ERROR);
        final String finalBase64_po = base64_po;

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Request Purchase Order ["+po.get_id()+"] Approval");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText("PURCHASE ORDER ["+po.get_id()+"] APPROVAL REQUEST");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        //set default message
        Employee sender = SessionManager.getInstance().getActiveEmployee();
        String title = sender.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";;
        String def_msg = "Good day,\n\nCould you please assist me" +
                " by approving this purchase order to be issued to "  + po.getSupplier().getSupplier_name() + ".\nThank you.\n\nBest Regards,\n"
                + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
        txt_message.setText(def_msg);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            //TODO: check this
            //if(!Validators.isValidNode(cbx_destination, cbx_destination.getValue()==null?"":cbx_destination.getValue().getEmail(), 1, ".+"))
            //    return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            //ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //params.add(new AbstractMap.SimpleEntry<>("message", msg));

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));//multipart/form-data
                headers.add(new AbstractMap.SimpleEntry<>("purchaseorder_id", po.get_id()));
                //headers.add(new AbstractMap.SimpleEntry<>("to_email", cbx_destination.getValue().getEmail()));
                headers.add(new AbstractMap.SimpleEntry<>("message", msg));
                headers.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));

                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive().getSession_id()));
                    headers.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().toString()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                FileMetadata fileMetadata = new FileMetadata("purchase_order_"+po.get_id()+".pdf","application/pdf");
                fileMetadata.setFile(finalBase64_po);
                HttpURLConnection connection = RemoteComms.postJSON("/purchaseorders/approval_request", fileMetadata.toString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully requested Purchase Order approval!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    } else {
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(subject);
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

    class PurchaseOrderServerObject extends ServerObject
    {
        private PurchaseOrderServerObject.Embedded _embedded;

        PurchaseOrderServerObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(PurchaseOrderServerObject.Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private PurchaseOrder[] purchase_orders;

            public PurchaseOrder[] getPurchase_orders()
            {
                return purchase_orders;
            }

            public void setPurchase_orders(PurchaseOrder[] purchase_orders)
            {
                this.purchase_orders = purchase_orders;
            }
        }
    }

    class PurchaseOrderAssetServerObject extends ServerObject
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
            private PurchaseOrderAsset[] purchase_order_assets;

            public PurchaseOrderAsset[] getPurchase_order_assets()
            {
                return purchase_order_assets;
            }

            public void setPurchase_order_assets(PurchaseOrderAsset[] purchase_order_assets)
            {
                this.purchase_order_assets = purchase_order_assets;
            }
        }
    }

    class PurchaseOrderResourceServerObject extends ServerObject
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
            private PurchaseOrderResource[] purchase_order_resources;

            public PurchaseOrderResource[] getPurchase_order_resources()
            {
                return purchase_order_resources;
            }

            public void setPurchase_order_resources(PurchaseOrderResource[] purchase_order_resources)
            {
                this.purchase_order_resources = purchase_order_resources;
            }
        }
    }
}
