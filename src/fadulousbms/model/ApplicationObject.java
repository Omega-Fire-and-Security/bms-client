package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Link;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.EmployeeManager;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by th3gh0st on 2017/01/04.
 * @author th3gh0st
 */
public abstract class ApplicationObject implements Serializable
{
    private String _id;
    private long date_logged;
    private String creator;
    private String other;
    private long object_number;
    private boolean marked;
    private Link _links;
    public static final int STATUS_PENDING =0;
    public static final int STATUS_FINALISED =1;
    public static final int STATUS_ARCHIVED =2;

    //Read/Write permissions
    //public static final int READ_MIN_ACCESS_LEVEL = AccessLevel.STANDARD.getLevel();
    //public static final int WRITE_MIN_ACCESS_LEVEL = AccessLevel.STANDARD.getLevel();

    public abstract AccessLevel getReadMinRequiredAccessLevel();

    public abstract AccessLevel getWriteMinRequiredAccessLevel();

    public abstract ApplicationObjectManager getManager();

    /**
     * Function to get identifier of Quote object.
     * @return Quote identifier.
     */
    public String get_id()
    {
        return _id;
    }

    /**
     * Method to assign identifier to this object.
     * @param _id identifier to be assigned to this object.
     */
    public ApplicationObject set_id(String _id)
    {
        this._id = _id;
        return this;
    }

    public long getDate_logged()
    {
        return this.date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public String getCreator()
    {
        return this.creator;
    }

    public Employee getCreatorEmployee()
    {
        EmployeeManager.getInstance().initialize();
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getDataset();
        if(employees!=null)
            return employees.get(getCreator());
        return null;
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public long getObject_number()
    {
        return object_number;
    }

    public void setObject_number(long object_number)
    {
        this.object_number = object_number;
    }

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }

    public boolean isMarked()
    {
        return marked;
    }

    public void setMarked(boolean marked){this.marked=marked;}

    public void set_links(Link links)
    {
        this._links=links;
    }

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    public SimpleLongProperty object_numberProperty(){return new SimpleLongProperty(object_number);}

    /**
     * Function to get a shortened identifier of this object.
     * @return The shortened identifier
     */
    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    public SimpleStringProperty creator_nameProperty()
    {
        if(getCreatorEmployee()!=null)
            return new SimpleStringProperty(getCreatorEmployee().getName());
        else return new SimpleStringProperty(getCreator());
    }

    public StringProperty creatorProperty()
    {
        return new SimpleStringProperty(String.valueOf(getCreator()));
    }

    public void parse(String var, Object val) throws ParseException
    {
        switch (var.toLowerCase())
        {
            case "_id":
                set_id(String.valueOf(val));
                break;
            case "object_number":
                setObject_number(Long.parseLong(String.valueOf(val)));
                break;
            case "date_logged":
                setDate_logged(Long.parseLong(String.valueOf(val)));
                break;
            case "creator":
                setCreator(String.valueOf(val));
                break;
            case "other":
                setOther(String.valueOf(val));
                break;
            case "marked":
                setMarked(Boolean.valueOf((String) val));
                break;
        }
    }

    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "_id":
                return get_id();
            case "object_number":
                return getObject_number();
            case "date_logged":
                return getDate_logged();
            case "creator":
                return getCreator();
            case "marked":
                return isMarked();
            case "other":
                return getOther();
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown "+getClass().getName()+" attribute '" + var + "'.");
                return null;
        }
    }

    public abstract String apiEndpoint();

    public String getJSONString()
    {
        String json_obj = "{"+(get_id()!=null?"\"_id\":\""+get_id()+"\",":"")
                +"\"object_number\":\""+getObject_number()+"\"";
        if(getCreator()!=null)
            json_obj+=",\"creator\":\""+getCreator()+"\"";
        if(getDate_logged()>0)
            json_obj+=",\"date_logged\":\""+getDate_logged()+"\"";
        json_obj+=",\"other\":\""+getOther()+"\"}";

        //IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof ApplicationObject)
            if(((ApplicationObject)o).get_id()==null)
                return false;
            else return ((ApplicationObject)o).get_id().equals(get_id());
        else return false;
    }

    @Override
    public int hashCode()
    {
        if(get_id()==null)
            return 0;
        return get_id().hashCode();
    }
}