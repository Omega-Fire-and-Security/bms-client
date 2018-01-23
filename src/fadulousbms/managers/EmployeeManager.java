package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.controllers.JobsController;
import fadulousbms.model.*;
import fadulousbms.model.Error;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 */
public class EmployeeManager extends BusinessObjectManager
{
    private HashMap<String, Employee> employees;
    private Gson gson;
    private static EmployeeManager employeeManager = new EmployeeManager();
    public static String[] access_levels = {"NONE", "NORMAL", "ADMIN", "SUPER"};
    public static String[] sexes = {"MALE", "FEMALE"};

    private EmployeeManager()
    {
    }

    public HashMap<String, Employee> getEmployees(){return this.employees;}

    public static EmployeeManager getInstance()
    {
        return employeeManager;
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
            if(employees==null)
                reloadDataFromServer();
            else IO.log(getClass().getName(), IO.TAG_INFO, "clients object has already been set.");
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
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if(smgr.getActive()!=null)
            {
                if(!smgr.getActive().isExpired())
                {
                    gson  = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                    String employee_json_object = RemoteComms.sendGetRequest("/employees", headers);
                    EmployeeServerObject employeeServerObject = gson.fromJson(employee_json_object, EmployeeServerObject.class);
                    //Employee employeeObject = gson.fromJson(employees_json, Employee.class);
                    //System.out.println("Embedded: "+employeeObject.get_embedded());
                    if(employeeServerObject!=null)
                    {
                        if(employeeServerObject.get_embedded()!=null)
                        {
                            Employee[] users = employeeServerObject.get_embedded().get_employees();
                            /*System.out.println("Employee count: " + employeeServerObject.getPage().getTotalElements());
                            System.out
                                    .println("Employee link: " + employeeServerObject.get_links().getSelf().getHref());*/

                            employees = new HashMap();
                            for (Employee employee : users)
                                employees.put(employee.getUsr(), employee);
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Employees in database.");
                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "EmployeeServerObject (containing Employee objects & other metadata) is null");
                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded employee collection.");
                } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            } else IO.logAndAlert("Session Expired", "Active session is invalid", IO.TAG_ERROR);
        } catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    public void newExternalEmployeeWindow(String title, Callback callback)
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error", "Invalid active session.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error", "Active session has expired.", IO.TAG_ERROR);
            return;
        }
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - " + title);
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.centerOnScreen();

        final TextField txtFirstname,txtLastname,txtEmail,txtTelephone,txtCellphone;
        final TextArea txtOther;

        VBox vbox = new VBox(1);

        txtFirstname = new TextField();
        txtFirstname.setMinWidth(200);
        txtFirstname.setMaxWidth(Double.MAX_VALUE);
        HBox first_name = CustomTableViewControls.getLabelledNode("First Name", 200, txtFirstname);

        txtLastname = new TextField();
        txtLastname.setMinWidth(200);
        txtLastname.setMaxWidth(Double.MAX_VALUE);
        HBox last_name = CustomTableViewControls.getLabelledNode("Last Name", 200, txtLastname);

        txtEmail = new TextField();
        txtEmail.setMinWidth(200);
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        HBox email = CustomTableViewControls.getLabelledNode("eMail Address:", 200, txtEmail);

        txtTelephone = new TextField();
        txtTelephone.setMinWidth(200);
        txtTelephone.setMaxWidth(Double.MAX_VALUE);
        HBox telephone = CustomTableViewControls.getLabelledNode("Telephone #: ", 200, txtTelephone);

        txtCellphone = new TextField();
        txtCellphone.setMinWidth(200);
        txtCellphone.setMaxWidth(Double.MAX_VALUE);
        HBox cellphone = CustomTableViewControls.getLabelledNode("Cellphone #: ", 200, txtCellphone);

        txtOther = new TextArea();
        txtOther.setMinWidth(200);
        txtOther.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other: ", 200, txtOther);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(!validateFormField(txtFirstname, "Invalid Firstname", "please enter a valid first name", "^.*(?=.{1,}).*"))
                return;
            if(!validateFormField(txtLastname, "Invalid Lastname", "please enter a valid last name", "^.*(?=.{1,}).*"))
                return;
            if(!validateFormField(txtEmail, "Invalid Email", "please enter a valid email address", "^.*(?=.{5,})(?=(.*@.*\\.)).*"))
                return;
            if(!validateFormField(txtTelephone, "Invalid Telephone Number", "please enter a valid telephone number", "^.*(?=.{10,}).*"))
                return;
            if(!validateFormField(txtCellphone, "Invalid Cellphone Number", "please enter a valid cellphone number", "^.*(?=.{10,}).*"))
                return;

