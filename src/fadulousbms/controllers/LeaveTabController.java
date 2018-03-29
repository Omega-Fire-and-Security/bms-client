package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.ResourceBundle;

/**
 * Created by ghost on 2017/11/15.
 */
public class LeaveTabController extends ScreenController implements Initializable
{
    @FXML
    private TableColumn colLeaveId, colEmployee, colStatus, colStartDate, colEndDate,
            colReturnDate, colDateLogged, colOther, colAction;
    @FXML
    private TableView<Leave> tblLeave;
    public static final String TAB_ID = "leaveTab";

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
        new Thread(() ->
                refreshModel(param ->
                {
                    Platform.runLater(() -> refreshView());
                    return null;
                })).start();
    }

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading leave tab view.");

        colLeaveId.setMinWidth(100);
        colLeaveId.setCellValueFactory(new PropertyValueFactory<>("_id"));
        colEmployee.setMinWidth(100);
        colEmployee.setCellValueFactory(new PropertyValueFactory<>("employee"));
        colStatus.setCellValueFactory(new PropertyValueFactory<>("status"));
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colStartDate, "start_date");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colEndDate, "end_date");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colReturnDate, "return_date");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateLogged, "date_logged");
        CustomTableViewControls.makeEditableTableColumn(colOther, TextFieldTableCell.forTableColumn(), 120, "other", LeaveManager.getInstance());

        if(LeaveManager.getInstance().getDataset()!=null)
        {
            ObservableList<Leave> lst_leave = FXCollections.observableArrayList();
            lst_leave.addAll(LeaveManager.getInstance().getDataset().values());
            tblLeave.setItems(lst_leave);
        } else IO.log(getClass().getName(), IO.TAG_WARN, "no leave records were found in the database.");

        tblLeave.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                LeaveManager.getInstance().setSelected(tblLeave.getSelectionModel().getSelectedItem()));

        Callback<TableColumn<Leave, String>, TableCell<Leave, String>> cellFactory
                =
                new Callback<TableColumn<Leave, String>, TableCell<Leave, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Leave, String> param)
                    {
                        final TableCell<Leave, String> cell = new TableCell<Leave, String>()
                        {
                            final Button btnApprove = new Button("Approve");
                            final Button btnPDF = new Button("View As PDF");
                            final Button btnUpload = new Button("Upload Signed");
                            final Button btnRequestApproval = new Button("Request Approval");
                            final Button btnViewSigned = new Button("View Signed");
                            final Button btnEmailSigned = new Button("eMail Signed");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnApprove.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnApprove.setMinWidth(100);
                                btnApprove.setMinHeight(35);
                                HBox.setHgrow(btnApprove, Priority.ALWAYS);
                                btnApprove.setDisable(true);
                                if(SessionManager.getInstance().getActiveEmployee()!=null)
                                {
                                    //disable approve button if not authorised
                                    if (SessionManager.getInstance().getActiveEmployee().getAccessLevel()>= AccessLevel.SUPERUSER.getLevel())
                                    {
                                        btnApprove.getStyleClass().add("btnDefault");
                                        btnApprove.setDisable(false);
                                    } else btnApprove.getStyleClass().add("btnDisabled");
                                } else IO.logAndAlert("Error: Invalid Session", "No valid active employee session found, please log in.", IO.TAG_ERROR);

                                btnRequestApproval.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnRequestApproval.getStyleClass().add("btnDefault");
                                btnRequestApproval.setMinWidth(100);
                                btnRequestApproval.setMinHeight(35);
                                HBox.setHgrow(btnRequestApproval, Priority.ALWAYS);

                                btnPDF.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnPDF.getStyleClass().add("btnDefault");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnUpload.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnUpload.getStyleClass().add("btnAdd");
                                btnUpload.setMinWidth(100);
                                btnUpload.setMinHeight(35);
                                HBox.setHgrow(btnUpload, Priority.ALWAYS);

                                btnViewSigned.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnViewSigned.getStyleClass().add("btnDefault");
                                btnViewSigned.setMinWidth(100);
                                btnViewSigned.setMinHeight(35);
                                HBox.setHgrow(btnViewSigned, Priority.ALWAYS);

                                btnEmailSigned.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnEmailSigned.setMinWidth(100);
                                btnEmailSigned.setMinHeight(35);
                                HBox.setHgrow(btnEmailSigned, Priority.ALWAYS);
                                if(!empty)
                                {
                                    if (getTableView().getItems().get(getIndex()).getStatus()>=Leave.STATUS_FINALISED)
                                    {
                                        btnEmailSigned.getStyleClass().add("btnDefault");
                                        btnEmailSigned.setDisable(false);
                                    } else
                                    {
                                        btnEmailSigned.getStyleClass().add("btnDisabled");
                                        btnEmailSigned.setDisable(true);
                                    }
                                }

                                btnRemove.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(100);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                } else
                                {
                                    HBox hBox = new HBox(btnApprove, btnRequestApproval, btnPDF, btnUpload, btnViewSigned, btnEmailSigned, btnRemove);
                                    hBox.setMaxWidth(Double.MAX_VALUE);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    Leave leave = getTableView().getItems().get(getIndex());

                                    btnApprove.setOnAction(event ->
                                            LeaveManager.approveLeave(leave, param ->
                                            {
                                                new Thread(() ->
                                                        refreshModel(param1 ->
                                                        {
                                                            Platform.runLater(() -> refreshView());
                                                            return null;
                                                        })).start();
                                                return null;
                                            }));

                                    btnPDF.setOnAction(event ->
                                    {
                                        LeaveManager.getInstance().initialize();
                                        if (leave == null)
                                        {
                                            IO.logAndAlert("Error " + getClass()
                                                    .getName(), "Leave object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        //update selected leave object on respective manager
                                        if(LeaveManager.getInstance().getDataset()!=null)
                                            LeaveManager.getInstance().setSelected(LeaveManager.getInstance().getDataset().get(leave.get_id()));
                                        else IO.log(getClass().getName(), IO.TAG_ERROR, "no leave applications were found in the database." );

                                        LeaveManager.getInstance().viewPDF(leave);
                                    });

                                    btnUpload.setOnAction(event ->
                                    {
                                        if (leave == null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(),
                                                    "Leave application object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        LeaveManager.getInstance().uploadSigned(leave.get_id());
                                    });

                                    btnViewSigned.setOnAction(event ->
                                            viewSignedLeaveApplication(leave));

                                    btnRequestApproval.setOnAction(event ->
                                    {
                                        try {
                                            LeaveManager.getInstance().requestApproval(leave, null);
                                        } catch (IOException e) {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnEmailSigned.setOnAction(event ->
                                            LeaveManager.getInstance().emailSigned(leave, null));

                                    btnRemove.setOnAction(event ->
                                    {
                                        //197.242.144.30
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        //getTableView().getItems().remove(quote);
                                        //getTableView().refresh();
                                        //TODO: remove from server
                                        //IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
                                    });

                                    hBox.setFillHeight(true);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    hBox.setSpacing(5);
                                    setGraphic(hBox);
                                    setText(null);
                                }
                            }
                        };
                        return cell;
                    }
                };

        colAction.setCellValueFactory(new PropertyValueFactory<>(""));
        colAction.setCellFactory(cellFactory);
        colAction.setMinWidth(700);
    }

    @Override
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading leave tab model.");
        LeaveManager.getInstance().initialize();
        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        LeaveManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    private static void viewSignedLeaveApplication(Leave leave)
    {
        if(leave==null)
        {
            IO.logAndAlert("Error", "Selected Leave object is invalid.", IO.TAG_ERROR);
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
                    byte[] file = RemoteComms.sendFileRequest("/leave_application/signed/" + leave.get_id(), headers);

                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        String local_filename = leave.get_id() + "_signed.pdf";
                        String local_path = "out/pdf/" + local_filename;
                        /*if (new File("out/" + local_filename).exists())
                            Files.delete(new File("out/" + local_filename).toPath());*/
                        //TODO: fix this hack
                        int i = 1;
                        File f = new File(local_path);
                        if (f.exists())
                        {
                            if (f.delete())
                                IO.log(JobsController.class.getName(), IO.TAG_INFO, "deleted file [" + f
                                        .getAbsolutePath() + "]");
                            else
                            {
                                IO.log(JobsController.class.getName(), IO.TAG_WARN, "could not delete file ["+f.getAbsolutePath()+"]");
                                //get new filename
                                while((f=new File(local_path)).exists())
                                {
                                    local_path = "out/pdf/"+leave.get_id() + "_signed." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File(local_path));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded signed leave application [" + leave.get_id()
                                +"] to path [" + local_path + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen(local_path);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader", "Signed leave application '" + leave.get_id()
                                +"' for user ["+leave.getUsr()+"] could not be downloaded.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("Error", "Could not download signed leave application for user ["+leave.getUsr()+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static RadialMenuItem[] getContextMenu()
    {
        RadialMenuItem menuLeave = new RadialMenuItemCustom(30, "Approve", null, null, event ->
            LeaveManager.approveLeave((Leave) LeaveManager.getInstance().getSelected(), param ->
            {
                //refresh UI on approve
                //refresh model
                LeaveManager.getInstance().initialize();
                //refresh view
                final ScreenManager screenManager = ScreenManager.getInstance();
                ScreenManager.getInstance().showLoadingScreen(arg ->
                {
                    new Thread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            try
                            {
                                if(screenManager.loadScreen(Screens.HR.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.HR.getScreen())))
                                {
                                    //Platform.runLater(() ->
                                    screenManager.setScreen(Screens.HR.getScreen());
                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load HR screen.");
                            } catch (IOException e)
                            {
                                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }).start();
                    return null;
                });
                return null;
            }));
        return new RadialMenuItem[]{menuLeave};
    }


}