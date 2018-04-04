package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.ApplicationObject;
import fadulousbms.model.Client;
import fadulousbms.model.TimesheetActivity;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import jfxtras.scene.control.CalendarPicker;
import jfxtras.scene.control.CalendarTextField;
import jfxtras.scene.layout.CircularPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by th3gh0st on 2018/03/29.
 * @author th3gh0st
 */
public class TimesheetManager extends ApplicationObjectManager
{
    private HashMap<String, HashMap<Long, TimesheetActivity>> timesheet_activities;
    private Gson gson;
    private static TimesheetManager timesheet_manager = new TimesheetManager();
    public static final String TAG = "TimesheetManager";
    public static final String ROOT_PATH = "cache/timesheet/";
    public String filename = "";
    private long timestamp;

    private TimesheetManager()
    {
    }

    public static TimesheetManager getInstance()
    {
        return timesheet_manager;
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    /**
     * Method to get a map of all TimesheetActivities in the database.
     * @return
     */
    @Override
    public HashMap getDataset()
    {
        return this.timesheet_activities;
    }

    public HashMap<String, TimesheetActivity> getActivitiesForWeek(int year, int month, int week)
    {
        throw new NotImplementedException();
    }

    @Override
    public TimesheetActivity getSelected()
    {
        return (TimesheetActivity) super.getSelected();
    }

    @Override
    Callback getSynchronisationCallback()
    {
        return new Callback()
        {
            @Override
            public Object call(Object param)
            {
                try
                {
                    SessionManager smgr = SessionManager.getInstance();
                    if(smgr.getActive()!=null)
                    {
                        if(!smgr.getActive().isExpired())
                        {
                            gson = new GsonBuilder().create();
                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms.get("/timestamp/timesheet_activities_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "timesheet_activities_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            } else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                //Load TimesheetActivity objects from server
                                String timesheet_activities_json = RemoteComms.get("/timesheet/activities", headers);
                                TimesheetActivityServerResponseObject timesheet_activityServerObject = (TimesheetActivityServerResponseObject) TimesheetManager.getInstance().parseJSONobject(timesheet_activities_json, new TimesheetActivityServerResponseObject());
                                if (timesheet_activityServerObject != null)
                                {
                                    if (timesheet_activityServerObject.get_embedded() != null)
                                    {
                                        TimesheetActivity[] timesheet_activities_arr = timesheet_activityServerObject.get_embedded().getTimesheetActivities();

                                        timesheet_activities = new HashMap<>();
                                        for (TimesheetActivity timesheet_activity : timesheet_activities_arr)
                                        {
                                            HashMap<Long, TimesheetActivity> user_activity_bucket = timesheet_activities.get(timesheet_activity.getCreator());
                                            if(user_activity_bucket==null)//if user activity bucket is empty
                                            {
                                                user_activity_bucket = new HashMap<>();//initialise it
                                                //add activity to bucket
                                                user_activity_bucket.put(timesheet_activity.getDate_executed(), timesheet_activity);//use execution date as identifier
                                                //assign bucket to user
                                                timesheet_activities.putIfAbsent(timesheet_activity.getCreator(), user_activity_bucket);
                                            } else // just add activity to bucket, use execution date as identifier
                                                timesheet_activities.get(timesheet_activity.getCreator()).put(timesheet_activity.getDate_executed(), timesheet_activity);
                                            //Instant.ofEpochSecond(timesheet_activity.getDate_executed()).atZone(ZoneId.systemDefault())
                                        }
                                        //TODO: use username.activity_id as identifier for above
                                        IO.log(getClass().getName(), IO.TAG_VERBOSE, "reloaded timesheet_activities.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any TimesheetActivities in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any TimesheetActivities in the database.");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of timesheet_activities.");

                                serialize(ROOT_PATH + filename, timesheet_activities);

                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                timesheet_activities = (HashMap<String, HashMap<Long, TimesheetActivity>>) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "No valid active sessions found.", IO.TAG_ERROR);
                } catch (MalformedURLException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                } catch (ClassNotFoundException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                } catch (IOException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    /**
     * Method to look for TimesheetActivities between a given range
     * @param activities HashMap to search through
     * @param start_sec Epoch seconds to start searching from
     * @param end_sec Epoch seconds to stop searching
     * @return reduced HashMap of TimesheetActivities between the given range using timestamps as identifiers
     */
    public HashMap<Long, TimesheetActivity> getTimesheet_activities_between(HashMap<Long, TimesheetActivity> activities, long start_sec, long end_sec)
    {
        if(activities==null)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, "invalid list of activities");
            return null;
        }
        //
        HashMap<Long, TimesheetActivity> reduced_map = new HashMap<>();
        activities.forEach((date_exec, timesheetActivity) ->
                           {
                               if(date_exec>=start_sec && date_exec<=end_sec)
                                   reduced_map.putIfAbsent(date_exec, timesheetActivity);
                           });
        //TODO: consider activities.get(start_sec+current_sec) while(current_sec<=end_sec) and check if start_sec>=0 & end_sec>=0 && else loop breaks
        return reduced_map;
    }

    public void newActivityPopOver(Node parent, Callback callback)
    {
        setSelected(null);
        ClientManager.getInstance().setSelected(null);

        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

        final TextField txt_client_name = new TextField();
        txt_client_name.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        txt_client_name.setMinWidth(200);
        txt_client_name.setMaxWidth(Double.MAX_VALUE);
        //HBox client_name = CustomTableViewControls.getLabelledNode("Client Name", 200, txt_client_name);

        final TextArea txt_location = new TextArea();
        txt_location.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        txt_location.setMinWidth(200);
        txt_location.setMaxWidth(Double.MAX_VALUE);
        txt_location.setPrefHeight(70);
        //HBox physical_address = CustomTableViewControls.getLabelledNode("Physical Address", 200, txt_physical_address);

        final TextArea txt_description = new TextArea();
        txt_description.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        txt_description.setMinWidth(200);
        txt_description.setMaxWidth(Double.MAX_VALUE);
        txt_description.setPrefHeight(70);
        //HBox postal_address = CustomTableViewControls.getLabelledNode("Postal Address", 200, txt_postal_address);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        txt_other.setPrefHeight(70);
        //HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other);

        Button btnSubmit = new Button("Create Activity");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(45);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(new Label("Client Name: "), 0, 0);
        page.add(txt_client_name, 1, 0);

        page.add(new Label("Date Executed: "), 0, 1);

        CalendarPicker calendarPicker = new CalendarPicker();
        calendarPicker.setShowTime(true);
        calendarPicker.setAllowNull(false);
        page.add(calendarPicker, 1, 1);
        /*calendarTextField.setCalendarRangeCallback( (range) -> {
            calendarPicker.disabledCalenders().clear;
            calendarPicker.disabledCalenders.addAll( BusinessModel.findDisabledCalendarsIn(range.getStartCalendar(), range.getEndCalendar() );
        });*/

        page.add(new Label("Location"), 0, 2);
        page.add(txt_location, 1, 2);

        page.add(new Label("Activity Description"), 0, 3);
        page.add(txt_description, 1, 3);

        page.add(new Label("Other Info: "), 0, 4);
        page.add(txt_other, 1, 4);

        page.add(btnSubmit, 1, 5);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create Activity");
        popover.setDetached(true);
        popover.show(parent);

        TextFields.bindAutoCompletion(txt_client_name, ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
         {
             ClientManager.getInstance().setSelected(null);

             if(event!=null)
             {
                 if(event.getCompletion()!=null)
                 {
                     ClientManager.getInstance().setSelected(event.getCompletion());
                     txt_location.setText(ClientManager.getInstance().getSelected().getPhysical_address());
                 }
             }
         });

        btnSubmit.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                if(SessionManager.getInstance().getActive()==null)
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }
                if(SessionManager.getInstance().getActive().isExpired())
                {
                    IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    return;
                }

                String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

                if(!Validators.isValidNode(txt_client_name, txt_client_name.getText(), 1, ".+"))
                    return;
                /*if(!Validators.isValidNode(Time(), calendarPicker.getValue()==null?"":calendarPicker.getValue().toString(), 4, date_regex))
                    return;*/
                if(calendarPicker.getCalendar().getTimeInMillis()<=0)
                {
                    calendarPicker.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    calendarPicker.getStyleClass().remove("form-control-default");
                    calendarPicker.getStyleClass().add("control-input-error");
                }
                if(!Validators.isValidNode(txt_location, txt_location.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txt_description, txt_description.getText(), 1, ".+"))
                    return;

                //if txt_client_name  is not empty and selected_client is not set ask if they want to create a new client
                String proceed = IO.NO;
                if(ClientManager.getInstance().getSelected()==null)
                    proceed = IO.showConfirm("Client ["+txt_client_name.getText()+"] not found?", "Client with the name ["
                            +txt_client_name.getText()+"] was not found in the database, create new client?");

                //did they choose to continue with the creation or cancel?
                if(proceed.toLowerCase().equals(IO.OK.toLowerCase()))
                {
                    // show client creation window
                    ClientManager.getInstance().newClientPopOver(ScreenManager.getInstance(), param ->
                    {
                        if(param!=null)
                        {
                            //update autocompletion data after creating a new client
                            TextFields.bindAutoCompletion(txt_client_name, ClientManager.getInstance().getDataset().values()).setOnAutoCompleted(
                                event1 ->
                                {
                                    if (event1 != null)
                                        if (event1.getCompletion() != null)
                                            setSelected(event1.getCompletion());
                                });
                        } else IO.log(getClass().getName(), IO.TAG_WARN, "did NOT create new client.");
                        return null;
                    });
                    return;
                } else if(ClientManager.getInstance().getSelected()==null)
                {
                    //show error on client text box
                    calendarPicker.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    txt_client_name.getStyleClass().remove("form-control-default");
                    txt_client_name.getStyleClass().add("control-input-error");
                    IO.logAndAlert("Warning", "Unknown client ["+txt_client_name.getText()+"]", IO.TAG_WARN);
                    return;
                }

                //valid input, create new activity
                long date_partnered_in_sec = calendarPicker.getCalendar().getTimeInMillis()/1000;//dpk_date_executed.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

                TimesheetActivity activity = new TimesheetActivity();
                activity.setClient_id(ClientManager.getInstance().getSelected().get_id());
                activity.setLocation(txt_location.getText());
                activity.setDescription(txt_description.getText());
                activity.setDate_executed(date_partnered_in_sec);
                activity.setCreator(SessionManager.getInstance().getActive().getUsr());
                activity.setStatus(TimesheetActivity.STATUS_COMPLETED);
                if(txt_other.getText()!=null)
                    activity.setOther(txt_other.getText());

                try
                {
                    TimesheetManager.getInstance().putObject(activity, new_activity_id ->
                    {
                        if(new_activity_id!=null)
                        {
                            setSelected((ApplicationObject) TimesheetManager.getInstance().getDataset().get(new_activity_id));
                            //execute callback w/ args
                            if(callback!=null)
                                callback.call(new_activity_id);
                            return null;
                        } else IO.logAndAlert("Error", "Could not create new timesheet activity ["+txt_description.getText()+"]", IO.TAG_ERROR);
                        //execute callback w/o args
                        if(callback!=null)
                            callback.call(null);
                        return null;
                    });
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
        });
    }

    class TimesheetActivityServerResponseObject extends ServerResponseObject
    {
        private Embedded _embedded;

        Embedded get_embedded()
        {
            return _embedded;
        }

        void set_embedded(Embedded _embedded)
        {
            this._embedded = _embedded;
        }

        class Embedded
        {
            private TimesheetActivity[] timesheet_activities;

            public TimesheetActivity[] getTimesheetActivities()
            {
                return timesheet_activities;
            }

            public void setTimesheetActivities(TimesheetActivity[] timesheet_activities)
            {
                this.timesheet_activities = timesheet_activities;
            }
        }
    }
}