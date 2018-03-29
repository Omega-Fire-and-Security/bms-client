package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * Created by th3gh0st on 2017/01/13.
 * @author th3gh0st
 */
public class Transaction extends ApplicationObject
{
    private long date;
    private ApplicationObject applicationObject;

    public Transaction(String id, long date, ApplicationObject applicationObject)
    {
        super.set_id(id);
        this.date = date;
        this.applicationObject = applicationObject;
    }

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.ADMIN;
    }

    @Override
    public ApplicationObjectManager getManager()
    {
        throw new NotImplementedException();
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date){this.date=date;}

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        switch (var)
        {
            case "date":
                setDate(Long.valueOf(String.valueOf(val)));
                break;
            case "business_object":
            case "businessobject":
                setApplicationObject(applicationObject);
                break;
            default:
                IO.log(getClass().getName(), IO.TAG_WARN, "unknown " +getClass().getName()+ " attribute ["+var+"], ignoring.");
                break;
        }
    }

    public ApplicationObject getApplicationObject()
    {
        return applicationObject;
    }

    public void setApplicationObject(ApplicationObject applicationObject)
    {
        this.applicationObject = applicationObject;
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
                getApplicationObject();
                break;
            default:
                IO.log(getClass().getName(), IO.TAG_WARN, "unknown " +getClass().getName()+ " attribute ["+var+"], ignoring.");
                break;
        }
        return super.get(var);
    }

    @Override
    public String toString()
    {
        return get_id();
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
