/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.PDF;
import fadulousbms.auxilary.PDFViewer;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
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
    private TableColumn     colId, colClient, colDescription, colType, colDateLogged, colResponsiblePerson, colStatus, colCreator,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading requisitions view..");

        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees were found in the database.", IO.TAG_ERROR);
            return;
        }
        if(RequisitionManager.getInstance().getRequisitions()==null)
        {
            IO.logAndAlert(getClass().getName(), "no requisitions were found in the database.", IO.TAG_ERROR);
            return;
        }
        if(ClientManager.getInstance().getClients()==null)
        {
            IO.logAndAlert(getClass().getName(), "no clients were found in the database.", IO.TAG_ERROR);
            return;
        }

        /*switch (RequisitionManager.getInstance().getSelected().getStatus())
        {
            case Requisition.STATUS_PENDING:
                status = "PENDING";
                break;
            case Requisition.STATUS_APPROVED:
                status = "APPROVED";
                break;
            case Requisition.STATUS_ARCHIVED:
                status = "ARCHIVED";
                break;
            default:
                status = "UNKNOWN";
                IO.logAndAlert("Error", "Unknown Requisition status: " + RequisitionManager.getInstance().getSelected()
                        .getStatus(), IO.TAG_ERROR);
                break;
        }*/

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colClient.setMinWidth(120);
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_id"));
        colClient.setCellFactory(col -> new ComboBoxTableCell(ClientManager.getInstance().getClients(), "client_id", "client_id"));
        colResponsiblePerson.setMinWidth(120);
        colResponsiblePerson.setCellValueFactory(new PropertyValueFactory<>("responsible_person_id"));
        colResponsiblePerson.setCellFactory(col -> new ComboBoxTableCell(EmployeeManager.getInstance().getEmployees(), "responsible_person_id", "usr"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged");
        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 100, "description", "/requisitions");
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", RequisitionManager.TYPES, false,"/requisitions");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));

        Callback<TableColumn<Requisition, String>, TableCell<Requisition, String>> cellFactory
                =
                new Callback<TableColumn<Requisition, String>, TableCell<Requisition, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Requisition, String> param)
                    {
                        final TableCell<Requisition, String> cell = new TableCell<Requisition, String>()
                        {
                            final Button btnView = new Button("View Requisition");
                            final Button btnPDF = new Button("View as PDF");
                            final Button btnEmail = new Button("eMail Requisition");
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

                                btnPDF.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnPDF.getStyleClass().add("btnDefault");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnEmail.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnEmail.setMinWidth(100);
                                btnEmail.setMinHeight(35);
                                HBox.setHgrow(btnEmail, Priority.ALWAYS);

                                if(!empty)
                                {
                                    if (getTableView().getItems().get(getIndex()).getStatus()==PurchaseOrderManager.PO_STATUS_APPROVED)
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
                                    HBox hBox = new HBox(btnView, btnPDF, btnEmail, btnRemove);

                                    btnView.setOnAction(event ->
                                    {
                                        Requisition requisition = getTableView().getItems().get(getIndex());
                                        if(requisition==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Requisition object is not set", IO.TAG_ERROR);
                                            return;
                                        }

                                        /*ScreenManager.getInstance().showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    RequisitionManager.getInstance().setSelected(requisition);
                                                    try
                                                    {
                                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_REQUISITION.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_REQUISITION.getScreen())))
                                                        {
                                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_REQUISITION.getScreen()));
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load requisitions viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                    }
                                                }
                                            }).start();
                                            return null;
                                        });*/
                                    });

                                    btnPDF.setOnAction(event ->
                                    {
                                        /*Requisition requisition = getTableView().getItems().get(getIndex());
                                        try
                                        {
                                            String path = PDF.createRequisitionPdf(requisition);
                                            if(path!=null)
                                            {
                                                PDFViewer pdfViewer = PDFViewer.getInstance();
                                                pdfViewer.setVisible(true);
                                                pdfViewer.doOpen(path);
                                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid requisition pdf path returned.");
                                        } catch (IOException ex)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                                        }*/
                                    });

                                    btnEmail.setOnAction(event ->
                                    {
                                        Requisition requisition = getTableView().getItems().get(getIndex());
                                        if(requisition!=null)
                                            RequisitionManager.getInstance().emailRequisition(requisition, null);
                                        else IO.logAndAlert("Error", "Requisition object is null.", IO.TAG_ERROR);
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

        if(RequisitionManager.getInstance().getRequisitions()!=null)
            tblRequisitions.setItems(FXCollections.observableArrayList(RequisitionManager.getInstance().getRequisitions().values()));
        else IO.log(getClass().getName(), IO.TAG_WARN, "no requisitions were found in the database.");
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading requisitions data model..");

        EmployeeManager.getInstance().loadDataFromServer();
        ClientManager.getInstance().loadDataFromServer();
        RequisitionManager.getInstance().loadDataFromServer();
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

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}