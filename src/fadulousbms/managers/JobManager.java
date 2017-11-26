package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.controllers.HomescreenController;
import fadulousbms.controllers.OperationsController;
import fadulousbms.model.*;
import fadulousbms.model.Error;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    public HashMap<String, Job> getJobs()
    {
        return this.jobs;
    }

    public void setSelectedJob(Job job)
    {
        this.selected_job = job;
        if(selected_job!=null)
            IO.log(getClass().getName(), IO.TAG_INFO, "set selected job to: " + job);
        //}else IO.log(getClass().getName(), IO.TAG_ERROR, "job to be set as selected is null.");
    }

    public void nullifySelected()
    {
        this.selected_job=null;
    }

    public void setSelectedJob(String job_id)
    {
        if(jobs==null)
        {
            IO.logAndAlert(getClass().getName(), IO.TAG_ERROR, "No jobs were found on the database.");
            return;
        }
        if(jobs.get(job_id)!=null)
        {
            setSelectedJob(jobs.get(job_id));
        }
    }

    public Job getSelectedJob()
    {
        return selected_job;
    }

    public boolean createJob(Job job)
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
            }else IO.logAndAlert("Job Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert("Jobs Manager", e.getMessage(), IO.TAG_ERROR);
        }
        return false;
    }

    public String createNewJob(Job job)
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
            IO.logAndAlert("Jobs Manager", e.getMessage(), IO.TAG_ERROR);
        }
        return null;
    }

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

    public void viewJobSafetyCatalogue()
    {
        /*if(jobs !=null && tblJobs!=null)
        {
            int selected = tblJobs.getSelectionModel().selectedIndexProperty().get();
            if(selected >= 0 && selected < jobs.length)
            {
                showJobSafetyFile(jobs[selected]);
            } else IO.log(TAG, IO.TAG_ERROR, "viewJobSafetyCatalogue> index out of bounds.");
        }else IO.log(TAG, IO.TAG_ERROR, "viewJobSafetyCatalogue> jobs array or TableView is null.");*/
    }

    public void showJobSafetyFile(Job job)
    {
        if(job !=null)
        {
            SafetyManager.listSafetyDocuments(job.getSafety_catalogue(), job.get_id());
        }else IO.log(TAG, IO.TAG_ERROR, "showJobSafetyFile> job object is null.");
    }

    public void jobReps()
    {
        /*int selected_index = tblJobs.getSelectionModel().selectedIndexProperty().get();
        if (selected_index >= 0 && selected_index < jobs.length)
        {
            showJobReps(jobs[selected_index]);
        }else IO.log(TAG, IO.TAG_ERROR, "jobs array index out of bounds: " + selected_index);*/
    }

    public void showJobReps(Job job)
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

    public void handleNewJobCard()
    {
        /*if(jobs==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "jobs array is null.");
            return;
        }
        int selected = tblJobs.getSelectionModel().selectedIndexProperty().get();
        if(selected>=0 && selected<jobs.length)
        {
            showJobCard(jobs[selected]);
        }else {
            IO.log(TAG, IO.TAG_ERROR, "selected job index is out of bounds.");
        }*/
    }

    public static void showJobCard(Job job)
    {
        if(job!=null)
        {
            try
            {
                PDF.createJobCardPdf(job);
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        }else{
            IO.log(TAG, IO.TAG_ERROR, "Job object is null");
        }
    }

    public void handleNewJobRep(Stage parentStage)
    {
        /*if(tblJobs==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "jobs table is null!");
            return;
        }

        if(employees==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "no employees were found in the database.");
            return;
        }

        int selected_index = tblJobs.getSelectionModel().selectedIndexProperty().get();

        if(jobs!=null)
        {
            if (selected_index < 0 || selected_index >= jobs.length)
            {
                IO.log(TAG, IO.TAG_ERROR, "jobs array index is out of bounds");
                return;
            }
        }else{
            IO.log(TAG, IO.TAG_ERROR, "jobs array is null!");
            return;
        }
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add new job representative");
        stage.setMinWidth(320);
        stage.setMinHeight(120);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        //employee combo box
        final ComboBox<Employee> cbx_employee = new ComboBox<>();
        cbx_employee.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>()
        {
            @Override
            public ListCell<Employee> call(ListView<Employee> lst_reps)
            {
                return new ListCell<Employee>()
                {
                    @Override
                    protected void updateItem(Employee employee, boolean empty)
                    {
                        super.updateItem(employee, empty);
                        if(employee!=null && !empty)
                        {
                            setText(employee.getFirstname() + " " + employee.getLastname());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_employee.setButtonCell(new ListCell<Employee>()
        {
            @Override
            protected void updateItem(Employee employee, boolean empty)
            {
                super.updateItem(employee, empty);
                if(employee!=null && !empty)
                {
                    setText(employee.getFirstname() + " " + employee.getLastname());
                }else{
                    setText("");
                }
            }
        });

        cbx_employee.setItems(FXCollections.observableArrayList(employees));
        cbx_employee.setMinWidth(200);
        cbx_employee.setMaxWidth(Double.MAX_VALUE);
        HBox employee = CustomTableViewControls.getLabelledNode("Employee", 200, cbx_employee);

        DatePicker dpk_date_assigned = new DatePicker();
        dpk_date_assigned.setMinWidth(200);
        dpk_date_assigned.setMaxWidth(Double.MAX_VALUE);
        HBox date_assigned = CustomTableViewControls.getLabelledNode("Date assigned", 200, dpk_date_assigned);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Add", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(cbx_employee.getValue()!=null)
            {
                if (!Validators.isValidNode(cbx_employee, cbx_employee.getValue().get_id() == null ? "" : cbx_employee.getValue().get_id(), 1, ".+"))
                    return;
            }
            else
            {
                Validators.isValidNode(cbx_employee, "", 1, ".+");
                return;
            }
            if(!Validators.isValidNode(dpk_date_assigned, dpk_date_assigned.getValue()==null?"":dpk_date_assigned.getValue().toString(), 4, date_regex))
                return;

            long date_generated_in_sec=0;
            if(dpk_date_assigned.getValue()!=null)
                date_generated_in_sec = dpk_date_assigned.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();


            //String str_employee_usr = (String)cbx_employee.getValue().get("usr");
            Employee e = cbx_employee.getValue();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //Mandatory
            params.add(new AbstractMap.SimpleEntry<>("job_id", jobs[selected_index].get_id()));
            params.add(new AbstractMap.SimpleEntry<>("usr", e.getUsr()));
            params.add(new AbstractMap.SimpleEntry<>("date_assigned", String.valueOf(date_generated_in_sec)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/job/employees/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added a new job representative!", IO.TAG_INFO);
                    }else
                    {
                        //Get error message
                        String msg = IO.readStream(connection.getErrorStream());
                        Gson gson = new GsonBuilder().create();
                        Error error = gson.fromJson(msg, Error.class);
                        IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), error.getError(), IO.TAG_ERROR);
                    }
                }
            } catch (IOException ex)
            {
                IO.logAndAlert(TAG, ex.getMessage(), IO.TAG_ERROR);
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(employee);
        vbox.getChildren().add(date_assigned);
        vbox.getChildren().add(submit);

        //Setup scene and display
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setOnCloseRequest(event ->
        {
            IO.log(TAG, IO.TAG_INFO, "reloading local data.");
            loadDataFromServer();
        });

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);*/
    }

    public void handleNewJob(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Job");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        /*final TextField txt_job_name = new TextField();
        txt_job_name.setMinWidth(200);
        txt_job_name.setMaxWidth(Double.MAX_VALUE);
        HBox job_name = getLabelledNode("Job name", 200, txt_job_name);*/

        final TextField txt_job_description = new TextField();
        txt_job_description.setMinWidth(200);
        txt_job_description.setMaxWidth(Double.MAX_VALUE);
        HBox job_description = CustomTableViewControls.getLabelledNode("Job description", 200, txt_job_description);

        final ComboBox<Client> cbx_client_id = new ComboBox<>();
        //cbx_client_id.setCellFactory(ComboBoxListCell.forListView(clients));// new PropertyValueFactory<Client, String>(""));
        cbx_client_id.setCellFactory(new Callback<ListView<Client>, ListCell<Client>>()
        {
            @Override
            public ListCell<Client> call(ListView<Client> lst_clients)
            {
                return new ListCell<Client>()
                {
                    @Override
                    protected void updateItem(Client client, boolean empty)
                    {
                        super.updateItem(client, empty);
                        if(client!=null && !empty)
                        {
                            setText(client.getClient_name());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_client_id.setButtonCell(new ListCell<Client>()
        {
            @Override
            protected void updateItem(Client client, boolean empty)
            {
                super.updateItem(client, empty);
                if(client!=null && !empty)
                {
                    setText(client.getClient_name());
                }else{
                    setText("");
                }
            }
        });
        //cbx_client_id.setItems(FXCollections.observableArrayList(clients));
        cbx_client_id.setMinWidth(200);
        cbx_client_id.setMaxWidth(Double.MAX_VALUE);
        HBox client_id = CustomTableViewControls.getLabelledNode("Client", 200, cbx_client_id);

        DatePicker dpk_planned_start_date = new DatePicker();
        dpk_planned_start_date.setMinWidth(200);
        dpk_planned_start_date.setMaxWidth(Double.MAX_VALUE);
        HBox planned_start_date = CustomTableViewControls.getLabelledNode("Planned start date", 200, dpk_planned_start_date);

        DatePicker dpk_date_logged = new DatePicker();
        dpk_date_logged.setMinWidth(200);
        dpk_date_logged.setMaxWidth(Double.MAX_VALUE);
        HBox date_logged = CustomTableViewControls.getLabelledNode("Date logged", 200, dpk_date_logged);

        DatePicker dpk_date_assigned = new DatePicker();
        dpk_date_assigned.setMinWidth(200);
        dpk_date_assigned.setMaxWidth(Double.MAX_VALUE);
        HBox date_assigned = CustomTableViewControls.getLabelledNode("Date assigned", 200, dpk_date_assigned);

        DatePicker dpk_date_started = new DatePicker();
        dpk_date_started.setMinWidth(200);
        dpk_date_started.setMaxWidth(Double.MAX_VALUE);
        HBox date_started = CustomTableViewControls.getLabelledNode("Date started", 200, dpk_date_started);

        DatePicker dpk_date_ended = new DatePicker();
        dpk_date_ended.setMinWidth(200);
        dpk_date_ended.setMaxWidth(Double.MAX_VALUE);
        HBox date_ended = CustomTableViewControls.getLabelledNode("Date ended", 200, dpk_date_ended);

        /*final TextField txt_invoice_id = new TextField();
        txt_invoice_id.setMinWidth(200);
        txt_invoice_id.setMaxWidth(Double.MAX_VALUE);
        HBox invoice_id = getLabelledNode("Invoice", 200, txt_invoice_id);*/

        final ComboBox<Invoice> cbx_invoice_id = new ComboBox<>();
        cbx_invoice_id.setCellFactory(new Callback<ListView<Invoice>, ListCell<Invoice>>()
        {
            @Override
            public ListCell<Invoice> call(ListView<Invoice> lst_invoices)
            {
                return new ListCell<Invoice>()
                {
                    @Override
                    protected void updateItem(Invoice invoice, boolean empty)
                    {
                        super.updateItem(invoice, empty);
                        if(invoice!=null && !empty)
                        {
                            setText(invoice.getShort_id());
                        }else{
                            setText("");
                        }
                    }
                };
            }
        });
        cbx_invoice_id.setButtonCell(new ListCell<Invoice>()
        {
            @Override
            protected void updateItem(Invoice invoice, boolean empty)
            {
                super.updateItem(invoice, empty);
                if(invoice!=null && !empty)
                {
                    setText(invoice.getShort_id());
                }else
                {
                    setText("");
                }
            }
        });
        //cbx_invoice_id.setItems(FXCollections.observableArrayList(invoices));
        cbx_invoice_id.setMinWidth(200);
        cbx_invoice_id.setMaxWidth(Double.MAX_VALUE);
        HBox invoice_id = CustomTableViewControls.getLabelledNode("Invoice ID", 200, cbx_invoice_id);


        CheckBox chbx_job_completed = new CheckBox();
        HBox job_completed = CustomTableViewControls.getLabelledNode("Completed", 200, chbx_job_completed);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_job_description, txt_job_description.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(cbx_client_id, cbx_client_id.getValue()==null?"":cbx_client_id.getValue().get_id(), 1, ".+"))
                return;
            //if(!Validators.isValidNode(cbx_invoice_id, cbx_invoice_id.getValue()==null?"":cbx_invoice_id.getValue().get_id(), 1, ".+"))
                //return;
            if(!Validators.isValidNode(dpk_date_logged, dpk_date_logged.getValue()==null?"":dpk_date_logged.getValue().toString(), 4, date_regex))
                return;
            /*if(!Validators.isValidNode(dpk_planned_start_date, dpk_planned_start_date.getValue()==null?"":dpk_planned_start_date.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(dpk_date_assigned, dpk_date_assigned.getValue()==null?"":dpk_date_assigned.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(dpk_date_started, dpk_date_started.getValue()==null?"":dpk_date_started.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(dpk_date_ended, dpk_date_ended.getValue()==null?"":dpk_date_ended.getValue().toString(), 4, date_regex))
                return;*/

            long planned_start_date_in_sec=0, date_logged_in_sec=0, date_assigned_in_sec=0, date_started_in_sec=0, date_ended_in_sec=0;
            String str_job_description = txt_job_description.getText();
            String str_client_id = cbx_client_id.getValue().get_id();
            String str_invoice_id = cbx_client_id.getValue().get_id();
            date_logged_in_sec = dpk_date_logged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_planned_start_date.getValue()!=null)
                planned_start_date_in_sec = dpk_planned_start_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_date_assigned.getValue()!=null)
                date_assigned_in_sec = dpk_date_assigned.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_date_started.getValue()!=null)
                date_started_in_sec = dpk_date_started.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            if(dpk_date_ended.getValue()!=null)
                date_ended_in_sec = dpk_date_ended.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            boolean is_complete = chbx_job_completed.selectedProperty().get();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            //Mandatory
            params.add(new AbstractMap.SimpleEntry<>("job_description", str_job_description));
            params.add(new AbstractMap.SimpleEntry<>("client_id", str_client_id));
            params.add(new AbstractMap.SimpleEntry<>("date_logged", String.valueOf(date_logged_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("invoice_id", str_invoice_id));
            params.add(new AbstractMap.SimpleEntry<>("job_completed", String.valueOf(is_complete)));
            //params.add(new AbstractMap.SimpleEntry<>("job_number", String.valueOf(jobs==null?0:jobs.length)));
            //Optional
            if(planned_start_date_in_sec>0)
                params.add(new AbstractMap.SimpleEntry<>("planned_start_date", String.valueOf(planned_start_date_in_sec)));
            if(date_assigned_in_sec>0)
                params.add(new AbstractMap.SimpleEntry<>("date_assigned", String.valueOf(date_assigned_in_sec)));
            if(date_started_in_sec>0)
                params.add(new AbstractMap.SimpleEntry<>("date_started", String.valueOf(date_started_in_sec)));
            if(date_ended_in_sec>0)
                params.add(new AbstractMap.SimpleEntry<>("date_completed", String.valueOf(date_ended_in_sec)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/job/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new job!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        JOptionPane.showMessageDialog(null, connection.getResponseCode(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (IOException e)
            {
                IO.log(TAG, IO.TAG_ERROR, e.getMessage());
            }
        });
        //Add form controls vertically on the scene
        vbox.getChildren().add(job_description);
        vbox.getChildren().add(client_id);
        vbox.getChildren().add(date_logged);
        vbox.getChildren().add(planned_start_date);
        vbox.getChildren().add(date_assigned);
        vbox.getChildren().add(date_started);
        vbox.getChildren().add(date_ended);
        vbox.getChildren().add(invoice_id);
        vbox.getChildren().add(job_completed);
        vbox.getChildren().add(submit);

        //Setup scene and display
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }

    public void uploadSigned(String job_id)
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
                            IO.log(getClass().getName(), IO.TAG_INFO, "\n uploaded signed job ["+job_id+"], file size: [" + buffer.length + "] bytes.");
                        } else
                        {
                            IO.logAndAlert(getClass().getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(getClass().getName(), "File object is null.", IO.TAG_ERROR);
                    }
                }catch (IOException e)
                {
                    IO.log(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void emailSignedJobCard(String job_id, Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail Signed Job Card ["+job_id+"]");
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
            params.add(new AbstractMap.SimpleEntry<>("job_id", job_id));
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

    public void emailJobCard(Job job, Callback callback)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid Job.", IO.TAG_ERROR);
            return;
        }
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
                String path = PDF.createJobCardPdf(job);
                if(path!=null)
                {
                    File f = new File(path);
                    if(f.exists())
                    {
                        byte[] file_arr = new byte[(int)f.length()];
                        FileInputStream in = new FileInputStream(f);
                        in.read(file_arr, 0, (int)f.length());
                        params.add(new AbstractMap.SimpleEntry<>("attachment", Base64.getEncoder().encodeToString(file_arr)));

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
                    }
                }
            } catch (IOException e)
            {
                e.printStackTrace();
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
