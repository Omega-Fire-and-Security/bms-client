package fadulousbms.model;

import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

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
    private BusinessObject item;
    private String type;
    public static final String TAG = "PurchaseOrderItem";

    public StringProperty item_numberProperty(){return new SimpleStringProperty(String.valueOf(item_number));}

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

    public StringProperty purchase_order_idProperty(){return new SimpleStringProperty(purchase_order_id);}

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

    public StringProperty item_idProperty(){return new SimpleStringProperty(item_id);}

    public String getItem_id()
    {
        return item_id;
    }

    public void setItem_id(String item_id)
    {
        this.item_id = item_id;
    }

    public StringProperty item_nameProperty()
    {
        return new SimpleStringProperty(getItem_name());
    }

    public abstract String getItem_name();

    public StringProperty item_descriptionProperty(){return new SimpleStringProperty(getItem_description());}

    public abstract String getItem_description();

    private StringProperty unitProperty(){return new SimpleStringProperty(getUnit());}

    public abstract String getUnit();

    private StringProperty quantityProperty(){return new SimpleStringProperty(getQuantity());}

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

    private StringProperty costProperty(){return new SimpleStringProperty(getCost());}

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

    private StringProperty discountProperty(){return new SimpleStringProperty(String.valueOf(discount));}

    public String getDiscount(){return String.valueOf(this.discount) + "%";}

    public double getDiscountValue(){return this.discount;}

    public void setDiscount(double discount)
    {
        this.discount = discount;
    }

    private StringProperty totalProperty(){return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + String.valueOf(getTotal()));}

    public double getTotal()
    {
        //return (getCostValue()-(getCostValue()*(getDiscountValue()/100)))*getQuantityValue(); //discounted value * qty
        return getCostValue()*getQuantityValue();
    }

    public abstract BusinessObject getItem();

    //public abstract BusinessObject getItem();
   /* public BusinessObject getItem()
    {
        return this.item;
    }

    public void setItem(BusinessObject item)
    {
        this.item=item;
    }*/

    @Override
    public void parse(String var, Object val)
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
            case "item_name":
                return getItem_name();
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
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("purchase_order_id","UTF-8") + "="
                    + URLEncoder.encode(purchase_order_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("item_id","UTF-8") + "="
                    + URLEncoder.encode(item_id, "UTF-8"));
            result.append("&" + URLEncoder.encode("item_number","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(item_number), "UTF-8"));
            result.append("&" + URLEncoder.encode("quantity","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(quantity), "UTF-8"));
            result.append("&" + URLEncoder.encode("discount","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(discount), "UTF-8"));
            result.append("&" + URLEncoder.encode("cost","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(cost), "UTF-8"));
            if(getDate_logged()>0)
                result.append("&" + URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8"));
            if(getOther()!=null)
                if(!getOther().isEmpty())
                    result.append("&" + URLEncoder.encode("other","UTF-8") + "="
                            + URLEncoder.encode(getOther(), "UTF-8"));
            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String toString()
    {
        String super_json = super.toString();
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
}