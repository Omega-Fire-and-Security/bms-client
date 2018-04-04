/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Screens;

import java.io.File;
import java.io.IOException;

import fadulousbms.model.Task;
import fadulousbms.model.TimesheetActivity;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import java.net.URL;
import java.sql.Time;
import java.time.*;
import java.util.HashMap;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Callback;
import javafx.util.Duration;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import org.controlsfx.control.PopOver;

import javax.swing.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class HomescreenController extends ScreenController implements Initializable
{
    private ColorAdjust colorAdjust = new ColorAdjust();
    private int year = YearMonth.now().getYear(), week = 1, num_weeks_in_month = 4;
    private Month month = YearMonth.now().getMonth();

    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        ClientManager.getInstance().initialize();
        TimesheetManager.getInstance().initialize();
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading homescreen view..");
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading homescreen data model..");
        //execute callback
        if(callback!=null)
            callback.call(null);
    }
    /**
     * Initializes the controller class.
     */

    @Override
    public void forceSynchronise()
    {
        refreshModel(param ->
        {
            Platform.runLater(() -> refreshView());
            return null;
        });

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
                        if(screenManager.loadScreen(Screens.OPERATIONS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
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

    public void agendaClick()
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
                        if(screenManager.loadScreen(Screens.AGENDA.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.AGENDA.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.AGENDA.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load agenda screen.");
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
    public void showTimesheet()
    {
        File fCss = new File(IO.STYLES_ROOT_PATH + "home.css");

        final int num_days_in_week = DayOfWeek.values().length;
        num_weeks_in_month = YearMonth.of(year, month).lengthOfMonth() / num_days_in_week;

        GridPane grdDays = new GridPane();
        grdDays.setVgap(10);
        grdDays.setHgap(10);

        HBox hbx_toolbar = new HBox();

        ComboBox cbx_year = new ComboBox();
        ComboBox<Month> cbx_month = new ComboBox();
        ComboBox cbx_week = new ComboBox();

        Button btnNewActivity = new Button("New Activity");
        btnNewActivity.getStylesheets().add("file:///" + fCss.getAbsolutePath().replace("\\", "/"));
        btnNewActivity.getStyleClass().add("btnAdd");
        btnNewActivity.setMinWidth(100);
        btnNewActivity.setMinHeight(35);
        HBox.setHgrow(btnNewActivity, Priority.ALWAYS);

        hbx_toolbar.getChildren().addAll(new Label("Year: "), cbx_year,
                                         new Label("Month: "), cbx_month,
                                         new Label("Week: "), cbx_week, btnNewActivity);
        hbx_toolbar.setSpacing(40);

        //create array of years between now and the past 200 years
        int[] years = new int[200];
        for(int i = 0; i < years.length; i++)
        {
            years[i] = year - i;
            cbx_year.getItems().add(years[i]);
        }

        //create array of weeks in a month, i.e. 0-4
        int[] weeks = new int[num_weeks_in_month];
        for(int i=0; i<weeks.length; i++)
        {
            weeks[i] = i + 1;
            cbx_week.getItems().add(weeks[i]);
        }

        cbx_month.setItems(FXCollections.observableArrayList(Month.values()));

        cbx_year.valueProperty().addListener((observable, oldValue, newValue) ->
                                                              year = (int) newValue);
        cbx_month.valueProperty().addListener((observable, oldValue, newValue) ->
                                                               month = newValue);

        cbx_week.valueProperty().addListener(new ChangeListener()
        {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue)
            {
                week = (int) newValue +1;
                //render week Activities
                //TODO: TimesheetManager.getInstance().getActivitiesForWeek(year, month.getValue(), week);
                if(TimesheetManager.getInstance().getDataset()!=null)
                {
                    //tblDays.setItems(FXCollections.observableArrayList(TimesheetManager.getInstance().getDataset().values()));
                    //tblDays.refresh();
                    IO.log(getClass().getName(), IO.TAG_VERBOSE, "updated timesheet activities table");
                } else IO.logAndAlert("Warning", "No timesheet activities were found in the database", IO.TAG_WARN);
            }
        });

        btnNewActivity.setOnMouseClicked(event -> TimesheetManager.getInstance().newActivityPopOver(ScreenManager.getInstance(), activity_id->
        {
            //TODO: add activity to table
            if(activity_id==null)
                IO.logAndAlert("Error", "Could not create activity", IO.TAG_ERROR);
            else {
                if(TimesheetManager.getInstance().getSelected()!=null)
                    IO.logAndAlert("Success", "Successfully created activity ["+TimesheetManager.getInstance().getSelected().getDescription()+"]", IO.TAG_INFO);
                else IO.logAndAlert("Success", "Successfully created activity ["+activity_id+"]", IO.TAG_INFO);
            }
            return null;
        }));

        int row = 0;

        // render labels for week
        for(int week_day = 1; week_day <= num_days_in_week; week_day++)
        {
            Label lbl_day = new Label(DayOfWeek.of(week_day).toString());
            grdDays.add(lbl_day, week_day, row);
        }

        int end_of_week = week * num_days_in_week;
        int first_day_of_week = end_of_week - num_days_in_week + 1;

        // label times for the week
        for(int hour = 7; hour<18; hour++)
        {
            // go to next row
            row++;
            // get a string representation of the current hour
            String str_current_hour = (hour>9?hour:"0"+hour) + ":00 - " + (hour+1>9?hour+1:"0"+(hour+1)) + ":00";
            // add string representation of the current hour (as a Label) to the GridPane
            grdDays.add(new Label(str_current_hour), 0, row);

            // render GridPane cells
            for(int day_of_week = 1; day_of_week <= num_days_in_week; day_of_week++)
            {
                System.out.println("\n");
                IO.log(getClass().getName(), IO.TAG_VERBOSE, "processing day ["+DayOfWeek.of(day_of_week)+", "+year+"-"+month.getValue()+"-"+(first_day_of_week+day_of_week)+"] hour ["+str_current_hour+"]");

                LocalDate current_day = LocalDate.of(year, month, first_day_of_week + day_of_week);
                ZonedDateTime current_hour = current_day.atStartOfDay(ZoneId.systemDefault()).plusHours(hour);

                HashMap<Long, TimesheetActivity> user_activities = null;// all signed-in user's activities
                if(TimesheetManager.getInstance().getDataset() != null)
                    user_activities = (HashMap<Long, TimesheetActivity>) TimesheetManager.getInstance().getDataset().get(SessionManager.getInstance().getActive().getUsr());

                // get all signed-in user's Activities for the current hour
                HashMap<Long, TimesheetActivity> user_activities_current_hour =
                        TimesheetManager.getInstance().getTimesheet_activities_between(user_activities,
                                                                                       current_hour.toEpochSecond(),
                                                                                       current_hour.plusHours(1).toEpochSecond());

                // render Activit(y/ies)
                Node cell_node = null;//Node component for current GridPane cell
                if(user_activities_current_hour!=null)
                {
                    // activities exist for current hour
                    if(user_activities_current_hour.size()==1)
                    {
                        // only one Activity found
                        // render TextField & Button?
                        Long activity_current_hour_key = (Long) user_activities_current_hour.keySet().toArray()[0];
                        TimesheetActivity activity_current_hour = user_activities_current_hour.get(activity_current_hour_key);//forEach(key->System.out.println("##########["+dow.toString()+"]: " + user_activities_for_current_hour.get(key)));

                        IO.log(getClass().getName(), IO.TAG_VERBOSE, "found activity executed at [" + LocalDateTime.ofInstant(Instant.ofEpochSecond(activity_current_hour_key), ZoneId.systemDefault()) + "] >> [" + activity_current_hour.getDescription() + "]");
                        // add TextField associated with current Activity on GridPane
                        cell_node = new TextField(activity_current_hour.getDescription());
                    } else if(user_activities_current_hour.size() > 1)
                    {
                        //TODO: more than one Activity, combo box?
                        cell_node = new Label("N/A");
                    } else // no activities for this hour, TextField & button?
                    {
                        IO.log(getClass().getName(), IO.TAG_WARN, "no activities were found on ["+DayOfWeek.of(day_of_week)+", "+year+"-"+month.getValue()+"-"+(first_day_of_week+day_of_week)+"] hour ["+str_current_hour+"]");
                        cell_node = new Label("No Activities");//TODO: Node must contain button to create new Activity or TextField with onEnter()
                    }
                } else // no Activities for this hour, TextField & button?
                {
                    IO.log(getClass().getName(), IO.TAG_WARN, "no activities were found on ["+DayOfWeek.of(day_of_week)+", "+year+"-"+month.getValue()+"-"+(first_day_of_week+day_of_week)+"] hour ["+str_current_hour+"]");
                    cell_node = new Label("NuLL");//TODO: Node must contain button to create new Activity or TextField with onEnter()
                }
                // add Node associated with current Activity on to GridPane
                grdDays.add(cell_node, day_of_week, row);
            }
        }

        VBox vbox = new VBox(hbx_toolbar, grdDays);
        vbox.setSpacing(20);

        PopOver popOver = new PopOver(vbox);
        popOver.setTitle("Timesheets");
        popOver.setDetached(true);
        popOver.show(ScreenManager.getInstance());
    }

    @FXML
    public void hrClick()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.HR.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.HR.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.HR.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load human resources screen.");
        } catch (IOException e)
        {
            e.printStackTrace();
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
                        if(screenManager.loadScreen(Screens.ACCOUNTING.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.ACCOUNTING.getScreen())))
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
    public void showSettings()
    {
        try
        {
            if(ScreenManager.getInstance().loadScreen(Screens.SETTINGS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.SETTINGS.getScreen())))
                ScreenManager.getInstance().setScreen(Screens.SETTINGS.getScreen());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load settings screen.");
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}
