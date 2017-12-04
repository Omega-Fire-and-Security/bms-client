package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.JobManager;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jdk.nashorn.internal.scripts.JO;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class Overtime implements BusinessObject, Serializable
{
    private String _id;
    private String usr;
    private String job_id;
    private long date;
    private long time_in;
    private long time_out;
    private long date_logged;
    private int status;
    private String extra;
    private boolean marked;
    public static final String TAG = "Overtime";
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

    public StringProperty job_numberProperty()
    {
        if(getJob()!=null)
            return getJob().job_numberProperty();
        else return new SimpleStringProperty("N/A");
    }

    public StringProperty job_idProperty(){return new SimpleStringProperty(getJob_id());}

    public String getJob_id()
    {
        return job_id;
    }

    public void setJob_id(String job_id)
    {
        this.job_id = job_id;
    }

    public Job getJob()
    {
        if(job_id==null)
            return null;
        HashMap<String, Job> jobs = JobManager.getInstance().getJobs();
        if(jobs!=null)
            return jobs.get(job_id);
        return null;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date)
    {
        this.date = date;
    }

    public long getTime_in()
    {
        return time_in;
    }

    public StringProperty time_inProperty(){return new SimpleStringProperty(String.valueOf(getTime_in()));}

    public void setTime_in(long time_in)
    {
        this.time_in = time_in;
    }

    public long getTime_out()
    {
        return time_out;
    }

    public StringProperty time_outProperty(){return new SimpleStringProperty(String.valueOf(getTime_out()));}

    public void setTime_out(long time_out)
    {
        this.time_out = time_out;
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
        return "/api/overtime_record";
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
            result.append("&" + URLEncoder.encode("job_id","UTF-8") + "="
                    + URLEncoder.encode(job_id, "UTF-8"));
            if(getStatus()>0)
                result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getStatus()), "UTF-8"));
            if(getDate()>0)
                result.append("&" + URLEncoder.encode("date","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate()), "UTF-8"));
            if(getTime_in()>0)
                result.append("&" + URLEncoder.encode("time_in","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getTime_in()), "UTF-8"));
            if(getTime_out()>0)
                result.append("&" + URLEncoder.encode("time_out","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getTime_out()), "UTF-8"));
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
                case "job_id":
                    setJob_id(String.valueOf(val));
                    break;
                case "date":
                    setDate(Long.valueOf(String.valueOf(val)));
                    break;
                case "date_logged":
                    setDate_logged(Long.parseLong(String.valueOf(val)));
                    break;
                case "time_in":
                    setTime_in(Long.parseLong(String.valueOf(val)));
                    break;
                case "time_out":
                    setTime_out(Long.parseLong(String.valueOf(val)));
                    break;
                case "status":
                    setStatus(Integer.parseInt(String.valueOf(val)));
                    break;
                case "extra":
                    setExtra((String)val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown Overtime attribute '" + var + "'.");
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
            case "job_id":
                return getJob_id();
            case "date":
                return getDate();
            case "date_logged":
                return date_logged;
            case "time_in":
                return getTime_in();
            case "time_out":
                return getTime_out();
            case "status":
                return getStatus();
            case "extra":
                return getExtra();
            default:
                IO.log(TAG, IO.TAG_ERROR, "Unknown Overtime attribute '" + var + "'.");
                return null;
        }
    }
}