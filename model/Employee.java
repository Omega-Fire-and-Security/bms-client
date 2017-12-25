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
public class Employee implements BusinessObject, Serializable
{
    /*private Occupation current_occupation;
    private Job current_job;
    private Comment comments[];*/
    private String _id;
    private String usr;
    private String pwd;//hashed
    private String firstname;
    private String lastname;
    private String gender;
    private String email;
    private long date_joined;
    private String tel;
    private String cell;
    private int access_level;
    private boolean active;
    private String other;
    private boolean marked;
    private Employee[] employees;//JSON object embedded Employees
    public static final String TAG = "Employee";
    public static int ACCESS_LEVEL_NONE = 0;
    public static int ACCESS_LEVEL_NORMAL = 1;
    public static int ACCESS_LEVEL_ADMIN = 2;
    public static int ACCESS_LEVEL_SUPER = 3;

    public Employee()
    {
        /*super._embedded=new Embedded()
        {
            private Employee[] employees;
            public Employee[] getEmployees()
            {
                return employees;
            }

            public void setEmployees(Employee[] employees)
            {
                this.employees = employees;
            }
        };*/
    }

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
    public boolean isMarked()
    {
        return marked;
    }

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    @Override
    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    private StringProperty usrProperty(){return new SimpleStringProperty(usr);}

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    private StringProperty pwdProperty(){return new SimpleStringProperty(pwd);}

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    private StringProperty access_levelProperty(){return new SimpleStringProperty(String.valueOf(access_level));}

    public int getAccessLevel() {
        return access_level;
    }

    public void setAccessLevel(int access_level)
    {
        this.access_level = access_level;
    }

    private StringProperty activeProperty(){return new SimpleStringProperty(String.valueOf(active));}

    public String isActive()
    {
        return String.valueOf(active);
    }

    public boolean isActiveVal()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    private StringProperty otherProperty(){return new SimpleStringProperty(other);}

    public String getOther() 
    {
        return other;
    }

    public void setOther(String other) 
    {
        this.other = other;
    }

    private StringProperty firstnameProperty(){return new SimpleStringProperty(firstname);}

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    private StringProperty lastnameProperty(){return new SimpleStringProperty(lastname);}

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    private StringProperty date_joinedProperty(){return new SimpleStringProperty(String.valueOf(date_joined));}

    public long getDate_joined()
    {
        return date_joined;
    }

    public void setDate_joined(long date_joined)
    {
        this.date_joined = date_joined;
    }

    private StringProperty emailProperty(){return new SimpleStringProperty(email);}

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    private StringProperty telProperty(){return new SimpleStringProperty(usr);}

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    private StringProperty cellProperty(){return new SimpleStringProperty(usr);}

    public String getCell()
    {
        return cell;
    }

    public void setCell(String cell)
    {
        this.cell = cell;
    }

    private StringProperty genderProperty(){return new SimpleStringProperty(usr);}

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    @Override
    public void parse(String var, Object val)
    {
        try
        {
            switch (var.toLowerCase())
            {
                case "firstname":
                    setFirstname((String)val);
                    break;
                case "lastname":
                    setLastname((String)val);
                    break;
                case "usr":
                    setUsr((String)val);
                    break;
                case "gender":
                    setGender((String)val);
                    break;
                case "email":
                    setEmail((String)val);
                    break;
                case "access_level":
                    setAccessLevel(Integer.parseInt((String)val));
                    break;
                case "tel":
                    setTel((String)val);
                    break;
                case "cell":
                    setCell((String)val);
                    break;
                case "date_joined":
                    setDate_joined(Long.parseLong(String.valueOf(val)));
                    break;
                case "active":
                    setActive(Boolean.parseBoolean((String)val));
                    break;
                case "other":
                    setOther((String)val);
                    break;
                default:
                    IO.log(TAG, IO.TAG_WARN, String.format("unknown Employee attribute '%s'", var));
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
            case "firstname":
                return firstname;
            case "lastname":
                return lastname;
            case "usr":
                return usr;
            case "access_level":
                return access_level;
            case "gender":
                return gender;
            case "email":
                return email;
            case "tel":
                return tel;
            case "cell":
                return cell;
            case "date_joined":
                return date_joined;
            case "active":
                return active;
            case "other":
                return other;
            default:
                IO.log(TAG, IO.TAG_WARN, String.format("unknown Employee attribute '%s'", var));
                return null;
        }
    }

    @Override
    public String toString()
    {
        return firstname + " " + lastname;
    }

    public String getInitials(){return new String(firstname.substring(0,1) + lastname.substring(0,1));}

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("usr","UTF-8") + "="
                    + URLEncoder.encode(usr, "UTF-8") + "&");
            result.append(URLEncoder.encode("pwd","UTF-8") + "="
                    + URLEncoder.encode(pwd, "UTF-8") + "&");
            result.append(URLEncoder.encode("access_level","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(access_level), "UTF-8") + "&");
            result.append(URLEncoder.encode("firstname","UTF-8") + "="
                    + URLEncoder.encode(firstname, "UTF-8") + "&");
            result.append(URLEncoder.encode("lastname","UTF-8") + "="
                    + URLEncoder.encode(lastname, "UTF-8") + "&");
            result.append(URLEncoder.encode("gender","UTF-8") + "="
                    + URLEncoder.encode(gender, "UTF-8") + "&");
            result.append(URLEncoder.encode("email","UTF-8") + "="
                    + URLEncoder.encode(email, "UTF-8") + "&");
            result.append(URLEncoder.encode("tel","UTF-8") + "="
                    + URLEncoder.encode(tel, "UTF-8") + "&");
            result.append(URLEncoder.encode("cell","UTF-8") + "="
                    + URLEncoder.encode(cell, "UTF-8") + "&");
            result.append(URLEncoder.encode("date_joined","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(date_joined), "UTF-8") + "&");
            result.append(URLEncoder.encode("active","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(active), "UTF-8"));
            if(other!=null)
                if(!other.isEmpty())
                    result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                        + URLEncoder.encode(other, "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    public Employee[] getEmployees()
    {
        return employees;
    }

    public void setEmployees(Employee[] employees)
    {
        this.employees = employees;
    }

    @Override
    public String apiEndpoint()
    {
        return "/employees";
    }
}
