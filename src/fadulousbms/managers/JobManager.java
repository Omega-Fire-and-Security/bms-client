package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 * @author ghost
 */
public class JobManager extends ApplicationObjectManager
{
    private HashMap<String, Job> jobs;
    private HashMap<String, HashMap<String, JobEmployee>> job_employees;//grouped by Job _id
    private Gson gson;
    private static JobManager job_manager = new JobManager();
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
        synchroniseDataset();
    }

    /**
     * Method to get a map of all Jobs in the database.
     * @return
     */
    @Override
    public HashMap<String, Job> getDataset()
    {
        return this.jobs;
    }

    public HashMap<String, HashMap<String, JobEmployee>> getJob_employees()
    {
        return this.job_employees;
    }

    @Override
    public Job getSelected()
    {
        return (Job) super.getSelected();
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
                            gson = new GsonBuilder().create();
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms.get("/timestamp/jobs_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "jobs_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            }
                            else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                //Load Job objects from server
                                String jobs_json = RemoteComms.get("/jobs", headers);
                                JobServerResponseObject jobServerObject = (JobServerResponseObject) JobManager.getInstance().parseJSONobject(jobs_json, new JobServerResponseObject());
                                if (jobServerObject != null)
                                {
                                    if (jobServerObject.get_embedded() != null)
                                    {
                                        Job[] jobs_arr = jobServerObject.get_embedded().getJobs();

                                        jobs = new HashMap<>();
                                        for (Job job : jobs_arr)
                                            jobs.put(job.get_id(), job);
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Jobs in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_WARN, "could not find any Jobs in the database.");

                                String job_employees_json = RemoteComms.get("/jobs/employees", headers);
                                JobEmployeeServerResponseObject jobEmployeeServerObject = (JobEmployeeServerResponseObject) JobManager.getInstance().parseJSONobject(job_employees_json, new JobEmployeeServerResponseObject());
                                if (jobEmployeeServerObject != null)
                                {
                                    if (jobEmployeeServerObject.get_embedded() != null)
                                    {
                                        JobEmployee[] job_employees_arr = jobEmployeeServerObject.get_embedded().getJobemployees();

                                        job_employees = new HashMap<>();
                                        for (JobEmployee jobEmployee : job_employees_arr)
                                        {
                                            //check if bucket exists for Job
                                            if(job_employees.get(jobEmployee.getJob_id())!=null)
                                                job_employees.get(jobEmployee.getJob_id()).put(jobEmployee.get_id(), jobEmployee);//add JobEmployee to Jobs's bucket
                                            else //does not exist, create one
                                            {
                                                //init Job JobEmployee bucket
                                                HashMap employees = new HashMap<>();
                                                employees.put(jobEmployee.get_id(), jobEmployee);
                                                //put first item in bucket
                                                job_employees.put(jobEmployee.getJob_id(), employees);
                                            }
                                        }

                                        IO.log(getClass().getName(), IO.TAG_VERBOSE, "reloaded job assignees.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any JobEmployees in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any JobEmployees in the database.");

                                /*
                                //get Job Employees for each Job
                                if (jobs != null)
                                {
                                    for (Job job : jobs.values())
                                    {
                                        //Load JobEmployee objects using Job_id
                                        String job_employees_json = RemoteComms.get("/jobs/employees/" + job.get_id(), headers);
                                        JobEmployeeServerResponseObject jobEmployeeServerObject = gson.fromJson(job_employees_json, JobEmployeeServerResponseObject.class);
                                        if (jobEmployeeServerObject != null)
                                        {
                                            if(jobEmployeeServerObject.get_embedded()!=null)
                                            {
                                                JobEmployee[] job_employees_arr = jobEmployeeServerObject.get_embedded().getJobemployees();
                                                if (job_employees_arr != null)
                                                {
                                                    // make Employee[] of same size as JobEmployee[]
                                                    Employee[] employees_arr = new Employee[job_employees_arr.length];
                                                    // Load actual Employee objects from JobEmployee[] objects
                                                    int i = 0;
                                                    for (JobEmployee jobEmployee : job_employees_arr)
                                                        if (EmployeeManager.getInstance().getDataset() != null)
                                                            employees_arr[i++] = EmployeeManager.getInstance().getDataset().get(jobEmployee.getUsr());
                                                        else IO.log(getClass()
                                                                .getName(), IO.TAG_WARN, "no Employees found in database.");
                                                    // Set Employee objects on to Job object.
                                                    job.setAssigned_employees(employees_arr);
                                                } else IO.log(getClass()
                                                        .getName(), IO.TAG_WARN, "could not load assigned Employees for job #" + job
                                                        .get_id());

                                            } else IO.log(getClass()
                                                    .getName(), IO.TAG_WARN, "could not load assigned Employees for job #"
                                                    + job.get_id()+". Could not find any JobEmployee documents in collection.");
                                        } else IO.log(getClass().getName(), IO.TAG_WARN, "invalid JobEmployeeServerResponseObject for Job#"+job.get_id());
                                    }
                                } else IO.log(getClass().getName(), IO.TAG_WARN, "could not find any Jobs in the database.");*/

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of jobs.");

                                serialize(ROOT_PATH + filename, jobs);
                                if(jobs!=null)
                                    IO.log(getClass().getName(), IO.TAG_VERBOSE, "serialised ["+jobs.size()+"] jobs");

                                serialize(ROOT_PATH + "assignees.dat", job_employees);
                                if(job_employees!=null)
                                    IO.log(getClass().getName(), IO.TAG_VERBOSE, "serialised ["+job_employees.size()+"] job assignees");
                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                jobs = (HashMap<String, Job>) deserialize(ROOT_PATH + filename);
                                job_employees = (HashMap<String, HashMap<String, JobEmployee>>) deserialize(ROOT_PATH + "assignees.dat");
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "No valid active sessions found.", IO.TAG_ERROR);
                } catch (MalformedURLException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                } catch (ClassNotFoundException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                } catch (IOException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                }
                return null;
            }
        };
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
    public static void approveJob(Job job, Callback callback) throws IOException
    {
        //check if job is valid
        if(job==null)
        {
            IO.logAndAlert("Error: Invalid Job", "Selected Job object is invalid.", IO.TAG_ERROR);
            return;
        }
        //check if date job started is valid
        if(job.getDate_started()<=0)
        {
            IO.logAndAlert("Error: Job Start Date Invalid", "Selected Job has not been started yet.", IO.TAG_ERROR);
            return;
        }
        //check if date job completed is valid
        if(job.getDate_completed()<=0)
        {
            IO.logAndAlert("Error: Job Not Completed", "Selected Job has not been completed yet.", IO.TAG_ERROR);
            return;
        }
        //check if job date started isn't past the completion date
        if(job.getDate_started()>job.getDate_completed())
        {
            IO.logAndAlert("Error: Job Start Date Invalid", "Selected Job's start date is later than completion date.", IO.TAG_ERROR);
            return;
        }
        //check if signed in Employee is valid
        if(SessionManager.getInstance().getActiveEmployee()!=null)
        {
            IO.logAndAlert("Error", "Active user session is invalid.\nPlease sign in.", IO.TAG_ERROR);
            return;
        }
        //check if Employee authorised to approve Jobs
        if(SessionManager.getInstance().getActiveEmployee().getAccessLevel() < AccessLevel.SUPERUSER.getLevel())
        {
            IO.logAndAlert("Error", "You are not authorised to finalise jobs.", IO.TAG_ERROR);
            return;
        }
        //check if already finalised
        if(job.getStatus() == Job.STATUS_FINALISED)
        {
            IO.logAndAlert("Error", "Selected job has already been finalised.", IO.TAG_ERROR);
            return;
        }
        //check if archived
        if(job.getStatus() == Job.STATUS_ARCHIVED)
        {
            IO.logAndAlert("Error", "Selected job has been archived.", IO.TAG_ERROR);
            return;
        }

        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));

        job.setStatus(ApplicationObject.STATUS_FINALISED);

        JobManager.getInstance().patchObject(job, job_id->
        {
            if(job_id!=null)
            {
                IO.logAndAlert("Success", "Successfully finalised Job[" + job.getObject_number() + "]", IO.TAG_INFO);
                if (callback != null)
                    callback.call(null);
            } else IO.logAndAlert("Error", "Could not finalise Job[" + job.getObject_number() + "]", IO.TAG_ERROR);

            return null;
        });
    }

    public void requestJobApproval(Job job, Callback callback) throws IOException
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid Job.", IO.TAG_ERROR);
            return;
        }
        if(job.getQuote()==null)
        {
            IO.logAndAlert("Error", "Invalid Job->Quote.", IO.TAG_ERROR);
            return;
        }
        if(job.getQuote().getClient()==null)
        {
            IO.logAndAlert("Error", "Invalid Job->Quote->Client.", IO.TAG_ERROR);
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
        String path = PDF.createJobCardPdf(job);
        String base64_job = null;
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
                    base64_job = Base64.getEncoder().encodeToString(buffer);
                } else
                {
                    IO.logAndAlert(JobManager.class.getName(), "File [" + path + "] not found.", IO.TAG_ERROR);
                }
            } else
            {
                IO.log(JobManager.class.getName(), "File [" + path + "] object is null.", IO.TAG_ERROR);
            }
        } else IO.log(JobManager.class.getName(), "Could not get valid path for created Job pdf.", IO.TAG_ERROR);
        final String finalBase64_job = base64_job;

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Request Job ["+job.getObject_number()+"] Approval");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText("JOB ["+job.getObject_number()+"] APPROVAL REQUEST");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        //set default message
        Employee sender = SessionManager.getInstance().getActiveEmployee();
        String title = sender.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";;
        String def_msg = "Good day,\n\nCould you please assist me" +
                " by approving this job to be issued to "  + job.getQuote().getClient().getClient_name() + ".\nThank you.\n\nBest Regards,\n"
                + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
        txt_message.setText(def_msg);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));//multipart/form-data
                headers.add(new AbstractMap.SimpleEntry<>("job_id", job.get_id()));
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

                Metafile metafile = new Metafile("job_"+job.get_id()+".pdf","application/pdf");
                metafile.setFile(finalBase64_job);
                HttpURLConnection connection = RemoteComms.post("/job/approval_request", metafile.getJSONString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully requested Job approval!", IO.TAG_INFO);
                        //execute callback w/ args
                        if(callback!=null)
                            callback.call(IO.readStream(connection.getInputStream()));
                        return;
                    } else
                    {
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
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
                    if(Desktop.isDesktopSupported())
                    {
                        Desktop.getDesktop().open(new File(path));
                    }else IO.logAndAlert("Error", "This environment not supported.", IO.TAG_ERROR);
                    /*PDFViewer pdfViewer = PDFViewer.getInstance();
                    pdfViewer.setVisible(true);
                    pdfViewer.doOpen(path);*/
                } else IO.log("JobManager", IO.TAG_ERROR, "could not get a valid path for generated Job[#"+job.getObject_number()+"] card PDF.");
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        }else{
            IO.log(TAG, IO.TAG_ERROR, "Job object is null");
        }
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            //headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            //update signed job property
                            //TODO: if access level is sufficient, sign it off once
                            //TODO: job.setSigned_job(Base64.getEncoder().encodeToString(buffer));

                            //TODO:
                            RemoteComms.uploadFile("/file/upload" + job.get_id(), headers, buffer);
                            IO.logAndAlert("Success", "successfully uploaded signed job [#"+job.getObject_number()+"], file size: [" + buffer.length + "] bytes.", IO.TAG_INFO);
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            RemoteComms.uploadFile("/file/upload/" + job_id, headers, buffer);
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

    class JobServerResponseObject extends ServerResponseObject
    {
        private JobServerResponseObject.Embedded _embedded;

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
            private Job[] jobs;

            public Job[] getJobs()
            {
                return jobs;
            }

            public void setJobs(Job[] jobs)
            {
                this.jobs = jobs;
            }
        }
    }

    class JobEmployeeServerResponseObject extends ServerResponseObject
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
            private JobEmployee[] jobemployees;

            public JobEmployee[] getJobemployees()
            {
                return jobemployees;
            }

            public void setJobemployees(JobEmployee[] jobemployees)
            {
                this.jobemployees = jobemployees;
            }
        }
    }
}