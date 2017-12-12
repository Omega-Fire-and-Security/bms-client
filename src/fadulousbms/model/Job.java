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
import java.lang.annotation.Native;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 *
 * @author ghost
 */
public class Job implements BusinessObject, Serializable
{
    private String _id;
    //private String job_name;
    //private String job_description;
    //private String client_id;
    private long planned_start_date;
    private long date_logged;
    private long date_assigned;
    private long date_started;
    private long date_completed;
    private long job_number;
    private String invoice_id;
    private String quote_id;
    private String creator;
    private boolean signed;
    private boolean marked;
    private String signed_job;
    private Employee[] assigned_employees;
    private FileMetadata[] safety_catalogue;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    @Override
    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

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

    //public StringProperty planned_start_dateProperty(){return new SimpleStringProperty(String.valueOf(planned_start_date));}

    public long getPlanned_start_date() {return planned_start_date;}

    public void setPlanned_start_date(long planned_start_date) {this.planned_start_date = planned_start_date;}

    public long getDate_logged() 
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged) 
    {
        this.date_logged = date_logged;
    }
    
    /*public StringProperty date_loggedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_logged));
    }*/

    public StringProperty job_numberProperty()
    {
        return new SimpleStringProperty(String.valueOf(getJob_number()));
    }

    public long getJob_number()
    {
        return job_number;
    }

    public void setJob_number(long job_number)
    {
        this.job_number = job_number;
    }

    public Employee[] getAssigned_employees()
    {
        return assigned_employees;
    }

    public void setAssigned_employees(Employee[] employees)
    {
        this.assigned_employees=employees;
    }

    public void setAssigned_employees(ArrayList<Employee> reps)
    {
        this.assigned_employees = new Employee[reps.size()];
        for(int i=0;i<reps.size();i++)
        {
            this.assigned_employees[i] = reps.get(i);
        }
    }

    public StringProperty assigned_employeesProperty()
    {
        String s="";
        for(Employee e: assigned_employees)
            s += e.getFirstname() + " " + e.getLastname() + ",";
        return new SimpleStringProperty(s.substring(0,s.length()-1));
    }

    public FileMetadata[] getSafety_catalogue()
    {
        return safety_catalogue;
    }

    public void setSafety_catalogue(FileMetadata[] safety_catalogue)
    {
        this.safety_catalogue=safety_catalogue;
    }

    public StringProperty safety_catalogueProperty()
    {
        String s="";
        for(FileMetadata file: safety_catalogue)
            s += file.getIndex() + " : " + file.getLabel() + ",";
        return new SimpleStringProperty(s.substring(0,s.length()-1));
    }


    public long getDate_assigned() 
    {
        return date_assigned;
    }

    public void setDate_assigned(long date_assigned) 
    {
        this.date_assigned = date_assigned;
    }
    
    /*public StringProperty date_assignedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_assigned));
    }*/

    public long getDate_started() 
    {
        return date_started;
    }

    public void setDate_started(long date_started) 
    {
        this.date_started = date_started;
    }
    
    /*public StringProperty date_startedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_started));
    }*/

    public long getDate_completed() 
    {
        return date_completed;
    }

    public void setDate_completed(long date_completed) 
    {
        this.date_completed = date_completed;
    }
    
    /*public StringProperty date_completedProperty()
    {
        return new SimpleStringProperty(String.valueOf(date_completed));
    }*/

    public boolean isJob_completed()
    {
        return (date_completed>0);
    }

    /*public void setJob_completed(boolean executed)
    {
        this.job_completed = executed;
    }
    
    public StringProperty job_completedProperty()
    {
        return new SimpleStringProperty(String.valueOf(job_completed));
    }*/
    
    public StringProperty creatorProperty()
    {
        return new SimpleStringProperty(String.valueOf(getCreator()));
    }

    public String getCreator()
    {
        if(creator==null)
            return "N/A";
        else
        {
            EmployeeManager.getInstance().loadDataFromServer();
            Employee employee = EmployeeManager.getInstance().getEmployees().get(creator);
            if(employee!=null)
                return employee.toString();
            else return "N/A";
        }
    }

    public String getCreatorID(){return this.creator;}

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public void setSigned(boolean signed)
    {
        this.signed=signed;
    }

    public boolean isSigned()
    {
        return signed;
    }

    /**
     * @return base64 encoded string of signed Job
     */
    public String getSigned_job()
    {
        return signed_job;
    }

    /**
     * @param signed_job Base64 representation of signed Job
     */
    public void setSigned_job(String signed_job)
    {
        this.signed_job = signed_job;
    }

    public StringProperty quote_idProperty()
    {
        return new SimpleStringProperty(quote_id);
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
        if(QuoteManager.getInstance().getQuotes()!=null)
        {
            //return latest revision
            Quote[] revisions = QuoteManager.getInstance().getQuotes().get(quote_id).getSiblings("revision");
            return revisions[revisions.length-1];
        }
        else IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, "No quotes were found on the database.");
        return null;
    }

    public StringProperty invoice_idProperty()
    {
        return new SimpleStringProperty(invoice_id);
    }

    public String getInvoice_id() 
    {
        return invoice_id;
    }

    public void setInvoice_id(String invoice_id) 
    {
        this.invoice_id = invoice_id;
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
                return new SimpleStringProperty(quote.getContact_person().toString());
            else return new SimpleStringProperty("N/A");
        else return new SimpleStringProperty("N/A");
    }

    public SimpleStringProperty totalProperty(){return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));}

    public double getTotal()
    {
        Quote quote = getQuote();
        //Compute job total
        if(quote!=null)
            return quote.getTotal();
        else return 0;
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/job";
    }

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
            result.append("&" + URLEncoder.encode("signed","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(signed), "UTF-8"));
            if(getSigned_job()!=null)
                result.append("&" + URLEncoder.encode("signed_job","UTF-8") + "="
                        + URLEncoder.encode(getSigned_job(), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            if(date_logged>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
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

    @Override
    public String toString()
    {
        return "Job #"+job_number;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "quote_id":
                    quote_id = (String)val;
                    break;
                case "signed":
                    signed = (Boolean) val;
                    break;
                case "signed_job":
                    signed_job = (String) val;
                    break;
                case "planned_start_date":
                    planned_start_date = Long.parseLong(String.valueOf(val));
                    break;
                case "date_logged":
                    date_logged = Long.parseLong(String.valueOf(val));
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
                case "job_number":
                    job_number = Long.parseLong(String.valueOf(val));
                    break;
                case "creator":
                    creator = String.valueOf(val);
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
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Job attribute '" + var + "'.");
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
            case "job_number":
                return getJob_number();
            case "quote_id":
                return getQuote_id();
            case "signed":
                return isSigned();
            case "signed_job":
                return getSigned_job();
            case "planned_start_date":
                return getPlanned_start_date();
            case "date_logged":
                return getDate_logged();
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
            case "creator":
                return creator;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Job attribute '" + var + "'.");
                return null;
        }
    }
}