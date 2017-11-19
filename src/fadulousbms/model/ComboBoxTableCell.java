package fadulousbms.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fadulousbms.auxilary.Globals;
import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.managers.SessionManager;
import fadulousbms.model.BusinessObject;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 * Created by ghost on 2017/01/09.
 */

public class ComboBoxTableCell<T extends  BusinessObject> extends TableCell<T, String>
{
    private ComboBox<BusinessObject> comboBox;
    private String update_property, label_property, comparator_property, api_method;
    private HashMap<String, T> business_objects;
    public static final String TAG = "ComboBoxTableCell";

    public ComboBoxTableCell(HashMap<String, T> business_objects, String update_property, String api_method)
    {
        super();
        this.update_property = update_property;
        this.api_method = api_method;
        this.business_objects=business_objects;

        if(business_objects==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects list for the combo box cannot be null!");
            return;
        }
        if(business_objects.size()<=0)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects list for the combo box cannot be empty!");
            return;
        }

        comboBox = new ComboBox<>(FXCollections.observableArrayList(business_objects.values()));
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            //render combo box
            /*if(newValue!=null)
                updateItem(newValue.get_id(), false);
            else return;//if newValue is null, then we don't want anything to do with this update

            IO.log(TAG, IO.TAG_INFO, "updated combo box GUI.");*/

            //commit only if a change by the user was made
            if(comboBox.isShowing() || comboBox.isFocused() && oldValue!=null)
                commitEdit(newValue.get_id());
            else IO.log(TAG, IO.TAG_WARN, "not committing to server.");
        });
    }

    public ComboBoxTableCell(HashMap<String, T> business_objects, String update_property, String comparator_property, String api_method)
    {
        super();
        this.update_property = update_property;
        this.comparator_property = comparator_property;
        this.api_method = api_method;
        this.business_objects=business_objects;

        if(business_objects==null)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects list for the combo box cannot be null!");
            return;
        }
        if(business_objects.size()<=0)
        {
            IO.log(TAG, IO.TAG_ERROR, "business objects list for the combo box cannot be empty!");
            return;
        }

        comboBox = new ComboBox<>(FXCollections.observableArrayList(business_objects.values()));
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            //commit only if a change by the user was made
            if(comboBox.isShowing() || comboBox.isFocused() && oldValue!=null)
                if(newValue.get(comparator_property)!=null)
                    commitEdit((String) newValue.get(comparator_property));
                else IO.log(TAG, IO.TAG_WARN, "object [" + newValue + "] parser returned null for property [" + comparator_property + "]." );
            else IO.log(TAG, IO.TAG_WARN, "not committing to server.");
        });
    }

    @Override
    public void commitEdit(String selected_id)
    {
        super.commitEdit(selected_id);
        IO.log(getClass().getName(), IO.TAG_INFO, "attempting to update object: " + selected_id);
        if(selected_id!=null)
        {
            if (getTableRow() != null)
            {
                if (getTableRow().getItem() instanceof BusinessObject)
                {
                    BusinessObject row_item = (BusinessObject) getTableRow().getItem();

                    if (row_item != null)
                    {
                        //String update_property_val = comboBox.getValue().get_id();
                        row_item.parse(update_property, selected_id);
                        RemoteComms.updateBusinessObjectOnServer(row_item, api_method, update_property);
                        IO.log(TAG, IO.TAG_INFO, "updated business object: " + "[" + row_item + "]'s "
                                + update_property + " property to [" + selected_id + "].");
                    } else
                        IO.log(TAG, IO.TAG_ERROR, "row business object is not set.");
                } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object type: " + getTableRow().getItem()));
            } else IO.log(TAG, IO.TAG_ERROR, "selected row is not set.");
        } else IO.log(TAG, IO.TAG_ERROR, "selected combo box object id is not set.");
    }

    @Override
    protected void updateItem(String selected_id, boolean empty)
    {
        super.updateItem(selected_id, empty);
        if (getTableRow() != null)
        {
            if (getTableRow().getItem() instanceof BusinessObject)
            {
                BusinessObject row_item = (BusinessObject) getTableRow().getItem();
                //get the property value of the table row item, i.e client_id, supplier_id etc.
                String upd_id = (String)row_item.get(update_property);

                //use the property value to get selected object for combo box
                BusinessObject selected_cbx_item = business_objects.get(upd_id);
                if(selected_cbx_item!=null)
                    comboBox.setValue(selected_cbx_item);
                setGraphic(comboBox);
            } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object type: " + getTableRow().getItem()));
        }else IO.log(TAG, IO.TAG_ERROR, "selected row is null.");
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
    }
}
