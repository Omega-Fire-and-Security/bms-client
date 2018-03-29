/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.BusinessObjectManager;
import fadulousbms.managers.JobManager;
import fadulousbms.managers.TaskManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.util.HashMap;

/**
 * Created by ghost on 2018/03/22.
 * @author ghost
 */
public class Task extends BusinessObject
{
    private long date_assigned;
    private long date_started;
    private long date_completed;
    private long date_scheduled;
    private String job_id;
    private String description;
    private String location;
    private int status;

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

    @Override
    public BusinessObjectManager getManager()
    {
        return TaskManager.getInstance();
    }

    //Getters and setters

    public long getDate_assigned() 
    {
        return date_assigned;
    }

    public void setDate_assigned(long date_assigned) 
    {
        this.date_assigned = date_assigned;
    }

    public long getDate_started() 
    {
        return date_started;
    }

    public void setDate_started(long date_started) 
    {
        this.date_started = date_started;
    }

    public long getDate_completed() 
    {
        return date_completed;
    }

    public void setDate_completed(long date_completed) 
    {
        this.date_completed = date_completed;
    }

    public long getDate_scheduled() {return date_scheduled;}

    public void setDate_scheduled(long date_scheduled) {this.date_scheduled = date_scheduled;}

    public boolean isCompleted()
    {
        return (date_completed>0 && status== STATUS_FINALISED);
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

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
        if(JobManager.getInstance().getDataset()!=null)
        {
            //return latest revision
            return JobManager.getInstance().getDataset().get(job_id);
        }
        else IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, "No jobs were found on the database.");
        return null;
    }

    public HashMap<String, TaskItem> getTaskItems()
    {
        return TaskManager.getInstance().getTaskItems(get_id());
    }

    /**
     * @return HashMap of Employees assigned to this Task.
     */
    public HashMap<String, Employee> getAssignees()
    {
        Job job = getJob();
        if(job!=null)
        {
            if (job.getAssigned_employees() != null)
            {
                HashMap<String, Employee> taskEmployeesMap = new HashMap<>();
                for (JobEmployee job_employee : job.getAssigned_employees().values())
                    if(get_id().equals(job_employee.getTask_id()))
                        taskEmployeesMap.put(job_employee.getUsr(), job_employee.getEmployee());
                return taskEmployeesMap;
            } else IO.logAndAlert("Error", "No employees have been assigned to this job", IO.TAG_WARN);
        } else IO.logAndAlert("Error", "Could not find a job for this task", IO.TAG_WARN);
        return null;
    }

    //Properties

    public StringProperty job_idProperty()
    {
        return new SimpleStringProperty(job_id);
    }

    public StringProperty task_descriptionProperty()
    {
        return new SimpleStringProperty(description);
    }

    //
    /**
     * Method to parse Model attribute.
     * @param var Model attribute to be parsed.
     * @param val Model attribute value to be set.
     */
    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "job_id":
                    job_id = (String)val;
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
                    break;
                case "date_scheduled":
                    date_scheduled = Long.parseLong(String.valueOf(val));
                    break;
                case "date_assigned":
                    date_assigned = Long.parseLong(String.valueOf(val));
                    break;
                case "date_started":
                    date_started = Long.parseLong(String.valueOf(val));
                    break;
                case "date_completed":
                    date_completed = Long.parseLong(String.valueOf(val));
                    break;
                case "description":
                    description = (String)val;
                    break;
                case "location":
                    location = (String)val;
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown "+getClass().getName()+" attribute '" + var + "'.");
                    break;
            }
        } catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    /**
     * @param var Model attribute whose value is to be returned.
     * @return Model attribute value.
     */
    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "job_id":
                return getJob_id();
            case "status":
                return getStatus();
            case "date_scheduled":
                return getDate_scheduled();
            case "date_assigned":
                return getDate_assigned();
            case "date_started":
                return getDate_started();
            case "date_completed":
                return getDate_completed();
            case "description":
                return getDescription();
            case "location":
                return getLocation();
            case "assignees":
                return getAssignees();
        }
        return super.get(var);
    }

    /**
     * @return JSON representation of Job object.
     */
    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"job_id\":\""+getJob_id()+"\""
                +",\"description\":\""+getDescription()+"\""
                +",\"status\":\""+getStatus()+"\"";
        if(date_assigned>0)
            json_obj+=",\"date_assigned\":\""+date_assigned+"\"";
        if(date_scheduled>0)
            json_obj+=",\"date_scheduled\":\""+date_scheduled+"\"";
        if(date_started>0)
            json_obj+=",\"date_started\":\""+date_started+"\"";
        if(date_completed>0)
            json_obj+=",\"date_completed\":\""+date_completed+"\"";
        if(location!=null)
            json_obj+=",\"location\":\""+getLocation()+"\"";
        json_obj+="}";

        return json_obj;
    }

    @Override
    public String toString()
    {
        String str = "#" + getObject_number() + " " + getDescription();
        if(getJob()!=null)
            str += ", for job " + getJob().toString();
        return str;
    }

    /**
     * @return Task model's endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/task";
    }
}