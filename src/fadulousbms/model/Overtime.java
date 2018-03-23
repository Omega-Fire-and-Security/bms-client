package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.JobManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class Overtime extends BusinessObject implements Serializable
{
    private String usr;
    private String job_id;
    private long date;
    private long time_in;
    private long time_out;
    private int status;
    public static final String TAG = "Overtime";

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

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
            return new SimpleStringProperty(String.valueOf(getJob().getObject_number()));
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job object for Overtime record["+get_id()+"]{"+getObject_number()+"} is not set");
        return new SimpleStringProperty("N/A");
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
        HashMap<String, Job> jobs = JobManager.getInstance().getDataset();
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
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getDataset();
        if(employees!=null)
            return employees.get(usr);
        return null;
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
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
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "_id":
                return get_id();
            case "usr":
                return getUsr();
            case "job_id":
                return getJob_id();
            case "date":
                return getDate();
            case "time_in":
                return getTime_in();
            case "time_out":
                return getTime_out();
            case "status":
                return getStatus();
        }
        return super.get(var);
    }

    /**
     * @return JSON representation of Leave object.
     */
    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"usr\":\""+usr+"\""
                +",\"job_id\":\""+job_id+"\"";
        if(status>0)
            json_obj+=",\"status\":\""+status+"\"";
        if(date>0)
            json_obj+=",\"date\":\""+date+"\"";
        if(time_in>0)
            json_obj+=",\"time_in\":\""+time_in+"\"";
        if(time_out>0)
            json_obj+=",\"time_out\":\""+time_out+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/overtime_records";
    }

}