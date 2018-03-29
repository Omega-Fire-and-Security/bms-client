/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import jfxtras.scene.control.agenda.Agenda;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/01.
 * @author ghost
 */
public class Job extends BusinessObject implements Agenda.Appointment, Temporal
{
    private long planned_start_date;
    private long date_assigned;
    private long date_started;
    private long date_completed;
    private String invoice_id;
    private String quote_id;
    private int status;
    private Metafile[] safety_catalogue;

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
    public JobManager getManager()
    {
        return JobManager.getInstance();
    }

    //Getters and setters

    public long getPlanned_start_date() {return planned_start_date;}

    public void setPlanned_start_date(long planned_start_date) {this.planned_start_date = planned_start_date;}

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

    public boolean isJob_completed()
    {
        return (date_completed>0);
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public HashMap<String, Task> getTasks()
    {
        HashMap<String, Task> all_tasks = TaskManager.getInstance().getDataset();
        if(all_tasks!=null) {
            HashMap<String, Task> job_tasks = new HashMap<>();
            for (Task task : all_tasks.values())
                if (task.getJob_id() != null)
                    if (this.get_id().equals(task.getJob_id()))
                        job_tasks.put(task.get_id(), task);
            return job_tasks;
        } else return null;
    }

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }

    public Quote getQuote()
    {
        QuoteManager.getInstance().initialize();
        if(QuoteManager.getInstance().getDataset()!=null)
        {
            //return latest revision
            Quote[] revisions = QuoteManager.getInstance().getDataset().get(quote_id).getSortedSiblings("revision");
            return revisions[revisions.length-1];
        }
        else IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, "No quotes were found on the database.");
        return null;
    }

    public String getInvoice_id() 
    {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id) 
    {
        this.invoice_id = invoice_id;
    }

    public HashMap<String, JobEmployee> getJobEmployees(String job_id)
    {
        IO.log(getClass().getName(), IO.TAG_VERBOSE, "querying assignees for job: " + job_id);

        if(getManager().getJob_employees()!=null)
        {
            if(getManager().getJob_employees().get(job_id)!=null)
            {
                return getManager().getJob_employees().get(job_id);
            } else
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, "no job assignees were found in the database for job: " + job_id);
                return null;
            }
        } else
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "no job assignees were found in the database." );
            return null;
        }
    }

    /**
     * @return Map of Employees assigned to this Job object.
     */
    public HashMap<String, JobEmployee> getAssigned_employees()
    {
        HashMap<String, JobEmployee> all_job_employees = getJobEmployees(get_id());
        if(all_job_employees!=null)
        {
            //try get distinct Employees for this Job - JobEmployee objects may be assigned to the same Job but different Tasks
            HashMap<String, JobEmployee> unique_job_employees = new HashMap<>();
            for(JobEmployee jobEmployee: all_job_employees.values())
                unique_job_employees.putIfAbsent(jobEmployee.getUsr(), jobEmployee);
            IO.log(getClass().getName(), IO.TAG_VERBOSE, "job [" + get_id() + "] has [" + all_job_employees.size() + "] employee assignments in total and [" + unique_job_employees.size() + "] unique assignees.");
            return unique_job_employees;
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "job #"+ getObject_number() + " has no assignees." );

        return null;
    }

    /**
     * @return Safety documents associated with this Job object.
     */
    public Metafile[] getSafety_catalogue()
    {
        return safety_catalogue;
    }

    /**
     * @param safety_catalogue Safety documents associated with a Job object.
     */
    public void setSafety_catalogue(Metafile[] safety_catalogue)
    {
        this.safety_catalogue=safety_catalogue;
    }

    //Properties

    public StringProperty invoice_idProperty()
    {
        return new SimpleStringProperty(invoice_id);
    }

    public StringProperty quote_idProperty()
    {
        return new SimpleStringProperty(quote_id);
    }

    public StringProperty safety_catalogueProperty()
    {
        String s="";
        for(Metafile file: safety_catalogue)
            s += " : " + file.getLabel() + ",";
        return new SimpleStringProperty(s.substring(0,s.length()-1));
    }

    public StringProperty job_descriptionProperty()
    {
        Quote quote = getQuote();
        if(quote!=null)
            return new SimpleStringProperty(quote.getRequest());
        else return new SimpleStringProperty("N/A");
    }

    public StringProperty client_nameProperty()
    {
        Quote quote = getQuote();
        if(quote!=null)
            if(quote.getClient()!=null)
                return new SimpleStringProperty(quote.getClient().getClient_name());
            else return new SimpleStringProperty("N/A");
        else return new SimpleStringProperty("N/A");
    }

    public StringProperty sitenameProperty()
    {
        Quote quote = getQuote();
        if(quote!=null)
            return new SimpleStringProperty(quote.getSitename());
        else return new SimpleStringProperty("N/A");
    }

    public StringProperty contact_personProperty()
    {
        Quote quote = getQuote();
        if(quote!=null)
            if(quote.getContact_person()!=null)
                return new SimpleStringProperty(quote.getContact_person().getName());
            else return new SimpleStringProperty("N/A");
        else return new SimpleStringProperty("N/A");
    }

    public SimpleStringProperty totalProperty(){return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));}

    /**
     * @return The total cost of this Job (derived from Quote object)
     */
    public double getTotal()
    {
        Quote quote = getQuote();
        //Compute job total
        if(quote!=null)
            return quote.getTotal();
        else return 0;
    }

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
                case "quote_id":
                    quote_id = (String)val;
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
                    break;
                case "planned_start_date":
                    planned_start_date = Long.parseLong(String.valueOf(val));
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
                case "invoice_id":
                    invoice_id = (String)val;
                    break;
                case "safety_catalogue":
                    if(val!=null)
                        safety_catalogue = (Metafile[]) val;
                    else IO.log(getClass().getName(), IO.TAG_WARN, "value to be casted to Metafile[] is null.");
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
            case "quote_id":
                return getQuote_id();
            case "status":
                return getStatus();
            case "planned_start_date":
                return getPlanned_start_date();
            case "date_assigned":
                return getDate_assigned();
            case "date_started":
                return getDate_started();
            case "date_completed":
                return getDate_completed();
            case "invoice_id":
                return getInvoice_id();
            case "assigned_employees":
                return getAssigned_employees();
            case "safety_catalogue":
                return getSafety_catalogue();
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
                +",\"quote_id\":\""+quote_id+"\""
                +",\"status\":\""+status+"\"";
        if(date_assigned>0)
            json_obj+=",\"date_assigned\":\""+date_assigned+"\"";
        if(planned_start_date>0)
            json_obj+=",\"planned_start_date\":\""+planned_start_date+"\"";
        if(date_started>0)
            json_obj+=",\"date_started\":\""+date_started+"\"";
        if(date_completed>0)
            json_obj+=",\"date_completed\":\""+date_completed+"\"";
        if(invoice_id!=null)
            json_obj+=",\"invoice_id\":\""+invoice_id+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        String str = "#" + getObject_number() + ", ["  + getDescription()+"]";
        if(getQuote()!=null)
            str += ", for quote " + getQuote().toString();
        return str;
    }

    /**
     * @return Job model's endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/job";
    }

    @Override
    public Boolean isWholeDay() {
        IO.log(getClass().getName(), IO.TAG_VERBOSE, "duration: " + (getDate_completed()-getDate_started())/1000/60/60 + " hours");
        return (getDate_completed()-getDate_started())/1000/60/60 == 24 ? true : false;
    }

    @Override
    public void setWholeDay(Boolean aBoolean) {

    }

    @Override
    public String getSummary() {
        if(getQuote()!=null)
            if(getQuote().getClient()!=null)
                return "Job to be executed at " + getQuote().getSitename() + " for client \"" + getQuote().getClient().getClient_name() + "\"";
            else return "N/A - no clients in database.";
        else return "N/A - no quotes in database.";
    }

    @Override
    public void setSummary(String s) {

    }

    @Override
    public String getDescription()
    {
        if(getQuote()!=null)
            return getQuote().getRequest();
        else return "N/A";
    }

    @Override
    public void setDescription(String s) {

    }

    @Override
    public String getLocation() {
        if(getQuote()!=null)
            return getQuote().getSitename();
        else return "N/A";
    }

    @Override
    public void setLocation(String s) {

    }

    @Override
    public Agenda.AppointmentGroup getAppointmentGroup() {
        return new Agenda.AppointmentGroupImpl();
    }

    @Override
    public void setAppointmentGroup(Agenda.AppointmentGroup appointmentGroup) {

    }

    @Override
    public Calendar getStartTime() {
        Date date = new Date();
        date.setTime(getDate_started());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    @Override
    public void setStartTime(Calendar calendar) {

    }

    @Override
    public Calendar getEndTime()
    {
        Date date = new Date();
        date.setTime(getDate_completed());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    @Override
    public void setEndTime(Calendar calendar) {

    }

    @Override
    public Temporal getStartTemporal() {
        return this;
    }

    @Override
    public void setStartTemporal(Temporal temporal) {

    }

    @Override
    public Temporal getEndTemporal() {
        return this;
    }

    @Override
    public void setEndTemporal(Temporal temporal) {

    }

    @Override
    public LocalDateTime getStartLocalDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public void setStartLocalDateTime(LocalDateTime localDateTime) {

    }

    @Override
    public LocalDateTime getEndLocalDateTime() {
        return LocalDateTime.now();
    }

    @Override
    public void setEndLocalDateTime(LocalDateTime localDateTime) {

    }

    @Override
    public boolean isSupported(TemporalUnit unit) {
        return false;
    }

    @Override
    public Temporal with(TemporalField field, long newValue) {
        return this;
    }

    @Override
    public Temporal plus(long amountToAdd, TemporalUnit unit) {
        return this;
    }

    @Override
    public long until(Temporal endExclusive, TemporalUnit unit) {
        return 0;
    }

    @Override
    public boolean isSupported(TemporalField field) {
        return false;
    }

    @Override
    public long getLong(TemporalField field) {
        return 0;
    }
}