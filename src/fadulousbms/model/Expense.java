package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/21.
 */
public class Expense extends BusinessObject implements Serializable
{
    private String expense_title;
    private String expense_description;
    private double expense_value;
    private String supplier;
    private long date_logged;
    private String creator;
    private String account;
    private String other;
    private Employee creator_employee;
    private Supplier supplier_obj;
    public static final String TAG = "Expense";

    public String getSupplier()
    {
        return supplier;
    }

    public void setSupplier(String supplier)
    {
        this.supplier = supplier;
    }

    public void setSupplier_obj(Supplier supplier_obj)
    {
        this.supplier_obj=supplier_obj;
        if(supplier_obj!=null)
            setSupplier(supplier_obj.get_id());
    }

    public Supplier getSupplier_obj()
    {
        return supplier_obj;
    }

    public StringProperty expense_titleProperty(){return new SimpleStringProperty(expense_title);}

    public String getExpense_title()
    {
        return expense_title;
    }

    public void setExpense_title(String expense_title)
    {
        this.expense_title = expense_title;
    }

    public StringProperty expense_descriptionProperty(){return new SimpleStringProperty(expense_description);}

    public String getExpense_description()
    {
        return expense_description;
    }

    public void setExpense_description(String expense_description)
    {
        this.expense_description = expense_description;
    }

    public StringProperty expense_valueProperty(){return new SimpleStringProperty(String.valueOf(expense_value));}

    public double getExpense_value()
    {
        return expense_value;
    }

    public void setExpense_value(double expense_value)
    {
        this.expense_value = expense_value;
    }

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public StringProperty creatorProperty()
    {
        if(creator_employee==null)
            return new SimpleStringProperty(String.valueOf(creator));
        else return new SimpleStringProperty(String.valueOf(creator_employee.toString()));
    }

    public String getCreator()
    {
        if(creator_employee==null)
            return creator;
        else return creator_employee.toString();
    }

    public String getCreatorID(){return this.creator;}

    public Employee getCreatorEmployee()
    {
        return this.creator_employee;
    }

    public void setCreator(Employee creator_employee)
    {
        this.creator_employee = creator_employee;
        if(creator_employee!=null)
            setCreator(creator_employee.getUsr());
    }

    public void setCreator(String creator)
    {
        this.creator = creator;
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

    public StringProperty otherProperty(){return new SimpleStringProperty(other);}

    public String getOther()
    {
        return other;
    }

    public void setOther(String other)
    {
        this.other = other;
    }

    @Override
    public void parse(String var, Object val)
    {
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
                case "date_logged":
                    date_logged = Long.parseLong(String.valueOf(val));
                    break;
                case "creator":
                    creator = String.valueOf(val);
                    break;
                case "account":
                    account = String.valueOf(val);
                    break;
                case "other":
                    other = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR,"unknown Expense attribute '" + var + "'.");
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
            case "expense_title":
                return expense_title;
            case "expense_description":
                return expense_description;
            case "expense_value":
                return expense_value;
            case "supplier":
                return supplier;
            case "date_logged":
                return date_logged;
            case "creator":
                return creator;
            case "account":
                return account;
            case "other":
                return other;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR,"unknown Expense attribute '" + var + "'.");
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
            result.append(URLEncoder.encode("expense_title","UTF-8") + "="
                    + URLEncoder.encode(expense_title, "UTF-8"));
            result.append("&" + URLEncoder.encode("expense_description","UTF-8") + "="
                    + URLEncoder.encode(expense_description, "UTF-8"));
            result.append("&" + URLEncoder.encode("expense_value","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(expense_value), "UTF-8"));
            result.append("&" + URLEncoder.encode("supplier","UTF-8") + "="
                    + URLEncoder.encode(supplier, "UTF-8"));
            if(date_logged>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
            if(other!=null)
                if(!other.isEmpty())
                    result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                            + URLEncoder.encode(other, "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String toString()
    {
        return this.expense_title;
    }

    @Override
    public String apiEndpoint()
    {
        return "/expenses";
    }
}
