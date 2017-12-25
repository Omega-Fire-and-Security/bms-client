package fadulousbms.model;

import fadulousbms.auxilary.*;
import fadulousbms.managers.JobManager;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.BusinessObject;
import fadulousbms.model.ComboBoxTableCell;
import fadulousbms.model.DatePickerCell;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import javax.print.PrintException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ghost on 2017/01/11.
 */
public class CustomTableViewControls
{
    public static final String TAG = "CustomTableViewControls";

    /*public static void makeComboBoxTableColumn(TableColumn<BusinessObject, String> comboBox_col, HashMap<String, BusinessObject> combo_box_items, String property, String label_property, String api_method, int pref_width)
    {
        comboBox_col.setMinWidth(120);
        comboBox_col.setPrefWidth(pref_width);
        comboBox_col.setCellValueFactory(new PropertyValueFactory<>(property));
        comboBox_col.setCellFactory(col -> new ComboBoxTableCell(combo_box_items, property, label_property, api_method));
        //comboBox_col.setEditable(true);
        comboBox_col.setOnEditCommit(event ->
        {
            event.getRowValue().parse(property, event.getNewValue());
            System.out.println("ComboBox edit commit!");
        });
    }

    public static void makeComboBoxTableColumn(TableColumn<BusinessObject, String> comboBox_col, HashMap<String, BusinessObject> combo_box_items, String property, String label_property, String api_method, int pref_width, boolean is_multi_types)
    {
        comboBox_col.setMinWidth(120);
        comboBox_col.setPrefWidth(pref_width);
        comboBox_col.setCellValueFactory(new PropertyValueFactory<>(property));
        comboBox_col.setCellFactory(col -> new ComboBoxTableCell(combo_box_items, property, label_property, api_method));
        //comboBox_col.setEditable(true);
        comboBox_col.setOnEditCommit(event ->
        {
            event.getRowValue().parse(property, event.getNewValue());
        });
    }*/

    /*public static void makeComboBoxTableColumn(TableColumn<BusinessObject, String> comboBox_col, String[] combo_box_items, String property, String label_var, String api_method)
    {
        comboBox_col.setMinWidth(120);
        comboBox_col.setPrefWidth(220);
        comboBox_col.setCellValueFactory(new PropertyValueFactory<>(property));
        comboBox_col.setCellFactory(col -> new ComboBoxTableCell(combo_box_items, property, api_method));
        comboBox_col.setEditable(true);
        comboBox_col.setOnEditCommit(event -> event.getRowValue().parse(property, event.getNewValue()));
    }*/

    public static void makeDatePickerTableColumn(TableColumn<BusinessObject, Long> date_col, String property, String api_method)
    {
        date_col.setMinWidth(130);
        date_col.setCellValueFactory(new PropertyValueFactory<>(property));
        date_col.setCellFactory(col -> new DatePickerCell(property, api_method));
        date_col.setEditable(true);
        date_col.setOnEditCommit(event -> event.getRowValue().parse(property, event.getNewValue()));
    }

    public static void makeLabelledDatePickerTableColumn(TableColumn<BusinessObject, Long> date_col, String property, String api_method)
    {
        date_col.setMinWidth(130);
        date_col.setCellValueFactory(new PropertyValueFactory<>(property));
        date_col.setCellFactory(col -> new LabelledDatePickerCell(property, api_method));
        date_col.setEditable(true);
        date_col.setOnEditCommit(event -> event.getRowValue().parse(property, event.getNewValue()));
    }

    public static void makeDatePickerTableColumn(TableColumn<BusinessObject, Long> date_col, String property, boolean editable)
    {
        date_col.setMinWidth(130);
        date_col.setCellValueFactory(new PropertyValueFactory<>(property));
        date_col.setCellFactory(col -> new DatePickerCell(property, editable));
        date_col.setEditable(false);
        date_col.setOnEditCommit(event -> event.getRowValue().parse(property, event.getNewValue()));
    }

