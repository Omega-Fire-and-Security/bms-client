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
import javafx.scene.control.*;

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
            if(selected.getRoot()==null)
            {
                IO.logAndAlert("View Quote Error", "Selected quote's root is null. Should return self if base.", IO.TAG_ERROR);
                return;
            }

            //get selected Quote's siblings sorted by revision number
            Quote[] siblings = selected.getSortedSiblings("revision");
            if(siblings==null)
                IO.log("View Quote Warning", "selected quote has no siblings. using self.", IO.TAG_WARN);
            else selected = siblings[QuoteManager.getInstance().selected_quote_sibling_cursor];//set selected Quote to be selected_quote_sibling_cursor revision.

            if(siblings!=null)
                IO.log(getClass().getName(), IO.TAG_INFO, "selected Quote sibling count: " + siblings.length);

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
            //Hide [Approve] button if not authorized
            if(SessionManager.getInstance().getActiveEmployee().getAccessLevel()<AccessLevels.SUPERUSER.getLevel())
            {
                btnApprove.setVisible(false);
                btnApprove.setDisable(true);
            }else{
                btnApprove.setVisible(true);
                btnApprove.setDisable(false);
            }

            cbxClients.setValue(selected.getClient());
            cbxContactPerson.setValue(selected.getContact_person());
            txtCell.setText(selected.getContact_person().getCell());
            txtTel.setText(selected.getContact_person().getTel());
            txtEmail.setText(selected.getContact_person().getEmail());
            txtFax.setText(cbxClients.getValue().getFax());
            if(selected.getParent_id()!=null)
                txtQuoteId.setText(selected.getParent().get_id());
            else txtQuoteId.setText(selected.get_id());
            txtRevision.setText(String.valueOf(selected.getRevision()));
            /*if(selected.getParent_id()!=null)
                txtBase.setText(selected.getParent_id().get_id());
            else
            {
                txtBase.setText("NONE");
                IO.log(getClass().getName(), IO.TAG_WARN, "quote " + selected.get_id() + " has no parent.");
            }*/

            //set VAT toggle button value
            toggleVatExempt.setText(QuoteManager.getInstance().getSelectedQuote().getVat()==QuoteManager.VAT?QuoteManager.VAT+"%":"VAT exempt");
            toggleVatExempt.setSelected(QuoteManager.getInstance().getSelectedQuote().getVat()==QuoteManager.VAT?false:true);
            //load account[s] for Client
            if(QuoteManager.getInstance().getSelectedQuote().getClient()!=null)
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", QuoteManager.getInstance().getSelectedQuote().getClient().getAccount_name()}));
            else IO.log(getClass().getName(), IO.TAG_ERROR, "Selected Quote Client is null.");
            //set selected Supplier account
            cbxAccount.getSelectionModel().select(QuoteManager.getInstance().getSelectedQuote().getAccount_name());
            //cbxAccount.setValue(QuoteManager.getInstance().getSelectedQuote().getAccount_name());
            txtSite.setText(selected.getSitename());
            txtRequest.setText(selected.getRequest());
            //txtVat.setText(String.valueOf(selected.getVat_number()));

            try
            {
                //String date = LocalDate.parse(new SimpleDateFormat("EEE, d MMM yyyy").format(new Date(selected.getDate_generated()*1000))).toString();
                String date = new Date(selected.getDate_logged()).toString();
                txtDateGenerated.setText(date);
            } catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            if (selected.getResources() != null)
                tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getResources()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");

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
        } else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    public void viewBase()
    {
        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
            if(selected.getParent_id()!=null)
            {
                QuoteManager.getInstance().selected_quote_sibling_cursor=0;
                //QuoteManager.getInstance().setSelectedQuote(selected.getParent_id());
                //refresh GUI
                new Thread(() ->
                {
                    refreshModel();
                    Platform.runLater(() -> refreshView());
                }).start();
            } else IO.logAndAlert("View Quote Error", "Selected quote has no base quote.", IO.TAG_ERROR);
        else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    public void nextRev()
    {
        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
        {
            //get selected Quote's siblings and traverse through them
            Quote[] siblings = selected.getSortedSiblings("revision");
            if (siblings != null)
            {
                //set selected quote
                if(QuoteManager.getInstance().selected_quote_sibling_cursor+1>=siblings.length)
                    QuoteManager.getInstance().selected_quote_sibling_cursor=0;//wrap around to first revision
                else QuoteManager.getInstance().selected_quote_sibling_cursor++;//go to next revision
                //QuoteManager.getInstance().setSelectedQuote(siblings[QuoteManager.getInstance().selected_quote_sibling_cursor]);
                //refresh GUI
                new Thread(() ->
                {
                    refreshModel();
                    Platform.runLater(() -> refreshView());
                }).start();
            } else IO.logAndAlert("View Quote Error", "Selected quote has no siblings. Should return self as first arg of array.", IO.TAG_ERROR);
        } else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    public void prevRev()
    {
        Quote selected = QuoteManager.getInstance().getSelectedQuote();
        if(selected!=null)
        {
            //get selected Quote's parent's children and traverse through them
            Quote[] siblings = selected.getSortedSiblings("revision");
            if (siblings != null)
            {
                //set selected quote
                if(QuoteManager.getInstance().selected_quote_sibling_cursor-1<0)
                    QuoteManager.getInstance().selected_quote_sibling_cursor=0;//wrap around to first revision
                else QuoteManager.getInstance().selected_quote_sibling_cursor--;//go to next revision
                //QuoteManager.getInstance().setSelectedQuote(siblings[QuoteManager.getInstance().selected_quote_sibling_cursor]);//set selected to be
                //refresh GUI
                new Thread(() ->
                {
                    refreshModel();
                    Platform.runLater(() -> refreshView());
                }).start();
            } else IO.logAndAlert("View Quote Error", "Selected quote has no siblings. Should return self as first arg of siblings array.", IO.TAG_ERROR);
        } else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void back()
    {
        ScreenController.previousScreen();
    }
}