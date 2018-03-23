package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by ghost on 2017/01/13.
 * @author ghost
 */
public class Type extends BusinessObject implements Serializable
{
    private String type_name;
    private String type_description;

    public Type(String type_name, String type_description)
    {
        setType_name(type_name);
        setType_description(type_description);
    }

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    public StringProperty type_nameProperty(){return new SimpleStringProperty(type_name);}

    public String getType_name()
    {
        return type_name;
    }

    public void setType_name(String type_name)
    {
        this.type_name = type_name;
    }

    public StringProperty type_descriptionProperty(){return new SimpleStringProperty(type_description);}

    public String getType_description()
    {
        return type_description;
    }

    public void setType_description(String type_description)
    {
        this.type_description = type_description;
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        switch (var.toLowerCase())
        {
            case "type_name":
                type_name = (String)val;
                break;
            case "type_description":
                type_description = (String)val;
                break;
            default:
                IO.log(getClass().getName(), "Unknown attribute '" + var + "'.", IO.TAG_ERROR);
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "type_name":
                return type_name;
            case "type_description":
                return type_description;
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"type_name\":\""+getType_name()+"\""
                +",\"type_description\":\""+getType_description()+"\"}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        return getType_name();
    }

    @Override
    public String apiEndpoint()
    {
        return "/resources/types";
    }
}
