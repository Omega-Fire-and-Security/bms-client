/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class RevenueController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Revenue>    tblRevenue;
    @FXML
    private TableColumn     colId,colTitle,colDescription,colValue,colDateLogged,
                            colCreator,colAccount,colOther,colAction;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
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
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading revenue view..");

        //Set up expenses table
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colTitle, TextFieldTableCell.forTableColumn(), 100, "revenue_title", RevenueManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 100, "revenue_description", RevenueManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colValue, TextFieldTableCell.forTableColumn(), 100, "revenue_value", RevenueManager.getInstance());
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged", false);
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 100, "account", RevenueManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 100, "extra", RevenueManager.getInstance());

        Callback colGenericCellFactory
                =
                new Callback<TableColumn<Revenue, String>, TableCell<Revenue, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Revenue, String> param)
                    {
                        final TableCell<Revenue, String> cell = new TableCell<Revenue, String>()
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
                                        Revenue revenue = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(revenue);
                                        getTableView().refresh();
                                        //TODO: remove from server
                                        IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed revenue: " + revenue.get_id());
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

        tblRevenue.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                RevenueManager.getInstance().setSelected(tblRevenue.getSelectionModel().getSelectedItem()));
        tblRevenue.setItems(FXCollections.observableArrayList(RevenueManager.getInstance().getDataset().values()));
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading revenue data model..");

        EmployeeManager.getInstance().initialize();
        RevenueManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        RevenueManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
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
