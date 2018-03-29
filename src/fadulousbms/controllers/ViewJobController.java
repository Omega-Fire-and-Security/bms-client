/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * ViewJob Controller class
 * Created by ghost on 2017/01/13.
 * @author th3gh0st
 */
public class ViewJobController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TableView<JobEmployee> tblEmployees;
    @FXML
    private TableColumn colFirstname,colLastname,colCell,colEmail,colTel,colGender,
                        colActive,colEmployeeAction;
    @FXML
    private TextField txtJobNumber,txtCompany, txtContact, txtCell,txtTel,txtTotal,txtFax,txtEmail,txtSite,
            txtDateGenerated,txtQuoteNumber, txtStatus,txtExtra;
    @FXML
    private DatePicker dateCompleted, dateStarted, dateAssigned;
    @FXML
    private TextArea txtRequest;
    @FXML
    private Button btnApprove;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        Callback<TableColumn<JobEmployee, String>, TableCell<JobEmployee, String>> actionColCellFactory
                =
                new Callback<TableColumn<JobEmployee, String>, TableCell<JobEmployee, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<JobEmployee, String> param)
                    {
                        final TableCell<JobEmployee, String> cell = new TableCell<JobEmployee, String>()
                        {
                            final Button btnRemove = new Button("Remove");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);

                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnRemove.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(100);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                } else
                                {
                                    btnRemove.setOnAction(event ->
                                    {
                                        try
                                        {
                                            //remove JobEmployee from remote server
                                            EmployeeManager.getInstance().deleteObject(getTableView().getItems().get(getIndex()), job_empl_id->
                                            {
                                                if(job_empl_id != null)
                                                {
                                                    //TODO: deallocate all JobEmployee objects with same usr from all Job's Tasks too?
                                                    IO.logAndAlert("Success", "Successfully dissociated user [" + getTableView().getItems().get(getIndex()).getEmployee().getName() + "] from job #" + getTableView().getItems().get(getIndex()).getJob().getObject_number(), IO.TAG_INFO);
                                                    //remove JobEmployee from memory
                                                    JobManager.getInstance().getJob_employees().remove(getTableView().getItems().get(getIndex()));
                                                    //remove JobEmployee from table
                                                    tblEmployees.getItems().remove(getTableView().getItems().get(getIndex()));
                                                    tblEmployees.refresh();//update table
                                                } else IO.logAndAlert("Error", "Could not dissociate employee ["+getTableView().getItems().get(getIndex()).getEmployee().getName()+"]'s from job #" + getTableView().getItems().get(getIndex()).getJob().getObject_number(), IO.TAG_ERROR);
                                                return null;
                                            });
                                        } catch (IOException e)
                                        {
                                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                            e.printStackTrace();
                                        }
                                    });
                                    setGraphic(btnRemove);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };

        colEmployeeAction.setMinWidth(120);
        colEmployeeAction.setCellFactory(actionColCellFactory);
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading job viewer..");

        tblEmployees.getItems().clear();

        //Hide [Approve] button if not authorized
        if(SessionManager.getInstance().getActiveEmployee().getAccessLevel()< AccessLevel.SUPERUSER.getLevel())
        {
            btnApprove.setVisible(false);
            btnApprove.setDisable(true);
        }else
        {
            btnApprove.setVisible(true);
            btnApprove.setDisable(false);
        }

        //Setup Sale Reps table
        colFirstname.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        colLastname.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        colCell.setCellValueFactory(new PropertyValueFactory<>("cell"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        //Populate fields
        Job selected = JobManager.getInstance().getSelected();
        if(selected!=null)
        {
            if(selected.getQuote()==null)
            {
                IO.logAndAlert("Job Viewer", "Selected Job has no valid quote object.", IO.TAG_ERROR);
                return;
            }
            txtCompany.setEditable(false);
            txtContact.setEditable(false);
            if(selected.getQuote().getClient()!=null)
                txtCompany.setText(selected.getQuote().getClient().getClient_name());
            if(selected.getQuote().getContact_person()!=null)
            {
                txtContact.setText(selected.getQuote().getContact_person().getName());
                txtCell.setText(selected.getQuote().getContact_person().getCell());
                txtTel.setText(selected.getQuote().getContact_person().getTel());
                txtEmail.setText(selected.getQuote().getContact_person().getEmail());
            }
            if(selected.getQuote().getClient()!=null)
                txtFax.setText(selected.getQuote().getClient().getFax());
            txtJobNumber.setText(String.valueOf(selected.getObject_number()));
            txtSite.setText(selected.getQuote().getSitename());
            txtRequest.setText(selected.getQuote().getRequest());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                        String.valueOf(selected.getQuote().getTotal()));
            txtStatus.setText(selected.getStatus()>=BusinessObject.STATUS_FINALISED ?"APPROVED":"PENDING");
            txtQuoteNumber.setText(String.valueOf(selected.getQuote().getObject_number()));

            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

            //clear date fields
            dateAssigned.setValue(LocalDate.parse(formatter.format(new Date(0))));
            dateStarted.setValue(LocalDate.parse(formatter.format(new Date(0))));
            dateCompleted.setValue(LocalDate.parse(formatter.format(new Date(0))));
            //set date fields
            if(selected.getDate_assigned()>0)
                dateAssigned.setValue(LocalDate.parse(formatter.format(new Date(selected.getDate_assigned()))));
            if(selected.getDate_started()>0)
                dateStarted.setValue(LocalDate.parse(formatter.format(new Date(selected.getDate_started()))));
            if(selected.getDate_completed()>0)
                dateCompleted.setValue(LocalDate.parse(formatter.format(new Date(selected.getDate_completed()))));

            dateAssigned.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                if(newValue!=null && dateAssigned.isFocused())
                {
                    if(newValue==oldValue)
                    {
                        IO.log(getClass().getName(), IO.TAG_WARN, "same date");
                        return;
                    }

                    if(SessionManager.getInstance().getActive()!=null)
                    {
                        if(!SessionManager.getInstance().getActive().isExpired())
                        {
                            selected.setDate_assigned(newValue.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);//epoch milliseconds

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));
                            try
                            {
                                HttpURLConnection connection = RemoteComms.post(selected.apiEndpoint(), selected.getJSONString(), headers);
                                if (connection != null)
                                {
                                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                        IO.logAndAlert("Success", "Successfully updated job's date_assigned attribute: " + IO
                                                .readStream(connection.getInputStream()), IO.TAG_INFO);
                                    else IO.logAndAlert("Update Error", "Could not update Job object: " + IO
                                            .readStream(connection.getErrorStream()), IO.TAG_ERROR);
                                    connection.disconnect();
                                } else IO.logAndAlert("Connection Error", "Connection to server was lost.", IO.TAG_ERROR);
                            } catch (IOException e)
                            {
                                IO.logAndAlert("IO Error", e.getMessage(), IO.TAG_ERROR);
                            }
                        } else IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
                }  else IO.log(getClass().getName(), IO.TAG_WARN, "new value is null or date_assigned DatePicker is not focused.");
            });

            dateStarted.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                if(newValue!=null && dateStarted.isFocused())
                {
                    if(newValue==oldValue)
                    {
                        IO.log(getClass().getName(), IO.TAG_WARN, "same date");
                        return;
                    }

                    if(SessionManager.getInstance().getActive()!=null)
                    {
                        if(!SessionManager.getInstance().getActive().isExpired())
                        {
                            selected.setDate_started(newValue.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));
                            try
                            {
                                HttpURLConnection connection = RemoteComms.post(selected.apiEndpoint(), selected.getJSONString(), headers);
                                if(connection!=null)
                                {
                                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                                    {
                                        IO.logAndAlert("Success", "Successfully updated job's date_started attribute: " + IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                                    } else IO.logAndAlert("Error", "Could not update Job object: " + IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                                    connection.disconnect();
                                } else IO.logAndAlert("Error", "Connection to server was lost.", IO.TAG_ERROR);
                            } catch (IOException e)
                            {
                                IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                e.printStackTrace();
                            }
                        } else IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
                } else IO.log(getClass().getName(), IO.TAG_WARN, "new value is null or date_started DatePicker is not focused.");
            });

            dateCompleted.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                if(newValue!=null && dateCompleted.isFocused())
                {
                    if(newValue==oldValue)
                    {
                        IO.log(getClass().getName(), IO.TAG_WARN, "same date");
                        return;
                    }
                    if(SessionManager.getInstance().getActive()!=null)
                    {
                        if(!SessionManager.getInstance().getActive().isExpired())
                        {
                            selected.setDate_completed(newValue.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));
                            try
                            {
                                HttpURLConnection connection = RemoteComms.post(selected.apiEndpoint(), selected.getJSONString(), headers);
                                if(connection!=null)
                                {
                                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                                        IO.logAndAlert("Success", "Successfully updated job's date_completed attribute: " + IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                                    else IO.logAndAlert("Error", "Could not update Job object: " + IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                                    connection.disconnect();
                                } else IO.logAndAlert("Error", "Connection to server was lost.", IO.TAG_ERROR);
                            } catch (IOException e)
                            {
                                IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                e.printStackTrace();
                            }
                        } else IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
                } else IO.log(getClass().getName(), IO.TAG_WARN, "new value is null or date_completed DatePicker is not focused.");
            });

            try
            {
                String date = new Date(selected.getDate_logged()).toString();
                txtDateGenerated.setText(date);
            }catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            HashMap job_employees = selected.getAssigned_employees();
            if (job_employees != null)
            {
                tblEmployees.setItems(FXCollections.observableArrayList(job_employees.values()));
                tblEmployees.refresh();
            } else IO.log(getClass().getName(), IO.TAG_WARN, "job [" + selected.get_id() + "] has no assignees.");
        } else IO.logAndAlert("Job Viewer", "Selected Job is invalid.", IO.TAG_ERROR);
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading jobs data model..");

        QuoteManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        JobManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        JobManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void assignEmployee()
    {
        EmployeeManager.getInstance().initialize();

        if(EmployeeManager.getInstance().getDataset()!=null)
        {
            if(EmployeeManager.getInstance().getDataset().size()>0)
            {
                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                Employee[] employees = new Employee[EmployeeManager.getInstance().getDataset().values().toArray().length];
                EmployeeManager.getInstance().getDataset().values().toArray(employees);

                ComboBox<Employee> employeeComboBox = new ComboBox<>();
                employeeComboBox.setMinWidth(120);
                employeeComboBox.setItems(FXCollections.observableArrayList(employees));
                HBox.setHgrow(employeeComboBox, Priority.ALWAYS);

                Button btnAdd = new Button("Add");
                btnAdd.setMinWidth(80);
                btnAdd.setMinHeight(40);
                btnAdd.setDefaultButton(true);
                btnAdd.getStyleClass().add("btnApply");
                btnAdd.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                Button btnCancel = new Button("Close");
                btnCancel.setMinWidth(80);
                btnCancel.setMinHeight(40);
                btnCancel.getStyleClass().add("btnBack");
                btnCancel.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                HBox hBox = new HBox(new Label("Employee: "), employeeComboBox);
                HBox.setHgrow(hBox, Priority.ALWAYS);
                hBox.setSpacing(20);

                HBox hBoxButtons = new HBox(btnAdd, btnCancel);
                hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                hBoxButtons.setSpacing(20);

                VBox vBox = new VBox(hBox, hBoxButtons);
                VBox.setVgrow(vBox, Priority.ALWAYS);
                vBox.setSpacing(20);
                HBox.setHgrow(vBox, Priority.ALWAYS);
                vBox.setFillWidth(true);

                Stage stage = new Stage();
                stage.setTitle("Add Job Technician");
                stage.setScene(new Scene(vBox));
                stage.setAlwaysOnTop(true);
                stage.show();

                btnAdd.setOnAction(event ->
                {
                    if(employeeComboBox.getValue()!=null)
                    {
                        if(JobManager.getInstance().getSelected()!=null)
                        {
                            if(SessionManager.getInstance().getActive()!=null)
                            {
                                JobEmployee jobEmployee = new JobEmployee();
                                jobEmployee.setJob_id(JobManager.getInstance().getSelected().get_id());
                                jobEmployee.setUsr(employeeComboBox.getValue().getUsr());
                                jobEmployee.setCreator(SessionManager.getInstance().getActive().getUsr());

                                tblEmployees.getItems().add(jobEmployee);
                                tblEmployees.refresh();

                                itemsModified = true;

                                //update date_assigned of current job
                                if(JobManager.getInstance().getSelected().getDate_assigned()<=0)
                                {
                                    //update in memory
                                    JobManager.getInstance().getSelected().setDate_assigned(System.currentTimeMillis());
                                    //update on server
                                    try
                                    {
                                        JobManager.getInstance().patchObject(JobManager.getInstance().getSelected(), job_id->
                                        {
                                            if(job_id!=null)
                                                IO.logAndAlert("Success", "Updated job #" +JobManager.getInstance().getSelected().getObject_number()+"'s date assigned attribute.", IO.TAG_INFO);
                                            else IO.logAndAlert("Error", "Could not update job #" +JobManager.getInstance().getSelected().getObject_number()+"'s date assigned attribute.", IO.TAG_WARN);
                                            return null;
                                        });
                                    } catch (IOException e)
                                    {
                                        IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                        e.printStackTrace();
                                    }
                                } else IO.log(getClass().getName(), IO.TAG_VERBOSE, "Job #" + JobManager.getInstance().getSelected().getObject_number() + " already has a valid date_assigned, ignoring patch.");
                            }  else IO.logAndAlert("Error", "Active session is invalid.\nPlease log in.", IO.TAG_ERROR);
                        } else IO.logAndAlert("Error", "Selected job is invalid.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error", "Selected employee is invalid.", IO.TAG_ERROR);
                });

                btnCancel.setOnAction(event ->
                    stage.close());
                return;
            } else IO.logAndAlert("New Job Representative", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
        } else IO.logAndAlert("New Job Representative", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
    }

    @FXML
    public void exportPDF()
    {
        if(JobManager.getInstance().getSelected()!=null)
        {
            if((JobManager.getInstance().getSelected()).getAssigned_employees()!=null)
            {
                if((JobManager.getInstance().getSelected()).getAssigned_employees().size()>0)
                    JobManager.showJobCard((JobManager.getInstance().getSelected()));
                else IO.logAndAlert("Error", "Selected job has no assigned employees, please assign employees first then try again.", IO.TAG_ERROR);
            } else IO.logAndAlert("Error", "Selected job has no assigned employees, please assign employees first then try again.", IO.TAG_ERROR);
        } else IO.logAndAlert("Error", "Selected job is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void update()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                if(JobManager.getInstance().getSelected()==null)
                {
                    IO.log(getClass().getName(), IO.TAG_WARN, "selected job is invalid.");
                    return;
                }
                if(JobManager.getInstance().getSelected()!=null)
                {
                    //update Job object on server (only the date_assigned attribute is set and has not been approved already.)
                    if(JobManager.getInstance().getSelected().getStatus()!=Job.STATUS_FINALISED)
                    {
                        //set date assigned to current UNIX epoch date
                        //if (selected.getDate_assigned() <= 0)
                        //    selected.setDate_assigned(System.currentTimeMillis());

                        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                        headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));

                        try
                        {
                            HttpURLConnection connection = RemoteComms.post(JobManager.getInstance().getSelected().apiEndpoint(), JobManager.getInstance().getSelected().getJSONString(), headers);
                            if (connection != null)
                            {
                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                {
                                    //add job representatives
                                    if (tblEmployees.getItems() != null)
                                    {
                                        for (JobEmployee jobEmployee : tblEmployees.getItems())
                                        {
                                            if(jobEmployee.get_id()==null)
                                            {
                                                //Employee has not been associated with Job yet, create new record on db
                                                JobManager.getInstance().putObject(jobEmployee, new_je_id->
                                                {
                                                    if(new_je_id==null)
                                                        IO.logAndAlert("Success", "Successfully updated job[#"
                                                                +String.valueOf(JobManager.getInstance().getSelected().getObject_number())
                                                                + "] representatives.", IO.TAG_INFO);
                                                    else
                                                        IO.logAndAlert("Error", "Could NOT update job[#"
                                                            + String.valueOf(JobManager.getInstance().getSelected().getObject_number())
                                                            + "] representatives.", IO.TAG_ERROR);
                                                    return null;
                                                });
                                                /*created_all = JobManager.createJobRepresentative(JobManager.getInstance().getSelected().get_id(), employee.getUsr());*/
                                            } else IO.log(getClass().getName(), IO.TAG_VERBOSE, "Employee " + jobEmployee.getUsr() + " has already been assigned to Job #" + JobManager.getInstance().getSelected().getObject_number() + ", skipping."); //don't duplicate record
                                        }
                                    } else IO.logAndAlert("Error", "You have not assigned any employees to this job.", IO.TAG_ERROR);
                                } else IO.logAndAlert("Error: " + connection
                                        .getResponseCode(), "Could not update Job object: " + IO
                                        .readStream(connection.getErrorStream()), IO.TAG_ERROR);

                                //terminate connection
                                if (connection != null)
                                    connection.disconnect();
                            } else IO.logAndAlert("Error", "Connection to server was lost.", IO.TAG_ERROR);
                        } catch (MalformedURLException ex)
                        {
                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
                        } catch (IOException ex)
                        {
                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
                        }
                    } else IO.logAndAlert("Error", "Selected Job has already been approved and can no longer be edited.\nPlease create a new one.", IO.TAG_ERROR);
                } else IO.logAndAlert("Error", "Selected Job is invalid", IO.TAG_ERROR);
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void back()
    {
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.OPERATIONS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            ScreenManager.getInstance().setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void requestApproval()
    {
        try
        {
            //send email requesting approval of Job
            if(JobManager.getInstance().getSelected()!=null)
                JobManager.getInstance().requestJobApproval((Job)JobManager.getInstance().getSelected(), null);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void approveJob()
    {
        if(JobManager.getInstance().getSelected()==null)
        {
            IO.logAndAlert("Error", "Selected Job object is invalid.", IO.TAG_ERROR);
            return;
        }

        try
        {
            JobManager.getInstance().approveJob(JobManager.getInstance().getSelected(), job_id ->
            {
                //Refresh UI
                new Thread(() ->
                        refreshModel(param ->
                        {
                            Platform.runLater(() -> refreshView());
                            return null;
                        })).start();
                return null;
            });
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @FXML
    public void viewQuote()
    {
        QuoteManager.getInstance().initialize();
        if (JobManager.getInstance().getSelected() == null)
        {
            IO.logAndAlert("Error " + getClass().getName(), "Selected Job object invalid.", IO.TAG_ERROR);
            return;
        }
        //set selected Quote
        QuoteManager.getInstance().setSelected(JobManager.getInstance().getSelected().getQuote());

        //load Quote viewer
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_QUOTE.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_QUOTE.getScreen())))
                        {
                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_QUOTE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes viewer screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }
}
