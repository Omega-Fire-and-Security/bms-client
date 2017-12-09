/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Base64;
import java.util.ResourceBundle;

/**
 * views Controller class
 *
 * @author ghost
 */
public class JobsController extends ScreenController implements Initializable
{
    @FXML
    private TableView<Job> tblJobs;
    @FXML
    private TableColumn colJobNum, colClient, colSitename, colRequest, colTotal,
            colContactPerson, colDateGenerated, colPlannedStartDate,
            colDateAssigned, colDateStarted, colDateEnded, colCreator, colExtra, colAction;
    public static final String TAB_ID = "jobsTab";

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading jobs view..");

        if (JobManager.getInstance().getJobs() == null)
        {
            IO.logAndAlert(getClass().getName(), "no jobs in database", IO.TAG_ERROR);
            return;
        }
        if (JobManager.getInstance().getJobs().values() == null)
        {
            IO.logAndAlert(getClass().getName(), "no jobs in database", IO.TAG_ERROR);
            return;
        }
        colJobNum.setMinWidth(100);
        colJobNum.setCellValueFactory(new PropertyValueFactory<>("job_number"));
        CustomTableViewControls.makeEditableTableColumn(colRequest, TextFieldTableCell
                .forTableColumn(), 215, "job_description", "/api/job");
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_name"));
        colSitename.setCellValueFactory(new PropertyValueFactory<>("sitename"));
        colContactPerson.setCellValueFactory(new PropertyValueFactory<>("contact_person"));
        //TODO: contact_personProperty
        CustomTableViewControls
                .makeLabelledDatePickerTableColumn(colPlannedStartDate, "planned_start_date", "/api/job");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_logged", "/api/job");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateAssigned, "date_assigned", "/api/job");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateStarted, "date_started", "/api/job");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateEnded, "date_completed", "/api/job");
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        //CustomTableViewControls.makeJobManagerAction(colAction, 600, null);
        //colCreator.setCellValueFactory(new PropertyValueFactory<>("creator"));
        //TODO: creatorProperty
        //colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
        //TODO: totalProperty
        //CustomTableViewControls.makeEditableTableColumn(colExtra, TextFieldTableCell.forTableColumn(), 100, "extra", "/api/quote");
        //TODO: extraProperty
        //CustomTableViewControls.makeCheckboxedTableColumn(job_completed, CheckBoxTableCell.forTableColumn(job_completed), 80, "job_completed", "/api/job");
        //CustomTableViewControls.makeComboBoxTableColumn(invoice_id, invoices, "invoice_id", "short_id", "/api/job", 220);
        //invoice_id.setMinWidth(100);
        //invoice_id.setCellValueFactory(new PropertyValueFactory<>("invoice_id"));

        ObservableList<Job> lst_jobs = FXCollections.observableArrayList();
        lst_jobs.addAll(JobManager.getInstance().getJobs().values());
        tblJobs.setItems(lst_jobs);

