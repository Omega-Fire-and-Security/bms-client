package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Counters;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.model.Invoice;
import fadulousbms.model.Job;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by ghost on 2017/01/27.
 */
public class AccountingManager extends BusinessObjectManager
{
    private Invoice[] invoices= null;
    private static AccountingManager accounting_manager = new AccountingManager();
    private ScreenManager screenManager = null;
    private Invoice selected_invoice;
    private Gson gson;
    public static final String ROOT_PATH = "cache/invoices/";
    public String filename = "";
    private long timestamp;
    private static final String TAG = "InvoiceManager";

    private AccountingManager()
    {
    }

    public static AccountingManager getInstance()
    {
        return accounting_manager;
    }

    @Override
    public void initialize()
    {
        /*loadDataFromServer();*/
    }

    /*public void loadDataFromServer()
    {
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if(smgr.getActive()!=null)
            {
                if(!smgr.getActive().isExpired())
                {
                    gson  = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/invoices_timestamp", headers);
                    Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if(cntr_timestamp!=null)
                    {
                        timestamp = cntr_timestamp.getCount();
                        filename = "invoices_"+timestamp+".dat";
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+timestamp);
                    }else {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if(!isSerialized(ROOT_PATH+filename))
                    {
                        String invoices_json = RemoteComms.sendGetRequest("/api/invoices", headers);
                        invoices = gson.fromJson(invoices_json, Invoice[].class);

                        //set Invoice job object
                        JobManager.getInstance().loadDataFromServer();
                        for(Invoice invoice: invoices)
                            for(Job job : JobManager.getInstance().getJobs())
                                if(job.get_id().equals(invoice.getJob_id()))
                                    invoice.setJob(job);

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of invoices.");
                        this.serialize(ROOT_PATH+filename, invoices);
                    }else{
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                        invoices = (Invoice[]) this.deserialize(ROOT_PATH+filename);
                    }
                }else{
                    JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        } catch (MalformedURLException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        } catch (ClassNotFoundException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        } catch (IOException ex)
        {
            IO.log(TAG, IO.TAG_ERROR, ex.getMessage());
        }
    }

    public Invoice[] getInvoices()
    {
        return invoices;
    }

    public Invoice getSelected()
    {
        return selected_invoice;
    }

    public void setSelected(Invoice selected_invoice)
    {
        this.selected_invoice = selected_invoice;
    }

    public void generateInvoice(Job job) throws IOException
    {
        if(job.getQuote()==null)
        {
            IO.logAndAlert(getClass().getName(), "Job Quote object is not set.", IO.TAG_ERROR);
            return;
        }
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                gson  = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                Invoice invoice = new Invoice();
                invoice.setCreator(smgr.getActiveEmployee().getUsr());
                invoice.setJob_id(job.get_id());
                invoice.setAccount("Cash");
                invoice.setReceivable(job.getQuote().getTotal());

                HttpURLConnection response = RemoteComms.postData("/api/invoice/add", invoice.asUTFEncodedString(), headers);
                if(response!=null)
                {
                    if(response.getResponseCode()==HttpURLConnection.HTTP_OK)
                        IO.logAndAlert("Success", IO.readStream(response.getInputStream()), IO.TAG_INFO);
                    else IO.logAndAlert("Error", IO.readStream(response.getErrorStream()), IO.TAG_ERROR);
                }else IO.logAndAlert("Error", "Response object is null.", IO.TAG_ERROR);
            }else{
                JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
        }
    }*/
}
