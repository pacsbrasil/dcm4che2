package com.tiani.prnscp.client.ddl2odgui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ODCurve
{
    static interface ODCurveOperation
    {
        float operate(float v1, float v2);
    }
    
    private float[] ods;
    private float max;
    private Color color;
    private boolean selected = false;
    
    ODCurve(float[] ods, Color color)
    {
        if (ods == null)
            throw new NullPointerException("Curve data can not be null");
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
    
    static ODCurve newCurveFromComparison(ODCurve curve1, ODCurve curve2,
                                   ODCurveOperation cmp, Color color)
    {
        final int maxind = Math.min(curve1.ods.length, curve2.ods.length) - 1;
        float[] result = new float[maxind + 1];
        
        for (int i = 0; i < maxind; i++) {
            result[i] = cmp.operate(curve1.ods[i], curve2.ods[i]);
        }
        
        return new ODCurve(result, color);
    }
    
    public float getMax()
    {
        return max;
    }
    
    public float[] getData()
    {
        return ods;
    }
    
    public void setSelected(boolean selected)
    {
        this.selected = selected;
    }
    
    public boolean isSelected()
    {
        return selected;
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
        g.setStroke(new BasicStroke(2));
        for (int i=0; i<ods.length; i++) {
            x = x0 + (int)((ods.length - 1 - i) * fx + 0.5f);
            y = y0 + (int)(height - 1 - (ods[i] * fy + 0.5f));
            g.drawLine(lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }
        g.setStroke(new BasicStroke(1));
        g.setColor(Color.BLACK);
        if (selected) {
            for (int i=0; i<ods.length; i++) {
                x = x0 + (int)((ods.length - 1 - i) * fx + 0.5f);
                y = y0 + (int)(height - 1 - (ods[i] * fy + 0.5f));
                g.drawRect(x, y, 1, 1);
            }
        }
    }
}
