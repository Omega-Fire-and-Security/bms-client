package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.*;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Employee;
import fadulousbms.model.Overtime;
import fadulousbms.model.Screens;
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
 * Created by ghost on 2017/11/15.
 */
public class OvertimeTabController extends ScreenController implements Initializable
{
    @FXML
    private TableColumn colEmployeeId, colEmployee, colStatus, colJobNumber, colDate,
            colTimeIn, colTimeOut, colDateLogged, colOther, colAction;
    @FXML
    private TableView<Overtime> tblOvertime;
    public static final String TAB_ID = "overtimeTab";

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

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading overtime tab view.");

        colEmployeeId.setMinWidth(100);
        colEmployeeId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colEmployee.setMinWidth(100);
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employee"));
        colJobNumber.setMinWidth(100);
        colJobNumber.setCellValueFactory(new PropertyValueFactory<>("job_number"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        CustomTableViewControls.makeEditableTableColumn(colTimeIn, TextFieldTableCell.forTableColumn(), 120, "time_in", "/overtime_records");
        CustomTableViewControls.makeEditableTableColumn(colTimeOut, TextFieldTableCell.forTableColumn(), 120, "time_out", "/overtime_records");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDate, "date");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 120, "other", "");

        if(OvertimeManager.getInstance().getDataset()!=null)
        {
            ObservableList<Overtime> lst_overtime = FXCollections.observableArrayList();
            lst_overtime.addAll(OvertimeManager.getInstance().getDataset().values());
            tblOvertime.setItems(lst_overtime);
        } else IO.log(getClass().getName(), IO.TAG_WARN, "no overtime records were found in the database.");

        tblOvertime.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                OvertimeManager.getInstance().setSelected(tblOvertime.getSelectionModel().getSelectedItem()));

        Callback<TableColumn<Overtime, String>, TableCell<Overtime, String>> cellFactory
                =
                new Callback<TableColumn<Overtime, String>, TableCell<Overtime, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Overtime, String> param)
                    {
                        final TableCell<Overtime, String> cell = new TableCell<Overtime, String>()
                        {
                            final Button btnApprove = new Button("Approve");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnApprove.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnApprove.getStyleClass().add("btnDefault");
                                btnApprove.setMinWidth(100);
                                btnApprove.setMinHeight(35);
                                HBox.setHgrow(btnApprove, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnApprove, btnRemove);
                                    hBox.setMaxWidth(Double.MAX_VALUE);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    Overtime overtime = getTableView().getItems().get(getIndex());

                                    btnApprove.setOnAction(event ->
                                            OvertimeManager.approveOvertime(overtime, param ->
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

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading overtime tab model.");
        JobManager.getInstance().initialize();
        OvertimeManager.getInstance().initialize();
    }

    @Override
    public void forceSynchronise()
    {
        OvertimeManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getContextMenu()
    {
        RadialMenuItem menuOvertime = new RadialMenuItemCustom(30, "Approve", null, null, event ->
                OvertimeManager.approveOvertime((Overtime) OvertimeManager.getInstance().getSelected(), param ->
                {
                    //refresh UI on approve
                    //refresh model
                    OvertimeManager.getInstance().initialize();
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
                                    if(screenManager.loadScreen(Screens.HR.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.HR.getScreen())))
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
                    return null;
                }));
        return new RadialMenuItem[]{menuOvertime};
    }


}
