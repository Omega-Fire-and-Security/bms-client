package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/01.
 */
public class Asset implements BusinessObject, Serializable
{
    private boolean marked;
    private String _id;
    private String asset_name;
    private String asset_description;
    private String asset_serial;
    private String asset_type;
    private double asset_value;
    private long date_acquired;
    private long date_exhausted;
    private long quantity;
    private String unit;
    private String other;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    @Override
    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    @Override
    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    @Override
    public boolean isMarked()
    {
        return marked;
    }

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    public StringProperty asset_nameProperty(){return new SimpleStringProperty(asset_name);}

    public String getAsset_name()
    {
        return asset_name;
    }

    public void setAsset_name(String asset_name)
    {
        this.asset_name = asset_name;
    }

    public StringProperty asset_descriptionProperty(){return new SimpleStringProperty(asset_description);}

    public String getAsset_description()
    {
        return asset_description;
    }

    public void setAsset_description(String asset_description)
    {
        this.asset_description = asset_description;
    }

    public StringProperty asset_serialProperty(){return new SimpleStringProperty(asset_serial);}

    public String getAsset_serial()
    {
        return asset_serial;
    }

    public void setAsset_serial(String asset_serial)
    {
        this.asset_serial = asset_serial;
    }

    public StringProperty asset_typeProperty(){return new SimpleStringProperty(asset_type);}

    public String getAsset_type()
    {
        return asset_type;
    }

    public void setAsset_type(String asset_type)
    {
        this.asset_type = asset_type;
    }

    public StringProperty asset_valueProperty(){return new SimpleStringProperty(String.valueOf(Globals.CURRENCY_SYMBOL.getValue() + " " + getAsset_value()));}

    public double getAsset_value()
    {
        return asset_value;
    }

    public void setAsset_value(double asset_value)
    {
        this.asset_value = asset_value;
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

    public StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}

    public long getQuantity()
    {
        return quantity;
    }

    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
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

    public StringProperty otherProperty(){return new SimpleStringProperty(String.valueOf(other));}

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
            case "other":
                other = (String)val;
                break;
            default:
                IO.log(getClass().getName(), "Unknown Asset attribute '" + var + "'.", IO.TAG_ERROR);
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
            case "other":
                return other;
            default:
                IO.log(getClass().getName(), "Unknown Asset attribute '" + var + "'.", IO.TAG_ERROR);
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
            result.append("&" + URLEncoder.encode("asset_name","UTF-8") + "="
                    + URLEncoder.encode(asset_name, "UTF-8"));
            result.append("&" + URLEncoder.encode("asset_type","UTF-8") + "="
                    + URLEncoder.encode(asset_type, "UTF-8"));
            result.append("&" + URLEncoder.encode("asset_description","UTF-8") + "="
                    + URLEncoder.encode(asset_description, "UTF-8"));
            result.append("&" + URLEncoder.encode("asset_serial","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(asset_serial), "UTF-8"));
            result.append("&" + URLEncoder.encode("asset_value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(asset_value), "UTF-8"));
            if(date_acquired>0)
                result.append("&" + URLEncoder.encode("date_acquired","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_acquired), "UTF-8"));
            if(date_exhausted>0)
                result.append("&" +  URLEncoder.encode("date_exhausted","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_exhausted), "UTF-8"));
            result.append("&" + URLEncoder.encode("quantity","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quantity), "UTF-8"));
            result.append("&" + URLEncoder.encode("unit","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(unit), "UTF-8"));
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
    public String apiEndpoint()
    {
        return "/api/asset";
    }

    @Override
    public String toString()
    {
        return getAsset_name();
    }
}
