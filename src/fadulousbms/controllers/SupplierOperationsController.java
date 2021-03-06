package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.SupplierManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TabPane;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/11/15.
 */
public class SupplierOperationsController extends ScreenController implements Initializable
{
    @FXML
    private TabPane suppliersTabs;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //change selected tab when tabs are changed
        suppliersTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            if(newValue!=null)
                OperationsController.setSelectedTab(newValue);
        });
        
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
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading supplier ops tab view.");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading supplier ops tab model.");

        SupplierManager.getInstance().forceSynchronise();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        //SupplierManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
