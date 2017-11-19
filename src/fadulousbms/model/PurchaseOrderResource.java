package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrderResource extends PurchaseOrderItem
{
    public static final String TAG = "PurchaseOrderResource";

    public String getItem_name()
    {
        if(getItem()!=null)
            return getItem().getResource_name();
        return "N/A";
    }

    public String getItem_description()
    {
        if(getItem()!=null)
            return getItem().getResource_description();
        return "N/A";
    }

    public String getUnit()
    {
        if(getItem()!=null)
            return getItem().getUnit();
        return "N/A";
    }

    /*public double getCostValue()
    {
        if(getItem()!=null)
            return getItem().getResource_value();
        return 0;
    }*/

    public Resource getItem()
    {
        //if(super.getItem()!=null)
        //    return (Resource) super.getItem();

        ResourceManager.getInstance().loadDataFromServer();
        if (ResourceManager.getInstance().getAll_resources() != null)
        {
            Resource resource = ResourceManager.getInstance().getAll_resources().get(getItem_id());
            if (resource != null)
            {
                return resource;
            }
            else IO.log(TAG, IO.TAG_ERROR, "key returns null po resource object.");
        }
        else IO.log(TAG, IO.TAG_ERROR, "no resources on database.");
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/purchaseorder/item";
    }
}