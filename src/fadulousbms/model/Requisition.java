package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.QuoteManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/21.
 */
public class Requisition extends BusinessObject
{
    private String client_id;
    private String responsible_person_id;
    private String description;
    private String type;
    private int status;
    public static final String TAG = "Requisition";

    public StringProperty client_idProperty(){return new SimpleStringProperty(client_id);}

    public String getClient_id()
    {
        return client_id;
    }

    public void setClient_id(String client_id)
    {
        this.client_id = client_id;
    }

    public StringProperty responsible_person_idProperty(){return new SimpleStringProperty(responsible_person_id);}

    public String getResponsible_person_id()
    {
        return responsible_person_id;
    }

    public void setResponsible_person_id(String responsible_person_id)
    {
        this.responsible_person_id = responsible_person_id;
    }

    public StringProperty typeProperty(){return new SimpleStringProperty(type);}

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public StringProperty descriptionProperty(){return new SimpleStringProperty(description);}

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    private StringProperty statusProperty()
    {
        return new SimpleStringProperty(getStatusName());
    }

    private String getStatusName()
    {
        switch (status)
        {
            case BusinessObject.STATUS_APPROVED:
                return "Approved";
            case BusinessObject.STATUS_PENDING:
                return "Pending";
            case BusinessObject.STATUS_ARCHIVED:
                return "Archived";
            default:
                return "Unknown " + getClass().getName() + " status: " + status;
        }
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public Client getClient()
    {
        HashMap<String, Client> clients = (HashMap<String, Client>) ClientManager.getInstance().getDataset();
        if(clients!=null)
            return clients.get(client_id);
        else IO.log(getClass().getName(), IO.TAG_ERROR, "no clients were found in database.");
        return null;
    }

    public Employee getResponsible_person()
    {
        EmployeeManager.getInstance().initialize();
        HashMap<String, Employee> employees = EmployeeManager.getInstance().getDataset();
        if(employees!=null)
            return employees.get(responsible_person_id);
        return null;
    }

    public SimpleStringProperty responsible_personProperty()
    {
        if(getResponsible_person()!=null)
            return new SimpleStringProperty(getResponsible_person().getName());
        else return new SimpleStringProperty(this.getResponsible_person_id());
    }

    public SimpleStringProperty client_nameProperty()
    {
        if(getClient()!=null)
            return new SimpleStringProperty(getClient().getClient_name());
        else return new SimpleStringProperty(getClient_id());
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
                case "responsible_person_id":
                    responsible_person_id = (String)val;
                    break;
                case "type":
                    type = String.valueOf(val);
                    break;
                case "description":
                    description = String.valueOf(val);
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
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
            case "responsible_person_id":
                return responsible_person_id;
            case "type":
                return type;
            case "description":
                return description;
            case "status":
                return status;
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"responsible_person_id\":\""+ responsible_person_id +"\""
                +",\"type\":\""+type+"\""
                +",\"description\":\""+ description +"\"";
                if(getClient_id()!=null)
                    json_obj+=",\"client_id\":\""+client_id+"\"";
                if(getStatus()>0)
                    json_obj+=",\"status\":\""+status+"\"";
                json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String apiEndpoint()
    {
        return "/requisitions";
    }
}
