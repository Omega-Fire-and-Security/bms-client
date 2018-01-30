/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.PDF;
import fadulousbms.auxilary.PDFViewer;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.InvoiceManager;
import fadulousbms.managers.PurchaseOrderManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class PurchaseOrdersController extends ScreenController implements Initializable
{
    @FXML
    private TableView tblPurchaseOrders;
    @FXML
    private TableColumn colId,colPONumber,colSupplier,colDateLogged,colStatus,colCreator,colVat,colTotal,colExtra,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading purchase orders view..");

        if(SupplierManager.getInstance().getSuppliers()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No suppliers were found in the database.", IO.TAG_WARN);
            return;
        }
        Supplier[] suppliers = new Supplier[SupplierManager.getInstance().getSuppliers().values().toArray().length];
        SupplierManager.getInstance().getSuppliers().values().toArray(suppliers);

        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colId.setPrefWidth(100);
        colPONumber.setCellValueFactory(new PropertyValueFactory<>("object_number"));
        colPONumber.setPrefWidth(80);

        colSupplier.setMinWidth(120);
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier_name"));
        //colSupplier.setCellFactory(col -> new ComboBoxTableCell(SupplierManager.getInstance().getSuppliers(), "supplier_id", "/purchaseorders"));
        //CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 80, "account", "/api/purchaseorder");
        //CustomTableViewControls.makeEditableTableColumn(colVat, TextFieldTableCell.forTableColumn(), 50, "vat", "/api/purchaseorder");
        colVat.setCellValueFactory(new PropertyValueFactory<>("vat"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged", false);
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,100, "status", new String[]{"0","PENDING","1","APPROVED"}, false,"/purchaseorders");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator_name"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 215, "extra", "/purchaseorders");

        ObservableList<PurchaseOrder> lst_po = FXCollections.observableArrayList();
        lst_po.addAll(PurchaseOrderManager.getInstance().getPurchaseOrders().values());
        tblPurchaseOrders.setItems(lst_po);

        Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>> cellFactory
                =
                new Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<PurchaseOrder, String> param)
                    {
                        final TableCell<PurchaseOrder, String> cell = new TableCell<PurchaseOrder, String>()
                        {
                            final Button btnView = new Button("View PO");
                            final Button btnPDF = new Button("View as PDF");
                            final Button btnEmail = new Button("eMail PO");
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
                                    if (getTableView().getItems().get(getIndex()).getStatus()==BusinessObject.STATUS_APPROVED)
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
                                    PurchaseOrder po = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        if(po==null)
                                        {
                                            IO.logAndAlert("Error", "Purchase Order object is not set", IO.TAG_ERROR);
                                            return;
                                        }

                                        ScreenManager.getInstance().showLoadingScreen(param ->
                                        {
                                            new Thread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    try
                                                    {
                                                        //refresh model
                                                        PurchaseOrderManager.getInstance().reloadDataFromServer();
                                                        if(PurchaseOrderManager.getInstance().getPurchaseOrders()!=null)
                                                            PurchaseOrderManager.getInstance().setSelected(PurchaseOrderManager.getInstance().getPurchaseOrders().get(po.get_id()));
                                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any purchase orders in server.");

                                                        //load PO viewer
                                                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_PURCHASE_ORDER.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_PURCHASE_ORDER.getScreen())))
                                                        {
                                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_PURCHASE_ORDER.getScreen()));
                                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchase order viewer screen.");
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
                                            }).start();
                                            return null;
                                        });
                                    });

                                    btnPDF.setOnAction(event ->
                                    {
                                        if (po == null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Purchase Order object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        PurchaseOrderManager.getInstance().setSelected(po);
                                        try
                                        {
                                            String path = PDF.createPurchaseOrderPdf(po);
                                            if(path!=null)
                                            {
                                                PDFViewer pdfViewer = PDFViewer.getInstance();
                                                pdfViewer.setVisible(true);
                                                pdfViewer.doOpen(path);
                                            } else IO.logAndAlert("Error", "Could not get path of generated Purchase Order PDF document.", IO.TAG_ERROR);
                                        } catch (IOException e)
                                        {
                                            IO.logAndAlert("IO Error " + getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                                            e.printStackTrace();
                                        }
                                    });

                                    btnEmail.setOnAction(event ->
                                    {
                                        if (po == null)
                                        {
                                            IO.logAndAlert("Error", "Purchase Order object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        try
                                        {
                                            PurchaseOrderManager.getInstance().emailBusinessObject(po, PDF.createPurchaseOrderPdf(po), null);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(po);
                                        getTableView().refresh();
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

        colAction.setMinWidth(400);
        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);

        tblPurchaseOrders.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                PurchaseOrderManager.getInstance().setSelected((PurchaseOrder) tblPurchaseOrders.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        PurchaseOrderManager.getInstance().initialize();
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
            if(PurchaseOrderManager.getInstance().getPurchaseOrders()!=null)
                Platform.runLater(() -> refreshView());
        }).start();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
