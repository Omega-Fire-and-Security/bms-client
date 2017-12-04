package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class Leave implements BusinessObject, Serializable
{
    private String _id;
    private String usr;
    private long start_date;
    private long end_date;
    private long return_date;
    private long date_logged;
    private int status;
    private String extra;
    private boolean marked;
    public static final String TAG = "Leave";
    public static final int STATUS_PENDING =0;
    public static final int STATUS_APPROVED =1;
    public static final int STATUS_ARCHIVED =2;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of Quote object.
     * @return Quote identifier.
     */
    @Override
    public String get_id()
    {
        return _id;
    }

    /**
     * Method to assign identifier to this object.
     * @param _id identifier to be assigned to this object.
     */
    public void set_id(String _id)
    {
        this._id = _id;
    }

    /**
     * Function to get a shortened identifier of this object.
     * @return The shortened identifier.
     */
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

    public StringProperty usrProperty(){return new SimpleStringProperty(getUsr());}

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr)
    {
        this.usr = usr;
    }

    public long getStart_date()
    {
        return start_date;
    }

    public void setStart_date(long date)
    {
        this.start_date = date;
    }

    public long getEnd_date()
    {
        return end_date;
    }

    public void setEnd_date(long date)
    {
        this.end_date = date;
    }

    public long getReturn_date()
    {
        return return_date;
    }

    public void setReturn_date(long date)
    {
        this.return_date = date;
    }

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public StringProperty statusProperty()
    {
        switch (getStatus())
        {
            case STATUS_PENDING:
                return new SimpleStringProperty("Pending");
            case STATUS_APPROVED:
                return new SimpleStringProperty("Approved");
            case STATUS_ARCHIVED:
                return new SimpleStringProperty("Archived");
            default:
                return new SimpleStringProperty("N/A");
        }
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status= status;
    }

    public StringProperty extraProperty(){return new SimpleStringProperty(getExtra());}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    public StringProperty employeeProperty()
    {
        Employee employee = getEmployee();
        if(employee!=null)
            return new SimpleStringProperty(getEmployee().toString());
        else return new SimpleStringProperty("N/A");
    }

    public Employee getEmployee()
    {
        if(usr==null)
            return null;
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getEmployees();
        if(employees!=null)
            return employees.get(usr);
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/leave_record";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("usr","UTF-8") + "="
                    + URLEncoder.encode(usr, "UTF-8"));
            if(getStatus()>0)
                result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getStatus()), "UTF-8"));
            if(getStart_date()>0)
                result.append("&" + URLEncoder.encode("start_date","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getStart_date()), "UTF-8"));
            if(getEnd_date()>0)
                result.append("&" + URLEncoder.encode("end_date","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getEnd_date()), "UTF-8"));
            if(getReturn_date()>0)
                result.append("&" + URLEncoder.encode("return_date","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getReturn_date()), "UTF-8"));
            if(getDate_logged()>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8"));
            if(getExtra()!=null)
                if(!getExtra().isEmpty())
                    result.append("&" + URLEncoder.encode("extra","UTF-8") + "="
                            + URLEncoder.encode(getExtra(), "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "usr":
                    setUsr(String.valueOf(val));
                    break;
                case "start_date":
                    setStart_date(Long.valueOf(String.valueOf(val)));
                    break;
                case "end_date":
                    setEnd_date(Long.parseLong(String.valueOf(val)));
                    break;
                case "return_date":
                    setReturn_date(Long.parseLong(String.valueOf(val)));
                    break;
                case "status":
                    setStatus(Integer.parseInt(String.valueOf(val)));
                    break;
                case "extra":
                    setExtra((String)val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown Leave attribute '" + var + "'.");
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
            case "_id":
                return get_id();
            case "usr":
                return getUsr();
            case "start_date":
                return getStart_date();
            case "end_date":
                return getEnd_date();
            case "return_date":
                return getReturn_date();
            case "date_logged":
                return getDate_logged();
            case "status":
                return getStatus();
            case "extra":
                return getExtra();
            default:
                IO.log(TAG, IO.TAG_ERROR, "Unknown Leave attribute '" + var + "'.");
                return null;
        }
    }
}