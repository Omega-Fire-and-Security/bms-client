package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.image.ImageView;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Employee;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Window;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class NavController extends ScreenController implements Initializable
{
    @FXML
    private Label lblScreen,company_name;
    @FXML
    private ImageView btnBack,btnNext,btnHome,img_logo;
    public static int FONT_SIZE_MULTIPLIER = 40;
    public static boolean first_run = true;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        if(ScreenManager.getInstance()!=null)
            ScreenManager.getInstance().setLblScreenName(lblScreen);
        refreshView();
    }

    @Override
    public void refreshView()
    {
        /*try
        {
            //Render default profile image
            Image image = SwingFXUtils.toFXImage(ImageIO.read(new File("images/profile.png")), null);
            super.getProfileImageView().setImage(image);

            //Render logo
            image = SwingFXUtils.toFXImage(ImageIO.read(new File("images/logo.png")), null);
            img_logo.setImage(image);

            //Render forward nav icon
            /*image = SwingFXUtils.toFXImage(ImageIO.read(new File("images/chevron_right_black.png")), null);
            btnNext.setImage(image);

            //Render previous nav icon
            image = SwingFXUtils.toFXImage(ImageIO.read(new File("images/chevron_left_black.png")), null);
            btnBack.setImage(image);

            //Render home nav icon
            image = SwingFXUtils.toFXImage(ImageIO.read(new File("images/home_black.png")), null);
            btnHome.setImage(image);*
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, "Could not load default profile image.");
        }*/

        if(!first_run)
        {
            if (SessionManager.getInstance().getActive() != null)
            {
                if (!SessionManager.getInstance().getActive().isExpired())
                {
                    //Render user name
                    Employee e = SessionManager.getInstance().getActiveEmployee();
                    if (e != null)
                        this.getUserNameLabel().setText(e.getName());
                    else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");
                }
                else
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions were found!");
                    return;
                }
            }
            else
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, "No valid sessions were found!");
                return;
            }
            //Render current screen name
            lblScreen.setText(ScreenManager.getInstance().peekScreenControllers().getKey());

            //Render company name
            company_name.setText(Globals.APP_NAME.getValue());
        } else
        {
            IO.log(getClass().getName(), IO.TAG_INFO, "first run, ignoring session checks.");
            first_run = false;
        }

        if(ScreenManager.getInstance().getScene()!=null)
        {
            Window app_window = ScreenManager.getInstance().getScene().getWindow();
            //resize app title on screen resize
            app_window.widthProperty().addListener((observable, oldValue, newValue) ->
                    company_name.setFont(Font.font(newValue.intValue() / FONT_SIZE_MULTIPLIER)));

            //trigger resize handler to resize font every time the nav is reloaded, yes it is hacky but works ;)
            app_window.setWidth(
                    app_window.getWidth() + 1 >= GraphicsEnvironment.getLocalGraphicsEnvironment()
                            .getDefaultScreenDevice().getDisplayMode().getWidth() - 60 ?
                            app_window.getWidth() - 1 : app_window.getWidth() + 1);
        }
    }

    @Override
    public void refreshModel(Callback callback)
    {
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        if(ScreenManager.getInstance().getFocused()!=null)
        {
            ScreenManager.getInstance().getFocused().forceSynchronise();
            Platform.runLater(() -> ScreenManager.getInstance().getFocused().refreshView());
        } else IO.log(getClass().getName(), IO.TAG_WARN, "no screen is focused, not refreshing.");
    }

    @FXML
    public void showContextMenu()
    {
        ScreenManager.getInstance().showContextMenu();
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void login()
    {
        ScreenController.showLogin();
    }

    @FXML
    public void home()
    {
        ScreenController.showMain();
    }

    @FXML
    public void back()
    {
        ScreenController.previousScreen();
    }
}
