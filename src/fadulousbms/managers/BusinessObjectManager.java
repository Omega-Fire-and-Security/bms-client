package fadulousbms.managers;

import fadulousbms.auxilary.*;
import fadulousbms.model.*;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 */
public abstract class BusinessObjectManager
{
    private BusinessObject selected;
    private long refresh_lock = 0;

    public abstract void initialize();

    protected long getRefresh_lock()
    {
        return refresh_lock;
    }

    protected void setRefresh_lock(long refresh_lock)
    {
        this.refresh_lock = refresh_lock;
    }

    public boolean isSerialized(String path)
    {
        File file = new File(path);
        return file.exists();
    }

    public void forceSynchronise()
    {
        try
        {
            if(getRefresh_lock()<=0)//if there's no other thread synchronising the data-set, synchronise the data-set
                reloadDataFromServer(getSynchronisationCallback());
            else IO.log(getClass().getName(), IO.TAG_WARN, "can't synchronize model's data-set, thread started at ["+getRefresh_lock()+"] is still busy.");
        } catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
        } catch (ClassNotFoundException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    protected void synchroniseDataset()
    {
        try
        {
            if(getDataset()==null)//TODO: improve data set timestamp checks before synchronization
                if(getRefresh_lock()<=0)//if there's no other thread synchronising the data-set, synchronise the data-set
                    reloadDataFromServer(getSynchronisationCallback());
                else IO.log(getClass().getName(), IO.TAG_WARN, "can't synchronize model's data-set, thread started at ["+getRefresh_lock()+"] is still busy.");
            else IO.log(getClass().getName(), IO.TAG_WARN, "model's data-set has already been set, not synchronizing.");
        } catch (MalformedURLException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("URL Error", ex.getMessage(), IO.TAG_ERROR);
        } catch (ClassNotFoundException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            IO.showMessage("ClassNotFoundException", e.getMessage(), IO.TAG_ERROR);
        } catch (IOException ex)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
            IO.showMessage("I/O Error", ex.getMessage(), IO.TAG_ERROR);
        }
    }

    private void reloadDataFromServer(Callback callback) throws ClassNotFoundException, IOException
    {
        //set model refresh lock
        setRefresh_lock(System.currentTimeMillis());
        if(callback!=null)
        {
            callback.call(null);//execute anonymous method to reload model's data-set
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "no anonymous function was provided to be executed when reloading " + getClass().getName() + "'s data-set.");
        //unlock model's refresh lock
        setRefresh_lock(0);
    }

    abstract Callback getSynchronisationCallback();

    public abstract HashMap<String, ? extends BusinessObject> getDataset();

    public void setSelected(BusinessObject selected)
    {
        this.selected=selected;
    }

    public BusinessObject getSelected()
    {
        return this.selected;
    }

    public void serialize(String file_path, Object obj) throws IOException
    {
        String path = file_path.substring(0, file_path.lastIndexOf("/")+1);
        File fpath = new File(path);
        if(!fpath.exists())
        {
            if (fpath.mkdirs())
            {
                IO.log(getClass().getName(), IO.TAG_INFO,"successfully created new directory["+path+"].");
            } else {
                IO.log(getClass().getName(), IO.TAG_ERROR,"could not create new directory["+path+"].");
                return;
            }
        }else IO.log(getClass().getName(), IO.TAG_INFO, "directory["+path+"] already exists.");

        File file = new File(file_path);
        if(!file.exists())
        {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
            out.writeObject(obj);
            out.flush();
            out.close();
            IO.log(getClass().getName(), IO.TAG_INFO, "successfully serialized file["+file.getAbsolutePath()+"] to disk.");
        }else IO.log(getClass().getName(), IO.TAG_INFO, "file["+file.getAbsolutePath()+"] is already up-to-date.");
    }

    public Object deserialize(String path) throws IOException, ClassNotFoundException
    {
        ObjectInputStream in = new ObjectInputStream(new FileInputStream(new File(path)));
        Object obj = in.readObject();
        in.close();
        IO.log(getClass().getName(), IO.TAG_INFO, "successfully deserialized file ["+path+"].");
        return obj;
    }

