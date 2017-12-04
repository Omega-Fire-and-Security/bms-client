package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/01/03.
 */
public class Supplier implements BusinessObject, Serializable
{
    private String _id;
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
    private String registration;
    private String vat;
    private String account_name;
    private String other;
    private boolean marked;

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    @Override
    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

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

    public StringProperty registrationProperty(){return new SimpleStringProperty(getRegistration());}

    public String getRegistration()
    {
        return registration;
    }

    public void setRegistration(String registration)
    {
        this.registration = registration;
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

    public StringProperty vatProperty(){return new SimpleStringProperty(getVat());}

    public String getVat()
    {
        return vat;
    }

    public void setVat(String vat)
    {
        this.vat = vat;
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
                case "speciality":
                    setSpeciality((String)val);
                    break;
                case "active":
                    setActive(Boolean.parseBoolean(String.valueOf(val)));
                    break;
                case "date_partnered":
                    setDate_partnered(Long.parseLong(String.valueOf(val)));
                    break;
                case "website":
                    setWebsite((String)val);
                    break;
                case "registration":
                    setRegistration((String)val);
                    break;
                case "vat":
                    setVat((String)val);
                    break;
                case "account_name":
                    setAccount_name((String)val);
                    break;
                case "contact_email":
                    setContact_email((String)val);
                    break;
                case "other":
                    setOther((String)val);
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Supplier attribute '" + var + "'.");
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
            case "speciality":
                return getSpeciality();
            case "active":
                return isActive();
            case "date_partnered":
                return getDate_partnered();
            case "website":
                return getWebsite();
            case "registration":
                return getRegistration();
            case "vat":
                return getVat();
            case "account_name":
                return getAccount_name();
            case "contact_email":
                return getContact_email();
            case "other":
                return getOther();
            default:
                IO.log(getClass().getName(), IO.TAG_ERROR, "unknown Supplier attribute '" + var + "'.");
                return null;
        }
    }

    @Override
    public String apiEndpoint()
    {
        return "/api/supplier";
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("supplier_name","UTF-8") + "="
                    + URLEncoder.encode(supplier_name, "UTF-8"));
            result.append("&" + URLEncoder.encode("physical_address","UTF-8") + "="
                    + URLEncoder.encode(physical_address, "UTF-8"));
            result.append("&" + URLEncoder.encode("postal_address","UTF-8") + "="
                    + URLEncoder.encode(postal_address, "UTF-8"));
            result.append("&" + URLEncoder.encode("tel","UTF-8") + "="
                    + URLEncoder.encode(tel, "UTF-8"));
            if(fax!=null)
                result.append("&" + URLEncoder.encode("fax","UTF-8") + "="
                        + URLEncoder.encode(fax, "UTF-8"));
                    result.append("&" + URLEncoder.encode("speciality","UTF-8") + "="
                            + URLEncoder.encode(speciality, "UTF-8"));
            result.append("&" + URLEncoder.encode("active","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(active), "UTF-8"));
            if(date_partnered>0)
                result.append("&" + URLEncoder.encode("date_partnered","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_partnered), "UTF-8"));
            result.append("&" + URLEncoder.encode("website","UTF-8") + "="
                    + URLEncoder.encode(website, "UTF-8"));
            result.append("&" + URLEncoder.encode("registration","UTF-8") + "="
                    + URLEncoder.encode(registration, "UTF-8"));
            result.append("&" + URLEncoder.encode("vat","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(getVat()), "UTF-8"));
            result.append("&" + URLEncoder.encode("account_name","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(getAccount_name()), "UTF-8"));
            result.append("&" + URLEncoder.encode("contact_email","UTF-8") + "="
                    + URLEncoder.encode(contact_email, "UTF-8"));
            if(other!=null)
                result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(other, "UTF-8"));

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
        return supplier_name;
    }
}
