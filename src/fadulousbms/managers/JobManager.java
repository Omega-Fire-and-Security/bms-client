package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.controllers.HomescreenController;
import fadulousbms.controllers.JobsController;
import fadulousbms.controllers.OperationsController;
import fadulousbms.model.*;
import fadulousbms.model.Error;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ghost on 2017/01/11.
 */
public class JobManager extends BusinessObjectManager
{
    private HashMap<String, Job> jobs;
    private BusinessObject[] genders=null, domains=null;
    private Gson gson;
    private ScreenManager screenManager = null;
    private static JobManager job_manager = new JobManager();
    private Job selected_job;
    public static final String TAG = "JobManager";
    public static final String ROOT_PATH = "cache/jobs/";
    public String filename = "";
    private long timestamp;

    private JobManager()
    {
    }

    public static JobManager getInstance()
    {
        return job_manager;
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
    }

    /**
     * Method to load Job objects from the server if they have not already been reloaded.
     */
    public void loadDataFromServer()
    {
        try
        {
            if(jobs==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "jobs object has already been set.");
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

    /**
     * Method to force synchronize Job objects from the server with local storage.
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public void reloadDataFromServer() throws ClassNotFoundException, IOException
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                gson = new GsonBuilder().create();
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                //Get Timestamp
                String timestamp_json = RemoteComms.sendGetRequest("/api/timestamp/jobs_timestamp", headers);
                Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                if (cntr_timestamp != null)
                {
                    timestamp = cntr_timestamp.getCount();
                    filename = "jobs_" + timestamp + ".dat";
                    IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                }
                else
                {
                    IO.logAndAlert(this.getClass().getName(), "could not get valid timestamp", IO.TAG_ERROR);
                    return;
                }

                if (!isSerialized(ROOT_PATH + filename))
                {
                    String jobs_json = RemoteComms.sendGetRequest("/api/jobs", headers);
                    Job[] jobs_arr = gson.fromJson(jobs_json, Job[].class);

                    jobs = new HashMap<>();
                    for (Job job : jobs_arr)
                    {
                        //Load JobEmployee objects using Job_id
                        String jobemployees_json = RemoteComms
                                .sendGetRequest("/api/job/employees/" + job.get_id(), headers);
                        JobEmployee[] jobemployees = gson.fromJson(jobemployees_json, JobEmployee[].class);

                        // Get Employee objects from Employee_id derived from JobEmployee objects
                        // And load them into an array.
                        Employee[] employees_arr = new Employee[jobemployees.length];
                        for (int i = 0; i < jobemployees.length; i++)
                        {
                            String employees_json = RemoteComms
                                    .sendGetRequest("/api/employee/" + jobemployees[i].getUsr(), headers);
                            Employee employee = gson.fromJson(employees_json, Employee.class);
                            employees_arr[i] = employee;
                        }
                        // Set Employee objects on to Job object.
                        job.setAssigned_employees(employees_arr);

                        //Load Job Safety Catalogue
                        String job_cat_json = RemoteComms
                                .sendGetRequest("/api/job/safetycatalogue/" + job.get_id(), headers);
                        JobSafetyCatalogue[] safetyCatalog = gson
                                .fromJson(job_cat_json, JobSafetyCatalogue[].class);
                        FileMetadata[] safety_docs = new FileMetadata[safetyCatalog.length];
                        for (int i = 0; i < safetyCatalog.length; i++)
                        {
                            String safety_doc_json = RemoteComms
                                    .sendGetRequest("/api/safety/index/" + safetyCatalog[i]
                                            .getSafety_id(), headers);
                            FileMetadata safety_doc = gson.fromJson(safety_doc_json, FileMetadata.class);
                            safety_docs[i] = safety_doc;
                        }
                        job.setSafety_catalogue(safety_docs);
                        jobs.put(job.get_id(), job);
                    }
                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of jobs.");
                    this.serialize(ROOT_PATH + filename, jobs);
                }
                else
                {
                    IO.log(this.getClass()
                            .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                    jobs = (HashMap<String, Job>) this.deserialize(ROOT_PATH + filename);
                }
            }else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /**
     * Method to get a map of all Jobs in the database.
     * @return
     */
    public HashMap<String, Job> getJobs()
    {
        return this.jobs;
    }

    /**
     * Method to create new Job object on the database server.
     * @param job Job object to be created.
     * @param callback Callback to be executed on if request was successful.
     * @return server response.
     */
    public String createNewJob(Job job, Callback callback)
    {
        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            //create new job on database
            HttpURLConnection connection = RemoteComms.postData("/api/job/add", job.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "successfully created a new job: " + response);

                    JobManager.getInstance().reloadDataFromServer();
                    if(callback!=null)
                        callback.call(null);

                    if(connection!=null)
                        connection.disconnect();
                    return response;
                }else{
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    if(connection!=null)
                        connection.disconnect();
                    return null;
                }
            }else IO.logAndAlert("Job Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    /**
     * Method to set the currently selected Job object.
     * @param job Job object to be set as selected Job object.
     */
    public void setSelected(Job job)
    {
        this.selected_job = job;
        if(selected_job!=null)
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected job to: " + job);
        //}else IO.log(getClass().getName(), IO.TAG_ERROR, "job to be set as selected is null.");
    }

    /**
     * Method to return the currently selected Job object.
     * @return selected Job object.
     */
    public Job getSelected()
    {
        return selected_job;
    }

    /**
     * Assign Employees to a Job.
     * @param job_id Object identifier of Job to be assigned Employees.
     * @param usr username of Employee to be assigned to Job.
     * @return boolean value determining if assingnment was un/successful.
     */
    public static boolean createJobRepresentative(String job_id, String usr)
    {
        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("job_id", job_id));
            params.add(new AbstractMap.SimpleEntry<>("usr", usr));

            String job_employee_object = URLEncoder.encode("job_id", "UTF-8") + "="
                                        + URLEncoder.encode(job_id, "UTF-8")
                                        + "&" + URLEncoder.encode("usr", "UTF-8") + "="
                                        + URLEncoder.encode(usr, "UTF-8");
            //create new job on database
            HttpURLConnection connection = RemoteComms.postData("/api/job/employee/add", job_employee_object, headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log("Job Manager", IO.TAG_INFO, "successfully created a new job representative: " + response);
                    //IO.logAndAlert("Job Manager", "Successfully created a new job.", IO.TAG_INFO);
                    if(connection!=null)
                        connection.disconnect();
                    return true;
                }else{
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    if(connection!=null)
                        connection.disconnect();
                    return false;
                }
            }else IO.logAndAlert("Job Representative Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert("Jobs Manager", e.getMessage(), IO.TAG_ERROR);
        }
        return false;
    }

