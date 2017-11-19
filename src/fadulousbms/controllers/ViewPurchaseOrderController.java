/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ViewPurchaseOrderController extends OperationsController implements Initializable
{
    @FXML
    private TableView<PurchaseOrderItem> tblPurchaseOrderItems;
    @FXML
    private TableColumn colId, colItemNumber, colName, colDescription, colUnit,colQuantity, colValue, colDiscount,
            colCreator, colAction;
    @FXML
    private TextField txtNumber, txtVat, txtAccount, txtCreator, txtStatus;
    @FXML
    private ComboBox<Supplier> cbxSuppliers;
    @FXML
    private ComboBox<Employee> cbxContactPerson;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading view purchase order view..");

        if(PurchaseOrderManager.getInstance().getSelected()==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "selected po is invalid[null].");
            return;
        }
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

        //setup suppliers combo box
        Supplier[] suppliers = new Supplier[SupplierManager.getInstance().getSuppliers().values().toArray().length];
        SupplierManager.getInstance().getSuppliers().values().toArray(suppliers);
        cbxSuppliers.setItems(FXCollections.observableArrayList(suppliers));
        //set selected
        if(PurchaseOrderManager.getInstance().getSelected().getSupplier()!=null)
            cbxSuppliers.setValue(PurchaseOrderManager.getInstance().getSelected().getSupplier());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po has no valid supplier.");

        //setup employees combo box
        Employee[] employees = new Employee[EmployeeManager.getInstance().getEmployees().values().toArray().length];
        EmployeeManager.getInstance().getEmployees().values().toArray(employees);
        cbxContactPerson.setItems(FXCollections.observableArrayList(employees));
        //set selected
        if(PurchaseOrderManager.getInstance().getSelected().getSupplier()!=null)
            cbxContactPerson.setValue(PurchaseOrderManager.getInstance().getSelected().getContact_person());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po has no valid contact person.");

        //set up text fields
        txtVat.setText(String.valueOf(QuoteManager.VAT));
        txtAccount.setText(PurchaseOrderManager.getInstance().getSelected().getAccount());
        txtNumber.setText(PurchaseOrderManager.getInstance().getSelected().getNumber());
        txtCreator.setText(PurchaseOrderManager.getInstance().getSelected().getCreator().toString());
        String status;
        if(PurchaseOrderManager.getInstance().getSelected().getStatus()==0)
            status="PENDING";
        else if(PurchaseOrderManager.getInstance().getSelected().getStatus()==1)
            status="APPROVED";
        else status = "ARCHIVED";
        txtStatus.setText(status);

        //set up PurchaseOrderItems table
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colItemNumber.setCellValueFactory(new PropertyValueFactory<>("item_number"));
        colName.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("item_name", "item_name", null));
        colDescription.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("item_description", "item_description", null));
        colValue.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("cost", "cost", null));
        colUnit.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("unit", "unit", null));
        colQuantity.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("quantity", "quantity", null));
        colDiscount.setCellFactory(col -> new fadulousbms.model.TextFieldTableCell("discount", "discount", null));

        tblPurchaseOrderItems.setItems(FXCollections.observableArrayList(PurchaseOrderManager.getInstance().getSelected().getItems()));


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

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading purchase order data model..");

        EmployeeManager.getInstance().loadDataFromServer();
        AssetManager.getInstance().loadDataFromServer();
        ResourceManager.getInstance().loadDataFromServer();
        SupplierManager.getInstance().loadDataFromServer();
        PurchaseOrderManager.getInstance().loadDataFromServer();
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

                            //computeQuoteTotal();

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
            if (AssetManager.getInstance().getAssets() != null)
            {
                if (AssetManager.getInstance().getAssets().size() > 0)
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
                        if(PurchaseOrderManager.getInstance().getSelected()==null)
                        {
                            IO.log(getClass().getName(), IO.TAG_ERROR, "selected purchase order is invalid.");
                            return;
                        }
                        if (assetComboBox.getValue() != null)
                        {
                            PurchaseOrderAsset purchaseOrderAsset = new PurchaseOrderAsset();
                            purchaseOrderAsset.setPurchase_order_id(PurchaseOrderManager.getInstance().getSelected().get_id());
                            purchaseOrderAsset.setItem_number(tblPurchaseOrderItems.getItems().size());
                            //purchaseOrderAsset.setItem(assetComboBox.getValue());
                            purchaseOrderAsset.setItem_id(assetComboBox.getValue().get_id());
                            purchaseOrderAsset.setQuantity(1);
                            purchaseOrderAsset.setDiscount(0);
                            tblPurchaseOrderItems.getItems().add(purchaseOrderAsset);
                            tblPurchaseOrderItems.refresh();

                            //itemsModified = true;

                            //computeQuoteTotal();

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
        }
        IO.logAndAlert("Asset Purchase Order", "No assets were found in the database, please add some assets first and try again.", IO.TAG_ERROR);
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

        if (!Validators.isValidNode(txtVat, txtVat.getText(), 1, ".+"))
        {
            txtVat.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
        }
        if (!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
            return;
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
            str_vat = txtVat.getText();
            str_account = txtAccount.getText();
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
        EmployeeManager.getInstance().newExternalEmployeeWindow("Create a new Contact Person for this Purchase Order", param ->
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
}