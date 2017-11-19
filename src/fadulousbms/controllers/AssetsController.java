/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.AssetManager;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

import java.net.URL;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class AssetsController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Asset> tblAssets;
    @FXML
    private TableColumn colId,colName,colSerial,colType,colDescription,colValue,colAccount,colUnit,
                        colQuantity,colDateAcquired,colDateExhausted,colOther,colAction;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading assets view..");

        if(AssetManager.getInstance().getAssets()==null)
        {
            IO.logAndAlert(getClass().getName(), "no assets found in database.", IO.TAG_ERROR);
            return;
        }
        if(AssetManager.getInstance().getAsset_types()==null)
        {
            IO.logAndAlert(getClass().getName(), "no asset types found in database.", IO.TAG_ERROR);
            return;
        }

        Asset[] assets = new Asset[AssetManager.getInstance().getAssets().size()];
        AssetManager.getInstance().getAssets().values().toArray(assets);

        AssetType[] asset_types = new AssetType[AssetManager.getInstance().getAsset_types().size()];
        AssetManager.getInstance().getAsset_types().values().toArray(asset_types);

        colId.setMinWidth(100);
        colId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        CustomTableViewControls.makeEditableTableColumn(colName, TextFieldTableCell.forTableColumn(), 120, "asset_name", "/api/asset");
        CustomTableViewControls.makeEditableTableColumn(colSerial, TextFieldTableCell.forTableColumn(), 60, "asset_serial", "/api/asset");

        colType.setMinWidth(120);
        colType.setCellValueFactory(new PropertyValueFactory<>("asset_type"));
        colType.setCellFactory(col -> new ComboBoxTableCell(AssetManager.getInstance().getAsset_types(), "asset_type", "/api/asset"));

        CustomTableViewControls.makeEditableTableColumn(colDescription, TextFieldTableCell.forTableColumn(), 215, "asset_description", "/api/asset");
        CustomTableViewControls.makeEditableTableColumn(colValue, TextFieldTableCell.forTableColumn(), 60, "asset_value", "/api/asset");
        CustomTableViewControls.makeEditableTableColumn(colAccount, TextFieldTableCell.forTableColumn(), 60, "account", "/api/asset");
        CustomTableViewControls.makeEditableTableColumn(colUnit, TextFieldTableCell.forTableColumn(), 50, "unit", "/api/asset");
        colQuantity.setMinWidth(80);
        colQuantity.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        //CustomTableViewControls.makeEditableTableColumn(colQuantity, TextFieldTableCell.forTableColumn(), 50, "quantity", "/api/asset");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateAcquired, "date_acquired", "/api/asset");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateExhausted, "date_exhausted", "/api/asset");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 120, "other", "/api/asset");

        ObservableList<Asset> lst_assets = FXCollections.observableArrayList();
        lst_assets.addAll(AssetManager.getInstance().getAssets().values());
        tblAssets.setItems(lst_assets);

        Callback<TableColumn<Asset, String>, TableCell<Asset, String>> cellFactory
                =
                new Callback<TableColumn<Asset, String>, TableCell<Asset, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Asset, String> param)
                    {
                        final TableCell<Asset, String> cell = new TableCell<Asset, String>()
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
                                    Asset asset = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        //System.out.println("Successfully added material quote number " + quoteItem.getItem_number());
                                        AssetManager.getInstance().setSelected(asset);
                                        //screenManager.setScreen(Screens.VIEW_JOB.getScreen());
                                    });

                                    btnRemove.setOnAction(event ->
                                    {
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        getTableView().getItems().remove(asset);
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

        tblAssets.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                AssetManager.getInstance().setSelected(tblAssets.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        AssetManager.getInstance().initialize();
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
}
