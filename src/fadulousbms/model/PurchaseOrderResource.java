package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.ResourceManager;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrderResource extends PurchaseOrderItem
{
    public static final String TAG = "PurchaseOrderResource";

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

    public Resource getItem()
    {
        ResourceManager.getInstance().initialize();
        if (ResourceManager.getInstance().getDataset() != null)
        {
            Resource resource = ResourceManager.getInstance().getDataset().get(getItem_id());
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
        return "/purchaseorders/resources";
    }
}