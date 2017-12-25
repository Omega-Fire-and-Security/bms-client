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
public class InvoiceResource implements BusinessObject, Serializable
{
    private String _id;
    private String invoice_id;
    private String resource_id;
    private double markup;
    private boolean marked;
    public static final String TAG = "InvoiceResource";

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

    private StringProperty invoice_idProperty(){return new SimpleStringProperty(invoice_id);}

    public String getInvoice_id()
    {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id)
    {
        this.invoice_id = invoice_id;
    }

    private StringProperty resource_idProperty(){return new SimpleStringProperty(resource_id);}

    public String getResource_id()
    {
        return resource_id;
    }

    public void setResource_id(String resource_id)
    {
        this.resource_id = resource_id;
    }

    private StringProperty markupProperty(){return new SimpleStringProperty(String.valueOf(markup));}

    public double getMarkup()
    {
        return markup;
    }

    public void setMarkup(double markup)
    {
        this.markup = markup;
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
                case "resource_id":
                    resource_id = String.valueOf(val);
                    break;
                case "markup":
                    markup = Double.parseDouble((String) val);
                    break;
                default:
                    System.err.println("Unknown InvoiceResource attribute '" + var + "'.");
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
            case "resource_id":
                return resource_id;
            case "markup":
                return markup;
            default:
                System.err.println("Unknown InvoiceResource attribute '" + var + "'.");
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
            result.append(URLEncoder.encode("resource_id","UTF-8") + "="
                    + URLEncoder.encode(resource_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("markup","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(markup), "UTF-8") + "&");

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
        return "/invoice/resource";
    }
}
