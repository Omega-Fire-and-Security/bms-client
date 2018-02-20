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
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author ghost
 */
public class ServiceItem extends BusinessObject implements Serializable
{
    private String service_id;
    private String item_name;
    private String item_description;
    private double item_rate;
    private long quantity;
    private String unit;
    public static final String TAG = "ServiceItem";

    public String getService_id()
    {
        return service_id;
    }

    public void setService_id(String service_id)
    {
        this.service_id = service_id;
    }

    public String getItem_name()
    {
        return item_name;
    }

    public void setItem_name(String item_name)
    {
        this.item_name = item_name;
    }

    public String getItem_description()
    {
        return item_description;
    }

    public void setItem_description(String description)
    {
        this.item_description = description;
    }

    public double getItem_rate()
    {
        return item_rate;
    }

    public void setItem_rate(double item_rate)
    {
        this.item_rate = item_rate;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public long getQuantity()
    {
        return quantity;
    }

    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }

    //Properties

    public StringProperty service_idProperty(){return new SimpleStringProperty(service_id);}
    public StringProperty item_nameProperty(){return new SimpleStringProperty(item_name);}
    public StringProperty item_descriptionProperty(){return new SimpleStringProperty(item_description);}
    public StringProperty item_rateProperty(){return new SimpleStringProperty(String.valueOf(item_rate));}
    public StringProperty unitProperty(){return new SimpleStringProperty(unit);}
    public StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "service_id":
                    service_id = (String)val;
                    break;
                case "item_name":
                    item_name = (String)val;
                    break;
                case "item_description":
                    item_description = (String)val;
                    break;
                case "item_rate":
                    item_rate = Double.parseDouble(String.valueOf(val));
                    break;
                case "quantity":
                    quantity = Long.parseLong(String.valueOf(val));
                    break;
                case "unit":
                    unit = String.valueOf(val);
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
            case "service_id":
                return getService_id();
            case "name":
            case "item_name":
                return getItem_name();
            case "description":
            case "item_description":
                return getItem_description();
            case "cost":
            case "value":
            case "item_rate":
                return getItem_rate();
            case "quantity":
                return quantity;
            case "unit":
                return unit;
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"service_id\":\""+getService_id()+"\""
                +",\"item_name\":\""+getItem_name()+"\""
                +",\"item_description\":\""+getItem_description()+"\""
                +",\"item_rate\":\""+getItem_rate()+"\""
                +",\"unit\":\""+getUnit()+"\"";
        if(getQuantity()>0)
            json_obj+=",\"quantity\":\""+getQuantity()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        return getItem_name();
    }

    @Override
    public String apiEndpoint()
    {
        return "/services/items";
    }
}
