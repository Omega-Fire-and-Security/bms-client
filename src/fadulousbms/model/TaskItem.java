package fadulousbms.model;

import fadulousbms.auxilary.AccessLevel;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import fadulousbms.managers.ApplicationObjectManager;
import fadulousbms.managers.ResourceManager;
import fadulousbms.managers.TaskManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by ghost on 2018/03/22.
 * @author th3gh0st
 */
public class TaskItem extends ApplicationObject implements Serializable
{
    private long quantity;
    private double unit_cost;
    private double markup;
    private String task_id;
    private String resource_id;
    private String category;
    private String serial;
    public static final String TAG = "TaskItem";

    @Override
    public AccessLevel getReadMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public AccessLevel getWriteMinRequiredAccessLevel()
    {
        return AccessLevel.STANDARD;
    }

    @Override
    public ApplicationObjectManager getManager()
    {
        return TaskManager.getInstance();
    }

    public String getTask_id()
    {
        return task_id;
    }

    public void setTask_id(String task_id)
    {
        this.task_id = task_id;
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

    public long getQuantityValue()
    {
        return quantity;
    }

    public void setQuantity(long quantity)
    {
        this.quantity = quantity;
    }

    public double getUnit_cost()
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

    public Task getTask()
    {
        HashMap<String, Task> tasks = TaskManager.getInstance().getDataset();
        if(tasks!=null)
            return tasks.get(getTask_id());
        return null;
    }

    public double getRate()
    {
        double marked_up_cost = getUnit_cost() + getUnit_cost()*(markup/100);
        return marked_up_cost;
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

    public String getSerial()
    {
        return serial;
    }

    public void setSerial(String serial)
    {
        this.serial = serial;
    }

    public double getTotal()
    {
        return getRate()*getQuantityValue();
    }

    // TaskItem Model Properties

    public StringProperty task_idProperty(){return new SimpleStringProperty(task_id);}
    public StringProperty resource_idProperty(){return new SimpleStringProperty(resource_id);}
    public StringProperty descriptionProperty(){return new SimpleStringProperty(getEquipment_description());}
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
    public StringProperty serialProperty()
    {
        return new SimpleStringProperty(String.valueOf(getSerial()));
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
                case "task_id":
                    setTask_id(String.valueOf(val));
                    break;
                case "resource_id":
                    setResource_id(String.valueOf(val));
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
                case "serial":
                    setSerial((String) val);
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
            case "task_id":
                return getTask_id();
            case "resource_id":
                return getResource_id();
            case "equipment_description":
                return getEquipment_description();
            case "unit":
                return getUnit();
            case "quantity":
                return getQuantityValue();
            case "cost":
            case "unitcost":
            case "unit_cost":
                return getUnit_cost();
            case "value":
                return getCurrentValue();
            case "markup":
                return getMarkupValue();
            case "category":
                return getCategory();
            case "serial":
                return getSerial();
        }
        return super.get(var);
    }

    @Override
    public String getJSONString()
    {
        String super_json = super.getJSONString();
        String json_obj = super_json.substring(0, super_json.length()-1)//toString().length()-1 to ignore the last brace.
                +",\"resource_id\":\""+resource_id+"\""
                +",\"task_id\":\""+task_id+"\""
                +",\"quantity\":\""+quantity+"\""
                +",\"unit_cost\":\""+unit_cost+"\""
                +",\"markup\":\""+markup+"\"";
        if(getCategory()!=null)
                json_obj+=",\"category\":\""+category+"\"";
        if(getSerial()!=null)
            json_obj+=",\"serial\":\""+serial+"\"";
        json_obj+="}";

        IO.log(getClass().getName(),IO.TAG_INFO, json_obj);
        return json_obj;
    }


    @Override
    public String toString()
    {
        String str = "#" + getObject_number() + ", " + getEquipment_description();
        if(getTask()!=null)
            str+= ", for task " + getTask().toString();
        return str;
    }

    /**
     * @return this model's root endpoint URL.
     */
    @Override
    public String apiEndpoint()
    {
        return "/task/resource";
    }
}