    public void emailBusinessObject(BusinessObject businessObject, String pdf_path, Callback callback) throws IOException
    {
        if(businessObject==null)
        {
            IO.logAndAlert("Error", "Invalid "+businessObject.getClass().getName(), IO.TAG_ERROR);
            return;
        }
        if(EmployeeManager.getInstance().getDataset()==null)
        {
            IO.logAndAlert("Error", "Could not find any employees in the system.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive()==null)
        {
            IO.logAndAlert("Error: Invalid Session", "Could not find any valid sessions.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActive().isExpired())
        {
            IO.logAndAlert("Error: Session Expired", "The active session has expired.", IO.TAG_ERROR);
            return;
        }
        if(SessionManager.getInstance().getActiveEmployee()==null)
        {
            IO.logAndAlert("Error: Invalid Employee Session", "Could not find any active employee sessions.", IO.TAG_ERROR);
            return;
        }
        String base64_obj = null;
        if(pdf_path!=null)
        {
            File f = new File(pdf_path);
            if (f != null)
            {
                if (f.exists())
                {
                    FileInputStream in = new FileInputStream(f);
                    byte[] buffer =new byte[(int) f.length()];
                    in.read(buffer, 0, buffer.length);
                    in.close();
                    base64_obj = Base64.getEncoder().encodeToString(buffer);
                } else
                {
                    IO.logAndAlert(QuoteManager.class.getName(), "File [" + pdf_path + "] not found.", IO.TAG_ERROR);
                }
            } else
            {
                IO.log(QuoteManager.class.getName(), "File [" + pdf_path + "] object is null.", IO.TAG_ERROR);
            }
        } else IO.log(QuoteManager.class.getName(), "Could not get valid path for created "+businessObject.getClass().getName()+" pdf.", IO.TAG_ERROR);
        final String finalBase64_obj = base64_obj;

        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - eMail "+businessObject.getClass().getSimpleName()+" ["+businessObject.get_id()+"]");
        stage.setMinWidth(320);
        stage.setHeight(350);
        stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(1);

        final TextField txt_destination = new TextField();
        txt_destination.setMinWidth(200);
        txt_destination.setMaxWidth(Double.MAX_VALUE);
        txt_destination.setPromptText("Type in recipient eMail addresses.");
        HBox destination = CustomTableViewControls.getLabelledNode("Recipient/s: ", 200, txt_destination);

        final TextField txt_subject = new TextField();
        txt_subject.setMinWidth(200);
        txt_subject.setMaxWidth(Double.MAX_VALUE);
        txt_subject.setPromptText("Type in an eMail subject");
        txt_subject.setText(businessObject.getClass().getSimpleName()+" ["+businessObject.get_id()+"]");
        HBox subject = CustomTableViewControls.getLabelledNode("Subject: ", 200, txt_subject);

        final TextArea txt_message = new TextArea();
        txt_message.setMinWidth(200);
        txt_message.setMaxWidth(Double.MAX_VALUE);
        HBox message = CustomTableViewControls.getLabelledNode("Message: ", 200, txt_message);

        //set default message
        Employee sender = SessionManager.getInstance().getActiveEmployee();
        String title = sender.getGender().toLowerCase().equals("male") ? "Mr." : "Miss.";;
        String def_msg = "Good day,\n\n<<insert message here>>\n\nThank you.\n\nBest Regards,\n"
                + title + " " + sender.getFirstname().toCharArray()[0]+". "+sender.getLastname();
        txt_message.setText(def_msg);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Send", event ->
        {
            if(!Validators.isValidNode(txt_destination, txt_destination.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_subject, txt_subject.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_message, txt_message.getText(), 1, ".+"))
                return;

            String msg = txt_message.getText();

            //convert all new line chars to HTML break-lines
            msg = msg.replaceAll("\\n", "<br/>");

            try
            {
                //send email
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/json"));//multipart/form-data
                headers.add(new AbstractMap.SimpleEntry<>("_id", businessObject.get_id()));
                headers.add(new AbstractMap.SimpleEntry<>("destination", txt_destination.getText()));
                headers.add(new AbstractMap.SimpleEntry<>("message", msg));
                headers.add(new AbstractMap.SimpleEntry<>("subject", txt_subject.getText()));

                if(SessionManager.getInstance().getActive()!=null)
                {
                    headers.add(new AbstractMap.SimpleEntry<>("session_id", SessionManager.getInstance().getActive().getSession_id()));
                    headers.add(new AbstractMap.SimpleEntry<>("from_name", SessionManager.getInstance().getActiveEmployee().getName()));
                } else
                {
                    IO.logAndAlert( "No active sessions.", "Session expired", IO.TAG_ERROR);
                    return;
                }

                FileMetadata fileMetadata = new FileMetadata(businessObject.getClass().getSimpleName().toLowerCase()+"_"+businessObject.get_id()+".pdf","application/pdf");
                fileMetadata.setCreator(SessionManager.getInstance().getActive().getUsr());
                fileMetadata.setFile(finalBase64_obj);
                HttpURLConnection connection = RemoteComms.postJSON(businessObject.apiEndpoint()+"/mailto", fileMetadata.getJSONString(), headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        //TODO: CC self
                        IO.logAndAlert("Success", "Successfully emailed "+businessObject.getClass().getSimpleName()+"!", IO.TAG_INFO);
                        if(callback!=null)
                            callback.call(null);
                    } else {
                        IO.logAndAlert( "ERROR " + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //Add form controls vertically on the stage
        vbox.getChildren().add(destination);
        vbox.getChildren().add(subject);
        vbox.getChildren().add(message);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File("src/fadulousbms/styles/home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }
}
