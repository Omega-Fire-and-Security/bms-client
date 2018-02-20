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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/11/15.
 */
public class ServiceController extends ScreenController implements Initializable
{
    @FXML
    private TextField txtServiceName, txtName, txtRate, txtUnit, txtQuantity;
    @FXML
    private TextArea txtServiceDescription, txtDescription;
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
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading services view.");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading services model's data-set.");

        ServiceManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        ServiceManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    @FXML
    public void createService()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Session Invalid", "No valid active sessions.\nPlease log in.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
            return;
        }
        if(txtServiceName.getText()==null)
        {
            IO.logAndAlert("Error", "Invalid service name.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        if(txtServiceName.getText().isEmpty())
        {
            IO.logAndAlert("Error", "Invalid service name.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }

        Service service = new Service();
        service.setService_title(txtServiceName.getText());
        service.setCreator(SessionManager.getInstance().getActive().getUsr());
        if(txtServiceDescription.getText()!=null)
            service.setService_description(txtServiceDescription.getText());

        try
        {
            ServiceManager.getInstance().createService(service, null);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void createServiceItem()
    {
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Session Invalid", "No valid active sessions.\nPlease log in.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Session Expired", "Active session has expired.\nPlease log in.", IO.TAG_ERROR);
            return;
        }
        if(ServiceManager.getInstance().getSelected()==null)
        {
            IO.logAndAlert("Error", "Selected service is invalid.\nPlease select a valid service.", IO.TAG_ERROR);
            return;
        }
        if(txtName.getText()==null)
        {
            IO.logAndAlert("Error", "Invalid service item name.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        if(txtName.getText().isEmpty())
        {
            IO.logAndAlert("Error", "Invalid service item name.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        /*if(txtDescription.getText()==null)
        {
            IO.logAndAlert("Error", "Invalid service item description.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        if(txtDescription.getText().isEmpty())
        {
            IO.logAndAlert("Error", "Invalid service item description.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }*/

        if(txtUnit.getText()==null)
        {
            IO.logAndAlert("Error", "Invalid service item unit.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        if(txtUnit.getText().isEmpty())
        {
            IO.logAndAlert("Error", "Invalid service item unit.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }

        if(txtRate.getText()==null)
        {
            IO.logAndAlert("Error", "Invalid service item rate.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        if(txtRate.getText().isEmpty())
        {
            IO.logAndAlert("Error", "Invalid service item rate.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }

        if(txtQuantity.getText()==null)
        {
            IO.logAndAlert("Error", "Invalid service item quantity.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }
        if(txtQuantity.getText().isEmpty())
        {
            IO.logAndAlert("Error", "Invalid service item quantity.\nPlease enter a valid value.", IO.TAG_WARN);
            return;
        }

        ServiceItem service_item = new ServiceItem();
        service_item.setService_id(ServiceManager.getInstance().getSelected().get_id());
        service_item.setItem_name(txtName.getText());
        service_item.setItem_description(txtDescription.getText());
        service_item.setUnit(txtUnit.getText());
        service_item.setCreator(SessionManager.getInstance().getActive().getUsr());
        if(txtDescription.getText()!=null)
            service_item.setItem_description(txtDescription.getText());
        try
        {
            service_item.setItem_rate(Double.parseDouble(txtRate.getText()));
            service_item.setQuantity(Integer.parseInt(txtQuantity.getText()));
        } catch (NumberFormatException e)
        {
            IO.logAndAlert("Error",e.getMessage(), IO.TAG_ERROR);
            return;
        }

        try
        {
            ServiceManager.getInstance().createServiceItem(service_item, null);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
