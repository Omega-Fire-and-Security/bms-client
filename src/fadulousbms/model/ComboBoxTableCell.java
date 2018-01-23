package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.util.*;

/**
 * Created by ghost on 2017/01/09.
 */

public class ComboBoxTableCell<T extends  BusinessObject> extends TableCell<T, String>
{
    private ComboBox<BusinessObject> comboBox;
    private String update_property;
    private HashMap<String, T> business_objects;
    public static final String TAG = "ComboBoxTableCell";

    public ComboBoxTableCell(HashMap<String, T> business_objects, String update_property, String comparator_property)
    {
        super();
        this.update_property = update_property;
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
            //commit only if a change was made by the user
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
                        RemoteComms.updateBusinessObjectOnServer(row_item, update_property);
                        IO.log(TAG, IO.TAG_INFO, "updated business object: " + "[" + row_item.getClass().getName() + "]'s "
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
        } else IO.log(TAG, IO.TAG_ERROR, "selected row is null.");
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
    }
}
