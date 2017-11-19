/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.model;

/**
 *
 * @author ghost
 */
public class Comment 
{
    private String comment;
    private long date;
    private Employee by;
    
    public String getComment() 
    {
        return comment;
    }

    public void setComment(String comment) 
    {
        this.comment = comment;
    }

    public long getDate() 
    {
        return date;
    }

    public void setDate(long date) 
    {
        this.date = date;
    }

    public Employee getBy()
    {
        return by;
    }

    public void setBy(Employee by)
    {
        this.by = by;
    }
}
