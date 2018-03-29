package fadulousbms.model;

import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ApplicationObjectManager;

import java.io.Serializable;

/**
 * Created by th3gh0st on 2017/02/01.
 * @author th3gh0st
 */
public class AssetType extends Type implements Serializable
{
    public AssetType(String type_name, String type_description)
    {
        super(type_name, type_description);
    }

    @Override
    public ApplicationObjectManager getManager()
    {
        return AssetManager.getInstance();
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/asset/type";
    }
}
