package fadulousbms.model;

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
    public String apiEndpoint()
    {
        return "/resources/types";
    }
}
