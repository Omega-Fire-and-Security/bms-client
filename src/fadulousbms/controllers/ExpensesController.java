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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ExpensesController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Expense>    tblExpenses;
    @FXML
    private TableColumn     colId,colTitle,colDescription,colValue,colSupplier,
                            colDateLogged,colCreator,colAccount,colOther,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading expenses view..");

        if(SupplierManager.getInstance().getSuppliers()==null)
        {
            IO.logAndAlert(getClass().getName(), "no suppliers found in the database.", IO.TAG_ERROR);
            return;
        }
        Supplier[] suppliers = new Supplier[SupplierManager.getInstance().getSuppliers().size()];
        SupplierManager.getInstance().getSuppliers().values().toArray(suppliers);

        //Set up expenses table
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colTitle, TextFieldTableCell.forTableColumn(), 100, "expense_title", "/api/expense");
        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 100, "expense_description", "/api/expense");
        CustomTableViewControls.makeEditableTableColumn(colValue, TextFieldTableCell.forTableColumn(), 100, "expense_value", "/api/expense");

        colSupplier.setMinWidth(120);
        colSupplier.setCellValueFactory(new PropertyValueFactory<>("supplier_id"));
        colSupplier.setCellFactory(col -> new ComboBoxTableCell(SupplierManager.getInstance().getSuppliers(), "supplier_id", "/api/expense"));

        CustomTableViewControls.makeDatePickerTableColumn(colDateLogged, "date_logged", "/api/expense");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 100, "account", "/api/expense");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/expense");

        Callback colGenericCellFactory
                =
                new Callback<TableColumn<Expense, String>, TableCell<Expense, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Expense, String> param)
                    {
                        final TableCell<Expense, String> cell = new TableCell<Expense, String>()
                        {
                            final Button btnView = new Button("View");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnView.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnView.getStyleClass().add("btnApply");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

                                btnRemove.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
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

                                    btnView.setOnAction(event ->
                                    {
                                        /*Expense expense = getTableView().getItems().get(getIndex());
                                        ExpenseManager.getInstance().setSelected(expense);
                                        try
                                        {
                                            if(screenManager.loadScreen(Screens.VIEW_EXPENSE.getScreen(),getClass().getResource("../views/"+Screens.VIEW_EXPENSE.getScreen())))
                                                screenManager.setScreen(Screens.VIEW_EXPENSE.getScreen());
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load expense viewer screen.");
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }*/
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        Expense expense = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(expense);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed expense: " + expense.get_id());
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
        colAction.setCellFactory(colGenericCellFactory);

        tblExpenses.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                ExpenseManager.getInstance().setSelected(tblExpenses.getSelectionModel().getSelectedItem()));
        tblExpenses.setItems(FXCollections.observableArrayList(ExpenseManager.getInstance().getExpenses()));
    }

    @Override
    public void refreshModel()
    {
        ExpenseManager.getInstance().initialize();
        SupplierManager.getInstance().initialize();
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
