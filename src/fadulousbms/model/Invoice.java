/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.JobManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;

/**
 *
 * @author ghost
 */
public class Invoice extends BusinessObject implements Serializable
{
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

    public void setQuote_revision_numbers(String quote_revision_numbers)
    {
        this.quote_revision_numbers = quote_revision_numbers;
    }

    public double getReceivable()
    {
        return receivable;
    }

    private String job_id;
    private String quote_revision_numbers;//semi-colon separated array of Quote revision numbers associated with Invoice.
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

    public void setReceivable(double receivable)
    {
        this.receivable = receivable;
    }

    public String getJob_id()
    {
        return job_id;
    }

    public void setJob_id(String job_id)
    {
        this.job_id = job_id;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public double getTotal()
    {
        if(getJob()!=null)
            return getJob().getTotal();
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

    public Job getJob()
    {
        if(getJob_id()==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "job_id is not set.");
            return null;
        }
        JobManager.getInstance().initialize();
        if(JobManager.getInstance().getDataset()!=null)
        {
            return JobManager.getInstance().getDataset().get(job_id);
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "No Jobs were found in the database.");
        return null;
    }

    //Properties

    public StringProperty accountProperty(){return new SimpleStringProperty(getAccount());}

    public StringProperty receivableProperty(){return new SimpleStringProperty(String.valueOf(receivable));}

    public StringProperty job_idProperty(){return new SimpleStringProperty(job_id);}

    public StringProperty statusProperty(){return new SimpleStringProperty(String.valueOf(status));}

    public StringProperty totalProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));
    }

    public StringProperty clientProperty()
    {
        if(getClient()!=null)
            return new SimpleStringProperty(getClient().getClient_name());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job->Quote->Client object is not set");
        return null;
    }

    public StringProperty job_numberProperty()
    {
        if(getJob()!=null)
            return new SimpleStringProperty(String.valueOf(getJob().getObject_number()));
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job object for Invoice["+get_id()+"]{"+getObject_number()+"} is not set");
        return new SimpleStringProperty("N/A");
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
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"job_id\":\""+getJob_id()+"\""
                +",\"quote_revision_numbers\":\""+getQuote_revision_numbers()+"\""
                +",\"receivable\":\""+getReceivable()+"\"";
        if(getStatus()>0)
            json_obj+=",\"status\":\""+getStatus()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/invoices";
    }
}
