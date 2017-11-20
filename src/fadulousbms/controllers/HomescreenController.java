/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.Screens;

import java.io.IOException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import java.net.URL;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.swing.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class HomescreenController extends ScreenController implements Initializable
{
    @FXML
    private Button btnCreateAccount;
    private ColorAdjust colorAdjust = new ColorAdjust();

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading homescreen view..");
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading homescreen data model..");
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    private Rectangle createTile()
    {
        Rectangle rectangle = new Rectangle(160, 100);
        Random rand = new Random();
        double r = rand.nextDouble();
        double g = rand.nextDouble();
        double b = rand.nextDouble();
        rectangle.setStroke(Color.WHITE);
        rectangle.setFill(new Color(r,g,b,0.5));

        return rectangle;
    }
    
    public void operationsClick()
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
                        if(screenManager.loadScreen(Screens.OPERATIONS.getScreen(),getClass().getResource("../views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
    }

    public void safetyClick()
    {
        if (SessionManager.getInstance().getActive() != null)
        {
            if (!SessionManager.getInstance().getActive().isExpired())
            {
                try
                {
                    ScreenManager.getInstance().loadScreen(Screens.SAFETY.getScreen(),
                            HomescreenController.class.getResource("../views/" + Screens.SAFETY.getScreen()));
                    ScreenManager.getInstance().setScreen(Screens.SAFETY.getScreen());
                } catch (IOException ex)
                {
                    Logger.getLogger(HomescreenController.class.getName()).log(Level.SEVERE, null, ex);
                }
            }else{
                JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }else{
            JOptionPane.showMessageDialog(null, "No active sessions!", "Session expired", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }
    
    public void operationsMouseEnter()
    {
        KeyValue start_kv = new KeyValue(colorAdjust.brightnessProperty(), 
                                         colorAdjust.brightnessProperty().getValue(), 
                                         Interpolator.LINEAR);
        KeyValue end_kv =   new KeyValue(colorAdjust.brightnessProperty(), 
                                         1, 
                                         Interpolator.LINEAR);
        
        KeyFrame start_frame = new KeyFrame(Duration.millis(0), start_kv);
        KeyFrame end_frame = new KeyFrame(  Duration.millis(100), end_kv);
        
        Timeline fadeInTimeline = new Timeline(start_frame, end_frame);
        
        fadeInTimeline.setCycleCount(1);
        fadeInTimeline.setAutoReverse(false);
        fadeInTimeline.play();
        //System.out.println("Operations enter");
    }
    
    public void operationsMouseLeave()
    {
        KeyValue start_kv = new KeyValue(colorAdjust.brightnessProperty(), 
                                         colorAdjust.brightnessProperty().getValue(), 
                                         Interpolator.LINEAR);
        KeyValue end_kv =   new KeyValue(colorAdjust.brightnessProperty(), 
                                         0, 
                                         Interpolator.LINEAR);
        
        KeyFrame start_frame = new KeyFrame(Duration.millis(0), start_kv);
        KeyFrame end_frame = new KeyFrame(Duration.millis(100), end_kv);
        
        Timeline fadeOutTimeline = new Timeline(start_frame, end_frame);
        
        fadeOutTimeline.setCycleCount(1);
        fadeOutTimeline.setAutoReverse(false);
        fadeOutTimeline.play();
    }

    @FXML
    public void hrClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.HR.getScreen(),getClass().getResource("../views/"+Screens.HR.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.HR.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load human resources screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void accountingClick()
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
                        if(screenManager.loadScreen(Screens.ACCOUNTING.getScreen(),getClass().getResource("../views/"+Screens.ACCOUNTING.getScreen())))
                        {
                            Platform.runLater(() ->
                                    screenManager.setScreen(Screens.ACCOUNTING.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load accounting screen.");
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
    public void facilitiesClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.FACILITIES.getScreen(),getClass().getResource("../views/"+Screens.FACILITIES.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.FACILITIES.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load facilities screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @FXML
    public void showSettings()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.SETTINGS.getScreen(),getClass().getResource("../views/"+Screens.SETTINGS.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.SETTINGS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load settings screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }
}
