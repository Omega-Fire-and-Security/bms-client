package fadulousbms.model;

import fadulousbms.managers.BusinessObjectManager;
import fadulousbms.managers.ResourceManager;

/**
 * Created by ghost on 2017/01/13.
 */
public class ResourceType extends Type
{
    public ResourceType(String type_name, String type_description)
    {
        super(type_name, type_description);
    }

    @Override
    public BusinessObjectManager getManager()
    {
        return ResourceManager.getInstance();
    }

    @Override
    public String apiEndpoint()
    {
        return "/resource/type";
    }
}
