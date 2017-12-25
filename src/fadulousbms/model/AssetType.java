package fadulousbms.model;

import java.io.Serializable;

/**
 * Created by ghost on 2017/02/01.
 */
public class AssetType extends Type implements Serializable
{
    @Override
    public String apiEndpoint()
    {
        return "/assets/types";
    }
}
