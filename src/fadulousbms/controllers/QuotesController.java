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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class QuotesController extends OperationsController implements Initializable
{
    @FXML
    private TableView<Quote>    tblQuotes;
    @FXML
    private TableColumn     colId, colClient, colSitename, colRequest, colContactPerson, colTotal,
                            colDateGenerated, colStatus, colCreator, colRevision,
                            colExtra,colAction;
    @FXML
    private TableColumn<BusinessObject, String> colVat;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading quotes view..");

        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees were found in the database.", IO.TAG_ERROR);
            return;
        }
        if(QuoteManager.getInstance().getQuotes()==null)
        {
            IO.logAndAlert(getClass().getName(), "no quotes were found in the database.", IO.TAG_ERROR);
            return;
        }
        if(ClientManager.getInstance().getClients()==null)
        {
            IO.logAndAlert(getClass().getName(), "no clients were found in the database.", IO.TAG_ERROR);
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colClient.setMinWidth(120);
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_id"));
        colClient.setCellFactory(col -> new ComboBoxTableCell(ClientManager.getInstance().getClients(), "client_id", "/quotes"));
        colContactPerson.setMinWidth(120);
        colContactPerson.setCellValueFactory(new PropertyValueFactory<>("contact_person_id"));
        colContactPerson.setCellFactory(col -> new ComboBoxTableCell(EmployeeManager.getInstance().getEmployees(), "contact_person_id", "usr"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_logged");
        CustomTableViewControls.makeEditableTableColumn(colRequest, TextFieldTableCell.forTableColumn(), 100, "request", "/quotes");
        CustomTableViewControls.makeEditableTableColumn(colSitename, TextFieldTableCell.forTableColumn(), 100, "sitename", "/quotes");
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","SALE"}, false,"/quotes");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        colRevision.setCellValueFactory(new PropertyValueFactory<>("revision"));
        colVat.setCellValueFactory(new PropertyValueFactory<>("vat"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 100, "other", "/quotes");

        Callback<TableColumn<Quote, String>, TableCell<Quote, String>> cellFactory
                =
                new Callback<TableColumn<Quote, String>, TableCell<Quote, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Quote, String> param)
                    {
                        final TableCell<Quote, String> cell = new TableCell<Quote, String>()
                        {
                            final Button btnView = new Button("View Quote");
                            final Button btnPDF = new Button("View as PDF");
                            final Button btnEmail = new Button("eMail Quote");
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
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        if(quote==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Quote object is not set", IO.TAG_ERROR);
                                            return;
                                        }

                                        ScreenManager.getInstance().showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    QuoteManager.getInstance().setSelectedQuote(quote);
                                                    try
                                                    {
                                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_QUOTE.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_QUOTE.getScreen())))
                                                        {
                                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_QUOTE.getScreen()));
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                                    }
                                                }
                                            }).start();
                                            return null;
                                        });
                                    });

                                    btnPDF.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        try
                                        {
                                            String path = PDF.createQuotePdf(quote);
                                            if(path!=null)
                                            {
                                                PDFViewer pdfViewer = PDFViewer.getInstance();
                                                pdfViewer.setVisible(true);
                                                pdfViewer.doOpen(path);
                                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid quote pdf path returned.");
                                        } catch (IOException ex)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                                        }
                                    });

                                    btnEmail.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        if(quote!=null)
                                            QuoteManager.getInstance().emailQuote(quote, null);
                                        else IO.logAndAlert("Error", "Quote object is null.", IO.TAG_ERROR);
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quote);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
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

        tblQuotes.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                QuoteManager.getInstance().setSelectedQuote(tblQuotes.getSelectionModel().getSelectedItem()));

        HashMap<String, Quote> latest_rev_quotes = new HashMap<>();
        for(Quote quote: QuoteManager.getInstance().getQuotes().values())
        {
            if(quote.getParent_id()!=null)//if quote has parent, i.e. siblings
            {
                //get Quote's siblings
                Quote[] siblings = quote.getSortedSiblings("revision");
                if (siblings != null)
                {
                    //add latest revision/sibling to list of latest revisions with identifier of parent.
                    latest_rev_quotes.put(quote.getParent_id().get_id(), siblings[siblings.length - 1]);//overwrite existing value if exists
                } else IO.log(getClass().getName(), IO.TAG_WARN, "Quote [" + quote.get_id() + "] has no siblings. should return self as first sibling.");
            } else if(quote.getChildren("revision")==null)//if Quote has no parent and no children add it to list.
            {
                latest_rev_quotes.put(quote.get_id(), quote);//has no parent & no children
            }
        }
        //set list of latest Quotes
        tblQuotes.setItems(FXCollections.observableArrayList(latest_rev_quotes.values()));
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading quotes data model..");

        EmployeeManager.getInstance().loadDataFromServer();
        ClientManager.getInstance().loadDataFromServer();
        ResourceManager.getInstance().loadDataFromServer();
        QuoteManager.getInstance().loadDataFromServer();
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