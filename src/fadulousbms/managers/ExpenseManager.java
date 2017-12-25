package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.ServerObject;
import fadulousbms.model.*;

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
    private ScreenManager screenManager = null;
    private Expense selected_expense;
    private Gson gson;
    private long timestamp;
    public static final String ROOT_PATH = "cache/expenses/";
    public String filename = "";

    public static ExpenseManager getInstance()
    {
        return expense_manager;
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
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));
                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/timestamp/expenses_timestamp", headers);
                    Counters cnt_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if (cnt_timestamp != null)
                    {
                        timestamp = cnt_timestamp.getCount();
                        filename = "expenses_" + timestamp + ".dat";
                        IO.log(ExpenseManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + cnt_timestamp.getCount());
                    }
                    else
                    {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if (!this.isSerialized(ROOT_PATH + filename))
                    {
                        //Load Expenses
                        String expenses_json = RemoteComms.sendGetRequest("/expenses", headers);
                        ExpenseServerObject expenseServerObject = gson
                                .fromJson(expenses_json, ExpenseServerObject.class);
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
                        this.serialize(ROOT_PATH + filename, expenses);
                    } else
                    {
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                        expenses = (HashMap<String, Expense>) this.deserialize(ROOT_PATH + filename);
                    }
                } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            } else IO.logAndAlert("Session Expired", "Active session is invalid.", IO.TAG_ERROR);
        } catch (MalformedURLException ex)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        } catch (ClassNotFoundException e)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }catch (IOException ex)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    public HashMap<String,Expense> getExpenses()
    {
        return expenses;
    }

    public void setSelected(Expense expense)
    {
        if(expense!=null)
        {
            this.selected_expense = expense;
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected expense to: " + selected_expense);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "expense to be set as selected is null.");
    }

    public void setSelected(String expense_id)
    {
        for(Expense expense : expenses.values())
        {
            if(expense.get_id().equals(expense_id))
            {
                setSelected(expense);
                break;
            }
        }
    }
    public Expense getSelected()
    {
        return selected_expense;
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
