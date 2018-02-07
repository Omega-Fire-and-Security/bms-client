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
public class Leave extends BusinessObject implements Serializable
{
    private String usr;
    private long start_date;
    private long end_date;
    private long return_date;
    private int status;
    private String type;
    public static final String TAG = "Leave";
    public static String[] TYPES = {"ANNUAL", "SICK", "UNPAID", "FAMILY RESPONSIBILITY - See BCEA for definition"};

    public Leave(String usr, long start_date, long end_date, String type)
    {
        setUsr(usr);
        setStart_date(start_date);
        setEnd_date(end_date);
        setType(type);
        setStatus(this.STATUS_PENDING);
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

    public StringProperty typeProperty(){return new SimpleStringProperty(getType());}

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
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
                case "type":
                    setType((String)val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "usr":
                return getUsr();
            case "start_date":
                return getStart_date();
            case "end_date":
                return getEnd_date();
            case "return_date":
                return getReturn_date();
            case "status":
                return getStatus();
            case "type":
                return getType();
        }
        return super.get(var);
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
            result.append("&" + URLEncoder.encode("type","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(getType()), "UTF-8"));
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
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * @return JSON representation of Leave object.
     */
    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"usr\":\""+usr+"\"";
        if(status>0)
            json_obj+=",\"status\":\""+status+"\"";
        if(start_date>0)
            json_obj+=",\"start_date\":\""+start_date+"\"";
        if(end_date>0)
            json_obj+=",\"end_date\":\""+end_date+"\"";
        if(return_date>0)
            json_obj+=",\"return_date\":\""+return_date+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/leave_records";
    }

}