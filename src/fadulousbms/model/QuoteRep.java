package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/29.
 */
public class QuoteRep extends BusinessObject implements Serializable
{
    private String quote_id;
    private String usr;
    public static final String TAG = "QuoteRep";

    private StringProperty quote_idProperty(){return new SimpleStringProperty(quote_id);}

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }

    private StringProperty usrProperty(){return new SimpleStringProperty(usr);}

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String employee_id)
    {
        this.usr = employee_id;
    }

    public Employee getUser()
    {
        return EmployeeManager.getInstance().getEmployees().get(getUsr());
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "quote_id":
                    quote_id = String.valueOf(val);
                    break;
                case "usr":
                    usr = String.valueOf(val);
                    break;
                default:
                    System.err.println("Unknown QuoteRep attribute '" + var + "'.");
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
            case "quote_id":
                return quote_id;
            case "usr":
                return usr;
            default:
                System.err.println("Unknown QuoteRep attribute '" + var + "'.");
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
            result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(quote_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("usr","UTF-8") + "="
                    + URLEncoder.encode(usr, "UTF-8") + "&");

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/quote/rep";
    }
}
