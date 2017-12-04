package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

public class LeaveManager extends BusinessObjectManager
{
    private HashMap<String, Leave> leave_records;
    private Gson gson;
    private static LeaveManager leaveManager = new LeaveManager();
    public static final String ROOT_PATH = "cache/leave/";
    public String filename = "";
    private long timestamp;
    private Leave selected;

    private LeaveManager()
    {
    }

    public HashMap<String, Leave> getLeaveRecords()
    {
        return this.leave_records;
    }

    public static LeaveManager getInstance()
    {
        return leaveManager;
    }

    public Leave getSelected()
    {
        return selected;
    }

    public void setSelected(Leave selected)
    {
        this.selected = selected;
    }

    @Override
    public void initialize()
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        try
        {
            if (leave_records == null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "leave_records object has already been set.");
        } catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
        } catch (ClassNotFoundException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    public void reloadDataFromServer() throws ClassNotFoundException, IOException
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
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/leave_timestamp", headers);
                    Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                    if (cntr_timestamp != null)
                    {
                        timestamp = cntr_timestamp.getCount();
                        filename = "leave_" + timestamp + ".dat";
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                    } else
                    {
                        IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                        return;
                    }

                    if (!isSerialized(ROOT_PATH + filename))
                    {
                        String leave_records_json = RemoteComms.sendGetRequest("/api/leave_records", headers);
                        Leave[] leave_records_arr = gson.fromJson(leave_records_json, Leave[].class);

                        leave_records = new HashMap();
                        for (Leave leave : leave_records_arr)
                            leave_records.put(leave.get_id(), leave);

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded leave_records collection.");
                        this.serialize(ROOT_PATH + filename, leave_records);
                    }
                    else
                    {
                        IO.log(this.getClass()
                                .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                        leave_records = (HashMap<String, Leave>) this.deserialize(ROOT_PATH + filename);
                    }
                }
                else
                {
                    IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                }
            }
            else
            {
                IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            }
        } catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    public void newLeaveApplicationWindow(Employee employee, Callback callback)
    {
        if (employee == null)
        {
            IO.logAndAlert("Leave Application Error", "Employee object is null.", IO.TAG_INFO);
            return;
        }
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Leave Application [" + employee + "]");
        stage.setMinWidth(280);
        stage.setHeight(400);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        VBox vbox = new VBox(1);

        final TextField txt_employee = new TextField();
        txt_employee.setMinWidth(200);
        txt_employee.setMaxWidth(Double.MAX_VALUE);
        txt_employee.setEditable(false);
        txt_employee.setText(employee.toString());
        HBox employee_container = CustomTableViewControls.getLabelledNode("Employee: ", 200, txt_employee);

        final DatePicker dpk_start_date = new DatePicker();
        dpk_start_date.setMinWidth(200);
        dpk_start_date.setMaxWidth(Double.MAX_VALUE);
        HBox start_date_container = CustomTableViewControls.getLabelledNode("Start Date", 200, dpk_start_date);

        final DatePicker dpk_end_date = new DatePicker();
        dpk_end_date.setMinWidth(200);
        dpk_end_date.setMaxWidth(Double.MAX_VALUE);
        HBox end_date_container = CustomTableViewControls.getLabelledNode("End Date", 200, dpk_end_date);

        /*final DatePicker dpk_return_date = new DatePicker();
        dpk_return_date.setMinWidth(200);
        dpk_return_date.setMaxWidth(Double.MAX_VALUE);
        HBox return_date_container = CustomTableViewControls.getLabelledNode("Return Date", 200, dpk_return_date);*/

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex = "\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if (!Validators
                    .isValidNode(dpk_start_date, dpk_start_date.getValue() == null ? "" : dpk_start_date
                            .getValue().toString(), 4, date_regex))
                return;
            if (!Validators
                    .isValidNode(dpk_end_date, dpk_end_date.getValue() == null ? "" : dpk_end_date
                            .getValue().toString(), 4, date_regex))
                return;

            long start_date_in_sec = dpk_start_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long end_date_in_sec = dpk_end_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("usr", SessionManager.getInstance().getActiveEmployee().getUsr()));
            params.add(new AbstractMap.SimpleEntry<>("start_date", String.valueOf(start_date_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("end_date", String.valueOf(end_date_in_sec)));

            if (str_other != null)
                if (!str_other.isEmpty())
                    params.add(new AbstractMap.SimpleEntry<>("other", str_other));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if (SessionManager.getInstance().getActive() != null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive()
                            .getSessionId()));
                else
                {
                    IO.logAndAlert("Error: Session expired", "No active sessions.", IO.TAG_INFO);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/leave_record/add", params, headers);
                if (connection != null)
                {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully logged leave application.", IO.TAG_INFO);
                        if (callback != null)
                            callback.call(null);
                    }
                    else
                    {
                        IO.logAndAlert("ERROR_" + connection.getResponseCode(), IO
                                .readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(employee_container);
        vbox.getChildren().add(start_date_container);
        vbox.getChildren().add(end_date_container);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///" + fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    public static void approveLeave(Leave leave, Callback callback)
    {
        if (leave == null)
        {
            IO.logAndAlert("Error", "Invalid leave record.", IO.TAG_ERROR);
            return;
        }
        if (leave.getStatus() == Leave.STATUS_APPROVED)
        {
            IO.logAndAlert("Error", "Leave record has already been approved.", IO.TAG_ERROR);
            return;
        }

        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

                leave.setStatus(Leave.STATUS_APPROVED);
                try
                {
                    HttpURLConnection connection = RemoteComms.postData( "/api/leave_record/update/"+leave.get_id(), leave.asUTFEncodedString(), headers);
                    if(connection!=null)
                    {
                        if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                        {
                            IO.logAndAlert("Success", leave.getEmployee().toString()+"'s leave application has been successfully approved.", IO.TAG_INFO);
                            if(callback!=null)
                                callback.call(null);
                        }else{
                            IO.logAndAlert("Error", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                        }
                        connection.disconnect();
                    }
                } catch (IOException e)
                {
                    e.printStackTrace();
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }
}
