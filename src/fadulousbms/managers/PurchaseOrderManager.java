package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

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
}
