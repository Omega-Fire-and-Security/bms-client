/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Session;
import fadulousbms.model.Employee;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;

/**
 *
 * @author ghost
 */
public class SessionManager 
{
    private static final SessionManager sess_mgr = new SessionManager();
    private List<Session> sessions = new ArrayList<Session>();
    private Session active;
    private Employee active_employee;
    public static final String TAG = "SessionManager";
    
    private SessionManager(){};
    
    public static SessionManager getInstance()
    {
        return sess_mgr;
    }
    
    public void addSession(Session session)
    {
        if(session==null)
            return;
        Session s = getUserSession(session.getUsername());
        if(s!=null)
        {
            s.setDate(session.getDate());
            s.setSessionId(session.getSessionId());
            s.setTtl(session.getTtl());
            setActive(s);
        }
        else{
            sessions.add(session);
            setActive(session);
        }
        try 
        {
            Session active_sess = getActive();
            if(active_sess!=null)
            {
                ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", active_sess.getSessionId()));
                String employee_json = RemoteComms.sendGetRequest("/api/employee/" + active_sess.getUsername(), headers);
                if(employee_json!=null)
                {
                    if(!employee_json.equals("[]") && !employee_json.equals("null"))
                    {
                        Gson gson = new GsonBuilder().create();
                        Employee e = gson.fromJson(employee_json, Employee.class);
                        setActiveEmployee(e);
                    }else{
                        JOptionPane.showMessageDialog(null, "No user was found that matches the given credentials.","Invalid Credentials", JOptionPane.ERROR_MESSAGE);
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "No user was found that matches the given credentials.","Invalid Credentials", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.","Session Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException ex) 
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }
    }
    
    public void setActiveEmployee(Employee empl)
    {
        this.active_employee=empl;
    }
    
    public Employee getActiveEmployee()
    {
        return this.active_employee;
    }
    
    public List<Session> getSessions()
    {
        return sessions;
    }
    
    public Session getUserSession(String usr)
    {
        for(Session s : sessions)
        {
            if(s.getUsername().equals(usr))
                return s;
        }
        return null;
    }
    
    public void setActive(Session session)
    {
        this.active = session;
    }
    
    public Session getActive()
    {
        return active;
    }
}
