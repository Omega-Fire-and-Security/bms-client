package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by ghost on 2017/01/13.
 */
public class Transaction implements BusinessObject, Serializable
{
    private String _id;
    private long date;
    private boolean marked;
    private BusinessObject businessObject;

    public Transaction(String id, long date, BusinessObject businessObject)
    {
        this._id = id;
        this.date = date;
        this.businessObject = businessObject;
    }

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

    public long getDate()
    {
        return date;
    }

    public void setDate(long date){this.date=date;}

    @Override
    public void parse(String var, Object val)
    {
        switch (var)
        {
            case "id":
            case "_id":
                set_id((String)val);
                break;
            case "date":
                setDate(Long.valueOf(String.valueOf(val)));
                break;
            case "marked":
                setMarked(Boolean.parseBoolean((String)val));
                break;
            case "business_object":
            case "businessobject":
                setBusinessObject((BusinessObject)businessObject);
                break;
            default:
                IO.log(getClass().getName(), IO.TAG_WARN, "unknown " +getClass().getName()+ " attribute ["+var+"], ignoring.");
                break;
        }
    }

    public BusinessObject getBusinessObject()
    {
        return businessObject;
    }

    public void setBusinessObject(BusinessObject businessObject)
    {
        this.businessObject = businessObject;
    }

    /*@Override
    public int compareTo(Transaction o)
    {
        /*if(getDate()>o.getDate())
            return 1;
        if(getDate()<o.getDate())
            return 2;
        return 0;*
        return (int)(getDate() - o.getDate());
    }*/

    @Override
    public Object get(String var)
    {
        switch (var)
        {
            case "id":
            case "_id":
                get_id();
                break;
            case "date":
                getDate();
                break;
            case "marked":
                isMarked();
                break;
            case "business_object":
            case "businessobject":
                getBusinessObject();
                break;
            default:
                IO.log(getClass().getName(), IO.TAG_WARN, "unknown " +getClass().getName()+ " attribute ["+var+"], ignoring.");
                break;
        }
        return null;
    }

    /**
     * Not a server side model so no need for UTF encoded string.
     * @return null.
     */
    @Override
    public String asUTFEncodedString()
    {
        return null;
    }

    /**
     * Not a server side model so no need for API endpoint value.
     * @return null.
     */
    @Override
    public String apiEndpoint()
    {
        return null;
    }
}
