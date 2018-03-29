/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.*;
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
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SuppliersController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Supplier>    tblSuppliers;
    @FXML
    private TableColumn     colSupplierId,colSupplierName,colSupplierPhysicalAddress,colSupplierSpeciality,
                            colSupplierPostalAddress,colSupplierTel,colSupplierFax,colSupplierEmail,colSupplierActive,
                            colSupplierDatePartnered,colSupplierWebsite,colSupplierRegistration,colSupplierVat,colSupplierAccount,colSupplierOther,colAction;
    @FXML
    private Tab suppliersTab;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        OperationsController.registerTabController(suppliersTab.getId(),this);
        new Thread(() ->
                refreshModel(param ->
                {
                    Platform.runLater(() -> refreshView());
                    return null;
                })).start();
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading suppliers view..");

        if(SupplierManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No suppliers found in the database.", IO.TAG_WARN);
            return;
        }
        Supplier[] suppliers = new Supplier[SupplierManager.getInstance().getDataset().values().toArray().length];
        SupplierManager.getInstance().getDataset().values().toArray(suppliers);

        colSupplierId.setMinWidth(100);
        colSupplierId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colSupplierName, TextFieldTableCell.forTableColumn(), 100, "supplier_name", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierPhysicalAddress, TextFieldTableCell.forTableColumn(), 120, "physical_address", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierPostalAddress, TextFieldTableCell.forTableColumn(), 120, "postal_address", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierTel, TextFieldTableCell.forTableColumn(), 80, "tel", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierFax, TextFieldTableCell.forTableColumn(), 80, "fax", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierEmail, TextFieldTableCell.forTableColumn(), 80, "contact_email", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierSpeciality, TextFieldTableCell.forTableColumn(), 80, "speciality", SupplierManager.getInstance());
        CustomTableViewControls.makeCheckboxedTableColumn(colSupplierActive, null, 80, "active", "/supplier");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colSupplierDatePartnered, "date_partnered");
        CustomTableViewControls.makeEditableTableColumn(colSupplierWebsite, TextFieldTableCell.forTableColumn(), 100, "website", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierRegistration, TextFieldTableCell.forTableColumn(), 100, "registration_number", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierVat, TextFieldTableCell.forTableColumn(), 100, "vat_number", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierAccount, TextFieldTableCell.forTableColumn(), 100, "account_name", SupplierManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSupplierOther, TextFieldTableCell.forTableColumn(), 80, "other", SupplierManager.getInstance());

        final ScreenManager screenManager = ScreenManager.getInstance();
        Callback<TableColumn<Supplier, String>, TableCell<Supplier, String>> cellFactory
                =
                new Callback<TableColumn<Supplier, String>, TableCell<Supplier, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Supplier, String> param)
                    {
                        final TableCell<Supplier, String> cell = new TableCell<Supplier, String>()
                        {
                            final Button btnView = new Button("View");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnView.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnView.getStyleClass().add("btnApply");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnView, btnRemove);
                                    Supplier supplier = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        SupplierManager.getInstance().setSelected(supplier);
                                        screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        if(getTableView().getItems().get(getIndex())!=null)
                                            if(getTableView().getItems().get(getIndex()) instanceof Supplier)
                                                SupplierManager.getInstance().setSelected(getTableView().getItems().get(getIndex()));

                                        try
                                        {
                                            //remove Supplier from remote server
                                            SupplierManager.getInstance().deleteObject(SupplierManager.getInstance().getSelected(), supplier_id->
                                            {
                                                if(supplier_id != null)
                                                {
                                                    IO.logAndAlert("Success", "Successfully deleted supplier [#" + SupplierManager.getInstance().getSelected().getObject_number() + "]{"+supplier_id+"}", IO.TAG_INFO);
                                                    //remove Supplier from memory
                                                    SupplierManager.getInstance().getDataset().remove(SupplierManager.getInstance().getSelected());
                                                    //remove Supplier from table
                                                    tblSuppliers.getItems().remove(SupplierManager.getInstance().getSelected());
                                                    tblSuppliers.refresh();//update table
                                                } else IO.logAndAlert("Error", "Could not delete supplier [#"+SupplierManager.getInstance().getSelected().getObject_number()+"]{"+supplier_id+"}", IO.TAG_ERROR);
                                                return null;
                                            });
                                        } catch (IOException e)
                                        {
                                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                            e.printStackTrace();
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

        ObservableList<Supplier> lst_suppliers = FXCollections.observableArrayList();
        lst_suppliers.addAll(suppliers);
        tblSuppliers.setItems(lst_suppliers);

        tblSuppliers.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                SupplierManager.getInstance().setSelected(tblSuppliers.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading suppliers data model..");

        SupplierManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        SupplierManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void createSupplierClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_SUPPLIER.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_SUPPLIER.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_SUPPLIER.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load supplier creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
