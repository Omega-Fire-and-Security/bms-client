/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.FadulousBMS;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class PurchaseOrderController extends OperationsController implements Initializable
{
    @FXML
    protected TableView<PurchaseOrderItem> tblPurchaseOrderItems;
    @FXML
    protected TableColumn colId, colItemNumber, colName, colDescription, colUnit,colQuantity, colValue, colDiscount,
            colCreator, colTotal, colAction;
    @FXML
    protected TextField txtNumber, txtCreator, txtStatus, txtTotal;
    @FXML
    protected Button btnApprove;
    //@FXML
    //protected Slider vatSlider;
    @FXML
    protected ToggleButton toggleVatExempt;
    @FXML
    protected ComboBox<String> cbxAccount;
    @FXML
    protected Label lblVat;
    @FXML
    protected ComboBox<Supplier> cbxSuppliers;
    @FXML
    protected ComboBox<Employee> cbxContactPerson;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading view purchase order view..");

        if(SupplierManager.getInstance().getSuppliers()==null)
        {
            IO.logAndAlert(getClass().getName(), "no suppliers found in the database.", IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getEmployees()==null)
        {
            IO.logAndAlert(getClass().getName(), "no employees found in the database.", IO.TAG_ERROR);
            return;
        }
        //setup PO default accounts
        cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash"}));

        //setup suppliers combo box
        Supplier[] suppliers = new Supplier[SupplierManager.getInstance().getSuppliers().values().toArray().length];
        SupplierManager.getInstance().getSuppliers().values().toArray(suppliers);
        cbxSuppliers.setItems(FXCollections.observableArrayList(suppliers));
        cbxSuppliers.valueProperty().addListener((observable, oldValue, newValue) ->
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", newValue.getAccount_name()})));

        //setup employees combo box
        Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().values().toArray().length];
        EmployeeManager.getInstance().getEmployees().values().toArray(employees);
        cbxContactPerson.setItems(FXCollections.observableArrayList(employees));
        //set default total
        //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " 0");

        //set vat slider max value
        //vatSlider.setMax(QuoteManager.VAT);

        refreshTotal();
        toggleVatExempt.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
                if(newValue)
                    toggleVatExempt.setText("VAT exempt");
                else toggleVatExempt.setText(QuoteManager.VAT+ "%");
                refreshTotal();
        });
        //every time the slider value changes, show the value on a label
        /*vatSlider.valueProperty().addListener(event ->
        {
            lblVat.setText("VAT ["+new DecimalFormat("##.##").format(vatSlider.getValue())+"%]");
            if(tblPurchaseOrderItems.getItems()!=null)
            {
                double total = PurchaseOrderManager.computePurchaseOrderTotal(tblPurchaseOrderItems.getItems());
                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                        new DecimalFormat("##.##").format((total + (total*(vatSlider.getValue()/100)))));
            }
        });*/

        //set up PurchaseOrderItems table
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colItemNumber.setCellValueFactory(new PropertyValueFactory<>("item_number"));
        colName.setCellValueFactory(new PropertyValueFactory<>("item_name"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("item_description"));
        colValue.setCellFactory(col -> new TextFieldTableCell("cost", "cost", event ->
        {
            refreshTotal();
            tblPurchaseOrderItems.refresh();
            return null;
        }));
        colUnit.setCellValueFactory(new PropertyValueFactory<>("unit"));
        colQuantity.setCellFactory(col -> new TextFieldTableCell("quantity", "quantity", event ->
        {
            refreshTotal();
            tblPurchaseOrderItems.refresh();
            return null;
        }));
        colDiscount.setCellFactory(col -> new TextFieldTableCell("discount", "discount", null));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        //update totals on TableCell commit
        /*tblPurchaseOrderItems.editingCellProperty().addListener((observable, oldValue, newValue) ->
        {
            refreshTotal();
            tblPurchaseOrderItems.refresh();
        });*/

        Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>> cellFactory
                =
                new Callback<TableColumn<PurchaseOrder, String>, TableCell<PurchaseOrder, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<PurchaseOrder, String> param)
                    {
                        final TableCell<PurchaseOrder, String> cell = new TableCell<PurchaseOrder, String>()
                        {
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnRemove.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(100);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                }
                                else
                                {
                                    HBox hBox = new HBox(btnRemove);

                                    btnRemove.setOnAction(event ->
                                    {
                                        PurchaseOrder purchaseOrder = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(purchaseOrder);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass()
                                                .getName(), IO.TAG_INFO, "successfully removed purchase order: " + purchaseOrder
                                                .get_id());
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
    }

    protected void refreshTotal()
    {
        double vat = QuoteManager.VAT;
        if(toggleVatExempt.isSelected())//if is VAT exempt
            vat =0.0;//is VAT exempt
        lblVat.setText("VAT ["+new DecimalFormat("##.##").format(vat)+"%]");
        if(tblPurchaseOrderItems.getItems()!=null)
        {
            double total = PurchaseOrderManager.computePurchaseOrderTotal(tblPurchaseOrderItems.getItems());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                    new DecimalFormat("##.##").format((total + (total*(vat)))));
        }
        if(tblPurchaseOrderItems.getItems()!=null)
        {
            double total = PurchaseOrderManager.computePurchaseOrderTotal(tblPurchaseOrderItems.getItems());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                    new DecimalFormat("##.##").format((total + (total*(vat/100)))));
        }
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading purchase order data model..");

        try
        {
            EmployeeManager.getInstance().reloadDataFromServer();
            ResourceManager.getInstance().reloadDataFromServer();
            SupplierManager.getInstance().reloadDataFromServer();
            PurchaseOrderManager.getInstance().reloadDataFromServer();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        AssetManager.getInstance().loadDataFromServer();
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

    @FXML
    public void newPurchaseOrderItem()
    {
        if (ResourceManager.getInstance() != null)
        {
            if (ResourceManager.getInstance().getResources() != null)
            {
                if (ResourceManager.getInstance().getResources().size() > 0)
                {
                    ComboBox<Resource> resourceComboBox = new ComboBox<>();
                    resourceComboBox.setMinWidth(120);
                    resourceComboBox.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getAll_resources().values()));
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnAdd");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnNewMaterial = new Button("New Material");
                    btnNewMaterial.setMinWidth(80);
                    btnNewMaterial.setMinHeight(40);
                    btnNewMaterial.setDefaultButton(true);
                    btnNewMaterial.getStyleClass().add("btnAdd");
                    btnNewMaterial.getStylesheets()
                            .add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Resource: "), resourceComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnNewMaterial, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setMaxWidth(300);
                    stage.setTitle("Resource Purchase Order");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.show();


                    btnAdd.setOnAction(event ->
                    {
                        if(PurchaseOrderManager.getInstance().getSelected()==null)
                        {
                            IO.log(getClass().getName(), IO.TAG_ERROR, "selected purchase order is invalid.");
                            return;
                        }
                        if (resourceComboBox.getValue() != null)
                        {
                            PurchaseOrderResource purchaseOrderResource = new PurchaseOrderResource();
                            purchaseOrderResource.setPurchase_order_id(PurchaseOrderManager.getInstance().getSelected().get_id());
                            purchaseOrderResource.setItem_number(tblPurchaseOrderItems.getItems().size());
                            //purchaseOrderResource.setItem(resourceComboBox.getValue());
                            purchaseOrderResource.setItem_id(resourceComboBox.getValue().get_id());
                            purchaseOrderResource.setQuantity(1);
                            purchaseOrderResource.setDiscount(0);
                            tblPurchaseOrderItems.getItems().add(purchaseOrderResource);
                            tblPurchaseOrderItems.refresh();

                            //itemsModified = true;

                            new Thread(() ->
                            {
                                refreshModel();
                                Platform.runLater(() -> refreshView());
                            }).start();

                        }
                        else IO.logAndAlert("Purchase Order Item Addition", "Invalid item selected.", IO.TAG_ERROR);
                    });

                    btnNewMaterial.setOnAction(event ->
                    {
                        stage.close();
                        ResourceManager.getInstance().newResourceWindow(param ->
                        {
                            new Thread(() ->
                            {
                                refreshModel();
                                Platform.runLater(() -> refreshView());
                            }).start();
                            return null;
                        });
                    });

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("Add Purchase Order Item", "No resources were found in the database, please add some resources first and try again.", IO.TAG_ERROR);
    }

    @FXML
    public void updatePurchaseOrder()
    {
        String date_regex = "\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";
        PurchaseOrder purchaseOrder = PurchaseOrderManager.getInstance().getSelected();
        if(purchaseOrder==null)
        {
            IO.logAndAlert(getClass().getName(), "selected purchase order is invalid.", IO.TAG_ERROR);
            return;
        }
        if(purchaseOrder.getStatus()>0)
        {
            IO.logAndAlert(getClass().getName(), "selected purchase order has already been approved and can no longer be edited.", IO.TAG_ERROR);
            return;
        }

        cbxSuppliers.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if (cbxSuppliers.getValue() == null)
        {
            cbxSuppliers.getStyleClass().remove("form-control-default");
            cbxSuppliers.getStyleClass().add("control-input-error");
            return;
        }
        else
        {
            cbxSuppliers.getStyleClass().remove("control-input-error");
            cbxSuppliers.getStyleClass().add("form-control-default");
        }

        cbxContactPerson.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if (cbxContactPerson.getValue() == null)
        {
            cbxContactPerson.getStyleClass().remove("form-control-default");
            cbxContactPerson.getStyleClass().add("control-input-error");
            return;
        }
        else
        {
            cbxContactPerson.getStyleClass().remove("control-input-error");
            cbxContactPerson.getStyleClass().add("form-control-default");
        }

        /*if (!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }*/

        cbxAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if (cbxAccount.getValue() == null)
        {
            cbxAccount.getStyleClass().remove("form-control-default");
            cbxAccount.getStyleClass().add("control-input-error");
            return;
        } else
        {
            cbxAccount.getStyleClass().remove("control-input-error");
            cbxAccount.getStyleClass().add("form-control-default");
        }


        List<PurchaseOrderItem> purchaseOrderItems = tblPurchaseOrderItems.getItems();

        if (purchaseOrderItems == null)
        {
            IO.logAndAlert("Invalid Purchase Order", "PurchaseOrder items list is null.", IO.TAG_ERROR);
            return;
        }
        if (purchaseOrderItems.size() <= 0)
        {
            IO.logAndAlert("Invalid Purchase Order", "PurchaseOrder has no materials", IO.TAG_ERROR);
            return;
        }

        String str_supplier = null;
        String str_contact = null;
        String str_vat = "";
        String str_account = "";
        try
        {
            str_supplier = cbxSuppliers.getValue().get_id();
            str_contact = cbxContactPerson.getValue().getUsr();
            double vat = QuoteManager.VAT;
            if(toggleVatExempt.isSelected())
                vat=0;
            str_vat = new DecimalFormat("##.##").format(vat);//txtVat.getText();
            str_account = cbxAccount.getValue();
        } catch (NumberFormatException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            return;
        }

        //prepare PurchaseOrder attributes
        purchaseOrder.setSupplier_id(str_supplier);
        purchaseOrder.setContact_person_id(str_contact);
        //purchaseOrder.setStatus(0);
        purchaseOrder.setAccount(str_account);
        purchaseOrder.setVat(Double.parseDouble(str_vat));
        //purchaseOrder.setCreator(SessionManager.getInstance().getActive().getUsername());

        PurchaseOrderItem[] items = new PurchaseOrderItem[purchaseOrderItems.size()];
        purchaseOrderItems.toArray(items);
        purchaseOrder.setItems(items);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            if (SessionManager.getInstance().getActive() != null)
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive()
                        .getSessionId()));
            else
            {
                IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                return;
            }

            //update purchase order on database
            HttpURLConnection connection = RemoteComms.postData("/api/purchaseorder/update/" + purchaseOrder.get_id(), purchaseOrder.asUTFEncodedString(), headers);
            if(connection != null)
            {
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "updated purchase order ["+purchaseOrder.get_id()+"]. Updating resources of purchase order.");

                    if (response == null)
                    {
                        IO.logAndAlert("New Purchase Order Item", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if (response.isEmpty())
                    {
                        IO.logAndAlert("New Purchase Order Item", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }

                    /* Add Purchase Order Resources to Purchase Order on database */
                    boolean added_all_po_items = true;
                    for (PurchaseOrderItem purchaseOrderItem : tblPurchaseOrderItems.getItems())
                    {
                        //TODO: close connection like this on all
                        //Close connection
                        if (connection != null)
                            connection.disconnect();

                        if(purchaseOrderItem.get_id()!=null)
                        {
                            if(!purchaseOrderItem.get_id().isEmpty())
                            {
                                //update po item
                                if(purchaseOrderItem.getItem() instanceof Resource)
                                    connection = RemoteComms.postData("/api/purchaseorder/item/update/" + purchaseOrderItem.get_id(), purchaseOrderItem.asUTFEncodedString(), headers);
                                else if(purchaseOrderItem.getItem() instanceof Asset)
                                    connection = RemoteComms.postData("/api/purchaseorder/asset/update/" + purchaseOrderItem.get_id(), purchaseOrderItem.asUTFEncodedString(), headers);
                                else IO.log(getClass().getName(), IO.TAG_ERROR, "unknown purchase order item type.");

                                if (connection != null)
                                {
                                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                    {
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully updated purchase order item [" + purchaseOrderItem.get_id() + "]");
                                    } else
                                    {
                                        added_all_po_items = false;
                                        //Get error message
                                        String msg = IO.readStream(connection.getErrorStream());
                                        IO.logAndAlert("Error " + String
                                                .valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                                    }
                                } else
                                    IO.logAndAlert("Purchase Order Item Update Failure", "Could not connect to server.", IO.TAG_ERROR);
                                continue;
                            }
                        }
                        //create new po item - prepare parameters for purchase order item.
                        //purchaseOrderItem.setPurchase_order_id(response);//response = entire po object

                        if(purchaseOrderItem.getItem() instanceof Resource)
                            connection = RemoteComms.postData("/api/purchaseorder/item/add", purchaseOrderItem
                                    .asUTFEncodedString(), headers);
                        else if(purchaseOrderItem.getItem() instanceof Asset)
                            connection = RemoteComms.postData("/api/purchaseorder/asset/add", purchaseOrderItem
                                    .asUTFEncodedString(), headers);
                        else IO.log(getClass().getName(), IO.TAG_ERROR, "unknown purchase order item type.");

                        if (connection != null)
                        {
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                            {
                                IO.log(getClass().getName(), IO.TAG_INFO, "successfully added a new purchase order[" + response + "] item.");
                                String _id = IO.readStream(connection.getInputStream());
                                purchaseOrderItem.set_id(_id);
                            } else
                            {
                                added_all_po_items = false;
                                //Get error message
                                String msg = IO.readStream(connection.getErrorStream());
                                IO.logAndAlert("Error " + String
                                        .valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                            }
                        } else
                            IO.logAndAlert("Purchase Order Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
                    }
                    //Close connection
                    if (connection != null)
                        connection.disconnect();

                    if (added_all_po_items)
                    {
                        PurchaseOrderItem[] po_items = new PurchaseOrderItem[tblPurchaseOrderItems.getItems().size()];
                        tblPurchaseOrderItems.getItems().toArray(po_items);
                        purchaseOrder.setItems(po_items);

                        try
                        {
                            //refresh data model after PO update
                            PurchaseOrderManager.getInstance().reloadDataFromServer();
                            PurchaseOrderManager.getInstance().setSelected(purchaseOrder);
                            tblPurchaseOrderItems.setItems(FXCollections
                                .observableArrayList(PurchaseOrderManager.getInstance().getSelected().getItems()));
                            tblPurchaseOrderItems.refresh();

                            IO.logAndAlert("Purchase Order Update Success", "Successfully updated Purchase Order.", IO.TAG_INFO);
                            //itemsModified = false;
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
                    else IO.logAndAlert("Purchase Order Update Failure", "Could not add items to Purchase Order.", IO.TAG_ERROR);
                }
                else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if (connection != null)
                    connection.disconnect();
            }
            else IO.logAndAlert("Purchase Order Update Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void requestApproval()
    {
        //send email requesting approval of Quote
        if(PurchaseOrderManager.getInstance().getSelected()!=null)
            PurchaseOrderManager.getInstance().requestPOApproval(PurchaseOrderManager.getInstance().getSelected(), null);
    }

    @FXML
    public void approvePurchaseOrder()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Invalid Session", "Active session is invalid.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Expired Session", "Active session is expired.", IO.TAG_ERROR);
            return;
        }

        ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
        headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

        if(PurchaseOrderManager.getInstance().getSelected()!=null)
        {
            if(PurchaseOrderManager.getInstance().getSelected().getStatus()!=PurchaseOrderManager.PO_STATUS_APPROVED)
            {
                // set PO status and update it on server.
                PurchaseOrderManager.getInstance().getSelected().setStatus(PurchaseOrderManager.PO_STATUS_APPROVED);
                RemoteComms.updateBusinessObjectOnServer(PurchaseOrderManager.getInstance().getSelected(), "/api/purchaseorder", "status");
                //updatePurchaseOrder();
                //System.out.println("Status::::::::: " + PurchaseOrderManager.getInstance().getSelected().getStatus());

                // Update date_acquired attribute of PurchaseOrderItems.
                // - making them visible in their respective viewers, i.e Resources.fxml, Assets.fxml etc.
                boolean updated_all = false;
                HttpURLConnection connection=null;
                for(PurchaseOrderItem item: PurchaseOrderManager.getInstance().getSelected().getItems())
                {
                    long date_acquired = System.currentTimeMillis()/1000;
                    BusinessObject obj = item.getItem();

                    if(item instanceof PurchaseOrderResource)
                    {
                        ((Resource)obj).setDate_acquired(date_acquired);
                        int new_qty = item.getQuantityValue();
                        ((Resource)obj).setQuantity(new_qty);//don't use old_qty + new_qty, server will increment accordingly
                        //obj.parse("date_acquired", date_acquired);
                    }else if(item instanceof PurchaseOrderAsset)
                    {
                        ((Asset)obj).setDate_acquired(date_acquired);
                        int new_qty = item.getQuantityValue();
                        ((Asset)obj).setQuantity(new_qty);//don't use old_qty + new_qty, server will increment accordingly
                    } else {
                        IO.logAndAlert("Error", "unknown PurchaseOrderItem item type.", IO.TAG_ERROR);
                        continue;
                    }

                    try
                    {
                        //update date_acquired & quantity
                        connection = RemoteComms.postData(obj.apiEndpoint() +"/increment_quantity/"+obj.get_id(), obj.asUTFEncodedString(), headers);
                        if(connection!=null)
                        {
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                                updated_all = true;
                            else IO.logAndAlert("PurchaseOrder Item Update Error", connection.getResponseCode() + ":" + IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                        }else IO.logAndAlert("PurchaseOrder Item Update Error", "Invalid server response.", IO.TAG_ERROR);
                    } catch (IOException e)
                    {
                        IO.logAndAlert("IO Error", e.getMessage(), IO.TAG_ERROR);
                    }
                }
                if(updated_all)
                {
                    if(connection!=null)
                        connection.disconnect();

                    IO.logAndAlert("Success", "Successfully approved purchase order.", IO.TAG_INFO);
                    new Thread(() ->
                    {
                        refreshModel();
                        Platform.runLater(() -> refreshView());
                    }).start();
                }else{
                    IO.logAndAlert("Error", "Could NOT update associated items", IO.TAG_ERROR);
                }
            }else{
                IO.logAndAlert("Purchase Order Already Approved", "Selected purchase order has already been approved.", IO.TAG_ERROR);
            }
        }
        else IO.logAndAlert("Invalid Purchase Order", "Selected purchase order is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void newResourcePurchaseOrderItem()
    {
        if (ResourceManager.getInstance() != null)
        {
            if (ResourceManager.getInstance().getAll_resources() != null)
            {
                if (ResourceManager.getInstance().getAll_resources().size() > 0)
                {
                    ComboBox<Resource> resourceComboBox = new ComboBox<>();
                    resourceComboBox.setMinWidth(120);
                    resourceComboBox.setItems(FXCollections.observableArrayList(ResourceManager.getInstance().getAll_resources().values()));
                    HBox.setHgrow(resourceComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnDefault");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnNewMaterial = new Button("New Material");
                    btnNewMaterial.setMinWidth(130);
                    btnNewMaterial.setMinHeight(40);
                    btnNewMaterial.setDefaultButton(true);
                    btnNewMaterial.getStyleClass().add("btnAdd");
                    btnNewMaterial.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

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
                    stage.setTitle("Add Material to Purchase Order");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.setResizable(false);
                    stage.show();


                    btnAdd.setOnAction(event ->
                    {
                        if (resourceComboBox.getValue() != null)
                        {
                            PurchaseOrderResource purchaseOrderResource = new PurchaseOrderResource();
                            purchaseOrderResource.setItem_number(tblPurchaseOrderItems.getItems().size());
                            //purchaseOrderResource.setItem(resourceComboBox.getValue());
                            purchaseOrderResource.setItem_id(resourceComboBox.getValue().get_id());
                            purchaseOrderResource.setQuantity(1);
                            purchaseOrderResource.setDiscount(0);
                            purchaseOrderResource.setCost(resourceComboBox.getValue().getResource_value());
                            tblPurchaseOrderItems.getItems().add(purchaseOrderResource);
                            tblPurchaseOrderItems.refresh();

                            //itemsModified = true;

                            new Thread(() ->
                            {
                                refreshModel();
                                Platform.runLater(() -> refreshView());
                            }).start();

                        }
                        else IO.logAndAlert("Purchase Order Item Addition", "Invalid item selected.", IO.TAG_ERROR);
                    });

                    btnNewMaterial.setOnAction(event ->
                    {
                        stage.close();
                        ResourceManager.getInstance().newResourceWindow(param ->
                        {
                            new Thread(() ->
                            {
                                refreshModel();
                                Platform.runLater(() -> refreshView());
                            }).start();
                            return null;
                        });
                    });

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        }
        IO.logAndAlert("Add Purchase Order Item", "No resources were found in the database, please add some resources first and try again.", IO.TAG_ERROR);
    }

    @FXML
    public void newAssetPurchaseOrderItem()
    {
        if (AssetManager.getInstance() != null)
        {
            if (AssetManager.getInstance().getAll_assets() != null)
            {
                if (AssetManager.getInstance().getAll_assets().size() > 0)
                {
                    ComboBox<Asset> assetComboBox = new ComboBox<>();
                    assetComboBox.setMinWidth(120);
                    assetComboBox.setItems(FXCollections.observableArrayList(AssetManager.getInstance().getAll_assets().values()));
                    HBox.setHgrow(assetComboBox, Priority.ALWAYS);

                    Button btnAdd = new Button("Add");
                    btnAdd.setMinWidth(80);
                    btnAdd.setMinHeight(40);
                    btnAdd.setDefaultButton(true);
                    btnAdd.getStyleClass().add("btnAdd");
                    btnAdd.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnNew = new Button("New Asset");
                    btnNew.setMinWidth(80);
                    btnNew.setMinHeight(40);
                    btnNew.setDefaultButton(true);
                    btnNew.getStyleClass().add("btnAdd");
                    btnNew.getStylesheets()
                            .add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    Button btnCancel = new Button("Close");
                    btnCancel.setMinWidth(80);
                    btnCancel.setMinHeight(40);
                    btnCancel.getStyleClass().add("btnBack");
                    btnCancel.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());

                    HBox hBox = new HBox(new Label("Asset: "), assetComboBox);
                    HBox.setHgrow(hBox, Priority.ALWAYS);
                    hBox.setSpacing(20);

                    HBox hBoxButtons = new HBox(btnAdd, btnNew, btnCancel);
                    hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                    hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                    hBoxButtons.setSpacing(20);

                    VBox vBox = new VBox(hBox, hBoxButtons);
                    VBox.setVgrow(vBox, Priority.ALWAYS);
                    vBox.setSpacing(20);
                    HBox.setHgrow(vBox, Priority.ALWAYS);
                    vBox.setFillWidth(true);

                    Stage stage = new Stage();
                    stage.setMaxWidth(300);
                    stage.setTitle("Asset Purchase Order");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.show();


                    btnAdd.setOnAction(event ->
                    {
                        if (assetComboBox.getValue() != null)
                        {
                            PurchaseOrderAsset purchaseOrderAsset = new PurchaseOrderAsset();
                            purchaseOrderAsset.setItem_number(tblPurchaseOrderItems.getItems().size());
                            //purchaseOrderAsset.setItem(assetComboBox.getValue());
                            purchaseOrderAsset.setItem_id(assetComboBox.getValue().get_id());
                            purchaseOrderAsset.setQuantity(1);
                            purchaseOrderAsset.setDiscount(0);
                            purchaseOrderAsset.setCost(assetComboBox.getValue().getAsset_value());
                            tblPurchaseOrderItems.getItems().add(purchaseOrderAsset);
                            tblPurchaseOrderItems.refresh();

                            //itemsModified = true;

                            new Thread(() ->
                            {
                                refreshModel();
                                Platform.runLater(() -> refreshView());
                            }).start();
                        }
                        else IO.logAndAlert("Purchase Order Item Addition", "Invalid item selected.", IO.TAG_ERROR);
                    });

                    btnNew.setOnAction(event ->
                    {
                        stage.close();
                        AssetManager.getInstance().newAssetWindow(param ->
                        {
                            new Thread(() ->
                            {
                                refreshModel();
                                Platform.runLater(() -> refreshView());
                            }).start();
                            return null;
                        });
                    });

                    btnCancel.setOnAction(event ->
                            stage.close());
                    return;
                }
            }
        } else IO.logAndAlert("Asset Purchase Order", "No assets were found in the database, please add some assets first and try again.", IO.TAG_ERROR);
    }

    @FXML
    public void createPurchaseOrder()
    {
        String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

        cbxSuppliers.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxSuppliers.getValue()==null)
        {
            cbxSuppliers.getStyleClass().remove("form-control-default");
            cbxSuppliers.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxSuppliers.getStyleClass().remove("control-input-error");
            cbxSuppliers.getStyleClass().add("form-control-default");
        }

        cbxContactPerson.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if(cbxContactPerson.getValue()==null)
        {
            cbxContactPerson.getStyleClass().remove("form-control-default");
            cbxContactPerson.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxContactPerson.getStyleClass().remove("control-input-error");
            cbxContactPerson.getStyleClass().add("form-control-default");
        }

        /*if(!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }*/
        cbxAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
        if (cbxAccount.getValue() == null)
        {
            cbxAccount.getStyleClass().remove("form-control-default");
            cbxAccount.getStyleClass().add("control-input-error");
            return;
        } else
        {
            cbxAccount.getStyleClass().remove("control-input-error");
            cbxAccount.getStyleClass().add("form-control-default");
        }

        List<PurchaseOrderItem> purchaseOrderItems = tblPurchaseOrderItems.getItems();

        if(purchaseOrderItems==null)
        {
            IO.logAndAlert("Invalid Purchase Order", "PurchaseOrder items list is null.", IO.TAG_ERROR);
            return;
        }
        if(purchaseOrderItems.size()<=0)
        {
            IO.logAndAlert("Invalid Purchase Order", "PurchaseOrder has no materials", IO.TAG_ERROR);
            return;
        }

        String str_supplier = null;
        String str_contact = null;
        String str_vat = "";
        String str_account = "";
        try
        {
            str_supplier = cbxSuppliers.getValue().get_id();
            str_contact = cbxContactPerson.getValue().getUsr();
            double vat = QuoteManager.VAT;
            if(toggleVatExempt.isSelected())
                vat=0;
            str_vat = new DecimalFormat("##.##").format(vat);//txtVat.getText();
            str_account = cbxAccount.getValue();//txtAccount.getText();
        }catch (NumberFormatException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            return;
        }

        //prepare PurchaseOrder attributes
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier_id(str_supplier);
        purchaseOrder.setContact_person_id(str_contact);
        purchaseOrder.setStatus(0);
        purchaseOrder.setAccount(str_account);
        purchaseOrder.setVat(Double.parseDouble(str_vat));
        purchaseOrder.setCreator(SessionManager.getInstance().getActive().getUsername());

        PurchaseOrderItem[] items = new PurchaseOrderItem[purchaseOrderItems.size()];
        purchaseOrderItems.toArray(items);
        purchaseOrder.setItems(items);

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            if(SessionManager.getInstance().getActive()!=null)
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
            else
            {
                IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                return;
            }

            //create new purchase order on database
            HttpURLConnection connection = RemoteComms.postData("/api/purchaseorder/add", purchaseOrder.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created purchaseorder ["+response+"]. Adding resources to purchaseorder.");

                    if(response==null)
                    {
                        IO.logAndAlert("New Purchase Order Item", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Purchase Order Item", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }

                    //Close connection
                    if(connection!=null)
                        connection.disconnect();
                    /* Add Purchase Order Resources to Purchase Order on database */

                    boolean added_all_po_items = true;
                    for(PurchaseOrderItem purchaseOrderItem: tblPurchaseOrderItems.getItems())
                    {
                        //prepare parameters for purchase order asset.
                        purchaseOrderItem.setPurchase_order_id(response);

                        if(purchaseOrderItem instanceof PurchaseOrderAsset)
                            connection = RemoteComms.postData("/api/purchaseorder/asset/add", purchaseOrderItem.asUTFEncodedString(), headers);
                        else if(purchaseOrderItem instanceof PurchaseOrderResource)
                            connection = RemoteComms.postData("/api/purchaseorder/item/add", purchaseOrderItem.asUTFEncodedString(), headers);
                        else IO.logAndAlert("Purchase Order Item Creation Error", "unknown purchase order item type ["+purchaseOrderItem+"].", IO.TAG_ERROR);

                        if (connection != null)
                        {
                            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                            {
                                IO.log(getClass().getName(), IO.TAG_INFO, "Successfully added a new purchase order["+response+"] item.");
                            } else
                            {
                                added_all_po_items = false;
                                //Get error message
                                String msg = IO.readStream(connection.getErrorStream());
                                IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                            }
                        }else IO.logAndAlert("New Purchase Order Item Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
                    }
                    if(added_all_po_items)
                    {
                        //System.out.println("po_id: "+response);
                        /*purchaseOrder.set_id(response);
                        PurchaseOrderItem[] arr_items = new PurchaseOrderItem[purchaseOrderItems.size()];
                        purchaseOrderItems.toArray(arr_items);
                        purchaseOrder.setItems(arr_items);
                        PurchaseOrderManager.getInstance().loadDataFromServer();
                        PurchaseOrderManager.getInstance().setSelected(purchaseOrder);
                        //tblPurchaseOrderItems.setItems(FXCollections.observableArrayList(PurchaseOrderManager.getInstance().getSelected().getItems()));*/

                        IO.logAndAlert("New Purchase Order Creation Success", "Successfully created a new Purchase Order.", IO.TAG_INFO);

                        try
                        {
                            PurchaseOrderManager.getInstance().reloadDataFromServer();
                            PurchaseOrderManager.getInstance().setSelected(PurchaseOrderManager.getInstance().getPurchaseOrders().get(response));

                            ScreenManager.getInstance().showLoadingScreen(param ->
                            {
                                new Thread(new Runnable()
                                {
                                    @Override
                                    public void run()
                                    {
                                        try
                                        {
                                            if(ScreenManager.getInstance().loadScreen(Screens.VIEW_PURCHASE_ORDER.getScreen(),getClass().getResource("../views/"+Screens.VIEW_PURCHASE_ORDER.getScreen())))
                                            {
                                                Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_PURCHASE_ORDER.getScreen()));
                                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchase order viewer screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    }
                                }).start();
                                return null;
                            });
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
                    } else IO.logAndAlert("New Purchase Order Creation Failure", "Could not add items to Purchase Order.", IO.TAG_ERROR);
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Purchase Order Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void newSupplier()
    {
        SupplierManager.getInstance().newSupplierWindow(param ->
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
    public void newEmployee()
    {
        EmployeeManager.getInstance().newExternalEmployeeWindow("Create a Contact Person for this Purchase Order", param ->
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
    public void newResource()
    {
        ResourceManager.getInstance().newResourceWindow(param ->
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
    public void newAsset()
    {
        AssetManager.getInstance().newAssetWindow(param ->
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
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.OPERATIONS.getScreen(),getClass().getResource("../views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            ScreenManager.getInstance().setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
    }
}