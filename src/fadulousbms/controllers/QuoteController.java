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
    protected TableColumn colMarkup,colQuantity, colItemNumber, colEquipmentName, colDescription, colCategory, colUnit,
            colUnitCost, colValue, colRate, colTotal, colAction;
    @FXML
    protected TextField txtCell, txtTel, txtTotal, txtQuoteId, txtEmail, txtSite, txtDateGenerated, txtStatus,
            txtRevision, txtClient, txtMaterials, txtContactPerson;
    @FXML
    protected TextArea txtRequest, txtNotes;
    @FXML
    protected ToggleButton toggleVatExempt;
    @FXML
    protected ComboBox<String> cbxAccount;
    @FXML
    protected Label lblVat;
    @FXML
    protected Button btnApprove, btnNewMaterial, btnNewClient, btnNewClientRepresentative;
    protected HashMap<String, TableColumn> colsMap = new HashMap<>();
    protected ObservableList<TableColumn<QuoteItem, ?>> default_cols;

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

                                        try
                                        {
                                            //remove QuoteItem from remote server
                                            QuoteManager.getInstance().deleteObject(getTableView().getItems().get(getIndex()), quote_item_id->
                                            {
                                                if(quote_item_id != null)
                                                {
                                                    IO.logAndAlert("Success", "Successfully dissociated material [" + getTableView().getItems().get(getIndex()).getEquipment_description() + "] from quote #" + getTableView().getItems().get(getIndex()).getQuote().getObject_number(), IO.TAG_INFO);
                                                    //remove QuoteItem from memory
                                                    //TODO: QuoteManager.getInstance().getItems().remove(getTableView().getItems().get(getIndex()).get_id());
                                                    //remove QuoteItem from table
                                                    tblQuoteItems.getItems().remove(getTableView().getItems().get(getIndex()));
                                                    tblQuoteItems.refresh();//update table
                                                } else IO.logAndAlert("Error", "Could not dissociate material ["+getTableView().getItems().get(getIndex()).getEquipment_description()+"] from quote #" + getTableView().getItems().get(getIndex()).getQuote().getObject_number(), IO.TAG_ERROR);
                                                return null;
                                            });
                                        } catch (IOException e)
                                        {
                                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                                            e.printStackTrace();
                                        }

                                        if(getTableView().getItems().remove(quoteItem))
                                            getTableView().setItems(getTableView().getItems());
                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "could not remove quote item: " + quoteItem);
                                        getTableView().refresh();
                                        IO.log(getClass().getName(), IO.TAG_INFO, "removed QuoteItem[#"+quoteItem.getObject_number()+"]");
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
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(getTableView().getItems())));
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
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(getTableView().getItems())));
                                tblQuoteItems.refresh();
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

        TextFields.bindAutoCompletion(txtClient, ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    ClientManager.getInstance().setSelected(event.getCompletion());

                    IO.log(getClass().getName(), IO.TAG_INFO, "selected client id: " + ClientManager.getInstance().getSelected().get_id());
                    cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", ClientManager.getInstance().getSelected().getAccount_name()}));
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
                    ResourceManager.getInstance().setSelected(event.getCompletion());
                    IO.log(getClass().getName(), IO.TAG_INFO, "selected resource: " + ResourceManager.getInstance().getSelected().getResource_description());
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
                    EmployeeManager.getInstance().setSelected(event.getCompletion());
                    IO.log(getClass().getName(), IO.TAG_INFO, "selected contact person: " + EmployeeManager.getInstance().getSelected().getName());
                    txtCell.setText(EmployeeManager.getInstance().getSelected().getCell());
                    txtTel.setText(EmployeeManager.getInstance().getSelected().getTel());
                    txtEmail.setText(EmployeeManager.getInstance().getSelected().getEmail());
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
                case Quote.STATUS_FINALISED:
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
        QuoteManager.getInstance().initialize();

        //execute callback w/ args
        if(callback!=null)
            callback.call(true);
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
            double total = QuoteManager.computeQuoteTotal(tblQuoteItems.getItems());

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
                                    txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(tblQuoteItems.getItems())));
                                }
                            });

                            txtMarkup.setOnKeyPressed(event ->
                            {
                                if (event.getCode() == KeyCode.ENTER)
                                {
                                    callback.call(null);
                                    tblQuoteItems.refresh();
                                    txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(tblQuoteItems.getItems())));
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

            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(tblQuoteItems.getItems())));
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
                            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue()+" "+String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems())));

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

        if(ResourceManager.getInstance().getSelected()!=null)
        {
            txtClient.getStyleClass().remove("control-input-error");
            txtClient.getStyleClass().add("text-field");

            QuoteItem quoteItem = new QuoteItem();

            //quoteItem.set_id(String.valueOf(System.currentTimeMillis()));
            quoteItem.setItem_number(tblQuoteItems.getItems().size());
            quoteItem.setQuantity(1);
            quoteItem.setUnit_cost(ResourceManager.getInstance().getSelected().getResource_value());
            quoteItem.setMarkup(0);
            quoteItem.setResource_id(ResourceManager.getInstance().getSelected().get_id());
            quoteItem.setCategory(ResourceManager.getInstance().getSelected().getResourceType());

            tblQuoteItems.getItems().add(quoteItem);
            tblQuoteItems.refresh();

            itemsModified = true;

            //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
            //        String.valueOf(QuoteManager.computeQuoteTotal(QuoteManager.getInstance().getSelectedQuote())));
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue()+" "+String.valueOf(QuoteManager.computeQuoteTotal(tblQuoteItems.getItems())));

        } else
        {
            txtMaterials.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            txtClient.getStyleClass().remove("form-control-default");
            txtClient.getStyleClass().add("control-input-error");
            IO.logAndAlert("Warning", "Selected resource is invalid.", IO.TAG_WARN);
        }
    }

    @FXML
    public void newMaterial()
    {
        ResourceManager.getInstance().newResourcePopOver(btnNewMaterial, new Callback()
        {
            @Override
            public Object call(Object new_res_id)
            {
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
                                    ResourceManager.getInstance().setSelected(evt.getCompletion());

                                    IO.log(getClass().getName(), IO.TAG_INFO, "selected material: " + ResourceManager.getInstance().getSelected().getResource_description());
                                    itemsModified = true;
                                }
                            }
                        });
                    }
                });
                return null;
            }
        });
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
            if(selected.getStatus()!=Quote.STATUS_FINALISED)
            {
                selected.setStatus(Quote.STATUS_FINALISED);

                ObservableList<QuoteItem> quoteItems = tblQuoteItems.getItems();
                if(!quoteItems.isEmpty())
                {
                    QuoteManager.getInstance().updateQuote(selected, quoteItems, false, param ->
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
        if(ClientManager.getInstance().getSelected()==null)
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
        if(EmployeeManager.getInstance().getSelected()==null)
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

        if((quoteItems!=null?quoteItems.size()<=0:true))
        {
            IO.logAndAlert("Invalid Quote", "Quote items and services list is empty", IO.TAG_ERROR);
            return;
        }

        //prepare quote attributes
        Quote quote = new Quote();
        quote.setClient_id(ClientManager.getInstance().getSelected().get_id());
        quote.setContact_person_id(EmployeeManager.getInstance().getSelected().getUsr());
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
            QuoteManager.getInstance().createQuote(quote, quoteItems, new Callback()
            {
                @Override
                public Object call(Object new_quote_id)
                {
                    if(QuoteManager.getInstance().getDataset()!=null)
                        QuoteManager.getInstance().setSelected(QuoteManager.getInstance().getDataset().get(new_quote_id));
                    else IO.log(getClass().getName(), IO.TAG_ERROR, "no quotes in database.");

                    //txtQuoteId.setText(new_quote_id.toString());

                    //load Quote viewer
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
        if(ClientManager.getInstance().getSelected()==null)
        {
            txtClient.getStyleClass().remove("form-control-default");
            txtClient.getStyleClass().add("control-input-error");
            return;
        }else{
            txtClient.getStyleClass().remove("control-input-error");
            txtClient.getStyleClass().add("form-control-default");
        }

        txtContactPerson.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(EmployeeManager.getInstance().getSelected()==null)
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
            if(selected.getStatus()==Quote.STATUS_FINALISED)
            {
                IO.logAndAlert("Error", "Selected Quote has already been approved and can no longer be changed. \nCreate a new revision if you'd like to make updates to this quote.", IO.TAG_ERROR);
                return;
            }
            selected.setClient_id(ClientManager.getInstance().getSelected().get_id());
            selected.setContact_person_id(EmployeeManager.getInstance().getSelected().getUsr());
            if(toggleVatExempt.isSelected())
                selected.setVat(0);
            else selected.setVat(QuoteManager.VAT);
            selected.setSitename(txtSite.getText());
            selected.setRequest(txtRequest.getText());
            selected.setAccount_name(cbxAccount.getValue());
            if(txtNotes.getText()!=null)
                selected.setOther(txtNotes.getText().replaceAll("\n", ";"));

            QuoteManager.getInstance().updateQuote(selected, tblQuoteItems.getItems(), true, param -> {
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
                        try
                        {
                            JobManager.getInstance().putObject(job, new_job_id ->
                            {
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
                                                                .loadScreen(Screens.VIEW_JOB.getScreen(), FadulousBMS.class.getResource("views/" + Screens.VIEW_JOB
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
                                return null;
                            });
                        } catch (IOException e)
                        {
                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                            e.printStackTrace();
                        }
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
        ClientManager.getInstance().newClientPopOver(btnNewClient, null);
    }

    @FXML
    public void newEmployee()
    {
        EmployeeManager.getInstance().newEmployee(btnNewClientRepresentative, null);
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