package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

public class NewJobController extends ScreenController implements Initializable

{
    @FXML
    protected TableView<QuoteItem> tblJobItems;
    @FXML
    private TableView<Employee> tblEmployees;
    //Job items table columns
    @FXML
    protected TableColumn colMarkup,colQuantity, colItemNumber, colEquipmentName, colDescription, colUnit, colValue, colRate, colTotal, colAction;
    @FXML
    protected ComboBox<Client> cbxClients;
    @FXML
    protected ComboBox<Employee> cbxContactPerson;
    @FXML
    protected TextField txtCell,txtTel,txtTotal,txtJobId,txtFax,txtEmail,txtSite,txtDateGenerated,txtStatus,txtRevision,txtExtra;
    //@FXML
    //protected Slider vatSlider;
    @FXML
    protected ToggleButton toggleVatExempt;
    @FXML
    protected ComboBox<String> cbxAccount;
    @FXML
    protected Label lblVat;
    @FXML
    protected Button btnApprove;
    @FXML
    protected TextArea txtRequest;
    protected HashMap<String, TableColumn> colsMap = new HashMap<>();
    private ObservableList<TableColumn<QuoteItem, ?>> default_cols;
    protected boolean itemsModified;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //colAction.setCellFactory(new ButtonTableCellFactory<>());
        colAction.setCellValueFactory(new PropertyValueFactory<>(""));

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
                                        QuoteItem jobItem = getTableView().getItems().get(getIndex());
                                        addQuoteItemAdditionalMaterial(jobItem);
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //TODO: deal with server side
                                        QuoteItem jobItem = getTableView().getItems().get(getIndex());
                                        //remove QuoteItem's additional costs TableColumns
                                        if (jobItem.getAdditional_costs() != null)
                                        {
                                            for (String str_cost : jobItem.getAdditional_costs().split(";"))
                                            {
                                                String[] arr = str_cost.split("=");
                                                if (arr != null)
                                                {
                                                    if (arr.length > 1)
                                                    {
                                                        //if column exists in map, remove it
                                                        if(colsMap.get(arr[0].toLowerCase())!=null)
                                                        {
                                                            tblJobItems.getColumns().remove(colsMap.get(arr[0].toLowerCase()));
                                                            IO.log(getClass().getName(), IO.TAG_INFO, "removed QuoteItem["+jobItem.get_id()+"] additional cost column: "+arr[0]);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        getTableView().getItems().remove(jobItem);
                                        getTableView().refresh();
                                        IO.log(getClass().getName(), IO.TAG_INFO, "removed QuoteItem["+jobItem.get_id()+"]");
                                        txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(((Job)JobManager.getInstance().getSelected()).getTotal()));
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

        Callback<TableColumn<Employee, String>, TableCell<Employee, String>> actionColCellFactory
                =
                new Callback<TableColumn<Employee, String>, TableCell<Employee, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Employee, String> param)
                    {
                        final TableCell<Employee, String> cell = new TableCell<Employee, String>()
                        {
                            final Button btnRemove = new Button("Remove");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

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
                                    btnRemove.setOnAction(event ->
                                    {
                                        Employee employee = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(employee);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        System.out.println("Successfully removed sale representative: " + employee.getName());
                                    });
                                    setGraphic(btnRemove);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };
    }
    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading job creation view.");

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

        Employee[] employees=null;
        if(EmployeeManager.getInstance().getDataset()!=null)
        {
            employees = new Employee[EmployeeManager.getInstance().getDataset().values().toArray().length];
            EmployeeManager.getInstance().getDataset().values().toArray(employees);
        }

        //setup Job default accounts
        cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash"}));
        cbxClients.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue!=null)
            {
                IO.log(getClass().getName(), IO.TAG_INFO, "selected client id: " + newValue.get_id());
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", newValue.getAccount_name()}));
                txtFax.setText(newValue.getFax());
                itemsModified=true;
            }
        });

