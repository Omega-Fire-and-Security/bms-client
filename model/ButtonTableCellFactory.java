package fadulousbms.model;

import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

/**
 * Created by ghost on 2017/07/25.
 */

public class ButtonTableCellFactory <S, String> implements Callback<TableColumn<S, String>, TableCell<S, String>>
{
    @Override
    public TableCell<S, String> call(TableColumn<S, String> param)
    {
        return new ButtonTableCell();
    }
}

class ButtonTableCell<S,String> extends TableCell<S, String>
{

    public ButtonTableCell()
    {
        //super((StringConverter<String>)new DefaultStringConverter());
        Button btn = new Button("action");
        btn.setOnAction(event ->
        {
            //commitEdit("clicked");
        });
    }

    @Override
    public void commitEdit(String val)
    {
        super.commitEdit(val);
    }

    /*@Override
    protected void updateItem(S obj, boolean empty)
    {
        super.updateItem(obj, empty);
        System.out.println("");
    }*/
}