package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
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
public class RequisitionManager extends ApplicationObjectManager
{
    private HashMap<String, Requisition> requisitions;
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

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static RequisitionManager getInstance()
    {
        return requisition_manager;
    }

    @Override
    public HashMap<String, Requisition> getDataset()
    {
        return requisitions;
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
                    if (smgr.getActive() != null)
                    {
                        if (!smgr.getActive().isExpired())
                        {
                            gson = new GsonBuilder().create();
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms.get("/timestamp/requisitions_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "requisitions_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                String requisitions_json = RemoteComms.get("/requisitions", headers);
                                RequisitionServerResponseObject requisitionServerObject = (RequisitionServerResponseObject) RequisitionManager.getInstance().parseJSONobject(requisitions_json, new RequisitionServerResponseObject());
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
                                        } else IO.log(getClass().getName(), IO.TAG_WARN, "no requisitions were found in database.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Requisitions in database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "RequisitionServerResponseObject (containing Requisition objects & other metadata) is null");
                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of purchase orders.");
                                serialize(ROOT_PATH + filename, requisitions);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                requisitions = (HashMap<String, Requisition>) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
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

    public void requestRequisitionApproval(Requisition requisition, Callback callback)
    {
        if(requisition==null)
        {
            IO.logAndAlert("Error", "Invalid Requisition.", IO.TAG_ERROR);
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
                    headers.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().getName()));
                else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                Metafile metafile = new Metafile("requisition_"+requisition.get_id()+".pdf","application/pdf");
                metafile.setCreator(SessionManager.getInstance().getActive().getUsr());
                metafile.setFile(finalBase64_requisition);

                HttpURLConnection connection = RemoteComms.post("/requisition/request_approval", metafile.getJSONString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully requested Requisition #"+requisition.get_id()+" approval!", IO.TAG_INFO);
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


        //Add form controls vertically on the stage
        vbox.getChildren().add(subject);
        //vbox.getChildren().add(hbox_job_id);
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

                                RemoteComms.uploadFile("/file/upload/" + requisition.get_id(), headers, buffer);
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

    class RequisitionServerResponseObject extends ServerResponseObject
    {
        private RequisitionServerResponseObject.Embedded _embedded;

        RequisitionServerResponseObject.Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(RequisitionServerResponseObject.Embedded _embedded)
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
