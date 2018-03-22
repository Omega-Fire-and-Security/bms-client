/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.JobManager;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.TaskManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jfxtras.scene.control.agenda.Agenda;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 *
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
    private String assignees;
    private HashMap<String, TaskItem> taskItems;

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
        return (date_completed>0 && status==STATUS_APPROVED);
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
        return taskItems;
    }

    public void setTaskItems(HashMap<String, TaskItem> taskItems)
    {
        this.taskItems = taskItems;
    }

    /**
     * @return Array of Employees assigned to a Job object.
     */
    public String getAssignees()
    {
        return assignees;
    }

    /**
     * @param assignees Array of Employees to be assigned to a Job object.
     */
    public void setAssignees(String assignees)
    {
        this.assignees=assignees;
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
    public void parse(String var, Object val)
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
                case "assigned_employees":
                case "assignees":
                    assignees = (String) val;
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
        if(assignees!=null)
            json_obj+=",\"assignees\":\""+assignees+"\"";
        if(location!=null)
            json_obj+=",\"location\":\""+getLocation()+"\"";
        json_obj+="}";

        return json_obj;
    }

    /**
     * @return Task model's endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/tasks";
    }
}