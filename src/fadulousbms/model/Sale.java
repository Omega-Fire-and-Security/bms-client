package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/18.
 */
public class Sale implements BusinessObject
{
    private String _id;
    private String creator;
    private long date_logged;
    private String quote_id;
    private String invoice_id;
    private boolean marked;
    private Employee creator_employee;
    private Quote quote;
    //private Invoice invoice;

    public Sale(String creator, String quote_id)
    {
        this.creator = creator;
        this.quote_id = quote_id;
    }

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

    public StringProperty creatorProperty()
    {
        if(creator_employee==null)
            return new SimpleStringProperty(String.valueOf(creator));
        else return new SimpleStringProperty(String.valueOf(creator_employee.getFirstname()+" "+creator_employee.getLastname()));
    }

    public String getCreator()
    {
        return creator;
    }

    public void setCreator(String sale_description)
    {
        this.creator = creator;
    }

    public Employee getCreatorEmployee(){return creator_employee;}

    public void setCreatorEmployee(Employee employee){creator_employee=employee;}

    /*public StringProperty date_loggedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_logged));
    }*/

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public StringProperty invoice_idProperty()
    {
        return new SimpleStringProperty(invoice_id);
    }

    public String getInvoice_id()
    {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id)
    {
        this.invoice_id = invoice_id;
    }

    public StringProperty quote_idProperty()
    {
        return new SimpleStringProperty(quote_id);
    }

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }

    public Quote getQuote(){return quote;}

    public void setQuote(Quote quote){this.quote=quote;}

    public SimpleStringProperty clientProperty(){
        if(quote!=null)
            if(quote.getClient()!=null)
                return new SimpleStringProperty(quote.getClient().getClient_name());
            else return new SimpleStringProperty(quote.getClient_id());
        else return new SimpleStringProperty("N/A");
    }

    public SimpleStringProperty sitenameProperty(){
        if(quote!=null)
            return new SimpleStringProperty(quote.getSitename());
        else return new SimpleStringProperty("N/A");
    }

    public SimpleStringProperty contactProperty(){
        if(quote!=null)
            if(quote.getContact_person()!=null)
                return new SimpleStringProperty(quote.getContact_person().toString());
            else return new SimpleStringProperty(quote.getContact_person_id());
        else return new SimpleStringProperty("N/A");
    }

    public SimpleStringProperty totalProperty(){
        if(quote!=null)
            return quote.totalProperty();
        else return new SimpleStringProperty("N/A");
    }

    public SimpleStringProperty quoteProperty(){
        if(quote!=null)
            return quote.quoteProperty();
        else return new SimpleStringProperty("N/A");
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "creator":
                    creator = (String)val;
                    break;
                case "date_logged":
                    date_logged = Long.parseLong(String.valueOf(val));
                    break;
                case "invoice_id":
                    invoice_id = (String)val;
                    break;
                case "quote_id":
                    quote_id = (String) val;
                    break;
                default:
                    System.err.println("Unknown Sale attribute '" + var + "'.");
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
            case "creator":
                return creator;
            case "date_logged":
                return date_logged;
            case "invoice_id":
                return invoice_id;
            case "quote_id":
                return quote_id;
            default:
                System.err.println("Unknown Sale attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/sale";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            result.append("&" + URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(quote_id, "UTF-8"));
            if(invoice_id!=null)
                result.append(URLEncoder.encode("invoice_id","UTF-8") + "="
                        + URLEncoder.encode(invoice_id, "UTF-8") + "&");

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }
}
