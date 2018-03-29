package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.BusinessObjectManager;
import fadulousbms.managers.SupplierManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by ghost on 2017/01/03.
 * @author ghost
 */
public class Supplier extends BusinessObject implements Serializable
{
    private String supplier_name;
    private String physical_address;
    private String postal_address;
    private String tel;
    private String fax;
    private String contact_email;
    private String speciality;
    private boolean active;
    private long date_partnered;
    private String website;
    private String registration_number;
    private String vat_number;
    private String account_name;

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.ADMIN;
    }

    @Override
    public BusinessObjectManager getManager()
    {
        return SupplierManager.getInstance();
    }

    public StringProperty supplier_nameProperty(){return new SimpleStringProperty(supplier_name);}

    public String getSupplier_name()
    {
        return supplier_name;
    }

    public void setSupplier_name(String supplier_name)
    {
        this.supplier_name = supplier_name;
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

    public StringProperty specialityProperty(){return new SimpleStringProperty(speciality);}

    public String getSpeciality()
    {
        return speciality;
    }

    public void setSpeciality(String speciality)
    {
        this.speciality = speciality;
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

    //public StringProperty date_partneredProperty(){return new SimpleStringProperty(String.valueOf(date_partnered));}

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

    public StringProperty account_nameProperty(){return new SimpleStringProperty(getAccount_name()==null?"N/A":getAccount_name());}

    public String getAccount_name()
    {
        return account_name;
    }

    public void setAccount_name(String account_name)
    {
        this.account_name = account_name;
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

    public StringProperty contact_emailProperty(){return new SimpleStringProperty(contact_email);}

    public String getContact_email()
    {
        return contact_email;
    }

    public void setContact_email(String contact_email)
    {
        this.contact_email = contact_email;
    }

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "supplier_name":
                    setSupplier_name((String)val);
                    break;
                case "physical_address":
                    setPhysical_address((String)val);
                    break;
                case "postal_address":
                    setPostal_address((String)val);
                    break;
                case "tel":
                    setTel((String)val);
                    break;
                case "fax":
                    setFax((String)val);
                    break;
                case "contact_email":
                    setContact_email((String)val);
                    break;
                case "speciality":
                    setSpeciality((String)val);
                    break;
                case "registration_number":
                    setRegistration_number((String)val);
                    break;
                case "vat_number":
                    setVat_number((String)val);
                    break;
                case "account_name":
                    setAccount_name((String)val);
                    break;
                case "date_partnered":
                    setDate_partnered(Long.parseLong(String.valueOf(val)));
                    break;
                case "website":
                    setWebsite((String)val);
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
            case "supplier_name":
                return getSupplier_name();
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
            case "speciality":
                return getSpeciality();
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
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"supplier_name\":\""+getSupplier_name()+"\""
                +",\"tel\":\""+getTel()+"\""
                +",\"fax\":\""+getFax()+"\""
                +",\"physical_address\":\""+getPhysical_address()+"\""
                +",\"postal_address\":\""+getPostal_address()+"\""
                +",\"contact_email\":\""+getContact_email()+"\""
                +",\"website\":\""+getWebsite()+"\""
                +",\"speciality\":\""+getSpeciality()+"\""
                +",\"account_name\":\""+getAccount_name()+"\""
                +",\"registration_number\":\""+getRegistration_number()+"\""
                +",\"vat_number\":\""+getVat_number()+"\"";
        if(getDate_partnered()>0)
            json_obj+=",\"date_partnered\":\""+getDate_partnered()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        return getSupplier_name();
    }

    @Override
    public String apiEndpoint()
    {
        return "/supplier";
    }
}
