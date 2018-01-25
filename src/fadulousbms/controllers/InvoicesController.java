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
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class InvoicesController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Invoice>    tblInvoices;
    @FXML
    private TableColumn     colInvoiceNum,colJobNum,colClient,colTotal,colReceivable,colDateGenerated,colAccount,colStatus,
                            colCreator,colExtra,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading invoices view..");
        if(InvoiceManager.getInstance().getInvoices()==null)
        {
            IO.logAndAlert(getClass().getName(), "no invoices were found in the database.", IO.TAG_ERROR);
            return;
        }
        colInvoiceNum.setMinWidth(140);
        colInvoiceNum.setCellValueFactory(new PropertyValueFactory<>("object_number"));
        colJobNum.setMinWidth(50);
        colJobNum.setCellValueFactory(new PropertyValueFactory<>("job_number"));
        colClient.setMinWidth(80);
        colClient.setCellValueFactory(new PropertyValueFactory<>("client"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_logged");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","APPROVED"}, false,"/invoices");
        colCreator.setMinWidth(70);
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator_name"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        colAccount.setCellValueFactory(new PropertyValueFactory<>("account"));
        CustomTableViewControls.makeEditableTableColumn(colReceivable, TextFieldTableCell.forTableColumn(), 80, "receivable", "/invoices");
        CustomTableViewControls.makeJobManagerAction(colAction, 600, null);
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 80, "other", "/invoices");
        colAction.setMinWidth(360);

        ObservableList<Invoice> lst_invoices = FXCollections.observableArrayList();
        lst_invoices.addAll(InvoiceManager.getInstance().getInvoices().values());
        tblInvoices.setItems(lst_invoices);

        final ScreenManager screenManager = ScreenManager.getInstance();
        Callback<TableColumn<Invoice, String>, TableCell<Invoice, String>> cellFactory
                =
                new Callback<TableColumn<Invoice, String>, TableCell<Invoice, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Invoice, String> param)
                    {
                        final TableCell<Invoice, String> cell = new TableCell<Invoice, String>()
                        {
                            final Button btnApproval = new Button("Request Approval");
                            final Button btnViewQuote = new Button("View linked Quote");
                            final Button btnViewJob = new Button("View linked Job");
                            final Button btnPDF = new Button("View as PDF");
                            final Button btnEmail = new Button("eMail Invoice");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnApproval.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnApproval.getStyleClass().add("btnAdd");
                                btnApproval.setMinWidth(100);
                                btnApproval.setMinHeight(35);
                                HBox.setHgrow(btnApproval, Priority.ALWAYS);

                                btnViewQuote.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnViewQuote.getStyleClass().add("btnDefault");
                                btnViewQuote.setMinWidth(100);
                                btnViewQuote.setMinHeight(35);
                                HBox.setHgrow(btnViewQuote, Priority.ALWAYS);

                                btnViewJob.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnViewJob.getStyleClass().add("btnDefault");
                                btnViewJob.setMinWidth(100);
                                btnViewJob.setMinHeight(35);
                                HBox.setHgrow(btnViewJob, Priority.ALWAYS);

                                btnPDF.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnPDF.getStyleClass().add("btnDefault");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnEmail.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnEmail.getStyleClass().add("btnDefault");
                                btnEmail.setMinWidth(100);
                                btnEmail.setMinHeight(35);
                                HBox.setHgrow(btnEmail, Priority.ALWAYS);
                                if(!empty)
                                {
                                    if (getTableView().getItems().get(getIndex())==null)
                                    {
                                        IO.logAndAlert("Error " + getClass().getName(), "Invoice object is not set", IO.TAG_ERROR);
                                        return;
                                    }
                                    if (getTableView().getItems().get(getIndex()).getStatus()==BusinessObject.STATUS_APPROVED)
                                    {
                                        btnEmail.getStyleClass().add("btnAdd");
                                        btnEmail.setDisable(false);
                                    } else
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
                                    HBox hBox = new HBox(btnApproval, btnViewQuote, btnViewJob, btnPDF, btnEmail, btnRemove);
                                    Invoice invoice = getTableView().getItems().get(getIndex());

                                    btnApproval.setOnAction(event ->
                                    {
                                        //send email requesting approval of Invoice
                                        try
                                        {
                                            InvoiceManager.getInstance().setSelected(invoice);
                                            if(InvoiceManager.getInstance().getSelected()!=null)
                                                InvoiceManager.getInstance().requestInvoiceApproval(InvoiceManager.getInstance().getSelected(), null);
                                            else IO.logAndAlert("Error", "Selected Invoice is invalid.", IO.TAG_ERROR);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnViewQuote.setOnAction(event ->
                                    {
                                        if(invoice.getJob()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(invoice.getJob().getQuote()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job->Quote object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        screenManager.showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    QuoteManager.getInstance().setSelectedQuote(invoice.getJob().getQuote());
                                                    try
                                                    {
                                                        if(screenManager.loadScreen(Screens.VIEW_QUOTE.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_QUOTE.getScreen())))
                                                        {
                                                            Platform.runLater(() -> screenManager.setScreen(Screens.VIEW_QUOTE.getScreen()));
                                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                    }
                                                }
                                            }).start();
                                            return null;
                                        });
                                        /*QuoteManager.getInstance().setSelectedQuote(invoice.getJob().getQuote());
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_QUOTE.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_QUOTE.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_QUOTE.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote viewing screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }*/
                                    });

                                    btnViewJob.setOnAction(event ->
                                    {
                                        if(invoice.getJob()==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        JobManager.getInstance().loadDataFromServer();

                                        screenManager.showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    JobManager.getInstance().setSelected(invoice.getJob());
                                                    try
                                                    {
                                                        if(screenManager.loadScreen(Screens.VIEW_JOB.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_JOB.getScreen())))
                                                        {
                                                            Platform.runLater(() -> screenManager.setScreen(Screens.VIEW_JOB.getScreen()));
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                    }
                                                }
                                            }).start();
                                            return null;
                                        });
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

                                    btnPDF.setOnAction(event ->
                                    {
                                        if(invoice.getJob()==null)
                                        {
                                            IO.logAndAlert("Error","Invalid Job object.", IO.TAG_ERROR);
                                            return;
                                        }
                                        if(invoice.getJob().getQuote()==null)
                                        {
                                            IO.logAndAlert("Error","Invalid Quote associated with this Invoice["+invoice.get_id()+"].", IO.TAG_ERROR);
                                            return;
                                        }
                                        try
                                        {
                                            String path = PDF.createInvoicePdf(invoice);
                                            if(path!=null)
                                            {
                                                PDFViewer pdfViewer = PDFViewer.getInstance();
                                                pdfViewer.setVisible(true);
                                                pdfViewer.doOpen(path);
                                            }else IO.logAndAlert("Error", "Could not get valid path of generated Invoice PDF document.", IO.TAG_ERROR);
                                        } catch (IOException ex)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                                        }
                                    });

                                    btnEmail.setOnAction(event ->
                                    {
                                        try
                                        {
                                            InvoiceManager.getInstance().emailBusinessObject(invoice, PDF.createInvoicePdf(invoice), null);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
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

        tblInvoices.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                InvoiceManager.getInstance().setSelected(tblInvoices.getSelectionModel().getSelectedItem()));
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

    @Override
    public void refreshModel()
    {
        EmployeeManager.getInstance().loadDataFromServer();
        ClientManager.getInstance().loadDataFromServer();
        try
        {
            QuoteManager.getInstance().reloadDataFromServer();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        JobManager.getInstance().loadDataFromServer();
        InvoiceManager.getInstance().loadDataFromServer();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
