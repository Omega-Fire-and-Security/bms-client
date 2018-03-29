package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

import java.io.IOException;
import java.util.*;

/**
 * Created by th3gh0st on 2017/01/09.
 * @author th3gh0st
 */

public class ComboBoxTableCell<T extends ApplicationObject> extends TableCell<T, String>
{
    private ComboBox<ApplicationObject> comboBox;
    private String update_property;
    private HashMap<String, T> app_objects;
    public static final String TAG = "ComboBoxTableCell";

    public ComboBoxTableCell(HashMap<String, T> app_objects, String update_property, String comparator_property)
    {
        super();
        this.update_property = update_property;
        this.app_objects = app_objects;

        if(app_objects ==null)
        {
            IO.log(TAG, IO.TAG_WARN, "business objects list for the combo box cannot be null!");
            return;
        }
        if(app_objects.size()<=0)
        {
            IO.log(TAG, IO.TAG_WARN, "business objects list for the combo box cannot be empty!");
            return;
        }

        comboBox = new ComboBox<>(FXCollections.observableArrayList(app_objects.values()));
        HBox.setHgrow(comboBox, Priority.ALWAYS);

        comboBox.valueProperty().addListener((observable, oldValue, newValue) ->
        {
            //commit only if a change was made by the user
            if(comboBox.isShowing() || comboBox.isFocused() && oldValue!=null)
                if(newValue.get(comparator_property)!=null)
                    commitEdit((String) newValue.get(comparator_property));
                else IO.log(TAG, IO.TAG_WARN, "object [" + newValue + "] parser returned null for property [" + comparator_property + "]." );
            else IO.log(TAG, IO.TAG_WARN, "no changes were made to "+newValue.getClass()+"'s "+comparator_property+" attribute, not committing to server.");
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
                if (getTableRow().getItem() instanceof ApplicationObject)
                {
                    ApplicationObject row_item = (ApplicationObject) getTableRow().getItem();

                    if (row_item != null)
                    {
                        //String update_property_val = comboBox.getValue().get_id();
                        try
                        {
                            row_item.parse(update_property, selected_id);
                            row_item.getManager().patchObject(row_item, param -> null);
                            //RemoteComms.updateBusinessObjectOnServer(row_item, update_property);
                            IO.log(TAG, IO.TAG_INFO, "updated business object: " + "[" + row_item.getClass().getName() + "]'s "
                                                            + update_property + " property to [" + selected_id + "].");
                        } catch (ParseException e)
                        {
                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                            e.printStackTrace();
                        } catch (IOException e)
                        {
                            IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                            e.printStackTrace();
                        }
                    } else IO.log(TAG, IO.TAG_WARN, "row business object is not set.");
                } else IO.log(TAG, IO.TAG_WARN, String.format("unknown row object type: " + getTableRow().getItem()));
            } else IO.log(TAG, IO.TAG_WARN, "selected row is not set.");
        } else IO.log(TAG, IO.TAG_WARN, "selected combo box object id is not set.");
    }

    @Override
    protected void updateItem(String selected_id, boolean empty)
    {
        super.updateItem(selected_id, empty);
        if (getTableRow() != null)
        {
            if (getTableRow().getItem() instanceof ApplicationObject)
            {
                ApplicationObject row_item = (ApplicationObject) getTableRow().getItem();
                //get the property value of the table row item, i.e client_id, supplier_id etc.
                String upd_id = (String)row_item.get(update_property);

                //use the property value to get selected object for combo box
                if(app_objects !=null && upd_id!=null)
                {
                    ApplicationObject selected_cbx_item = app_objects.get(upd_id);
                    if (selected_cbx_item != null)
                        comboBox.setValue(selected_cbx_item);
                    setGraphic(comboBox);
                }
            } else IO.log(TAG, IO.TAG_WARN, String.format("unknown row object type: " + getTableRow().getItem()));
        } else IO.log(TAG, IO.TAG_WARN, "selected row is null.");
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
    }
}
