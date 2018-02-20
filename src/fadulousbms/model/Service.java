/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author ghost
 */
public class Service extends BusinessObject implements Serializable
{
    private String service_title;
    private String service_description;
    private HashMap<String, ServiceItem> serviceItems = new HashMap<>();
    public static final String TAG = "Service";

    public String getService_title()
    {
        return service_title;
    }

    public void setService_title(String service_title)
    {
        this.service_title = service_title;
    }

    public String getService_description()
    {
        return service_description;
    }

    public void setService_description(String description)
    {
        this.service_description = service_description;
    }

    public HashMap<String, ServiceItem> getServiceItemsMap()
    {
        return serviceItems;
    }

    //Properties

    public StringProperty service_titleProperty(){return new SimpleStringProperty(service_title);}
    public StringProperty service_descriptionProperty(){return new SimpleStringProperty(service_description);}

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "service_title":
                    service_title = (String)val;
                    break;
                case "service_description":
                    service_description = (String)val;
                    break;
                default:
                    IO.log(TAG, IO.TAG_ERROR,"Unknown "+getClass().getName()+" attribute '" + var + "'.");
                    break;
            }
        } catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "title":
            case "service_title":
                return getService_title();
            case "description":
            case "service_description":
                return getService_description();
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"service_title\":\""+getService_title()+"\""
                +",\"service_description\":\""+getService_description()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        return getService_title();
    }

    @Override
    public String apiEndpoint()
    {
        return "/services";
    }
}
