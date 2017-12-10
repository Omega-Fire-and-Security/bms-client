package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Employee;
import fadulousbms.model.Job;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/11/15.
 */
public class EmployeesTabController extends ScreenController implements Initializable
{
    @FXML
    private TableColumn colEmployeeId, colEmployeeFirstName, colEmployeeLastName, colEmployeeUsername,
            colEmployeeEmail, colEmployeeCell, colEmployeeTel, colEmployeeSex, colEmployeeActive, colEmployeeDateJoined, colEmployeeOther, colAction;
    @FXML
    private TableView tblEmployees;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading employees tab view.");

        colEmployeeId.setMinWidth(100);
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colEmployeeFirstName, TextFieldTableCell.forTableColumn(), 120, "firstname", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeLastName, TextFieldTableCell.forTableColumn(), 120, "lastname", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeUsername, TextFieldTableCell.forTableColumn(), 120, "usr", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeEmail, TextFieldTableCell.forTableColumn(), 120, "email", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeCell, TextFieldTableCell.forTableColumn(), 120, "cell", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeTel, TextFieldTableCell.forTableColumn(), 120, "tell", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeSex, TextFieldTableCell.forTableColumn(), 120, "gender", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeActive, TextFieldTableCell.forTableColumn(), 120, "active", "/api/employee");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colEmployeeDateJoined, "date_joined", "/api/employee");
        CustomTableViewControls.makeEditableTableColumn(colEmployeeOther, TextFieldTableCell.forTableColumn(), 120, "other", "/api/employee");

        ObservableList<Employee> lst_employees = FXCollections.observableArrayList();
        lst_employees.addAll(EmployeeManager.getInstance().getEmployees().values());
        tblEmployees.setItems(lst_employees);

        Callback<TableColumn<Employee, String>, TableCell<Employee, String>> cellFactory
                =
                new Callback<TableColumn<Employee, String>, TableCell<Employee, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Employee, String> param)
                    {
                        final TableCell<Employee, String> cell = new TableCell<Employee, String>()
                        {
                            final Button btnView = new Button("View");
                            final Button btnUploadCV = new Button("Upload CV");
                            final Button btnViewCV = new Button("View CV");
                            final ToggleButton btnUploadID = new ToggleButton("Upload ID");
                            final ToggleButton btnViewID = new ToggleButton("View ID");
                            final Button btnUploadCertificate = new Button("Upload Certificate");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnView.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnView.getStyleClass().add("btnDefault");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

                                btnUploadCV.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnUploadCV.getStyleClass().add("btnAdd");
                                btnUploadCV.setMinWidth(130);
                                btnUploadCV.setMinHeight(35);
                                HBox.setHgrow(btnUploadCV, Priority.ALWAYS);

                                btnViewCV.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnViewCV.getStyleClass().add("btnDefault");
                                btnViewCV.setMinWidth(130);
                                btnViewCV.setMinHeight(35);
                                HBox.setHgrow(btnViewCV, Priority.ALWAYS);

                                btnUploadID.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnUploadID.getStyleClass().add("btnAdd");
                                btnUploadID.setMinWidth(130);
                                btnUploadID.setMinHeight(35);
                                HBox.setHgrow(btnUploadID, Priority.ALWAYS);

                                btnViewID.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnViewID.getStyleClass().add("btnDefault");
                                btnViewID.setMinWidth(130);
                                btnViewID.setMinHeight(35);
                                HBox.setHgrow(btnViewID, Priority.ALWAYS);

                                btnUploadCertificate.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnUploadCertificate.getStyleClass().add("btnDefault");
                                btnUploadCertificate.setMinWidth(100);
                                btnUploadCertificate.setMinHeight(35);
                                HBox.setHgrow(btnUploadCertificate, Priority.ALWAYS);

                                btnRemove.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
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
                                    HBox hBox = new HBox(btnView, btnUploadCV, btnViewCV, btnUploadID, btnViewID, btnUploadCertificate, btnRemove);
                                    hBox.setMaxWidth(Double.MAX_VALUE);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    Employee employee = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                    });

                                    btnUploadCV.setOnAction(event ->
                                    {
                                        if(employee==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Employee object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        EmployeeManager.getInstance().uploadCV(employee.get_id());
                                    });

                                    btnViewCV.setOnAction(event ->
                                    {
                                        if(employee==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Employee object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        EmployeeManager.getInstance().viewCV(employee.get_id());
                                    });

                                    btnUploadID.setOnAction(event ->
                                    {
                                        if(employee==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Employee object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        EmployeeManager.getInstance().uploadID(employee.get_id());
                                    });

                                    btnViewID.setOnAction(event ->
                                    {
                                        if(employee==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Employee object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        EmployeeManager.getInstance().viewID(employee.get_id());
                                    });

                                    btnUploadCertificate.setOnAction(event ->
                                    {
                                        if(employee==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Employee object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        //JobManager.getInstance().uploadSigned(job.get_id());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //197.242.144.30
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        //getTableView().getItems().remove(quote);
                                        //getTableView().refresh();
                                        //TODO: remove from server
                                        //IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
                                    });

                                    hBox.setFillHeight(true);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    hBox.setSpacing(5);
                                    setGraphic(hBox);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };

        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);
        colAction.setMinWidth(700);
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading employees tab model.");
        try
        {
            EmployeeManager.getInstance().reloadDataFromServer();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        new Thread(() ->
        {
            refreshModel();
            Platform.runLater(() -> refreshView());
        }).start();
    }
}
