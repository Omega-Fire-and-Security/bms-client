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

import java.io.IOException;
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
        Quote selected = ((Quote)QuoteManager.getInstance().getSelected());
        if(selected!=null)
        {
            if(selected.getRoot()==null)
            {
                IO.logAndAlert("View Quote Error", "Selected quote's root is null. Should return self if base.", IO.TAG_ERROR);
                return;
            }

            //get selected Quote's siblings sorted by revision number
            Quote[] selected_quote_siblings = selected.getSortedSiblings("revision");
            //Quote[] selected_quote_children = selected.getChildren("revision");
            if(selected_quote_siblings==null)
            {
                IO.log(getClass().getName(), IO.TAG_WARN, "selected quote has no siblings, which shouldn't be possible, so please reload this quote.");
                return;
            }
            if(selected_quote_siblings.length<=0)
            {
                IO.log(getClass().getName(), IO.TAG_WARN, "selected quote has no siblings, which shouldn't be possible, so please reload this quote.");
                return;
            }
            //QuoteManager.getInstance().selected_quote_sibling_cursor =revs.length-1;//point selected_quote_sibling_cursor to latest revision
            if(selected_quote_siblings != null)
            {
                if (QuoteManager.getInstance().selected_quote_sibling_cursor < selected_quote_siblings.length)
                    selected = selected_quote_siblings[QuoteManager.getInstance().selected_quote_sibling_cursor];//make quote revision at [selected_quote_sibling_cursor] be selected quote
                else
                    selected = selected.getLatestRevisionFromChildren();//[selected_quote_children.length-1];//make last quote revision be selected quote
            } //else has no other revisions, use currently selected Quote object

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
            } else{
                btnApprove.setVisible(true);
                btnApprove.setDisable(false);
            }

            selected_client = selected.getClient();
            selected_contact_person = selected.getContact_person();

            txtClient.setText(selected.getClient().getClient_name());
            txtContactPerson.setText(selected.getContact_person().getName());

            txtCell.setText(selected.getContact_person().getCell());
            txtTel.setText(selected.getContact_person().getTel());
            txtEmail.setText(selected.getContact_person().getEmail());
            //txtFax.setText(selected.getClient().getFax());
            if(selected.getParent_id()!=null)
                txtQuoteId.setText(selected.getParent().get_id());
            else txtQuoteId.setText(selected.get_id());
            txtRevision.setText(String.valueOf(selected.getRevision()));

            if(selected.getOther()!=null)
                txtNotes.setText(selected.getOther().replaceAll(";", "\n"));
            /*if(selected.getParent_id()!=null)
                txtBase.setText(selected.getParent_id().get_id());
            else
            {
                txtBase.setText("NONE");
                IO.log(getClass().getName(), IO.TAG_WARN, "quote " + selected.get_id() + " has no parent.");
            }*/

            //set VAT toggle button value
            toggleVatExempt.setText(((Quote)QuoteManager.getInstance().getSelected()).getVat()==QuoteManager.VAT?QuoteManager.VAT+"%":"VAT exempt");
            toggleVatExempt.setSelected(((Quote)QuoteManager.getInstance().getSelected()).getVat()==QuoteManager.VAT?false:true);
            //load account[s] for Client
            if(((Quote)QuoteManager.getInstance().getSelected()).getClient()!=null)
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", ((Quote)QuoteManager.getInstance().getSelected()).getClient().getAccount_name()}));
            else IO.log(getClass().getName(), IO.TAG_ERROR, "Selected Quote Client is null.");
            //set selected Supplier account
            cbxAccount.getSelectionModel().select(((Quote)QuoteManager.getInstance().getSelected()).getAccount_name());
            //cbxAccount.setValue(QuoteManager.getInstance().getSelectedQuote().getAccount_name());
            txtSite.setText(selected.getSitename());
            txtRequest.setText(selected.getRequest());
            //txtVat.setText(String.valueOf(selected.getVat_number()));

            try
            {
                String date = new Date(selected.getDate_logged()).toString();
                txtDateGenerated.setText(date);
            } catch (DateTimeException e)
            {
                IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
            }

            //set quote items
            if (selected.getResources() != null)
                tblQuoteItems.setItems(FXCollections.observableArrayList(selected.getResources()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no resources.");

            //set quote services
            if (selected.getServices() != null)
                tblQuoteServices.setItems(FXCollections.observableArrayList(selected.getServices()));
            else IO.log(getClass().getName(), IO.TAG_WARN, "quote [" + selected.get_id() + "] has no services.");

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
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + QuoteManager.computeQuoteTotal(
                    selected.getResources()!=null?FXCollections.observableArrayList(selected.getResources()):null,
                    selected.getServices()!=null?FXCollections.observableArrayList(selected.getServices()):null));
        } else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    public void viewBase()
    {
        QuoteManager.getInstance().selected_quote_sibling_cursor=0;
        //refresh GUI
        new Thread(() ->
            refreshModel(param ->
            {
                Platform.runLater(() -> refreshView());
                return null;
            })).start();
        /*Quote selected = ((Quote)QuoteManager.getInstance().getSelected());
        if(selected!=null)
            if(selected.getParent_id()!=null)
            {
                QuoteManager.getInstance().selected_quote_sibling_cursor=0;
                //refresh GUI
                new Thread(() ->
                        refreshModel(param ->
                        {
                            Platform.runLater(() -> refreshView());
                            return null;
                        })).start();
            } else IO.logAndAlert("View Quote Error", "Selected quote has no base quote.", IO.TAG_ERROR);
        else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);*/
    }

    public void nextRev()
    {
        Quote selected = ((Quote)QuoteManager.getInstance().getSelected());
        if(selected!=null)
        {
            //get selected Quote's siblings and traverse through them
            //Quote[] siblings = selected.getSortedSiblings("revision");
            //HashMap childrenQuotes = selected.getChildrenMap();
            HashMap revs = selected.getSiblingsMap();//uses a bit less resources

            if (revs != null)
            {
                //set selected quote
                if(QuoteManager.getInstance().selected_quote_sibling_cursor+1>=revs.size())
                    QuoteManager.getInstance().selected_quote_sibling_cursor=0;//wrap around to first revision
                else QuoteManager.getInstance().selected_quote_sibling_cursor++;//go to next revision
                //refresh GUI
                new Thread(() ->
                        refreshModel(param ->
                        {
                            Platform.runLater(() -> refreshView());
                            return null;
                        })).start();
            } else IO.logAndAlert("View Quote Error", "Selected quote has no siblings. Should return self as first object in siblings array.", IO.TAG_ERROR);
        } else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    public void prevRev()
    {
        Quote selected = ((Quote)QuoteManager.getInstance().getSelected());
        if(selected!=null)
        {
            //get selected Quote's parent's children and traverse through them
            //Quote[] siblings = selected.getSortedSiblings("revision");
            //HashMap childrenQuotes = selected.getChildrenMap();
            HashMap revs = selected.getSiblingsMap();//uses a bit less resources

            if (revs != null)
            {
                //set selected quote
                if(QuoteManager.getInstance().selected_quote_sibling_cursor-1<0)
                    QuoteManager.getInstance().selected_quote_sibling_cursor=revs.size()-1;//wrap around to first revision
                else QuoteManager.getInstance().selected_quote_sibling_cursor--;//go to previous revision

                //refresh GUI
                new Thread(() ->
                        refreshModel(param ->
                        {
                            Platform.runLater(() -> refreshView());
                            return null;
                        })).start();
            } else IO.logAndAlert("View Quote Error", "Selected quote has no siblings. Should return self as first object in siblings array.", IO.TAG_ERROR);
        } else IO.logAndAlert("View Quote Error", "Selected quote is invalid.", IO.TAG_ERROR);
    }

    @FXML
    public void back()
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
                        if(ScreenManager.getInstance().loadScreen(Screens.OPERATIONS.getScreen(), fadulousbms.FadulousBMS.class.getResource("views/"+Screens.OPERATIONS.getScreen())))
                        {
                            //Platform.runLater(() ->
                            ScreenManager.getInstance().setScreen(Screens.OPERATIONS.getScreen());
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "could not load operations screen.");
                    } catch (IOException e)
                    {
                        IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
                        e.printStackTrace();
                    }
                }
            }).start();
            return null;
        });
    }
}