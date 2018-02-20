package fadulousbms.model;

import fadulousbms.auxilary.IO;
import fadulousbms.auxilary.RemoteComms;
import fadulousbms.model.BusinessObject;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableCell;

import javax.swing.*;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by ghost on 2017/01/07.
 */
public class DatePickerCell extends TableCell<BusinessObject, Long>
{
    private final SimpleDateFormat formatter;
    private final DatePicker datePicker;
    private String property, api_method;

    public DatePickerCell(String property, boolean editable)
    {
        this.property = property;
        this.api_method = "";

        formatter = new SimpleDateFormat("yyyy-MM-dd");
        datePicker = new DatePicker();
        //datePicker.setEditable(editable);
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

    public DatePickerCell(String property, String api_method)
    {
        this.property = property;
        this.api_method = api_method;

        formatter = new SimpleDateFormat("yyyy-MM-dd");
        datePicker = new DatePicker();

        datePicker.setOnAction(event ->
        {
            if(!isEmpty())
            {
                commitEdit(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
                updateItem(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond(), isEmpty());
            }
        });
        /*datePicker.addEventFilter( EventType.ROOT, event ->
        {
            //System.out.println("DatepickerCell event!");
            /*if (event.getCode() == KeyEvent.VK_ENTER || event.getKeyCode() == KeyEvent.VK_TAB)
            {
                datePicker.setValue(datePicker.getConverter().fromString(datePicker.getEditor().getText()));
                commitEdit(datePicker.getValue().atStartOfDay(ZoneId.systemDefault()).toEpochSecond());
            }
            if (event.getKeyCode() == KeyEvent.VK_ESCAPE)
            {
                cancelEdit();
            }
        });*/


        /*datePicker.setDayCellFactory(picker ->
        {
            DateCell cell = new DateCell();
            cell.addEventFilter(MouseEvent.MOUSE_CLICKED, event ->
            {
                datePicker.setValue(cell.getItem());
                if (event.getClickCount() == 2)
                {
                    datePicker.hide();
                    //commitEdit(MonthDay.from(cell.getItem()));
                }
                event.consume();
            });
            /*cell.addEventFilter(KeyEvent.KEY_PRESSED, event ->
            {
                System.out.println(datePicker.getValue());
                /*if (event.get == KeyCode.ENTER) {
                    commitEdit(MonthDay.from(datePicker.getValue()));
                }*
            });*
            return cell ;
        });*/

        /*contentDisplayProperty().bind(Bindings.when(editingProperty())
                .then(ContentDisplay.CENTER)
                .otherwise(ContentDisplay.TEXT_ONLY));*/
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
