/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fadulousbms.controllers;

import fadulousbms.auxilary.Globals;
import fadulousbms.managers.*;
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
            vatSlider.setValue(PurchaseOrderManager.getInstance().getSelected().getVatVal());
            lblVat.setText("VAT ["+new DecimalFormat("##.##").format(vatSlider.getValue())+"%]");
            //set up text fields
            txtAccount.setText(PurchaseOrderManager.getInstance().getSelected().getAccount());
            txtNumber.setText(PurchaseOrderManager.getInstance().getSelected().getNumber());
            txtCreator.setText(PurchaseOrderManager.getInstance().getSelected().getCreator().toString());
            txtTotal.setText(Globals.CURRENCY_SYMBOL.getValue() + " " + PurchaseOrderManager.getInstance().getSelected().getTotal());
            String status;
            if(PurchaseOrderManager.getInstance().getSelected().getStatus()==0)
                status="PENDING";
            else if(PurchaseOrderManager.getInstance().getSelected().getStatus()==1)
                status="APPROVED";
            else status = "ARCHIVED";
            txtStatus.setText(status);
        }
    }
    public static RadialMenuItem[] getDefaultContextMenu()
    {
        //RadialMenuItem level1Item = new RadialMenuItemCustom(ScreenManager.MENU_SIZE, "level 1 item 1", null, null, null);//RadialMenuItem(menuSize, "level 1 item", null, null);
        return ScreenController.getDefaultContextMenu();
    }
}