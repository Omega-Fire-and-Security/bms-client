package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrderAsset extends PurchaseOrderItem
{
    public static final String TAG = "PurchaseOrderAsset";

    public String getItem_name()
    {
        if(getItem()!=null)
            return getItem().getAsset_name();
        return "N/A";
    }

    public String getItem_description()
    {
        if(getItem()!=null)
            return getItem().getAsset_description();
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
            return getItem().getAsset_value();
        return 0;
    }*/

    public Asset getItem()
    {
        //if(super.getItem()!=null)
        //    return (Asset) super.getItem();

        AssetManager.getInstance().initialize();
        if (AssetManager.getInstance().getAll_assets() != null)
        {
            Asset asset = AssetManager.getInstance().getAll_assets().get(getItem_id());
            if (asset != null)
                return asset;
            else IO.log(TAG, IO.TAG_ERROR, "key returns null po asset object.");
        }
        else IO.log(TAG, IO.TAG_ERROR, "no assets found on database.");
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/purchaseorder/asset";
    }
}