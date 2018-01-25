package fadulousbms.auxilary;

/**
 * Created by ghost on 2017/04/06.
 */
public enum AccessLevels
{
    NO_ACCESS(0),
    STANDARD(1),
    ADMIN(2),
    SUPERUSER(3);

    private int level;

    private AccessLevels(int level)
    {
        this.level=level;
    }

    public int getLevel()
    {
        return this.level;
    }
}
