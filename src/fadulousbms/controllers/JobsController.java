/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.BMSPreloader;
import fadulousbms.FadulousBMS;
import fadulousbms.auxilary.*;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.TextFields;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.*;
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

    protected Resource selected_material = null;
    protected ResourceType selected_material_type = null;

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
                            final Button btnCalendar = new Button("View Tasks");
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

                                btnView.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                btnCalendar.getStyleClass().add("btnDefault");
                                btnCalendar.setMinWidth(100);
                                btnCalendar.setMinHeight(35);
                                HBox.setHgrow(btnCalendar, Priority.ALWAYS);

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
                                    HBox hBox = new HBox(btnView, btnCalendar, btnUpload, btnApprove, btnViewSigned, btnInvoice, btnPDF, btnEmail, btnEmailSigned, btnRemove);
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

                                    btnCalendar.setOnAction(event -> showProjectOnCalendar(getTableView().getItems().get(getIndex()), btnCalendar));

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
                                                        refreshModel(cb->{Platform.runLater(() -> refreshView());return null;})).start();
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
    public void refreshModel(Callback callback)
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading jobs data model..");

        //initialize dependencies
        ResourceManager.getInstance().initialize();
        SupplierManager.getInstance().initialize();
        ClientManager.getInstance().initialize();
        QuoteManager.getInstance().initialize();
        JobManager.getInstance().initialize();
        TaskManager.getInstance().initialize();
        //synchronise model data set
        JobManager.getInstance().initialize();

        //execute callback
        if(callback!=null)
            callback.call(null);
    }

    @Override
    public void forceSynchronise()
    {
        JobManager.getInstance().forceSynchronise();
        Platform.runLater(() -> refreshView());
    }

    public void showProjectOnCalendar(Job job, Node parent)
    {
        if(job==null)
        {
            IO.logAndAlert("Invalid Job", "Selected Job is invalid.", IO.TAG_ERROR);
            return;
        }
        if(job.getQuote()==null)
        {
            IO.logAndAlert("Invalid Job", "Selected Job has no valid Quote associated with it.", IO.TAG_ERROR);
            return;
        }

        //LocalDateTime date = LocalDateTime.ofEpochSecond(System.currentTimeMillis()/1000, 0, ZoneOffset.of(ZoneId.systemDefault().getId()));
        //int days = date.toLocalDate().lengthOfMonth();
        int year = 2018, month = 3, day = 1;

        YearMonth yearMonth = YearMonth.of(year, month);
        DayOfWeek dayOfWeek = yearMonth.atDay(day).getDayOfWeek();
        IO.log(getClass().getName(), IO.TAG_INFO, "Num days: " + yearMonth.lengthOfMonth() + ", day of week: " + dayOfWeek.name() + "[" + dayOfWeek.getValue()+"]");

        TabPane tabPane = new TabPane();
        //tabPane.getStylesheets().add(FadulousBMS.class.getResource("styles/tabs.css").toExternalForm());
        //tabPane.getStyleClass().add("projectCalendarTabs");

        Tab tasks_tab = new Tab("All Project Tasks");
        Tab calendar_tab = new Tab("Project Calendar");

        tabPane.getTabs().addAll(tasks_tab, calendar_tab);

        TableView<Task> tblTasks = new TableView<>();
        tblTasks.setEditable(true);

        TableColumn col_description = new TableColumn("Description");
        CustomTableViewControls.makeEditableTableColumn(col_description, TextFieldTableCell.forTableColumn(), 120, "description", "/tasks");

        TableColumn col_location = new TableColumn("Location");
        CustomTableViewControls.makeEditableTableColumn(col_location, TextFieldTableCell.forTableColumn(), 100, "location", "/tasks");

        TableColumn col_date_scheduled = new TableColumn("Date Scheduled");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(col_date_scheduled, "date_scheduled", true);

        TableColumn col_date_assigned = new TableColumn("Date Assigned");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(col_date_assigned, "date_assigned", false);

        TableColumn col_date_started = new TableColumn("Date Started");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(col_date_started, "date_started", true);

        TableColumn col_date_completed = new TableColumn("Date Completed");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(col_date_completed, "date_completed", true);

        TableColumn col_date_logged = new TableColumn("Date Logged");
        CustomTableViewControls.makeLabelledDatePickerTableColumn(col_date_logged, "date_logged", false);

        TableColumn col_status = new TableColumn("Status");
        col_status.setCellValueFactory(new PropertyValueFactory<>("status"));

        tblTasks.getColumns().addAll(col_description, col_location, col_date_scheduled, col_date_assigned, col_date_started, col_date_completed, col_date_logged, col_status);

        HashMap<String, Task> job_tasks = job.getTasks();
        if(job_tasks!=null)
        {
            tblTasks.setItems(FXCollections.observableArrayList(job_tasks.values()));
            tblTasks.refresh();
        } else IO.log(getClass().getName(), IO.TAG_WARN, "no tasks were found in the database for job #" + job.getObject_number());

        tasks_tab.setContent(tblTasks);

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setStyle("-fx-background-color: #fff");
        //page.setGridLinesVisible(true);
        page.setHgap(2);
        page.setVgap(2);

        int row=0;

        //render month
        Label lbl_month = new Label(yearMonth.getMonth().name());
        GridPane.setColumnSpan(lbl_month, GridPane.REMAINING);
        page.add(lbl_month, 3, row);

        row++;
        //render weekday names
        for(int i=0;i<DayOfWeek.values().length;i++)
            page.add(new Label(DayOfWeek.of(i+1).name()), i, row);
        row++;
        int col = dayOfWeek.getValue()-1;
        //render days of the month
        for(int current_day=1;current_day<yearMonth.lengthOfMonth();current_day++)
        {
            BorderPane calendarCell = new CalendarCell(year, month, current_day);//new BorderPane();
            calendarCell.setPrefWidth(120);
            calendarCell.setPrefHeight(90);
            calendarCell.setCenter(new Label(String.valueOf(current_day)));

            //count number of tasks on this day
            int task_count=0;
            for(Task task: job.getTasks().values())
                if(task.getDate_scheduled() == ((CalendarCell)calendarCell).getDate().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000)
                    task_count++;

            if(task_count>0)
                calendarCell.setBottom(new Label("*" + task_count + " task" + (task_count>1?"s":"")));

            calendarCell.getStylesheets().add(FadulousBMS.class.getResource("styles/home.css").toExternalForm());
            calendarCell.getStyleClass().add("calendarButton");

            if(LocalDate.now().equals(LocalDate.of(year, month, current_day)))
                calendarCell.getStyleClass().add("calendarButtonActive");

            calendarCell.setOnMouseClicked(evt->
            {
                //new SimpleDateFormat("yyyy-MM-dd").format(((CalendarCell)calendarCell).getDate())
                TableView<Task> tblDayTasks = new TableView<>();
                tblDayTasks.setPrefWidth(420);
                tblDayTasks.setPrefHeight(200);
                tblDayTasks.setTableMenuButtonVisible(true);
                tblDayTasks.setEditable(true);

                TableColumn col_day_task_description = new TableColumn("Description");
                CustomTableViewControls.makeEditableTableColumn(col_day_task_description, TextFieldTableCell.forTableColumn(), 80, "description", "/tasks");

                TableColumn col_day_task_location = new TableColumn("Location");
                CustomTableViewControls.makeEditableTableColumn(col_day_task_location, TextFieldTableCell.forTableColumn(), 50, "location", "/tasks");

                TableColumn col_day_task_date_scheduled = new TableColumn("Date Scheduled");
                col_day_task_date_scheduled.setVisible(false);
                col_day_task_date_scheduled.setPrefWidth(70);
                CustomTableViewControls.makeLabelledDatePickerTableColumn(col_day_task_date_scheduled, "date_scheduled", true);

                TableColumn col_day_task_date_assigned = new TableColumn("Date Assigned");
                col_day_task_date_scheduled.setVisible(false);
                col_day_task_date_assigned.setPrefWidth(70);
                CustomTableViewControls.makeLabelledDatePickerTableColumn(col_day_task_date_assigned, "date_assigned", false);

                TableColumn col_day_task_date_started = new TableColumn("Date Started");
                col_day_task_date_started.setPrefWidth(70);
                CustomTableViewControls.makeLabelledDatePickerTableColumn(col_day_task_date_started, "date_started", true);

                TableColumn col_day_task_date_completed = new TableColumn("Date Completed");
                col_day_task_date_completed.setPrefWidth(70);
                CustomTableViewControls.makeLabelledDatePickerTableColumn(col_day_task_date_completed, "date_completed", true);

                TableColumn col_day_task_date_logged = new TableColumn("Date Logged");
                col_day_task_date_logged.setVisible(false);
                col_day_task_date_logged.setPrefWidth(70);
                CustomTableViewControls.makeLabelledDatePickerTableColumn(col_day_task_date_logged, "date_logged", false);

                TableColumn col_day_task_status = new TableColumn("Status");
                col_day_task_status.setPrefWidth(50);
                col_day_task_status.setCellValueFactory(new PropertyValueFactory<>("status"));

                TableColumn col_action = new TableColumn("Action");
                Callback<TableColumn<Task, String>, TableCell<Task, String>> cellFactory
                        =
                        new Callback<TableColumn<Task, String>, TableCell<Task, String>>()
                        {
                            @Override
                            public TableCell call(final TableColumn<Task, String> param)
                            {
                                final TableCell<Task, String> cell = new TableCell<Task, String>()
                                {
                                    final Button btnNewMat = new Button("New Material");
                                    final Button btnShowMat = new Button("Show Materials");
                                    final Button btnRemove = new Button("Delete");

                                    @Override
                                    public void updateItem(String item, boolean empty)
                                    {
                                        super.updateItem(item, empty);
                                        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                                        btnNewMat.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                        btnNewMat.getStyleClass().add("btnAdd");
                                        btnNewMat.setMinWidth(100);
                                        btnNewMat.setMinHeight(35);
                                        HBox.setHgrow(btnNewMat, Priority.ALWAYS);

                                        btnShowMat.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                                        btnShowMat.getStyleClass().add("btnDefault");
                                        btnShowMat.setMinWidth(100);
                                        btnShowMat.setMinHeight(35);
                                        HBox.setHgrow(btnShowMat, Priority.ALWAYS);

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
                                            HBox hBox = new HBox(btnNewMat, btnShowMat, btnRemove);

                                            btnNewMat.setOnAction(evt->newTaskMaterial(getTableView().getItems().get(getIndex()), btnNewMat));

                                            btnShowMat.setOnAction(evt->showTaskItems(getTableView().getItems().get(getIndex())));

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
                col_action.setCellFactory(cellFactory);

                tblDayTasks.getColumns().addAll(col_day_task_description, col_day_task_location, col_day_task_date_scheduled, col_day_task_date_assigned, col_day_task_date_started, col_day_task_date_completed, col_day_task_date_logged, col_day_task_status, col_action);
                HashMap<String, Task> day_tasks = new HashMap<>();
                if(job_tasks!=null)
                    for(Task task: job_tasks.values())
                    {
                        //System.out.print("######### comparing " + new Date(task.getDate_scheduled()) + " with " + ((CalendarCell) calendarCell).getDate());
                        //System.out.println("\n\t######### comparing " + task.getDate_scheduled() + " with " + ((((CalendarCell) calendarCell).getDate()).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000));
                        //if (new Date(task.getDate_scheduled()).compareTo(((CalendarCell) calendarCell).getDate()))
                        if (task.getDate_scheduled() == (((CalendarCell) calendarCell).getDate()).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000)
                            day_tasks.put(task.get_id(), task);
                    }

                //populate day tasks table
                tblDayTasks.setItems(FXCollections.observableArrayList(day_tasks.values()));
                tblDayTasks.refresh();

                Button btnNewTask = new Button("New Task for Job #" + job.getObject_number() + " on " + ((CalendarCell)calendarCell).getDate());
                btnNewTask.getStylesheets().add(FadulousBMS.class.getResource("styles/home.css").toExternalForm());
                btnNewTask.getStyleClass().add("btnAdd");
                btnNewTask.setMinWidth(90);
                btnNewTask.setMinHeight(45);
                btnNewTask.setAlignment(Pos.CENTER);

                btnNewTask.setOnAction(ev->
                {
                    TextField txt_description = new TextField("");
                    txt_description.setMinWidth(120);
                    txt_description.setPromptText("Summary of the task to be done");
                    Label lbl_des = new Label("Task Description*: ");
                    lbl_des.setMinWidth(160);

                    TextField txt_location = new TextField(job.getQuote().getSitename());
                    txt_location.setMinWidth(120);
                    txt_location.setPromptText("Location the task is to be done at");
                    Label lbl_loc = new Label("Location*: ");
                    lbl_loc.setMinWidth(160);

                    DatePicker date_scheduled = new DatePicker(((CalendarCell) calendarCell).getDate());
                    date_scheduled.setMinWidth(200);
                    date_scheduled.setMaxWidth(Double.MAX_VALUE);

                    Button submit = new Button("Create Task");

                    submit.setOnAction(event ->
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

                        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
                        if(!Validators.isValidNode(txt_description, txt_description.getText(), 1, ".+"))
                        {
                            txt_description.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                            return;
                        }
                        if(!Validators.isValidNode(txt_location, txt_location.getText(), 1, ".+"))
                        {
                            txt_location.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                            return;
                        }
                        if(!Validators.isValidNode(date_scheduled, (date_scheduled.getValue()==null?"":String.valueOf(date_scheduled.getValue())), "^.*(?=.{1,}).*"))
                            return;

                        Task task = new Task();
                        task.setJob_id(job.get_id());
                        task.setDescription(txt_description.getText());
                        task.setLocation(txt_location.getText());
                        task.setStatus(0);
                        task.setDate_scheduled(date_scheduled.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000);
                        task.setCreator(SessionManager.getInstance().getActive().getUsr());

                        //create Task on database
                        try
                        {
                            TaskManager.getInstance().createBusinessObject(task, new Callback()
                            {
                                @Override
                                public Object call(Object task_id)
                                {
                                    if(task_id!=null)
                                    {
                                        //TaskManager.getInstance().forceSynchronise();
                                        Task new_task = TaskManager.getInstance().getDataset().get(task_id);
                                        if(new_task!=null)
                                            TaskManager.getInstance().setSelected(new_task);
                                        else
                                        {
                                            IO.log(getClass().getName(), IO.TAG_WARN, "could not update selected task - not found in data-set.");
                                            //fallback to manually setting the _id of the created Task then make it the selected Task
                                            task.set_id((String) task_id);
                                            TaskManager.getInstance().setSelected(task);
                                        }
                                        //refresh day tasks table
                                        //TODO: fix this - will keep making new day task maps every time a new task is created
                                        //HashMap day_tasks = new HashMap<>();
                                        /*if(job!=null)
                                            if(job.getTasks()!=null)
                                                for(Task task: job.getTasks().values())
                                                    if (task.getDate_scheduled() == (((CalendarCell) calendarCell).getDate()).atStartOfDay(ZoneId.systemDefault()).toEpochSecond()*1000)
                                                        day_tasks.put(task.get_id(), task);*/

                                        day_tasks.put(TaskManager.getInstance().getSelected().get_id(), (Task) TaskManager.getInstance().getSelected());
                                        //populate day tasks table
                                        //tblDayTasks.setItems(FXCollections.observableArrayList(day_tasks.values()));
                                        tblDayTasks.getItems().add((Task) TaskManager.getInstance().getSelected());
                                        tblDayTasks.refresh();

                                        IO.logAndAlert("Success", "Successfully created task: " + task.getDescription(), IO.TAG_INFO);
                                    } else IO.logAndAlert("Error", "Could not create new task.", IO.TAG_ERROR);
                                    return null;
                                }
                            });
                        } catch (IOException e)
                        {
                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        }
                    });

                    GridPane new_task_grid = new GridPane();
                    new_task_grid.setAlignment(Pos.CENTER_LEFT);
                    new_task_grid.setHgap(10);
                    new_task_grid.setVgap(10);

                    //description
                    new_task_grid.add(lbl_des, 0, 0);
                    new_task_grid.add(txt_description, 1, 0);

                    //location
                    new_task_grid.add(lbl_loc, 0, 1);
                    new_task_grid.add(txt_location, 1, 1);

                    //date scheduled
                    new_task_grid.add(new Label("Date Scheduled: "), 0, 2);
                    new_task_grid.add(date_scheduled, 1, 2);

                    new_task_grid.add(submit, 1, 3);

                    PopOver new_task = new PopOver(new_task_grid);
                    new_task.setMinWidth(200);
                    new_task.setMinHeight(300);
                    new_task.show(calendarCell);
                });

                BorderPane btn_container = new BorderPane();
                btn_container.setCenter(btnNewTask);

                PopOver tasks = new PopOver(new VBox(new Label(day_tasks.size() + " Tasks for Job #"+ job.getObject_number() + " on " + ((CalendarCell)calendarCell).getDate()), tblDayTasks, btn_container));
                tasks.setTitle(day_tasks.size() + " Tasks for Job #"+ job.getObject_number() + " on " + ((CalendarCell)calendarCell).getDate());
                tasks.setDetached(true);
                tasks.setMinWidth(200);
                tasks.setMinHeight(300);
                tasks.show(calendarCell);
            });

            page.add(calendarCell, col, row);

            if(col+1>6)
            {
                col = 0;//reset to beginning of week
                row++;//go to next row
            } else col++;//go to next day of week
        }

        calendar_tab.setContent(page);

        PopOver popover = new PopOver(tabPane);
        popover.setTitle("Project Calendar");
        if(job!=null)
            if(job.getQuote()!=null)
                popover.setTitle("Project Calendar for job #" + job.getObject_number() + " at " + job.getQuote().getSitename());
        popover.setDetached(true);
        popover.show(tblJobs);
    }

    private void showTaskItems(Task task)
    {
        if(task==null)
        {
            IO.logAndAlert("Error", "Selected task is invalid", IO.TAG_ERROR);
            return;
        }
        TableView<TaskItem> tblTaskItems = new TableView<>();
        tblTaskItems.setPrefWidth(450);
        tblTaskItems.setPrefHeight(180);
        tblTaskItems.setTableMenuButtonVisible(true);
        tblTaskItems.setEditable(true);

        TableColumn col_description = new TableColumn("Description");
        col_description.setCellValueFactory(new PropertyValueFactory<>("description"));

        TableColumn col_serial = new TableColumn("Serial/Model");
        CustomTableViewControls.makeEditableTableColumn(col_serial, TextFieldTableCell.forTableColumn(), 50, "serial", "/tasks/resources");

        TableColumn col_cat = new TableColumn("Category");
        CustomTableViewControls.makeEditableTableColumn(col_cat, TextFieldTableCell.forTableColumn(), 50, "category", "/tasks/resources");

        TableColumn col_item_cost = new TableColumn("Cost");
        col_item_cost.setPrefWidth(60);
        CustomTableViewControls.makeEditableTableColumn(col_item_cost, TextFieldTableCell.forTableColumn(), 50, "unit_cost", "/tasks/resources");

        TableColumn col_item_qty = new TableColumn("Qty");
        col_item_qty.setPrefWidth(50);
        CustomTableViewControls.makeEditableTableColumn(col_item_qty, TextFieldTableCell.forTableColumn(), 50, "quantity", "/tasks/resources");

        TableColumn col_unit = new TableColumn("Unit");
        col_unit.setPrefWidth(50);
        col_unit.setVisible(false);
        col_unit.setCellValueFactory(new PropertyValueFactory<>("unit"));

        TableColumn col_rate = new TableColumn("Rate");
        col_rate.setPrefWidth(50);
        col_rate.setVisible(false);
        col_rate.setCellValueFactory(new PropertyValueFactory<>("rate"));

        TableColumn col_total = new TableColumn("Total");
        col_total.setPrefWidth(80);
        col_total.setCellValueFactory(new PropertyValueFactory<>("total"));

        TableColumn col_date_logged = new TableColumn("Date Logged");
        col_date_logged.setVisible(false);
        col_date_logged.setPrefWidth(70);
        CustomTableViewControls.makeLabelledDatePickerTableColumn(col_date_logged, "date_logged", false);

        tblTaskItems.getColumns().addAll(col_description, col_cat, col_item_cost, col_serial, col_item_qty, col_unit, col_rate, col_total, col_date_logged);

        HashMap task_items = TaskManager.getInstance().getTaskItems(task.get_id());
        if(task_items!=null)
            if(task_items.values()!=null)
                tblTaskItems.setItems(FXCollections.observableArrayList(task_items.values()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "task [" + task.get_id() + "] has no items.");
        else IO.log(getClass().getName(), IO.TAG_WARN, "task [" + task.get_id() + "] has no items/materials.");

        PopOver popOver = new PopOver(tblTaskItems);
        popOver.setTitle("Materials for Task #" + task.getObject_number());
        popOver.setDetached(true);
        popOver.show(ScreenManager.getInstance());
    }

    private void newTaskMaterial(Task task, Node parent)
    {
        if(task==null)
        {
            IO.logAndAlert("Error", "Invalid task", IO.TAG_ERROR);
            return;
        }
        selected_material = null;
        selected_material_type = null;

        TextField txt_mat_description = new TextField("");
        txt_mat_description.setMinWidth(120);
        txt_mat_description.setPromptText("Summary of material");
        Label lbl_des = new Label("Material Description*: ");
        lbl_des.setMinWidth(160);

        TextField txt_mat_category = new TextField("");
        txt_mat_category.setMinWidth(120);
        txt_mat_category.setPromptText("Material type e.g. Access Control Hardware");
        Label lbl_cat = new Label("Material Category*: ");
        lbl_cat.setMinWidth(160);

        TextField txt_mat_value = new TextField("");
        txt_mat_value.setMinWidth(120);
        txt_mat_value.setPromptText("Material cost excl. tax");
        Label lbl_val = new Label("Material Value*: ");
        lbl_val.setMinWidth(160);

        TextField txt_mat_unit = new TextField("");
        txt_mat_unit.setMinWidth(120);
        txt_mat_unit.setPromptText("Unit of measurement");
        Label lbl_unit = new Label("Material Unit*: ");
        lbl_unit.setMinWidth(160);

        TextField txt_mat_qty = new TextField("");
        txt_mat_qty.setMinWidth(120);
        txt_mat_qty.setPromptText("Quantity");
        Label lbl_qty = new Label("Material Quantity*: ");
        lbl_qty.setMinWidth(160);

        TextField txt_mat_model = new TextField("");
        txt_mat_model.setMinWidth(120);
        txt_mat_model.setPromptText("Model or serial number");
        Label lbl_model = new Label("Model/Serial: ");
        lbl_model.setMinWidth(160);

        Button btnSubmit = new Button("Create & Add Material");
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        btnSubmit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
        btnSubmit.getStyleClass().add("btnAdd");
        btnSubmit.setMinWidth(140);
        btnSubmit.setMinHeight(35);
        HBox.setMargin(btnSubmit, new Insets(15, 0, 0, 10));

        GridPane page = new GridPane();
        page.setAlignment(Pos.CENTER_LEFT);
        page.setHgap(20);
        page.setVgap(20);

        page.add(lbl_des, 0, 0);
        page.add(txt_mat_description, 1, 0);

        page.add(lbl_cat, 0, 1);
        page.add(txt_mat_category, 1, 1);

        page.add(lbl_val, 0, 2);
        page.add(txt_mat_value, 1, 2);

        page.add(lbl_unit, 0, 3);
        page.add(txt_mat_unit, 1, 3);

        page.add(lbl_qty, 0, 4);
        page.add(txt_mat_qty, 1, 4);

        page.add(lbl_model, 0, 5);
        page.add(txt_mat_model, 1, 5);

        page.add(btnSubmit, 0, 6);

        PopOver popover = new PopOver(page);
        popover.setTitle("New material for task #" + task.getObject_number() + " with job #" + task.getJob().getObject_number() + " on " + (new SimpleDateFormat("yyyy-MM-dd").format(new Date(task.getDate_scheduled()))));
        popover.setDetached(true);
        popover.show(ScreenManager.getInstance());

        TextFields.bindAutoCompletion(txt_mat_category, ResourceManager.getInstance().getResource_types().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    selected_material_type = event.getCompletion();
                }
            }
        });

        TextFields.bindAutoCompletion(txt_mat_description, ResourceManager.getInstance().getDataset().values()).setOnAutoCompleted(event ->
        {
            if(event!=null)
            {
                if(event.getCompletion()!=null)
                {
                    //update selected material
                    selected_material = event.getCompletion();

                    IO.log(getClass().getName(), IO.TAG_INFO, "auto-completed material: " + selected_material.getResource_description());
                    txt_mat_description.setText(selected_material.getResource_description());

                    if(ResourceManager.getInstance().getResource_types()!=null && selected_material.getResource_type()!=null)
                    {
                        selected_material_type = ResourceManager.getInstance().getResource_types().get(selected_material.getResource_type());
                        txt_mat_category.setText(selected_material_type.getType_name());
                    }
                    txt_mat_value.setText(String.valueOf(selected_material.getResource_value()));
                    txt_mat_unit.setText(selected_material.getUnit());
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

                File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");

                if(!Validators.isValidNode(txt_mat_description, txt_mat_description.getText(), 1, ".+"))
                {
                    txt_mat_description.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }

                if(txt_mat_category.getText()==null)
                {
                    IO.logAndAlert("Error", "Invalid material category.\nPlease enter a valid value.", IO.TAG_WARN);
                    return;
                }

                if(txt_mat_category.getText().isEmpty())
                {
                    IO.logAndAlert("Error", "Invalid material category.\nPlease enter a valid value.", IO.TAG_WARN);
                    return;
                }

                if(!Validators.isValidNode(txt_mat_value, txt_mat_value.getText(), 1, ".+"))
                {
                    txt_mat_value.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }
                if(!Validators.isValidNode(txt_mat_unit, txt_mat_unit.getText(), 1, ".+"))
                {
                    txt_mat_unit.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }
                if(!Validators.isValidNode(txt_mat_qty, txt_mat_qty.getText(), 1, ".+"))
                {
                    txt_mat_qty.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }
                /*if(!Validators.isValidNode(txt_mat_model, txt_mat_model.getText(), 1, ".+"))
                {
                    txt_mat_model.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
                    return;
                }*/

                TaskItem taskItem = new TaskItem();
                taskItem.setTask_id(task.get_id());
                if(txt_mat_model.getText()!=null)
                    taskItem.setSerial(txt_mat_model.getText());
                try {
                    taskItem.setQuantity(Long.valueOf(txt_mat_qty.getText()));
                }catch (NumberFormatException e){
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    return;
                }
                taskItem.setCategory(txt_mat_category.getText());
                taskItem.setCreator(SessionManager.getInstance().getActive().getUsr());

                String resource_type_id = null;
                if(selected_material == null)
                {
                    //no selected Material, create new Material then create new TaskItem
                    if (selected_material_type != null)
                    {
                            /*
                                If category text is not exactly the same as the category text inputted in the material creation
                                Form then create new category/material type.
                             */
                        if (selected_material_type.getType_name().equals(txt_mat_category.getText()))
                            resource_type_id = selected_material_type.get_id();
                    }

                    Resource resource = new Resource();
                    resource.setResource_description(txt_mat_description.getText());
                    resource.setUnit(txt_mat_unit.getText());
                    resource.setQuantity(Long.valueOf(1));
                    resource.setDate_acquired(System.currentTimeMillis());
                    resource.setCreator(SessionManager.getInstance().getActive().getUsr());
                    try
                    {
                        resource.setResource_value(Double.valueOf(txt_mat_value.getText()));
                    } catch (NumberFormatException e)
                    {
                        IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                        return;
                    }
                    /*
                        If selected_material_type is null then create new material type/category using inputted
                        Text from material creation form
                     */
                    if (resource_type_id == null)
                    {
                        //create new resource type/category
                        ResourceType resourceType = new ResourceType(txt_mat_category.getText(), "");
                        resourceType.setCreator(SessionManager.getInstance().getActive().getUsr());
                        try
                        {
                            ResourceManager.getInstance().createBusinessObject(resourceType, material_category_id ->
                            {
                                if (material_category_id != null) {
                                    selected_material_type = ResourceManager.getInstance().getResource_types().get(material_category_id);

                                    resource.setResource_type((String) material_category_id);

                                    //create new material using new category
                                    createMaterial(resource, new_mat_id ->
                                    {
                                        if(new_mat_id!=null)
                                        {
                                            taskItem.setResource_id((String) new_mat_id);
                                            createTaskItem(taskItem, cb->{IO.logAndAlert("Success", "Successfully created task material [" + txt_mat_description.getText() + "] for task #" + task.getObject_number() + ", job #" + task.getJob().getObject_number(), IO.TAG_INFO);return null;});
                                        }
                                        return null;
                                    });
                                } else
                                    IO.logAndAlert("Error", "Could not create material category [" + txt_mat_category.getText() + "]", IO.TAG_ERROR);
                                return null;
                            });
                        } catch (IOException e)
                        {
                            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        }
                    } else //new material uses existing category
                    {
                        //create new material using selected category
                        resource.setResource_type(resource_type_id);
                        createMaterial(resource, new_mat_id ->
                        {
                            if(new_mat_id!=null)
                            {
                                taskItem.setResource_id((String) new_mat_id);
                                createTaskItem(taskItem, cb->{IO.logAndAlert("Success", "Successfully created task material [" + txt_mat_description.getText() + "] for task #" + task.getObject_number() + ", job #" + task.getJob().getObject_number(), IO.TAG_INFO);return null;});
                            }
                            return null;
                        });
                    }
                } else //create new TaskItem based off selected material
                {
                    taskItem.setResource_id(selected_material.get_id());
                    createTaskItem(taskItem, cb->{IO.logAndAlert("Success", "Successfully created task material [" + txt_mat_description.getText() + "] for task #" + task.getObject_number() + ", job #" + task.getJob().getObject_number(), IO.TAG_INFO);return null;});
                }
            }
        });
    }

    public void createMaterial(Resource resource, Callback callback)
    {
        if(resource==null)
        {
            IO.logAndAlert("Error: Invalid Resource", "Resource to be created is invalid.", IO.TAG_ERROR);
            return;
        }
        try
        {
            String proceed = IO.OK;
            if(selected_material!=null)
                if(resource.getResource_description().equals(selected_material.getResource_description()))
                    proceed = IO.showConfirm("Duplicate material found, proceed?", "New material's description is the same as an existing material, continue with creation of material?");

            if(proceed.equals(IO.OK))
            {
                ResourceManager.getInstance().createBusinessObject(resource, new_res_id ->
                {
                    //update selected material
                    selected_material = ResourceManager.getInstance().getDataset().get(new_res_id);

                    //execute callback w/ args
                    if(callback!=null)
                        callback.call(new_res_id);
                    //refresh materials combobox
                    /*Platform.runLater(() ->
                    {
                        if (ResourceManager.getInstance().getDataset().values() != null)
                        {
                            TextFields.bindAutoCompletion(txtMaterials, FXCollections.observableArrayList());
                            TextFields.bindAutoCompletion(txtMaterials, ResourceManager.getInstance().getDataset().values()).setOnAutoCompleted(evt ->
                            {
                                if (evt != null)
                                {
                                    if (evt.getCompletion() != null)
                                    {
                                        //update selected material
                                        selected_material = evt.getCompletion();

                                        IO.log(getClass().getName(), IO.TAG_INFO, "selected material: " + selected_material.getResource_description());
                                        itemsModified = true;
                                    }
                                }
                            });
                        }
                    });*/
                    return null;
                });
            } else {
                IO.log(getClass().getName(), IO.TAG_ERROR, "aborted material creation procedure.");
                //execute callback w/o args
                if(callback!=null)
                    callback.call(null);
            }
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            //execute callback w/o args
            if(callback!=null)
                callback.call(null);
        }
    }

    public void createTaskItem(TaskItem taskItem, Callback callback)
    {
        if(taskItem==null)
        {
            IO.logAndAlert("Error: Invalid TaskItem", "TaskItem to be created is invalid.", IO.TAG_ERROR);
            return;
        }
        try
        {
            TaskManager.getInstance().createBusinessObject(taskItem, new_task_id ->
            {
                TaskManager.getInstance().forceSynchronise();

                //execute callback w/ args
                if(callback!=null)
                    callback.call(new_task_id);
                return null;
            });
        } catch (IOException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            //execute callback w/o args
            if(callback!=null)
                callback.call(null);
        }
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
            /*ResourceManager.getInstance().initialize();
            SupplierManager.getInstance().initialize();
            ClientManager.getInstance().initialize();
            QuoteManager.getInstance().initialize();
            JobManager.getInstance().initialize();*/
            refreshModel(cb->{Platform.runLater(() -> refreshView());return null;});
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
