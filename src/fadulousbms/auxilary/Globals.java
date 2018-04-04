package fadulousbms.auxilary;

/**
 * Created by ghost on 2017/01/18.
 */
public enum Globals
{
    APP_NAME("Enterprise Resource Engine"),
    COMPANY("Omega Fire & Security"),
    PHYSICAL_ADDRESS("2 JERMYN STREET, ROBERTSHAM, JOHANNESBURG SOUTH, 2091"),
    POSTAL_ADDRESS(""),
    TEL("011 640 0640"),
    WEBSITE("http://www.omegafs.co.za/"),
    EMAIL("admin@omegafs.co.za"),
    REGISTRATION_NUMBER("N/A"),
    TAX_NUMBER("N/A"),
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
