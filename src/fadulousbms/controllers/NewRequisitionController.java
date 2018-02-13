/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.bouncycastle.cert.ocsp.Req;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewRequisitionController extends ScreenController implements Initializable
{
    @FXML
    protected ComboBox<String> cbxType;
    @FXML
    protected ComboBox<Client> cbxClient;
    @FXML
    protected ComboBox<Employee> cbxResponsiblePerson;
    @FXML
    protected TextField txtStatus;
    @FXML
    protected Button btnApprove;
    @FXML
    protected TextArea txtDescription;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @Override
    public void refreshView()
    {
        if(EmployeeManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees were found in the database.", IO.TAG_WARN);
            //return;
        }
        if( ClientManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getName(), "no clients were found in the database.", IO.TAG_WARN);
            //return;
        }

        Employee[] employees=null;
        if(EmployeeManager.getInstance().getDataset()!=null)
        {
            employees = new Employee[EmployeeManager.getInstance().getDataset().values().toArray().length];
            EmployeeManager.getInstance().getDataset().values().toArray(employees);
        }

        //setup Requisition combo boxes
        cbxType.setItems(FXCollections.observableArrayList(RequisitionManager.TYPES));
        cbxClient.setItems(FXCollections.observableArrayList((Collection<Client>) (ClientManager.getInstance().getDataset().values())));
        if(employees!=null)
            cbxResponsiblePerson.setItems(FXCollections.observableArrayList(employees));

        //set status
        String status;
        if(RequisitionManager.getInstance().getSelected()!=null)
        {
            switch (((Requisition)(RequisitionManager.getInstance().getSelected())).getStatus())
            {
                case BusinessObject.STATUS_PENDING:
                    status = "PENDING";
                    break;
                case BusinessObject.STATUS_APPROVED:
                    status = "APPROVED";
                    break;
                case BusinessObject.STATUS_ARCHIVED:
                    status = "ARCHIVED";
                    break;
                default:
                    status = "UNKNOWN";
                    IO.logAndAlert("Error", "Unknown Requisition status: " + ((Requisition)(RequisitionManager.getInstance().getSelected())).getStatus(), IO.TAG_ERROR);
                    break;
            }
            if(txtStatus!=null)
                txtStatus.setText(status);
        }
    }

    @Override
    public void refreshModel()
    {
        EmployeeManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        RequisitionManager.getInstance().initialize();
    }

    @Override
    public void forceSynchronise()
    {
        RequisitionManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    @FXML
    public void createRequisition()
    {
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        cbxClient.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxClient.getValue()==null)
        {
            cbxClient.getStyleClass().remove("form-control-default");
            cbxClient.getStyleClass().add("control-input-error");
            return;
        } else
        {
            cbxClient.getStyleClass().remove("control-input-error");
            cbxClient.getStyleClass().add("form-control-default");
        }

        cbxResponsiblePerson.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxResponsiblePerson.getValue()==null)
        {
            cbxResponsiblePerson.getStyleClass().remove("form-control-default");
            cbxResponsiblePerson.getStyleClass().add("control-input-error");
            return;
        } else
        {
            cbxResponsiblePerson.getStyleClass().remove("control-input-error");
            cbxResponsiblePerson.getStyleClass().add("form-control-default");
        }

        cbxType.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxType.getValue()==null)
        {
            cbxType.getStyleClass().remove("form-control-default");
            cbxType.getStyleClass().add("control-input-error");
            return;
        } else
        {
            cbxType.getStyleClass().remove("control-input-error");
            cbxType.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtDescription, txtDescription.getText(), 1, ".+"))
        {
            txtDescription.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }

        //prepare Requisition attributes
        Requisition requisition = new Requisition();
        requisition.setClient_id(cbxClient.getValue().get_id());
        requisition.setDescription(txtDescription.getText());
        requisition.setType(cbxType.getValue());
        requisition.setResponsible_person_id(cbxResponsiblePerson.getValue().getUsr());
        requisition.setStatus(Requisition.STATUS_PENDING);
        requisition.setCreator(SessionManager.getInstance().getActive().getUsr());

        try
        {
            RequisitionManager.getInstance().createRequisition(requisition, null);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void newClient()
    {
        ClientManager.newClientWindow("Create a new Client for this Requisition", param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });
    }

    @FXML
    public void back()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.OPERATIONS.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
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