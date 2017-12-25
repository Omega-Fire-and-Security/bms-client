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
public class Occupation 
{
    private String id;
    private String title;
    private String description;
    private OccupationRequirement requirements[];
    private Rank rank;
    private double base_salary;
    private Bonus[] assigned_bonuses;
    private String frequency;

    public String getId() 
    {
        return id;
    }

    public void setId(String id) 
    {
        this.id = id;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public OccupationRequirement[] getRequirements() 
    {
        return requirements;
    }

    public void setRequirements(OccupationRequirement[] requirements) 
    {
        this.requirements = requirements;
    }

    public Rank getRank() 
    {
        return rank;
    }

    public void setRank(Rank rank) 
    {
        this.rank = rank;
    }

    public double getBaseSalary() 
    {
        return base_salary;
    }

    public void setBaseSalary(double base_salary) 
    {
        this.base_salary = base_salary;
    }

    public Bonus[] getAssignedBonuses() 
    {
        return assigned_bonuses;
    }

    public void setAssignedBonuses(Bonus[] assigned_bonuses) 
    {
        this.assigned_bonuses = assigned_bonuses;
    }

    public String getFrequency() 
    {
        return frequency;
    }

    public void setFrequency(String frequency) 
    {
        this.frequency = frequency;
    }
}
