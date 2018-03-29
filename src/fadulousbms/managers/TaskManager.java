package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.util.Callback;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 * @author ghost
 */
public class TaskManager extends BusinessObjectManager
{
    private HashMap<String, Task> tasks;
    private HashMap<String, HashMap<String, TaskItem>> task_items;
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

    public HashMap<String, TaskItem> getTaskItems(String task_id)
    {
        if(task_id!=null && task_items!=null)
            return task_items.get(task_id);
        else return null;
    }

    @Override
    public Task getSelected()
    {
        return (Task) super.getSelected();
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
                            String timestamp_json = RemoteComms.get("/timestamp/tasks_timestamp", headers);
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
                                String tasks_json = RemoteComms.get("/tasks", headers);
                                TaskServerObject taskServerObject = (TaskServerObject) TaskManager.getInstance().parseJSONobject(tasks_json, new TaskServerObject());
                                if (taskServerObject != null)
                                {
                                    if (taskServerObject.get_embedded() != null)
                                    {
                                        Task[] tasks_arr = taskServerObject.get_embedded().getTasks();

                                        tasks = new HashMap<>();
                                        for (Task task : tasks_arr)
                                            tasks.put(task.get_id(), task);

                                        IO.log(getClass().getName(), IO.TAG_VERBOSE, "reloaded tasks.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Tasks in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Tasks in the database.");

                                String task_items_json = RemoteComms.get("/tasks/resources", headers);
                                TaskItemServerObject taskItemsServerObject = (TaskItemServerObject) TaskManager.getInstance().parseJSONobject(task_items_json, new TaskItemServerObject());
                                if (taskItemsServerObject != null)
                                {
                                    if (taskItemsServerObject.get_embedded() != null)
                                    {
                                        TaskItem[] task_items_arr = taskItemsServerObject.get_embedded().getTask_resources();

                                        task_items = new HashMap<>();
                                        for (TaskItem taskItem : task_items_arr)
                                        {
                                            //check if bucket exists for Task
                                            if(task_items.get(taskItem.getTask_id())!=null)
                                                task_items.get(taskItem.getTask_id()).put(taskItem.get_id(), taskItem);//add TaskItem to Task's bucket
                                            else //does not exist, create one
                                            {
                                                //init Task TaskItem bucket
                                                HashMap items = new HashMap<>();
                                                items.put(taskItem.get_id(), taskItem);
                                                //put first item in bucket
                                                task_items.put(taskItem.getTask_id(), items);
                                            }
                                        }

                                        IO.log(getClass().getName(), IO.TAG_VERBOSE, "reloaded task resources/materials.");
                                    } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any TaskItems in the database.");
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any TaskItems in the database.");

                                IO.log(getClass().getName(), IO.TAG_INFO, "reloaded collection of tasks.");

                                serialize(ROOT_PATH + filename, tasks);
                                serialize(ROOT_PATH + "task_items.dat", task_items);

                            } else
                            {
                                IO.log(this.getClass()
                                        .getName(), IO.TAG_INFO, "binary object [" + ROOT_PATH + filename + "] on local disk is already up-to-date.");
                                tasks = (HashMap<String, Task>) deserialize(ROOT_PATH + filename);
                                task_items = (HashMap<String, HashMap<String, TaskItem>>) deserialize(ROOT_PATH + "task_items.dat");
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

    class TaskItemServerObject extends ServerObject
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
            private TaskItem[] task_resources;

            public TaskItem[] getTask_resources()
            {
                return task_resources;
            }

            public void setTask_resources(TaskItem[] task_resources)
            {
                this.task_resources = task_resources;
            }
        }
    }
}