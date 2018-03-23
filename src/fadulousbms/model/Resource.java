/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.SupplierManager;
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
    //private String brand_name;
    private String resource_description;
    private String resource_code;
    private String resource_type;
    private double resource_value;
    private long quantity;
    private long date_acquired;
    private long date_exhausted;
    private String unit;
    private String supplier_id;
    private String part_number;
    public static final String TAG = "Resource";

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

    public String getResource_description()
    {
        return resource_description;
    }

    public void setResource_description(String description)
    {
        this.resource_description = description;
    }

    public String getResource_code()
    {
        return resource_code;
    }

    public void setResource_code(String resource_code)
    {
        this.resource_code = resource_code;
    }

    public String getResource_type()
    {
        return resource_type;
    }

    public String getResourceType()
    {
        if(ResourceManager.getInstance().getResource_types()!=null && getResource_type()!=null)
            return ResourceManager.getInstance().getResource_types().get(getResource_type()).getType_name();
        else return getResource_type();
    }

    public void setResource_type(String resource_type)
    {
        this.resource_type = resource_type;
    }

    public double getResource_value()
    {
        return resource_value;
    }

    public void setResource_value(double resource_value)
    {
        this.resource_value = resource_value;
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

    public long getDate_acquired()
    {
        return date_acquired;
    }

    public void setDate_acquired(long date_acquired)
    {
        this.date_acquired = date_acquired;
    }

    public long getDate_exhausted()
    {
        return date_exhausted;
    }

    public void setDate_exhausted(long date_exhausted)
    {
        this.date_exhausted = date_exhausted;
    }

    public String getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public String getPart_number()
    {
        return part_number;
    }

    public void setPart_number(String part_number)
    {
        this.part_number = part_number;
    }

    //Properties

    public StringProperty resource_descriptionProperty(){return new SimpleStringProperty(resource_description);}
    public StringProperty resource_codeProperty(){return new SimpleStringProperty(resource_code);}
    public StringProperty resource_typeProperty(){return new SimpleStringProperty(getResource_type());}
    public StringProperty resource_valueProperty(){return new SimpleStringProperty(String.valueOf(resource_value));}
    public StringProperty unitProperty(){return new SimpleStringProperty(unit);}
    public StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}
    public StringProperty supplierProperty()
    {
        if(getSupplier_id()!=null)
            if(SupplierManager.getInstance().getDataset()!=null)
                return new SimpleStringProperty(String.valueOf(SupplierManager.getInstance().getDataset().get(getSupplier_id())));
            else return new SimpleStringProperty("N/A");
        else return new SimpleStringProperty("N/A");
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "resource_type":
                    resource_type = (String)val;
                    break;
                case "resource_description":
                    resource_description = (String)val;
                    break;
                case "resource_code":
                    resource_code = (String)val;
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
                case "supplier_id":
                    supplier_id = (String)val;
                    break;
                case "part_number":
                    part_number = (String)val;
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
            case "resource_type":
                return resource_type;
            case "resource_description":
                return resource_description;
            case "resource_code":
                return getResource_code();
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
            case "supplier_id":
                return getSupplier_id();
            case "part_number":
                return getPart_number();
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"resource_description\":\""+getResource_description()+"\""
                +",\"resource_value\":\""+getResource_value()+"\""
                +",\"resource_code\":\""+getResource_code()+"\""
                +",\"resource_type\":\""+getResource_type()+"\""
                +",\"unit\":\""+getUnit()+"\"";
        if(getPart_number()!=null)
            json_obj+=",\"part_number\":\""+getPart_number()+"\"";
        if(getSupplier_id()!=null)
            json_obj+=",\"supplier_id\":\""+getSupplier_id()+"\"";
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
    public String toString()
    {
        return getResource_description();
    }

    @Override
    public String apiEndpoint()
    {
        return "/resources";
    }
}
