package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Employee;
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
public class ClientsTabController extends ScreenController implements Initializable
{
    @FXML
    private TabPane clientTabs;

    @Override
    public void refreshView()
    {
        //IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients tab view.");
    }

    @Override
    public void refreshModel()
    {
        //IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients tab model.");
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //set [Clients->Job Log Sheet] Tab as selected tab by default
        if(clientTabs.getTabs()!=null)
            if(clientTabs.getTabs().size()>0)
                OperationsController.setSelectedTab(clientTabs.getTabs().get(0));

        //change selected tab when tabs are changed
        clientTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            OperationsController.setSelectedTab(newValue);
            IO.log(getClass().getName(), IO.TAG_INFO, "selected tab: " + newValue.getText());
        });

        new Thread(() ->
        {
            refreshModel();
            Platform.runLater(() -> refreshView());
        }).start();
    }
}
