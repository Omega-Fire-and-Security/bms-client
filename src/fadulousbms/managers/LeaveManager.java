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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.io.FileInputStream;
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
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                    //Get Timestamp
                    String timestamp_json = RemoteComms.sendGetRequest("/timestamp/leave_timestamp", headers);
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
                        String leave_records_json = RemoteComms.sendGetRequest("/leave_records", headers);
                        LeaveServerObject leaveServerObject = gson.fromJson(leave_records_json, LeaveServerObject.class);
                        if (leaveServerObject != null)
                        {
                            if(leaveServerObject.get_embedded()!=null)
                            {
                                Leave[] leave_records_arr = leaveServerObject.get_embedded().getLeave_records();

                                if(leave_records_arr!=null)
                                {
                                    leave_records = new HashMap<>();
                                    for (Leave leave : leave_records_arr)
                                        leave_records.put(leave.get_id(), leave);
                                } else IO.log(getClass().getName(), IO.TAG_WARN, "could not find any leave_records in database");
                            } else IO.log(getClass().getName(), IO.TAG_WARN, "could not find any Leave records in the database.");
                        } else IO.log(getClass().getName(), IO.TAG_WARN, "no Leave records were found in the database.");

                        IO.log(getClass().getName(), IO.TAG_INFO, "reloaded leave_records collection.");
                        this.serialize(ROOT_PATH + filename, leave_records);
                    } else
                    {
                        IO.log(this.getClass().getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                        leave_records = (HashMap<String, Leave>) this.deserialize(ROOT_PATH + filename);
                    }
                } else
                {
                    IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                }
            } else
            {
                IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
            }
        } catch (MalformedURLException ex)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        } catch (IOException ex)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    public static void viewPDF(Leave leave)
    {
        if(leave!=null)
        {
            try
            {
                String path = PDF.createLeaveApplicationPDF(leave);

                if(path!=null)
                {
                    PDFViewer pdfViewer = PDFViewer.getInstance();
                    pdfViewer.setVisible(true);
                    pdfViewer.doOpen(path);
                } else IO.log(LeaveManager.class.getName(), IO.TAG_ERROR, "could not get a valid path for generated leave application PDF for user ["+leave.getUsr()+"].");
            } catch (IOException e)
            {
                IO.log(LeaveManager.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        }else{
            IO.log(LeaveManager.class.getName(), IO.TAG_ERROR, "Leave object is null");
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

        final ComboBox<String> cbx_type = new ComboBox<>();
        cbx_type.setMinWidth(200);
        cbx_type.setMaxWidth(Double.MAX_VALUE);
        cbx_type.setEditable(false);
        cbx_type.setItems(FXCollections.observableArrayList(Leave.TYPES));
        HBox type_container = CustomTableViewControls.getLabelledNode("Leave Type: ", 200, cbx_type);

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

            if (!Validators.isValidNode(cbx_type, cbx_type.getValue() == null ? "" : cbx_type.getValue(), ".+"))
                return;
            if (!Validators.isValidNode(dpk_start_date, dpk_start_date.getValue() == null ? "" : dpk_start_date.getValue().toString(), 4, date_regex))
                return;
            if (!Validators.isValidNode(dpk_end_date, dpk_end_date.getValue() == null ? "" : dpk_end_date.getValue().toString(), 4, date_regex))
                return;
            long start_date_in_sec = dpk_start_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            long end_date_in_sec = dpk_end_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("usr", SessionManager.getInstance().getActiveEmployee().getUsr()));
            params.add(new AbstractMap.SimpleEntry<>("type", cbx_type.getValue()));
            params.add(new AbstractMap.SimpleEntry<>("start_date", String.valueOf(start_date_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("end_date", String.valueOf(end_date_in_sec)));

            Leave leave_record = new Leave(SessionManager.getInstance().getActiveEmployee().getUsr(),start_date_in_sec, end_date_in_sec, cbx_type.getValue());
            leave_record.setCreator(SessionManager.getInstance().getActiveEmployee().getUsr());

            if (str_other != null)
                if (!str_other.isEmpty())
                    params.add(new AbstractMap.SimpleEntry<>("other", str_other));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
                if (SessionManager.getInstance().getActive() != null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
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
        vbox.getChildren().add(type_container);
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

    /**
     * Method to upload a Leave application (in PDF format) to the server.
     * @param leave_id identifier of Leave object to be uploaded.
     */
    public static void uploadSigned(String leave_id)
    {
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    FileChooser fileChooser = new FileChooser();
                    File f = fileChooser.showOpenDialog(null);
                    if (f != null)
                    {
                        if (f.exists())
                        {
                            FileInputStream in = new FileInputStream(f);
                            byte[] buffer = new byte[(int) f.length()];
                            in.read(buffer, 0, buffer.length);
                            in.close();

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            RemoteComms.uploadFile("/api/leave_record/signed/upload/" + leave_id, headers, buffer);
                            IO.log(LeaveManager.class.getName(), IO.TAG_INFO, "\n uploaded signed leave application ["+leave_id+"], file size: [" + buffer.length + "] bytes.");
                        } else
                        {
                            IO.logAndAlert(LeaveManager.class.getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(LeaveManager.class.getName(), "File object is null.", IO.TAG_ERROR);
                    }
                }catch (IOException e)
                {
                    IO.log(LeaveManager.class.getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.logAndAlert("Error: Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.logAndAlert("Error: Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /**
     * Method to upload a Leave application (in PDF format) to the server.
     * @param leave Leave object to be uploaded.
     */
    public static void uploadPDF(Leave leave)
    {
        if(leave==null)
        {
            IO.logAndAlert("Error", "Invalid Leave object passed.", IO.TAG_ERROR);
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
                    String path = PDF.createLeaveApplicationPDF(leave);
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

                                RemoteComms.uploadFile("/api/leave_record/upload/" + leave.get_id(), headers, buffer);
                                IO.log(LeaveManager.class.getName(), IO.TAG_INFO, "\n uploaded leave application [#" + leave.get_id()
                                        + "], file size: [" + buffer.length + "] bytes.");
                            } else {
                                IO.logAndAlert(LeaveManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                            }
                        } else
                        {
                            IO.log(LeaveManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
                        }
                    } else IO.log(LeaveManager.class.getName(), "Could not get valid path for created leave application PDF.", IO.TAG_ERROR);
                } catch (IOException e)
                {
                    IO.log(LeaveManager.class.getName(), e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /**
     * Method to send eMail containing Leave application (PDF) and a URL to sign the Leave.
     * @param leave Leave object to be signed.
     * @param callback Callback to be executed on successful request.
     */
    public static void requestApproval(Leave leave, Callback callback)
    {
        if(leave==null)
        {
            IO.logAndAlert("Error", "Invalid Leave object.", IO.TAG_ERROR);
            return;
        }
        if(LeaveManager.getInstance().getLeaveRecords()==null)
        {
            IO.logAndAlert("Error", "Could not find any leave applications in the system.", IO.TAG_ERROR);
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

        //upload Leave PDF to server
        uploadPDF(leave);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Request Leave Application Approval");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        //gather list of Employees with enough clearance to sign leaves
        ArrayList<Employee> lst_auth_employees = new ArrayList<>();
        for(Employee employee: EmployeeManager.getInstance().getEmployees().values())
            if(employee.getAccessLevel()>=AccessLevels.SUPERUSER.getLevel())
                lst_auth_employees.add(employee);

        if(lst_auth_employees==null)
        {
            IO.logAndAlert("Error", "Could not find any employee with the required access rights to approval the leave application.", IO.TAG_ERROR);
            return;
        }

        final ComboBox<Employee> cbx_destination = new ComboBox(FXCollections.observableArrayList(lst_auth_employees));
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
        HBox destination = CustomTableViewControls.getLabelledNode("To: ", 200, cbx_destination);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText("LEAVE APPLICATION APPROVAL REQUEST["+leave.getEmployee()+"]");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            //TODO: check this
            if(!Validators.isValidNode(cbx_destination, cbx_destination.getValue()==null?"":cbx_destination.getValue().getEmail(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("leave_record_id", leave.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("to_email", cbx_destination.getValue().getEmail()));
            params.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));
            params.add(new AbstractMap.SimpleEntry<>("message", msg));

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

                HttpURLConnection connection = RemoteComms.postData("/api/leave_record/request_approval", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully requested leave application approval from ["+cbx_destination.getValue()+"]!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    } else {
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                IO.log(LeaveManager.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        cbx_destination.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue==null)
            {
                IO.log(LeaveManager.class.getName(), "invalid destination address.", IO.TAG_ERROR);
                return;
            }
            Employee sender = SessionManager.getInstance().getActiveEmployee();
            String title = null;
            if(newValue.getGender()!=null)
                title = newValue.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";
            String msg = "Good day " + title + " " + newValue.getLastname() + ",\n\nCould you please assist me" +
                    " by approving this leave application for "  + leave.getEmployee() + ".\nThank you.\n\nBest Regards,\n"
                    + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
            txt_message.setText(msg);
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        //vbox.getChildren().add(hbox_leave_id);
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

    /**
     * Method to email a signed copy of the Leave application to any specified recipient.
     * @param leave Leave object to be emailed.
     * @param callback Callback to be executed on successful request.
     */
    public static void emailSigned(Leave leave, Callback callback)
    {
        if(leave==null)
        {
            IO.logAndAlert("Error", "Invalid Leave object passed.", IO.TAG_ERROR);
            return;
        }
        //valid data - move on ...
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Signed Leave Application ["+leave.get_id()+"]");
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

        final TextField txt_leave_id = new TextField();
        txt_leave_id.setMinWidth(200);
        txt_leave_id.setMaxWidth(Double.MAX_VALUE);
        txt_leave_id.setPromptText("Type in a message");
        txt_leave_id.setEditable(false);
        txt_leave_id.setText(leave.get_id());
        HBox hbox_leave_id = CustomTableViewControls.getLabelledNode("Leave ID: ", 200, txt_leave_id);

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
            params.add(new AbstractMap.SimpleEntry<>("leave_record_id", leave.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("to_email", str_destination));
            params.add(new AbstractMap.SimpleEntry<>("subject", str_subject));
            params.add(new AbstractMap.SimpleEntry<>("message", str_message));
            //params.add(new AbstractMap.SimpleEntry<>("signed_leave_object", leave.getSigned_leave()));

            try
            {
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

                HttpURLConnection connection = RemoteComms.postData("/api/leave_record/signed/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully emailed signed leave application to: " + txt_destination.getText(), IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR: " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(LeaveManager.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        vbox.getChildren().add(hbox_leave_id);
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
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

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

    class LeaveServerObject extends ServerObject
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
            private Leave[] leave_records;

            public Leave[] getLeave_records()
            {
                return leave_records;
            }

            public void setLeave_records(Leave[] leave_records)
            {
                this.leave_records = leave_records;
            }
        }
    }
}
