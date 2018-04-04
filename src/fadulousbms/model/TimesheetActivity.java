/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.ClientManager;
import fadulousbms.managers.TaskManager;
import fadulousbms.managers.TimesheetManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by th3gh0st on 2018/03/29.
 * @author th3gh0st
 */
public class TimesheetActivity extends ApplicationObject
{
    private String client_id;//or INTERNAL
    private String description;
    private String location;
    private long date_executed;
    private int status;
    public static final int STATUS_COMPLETED = 1;
    public static final String INTERNAL_ACTIVITY = "INTERNAL";

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
        return TimesheetManager.getInstance();
    }

    //Getters and setters

    public long getDate_executed()
    {
        return date_executed;
    }

    public void setDate_executed(long date_executed)
    {
        this.date_executed = date_executed;
    }

    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getClient_id()
    {
        return client_id;
    }

    public void setClient_id(String client_id)
    {
        this.client_id = client_id;
    }

    /**
     * Method to return an instance of a Client object associated with this TimesheetActivity using the client_id
     * attribute, if the client_id is == TimesheetActivity.INTERNAL_ACTIVITY then return an instance of the local organisation
     * @return an instance of a Client object associated with this TimesheetActivity.
     */
    public Client getClient()
    {
        if(ClientManager.getInstance().getDataset() != null)
        {
            if (getClient_id() != null)
            {
                if (getClient_id().toLowerCase().equals(TimesheetActivity.INTERNAL_ACTIVITY.toLowerCase()))
                {
                    return (Client) new Client().setClient_name(Globals.COMPANY.getValue())
                                                .setActive(true)
                                                .setPhysical_address(Globals.PHYSICAL_ADDRESS.getValue())
                                                .setPostal_address(Globals.POSTAL_ADDRESS.getValue())
                                                .setRegistration_number(Globals.REGISTRATION_NUMBER.getValue())
                                                .setTax_number(Globals.TAX_NUMBER.getValue())
                                                .setContact_email(Globals.EMAIL.getValue())
                                                .setTel(Globals.TEL.getValue()).setWebsite(Globals.WEBSITE.getValue())
                                                .set_id(Client.INTERNAL);
                } else return ClientManager.getInstance().getDataset().get(getClient_id());
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid client_id");
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "no clients were found in the database.");

        return null;
    }

    //TimesheetActivity Model Properties

    public StringProperty client_idProperty()
    {
        return new SimpleStringProperty(getClient_id());
    }

    public StringProperty descriptionProperty()
    {
        return new SimpleStringProperty(getDescription());
    }

    //
    /**
     * Method to parse Model attribute.
     * @param var TimesheetActivity attribute to be parsed.
     * @param val TimesheetActivity attribute value to be set.
     */
    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "client_id":
                    client_id = (String)val;
                    break;
                case "status":
                    status = Integer.parseInt(String.valueOf(val));
                    break;
                case "date_executed":
                    date_executed = Long.parseLong(String.valueOf(val));
                    break;
                case "description":
                    description = (String)val;
                    break;
                case "location":
                    location = (String)val;
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown "+getClass().getName()+" attribute '" + var + "'.");
                    break;
            }
        } catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    /**
     * @param var TimesheetActivity attribute whose value is to be returned.
     * @return TimesheetActivity attribute value.
     */
    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "client_id":
                return getClient_id();
            case "status":
                return getStatus();
            case "date_executed":
                return getDate_executed();
            case "description":
                return getDescription();
            case "location":
                return getLocation();
        }
        return super.get(var);
    }

    /**
     * @return JSON representation of TimesheetActivity object.
     */
    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"client_id\":\""+getClient_id()+"\""
                +",\"description\":\""+getDescription()+"\""
                +",\"status\":\""+getStatus()+"\"";
        if(getDate_executed()>0)
            json_obj+=",\"date_executed\":\""+getDate_executed()+"\"";
        if(location!=null)
            json_obj+=",\"location\":\""+getLocation()+"\"";
        json_obj+="}";

        return json_obj;
    }

    @Override
    public String toString()
    {
        Client client = getClient();
        String str = "#" + getObject_number() + " " + getDescription();
        if(client!=null)
            str += ", for client " + client.toString();
        return str;
    }

    /**
     * @return TimesheetActivity model's endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/timesheet/activity";
    }
}