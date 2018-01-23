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
public class Employee extends BusinessObject implements Serializable
{
    private String usr;
    private String pwd;//hashed
    private String firstname;
    private String lastname;
    private String gender;
    private String email;
    private String tel;
    private String cell;
    private int access_level;
    private boolean active;
    public static final String TAG = "Employee";
    public static int ACCESS_LEVEL_NONE = 0;
    public static int ACCESS_LEVEL_NORMAL = 1;
    public static int ACCESS_LEVEL_ADMIN = 2;
    public static int ACCESS_LEVEL_SUPER = 3;

    public StringProperty usrProperty(){return new SimpleStringProperty(usr);}

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public StringProperty pwdProperty(){return new SimpleStringProperty(pwd);}

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public StringProperty access_levelProperty(){return new SimpleStringProperty(String.valueOf(access_level));}

    public int getAccessLevel() {
        return access_level;
    }

    public void setAccessLevel(int access_level)
    {
        this.access_level = access_level;
    }

    public StringProperty activeProperty(){return new SimpleStringProperty(String.valueOf(active));}

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

    public StringProperty firstnameProperty(){return new SimpleStringProperty(firstname);}

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public StringProperty lastnameProperty(){return new SimpleStringProperty(lastname);}

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public StringProperty emailProperty(){return new SimpleStringProperty(email);}

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public StringProperty telProperty(){return new SimpleStringProperty(usr);}

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public StringProperty cellProperty(){return new SimpleStringProperty(usr);}

    public String getCell()
    {
        return cell;
    }

    public void setCell(String cell)
    {
        this.cell = cell;
    }

    public StringProperty genderProperty(){return new SimpleStringProperty(usr);}

    public String getGender()
    {
        return gender;
    }

    public void setGender(String gender)
    {
        this.gender = gender;
    }

    public String getName()
    {
        return getFirstname() + " " + getLastname();
    }

    @Override
    public void parse(String var, Object val)
    {
        super.parse(var, val);
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
                case "active":
                    setActive(Boolean.parseBoolean((String)val));
                    break;
                case "other":
                    setOther((String)val);
                    break;
                default:
                    IO.log(TAG, IO.TAG_WARN, String.format("unknown "+getClass().getName()+" attribute '%s'", var));
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
            case "active":
                return active;
        }
        return super.get(var);
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
            result.append(URLEncoder.encode("date_logged","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8") + "&");
            result.append(URLEncoder.encode("active","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(active), "UTF-8"));
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
        String json_obj = "{"+(get_id()!=null?"\"_id\":\""+get_id()+"\",":"")
                +"\"usr\":\""+getUsr()+"\""
                +",\"pwd\":\""+getPwd()+"\""
                +",\"firstname\":\""+getFirstname()+"\""
                +",\"lastname\":\""+getLastname()+"\""
                +",\"gender\":\""+getGender()+"\""
                +",\"email\":\""+getEmail()+"\""
                +",\"cell\":\""+getCell()+"\""
                +",\"tel\":\""+getTel()+"\""
                +",\"access_level\":\""+getAccessLevel()+"\"";
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
        return "/employees";
    }
}
