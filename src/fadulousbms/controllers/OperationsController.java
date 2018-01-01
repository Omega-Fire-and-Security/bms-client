/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import jfxtras.labs.scene.control.radialmenu.RadialContainerMenuItem;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

/**
 * views Controller class
 *
 * @author ghost
 */
public class OperationsController extends ScreenController implements Initializable
{
    @FXML
    private TabPane BMSTabs;
    private static Tab selected_tab;
    public static final String TAG="OperationsController";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        //set default selected tab, Clients->Job Log Sheet
        //if(BMSTabs.getTabs()!=null)
        //    if(BMSTabs.getTabs().size()>0)
        //        selected_tab = BMSTabs.getTabs().get(0);

        //change selected tab when tabs are changed
        BMSTabs.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) ->
        {
            //TODO: choose first sub tab of selected tab
            OperationsController.setSelectedTab(newValue);
            IO.log(getClass().getName(), IO.TAG_INFO, "selected tab: " + newValue.getText());
            /*if(!newValue.getId().toLowerCase().equals("clientsTab") && !newValue.getId().toLowerCase().equals("suppliersTab"))
            {
                OperationsController.setSelectedTab(newValue);
                IO.log(getClass().getName(), IO.TAG_INFO, "selected tab: " + newValue.getText());
            }*/
        });
    }

    @Override
    public void refreshView()
    {
        /*Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.toString());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");*/
        //System.out.println("Focused: "+ScreenManager.getInstance().getFocused_id());
    }

    @Override
    public void refreshModel()
    {
        //ClientManager.getInstance().initialize();
        //clientsController.refreshModel();
        //clientsController.refreshView();
    }

    public RadialMenuItem[] getContextMenu()
    {
        RadialMenuItem[] default_context_menu = ScreenController.getDefaultContextMenu();
        RadialMenuItem[] context_menu = new RadialMenuItem[default_context_menu.length+5];
        System.arraycopy(default_context_menu, 0,  context_menu, 0, default_context_menu.length);

        //append to default context menu
        context_menu[default_context_menu.length] = new RadialMenuItemCustom(30, "New Client", null, null, event -> newClientClick());
        context_menu[default_context_menu.length+1] = new RadialMenuItemCustom(30, "New Supplier", null, null, event -> newSupplierClick());
        context_menu[default_context_menu.length+2] = new RadialMenuItemCustom(30, "New Material", null, null, event -> newMaterialClick());
        context_menu[default_context_menu.length+3] = new RadialMenuItemCustom(30, "New Quote", null, null, event -> newQuoteClick());
        context_menu[default_context_menu.length+4] = new RadialContainerMenuItem(30, "Selected", null);

        //level 2 menu items
        //((RadialContainerMenuItem)context_menu[default_context_menu.length+4]).addMenuItem(context_menu[0]);
        //RadialMenuItem[] context_menu_lvl2;
        if(selected_tab!=null)
        {
            switch (selected_tab.getId())
            {
                case JobsController.TAB_ID:
                    IO.log(OperationsController.class.getName(), IO.TAG_INFO, "showing jobs context menu.");
                    if(JobsController.getContextMenu()!=null)
                    {
                        //add level 2 context menu items
                        for (RadialMenuItem menuItem: JobsController.getContextMenu())
                            ((RadialContainerMenuItem)context_menu[default_context_menu.length+4]).addMenuItem(menuItem);
                        //context_menu_lvl2 = new RadialMenuItem[JobsController.getContextMenu().length];
                        //System.arraycopy(JobsController.getContextMenu(), 0, context_menu_lvl2, 0, JobsController.getContextMenu().length);
                    } else IO.log(OperationsController.class.getName(), IO.TAG_WARN, JobsController.class.getName() + ": has no explicitly defined context menu.");
                    break;
                default:
                    IO.log(OperationsController.class.getName(), IO.TAG_WARN, "unknown selected operations tab: " + selected_tab.getId());
            }

        } else IO.log(OperationsController.class.getName(), IO.TAG_ERROR, "selected tab is null");
        return context_menu;
    }

    public static void setSelectedTab(Tab tab)
    {
        selected_tab=tab;
    }

    @FXML
    public void newClient()
    {
        newClientClick();
    }

    public static void newClientClick()
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
                        if(screenManager.loadScreen(Screens.NEW_CLIENT.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_CLIENT.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_CLIENT.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load client creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newSupplier()
    {
        newSupplierClick();
    }

    public static void newSupplierClick()
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
                        if(screenManager.loadScreen(Screens.NEW_SUPPLIER.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_SUPPLIER.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_SUPPLIER.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load supplier creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newMaterial()
    {
        newMaterialClick();
    }

    public static void newMaterialClick()
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
                        if(screenManager.loadScreen(Screens.NEW_RESOURCE.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_RESOURCE.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_RESOURCE.getScreen());
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

    @FXML
    public void newQuote()
    {
        newQuoteClick();
    }

    public static void newQuoteClick()
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
                        if(screenManager.loadScreen(Screens.NEW_QUOTE.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_QUOTE.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_QUOTE.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load quote creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void newRequisitionClick()
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
                        if(screenManager.loadScreen(Screens.NEW_REQUISITION.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_REQUISITION.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.NEW_REQUISITION.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load requisition creation screen.");
                    } catch (IOException e)
                    {
                        e.printStackTrace();
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    public void createPurchaseOrderClick()
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
                        if(screenManager.loadScreen(Screens.NEW_PURCHASE_ORDER.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_PURCHASE_ORDER.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.NEW_PURCHASE_ORDER.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load purchase order creation screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    //Production event handlers
    @FXML
    public void suppliersClick()
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
                        if(screenManager.loadScreen(Screens.SUPPLIERS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.SUPPLIERS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.SUPPLIERS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load suppliers screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void jobsClick()
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
                        if(screenManager.loadScreen(Screens.JOBS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.JOBS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.JOBS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    @FXML
    public void clientsClick()
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
                        if(screenManager.loadScreen(Screens.CLIENTS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.CLIENTS.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.CLIENTS.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load clients screen.");
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
