package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.util.Callback;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 */
public class TaskManager extends BusinessObjectManager
{
    private HashMap<String, Task> tasks;
    private Gson gson;
    private static TaskManager task_manager = new TaskManager();
    public static final String TAG = "TaskManager";
    public static final String ROOT_PATH = "cache/tasks/";
    public String filename = "";
    private long timestamp;

    private TaskManager()
    {
    }

    public static TaskManager getInstance()
    {
        return task_manager;
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    /**
     * Method to get a map of all Tasks in the database.
     * @return
     */
    @Override
    public HashMap<String, Task> getDataset()
    {
        return this.tasks;
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
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                            //Get Timestamp
                            String timestamp_json = RemoteComms.sendGetRequest("/timestamp/tasks_timestamp", headers);
                            Counters cntr_timestamp = gson.fromJson(timestamp_json, Counters.class);
                            if (cntr_timestamp != null)
                            {
                                timestamp = cntr_timestamp.getCount();
                                filename = "tasks_" + timestamp + ".dat";
                                IO.log(this.getClass().getName(), IO.TAG_INFO, "Server Timestamp: " + timestamp);
                            }
                            else
                            {
                                IO.log(this.getClass().getName(), IO.TAG_WARN, "could not get valid timestamp");
                                return null;
                            }

                            if (!isSerialized(ROOT_PATH + filename))
                            {
                                //Load Task objects from server
                                String tasks_json = RemoteComms.sendGetRequest("/tasks", headers);
                                TaskServerObject taskServerObject = gson.fromJson(tasks_json, TaskServerObject.class);
                                if (taskServerObject != null)
                                {
                                    if (taskServerObject.get_embedded() != null)
                                    {
                                        Task[] tasks_arr = taskServerObject.get_embedded().getTasks();

                                        tasks = new HashMap<>();
                                        for (Task task : tasks_arr)
                                            tasks.put(task.get_id(), task);
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Tasks in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Tasks in the database.");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of tasks.");
                                serialize(ROOT_PATH + filename, tasks);
                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                tasks = (HashMap<String, Task>) deserialize(ROOT_PATH + filename);
                            }
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "No valid active sessions found.", IO.TAG_ERROR);
                } catch (MalformedURLException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (ClassNotFoundException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
                return null;
            }
        };
    }

    /**
     * Method to create new Task object on the database server.
     * @param task Task object to be created.
     * @param callback Callback to be executed on if request was successful.
     * @return server response.
     */
    public String createNewTask(Task task, Callback callback)
    {
        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

            //create new task on database
            HttpURLConnection connection = RemoteComms.putJSON("/tasks", task.getJSONString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());

                    //server will return message object in format "<task_id>"
                    String new_task_id = response.replaceAll("\"","");//strip inverted commas around task_id
                    new_task_id = new_task_id.replaceAll("\n","");//strip new line chars
                    new_task_id = new_task_id.replaceAll(" ","");//strip whitespace chars

                    if(connection!=null)
                        connection.disconnect();

                    IO.logAndAlert("Success", "Successfully created a new task: " + new_task_id, IO.TAG_INFO);

                    TaskManager.getInstance().synchroniseDataset();
                    //execute callback w/ args
                    if(callback!=null)
                        callback.call(new_task_id);
                    return new_task_id;
                } else
                {
                    //Get error message
                    if(connection!=null)
                    {
                        connection.disconnect();
                        String msg = IO.readStream(connection.getErrorStream());
                        IO.logAndAlert("Error " + String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                    }
                    //execute callback w/o args
                    if(callback!=null)
                        callback.call(null);
                    return null;
                }
            } else IO.logAndAlert("Task Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    class TaskServerObject extends ServerObject
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
            private Task[] tasks;

            public Task[] getTasks()
            {
                return tasks;
            }

            public void setTasks(Task[] tasks)
            {
                this.tasks = tasks;
            }
        }
    }
}