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
public class Resource extends BusinessObject implements Serializable
{
    private String resource_name;
    private String resource_description;
    private String resource_serial;
    private String resource_type;
    private double resource_value;
    private long quantity;
    private long date_acquired;
    private long date_exhausted;
    private String unit;
    public static final String TAG = "Resource";

    public StringProperty resource_nameProperty(){return new SimpleStringProperty(resource_name);}

    public String getResource_name()
    {
        return resource_name;
    }

    public void setResource_name(String resource_name)
    {
        this.resource_name = resource_name;
    }

    public StringProperty resource_descriptionProperty(){return new SimpleStringProperty(resource_description);}

    public String getResource_description()
    {
        return resource_description;
    }

    public void setResource_description(String description)
    {
        this.resource_description = description;
    }

    public StringProperty resource_serialProperty(){return new SimpleStringProperty(resource_serial);}

    public String getResource_serial()
    {
        return resource_serial;
    }

    public void setResource_serial(String resource_serial)
    {
        this.resource_serial = resource_serial;
    }

    public StringProperty resource_typeProperty(){return new SimpleStringProperty(resource_type);}

    public String getResource_type()
    {
        return resource_type;
    }

    public void setResource_type(String resource_type)
    {
        this.resource_type = resource_type;
    }

    public StringProperty resource_valueProperty(){return new SimpleStringProperty(String.valueOf(resource_value));}

    public double getResource_value()
    {
        return resource_value;
    }

    public void setResource_value(double resource_value)
    {
        this.resource_value = resource_value;
    }

    public StringProperty unitProperty(){return new SimpleStringProperty(unit);}

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}

    public long getQuantity()
    {
        return quantity;
    }

    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }

    //public StringProperty date_acquiredProperty(){return new SimpleStringProperty(String.valueOf(date_acquired));}

    public long getDate_acquired()
    {
        return date_acquired;
    }

    public void setDate_acquired(long date_acquired)
    {
        this.date_acquired = date_acquired;
    }

    //public StringProperty date_exhaustedProperty(){return new SimpleStringProperty(String.valueOf(date_exhausted));}

    public long getDate_exhausted()
    {
        return date_exhausted;
    }

    public void setDate_exhausted(long date_exhausted)
    {
        this.date_exhausted = date_exhausted;
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "resource_name":
                    resource_name = (String)val;
                    break;
                case "resource_type":
                    resource_type = (String)val;
                    break;
                case "resource_description":
                    resource_description = (String)val;
                    break;
                case "resource_serial":
                    resource_serial = (String)val;
                    break;
                case "resource_value":
                    resource_value = Double.parseDouble(String.valueOf(val));
                    break;
                case "date_acquired":
                    date_acquired = Long.parseLong(String.valueOf(val));
                    break;
                case "date_exhausted":
                    date_exhausted = Long.parseLong(String.valueOf(val));
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
            case "name":
            case "resource_name":
                return getResource_name();
            case "resource_type":
                return resource_type;
            case "resource_description":
                return resource_description;
            case "resource_serial":
                return resource_serial;
            case "cost":
            case "value":
            case "resource_value":
                return getResource_value();
            case "date_acquired":
                return date_acquired;
            case "date_exhausted":
                return date_exhausted;
            case "quantity":
                return quantity;
            case "unit":
                return unit;
        }
        return super.get(var);
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("resource_name","UTF-8") + "="
                    + URLEncoder.encode(resource_name, "UTF-8"));
            result.append("&" + URLEncoder.encode("resource_type","UTF-8") + "="
                    + URLEncoder.encode(resource_type, "UTF-8"));
            result.append("&" + URLEncoder.encode("resource_description","UTF-8") + "="
                    + URLEncoder.encode(resource_description, "UTF-8"));
            result.append("&" + URLEncoder.encode("resource_serial","UTF-8") + "="
                    + URLEncoder.encode(resource_serial, "UTF-8"));
            result.append("&" + URLEncoder.encode("resource_value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(resource_value), "UTF-8"));
            if(date_acquired>0)
                result.append("&" + URLEncoder.encode("date_acquired","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_acquired), "UTF-8"));
            if(date_exhausted>0)
                result.append("&" + URLEncoder.encode("date_exhausted","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_exhausted), "UTF-8"));
            result.append("&" + URLEncoder.encode("unit","UTF-8") + "="
                    + URLEncoder.encode(unit, "UTF-8"));
            result.append("&" + URLEncoder.encode("quantity","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quantity), "UTF-8"));
            if(getOther()!=null)
                if(!getOther().isEmpty())
                    result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                            + URLEncoder.encode(getOther(), "UTF-8"));

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"resource_name\":\""+getResource_name()+"\""
                +",\"resource_description\":\""+getResource_description()+"\""
                +",\"resource_value\":\""+getResource_value()+"\""
                +",\"resource_serial\":\""+getResource_serial()+"\""
                +",\"resource_type\":\""+getResource_type()+"\""
                +",\"unit\":\""+getUnit()+"\"";
        if(getQuantity()>0)
            json_obj+=",\"quantity\":\""+getQuantity()+"\"";
        if(getDate_acquired()>0)
            json_obj+=",\"date_acquired\":\""+getDate_acquired()+"\"";
        if(getDate_exhausted()>0)
            json_obj+=",\"date_exhausted\":\""+getDate_exhausted()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/resources";
    }
}