        refreshTotal();
        toggleVatExempt.selectedProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue)
                toggleVatExempt.setText("VAT exempt");
            else toggleVatExempt.setText(QuoteManager.VAT+ "%");
            refreshTotal();
        });

        tblJobItems.getItems().clear();

        //Setup Job Items table
        colMarkup.setCellValueFactory(new PropertyValueFactory<>("markup"));
        colMarkup.setCellFactory(param -> new TableCell()
        {
            final TextField txt = new TextField("0.0");

            @Override
            protected void updateItem(Object item, boolean empty)
            {
                super.updateItem(item, empty);
                if (getIndex() >= 0 && getIndex() < tblJobItems.getItems().size())
                {
                    QuoteItem jobItem = tblJobItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem job_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                job_item.setMarkup(Double.valueOf(txt.getText()));
                                //TODO: include service items below
                                txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.getInstance().computeQuoteTotal(getTableView().getItems())));
                                tblJobItems.refresh();
                                //RemoteComms.updateBusinessObjectOnServer(job_item, "/api/job/resource", "markup");
                                //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));
                                //tblJobItems.refresh();
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid markup percentage.", IO.TAG_ERROR);
                                return;
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO,"Successfully updated [markup percentage] property for job item #" + job_item.getItem_number());
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(jobItem.getMarkup());
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
                if (getIndex() >= 0 && getIndex() < tblJobItems.getItems().size())
                {
                    QuoteItem jobItem = tblJobItems.getItems().get(getIndex());
                    //update QuoteItem object on TextField commit
                    txt.setOnKeyPressed(event ->
                    {
                        if(event.getCode()== KeyCode.ENTER)
                        {
                            QuoteItem job_item = (QuoteItem) getTableView().getItems().get(getIndex());
                            try
                            {
                                job_item.setQuantity(Integer.valueOf(txt.getText()));
                                //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(getTableView().getItems())));
                                refreshTotal();
                                tblJobItems.refresh();
                            }catch (NumberFormatException e)
                            {
                                IO.logAndAlert("Error","Please enter a valid quantity.", IO.TAG_ERROR);
                                return;
                            }
                            IO.log(getClass().getName(), IO.TAG_INFO,"Successfully updated [quantity] property for job item #" + job_item.getItem_number());
                        }
                    });

                    if (!empty)
                    {
                        txt.setText(jobItem.getQuantity());
                        setGraphic(txt);
                    } else setGraphic(null);
                    getTableView().refresh();
                }
            }
        });

        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        cbxClients.setItems(FXCollections.observableArrayList((Collection<Client>) ClientManager.getInstance().getDataset().values()));
        cbxClients.setCellFactory(new Callback<ListView<Client>, ListCell<Client>>()
        {
            @Override
            public ListCell<Client> call(ListView<Client> param)
            {
                return new ListCell<Client>()
                {
                    @Override
                    protected void updateItem(Client item, boolean empty)
                    {
                        super.updateItem(item, empty);
                        if (item == null || empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            setText(item.getClient_name());
                        }
                    }
                };
            }
        });
        cbxClients.setButtonCell(null);
        /*cbxClients.setOnAction(event ->
        {
            if(cbxClients.getValue()!=null)
            {
                txtFax.setText(cbxClients.getValue().getFax());
                itemsModified=true;
            }
            //else IO.logAndAlert("Invalid Client", "Client company selected is invalid.", IO.TAG_ERROR);
        });*/

        cbxContactPerson.setCellFactory(new Callback<ListView<Employee>, ListCell<Employee>>()
        {
            @Override public ListCell<Employee> call(ListView<Employee> p)
            {
                return new ListCell<Employee>()
                {
                    @Override
                    protected void updateItem(Employee item, boolean empty)
                    {
                        super.updateItem(item, empty);

                        if (item == null || empty)
                        {
                            setGraphic(null);
                        } else{
                            setText(item.getFirstname() + " " + item.getLastname());
                        }
                    }
                };
            }
        });
        //cbxContactPerson.setButtonCell(null);
        if(employees!=null)
            cbxContactPerson.setItems(FXCollections.observableArrayList(employees));
        cbxContactPerson.setOnAction(event ->
        {
            Employee employee = cbxContactPerson.getValue();
            if(employee!=null)
            {
                txtCell.setText(employee.getCell());
                txtTel.setText(employee.getTel());
                txtEmail.setText(employee.getEmail());
                itemsModified=true;
            }//else IO.logAndAlert("Invalid Employee", "Selected contact person is invalid", IO.TAG_ERROR);
        });

        if(default_cols==null)
            default_cols= tblJobItems.getColumns();

        //set status
        String status;
        if(JobManager.getInstance().getSelected()!=null)
        {
            switch (((Job)JobManager.getInstance().getSelected()).getStatus())
            {
                case Job.STATUS_PENDING:
                    status = "PENDING";
                    break;
                case Job.STATUS_FINALISED:
                    status = "APPROVED";
                    break;
                case Job.STATUS_ARCHIVED:
                    status = "ARCHIVED";
                    break;
                default:
                    status = "UNKNOWN";
                    IO.logAndAlert("Error", "Unknown Job status: " + ((Job)JobManager.getInstance().getSelected()).getStatus(), IO.TAG_ERROR);
                    break;
            }
            if(txtStatus!=null)
                txtStatus.setText(status);
        }
    }

    protected void refreshTotal()
    {
        double vat = QuoteManager.VAT;
        if(toggleVatExempt.isSelected())//if is VAT exempt
            vat =0.0;//is VAT exempt
        lblVat.setText("VAT ["+new DecimalFormat("##.##").format(vat)+"%]");
        if(tblJobItems.getItems()!=null)
        {
            double total = QuoteManager.computeQuoteTotal(tblJobItems.getItems());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                    new DecimalFormat("##.##").format((total + (total*(vat)))));
        }
        if(tblJobItems.getItems()!=null)
        {
            double total = QuoteManager.computeQuoteTotal(tblJobItems.getItems());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                    new DecimalFormat("##.##").format((total + (total*(vat/100)))));
        }
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading job model data-set.");

        EmployeeManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        ResourceManager.getInstance().initialize();
        QuoteManager.getInstance().initialize();
        JobManager.getInstance().initialize();
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        JobManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
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
                        if (getIndex() >= 0 && getIndex() < tblJobItems.getItems().size())
                        {
                            QuoteItem jobItem = tblJobItems.getItems().get(getIndex());
                            //update QuoteItem object on TextField commit
                            Callback callback = new Callback()
                            {
                                @Override
                                public Object call(Object param)
                                {
                                    if (jobItem != null)
                                    {
                                        String new_cost = col.getText().toLowerCase() + "=" + txtCost.getText() + "*" + txtMarkup.getText();
                                        String old_add_costs = "";
                                        if (jobItem.getAdditional_costs() != null)
                                            old_add_costs = jobItem.getAdditional_costs().toLowerCase();
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
                                        IO.log(getClass().getName(), IO.TAG_INFO, "committed additional costs for job item [#" + jobItem.getItem_number() + "]:: " + new_add_costs);
                                        jobItem.setAdditional_costs(new_add_costs);
                                    }
                                    return null;
                                }
                            };

                            txtCost.setOnKeyPressed(event ->
                            {
                                if (event.getCode() == KeyCode.ENTER)
                                {
                                    callback.call(null);
                                    tblJobItems.refresh();
                                    txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));
                                }
                            });
                            txtMarkup.setOnKeyPressed(event ->
                            {
                                if (event.getCode() == KeyCode.ENTER)
                                {
                                    callback.call(null);
                                    tblJobItems.refresh();
                                    txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));
                                }
                            });

                            //render the cell
                            if (!empty)// && item!=null
                            {
                                if (jobItem.getAdditional_costs() == null)
                                {
                                    txtCost.setText("0.0");
                                    txtMarkup.setText("0.0");
                                } else if (jobItem.getAdditional_costs().length() <= 0)
                                {
                                    txtCost.setText("0.0");
                                    txtMarkup.setText("0.0");
                                } else if (jobItem.getAdditional_costs().length() > 0)
                                {
                                    if(jobItem.getAdditional_costs() != null)
                                    {
                                        for (String str_cost : jobItem.getAdditional_costs().split(";"))
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
                                                        }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid job item additional cost ["+str_cost+"].");
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
                        } //else IO.log("Job Materials Table", IO.TAG_ERROR, "index out of bounds [" + getIndex() + "]");
                    }
                };
            }
        };
    }

    public void addQuoteItemAdditionalMaterial(QuoteItem jobItem)
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
            if(jobItem.getAdditional_costs()==null)
                jobItem.setAdditional_costs(new_cost);
            else if(jobItem.getAdditional_costs().isEmpty())
                jobItem.setAdditional_costs(new_cost);
            else
            {
                //if additional cost exists already, update its value
                if(jobItem.getAdditional_costs().toLowerCase().contains(txtName.getText().toLowerCase()))
                {
                    String old_add_costs = jobItem.getAdditional_costs().toLowerCase();
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
                    jobItem.setAdditional_costs(new_add_costs);
                } else jobItem.setAdditional_costs(jobItem.getAdditional_costs()+";"+new_cost);
            }

            //RemoteComms.updateBusinessObjectOnServer(jobItem, "/api/job/resource", txtName.getText());

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
            tblJobItems.getColumns().add(col);
            tblJobItems.refresh();

            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));
        });

        HBox row1 = new HBox(lblName, txtName);
        HBox row2 = new HBox(lblCost, txtCost);
        HBox row3 = new HBox(lblMarkup, txtMarkup);
        row1.setSpacing(20);
        row2.setSpacing(20);
        row3.setSpacing(20);
        //HBox row3 = new HBox(new Label("Markup"), txtMarkup);

        stage.setTitle("Extra Costs For Job Item #"+jobItem.getItem_numberValue());
        stage.setScene(new Scene(new VBox(row1, row2, row3, btnAdd)));
        stage.show();
    }

    @FXML
    public void newJobItem()
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
                    stage.setTitle("Add Job Materials");
                    stage.setScene(new Scene(vBox));
                    stage.setAlwaysOnTop(true);
                    stage.setResizable(false);
                    stage.show();

                    btnAdd.setOnAction(event ->
                    {
                        if(resourceComboBox.getValue()!=null)
                        {
                            QuoteItem jobItem = new QuoteItem();

                            jobItem.setItem_number(tblJobItems.getItems().size());
                            jobItem.setQuantity(1);
                            jobItem.setUnit_cost(resourceComboBox.getValue().getResource_value());
                            jobItem.setMarkup(0);
                            jobItem.setResource_id(resourceComboBox.getValue().get_id());
                            //jobItem.setEquipment_description(resourceComboBox.getValue().getResource_description());
                            //jobItem.setUnit(resourceComboBox.getValue().getUnit());
                            //jobItem.setRate(resourceComboBox.getValue().getResource_value());
                            //jobItem.setValue(resourceComboBox.getValue().getResource_value());
                            //jobItem.setResource(resourceComboBox.getValue());
                            //jobItem.setEquipment_name(resourceComboBox.getValue().getResource_name());

                            tblJobItems.getItems().add(jobItem);
                            tblJobItems.refresh();

                            itemsModified = true;

                            //txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " +
                            //        String.valueOf(QuoteManager.computeQuoteTotal(JobManager.getInstance().getSelectedJob())));
                            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue()+" "+String.valueOf(QuoteManager.computeQuoteTotal(tblJobItems.getItems())));

                        } else IO.logAndAlert("New Job Resource", "Invalid resource selected.", IO.TAG_ERROR);
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
                                            newJobItem();
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
    }

    public void createJob()
    {
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

        cbxClients.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxClients.getValue()==null)
        {
            cbxClients.getStyleClass().remove("form-control-default");
            cbxClients.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxClients.getStyleClass().remove("control-input-error");
            cbxClients.getStyleClass().add("form-control-default");
        }

        cbxContactPerson.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        if(cbxContactPerson.getValue()==null)
        {
            cbxContactPerson.getStyleClass().remove("form-control-default");
            cbxContactPerson.getStyleClass().add("control-input-error");
            return;
        }else{
            cbxContactPerson.getStyleClass().remove("control-input-error");
            cbxContactPerson.getStyleClass().add("form-control-default");
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

        List<QuoteItem> jobItems = tblJobItems.getItems();

        if(jobItems==null)
        {
            IO.logAndAlert("Invalid Job", "Job items list is null.", IO.TAG_ERROR);
            return;
        }
        if(jobItems.size()<=0)
        {
            IO.logAndAlert("Invalid Job", "Job has no materials", IO.TAG_ERROR);
            return;
        }

        //prepare job attributes
        Quote quote = new Quote();
        quote.setClient_id(cbxClients.getValue().get_id());
        quote.setContact_person_id(cbxContactPerson.getValue().getUsr());
        quote.setSitename(txtSite.getText());
        quote.setRequest(txtRequest.getText());
        quote.setStatus(Job.STATUS_FINALISED);
        quote.setAccount_name(cbxAccount.getValue());
        quote.setCreator(SessionManager.getInstance().getActive().getUsr());
        quote.setRevision(1.0);

        if(toggleVatExempt.isSelected())
            quote.setVat(0);
        else quote.setVat(QuoteManager.VAT);

        if(txtExtra!=null)
            if(txtExtra.getText()!=null)
                quote.setOther(txtExtra.getText());

        try
        {
            //create Job
            QuoteManager.getInstance().createQuote(quote, tblJobItems.getItems(), new Callback()
            {
                @Override
                public Object call(Object quote_id)
                {
                    if(quote_id!=null)
                    {
                        Job job = new Job();
                        job.setQuote_id((String) quote_id);
                        job.setCreator(SessionManager.getInstance().getActiveEmployee().getUsr());

                        //create Job
                        try {
                            JobManager.getInstance().putObject(job, new Callback()
                            {
                                @Override
                                public Object call(Object job_id)
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
                                                    //reload quote data-set
                                                    QuoteManager.getInstance().forceSynchronise();
                                                    //reload job data-set
                                                    JobManager.getInstance().forceSynchronise();
                                                    if(job_id!=null)
                                                    {
                                                        //update selected job
                                                        JobManager.getInstance().setSelected(JobManager.getInstance().getDataset().get(job_id));
                                                        //load viewer
                                                        if (ScreenManager.getInstance().loadScreen(Screens.VIEW_JOB.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/" + Screens.VIEW_JOB.getScreen()))) {
                                                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_JOB.getScreen()));
                                                        } else
                                                            IO.log(getClass().getName(), IO.TAG_ERROR, "could not load job viewer  screen.");
                                                    }
                                                } catch (IOException e) {
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
                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                            e.printStackTrace();
                        }
                        txtJobId.setText(quote_id.toString());
                    } //else did not create job
                    return null;
                }
            });
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void assignEmployee()
    {
        EmployeeManager.getInstance().initialize();

        if(EmployeeManager.getInstance().getDataset()!=null)
        {
            if(EmployeeManager.getInstance().getDataset().size()>0)
            {
                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                Employee[] employees = new Employee[EmployeeManager.getInstance().getDataset().values().toArray().length];
                EmployeeManager.getInstance().getDataset().values().toArray(employees);

                ComboBox<Employee> employeeComboBox = new ComboBox<>();
                employeeComboBox.setMinWidth(120);
                employeeComboBox.setItems(FXCollections.observableArrayList(employees));
                HBox.setHgrow(employeeComboBox, Priority.ALWAYS);

                Button btnAdd = new Button("Add");
                btnAdd.setMinWidth(80);
                btnAdd.setMinHeight(40);
                btnAdd.setDefaultButton(true);
                btnAdd.getStyleClass().add("btnApply");
                btnAdd.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                Button btnCancel = new Button("Close");
                btnCancel.setMinWidth(80);
                btnCancel.setMinHeight(40);
                btnCancel.getStyleClass().add("btnBack");
                btnCancel.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

                HBox hBox = new HBox(new Label("Employee: "), employeeComboBox);
                HBox.setHgrow(hBox, Priority.ALWAYS);
                hBox.setSpacing(20);

                HBox hBoxButtons = new HBox(btnAdd, btnCancel);
                hBoxButtons.setHgrow(btnAdd, Priority.ALWAYS);
                hBoxButtons.setHgrow(btnCancel, Priority.ALWAYS);
                hBoxButtons.setSpacing(20);

                VBox vBox = new VBox(hBox, hBoxButtons);
                VBox.setVgrow(vBox, Priority.ALWAYS);
                vBox.setSpacing(20);
                HBox.setHgrow(vBox, Priority.ALWAYS);
                vBox.setFillWidth(true);

                Stage stage = new Stage();
                stage.setTitle("Add Job Representative");
                stage.setScene(new Scene(vBox));
                stage.setAlwaysOnTop(true);
                stage.show();

                btnAdd.setOnAction(event ->
                {
                    if(employeeComboBox.getValue()!=null)
                    {
                        tblEmployees.getItems().add(employeeComboBox.getValue());
                        itemsModified=true;
                    } else IO.logAndAlert("Add Job Representative", "Invalid employee selected.", IO.TAG_ERROR);
                });

                btnCancel.setOnAction(event ->
                        stage.close());
                return;
            } else IO.logAndAlert("New Job Representative", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
        }else IO.logAndAlert("New Job Representative", "No employees were found in the database, please add an employee first and try again.",IO.TAG_ERROR);
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
