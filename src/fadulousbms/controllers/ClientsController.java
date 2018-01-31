/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.*;
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
public class ClientsController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Client>    tblClients;
    @FXML
    private TableColumn     colClientId,colClientName,colClientPhysicalAddress,
                            colClientPostalAddress,colClientTel,colClientFax,colClientEmail,colClientRegistration,
                            colClientVat,colClientAccount,colClientDatePartnered,colClientWebsite,colClientActive,colClientOther,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients view..");
        if( ClientManager.getInstance().getClients()==null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No clients were found in the database.", IO.TAG_WARN);
            return;
        }

        colClientId.setMinWidth(100);
        colClientId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colClientName, TextFieldTableCell.forTableColumn(), 80, "client_name", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientPhysicalAddress, TextFieldTableCell.forTableColumn(), 120, "physical_address", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientPostalAddress, TextFieldTableCell.forTableColumn(), 120, "postal_address", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientTel, TextFieldTableCell.forTableColumn(), 80, "tel", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientFax, TextFieldTableCell.forTableColumn(), 80, "fax", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientEmail, TextFieldTableCell.forTableColumn(), 80, "contact_email", "/clients");
        CustomTableViewControls.makeCheckboxedTableColumn(colClientActive, null, 80, "active", "/clients");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colClientDatePartnered, "date_partnered");
        CustomTableViewControls.makeEditableTableColumn(colClientWebsite, TextFieldTableCell.forTableColumn(), 100, "website", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientRegistration, TextFieldTableCell.forTableColumn(), 100, "registration_number", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientVat, TextFieldTableCell.forTableColumn(), 100, "vat_number", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientAccount, TextFieldTableCell.forTableColumn(), 100, "account_name", "/clients");
        CustomTableViewControls.makeEditableTableColumn(colClientOther, TextFieldTableCell.forTableColumn(), 50, "other", "/clients");

        ObservableList<Client> lst_clients = FXCollections.observableArrayList();
        lst_clients.addAll(ClientManager.getInstance().getClients().values());
        tblClients.setItems(lst_clients);

        final ScreenManager screenManager = ScreenManager.getInstance();
        Callback<TableColumn<Client, String>, TableCell<Client, String>> cellFactory
                =
                new Callback<TableColumn<Client, String>, TableCell<Client, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Client, String> param)
                    {
                        final TableCell<Client, String> cell = new TableCell<Client, String>()
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
                                    Client client = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        ClientManager.getInstance().setSelected(client);
                                        screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(client);
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

        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);

        tblClients.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                ClientManager.getInstance().setSelected(tblClients.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients data model..");
        ClientManager.getInstance().initialize();
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

    @FXML
    public void createClientClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_CLIENT.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_CLIENT.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_CLIENT.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load client creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
