package fadulousbms.auxilary;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by ghost on 2017/02/24.
 */
public class Canvas extends JPanel
{
    private BufferedImage image;
    private int w,h;

    public Canvas(BufferedImage img, int w, int h)
    {
        this.image=img;
        this.w = w;
        this.h = h;
    }

    @Override
    protected void paintComponent(Graphics graphics)
    {
        graphics.drawImage(image, 0,0,w,h,null);
    }

    public void setWidth(int w)
    {
        this.w=w;
    }

    public void setHeight(int h)
    {
        this.h=h;
    }
}
