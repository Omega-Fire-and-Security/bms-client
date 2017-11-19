package fadulousbms.auxilary;

import au.com.southsky.jfreesane.SaneDevice;
import au.com.southsky.jfreesane.SaneException;
import au.com.southsky.jfreesane.SaneSession;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Created by ghost on 2017/05/07.
 */
public class Sane
{
    public static void scan() throws IOException
    {
        InetAddress address = InetAddress.getByName("192.168.1.106");//scan-client.fadulous.co.za
        SaneSession session = SaneSession.withRemoteSane(address);
        try
        {
            List<SaneDevice> deviceList = session.listDevices();
            System.err.println("Scanner device count: " + deviceList.size());
        } catch (SaneException e)
        {
            IO.logAndAlert(Sane.class.getName(), e.getMessage(), IO.TAG_ERROR);
        }

    }
}
