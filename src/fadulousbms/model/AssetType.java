package fadulousbms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/01.
 */
public class AssetType extends Type implements BusinessObject, Serializable
{
    @Override
    public String apiEndpoint()
    {
        return "/api/asset/type";
    }
}
