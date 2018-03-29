package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.*;
import fadulousbms.controllers.JobsController;
import fadulousbms.model.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 * @author ghost
 */
public class EmployeeManager extends ApplicationObjectManager
{
    private HashMap<String, Employee> employees;
    private Gson gson;
    private static EmployeeManager employeeManager = new EmployeeManager();

    private EmployeeManager()
    {
    }

    @Override
    public void initialize()
    {
        synchroniseDataset();
    }

    @Override
    public HashMap<String, Employee> getDataset(){return this.employees;}

    public static EmployeeManager getInstance()
    {
        return employeeManager;
    }

    @Override
    public Employee getSelected()
    {
        return (Employee) super.getSelected();
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
                            gson  = new GsonBuilder().create();
                            ArrayList<AbstractMap.SimpleEntry<String,String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("session_id", smgr.getActive().getSession_id()));

                            String employee_json_object = RemoteComms.get("/employees", headers);
                            EmployeeServerObject employeeServerObject = (EmployeeServerObject) EmployeeManager.getInstance().parseJSONobject(employee_json_object, new EmployeeServerObject());

                            if(employeeServerObject!=null)
                            {
                                if(employeeServerObject.get_embedded()!=null)
                                {
                                    Employee[] users = employeeServerObject.get_embedded().get_employees();

                                    employees = new HashMap();
                                    for (Employee employee : users)
                                        employees.put(employee.getUsr(), employee);
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not find any Employees in database.");
                            } else IO.log(getClass().getName(), IO.TAG_ERROR, "EmployeeServerObject (containing Employee objects & other metadata) is null");
                            IO.log(getClass().getName(), IO.TAG_INFO, "reloaded employee collection.");
                        } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                    } else IO.logAndAlert("Session Expired", "Active session is invalid", IO.TAG_ERROR);
                } catch (MalformedURLException ex)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                } catch (IOException ex)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, ex.getMessage());
                }
                return null;
            }
        };
    }

    public void newEmployee(Node parent, Callback callback)
    {
        //TODO: pass set_password boolean arg
        EmployeeManager.getInstance().setSelected(null);

        TextField txtFirstname = new TextField();
        txtFirstname.setMinWidth(200);
        txtFirstname.setMaxWidth(Double.MAX_VALUE);
        //HBox first_name = CustomTableViewControls.getLabelledNode("First Name", 200, txtFirstname);

        TextField txtLastname = new TextField();
        txtLastname.setMinWidth(200);
        txtLastname.setMaxWidth(Double.MAX_VALUE);
        //HBox last_name = CustomTableViewControls.getLabelledNode("Last Name", 200, txtLastname);

        TextField txtEmail = new TextField();
        txtEmail.setMinWidth(200);
        txtEmail.setMaxWidth(Double.MAX_VALUE);
        //HBox email = CustomTableViewControls.getLabelledNode("eMail Address:", 200, txtEmail);

        TextField txtTelephone = new TextField();
        txtTelephone.setMinWidth(200);
        txtTelephone.setMaxWidth(Double.MAX_VALUE);
        //HBox telephone = CustomTableViewControls.getLabelledNode("Telephone #: ", 200, txtTelephone);

        TextField txtCellphone = new TextField();
        txtCellphone.setMinWidth(200);
        txtCellphone.setMaxWidth(Double.MAX_VALUE);
        //HBox cellphone = CustomTableViewControls.getLabelledNode("Cellphone #: ", 200, txtCellphone);

        TextArea txtOther = new TextArea();
        txtOther.setMinWidth(200);
        txtOther.setMaxWidth(Double.MAX_VALUE);
        //HBox other = CustomTableViewControls.getLabelledNode("Other: ", 200, txtOther);

        Button btnSubmit = new Button("Create New Client Representative");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(45);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(new Label("First Name: "), 0, 0);
        page.add(txtFirstname, 1, 0);

        page.add(new Label("Last Name: "), 0, 1);
        page.add(txtLastname, 1, 1);

        page.add(new Label("eMail Address: "), 0, 2);
        page.add(txtEmail, 1, 2);

        page.add(new Label("Tel No.: "), 0, 3);
        page.add(txtTelephone, 1, 3);

        page.add(new Label("Cellphone Address: "), 0, 4);
        page.add(txtCellphone, 1, 4);

        page.add(new Label("Other info: "), 0, 5);
        page.add(txtOther, 1, 5);

        page.add(btnSubmit, 1, 6);

        PopOver popover = new PopOver(page);
        popover.setTitle("Create new Client Representative");
        popover.setDetached(true);
        popover.show(parent);

        TextFields.bindAutoCompletion(txtFirstname, EmployeeManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    setSelected(event.getCompletion());

                    if(getSelected().getFirstname()!=null)
                        txtFirstname.setText(getSelected().getFirstname());
                    if(getSelected().getLastname()!=null)
                        txtLastname.setText(getSelected().getLastname());
                    if(getSelected().getCell()!=null)
                        txtCellphone.setText(getSelected().getCell());
                    if(getSelected().getTel()!=null)
                        txtTelephone.setText(getSelected().getTel());
                    if(getSelected().getEmail()!=null)
                        txtEmail.setText(getSelected().getEmail());
                    if(getSelected().getTel()!=null)
                        txtOther.setText(getSelected().getOther());
                }
            }
        });

        TextFields.bindAutoCompletion(txtLastname, EmployeeManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    setSelected(event.getCompletion());

                    if(getSelected().getFirstname()!=null)
                        txtFirstname.setText(getSelected().getFirstname());
                    if(getSelected().getLastname()!=null)
                        txtLastname.setText(getSelected().getLastname());
                    if(getSelected().getCell()!=null)
                        txtCellphone.setText(getSelected().getCell());
                    if(getSelected().getTel()!=null)
                        txtTelephone.setText(getSelected().getTel());
                    if(getSelected().getEmail()!=null)
                        txtEmail.setText(getSelected().getEmail());
                    if(getSelected().getTel()!=null)
                        txtOther.setText(getSelected().getOther());
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

                if(!Validators.isValidNode(txtFirstname, txtFirstname.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txtLastname, txtLastname.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txtCellphone, txtCellphone.getText(), 1, ".+"))
                    return;
                if(!Validators.isValidNode(txtTelephone, txtTelephone.getText(), 1, ".+"))
                    return;

                //TODO: check if email doesn't exist already
                if(!Validators.isValidNode(txtEmail, txtEmail.getText(), 1, ".+"))
                    return;

                boolean found = false;
                for(Employee employee: EmployeeManager.getInstance().getDataset().values())
                    if(txtEmail.getText().toLowerCase().equals(employee.getEmail().toLowerCase()))
                        found=true;

                if(found)
                {
                    IO.logAndAlert("Error", "User with email address ["+txtEmail.getText()+"] already exists.", IO.TAG_ERROR);
                    return;
                }

                //if txtFirstname and txtLastname matches selected_contact_person's first_name and last_name ask if they want to make a duplicate record
                String proceed = IO.OK;
                if(getSelected()!=null)
                    if(txtFirstname.getText().equals(getSelected().getFirstname()) && txtLastname.equals(getSelected().getLastname()))
                        proceed = IO.showConfirm("Found duplicate person, continue?", "Found person with the name ["+getSelected().getName()+"], add another record?");

                //did they choose to continue with the creation or cancel?
                if(!proceed.equals(IO.OK))
                {
                    IO.log(getClass().getName(), "aborting new Client contact person creation.", IO.TAG_VERBOSE);
                    return;
                }

                int access_lvl=0;

                Employee employee = new Employee();
                employee.setUsr(txtEmail.getText().toLowerCase());
                String pwd = "";
                try
                {
                    pwd = IO.generateRandomString(32, true, true);
                    assert pwd.length()==12;
                    employee.setPwd(IO.getEncryptedHexString(pwd));
                } catch (Exception e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }

                employee.setAccessLevel(access_lvl);
                employee.setFirstname(txtFirstname.getText());
                employee.setLastname(txtLastname.getText());
                employee.setGender("Male");
                employee.setEmail(txtEmail.getText());
                employee.setTel(txtTelephone.getText());
                employee.setCell(txtCellphone.getText());
                employee.setCreator(SessionManager.getInstance().getActive().getUsr());

                if(txtOther.getText()!=null)
                    employee.setOther(txtOther.getText());
                employee.setOther(employee.getOther() + " pwd=" + pwd);
                try
                {
                    EmployeeManager.getInstance().putObject(employee, callback);
                } catch (IOException e)
                {
                    IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                }
            }
        });
    }

    public void uploadID(String employee_id)
    {
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    FileChooser fileChooser = new FileChooser();
                    File f = fileChooser.showOpenDialog(null);
                    if (f != null)
                    {
                        if (f.exists())
                        {
                            FileInputStream in = new FileInputStream(f);
                            byte[] buffer = new byte[(int) f.length()];
                            in.read(buffer, 0, buffer.length);
                            in.close();

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            RemoteComms.uploadFile("/file/upload/" + employee_id, headers, buffer);
                            IO.log(getClass().getName(), IO.TAG_INFO, "\n uploaded ID for employee ["+employee_id+"], file size: [" + buffer.length + "] bytes.");
                        } else
                        {
                            IO.logAndAlert(getClass().getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(getClass().getName(), "File object is null.", IO.TAG_ERROR);
                    }
                }catch (IOException e)
                {
                    IO.log(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void uploadCV(String employee_id)
    {
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                try
                {
                    FileChooser fileChooser = new FileChooser();
                    File f = fileChooser.showOpenDialog(null);
                    if (f != null)
                    {
                        if (f.exists())
                        {
                            FileInputStream in = new FileInputStream(f);
                            byte[] buffer = new byte[(int) f.length()];
                            in.read(buffer, 0, buffer.length);
                            in.close();

                            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                            headers.add(new AbstractMap.SimpleEntry<>("Content-Type", "application/pdf"));

                            //TODO:
                            RemoteComms.uploadFile("/file/upload/" + employee_id, headers, buffer);
                            IO.log(getClass().getName(), IO.TAG_INFO, "\n uploaded CV for employee ["+employee_id+"], file size: [" + buffer.length + "] bytes.");
                        } else
                        {
                            IO.logAndAlert(getClass().getName(), "File not found.", IO.TAG_ERROR);
                        }
                    } else
                    {
                        IO.log(getClass().getName(), "File object is null.", IO.TAG_ERROR);
                    }
                }catch (IOException e)
                {
                    IO.log(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
                }
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void viewID(String employee_id)
    {
        if(employee_id==null)
        {
            IO.logAndAlert("Error", "Invalid employee identifier object passed.", IO.TAG_ERROR);
            return;
        }

        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

                    //String filename = String.valueOf(bo.get(property));
                    long start = System.currentTimeMillis();
                    byte[] file = RemoteComms.sendFileRequest("/file/" + employee_id, headers);

                    //TODO:
                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        //String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                        String local_filename = employee_id + "_id.pdf";
                        /*if (new File("out/" + local_filename).exists())
                            Files.delete(new File("out/" + local_filename).toPath());*/
                        //TODO: fix this hack
                        int i = 1;
                        File f = new File("out/" + local_filename);
                        if (f.exists())
                        {
                            if (f.delete())
                                IO.log(JobsController.class.getName(), IO.TAG_INFO, "deleted file [" + f
                                        .getAbsolutePath() + "]");
                            else
                            {
                                IO.log(EmployeeManager.class.getName(), IO.TAG_WARN, "could not delete file ["+f.getAbsolutePath()+"]");
                                //get new filename
                                while((f=new File("out/"+local_filename)).exists())
                                {
                                    local_filename = employee_id + "_id." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File("out/" + local_filename));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded employee ID document [" + employee_id
                                +"] to path [out/" + local_filename + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen("out/" + local_filename);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader Error", "Employee ["+employee_id
                                +"] ID document file could not be downloaded because the active session has expired.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("File Downloader Error", "Employee ["+employee_id+"] ID document file could not be downloaded.", IO.TAG_ERROR);
                    //IO.logAndAlert("Error", "Could not download ID document for employee ["+employee_id+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static void viewCV(String employee_id)
    {
        if(employee_id==null)
        {
            IO.logAndAlert("Error", "Invalid employee identifier object passed.", IO.TAG_ERROR);
            return;
        }

        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

                    //String filename = String.valueOf(bo.get(property));
                    long start = System.currentTimeMillis();
                    //TODO:
                    byte[] file = RemoteComms.sendFileRequest("/file/" + employee_id, headers);

                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        //String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                        String local_filename = employee_id + "_cv.pdf";
                        /*if (new File("out/" + local_filename).exists())
                            Files.delete(new File("out/" + local_filename).toPath());*/
                        //TODO: fix this hack
                        int i = 1;
                        File f = new File("out/" + local_filename);
                        if (f.exists())
                        {
                            if (f.delete())
                                IO.log(JobsController.class.getName(), IO.TAG_INFO, "deleted file [" + f
                                        .getAbsolutePath() + "]");
                            else
                            {
                                IO.log(EmployeeManager.class.getName(), IO.TAG_WARN, "could not delete file ["+f.getAbsolutePath()+"]");
                                //get new filename
                                while((f=new File("out/"+local_filename)).exists())
                                {
                                    local_filename = employee_id + "_cv." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File("out/" + local_filename));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded employee CV document [" + employee_id
                                +"] to path [out/" + local_filename + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen("out/" + local_filename);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader Error", "Employee ["+employee_id
                                +"] CV document file could not be downloaded because the active session has expired.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("File Downloader Error", "Employee ["+employee_id+"] CV document file could not be downloaded.", IO.TAG_ERROR);
                    //IO.logAndAlert("Error", "Could not download ID document for employee ["+employee_id+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    class EmployeeServerObject extends ServerObject
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
            private Employee[] employees;

            public Employee[] get_employees()
            {
                return employees;
            }

            public void set_employees(Employee[] employees)
            {
                this.employees = employees;
            }
        }
    }
}
