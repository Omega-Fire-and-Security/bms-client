package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by ghost on 2017/02/03.
 */
public class JobEmployee extends BusinessObject
{
    private String job_id;
    private String task_id;
    private String usr;
    public static final String TAG = "JobEmployee";

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

    public String getJob_id()
    {
        return job_id;
    }

    public void setJob_id(String job_id)
    {
        this.job_id = job_id;
    }

    public String getTask_id()
    {
        return task_id;
    }

    public void setTask_id(String task_id)
    {
        this.task_id = task_id;
    }

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr)
    {
        this.usr = usr;
    }

    private StringProperty job_idProperty(){return new SimpleStringProperty(job_id);}
    private StringProperty task_idProperty(){return new SimpleStringProperty(task_id);}
    private StringProperty usrProperty(){return new SimpleStringProperty(usr);}

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "job_id":
                    job_id = String.valueOf(val);
                    break;
                case "task_id":
                    task_id = String.valueOf(val);
                    break;
                case "usr":
                    usr = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "job_id":
                return job_id;
            case "task_id":
                return task_id;
            case "usr":
                return usr;
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
                +",\"job_id\":\""+job_id+"\""
                +",\"usr\":\""+usr+"\"";
        if(getTask_id()!=null)
            json_obj+=",\"task_id\":\""+task_id+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/jobs/employees";
    }
}
