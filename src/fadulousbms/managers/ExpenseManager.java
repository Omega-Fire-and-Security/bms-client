package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.ServerObject;
import fadulousbms.model.*;
import javafx.util.Callback;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/09/15.
 */
public class ExpenseManager extends BusinessObjectManager
{
    private HashMap<String, Expense> expenses = null;
    private static ExpenseManager expense_manager = new ExpenseManager();
    private Gson gson;
    private long timestamp;
    public static final String ROOT_PATH = "cache/expenses/";
    public String filename = "";

    private ExpenseManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static ExpenseManager getInstance()
    {
        return expense_manager;
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
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));
                            //Get Timestamp
                            String timestamp_json = RemoteComms.get("/timestamp/expenses_timestamp", headers);
                            Counters cnt_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cnt_timestamp != null)
                            {
                                timestamp = cnt_timestamp.getCount();
                                filename = "expenses_" + timestamp + ".dat";
                                IO.log(ExpenseManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + cnt_timestamp.getCount());
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                //Load Expenses
                                String expenses_json = RemoteComms.get("/expenses", headers);
                                ExpenseServerObject expenseServerObject = (ExpenseServerObject) ExpenseManager.getInstance().parseJSONobject(expenses_json, new ExpenseServerObject());

                                if (expenseServerObject != null)
                                {
                                    if (expenseServerObject.get_embedded() != null)
                                    {
                                        Expense[] expenses_arr = expenseServerObject.get_embedded().getExpenses();

                                        expenses = new HashMap<>();
                                        for (Expense expense : expenses_arr)
                                            expenses.put(expense.get_id(), expense);
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Expenses in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "ExpenseServerObject (containing Expense objects & other metadata) is null");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of expenses.");
                                serialize(ROOT_PATH + filename, expenses);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                expenses = (HashMap<String, Expense>) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "Active session is invalid.", IO.TAG_ERROR);
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

    public HashMap<String,Expense> getDataset()
    {
        return expenses;
    }

    class ExpenseServerObject extends ServerObject
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
            private Expense[] expenses;

            public Expense[] getExpenses()
            {
                return expenses;
            }

            public void setExpenses(Expense[] expenses)
            {
                this.expenses = expenses;
            }
        }
    }
}
