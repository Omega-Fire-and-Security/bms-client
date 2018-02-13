package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by ghost on 2017/01/13.
 */
public class Transaction extends BusinessObject
{
    private long date;
    private BusinessObject businessObject;

    public Transaction(String id, long date, BusinessObject businessObject)
    {
        super.set_id(id);
        this.date = date;
        this.businessObject = businessObject;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date){this.date=date;}

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        switch (var)
        {
            case "date":
                setDate(Long.valueOf(String.valueOf(val)));
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
        switch (var.toLowerCase())
        {
            case "date":
                getDate();
                break;
            case "business_object":
            case "businessobject":
                getBusinessObject();
                break;
            default:
                IO.log(getClass().getName(), IO.TAG_WARN, "unknown " +getClass().getName()+ " attribute ["+var+"], ignoring.");
                break;
        }
        return super.get(var);
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
