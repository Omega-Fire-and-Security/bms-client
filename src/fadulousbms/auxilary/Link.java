package fadulousbms.auxilary;

import java.io.Serializable;

public class Link implements Serializable
{
    Self self;

    public Self getSelf()
    {
        return self;
    }

    public void setSelf(Self self)
    {
        this.self = self;
    }

    public class Self implements Serializable
    {
        String href;

        public String getHref()
        {
            return href;
        }

        public void setHref(String href)
        {
            this.href = href;
        }
    }
}
