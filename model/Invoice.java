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
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author ghost
 */
public class Invoice implements BusinessObject, Serializable
{
    private String _id;
    private String job_id;
    private String creator;
    private long date_generated;
    private String account;
    private String extra;
    private boolean marked;
    private double receivable;

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
    public boolean isMarked() { return marked;}

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    public long getDate_generated()
    {
        return date_generated;
    }

    public void setDate_generated(long date_generated)
    {
        this.date_generated = date_generated;
    }

    public StringProperty accountProperty(){return new SimpleStringProperty(account);}

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
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
        return new SimpleStringProperty(_id);//TODO: fix this!
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

    private StringProperty clientProperty()
    {
        if(getClient()!=null)
            return new SimpleStringProperty(getClient().toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "Job->Quote->Client object is not set");
        return null;
    }

    private StringProperty creatorProperty()
    {
        return new SimpleStringProperty(getCreator().toString());
    }

    public Employee getCreator()
    {
        if(creator==null)
            return null;
        else
        {
            EmployeeManager.getInstance().loadDataFromServer();
            return EmployeeManager.getInstance().getEmployees().get(creator);
        }
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    private StringProperty extraProperty(){return new SimpleStringProperty(extra);}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    public Job getJob()
    {
        if(job_id==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "job_id is not set.");
            return null;
        }
        if(JobManager.getInstance().getJobs()!=null)
        {
            return JobManager.getInstance().getJobs().get(job_id);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "No Jobs were found in the database.");
        return null;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "date_generated":
                    setDate_generated(Long.parseLong(String.valueOf(val)));
                    break;
                case "job_id":
                    setJob_id(String.valueOf(val));
                    break;
                case "creator":
                    setCreator(String.valueOf(val));
                    break;
                case "account":
                    setAccount(String.valueOf(val));
                    break;
                case "receivable":
                    setReceivable(Double.valueOf(String.valueOf(val)));
                    break;
                case "extra":
                    setExtra(String.valueOf(val));
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Invoice attribute '" + var + "'.");
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
            case "short_id":
                return getShort_id();
            case "job_id":
                return getJob_id();
            case "date_generated":
                return getDate_generated();
            case "creator":
                return getCreator();
            case "account":
                return getAccount();
            case "receivable":
                return getReceivable();
            case "extra":
                return getExtra();
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Invoice attribute '" + var + "'.");
                return null;
        }
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
            result.append("&" + URLEncoder.encode("job_id","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(job_id), "UTF-8"));
            if(date_generated>0)
                result.append("&" + URLEncoder.encode("date_generated","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_generated), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(creator), "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
            result.append("&" + URLEncoder.encode("receivable","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(receivable), "UTF-8"));
            if(extra!=null)
                result.append(URLEncoder.encode("extra","UTF-8") + "="
                        + URLEncoder.encode(extra, "UTF-8") + "&");
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/invoice";
    }
}
