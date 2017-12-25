package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/13.
 */
public class Type extends BusinessObject implements Serializable
{
    private String type_name;
    private String type_description;
    private String other;

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

    public StringProperty otherProperty(){return new SimpleStringProperty(other);}

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }

    @Override
    public void parse(String var, Object val)
    {
        switch (var.toLowerCase())
        {
            case "type_name":
                type_name = (String)val;
                break;
            case "type_description":
                type_description = (String)val;
                break;
            case "other":
                other = (String)val;
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
            case "other":
                return other;
            default:
                IO.log(getClass().getName(), "Unknown attribute '" + var + "'.", IO.TAG_ERROR);
                return null;
        }
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("type_name","UTF-8") + "="
                    + URLEncoder.encode(type_name, "UTF-8"));
            result.append("&" + URLEncoder.encode("type_description","UTF-8") + "="
                    + URLEncoder.encode(type_description, "UTF-8"));
            if(other!=null)
                result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(other, "UTF-8"));

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
        return null;
    }

    @Override
    public String toString()
    {
        return this.type_name;
    }

    @Override
    public String apiEndpoint()
    {
        return "/resource/type";
    }
}
