package fadulousbms.model;

import fadulousbms.auxilary.IO;

import java.util.ArrayList;

/**
 * Created by ghost on 2017/09/28.
 */
public class Account
{
    private String account_name;
    private double debit;
    private double credit;
    private ArrayList<Transaction> transactions;

    public Account(String account_name, double debit, double credit)
    {
        this.account_name=account_name;
        this.debit=debit;
        this.credit=credit;
        this.transactions = new ArrayList<>();
    }

    public String getAccount_name()
    {
        return account_name;
    }

    public void setAccount_name(String account_name)
    {
        this.account_name = account_name;
    }

    public double getDebit()
    {
        return debit;
    }

    public void setDebit(double debit)
    {
        this.debit = debit;
    }

    public double getCredit()
    {
        return credit;
    }

    public void setCredit(double credit)
    {
        this.credit = credit;
    }

    public ArrayList<Transaction> getTransactions()
    {
        return transactions;
    }

    public void setTransactions(ArrayList<Transaction> transactions)
    {
        this.transactions = transactions;
    }

    public void addTransaction(Transaction transaction)
    {
        if(transaction!=null)
            this.transactions.add(transaction);
        else IO.log(getClass().getName(), IO.TAG_ERROR, "transaction is null.");
    }
}
