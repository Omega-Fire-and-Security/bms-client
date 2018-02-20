package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.ServiceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.HashMap;

/**
 * Created by ghost on 2017/02/03.
 */
public class QuoteService extends BusinessObject
{
    private String quote_id;
    private String service_id;
    public static final String TAG = "QuoteService";

    private StringProperty quote_idProperty(){return new SimpleStringProperty(quote_id);}

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String job_id)
    {
        this.quote_id = quote_id;
    }

    private StringProperty service_idProperty(){return new SimpleStringProperty(service_id);}

    public String getService_id()
    {
        return service_id;
    }

    public void setService_id(String service_id)
    {
        this.service_id = service_id;
    }

    public Quote getQuote()
    {
        HashMap<String, Quote> quotes = QuoteManager.getInstance().getDataset();
        if(quotes!=null)
            return quotes.get(getQuote_id());
        return null;
    }

    public Service getService()
    {
        HashMap<String, Service> services = ServiceManager.getInstance().getDataset();
        if(services!=null)
            return services.get(getService_id());
        return null;
    }

    //Properties

    public StringProperty service_titleProperty()
    {
        Service service = getService();
        if(service!=null)
            return new SimpleStringProperty(service.getService_title());
        else return new SimpleStringProperty("N/A");
    }

    public StringProperty service_descriptionProperty()
    {
        Service service = getService();
        if(service!=null)
            return new SimpleStringProperty(service.getService_description());
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
                case "quote_id":
                    quote_id = String.valueOf(val);
                    break;
                case "service_id":
                    service_id = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "quote_id":
                return quote_id;
            case "service_id":
                return service_id;
        }
        return super.get(var);
    }

    /**
     * @return JSON representation of JobEmployee object.
     */
    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"quote_id\":\""+quote_id+"\""
                +",\"service_id\":\""+service_id+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/quotes/services";
    }
}
