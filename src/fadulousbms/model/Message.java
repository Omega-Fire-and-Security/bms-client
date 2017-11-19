package fadulousbms.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by ghost on 2017/02/24.
 */
public class Message
{
    private String _id;
    private String message;

    public static final String TAG = "Message";

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    public StringProperty messageProperty(){return new SimpleStringProperty(message);}

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }
}

