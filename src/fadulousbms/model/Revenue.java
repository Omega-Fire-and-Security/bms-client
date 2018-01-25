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
public class Revenue extends BusinessObject implements Serializable
{
    private String revenue_title;
    private String revenue_description;
    private double revenue_value;
    private String account;
    public static final String TAG = "Revenue";

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

    public StringProperty accountProperty(){return new SimpleStringProperty(account);}

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
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
                case "account":
                    account = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR,"unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "revenue_title":
                return revenue_title;
            case "revenue_description":
                return revenue_description;
            case "revenue_value":
                return revenue_value;
            case "account":
                return account;
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
            result.append(URLEncoder.encode("revenue_title","UTF-8") + "="
                    + URLEncoder.encode(revenue_title, "UTF-8"));
            result.append("&" + URLEncoder.encode("revenue_description","UTF-8") + "="
                    + URLEncoder.encode(revenue_description, "UTF-8"));
            result.append("&" + URLEncoder.encode("revenue_value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(revenue_value), "UTF-8"));
            if(getDate_logged()>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(getCreator(), "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
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
    public String toString()
    {
        String super_json = super.toString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"revenue_title\":\""+getRevenue_title()+"\""
                +",\"revenue_description\":\""+getRevenue_description()+"\""
                +",\"revenue_value\":\""+getRevenue_value()+"\""
                +",\"account\":\""+getAccount()+"\"}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/revenues";
    }
}
