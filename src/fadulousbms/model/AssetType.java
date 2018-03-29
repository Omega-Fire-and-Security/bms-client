package fadulousbms.model;

import fadulousbms.managers.AssetManager;
import fadulousbms.managers.BusinessObjectManager;

import java.io.Serializable;

/**
 * Created by ghost on 2017/02/01.
 */
public class AssetType extends Type implements Serializable
{
    public AssetType(String type_name, String type_description)
    {
        super(type_name, type_description);
    }

    @Override
    public BusinessObjectManager getManager()
    {
        return AssetManager.getInstance();
    }

    @Override
    public String apiEndpoint()
    {
        return "/asset/type";
    }
}
