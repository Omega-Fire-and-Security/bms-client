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
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

import java.io.IOException;
import java.net.URL;
import java.time.DateTimeException;
import java.util.*;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ViewQuoteController extends QuoteController
{
    @Override
    public void refreshView()
    {
        super.refreshView();

        //Populate fields
        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
        {
            if(selected.getContact_person()==null)
            {
                IO.logAndAlert("View Quote Error", "Selected quote's contact person attribute is null.", IO.TAG_ERROR);
                return;
            }
            if(selected.getClient()==null)
            {
                IO.logAndAlert("View Quote Error", "Selected quote's client attribute is null.", IO.TAG_ERROR);
                return;
            }
            cbxClients.setValue(selected.getClient());
            cbxContactPerson.setValue(selected.getContact_person());
            txtCell.setText(selected.getContact_person().getCell());
            txtTel.setText(selected.getContact_person().getTel());
            txtEmail.setText(selected.getContact_person().getEmail());
            txtFax.setText(cbxClients.getValue().getFax());
            txtQuoteId.setText(selected.get_id());
            //set VAT toggle button value
            toggleVatExempt.setText(QuoteManager.getInstance().getSelectedQuote().getVat()==QuoteManager.VAT?QuoteManager.VAT+"%":"VAT exempt");
            toggleVatExempt.setSelected(QuoteManager.getInstance().getSelectedQuote().getVat()==QuoteManager.VAT?false:true);
            //load account[s] for Client
            if(QuoteManager.getInstance().getSelectedQuote().getClient()!=null)
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", QuoteManager.getInstance().getSelectedQuote().getClient().getAccount_name()}));
            else IO.log(getClass().getName(), IO.TAG_ERROR, "Selected Quote Client is null.");
            //set selected Supplier account
            cbxAccount.setValue(QuoteManager.getInstance().getSelectedQuote().getClient().getAccount_name());
            txtSite.setText(selected.getSitename());
            txtRequest.setText(selected.getRequest());
            //txtVat.setText(String.valueOf(selected.getVat()));

            try
            {
                //String date = LocalDate.parse(new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(selected.getDate_generated()*1000))).toString();
                String date = new Date(selected.getDate_generated() * 1000).toString();
                txtDateGenerated.setText(date);
            } catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            if (selected.getResources() != null)
                tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getResources()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");
            if (selected.getRepresentatives() != null)
                tblSaleReps.setItems(FXCollections.observableArrayList(selected.getRepresentatives()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected
                    .get_id() + "] has no representatives.");

            /** Store additional cost cols in a HashMap -  used a map
             * To ensure that only a single instance of all additional
             * Cost columns are stored.
             **/
            //HashMap<String, TableColumn> map = new HashMap<>();
            //search for matching column for each additional cost
            for (QuoteItem item : tblQuoteItems.getItems())
            {
                if (item.getAdditional_costs() != null)
                {
                    for (String str_cost : item.getAdditional_costs().split(";"))
                    {
                        String[] arr = str_cost.split("=");
                        if (arr != null)
                        {
                            if (arr.length > 1)
                            {
                                TableColumn col = new TableColumn(arr[0]);

                                col.setCellFactory(super.getAdditionalCostCallback(col));
                                //if column absent from map, add it
                                if(colsMap.get(arr[0].toLowerCase())==null)
                                {
                                    col.setPrefWidth(80);
                                    tblQuoteItems.getColumns().add(col);
                                    super.addAdditionalCostColToMap(arr[0].toLowerCase(), col);//TODO: use quoteItem.getResource_id()
                                    //colsMap.put(arr[0].toLowerCase(), col);
                                }
                            }
                        }
                    }
                }
            }
            tblQuoteItems.refresh();
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(selected.getTotal()));
        }else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void back()
    {
        ScreenController.previousScreen();
    }
}