        Callback<TableColumn<Job, String>, TableCell<Job, String>> cellFactory
                =
                new Callback<TableColumn<Job, String>, TableCell<Job, String>>()
                {
                    @Override
                    public TableCell call(final TableColumn<Job, String> param)
                    {
                        final TableCell<Job, String> cell = new TableCell<Job, String>()
                        {
                            final Button btnView = new Button("View Job");
                            final Button btnUpload = new Button("Upload Signed");
                            final ToggleButton btnSign = new ToggleButton("Not Signed");
                            final Button btnViewSigned = new Button("View Signed Document");
                            final Button btnInvoice = new Button("Generate Invoice");
                            final Button btnPDF = new Button("View as PDF");
                            final Button btnEmail = new Button("eMail Job Card");
                            final Button btnEmailSigned = new Button("eMail Signed Job Card");
                            final Button btnRemove = new Button("Delete");

                            @Override
                            public void updateItem(String item, boolean empty)
                            {
                                super.updateItem(item, empty);
                                btnView.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnView.getStyleClass().add("btnDefault");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

                                btnUpload.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnUpload.getStyleClass().add("btnDefault");
                                btnUpload.setMinWidth(130);
                                btnUpload.setMinHeight(35);
                                HBox.setHgrow(btnUpload, Priority.ALWAYS);

                                //btnSign.getStylesheets().add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                //btnSign.getStyleClass().add("btnDefault");
                                //btnSign.setStyle("-fx-border-radius: 20;");
                                btnSign.setMinWidth(100);
                                btnSign.setMinHeight(35);

                                HBox.setHgrow(btnSign, Priority.ALWAYS);

                                btnViewSigned.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnViewSigned.getStyleClass().add("btnDefault");
                                btnViewSigned.setMinWidth(130);
                                btnViewSigned.setMinHeight(35);
                                HBox.setHgrow(btnViewSigned, Priority.ALWAYS);

                                btnInvoice.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnInvoice.getStyleClass().add("btnDefault");
                                btnInvoice.setMinWidth(100);
                                btnInvoice.setMinHeight(35);
                                HBox.setHgrow(btnInvoice, Priority.ALWAYS);

                                btnPDF.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnPDF.getStyleClass().add("btnDefault");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnEmail.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnEmail.getStyleClass().add("btnAdd");
                                btnEmail.setMinWidth(100);
                                btnEmail.setMinHeight(35);
                                HBox.setHgrow(btnEmail, Priority.ALWAYS);

                                btnEmailSigned.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnEmailSigned.getStyleClass().add("btnAdd");
                                btnEmailSigned.setMinWidth(100);
                                btnEmailSigned.setMinHeight(35);
                                HBox.setHgrow(btnEmailSigned, Priority.ALWAYS);

                                btnRemove.getStylesheets()
                                        .add(this.getClass().getResource("../styles/home.css").toExternalForm());
                                btnRemove.getStyleClass().add("btnBack");
                                btnRemove.setMinWidth(100);
                                btnRemove.setMinHeight(35);
                                HBox.setHgrow(btnRemove, Priority.ALWAYS);

                                if (empty)
                                {
                                    setGraphic(null);
                                    setText(null);
                                }
                                else
                                {
                                    HBox hBox = new HBox(btnView, btnUpload, btnSign, btnViewSigned, btnInvoice, btnPDF, btnEmail, btnEmailSigned, btnRemove);
                                    hBox.setMaxWidth(Double.MAX_VALUE);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    Job job = getTableView().getItems().get(getIndex());

                                    btnSign.setText(job.isSigned() ? "Signed" : "Not Signed");
                                    btnSign.setSelected(job.isSigned());

                                    btnView.setOnAction(event ->
                                    {
                                        try
                                        {
                                            JobManager.getInstance().reloadDataFromServer();
                                            if (job == null)
                                            {
                                                IO.logAndAlert("Error " + getClass()
                                                        .getName(), "Job object is not set", IO.TAG_ERROR);
                                                return;
                                            }
                                            if(JobManager.getInstance().getJobs()!=null)
                                                JobManager.getInstance().setSelected(JobManager.getInstance().getJobs().get(job.get_id()));
                                            else IO.log(getClass().getName(), IO.TAG_ERROR, "no jobs were found in the database." );
                                            JobManager.getInstance().viewJob(JobManager.getInstance().getSelected());
                                        } catch (ClassNotFoundException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

                                    btnUpload.setOnAction(event ->
                                    {
                                        if (job == null)
                                        {
                                            IO.logAndAlert("Error " + getClass()
                                                    .getName(), "Job object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        JobManager.getInstance().uploadSigned(job.get_id());
                                    });

                                    btnSign.setOnAction(event ->
                                            JobManager.getInstance().signJob(job, param1 ->
                                            {
                                                //Refresh UI
                                                new Thread(() ->
                                                {
                                                    refreshModel();
                                                    Platform.runLater(() -> refreshView());
                                                }).start();
                                                return null;
                                            }));

                                    btnViewSigned.setOnAction(event ->
                                            viewSignedJob(job));

                                    btnInvoice.setOnAction(event ->
                                            generateJobInvoice(job));

                                    btnEmail.setOnAction(event ->
                                            JobManager.getInstance().emailJobCard(job, null));

                                    btnEmailSigned.setOnAction(event ->
                                            JobManager.getInstance().emailSignedJobCard(job, null));

                                    btnRemove.setOnAction(event ->
                                    {
                                        //197.242.144.30
                                        //Quote quote = getTableView().getItems().get(getIndex());
                                        //getTableView().getItems().remove(quote);
                                        //getTableView().refresh();
                                        //TODO: remove from server
                                        //IO.log(getClass().getName(), IO.TAG_INFO, "successfully removed quote: " + quote.get_id());
                                    });

                                    btnPDF.setOnAction(event -> JobManager.showJobCard(job));

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
        colAction.setMinWidth(900);

        tblJobs.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                JobManager.getInstance().setSelected(tblJobs.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading jobs data model..");

        try
        {
            ResourceManager.getInstance().reloadDataFromServer();
            SupplierManager.getInstance().reloadDataFromServer();
            ClientManager.getInstance().reloadDataFromServer();
            QuoteManager.getInstance().reloadDataFromServer();
            JobManager.getInstance().reloadDataFromServer();
        } catch (ClassNotFoundException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        } catch (IOException e)
        {
            e.printStackTrace();
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        new Thread(() ->
        {
            refreshModel();
            Platform.runLater(() -> refreshView());
        }).start();
    }

    private static void generateJobInvoice(Job job)
    {
        try
        {
            if (job != null)
            {
                if(job.isSigned())
                {
                    if (job.getAssigned_employees() != null)
                    {
                        if (job.getDate_started() > 0 && job.getDate_completed() > 0)
                        {
                            if (job.getDate_completed() >= job.getDate_started())
                            {
                                InvoiceManager.getInstance().generateInvoice(job);
                                ScreenManager.getInstance().showLoadingScreen(param ->
                                {
                                    new Thread(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            try
                                            {
                                                //TODO: show Invoices tab
                                                if (ScreenManager.getInstance()
                                                        .loadScreen(Screens.OPERATIONS
                                                                .getScreen(), getClass()
                                                                .getResource("../views/" + Screens.OPERATIONS
                                                                        .getScreen())))
                                                {
                                                    Platform.runLater(() -> ScreenManager
                                                            .getInstance()
                                                            .setScreen(Screens.OPERATIONS
                                                                    .getScreen()));
                                                } else IO.log(getClass()
                                                        .getName(), IO.TAG_ERROR, "could not load invoice viewer screen.");
                                            } catch (IOException e)
                                            {
                                                IO.log(getClass()
                                                        .getName(), IO.TAG_ERROR, e
                                                        .getMessage());
                                            }
                                        }
                                    }).start();
                                    return null;
                                });
                            } else
                                IO.logAndAlert("Error", "Date started cannot be less than date completed.", IO.TAG_ERROR);
                        } else
                            IO.logAndAlert("Error", "Please ensure that you've entered valid dates then try again.", IO.TAG_ERROR);
                    } else
                        IO.logAndAlert("Error", "Selected job has no assigned employees, please assign employees first then try again.", IO.TAG_ERROR);
                } else
                    IO.logAndAlert("Error", "Selected job has not been SIGNED yet, please sign it first and try again.", IO.TAG_ERROR);
            } else IO.logAndAlert("Error", "Selected job is invalid.", IO.TAG_ERROR);
        } catch (IOException ex)
        {
            IO.log(JobsController.class.getName(), IO.TAG_ERROR, ex.getMessage());
        }
    }

    private static void viewSignedJob(Job job)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid job object passed.", IO.TAG_ERROR);
            return;
        }
        if(!job.isSigned())
        {
            IO.logAndAlert("Error", "Job object passed has not been signed yet.", IO.TAG_ERROR);
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
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));

                    //String filename = String.valueOf(bo.get(property));
                    long start = System.currentTimeMillis();
                    byte[] file = RemoteComms.sendFileRequest("/api/job/signed/" + job.get_id(), headers);

                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        //String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                        String local_filename = job.get_id() + "_signed.pdf";
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
                                IO.log(JobsController.class.getName(), IO.TAG_WARN, "could not delete file ["+f.getAbsolutePath()+"]");
                                //get new filename
                                while((f=new File("out/"+local_filename)).exists())
                                {
                                    local_filename = job.get_id() + "_signed." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File("out/" + local_filename));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded signed job [" + job.get_id()
                                +"] to path [out/" + local_filename + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen("out/" + local_filename);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader", "File '" + job
                                .get_id() + "_signed.pdf' could not be downloaded because the active session has expired.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("Error", "Could not download signed job card for [#"+job.getJob_number()+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    /*private static void viewSignedJob(Job job)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Invalid job object passed.", IO.TAG_ERROR);
            return;
        }
        if(job.getSigned_job()==null)
        {
            IO.logAndAlert("Error", "could not find signed job card for selected job [#"+job.getJob_number()+"].", IO.TAG_ERROR);
            return;
        }

        try
        {
            //String filename = String.valueOf(bo.get(property));
            long start = System.currentTimeMillis();

            byte[] file = Base64.getDecoder().decode(job.getSigned_job());
            if (file != null)
            {
                //long ellapsed = System.currentTimeMillis()-start;
                //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                PDFViewer pdfViewer = PDFViewer.getInstance();
                pdfViewer.setVisible(true);

                //String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                String local_filename = job.get_id() + "_signed.pdf";
                if(new File("out/"+local_filename).exists())
                    Files.delete(new File("out/"+local_filename).toPath());
                FileOutputStream out = new FileOutputStream(new File("out/"+local_filename));
                out.write(file, 0, file.length);
                out.flush();
                out.close();

                IO.log(JobsController.class.getName(), IO.TAG_INFO, "serialized signed job ["+job.get_id()+"] to path [out/"+local_filename+"], size: "+file.length+" bytes. launching PDF viewer.");
                //pdfViewer.doOpen("bin/" + filename + ".bin");
                pdfViewer.doOpen("out/"+local_filename);
                //Clean up
                Files.delete(Paths.get("out/"+local_filename));
            } else
            {
                IO.logAndAlert("File Downloader", "File '" + job.get_id() + "_signed.pdf' could not be downloaded because the active session has expired.", IO.TAG_ERROR);
            }
        }catch (IOException e)
        {
            IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
        }
    }*/



    public static RadialMenuItem[] getContextMenu()
    {
        RadialMenuItem[] context_menu = new RadialMenuItem[7];

        //View Job Menu item
        context_menu[0] = new RadialMenuItemCustom(30, "View Job", null, null, event ->
                JobManager.getInstance().viewJob(JobManager.getInstance().getSelected()));

        //Sign Job menu item
        context_menu[1] = new RadialMenuItemCustom(30, "Sign Job", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.getInstance().signJob(JobManager.getInstance().getSelected(), null);
        });

        //View signed Job menu item
        context_menu[2] = new RadialMenuItemCustom(30, "View Signed Job", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            viewSignedJob(JobManager.getInstance().getSelected());
        });

        //Generate Job Invoice menu item
        context_menu[3] = new RadialMenuItemCustom(30, "Generate Invoice", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            viewSignedJob(JobManager.getInstance().getSelected());
        });

        //eMail Job menu item
        context_menu[4] = new RadialMenuItemCustom(30, "e-Mail Job Card", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.getInstance().emailJobCard(JobManager.getInstance().getSelected(), null);
        });

        //eMail Signed Job Card menu item
        context_menu[5] = new RadialMenuItemCustom(30, "e-Mail SIGNED Job Card", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.getInstance().emailSignedJobCard(JobManager.getInstance().getSelected(), null);
        });

        //View Job PDF menu item
        context_menu[6] = new RadialMenuItemCustom(30, "View Job Card [PDF]", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.showJobCard(JobManager.getInstance().getSelected());
        });
        return context_menu;
    }
}
