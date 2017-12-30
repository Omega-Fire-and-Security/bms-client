package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

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

        AssetManager.getInstance().loadDataFromServer();
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
    public String toString()
    {
        String json_obj = "{"+(get_id()!=null?"\"_id\":\""+get_id()+"\",":"")
                +"\"purchase_order_id\":\""+getPurchase_order_id()+"\""
                +",\"item_id\":\""+getItem_id()+"\""
                //+"\"number\":\""+getItem_name()+"\""
                //*+",\"supplier_id\":\""+getItem_description()+"\""
                +",\"type\":\""+getType()+"\""
                +",\"unit\":\""+getUnit()+"\""
                +",\"quantity\":\""+getQuantity()+"\""
                +",\"cost\":\""+getCost()+"\"";
        if(getDiscountValue()>0)
            json_obj+=",\"discount\":\""+getDiscountValue()+"\"";
        if(getCreator()!=null)
            json_obj+=",\"creator\":\""+getCreator()+"\"";
        if(getDate_logged()>0)
            json_obj+=",\"date_logged\":\""+getDate_logged()+"\"";
        json_obj+=",\"other\":\""+getOther()+"\"}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/purchaseorders/assets";
    }
}