/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.auxilary;

/**
 *
 * @author ghost
 */
public class Session 
{
    private String usr;
    private String session_id;
    private long date;
    private int ttl;
    
    public Session(){}
    
    public Session(String usr, String session_id, int date, int ttl)
    {
        this.usr = usr;
        this.session_id=session_id;
        this.date=date;
        this.ttl=ttl;
    }
    
    public String getSessionId() 
    {
        return session_id;
    }

    public void setSessionId(String session_id) 
    {
        this.session_id = session_id;
    }

    public long getDate()
    {
        return date;
    }

    public void setDate(long date)
    {
        this.date = date;
    }

    public int getTtl() 
    {
        return ttl;
    }

    public void setTtl(int ttl) 
    {
        this.ttl = ttl;
    }
    
    public String getUsername()
    {
        return usr;
    }
    
    public void setUser(String usr)
    {
        this.usr = usr;
    }

    public boolean isExpired()
    {
        return (System.currentTimeMillis()/1000) >= getDate()+getTtl();
    }
}
