/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.*;
import fadulousbms.model.PurchaseOrder;
import javafx.collections.FXCollections;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;

/**
 * views Controller class
 *
 * @author ghost
 */
public class ViewPurchaseOrderController extends PurchaseOrderController
{
    @Override
    public void refreshView()
    {
        super.refreshView();
        if(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())!=null)
        {
            //set selected supplier combo box value
            if(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getSupplier()!=null)
                cbxSuppliers.setValue(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getSupplier());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po has no valid supplier.");
            cbxSuppliers.setPromptText("prompt text");
            //set selected supplier contact person combo box value
            if(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getContact_person()!=null)
                cbxContactPerson.setValue(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getContact_person());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po has no valid contact person.");

            //Hide [Approve] button if not authorized
            if(SessionManager.getInstance().getActiveEmployee().getAccessLevel()< AccessLevel.SUPERUSER.getLevel())
            {
                btnApprove.setVisible(false);
                btnApprove.setDisable(true);
            }else{
                btnApprove.setVisible(true);
                btnApprove.setDisable(false);
            }

            //set selected PO's table items
            if(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getItems()!=null)
                tblPurchaseOrderItems.setItems(FXCollections
                        .observableArrayList(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getItems()));

            //set VAT toggle button value
            toggleVatExempt.setText(((PurchaseOrder)((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected())).getVatVal()==QuoteManager.VAT?QuoteManager.VAT+"%":"VAT exempt");
            toggleVatExempt.setSelected(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getVatVal()==QuoteManager.VAT?false:true);
            //set selected PO number
            txtNumber.setText(String.valueOf(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getObject_number()));
            //set selected PO creator Employee name
            txtCreator.setText(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getCreatorEmployee().getName());

            //load account[s] for Supplier
            if(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getSupplier()!=null)
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", ((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getSupplier().getAccount_name()}));
            else IO.log(getClass().getName(), IO.TAG_ERROR, "PO Supplier is null.");
            //set selected Supplier account
            cbxAccount.setValue(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getSupplier().getAccount_name());

            //render PO status
            String status;
            if(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getStatus()==0)
                status="PENDING";
            else if(((PurchaseOrder)PurchaseOrderManager.getInstance().getSelected()).getStatus()==1)
                status="APPROVED";
            else status = "ARCHIVED";
            txtStatus.setText(status);
        } else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po is invalid[null].");
    }

    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}