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

public class OvertimeManager extends BusinessObjectManager
{
    private HashMap<String, Overtime> overtime_records;
    private Gson gson;
    private static OvertimeManager overtimeManager = new OvertimeManager();
    public static final String ROOT_PATH = "cache/overtime/";
    public String filename = "";
    private long timestamp;

    private OvertimeManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    public static OvertimeManager getInstance()
    {
        return overtimeManager;
    }

    @Override
    public HashMap<String, Overtime> getDataset()
    {
        return this.overtime_records;
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms.sendGetRequest("/timestamp/overtime_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "overtime_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            }
                            else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_ERROR, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                String overtime_records_json = RemoteComms.sendGetRequest("/overtime_records", headers);
                                OvertimeServerObject overtimeServerObject = gson.fromJson(overtime_records_json, OvertimeServerObject.class);
                                if (overtimeServerObject != null)
                                {
                                    if(overtimeServerObject.get_embedded()!=null)
                                    {
                                        Overtime[] overtime_records_arr = overtimeServerObject.get_embedded()
                                                .getOvertime_records();

                                        if(overtime_records_arr!=null)
                                        {
                                            overtime_records = new HashMap<>();
                                            for (Overtime overtime : overtime_records_arr)
                                                overtime_records.put(overtime.get_id(), overtime);
                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Overtime records in the database.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Overtime records in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_WARN, "no Overtime records found in the database.");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded Overtime records collection.");
                                serialize(ROOT_PATH + filename, overtime_records);
                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                overtime_records = (HashMap<String, Overtime>) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "No valid active sessions found.", IO.TAG_ERROR);
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

    public void newOvertimeApplicationWindow(Employee employee, Job job, Callback callback)
    {
        if (employee == null)
        {
            IO.logAndAlert("OvertimeManager Error", "Employee object is null.", IO.TAG_INFO);
            return;
        }
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Overtime Application [" + employee + "]");
        stage.setMinWidth(280);
        stage.setHeight(400);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        VBox vbox = new VBox(1);

        final TextField txt_employee = new TextField();
        txt_employee.setMinWidth(200);
        txt_employee.setMaxWidth(Double.MAX_VALUE);
        txt_employee.setEditable(false);
        txt_employee.setText(employee.getName());
        HBox employee_container = CustomTableViewControls.getLabelledNode("Employee: ", 200, txt_employee);

        ComboBox<Job> cbx_jobs = new ComboBox<>();
        cbx_jobs.setCellFactory(new Callback<ListView<Job>, ListCell<Job>>()
        {
            @Override
            public ListCell<Job> call(ListView<Job> param)
            {

                return new ListCell<Job>()
                {
                    @Override
                    protected void updateItem(Job job, boolean empty)
                    {
                        super.updateItem(job, empty);
                        if (job != null && !empty)
                            setText("Job #" + String.valueOf(job.getObject_number()));
                    }
                };
            }
        });

        if (JobManager.getInstance().getDataset() != null)
            cbx_jobs.setItems(FXCollections.observableArrayList(JobManager.getInstance().getDataset().values()));
        else
        {
            IO.logAndAlert("Error", "No jobs were found in the database.", IO.TAG_ERROR);
            stage.close();
            return;
        }
        //set job on combo box if not null
        if (job != null)
            cbx_jobs.setValue(job);

        cbx_jobs.setMinWidth(200);
        cbx_jobs.setMaxWidth(Double.MAX_VALUE);
        HBox job_container = CustomTableViewControls.getLabelledNode("Job Number: ", 200, cbx_jobs);

        final DatePicker dpk_overtime_date = new DatePicker();
        dpk_overtime_date.setMinWidth(200);
        dpk_overtime_date.setMaxWidth(Double.MAX_VALUE);
        HBox date_container = CustomTableViewControls.getLabelledNode("Overtime Date", 200, dpk_overtime_date);

        final TextField txt_checkin = new TextField();
        txt_checkin.setMinWidth(200);
        txt_checkin.setMaxWidth(Double.MAX_VALUE);
        HBox checkin_container = CustomTableViewControls.getLabelledNode("Check-in Time: ", 200, txt_checkin);

        final TextField txt_checkout = new TextField();
        txt_checkout.setMinWidth(200);
        txt_checkout.setMaxWidth(Double.MAX_VALUE);
        HBox checkout_container = CustomTableViewControls.getLabelledNode("Check-out Time: ", 200, txt_checkout);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex = "\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if (cbx_jobs.getValue() == null)
            {
                IO.logAndAlert("Error", "Invalid job number selection, please choose a valid job number and try again.", IO.TAG_ERROR);
                return;
            }
            if (!Validators
                    .isValidNode(dpk_overtime_date, dpk_overtime_date.getValue() == null ? "" : dpk_overtime_date
                            .getValue().toString(), 4, date_regex))
                return;
            if (!Validators.isValidNode(txt_checkin, txt_checkin.getText(), 1, ".+"))
                return;
            if (!Validators.isValidNode(txt_checkout, txt_checkout.getText(), 1, ".+"))
                return;

            long date_in_sec = dpk_overtime_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("usr", SessionManager.getInstance().getActiveEmployee().getUsr()));
            params.add(new AbstractMap.SimpleEntry<>("job_id", cbx_jobs.getValue().get_id()));
            params.add(new AbstractMap.SimpleEntry<>("date", String.valueOf(date_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("time_in", txt_checkin.getText()));
            params.add(new AbstractMap.SimpleEntry<>("time_out", txt_checkout.getText()));
            if (str_other != null)
                if (!str_other.isEmpty())
                    params.add(new AbstractMap.SimpleEntry<>("other", str_other));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if (SessionManager.getInstance().getActive() != null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive()
                            .getSession_id()));
                else
                {
                    IO.logAndAlert("Error: Session expired", "No active sessions.", IO.TAG_INFO);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/overtime_record/add", params, headers);
                if (connection != null)
                {
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully logged overtime record.", IO.TAG_INFO);
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

        //populate clients combobox

        //Add form controls vertically on the stage
        vbox.getChildren().add(employee_container);
        vbox.getChildren().add(job_container);
        vbox.getChildren().add(date_container);
        vbox.getChildren().add(checkin_container);
        vbox.getChildren().add(checkout_container);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///" + fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                synchroniseDataset());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
    }

    public static void approveOvertime(Overtime overtime, Callback callback)
    {
        if (overtime == null)
        {
            IO.logAndAlert("Error", "Invalid overtime record.", IO.TAG_ERROR);
            return;
        }
        if (overtime.getStatus() == Overtime.STATUS_APPROVED)
        {
            IO.logAndAlert("Error", "Overtime record has already been approved.", IO.TAG_ERROR);
            return;
        }

        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

                overtime.setStatus(Overtime.STATUS_APPROVED);
                try
                {
                    HttpURLConnection connection = RemoteComms.postData( "/api/overtime_record/update/"+overtime.get_id(), overtime.asUTFEncodedString(), headers);
                    if(connection!=null)
                    {
                        if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                        {
                            IO.logAndAlert("Success", overtime.getEmployee().getName()+"'s overtime request has been successfully approved.", IO.TAG_INFO);
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

    class OvertimeServerObject extends ServerObject
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
            private Overtime[] overtime_records;

            public Overtime[] getOvertime_records()
            {
                return overtime_records;
            }

            public void setOvertime_records(Overtime[] overtime_records)
            {
                this.overtime_records = overtime_records;
            }
        }
    }
}
