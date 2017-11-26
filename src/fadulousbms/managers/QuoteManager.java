package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ghost on 2017/01/21.
 */
public class QuoteManager extends BusinessObjectManager
{
    private HashMap<String, Quote> quotes;
    private BusinessObject[] genders=null, domains=null;
    private Gson gson;
    private static QuoteManager quote_manager = new QuoteManager();
    private Quote selected_quote;
    private long timestamp;
    public static final String ROOT_PATH = "cache/quotes/";
    public String filename = "";
    public static final double VAT = 14.0;

    private QuoteManager()
    {
    }

    public static QuoteManager getInstance()
    {
        return quote_manager;
    }

    @Override
    public void initialize()
    {
        //init genders
        BusinessObject male = new Gender();
        male.set_id("male");
        male.parse("gender", "male");

        BusinessObject female = new Gender();
        female.set_id("female");
        female.parse("gender", "female");

        genders = new BusinessObject[]{male, female};

        //init domains
        BusinessObject internal = new Domain();
        internal.set_id("true");
        internal.parse("domain", "internal");

        BusinessObject external = new Domain();
        external.set_id("false");
        external.parse("domain", "external");

        domains = new BusinessObject[]{internal, external};

        loadDataFromServer();

        /*organisations = new BusinessObject[clients.length + suppliers.length + 3];
        BusinessObject lbl_clients = new Client();
        lbl_clients.parse("client_name", "________________________Clients________________________");

        BusinessObject lbl_internal = new Client();
        lbl_internal.parse("client_name", "INTERNAL");
        lbl_internal.set_id("INTERNAL");

        BusinessObject lbl_suppliers = new Supplier();
        lbl_suppliers.parse("supplier_name", "________________________Suppliers________________________");

        //Prepare the list of BusinessObjects to be added to the combo boxes.
        organisations[0] = lbl_internal;
        organisations[1] = lbl_clients;
        int cursor = 1;
        for(int i=0;i<clients.length;i++)
            organisations[++cursor]=clients[i];
        organisations[++cursor] = lbl_suppliers;
        for(int i=0;i<suppliers.length;i++)
            organisations[++cursor]=suppliers[i];*/
    }

    public HashMap<String, Quote> getQuotes()
    {
        return quotes;
    }

    public void setSelectedQuote(Quote quote)
    {
        if(quote!=null)
        {
            this.selected_quote = quote;
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected quote to: " + selected_quote);
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "quote to be set as selected is null.");
    }

