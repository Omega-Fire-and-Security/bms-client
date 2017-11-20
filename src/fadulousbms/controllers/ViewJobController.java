/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.DateTimeException;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

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
    private TextField txtJobNumber,txtCompany, txtContact, txtCell,txtTel,txtTotal,txtFax,txtEmail,txtSite,txtDateGenerated,txtExtra;
    @FXML
    private TextArea txtRequest;

    @Override
    public void refreshView()
    {
        tblEmployees.getItems().clear();

        //Setup Sale Reps table
        colFirstname.setCellValueFactory(new PropertyValueFactory<>("firstname"));
        colLastname.setCellValueFactory(new PropertyValueFactory<>("lastname"));
        colCell.setCellValueFactory(new PropertyValueFactory<>("cell"));
        colTel.setCellValueFactory(new PropertyValueFactory<>("tel"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colActive.setCellValueFactory(new PropertyValueFactory<>("active"));

        //Populate fields
        Job selected = JobManager.getInstance().getSelectedJob();
        if(selected!=null)
        {
            if(selected.getQuote()==null)
            {
                IO.logAndAlert("Job Viewer", "Selected Job has no valid quote object.", IO.TAG_ERROR);
                return;
            }
            if(selected.getQuote().getClient()!=null)
                txtCompany.setText(selected.getQuote().getClient().getClient_name());
            if(selected.getQuote().getContact_person()!=null)
            {
                txtContact.setText(selected.getQuote().getContact_person().toString());
                txtCell.setText(selected.getQuote().getContact_person().getCell());
                txtTel.setText(selected.getQuote().getContact_person().getTel());
                txtEmail.setText(selected.getQuote().getContact_person().getEmail());
            }
            if(selected.getQuote().getClient()!=null)
                txtFax.setText(selected.getQuote().getClient().getFax());
            txtJobNumber.setText(String.valueOf(selected.getJob_number()));
            txtSite.setText(selected.getQuote().getSitename());
            txtRequest.setText(selected.getQuote().getRequest());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                        String.valueOf(selected.getQuote().getTotal()));

            try
            {
                //String date = LocalDate.parse(new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(selected.getDate_generated()*1000))).toString();
                String date = new Date(selected.getDate_logged()*1000).toString();
                txtDateGenerated.setText(date);
            }catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            /*if (selected.getItems() != null)
                tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getItems()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");*/
            if (selected.getAssigned_employees() != null)
            {
                tblEmployees.setItems(FXCollections.observableArrayList(selected.getAssigned_employees()));
                tblEmployees.refresh();
            }
            else IO.log(getClass().getName(), IO.TAG_WARN, "job [" + selected.get_id() + "] has no representatives.");
        }else IO.logAndAlert("Job Viewer", "Selected Job is invalid.", IO.TAG_ERROR);
    }

    @Override
    public void refreshModel()
    {
        QuoteManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        JobManager.getInstance().initialize();
    }

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

                                btnRemove.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
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
                                        System.out.println("Successfully removed sale representative: " + employee.toString());
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

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void assignEmployee()
    {
        EmployeeManager.getInstance().loadDataFromServer();

        if(EmployeeManager.getInstance().getEmployees()!=null)
        {
            if(EmployeeManager.getInstance().getEmployees().size()>0)
            {
                Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().values().toArray().length];
                EmployeeManager.getInstance().getEmployees().values().toArray(employees);

                ComboBox<Employee> employeeComboBox = new ComboBox<>();
                employeeComboBox.setMinWidth(120);
                employeeComboBox.setItems(FXCollections.observableArrayList(employees));
                HBox.setHgrow(employeeComboBox, Priority.ALWAYS);

                Button btnAdd = new Button("Add");
                btnAdd.setMinWidth(80);
                btnAdd.setMinHeight(40);
                btnAdd.setDefaultButton(true);
                btnAdd.getStyleClass().add("btnApply");
                btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                Button btnCancel = new Button("Close");
                btnCancel.setMinWidth(80);
                btnCancel.setMinHeight(40);
                btnCancel.getStyleClass().add("btnBack");
                btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

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
                stage.setTitle("Add Job Representative");
                stage.setScene(new Scene(vBox));
                stage.setAlwaysOnTop(true);
                stage.show();

                btnAdd.setOnAction(event ->
                {
                    if(employeeComboBox.getValue()!=null)
                    {
                        tblEmployees.getItems().add(employeeComboBox.getValue());
                        itemsModified=true;
                    } else IO.logAndAlert("Add Job Representative", "Invalid employee selected.", IO.TAG_ERROR);
                });

                btnCancel.setOnAction(event ->
                    stage.close());
                return;
            } else IO.logAndAlert("New Job Representative", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
        }else IO.logAndAlert("New Job Representative", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
    }

    @FXML
    public void exportPDF()
    {
        if(JobManager.getInstance().getSelectedJob()!=null)
        {
            if(JobManager.getInstance().getSelectedJob().getAssigned_employees()!=null)
            {
                if(JobManager.getInstance().getSelectedJob().getAssigned_employees().length>0)
                    JobManager.showJobCard(JobManager.getInstance().getSelectedJob());
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
                if(JobManager.getInstance().getSelectedJob()!=null)
                {
                    //add job representatives
                    boolean created_all=true;
                    if (tblEmployees.getItems() != null)
                        for (Employee employee : tblEmployees.getItems())
                            created_all=JobManager.createJobRepresentative(JobManager.getInstance().getSelectedJob().get_id(), employee.getUsr());
                    if(created_all)
                    {
                        IO.logAndAlert("Success", "Successfully updated job[#" + String
                                .valueOf(JobManager.getInstance().getSelectedJob()
                                        .getJob_number()) + "] representatives.", IO.TAG_INFO);
                        try
                        {
                            JobManager.getInstance().reloadDataFromServer();
                            //JobManager.getInstance().setSelectedJob(JobManager.getInstance().getJobs().get(new_job_id));

                            JobManager.getInstance().setSelectedJob(JobManager.getInstance().getJobs().get(JobManager.getInstance().getSelectedJob().get_id()));

                            if(JobManager.getInstance().getJobs()!=null)
                            {
                                ScreenManager.getInstance().showLoadingScreen(param ->
                                {
                                    new Thread(() ->
                                    {
                                        refreshModel();
                                        Platform.runLater(() -> refreshView());
                                    }).start();

                                    /*new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                if (ScreenManager.getInstance().loadScreen(Screens.VIEW_JOB.getScreen(), getClass()
                                                                .getResource("../views/" + Screens.VIEW_JOB.getScreen())))
                                                {
                                                    Platform.runLater(() -> ScreenManager.getInstance()
                                                            .setScreen(Screens.VIEW_JOB.getScreen()));
                                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load job viewer screen.");
                                            } catch (IOException e)
                                            {
                                                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                            }
                                        }
                                    }).start();*/

                                    return null;
                                });
                            } else IO.logAndAlert("Error", "Could not find any jobs in the database.", IO.TAG_INFO);
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
                    else IO.logAndAlert("Error", "Could NOT update job[#"+String.valueOf(JobManager.getInstance().getSelectedJob().getJob_number())+"] representatives.", IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void back()
    {
        ScreenController.previousScreen();
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
                /*update_counter++;
                if(updated_ids.putIfAbsent(selected_id, "")==null)
                {
                    setGraphic(comboBox);
                    System.out.println("Added new ID.");
                }else System.err.println("ID was present in HashMap already, thus no setGraphic().");
                /*if(update_counter<getTableView().getItems().size())
                {

                }*/
                //System.out.println("\n-->id:"+selected_id+"\tupdate count:"+update_counter+"<--\n");

                //IO.log(TAG, IO.TAG_INFO, String.format("updated selected item to [%s] on combo box.", selected_id));
            }
            //IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
            /*BusinessObject tbl_row_businessObject;
            if(selected_id==null)
            {
                if (getTableRow() != null)
                {
                    if (getTableRow().getItem() instanceof BusinessObject)
                    {
                        tbl_row_businessObject = (BusinessObject) getTableRow().getItem();
                        if (tbl_row_businessObject != null)
                        {
                            if(SessionManager.getInstance().getActive()!=null)
                            {
                                String url = tbl_row_businessObject.apiEndpoint() + "/" + tbl_row_businessObject.get_id();
                                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                                try
                                {
                                    String obj_json = RemoteComms.sendGetRequest(url, headers);

                                    System.out.println(obj_json);
                                    if (obj_json != null)
                                    {
                                        Gson gson = new GsonBuilder().create();
                                        if(!obj_json.toString().equals("") && !obj_json.toString().equals("[]") &&
                                                !obj_json.toString().equals("{}") && obj_json.toString().contains("{"))
                                        {
                                            BusinessObject obj = gson.fromJson(obj_json, tbl_row_businessObject.getClass());

                                            for (BusinessObject combo_item : business_objects)
                                            {
                                                if (combo_item.get_id() != null)
                                                {
                                                    if (combo_item.get_id().equals(obj.get(property)))
                                                    {
                                                        String prop_val;
                                                    /*
                                                        If the combo items are of multiple data types then
                                                        Get the appropriate label for each data type.
                                                        Labels are passed through the label_property property -
                                                        If there are multi-types the labels are separated by the pipe (|) symbol.
                                                     *
                                                        String[] properties = label_property.split("\\|");
                                                        prop_val = getBusinessObjectProperty(properties, combo_item);

                                                        //if a valid label was found on the object then the combo box value and graphic are set
                                                        if (prop_val != null)
                                                        {
                                                            comboBox.setValue(prop_val);
                                                            setGraphic(comboBox);
                                                            IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
                                                            break;
                                                        } else
                                                        {
                                                            IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, combo_item.getClass().getName()));
                                                        }
                                                    }
                                                } else
                                                {
                                                    IO.log(TAG, IO.TAG_WARN, "combo box item id is null.");
                                                }
                                            }
                                        }else{
                                            IO.log(getClass().getName(), IO.TAG_ERROR, "invalid JSON object ["+tbl_row_businessObject.get_id()+" type "+tbl_row_businessObject.getClass().getName()+"]\n" + obj_json);
                                        }
                                    } else
                                    {
                                        IO.log(TAG, IO.TAG_ERROR, "JSON data from server is null.");
                                    }
                                } catch (IOException e)
                                {
                                    IO.log(TAG, IO.TAG_ERROR, e.getMessage());
                                }
                            } else
                            {
                                IO.log(TAG, IO.TAG_ERROR, "no active sessions.");
                            }
                        } else
                        {
                            IO.log(TAG, IO.TAG_ERROR, "row object is null.");
                        }
                    } else
                    {
                        IO.log(TAG, IO.TAG_ERROR, "unknown row object: " + getTableRow().getItem());
                    }
                } else
                {
                    IO.log(TAG, IO.TAG_ERROR, "row is null.");
                }
            }else{
                if(business_objects!=null)
                {
                    for (BusinessObject combo_item : business_objects)
                    {
                        if (combo_item.get_id() != null)
                        {
                            if (combo_item.get_id().equals(selected_id))
                            {
                                String prop_val;
                            /*
                                If the combo items are of multiple data types then
                                Get the appropriate label for each data type.
                                Labels are passed through the label_property property -
                                If there are multi-types the labels are separated by the pipe (|) symbol.
                             *
                                String[] properties = label_property.split("\\|");
                                prop_val = getBusinessObjectProperty(properties, combo_item);

                                //if a valid label was found on the object then the combo box value and graphic are set
                                if (prop_val != null)
                                {
                                    comboBox.setValue(prop_val);
                                    setGraphic(comboBox);
                                    IO.log(TAG, IO.TAG_INFO, String.format("set property value of '%s' on combo box.", prop_val));
                                    break;
                                } else
                                {
                                    IO.log(TAG, IO.TAG_WARN, String.format("property '%s' on object of type '%s' is null.", label_property, combo_item.getClass().getName()));
                                }
                            }
                        } else
                        {
                            IO.log(TAG, IO.TAG_WARN, "combo box item id is null.");
                        }
                    }
                }else{
                    IO.log(TAG, IO.TAG_WARN, (getTableView().getItems().size()>0?"business objects of type " + getTableView().getItems().get(0).getClass().getName():"business objects of " + selected_id) + " are NULL.");
                }
            }*/
        /*if(selected_id!=null)
        {
            if(Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                System.out.println(String.format("ComboBox> info: selected id is '%s'.", selected_id));

            for (BusinessObject bo : business_objects)
            {
                if (bo.get_id().equals(selected_id))
                {
                    comboBox.setValue((String) bo.get(label_property));
                    if(Globals.DEBUG_INFO.getValue().toLowerCase().equals("on"))
                        System.out.println(String.format("ComboBox> info: selected '%s'.", (String) bo.get(label_property)));
                }
            }
            setGraphic(comboBox);
        } else
            if(Globals.DEBUG_ERRORS.getValue().toLowerCase().equals("on"))
                System.err.println("ComboBox> error: selected id is null, ignoring.");*/

        }

        @Override
        public void startEdit()
        {
            super.startEdit();
        }
    }
}
