package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/03.
 */
public class JobEmployee extends BusinessObject implements Serializable
{
    private String job_id;
    private String usr;
    private long date_logged;
    public static final String TAG = "JobEmployee";

    private StringProperty job_idProperty(){return new SimpleStringProperty(job_id);}

    public String getJob_id()
    {
        return job_id;
    }

    public void setJob_id(String job_id)
    {
        this.job_id = job_id;
    }

    private StringProperty usrProperty(){return new SimpleStringProperty(usr);}

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr)
    {
        this.usr = usr;
    }

    private StringProperty date_loggedProperty(){return new SimpleStringProperty(String.valueOf(date_logged));}

    public double getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "job_id":
                    job_id = String.valueOf(val);
                    break;
                case "usr":
                    usr = String.valueOf(val);
                    break;
                case "date_logged":
                    date_logged = Long.parseLong((String) val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown JobEmployee attribute '" + var + "'.");
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
            case "usr":
                return usr;
            case "date_logged":
                return date_logged;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown JobEmployee attribute '" + var + "'.");
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
            result.append(URLEncoder.encode("job_id","UTF-8") + "="
                    + URLEncoder.encode(job_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("usr","UTF-8") + "="
                    + URLEncoder.encode(usr, "UTF-8") + "&");
            result.append(URLEncoder.encode("date_logged","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/job/employee";
    }
}
