/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RadialMenuItemCustom;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.auxilary.Validators;
import fadulousbms.managers.*;
import fadulousbms.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.File;
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
public class NewExpenseController extends ScreenController implements Initializable
{
    private boolean itemsModified;
    private Date date_generated;
    @FXML
    private TextField txtTitle,txtDescription,txtValue,txtOther,txtAccount;
    @FXML
    private ComboBox<Supplier> cbxSupplier;
    @FXML
    private DatePicker dateLogged;

    @Override
    public void refreshView()
    {
        IO.log(getClass().getName(), IO.TAG_INFO, "reloading new purchase order view..");
        if(SupplierManager.getInstance().getSuppliers()==null)
        {
            IO.logAndAlert(getClass().getName(), "no suppliers found in the database.", IO.TAG_ERROR);
            return;
        }
        Supplier[] suppliers = (Supplier[]) SupplierManager.getInstance().getSuppliers().values().toArray();

        cbxSupplier.setItems(FXCollections.observableArrayList(suppliers));
    }

    @Override
    public void refreshModel()
    {
        SupplierManager.getInstance().initialize();
    }

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb)
    {
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }

    @FXML
    public void createExpense()
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
        if(!Validators.isValidNode(txtTitle, txtTitle.getText(), 1, ".+"))
        {
            txtTitle.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtDescription, txtDescription.getText(), 1, ".+"))
        {
            txtDescription.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtValue, txtValue.getText(), 1, ".+"))
        {
            txtValue.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(!Validators.isValidNode(txtAccount, txtAccount.getText(), 1, ".+"))
        {
            txtAccount.getStylesheets().add("file:///"+ fCss.getAbsolutePath().replace("\\", "/"));
            return;
        }
        if(cbxSupplier.getValue()==null)
        {
            IO.logAndAlert("Error", "Please choose a valid supplier.", IO.TAG_ERROR);
            return;
        }
        if(dateLogged.getValue()==null)
        {
            IO.logAndAlert("Error", "Please choose a valid purchase date.", IO.TAG_ERROR);
            return;
        }
        if(dateLogged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond()<=0)
        {
            IO.logAndAlert("Error", "Please choose a valid purchase date.", IO.TAG_ERROR);
            return;
        }

        //prepare expense parameters
        Expense expense = new Expense();
        expense.setExpense_title(txtTitle.getText());
        expense.setExpense_description(txtDescription.getText());
        expense.setExpense_value(Double.parseDouble(txtValue.getText()));
        expense.setCreator(SessionManager.getInstance().getActive().getUsr());
        expense.setAccount(txtAccount.getText());
        expense.setSupplier(cbxSupplier.getValue().get_id());
        expense.setDate_logged(dateLogged.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
        if(txtOther.getText()!=null)
            expense.setOther(txtOther.getText());

        try
        {
            ArrayList<AbstractMap.SimpleEntry<String, String>> headers = new ArrayList<>();
            headers.add(new AbstractMap.SimpleEntry<>("Cookie", SessionManager.getInstance().getActive().getSession_id()));

            //create new quote on database
            HttpURLConnection connection = RemoteComms.postData("/api/expense/add", expense.asUTFEncodedString(), headers);
            if(connection!=null)
            {
                if(connection.getResponseCode()==HttpURLConnection.HTTP_OK)
                {
                    String response = IO.readStream(connection.getInputStream());
                    IO.log(getClass().getName(), IO.TAG_INFO, "created expense["+response+"].");

                    if(response==null)
                    {
                        IO.logAndAlert("New Expense Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    if(response.isEmpty())
                    {
                        IO.logAndAlert("New Expense Error", "Invalid server response.", IO.TAG_ERROR);
                        return;
                    }
                    IO.logAndAlert("New Expense Creation Success", "Successfully created a new expense.", IO.TAG_INFO);
                    itemsModified = false;
                }else
                {
                    //Get error message
                    String msg = IO.readStream(connection.getErrorStream());
                    IO.logAndAlert("Error " +String.valueOf(connection.getResponseCode()), msg, IO.TAG_ERROR);
                }
                if(connection!=null)
                    connection.disconnect();
            }else IO.logAndAlert("New Expense Creation Failure", "Could not connect to server.", IO.TAG_ERROR);
        } catch (IOException e)
        {
            IO.logAndAlert(getClass().getName(), e.getMessage(), IO.TAG_ERROR);
        }
    }

    @FXML
    public void back()
    {
        final ScreenManager screenManager = ScreenManager.getInstance();
        ScreenManager.getInstance().showLoadingScreen(param ->
        {
            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        if(screenManager.loadScreen(Screens.OPERATIONS.getScreen(),fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            screenManager.setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                    }
                }
            }).start();
            return null;
        });
    }
}
