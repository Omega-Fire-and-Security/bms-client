/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.managers;

import fadulousbms.auxilary.Session;
import fadulousbms.model.Employee;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author ghost
 */
public class SessionManager 
{
    private static final SessionManager sess_mgr = new SessionManager();
    private List<Session> sessions = new ArrayList<Session>();
    private Session active;
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
        Session s = getUserSession(session.getUsr());
        if(s!=null)
        {
            s.setDate(session.getDate());
            s.setSession_id(session.getSession_id());
            s.setTtl(session.getTtl());
            setActive(s);
        } else
        {
            sessions.add(session);
            setActive(session);
        }
    }

    public Employee getActiveEmployee()
    {
        if(getActive()!=null)
        {
            EmployeeManager.getInstance().initialize();
            if (EmployeeManager.getInstance().getDataset() != null)
                return EmployeeManager.getInstance().getDataset().get(getActive().getUsr());
        }
        return null;
    }
    
    public List<Session> getSessions()
    {
        return sessions;
    }
    
    public Session getUserSession(String usr)
    {
        for(Session s : sessions)
        {
            if(s.getUsr().equals(usr))
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
