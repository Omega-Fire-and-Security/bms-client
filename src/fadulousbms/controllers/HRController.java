package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import jfxtras.labs.scene.control.radialmenu.RadialContainerMenuItem;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/02/02.
 */
public class HRController extends ScreenController implements Initializable
{
    @FXML
    private TabPane hrTabs;
    private static Tab selected_tab;

    @Override
    public void refreshView()
    {
    }

    @Override
    public void refreshModel()
    {
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //set default selected tab, HR->Employees
        if(hrTabs.getTabs()!=null)
            if(hrTabs.getTabs().size()>0)
                selected_tab = hrTabs.getTabs().get(0);

        //change selected tab when tabs are changed
        hrTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            //TODO: choose first sub tab of selected tab
            HRController.setSelectedTab(newValue);
            IO.log(getClass().getName(), IO.TAG_INFO, "selected tab: " + newValue.getText());
            /*if(!newValue.getId().toLowerCase().equals("clientsTab") && !newValue.getId().toLowerCase().equals("suppliersTab"))
            {
                OperationsController.setSelectedTab(newValue);
                IO.log(getClass().getName(), IO.TAG_INFO, "selected tab: " + newValue.getText());
            }*/
        });
    }

    public static void setSelectedTab(Tab tab)
    {
        selected_tab=tab;
    }

    public RadialMenuItem[] getContextMenu()
    {
        //Copy default context menu
        RadialMenuItem[] default_context_menu = ScreenController.getDefaultContextMenu();
        RadialMenuItem[] context_menu = new RadialMenuItem[default_context_menu.length+4];
        System.arraycopy(default_context_menu, 0,  context_menu, 0, default_context_menu.length);

        //append to default context menu
        //new employee menu item
        context_menu[default_context_menu.length] = new RadialMenuItemCustom(30, "New Employee", null,
                null, event -> EmployeeManager.getInstance().newExternalEmployeeWindow("Create New Employee",null));
        //record overtime menu item
        context_menu[default_context_menu.length+1] = new RadialMenuItemCustom(30, "Overtime Application", null, null, event ->
        {
            if(SessionManager.getInstance().getActiveEmployee()!=null)
                OvertimeManager.getInstance().newOvertimeApplicationWindow(SessionManager.getInstance().getActiveEmployee(), null, null);
        });
        //record leave application menu item
        context_menu[default_context_menu.length+2] = new RadialMenuItemCustom(30, "Leave Application", null, null, event ->
        {
            if(SessionManager.getInstance().getActiveEmployee()!=null)
                LeaveManager.getInstance().newLeaveApplicationWindow(SessionManager.getInstance().getActiveEmployee(), null);
        });
        context_menu[default_context_menu.length+3] = new RadialContainerMenuItem(30, "Selected", null);

        //Get context menu for selected tab
        if(selected_tab!=null)
        {
            switch (selected_tab.getId())
            {
                case OvertimeTabController.TAB_ID:
                    IO.log(getClass().getName(), IO.TAG_INFO, "showing overtime context menu.");
                    if(OvertimeTabController.getContextMenu()!=null)
                    {
                        //add level 2 context menu items
                        for (RadialMenuItem menuItem: OvertimeTabController.getContextMenu())
                            ((RadialContainerMenuItem)context_menu[default_context_menu.length+3]).addMenuItem(menuItem);
                    } else IO.log(getClass().getName(), IO.TAG_WARN, OvertimeTabController.class.getName() + ": has no explicitly defined context menu.");
                    break;
                case LeaveTabController.TAB_ID:
                    IO.log(getClass().getName(), IO.TAG_INFO, "showing leave context menu.");
                    if(LeaveTabController.getContextMenu()!=null)
                    {
                        //add level 2 context menu items
                        for (RadialMenuItem menuItem: LeaveTabController.getContextMenu())
                            ((RadialContainerMenuItem)context_menu[default_context_menu.length+3]).addMenuItem(menuItem);
                    } else IO.log(getClass().getName(), IO.TAG_WARN, LeaveTabController.class.getName() + ": has no explicitly defined context menu.");
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_WARN, "unknown selected HR tab: " + selected_tab.getId());
            }

        } else IO.log(getClass().getName(), IO.TAG_ERROR, "selected tab is null");
        return context_menu;
    }

    @FXML
    public void overtimeApplication()
    {
        if(SessionManager.getInstance().getActive()!=null)
            if(!SessionManager.getInstance().getActive().isExpired())
                if(SessionManager.getInstance().getActiveEmployee()!=null)
                    OvertimeManager.getInstance().newOvertimeApplicationWindow(SessionManager.getInstance().getActiveEmployee(), null, null);
                else IO.logAndAlert("Error", "Active Employee is invalid.", IO.TAG_ERROR);
            else IO.logAndAlert("Error", "Active session has expired.", IO.TAG_ERROR);
        else IO.logAndAlert("Error", "Active session is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void leaveApplication()
    {
        if(SessionManager.getInstance().getActive()!=null)
            if(!SessionManager.getInstance().getActive().isExpired())
                if(SessionManager.getInstance().getActiveEmployee()!=null)
                    LeaveManager.getInstance().newLeaveApplicationWindow(SessionManager.getInstance().getActiveEmployee(), null);
                else IO.logAndAlert("Error", "Active Employee is invalid.", IO.TAG_ERROR);
            else IO.logAndAlert("Error", "Active session has expired.", IO.TAG_ERROR);
        else IO.logAndAlert("Error", "Active session is invalid.", IO.TAG_ERROR);
    }
}