    /*public static void makeEditableNumberTableColumn(TableColumn<BusinessObject, Double> col, Callback<TableColumn<BusinessObject, Double>, TableCell<BusinessObject, Double>> editable_control_callback, int min_width, String property, String api_call)
    {
        if(col!=null)
        {
            col.setMinWidth(min_width);
            col.setCellValueFactory(new PropertyValueFactory<>(property));
            col.setCellFactory(editable_control_callback);
            col.setOnEditCommit(event ->
            {
                BusinessObject bo = event.getRowValue();
                bo.parse(property, event.getNewValue());

                RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
            });
        }else{
            System.err.println("Null table column!");//TODO: logging
        }
    }*/

    public static void makeEditableTableColumn(TableColumn<BusinessObject, String> col, Callback<TableColumn<BusinessObject, String>, TableCell<BusinessObject, String>> editable_control_callback, int min_width, String property, String api_call)
    {
        if(col!=null)
        {
            col.setMinWidth(min_width);
            col.setCellValueFactory(new PropertyValueFactory<>(property));
            col.setCellFactory(editable_control_callback);
            col.setOnEditCommit(event ->
            {
                BusinessObject bo = event.getRowValue();
                if(bo!=null)
                {
                    bo.parse(property, event.getNewValue());
                    RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
                }
            });
        }else{
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeEditableColumn(TableColumn<BusinessObject, String> col, Callback<TableColumn<BusinessObject, String>, TableCell<BusinessObject, String>> editable_control_callback, int min_width, String property, String api_call)
    {
        if(col!=null)
        {
            col.setMinWidth(min_width);
            col.setCellValueFactory(new PropertyValueFactory<>(property));
            col.setCellFactory(editable_control_callback);
            col.setOnEditCommit(event ->
            {
                BusinessObject bo = event.getRowValue();
                if(bo!=null)
                {
                    bo.parse(property, event.getNewValue());
                    RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
                }
            });
        }else{
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void createEditableTableColumn(TableColumn<BusinessObject, String> col, Callback<TableColumn<BusinessObject, String>, TableCell<BusinessObject, String>> editable_control_callback, int min_width, String property, String api_call)
    {
        if(col!=null)
        {
            col.setMinWidth(min_width);
            col.setCellValueFactory(new PropertyValueFactory<>(property));
            col.setCellFactory(editable_control_callback);
            col.setOnEditCommit(event ->
            {
                BusinessObject bo = event.getRowValue();
                if(bo!=null)
                    bo.parse(property, event.getNewValue());
            });
        }else{
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeCheckboxedTableColumn(TableColumn<BusinessObject, GridPane> col, Callback<TableColumn<BusinessObject, GridPane>, TableCell<BusinessObject,GridPane>> editable_control_callback, int min_width, String property, String api_call)
    {
        if (col != null)
        {
            col.setMinWidth(min_width);
            //col.setCellFactory(editable_control_callback);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, GridPane> param) ->
            {
                BusinessObject bo = param.getValue();

                CheckBox cbx = new CheckBox();
                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);

                cbx.setAlignment(Pos.CENTER);
                grid.add(cbx, 0, 0);
                if(property.toLowerCase().equals("marked"))
                {
                    bo.setMarked(false);
                }
                cbx.selectedProperty().addListener((observable, oldValue, newValue) ->
                        bo.setMarked(newValue));
                return new SimpleObjectProperty<>(grid);
            });
        } else
        {
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeToggleButtonTypeTableColumn(TableColumn<BusinessObject, GridPane> col, Callback<TableColumn<BusinessObject, GridPane>, TableCell<BusinessObject,GridPane>> editable_control_callback, int min_width, String property, String api_call)
    {
        if (col != null)
        {
            col.setMinWidth(min_width);
            //col.setCellFactory(editable_control_callback);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, GridPane> param) ->
            {
                BusinessObject bo = param.getValue();

                //Make toggle button and set button state from data from database.
                ToggleButton toggleButton;
                if(bo instanceof FileMetadata)
                {
                    Object val = bo.get(property);
                    if(val!=null)
                    {
                        String str_val = (String)val;//Boolean.parseBoolean(val);
                        if(str_val.toLowerCase().equals("generic"))
                        {
                            toggleButton = new ToggleButton("Generic");
                            toggleButton.setSelected(true);
                        }else{
                            toggleButton = new ToggleButton("Custom");
                            toggleButton.setSelected(false);
                        }
                    }else{
                        //value not set - assume false
                        toggleButton = new ToggleButton("Unknown");
                        toggleButton.setSelected(false);
                    }
                }else
                {
                    toggleButton = new ToggleButton("Unknown");
                    toggleButton.setSelected(false);
                }

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);

                toggleButton.setAlignment(Pos.CENTER);
                grid.add(toggleButton, 0, 0);

                toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                {
                    String newVal = null;
                    switch (toggleButton.getText().toLowerCase())
                    {
                        case "generic":
                            newVal = "custom";
                            toggleButton.setText("Custom");
                            break;
                        case "custom":
                            newVal = "generic";
                            toggleButton.setText("Generic");
                            break;
                        case "unknown":
                            newVal = "unknown";
                            toggleButton.setText("Unknown");
                            break;
                        default:
                            IO.log(TAG, IO.TAG_ERROR, "unknown File Type :" + toggleButton.getText());
                            break;
                    }

                    if((bo instanceof FileMetadata))
                    {
                        if (newVal != null)
                            bo.parse(property, newVal);
                        else IO.log(TAG, IO.TAG_ERROR, "new value [meant to be either generic/custom/unknown] is null.");
                    }else IO.log(TAG, IO.TAG_ERROR, "unknown class, attempting to set boolean value to Business Object's " + property + " property.");

                    RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
                });
                return new SimpleObjectProperty<>(grid);
            });
        } else
        {
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeToggleButtonTableColumn(TableColumn<BusinessObject, GridPane> col, Callback<TableColumn<BusinessObject, GridPane>, TableCell<BusinessObject,GridPane>> editable_control_callback, int min_width, String property, String api_call)
    {
        if (col != null)
        {
            col.setMinWidth(min_width);
            //col.setCellFactory(editable_control_callback);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, GridPane> param) ->
            {
                BusinessObject bo = param.getValue();

                //Make toggle button and set button state from data from database.
                ToggleButton toggleButton;
                if(bo instanceof FileMetadata)
                {
                    Object val = bo.get(property);
                    if(val!=null)
                    {
                        boolean bool_val = (Boolean)val;//Boolean.parseBoolean(val);
                        if(bool_val)
                        {
                            toggleButton = new ToggleButton("Yes");
                            toggleButton.setSelected(true);
                        }else{
                            toggleButton = new ToggleButton("No");
                            toggleButton.setSelected(false);
                        }
                    }else{
                        //value not set - assume false
                        toggleButton = new ToggleButton("No");
                        toggleButton.setSelected(false);
                    }
                }else
                {
                    toggleButton = new ToggleButton("No");
                    toggleButton.setSelected(false);
                }

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);

                toggleButton.setAlignment(Pos.CENTER);
                grid.add(toggleButton, 0, 0);

                toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                {
                    if(newValue)
                    {
                        toggleButton.setText("Yes");
                    }else{
                        toggleButton.setText("No");
                    }
                    if(!(bo instanceof FileMetadata))
                        IO.log(TAG, IO.TAG_ERROR, "unknown class, attempting to set boolean value to Business Object's " + property + " property.");
                    else IO.log(TAG, IO.TAG_INFO, "attempting to set boolean value to FileMetadata's " + property + " property.");

                    bo.parse(property, newValue);
                    RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
                });
                return new SimpleObjectProperty<>(grid);
            });
        } else
        {
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeToggleButtonTableColumn(TableColumn<BusinessObject, ToggleButton> col, Callback<TableColumn<BusinessObject, ToggleButton>, TableCell<BusinessObject,ToggleButton>> editable_control_callback, int min_width, String property, String[] props, String api_call)
    {
        if(props==null){
            IO.log(TAG, IO.TAG_ERROR, "makeToggleButtonTableColumn()> props[] is null.");
            return;
        }
        if(props.length>3){
            IO.log(TAG, IO.TAG_ERROR, "makeToggleButtonTableColumn()> props[] is incomplete.");
            return;
        }
        if (col != null)
        {
            col.setMinWidth(min_width);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, ToggleButton> param) ->
            {
                BusinessObject bo = param.getValue();

                //Make toggle button and set button state from data from database.
                ToggleButton toggleButton;

                if(bo.get(property).equals(props[0]))
                {
                    toggleButton = new ToggleButton(props[1]);
                    toggleButton.setSelected(true);
                }else if(bo.get(property).equals(props[2]))
                {
                    toggleButton = new ToggleButton(props[3]);
                    toggleButton.setSelected(false);
                }else {
                    toggleButton = new ToggleButton("UNKNOWN");
                }
                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);

                toggleButton.setAlignment(Pos.CENTER);
                grid.add(toggleButton, 0, 0);

                toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                {
                    if(newValue)//toggle button selected
                    {
                        bo.parse(property, props[0]);
                        toggleButton.setText(props[1]);
                    }
                    else{
                        bo.parse(property, props[2]);
                        toggleButton.setText(props[3]);
                    }
                    RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
                });
                return new SimpleObjectProperty<ToggleButton>(toggleButton);
            });
        } else
        {
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeDynamicToggleButtonTableColumn(TableColumn<BusinessObject, GridPane> col, int min_width, String property, String[] props, boolean clickable, String api_call)
    {
        if (col != null)
        {
            col.setMinWidth(min_width);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, GridPane> param) ->
            {
                BusinessObject bo = param.getValue();

                //Make toggle button and set button state from data from database.
                ToggleButton toggleButton;
                Object val = bo.get(property);
                if(val!=null)
                {
                    //boolean bool_val = (Boolean)val;//Boolean.parseBoolean(val);
                    boolean bool_val = val.toString().equals(props[0]);
                    if(bool_val)
                    {
                        toggleButton = new ToggleButton(props[1]);
                        toggleButton.setSelected(false);
                    }else{
                        toggleButton = new ToggleButton(props[3]);
                        toggleButton.setSelected(true);
                    }
                    toggleButton.setDisable(!clickable);
                }else{
                    //value not set - assume false
                    toggleButton = new ToggleButton("UNKNOWN");
                    toggleButton.setSelected(false);
                }

                GridPane grid = new GridPane();
                grid.setAlignment(Pos.CENTER);
                HBox.setHgrow(toggleButton, Priority.ALWAYS);
                toggleButton.setMaxHeight(Double.MAX_VALUE);

                toggleButton.setAlignment(Pos.CENTER);
                grid.add(toggleButton, 0, 0);

                toggleButton.selectedProperty().addListener((observable, oldValue, newValue) ->
                {
                    if(newValue)
                    {
                        toggleButton.setText(props[3]);
                        bo.parse(property, props[2]);
                    }else{
                        toggleButton.setText(props[1]);
                        bo.parse(property, props[0]);
                    }

                    RemoteComms.updateBusinessObjectOnServer(bo, api_call, property);
                });
                return new SimpleObjectProperty<>(grid);
            });
        } else
        {
            IO.log(TAG, IO.TAG_ERROR, "Null table column!");
        }
    }

