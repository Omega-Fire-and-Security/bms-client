package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.ApplicationObjectManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by th3gh0st on 2017/02/01.
 * @author th3gh0st
 */
public class Asset extends ApplicationObject
{
    private String asset_name;
    private String asset_description;
    private String asset_serial;
    private String asset_type;
    private double asset_value;
    private long date_acquired;
    private long date_exhausted;
    private long quantity;
    private String unit;

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.ADMIN;
    }

    @Override
    public ApplicationObjectManager getManager()
    {
        return AssetManager.getInstance();
    }

    public String getAsset_name()
    {
        return asset_name;
    }

    public void setAsset_name(String asset_name)
    {
        this.asset_name = asset_name;
    }

    public String getAsset_description()
    {
        return asset_description;
    }

    public void setAsset_description(String asset_description)
    {
        this.asset_description = asset_description;
    }

    public String getAsset_serial()
    {
        return asset_serial;
    }

    public void setAsset_serial(String asset_serial)
    {
        this.asset_serial = asset_serial;
    }

    public String getAsset_type()
    {
        return asset_type;
    }

    public void setAsset_type(String asset_type)
    {
        this.asset_type = asset_type;
    }

    public double getAsset_value()
    {
        return asset_value;
    }

    public void setAsset_value(double asset_value)
    {
        this.asset_value = asset_value;
    }

    public void setDate_acquired(long date_acquired)
    {
        this.date_acquired = date_acquired;
    }

    public long getDate_acquired()
    {
        return date_acquired;
    }

    public long getDate_exhausted()
    {
        return date_exhausted;
    }

    public long getQuantity()
    {
        return quantity;
    }

    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }

    public String getUnit()
    {
        return unit;
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public void setDate_exhausted(long date_exhausted)
    {
        this.date_exhausted = date_exhausted;
    }

    //model properties

    public StringProperty asset_nameProperty(){return new SimpleStringProperty(asset_name);}
    public StringProperty asset_descriptionProperty(){return new SimpleStringProperty(asset_description);}
    public StringProperty asset_serialProperty(){return new SimpleStringProperty(asset_serial);}
    public StringProperty asset_typeProperty(){return new SimpleStringProperty(asset_type);}
    public StringProperty asset_valueProperty(){return new SimpleStringProperty(String.valueOf(Globals.CURRENCY_SYMBOL.getValue() + " " + getAsset_value()));}
    //public StringProperty date_exhaustedProperty(){return new SimpleStringProperty(String.valueOf(date_exhausted));}
    //public StringProperty date_acquiredProperty(){return new SimpleStringProperty(String.valueOf(date_acquired));}
    public StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}
    public StringProperty unitProperty(){return new SimpleStringProperty(unit);}

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        switch (var.toLowerCase())
        {
            case "asset_name":
                asset_name = (String)val;
                break;
            case "asset_description":
                asset_description = (String)val;
                break;
            case "asset_serial":
                asset_serial = (String)val;
                break;
            case "asset_type":
                asset_type = (String)val;
                break;
            case "asset_value":
                asset_value = Double.parseDouble(String.valueOf(val));
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
                unit = (String)val;
                break;
            default:
                IO.log(getClass().getName(), "Unknown "+getClass().getName()+" attribute '" + var + "'.", IO.TAG_ERROR);
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "name":
            case "asset_name":
                return getAsset_name();
            case "asset_type":
                return asset_type;
            case "description":
            case "asset_description":
                return getAsset_description();
            case "asset_serial":
                return asset_serial;
            case "cost":
            case "value":
            case "asset_value":
                return getAsset_value();
            case "date_acquired":
                return date_acquired;
            case "date_exhausted":
                return date_exhausted;
            case "quantity":
                return quantity;
            case "unit":
                return unit;
        }
        return this.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"asset_name\":\""+getAsset_name()+"\""
                +",\"asset_description\":\""+getAsset_description()+"\""
                +",\"asset_value\":\""+getAsset_value()+"\""
                +",\"asset_serial\":\""+getAsset_serial()+"\""
                +",\"unit\":\""+getUnit()+"\""
                +",\"asset_type\":\""+getAsset_type()+"\""
                +",\"quantity\":\""+getQuantity()+"\"";
        if(getDate_acquired()>0)
            json_obj+=",\"date_acquired\":\""+getDate_acquired()+"\"";
        if(getDate_exhausted()>0)
            json_obj+=",\"date_exhausted\":\""+getDate_exhausted()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/asset";
    }
}
