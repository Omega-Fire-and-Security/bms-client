package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.*;

/**
 * Created by ghost on 2017/01/13.
 */
public class RequisitionManager extends BusinessObjectManager
{
    private HashMap<String, Requisition> requisitions;
    private Requisition selected;
    private Gson gson;
    private static RequisitionManager requisition_manager = new RequisitionManager();
    public static final String TAG = "RequisitionManager";
    public static final String ROOT_PATH = "cache/requisitions/";
    public String filename = "";
    private long timestamp;
    public static String[] TYPES = {"Quote","Tender","Callout"};

    private RequisitionManager()
    {
    }

    public static RequisitionManager getInstance()
    {
        return requisition_manager;
    }

    public HashMap<String, Requisition> getRequisitions()
    {
        return requisitions;
    }

    public void setSelected(Requisition requisition)
    {
        if(requisition!=null)
            this.selected=requisition;
        else IO.log(getClass().getName()+">setSelected()", IO.TAG_WARN, "invalid requisition.");
    }

    public void setSelected(String requisition_id)
    {
        if(requisitions!=null && requisition_id!=null)
            this.selected=requisitions.get(requisition_id);
        else IO.log(getClass().getName(), IO.TAG_WARN, "collection of requisitions or requisition_id not set.");
    }

    public Requisition getSelected()
    {
        return this.selected;
    }

