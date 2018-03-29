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
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import org.controlsfx.control.textfield.TextFields;

import java.awt.*;
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
    private Tab quotesTab;
    @FXML
    private TableColumn<BusinessObject, String> colVat;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading quotes view..");

        if(EmployeeManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No employees were found in the database.", IO.TAG_ERROR);
            return;
        }
        if(ClientManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No clients were found in the database.", IO.TAG_WARN);
            return;
        }
        if(QuoteManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No quotes were found in the database.", IO.TAG_WARN);
            return;
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colClient.setMinWidth(120);
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_id"));
        //colClient.setCellFactory(col -> new ComboBoxTableCell(ClientManager.getInstance().getClients(), "client_id", "client_name"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_name"));
        colContactPerson.setMinWidth(120);
        colContactPerson.setCellValueFactory(new PropertyValueFactory<>("contact_person"));
        //colContactPerson.setCellFactory(col -> new ComboBoxTableCell(EmployeeManager.getInstance().getEmployees(), "contact_person_id", "usr"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_logged", false);
        CustomTableViewControls.makeEditableTableColumn(colRequest, TextFieldTableCell.forTableColumn(), 100, "request", QuoteManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSitename, TextFieldTableCell.forTableColumn(), 100, "sitename", QuoteManager.getInstance());
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","APPROVED"}, false,"/quote");
        colCreator.setMinWidth(100);
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator_name"));
        colRevision.setCellValueFactory(new PropertyValueFactory<>("revision"));
        colVat.setCellValueFactory(new PropertyValueFactory<>("vat"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 100, "other", QuoteManager.getInstance());

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
                            final Button btnRequestApproval = new Button("Request Quote Approval");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnView.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnView.getStyleClass().add("btnDefault");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

                                btnRequestApproval.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnRequestApproval.getStyleClass().add("btnAdd");
                                btnRequestApproval.setMinWidth(100);
                                btnRequestApproval.setMinHeight(35);
                                HBox.setHgrow(btnRequestApproval, Priority.ALWAYS);

                                btnPDF.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnPDF.getStyleClass().add("btnDefault");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnEmail.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnEmail.setMinWidth(100);
                                btnEmail.setMinHeight(35);
                                HBox.setHgrow(btnEmail, Priority.ALWAYS);

                                if(!empty)
                                {
                                    Quote quote = getTableView().getItems().get(getIndex());
                                    if(quote==null)
                                    {
                                        IO.logAndAlert("Error " + getClass().getName(), "Quote object is not set", IO.TAG_ERROR);
                                        return;
                                    }
                                    if (quote.getStatus()==BusinessObject.STATUS_FINALISED)
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
                                    HBox hBox = new HBox(btnView, btnRequestApproval, btnPDF, btnEmail, btnRemove);

                                    btnRequestApproval.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        if(quote==null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Quote object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        try
                                        {
                                            QuoteManager.getInstance().requestQuoteApproval(quote, null);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                        /*try
                                        {
                                            RemoteComms.emailAttachment("Email with attachment", "test",
                                                    new Employee[]{SessionManager.getInstance().getActiveEmployee()},
                                                    new Metafile[]{new Metafile("some-file.txt", "Some File", "/files", "text/plain")});
                                        } catch (MailjetSocketTimeoutException e)
                                        {
                                            e.printStackTrace();
                                        } catch (MailjetException e)
                                        {
                                            e.printStackTrace();
                                        }*/
                                    });

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
                                                    QuoteManager.getInstance().setSelected(quote);
                                                    if(quote.getSiblingsMap().size()>0)
                                                        QuoteManager.getInstance().selected_quote_sibling_cursor = quote.getSiblingsMap().size()-1;//point to latest revision
                                                    try
                                                    {
                                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_QUOTE.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_QUOTE.getScreen())))
                                                        {
                                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_QUOTE.getScreen()));
                                                        }
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quotes viewer screen.");
                                                    } catch (IOException e)
                                                    {
                                                        e.printStackTrace();
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
                                                if(Desktop.isDesktopSupported())
                                                {
                                                    Desktop.getDesktop().open(new File(path));
                                                }
                                                /*PDFViewer pdfViewer = PDFViewer.getInstance();
                                                pdfViewer.setVisible(true);
                                                pdfViewer.doOpen(path);*/
                                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid quote pdf path returned.");
                                        } catch (IOException ex)
                                        {
                                            IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
                                        }
                                    });

                                    btnEmail.setOnAction(event ->
                                    {
                                        Quote quote = getTableView().getItems().get(getIndex());
                                        try
                                        {
                                            if(quote!=null)
                                                QuoteManager.getInstance().emailBusinessObject(quote, PDF.createQuotePdf(quote), null);
                                            else IO.logAndAlert("Error", "Quote object is null.", IO.TAG_ERROR);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        if(getTableView().getItems().get(getIndex())!=null)
                                            if(getTableView().getItems().get(getIndex()) instanceof Quote)
                                                QuoteManager.getInstance().setSelected(getTableView().getItems().get(getIndex()));

                                        try
                                        {
                                            //remove Quote from remote server
                                            QuoteManager.getInstance().deleteObject(QuoteManager.getInstance().getSelected(), quote_id->
                                            {
                                                if(quote_id != null)
                                                {
                                                    IO.logAndAlert("Success", "Successfully deleted quote [#" + QuoteManager.getInstance().getSelected().getObject_number() + "]", IO.TAG_INFO);
                                                    //remove Quote from memory
                                                    QuoteManager.getInstance().getDataset().remove(QuoteManager.getInstance().getSelected());
                                                    //remove Quote from table
                                                    tblQuotes.getItems().remove(QuoteManager.getInstance().getSelected());
                                                    tblQuotes.refresh();//update table
                                                } else IO.logAndAlert("Error", "Could not delete quote [#"+QuoteManager.getInstance().getSelected().getObject_number()+"]", IO.TAG_ERROR);
                                                return null;
                                            });
                                        } catch (IOException e)
                                        {
                                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                            e.printStackTrace();
                                        }

                                        Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(quote);
                                        getTableView().refresh();
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
                QuoteManager.getInstance().setSelected(tblQuotes.getSelectionModel().getSelectedItem()));

        //set list of latest Quotes
        HashMap latest_revs =QuoteManager.getInstance().getLatestRevisions();
        if(latest_revs!=null)
        {
            tblQuotes.setItems(FXCollections.observableArrayList(latest_revs.values()));
            tblQuotes.refresh();
        }
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading quotes data model..");

        EmployeeManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        QuoteManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        QuoteManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        //register tab controller
        OperationsController.registerTabController(quotesTab.getId(),this);
        //refresh data-set and GUI
        new Thread(() ->
                refreshModel(cb->{Platform.runLater(() -> refreshView());return null;})).start();
    }

    public Callback getContextSearchCallback()
    {
        return txtSearch ->
        {
            if(txtSearch!=null)
            {
                ClientManager.getInstance().initialize();
                TextFields.bindAutoCompletion((TextField) txtSearch, ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
                {
                    if (event != null)
                    {
                        if (event.getCompletion() != null)
                        {
                            Client selected_client = (Client) event.getCompletion();
                            HashMap<String, Quote> selected_client_quotes = new HashMap<>();
                            for(Quote quote: QuoteManager.getInstance().getLatestRevisions().values())
                                if(quote.getClient_id().equals(selected_client.get_id()))
                                    selected_client_quotes.putIfAbsent(quote.get_id(), quote);

                            tblQuotes.setItems(FXCollections.observableArrayList(selected_client_quotes.values()));
                            tblQuotes.refresh();
                            //itemsModified = true;
                        }
                    }
                });
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "context search TextField is null");
            return null;
        };
    }

    public Callback getContextSearchResetCallback()
    {
        return param ->
        {
            if(QuoteManager.getInstance().getLatestRevisions()!=null)
            {
                tblQuotes.setItems(FXCollections.observableArrayList(QuoteManager.getInstance().getLatestRevisions().values()));
                tblQuotes.refresh();
            }
            return null;
        };
    }
    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}