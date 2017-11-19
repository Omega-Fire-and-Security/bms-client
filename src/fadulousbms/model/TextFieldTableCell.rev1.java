package fadulousbms.model;

import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Callback;

/**
 * Created by ghost on 2017/07/28.
 */
class TextFieldCellFactory<S,String> implements Callback<TableColumn<S, String>, TableCell<S, String>>
{

    @Override
    public TableCell<S, String> call(TableColumn<S, String> param)
    {
        TextFieldTableCellOld textFieldCell = new TextFieldTableCellOld();
        return textFieldCell;
    }
}

class TextFieldTableCellOld<S,String> extends TableCell<S, String>
{

    private TextField textField;
    private StringProperty boundToCurrently = null;
    private String newval;

    public TextFieldTableCellOld()
    {


        textField = new TextField();
        textField.setOnKeyPressed(new EventHandler<KeyEvent>()
        {
            @Override
            public void handle(KeyEvent t)
            {
                if (t.getCode() == KeyCode.ENTER)
                {
                    System.out.println("key pressed");
                    //commitEdit(textField.getText());

                } else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });

        /*textField.textProperty().addListener(new ChangeListener<String>()
        {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue)
            {
                // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.

                // commitEdit(newValue);
                System.out.println("" + newValue);
                newval = newValue;
            }

        });*/

        textField.focusedProperty().addListener(new ChangeListener<Boolean>()
        {

            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if(!newValue){
                    System.out.println("losing focus" + newval);
                    //commichange();
                    //commitEdit(textField.getText());
                }

            }
        });

        this.setGraphic(textField);
    }

    @Override
    protected void updateItem(String item, boolean empty)
    {
        super.updateItem(item, empty);
        if (!empty)
        {
            // Show the Text Field
            this.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.setText(item.toString());
        } else this.setContentDisplay(ContentDisplay.TEXT_ONLY);
    }
}
