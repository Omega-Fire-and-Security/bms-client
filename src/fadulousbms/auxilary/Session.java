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
    private String usr;//username associated with session
    private String session_id;
    private long date;//creation date
    private int ttl;//time-to-live

    public Session(){}

    public Session(String usr, String session_id, int date, int ttl)
    {
        this.usr = usr;
        this.session_id=session_id;
        this.date=date;
        this.ttl=ttl;
    }

    public String getSession_id()
    {
        return session_id;
    }

    public void setSession_id(String session_id)
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

    public String getUsr()
    {
        return usr;
    }

    public void setUsr(String usr)
    {
        this.usr = usr;
    }

    public boolean isExpired()
    {
        return (System.currentTimeMillis()/1000) >= getDate()+getTtl();
    }

    @Override
    public String toString()
    {
        return "{\"session_id\":\""+session_id+"\", "+
                "\"usr\":\""+usr+"\","+
                "\"date\":\""+date+"\","+
                "\"ttl\":\""+ttl+"\"}";
    }
}
