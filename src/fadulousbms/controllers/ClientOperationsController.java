package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.managers.JobManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.util.Callback;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/11/15.
 */
public class ClientOperationsController extends ScreenController implements Initializable
{
    @FXML
    private TabPane clientsTabs;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //set [Clients->Job Log Sheet] Tab as selected tab by default
        if(clientsTabs.getTabs()!=null)
            if(clientsTabs.getTabs().size()>0)
                OperationsController.setSelectedTab(clientsTabs.getTabs().get(0));

        //change selected tab when tabs are changed
        clientsTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
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
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients tab view.");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading clients tab model.");
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

}
