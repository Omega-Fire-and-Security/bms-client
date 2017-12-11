/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.managers.*;
import fadulousbms.model.Employee;
import javafx.collections.FXCollections;
import jfxtras.labs.scene.control.radialmenu.RadialMenuItem;
import java.text.DecimalFormat;

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
        if(PurchaseOrderManager.getInstance().getSelected()!=null)
        {
            //set selected supplier combo box value
            if(PurchaseOrderManager.getInstance().getSelected().getSupplier()!=null)
                cbxSuppliers.setValue(PurchaseOrderManager.getInstance().getSelected().getSupplier());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po has no valid supplier.");
            //set selected supplier contact person combo box value
            if(PurchaseOrderManager.getInstance().getSelected().getContact_person()!=null)
                cbxContactPerson.setValue(PurchaseOrderManager.getInstance().getSelected().getContact_person());
            else IO.log(getClass().getName(), IO.TAG_ERROR, "selected po has no valid contact person.");

            //Hide [Approve] button if not authorized
            if(SessionManager.getInstance().getActiveEmployee().getAccessLevel()< Employee.ACCESS_LEVEL_SUPER)
            {
                btnApprove.setVisible(false);
                btnApprove.setDisable(true);
            }else{
                btnApprove.setVisible(true);
                btnApprove.setDisable(false);
            }

            //set selected PO's table items
            if(PurchaseOrderManager.getInstance().getSelected().getItems()!=null)
                tblPurchaseOrderItems.setItems(FXCollections
                        .observableArrayList(PurchaseOrderManager.getInstance().getSelected().getItems()));

            //set VAT toggle button value
            toggleVatExempt.setText(PurchaseOrderManager.getInstance().getSelected().getVatVal()==QuoteManager.VAT?QuoteManager.VAT+"%":"VAT exempt");
            toggleVatExempt.setSelected(PurchaseOrderManager.getInstance().getSelected().getVatVal()==QuoteManager.VAT?false:true);
            //set selected PO number
            txtNumber.setText(PurchaseOrderManager.getInstance().getSelected().getNumber());
            //set selected PO creator Employee name
            txtCreator.setText(PurchaseOrderManager.getInstance().getSelected().getCreator().toString());

            //load account[s] for Supplier
            if(PurchaseOrderManager.getInstance().getSelected().getSupplier()!=null)
                cbxAccount.setItems(FXCollections.observableArrayList(new String[]{"Cash", PurchaseOrderManager.getInstance().getSelected().getSupplier().getAccount_name()}));
            else IO.log(getClass().getName(), IO.TAG_ERROR, "PO Supplier is null.");
            //set selected Supplier account
            cbxAccount.setValue(PurchaseOrderManager.getInstance().getSelected().getSupplier().getAccount_name());

            //render PO status
            String status;
            if(PurchaseOrderManager.getInstance().getSelected().getStatus()==0)
                status="PENDING";
            else if(PurchaseOrderManager.getInstance().getSelected().getStatus()==1)
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