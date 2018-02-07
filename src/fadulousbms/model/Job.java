/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.QuoteManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class Job extends BusinessObject
{
    private long planned_start_date;
    private long date_assigned;
    private long date_started;
    private long date_completed;
    private String invoice_id;
    private String quote_id;
    private int status;
    private Employee[] assigned_employees;
    private FileMetadata[] safety_catalogue;

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

    /**
     * @return Array of Employees assigned to a Job object.
     */
    public Employee[] getAssigned_employees()
    {
        return assigned_employees;
    }

    /**
     * @param employees Array of Employees to be assigned to a Job object.
     */
    public void setAssigned_employees(Employee[] employees)
    {
        this.assigned_employees=employees;
    }

    /**
     * @param reps ArrayList of Employees to be assigned to a Job object.
     */
    public void setAssigned_employees(ArrayList<Employee> reps)
    {
        this.assigned_employees = new Employee[reps.size()];
        for(int i=0;i<reps.size();i++)
        {
            this.assigned_employees[i] = reps.get(i);
        }
    }

    /**
     * @return Safety documents associated with a Job object.
     */
    public FileMetadata[] getSafety_catalogue()
    {
        return safety_catalogue;
    }

    /**
     * @param safety_catalogue Safety documents associated with a Job object.
     */
    public void setSafety_catalogue(FileMetadata[] safety_catalogue)
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
        for(FileMetadata file: safety_catalogue)
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
    public void parse(String var, Object val)
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
                case "assigned_employees":
                    if(val!=null)
                        assigned_employees = (Employee[]) val;
                    else IO.log(getClass().getName(), IO.TAG_WARN, "value to be casted to Employee[] is null.");
                    break;
                case "safety_catalogue":
                    if(val!=null)
                        safety_catalogue = (FileMetadata[]) val;
                    else IO.log(getClass().getName(), IO.TAG_WARN, "value to be casted to FileMetadata[] is null.");
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
     * @return UTF-8 String representation of Job object.
     */
    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            /*result.append(URLEncoder.encode("job_number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(job_number), "UTF-8"));*/
            result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(quote_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            if(date_assigned>0)
                result.append("&" + URLEncoder.encode("date_assigned","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_assigned), "UTF-8"));
            if(planned_start_date>0)
                result.append("&" + URLEncoder.encode("planned_start_date","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(planned_start_date), "UTF-8"));
            if(date_started>0)
                result.append("&" + URLEncoder.encode("date_started","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_started), "UTF-8"));
            if(date_completed>0)
                result.append("&" + URLEncoder.encode("date_completed","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_completed), "UTF-8"));
            if(invoice_id!=null)
                result.append("&" + URLEncoder.encode("invoice_id","UTF-8") + "="
                        + URLEncoder.encode(invoice_id, "UTF-8"));
            /*if(assigned_employees!=null)
                result.append("&" + URLEncoder.encode("assigned_employees","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(assigned_employees.length), "UTF-8"));
            if(safety_catalogue!=null)
                result.append("&" + URLEncoder.encode("safety_catalogue","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(safety_catalogue.length), "UTF-8"));*/

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
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

    /**
     * @return Job model's endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/jobs";
    }

}