    public static void makeActionTableColumn(TableColumn<BusinessObject, HBox> col, int min_width, String property, String api_call)
    {
        if (col != null)
        {
            col.setMinWidth(min_width);
            //col.setCellFactory(param -> null);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, HBox> param) ->
            {
                BusinessObject bo = param.getValue();

                Button btnView = new Button("View Document");
                btnView.setOnAction(event ->
                {
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                    Session active = SessionManager.getInstance().getActive();
                    try
                    {
                        if (active != null)
                        {
                            if (!active.isExpired())
                            {
                                headers.add(new AbstractMap.SimpleEntry<>("Cookie", active.getSession_id()));
                                headers.add(new AbstractMap.SimpleEntry<>("logo_options", (String)bo.get("logo_options")));

                                String filename = String.valueOf(bo.get(property));
                                long start = System.currentTimeMillis();
                                byte[] file = RemoteComms.sendFileRequest("/api/file/"+filename, headers);
                                if (file != null)
                                {
                                    long ellapsed = System.currentTimeMillis()-start;
                                    IO.log(TAG, IO.TAG_INFO, "File ["+filename+"] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                                    PDFViewer pdfViewer = PDFViewer.getInstance();
                                    pdfViewer.setVisible(true);

                                    String local_filename = filename.substring(filename.lastIndexOf('/')+1);
                                    if(new File("out/"+local_filename).exists())
                                        Files.delete(new File("out/"+local_filename).toPath());
                                    FileOutputStream out = new FileOutputStream(new File("out/"+local_filename));
                                    out.write(file, 0, file.length);
                                    out.flush();
                                    out.close();

                                    //pdfViewer.doOpen("bin/" + filename + ".bin");
                                    pdfViewer.doOpen("out/"+local_filename);
                                    //Clean up
                                    Files.delete(Paths.get("out/"+local_filename));
                                } else
                                {
                                    IO.logAndAlert("File Downloader", "File '" + filename + "' could not be downloaded.", IO.TAG_ERROR);
                                }
                            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    }catch (IOException e)
                    {
                        IO.log(TAG, IO.TAG_ERROR, e.getMessage());
                    }

                    /*try
                    {
                        //pdfViewer.set
                        PDF.viewPDF("dist/" + String.valueOf(bo.get(property)));
                    } catch (IOException e)
                    {
                        IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                    }*/
                });
                Button btnPrint = new Button("Print Now");
                HBox.setMargin(btnPrint, new Insets(0,10,0,10));
                btnPrint.setOnAction(event -> printDocument(bo, property));

                HBox container = new HBox();
                container.getChildren().addAll(btnView, btnPrint);
                container.setAlignment(Pos.CENTER);

                return new SimpleObjectProperty<>(container);
            });
        } else
        {
            System.err.println("Null table column!");//TODO: logging
        }
    }

    public static void makeJobManagerAction(TableColumn<BusinessObject, HBox> col, int min_width, String property)
    {
        if (col != null)
        {
            col.setMinWidth(min_width);
            //col.setCellFactory(editable_control_callback);
            col.setCellValueFactory((TableColumn.CellDataFeatures<BusinessObject, HBox> param) ->
            {
                Job job = (Job)param.getValue();

                Button btnSafety = new Button("Safety File");
                btnSafety.setOnAction(event ->
                {
                    JobManager jobManager = JobManager.getInstance();
                    jobManager.showJobSafetyFile(job);
                });

                Button btnJobCard = new Button("View Job Card");
                btnJobCard.setOnAction(event ->
                {
                    JobManager jobManager = JobManager.getInstance();
                    jobManager.showJobCard(job);
                });

                Button btnEmployees = new Button("Assigned Employees");
                btnEmployees.setOnAction(event ->
                {
                    JobManager jobManager = JobManager.getInstance();
                    jobManager.showJobReps(job);
                    /*ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<AbstractMap.SimpleEntry<String, String>>();
                    Session active = SessionManager.getInstance().getActive();
                    try
                    {
                        if (active != null)
                        {
                            if (!active.isExpired())
                            {
                                headers.add(new AbstractMap.SimpleEntry<String, String>("Cookie", active.getSession_id()));
                                headers.add(new AbstractMap.SimpleEntry<String, String>("logo_options", (String)bo.get("logo_options")));

                                String filename = String.valueOf(bo.get(property));
                                long start = System.currentTimeMillis();
                                byte[] file = RemoteComms.sendFileRequest(filename, headers);
                                if (file != null)
                                {
                                    long ellapsed = System.currentTimeMillis()-start;
                                    IO.log(TAG, IO.TAG_INFO, "File ["+filename+"] download complete, size: "+file.length+" bytes in "+ellapsed+"msec.");
                                    PDFViewer pdfViewer = new PDFViewer(true);
                                    pdfViewer.setVisible(true);

                                    FileOutputStream out = new FileOutputStream(new File("out/temp.pdf"));
                                    out.write(file, 0, file.length);
                                    out.flush();
                                    out.close();

                                    //pdfViewer.doOpen("bin/" + filename + ".bin");
                                    pdfViewer.doOpen("out/temp.pdf");
                                    //Clean up
                                    Files.delete(Paths.get("out/temp.pdf"));
                                } else
                                {
                                    IO.logAndAlert("File Downloader", "File '" + filename + "' could not be downloaded.", IO.TAG_ERROR);
                                }
                            } else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
                        } else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
                    }catch (IOException e)
                    {
                        IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                    }*/
                });
                /*Button btnPrint = new Button("Print Now");
                HBox.setMargin(btnPrint, new Insets(0,10,0,10));
                btnPrint.setOnAction(event -> printDocument(bo, property));*/

                HBox container = new HBox();
                container.getChildren().addAll(btnEmployees, btnSafety, btnJobCard);
                HBox.setMargin(btnEmployees, new Insets(0,10,0,10));
                HBox.setMargin(btnJobCard, new Insets(0,10,0,10));
                container.setAlignment(Pos.CENTER);
                container.setMinHeight(min_width);

                return new SimpleObjectProperty<>(container);
            });
        } else
        {
            IO.log(TAG, IO.TAG_ERROR, "null table column!");
        }
    }

    public static void printDocument(BusinessObject bo, String property)
    {
        //Validate session - also done on server-side don't worry ;)
        SessionManager smgr = SessionManager.getInstance();
        if (smgr.getActive() != null)
        {
            if (!smgr.getActive().isExpired())
            {
                try
                {
                    //Prepare headers
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                    String filename = String.valueOf(bo.get(property));
                    long start = System.currentTimeMillis();
                    byte[] file = RemoteComms.sendFileRequest(filename, headers);
                    if (file != null)
                    {
                        long ellapsed = System.currentTimeMillis() - start;
                        IO.log(TAG, IO.TAG_INFO, "File [" + filename + "] download complete, size: " + file.length + " bytes in " + ellapsed + "msec.");

                        PDF.printPDF(file);
                        IO.log(TAG, IO.TAG_INFO, "Printing: " + filename + " ["+bo.get_id()+"]");
                    }else{
                        IO.logAndAlert("Print Job", "Could not download file '"+filename+"'", IO.TAG_ERROR);
                    }
                }catch (PrintException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }catch (IOException e)
                {
                    IO.logAndAlert(TAG, e.getMessage(), IO.TAG_ERROR);
                }
            } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public static HBox getLabelledNode(String label, int lbl_min_w, Node node)
    {
        HBox hbox = new HBox();
        Label lbl = new Label(label);
        HBox.setMargin(lbl, new Insets(8));
        hbox.getChildren().add(lbl);
        lbl.setMinWidth(lbl_min_w);

        //node.minWidth(node_min_w);
        HBox.setHgrow(node, Priority.ALWAYS);
        hbox.getChildren().add(node);
        VBox.setMargin(hbox, new Insets(0,10,0,10));

        HBox.setHgrow(hbox, Priority.ALWAYS);
        return hbox;
    }

    public static HBox getLabelledNode(String label, int lbl_min_w, Node node, Color label_colour)
    {
        HBox hbox = new HBox();
        Label lbl = new Label(label);
        lbl.setTextFill(label_colour);
        HBox.setMargin(lbl, new Insets(8));
        hbox.getChildren().add(lbl);
        lbl.setMinWidth(lbl_min_w);

        //node.minWidth(node_min_w);
        HBox.setHgrow(node, Priority.ALWAYS);
        hbox.getChildren().add(node);
        VBox.setMargin(hbox, new Insets(0,10,0,10));

        HBox.setHgrow(hbox, Priority.ALWAYS);
        return hbox;
    }

    public static HBox getSpacedButton(String btn_name, EventHandler<ActionEvent> btnClickHandler)
    {
        HBox name = new HBox();
        Label lbl = new Label("");
        HBox.setMargin(lbl, new Insets(5));
        name.getChildren().add(lbl);
        lbl.setMinWidth(150);

        Button btn = new Button(btn_name);
        btn.setMinWidth(150);
        btn.setMinHeight(50);
        btn.getStyleClass().add("btnDefault");
        HBox.setHgrow(btn, Priority.ALWAYS);
        btn.setMaxWidth(400);
        btn.setMaxHeight(100);
        name.getChildren().add(btn);

        btn.setOnAction(btnClickHandler);

        return name;
    }
}
