package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class Revenue implements BusinessObject, Serializable
{
    private String _id;
    private String revenue_title;
    private String revenue_description;
    private double revenue_value;
    private long date_logged;
    private String creator;
    private String account;
    private String other;
    private Employee creator_employee;
    private boolean marked;
    public static final String TAG = "Revenue";

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of Quote object.
     * @return Quote identifier.
     */
    @Override
    public String get_id()
    {
        return _id;
    }

    /**
     * Method to assign identifier to this object.
     * @param _id identifier to be assigned to this object.
     */
    public void set_id(String _id)
    {
        this._id = _id;
    }


    /**
     * Function to get a shortened identifier of this object.
     * @return The shortened identifier.
     */
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

    public StringProperty revenue_titleProperty()
    {
        return new SimpleStringProperty(revenue_title);
    }

    public String getRevenue_title()
    {
        return revenue_title;
    }

    public void setRevenue_title(String revenue_title)
    {
        this.revenue_title = revenue_title;
    }

    public StringProperty revenue_descriptionProperty()
    {
        return new SimpleStringProperty(revenue_description);
    }

    public String getRevenue_description()
    {
        return revenue_description;
    }

    public void setRevenue_description(String revenue_description)
    {
        this.revenue_description = revenue_description;
    }

    public StringProperty revenue_valueProperty()
    {
        return new SimpleStringProperty(String.valueOf(revenue_value));
    }

    public double getRevenue_value()
    {
        return revenue_value;
    }

    public void setRevenue_value(double revenue_value)
    {
        this.revenue_value = revenue_value;
    }

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public StringProperty creatorProperty()
    {
        if(creator_employee==null)
            return new SimpleStringProperty(String.valueOf(creator));
        else return new SimpleStringProperty(String.valueOf(creator_employee.toString()));
    }

    public String getCreator()
    {
        if(creator_employee==null)
            return creator;
        else return creator_employee.toString();
    }

    public String getCreatorID(){return this.creator;}

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public StringProperty accountProperty(){return new SimpleStringProperty(account);}

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
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


    public Employee getCreatorEmployee()
    {
        return this.creator_employee;
    }

    public void setCreator(Employee creator_employee)
    {
        this.creator_employee = creator_employee;
        if(creator_employee!=null)
            setCreator(creator_employee.getUsr());
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "revenue_title":
                    revenue_title = (String)val;
                    break;
                case "revenue_description":
                    revenue_description = (String)val;
                    break;
                case "revenue_value":
                    revenue_value = Double.parseDouble(String.valueOf(val));
                    break;
                case "date_logged":
                    date_logged = Long.parseLong(String.valueOf(val));
                    break;
                case "creator":
                    creator = String.valueOf(val);
                    break;
                case "account":
                    account = String.valueOf(val);
                    break;
                case "other":
                    other = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR,"unknown Revenue attribute '" + var + "'.");
                    break;
            }
        }catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "_id":
                return _id;
            case "revenue_title":
                return revenue_title;
            case "revenue_description":
                return revenue_description;
            case "revenue_value":
                return revenue_value;
            case "date_logged":
                return date_logged;
            case "creator":
                return creator;
            case "account":
                return account;
            case "other":
                return other;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR,"unknown Revenue attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/revenue";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("revenue_title","UTF-8") + "="
                    + URLEncoder.encode(revenue_title, "UTF-8"));
            result.append("&" + URLEncoder.encode("revenue_description","UTF-8") + "="
                    + URLEncoder.encode(revenue_description, "UTF-8"));
            result.append("&" + URLEncoder.encode("revenue_value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(revenue_value), "UTF-8"));
            if(date_logged>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
            if(other!=null)
                if(!other.isEmpty())
                    result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                            + URLEncoder.encode(other, "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String toString()
    {
        return this.revenue_title;
    }
}
