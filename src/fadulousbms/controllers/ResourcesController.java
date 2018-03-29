/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.ScreenManager;
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
public class ResourcesController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Resource> tblResources;
    @FXML
    private TableColumn colId,colName,colSerial,colType,colDescription,colValue,colAccount,colUnit,
                        colQuantity,colDateAcquired,colDateExhausted,colOther,colAction;
    @FXML
    private Tab stockTab;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading resources/materials view..");
        /*if(ResourceManager.getInstance().getResource_types()!=null)
        {
            ResourceType[] res_types = new ResourceType[ResourceManager.getInstance().getResource_types().size()];
            ResourceManager.getInstance().getResource_types().values().toArray(res_types);
        }*/

        colId.setMinWidth(80);
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colName, TextFieldTableCell.forTableColumn(), 80, "brand_name", ResourceManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colSerial, TextFieldTableCell.forTableColumn(), 80, "resource_code", ResourceManager.getInstance());

        colType.setMinWidth(120);
        colType.setCellValueFactory(new PropertyValueFactory<>("resource_type"));
        colType.setCellFactory(col -> new ComboBoxTableCell(ResourceManager.getInstance().getResource_types(), "resource_type", "type_name"));

        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 100, "resource_description", ResourceManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colValue, TextFieldTableCell.forTableColumn(), 80, "resource_value", ResourceManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 80, "account", ResourceManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colUnit, TextFieldTableCell.forTableColumn(), 50, "unit", ResourceManager.getInstance());
        CustomTableViewControls.makeEditableTableColumn(colQuantity, TextFieldTableCell.forTableColumn(), 50, "quantity", ResourceManager.getInstance());
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateAcquired, "date_acquired");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateExhausted, "date_exhausted");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 80, "other", ResourceManager.getInstance());

        ObservableList<Resource> lst_resources = FXCollections.observableArrayList();
        if(ResourceManager.getInstance().getDataset()!=null)
        {
            lst_resources.addAll(ResourceManager.getInstance().getDataset().values());
            tblResources.setItems(lst_resources);
        }

        Callback<TableColumn<Resource, String>, TableCell<Resource, String>> cellFactory
                =
                new Callback<TableColumn<Resource, String>, TableCell<Resource, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Resource, String> param)
                    {
                        final TableCell<Resource, String> cell = new TableCell<Resource, String>()
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
                                    Resource resource = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        ResourceManager.getInstance().setSelected(resource);
                                        //screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        if(getTableView().getItems().get(getIndex())!=null)
                                            if(getTableView().getItems().get(getIndex()) instanceof Resource)
                                                ResourceManager.getInstance().setSelected(getTableView().getItems().get(getIndex()));

                                        try
                                        {
                                            //remove Resource from remote server
                                            ResourceManager.getInstance().deleteObject(ResourceManager.getInstance().getSelected(), res_id->
                                            {
                                                if(res_id != null)
                                                {
                                                    IO.logAndAlert("Success", "Successfully deleted material [#" + ResourceManager.getInstance().getSelected().getObject_number() + "]{"+res_id+"}", IO.TAG_INFO);
                                                    //remove Resource from memory
                                                    ResourceManager.getInstance().getDataset().remove(ResourceManager.getInstance().getSelected());
                                                    //remove Resource from table
                                                    tblResources.getItems().remove(ResourceManager.getInstance().getSelected());
                                                    tblResources.refresh();//update table
                                                } else IO.logAndAlert("Error", "Could not delete material [#"+ResourceManager.getInstance().getSelected().getObject_number()+"]{"+res_id+"}", IO.TAG_ERROR);
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

        colAction.setMinWidth(250);
        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);

        tblResources.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                ResourceManager.getInstance().setSelected(tblResources.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading resources data model..");

        ResourceManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        ResourceManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        OperationsController.registerTabController(stockTab.getId(),this);
        new Thread(() ->
                refreshModel(param ->
                {
                    Platform.runLater(() -> refreshView());
                    return null;
                })).start();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void createResourceClick()
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
                        if(screenManager.loadScreen(Screens.NEW_RESOURCE.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_RESOURCE.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_RESOURCE.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load resource creation screen.");
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