    /**
     * Method to show a catalog (in PDF viewer) of safety files associated with the Job
     * @param job Job object whose safety catalog is to be shown.
     */
    public void showJobSafetyFile(Job job)
    {
        if(job !=null)
        {
            SafetyManager.listSafetyDocuments(job.getSafety_catalogue(), job.get_id());
        }else IO.log(TAG, IO.TAG_ERROR, "showJobSafetyFile> job object is null.");
    }

    /**
     * Method to send request to server to sign a Job.
     * @param job Job object to be signed.
     * @param callback Callback to be executed on successful request.
     */
    public static void signJob(Job job, Callback callback)
    {
        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
        Session active = SessionManager.getInstance().getActive();
        try
        {
            if (active != null)
            {
                if (!active.isExpired())
                {
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", active.getSessionId()));
                    HttpURLConnection conn = RemoteComms.postData("/api/job/sign/" + job.get_id(),"", headers);
                    if(conn!=null)
                    {
                        if(conn.getResponseCode()==HttpURLConnection.HTTP_OK)
                        {
                            IO.logAndAlert("Success", "Successfully signed job[" + job.get_id()+"]", IO.TAG_ERROR);
                            if(callback!=null)
                                callback.call(null);
                        } else IO.logAndAlert("Error", "Could not sign job["+job.get_id()+"]: "+IO.readStream(conn.getErrorStream()), IO.TAG_ERROR);
                        conn.disconnect();
                    }
                } else IO.showMessage("Error: Session Expired", "Active session has expired.", IO.TAG_ERROR);
            } else IO.showMessage("Error: Session Expired", "No active sessions.", IO.TAG_ERROR);
        }catch (IOException e)
        {
            IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    /**
     * Method to view Job info in editable form.
     * @param job Job object to exported to a PDF document.
     */
    public static void viewJob(Job job)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
            return;
        }

        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    JobManager.getInstance().setSelected(job);
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_JOB.getScreen(),getClass().getResource("../views/"+Screens.VIEW_JOB.getScreen())))
                        {
                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_JOB.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs viewer screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    /**
     * Method to view Job in PDF viewer.
     * @param job Job object to exported to a PDF document.
     */
    public static void showJobCard(Job job)
    {
        if(job!=null)
        {
            try
            {
                String path = PDF.createJobCardPdf(job);

                if(path!=null)
                {
                    PDFViewer pdfViewer = PDFViewer.getInstance();
                    pdfViewer.setVisible(true);
                    pdfViewer.doOpen(path);
                } else IO.log("JobManager", IO.TAG_ERROR, "could not get a valid path for generated Job[#"+job.getJob_number()+"] card PDF.");
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        }else{
            IO.log(TAG, IO.TAG_ERROR, "Job object is null");
        }
    }

    /**
     * Method to send eMail containing Job card (PDF) and a URL to sign the Job.
     * @param job Job object to be signed.
     * @param callback Callback to be executed on successful request.
     */
    public static void requestSignature(Job job, Callback callback)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid Job.", IO.TAG_ERROR);
            return;
        }
        if(JobManager.getInstance().getJobs()==null)
        {
            IO.logAndAlert("Error", "Could not find any jobs in the system.", IO.TAG_ERROR);
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

        //upload Job PDF to server
        uploadJobCardPDF(job);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Job ["+job.get_id()+"]");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        //gather list of Employees with enough clearance to sign jobs
        ArrayList<Employee> lst_auth_employees = new ArrayList<>();
        for(Employee employee: EmployeeManager.getInstance().getEmployees().values())
            if(employee.getAccessLevel()>=Employee.ACCESS_LEVEL_SUPER)
                lst_auth_employees.add(employee);

        if(lst_auth_employees==null)
        {
            IO.logAndAlert("Error", "Could not find any employee with the required access rights to sign off the job.", IO.TAG_ERROR);
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
        txt_subject.setText("JOB [#"+job.getJob_number()+"] SIGNATURE REQUEST");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        /*final TextField txt_job_id = new TextField();
        txt_job_id.setMinWidth(200);
        txt_job_id.setMaxWidth(Double.MAX_VALUE);
        txt_job_id.setPromptText("Type in a message");
        txt_job_id.setEditable(false);
        txt_job_id.setText(String.valueOf(quote.get_id()));
        HBox hbox_job_id = CustomTableViewControls.getLabelledNode("Quote ID: ", 200, txt_job_id);*/

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
            params.add(new AbstractMap.SimpleEntry<>("job_id", job.get_id()));
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
                            .getSessionId()));
                    params.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().toString()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/job/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully sent email to ["+cbx_destination.getValue()+"]!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                IO.log(JobManager.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        cbx_destination.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue==null)
            {
                IO.log(JobManager.class.getName(), "invalid destination address.", IO.TAG_ERROR);
                return;
            }
            Employee sender = SessionManager.getInstance().getActiveEmployee();
            String title = null;
            if(newValue.getGender()!=null)
                title = newValue.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";
            String msg = "Good day " + title + " " + newValue.getLastname() + ",\n\nCould you please assist me" +
                    " by signing this job to be rendered to "  + job.getQuote().getClient().getClient_name() + ".\nThank you.\nBest Regards,\n"
                    + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
            txt_message.setText(msg);
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
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
        stage.setResizable(true);
    }

    /**
     * Method to upload a Job (in Base64 format) to the server.
     * @param job Job object to be uploaded.
     */
    public static void uploadSigned(Job job)
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                            //headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            //update signed job property
                            //TODO: if access level is sufficient, sign it off once
                            job.setSigned_job(Base64.getEncoder().encodeToString(buffer));

                            RemoteComms.uploadFile("/api/job/update/" + job.get_id(), headers, buffer);
                            IO.logAndAlert("Success", "successfully uploaded signed job [#"+job.getJob_number()+"], file size: [" + buffer.length + "] bytes.", IO.TAG_INFO);
                            /*HttpURLConnection connection = RemoteComms.postData("/api/job/update/" + job.get_id(), job.asUTFEncodedString(), headers);
                            if(connection!=null)
                            {
                                IO.logAndAlert("Success", "successfully uploaded signed job [#"+job.getJob_number()+"], file size: [" + buffer.length + "] bytes.", IO.TAG_INFO);
                                connection.disconnect();
                                System.out.println("uploaded file: " + job.getSigned_job());
                            } else IO.logAndAlert("ERROR_" + connection.getResponseCode(), "could not upload signed job [#"+job.getJob_number()
                                    +"], file size: [" + buffer.length + "] bytes. " + IO.readStream(connection.getErrorStream()), IO.TAG_INFO);*/
                        } else
                        {
                            IO.logAndAlert(JobManager.class.getName(), "File ["+f.getAbsolutePath()+"] not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(JobManager.class.getName(), "File object is null.", IO.TAG_ERROR);
                    }
                }catch (IOException e)
                {
                    IO.log(JobManager.class.getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /**
     * Method to upload a Job card (in PDF format) to the server.
     * @param job_id identifier of Job object to be uploaded.
     */
    public static void uploadSigned(String job_id)
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            RemoteComms.uploadFile("/api/job/signed/upload/" + job_id, headers, buffer);
                            IO.log(JobManager.class.getName(), IO.TAG_INFO, "\n uploaded signed job ["+job_id+"], file size: [" + buffer.length + "] bytes.");
                        } else
                        {
                            IO.logAndAlert(JobManager.class.getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(JobManager.class.getName(), "File object is null.", IO.TAG_ERROR);
                    }
                }catch (IOException e)
                {
                    IO.log(JobManager.class.getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.logAndAlert("Error: Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.logAndAlert("Error: Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /**
     * Method to upload a Job card (in PDF format) to the server.
     * @param job Job object to be uploaded.
     */
    public static void uploadJobCardPDF(Job job)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid job object passed.", IO.TAG_ERROR);
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
                    String path = PDF.createJobCardPdf(job);
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
                                        .getActive().getSessionId()));
                                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                                RemoteComms.uploadFile("/api/job/upload/" + job.get_id(), headers, buffer);
                                IO.log(JobManager.class.getName(), IO.TAG_INFO, "\n uploaded job card [#" + job
                                        .getJob_number() + "], file size: [" + buffer.length + "] bytes.");
                            }
                            else
                            {
                                IO.logAndAlert(JobManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                            }
                        }
                        else
                        {
                            IO.log(JobManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
                        }
                    } else IO.log(JobManager.class.getName(), "Could not get valid path for created job card pdf. Please make sure you've assigned employees to the job.", IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.log(JobManager.class.getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /**
     * Method to email a signed copy of the Job card to any specified recipient.
     * @param job Job object to be emailed.
     * @param callback Callback to be executed on successful request.
     */
    public static void emailSignedJobCard(Job job, Callback callback)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid job object passed.", IO.TAG_ERROR);
            return;
        }
        /*if(job.getSigned_job()==null)
        {
            IO.logAndAlert("Error", "could not find signed job card for selected job [#"+job.getJob_number()+"].", IO.TAG_ERROR);
            return;
        }*/
        //valid data - move on ...
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Signed Job Card ["+job.get_id()+"]");
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
        txt_job_id.setText(job.get_id());
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
            params.add(new AbstractMap.SimpleEntry<>("job_id", job.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("to_email", str_destination));
            params.add(new AbstractMap.SimpleEntry<>("subject", str_subject));
            params.add(new AbstractMap.SimpleEntry<>("message", str_message));
            //params.add(new AbstractMap.SimpleEntry<>("signed_job", job.getSigned_job()));

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

                HttpURLConnection connection = RemoteComms.postData("/api/job/signed/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully emailed signed job card to: " + txt_destination.getText(), IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
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

    /**
     * Method to email a copy of the Job card to any specified recipient.
     * @param job Job object to be emailed.
     * @param callback Callback to be executed on successful request.
     */
    public static void emailJobCard(Job job, Callback callback)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid Job.", IO.TAG_ERROR);
            return;
        }
        if(job.getAssigned_employees()==null)
        {
            IO.logAndAlert("Error", "Job[#"+job.getJob_number()+"] has not been assigned any employees, please fix this and .", IO.TAG_ERROR);
            return;
        }

        //upload Job Card PDF to server
        uploadJobCardPDF(job);

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Job Card ["+job.get_id()+"]");
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
        txt_job_id.setText(String.valueOf(job.getJob_number()));
        HBox hbox_job_id = CustomTableViewControls.getLabelledNode("Job Number: ", 200, txt_job_id);

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
            params.add(new AbstractMap.SimpleEntry<>("job_id", job.get_id()));
            params.add(new AbstractMap.SimpleEntry<>("to_email", str_destination));
            params.add(new AbstractMap.SimpleEntry<>("subject", str_subject));
            params.add(new AbstractMap.SimpleEntry<>("message", str_message));
            try
            {
                /*String path = PDF.createJobCardPdf(job);
                if(path!=null)
                {
                    File f = new File(path);
                    if(f.exists())
                    {
                        byte[] file_arr = new byte[(int)f.length()];
                        FileInputStream in = new FileInputStream(f);
                        in.read(file_arr, 0, (int)f.length());
                        params.add(new AbstractMap.SimpleEntry<>("attachment", Base64.getEncoder().encodeToString(file_arr)));
                    } else IO.logAndAlert( "ERROR",  "job card [#"+job.getJob_number()+"] file does not exist.", IO.TAG_ERROR);
                }*/
                //send email
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

                HttpURLConnection connection = RemoteComms.postData("/api/job/mailto", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully emailed job card to ["+txt_destination.getText()+"]!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                e.printStackTrace();
                IO.log(JobManager.class.getName(), IO.TAG_ERROR, e.getMessage());
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

    /**
     * Method to show Job representatives
     * @param job Job object whose representatives are to be shown.
     */
    public static void showJobReps(Job job)
    {
        /*SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - job representatives");
                stage.setMinWidth(320);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblJobRepresentatives = new TableView();
                tblJobRepresentatives.setEditable(true);

                TableColumn<BusinessObject, String> resource_id = new TableColumn<>("Employee ID");
                resource_id.setMinWidth(100);
                resource_id.setCellValueFactory(new PropertyValueFactory<>("short_id"));

                TableColumn<BusinessObject, String> firstname = new TableColumn("First name");
                CustomTableViewControls.makeEditableTableColumn(firstname, TextFieldTableCell.forTableColumn(), 80, "firstname", "/api/employee");

                TableColumn<BusinessObject, String> lastname = new TableColumn("Last name");
                CustomTableViewControls.makeEditableTableColumn(lastname, TextFieldTableCell.forTableColumn(), 80, "lastname", "/api/employee");

                //TableColumn<BusinessObject, String> access_level = new TableColumn("Access level");
                //CustomTableViewControls.makeEditableTableColumn(access_level, TextFieldTableCellOld.forTableColumn(), 80, "access_level", "/api/employee");

                TableColumn<BusinessObject, String> gender = new TableColumn("Gender");
                CustomTableViewControls.makeComboBoxTableColumn(gender, genders, "gender", "gender", "/api/employee", 80);

                TableColumn<BusinessObject, String> email_address = new TableColumn("eMail address");
                CustomTableViewControls.makeEditableTableColumn(email_address, TextFieldTableCell.forTableColumn(), 80, "email", "/api/employee");

                TableColumn<BusinessObject, Long> date_joined = new TableColumn("Date joined");
                CustomTableViewControls.makeDatePickerTableColumn(date_joined, "date_joined", "/api/employee");

                TableColumn<BusinessObject, String> tel = new TableColumn("Tel. number");
                CustomTableViewControls.makeEditableTableColumn(tel, TextFieldTableCell.forTableColumn(), 80, "tel", "/api/employee");

                TableColumn<BusinessObject, String> cell = new TableColumn("Cell number");
                CustomTableViewControls.makeEditableTableColumn(cell, TextFieldTableCell.forTableColumn(), 80, "cell", "/api/employee");

                TableColumn<BusinessObject, String> domain = new TableColumn("Domain");
                CustomTableViewControls.makeComboBoxTableColumn(domain, domains, "active", "domain", "/api/employee", 80);
                //CustomTableViewControls.makeEditableTableColumn(other, TextFieldTableCellOld.forTableColumn(), 80, "other", "/api/quote/resource");

                TableColumn<BusinessObject, String> other = new TableColumn("Other");
                CustomTableViewControls.makeEditableTableColumn(other, TextFieldTableCell.forTableColumn(), 80, "other", "/api/employee");

                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu edit = new Menu("Edit");

                MenuItem new_resource = new MenuItem("New representative");
                new_resource.setOnAction(event -> handleNewJobRep(stage));
                MenuItem save = new MenuItem("Save");
                MenuItem print = new MenuItem("Print");


                ObservableList<Employee> lst_job_reps = FXCollections.observableArrayList();

                //Quote selected_quote = (Quote) tblQuotes.selectionModelProperty().get();
                //make fancy "New representative" label - not really necessary though
                if(jobs!=null)
                {
                    if(job.getAssigned_employees()!=null)
                    {
                       lst_job_reps.addAll(job.getAssigned_employees());
                       IO.log(TAG, IO.TAG_INFO, String.format("job '%s'  has %s representatives.", job.get_id(), job.getAssigned_employees().length));
                       IO.log(TAG, IO.TAG_INFO, String.format("added job '%s' representatives.", job.get_id()));
                        /*for (BusinessObject businessObject : organisations)
                        {
                            if(businessObject.get_id()!=null)
                            {
                                if (businessObject.get_id().equals(quotes[selected_index].get("issuer_org_id")))
                                {
                                    if (label_properties.split("\\|").length > 1)
                                    {
                                        String name = (String) businessObject.get(label_properties.split("\\|")[0]);
                                        if (name == null)
                                            name = (String) businessObject.get(label_properties.split("\\|")[1]);
                                        if (name == null)
                                            IO.log(TAG, IO.TAG_ERROR, "neither of the label_properties were found in object!");
                                        else
                                        {
                                            new_resource.setText("New representative for quote issued by " + name);
                                            IO.log(TAG, IO.TAG_INFO, String.format("set quote [representative] context to [quote issued by] '%s'", name));
                                        }
                                    } else IO.log(TAG, IO.TAG_ERROR, "label_properties split array index out of bounds!");
                                }
                            }else IO.log(TAG, IO.TAG_WARN, "business object id is null.");
                        }*
                    }else IO.log(TAG, IO.TAG_ERROR, String.format("assigned_employees of selected job '%s' is null.", job.get_id()));
                }else IO.log(TAG, IO.TAG_ERROR, "jobs array is null!");

                tblJobRepresentatives.setItems(lst_job_reps);
                tblJobRepresentatives.getColumns().addAll(firstname, lastname, gender,
                        email_address, date_joined, tel, cell, domain, other);


                file.getItems().addAll(new_resource, save, print);

                menu_bar.getMenus().addAll(file, edit);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblJobRepresentatives);

                stage.setOnCloseRequest(event ->
                {
                    IO.log(TAG, IO.TAG_INFO,"reloading local data.");
                    loadDataFromServer();
                    stage.close();
                });

                Scene scene = new Scene(border_pane);
                stage.setScene(scene);
                stage.show();
                stage.centerOnScreen();
                stage.setResizable(true);
            }else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);*/
    }

    class JobSafetyCatalogue implements BusinessObject
    {
        private String _id;
        private String job_id;
        private String safety_id;
        private long date_assigned;
        private boolean marked;
        public static final String TAG = "JobSafetyCatalogue";

        public StringProperty idProperty(){return new SimpleStringProperty(_id);}

        @Override
        public String get_id()
        {
            return _id;
        }

        public void set_id(String _id)
        {
            this._id = _id;
        }

        public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

        @Override
        public String getShort_id()
        {
            return _id.substring(0, 8);
        }

        private StringProperty job_idProperty(){return new SimpleStringProperty(job_id);}

        public String getJob_id()
        {
            return job_id;
        }

        public void setJob_id(String job_id)
        {
            this.job_id = job_id;
        }

        private StringProperty safety_idProperty(){return new SimpleStringProperty(safety_id);}

        public String getSafety_id()
        {
            return safety_id;
        }

        public void setSafety_id(String safety_id)
        {
            this.safety_id = safety_id;
        }

        private StringProperty date_assignedProperty(){return new SimpleStringProperty(String.valueOf(date_assigned));}

        public double getDate_assigned()
        {
            return date_assigned;
        }

        public void setDate_assigned(long date_assigned)
        {
            this.date_assigned = date_assigned;
        }

        @Override
        public boolean isMarked()
        {
            return marked;
        }

        @Override
        public void setMarked(boolean marked){this.marked=marked;}

        @Override
        public void parse(String var, Object val)
        {
            switch (var.toLowerCase())
            {
                case "job_id":
                    job_id = String.valueOf(val);
                    break;
                case "safety_id":
                    safety_id = String.valueOf(val);
                    break;
                case "date_assigned":
                    date_assigned = Long.parseLong((String)val);
                    break;
                default:
                    System.err.println("Unknown "+TAG+" attribute '" + var + "'.");
                    break;
            }
        }

        @Override
        public Object get(String var)
        {
            switch (var.toLowerCase())
            {
                case "job_id":
                    return job_id;
                case "safety_id":
                    return safety_id;
                case "date_assigned":
                    return date_assigned;
                default:
                    System.err.println("Unknown "+TAG+" attribute '" + var + "'.");
                    return null;
            }
        }

        @Override
        public String asUTFEncodedString()
        {
            //Return encoded URL parameters in UTF-8 charset
            StringBuilder result = new StringBuilder();
            try
            {
                result.append(URLEncoder.encode("job_id","UTF-8") + "="
                        + URLEncoder.encode(job_id, "UTF-8") + "&");
                result.append(URLEncoder.encode("safety_id","UTF-8") + "="
                        + URLEncoder.encode(safety_id, "UTF-8") + "&");
                result.append(URLEncoder.encode("date_assigned","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(date_assigned), "UTF-8"));

                return result.toString();
            } catch (UnsupportedEncodingException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
            return null;
        }

        @Override
        public String apiEndpoint()
        {
            return "/api/job/safetycatalogue";
        }
    }
}
