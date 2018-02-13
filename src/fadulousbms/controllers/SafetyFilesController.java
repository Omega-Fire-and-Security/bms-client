/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.EmployeeManager;
import fadulousbms.managers.SafetyManager;
import fadulousbms.managers.ScreenManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.Employee;
import fadulousbms.model.FileMetadata;
import fadulousbms.model.Screens;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class SafetyFilesController extends ScreenController implements Initializable
{
    @FXML
    private TableView tblSafety;
    @FXML
    private TableColumn colIndex,colLabel,colPath,colRequired,colOptions,colType,colSelect,colAction;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        refreshModel();
        refreshView();
    }

    @Override
    public void refreshView()
    {
        Employee e = SessionManager.getInstance().getActiveEmployee();
        if(e!=null)
            this.getUserNameLabel().setText(e.getFirstname() + " " + e.getLastname());
        else IO.log(getClass().getName(), IO.TAG_ERROR, "No active sessions.");

        CustomTableViewControls.makeEditableTableColumn(colIndex, TextFieldTableCell.forTableColumn(), 215, "index", "/api/safety/index");
        CustomTableViewControls.makeEditableTableColumn(colLabel, TextFieldTableCell.forTableColumn(), 215, "label", "/api/safety/index");
        CustomTableViewControls.makeEditableTableColumn(colPath, TextFieldTableCell.forTableColumn(), 215, "pdf_path", "/api/safety/index");
        CustomTableViewControls.makeToggleButtonTableColumn(colRequired, null,60, "required", "/api/safety/index");
        CustomTableViewControls.makeEditableTableColumn(colOptions, TextFieldTableCell.forTableColumn(), 215, "logo_options", "/api/safety/index");
        CustomTableViewControls.makeCheckboxedTableColumn(colSelect, null, 60, "marked", "/api/safety/index");
        CustomTableViewControls.makeEditableTableColumn(colType, TextFieldTableCell.forTableColumn(), 60, "type", "/api/safety/index");
        CustomTableViewControls.makeActionTableColumn(colAction, 270, "pdf_path", "/api/ safety/index");

        if(SafetyManager.getInstance().getDataset()!=null)
        {
            FileMetadata[] files_arr = new FileMetadata[SafetyManager.getInstance().getDataset().size()];
            SafetyManager.getInstance().getDataset().values().toArray(files_arr);

            ObservableList<FileMetadata> lst_safety = FXCollections.observableArrayList();
            lst_safety.addAll(files_arr);
            tblSafety.setItems(lst_safety);
        }
    }

    @Override
    public void refreshModel()
    {
        EmployeeManager.getInstance().initialize();
        SafetyManager.getInstance().initialize();
    }

    @Override
    public void forceSynchronise()
    {
        refreshModel();
        Platform.runLater(() -> refreshView());
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void generateIndex()
    {
        if(SafetyManager.getInstance().getDataset()!=null)
        {
            FileMetadata[] files_arr = new FileMetadata[SafetyManager.getInstance().getDataset().size()];
            SafetyManager.getInstance().getDataset().values().toArray(files_arr);

            IO.viewIndexPage("Safety Documents Index", files_arr, "bin/safety_index.pdf");
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "no documents found in the database.");
    }

    @FXML
    public void printIndex()
    {
        IO.printIndexPage("bin/safety_index.pdf");
    }

    @FXML
    public void printMarked()
    {
        if(SafetyManager.getInstance().getDataset()!=null)
        {
            FileMetadata[] files_arr = new FileMetadata[SafetyManager.getInstance().getDataset().size()];
            SafetyManager.getInstance().getDataset().values().toArray(files_arr);

            IO.printSelectedDocuments(files_arr);
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "no safety documents were found in the database.");
    }

    @FXML
    public void printAll()
    {
        if(SafetyManager.getInstance().getDataset()!=null)
        {
            FileMetadata[] files_arr = new FileMetadata[SafetyManager.getInstance().getDataset().size()];
            SafetyManager.getInstance().getDataset().values().toArray(files_arr);

            IO.printAllDocuments(files_arr);
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "no safety documents were found in the database.");
    }

    @FXML
    public void upload()
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
                            headers.add(new AbstractMap.SimpleEntry<>("Filename", f.getName()));
                            RemoteComms.uploadFile("/api/upload", headers, buffer);
                            System.out.println("\n File size: " + buffer.length + " bytes.");
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

    @FXML
    public void newSafetyDocReference()
    {
        SafetyManager.newSafetyDocumentReference(null);
    }

    @FXML
    public void repopulateSafetyDocs()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                try
                {
                    HttpURLConnection connection = RemoteComms.postData("/api/safety/init", "", headers);
                    if(connection!=null)
                    {
                        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK)
                        {
                            IO.logAndAlert("Success", "Successfully repopulated safety documents.", IO.TAG_INFO);
                        }
                        else
                        {
                            IO.logAndAlert("Error: " + connection.getResponseCode(), IO
                                    .readStream(connection.getErrorStream()), IO.TAG_ERROR);
                        }
                        connection.disconnect();
                    } else IO.logAndAlert("Error", "Could not connect to server.", IO.TAG_ERROR);
                } catch (IOException e)
                {
                    IO.log(getClass().getName() , IO.TAG_ERROR, e.getMessage());
                }
            }else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    @FXML
    public void back()
    {
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.OPERATIONS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            ScreenManager.getInstance().setScreen(Screens.OPERATIONS.getScreen());
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
}
