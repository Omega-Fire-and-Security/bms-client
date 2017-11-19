package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.model.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/09/15.
 */
public class ExpenseManager extends BusinessObjectManager
{
    private Expense[] expenses = null;
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
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/expenses_timestamp", headers);
                    Counters cnt_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if (cnt_timestamp != null)
                    {
                        timestamp = cnt_timestamp.getCount();
                        filename = "expenses_"+timestamp+".dat";
                        IO.log(ExpenseManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + cnt_timestamp.getCount());
                    } else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if (!this.isSerialized(ROOT_PATH+filename))
                    {
                        //Load Expenses
                        String expenses_json = RemoteComms.sendGetRequest("/api/expenses", headers);
                        expenses = gson.fromJson(expenses_json, Expense[].class);

                        if (expenses != null)
                        {
                            for (Expense expense : expenses)
                            {
                                //Set Expense creator
                                for (Employee employee : EmployeeManager.getInstance().getEmployees().values())
                                {
                                    if (employee.getUsr().equals(expense.getCreator()))
                                    {
                                        expense.setCreator(employee);
                                        break;
                                    }
                                }
                                //Load Expense Suppliers
                                if(SupplierManager.getInstance().getSuppliers()!=null)
                                {
                                    for (Supplier supplier : SupplierManager.getInstance().getSuppliers().values())
                                    {
                                        if (supplier.get_id().equals(expense.getSupplier()))
                                        {
                                            expense.setSupplier_obj(supplier);
                                            break;
                                        }
                                    }
                                }else IO.log(getClass().getName(), IO.TAG_ERROR, "no suppliers found in database.");
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of expenses.");
                            this.serialize(ROOT_PATH+filename, expenses);
                        }else{
                            IO.log(getClass().getName(), IO.TAG_ERROR, "expense object is null.");
                            IO.showMessage("No expenses", "no expenses found in database.", IO.TAG_ERROR);
                        }
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        expenses = (Expense[]) this.deserialize(ROOT_PATH+filename);
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

    public Expense[] getExpenses()
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
        for(Expense expense : expenses)
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
}
