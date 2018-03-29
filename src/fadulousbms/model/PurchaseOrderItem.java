package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.BusinessObjectManager;
import fadulousbms.managers.PurchaseOrderManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by ghost on 2017/01/21.
 */
public abstract class PurchaseOrderItem extends BusinessObject
{
    private int item_number;
    private String purchase_order_id;
    private String item_id;
    private int quantity;
    private double discount;
    private double cost;
    private String type;
    public static final String TAG = "PurchaseOrderItem";

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.ADMIN;
    }

    @Override
    public BusinessObjectManager getManager()
    {
        return PurchaseOrderManager.getInstance();
    }

    public String getItem_number()
    {
        return String.valueOf(item_number);
    }

    public int getItem_numberValue()
    {
        return item_number;
    }

    public void setItem_number(int item_number)
    {
        this.item_number = item_number;
    }

    public String getPurchase_order_id()
    {
        return purchase_order_id;
    }

    public void setPurchase_order_id(String purchase_order_id)
    {
        this.purchase_order_id = purchase_order_id;
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public String getItem_id()
    {
        return item_id;
    }

    public void setItem_id(String item_id)
    {
        this.item_id = item_id;
    }

    public abstract String getItem_description();

    public abstract String getUnit();

    public String getQuantity()
    {
        return String.valueOf(quantity);
    }

    public int getQuantityValue()
    {
        return quantity;
    }

    public void setQuantity(int quantity)
    {
        this.quantity = quantity;
    }

    public String getCost()
    {
        return String.valueOf(getCostValue());
    }//Globals.CURRENCY_SYMBOL.getValue() + " " +

    //public abstract double getCostValue();
    public double getCostValue()
    {
        return cost;
    }

    public void setCost(double cost)
    {
        this.cost=cost;
    }

    public String getDiscount(){return String.valueOf(this.discount) + "%";}

    public double getDiscountValue(){return this.discount;}

    public void setDiscount(double discount)
    {
        this.discount = discount;
    }

    public PurchaseOrder getPurchaseOrder()
    {
        if(getManager().getDataset()!=null)
            if(getManager().getDataset().get(getPurchase_order_id())!=null)
                return (PurchaseOrder) getManager().getDataset().get(getPurchase_order_id());
        return null;
    }

    public StringProperty item_descriptionProperty(){return new SimpleStringProperty(getItem_description());}
    public StringProperty item_idProperty(){return new SimpleStringProperty(item_id);}
    public StringProperty item_numberProperty(){return new SimpleStringProperty(String.valueOf(item_number));}
    public StringProperty purchase_order_idProperty(){return new SimpleStringProperty(purchase_order_id);}
    private StringProperty unitProperty(){return new SimpleStringProperty(getUnit());}
    private StringProperty quantityProperty(){return new SimpleStringProperty(getQuantity());}
    private StringProperty costProperty(){return new SimpleStringProperty(getCost());}
    private StringProperty discountProperty(){return new SimpleStringProperty(String.valueOf(discount));}
    private StringProperty totalProperty(){return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));}

    public double getTotal()
    {
        return getCostValue()*getQuantityValue();
    }

    public abstract BusinessObject getItem();

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "purchase_order_id":
                    setPurchase_order_id(String.valueOf(val));
                    break;
                case "item_id":
                    setItem_id(String.valueOf(val));
                    break;
                case "item_number":
                    setItem_number(Integer.valueOf((String)val));
                    break;
                case "quantity":
                    setQuantity(Integer.valueOf((String)val));
                    break;
                case "discount":
                    setDiscount(Double.parseDouble((String) val));
                    break;
                case "cost":
                    setCost(Double.parseDouble((String) val));
                    break;
                default:
                    IO.log(getClass().getName(), IO.TAG_ERROR, "Unknown "+getClass().getName()+" attribute '" + var + "'.");
                    break;
            }
        } catch (NumberFormatException e)
        {
            IO.log(getClass().getName(), IO.TAG_ERROR, e.getMessage());
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "purchase_order_id":
                return getPurchase_order_id();
            case "item_id":
                return getItem_id();
            case "item_number":
                return getItem_number();
            case "cost":
                return getCostValue();
            case "item_description":
                return getItem_description();
            case "unit":
                return getUnit();
            case "quantity":
                return getQuantity();
            case "discount":
                return getDiscount();
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"purchase_order_id\":\""+getPurchase_order_id()+"\""
                +",\"item_id\":\""+getItem_id()+"\""
                +",\"type\":\""+getType()+"\""
                +",\"unit\":\""+getUnit()+"\""
                +",\"quantity\":\""+getQuantity()+"\""
                +",\"cost\":\""+getCost()+"\"";
        if(getDiscountValue()>0)
            json_obj+=",\"discount\":\""+getDiscountValue()+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        String str = "#" + getObject_number();
        if(getPurchaseOrder()!=null)
            str += ", for purchase order " + getPurchaseOrder().toString();
        else str+= ", for purchase order [" + getPurchase_order_id() + "]";
        return str;
    }

    //TODO: apiEndpoint() pointing to /purchaseorder/item that searches from all PurchaseOrderItem collections?
}