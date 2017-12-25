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
public class Bonus 
{
    private String name;
    private String description;
    private double value;
    
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

    public double getValue() 
    {
        return value;
    }

    public void setValue(double value) 
    {
        this.value = value;
    }
}
