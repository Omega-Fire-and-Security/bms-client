package fadulousbms.model;

import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.ResourceManager;

/**
 * Created by th3gh0st on 2017/01/13.
 * @author th3gh0st
 */
public class ResourceType extends Type
{
    public ResourceType(String type_name, String type_description)
    {
        super(type_name, type_description);
    }

    @Override
    public ApplicationObjectManager getManager()
    {
        return ResourceManager.getInstance();
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/resource/type";
    }
}
