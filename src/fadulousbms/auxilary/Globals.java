package fadulousbms.auxilary;

/**
 * Created by ghost on 2017/01/18.
 */
public enum Globals
{
    APP_NAME("Enterprise Resource Engine"),
    COMPANY("Omega Fire & Security"),
    COMPANY_SECTOR("(Pty) Ltd"),
    DEBUG_WARNINGS("on"),
    DEBUG_INFO("on"),
    DEBUG_VERBOSE("on"),
    CURRENCY_SYMBOL("R"),
    DEBUG_ERRORS("on");

    private String value;

    Globals(String value){this.value=value;}

    public String getValue(){return this.value;}
}
