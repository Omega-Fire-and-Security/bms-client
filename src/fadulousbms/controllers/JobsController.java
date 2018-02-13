/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.FadulousBMS;
import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
            colDateAssigned, colDateStarted, colDateEnded, colCreator, colExtra, colStatus, colAction;
    @FXML
    private Tab jobsTab;
    public static final String TAB_ID = "jobsTab";

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading jobs view..");

        if (JobManager.getInstance().getDataset() == null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No jobs were found in the database.", IO.TAG_WARN);
            return;
        }
        if (JobManager.getInstance().getDataset().values() == null)
        {
            IO.logAndAlert(getClass().getSimpleName(), "No jobs were found in the database.", IO.TAG_WARN);
            return;
        }
        colJobNum.setMinWidth(100);
        colJobNum.setCellValueFactory(new PropertyValueFactory<>("object_number"));
        //CustomTableViewControls.makeEditableTableColumn(colRequest, TextFieldTableCell.forTableColumn(), 215, "job_description", "/jobs");
        colRequest.setCellValueFactory(new PropertyValueFactory<>("job_description"));
        colClient.setCellValueFactory(new PropertyValueFactory<>("client_name"));
        colSitename.setCellValueFactory(new PropertyValueFactory<>("sitename"));
        colContactPerson.setCellValueFactory(new PropertyValueFactory<>("contact_person"));
        //TODO: contact_personProperty
        CustomTableViewControls.makeDynamicToggleButtonTableColumn(colStatus,90, "status", new String[]{"0","PENDING","1","APPROVED"}, false,"/jobs");
        CustomTableViewControls
                .makeLabelledDatePickerTableColumn(colPlannedStartDate, "planned_start_date");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateGenerated, "date_logged", false);
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateAssigned, "date_assigned");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateStarted, "date_started");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(colDateEnded, "date_completed");
        colCreator.setCellValueFactory(new PropertyValueFactory<>("creator_name"));
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
        lst_jobs.addAll(JobManager.getInstance().getDataset().values());
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
                            final Button btnApprove = new Button("Approve");
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
                                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                                btnView.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnView.getStyleClass().add("btnDefault");
                                btnView.setMinWidth(100);
                                btnView.setMinHeight(35);
                                HBox.setHgrow(btnView, Priority.ALWAYS);

                                btnUpload.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnUpload.getStyleClass().add("btnDefault");
                                btnUpload.setMinWidth(130);
                                btnUpload.setMinHeight(35);
                                HBox.setHgrow(btnUpload, Priority.ALWAYS);

                                //btnSign.getStylesheets().add(fadulousbms.FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                                //btnSign.getStyleClass().add("btnDefault");
                                //btnSign.setStyle("-fx-border-radius: 20;");
                                btnApprove.setMinWidth(100);
                                btnApprove.setMinHeight(35);
                                btnApprove.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                if(SessionManager.getInstance().getActiveEmployee()!=null)
                                {
                                    //disable [Approve] button if not authorised
                                    if (SessionManager.getInstance().getActiveEmployee().getAccessLevel()>=AccessLevels.SUPERUSER.getLevel())
                                    {
                                        btnApprove.getStyleClass().add("btnAdd");
                                        btnApprove.setDisable(false);
                                    } else {
                                        btnApprove.getStyleClass().add("btnDisabled");
                                        btnApprove.setDisable(true);
                                    }
                                } else IO.logAndAlert("Error", "No valid active employee session found, please log in.", IO.TAG_ERROR);

                                HBox.setHgrow(btnApprove, Priority.ALWAYS);

                                btnViewSigned.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnViewSigned.getStyleClass().add("btnDefault");
                                btnViewSigned.setMinWidth(130);
                                btnViewSigned.setMinHeight(35);
                                HBox.setHgrow(btnViewSigned, Priority.ALWAYS);

                                btnInvoice.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnInvoice.getStyleClass().add("btnDefault");
                                btnInvoice.setMinWidth(100);
                                btnInvoice.setMinHeight(35);
                                HBox.setHgrow(btnInvoice, Priority.ALWAYS);

                                btnPDF.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnPDF.getStyleClass().add("btnDefault");
                                btnPDF.setMinWidth(100);
                                btnPDF.setMinHeight(35);
                                HBox.setHgrow(btnPDF, Priority.ALWAYS);

                                btnEmail.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnEmail.setMinWidth(100);
                                btnEmail.setMinHeight(35);
                                HBox.setHgrow(btnEmail, Priority.ALWAYS);
                                if(!empty)
                                {
                                    if (getTableView().getItems().get(getIndex()).getStatus()>=BusinessObject.STATUS_APPROVED)
                                    {
                                        btnEmail.getStyleClass().add("btnDefault");
                                        btnEmail.setDisable(false);
                                    } else
                                    {
                                        btnEmail.getStyleClass().add("btnDisabled");
                                        btnEmail.setDisable(true);
                                    }
                                }

                                btnEmailSigned.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnEmailSigned.setMinWidth(100);
                                btnEmailSigned.setMinHeight(35);
                                HBox.setHgrow(btnEmailSigned, Priority.ALWAYS);
                                if(!empty)
                                {
                                    if (getTableView().getItems().get(getIndex()).getStatus()>=BusinessObject.STATUS_APPROVED)
                                    {
                                        btnEmailSigned.getStyleClass().add("btnAdd");
                                        btnEmailSigned.setDisable(false);
                                    }
                                    else
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
                                    HBox hBox = new HBox(btnView, btnUpload, btnApprove, btnViewSigned, btnInvoice, btnPDF, btnEmail, btnEmailSigned, btnRemove);
                                    hBox.setMaxWidth(Double.MAX_VALUE);
                                    HBox.setHgrow(hBox, Priority.ALWAYS);
                                    Job job = getTableView().getItems().get(getIndex());

                                    btnView.setOnAction(event ->
                                    {
                                        JobManager.getInstance().initialize();
                                        if (job == null)
                                        {
                                            IO.logAndAlert("Error " + getClass().getName(), "Job object is not set", IO.TAG_ERROR);
                                            return;
                                        }
                                        //set selected Job
                                        JobManager.getInstance().setSelected(JobManager.getInstance().getDataset().get(job.get_id()));
                                        viewJob((Job)JobManager.getInstance().getSelected());
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

                                    btnApprove.setOnAction(event ->
                                            JobManager.approveJob(job, param1 ->
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
                                            generateQuoteInvoice(job));

                                    btnEmail.setOnAction(event ->
                                    {
                                        try
                                        {
                                            if(job!=null)
                                                JobManager.getInstance().emailBusinessObject(job, PDF.createJobCardPdf(job), null);
                                            else IO.logAndAlert("Error", "Job object is invalid.", IO.TAG_ERROR);
                                        } catch (IOException e)
                                        {
                                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                                        }
                                    });

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
        colAction.setMinWidth(1000);

        tblJobs.getSelectionModel().selectedIndexProperty().addListener((observable, oldValue, newValue) ->
                JobManager.getInstance().setSelected(tblJobs.getSelectionModel().getSelectedItem()));
    }

    @Override
    public void refreshModel()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading jobs data model..");

        //initialize dependencies
        ResourceManager.getInstance().initialize();
        SupplierManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        QuoteManager.getInstance().initialize();
        JobManager.getInstance().initialize();
        //synchronise model data set
        JobManager.getInstance().initialize();
    }

    @Override
    public void forceSynchronise()
    {
        JobManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) 
    {
        OperationsController.registerTabController(jobsTab.getId(),this);
        new Thread(() ->
        {
            ResourceManager.getInstance().initialize();
            SupplierManager.getInstance().initialize();
            ClientManager.getInstance().initialize();
            QuoteManager.getInstance().initialize();
            JobManager.getInstance().initialize();

            Platform.runLater(() -> refreshView());
        }).start();
    }

    private static void generateQuoteInvoice(Job job)
    {
        generateInvoice(job);//, cbx_quote_revision.getValue().get_id()
    }

    private static void generateInvoice(Job job)
    {
        if (job != null)
        {
            if(job.getStatus()>=BusinessObject.STATUS_APPROVED)
            {
                if (job.getAssigned_employees() != null)
                {
                    if (job.getDate_started() > 0 && job.getDate_completed() > 0)
                    {
                        if (job.getDate_completed() >= job.getDate_started())
                        {
                            Stage stage = new Stage();
                            stage.setTitle("Select Quote["+job.getQuote().get_id()+"] Revisions");
                            stage.setResizable(false);

                            VBox container = new VBox();

                            TextField txt_receivable = new TextField();
                            HBox hbx_receivable = new HBox(new Label("Amount Receivable: "), txt_receivable);

                            container.getChildren().add(hbx_receivable);
                            container.getChildren().add(new Label("Choose Quote Revisions"));

                            HashMap<String, Quote> quote_revs = new HashMap<>();
                            for(Quote quote_rev: job.getQuote().getSortedSiblings("revision"))
                            {
                                CheckBox checkBox = new CheckBox("Revision "+quote_rev.getRevision());
                                checkBox.selectedProperty().addListener((observable, oldValue, newValue) ->
                                {
                                    //add Quote to map on checkbox check, remove otherwise
                                    if(newValue)
                                        quote_revs.put(quote_rev.get_id(), quote_rev);
                                    else quote_revs.remove(quote_rev.get_id());
                                });
                                container.setSpacing(10);
                                container.getChildren().add(checkBox);
                            }
                            Button btnSubmit = new Button("Submit");
                            btnSubmit.setOnAction(event1 ->
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
                                                String str_quote_revs="";
                                                for(Quote quote: quote_revs.values())
                                                    str_quote_revs+=(str_quote_revs==""?quote.getRevision():";"+quote.getRevision());//comma separated revision numbers
                                                InvoiceManager.getInstance().createInvoice(job, str_quote_revs, Double.parseDouble(txt_receivable.getText()), callback -> null);

                                                //TODO: show Invoices tab
                                                if (ScreenManager.getInstance()
                                                        .loadScreen(Screens.OPERATIONS
                                                                .getScreen(), FadulousBMS.class.getResource("views/" + Screens.OPERATIONS
                                                                .getScreen())))
                                                {
                                                    Platform.runLater(() -> ScreenManager
                                                            .getInstance()
                                                            .setScreen(Screens.OPERATIONS
                                                                    .getScreen()));
                                                } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load invoices list screen.");
                                            } catch (NumberFormatException e)
                                            {
                                                IO.log(JobsController.class.getName(), IO.TAG_ERROR, "Invalid amount receivable: " + e.getMessage());
                                            } catch (IOException e)
                                            {
                                                IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                                            }
                                        }
                                    }).start();
                                    return null;
                                });
                            });
                            container.getChildren().add(btnSubmit);
                            stage.setScene(new Scene(container));
                            stage.show();
                            stage.centerOnScreen();
                        } else
                            IO.logAndAlert("Error", "Date started cannot be less than date completed.", IO.TAG_ERROR);
                    } else
                        IO.logAndAlert("Error", "Please ensure that you've entered valid dates then try again.", IO.TAG_ERROR);
                } else
                    IO.logAndAlert("Error", "Selected job has no assigned employees, please assign employees first then try again.", IO.TAG_ERROR);
            } else
                IO.logAndAlert("Error", "Selected job has not been SIGNED yet, please sign it first and try again.", IO.TAG_ERROR);
        } else IO.logAndAlert("Error", "Selected job is invalid.", IO.TAG_ERROR);
    }

    /**
     * Method to view Job info in editable form.
     * @param job Job object to exported to a PDF document.
     */
    public static void viewJob(Job job)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
            return;
        }

        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    JobManager.getInstance().setSelected(job);
                    try
                    {
                        if(ScreenManager.getInstance().loadScreen(Screens.VIEW_JOB.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.VIEW_JOB.getScreen())))
                        {
                            Platform.runLater(() -> ScreenManager.getInstance().setScreen(Screens.VIEW_JOB.getScreen()));
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load jobs viewer screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }

    private static void viewSignedJob(Job job)
    {
        if(job==null)
        {
            IO.logAndAlert("Error", "Selected Job object is invalid.", IO.TAG_ERROR);
            return;
        }

        //Validate session - also done on the backend
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
                    byte[] file = RemoteComms.sendFileRequest("/api/job/signed/" + job.get_id(), headers);

                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        //IO.log(JobsController.class.getName(), IO.TAG_INFO, "File ["+job.get_id()+".pdf] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                        PDFViewer pdfViewer = PDFViewer.getInstance();
                        pdfViewer.setVisible(true);

                        String local_filename = job.get_id() + "_signed.pdf";
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
                                    local_path = "out/pdf/"+job.get_id() + "_signed." + i + ".pdf";
                                    i++;
                                }
                            }
                        }

                        FileOutputStream out = new FileOutputStream(new File(local_path));
                        out.write(file, 0, file.length);
                        out.flush();
                        out.close();

                        IO.log(JobsController.class.getName(), IO.TAG_INFO, "downloaded signed job [" + job.get_id()
                                +"] to path [" + local_path + "], size: " + file.length + " bytes, in "+ellapsed
                                +" msec. launching PDF viewer.");

                        pdfViewer.doOpen(local_path);
                    }
                    else
                    {
                        IO.logAndAlert("File Downloader", "File '" + job
                                .get_id() + "_signed.pdf' could not be downloaded because the active session has expired.", IO.TAG_ERROR);
                    }
                } catch (IOException e)
                {
                    IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
                    IO.logAndAlert("Error", "Could not download signed job card for [#"+job.getObject_number()+"]: " + e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static RadialMenuItem[] getContextMenu()
    {
        RadialMenuItem[] context_menu = new RadialMenuItem[7];

        //View Job Menu item
        context_menu[0] = new RadialMenuItemCustom(30, "View Job", null, null, event ->
                viewJob((Job)JobManager.getInstance().getSelected()));

        //Sign Job menu item
        context_menu[1] = new RadialMenuItemCustom(30, "Sign Job", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.approveJob((Job)JobManager.getInstance().getSelected(), null);
        });

        //View signed Job menu item
        context_menu[2] = new RadialMenuItemCustom(30, "View Signed Job", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            viewSignedJob((Job) JobManager.getInstance().getSelected());
        });

        //Generate Job Invoice menu item
        context_menu[3] = new RadialMenuItemCustom(30, "Generate Invoice", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            viewSignedJob((Job) JobManager.getInstance().getSelected());
        });

        //eMail Job menu item
        context_menu[4] = new RadialMenuItemCustom(30, "e-Mail Job Card", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            try
            {
                JobManager.getInstance().emailBusinessObject(JobManager.getInstance().getSelected(), PDF.createJobCardPdf((Job) JobManager.getInstance().getSelected()), null);
            } catch (IOException e)
            {
                IO.log(JobsController.class.getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //eMail Signed Job Card menu item
        context_menu[5] = new RadialMenuItemCustom(30, "e-Mail SIGNED Job Card", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.getInstance().emailSignedJobCard((Job) JobManager.getInstance().getSelected(), null);
        });

        //View Job PDF menu item
        context_menu[6] = new RadialMenuItemCustom(30, "View Job Card [PDF]", null, null, event ->
        {
            if(JobManager.getInstance().getSelected()==null)
            {
                IO.logAndAlert("Error", "Selected Job object is not set.", IO.TAG_ERROR);
                return;
            }
            JobManager.showJobCard((Job) JobManager.getInstance().getSelected());
        });
        return context_menu;
    }
}
