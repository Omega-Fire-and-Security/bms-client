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
public class FileMetadata implements BusinessObject, Serializable
{
    private String _id;
    private double index;
    private String label;
    private String pdf_path;
    private String logo_options;
    private String type;
    private boolean marked;
    private boolean required;
    public static final String TAG = "FileMetadata";

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

    public StringProperty indexProperty(){return new SimpleStringProperty(String.valueOf(index));}

    public double getIndex()
    {
        return index;
    }

    public void setIndex(int index)
    {
        this.index = index;
    }

    public StringProperty labelProperty(){return new SimpleStringProperty(label);}

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public StringProperty pdf_pathProperty(){return new SimpleStringProperty(pdf_path);}

    public String getPdf_path()
    {
        return pdf_path;
    }

    public void setPdf_path(String pdf_path)
    {
        this.pdf_path = pdf_path;
    }

    public StringProperty typeProperty(){return new SimpleStringProperty(type);}

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public StringProperty logo_optionsProperty(){return new SimpleStringProperty(logo_options);}

    public String getLogo_options()
    {
        return logo_options;
    }

    public void setLogo_options(String logo_options)
    {
        this.logo_options = logo_options;
    }

    public StringProperty requiredProperty(){return new SimpleStringProperty(String.valueOf(required));}

    public boolean getRequired(){return required;}

    public void setRequired(boolean required){this.required=required;}

    @Override
    public void parse(String var, Object val)
    {
        switch (var.toLowerCase())
        {
            case "index":
                index = Double.parseDouble((String) val);
                break;
            case "label":
                label=(String)val;
                break;
            case "pdf_path":
                pdf_path=(String)val;
                break;
            case "logo_options":
                logo_options=(String)val;
                break;
            case "type":
                type=(String)val;
                break;
            case "required":
                required=(Boolean) val;
                break;
            default:
                IO.log(TAG, IO.TAG_ERROR, "unknown FileMetadata attribute '" + var + "'");
                break;
        }
    }

    @Override
    public Object get(String var)
    {
        switch (var.toLowerCase())
        {
            case "index":
                return index;
            case "label":
                return label;
            case "pdf_path":
                return pdf_path;
            case "logo_options":
                return logo_options;
            case "type":
                return type;
            case "required":
                return required;
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
            result.append(URLEncoder.encode("index","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(index), "UTF-8") + "&");
            result.append(URLEncoder.encode("label","UTF-8") + "="
                    + URLEncoder.encode(label, "UTF-8") + "&");
            result.append(URLEncoder.encode("required","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(required), "UTF-8") + "&");
            result.append(URLEncoder.encode("pdf_path","UTF-8") + "="
                    + URLEncoder.encode(pdf_path, "UTF-8") + "&");
            result.append(URLEncoder.encode("type","UTF-8") + "="
                    + URLEncoder.encode(type, "UTF-8") + "&");
            result.append(URLEncoder.encode("logo_options","UTF-8") + "="
                    + URLEncoder.encode(String.valueOf(logo_options), "UTF-8"));

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
    static int partition(FileMetadata arr[], int left, int right)
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
    }
}

