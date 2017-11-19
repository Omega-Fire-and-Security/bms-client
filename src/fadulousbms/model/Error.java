package fadulousbms.model;

/**
 * Created by ghost on 2017/02/19.
 */
public class Error
{
    private String error;

    public Error()
    {
        this.error="Error";
    }

    public Error(String error)
    {
        this.error=error;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error=error;
    }
}
