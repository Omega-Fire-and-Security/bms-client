package fadulousbms.auxilary;

public abstract class ServerObject
{
    Link _links;
    Page page;

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