    public void setSelectedQuote(String quote_id)
    {
        if(quotes==null)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, "No quotes were found on the database.");
            return;
        }
        if(quotes.get(quote_id)!=null)
        {
            setSelectedQuote(quotes.get(quote_id));
        }
    }

    public Quote getSelectedQuote()
    {
        /*if(selected_quote>-1)
            return quotes[selected_quote];
        else return null;*/
        return selected_quote;
    }

    public void nullifySelected()
    {
        this.selected_quote=null;
    }

    public void loadDataFromServer()
    {
        try
        {
            if(quotes==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "quotes object has already been set.");
        }catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
        }catch (ClassNotFoundException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    public void reloadDataFromServer() throws ClassNotFoundException, IOException
    {
        //quotes = null;
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {

                gson  = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));
                //Get Timestamp
                String quotes_timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/quotes_timestamp", headers);
                Counters quotes_timestamp = gson.fromJson(quotes_timestamp_json, Counters.class);
                if(quotes_timestamp!=null)
                {
                    timestamp = quotes_timestamp.getCount();
                    filename = "quotes_"+timestamp+".dat";
                    IO.log(QuoteManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+quotes_timestamp.getCount());
                } else {
                    IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                    return;
                }

                if(!isSerialized(ROOT_PATH+filename))
                {
                    //Load Quotes
                    String quotes_json = RemoteComms.sendGetRequest("/api/quotes", headers);
                    Quote[] quotes_arr = gson.fromJson(quotes_json, Quote[].class);
                    quotes = new HashMap<>();

                    Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().values().toArray().length];
                    EmployeeManager.getInstance().getEmployees().values().toArray(employees);

                    if(quotes!=null)
                    {
                        if(quotes_arr.length>0)
                        {
                            for (Quote quote : quotes_arr)
                            {
                                //Load Quote Resources
                                String quote_item_ids_json = RemoteComms.sendGetRequest("/api/quote/resources/" + quote.get_id(), headers);
                                if (quote_item_ids_json != null)
                                {
                                    if (!quote_item_ids_json.equals("[]"))
                                    {
                                        QuoteItem[] quote_items = gson.fromJson(quote_item_ids_json, QuoteItem[].class);
                                        quote.setResources(quote_items);
                                    } else
                                        IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any resources.", quote.get_id()));
                                } else
                                    IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any resources.", quote.get_id()));

                                //Load Quote Representatives
                                String quote_rep_ids_json = RemoteComms.sendGetRequest("/api/quote/reps/" + quote.get_id(), headers);
                                if (quote_rep_ids_json != null)
                                {
                                    if (!quote_rep_ids_json.equals("[]"))
                                    {
                                        QuoteRep[] quote_reps = gson.fromJson(quote_rep_ids_json, QuoteRep[].class);
                                        quote.setRepresentatives(quote_reps);
                                        IO.log(getClass().getName(), IO.TAG_INFO, String.format("set reps for quote '%s'.", quote.get_id()));
                                    } else IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any representatives.", quote.get_id()));
                                } else IO.log(getClass().getName(), IO.TAG_WARN, String.format("quote '%s does not have any representatives.", quote.get_id()));

                                //Update selected quote data
                                if(selected_quote!=null)
                                {
                                    if (quote.get_id().equals(selected_quote.get_id()))
                                        selected_quote = quote;
                                }
                                quotes.put(quote.get_id(), quote);
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of quotes.");
                            this.serialize(ROOT_PATH+filename, quotes);
                        }else{
                            IO.log(getClass().getName(), IO.TAG_ERROR, "no quotes found in database.");
                            //IO.showMessage("No quotes", "no quotes found in database.", IO.TAG_ERROR);
                        }
                    }else{
                        IO.log(getClass().getName(), IO.TAG_ERROR, "quotes object is null.");
                        //IO.showMessage("No quotes", "no quotes found in database.", IO.TAG_ERROR);
                    }
                } else{
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                    quotes = (HashMap<String, Quote>) this.deserialize(ROOT_PATH+filename);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static double computeQuoteTotal(List<QuoteItem> quoteItems)
    {
        //compute total
        double total=0;
        for(QuoteItem item:  quoteItems)
            total += item.getTotal();
        return total;
    }

    public void generatePDF() throws IOException
    {
        if(selected_quote!=null)
            PDF.createQuotePdf(selected_quote);
        else IO.logAndAlert("Error", "Please choose a valid quote.", IO.TAG_ERROR);
    }

    public void updateQuote(Quote quote, ObservableList<QuoteItem> quoteItems, ObservableList<Employee> quoteReps)
    {
        if(quoteItems==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote items list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteItems.size()<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no items", IO.TAG_ERROR);
            return;
        }

        if(quoteReps==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote representatives list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteReps.size()<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no representatives", IO.TAG_ERROR);
            return;
        }

        QuoteItem[] items = new QuoteItem[quoteItems.size()];
        quoteItems.toArray(items);

        Employee[] employees = new Employee[quoteReps.size()];
        quoteReps.toArray(employees);

        updateQuote(quote, items, employees);
    }

    public void updateQuote(Quote quote, QuoteItem[] quoteItems, Employee[] quoteReps)
    {
        if (SessionManager.getInstance().getActive() == null)
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }
        if (SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            return;
        }

        if(quoteItems==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote items list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteItems.length<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no items", IO.TAG_ERROR);
            return;
        }

        if(quoteReps==null)
        {
            IO.logAndAlert("Invalid Quote", "Quote representatives list is null.", IO.TAG_ERROR);
            return;
        }
        if(quoteReps.length<=0)
        {
            IO.logAndAlert("Invalid Quote", "Quote has no representatives", IO.TAG_ERROR);
            return;
        }

        //Quote selected = getSelectedQuote();
        if(quote!=null)
        {
            //prepare quote parameters
            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                //update quote on database
                HttpURLConnection connection = RemoteComms.postData("/api/quote/update/"+quote.get_id(), quote.asUTFEncodedString(), headers);
                if (connection != null)
                {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        String response = IO.readStream(connection.getInputStream());
                        IO.log(getClass().getName(), IO.TAG_INFO, "updated quote[" + quote.get_id() + "]. Adding representatives and resources to quote.");

                        if (response == null)
                        {
                            IO.logAndAlert("Quote Update", "Invalid server response.", IO.TAG_ERROR);
                            return;
                        }
                        if (response.isEmpty())
                        {
                            IO.logAndAlert("Quote Update", "Invalid server response: " + response, IO.TAG_ERROR);
                            return;
                        }

                        boolean updated_all_quote_items = updateQuoteItems(quote.get_id(), quoteItems, headers);
                        boolean updated_all_quote_reps = updateQuoteReps(quote, quoteReps, headers);
                        //boolean updated_all_quote_reps = true;

                        if (updated_all_quote_items && updated_all_quote_reps)
                        {
                            IO.logAndAlert("Quote Manager","successfully updated quote[" + quote.get_id() + "].", IO.TAG_INFO);
                            loadDataFromServer();
                        } else {
                            if(!updated_all_quote_items)
                                IO.logAndAlert("Quote Update Failure", "Could not update all Quote Items for Quote["+quote.get_id()+"].", IO.TAG_INFO);
                            if(!updated_all_quote_reps)
                                IO.logAndAlert("Quote Update Failure", "Could not update all Quote Representatives for Quote["+quote.get_id()+"].", IO.TAG_INFO);
                        }
                    } else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    }
                    //Close connection
                    if (connection != null)
                        connection.disconnect();
                } else IO.logAndAlert("Quote Update Failure", "Could not connect to server.", IO.TAG_ERROR);
            } catch (IOException e)
            {
                IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            }
        }else IO.logAndAlert("Update Quote","Selected Quote is invalid.", IO.TAG_ERROR);
    }

    public boolean updateQuoteItems(String quote_id, QuoteItem[] quoteItems, ArrayList headers) throws IOException
    {
        if(quote_id==null || quoteItems==null || headers == null)
            return false;

        boolean all_successful = true;
        /* Update/Create QuoteItems on database */
        for (QuoteItem quoteItem : quoteItems)
        {
            if (quoteItem != null)
            {
                /*
                    if QuoteItem has an ID then it's been already
                    added to the database - then update it, else create new record on db.
                 */
                if (quoteItem.get_id() != null)
                {
                    //update quote_item
                    all_successful = updateQuoteItem(quoteItem, headers);
                } else
                {
                    //new quote_item
                    //prepare parameters for quote_item.
                    /*ArrayList params = new ArrayList<>();
                    params.add(new AbstractMap.SimpleEntry<>("item_number", quoteItem.getItem_number()));
                    params.add(new AbstractMap.SimpleEntry<>("resource_id", quoteItem.getResource().get_id()));
                    params.add(new AbstractMap.SimpleEntry<>("quote_id", quote_id));
                    params.add(new AbstractMap.SimpleEntry<>("markup", quoteItem.getMarkup()));
                    params.add(new AbstractMap.SimpleEntry<>("labour", quoteItem.getLabour()));
                    params.add(new AbstractMap.SimpleEntry<>("quantity", quoteItem.getQuantity()));
                    params.add(new AbstractMap.SimpleEntry<>("additional_costs", quoteItem.getAdditional_costs()));*/


                    quoteItem.setQuote_id(quote_id);
                    quoteItem.setResource_id(quoteItem.getResource().get_id());

                    all_successful = createQuoteItem(quote_id, quoteItem, headers);
                }
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        }
        return all_successful;
    }

    public boolean createQuoteItem(String quote_id, QuoteItem quoteItem,ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new quote_item for quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/resource/add", quoteItem.asUTFEncodedString(), headers);

        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_item for quote["+quote_id+"].");
                //loadDataFromServer();//refresh data set
                //Close connection
                if (connection != null)
                    connection.disconnect();
                return true;
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
        }else IO.logAndAlert("New Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }

    public boolean createQuoteItem(String quote_id, ArrayList params,ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new quote_item for quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/resource/add", params, headers);

        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_item for quote["+quote_id+"].");
                //loadDataFromServer();//refresh data set
                //Close connection
                if (connection != null)
                    connection.disconnect();
                return true;
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
        }else IO.logAndAlert("New Quote Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }

    public boolean updateQuoteItem(QuoteItem quoteItem, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        if(quoteItem!=null)
        {
            IO.log(getClass().getName(), IO.TAG_INFO, "attempting to update quote_item["+quoteItem.get_id()+"] for quote[" + quoteItem.getQuote_id() + "].");
            HttpURLConnection connection = RemoteComms.postData("/api/quote/resource/update/" + quoteItem.get_id(), quoteItem.asUTFEncodedString(), headers);
            if (connection != null)
            {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully updated quote_item[" + quoteItem.get_id() + "] for quote[" + quoteItem.getQuote_id() + "].");
                    //Close connection
                    if (connection != null)
                        connection.disconnect();
                    return true;
                } else
                {
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.log(getClass().getName(),IO.TAG_ERROR,"Error " + String.valueOf(connection.getResponseCode()) + ":" + msg);
                }
            } else IO.logAndAlert("Quote Item Update Failure", "Could not connect to server.", IO.TAG_ERROR);
            //Close connection
            if (connection != null)
                connection.disconnect();
        }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        return false;
    }

    public boolean updateQuoteReps(Quote quote, Employee[] reps, ArrayList headers) throws IOException
    {
        if(quote==null || reps==null || headers == null)
            return false;

        boolean all_successful = true;
        /* Update/Create Quote representatives on database */
        for (Employee rep : reps)
        {
            if (rep != null)
            {
                if (rep.get_id() != null)
                {
                    //check if employee already in list of quote reps
                    boolean found=false;
                    if(quote.getRepresentatives()!=null)
                    {
                        for (Employee employee : quote.getRepresentatives())
                        {
                            if (employee.get_id().equals(rep.get_id()))
                            {
                                found = true;
                                break;
                            }
                        }
                    }
                    if(!found)
                    {
                        //new quote rep
                        //prepare parameters for quote_item.
                        ArrayList params = new ArrayList<>();
                        params.add(new AbstractMap.SimpleEntry<>("quote_id", quote.get_id()));
                        params.add(new AbstractMap.SimpleEntry<>("usr", rep.getUsr()));

                        all_successful = createQuoteRep(quote.get_id(), params, headers);
                    }else IO.log(getClass().getName(), IO.TAG_INFO, "quote representatives are up to date.");
                }
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        }
        return all_successful;
    }

    public boolean createQuoteRep(String quote_id, ArrayList params,ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to create new quote_rep for quote[" + quote_id + "].");
        HttpURLConnection connection = RemoteComms.postData("/api/quote/rep/add/"+quote_id, params, headers);
        if (connection != null)
        {
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new quote_rep for quote["+quote_id+"].");
                //loadDataFromServer();//refresh data set
                //Close connection
                if (connection != null)
                    connection.disconnect();
                return true;
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
        }else IO.logAndAlert("New Quote Representative Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        //Close connection
        if (connection != null)
            connection.disconnect();
        return false;
    }

    public void sendEmail(String job_id, Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Quote ["+job_id+"]");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_destination = new TextField();
        txt_destination.setMinWidth(200);
        txt_destination.setMaxWidth(Double.MAX_VALUE);
        txt_destination.setPromptText("Type in email address/es separated by commas");
        HBox destination = CustomTableViewControls.getLabelledNode("To: ", 200, txt_destination);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextField txt_job_id = new TextField();
        txt_job_id.setMinWidth(200);
        txt_job_id.setMaxWidth(Double.MAX_VALUE);
        txt_job_id.setPromptText("Type in a message");
        txt_job_id.setEditable(false);
        txt_job_id.setText(job_id);
        HBox hbox_job_id = CustomTableViewControls.getLabelledNode("Job ID: ", 200, txt_job_id);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_destination, txt_destination.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String str_destination = txt_destination.getText();
            String str_subject = txt_subject.getText();
            String str_message = txt_message.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("quote_id", job_id));
            params.add(new AbstractMap.SimpleEntry<>("to_email", str_destination));
            params.add(new AbstractMap.SimpleEntry<>("subject", str_subject));
            params.add(new AbstractMap.SimpleEntry<>("message", str_message));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive()
                            .getSessionId()));
                    params.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().toString()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/quote/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully sent email!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        vbox.getChildren().add(hbox_job_id);
        vbox.getChildren().add(message);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }
}
