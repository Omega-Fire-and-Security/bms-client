package fadulousbms.model;

/**
 * Created by ghost on 2017/01/04.
 */
public interface BusinessObject
{
    String get_id();
    void set_id(String id);
    String getShort_id();
    void parse(String var, Object val);
    Object get(String var);
    boolean isMarked();
    void setMarked(boolean marked);
    String apiEndpoint();
    String asUTFEncodedString();
}
