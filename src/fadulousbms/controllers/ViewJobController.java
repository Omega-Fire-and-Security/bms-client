/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.AccessLevels;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
 * views Controller class
 *
 * @author ghost
 */
public class ViewJobController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    @FXML
    private TableView<Employee> tblEmployees;
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
        Callback<TableColumn<Employee, String>, TableCell<Employee, String>> actionColCellFactory
                =
                new Callback<TableColumn<Employee, String>, TableCell<Employee, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Employee, String> param)
                    {
                        final TableCell<Employee, String> cell = new TableCell<Employee, String>()
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
                                        Employee employee = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(employee);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        System.out.println("Successfully removed sale representative: " + employee.getName());
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
        if(SessionManager.getInstance().getActiveEmployee().getAccessLevel()< AccessLevels.SUPERUSER.getLevel())
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
        Job selected = ((Job)JobManager.getInstance().getSelected());
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
            txtStatus.setText(selected.getStatus()>=BusinessObject.STATUS_APPROVED?"APPROVED":"PENDING");
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
                if(newValue!=null)
                {
                    if(SessionManager.getInstance().getActive()!=null)
                    {
                        if(!SessionManager.getInstance().getActive().isExpired())
                        {
                            selected.setDate_assigned(newValue.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);//epoch milliseconds

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));
                            try
                            {
                                HttpURLConnection connection = RemoteComms
                                        .postJSON(selected.apiEndpoint(), selected.getJSONString(), headers);
                                if (connection != null)
                                {
                                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                    {
                                        IO.logAndAlert("Success", "Successfully updated job's date_assigned attribute: " + IO
                                                .readStream(connection.getInputStream()), IO.TAG_INFO);
                                    } else IO.logAndAlert("Update Error", "Could not update Job object: " + IO
                                            .readStream(connection.getErrorStream()), IO.TAG_ERROR);
                                    connection.disconnect();
                                } else IO.logAndAlert("Connection Error", "Connection to server was lost.", IO.TAG_ERROR);
                            } catch (IOException e)
                            {
                                IO.logAndAlert("IO Error", e.getMessage(), IO.TAG_ERROR);
                            }
                        } else IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
                }
            });

            dateStarted.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                if(newValue!=null)
                {
                    if(SessionManager.getInstance().getActive()!=null)
                    {
                        if(!SessionManager.getInstance().getActive().isExpired())
                        {
                            selected.setDate_started(newValue.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));
                            try
                            {
                                HttpURLConnection connection = RemoteComms.postJSON(selected.apiEndpoint(), selected.getJSONString(), headers);
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
                            }
                        } else IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
                }
            });

            dateCompleted.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                if(newValue!=null)
                {
                    if(SessionManager.getInstance().getActive()!=null)
                    {
                        if(!SessionManager.getInstance().getActive().isExpired())
                        {
                            selected.setDate_completed(newValue.atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));
                            try
                            {
                                HttpURLConnection connection = RemoteComms.postJSON(selected.apiEndpoint(), selected.getJSONString(), headers);
                                if(connection!=null)
                                {
                                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                                    {
                                        IO.logAndAlert("Success", "Successfully updated job's date_completed attribute: " + IO.readStream(connection.getInputStream()), IO.TAG_INFO);
                                    } else IO.logAndAlert("Error", "Could not update Job object: " + IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                                    connection.disconnect();
                                } else IO.logAndAlert("Error", "Connection to server was lost.", IO.TAG_ERROR);
                            } catch (IOException e)
                            {
                                IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                            }
                        } else IO.logAndAlert("Error: Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error: Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
                }
            });

            try
            {
                String date = new Date(selected.getDate_logged()).toString();
                txtDateGenerated.setText(date);
            }catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            if (selected.getAssigned_employees() != null)
            {
                tblEmployees.setItems(FXCollections.observableArrayList(selected.getAssigned_employees()));
                tblEmployees.refresh();
            } else IO.log(getClass().getName(), IO.TAG_WARN, "job [" + selected.get_id() + "] has no representatives.");
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
                            ((Job)JobManager.getInstance().getSelected()).setDate_assigned(System.currentTimeMillis());
                        } else IO.log(getClass().getName(), IO.TAG_WARN, "selected job is invalid.");
                        tblEmployees.getItems().add(employeeComboBox.getValue());
                        itemsModified=true;
                    } else IO.logAndAlert("Add Job Technician", "Invalid employee selected.", IO.TAG_ERROR);
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
            if(((Job)JobManager.getInstance().getSelected()).getAssigned_employees()!=null)
            {
                if(((Job)JobManager.getInstance().getSelected()).getAssigned_employees().length>0)
                    JobManager.showJobCard(((Job)JobManager.getInstance().getSelected()));
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
                Job selected = ((Job)JobManager.getInstance().getSelected());
                if(selected!=null)
                {
                    //update Job object on server (only the date_assigned attribute is set and has not been approved already.)
                    if(selected.getStatus()!=Job.STATUS_APPROVED)
                    {
                        //set date assigned to current UNIX epoch date
                        //if (selected.getDate_assigned() <= 0)
                        //    selected.setDate_assigned(System.currentTimeMillis());

                        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                        headers.add(new AbstractMap.SimpleEntry("Cookie", smgr.getActive().getSession_id()));
                        headers.add(new AbstractMap.SimpleEntry("Content-Type", "application/json"));

                        try
                        {
                            HttpURLConnection connection = RemoteComms.postJSON(selected.apiEndpoint(), selected.getJSONString(), headers);
                            if (connection != null)
                            {
                                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                {
                                    //add job representatives
                                    boolean created_all = true;
                                    if (tblEmployees.getItems() != null)
                                        for (Employee employee : tblEmployees.getItems())
                                            created_all = JobManager
                                                    .createJobRepresentative(JobManager.getInstance().getSelected()
                                                            .get_id(), employee.getUsr());
                                    if (created_all)
                                    {
                                        IO.logAndAlert("Success", "Successfully updated job[#"
                                                +String.valueOf(JobManager.getInstance().getSelected().getObject_number())
                                                + "] representatives.", IO.TAG_INFO);

                                        //force refresh Job data-set
                                        JobManager.getInstance().forceSynchronise();

                                        if (JobManager.getInstance().getDataset() != null)
                                        {
                                            //update selected Job
                                            JobManager.getInstance().setSelected(JobManager.getInstance().getDataset()
                                                    .get(JobManager.getInstance().getSelected().get_id()));

                                            ScreenManager.getInstance().showLoadingScreen(param ->
                                            {
                                                new Thread(new Runnable()
                                                {
                                                    @Override
                                                    public void run()
                                                    {
                                                        try
                                                        {
                                                            if (ScreenManager.getInstance().loadScreen(Screens.VIEW_JOB
                                                                    .getScreen(), fadulousbms.FadulousBMS.class
                                                                    .getResource("views/" + Screens.VIEW_JOB
                                                                            .getScreen())))
                                                            {
                                                                Platform.runLater(() -> ScreenManager.getInstance()
                                                                        .setScreen(Screens.VIEW_JOB.getScreen()));
                                                            }
                                                            else IO.log(getClass()
                                                                    .getName(), IO.TAG_ERROR, "could not load job viewer screen.");
                                                        } catch (IOException e)
                                                        {
                                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                        }
                                                    }
                                                }).start();

                                                return null;
                                            });
                                        } else
                                            IO.logAndAlert("Error", "Could not find any jobs in the database.", IO.TAG_INFO);

                                    } else IO.logAndAlert("Error", "Could NOT update job[#"
                                            + String.valueOf(JobManager.getInstance().getSelected().getObject_number())
                                            + "] representatives.", IO.TAG_ERROR);
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

        JobManager.getInstance().approveJob((Job) JobManager.getInstance().getSelected(), param1 ->
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
        QuoteManager.getInstance().setSelected(((Job) JobManager.getInstance().getSelected()).getQuote());
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

    class ComboBoxTableCell extends TableCell<BusinessObject, String>
    {
        private ComboBox<String> comboBox;
        private String property, label_property, api_method;
        private BusinessObject[] business_objects;
        public static final String TAG = "ComboBoxTableCell";

        public ComboBoxTableCell(BusinessObject[] business_objects, String property, String label_properties)
        {
            super();
            this.property = property;
            this.api_method = api_method;
            this.business_objects=business_objects;
            this.label_property = label_properties;

            String[] combobox_items;
            if(business_objects==null)
            {
                IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be null!");
                return;
            }
            if(business_objects.length<=0)
            {
                IO.log(TAG, IO.TAG_ERROR, "business objects array for the combo box cannot be empty!");
                return;
            }

            combobox_items = new String[business_objects.length];
            String[] properties = label_properties.split("\\|");
            for(int i=0;i<business_objects.length;i++)
            {
                String prop_val = getBusinessObjectProperty(properties, business_objects[i]);
                if (prop_val!=null)
                {
                    combobox_items[i] = prop_val;
                    IO.log(TAG, IO.TAG_INFO, String.format("set combo box array item #%s to '%s'.", i, prop_val));
                    //break;
                }else
                {
                    IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, business_objects[i].getClass().getName()));
                }
            }

            comboBox = new ComboBox<>(FXCollections.observableArrayList(combobox_items));
            HBox.setHgrow(comboBox, Priority.ALWAYS);

            IO.log(TAG, IO.TAG_INFO, "set array to combo box.");

            comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
            {
                int selected_pos = comboBox.selectionModelProperty().get().getSelectedIndex();
                if(selected_pos>=0 && selected_pos<business_objects.length)
                {
                    commitEdit(business_objects[selected_pos].get_id());
                    updateItem(business_objects[selected_pos].get_id(), business_objects[selected_pos].get_id()==null);
                    IO.log(TAG, IO.TAG_INFO, "selected: " + business_objects[selected_pos]);
                }else IO.log(TAG, IO.TAG_ERROR, "index out of bounds.");
            });
        }

        /**
         * Function to get the matching values to a list of BusinessObject attributes.
         * @param properties list of attributes to be retrieved.
         * @param business_object BusinessObject to retrieve the values from.
         * @return String with all the attribute values separated by a space.
         */
        public String getBusinessObjectProperty(String[] properties, BusinessObject business_object)
        {
            String prop_val = "";
            for (String label_property : properties)
                prop_val += business_object.get(label_property) + " ";
            if (prop_val != null)
                return prop_val.substring(0,prop_val.length()-1);//return the chained String - without the last space.
            return null;
        }

        @Override
        public void commitEdit(String selected_id)
        {
            super.commitEdit(selected_id);
            if(selected_id!=null)
            {
                if(!selected_id.isEmpty())
                {
                    if (getTableRow().getItem() instanceof BusinessObject)
                    {
                        int selected_pos = comboBox.selectionModelProperty().get().getSelectedIndex();
                        if(selected_pos>=0 && selected_pos<business_objects.length)
                        {
                            System.out.println("\n\ngetIndex():" + getIndex() + " getTableRow().getIndex():" + getTableRow().getIndex() + "\n\n");
                            //getTableRow().setItem(comboBox.get);
                            System.out.println("Current: " + getTableRow().getItem());
                            for(BusinessObject bo: business_objects)
                            {
                                if(bo.get_id().equals(selected_id))
                                {
                                    System.out.println("New: " + bo);
                                    System.out.println("TableView item count: "+getTableView().getItems().size());
                                    //getTableView().getItems().set(getIndex(), bo);
                                    List items = getTableView().getItems();
                                    items.set(getIndex(), bo);
                                    getTableView().setItems(FXCollections.observableArrayList(items));
                                    return;
                                }
                            }
                            //getTableView().getItems()
                            /*for (Employee employee : QuoteManager.getInstance().getEmployees())
                            {
                                if (employee.get_id().equals(selected_id))
                                {
                                    getTableView().getItems().set(selected_pos, employee);
                                    return;
                                }
                            }*/
                        }else IO.log(TAG, IO.TAG_ERROR, "index out of bounds.");
                        IO.logAndAlert(TAG, "NewQuoteController.ComboBoxTableCell> Selected Employee was not found in Employee list.", IO.TAG_ERROR);
                        /*
                        bo.parse(property, selected_id);
                        if (bo != null)
                        {
                            RemoteComms.updateBusinessObjectOnServer(bo, api_method, property);

                        } else
                        {
                            IO.log(TAG, IO.TAG_ERROR, "row business object is null.");
                        }*/
                    } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object: " + getTableRow().getItem()));
                } else IO.log(TAG, IO.TAG_ERROR, String.format("selected_id is empty"));
            }else IO.log(TAG, IO.TAG_ERROR, "selected_id is null.");
        }

        @Override
        protected void updateItem(String selected_id, boolean empty)
        {
            super.updateItem(selected_id, empty);
            //comboBox.setValue(prop_val);
            if(!empty && selected_id!=null)
            {
                setGraphic(comboBox);
            }
        }

        @Override
        public void startEdit()
        {
            super.startEdit();
        }
    }
}
