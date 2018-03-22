/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.FadulousBMS;
import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public abstract class QuoteController extends ScreenController implements Initializable
{
    protected boolean itemsModified;

    @FXML
    protected TableView<QuoteItem> tblQuoteItems;
    @FXML
    protected TableView<QuoteService> tblQuoteServices;
    @FXML
    protected TableColumn colMarkup,colQuantity, colItemNumber, colEquipmentName, colDescription, colCategory, colUnit, colUnitCost, colValue, colRate, colTotal, colAction, colServiceAction;
    @FXML
    protected TextField txtCell, txtTel, txtTotal, txtQuoteId, txtEmail, txtSite, txtDateGenerated, txtStatus,txtRevision, txtClient, txtMaterials, txtContactPerson;
    @FXML
    protected TextArea txtRequest, txtNotes;
    @FXML
    protected ToggleButton toggleVatExempt;
    @FXML
    protected ComboBox<String> cbxAccount;
    @FXML
    protected Label lblVat;
    @FXML
    protected Button btnApprove, btnNewMaterial, btnNewService, btnNewClient, btnNewClientRepresentative;
    protected HashMap<String, TableColumn> colsMap = new HashMap<>();
    protected ObservableList<TableColumn<QuoteItem, ?>> default_cols;

    protected Client selected_client = null;
    protected Resource selected_material = null;
    protected Employee selected_contact_person = null;
    protected ResourceType selected_material_type = null;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //colAction.setCellFactory(new ButtonTableCellFactory<>());
        //colAction.setCellValueFactory(new PropertyValueFactory<>(""));

        Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>> cellFactory
                =
                new Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<QuoteItem, String> param)
                    {
                        final TableCell<QuoteItem, String> cell = new TableCell<QuoteItem, String>()
                        {
                            final Button btnAdd = new Button("Add materials");
                            final Button btnRemove = new Button("Remove item");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnAdd.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnAdd.getStyleClass().add("btnAdd");
                                btnAdd.setMinWidth(100);
                                btnAdd.setMinHeight(35);
                                HBox.setHgrow(btnAdd, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnAdd, btnRemove);

                                    btnAdd.setOnAction(event ->
                                    {
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        addQuoteItemAdditionalMaterial(quoteItem);
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //TODO: deal with server side
                                        QuoteItem quoteItem = getTableView().getItems().get(getIndex());
                                        //remove QuoteItem's additional costs TableColumns
                                        if (quoteItem.getAdditional_costs() != null)
                                        {
                                            for (String str_cost : quoteItem.getAdditional_costs().split(";"))
                                            {
                                                String[] arr = str_cost.split("=");
                                                if (arr != null)
                                                {
                                                    if (arr.length > 1)
                                                    {
                                                        //if column exists in map, remove it
                                                        if(colsMap.get(arr[0].toLowerCase())!=null)
                                                        {
                                                            tblQuoteItems.getColumns().remove(colsMap.get(arr[0].toLowerCase()));
                                                            IO.log(getClass().getName(), IO.TAG_INFO, "removed QuoteItem["+quoteItem.get_id()+"] additional cost column: "+arr[0]);
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if(getTableView().getItems().remove(quoteItem))
                                            getTableView().setItems(getTableView().getItems());
                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not remove quote item: " + quoteItem);
                                        getTableView().refresh();
                                        IO.log(getClass().getName(), IO.TAG_INFO, "removed QuoteItem["+quoteItem.get_id()+"]");
                                        //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Quote)QuoteManager.getInstance().getSelected()).getTotal()));
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

        Callback<TableColumn<QuoteService, String>, TableCell<QuoteService, String>> servicesActionColCellFactory
                =
                new Callback<TableColumn<QuoteService, String>, TableCell<QuoteService, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<QuoteService, String> param)
                    {
                        final TableCell<QuoteService, String> cell = new TableCell<QuoteService, String>()
                        {
                            final Button btnNewServiceItem = new Button("New Service Item");
                            final Button btnShow = new Button("Show Service Items");
                            final Button btnRemove = new Button("Remove");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                                btnNewServiceItem.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnNewServiceItem.getStyleClass().add("btnAdd");
                                btnNewServiceItem.setMinWidth(150);
                                btnNewServiceItem.setMinHeight(35);
                                HBox.setHgrow(btnNewServiceItem, Priority.ALWAYS);

                                btnShow.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnShow.getStyleClass().add("btnDefault");
                                btnShow.setMinWidth(150);
                                btnShow.setMinHeight(35);
                                HBox.setHgrow(btnShow, Priority.ALWAYS);

                                btnRemove.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(150);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                HBox hBox = new HBox(btnNewServiceItem, btnShow, btnRemove);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                } else
                                {
                                    btnNewServiceItem.setOnAction(new EventHandler<ActionEvent>()
                                    {
                                        @Override
                                        public void handle(ActionEvent event)
                                        {
                                            QuoteService quoteService = getTableView().getItems().get(getIndex());
                                            if (quoteService != null)
                                            {
                                                ServiceManager.getInstance().setSelected(quoteService.getService());
                                            } else return;

                                            FXMLLoader loader = new FXMLLoader();
                                            loader.setLocation(FadulousBMS.class.getResource("views/NewServiceItem.fxml"));
                                            try
                                            {
                                                VBox page = loader.load();
                                                if(page!=null)
                                                {
                                                    PopOver popover = new PopOver(page);
                                                    popover.setTitle("Create Service Item");
                                                    popover.show(btnNewServiceItem);

                                                    popover.focusedProperty().addListener(new ChangeListener<Boolean>()
                                                    {
                                                        @Override
                                                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue)
                                                        {
                                                            IO.log(getClass().getName(), IO.TAG_VERBOSE, "reloading services combo box");
                                                            //reload services, to load the newly added service
                                                            ServiceManager.getInstance().forceSynchronise();

                                                            /*Platform.runLater(() ->
                                                            {
                                                                //refresh services combobox
                                                                if(ServiceManager.getInstance().getDataset()!=null)
                                                                    cbxServices.setItems(FXCollections.observableArrayList(ServiceManager.getInstance().getDataset().values()));
                                                            });*/
                                                        }
                                                    });
                                                }else IO.logAndAlert("Error", "Could not load service creation screen.", IO.TAG_ERROR);
                                            } catch (IOException e)
                                            {
                                                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                            }
                                        }
                                    });

                                    btnShow.setOnAction(event ->
                                    {
                                        if(getTableView().getItems().get(getIndex())!=null)
                                        {
                                            QuoteService quoteService = getTableView().getItems().get(getIndex());
                                            Service service = quoteService.getService();
                                            if (service != null)
                                            {
                                                ServiceManager.getInstance().setSelected(service);

                                                TableView<ServiceItem> tblServiceItems = new TableView<>();
                                                tblServiceItems.setTableMenuButtonVisible(true);
                                                TableColumn colName = new TableColumn("Item Name");
                                                colName.setCellValueFactory(new PropertyValueFactory<>("item_name"));
                                                colName.setMinWidth(150);

                                                TableColumn colItemDescription = new TableColumn("Item Description");
                                                colItemDescription.setCellValueFactory(new PropertyValueFactory<>("item_description"));
                                                colItemDescription.setMinWidth(100);
                                                colItemDescription.setVisible(false);

                                                TableColumn colRate = new TableColumn("Item Rate");
                                                colRate.setCellValueFactory(new PropertyValueFactory<>("item_rate"));
                                                colRate.setMinWidth(80);

                                                TableColumn colUnit = new TableColumn("Unit");
                                                colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
                                                colUnit.setMinWidth(70);

                                                TableColumn colQuantity = new TableColumn("Quantity");
                                                colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
                                                colQuantity.setMinWidth(70);

                                                tblServiceItems.getColumns().add(colName);
                                                tblServiceItems.getColumns().add(colItemDescription);
                                                tblServiceItems.getColumns().add(colRate);
                                                tblServiceItems.getColumns().add(colUnit);
                                                tblServiceItems.getColumns().add(colQuantity);

                                                if (service.getServiceItemsMap() != null)
                                                {
                                                    tblServiceItems.setItems(FXCollections.observableArrayList(service.getServiceItemsMap().values()));

                                                    PopOver popover = new PopOver(new VBox(new Label("List of Service Items"), tblServiceItems));
                                                    popover.setMinWidth(500);
                                                    popover.setTitle("Service Items");
                                                    popover.show(btnShow);

                                                } else
                                                    IO.logAndAlert("Error", "Could not find any service items.", IO.TAG_ERROR);
                                            }else
                                                IO.logAndAlert("Error", "Selected service is invalid.", IO.TAG_ERROR);
                                        }
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        QuoteService qservice = getTableView().getItems().get(getIndex());
                                        if(qservice!=null)
                                        {
                                            getTableView().getItems().remove(qservice);
                                            getTableView().refresh();
                                            //TODO: remove from server
                                            System.out.println("Successfully removed service: " + qservice.getService().getService_title());
                                        } else
                                            IO.logAndAlert("Error", "Selected service is invalid.", IO.TAG_ERROR);
                                    });

                                    setGraphic(hBox);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };
        colServiceAction.setMinWidth(400);
        colServiceAction.setCellFactory(servicesActionColCellFactory);
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
        if( ResourceManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getName(), "no resources were found in the database.", IO.TAG_WARN);
            //return;
        }
        if( ServiceManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert(getClass().getName(), "no services were found in the database.", IO.TAG_WARN);
            //return;
        }

        Employee[] employees=null;
        if(EmployeeManager.getInstance().getDataset()!=null)
        {
            employees = new Employee[EmployeeManager.getInstance().getDataset().size()];
            EmployeeManager.getInstance().getDataset().values().toArray(employees);
        }

        //setup Quote default accounts
        cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", "Credit"}));
        cbxAccount.getSelectionModel().select(0);

        refreshTotal();
        toggleVatExempt.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue)
                toggleVatExempt.setText("VAT exempt");
            else toggleVatExempt.setText(QuoteManager.VAT+ "%");
            refreshTotal();
        });

        tblQuoteItems.getItems().clear();
        tblQuoteServices.getItems().clear();

        //Setup Quote Items table
        colCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        colCategory.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if(tblQuoteItems.getItems()!=null)
                {
                    if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size()) {
                        QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                        //update QuoteItem object on TextField commit
                        txt.setOnKeyPressed(event ->
                        {
                            //when the user presses the enter key, update the QuoteItem category
                            if (event.getCode() == KeyCode.ENTER) {
                                //get selected QuoteItem
                                QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                                //apply new category
                                quote_item.setCategory(txt.getText());
                                //refresh the table
                                tblQuoteItems.refresh();
                                IO.log(getClass().getName(), IO.TAG_INFO, "Successfully updated [category] property for quote item #" + quote_item.getItem_number());
                            }
                        });

                        if (!empty) {
                            txt.setText(quoteItem.getCategory());
                            setGraphic(txt);
                        } else setGraphic(null);
                        getTableView().refresh();
                    }
                }
            }
        });

        colUnitCost.setCellValueFactory(new PropertyValueFactory<>("unit_cost"));
        colUnitCost.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                {
                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                quote_item.setUnit_cost(Double.valueOf(txt.getText()));
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(getTableView().getItems(), tblQuoteServices.getItems())));
                                tblQuoteItems.refresh();
                                //RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", "markup");
                                //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));
                                //tblJobItems.refresh();
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid markup percentage.", IO.TAG_ERROR);
                                return;
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO,"Successfully updated [markup percentage] property for quote item #" + quote_item.getItem_number());
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(String.valueOf(quoteItem.getUnit_Cost()));
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        colMarkup.setCellValueFactory(new PropertyValueFactory<>("markup"));
        colMarkup.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                {
                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                quote_item.setMarkup(Double.valueOf(txt.getText()));
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(getTableView().getItems(), tblQuoteServices.getItems())));
                                tblQuoteItems.refresh();
                                //RemoteComms.updateBusinessObjectOnServer(quote_item, "/api/quote/resource", "markup");
                                //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));
                                //tblJobItems.refresh();
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid markup percentage.", IO.TAG_ERROR);
                                return;
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO,"Successfully updated [markup percentage] property for quote item #" + quote_item.getItem_number());
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(quoteItem.getMarkup());
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        colQuantity.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                {
                    QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem quote_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                quote_item.setQuantity(Integer.valueOf(txt.getText()));
                                //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(getTableView().getItems())));
                                refreshTotal();
                                tblQuoteItems.refresh();
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid quantity.", IO.TAG_ERROR);
                                return;
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO,"Successfully updated [quantity] property for quote item #" + quote_item.getItem_number());
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(quoteItem.getQuantity());
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        TextFields.bindAutoCompletion(txtClient, (Collection<Client>)ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_client = event.getCompletion();

                    IO.log(getClass().getName(), IO.TAG_INFO, "selected client id: " + selected_client.get_id());
                    cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", selected_client.getAccount_name()}));
                    //txtFax.setText(selected_client.getFax());
                    itemsModified = true;
                }
            }
        });

        TextFields.bindAutoCompletion(txtMaterials, ResourceManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    //update selected material
                    selected_material = event.getCompletion();
                    IO.log(getClass().getName(), IO.TAG_INFO, "selected resource: " + selected_material.getResource_description());
                    itemsModified = true;
                }
            }
        });

        TextFields.bindAutoCompletion(txtContactPerson, EmployeeManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_contact_person = event.getCompletion();
                    IO.log(getClass().getName(), IO.TAG_INFO, "selected contact person: " + selected_contact_person.getName());
                    txtCell.setText(selected_contact_person.getCell());
                    txtTel.setText(selected_contact_person.getTel());
                    txtEmail.setText(selected_contact_person.getEmail());
                    itemsModified = true;
                } else IO.logAndAlert("Invalid Employee", "Selected contact person is invalid", IO.TAG_ERROR);
            }
        });

        if(default_cols==null)
            default_cols=tblQuoteItems.getColumns();

        //set status
        String status;
        if(QuoteManager.getInstance().getSelected()!=null)
        {
            switch (((Quote)QuoteManager.getInstance().getSelected()).getStatus())
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
                    IO.logAndAlert("Error", "Unknown Quote status: " + ((Quote)QuoteManager.getInstance().getSelected()).getStatus(), IO.TAG_ERROR);
                    break;
            }
            if(txtStatus!=null)
                txtStatus.setText(status);
        }
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading quotes data model.");

        EmployeeManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        ServiceManager.getInstance().initialize();
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

    protected void refreshTotal()
    {
        double vat = QuoteManager.VAT;
        if(toggleVatExempt.isSelected())//if is VAT exempt
            vat =0.0;//is VAT exempt
        lblVat.setText("VAT ["+new DecimalFormat("##.##").format(vat)+"%]");
        if(tblQuoteItems.getItems()!=null)
        {
            double total = QuoteManager.computeQuoteTotal(tblQuoteItems.getItems(), tblQuoteServices.getItems());

            //render total excluding tax
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                    new DecimalFormat("##.##").format((total)));
        }
    }

    public Callback getAdditionalCostCallback(TableColumn col)
    {
        return new Callback<TableColumn<QuoteItem, String>, TableCell<QuoteItem, String>>()
        {
            @Override
            public TableCell<QuoteItem, String> call(TableColumn<QuoteItem, String> param)
            {
                return new TableCell<QuoteItem, String>()
                {
                    final TextField txtCost = new TextField("0.0");
                    final TextField txtMarkup = new TextField("0.0");

                    @Override
                    protected void updateItem(String item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (getIndex() >= 0 && getIndex() < tblQuoteItems.getItems().size())
                        {
                            QuoteItem quoteItem = tblQuoteItems.getItems().get(getIndex());
                            //update QuoteItem object on TextField commit
                            Callback callback = new Callback()
                            {
                                @Override
                                public Object call(Object param)
                                {
                                    if (quoteItem != null)
                                    {
                                        String new_cost = col.getText().toLowerCase() + "=" + txtCost.getText() + "*" + txtMarkup.getText();
                                        String old_add_costs = "";
                                        if (quoteItem.getAdditional_costs() != null)
                                            old_add_costs = quoteItem.getAdditional_costs().toLowerCase();
                                        String new_add_costs = "";

                                        int old_var_index = old_add_costs.indexOf(col.getText().toLowerCase());
                                        if (old_var_index < 0)
                                        {
                                            //additional cost pair not found
                                            if (old_add_costs.isEmpty())
                                                //** key-value pair is the first and only pair - add it w/o semi-colon
                                                new_add_costs += new_cost;
                                            else
                                            /** Additional cost pair DNE but other additional costs exist
                                             * - append pair then add the rest of the pairs
                                             */
                                                new_add_costs += new_cost + ";" + old_add_costs;//.substring(old_add_costs.indexOf(';')-1)
                                        } else if (old_var_index == 0)
                                        {
                                            /** Additional cost key-value pair exists and is first pair. **/
                                            new_add_costs += new_cost;
                                            if (old_add_costs.indexOf(';') > 0)//if there are other pairs append them
                                                //copy from first occurrence of semi-colon - cutting the old record out.
                                                new_add_costs += ";" + old_add_costs.substring(old_add_costs.indexOf(';') + 1);
                                        }
                                        else
                                        {
                                            /** Additional cost key-value pair is not first - append to additional costs.**/
                                            //copy additional costs before current cost
                                            new_add_costs = old_add_costs.substring(0, old_var_index - 1);
                                            //append current cost
                                            new_add_costs += ";" + new_cost;
                                            //append additional costs after current cost
                                            int i = old_add_costs.substring(old_var_index).indexOf(';');
                                            new_add_costs += ";" + old_add_costs.substring(i + 1);
                                        }
                                        IO.log(getClass().getName(), IO.TAG_INFO, "committed additional costs for quote item [#" + quoteItem.getItem_number() + "]:: " + new_add_costs);
                                        quoteItem.setAdditional_costs(new_add_costs);
                                    }
                                    return null;
                                }
                            };

                            txtCost.setOnKeyPressed(event ->
                            {
                                if (event.getCode() == KeyCode.ENTER)
                                {
                                    callback.call(null);
                                    tblQuoteItems.refresh();
                                    txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(tblQuoteItems.getItems(), tblQuoteServices.getItems())));
                                }
                            });
                            txtMarkup.setOnKeyPressed(event ->
                            {
                                if (event.getCode() == KeyCode.ENTER)
                                {
                                    callback.call(null);
                                    tblQuoteItems.refresh();
                                    txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(tblQuoteItems.getItems(), tblQuoteServices.getItems())));
                                }
                            });

                            //render the cell
                            if (!empty)// && item!=null
                            {
                                if (quoteItem.getAdditional_costs() == null)
                                {
                                    txtCost.setText("0.0");
                                    txtMarkup.setText("0.0");
                                } else if (quoteItem.getAdditional_costs().length() <= 0)
                                {
                                    txtCost.setText("0.0");
                                    txtMarkup.setText("0.0");
                                } else if (quoteItem.getAdditional_costs().length() > 0)
                                {
                                    if(quoteItem.getAdditional_costs() != null)
                                    {
                                        for (String str_cost : quoteItem.getAdditional_costs().split(";"))
                                        {
                                            String[] arr = str_cost.split("=");
                                            if (arr != null)
                                            {
                                                if (arr.length > 1)
                                                {
                                                    if (arr[0].toLowerCase().equals(col.getText().toLowerCase()))
                                                    {
                                                        if(arr[1].contains("*"))
                                                        {
                                                            txtCost.setText(arr[1].split("\\*")[0]);
                                                            txtMarkup.setText(arr[1].split("\\*")[1]);
                                                        }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid quote item additional cost ["+str_cost+"].");
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                txtCost.setPrefWidth(50);
                                txtMarkup.setPrefWidth(50);
                                VBox vBoxCost = new VBox(new Label("Cost: "), txtCost);
                                vBoxCost.setAlignment(Pos.CENTER);
                                VBox.setMargin(txtCost, new Insets(0,10,0,10));
                                HBox.setHgrow(txtCost, Priority.ALWAYS);
                                HBox.setHgrow(vBoxCost, Priority.ALWAYS);

                                VBox vBoxMarkup = new VBox(new Label("Markup: "), txtMarkup);
                                vBoxMarkup.setAlignment(Pos.CENTER);
                                VBox.setMargin(txtMarkup, new Insets(0,10,0,10));
                                HBox.setHgrow(txtMarkup, Priority.ALWAYS);
                                HBox.setHgrow(vBoxMarkup, Priority.ALWAYS);

                                HBox hBox = new HBox(vBoxCost, vBoxMarkup);
                                hBox.setAlignment(Pos.CENTER);
                                HBox.setHgrow(hBox, Priority.ALWAYS);
                                setGraphic(hBox);
                            } else setGraphic(null);
                            getTableView().refresh();
                        } //else IO.log("Quote Materials Table", IO.TAG_ERROR, "index out of bounds [" + getIndex() + "]");
                    }
                };
            }
        };
    }

    public void addQuoteItemAdditionalMaterial(QuoteItem quoteItem)
    {
        Stage stage = new Stage();
        stage.setAlwaysOnTop(true);
        stage.setResizable(false);

        TextField txtName = new TextField();
        txtName.setMinWidth(150);
        Label lblName = new Label("Material name: ");
        lblName.setMinWidth(150);

        TextField txtCost = new TextField();
        txtCost.setMinWidth(150);
        Label lblCost = new Label("Material value: ");
        lblCost.setMinWidth(150);

        TextField txtMarkup = new TextField();
        txtMarkup.setMinWidth(150);
        Label lblMarkup = new Label("Markup [%]: ");
        lblMarkup.setMinWidth(150);

        Button btnAdd = new Button("Add");
        btnAdd.getStyleClass().add("btnAdd");
        btnAdd.setMinWidth(140);
        btnAdd.setOnAction(event ->
        {
            //validate cost name
            if(txtName.getText()!=null)
            {
                if (txtName.getText().isEmpty())
                {
                    IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost name.", IO.TAG_ERROR);
                    return;
                }
            }else{
                IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost name.", IO.TAG_ERROR);
                return;
            }
            //validate cost value
            if(txtCost.getText()!=null)
            {
                if (txtCost.getText().isEmpty())
                {
                    IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost value.", IO.TAG_ERROR);
                    return;
                }
            }else{
                IO.logAndAlert("New Additional Cost Error", "Please enter a valid cost value.", IO.TAG_ERROR);
                return;
            }

            String new_cost = txtName.getText()+"="+txtCost.getText()+"*"+txtMarkup.getText();
            if(quoteItem.getAdditional_costs()==null)
                quoteItem.setAdditional_costs(new_cost);
            else if(quoteItem.getAdditional_costs().isEmpty())
                quoteItem.setAdditional_costs(new_cost);
            else
            {
                //if additional cost exists already, update its value
                if(quoteItem.getAdditional_costs().toLowerCase().contains(txtName.getText().toLowerCase()))
                {
                    String old_add_costs = quoteItem.getAdditional_costs().toLowerCase();
                    String new_add_costs="";
                    int old_var_index = old_add_costs.indexOf(txtName.getText().toLowerCase());
                    if(old_var_index==0)
                    {
                        //key-value pair is first add it w/o semi-colon
                        new_add_costs += new_cost;
                    }else
                    {
                        new_add_costs = old_add_costs.substring(0, old_var_index);
                        new_add_costs += ";" + new_cost;
                    }
                    quoteItem.setAdditional_costs(new_add_costs);
                } else quoteItem.setAdditional_costs(quoteItem.getAdditional_costs()+";"+new_cost);
            }

            //RemoteComms.updateBusinessObjectOnServer(quoteItem, "/api/quote/resource", txtName.getText());

            TableColumn<QuoteItem, String> col = new TableColumn(txtName.getText());
            col.setPrefWidth(80);
            col.setCellFactory(getAdditionalCostCallback(col));

            colsMap.put(txtName.getText().toLowerCase(), col);
            /*boolean found=false;
            for(TableColumn c: tblJobItems.getColumns())
                if(col.getText().toLowerCase().equals(c.getText().toLowerCase()))
                {
                    found=true;
                    break;
                }
            if(!found)
                tblJobItems.getColumns().add(col);
            tblJobItems.refresh();*/
            tblQuoteItems.getColumns().add(col);
            tblQuoteItems.refresh();

            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(tblQuoteItems.getItems(), tblQuoteServices.getItems())));
        });

        HBox row1 = new HBox(lblName, txtName);
        HBox row2 = new HBox(lblCost, txtCost);
        HBox row3 = new HBox(lblMarkup, txtMarkup);
        row1.setSpacing(20);
        row2.setSpacing(20);
        row3.setSpacing(20);
        //HBox row3 = new HBox(new Label("Markup"), txtMarkup);

        stage.setTitle("Extra Costs For Quote Item #"+quoteItem.getItem_numberValue());
        stage.setScene(new Scene(new VBox(row1, row2, row3, btnAdd)));
        stage.show();
    }

    public void addAdditionalCostColToMap(String id, TableColumn col)
    {
        colsMap.put(id, col);
    }

    @FXML
    public void newQuoteItem()
    {
        if(ResourceManager.getInstance()!=null)
        {
            if(ResourceManager.getInstance().getDataset()!=null)
            {
                if(ResourceManager.getInstance().getDataset().size()>0)
                {
                    File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                    ComboBox<Resource> resourceComboBox = new ComboBox<>();
                    resourceComboBox.setMinWidth(240);
                    resourceComboBox.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getDataset().values()));
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnDefault");
                    btnAdd.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                    Button btnNewMaterial = new Button("New Material");
                    btnNewMaterial.setMinWidth(130);
                    btnNewMaterial.setMinHeight(40);
                    btnNewMaterial.setDefaultButton(true);
                    btnNewMaterial.getStyleClass().add("btnAdd");
                    btnNewMaterial.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                    HBox hBox = new HBox(new Label("Material: "), resourceComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);
                    hBox.setSpacing(20);
                    HBox.setMargin(hBox, new Insets(0, 0, 0, 20));

                    HBox hBoxButtons = new HBox(btnAdd, btnNewMaterial, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);
                    HBox.setMargin(hBoxButtons, new Insets(0, 0, 0, 20));

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setMaxWidth(350);
                    stage.setTitle("Add Quote Materials");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.setResizable(false);
                    stage.show();

                    btnAdd.setOnAction(event ->
                    {
                        if(resourceComboBox.getValue()!=null)
                        {
                            QuoteItem quoteItem = new QuoteItem();

                            quoteItem.setItem_number(tblQuoteItems.getItems().size());
                            quoteItem.setQuantity(1);
                            quoteItem.setUnit_cost(resourceComboBox.getValue().getResource_value());
                            quoteItem.setMarkup(0);
                            quoteItem.setResource_id(resourceComboBox.getValue().get_id());
                            //quoteItem.setEquipment_description(resourceComboBox.getValue().getResource_description());
                            //quoteItem.setUnit(resourceComboBox.getValue().getUnit());
                            //quoteItem.setRate(resourceComboBox.getValue().getResource_value());
                            //quoteItem.setValue(resourceComboBox.getValue().getResource_value());
                            //quoteItem.setResource(resourceComboBox.getValue());
                            //quoteItem.setEquipment_name(resourceComboBox.getValue().getResource_name());

                            tblQuoteItems.getItems().add(quoteItem);
                            tblQuoteItems.refresh();

                            itemsModified = true;

                            //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                            //        String.valueOf(QuoteManager.computeQuoteTotal(QuoteManager.getInstance().getSelectedQuote())));
                            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue()+" "+String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems(), tblQuoteServices.getItems())));

                        } else IO.logAndAlert("New Quote Resource", "Invalid resource selected.", IO.TAG_ERROR);
                    });

                    btnNewMaterial.setOnAction(event ->
                    {
                        //close material addition window
                        stage.close();
                        ResourceManager.getInstance().newResourceWindow(param ->
                        {
                            //refresh model & view when material has been created.
                            new Thread(() ->
                                    refreshModel(param1 ->
                                    {
                                        Platform.runLater(() ->
                                        {
                                            refreshView();
                                            //show material addition window again after material has been created.
                                            newQuoteItem();
                                        });
                                        return null;
                                    })).start();
                            return null;
                        });
                    });

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("New Quote Resource", "No resources were found in the database, please add some resources first and try again.",IO.TAG_ERROR);
    }

    @FXML
    public void addMaterial()
    {
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

        if(selected_material!=null)
        {
            txtClient.getStyleClass().remove("control-input-error");
            txtClient.getStyleClass().add("text-field");

            QuoteItem quoteItem = new QuoteItem();

            //quoteItem.set_id(String.valueOf(System.currentTimeMillis()));
            quoteItem.setItem_number(tblQuoteItems.getItems().size());
            quoteItem.setQuantity(1);
            quoteItem.setUnit_cost(selected_material.getResource_value());
            quoteItem.setMarkup(0);
            quoteItem.setResource_id(selected_material.get_id());
            quoteItem.setCategory(selected_material.getResourceType());

            tblQuoteItems.getItems().add(quoteItem);
            tblQuoteItems.refresh();

            itemsModified = true;

            //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
            //        String.valueOf(QuoteManager.computeQuoteTotal(QuoteManager.getInstance().getSelectedQuote())));
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue()+" "+String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems(), tblQuoteServices.getItems())));

        } else
        {
            txtMaterials.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            txtClient.getStyleClass().remove("form-control-default");
            txtClient.getStyleClass().add("control-input-error");
            IO.logAndAlert("Warning", "Invalid resource.", IO.TAG_WARN);
        }
    }

    @FXML
    public void newMaterial()
    {
        selected_material = null;
        selected_material_type = null;

        TextField txt_mat_description = new TextField("");
        txt_mat_description.setMinWidth(120);
        txt_mat_description.setPromptText("Summary of material");
        Label lbl_des = new Label("Material Description*: ");
        lbl_des.setMinWidth(160);

        TextField txt_mat_category = new TextField("");
        txt_mat_category.setMinWidth(120);
        txt_mat_category.setPromptText("Material type e.g. Access Control Hardware");
        Label lbl_cat = new Label("Material Category*: ");
        lbl_cat.setMinWidth(160);

        TextField txt_mat_value = new TextField("");
        txt_mat_value.setMinWidth(120);
        txt_mat_value.setPromptText("Material cost excl. tax");
        Label lbl_val = new Label("Material Value*: ");
        lbl_val.setMinWidth(160);

        TextField txt_mat_unit = new TextField("");
        txt_mat_unit.setMinWidth(120);
        txt_mat_unit.setPromptText("Unit of measurement");
        Label lbl_unit = new Label("Material Unit*: ");
        lbl_unit.setMinWidth(160);

        Button btnSubmit = new Button("Create & Add Material");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(35);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(lbl_des, 0, 0);
        page.add(txt_mat_description, 1, 0);

        page.add(lbl_cat, 0, 1);
        page.add(txt_mat_category, 1, 1);

        page.add(lbl_val, 0, 2);
        page.add(txt_mat_value, 1, 2);

        page.add(lbl_unit, 0, 3);
        page.add(txt_mat_unit, 1, 3);

        page.add(btnSubmit, 0, 4);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create & Add Material");
        popover.setDetached(true);
        popover.show(btnNewMaterial);

        TextFields.bindAutoCompletion(txt_mat_category, ResourceManager.getInstance().getResource_types().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_material_type = event.getCompletion();
                }
            }
        });

        TextFields.bindAutoCompletion(txt_mat_description, ResourceManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    //update selected material
                    selected_material = event.getCompletion();

                    IO.log(getClass().getName(), IO.TAG_INFO, "auto-completed material: " + selected_material.getResource_description());
                    txt_mat_description.setText(selected_material.getResource_description());

                    if(ResourceManager.getInstance().getResource_types()!=null && selected_material.getResource_type()!=null)
                    {
                        selected_material_type = ResourceManager.getInstance().getResource_types().get(selected_material.getResource_type());
                        txt_mat_category.setText(selected_material_type.getType_name());
                    }
                    txt_mat_value.setText(String.valueOf(selected_material.getResource_value()));
                    txt_mat_unit.setText(selected_material.getUnit());
                }
            }
        });

        btnSubmit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if(SessionManager.getInstance().getActive()==null)
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }
                if(SessionManager.getInstance().getActive().isExpired())
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                if(!Validators.isValidNode(txt_mat_description, txt_mat_description.getText(), 1, ".+"))
                {
                    txt_mat_description.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }

                if(txt_mat_category.getText()==null)
                {
                    IO.logAndAlert("Error", "Invalid material category.\nPlease enter a valid value.", IO.TAG_WARN);
                    return;
                }

                if(txt_mat_category.getText().isEmpty())
                {
                    IO.logAndAlert("Error", "Invalid material category.\nPlease enter a valid value.", IO.TAG_WARN);
                    return;
                }

                if(!Validators.isValidNode(txt_mat_value, txt_mat_value.getText(), 1, ".+"))
                {
                    txt_mat_value.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }
                if(!Validators.isValidNode(txt_mat_unit, txt_mat_unit.getText(), 1, ".+"))
                {
                    txt_mat_unit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }

                String resource_type_id = null;

                if(selected_material_type!=null)
                {
                    /*
                        If category text is not exactly the same as the category text inputted in the material creation
                        Form then create new category/material type.
                     */
                    if(selected_material_type.getType_name().equals(txt_mat_category.getText()))
                        resource_type_id = selected_material_type.get_id();
                }

                Resource resource = new Resource();
                resource.setResource_description(txt_mat_description.getText());
                resource.setUnit(txt_mat_unit.getText());
                resource.setQuantity(Long.valueOf(1));
                resource.setDate_acquired(System.currentTimeMillis());
                resource.setCreator(SessionManager.getInstance().getActive().getUsr());
                try
                {
                    resource.setResource_value(Double.valueOf(txt_mat_value.getText()));
                } catch (NumberFormatException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    return;
                }
                /*
                    If selected_material_type is null then create new material type/category using inputted
                    Text from material creation form
                 */
                if(resource_type_id==null)
                {
                    //create new resource type/category
                    ResourceType resourceType = new ResourceType(txt_mat_category.getText(), "");
                    resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
                    try
                    {
                        ResourceManager.getInstance().createBusinessObject(resourceType, material_category_id ->
                        {
                            if(material_category_id!=null)
                            {
                                selected_material_type = ResourceManager.getInstance().getResource_types().get(material_category_id);

                                resource.setResource_type((String) material_category_id);

                                //create new material using new category
                                createMaterial(resource);
                            } else IO.logAndAlert("Error", "Could not create material category ["+txt_mat_category.getText()+"]", IO.TAG_ERROR);
                            return null;
                        });
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                } else //new material not in new category
                {
                    //create new material using selected category
                    resource.setResource_type(resource_type_id);
                    createMaterial(resource);
                }
            }
        });
    }

    public void createMaterial(Resource resource)
    {
        if(resource==null)
        {
            IO.logAndAlert("Error: Invalid Resource", "Resource to be created is invalid.", IO.TAG_ERROR);
            return;
        }
        try
        {
            String proceed = IO.OK;
            if(selected_material!=null)
                if(resource.getResource_description().equals(selected_material.getResource_description()))
                    proceed = IO.showConfirm("Duplicate material found, proceed?", "New material's description is the same as an existing material, continue with creation of material?");

            if(proceed.equals(IO.OK))
            {
                ResourceManager.getInstance().createBusinessObject(resource, new_res_id ->
                {
                    //update selected material
                    selected_material = ResourceManager.getInstance().getDataset().get(new_res_id);
                    //ResourceManager.getInstance().setSelected(ResourceManager.getInstance().getDataset().get(new_res_id));

                    //add material to QuoteItems table
                    addMaterial();

                    //refresh materials combobox
                    Platform.runLater(() ->
                    {
                        if (ResourceManager.getInstance().getDataset().values() != null)
                        {
                            TextFields.bindAutoCompletion(txtMaterials, FXCollections.observableArrayList());
                            TextFields.bindAutoCompletion(txtMaterials, ResourceManager.getInstance().getDataset().values()).setOnAutoCompleted(evt ->
                            {
                                if (evt != null)
                                {
                                    if (evt.getCompletion() != null)
                                    {
                                        //update selected material
                                        selected_material = evt.getCompletion();

                                        IO.log(getClass().getName(), IO.TAG_INFO, "selected material: " + selected_material.getResource_description());
                                        itemsModified = true;
                                    }
                                }
                            });
                        }
                    });
                    return null;
                });
            } else IO.log(getClass().getName(), IO.TAG_ERROR, "aborted material creation procedure.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void newRevision()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                //Update Quote if already been added
                /*if(txtQuoteId.getText()!=null)
                {
                    if(!txtQuoteId.getText().isEmpty())
                    {
                        newQuoteRevision();
                        return;
                    }
                }*/
                //force createQuote() to create new QuoteItems for new Quote revision
                for(QuoteItem quoteItem: tblQuoteItems.getItems())
                    quoteItem.set_id(null);
                for(QuoteService quoteService: tblQuoteServices.getItems())
                    quoteService.set_id(null);

                //else create new Quote
                createQuote();
            }else IO.showMessage("Error: Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Error: Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void approveQuote()
    {
        Quote selected = (Quote) QuoteManager.getInstance().getSelected();
        if(selected!=null)
        {
            if(selected.getStatus()!=Quote.STATUS_APPROVED)
            {
                selected.setStatus(Quote.STATUS_APPROVED);

                ObservableList<QuoteItem> quoteItems = tblQuoteItems.getItems();
                ObservableList<QuoteService> quoteServices = tblQuoteServices.getItems();
                if(!quoteItems.isEmpty() || !quoteServices.isEmpty())
                {
                    QuoteManager.getInstance().updateQuote(selected, quoteItems, tblQuoteServices.getItems(), false, param ->
                    {
                        refreshModel(arg ->
                        {
                            Platform.runLater(() -> refreshView());
                            return null;
                        });
                        return null;
                    });
                } else IO.logAndAlert("Error", "Quote has no materials", IO.TAG_WARN);
            } else IO.logAndAlert("Error", "Selected quote has already been approved.", IO.TAG_WARN);
        } else IO.logAndAlert("Error", "Selected quote is invalid.", IO.TAG_WARN);
    }

    @FXML
    public void createQuote()
    {
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

        txtClient.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(selected_client==null)
        {
            txtClient.getStyleClass().remove("form-control-default");
            txtClient.getStyleClass().add("control-input-error");
            IO.logAndAlert("Error: Invalid input", "Selected client is invalid.", IO.TAG_WARN);
            return;
        } else {
            txtClient.getStyleClass().remove("control-input-error");
            txtClient.getStyleClass().add("form-control-default");
        }

        txtContactPerson.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(selected_contact_person==null)
        {
            txtContactPerson.getStyleClass().remove("form-control-default");
            txtContactPerson.getStyleClass().add("control-input-error");
            IO.logAndAlert("Error: Invalid input", "Selected contact person is invalid.", IO.TAG_WARN);
            return;
        }else{
            txtContactPerson.getStyleClass().remove("control-input-error");
            txtContactPerson.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtCell, txtCell.getText(), 1, ".+"))
        {
            txtCell.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
        {
            txtEmail.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }

        cbxAccount.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxAccount.getValue()==null)
        {
            cbxAccount.getStyleClass().remove("form-control-default");
            cbxAccount.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxAccount.getStyleClass().remove("control-input-error");
            cbxAccount.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtSite, txtSite.getText(), 1, ".+"))
        {
            txtSite.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            IO.logAndAlert("Error: Invalid input", "Invalid site name.", IO.TAG_WARN);
            return;
        }
        if(!Validators.isValidNode(txtRequest, txtRequest.getText(), 1, ".+"))
        {
            txtRequest.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            IO.logAndAlert("Error: Invalid input", "Invalid request.", IO.TAG_WARN);
            return;
        }

        ObservableList<QuoteItem> quoteItems = tblQuoteItems.getItems();
        ObservableList<QuoteService> quoteServices = tblQuoteServices.getItems();

        if((quoteItems!=null?quoteItems.size()<=0:true) && (quoteServices!=null?quoteServices.size()<=0:true))
        {
            IO.logAndAlert("Invalid Quote", "Quote items and services list is empty", IO.TAG_ERROR);
            return;
        }

        //prepare quote attributes
        Quote quote = new Quote();
        quote.setClient_id(selected_client.get_id());
        quote.setContact_person_id(selected_contact_person.getUsr());
        quote.setSitename(txtSite.getText());
        quote.setRequest(txtRequest.getText());
        quote.setStatus(Quote.STATUS_PENDING);
        quote.setAccount_name(cbxAccount.getValue());
        quote.setCreator(SessionManager.getInstance().getActive().getUsr());
        quote.setRevision(1.0);

        //set current Quote as parent
        if(txtQuoteId.getText()!=null)
        {
            if (!txtQuoteId.getText().isEmpty())
            {
                //increment revision number by +1
                quote.setRevision(((Quote) QuoteManager.getInstance().getSelected()).getSiblingsMap().size()+1);
                if (((Quote) QuoteManager.getInstance().getSelected()).getParent_id() != null)//use selected Quote's parent for as new Quote's parent_id
                    quote.setParent_id(((Quote) QuoteManager.getInstance().getSelected()).getParent_id());
                else if (((Quote) QuoteManager.getInstance().getSelected()).getParent_id() != null)//use selected Quote's _id as new Quote's parent_id
                    quote.setParent_id(QuoteManager.getInstance().getSelected().get_id());
                else quote.setParent_id(txtQuoteId.getText());//default to ID from TextField
            }
        }

        if(toggleVatExempt.isSelected())
            quote.setVat(0);
        else quote.setVat(QuoteManager.VAT);

        if(txtNotes.getText()!=null)
            quote.setOther(txtNotes.getText().replaceAll("\n", ";"));

        try
        {
            QuoteManager.getInstance().createQuote(quote, quoteItems, quoteServices, new Callback()
            {
                @Override
                public Object call(Object new_quote_id)
                {
                    if(QuoteManager.getInstance().getDataset()!=null)
                        QuoteManager.getInstance().setSelected(QuoteManager.getInstance().getDataset().get(new_quote_id));
                    else IO.log(getClass().getName(), IO.TAG_ERROR, "no quotes in database.");

                    //txtQuoteId.setText(new_quote_id.toString());

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
                    return null;
                }
            });
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void updateQuote()
    {
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

        txtClient.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(selected_client==null)
        {
            txtClient.getStyleClass().remove("form-control-default");
            txtClient.getStyleClass().add("control-input-error");
            return;
        }else{
            txtClient.getStyleClass().remove("control-input-error");
            txtClient.getStyleClass().add("form-control-default");
        }

        txtContactPerson.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(selected_contact_person==null)
        {
            txtContactPerson.getStyleClass().remove("form-control-default");
            txtContactPerson.getStyleClass().add("control-input-error");
            return;
        }else{
            txtContactPerson.getStyleClass().remove("control-input-error");
            txtContactPerson.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtCell, txtCell.getText(), 1, ".+"))
        {
            txtCell.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtTel, txtTel.getText(), 1, ".+"))
        {
            txtTel.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
        {
            txtEmail.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        cbxAccount.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxAccount.getValue()==null)
        {
            cbxAccount.getStyleClass().remove("form-control-default");
            cbxAccount.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxAccount.getStyleClass().remove("control-input-error");
            cbxAccount.getStyleClass().add("form-control-default");
        }

        if(!Validators.isValidNode(txtSite, txtSite.getText(), 1, ".+"))
        {
            txtSite.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtRequest, txtRequest.getText(), 1, ".+"))
        {
            txtRequest.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }

        Quote selected = (Quote) QuoteManager.getInstance().getSelected();
        if(selected!=null)
        {
            if(selected.getStatus()==Quote.STATUS_APPROVED)
            {
                IO.logAndAlert("Error", "Selected Quote has already been approved and can no longer be changed. \nCreate a new revision if you'd like to make updates to this quote.", IO.TAG_ERROR);
                return;
            }
            selected.setClient_id(selected_client.get_id());
            selected.setContact_person_id(selected_contact_person.getUsr());
            if(toggleVatExempt.isSelected())
                selected.setVat(0);
            else selected.setVat(QuoteManager.VAT);
            selected.setSitename(txtSite.getText());
            selected.setRequest(txtRequest.getText());
            selected.setAccount_name(cbxAccount.getValue());
            if(txtNotes.getText()!=null)
                selected.setOther(txtNotes.getText().replaceAll("\n", ";"));

            QuoteManager.getInstance().updateQuote(selected, tblQuoteItems.getItems(), tblQuoteServices.getItems(), true, param -> {
                QuoteManager.getInstance().initialize();
                if(QuoteManager.getInstance().getDataset()!=null)
                    QuoteManager.getInstance().setSelected(QuoteManager.getInstance().getDataset().get(selected.get_id()));
                new Thread(() ->
                        refreshModel(param1 ->
                        {
                            Platform.runLater(() -> refreshView());
                            return null;
                        })).start();
                return null;
            });
        } else IO.logAndAlert("Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void createJob()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Quote selected = (Quote) QuoteManager.getInstance().getSelected();
                if(selected!=null)
                {
                    if(selected.getStatus()>Quote.STATUS_PENDING)
                    {
                        Job job = new Job();
                        job.setQuote_id(selected.get_id());
                        job.setCreator(smgr.getActiveEmployee().getUsr());
                        /*if(JobManager.getInstance().getJobs()!=null)
                            job.setJob_number(JobManager.getInstance().getJobs().length);
                        else job.setJob_number(0);*/
                        String new_job_id = JobManager.getInstance().createNewJob(job, null);
                        if (new_job_id != null)
                        {
                            //IO.logAndAlert("Success", "Successfully created a new job.", IO.TAG_INFO);
                            //force refresh Job data-set
                            JobManager.getInstance().forceSynchronise();

                            if (JobManager.getInstance().getDataset() != null)
                            {
                                JobManager.getInstance().setSelected(JobManager.getInstance().getDataset().get(new_job_id));

                                ScreenManager.getInstance().showLoadingScreen(param ->
                                {
                                    new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                if (ScreenManager.getInstance()
                                                        .loadScreen(Screens.VIEW_JOB.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/" + Screens.VIEW_JOB
                                                                        .getScreen())))
                                                {
                                                    Platform.runLater(() -> ScreenManager.getInstance()
                                                            .setScreen(Screens.VIEW_JOB.getScreen()));
                                                }
                                                else IO.log(getClass()
                                                        .getName(), IO.TAG_ERROR, "could not load job viewer screen.");
                                            } catch (IOException e)
                                            {
                                                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                            }
                                        }
                                    }).start();
                                    return null;
                                });
                            } else IO.logAndAlert("Error", "Could not find any jobs in the database.", IO.TAG_INFO);
                        } else IO.logAndAlert("Error", "Could not successfully create a new job.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Error", "Quote has not been approved yet.", IO.TAG_ERROR);
                } else IO.logAndAlert("Cannot Create Job", "Cannot create job because the selected quote is invalid.", IO.TAG_ERROR);
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void createPDF()
    {
        try
        {
            if(QuoteManager.getInstance().getSelected()!=null)
            {
                String path = PDF.createQuotePdf((Quote) QuoteManager.getInstance().getSelected());
                if (path != null)
                {
                    if(Desktop.isDesktopSupported())
                    {
                        Desktop.getDesktop().open(new File(path));
                    } else IO.logAndAlert("Error", "This environment not supported.", IO.TAG_ERROR);
                    /*PDFViewer pdfViewer = PDFViewer.getInstance();
                    pdfViewer.setVisible(true);
                    pdfViewer.doOpen(path);*/
                } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid quote PDF path returned.");
            }
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    @FXML
    public void requestApproval()
    {
        //send email requesting approval of Quote
        try
        {
            if(QuoteManager.getInstance().getSelected()!=null)
                QuoteManager.getInstance().requestQuoteApproval((Quote) QuoteManager.getInstance().getSelected(), null);
            else IO.logAndAlert("Error", "Selected Quote is invalid.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void clearMaterials()
    {
        tblQuoteItems.getItems().removeAll();
        tblQuoteItems.refresh();
    }

    @FXML
    public void newClient()
    {
        selected_client = null;

        final TextField txt_client_name = new TextField();
        txt_client_name.setMinWidth(200);
        txt_client_name.setMaxWidth(Double.MAX_VALUE);
        //HBox client_name = CustomTableViewControls.getLabelledNode("Client Name", 200, txt_client_name);

        final TextArea txt_physical_address = new TextArea();
        txt_physical_address.setMinWidth(200);
        txt_physical_address.setMaxWidth(Double.MAX_VALUE);
        txt_physical_address.setPrefHeight(70);
        //HBox physical_address = CustomTableViewControls.getLabelledNode("Physical Address", 200, txt_physical_address);

        final TextArea txt_postal_address = new TextArea();
        txt_postal_address.setMinWidth(200);
        txt_postal_address.setMaxWidth(Double.MAX_VALUE);
        txt_postal_address.setPrefHeight(70);
        //HBox postal_address = CustomTableViewControls.getLabelledNode("Postal Address", 200, txt_postal_address);

        final TextField txt_tel = new TextField();
        txt_tel.setMinWidth(200);
        txt_tel.setMaxWidth(Double.MAX_VALUE);
        //HBox tel = CustomTableViewControls.getLabelledNode("Tel Number", 200, txt_tel);

        final TextField txt_contact_email = new TextField();
        txt_contact_email.setMinWidth(200);
        txt_contact_email.setMaxWidth(Double.MAX_VALUE);
        //HBox contact_email = CustomTableViewControls.getLabelledNode("eMail Address", 200, txt_contact_email);

        final TextField txt_client_reg = new TextField("N/A");
        txt_client_reg.setMinWidth(200);
        txt_client_reg.setMaxWidth(Double.MAX_VALUE);
        //HBox client_reg = CustomTableViewControls.getLabelledNode("Registration Number", 200, txt_client_reg);

        final TextField txt_client_vat = new TextField("N/A");
        txt_client_vat.setMinWidth(200);
        txt_client_vat.setMaxWidth(Double.MAX_VALUE);
        //HBox client_vat = CustomTableViewControls.getLabelledNode("VAT Number", 200, txt_client_vat);

        final TextField txt_client_account = new TextField();
        txt_client_account.setMinWidth(200);
        txt_client_account.setMaxWidth(Double.MAX_VALUE);
        //HBox client_account = CustomTableViewControls.getLabelledNode("Account Name", 200, txt_client_account);

        txt_client_name.textProperty().addListener((observable, oldValue, newValue) ->
        {
            if(txt_client_name.getText()!=null)
                txt_client_account.setText(txt_client_name.getText().toLowerCase().replaceAll(" ", "-"));
        });

        final DatePicker dpk_date_partnered = new DatePicker();
        dpk_date_partnered.setMinWidth(200);
        dpk_date_partnered.setMaxWidth(Double.MAX_VALUE);
        //HBox date_partnered = CustomTableViewControls.getLabelledNode("Date Partnered", 200, dpk_date_partnered);

        final TextField txt_website = new TextField();
        txt_website.setMinWidth(200);
        txt_website.setMaxWidth(Double.MAX_VALUE);
        //HBox website = CustomTableViewControls.getLabelledNode("Website", 200, txt_website);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        txt_other.setPrefHeight(70);
        //HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        Button btnSubmit = new Button("Create New Client");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(45);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(new Label("Client Name: "), 0, 0);
        page.add(txt_client_name, 1, 0);

        page.add(new Label("Physical Address: "), 0, 1);
        page.add(txt_physical_address, 1, 1);

        page.add(new Label("Postal Address: "), 0, 2);
        page.add(txt_postal_address, 1, 2);

        page.add(new Label("Tel No.: "), 0, 3);
        page.add(txt_tel, 1, 3);

        page.add(new Label("eMail address: "), 0, 4);
        page.add(txt_contact_email, 1, 4);

        page.add(new Label("Registration Number: "), 0, 5);
        page.add(txt_client_reg, 1, 5);

        page.add(new Label("Tax Number"), 0, 6);
        page.add(txt_client_vat, 1, 6);

        page.add(new Label("Credit account name: "), 0, 7);
        page.add(txt_client_account, 1, 7);

        page.add(new Label("Website: "), 0, 8);
        page.add(txt_website, 1, 8);

        page.add(new Label("Other Info: "), 0, 9);
        page.add(txt_other, 1, 9);

        page.add(btnSubmit, 1, 10);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create new Client");
        popover.setDetached(true);
        popover.show(btnNewClient);

        TextFields.bindAutoCompletion(txt_client_name, ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_client = (Client) event.getCompletion();

                    if(selected_client.getPhysical_address()!=null)
                        txt_physical_address.setText(selected_client.getPhysical_address());
                    if(selected_client.getPostal_address()!=null)
                        txt_postal_address.setText(selected_client.getRegistration_number());
                    if(selected_client.getRegistration_number()!=null)
                        txt_client_reg.setText(selected_client.getRegistration_number());
                    if(selected_client.getVat_number()!=null)
                        txt_client_vat.setText(selected_client.getRegistration_number());
                    if(selected_client.getAccount_name()!=null)
                        txt_client_account.setText(selected_client.getAccount_name());
                    if(selected_client.getTel()!=null)
                        txt_tel.setText(selected_client.getTel());
                    if(selected_client.getWebsite()!=null)
                        txt_website.setText(selected_client.getWebsite());
                    if(selected_client.getContact_email()!=null)
                        txt_contact_email.setText(selected_client.getContact_email());
                    if(selected_client.getOther()!=null)
                        txt_other.setText(selected_client.getOther());
                }
            }
        });

        btnSubmit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if(SessionManager.getInstance().getActive()==null)
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }
                if(SessionManager.getInstance().getActive().isExpired())
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

                if(!Validators.isValidNode(txt_client_name, txt_client_name.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_physical_address, txt_physical_address.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_postal_address, txt_postal_address.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_tel, txt_tel.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_contact_email, txt_contact_email.getText(), 1, ".+"))
                    return;
                /*if(!Validators.isValidNode(txt_client_reg, txt_client_reg.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_client_vat, txt_client_vat.getText(), 1, ".+"))
                    return;*/
                if(!Validators.isValidNode(txt_client_account, txt_client_account.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(dpk_date_partnered, dpk_date_partnered.getValue()==null?"":dpk_date_partnered.getValue().toString(), 4, date_regex))
                    return;
                /*if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                    return;*/

                //if txt_client_name matches selected_client's client_name ask if they want to make a duplicate record
                String proceed = IO.OK;
                if(selected_client!=null)
                    if(txt_client_name.getText().equals(selected_client.getClient_name()))
                        proceed = IO.showConfirm("Found duplicate client, continue?", "Found client with the name ["+txt_client_name.getText()+"], add another record?");

                //did they choose to continue with the creation or cancel?
                if(!proceed.equals(IO.OK))
                {
                    IO.log(getClass().getName(), "aborting new Client creation.", IO.TAG_VERBOSE);
                    return;
                }

                long date_partnered_in_sec = dpk_date_partnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

                Client client = new Client();
                client.setClient_name(txt_client_name.getText());
                client.setPhysical_address(txt_physical_address.getText());
                client.setPostal_address(txt_postal_address.getText());
                client.setTel(txt_tel.getText());
                client.setContact_email(txt_contact_email.getText());
                client.setRegistration_number(txt_client_reg.getText());
                client.setVat_number(txt_client_vat.getText());
                client.setAccount_name(txt_client_account.getText());
                client.setDate_partnered(date_partnered_in_sec);
                client.setWebsite(txt_website.getText());
                client.setActive(true);
                client.setCreator(SessionManager.getInstance().getActive().getUsr());
                if(txt_other.getText()!=null)
                    client.setOther(txt_other.getText());

                try
                {
                    ClientManager.getInstance().createBusinessObject(client, new_client_id ->
                    {
                        if(new_client_id!=null)
                        {
                            selected_client = (Client) ClientManager.getInstance().getDataset().get(new_client_id);
                        } else IO.logAndAlert("Error", "Could not create new client ["+txt_client_name.getText()+"]", IO.TAG_ERROR);
                        return null;
                    });
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
        });
    }

    @FXML
    public void newEmployee()
    {
        selected_client = null;

        TextField txtFirstname = new TextField();
        txtFirstname.setMinWidth(200);
        txtFirstname.setMaxWidth(Double.MAX_VALUE);
        //HBox first_name = CustomTableViewControls.getLabelledNode("First Name", 200, txtFirstname);

        TextField txtLastname = new TextField();
        txtLastname.setMinWidth(200);
        txtLastname.setMaxWidth(Double.MAX_VALUE);
        //HBox last_name = CustomTableViewControls.getLabelledNode("Last Name", 200, txtLastname);

        TextField txtEmail = new TextField();
        txtEmail.setMinWidth(200);
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        //HBox email = CustomTableViewControls.getLabelledNode("eMail Address:", 200, txtEmail);

        TextField txtTelephone = new TextField();
        txtTelephone.setMinWidth(200);
        txtTelephone.setMaxWidth(Double.MAX_VALUE);
        //HBox telephone = CustomTableViewControls.getLabelledNode("Telephone #: ", 200, txtTelephone);

        TextField txtCellphone = new TextField();
        txtCellphone.setMinWidth(200);
        txtCellphone.setMaxWidth(Double.MAX_VALUE);
        //HBox cellphone = CustomTableViewControls.getLabelledNode("Cellphone #: ", 200, txtCellphone);

        TextArea txtOther = new TextArea();
        txtOther.setMinWidth(200);
        txtOther.setMaxWidth(Double.MAX_VALUE);
        //HBox other = CustomTableViewControls.getLabelledNode("Other: ", 200, txtOther);

        Button btnSubmit = new Button("Create New Client Representative");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(45);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(new Label("First Name: "), 0, 0);
        page.add(txtFirstname, 1, 0);

        page.add(new Label("Last Name: "), 0, 1);
        page.add(txtLastname, 1, 1);

        page.add(new Label("eMail Address: "), 0, 2);
        page.add(txtEmail, 1, 2);

        page.add(new Label("Tel No.: "), 0, 3);
        page.add(txtTelephone, 1, 3);

        page.add(new Label("Cellphone Address: "), 0, 4);
        page.add(txtCellphone, 1, 4);

        page.add(new Label("Other info: "), 0, 5);
        page.add(txtOther, 1, 5);

        page.add(btnSubmit, 1, 6);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create new Client Representative");
        popover.setDetached(true);
        popover.show(btnNewClientRepresentative);

        TextFields.bindAutoCompletion(txtFirstname, EmployeeManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_contact_person = event.getCompletion();

                    if(selected_contact_person.getFirstname()!=null)
                        txtFirstname.setText(selected_contact_person.getFirstname());
                    if(selected_contact_person.getLastname()!=null)
                        txtLastname.setText(selected_contact_person.getLastname());
                    if(selected_contact_person.getCell()!=null)
                        txtCellphone.setText(selected_contact_person.getCell());
                    if(selected_contact_person.getTel()!=null)
                        txtTelephone.setText(selected_contact_person.getTel());
                    if(selected_contact_person.getEmail()!=null)
                        txtEmail.setText(selected_contact_person.getEmail());
                    if(selected_contact_person.getTel()!=null)
                        txtOther.setText(selected_contact_person.getOther());
                }
            }
        });

        TextFields.bindAutoCompletion(txtLastname, EmployeeManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_contact_person = event.getCompletion();

                    if(selected_contact_person.getFirstname()!=null)
                        txtFirstname.setText(selected_contact_person.getFirstname());
                    if(selected_contact_person.getLastname()!=null)
                        txtLastname.setText(selected_contact_person.getLastname());
                    if(selected_contact_person.getCell()!=null)
                        txtCellphone.setText(selected_contact_person.getCell());
                    if(selected_contact_person.getTel()!=null)
                        txtTelephone.setText(selected_contact_person.getTel());
                    if(selected_contact_person.getEmail()!=null)
                        txtEmail.setText(selected_contact_person.getEmail());
                    if(selected_contact_person.getTel()!=null)
                        txtOther.setText(selected_contact_person.getOther());
                }
            }
        });

        btnSubmit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if(SessionManager.getInstance().getActive()==null)
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }
                if(SessionManager.getInstance().getActive().isExpired())
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                //File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                //String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

                if(!Validators.isValidNode(txtFirstname, txtFirstname.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txtLastname, txtLastname.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txtCellphone, txtCellphone.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txtTelephone, txtTelephone.getText(), 1, ".+"))
                    return;
                //check if email doesn't exist already
                if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
                    return;

                boolean found = false;
                for(Employee employee: EmployeeManager.getInstance().getDataset().values())
                    if(txtEmail.getText().toLowerCase().equals(employee.getEmail().toLowerCase()))
                        found=true;

                if(found)
                {
                    IO.logAndAlert("Error", "User with email address ["+txtEmail.getText()+"] already exists.", IO.TAG_ERROR);
                    return;
                }

                //if txtFirstname and txtLastname matches selected_contact_person's first_name and last_name ask if they want to make a duplicate record
                String proceed = IO.OK;
                if(selected_contact_person!=null)
                    if(txtFirstname.getText().equals(selected_contact_person.getFirstname()) && txtLastname.equals(selected_contact_person.getLastname()))
                        proceed = IO.showConfirm("Found duplicate person, continue?", "Found person with the name ["+selected_contact_person.getName()+"], add another record?");

                //did they choose to continue with the creation or cancel?
                if(!proceed.equals(IO.OK))
                {
                    IO.log(getClass().getName(), "aborting new Client contact person creation.", IO.TAG_VERBOSE);
                    return;
                }

                int access_lvl=0;

                Employee employee = new Employee();
                employee.setUsr(txtEmail.getText().toLowerCase());
                String pwd = "";
                try
                {
                    pwd = IO.generateRandomString(12);
                    assert pwd.length()==12;
                    employee.setPwd(IO.getEncryptedHexString(pwd));
                } catch (Exception e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }

                employee.setAccessLevel(access_lvl);
                employee.setFirstname(txtFirstname.getText());
                employee.setLastname(txtLastname.getText());
                employee.setGender("Male");
                employee.setEmail(txtEmail.getText());
                employee.setTel(txtTelephone.getText());
                employee.setCell(txtCellphone.getText());
                employee.setCreator(SessionManager.getInstance().getActive().getUsr());

                if(txtOther.getText()!=null)
                    employee.setOther(txtOther.getText());
                employee.setOther(employee.getOther() + " pwd=" + pwd);
                try
                {
                    EmployeeManager.getInstance().createBusinessObject(employee, new_user_id ->
                    {
                        if(new_user_id!=null)
                        {
                            selected_contact_person = (Employee) EmployeeManager.getInstance().getDataset().get(new_user_id);
                        } else IO.logAndAlert("Error", "Could not create new client representative ["+txtFirstname.getText()+ " " + txtLastname.getText() + "]", IO.TAG_ERROR);
                        return null;
                    });
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
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
