package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/03.
 */
public class InvoiceRep extends BusinessObject implements Serializable
{
    private String invoice_id;
    private String usr;
    public static final String TAG = "InvoiceRep";

    private StringProperty invoice_idProperty(){return new SimpleStringProperty(invoice_id);}

    public String getInvoice_id()
    {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id)
    {
        this.invoice_id = invoice_id;
    }

    private StringProperty usrProperty(){return new SimpleStringProperty(usr);}

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String employee_id)
    {
        this.usr = usr;
    }


    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "invoice_id":
                    invoice_id = String.valueOf(val);
                    break;
                case "usr":
                    usr = String.valueOf(val);
                    break;
                default:
                    System.err.println("Unknown InvoiceRep attribute '" + var + "'.");
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
            case "invoice_id":
                return invoice_id;
            case "usr":
                return usr;
            default:
                System.err.println("Unknown InvoiceRep attribute '" + var + "'.");
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
            result.append(URLEncoder.encode("invoice_id","UTF-8") + "="
                    + URLEncoder.encode(invoice_id, "UTF-8") + "&");
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
        return "/invoice/rep";
    }
}
