package fadulousbms.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.model.BusinessObject;
import fadulousbms.model.CustomTableViewControls;
import fadulousbms.model.FileMetadata;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.util.AbstractMap;
import java.util.ArrayList;

/**
 * Created by ghost on 2017/02/24.
 */
public class RiskAssessmentManager extends BusinessObjectManager
{
    private TableView tblRisk;
    private FileMetadata[] documents;
    private static RiskAssessmentManager safety_manager = new RiskAssessmentManager();

    public static RiskAssessmentManager getInstance()
    {
        return safety_manager;
    }

    @Override
    public void initialize()
    {
        loadDataFromServer();
    }

    public void loadDataFromServer()
    {
        try
        {
            SessionManager smgr = SessionManager.getInstance();
            if (smgr.getActive() != null)
            {
                if (!smgr.getActive().isExpired())
                {
                    Gson gson = new GsonBuilder().create();
                    ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", smgr.getActive().getSession_id()));

                    String resources_json = RemoteComms.sendGetRequest("/api/risk/indices", headers);
                    documents = gson.fromJson(resources_json, FileMetadata[].class);

                    //Sort array in ascending order
                    //TODO:FileMetadata.quickSort(documents, 0, documents.length-1);
                } else IO.logAndAlert("Session Expired", "Active session has expired.", IO.TAG_ERROR);
            } else IO.logAndAlert("Session Expired", "No active sessions.", IO.TAG_ERROR);
        }catch (JsonSyntaxException ex)
        {
            IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
        }catch (MalformedURLException ex)
        {
            IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
        }catch (IOException ex)
        {
            IO.logAndAlert(getClass().getName(), ex.getMessage(), IO.TAG_ERROR);
        }
    }

