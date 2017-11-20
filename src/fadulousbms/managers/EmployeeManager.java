package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
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
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.File;
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
    //private Employee[] employees;
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
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSessionId()));

                    String employees_json = RemoteComms.sendGetRequest("/api/employees", headers);
                    Employee[] users = gson.fromJson(employees_json, Employee[].class);

                    employees = new HashMap();
                    for(Employee employee: users)
                        employees.put(employee.getUsr(), employee);

                    IO.log(getClass().getName(), IO.TAG_INFO, "reloaded employee collection.");
                }else{
                    JOptionPane.showMessageDialog(null, "Active session has expired.", "Session Expired", JOptionPane.ERROR_MESSAGE);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions.", "Session Expired", JOptionPane.ERROR_MESSAGE);
            }
        }catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    public void newExternalEmployeeWindow(String title, Callback callback)
    {
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

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("usr", txtEmail.getText()));
            params.add(new AbstractMap.SimpleEntry<>("pwd", txtTelephone.getText()));
            params.add(new AbstractMap.SimpleEntry<>("access_level", String.valueOf(access_lvl)));
            params.add(new AbstractMap.SimpleEntry<>("firstname", txtFirstname.getText()));
            params.add(new AbstractMap.SimpleEntry<>("lastname", txtLastname.getText()));
            params.add(new AbstractMap.SimpleEntry<>("gender", "female"));
            params.add(new AbstractMap.SimpleEntry<>("email", txtEmail.getText()));
            params.add(new AbstractMap.SimpleEntry<>("tel", txtTelephone.getText()));
            params.add(new AbstractMap.SimpleEntry<>("cell", txtCellphone.getText()));

            if(txtOther.getText()!=null)
                params.add(new AbstractMap.SimpleEntry<>("other", txtOther.getText()));

            try
            {
                HttpURLConnection connection = RemoteComms.postData("/api/employee/add", params, null);
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    IO.logAndAlert("Account Creation Success", "Successfully created new contact!", IO.TAG_INFO);
                    if(callback!=null)
                        callback.call(null);
                } else
                    IO.logAndAlert("Account Creation Failure", IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);

                connection.disconnect();
            }catch (IOException e)
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
                    HttpURLConnection connection = RemoteComms.postData("/api/employee/add", params, null);
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

    public void newOvertimeApplicationWindow(Employee employee, Callback callback)
    {
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Overtime Application ["+employee+"]");
        stage.setMinWidth(320);
        stage.setHeight(500);
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        VBox vbox = new VBox(1);

        final TextField txt_employee = new TextField();
        txt_employee.setMinWidth(200);
        txt_employee.setMaxWidth(Double.MAX_VALUE);
        HBox employee_container = CustomTableViewControls.getLabelledNode("Employee: ", 200, txt_employee);

        final TextField txt_job = new TextField();
        txt_job.setMinWidth(200);
        txt_job.setMaxWidth(Double.MAX_VALUE);
        HBox job_container = CustomTableViewControls.getLabelledNode("Job Number: ", 200, txt_job);

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
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(dpk_overtime_date, dpk_overtime_date.getValue()==null?"":dpk_overtime_date.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_checkin, txt_checkin.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_checkout, txt_checkout.getText(), 1, ".+"))
                return;

            long date_partnered_in_sec = dpk_overtime_date.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("date_partnered", String.valueOf(date_partnered_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("other", str_other));

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

                HttpURLConnection connection = RemoteComms.postData("/api/overtime/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully logged overtime.", IO.TAG_INFO);
                        callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
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
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
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
}
