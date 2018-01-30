package fadulousbms.model;

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
    public String apiEndpoint()
    {
        return "/assets/types";
    }
}
