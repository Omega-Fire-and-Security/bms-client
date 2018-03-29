package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.QuoteManager;
import fadulousbms.managers.ResourceManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by th3gh0st on 2017/01/21.
 * @author th3gh0st
 */
public class QuoteItem extends ApplicationObject implements Serializable
{
    private int item_number;
    private int quantity;
    private double unit_cost;
    private double markup;
    private String additional_costs;
    private String quote_id;
    private String resource_id;
    private String category;
    public static final String TAG = "QuoteItem";

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
    public ApplicationObjectManager getManager()
    {
        return QuoteManager.getInstance();
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

    public String getQuote_id()
    {
        return quote_id;
    }

    public void setQuote_id(String quote_id)
    {
        this.quote_id = quote_id;
    }

    public String getResource_id()
    {
        return resource_id;
    }

    public void setResource_id(String resource_id)
    {
        this.resource_id = resource_id;
    }

    public String getEquipment_description()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getResource_description();
        return "N/A";
    }

    public String getAdditional_costs()
    {
        return additional_costs;
    }

    public void setAdditional_costs(String additional_costs)
    {
        this.additional_costs = additional_costs;
    }

    public String getUnit()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getUnit();
        return "N/A";
    }

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

    /*public String getUnit_cost()
    {
        return String.valueOf(getUnitCost());
    }*/

    public double getUnit_Cost()
    {
        return unit_cost;
    }

    public void setUnit_cost(double unit_cost)
    {
        this.unit_cost = unit_cost;
    }

    public String getCurrentValue()
    {
        return String.valueOf(getCurrentUnitCost());
    }

    public double getCurrentUnitCost()
    {
        Resource resource = getResource();
        if(resource!=null)
            return resource.getResource_value();
        return 0;
    }

    public String getMarkup(){return String.valueOf(this.markup);}

    public double getMarkupValue(){return this.markup;}

    public void setMarkup(double markup){this.markup=markup;}

    public Resource getResource()
    {
        HashMap<String, Resource> resources = ResourceManager.getInstance().getDataset();
        if(resources!=null)
            return resources.get(getResource_id());
        return null;
    }

    public double getRate()
    {
        double marked_up_cost = getUnit_Cost() + getUnit_Cost()*(markup/100);
        double total = marked_up_cost;

        //check additional costs
        if (getAdditional_costs() != null)
        {
            if (!getAdditional_costs().isEmpty())
            {
                //compute additional costs for each Quote Item
                if(getAdditional_costs().contains(";"))//check cost delimiter
                {
                    String[] costs = getAdditional_costs().split(";");
                    for (String str_cost : costs)
                    {
                        if (str_cost.contains("="))
                        {
                            //retrieve cost and markup
                            String add_cost = str_cost.split("=")[1];//the cost value is [1] (the cost name is [0])

                            double cost,add_cost_markup=0;
                            if(add_cost.contains("*"))//if(in the form cost*markup)
                            {
                                cost = Double.parseDouble(add_cost.split("\\*")[0]);
                                add_cost_markup = Double.parseDouble(add_cost.split("\\*")[1]);
                            }else cost = Double.parseDouble(add_cost);

                            //add marked up additional cost to total
                            total += cost + cost*(add_cost_markup/100);
                        } else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid Quote Item additional cost.");
                    }
                } else if (getAdditional_costs().contains("="))//if only one additional cost
                {
                    double cost,add_cost_markup=0;
                    //get cost and markup
                    if(getAdditional_costs().split("=")[1].contains("*"))
                    {
                        cost = Double.parseDouble(getAdditional_costs().split("=")[1].split("\\*")[0]);
                        add_cost_markup = Double.parseDouble(getAdditional_costs().split("=")[1].split("\\*")[1]);
                    }else cost = Double.parseDouble(getAdditional_costs().split("=")[1]);
                    //add marked up additional cost to total
                    total += cost + cost*(add_cost_markup/100);
                }else IO.log(getClass().getName(), IO.TAG_ERROR, "invalid Quote Item additional cost.");
            }
        }
        return total;
    }

    public String getCategory()
    {
        if(category!=null)
            return category;
        else if(getResource()!=null)
            return getResource().getResourceType();
        else return null;
    }

    public void setCategory(String category)
    {
        this.category = category;
    }

    public double getTotal()
    {
        return getRate()*getQuantityValue();
    }

    public Quote getQuote()
    {
        if(getManager().getDataset()!=null)
            if(getManager().getDataset().get(getQuote_id())!=null)
                return (Quote) getManager().getDataset().get(getQuote_id());

        return null;
    }

    // QuoteItem Model Properties

    public StringProperty item_numberProperty(){return new SimpleStringProperty(String.valueOf(item_number));}
    public StringProperty quote_idProperty(){return new SimpleStringProperty(quote_id);}
    public StringProperty resource_idProperty(){return new SimpleStringProperty(resource_id);}
    public StringProperty equipment_descriptionProperty(){return new SimpleStringProperty(getEquipment_description());}
    public StringProperty additional_costsProperty(){return new SimpleStringProperty(additional_costs);}
    public StringProperty unitProperty(){return new SimpleStringProperty(getUnit());}
    public StringProperty quantityProperty(){return new SimpleStringProperty(String.valueOf(quantity));}
    public StringProperty unit_costProperty() {return new SimpleStringProperty(String.valueOf(unit_cost));}
    public StringProperty valueProperty(){return new SimpleStringProperty(String.valueOf(getCurrentValue()));}
    public StringProperty rateProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + getRate());
    }
    public StringProperty markupProperty(){return new SimpleStringProperty(String.valueOf(markup));}
    public StringProperty categoryProperty()
    {
        return new SimpleStringProperty(String.valueOf(getCategory()));
    }

    public StringProperty totalProperty()
    {
        return new SimpleStringProperty(Globals.CURRENCY_SYMBOL.getValue() + " " + getTotal());
    }

    @Override
    public void parse(String var, Object val) throws ParseException
    {
        super.parse(var, val);
        try
        {
            switch (var.toLowerCase())
            {
                case "quote_id":
                    setQuote_id(String.valueOf(val));
                    break;
                case "resource_id":
                    setResource_id(String.valueOf(val));
                    break;
                case "item_number":
                    setItem_number(Integer.valueOf((String)val));
                    break;
                case "additional_costs":
                    setAdditional_costs((String)val);
                    break;
                case "quantity":
                    setQuantity(Integer.valueOf((String)val));
                    break;
                case "unit_cost":
                    setUnit_cost(Double.valueOf((String)val));
                    break;
                case "markup":
                    setMarkup(Double.parseDouble((String) val));
                    break;
                case "category":
                    setCategory((String) val);
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
            case "quote_id":
                return getQuote_id();
            case "resource_id":
                return getResource_id();
            case "item_number":
                return getItem_number();
            case "equipment_description":
                return getEquipment_description();
            case "additional_costs":
                return getAdditional_costs();
            case "unit":
                return getUnit();
            case "quantity":
                return getQuantityValue();
            case "unit_cost":
                return getUnit_Cost();
            case "value":
                return getCurrentValue();
            case "markup":
                return getMarkupValue();
            case "category":
                return getCategory();
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"resource_id\":\""+resource_id+"\""
                +",\"quote_id\":\""+quote_id+"\""
                +",\"item_number\":\""+item_number+"\""
                +",\"quantity\":\""+quantity+"\""
                +",\"unit_cost\":\""+unit_cost+"\""
                +",\"markup\":\""+markup+"\"";
        if(getCategory()!=null)
                json_obj+=",\"category\":\""+category+"\"";
        if(getAdditional_costs()!=null)
                json_obj+=",\"additional_costs\":\""+additional_costs+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }

    @Override
    public String toString()
    {
        Quote quote = getQuote();
        String str = "#" + getObject_number();
        if(quote!=null)
            str += ", for quote " + quote.toString() + "";
        return str;
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/quote/resource";
    }
}