            //all valid, send data to server
            int access_lvl=0;

            /*ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("usr", txtEmail.getText()));
            params.add(new AbstractMap.SimpleEntry<>("pwd", txtTelephone.getText()));
            params.add(new AbstractMap.SimpleEntry<>("access_level", String.valueOf(access_lvl)));
            params.add(new AbstractMap.SimpleEntry<>("firstname", txtFirstname.getText()));
            params.add(new AbstractMap.SimpleEntry<>("lastname", txtLastname.getText()));
            params.add(new AbstractMap.SimpleEntry<>("gender", "female"));
            params.add(new AbstractMap.SimpleEntry<>("email", txtEmail.getText()));
            params.add(new AbstractMap.SimpleEntry<>("tel", txtTelephone.getText()));
            params.add(new AbstractMap.SimpleEntry<>("cell", txtCellphone.getText()));*/

            Employee employee = new Employee();
            employee.setUsr(txtEmail.getText());
            employee.setPwd(txtCellphone.getText());//TODO: hash
            employee.setAccessLevel(access_lvl);
            employee.setFirstname(txtFirstname.getText());
            employee.setLastname(txtLastname.getText());
            employee.setGender("male");
            employee.setEmail(txtEmail.getText());
            employee.setTel(txtTelephone.getText());
            employee.setCell(txtCellphone.getText());
            employee.setCreator(SessionManager.getInstance().getActive().getUsr());

