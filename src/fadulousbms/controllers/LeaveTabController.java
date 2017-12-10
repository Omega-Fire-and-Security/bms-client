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

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/11/15.
 */
public class LeaveTabController extends ScreenController implements Initializable
{
    @FXML
    private TableColumn colLeaveId, colEmployee, colStatus, colStartDate, colEndDate,
            colReturnDate, colDateLogged, colOther, colAction;
    @FXML
    private TableView<Leave> tblLeave;
    public static final String TAB_ID = "leaveTab";

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading leave tab view.");

        colLeaveId.setMinWidth(100);
        colLeaveId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colEmployee.setMinWidth(100);
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colStartDate, "start_date", "/api/leave");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colEndDate, "end_date", "/api/leave");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colReturnDate, "return_date", "/api/leave");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged", "/api/leave");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 120, "other", "/api/leave");

        if(LeaveManager.getInstance().getLeaveRecords()!=null)
        {
            ObservableList<Leave> lst_leave = FXCollections.observableArrayList();
            lst_leave.addAll(LeaveManager.getInstance().getLeaveRecords().values());
            tblLeave.setItems(lst_leave);
        } else IO.log(getClass().getName(), IO.TAG_WARN, "no leave records were found in the database.");

        tblLeave.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                LeaveManager.getInstance().setSelected(tblLeave.getSelectionModel().getSelectedItem()));

        Callback<TableColumn<Leave, String>, TableCell<Leave, String>> cellFactory
                =
                new Callback<TableColumn<Leave, String>, TableCell<Leave, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Leave, String> param)
                    {
                        final TableCell<Leave, String> cell = new TableCell<Leave, String>()
                        {
                            final Button btnApprove = new Button("Approve");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnApprove.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                btnApprove.getStyleClass().add("btnDefault");
                                btnApprove.setMinWidth(100);
                                btnApprove.setMinHeight(35);
                                HBox.setHgrow(btnApprove, Priority.ALWAYS);

                                btnRemove.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
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
                                    HBox hBox = new HBox(btnApprove, btnRemove);
                                    hBox.setMaxWidth(Double.MAX_VALUE);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    Leave leave = getTableView().getItems().get(getIndex());

                                    btnApprove.setOnAction(event ->
                                            LeaveManager.approveLeave(leave, param ->
                                            {
                                                new Thread(() ->
                                                {
                                                    refreshModel();
                                                    Platform.runLater(() -> refreshView());
                                                }).start();
                                                return null;
                                            }));

                                    btnRemove.setOnAction(event ->
                                    {
                                        //197.242.144.30
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        //getTableView().getItems().remove(quote);
                                        //getTableView().refresh();
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
        colAction.setMinWidth(700);
    }

    public void refreshTable()
    {
        tblLeave.refresh();
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading leave tab model.");
        try
        {
            LeaveManager.getInstance().reloadDataFromServer();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static RadialMenuItem[] getContextMenu()
    {
        RadialMenuItem menuLeave = new RadialMenuItemCustom(30, "Approve", null, null, event ->
                LeaveManager.approveLeave(LeaveManager.getInstance().getSelected(), param ->
                {
                    //refresh UI on approve
                    try
                    {
                        //refresh model
                        LeaveManager.getInstance().reloadDataFromServer();
                        //refresh view
                        final ScreenManager screenManager = ScreenManager.getInstance();
                        ScreenManager.getInstance().showLoadingScreen(arg ->
                        {
                            new Thread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        if(screenManager.loadScreen(Screens.HR.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.HR.getScreen())))
                                        {
                                            //Platform.runLater(() ->
                                            screenManager.setScreen(Screens.HR.getScreen());
                                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load HR screen.");
                                    } catch (IOException e)
                                    {
                                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        e.printStackTrace();
                                    }
                                }
                            }).start();
                            return null;
                        });
                    } catch (ClassNotFoundException e)
                    {
                        e.printStackTrace();
                        IO.log(LeaveTabController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                        IO.log(LeaveTabController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    }
                    return null;
                }));
        return new RadialMenuItem[]{menuLeave};
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
