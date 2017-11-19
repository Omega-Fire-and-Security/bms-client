package fadulousbms.auxilary;

import javafx.scene.Node;
import javafx.scene.control.TextField;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ghost on 2017/01/11.
 */
public class Validators
{
    public static boolean isValidNode(Node node, String text, int len, String regex)
    {
        //node.getStyleClass().clear();
        if(!validateString(text, len, regex))
        {
            node.getStyleClass().remove("form-control-default");
            node.getStyleClass().add("control-input-error");
            return false;
        }
        node.getStyleClass().remove("control-input-error");
        node.getStyleClass().add("form-control-default");
        return true;
    }

    public static boolean isValidNode(Node node, String text, String regex)
    {
        //node.getStyleClass().clear();
        if(!validateString(text, regex))
        {
            node.getStyleClass().remove("form-control-default");
            node.getStyleClass().add("control-input-error");
            return false;
        }
        node.getStyleClass().remove("control-input-error");
        node.getStyleClass().add("form-control-default");
        return true;
    }

    public static boolean validateString(String str, int len, String regex)
    {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        if(str.length()<len)
            return false;
        if(regex!=null)
            return true;
        if(regex.isEmpty())
            return true;
        return matcher.matches();
    }

    public static boolean validateString(String str, String regex)
    {
        Matcher matcher = Pattern.compile(regex).matcher(str);
        return matcher.matches();
    }
}
