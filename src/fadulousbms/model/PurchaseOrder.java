package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.SupplierManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrder extends BusinessObject
{
    private String supplier_id;
    private String contact_person_id;
    private double vat;
    private String account_name;
    private int status;
    public static final String TAG = "PurchaseOrder";
    public PurchaseOrderItem[] items;

    public String getVat()
    {
        return String.valueOf(getVatVal());
    }

    public double getVatVal()
    {
        return vat;
    }

    public void setVat(double vat)
    {
        this.vat= vat;
    }

    public String getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public double getTotal()
    {
        double total=0;
        if(getItems()!=null)
            for(PurchaseOrderItem item: getItems())
                total+=item.getCostValue()*item.getQuantityValue();
        return total;
    }

    public double getDiscount()
    {
        if(getItems()==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "purchase order has no items.");
            return 0;
        }
        double total_discount=0;
        for(PurchaseOrderItem item: getItems())
            total_discount+=item.getDiscountValue();
        if(total_discount>0)
            return total_discount/((getItems().length));
        return 0;
    }

    public String getContact_person_id()
    {
        return contact_person_id;
    }

    public void setContact_person_id(String contact_person_id)
    {
        this.contact_person_id=contact_person_id;
    }

    public PurchaseOrderItem[] getItems()
    {
        return items;
    }

    public void setItems(PurchaseOrderItem[] items)
    {
        this.items = items;
    }

    public Supplier getSupplier()
    {
        HashMap<String, Supplier> suppliers = SupplierManager.getInstance().getDataset();
        if(suppliers!=null)
            return suppliers.get(supplier_id);
        else IO.log(getClass().getName(), IO.TAG_ERROR, "no Suppliers were found in database.");
        return null;
    }

    public Employee getContact_person()
    {
        EmployeeManager.getInstance().initialize();
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getDataset();
        if(employees!=null)
            return employees.get(contact_person_id);
        return null;
    }

    public String getAccount_name()
    {
        return account_name;
    }

    public void setAccount_name(String account_name)
    {
        this.account_name = account_name;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    //Properties

    public SimpleStringProperty supplier_nameProperty()
    {
        if(getSupplier()!=null)
            return new SimpleStringProperty(getSupplier().getSupplier_name());
        else return new SimpleStringProperty(getSupplier_id());
    }

    private StringProperty account_nameProperty(){return new SimpleStringProperty(account_name);}

    private StringProperty vatProperty(){return new SimpleStringProperty(String.valueOf(getVatVal()));}

    public StringProperty discountProperty()
    {
        return new SimpleStringProperty(String.valueOf(getDiscount() + "%"));
    }

    public StringProperty statusProperty(){return new SimpleStringProperty(String.valueOf(status));}

    public StringProperty totalProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "supplier_id":
                    setSupplier_id(String.valueOf(val));
                    break;
                case "contact_person_id":
                    setContact_person_id(String.valueOf(val));
                    break;
                case "vat":
                    setVat(Double.valueOf((String)val));
                    break;
                case "account_name":
                    setAccount_name((String)val);
                    break;
                case "status":
                    setStatus(Integer.valueOf((String)val));
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown "+getClass().getName()+" attribute '" + var + "'.");
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
            case "supplier_id":
                return getSupplier_id();
            case "contact_person_id":
                return getContact_person_id();
            case "vat":
                return getVatVal();
            case "account_name":
                return getAccount_name();
            case "status":
                return getStatus();
        }
        return super.get(var);
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append("&" + URLEncoder.encode("supplier_id","UTF-8") + "="
                    + URLEncoder.encode(supplier_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("contact_person_id","UTF-8") + "="
                    + URLEncoder.encode(contact_person_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("vat","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(vat), "UTF-8"));
            result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            result.append("&" + URLEncoder.encode("account_name","UTF-8") + "="
                    + URLEncoder.encode(account_name, "UTF-8"));
            if(getDate_logged()>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(getCreator(), "UTF-8"));
            if(getOther()!=null)
                if(!getOther().isEmpty())
                    result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                            + URLEncoder.encode(getOther(), "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"supplier_id\":\""+getSupplier_id()+"\""
                +",\"account_name\":\""+getAccount_name()+"\""
                +",\"contact_person_id\":\""+getContact_person_id()+"\""
                +",\"status\":\""+getStatus()+"\""
                +",\"vat\":\""+getVat()+"\"";
        if(getDiscount()>0)
            json_obj+=",\"discount\":\""+getDiscount() +"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/purchaseorders";
    }
}