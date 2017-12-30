package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.QuoteManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class Quote extends BusinessObject
{
    private String client_id;
    private String contact_person_id;
    private String sitename;
    private String request;
    private double vat;
    private String account_name;
    private double revision;
    private int status;
    private String parent_id;
    private QuoteItem[] resources;
    private QuoteRep[] representatives;
    private int rev_cursor = -1;
    public static final String TAG = "Quote";
    public static final int STATUS_PENDING =0;
    public static final int STATUS_APPROVED =1;
    public static final int STATUS_ARCHIVED =2;

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

    public StringProperty parent_idProperty()
    {
        return new SimpleStringProperty(String.valueOf(getParentID()));
    }

    public Quote getParent_id()
    {
        if(parent_id ==null)
            return null;
        else
        {
            QuoteManager.getInstance().loadDataFromServer();
            return QuoteManager.getInstance().getQuotes().get(parent_id);
        }
    }

    public String getParentID(){return this.parent_id;}

    public void setParent_id(String parent_id)
    {
        this.parent_id = parent_id;
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
            IO.log(getClass().getName(), IO.TAG_ERROR, "quote ["+get_id()+"] has no representatives.");
            return null;
        }
        if(representatives.length==0)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "quote ["+get_id()+"] has no representatives.");
            return null;
        }
        Employee[] reps = new Employee[representatives.length];
        EmployeeManager.getInstance().loadDataFromServer();
        for(int i=0;i<representatives.length;i++)
            reps[i]= representatives[i].getUser();
        return  reps;
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
        while(quote.getParent_id()!=null)
            quote=quote.getParent_id();
        return quote;
    }

    public HashMap<Double, Quote> getSiblingsMap()
    {
        HashMap<Double, Quote> siblings = new HashMap<>();
        siblings.put(this.getRevision(), this);//make self be first child of requested siblings
        if(getParent_id()!=null)
        {
            QuoteManager.getInstance().loadDataFromServer();
            siblings.put(getParent_id().getRevision(), getParent_id());//make parent_id be second child of requested siblings
            if (QuoteManager.getInstance().getQuotes() != null)
            {
                for (Quote quote : QuoteManager.getInstance().getQuotes().values())
                    if (getParentID().equals(quote.getParentID()))
                        siblings.put(quote.getRevision(), quote);
            }
            else IO.log(getClass().getName(), IO.TAG_WARN, "no quotes in database.");
        } else IO.log(getClass().getName(), IO.TAG_WARN, "quote ["+get_id()+"] has no parent_id.");
        return siblings;
    }

    public Quote[] getSortedSiblings(String comparator)
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
                if(quote.getParentID()!=null)
                    if (quote.getParentID().equals(get_id()))
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
    public void parse(String var, Object val)
    {
        super.parse(var, val);
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
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
                    break;
                case "parent_id":
                    parent_id = String.valueOf(val);
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
            case "parent_id":
                return parent_id;
            case "vat":
                return vat;
            case "account_name":
                return account_name;
            case "revision":
                return revision;
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
            result.append(URLEncoder.encode("client_id","UTF-8") + "="
                    + URLEncoder.encode(client_id, "UTF-8") + "&");
            result.append(URLEncoder.encode("contact_person_id","UTF-8") + "="
                    + URLEncoder.encode(contact_person_id, "UTF-8") + "&");
            if(getDate_logged()>0)
                result.append(URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8"));
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
                    + URLEncoder.encode(getCreator(), "UTF-8"));
            if(parent_id !=null)
                result.append("&" + URLEncoder.encode("parent_id","UTF-8") + "="
                        + URLEncoder.encode(parent_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("revision","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(revision), "UTF-8"));
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
    public String toString()
    {
        String json_obj = "{\"_id\":\""+get_id()+"\""
                +",\"contact_person_id\":\""+contact_person_id+"\""
                +",\"sitename\":\""+sitename+"\""
                +",\"request\":\""+request+"\""
                +",\"vat\":\""+vat+"\""
                +",\"account_name\":\""+account_name+"\""
                +",\"revision\":\""+revision+"\"";
                if(getClient_id()!=null)
                    json_obj+=",\"client_id\":\""+client_id+"\"";
                if(parent_id!=null)
                    json_obj+=",\"parent_id\":\""+ parent_id +"\"";
                if(status>0)
                    json_obj+=",\"status\":\""+status+"\"";
                if(getCreator()!=null)
                    json_obj+=",\"creator\":\""+getCreator()+"\"";
                if(getDate_logged()>0)
                    json_obj+=",\"date_logged\":\""+getDate_logged()+"\"";
                json_obj+=",\"other\":\""+getOther()+"\"}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/quotes";
    }
}
