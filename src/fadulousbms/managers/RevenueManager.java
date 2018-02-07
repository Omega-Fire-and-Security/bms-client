package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.ServerObject;
import fadulousbms.model.Revenue;
import javafx.util.Callback;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/09/15.
 */
public class RevenueManager extends BusinessObjectManager
{
    private HashMap<String,Revenue> revenue_records = null;
    private static RevenueManager revenue_manager = new RevenueManager();
    private Gson gson;
    private long timestamp;
    public static final String ROOT_PATH = "cache/revenue/";
    public String filename = "";

    private RevenueManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static RevenueManager getInstance()
    {
        return revenue_manager;
    }

    @Override
    public HashMap<String,Revenue> getDataset()
    {
        return revenue_records;
    }

    @Override
    Callback getSynchronisationCallback()
    {
        return new Callback()
        {
            @Override
            public Object call(Object param)
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));
                            //Get Timestamp
                            String timestamp_json = RemoteComms.sendGetRequest("/timestamp/revenues_timestamp", headers);
                            Counters cnt_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cnt_timestamp != null)
                            {
                                timestamp = cnt_timestamp.getCount();
                                filename = "revenues_"+timestamp+".dat";
                                IO.log(RevenueManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + cnt_timestamp.getCount());
                            } else {
                                IO.log(this.getClass().getName(), IO.TAG_ERROR, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH+filename))
                            {
                                //Load Revenue
                                String revenues_json = RemoteComms.sendGetRequest("/revenue_records", headers);
                                RevenueServerObject revenueServerObject = gson.fromJson(revenues_json, RevenueServerObject.class);
                                if (revenueServerObject != null)
                                {
                                    if(revenueServerObject.get_embedded()!=null)
                                    {
                                        Revenue[] revenues_arr = revenueServerObject.get_embedded().getRevenues();

                                        revenue_records = new HashMap<>();
                                        for (Revenue revenue : revenues_arr)
                                            revenue_records.put(revenue.get_id(), revenue);
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Revenues in database.");
                                } else IO.log(getClass().getName(), IO.TAG_WARN, "no revenue records found in the database.");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of revenue_records.");
                                serialize(ROOT_PATH+filename, revenue_records);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                                revenue_records = (HashMap<String,Revenue>) deserialize(ROOT_PATH+filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Invalid Session", "No valid active sessions found.", IO.TAG_ERROR);
                } catch (MalformedURLException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (ClassNotFoundException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
                return null;
            }
        };
    }

    class RevenueServerObject extends ServerObject
    {
        private Embedded _embedded;

        Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private Revenue[] revenues;

            public Revenue[] getRevenues()
            {
                return revenues;
            }

            public void setRevenues(Revenue[] revenues)
            {
                this.revenues = revenues;
            }
        }
    }
}
