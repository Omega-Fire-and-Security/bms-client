package fadulousbms.auxilary;

/**
 * Created by th3gh0st on 2017/12/27.
 * @author th3gh0st
 */

public abstract class ServerResponseObject
{
    protected Link _links;
    protected Page page;
    //protected Embedded _embedded;

    /*public Embedded get_embedded()
    {
        return _embedded;
    }

    public void set_embedded(Embedded _embedded)
    {
        this._embedded = _embedded;
    }*/

    public Link get_links()
    {
        return _links;
    }

    public void set_links(Link _links)
    {
        this._links = _links;
    }

    public Page getPage()
    {
        return page;
    }

    public void setPage(Page page)
    {
        this.page = page;
    }
}
