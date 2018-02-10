/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SupplierManager;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 *
 * @author ghost
 */
public abstract class ScreenController
{
    @FXML
    private ImageView img_profile;
    @FXML
    private Label user_name;
    public static BufferedImage defaultProfileImage;
    @FXML
    private Circle shpServerStatus;
    @FXML
    private Label lblOutput;
    @FXML
    private BorderPane loading_pane;

    public ScreenController()
    {
    }

    public abstract void refreshView();

    public abstract void refreshModel();

    public void refreshStatusBar(String msg)
    {
        try
        {
            boolean ping = RemoteComms.pingServer();
            Platform.runLater(() ->
            {
                shpServerStatus.setStroke(Color.TRANSPARENT);
                if(ping)
                    shpServerStatus.setFill(Color.LIME);
                else shpServerStatus.setFill(Color.RED);
                lblOutput.setText(msg);
            });
        } catch (IOException e)
        {
            if(Globals.DEBUG_ERRORS.getValue().equalsIgnoreCase("on"))
                System.out.println(getClass().getName() + ">" + IO.TAG_ERROR + ">" + "could not refresh status bar: "+e.getMessage());
            Platform.runLater(() ->
            {
                shpServerStatus.setFill(Color.RED);
                lblOutput.setText(msg);
            });
        }
    }

    @FXML
    public void forceSynchronise()
    {
        refreshModel();
        refreshView();
    }

    @FXML
    public static void showLogin()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.LOGIN.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.LOGIN.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.LOGIN.getScreen());
            else IO.log("ScreenController", IO.TAG_ERROR, "could not load login screen.");
        } catch (IOException e)
        {
            IO.log("ScreenController", IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public static void showMain()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.HOME.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.HOME.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.HOME.getScreen());
            else IO.log("ScreenController", IO.TAG_ERROR, "could not load home screen.");
        } catch (IOException e)
        {
            IO.log("ScreenController", IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public static void previousScreen()
    {
        try
        {
            ScreenManager.getInstance().setPreviousScreen();
        } catch (IOException e)
        {
            IO.log("ScreenController", IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void createAccount()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.CREATE_ACCOUNT.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.CREATE_ACCOUNT.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.CREATE_ACCOUNT.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load account creation screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void newQuote()
    {
        QuoteManager.getInstance().setSelected(null);
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.NEW_QUOTE.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.NEW_QUOTE.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.NEW_QUOTE.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load new quotes screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void comingSoon()
    {
        IO.logAndAlert("Coming Soon", "This feature is currently being implemented.", IO.TAG_INFO);
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        RadialMenuItem menuClose = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Close", null, null, event -> ScreenManager.getInstance().hideContextMenu());
        RadialMenuItem menuBack = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Back", null, null, event -> previousScreen());
        RadialMenuItem menuForward = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Forward", null, null, event -> showMain());
        RadialMenuItem menuHome = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Home", null, null, event -> showMain());
        RadialMenuItem menuLogin = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Login", null, null, event -> showLogin());
        RadialMenuItem pdf_parser = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "Parse Suppliers PDF", null, null, event -> SupplierManager
                .parsePDF());

        return new RadialMenuItem[]{menuClose, menuBack, menuForward, menuHome, menuLogin, pdf_parser};
    }

    //public abstract RadialMenuItem[] getContextMenu();

    public ImageView getProfileImageView()
    {
        return this.img_profile;
    }

    public Label getUserNameLabel()
    {
        return this.user_name;
    }

    public BorderPane getLoadingPane()
    {
        return this.loading_pane;
    }
}
