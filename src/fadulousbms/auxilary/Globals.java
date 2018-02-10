package fadulousbms.auxilary;

/**
 * Created by ghost on 2017/01/18.
 */
public enum Globals
{
    COMPANY("Airotek Engineering"),
    APP_NAME("Enterprise Resource Engine"),
    DEBUG_WARNINGS("on"),
    DEBUG_INFO("on"),
    DEBUG_VERBOSE("on"),
    CURRENCY_SYMBOL("R"),
    DEBUG_ERRORS("on");

    private String value;

    Globals(String value){this.value=value;}

    public String getValue(){return this.value;}
}
