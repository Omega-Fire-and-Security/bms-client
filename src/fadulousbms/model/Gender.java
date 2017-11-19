package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by ghost on 2017/01/29.
 */
public class Gender implements BusinessObject
{
    private boolean marked;
    private String _id;
    public static final String TAG = "Gender";

    public StringProperty idProperty(){return new SimpleStringProperty(_id);}

    @Override
    public String get_id()
    {
        return _id;
    }

    public void set_id(String _id)
    {
        this._id = _id;
    }

    public StringProperty short_idProperty(){return new SimpleStringProperty(_id.substring(0, 8));}

    @Override
    public String getShort_id()
    {
        return _id.substring(0, 8);
    }

    @Override
    public boolean isMarked()
    {
        return marked;
    }

    @Override
    public void setMarked(boolean marked){this.marked=marked;}

    @Override
    public void parse(String var, Object val)
    {
        switch (var.toLowerCase())
        {
            case "gender":
                set_id((String)val);
                break;
            default:
                IO.log(TAG, IO.TAG_WARN, String.format("unknown Gender attribute '%s'", var));
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "gender":
                return this._id;
            default:
                IO.log(TAG, IO.TAG_WARN, String.format("unknown Gender attribute '%s'", var));
                return null;
        }
    }

    @Override
    public String asUTFEncodedString()
    {
        return "";
    }

    @Override
    public String apiEndpoint()
    {
        return null;
    }
}
