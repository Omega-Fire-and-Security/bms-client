package fadulousbms.model;

import fadulousbms.auxilary.IO;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ghost on 2017/02/24.
 */
public class FileMetadata extends BusinessObject implements Serializable
{
    private String filename;
    private String label;
    private String path;
    private long date_logged;
    private String content_type;
    private String extra;//{"logo_options":{}, "required":false}
    public static final String TAG = "FileMetadata";

    public StringProperty labelProperty(){return new SimpleStringProperty(label);}

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public StringProperty pathProperty(){return new SimpleStringProperty(path);}

    public String getPath()
    {
        return path;
    }

    public void setPath(String path)
    {
        this.path = path;
    }

    public StringProperty content_typeProperty(){return new SimpleStringProperty(content_type);}

    public String getContent_type()
    {
        return content_type;
    }

    public void setContent_type(String content_type)
    {
        this.content_type = content_type;
    }

    public long getDate_logged()
    {
        return date_logged;
    }

    public void setDate_logged(long date_logged)
    {
        this.date_logged = date_logged;
    }

    public StringProperty extraProperty(){return new SimpleStringProperty(extra);}

    public String getExtra()
    {
        return extra;
    }

    public void setExtra(String extra)
    {
        this.extra = extra;
    }

    @Override
    public void parse(String var, Object val)
    {
        switch (var.toLowerCase())
        {
            case "filename":
                filename = (String)val;
                break;
            case "label":
                label=(String)val;
                break;
            case "path":
                path=(String)val;
                break;
            case "content_type":
                content_type=(String)val;
                break;
            case "extra":
                extra=(String) val;
                break;
            default:
                IO.log(TAG, IO.TAG_ERROR, "unknown "+TAG+" attribute '" + var + "'");
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "filename":
                return filename;
            case "label":
                return label;
            case "path":
                return path;
            case "content_type":
                return content_type;
            case "extra":
                return extra;
            default:
                return null;
        }
    }

    @Override
    public String asUTFEncodedString()
    {
        //Return encoded URL parameters in UTF-8 charset
        StringBuilder result = new StringBuilder();
        try
        {
            result.append(URLEncoder.encode("label","UTF-8") + "="
                    + URLEncoder.encode(label, "UTF-8") + "&");
            result.append(URLEncoder.encode("path","UTF-8") + "="
                    + URLEncoder.encode(getPath(), "UTF-8") + "&");
            result.append(URLEncoder.encode("content_type","UTF-8") + "="
                    + URLEncoder.encode(getContent_type(), "UTF-8") + "&");
            if(getDate_logged()>0)
                result.append(URLEncoder.encode("date_logged","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getDate_logged()), "UTF-8") + "&");
            if(getExtra()!=null)
                result.append(URLEncoder.encode("extra","UTF-8") + "="
                        + URLEncoder.encode(String.valueOf(getExtra()), "UTF-8") + "&");

            return result.toString();
        } catch (UnsupportedEncodingException e)
        {
            IO.log(TAG, IO.TAG_ERROR, e.getMessage());
        }
        return null;
    }

    @Override
    public String apiEndpoint()
    {
        return "/file";
    }

    //Additional methods
    /*static int partition(FileMetadata arr[], int left, int right)
    {
        int i = left, j = right;
        FileMetadata tmp;
        double pivot = arr[(left + right) / 2].getIndex();

        while (i <= j)
        {
            while (arr[i].getIndex() < pivot)
                i++;
            while (arr[j].getIndex() > pivot)
                j--;
            if (i <= j)
            {
                tmp = arr[i];
                arr[i] = arr[j];
                arr[j] = tmp;
                i++;
                j--;
            }
        }

        return i;
    }

    public static void quickSort(FileMetadata arr[], int left, int right)
    {
        int index = partition(arr, left, right);
        if (left < index - 1)
            quickSort(arr, left, index - 1);
        if (index < right)
            quickSort(arr, index, right);
    }*/
}

