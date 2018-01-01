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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
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

    @Override
    public void refreshView()
    {
        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees were found in the database.", IO.TAG_WARN);
            //return;
        }
        if( ClientManager.getInstance().getClients()==null)
        {
            IO.logAndAlert(getClass().getName(), "no clients were found in the database.", IO.TAG_WARN);
            //return;
        }

        Employee[] employees=null;
        if(EmployeeManager.getInstance().getEmployees()!=null)
        {
            employees = new Employee[EmployeeManager.getInstance().getEmployees().values().toArray().length];
            EmployeeManager.getInstance().getEmployees().values().toArray(employees);
        }

        //setup Requisition combo boxes
        cbxType.setItems(FXCollections.observableArrayList(RequisitionManager.TYPES));
        cbxClient.setItems(FXCollections.observableArrayList(ClientManager.getInstance().getClients().values()));
        if(employees!=null)
            cbxResponsiblePerson.setItems(FXCollections.observableArrayList(employees));

        //set status
        String status;
        if(RequisitionManager.getInstance().getSelected()!=null)
        {
            switch (RequisitionManager.getInstance().getSelected().getStatus())
            {
                case Quote.STATUS_PENDING:
                    status = "PENDING";
                    break;
                case Quote.STATUS_APPROVED:
                    status = "APPROVED";
                    break;
                case Quote.STATUS_ARCHIVED:
                    status = "ARCHIVED";
                    break;
                default:
                    status = "UNKNOWN";
                    IO.logAndAlert("Error", "Unknown Requisition status: " + RequisitionManager.getInstance().getSelected()
                            .getStatus(), IO.TAG_ERROR);
                    break;
            }
            if(txtStatus!=null)
                txtStatus.setText(status);
        }
    }

    @Override
    public void refreshModel()
    {
        try
        {
            EmployeeManager.getInstance().reloadDataFromServer();
            ClientManager.getInstance().reloadDataFromServer();
            RequisitionManager.getInstance().reloadDataFromServer();
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
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    @FXML
    public void requestApproval()
    {
        //send email requesting approval of Requisition
        if(RequisitionManager.getInstance().getSelected()!=null)
            RequisitionManager.getInstance().requestRequisitionApproval(RequisitionManager.getInstance().getSelected(), null);
    }

    @FXML
    public void approveRequisition()
    {
        Requisition selected = RequisitionManager.getInstance().getSelected();
        if(selected!=null)
        {
            if(selected.getStatus()!=Requisition.STATUS_APPROVED)
            {
                selected.setStatus(Requisition.STATUS_APPROVED);
                //TODO: RequisitionManager.getInstance().updateRequisition(selected, tblQuoteItems.getItems(), tblSaleReps.getItems());
                refreshModel();
                refreshView();
            } else IO.logAndAlert("Error", "Selected quote has already been approved.", IO.TAG_ERROR);
        } else IO.logAndAlert("Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void createRequisition()
    {
        cbxClient.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
        if(cbxClient.getValue()==null)
        {
            cbxClient.getStyleClass().remove("form-control-default");
            cbxClient.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxClient.getStyleClass().remove("control-input-error");
            cbxClient.getStyleClass().add("form-control-default");
        }

        cbxResponsiblePerson.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
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

        cbxType.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
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
            txtDescription.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
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
            /*new Callback()
            {
                @Override
                public Object call(Object quote_id)
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
                                    if(ScreenManager.getInstance().loadScreen(Screens.VIEW_QUOTE.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_QUOTE.getScreen())))
                                    {
                                        //Platform.runLater(() ->
                                        ScreenManager.getInstance().setScreen(Screens.VIEW_QUOTE.getScreen());
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load view quote screen.");
                                } catch (IOException e)
                                {
                                    e.printStackTrace();
                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                }
                            }
                        }).start();
                        return null;
                    });
                    txtStatus.setText(quote_id.toString());
                    return null;
                }
            }*/
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void newClient()
    {
        ClientManager.getInstance().newClientWindow("Create a new Client for this Requisition", param ->
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
