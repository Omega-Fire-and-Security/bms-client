package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.PDF;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.SupplierManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class Quote implements BusinessObject, Serializable
{
    private String _id;
    private String client_id;
    private String contact_person_id;
    private String sitename;
    private String request;
    private double vat;
    private String account_name;
    private long date_generated;
    private String creator;
    private double revision;
    private String extra;
    private int status;
    private String parent;
    private QuoteItem[] resources;
    private QuoteRep[] representatives;
    private int rev_cursor = -1;
    //public static double VAT = 14.0;
    private boolean marked;
    public static final String TAG = "Quote";
    public static final int STATUS_PENDING =0;
    public static final int STATUS_APPROVED =1;
    public static final int STATUS_ARCHIVED =2;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    /**
     * Function to get identifier of Quote object.
     * @return Quote identifier.
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

    public StringProperty client_idProperty(){return new SimpleStringProperty(client_id);}

    public String getClient_id()
    {
        return client_id;
    }

    public void setClient_id(String client_id)
    {
        this.client_id = client_id;
    }

    public StringProperty contact_person_idProperty(){return new SimpleStringProperty(contact_person_id);}

    public String getContact_person_id()
    {
        return contact_person_id;
    }

    public void setContact_person_id(String contact_person_id)
    {
        this.contact_person_id = contact_person_id;
    }

    public StringProperty sitenameProperty(){return new SimpleStringProperty(sitename);}

    public String getSitename()
    {
        return sitename;
    }

    public void setSitename(String sitename)
    {
        this.sitename = sitename;
    }

    public StringProperty requestProperty(){return new SimpleStringProperty(request);}

    public String getRequest()
    {
        return request;
    }

    public void setRequest(String request)
    {
        this.request = request;
    }

    public long getDate_generated()
    {
        return date_generated;
    }

    public void setDate_generated(long date_generated)
    {
        this.date_generated = date_generated;
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

    public int getCursor()
    {
        return rev_cursor;
    }

    public void setCursor(int cursor)
    {
        this.rev_cursor = cursor;
    }

    public StringProperty vatProperty()
    {
        return new SimpleStringProperty(String.valueOf(getVat()));
    }

    public double getVat()
    {
        return vat;
    }

    public void setVat(double vat)
    {
        this.vat = vat;
    }

    private StringProperty account_nameProperty(){return new SimpleStringProperty(getAccount_name());}

    public String getAccount_name()
    {
        return account_name;
    }

    public void setAccount_name(String account_name)
    {
        this.account_name = account_name;
    }

    public StringProperty creatorProperty()
    {
        return new SimpleStringProperty(String.valueOf(getCreator()));
    }

    public String getCreator()
    {
        if(creator==null)
            return "N/A";
        else
        {
            EmployeeManager.getInstance().loadDataFromServer();
            Employee employee = EmployeeManager.getInstance().getEmployees().get(creator);
            if(employee!=null)
                return employee.toString();
            else return "N/A";
        }
    }

    public String getCreatorID(){return this.creator;}

    public void setCreator(String creator)
    {
        this.creator = creator;
    }

    public StringProperty parentProperty()
    {
        return new SimpleStringProperty(String.valueOf(getParentID()));
    }

    public Quote getParent()
    {
        if(parent==null)
            return null;
        else
        {
            QuoteManager.getInstance().loadDataFromServer();
            return QuoteManager.getInstance().getQuotes().get(parent);
        }
    }

    public String getParentID(){return this.parent;}

    public void setParent(String parent)
    {
        this.parent= parent;
    }

    public StringProperty revisionProperty(){return new SimpleStringProperty(String.valueOf(revision));}

    public double getRevision()
    {
        return revision;
    }

    public void setRevision(double revision)
    {
        this.revision = revision;
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

    public SimpleStringProperty totalProperty(){return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));}

    public double getTotal()
    {
        //Compute total including VAT
        double total=0;
        if(this.getResources()!=null)
        {
            for (QuoteItem item : this.getResources())
            {
                total += item.getTotal();
            }
        }
        return total * (getVat()/100) + total;
    }

    public QuoteItem[] getResources()
    {
        return resources;
    }

    public void setResources(QuoteItem[] resources)
    {
        this.resources=resources;
    }

    public Employee[] getRepresentatives()
    {
        if(representatives==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "quote ["+_id+"] has no representatives.");
            return null;
        }
        Employee[] reps = new Employee[representatives.length];
        EmployeeManager.getInstance().loadDataFromServer();
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getEmployees();
        if(employees!=null)
        {
            int i=0;
            for(Employee employee: employees.values())
            {
                employees.get(contact_person_id);
                reps[i] = employee;
            }
            return reps;
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "no employees were found in database.");
        return  null;
    }

    public void setRepresentatives(QuoteRep[] representatives)
    {
        this.representatives=representatives;
    }

    public Client getClient()
    {
        HashMap<String, Client> clients = ClientManager.getInstance().getClients();
        if(clients!=null)
        {
            return clients.get(client_id);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "no clients were found in database.");
        return null;
    }

    public Employee getContact_person()
    {
        EmployeeManager.getInstance().loadDataFromServer();
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getEmployees();
        if(employees!=null)
        {
            return employees.get(contact_person_id);
        }
        return null;
    }

    public Quote getRoot()
    {
        Quote quote = this;
        while(quote.getParent()!=null)
            quote=quote.getParent();
        return quote;
    }

    public HashMap<Double, Quote> getSiblingsMap()
    {
        HashMap<Double, Quote> siblings = new HashMap<>();
        siblings.put(this.getRevision(), this);//make self be first child of requested siblings
        if(getParent()!=null)
        {
            QuoteManager.getInstance().loadDataFromServer();
            siblings.put(getParent().getRevision(), getParent());//make parent be second child of requested siblings
            if (QuoteManager.getInstance().getQuotes() != null)
            {
                for (Quote quote : QuoteManager.getInstance().getQuotes().values())
                    if (getParentID().equals(quote.getParentID()))
                        siblings.put(quote.getRevision(), quote);
            }
            else IO.log(getClass().getName(), IO.TAG_WARN, "no quotes in database.");
        } else IO.log(getClass().getName(), IO.TAG_WARN, "quote ["+get_id()+"] has no parent.");
        return siblings;
    }

    public Quote[] getSiblings(String comparator)
    {
        HashMap<Double, Quote> siblings = getSiblingsMap();
        Quote[] siblings_arr = new Quote[siblings.size()];
        siblings.values().toArray(siblings_arr);
        if(siblings_arr!=null)
            if(siblings_arr.length>0)
            {
                IO.getInstance().quickSort(siblings_arr, 0, siblings_arr.length - 1, comparator);
                return siblings_arr;
            }
        return null;
    }

    public HashMap<Double, Quote> getChildrenMap()
    {
        HashMap<Double, Quote> children = new HashMap<>();
        QuoteManager.getInstance().loadDataFromServer();
        if (QuoteManager.getInstance().getQuotes() != null)
        {
            for (Quote quote : QuoteManager.getInstance().getQuotes().values())
                if (get_id().equals(quote.getParentID()))
                    children.put(quote.getRevision(), quote);
        } else IO.log(getClass().getName(), IO.TAG_WARN, "no quotes in database.");
        return children;
    }

    public Quote[] getChildren(String comparator)
    {
        HashMap<Double, Quote> children = getChildrenMap();
        Quote[] children_arr = new Quote[children.size()];
        children.values().toArray(children_arr);
        if(children_arr!=null)
            if(children_arr.length>0)
            {
                IO.getInstance().quickSort(children_arr, 0, children_arr.length - 1, comparator);
                return children_arr;
            }
        return null;
    }

    public SimpleStringProperty quoteProperty()
    {
        if(this!=null)
            if(this.getContact_person()!=null)
            {
                String quote_number = this.getContact_person().getFirstname() + "-"
                        + this.getContact_person().getInitials() + this.get_id().substring(0,8)
                        + " REV" + String.valueOf(this.getRevision()).substring(0,3);
                return new SimpleStringProperty(quote_number);
            }else return new SimpleStringProperty(this.getContact_person_id());
        else return new SimpleStringProperty("N/A");
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/quote";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("client_id","UTF-8") + "="
                    + URLEncoder.encode(client_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("contact_person_id","UTF-8") + "="
                    + URLEncoder.encode(contact_person_id, "UTF-8") + "&");
            if(date_generated>0)
                result.append(URLEncoder.encode("date_generated","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_generated), "UTF-8"));
            result.append("&" + URLEncoder.encode("sitename","UTF-8") + "="
                    + URLEncoder.encode(sitename, "UTF-8"));
            result.append("&" + URLEncoder.encode("request","UTF-8") + "="
                    + URLEncoder.encode(request, "UTF-8"));
            if(status>0)
                result.append("&" + URLEncoder.encode("status","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(status), "UTF-8"));
            result.append("&" + URLEncoder.encode("vat","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(vat), "UTF-8"));
            result.append("&" + URLEncoder.encode("account_name","UTF-8") + "="
                    + URLEncoder.encode(account_name, "UTF-8"));
            result.append("&" + URLEncoder.encode("creator","UTF-8") + "="
                    + URLEncoder.encode(creator, "UTF-8"));
            if(parent!=null)
                result.append("&" + URLEncoder.encode("parent","UTF-8") + "="
                        + URLEncoder.encode(parent, "UTF-8"));
            result.append("&" + URLEncoder.encode("revision","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(revision), "UTF-8"));
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

    @Override
    public String toString()
    {
        return this._id;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "client_id":
                    client_id = (String)val;
                    break;
                case "contact_person_id":
                    contact_person_id = (String)val;
                    break;
                case "sitename":
                    sitename = String.valueOf(val);
                    break;
                case "request":
                    request = String.valueOf(val);
                    break;
                case "date_generated":
                    date_generated = Long.parseLong(String.valueOf(val));
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
                    break;
                case "creator":
                    creator = String.valueOf(val);
                    break;
                case "parent":
                    parent = String.valueOf(val);
                    break;
                case "revision":
                    revision = Integer.parseInt(String.valueOf(val));
                    break;
                case "vat":
                    vat = Double.parseDouble(String.valueOf(val));
                    break;
                case "account_name":
                    account_name = String.valueOf(val);
                    break;
                case "extra":
                    extra = String.valueOf(val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown Quote attribute '" + var + "'.");
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
                return _id;
            case "client_id":
                return client_id;
            case "contact_person_id":
                return contact_person_id;
            case "sitename":
                return sitename;
            case "request":
                return request;
            case "status":
                return status;
            case "creator":
                return creator;
            case "parent":
                return parent;
            case "vat":
                return vat;
            case "account_name":
                return account_name;
            case "revision":
                return revision;
            case "extra":
                return extra;
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown Quote attribute '" + var + "'.");
                return null;
        }
    }
}
