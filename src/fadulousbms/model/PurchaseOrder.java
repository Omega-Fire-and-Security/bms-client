package fadulousbms.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.managers.SupplierManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class PurchaseOrder implements BusinessObject, Serializable
{
    private String _id;
    private int number;
    private String supplier_id;
    private String contact_person_id;
    private double vat;
    private long date_logged;
    private String creator;
    private String account;
    private int status;
    private boolean marked;
    private String extra;
    public static final String TAG = "PurchaseOrder";
    public PurchaseOrderItem[] items;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of PurchaseOrder object.
     * @return PurchaseOrder identifier.
     */
    @Override
    public String get_id()
    {
        return _id;
    }

    /**
     * Method to assign identifier to this object.
     * @param _id identifier to be assigned to this object.
     */
    public void set_id(String _id)
    {
        this._id = _id;
    }


    /**
     * Function to get a shortened identifier of this object.
     * @return The shortened identifier.
     */
    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    @Override
    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    @Override
    public boolean isMarked()
    {
        return marked;
    }

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    private StringProperty numberProperty(){return new SimpleStringProperty(String.valueOf(number));}

    public String getNumber()
    {
        return String.valueOf(number);
    }

    public int getNumberValue()
    {
        return number;
    }

    public void setNumber(int number)
    {
        this.number = number;
    }

    private StringProperty vatProperty(){return new SimpleStringProperty(String.valueOf(getVatVal()));}

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

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public String getSupplier_id()
    {
        return supplier_id;
    }

    public void setSupplier_id(String supplier_id)
    {
        this.supplier_id = supplier_id;
    }

    public StringProperty totalProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));
    }

    public double getTotal()
    {
        double total=0;
        for(PurchaseOrderItem item: getItems())
            total+=item.getCostValue()*item.getQuantityValue();
        return total;
    }

    public StringProperty discountProperty()
    {
        return new SimpleStringProperty(String.valueOf(getDiscount() + "%"));
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
        /*Gson gson = new GsonBuilder().create();
        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
        String purchase_order_items_json = RemoteComms.sendGetRequest("/api/purchaseorder/items/" + get_id(), headers);
        return gson.fromJson(purchase_order_items_json, PurchaseOrderItem[].class);*/
        return items;
    }

    public void setItems(PurchaseOrderItem[] items)
    {
        this.items = items;
    }

    public Supplier getSupplier()
    {
        HashMap<String, Supplier> suppliers = SupplierManager.getInstance().getSuppliers();
        if(suppliers!=null)
            return suppliers.get(supplier_id);
        else IO.log(getClass().getName(), IO.TAG_ERROR, "no suppliers were found in database.");
        return null;
    }

    public Employee getContact_person()
    {
        EmployeeManager.getInstance().loadDataFromServer();
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getEmployees();
        if(employees!=null)
            return employees.get(contact_person_id);
        return null;
    }

    public StringProperty creatorProperty()
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

    private StringProperty accountProperty(){return new SimpleStringProperty(account);}

    public String getAccount()
    {
        return account;
    }

    public void setAccount(String account)
    {
        this.account = account;
    }

    private StringProperty statusProperty(){return new SimpleStringProperty(String.valueOf(status));}

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
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

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "number":
                    setNumber(Integer.valueOf((String)val));
                    break;
                case "supplier_id":
                    setSupplier_id(String.valueOf(val));
                    break;
                case "contact_person_id":
                    setContact_person_id(String.valueOf(val));
                    break;
                case "vat":
                    setVat(Double.valueOf((String)val));
                    break;
                case "account":
                    setAccount((String)val);
                    break;
                case "date_logged":
                    setDate_logged(Long.valueOf((String)val));
                    break;
                case "status":
                    setStatus(Integer.valueOf((String)val));
                    break;
                case "creator":
                    setCreator((String)val);
                    break;
                case "extra":
                    setExtra(String.valueOf(val));
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown PurchaseOrder attribute '" + var + "'.");
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
            case "number":
                return getNumber();
            case "supplier_id":
                return getSupplier_id();
            case "contact_person_id":
                return getContact_person_id();
            case "vat":
                return getVatVal();
            case "account":
                return getAccount();
            case "date_logged":
                return getDate_logged();
            case "creator":
                return getCreator();
            case "status":
                return getStatus();
            case "extra":
                return getExtra();
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown PurchaseOrder attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/purchaseorder";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(number), "UTF-8"));
            result.append("&" + URLEncoder.encode("supplier_id","UTF-8") + "="
                    + URLEncoder.encode(supplier_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("contact_person_id","UTF-8") + "="
                    + URLEncoder.encode(contact_person_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("vat","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(vat), "UTF-8"));
            result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            result.append("&" + URLEncoder.encode("account","UTF-8") + "="
                    + URLEncoder.encode(account, "UTF-8"));
            if(date_logged>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_logged), "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            if(extra!=null)
                if(!extra.isEmpty())
                    result.append("&" + URLEncoder.encode("extra","UTF-8") + "="
                            + URLEncoder.encode(extra, "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }
}