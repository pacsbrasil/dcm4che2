package com.tiani.prnscp.client.ddl2odgui;

import java.awt.*;

public class ODCurve
{
    private float[] ods;
    private float max;
    private Color color;
    
    ODCurve(float[] ods, Color color)
    {
        if (ods == null)
            throw new NullPointerException("curve data can not be null");
        //set curve data
        this.ods = ods;
        max = ods[0];
        for (int i=1; i<ods.length; i++)
            if (ods[i] > max)
                max = ods[i];
        //color
        if (color == null)
            this.color = Color.BLACK;
        else
            this.color = color;
    }
    
    public float getMax()
    {
        return max;
    }
    
    public void draw(Graphics2D g, Rectangle rect, float maxOD)
    {
        final int x0 = (int)rect.getX();
        final int y0 = (int)rect.getY();
        final int width = (int)rect.getWidth();
        final int height = (int)rect.getHeight();
        final float fx = (float)(width - 1)/(ods.length - 1);
        final float fy = (float)(height - 1)/maxOD;
        int lastx = x0 + (int)(width - 1 + 0.5f);
        int lasty = y0 + (int)(height - 1 - (ods[0] * fy + 0.5f));
        int x, y;
        
        g.setColor(color);
        for (int i=0; i<ods.length; i++) {
            x = x0 + (int)((ods.length - 1 - i) * fx + 0.5f);
            y = y0 + (int)(height - 1 - (ods[i] * fy + 0.5f));
            g.drawLine(lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }
    }
}