    @Override
    public void initialize()
    {
        try
        {
            reloadDataFromServer();
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

    public void loadDataFromServer()
    {
        try
        {
            if(requisitions==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "requisitions object has already been set.");
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

    public void createRequisition(Requisition requisition, Callback callback) throws IOException
    {
        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
        if(SessionManager.getInstance().getActive()!=null)
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
        else
        {
            IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            return;
        }

        //create new requisition on database
        //ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
        HttpURLConnection connection = RemoteComms.putJSON("/requisitions", requisition.toString(), headers);
        if(connection!=null)
        {
            if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
            {
                String response = IO.readStream(connection.getInputStream());

                if(response==null)
                {
                    IO.logAndAlert("New Requisition Creation Error", "Invalid server response.", IO.TAG_ERROR);
                    return;
                }
                if(response.isEmpty())
                {
                    IO.logAndAlert("New Requisition Creation Error", "Invalid server response.", IO.TAG_ERROR);
                    return;
                }

                //server will return message object in format "<requisition_id>"
                String new_requisition_id = response.replaceAll("\"","");//strip inverted commas around requisition_id
                new_requisition_id = new_requisition_id.replaceAll("\n","");//strip new line chars
                new_requisition_id = new_requisition_id.replaceAll(" ","");//strip whitespace chars

                //Close connection
                if(connection!=null)
                    connection.disconnect();

                try
                {
                    //update selected requisition
                    RequisitionManager.getInstance().reloadDataFromServer();
                    RequisitionManager.getInstance().setSelected(new_requisition_id);

                    IO.logAndAlert("New Requisition Creation Success", "Successfully created new Requisition.", IO.TAG_INFO);
                    if(callback!=null)
                        if(new_requisition_id!=null)
                            callback.call(new_requisition_id);
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
            } else
            {
                //Get error message
                String msg = IO.readStream(connection.getErrorStream());
                IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
            }
            if(connection!=null)
                connection.disconnect();
        } else IO.logAndAlert("New Requisition Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
    }
    
    public void reloadDataFromServer() throws ClassNotFoundException, IOException
    {
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                gson = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                //Get Timestamp
                String timestamp_json = RemoteComms.sendGetRequest("/timestamp/requisitions_timestamp", headers);
                Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                if (cntr_timestamp != null)
                {
                    timestamp = cntr_timestamp.getCount();
                    filename = "requisitions_" + timestamp + ".dat";
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                } else
                {
                    IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                    return;
                }

                if (!isSerialized(ROOT_PATH + filename))
                {
                    String requisitions_json = RemoteComms.sendGetRequest("/requisitions", headers);
                    RequisitionServerObject requisitionServerObject= gson.fromJson(requisitions_json, RequisitionServerObject.class);
                    if(requisitionServerObject!=null)
                    {
                        if(requisitionServerObject.get_embedded()!=null)
                        {
                            Requisition[] requisitions_arr = requisitionServerObject.get_embedded().getPurchase_orders();

                            if (requisitions_arr != null)
                            {
                                requisitions = new HashMap<>();
                                for (Requisition requisition : requisitions_arr)
                                    requisitions.put(requisition.get_id(), requisition);
                            }
                            else IO.log(getClass().getName(), IO.TAG_WARN, "no requisitions were found in database.");
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Requisitions in database.");
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "RequisitionServerObject (containing Requisition objects & other metadata) is null");
                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of purchase orders.");
                    this.serialize(ROOT_PATH + filename, requisitions);
                } else
                {
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                    requisitions = (HashMap<String, Requisition>) this.deserialize(ROOT_PATH + filename);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void emailRequisition(Requisition requisition, Callback callback)
    {
        if(requisition==null)
        {
            IO.logAndAlert("Error", "Invalid Requisition.", IO.TAG_ERROR);
            return;
        }

        //upload requisition PDF to server
        uploadRequisitionPDF(requisition);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Requisition ["+requisition.get_id()+"]");
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

        final TextField txt_requisition_id = new TextField();
        txt_requisition_id.setMinWidth(200);
        txt_requisition_id.setMaxWidth(Double.MAX_VALUE);
        txt_requisition_id.setPromptText("Type in a message");
        txt_requisition_id.setEditable(false);
        txt_requisition_id.setText(String.valueOf(requisition.get_id()));
        HBox hbox_requisition_id = CustomTableViewControls.getLabelledNode("Requisition ID: ", 200, txt_requisition_id);

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
            params.add(new AbstractMap.SimpleEntry<>("requisition_id", requisition.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("to_email", str_destination));
            params.add(new AbstractMap.SimpleEntry<>("subject", str_subject));
            params.add(new AbstractMap.SimpleEntry<>("message", str_message));
            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive()
                            .getSession_id()));
                    params.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().toString()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/requisitions/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully emailed requisition to ["+txt_destination.getText()+"]!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(RequisitionManager.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        vbox.getChildren().add(hbox_requisition_id);
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

    public void requestRequisitionApproval(Requisition requisition, Callback callback)
    {
        if(requisition==null)
        {
            IO.logAndAlert("Error", "Invalid Requisition.", IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getEmployees()==null)
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

        //upload requisition PDF to server
        uploadRequisitionPDF(requisition);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Requisition ["+requisition.get_id()+"]");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText("REQUISITION ["+requisition.get_id()+"] APPROVAL REQUEST");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        //set default message
        Employee sender = SessionManager.getInstance().getActiveEmployee();
        String title = sender.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";;
        String def_msg = "Good day,\n\nCould you please assist me" +
                " by approving this requisition of services to be rendered to "  + requisition.getClient().getClient_name() + ".\nThank you.\n\nBest Regards,\n"
                + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
        txt_message.setText(def_msg);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            //TODO: check this
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            String path = null;
            try
            {
                path = PDF.createRequisitionPDF(requisition);
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
            String base64_requisition = null;
            if(path!=null)
            {
                File f = new File(path);
                if (f != null)
                {
                    if (f.exists())
                    {
                        FileInputStream in = null;
                        try
                        {
                            in = new FileInputStream(f);
                            byte[] buffer =new byte[(int) f.length()];
                            in.read(buffer, 0, buffer.length);
                            in.close();
                            base64_requisition = Base64.getEncoder().encodeToString(buffer);
                        } catch (FileNotFoundException e)
                        {
                            IO.log(RequisitionManager.class.getName(), IO.TAG_ERROR, e.getMessage());
                        } catch (IOException e)
                        {
                            IO.log(RequisitionManager.class.getName(), IO.TAG_ERROR, e.getMessage());
                        }
                    } else
                    {
                        IO.logAndAlert(RequisitionManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                    }
                } else
                {
                    IO.log(RequisitionManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
                }
            } else IO.log(RequisitionManager.class.getName(), "Could not get valid path for created Requisition pdf.", IO.TAG_ERROR);
            final String finalBase64_requisition = base64_requisition;

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));//multipart/form-data
                headers.add(new AbstractMap.SimpleEntry<>("requisition_id", requisition.get_id()));
                //headers.add(new AbstractMap.SimpleEntry<>("to_email", cbx_destination.getValue().getEmail()));
                headers.add(new AbstractMap.SimpleEntry<>("message", msg));
                headers.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));

                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive().getSession_id()));
                    headers.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().toString()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                FileMetadata fileMetadata = new FileMetadata("requisition_"+requisition.get_id()+".pdf","application/pdf");
                fileMetadata.setCreator(SessionManager.getInstance().getActive().getUsr());
                fileMetadata.setFile(finalBase64_requisition);

                HttpURLConnection connection = RemoteComms.postJSON("/requisitions/request_approval", fileMetadata.toString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully requested Requisition #"+requisition.get_id()+" approval!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(IO.readStream(connection.getInputStream()));
                    } else
                    {
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });


        //Add form controls vertically on the stage
        vbox.getChildren().add(subject);
        //vbox.getChildren().add(hbox_job_id);
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
    }

    public static void uploadRequisitionPDF(Requisition requisition)
    {
        if(requisition==null)
        {
            IO.logAndAlert("Error", "Invalid purchase order object passed.", IO.TAG_ERROR);
            return;
        }
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    String path = null;//PDF.createRequisitionPdf(requisition);
                    if(path!=null)
                    {
                        File f = new File(path);
                        if (f != null)
                        {
                            if (f.exists())
                            {
                                FileInputStream in = new FileInputStream(f);
                                byte[] buffer = new byte[(int) f.length()];
                                in.read(buffer, 0, buffer.length);
                                in.close();

                                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance()
                                        .getActive().getSession_id()));
                                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                                RemoteComms.uploadFile("/requisitions/upload/" + requisition.get_id(), headers, buffer);
                                IO.log(RequisitionManager.class.getName(), IO.TAG_INFO, "\n uploaded Requisition[#" + requisition.get_id()
                                        + "], file size: [" + buffer.length + "] bytes.");
                            } else
                            {
                                IO.logAndAlert(RequisitionManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                            }
                        } else
                        {
                            IO.log(RequisitionManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
                        }
                    } else IO.log(RequisitionManager.class.getName(), "Could not get valid path for created purchase order pdf.", IO.TAG_ERROR);
                } catch (IOException e)
                {
                    IO.log(RequisitionManager.class.getName(), e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    class RequisitionServerObject extends ServerObject
    {
        private RequisitionServerObject.Embedded _embedded;

        RequisitionServerObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(RequisitionServerObject.Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private Requisition[] requisitions;

            public Requisition[] getPurchase_orders()
            {
                return requisitions;
            }

            public void setPurchase_orders(Requisition[] requisitions)
            {
                this.requisitions = requisitions;
            }
        }
    }
}
