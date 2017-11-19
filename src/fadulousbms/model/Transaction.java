package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Comparator;

/**
 * Created by ghost on 2017/01/13.
 */
public class Transaction implements Comparable<Transaction>
{
    private String _id;
    private long date;
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

    public long getDate()
    {
        return date;
    }

    public void setDate(long date){this.date=date;}

    public BusinessObject getBusinessObject()
    {
        return businessObject;
    }

    public void setBusinessObject(BusinessObject businessObject)
    {
        this.businessObject = businessObject;
    }

    @Override
    public int compareTo(Transaction o)
    {
        /*if(getDate()>o.getDate())
            return 1;
        if(getDate()<o.getDate())
            return 2;
        return 0;*/
        return (int)(getDate() - o.getDate());
    }
}
