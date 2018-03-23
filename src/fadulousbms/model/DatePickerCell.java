package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Created by ghost on 2017/01/07.
 */
public class DatePickerCell extends TableCell<BusinessObject, Long>
{
    private final SimpleDateFormat formatter;
    private final DatePicker datePicker;
    private String property;

    public DatePickerCell(String property, boolean editable)
    {
        this.property = property;

        formatter = new SimpleDateFormat("yyyy-MM-dd");
        datePicker = new DatePicker();

        datePicker.setDisable(!editable);

        datePicker.setOnAction(event ->
        {
            if(editable)
            {
                if (!isEmpty())
                {
                    updateItem(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(), isEmpty());
                    commitEdit(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
                }
            }
        });
    }

    @Override
    public void commitEdit(Long newValue)
    {
        super.commitEdit(newValue);
        BusinessObject bo = (BusinessObject) getTableRow().getItem();
        if(bo!=null)
        {
            bo.parse(property, newValue);
            RemoteComms.updateBusinessObjectOnServer(bo, property);
        }else IO.log(getClass().getName(), IO.TAG_WARN, "TableRow BusinessObject is null.");
    }

    @Override
    protected void updateItem(Long date, boolean empty)
    {
        super.updateItem(date, empty);
        if (empty || date==null)
        {
            setText(null);
            setGraphic(null);
        } else
        {
            datePicker.setValue(LocalDate.parse(formatter.format(new Date(date))));
            //setText(formatter.format(new Date(date)));
            setGraphic(datePicker);
        }
    }

    @Override
    public void startEdit()
    {
        super.startEdit();
        if (!isEmpty())
        {
            //datePicker.setValue(getItem().atYear(LocalDate.now().getYear()));
        }
    }
}
