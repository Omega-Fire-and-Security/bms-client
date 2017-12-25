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
public class OccupationRequirement 
{
    private String name;
    private String description;
    private boolean acquired;

    public String getName() 
    {
        return name;
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public boolean isAcquired() 
    {
        return acquired;
    }

    public void setAcquired(boolean acquired) 
    {
        this.acquired = acquired;
    }
}
