package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.exceptions.ParseException;
import fadulousbms.model.*;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Created by ghost on 2017/01/21.
 */
public class QuoteManager extends ApplicationObjectManager
{
    private HashMap<String, Quote> quotes;
    private ApplicationObject[] genders=null, domains=null;
    private Gson gson;
    private static QuoteManager quote_manager = new QuoteManager();
    private long timestamp;
    public static final String ROOT_PATH = "cache/quotes/";
    public String filename = "";
    public int selected_quote_sibling_cursor = 0;
    public static final double VAT = 14.0;
    boolean added_all_quote_items = true;

    private QuoteManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static QuoteManager getInstance()
    {
        return quote_manager;
    }

    @Override
    public HashMap<String, Quote> getDataset()
    {
        return quotes;
    }

    @Override
    Callback getSynchronisationCallback()
    {
        return new Callback()
        {
            @Override
            public Object call(Object param)
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
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));
                            //Get Timestamp
                            String quotes_timestamp_json = RemoteComms.get("/timestamp/quotes_timestamp", headers);
                            Counters quotes_timestamp = gson.fromJson(quotes_timestamp_json, Counters.class);
                            if(quotes_timestamp!=null)
                            {
                                timestamp = quotes_timestamp.getCount();
                                filename = "quotes_"+timestamp+".dat";
                                IO.log(QuoteManager.getInstance().getClass().getName(), IO.TAG_INFO, "Server Timestamp: "+quotes_timestamp.getCount());
                            } else {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if(!isSerialized(ROOT_PATH+filename))
                            {
                                //Load Quotes
                                String quotes_json = RemoteComms.get("/quotes", headers);
                                QuoteServerObject quoteServerObject = (QuoteServerObject) QuoteManager.getInstance().parseJSONobject(quotes_json, new QuoteServerObject());
                                if(quoteServerObject!=null)
                                {
                                    if(quoteServerObject.get_embedded()!=null)
                                    {
                                        Quote[] quotes_arr = quoteServerObject.get_embedded().getQuotes();
                                        quotes = new HashMap<>();
                                        for (Quote quote : quotes_arr)
                                            quotes.put(quote.get_id(), quote);
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Quotes in database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "QuoteServerObject (containing Quote objects & other metadata) is null");

                                if(EmployeeManager.getInstance().getDataset()!=null)
                                {
                                    Employee[] employees = new Employee[EmployeeManager.getInstance().getDataset().values().toArray().length];
                                    EmployeeManager.getInstance().getDataset().values().toArray(employees);

                                    if (quotes != null)
                                    {
                                        if (!quotes.isEmpty())
                                        {
                                            try
                                            {
                                                for (Quote quote : quotes.values())
                                                {
                                                    //Load Quote Resources
                                                    String quote_item_ids_json = RemoteComms.get("/quote/resources/" + quote.get_id(), headers);
                                                    if (quote_item_ids_json != null)
                                                    {
                                                        if (!quote_item_ids_json.equals("[]"))
                                                        {
                                                            QuoteResourceServerObject quoteResourceServerObject = (QuoteResourceServerObject) QuoteManager.getInstance().parseJSONobject(quote_item_ids_json, new QuoteResourceServerObject());
                                                            if (quoteResourceServerObject != null)
                                                            {
                                                                if (quoteResourceServerObject.get_embedded() != null)
                                                                {
                                                                    QuoteItem[] quote_resources_arr = quoteResourceServerObject
                                                                            .get_embedded()
                                                                            .getQuote_resources();
                                                                    quote.setResources(quote_resources_arr);
                                                                    IO.log(getClass().getName(), IO.TAG_INFO,
                                                                           String.format("set resources for quote '%s'.",quote.get_id()));
                                                                }
                                                                else IO.log(getClass().getName(),
                                                                            IO.TAG_ERROR,
                                                                            "could not find any Resources for Quote #" + quote.get_id());
                                                            }
                                                            else IO.log(getClass().getName(),
                                                                        IO.TAG_ERROR,
                                                                        "QuoteResourceServerObject (containing QuoteItem objects & other metadata) is null");
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_WARN,
                                                                    String.format("quote '%s does not have any resources.",quote.get_id()));
                                                    }
                                                    else IO.log(getClass().getName(), IO.TAG_WARN,
                                                                String.format("quote '%s does not have any resources.", quote.get_id()));
                                                }

                                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of quotes.");
                                                serialize(ROOT_PATH + filename, quotes);
                                            }
                                            catch (ConcurrentModificationException e)
                                            {
                                                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                            }
                                        }
                                        else
                                            IO.log(getClass().getName(),IO.TAG_WARN, "no quotes found in database.");
                                    }
                                    else
                                        IO.log(getClass().getName(), IO.TAG_WARN, "could not find any Quotes in database.");
                                }else
                                    IO.log(getClass().getName(), IO.TAG_WARN, "could not find any Employees in database.");
                            } else {
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object ["+ROOT_PATH+filename+"] on local disk is already up-to-date.");
                                quotes = (HashMap<String, Quote>) deserialize(ROOT_PATH+filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Invalid Session", "No valid active sessions were found.", IO.TAG_ERROR);
                } catch (MalformedURLException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (ClassNotFoundException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
                return null;
            }
        };
    }

    public HashMap<String, Quote> getLatestRevisions()
    {
        if(QuoteManager.getInstance().getDataset()==null)
            return null;
        if(QuoteManager.getInstance().getDataset().values()==null)
            return null;
        if(QuoteManager.getInstance().getDataset().isEmpty())
            return null;

        //collect map of latest quote revisions
        HashMap<String, Quote> latest_rev_quotes = new HashMap<>();
        for(Quote quote: QuoteManager.getInstance().getDataset().values())
            if(quote.getParent_id()==null)
                latest_rev_quotes.put(quote.get_id(), quote.getLatestRevisionFromChildren());

        return latest_rev_quotes;
    }

    public static double computeQuoteTotal(List<QuoteItem> quoteItems)
    {
        //compute total
        double total=0;
        if(quoteItems!=null)
            for (QuoteItem item : quoteItems)
                total += item.getTotal();
        else IO.log(QuoteManager.class.getName(), IO.TAG_WARN, "selected quote has no materials.");
        return total;
    }

    public void createQuote(Quote quote, ObservableList<QuoteItem> quoteItems, Callback callback) throws IOException
    {
        if(quote==null)
        {
            IO.logAndAlert("Error", "Quote to be created is invalid.\nPlease fix the issue and try again.", IO.TAG_ERROR);
            return;
        }

        //Create Quote object on database
        QuoteManager.getInstance().putObject(quote, new Callback()
        {
            @Override
            public Object call(Object new_quote_id)
            {
                if(new_quote_id!=null)
                {
                    // Try to add Quote Resources to Quote on database
                    if(quoteItems!=null)
                    {
                        for (QuoteItem quoteItem : quoteItems)
                        {
                            quoteItem.setQuote_id((String) new_quote_id);

                            try
                            {
                                //create QuoteItem
                                QuoteManager.getInstance().putObject(quoteItem, (Object item_id) ->
                                {
                                    if(item_id!=null)
                                        IO.log(getClass().getName(), IO.TAG_INFO, "created new quote item ["+ item_id + "]");
                                    else
                                    {
                                        IO.logAndAlert("Warning", "Could not add item #"+quoteItem.getItem_number()+" to quote ["+quote.get_id()+"]", IO.TAG_WARN);
                                        added_all_quote_items = false;
                                    }
                                    return null;
                                });
                            } catch (IOException e)
                            {
                                IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                e.printStackTrace();
                            }
                        }
                    } else IO.log(getClass().getName(), IO.TAG_WARN, "Quote["+new_quote_id+"] has no items/resources.");

                    //alert user about the outcome
                    if(added_all_quote_items)
                    {
                        //set selected quote
                        QuoteManager.getInstance().forceSynchronise();

                        if(QuoteManager.getInstance().getDataset()!=null && new_quote_id!=null)
                            QuoteManager.getInstance().setSelected(QuoteManager.getInstance().getDataset().get(new_quote_id));

                        IO.logAndAlert("Quote Creation Success", "Successfully created a new Quote.", IO.TAG_INFO);

                        if(callback!=null)
                            if(new_quote_id!=null)
                                callback.call(new_quote_id);
                    } else IO.logAndAlert("New Quote Creation Failure", "Could not add all items and/or services to quote.", IO.TAG_ERROR);//TODO: revert Quote creation
                }
                return null;
            }
        });
    }

    public void updateQuote(Quote quote, ObservableList<QuoteItem> quoteItems, boolean check_status, Callback callback)
    {
        if(quote==null)
        {
            IO.logAndAlert("Error: Invalid Quote", "Quote is null.", IO.TAG_ERROR);
            return;
        }
        if((quoteItems!=null?quoteItems.size()<=0:true))
        {
            IO.logAndAlert("Invalid Quote", "Quote items and services list is empty", IO.TAG_ERROR);
            return;
        }
        if(check_status && quote.getStatus()==Quote.STATUS_FINALISED)
        {
            IO.logAndAlert("Error", "Selected quote has already been approved. \nCreate a new Revision if you would like to make changes to this quote.", IO.TAG_ERROR);
            return;
        }

        //try to convert QuoteItems list to array of QuoteItems
        QuoteItem[] items =null;
        if(quoteItems!=null)
        {
            items = new QuoteItem[quoteItems.size()];
            quoteItems.toArray(items);
        }

        updateQuote(quote, items, check_status, callback);
    }

    public void updateQuote(Quote quote, QuoteItem[] quoteItems, boolean check_status, Callback callback)
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
        if((quoteItems!=null?quoteItems.length<=0:true))
        {
            IO.logAndAlert("Invalid Quote", "Quote items and services list is empty", IO.TAG_ERROR);
            return;
        }
        if(check_status && quote.getStatus()==Quote.STATUS_FINALISED)
        {
            IO.logAndAlert("Error", "Selected quote has already been approved. \nCreate a new Revision if you would like to make changes to this quote.", IO.TAG_ERROR);
            return;
        }

        if(quote!=null)
        {
            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
                //update Quote on database
                HttpURLConnection connection = RemoteComms.post(quote.apiEndpoint(), quote.getJSONString(), headers);
                if (connection != null)
                {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        String response = IO.readStream(connection.getInputStream());
                        IO.log(QuoteManager.class.getName(), IO.TAG_INFO, "updated quote[" + quote.get_id() + "]. Updating quote resources & services.");

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

                        //update quote items
                        boolean upd_all_items = updateQuoteItems(quote.get_id(), quoteItems, headers);
                        if (upd_all_items)
                        {
                            IO.logAndAlert("Quote Successfully Updated", "Successfully updated quote[#" + quote.getObject_number() + "].", IO.TAG_INFO);

                            forceSynchronise();
                            //execute callback w/ args
                            if(callback!=null)
                                callback.call(upd_all_items);
                            return;
                        } else IO.logAndAlert("Quote Update Failure", "Could not update all Quote Materials for Quote[#"+quote.getObject_number()+"].", IO.TAG_ERROR);
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
            } catch (ParseException e)
            {
                IO.logAndAlert(QuoteManager.class.getSimpleName(), e.getMessage(), IO.TAG_ERROR);
                e.printStackTrace();
            } catch (IOException e)
            {
                IO.logAndAlert(QuoteManager.class.getSimpleName(), e.getMessage(), IO.TAG_ERROR);
                e.printStackTrace();
            }
        } else IO.logAndAlert("Quote Update Error","Selected Quote is invalid.", IO.TAG_ERROR);
        //execute callback w/o args
        if(callback!=null)
            callback.call(null);
    }

    public boolean updateQuoteItems(String quote_id, ApplicationObject[] quoteItems, ArrayList headers) throws IOException, ParseException
    {
        if(quote_id==null)
        {
            IO.logAndAlert(QuoteManager.class.getSimpleName(), "Invalid quote ID", IO.TAG_ERROR);
            return false;
        }

        if(quote_id.isEmpty())
        {
            IO.logAndAlert(QuoteManager.class.getSimpleName(), "Invalid quote ID", IO.TAG_ERROR);
            return false;
        }

        if(quoteItems==null)
        {
            IO.logAndAlert(QuoteManager.class.getSimpleName(), "Invalid quote items/services.", IO.TAG_ERROR);
            return false;
        }

        if(headers == null)
        {
            IO.logAndAlert(QuoteManager.class.getSimpleName(), "Invalid headers.", IO.TAG_ERROR);
            return false;
        }

        if(headers.isEmpty())
        {
            IO.logAndAlert(QuoteManager.class.getSimpleName(), "Invalid headers.", IO.TAG_ERROR);
            return false;
        }

        boolean all_successful = true;
        /* Update/Create QuoteItems on database */
        for (ApplicationObject obj : quoteItems)
        {
            if (obj != null)
            {
                obj.parse("quote_id", quote_id);

                if(obj instanceof QuoteItem)
                {
                    QuoteItem quoteItem = (QuoteItem) obj;
                    /*
                        if QuoteItem has an ID then it's been already
                        added to the database - then update it, else create new record on db.
                     */
                    if (quoteItem.get_id() != null)
                    {
                        //set quote item's quote_id attribute
                        quoteItem.setQuote_id(quote_id);
                        //set quote_item's creator attribute
                        quoteItem.setCreator(SessionManager.getInstance().getActive().getUsr());
                        //set quote_item's resource_id attribute
                        quoteItem.setResource_id(quoteItem.getResource().get_id());

                        //update quote_item
                        all_successful = updateQuoteItem(quoteItem, headers);
                    } else //has not been created
                    {
                        //set quote item's quote_id attribute
                        quoteItem.setQuote_id(quote_id);
                        //set quote_item's creator attribute
                        quoteItem.setCreator(SessionManager.getInstance().getActive().getUsr());
                        //set quote_item's resource_id attribute
                        quoteItem.setResource_id(quoteItem.getResource().get_id());

                        //create quote_item
                        //all_successful = createQuoteItem(quoteItem, headers);
                        RemoteComms.patch(quoteItem.apiEndpoint(), quoteItem.getJSONString(), headers);
                        IO.log(getClass().getName(), IO.TAG_ERROR, "Don't use the patch() method directly for updating quote items");
                        //TODO: update this code to update using managers
                    }
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid QuoteItem type: "+ obj);
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        }
        return all_successful;
    }

    public boolean updateQuoteItem(ApplicationObject quoteItem, ArrayList<AbstractMap.SimpleEntry<String,String>> headers) throws IOException
    {
        if(quoteItem!=null)
        {
            IO.log(getClass().getName(), IO.TAG_INFO, "attempting to update "+quoteItem.getClass().getSimpleName()+"["+quoteItem.get_id()+"] for quote[" + quoteItem.get("quote_id") + "].");
            HttpURLConnection connection = RemoteComms.post(quoteItem.apiEndpoint(), quoteItem.getJSONString(), headers);
            if (connection != null)
            {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully updated "+quoteItem.getClass().getSimpleName()+"[" + quoteItem.get_id() + "] for quote[" + quoteItem.get("quote_id") + "].");
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
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid[null] quote_item.");
        return false;
    }

    public void requestQuoteApproval(Quote quote, Callback callback) throws IOException
    {
        if(quote==null)
        {
            IO.logAndAlert("Error", "Invalid Quote.", IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert("Error", "Could not find any employees in the system.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error: Invalid Session", "Could not find any valid sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error: Session Expired", "The active session has expired.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActiveEmployee()==null)
        {
            IO.logAndAlert("Error: Invalid Employee Session", "Could not find any active employee sessions.", IO.TAG_ERROR);
            return;
        }
        String path = PDF.createQuotePdf(quote);
        String base64_quote = null;
        if(path!=null)
        {
            File f = new File(path);
            if (f != null)
            {
                if (f.exists())
                {
                    FileInputStream in = new FileInputStream(f);
                    byte[] buffer =new byte[(int) f.length()];
                    in.read(buffer, 0, buffer.length);
                    in.close();
                    base64_quote = Base64.getEncoder().encodeToString(buffer);
                } else
                {
                    IO.logAndAlert(QuoteManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                }
            } else
            {
                IO.log(QuoteManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
            }
        } else IO.log(QuoteManager.class.getName(), "Could not get valid path for created Quote pdf.", IO.TAG_ERROR);
        final String finalBase64_quote = base64_quote;
        //upload Quote PDF to server
        //uploadQuotePDF(quote);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Request Quote ["+quote.get_id()+"] Approval");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        //gather list of Employees with enough clearance to approve quotes
        /*ArrayList<Employee> lst_auth_employees = new ArrayList<>();
        for(Employee employee: EmployeeManager.getInstance().getEmployees().values())
            if(employee.getAccessLevel()>=Employee.ACCESS_LEVEL_SUPER)
                lst_auth_employees.add(employee);

        if(lst_auth_employees==null)
        {
            IO.logAndAlert("Error", "Could not find any employee with the required access rights to approve documents.", IO.TAG_ERROR);
            return;
        }

        /*final ComboBox<Employee> cbx_destination = new ComboBox(FXCollections.observableArrayList(lst_auth_employees));
        cbx_destination.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>()
        {
            @Override
            public ListCell<Employee> call(ListView<Employee> param)
            {
                return new ListCell<Employee>()
                {
                    @Override
                    protected void updateItem(Employee employee, boolean empty)
                    {
                        if(employee!=null && !empty)
                        {
                            super.updateItem(employee, empty);
                            setText(employee.toString() + " <" + employee.getEmail() + ">");
                        }
                    }
                };
            }
        });
        cbx_destination.setMinWidth(200);
        cbx_destination.setMaxWidth(Double.MAX_VALUE);
        cbx_destination.setPromptText("Pick a recipient");
        HBox destination = CustomTableViewControls.getLabelledNode("To: ", 200, cbx_destination);*/

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText("QUOTE ["+quote.get_id()+"] APPROVAL REQUEST");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        //set default message
        Employee sender = SessionManager.getInstance().getActiveEmployee();
        String title = sender.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";;
        String def_msg = "Good day,\n\nCould you please assist me" +
                " by approving this quote to be issued to "  + quote.getClient().getClient_name() + ".\nThank you.\n\nBest Regards,\n"
                + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
        txt_message.setText(def_msg);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            //TODO: check this
            //if(!Validators.isValidNode(cbx_destination, cbx_destination.getValue()==null?"":cbx_destination.getValue().getEmail(), 1, ".+"))
            //    return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            //ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //params.add(new AbstractMap.SimpleEntry<>("message", msg));

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));//multipart/form-data
                headers.add(new AbstractMap.SimpleEntry<>("quote_id", quote.get_id()));
                //headers.add(new AbstractMap.SimpleEntry<>("to_email", cbx_destination.getValue().getEmail()));
                headers.add(new AbstractMap.SimpleEntry<>("message", msg));
                headers.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));

                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().getName()));
                else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                //String data = "{\"file\":\""+finalBase64_quote+"\"}";
                Metafile metafile = new Metafile("quote_"+quote.get_id()+".pdf","application/pdf");
                metafile.setCreator(SessionManager.getInstance().getActive().getUsr());
                metafile.setFile(finalBase64_quote);
                HttpURLConnection connection = RemoteComms.post("/quote/approval_request", metafile.getJSONString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully requested Quote approval!", IO.TAG_INFO);
                        //execute callback w/ args
                        if(callback!=null)
                            callback.call(IO.readStream(connection.getInputStream()));
                        return;
                    } else IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    connection.disconnect();
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "Could not get a valid response from the server.");
            } catch (IOException e)
            {
                IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                e.printStackTrace();
            }
            //execute callback w/o args
            if(callback!=null)
                callback.call(null);
        });

        /*cbx_destination.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue==null)
            {
                IO.log(getClass().getName(), "invalid destination address.", IO.TAG_ERROR);
                return;
            }
            Employee sender = SessionManager.getInstance().getActiveEmployee();
            String title = null;
            if(newValue.getGender()!=null)
                title = newValue.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";
            String msg = "Good day " + title + " " + newValue.getLastname() + ",\n\nCould you please assist me" +
                            " by approving this quote to be issued to "  + quote.getClient().getClient_name() + ".\nThank you.\n\nBest Regards,\n"
                            + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
            txt_message.setText(msg);
        });*/

        //Add form controls vertically on the stage
        //vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        //vbox.getChildren().add(hbox_quote_id);
        vbox.getChildren().add(message);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    class QuoteServerObject extends ServerObject
    {
        private QuoteServerObject.Embedded _embedded;

        QuoteServerObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(QuoteServerObject.Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private Quote[] quotes;

            public Quote[] getQuotes()
            {
                return quotes;
            }

            public void setQuotes(Quote[] quotes)
            {
                this.quotes = quotes;
            }
        }
    }

    class QuoteResourceServerObject extends ServerObject
    {
        private Embedded _embedded;

        Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private QuoteItem[] quote_resources;

            public QuoteItem[] getQuote_resources()
            {
                return quote_resources;
            }

            public void setQuote_resources(QuoteItem[] quote_resources)
            {
                this.quote_resources = quote_resources;
            }
        }
    }
}
