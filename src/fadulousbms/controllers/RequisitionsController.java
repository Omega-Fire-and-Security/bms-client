/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.PDF;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class RequisitionsController extends OperationsController implements Initializable
{
    @FXML
    private TableView<Requisition>    tblRequisitions;
    @FXML
    private TableColumn     colId, colClient, colDescription, colType, colDateLogged, colResponsiblePerson, colStatus, colCreator, colAction;
    @FXML
    private Tab requisitionsTab;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading requisitions view..");

        if(EmployeeManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getName(), "No employees were found in the database.", IO.TAG_ERROR);
            return;
        }
        if(RequisitionManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No requisitions were found in the database.", IO.TAG_WARN);
            return;
        }
        if(ClientManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getName(), "No clients were found in the database.", IO.TAG_WARN);
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colClient.setMinWidth(120);
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_name"));
        //colClient.setCellFactory(col -> new ComboBoxTableCell(ClientManager.getInstance().getClients(), "client_id", "client_id"));
        colResponsiblePerson.setMinWidth(120);
        colResponsiblePerson.setCellValueFactory(new PropertyValueFactory<>("responsible_person"));
        //colResponsiblePerson.setCellFactory(col -> new ComboBoxTableCell(EmployeeManager.getInstance().getEmployees(), "responsible_person_id", "usr"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged", false);
        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 100, "description", "/requisitions");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        //CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", RequisitionManager.TYPES, false,"/requisitions");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","APPROVED"}, false,"/requisitions");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator_name"));

        Callback<TableColumn<Requisition, String>, TableCell<Requisition, String>> cellFactory
                =
                new Callback<TableColumn<Requisition, String>, TableCell<Requisition, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Requisition, String> param)
                    {
                        final TableCell<Requisition, String> cell = new TableCell<Requisition, String>()
                        {
                            final Button btnRequestAppproval= new Button("Request Approval");
                            final Button btnNewQuote = new Button("New Quote");
                            final Button btnEmail = new Button("eMail Requisition");
                            final Button btnRemove = new Button("Delete");

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

                                btnNewQuote.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnNewQuote.getStyleClass().add("btnAdd");
                                btnNewQuote.setMinWidth(100);
                                btnNewQuote.setMinHeight(35);
                                HBox.setHgrow(btnNewQuote, Priority.ALWAYS);

                                btnRequestAppproval.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnRequestAppproval.getStyleClass().add("btnAdd");
                                btnRequestAppproval.setMinWidth(100);
                                btnRequestAppproval.setMinHeight(35);
                                HBox.setHgrow(btnRequestAppproval, Priority.ALWAYS);

                                btnEmail.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnEmail.setMinWidth(100);
                                btnEmail.setMinHeight(35);
                                HBox.setHgrow(btnEmail, Priority.ALWAYS);

                                if(!empty)
                                {
                                    Requisition requisition= getTableView().getItems().get(getIndex());
                                    if(requisition==null)
                                    {
                                        IO.logAndAlert("Error " + getClass().getName(), "Requisition object is not set", IO.TAG_ERROR);
                                        return;
                                    }
                                    if (requisition.getStatus()==BusinessObject.STATUS_APPROVED)
                                    {
                                        btnEmail.getStyleClass().add("btnAdd");
                                        btnEmail.setDisable(false);
                                    }
                                    else
                                    {
                                        btnEmail.getStyleClass().add("btnDisabled");
                                        btnEmail.setDisable(true);
                                    }
                                }

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                } else
                                {
                                    HBox hBox = new HBox(btnRequestAppproval, btnNewQuote, btnEmail, btnRemove);

                                    btnRequestAppproval.setOnAction(event ->
                                    {
                                        Requisition requisition = getTableView().getItems().get(getIndex());

                                        if(requisition==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Requisition object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(requisition.getClient()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Invalid Requisition Client object", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(SessionManager.getInstance().getActive()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Invalid active Session.", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(SessionManager.getInstance().getActive().isExpired())
                                        {
                                            IO.logAndAlert("Error", "Active Session has expired.", IO.TAG_ERROR);
                                            return;
                                        }

                                        RequisitionManager.getInstance().setSelected(requisition);
                                        requestApproval();
                                    });

                                    btnNewQuote.setOnAction(event ->
                                    {
                                        Requisition requisition = getTableView().getItems().get(getIndex());
                                        RequisitionManager.getInstance().setSelected(requisition);
                                        if(requisition==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Requisition object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(requisition.getClient()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Invalid Requisition Client object", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(SessionManager.getInstance().getActive()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Invalid active Session.", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(SessionManager.getInstance().getActive().isExpired())
                                        {
                                            IO.logAndAlert("Error", "Active Session has expired.", IO.TAG_ERROR);
                                            return;
                                        }

                                        Quote quote = new Quote();
                                        quote.setRequisition_id(requisition.get_id());
                                        quote.setVat(QuoteManager.VAT);
                                        quote.setStatus(Quote.STATUS_PENDING);
                                        quote.setAccount_name(requisition.getClient().getAccount_name());
                                        quote.setRequest(requisition.getDescription());
                                        quote.setSitename("To be confirmed: "+requisition.getClient().getPhysical_address());
                                        quote.setClient_id(requisition.getClient().get_id());
                                        quote.setContact_person_id(requisition.getResponsible_person_id());
                                        quote.setCreator(SessionManager.getInstance().getActive().getUsr());
                                        quote.setRevision(1.0);

                                        List<Employee> quote_reps = new ArrayList<>();
                                        quote_reps.add(SessionManager.getInstance().getActiveEmployee());
                                        try
                                        {
                                            QuoteManager.getInstance().createQuote(quote, null, null, new Callback()
                                            {
                                                @Override
                                                public Object call(Object param)
                                                {
                                                    ScreenManager.getInstance().showLoadingScreen(arg ->
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
                                                                        Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_QUOTE.getScreen()));
                                                                    }
                                                                    else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load Quotes viewer screen.");
                                                                } catch (IOException e)
                                                                {
                                                                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                                }
                                                            }
                                                        }).start();
                                                        return null;
                                                    });
                                                    return null;
                                                }
                                            });
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnEmail.setOnAction(event ->
                                    {
                                        Requisition requisition= getTableView().getItems().get(getIndex());
                                        try
                                        {
                                            if(requisition!=null)
                                                RequisitionManager.getInstance().emailBusinessObject(requisition, PDF.createRequisitionPDF(requisition), null);
                                            else IO.logAndAlert("Error", "Quote object is null.", IO.TAG_ERROR);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        Requisition requisition = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(requisition);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed requisition: " + requisition.get_id());
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
        colAction.setCellFactory(cellFactory);

        tblRequisitions.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                RequisitionManager.getInstance().setSelected(tblRequisitions.getSelectionModel().getSelectedItem()));

        tblRequisitions.setItems(FXCollections.observableArrayList(RequisitionManager.getInstance().getDataset().values()));
    }

    public void requestApproval()
    {
        //send email requesting approval of Requisition
        if(RequisitionManager.getInstance().getSelected()!=null)
            RequisitionManager.getInstance().requestRequisitionApproval((Requisition) RequisitionManager.getInstance().getSelected(), null);
        else IO.logAndAlert("Error", "Selected Requisition is invalid.", IO.TAG_ERROR);
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading requisitions data model..");

        EmployeeManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        RequisitionManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        RequisitionManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        OperationsController.registerTabController(requisitionsTab.getId(),this);
        new Thread(() ->
                refreshModel(param ->
                {
                    Platform.runLater(() -> refreshView());
                    return null;
                })).start();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}