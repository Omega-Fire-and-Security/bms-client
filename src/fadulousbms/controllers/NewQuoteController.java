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
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Callback;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class NewQuoteController extends QuoteController
{
    /*@FXML
    private VBox vboxForm;
    @FXML
    private Label lblTitle;*/

    @Override
    public void refreshView()
    {
        super.refreshView();
    }

    /*@FXML
    public void newClient()
    {
        //VBox vbox = new VBox();
        //vboxForm.getChildren().removeAll();
        vboxForm.getChildren().setAll(new Node[]{});


        lblTitle.setText("Create New Client For Quote");

        final TextField txt_client_name = new TextField();
        txt_client_name.setMinWidth(200);
        txt_client_name.setMaxWidth(Double.MAX_VALUE);
        HBox client_name = CustomTableViewControls.getLabelledNode("Client Name", 200, txt_client_name, Color.WHITE);

        final TextArea txt_physical_address = new TextArea();
        txt_physical_address.setMinWidth(200);
        txt_physical_address.setMaxWidth(Double.MAX_VALUE);
        HBox physical_address = CustomTableViewControls.getLabelledNode("Physical Address", 200, txt_physical_address, Color.WHITE);

        final TextArea txt_postal_address = new TextArea();
        txt_postal_address.setMinWidth(200);
        txt_postal_address.setMaxWidth(Double.MAX_VALUE);
        HBox postal_address = CustomTableViewControls.getLabelledNode("Postal Address", 200, txt_postal_address, Color.WHITE);

        final TextField txt_tel = new TextField();
        txt_tel.setMinWidth(200);
        txt_tel.setMaxWidth(Double.MAX_VALUE);
        HBox tel = CustomTableViewControls.getLabelledNode("Tel Number", 200, txt_tel, Color.WHITE);

        final CheckBox chbx_active = new CheckBox();
        chbx_active.setMinWidth(200);
        chbx_active.setMaxWidth(Double.MAX_VALUE);
        HBox active = CustomTableViewControls.getLabelledNode("Active", 200, chbx_active, Color.WHITE);

        final DatePicker dpk_date_partnered = new DatePicker();
        dpk_date_partnered.setMinWidth(200);
        dpk_date_partnered.setMaxWidth(Double.MAX_VALUE);
        HBox date_partnered = CustomTableViewControls.getLabelledNode("Date Partnered", 200, dpk_date_partnered, Color.WHITE);

        final TextField txt_website = new TextField();
        txt_website.setMinWidth(200);
        txt_website.setMaxWidth(Double.MAX_VALUE);
        HBox website = CustomTableViewControls.getLabelledNode("Website", 200, txt_website, Color.WHITE);

        final TextArea txt_other = new TextArea();
        txt_other.setMinWidth(200);
        txt_other.setMaxWidth(Double.MAX_VALUE);
        HBox other = CustomTableViewControls.getLabelledNode("Other", 200, txt_other, Color.WHITE);

        HBox submit;
        submit = CustomTableViewControls.getSpacedButton("Submit", event ->
        {
            String date_regex="\\d+(\\-|\\/|\\\\)\\d+(\\-|\\/|\\\\)\\d+";

            if(!Validators.isValidNode(txt_client_name, txt_client_name.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_physical_address, txt_physical_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_postal_address, txt_postal_address.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_tel, txt_tel.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                return;
            if(!Validators.isValidNode(dpk_date_partnered, dpk_date_partnered.getValue()==null?"":dpk_date_partnered.getValue().toString(), 4, date_regex))
                return;
            if(!Validators.isValidNode(txt_website, txt_website.getText(), 1, ".+"))
                return;

            String str_client_name = txt_client_name.getText();
            String str_physical_address = txt_physical_address.getText();
            String str_postal_address = txt_postal_address.getText();
            String str_tel = txt_tel.getText();
            String str_website = txt_website.getText();
            long date_partnered_in_sec = dpk_date_partnered.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            String str_other = txt_other.getText();
            boolean is_active = chbx_active.selectedProperty().get();

            ArrayList<AbstractMap.SimpleEntry<String, String>> params = new ArrayList<>();
            params.add(new AbstractMap.SimpleEntry<>("client_name", str_client_name));
            params.add(new AbstractMap.SimpleEntry<>("physical_address", str_physical_address));
            params.add(new AbstractMap.SimpleEntry<>("postal_address", str_postal_address));
            params.add(new AbstractMap.SimpleEntry<>("tel", str_tel));
            params.add(new AbstractMap.SimpleEntry<>("website", str_website));
            params.add(new AbstractMap.SimpleEntry<>("date_partnered", String.valueOf(date_partnered_in_sec)));
            params.add(new AbstractMap.SimpleEntry<>("other", str_other));
            params.add(new AbstractMap.SimpleEntry<>("active", String.valueOf(is_active)));

            try
            {
                ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
                if(SessionManager.getInstance().getActive()!=null)
                    headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSessionId()));
                else
                {
                    JOptionPane.showMessageDialog(null, "No active sessions.", "Session expired", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                HttpURLConnection connection = RemoteComms.postData("/api/client/add", params, headers);
                if(connection!=null)
                {
                    if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                    {
                        IO.logAndAlert("Success", "Successfully added new client!", IO.TAG_INFO);
                        //TODO: callback.call(null);
                    }else{
                        IO.logAndAlert( "ERROR_" + connection.getResponseCode(),  IO.readStream(connection.getErrorStream()), IO.TAG_ERROR);
                    }
                    connection.disconnect();
                }
            } catch (IOException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }
        });

        //populate clients combobox

        //Add form controls vertically on the stage
        vboxForm.getChildren().add(client_name);
        vboxForm.getChildren().add(physical_address);
        vboxForm.getChildren().add(postal_address);
        vboxForm.getChildren().add(tel);
        vboxForm.getChildren().add(website);
        vboxForm.getChildren().add(date_partnered);
        vboxForm.getChildren().add(other);
        vboxForm.getChildren().add(active);
        vboxForm.getChildren().add(submit);

        /*ClientManager.getInstance().newClientWindow(param ->
        {
            new Thread(() ->
            {
                refreshModel();
                Platform.runLater(() -> refreshView());
            }).start();
            return null;
        });*
    }*/
}
