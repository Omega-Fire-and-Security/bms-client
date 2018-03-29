package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.exceptions.ParseException;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.util.Callback;

/**
 * Created by ghost on 2017/01/09.
 */

public class TextFieldTableCell extends TableCell<BusinessObject, String>
{
    private TextField txt;
    private Label lbl;
    private String property;
    public static final String TAG = "TextFieldTableCell";

    public TextFieldTableCell(String property, Callback callback)
    {
        super();
        this.property = property;

        lbl = new Label();
        txt = new TextField();
        HBox.setHgrow(txt, Priority.ALWAYS);

        txt.setOnKeyPressed(event ->
        {
            if(event.getCode()== KeyCode.ENTER)
            {
                BusinessObject obj = (BusinessObject)getTableRow().getItem();
                try
                {
                    obj.parse(property,txt.getText());

                    //execute callback w/ args
                    if(callback!=null)
                        callback.call(obj);

                    getTableRow().setItem(obj);
                } catch (ParseException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                    //TODO: execute callback w/o args?
                }
            }
        });
    }

    @Override
    public void commitEdit(String selected_id)
    {
        super.commitEdit(selected_id);

        if(selected_id!=null)
        {
            if (getTableRow().getItem() instanceof BusinessObject)
            {
                BusinessObject bo = (BusinessObject) getTableRow().getItem();
                try
                {
                    bo.parse(property, selected_id);
                } catch (ParseException e)
                {
                    IO.logAndAlert("Error", e.getMessage(), IO.TAG_ERROR);
                    e.printStackTrace();
                }
            } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object: " + getTableRow().getItem()));
        }else IO.log(TAG, IO.TAG_ERROR, "selected id is null.");
    }

    @Override
    protected void updateItem(String selected_id, boolean empty)
    {
        super.updateItem(selected_id, empty);

        if (getTableRow().getItem() instanceof BusinessObject)
        {
            BusinessObject bo = (BusinessObject) getTableRow().getItem();
            Object val = bo.get(property);
            if(val!=null)
                txt.setText(val.toString());
            setGraphic(txt);
        } else IO.log(TAG, IO.TAG_ERROR, String.format("unknown row object: " + getTableRow().getItem()));
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
    }
}
