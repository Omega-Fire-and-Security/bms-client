/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.JobManager;
import fadulousbms.managers.QuoteManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 *
 * @author ghost
 */
public class Invoice extends BusinessObject implements Serializable
{
    private String job_id;
    //private String quote_id;
    private String quote_revision_numbers;//semi-colon separated array of Quote revision numbers associated with Invoice.
    //private String account;
    private double receivable;
    private int status;

    public HashMap<String, Quote> quoteRevisions()
    {
        if(getJob()==null)
        {
            IO.log(getClass().getName(), IO.TAG_WARN, "invalid Invoice Job object.");
            return null;
        }
        if(getJob().getQuote()==null)
        {
            IO.log(getClass().getName(), IO.TAG_WARN, "invalid Invoice Quote object.");
            return null;
        }
        if(getJob().getQuote().getChildrenMap()==null)
        {
            IO.log(getClass().getName(), IO.TAG_WARN, "invalid Quote children map.");
            return null;
        }
        String[] str_revs = quote_revision_numbers.split(";");
        HashMap<String, Quote> quotes_map = new HashMap<>();
        quotes_map.put(this.getJob().getQuote().get_id(), getJob().getQuote());//add parent Quote to map of Quotes to be returned
        if(str_revs!=null)
        {
            if(str_revs.length>0)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "Invoice ["+get_id()+"] has ["+str_revs.length+"] Quote revisions.");
                for(String str_rev_num: str_revs)
                {
                    try
                    {
                        Quote quote = getJob().getQuote().getSiblingsMap().get(Double.parseDouble(str_rev_num));
                        if(quote!=null)
                            quotes_map.put(quote.get_id(), quote);
                        else IO.log(getClass().getName(), IO.TAG_WARN, "could not find any Quote [revision #:"+str_rev_num+"] " +
                                "associated with this "+getClass().getName()+"["+get_id()+"]");
                    } catch (NumberFormatException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }
        }
        return quotes_map;
    }

    public String getQuote_revision_numbers()
    {
        return quote_revision_numbers;
    }

    public void setQuote_revision_numbers(String quote_revision_numbers)
    {
        this.quote_revision_numbers = quote_revision_numbers;
    }

    public StringProperty accountProperty(){return new SimpleStringProperty(getAccount());}

    public String getAccount()
    {
        /*HashMap<String, Job> jobs_map = JobManager.getInstance().getJobs();
        if(jobs_map==null)
            return "N/A";
        Job job = jobs_map.get(getJob_id());*/
        Job job =getJob();
        if(job==null)
            return "N/A";
        if(job.getQuote()==null)
            return "N/A";
        return job.getQuote().getAccount_name();
    }

    public StringProperty receivableProperty(){return new SimpleStringProperty(String.valueOf(receivable));}

    public double getReceivable()
    {
        return receivable;
    }

    public void setReceivable(double receivable)
    {
        this.receivable = receivable;
    }

    private StringProperty job_idProperty(){return new SimpleStringProperty(job_id);}

    public String getJob_id()
    {
        return job_id;
    }

    public void setJob_id(String job_id)
    {
        this.job_id = job_id;
    }

    public StringProperty invoice_numberProperty()
    {
        return new SimpleStringProperty(get_id());//TODO: fix this!
    }

    private StringProperty statusProperty(){return new SimpleStringProperty(String.valueOf(status));}

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    private StringProperty totalProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));
    }

    public double getTotal()
    {
        if(getJob()!=null)
            return getJob().getTotal();
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job object is not set");
        return 0;
    }

    public StringProperty job_numberProperty()
    {
        return new SimpleStringProperty(String.valueOf(getJob_number()));
    }

    public long getJob_number()
    {
        if(getJob()!=null)
            return getJob().getJob_number();
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job object is not set");
        return 0;
    }

    public Client getClient()
    {
        if(getJob()!=null)
            if(getJob().getQuote()!=null)
                return getJob().getQuote().getClient();
            else IO.log(getClass().getName(), IO.TAG_ERROR, "Job->Quote object is not set");
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job object is not set");
        return null;
    }

    public StringProperty clientProperty()
    {
        if(getClient()!=null)
            return new SimpleStringProperty(getClient().getClient_name());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job->Quote->Client object is not set");
        return null;
    }

    public Job getJob()
    {
        if(getJob_id()==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "job_id is not set.");
            return null;
        }
        JobManager.getInstance().loadDataFromServer();
        if(JobManager.getInstance().getJobs()!=null)
        {
            return JobManager.getInstance().getJobs().get(job_id);
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "No Jobs were found in the database.");
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
                case "job_id":
                    setJob_id(String.valueOf(val));
                    break;
                case "receivable":
                    setReceivable(Double.valueOf(String.valueOf(val)));
                    break;
                case "quote_revision_numbers":
                    setQuote_revision_numbers(String.valueOf(val));
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
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

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "job_id":
                return getJob_id();
            case "status":
                return getStatus();
            case "account":
                return getAccount();
            case "quote_revision_numbers":
                return getQuote_revision_numbers();
            case "receivable":
                return getReceivable();
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
            /*result.append(URLEncoder.encode("quote_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quote_id), "UTF-8"));*/
            result.append(URLEncoder.encode("job_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(job_id), "UTF-8"));
            if(getDate_logged()>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(getCreator()), "UTF-8"));
            result.append("&" + URLEncoder.encode("receivable","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(receivable), "UTF-8"));
            if(getOther()!=null)
                result.append(URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(getOther(), "UTF-8") + "&");
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
        String json_obj = "{"+(get_id()!=null?"\"_id\":\""+get_id()+"\",":"")
                +"\"job_id\":\""+getJob_id()+"\""
                +",\"quote_revision_numbers\":\""+getQuote_revision_numbers()+"\""
                +",\"receivable\":\""+getReceivable()+"\"";
        if(getStatus()>0)
            json_obj+=",\"status\":\""+getStatus()+"\"";
        if(getCreator()!=null)
            json_obj+=",\"creator\":\""+getCreator()+"\"";
        if(getDate_logged()>0)
            json_obj+=",\"date_logged\":\""+getDate_logged()+"\"";
        json_obj+=",\"other\":\""+getOther()+"\"}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/invoices";
    }
}