    public void newWindow()
    {
        SessionManager smgr = SessionManager.getInstance();
        if(smgr.getActive()!=null)
        {
            if(!smgr.getActive().isExpired())
            {
                Stage stage = new Stage();
                stage.setTitle(Globals.APP_NAME.getValue() + " - Risk Assessment Documents");
                stage.setMinWidth(530);
                stage.setMinHeight(340);
                //stage.setAlwaysOnTop(true);

                tblRisk = new TableView();
                tblRisk.setEditable(true);

                TableColumn<BusinessObject, String> index = new TableColumn("Index");
                CustomTableViewControls.makeEditableTableColumn(index, TextFieldTableCell.forTableColumn(), 80, "index", "/api/risk/index");

                TableColumn<BusinessObject, String> label = new TableColumn("Label");
                CustomTableViewControls.makeEditableTableColumn(label, TextFieldTableCell.forTableColumn(), 250, "label", "/api/risk/index");

                TableColumn<BusinessObject, String> document = new TableColumn("Document");
                CustomTableViewControls.makeEditableTableColumn(document, TextFieldTableCell.forTableColumn(), 250, "pdf_path", "/api/risk/index");

                TableColumn<BusinessObject, HBox> action = new TableColumn("Action");
                CustomTableViewControls.makeActionTableColumn(action, 270, "pdf_path", "/api/risk/index");

                TableColumn<BusinessObject, GridPane> required = new TableColumn("Required?");
                CustomTableViewControls.makeToggleButtonTableColumn(required, null,100, "required", "/api/risk/index");

                TableColumn<BusinessObject, GridPane> mark = new TableColumn("Mark");
                CustomTableViewControls.makeCheckboxedTableColumn(mark, null,100, "marked", "/api/risk/index");

                ObservableList<FileMetadata> lst_inspection = FXCollections.observableArrayList();
                lst_inspection.addAll(documents);

                tblRisk.setItems(lst_inspection);
                tblRisk.getColumns().addAll(index, label, document, action, required, mark);

                //Menu bar
                MenuBar menu_bar = new MenuBar();
                Menu file = new Menu("File");
                Menu print = new Menu("Print");
                Menu view = new Menu("View");

                //Menu - File
                MenuItem new_item = new MenuItem("New Risk Assessment Document Reference");
                new_item.setOnAction(event -> handleNewRiskIndex(stage));

                MenuItem exit = new MenuItem("Close Window");
                exit.setOnAction(event -> stage.close());

                //Menu - View
                MenuItem generate_index = new MenuItem("Generate/View Index Page");
                generate_index.setOnAction(event -> IO.viewIndexPage("Risk Assessment Documents Index", documents, "bin/risk_index.pdf"));

                //Menu - Print
                MenuItem print_index = new MenuItem("Print Index Page");
                print_index.setOnAction(event -> IO.printIndexPage("bin/risk_index.pdf"));

                MenuItem print_selected = new MenuItem("Print Marked Documents");
                print_selected.setOnAction(event -> IO.printSelectedDocuments(documents));

                MenuItem print_all = new MenuItem("Print All Documents");
                print_all.setOnAction(event -> IO.printAllDocuments(documents));

                file.getItems().addAll(new_item, exit);
                view.getItems().add(generate_index);
                print.getItems().addAll(print_index, print_selected, print_all);

                menu_bar.getMenus().addAll(file, view, print);

                BorderPane border_pane = new BorderPane();
                border_pane.setTop(menu_bar);
                border_pane.setCenter(tblRisk);

                stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                        loadDataFromServer());

                Scene scene = new Scene(border_pane);
                stage.setScene(scene);
                stage.show();
                stage.centerOnScreen();
                stage.setResizable(true);
            }else IO.showMessage("Session Expired", "Active session has expired.", IO.TAG_ERROR);
        }else IO.showMessage("Session Expired", "No active sessions.", IO.TAG_ERROR);
    }

    public void handleNewRiskIndex(Stage parentStage)
    {
        parentStage.setAlwaysOnTop(false);
        Stage stage = new Stage();
        stage.setTitle(Globals.APP_NAME.getValue() + " - Add New Risk Assessment Document Reference");
        stage.setMinWidth(320);
        stage.setMinHeight(200);
        //stage.setAlwaysOnTop(true);

        VBox vbox = new VBox(10);

        final TextField txt_index = new TextField();
        txt_index.setMinWidth(200);
        txt_index.setMaxWidth(Double.MAX_VALUE);
        HBox index = CustomTableViewControls.getLabelledNode("Index", 200, txt_index);

        final TextField txt_label = new TextField();
        txt_label.setMinWidth(200);
        txt_label.setMaxWidth(Double.MAX_VALUE);
        HBox label = CustomTableViewControls.getLabelledNode("Label", 200, txt_label);

        final TextField txt_path = new TextField();
        txt_path.setMinWidth(200);
        txt_path.setMaxWidth(Double.MAX_VALUE);
        HBox path = CustomTableViewControls.getLabelledNode("Document path", 200, txt_path);
        //TODO: File picker

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            if(!Validators.isValidNode(txt_index, txt_index.getText(), 1, "(\\d+|\\d+\\.\\d+)"))
            {
                JOptionPane.showMessageDialog(null, "Please make sure that the index number is valid.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(!Validators.isValidNode(txt_label, txt_label.getText(), 1, ".+"))
            {
                JOptionPane.showMessageDialog(null, "Please make sure that the label is not empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if(!Validators.isValidNode(txt_path, txt_path.getText(), 1, ".+"))
            {
                JOptionPane.showMessageDialog(null, "Please make sure that the file path is not empty.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            String str_index = txt_index.getText();
            String str_label = txt_label.getText();
            String str_path = txt_path.getText();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("index", str_index));
            params.add(new AbstractMap.SimpleEntry<>("label", str_label));
            params.add(new AbstractMap.SimpleEntry<>("pdf_path", String.valueOf(str_path)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));
                else
                {
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/risk/index/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        JOptionPane.showMessageDialog(null, "Successfully added new risk assessment document reference!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    }else{
                        String msg = IO.readStream(connection.getErrorStream());
                        JOptionPane.showMessageDialog(null, msg, "Error " + connection.getResponseCode(), JOptionPane.ERROR_MESSAGE);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
            }
        });

        //Add form controls vertically on the scene
        vbox.getChildren().add(index);
        vbox.getChildren().add(label);
        vbox.getChildren().add(path);
        vbox.getChildren().add(submit);

        //Setup scene and display stage
        Scene scene = new Scene(vbox);
        File fCss = new File(IO.STYLES_ROOT_PATH+"home.css");
        scene.getStylesheets().clear();
        scene.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));

        stage.onHidingProperty().addListener((observable, oldValue, newValue) ->
                loadDataFromServer());

        stage.setScene(scene);
        stage.show();
        stage.centerOnScreen();
        stage.setResizable(true);
    }
}
