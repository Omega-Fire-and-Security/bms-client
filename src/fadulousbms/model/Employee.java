/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.AssetManager;
import fadulousbms.managers.BusinessObjectManager;
import fadulousbms.managers.EmployeeManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;

/**
 * Created by ghost on 2017/01/01.
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

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        //if Employee to be created has access rights > standard then user signed in must have superuser access rights
        if(getAccessLevel()>AccessLevel.STANDARD.getLevel())
            return AccessLevel.SUPERUSER;
        else return AccessLevel.STANDARD;
    }

    @Override
    public BusinessObjectManager getManager()
    {
        return EmployeeManager.getInstance();
    }

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr) {
        this.usr = usr;
    }

    public String getPwd()
    {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public int getAccessLevel() {
        return access_level;
    }

    public void setAccessLevel(int access_level)
    {
        this.access_level = access_level;
    }

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

    public String getFirstname()
    {
        return firstname;
    }

    public void setFirstname(String firstname)
    {
        this.firstname = firstname;
    }

    public String getLastname()
    {
        return lastname;
    }

    public void setLastname(String lastname)
    {
        this.lastname = lastname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getTel()
    {
        return tel;
    }

    public void setTel(String tel)
    {
        this.tel = tel;
    }

    public String getCell()
    {
        return cell;
    }

    public void setCell(String cell)
    {
        this.cell = cell;
    }

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

    public String getInitials(){return new String(firstname.substring(0,1) + lastname.substring(0,1));}

    //Properties
    public StringProperty usrProperty(){return new SimpleStringProperty(getUsr());}

    //public StringProperty pwdProperty(){return new SimpleStringProperty(pwd);}

    public StringProperty access_levelProperty(){return new SimpleStringProperty(String.valueOf(getAccessLevel()));}

    public StringProperty activeProperty(){return new SimpleStringProperty(String.valueOf(isActive()));}

    public StringProperty firstnameProperty(){return new SimpleStringProperty(getFirstname());}

    public StringProperty lastnameProperty(){return new SimpleStringProperty(getLastname());}

    public StringProperty nameProperty(){return new SimpleStringProperty(getName());}

    public StringProperty emailProperty(){return new SimpleStringProperty(getEmail());}

    public StringProperty telProperty(){return new SimpleStringProperty(getTel());}

    public StringProperty cellProperty(){return new SimpleStringProperty(getCell());}

    public StringProperty genderProperty(){return new SimpleStringProperty(getGender());}

    @Override
    public void parse(String var, Object val) throws ParseException
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

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"usr\":\""+getUsr()+"\""
                +",\"pwd\":\""+getPwd()+"\""
                +",\"firstname\":\""+getFirstname()+"\""
                +",\"lastname\":\""+getLastname()+"\""
                +",\"gender\":\""+getGender()+"\""
                +",\"email\":\""+getEmail()+"\""
                +",\"cell\":\""+getCell()+"\""
                +",\"tel\":\""+getTel()+"\""
                +",\"access_level\":\""+getAccessLevel()+"\"}";

        return json_obj;
    }

    @Override
    public String toString()
    {
        return getName();
    }

    @Override
    public String apiEndpoint()
    {
        return "/user";
    }
}
