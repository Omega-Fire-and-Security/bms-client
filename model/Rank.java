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
public class Rank 
{
    private String rank_id;
    private String rank_title;
    private double rank_salary_modifier;
    
    public String getRankId() 
    {
        return rank_id;
    }

    public void setRankId(String rank_id) 
    {
        this.rank_id = rank_id;
    }

    public String getRankTitle() 
    {
        return rank_title;
    }

    public void setRankTitle(String rank_title) 
    {
        this.rank_title = rank_title;
    }

    public double getRankSalaryModifier() 
    {
        return rank_salary_modifier;
    }

    public void setRankSalaryModifier(double rank_salary_modifier) 
    {
        this.rank_salary_modifier = rank_salary_modifier;
    }
}
