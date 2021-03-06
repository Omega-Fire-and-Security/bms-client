package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.ExpenseManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import java.io.Serializable;

/**
 * Created by th3gh0st on 2017/01/21.
 * @author th3gh0st
 */
public class Expense extends ApplicationObject implements Serializable
{
    private String expense_title;
    private String expense_description;
    private double expense_value;
    private String supplier;
    private String account;
    public static final String TAG = "Expense";

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public ApplicationObjectManager getManager()
    {
        return ExpenseManager.getInstance();
    }

    public String getSupplier()
    {
        return supplier;
    }

    public void setSupplier(String supplier)
    {
        this.supplier = supplier;
    }

    public String getExpense_title()
    {
        return expense_title;
    }

    public void setExpense_title(String expense_title)
    {
        this.expense_title = expense_title;
    }

    public String getExpense_description()
    {
        return expense_description;
    }

    public void setExpense_description(String expense_description)
    {
        this.expense_description = expense_description;
    }

    public double getExpense_value()
    {
        return expense_value;
    }

    public void setExpense_value(double expense_value)
    {
        this.expense_value = expense_value;
    }

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    // Model Properties

    public StringProperty expense_titleProperty(){return new SimpleStringProperty(expense_title);}

    public StringProperty expense_descriptionProperty(){return new SimpleStringProperty(expense_description);}

    public StringProperty expense_valueProperty(){return new SimpleStringProperty(String.valueOf(expense_value));}

    public StringProperty accountProperty(){return new SimpleStringProperty(account);}

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "expense_title":
                    expense_title = (String)val;
                    break;
                case "expense_description":
                    expense_description = (String)val;
                    break;
                case "expense_value":
                    expense_value = Double.parseDouble(String.valueOf(val));
                    break;
                case "supplier":
                    supplier = (String)val;
                    break;
                case "account":
                    account = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR,"unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "expense_title":
                return expense_title;
            case "expense_description":
                return expense_description;
            case "expense_value":
                return expense_value;
            case "supplier":
                return supplier;
            case "account":
                return account;
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"expense_title\":\""+getExpense_title()+"\""
                +",\"expense_description\":\""+getExpense_description()+"\""
                +",\"expense_value\":\""+getExpense_value()+"\""
                +",\"supplier\":\""+getSupplier()+"\""
                +",\"account\":\""+getAccount()+"\"}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/expense";
    }
}
