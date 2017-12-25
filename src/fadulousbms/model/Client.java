/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 *
 * @author ghost
 */
public class Client extends BusinessObject implements Serializable
{
    private String client_name;
    private String physical_address;
    private String postal_address;
    private String tel;
    private String fax;
    private String contact_email;
    private String registration_number;
    private String vat_number;
    private String account_name;
    private long date_partnered;
    private String website;
    private boolean active;
    private String other;
    private Job[] jobs;

    public StringProperty client_nameProperty(){return new SimpleStringProperty(client_name);}

    public String getClient_name()
    {
        return client_name;
    }

    public void setClient_name(String client_name)
    {
        this.client_name = client_name;
    }

    public StringProperty physical_addressProperty(){return new SimpleStringProperty(physical_address);}

    public String getPhysical_address()
    {
        return physical_address;
    }

    public void setPhysical_address(String physical_address)
    {
        this.physical_address = physical_address;
    }

    public StringProperty postal_addressProperty(){return new SimpleStringProperty(postal_address);}

    public String getPostal_address()
    {
        return postal_address;
    }

    public void setPostal_address(String postal_address)
    {
        this.postal_address = postal_address;
    }

    public StringProperty telProperty(){return new SimpleStringProperty(tel);}

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public StringProperty faxProperty(){return new SimpleStringProperty(fax);}

    public String getFax()
    {
        return fax;
    }

    public void setFax(String fax)
    {
        this.fax = fax;
    }

    public StringProperty contact_emailProperty(){return new SimpleStringProperty(contact_email);}

    public String getContact_email()
    {
        return contact_email;
    }

    public void setContact_email(String contact_email)
    {
        this.contact_email = contact_email;
    }

    public Job[] getJobs()
    {
        return jobs;
    }

    public void setJobs(Job[] jobs)
    {
        this.jobs = jobs;
    }

    public StringProperty activeProperty(){return new SimpleStringProperty(String.valueOf(active));}

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public long getDate_partnered()
    {
        return date_partnered;
    }

    public void setDate_partnered(long date_partnered)
    {
        this.date_partnered = date_partnered;
    }

    public StringProperty websiteProperty(){return new SimpleStringProperty(website);}

    public String getWebsite()
    {
        return website;
    }

    public void setWebsite(String website)
    {
        this.website = website;
    }

    public StringProperty registration_numberProperty(){return new SimpleStringProperty(getRegistration_number());}

    public String getRegistration_number()
    {
        return registration_number;
    }

    public void setRegistration_number(String registration_number)
    {
        this.registration_number = registration_number;
    }

    public StringProperty vat_numberProperty(){return new SimpleStringProperty(getVat_number());}

    public String getVat_number()
    {
        return vat_number;
    }

    public void setVat_number(String vat_number)
    {
        this.vat_number = vat_number;
    }

    public StringProperty account_nameProperty(){return new SimpleStringProperty(getAccount_name()==null?"N/A":getAccount_name());}

    public String getAccount_name()
    {
        return account_name;
    }

    public void setAccount_name(String account_name)
    {
        this.account_name = account_name;
    }

    public String getOther()
    {
        return other;
    }

    public StringProperty otherProperty(){return new SimpleStringProperty(other);}

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
                case "vat_number":
                    setVat_number((String) val);
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
                case "other":
                    setOther((String) val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Client attribute '" + var + "'.");
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
            case "vat_number":
                return getVat_number();
            case "account_name":
                return getAccount_name();
            case "date_partnered":
                return getDate_partnered();
            case "website":
                return getWebsite();
            case "active":
                return isActive();
            case "other":
                return getOther();
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Client attribute '" + var + "'.");
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
            result.append(URLEncoder.encode("client_name","UTF-8") + "="
                    + URLEncoder.encode(getClient_name(), "UTF-8"));
            result.append("&" + URLEncoder.encode("physical_address","UTF-8") + "="
                    + URLEncoder.encode(getPhysical_address(), "UTF-8"));
            result.append("&" + URLEncoder.encode("postal_address","UTF-8") + "="
                    + URLEncoder.encode(getPostal_address(), "UTF-8"));
            result.append("&" + URLEncoder.encode("tel","UTF-8") + "="
                    + URLEncoder.encode(getTel(), "UTF-8"));
            if(getFax()!=null)
                result.append("&" + URLEncoder.encode("fax","UTF-8") + "="
                        + URLEncoder.encode(getFax(), "UTF-8"));
            result.append("&" + URLEncoder.encode("contact_email","UTF-8") + "="
                    + URLEncoder.encode(getContact_email(), "UTF-8"));
            result.append("&" + URLEncoder.encode("registration_number","UTF-8") + "="
                    + URLEncoder.encode(getRegistration_number(), "UTF-8"));
            result.append("&" + URLEncoder.encode("vat_number","UTF-8") + "="
                    + URLEncoder.encode(getVat_number(), "UTF-8"));
            result.append("&" + URLEncoder.encode("account_name","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(getAccount_name()), "UTF-8"));
            if(getDate_partnered()>0)
                result.append("&" + URLEncoder.encode("date_partnered","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_partnered()), "UTF-8"));
            if(getWebsite()!=null)
                result.append("&" + URLEncoder.encode("website","UTF-8") + "="
                        + URLEncoder.encode(getWebsite(), "UTF-8"));
            result.append("&" + URLEncoder.encode("active","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(isActive()), "UTF-8"));
            if(other!=null)
                result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(getOther(), "UTF-8"));

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String toString()
    {
        return client_name;
    }

    @Override
    public String apiEndpoint()
    {
        return "/clients";
    }
}