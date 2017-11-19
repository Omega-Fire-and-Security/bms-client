package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.model.Employee;
import fadulousbms.model.Revenue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/09/15.
 */
public class RevenueManager extends BusinessObjectManager
{
    private Revenue[] revenues = null;
    private static RevenueManager revenue_manager = new RevenueManager();
    private ScreenManager screenManager = null;
    private Revenue selected_revenue;
    private Gson gson;
    private long timestamp;
    public static final String ROOT_PATH = "cache/revenue/";
    public String filename = "";

    public static RevenueManager getInstance()
    {
        return revenue_manager;
    }

    @Override
    public void initialize()
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if (smgr.getActive() != null)
            {
                if (!smgr.getActive().isExpired())
                {
                    EmployeeManager.getInstance().initialize();
                    SupplierManager.getInstance().initialize();

                    gson = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/revenues_timestamp", headers);
                    Counters cnt_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if (cnt_timestamp != null)
                    {
                        timestamp = cnt_timestamp.getCount();
                        filename = "revenues_"+timestamp+".dat";
                        IO.log(RevenueManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + cnt_timestamp.getCount());
                    } else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if (!this.isSerialized(ROOT_PATH+filename))
                    {
                        //Load Revenue
                        String expenses_json = RemoteComms.sendGetRequest("/api/revenues", headers);
                        revenues = gson.fromJson(expenses_json, Revenue[].class);

                        if (revenues != null)
                        {
                            for (Revenue revenue : revenues)
                            {
                                //Set Expense creator
                                for (Employee employee : (Employee[]) EmployeeManager.getInstance().getEmployees().values().toArray())
                                {
                                    if (employee.getUsr().equals(revenue.getCreator()))
                                    {
                                        revenue.setCreator(employee);
                                        break;
                                    }
                                }
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of revenues.");
                            this.serialize(ROOT_PATH+filename, revenues);
                        }else{
                            IO.log(getClass().getName(), IO.TAG_ERROR, "revenues object is null.");
                            IO.showMessage("No additional revenue", "no additional revenue found in database.", IO.TAG_ERROR);
                        }
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        revenues = (Revenue[]) this.deserialize(ROOT_PATH+filename);
                    }
                }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
        }catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
        }catch (ClassNotFoundException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    public Revenue[] getRevenues()
    {
        return revenues;
    }

    public void setSelected(Revenue revenue)
    {
        if(revenue!=null)
        {
            this.selected_revenue = revenue;
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected revenue to: " + selected_revenue);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "revenue to be set as selected is null.");
    }

    public void setSelected(String revenue_id)
    {
        for(Revenue revenue : revenues)
        {
            if(revenue.get_id().equals(revenue_id))
            {
                setSelected(revenue);
                break;
            }
        }
    }
    public Revenue getSelected()
    {
        return selected_revenue;
    }
}
