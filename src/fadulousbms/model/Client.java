/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.ClientManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by th3gh0st on 2017/01/01.
 * @author th3gh0st
 */
public class Client extends ApplicationObject implements Serializable
{
    private String client_name;
    private String physical_address;
    private String postal_address;
    private String tel;
    private String fax;
    private String contact_email;
    private String registration_number;
    private String tax_number;
    private String account_name;
    private long date_partnered;
    private String website;
    private boolean active;
    private Job[] jobs;
    public static final String INTERNAL = "INTERNAL";

    //Read/Write permissions
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
        return ClientManager.getInstance();
    }

    public String getClient_name()
    {
        return client_name;
    }

    public Client setClient_name(String client_name)
    {
        this.client_name = client_name;
        return this;
    }

    public String getPhysical_address()
    {
        return physical_address;
    }

    public Client setPhysical_address(String physical_address)
    {
        this.physical_address = physical_address;
        return this;
    }

    public String getPostal_address()
    {
        return postal_address;
    }

    public Client setPostal_address(String postal_address)
    {
        this.postal_address = postal_address;
        return this;
    }

    public String getTel()
    {
        return tel;
    }

    public Client setTel(String tel)
    {
        this.tel = tel;
        return this;
    }

    public String getFax()
    {
        return fax;
    }

    public Client setFax(String fax)
    {
        this.fax = fax;
        return this;
    }

    public String getContact_email()
    {
        return contact_email;
    }

    public Client setContact_email(String contact_email)
    {
        this.contact_email = contact_email;
        return this;
    }

    public Job[] getJobs()
    {
        return jobs;
    }

    public boolean isActive()
    {
        return active;
    }

    public Client setActive(boolean active)
    {
        this.active = active;
        return this;
    }

    public long getDate_partnered()
    {
        return date_partnered;
    }

    public Client setDate_partnered(long date_partnered)
    {
        this.date_partnered = date_partnered;
        return this;
    }

    public String getWebsite()
    {
        return website;
    }

    public Client setWebsite(String website)
    {
        this.website = website;
        return this;
    }

    public String getRegistration_number()
    {
        return registration_number;
    }

    public Client setRegistration_number(String registration_number)
    {
        this.registration_number = registration_number;
        return this;
    }

    public String getTax_number()
    {
        return tax_number;
    }

    public Client setTax_number(String tax_number)
    {
        this.tax_number = tax_number;
        return this;
    }

    public String getAccount_name()
    {
        return account_name;
    }

    public Client setAccount_name(String account_name)
    {
        this.account_name = account_name;
        return this;
    }

    //model properties

    public StringProperty client_nameProperty(){return new SimpleStringProperty(client_name);}

    public StringProperty physical_addressProperty(){return new SimpleStringProperty(physical_address);}

    public StringProperty postal_addressProperty(){return new SimpleStringProperty(postal_address);}

    public StringProperty telProperty(){return new SimpleStringProperty(tel);}

    public StringProperty faxProperty(){return new SimpleStringProperty(fax);}

    public StringProperty contact_emailProperty(){return new SimpleStringProperty(contact_email);}

    public StringProperty activeProperty(){return new SimpleStringProperty(String.valueOf(active));}

    public StringProperty websiteProperty(){return new SimpleStringProperty(website);}

    public StringProperty registration_numberProperty(){return new SimpleStringProperty(getRegistration_number());}

    public StringProperty tax_numberProperty(){return new SimpleStringProperty(getTax_number());}

    public StringProperty account_nameProperty(){return new SimpleStringProperty(getAccount_name()==null?"N/A":getAccount_name());}

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "client_name":
                    setClient_name((String) val);
                    break;
                case "physical_address":
                    setPhysical_address((String) val);
                    break;
                case "postal_address":
                    setPostal_address((String) val);
                    break;
                case "tel":
                    setTel((String) val);
                    break;
                case "fax":
                    setFax((String) val);
                    break;
                case "contact_email":
                    setContact_email((String)val);
                    break;
                case "registration_number":
                    setRegistration_number((String) val);
                    break;
                case "tax_number":
                    setTax_number((String) val);
                    break;
                case "account_name":
                    setAccount_name((String)val);
                    break;
                case "date_partnered":
                    setDate_partnered(Long.parseLong(String.valueOf(val)));
                    break;
                case "website":
                    setWebsite((String) val);
                    break;
                case "active":
                    setActive(Boolean.parseBoolean(String.valueOf(val)));
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

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "client_name":
                return getClient_name();
            case "physical_address":
                return getPhysical_address();
            case "postal_address":
                return getPostal_address();
            case "tel":
                return getTel();
            case "fax":
                return getFax();
            case "contact_email":
                return getContact_email();
            case "registration_number":
                return getRegistration_number();
            case "tax_number":
                return getTax_number();
            case "account_name":
                return getAccount_name();
            case "date_partnered":
                return getDate_partnered();
            case "website":
                return getWebsite();
            case "active":
                return isActive();
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"client_name\":\""+getClient_name()+"\""
                +",\"tel\":\""+getTel()+"\""
                +",\"fax\":\""+getFax()+"\""
                +",\"physical_address\":\""+getPhysical_address()+"\""
                +",\"postal_address\":\""+getPostal_address()+"\""
                +",\"contact_email\":\""+getContact_email()+"\""
                +",\"website\":\""+getWebsite()+"\""
                +",\"account_name\":\""+getAccount_name()+"\""
                +",\"registration_number\":\""+getRegistration_number()+"\""
                +",\"tax_number\":\""+ getTax_number()+"\"";
        if(getDate_partnered()>0)
            json_obj+=",\"date_partnered\":\""+getDate_partnered()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        return getClient_name();
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/client";
    }
}