            if(txtOther.getText()!=null)
                employee.setOther(txtOther.getText());

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));

                HttpURLConnection connection = RemoteComms.putJSON("/employees", employee.toString(), headers);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.logAndAlert("Account Creation Success", "Successfully created new user!", IO.TAG_INFO);
                    if(callback!=null)
                        callback.call(null);
                } else
                    IO.logAndAlert("Account Creation Failure", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

                connection.disconnect();
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });
        //Add form controls vertically on the stage
        vbox.getChildren().add(first_name);
        vbox.getChildren().add(last_name);
        vbox.getChildren().add(email);
        vbox.getChildren().add(telephone);
        vbox.getChildren().add(cellphone);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
    }

    public void newEmployeeWindow(Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Create New Employee [User]");
        stage.setMinWidth(320);
        stage.setMinHeight(350);
        stage.setHeight(500);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);
        stage.centerOnScreen();

        final ComboBox cbxSex,cbxAccessLevel;
        final TextField txtUsername,txtPassword,txtFirstname,txtLastname,txtEmail,txtTelephone,txtCellphone;
        final TextArea txtOther;

        VBox vbox = new VBox(1);

        txtUsername = new TextField();
        txtUsername.setMinWidth(200);
        txtUsername.setMaxWidth(Double.MAX_VALUE);
        HBox username = CustomTableViewControls.getLabelledNode("Username", 200, txtUsername);

        txtPassword = new TextField();
        txtPassword.setMinWidth(200);
        txtPassword.setMaxWidth(Double.MAX_VALUE);
        HBox password = CustomTableViewControls.getLabelledNode("Password", 200, txtPassword);

        txtFirstname = new TextField();
        txtFirstname.setMinWidth(200);
        txtFirstname.setMaxWidth(Double.MAX_VALUE);
        HBox first_name = CustomTableViewControls.getLabelledNode("First Name", 200, txtFirstname);

        txtLastname = new TextField();
        txtLastname.setMinWidth(200);
        txtLastname.setMaxWidth(Double.MAX_VALUE);
        HBox last_name = CustomTableViewControls.getLabelledNode("Last Name", 200, txtLastname);

        txtEmail = new TextField();
        txtEmail.setMinWidth(200);
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        HBox email = CustomTableViewControls.getLabelledNode("eMail Address:", 200, txtEmail);

        txtTelephone = new TextField();
        txtTelephone.setMinWidth(200);
        txtTelephone.setMaxWidth(Double.MAX_VALUE);
        HBox telephone = CustomTableViewControls.getLabelledNode("Telephone #: ", 200, txtTelephone);

        txtCellphone = new TextField();
        txtCellphone.setMinWidth(200);
        txtCellphone.setMaxWidth(Double.MAX_VALUE);
        HBox cellphone = CustomTableViewControls.getLabelledNode("Cellphone #: ", 200, txtCellphone);

        txtOther = new TextArea();
        txtOther.setMinWidth(200);
        txtOther.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other: ", 200, txtOther);

        cbxSex = new ComboBox(FXCollections.observableArrayList(sexes));
        cbxSex.setMinWidth(200);
        cbxSex.setMaxWidth(Double.MAX_VALUE);
        HBox sex = CustomTableViewControls.getLabelledNode("Sex: ", 200, cbxSex);

        cbxAccessLevel = new ComboBox(FXCollections.observableArrayList(access_levels));
        cbxAccessLevel.setMinWidth(200);
        cbxAccessLevel.setMaxWidth(Double.MAX_VALUE);
        HBox access_level = CustomTableViewControls.getLabelledNode("Acccess Level: ", 200, cbxAccessLevel);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Create Employee", event ->
        {
            int sex_index = cbxSex.getSelectionModel().selectedIndexProperty().get();

            if(!validateFormField(txtUsername, "Invalid Username", "please enter a valid username", "^.*(?=.{5,}).*"))
                return;
            if(!validateFormField(txtPassword, "Invalid Password", "please enter a valid password", "^.*(?=.{8,}).*"))//(?=[a-zA-Z])(?=.*[0-9])(?=.*[@#!$%^&*-+=])
                return;
            if(!validateFormField(txtFirstname, "Invalid Firstname", "please enter a valid first name", "^.*(?=.{1,}).*"))
                return;
            if(!validateFormField(txtLastname, "Invalid Lastname", "please enter a valid last name", "^.*(?=.{1,}).*"))
                return;

            if(sex_index<0)
            {
                cbxSex.getStyleClass().remove("form-control-default");
                cbxSex.getStyleClass().add("control-input-error");
            }else{
                cbxSex.getStyleClass().remove("control-input-error");
                cbxSex.getStyleClass().add("form-control-default");
            }
            if(!validateFormField(txtEmail, "Invalid Email", "please enter a valid email address", "^.*(?=.{5,})(?=(.*@.*\\.)).*"))
                return;
            if(!validateFormField(txtTelephone, "Invalid Telephone Number", "please enter a valid telephone number", "^.*(?=.{10,}).*"))
                return;
            if(!validateFormField(txtCellphone, "Invalid Cellphone Number", "please enter a valid cellphone number", "^.*(?=.{10,}).*"))
                return;

            //all valid, send data to server
            int access_level_index = cbxAccessLevel.getSelectionModel().getSelectedIndex();
            if(access_level_index>=0)
            {
                int access_lvl=0;
                if(access_levels[access_level_index].toLowerCase().equals("super"))
                    access_lvl=3;
                if(access_levels[access_level_index].toLowerCase().equals("admin"))
                    access_lvl=2;
                if(access_levels[access_level_index].toLowerCase().equals("normal"))
                    access_lvl=1;
                if(access_levels[access_level_index].toLowerCase().equals("none"))
                    access_lvl=0;

                ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
                params.add(new AbstractMap.SimpleEntry<>("usr", txtUsername.getText()));
                params.add(new AbstractMap.SimpleEntry<>("pwd", txtPassword.getText()));
                params.add(new AbstractMap.SimpleEntry<>("access_level", String.valueOf(access_lvl)));
                params.add(new AbstractMap.SimpleEntry<>("firstname", txtFirstname.getText()));
                params.add(new AbstractMap.SimpleEntry<>("lastname", txtLastname.getText()));
                params.add(new AbstractMap.SimpleEntry<>("gender", cbxSex.getItems().get(sex_index).toString()));
                params.add(new AbstractMap.SimpleEntry<>("email", txtEmail.getText()));
                params.add(new AbstractMap.SimpleEntry<>("tel", txtTelephone.getText()));
                params.add(new AbstractMap.SimpleEntry<>("cell", txtCellphone.getText()));
                if(txtOther.getText()!=null)
                    params.add(new AbstractMap.SimpleEntry<>("other", txtOther.getText()));

                try
                {
                    HttpURLConnection connection = RemoteComms.postData("/employee/add", params, null);
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Account Creation Success", "Successfully created a new employee!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    } else
                        IO.logAndAlert("Account Creation Failure", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

                    connection.disconnect();
                }catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
        });
        //Add form controls vertically on the stage
        vbox.getChildren().add(username);
        vbox.getChildren().add(password);
        vbox.getChildren().add(first_name);
        vbox.getChildren().add(last_name);
        vbox.getChildren().add(sex);
        vbox.getChildren().add(email);
        vbox.getChildren().add(telephone);
        vbox.getChildren().add(cellphone);
        vbox.getChildren().add(access_level);
        vbox.getChildren().add(other);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
    }

    private boolean validateFormField(TextField txt, String errTitle, String errMsg, String regex)
    {
        if(!Validators.isValidNode(txt, txt.getText(), regex))
        {
            //IO.logAndAlert(errTitle, errMsg, IO.TAG_ERROR);
            IO.log(getClass().getName(), IO.TAG_ERROR, errMsg);
            return false;
        }
        return true;
    }

    public void uploadID(String employee_id)
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

                            RemoteComms.uploadFile("/employee/id/upload/" + employee_id, headers, buffer);
                            IO.log(getClass().getName(), IO.TAG_INFO, "\n uploaded ID for employee ["+employee_id+"], file size: [" + buffer.length + "] bytes.");
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

    public void uploadCV(String employee_id)
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

                            RemoteComms.uploadFile("/employee/cv/upload/" + employee_id, headers, buffer);
                            IO.log(getClass().getName(), IO.TAG_INFO, "\n uploaded CV for employee ["+employee_id+"], file size: [" + buffer.length + "] bytes.");
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

    public static void viewID(String employee_id)
    {
        if(employee_id==null)
        {
            IO.logAndAlert("Error", "Invalid employee identifier object passed.", IO.TAG_ERROR);
            return;
        }

        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

                    //String filename = String.valueOf(bo.get(property));
                    long start = System.currentTimeMillis();
                    byte[] file = RemoteComms.sendFileRequest("/employee/id/" + employee_id, headers);

                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        //String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                        String local_filename = employee_id + "_id.pdf";
                        /*if (new File("out/" + local_filename).exists())
                            Files.delete(new File("out/" + local_filename).toPath());*/
                        //TODO: fix this hack
                        int i = 1;
                        File f = new File("out/" + local_filename);
                        if (f.exists())
                        {
                            if (f.delete())
                                IO.log(JobsController.class.getName(), IO.TAG_INFO, "deleted file [" + f
                                        .getAbsolutePath() + "]");
                            else
                            {
                                IO.log(EmployeeManager.class.getName(), IO.TAG_WARN, "could not delete file ["+f.getAbsolutePath()+"]");
                                //get new filename
                                while((f=new File("out/"+local_filename)).exists())
                                {
                                    local_filename = employee_id + "_id." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File("out/" + local_filename));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded employee ID document [" + employee_id
                                +"] to path [out/" + local_filename + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen("out/" + local_filename);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader Error", "Employee ["+employee_id
                                +"] ID document file could not be downloaded because the active session has expired.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("File Downloader Error", "Employee ["+employee_id+"] ID document file could not be downloaded.", IO.TAG_ERROR);
                    //IO.logAndAlert("Error", "Could not download ID document for employee ["+employee_id+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void viewCV(String employee_id)
    {
        if(employee_id==null)
        {
            IO.logAndAlert("Error", "Invalid employee identifier object passed.", IO.TAG_ERROR);
            return;
        }

        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

                    //String filename = String.valueOf(bo.get(property));
                    long start = System.currentTimeMillis();
                    byte[] file = RemoteComms.sendFileRequest("/employee/cv/" + employee_id, headers);

                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        //String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                        String local_filename = employee_id + "_cv.pdf";
                        /*if (new File("out/" + local_filename).exists())
                            Files.delete(new File("out/" + local_filename).toPath());*/
                        //TODO: fix this hack
                        int i = 1;
                        File f = new File("out/" + local_filename);
                        if (f.exists())
                        {
                            if (f.delete())
                                IO.log(JobsController.class.getName(), IO.TAG_INFO, "deleted file [" + f
                                        .getAbsolutePath() + "]");
                            else
                            {
                                IO.log(EmployeeManager.class.getName(), IO.TAG_WARN, "could not delete file ["+f.getAbsolutePath()+"]");
                                //get new filename
                                while((f=new File("out/"+local_filename)).exists())
                                {
                                    local_filename = employee_id + "_cv." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File("out/" + local_filename));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded employee CV document [" + employee_id
                                +"] to path [out/" + local_filename + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen("out/" + local_filename);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader Error", "Employee ["+employee_id
                                +"] CV document file could not be downloaded because the active session has expired.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("File Downloader Error", "Employee ["+employee_id+"] CV document file could not be downloaded.", IO.TAG_ERROR);
                    //IO.logAndAlert("Error", "Could not download ID document for employee ["+employee_id+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    class EmployeeServerObject extends ServerObject
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
            private Employee[] employees;

            public Employee[] get_employees()
            {
                return employees;
            }

            public void set_employees(Employee[] employees)
            {
                this.employees = employees;
            }
        }
    }
}
