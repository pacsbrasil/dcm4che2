package com.tiani.prnscp.client.ddl2odgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class ODCurve
{
    float[] ods;
    float max;
    Color color;
    
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
    
    public void draw(Graphics2D g, Rectangle rect)
    {
        final int x0 = (int)rect.getX();
        final int y0 = (int)rect.getY();
        final int width = (int)rect.getWidth();
        final int height = (int)rect.getHeight();
        final float fx = (float)width/ods.length;
        final float fy = (float)height/max;
        int lastx = x0, lasty = y0;
        int x, y;
        
        g.setColor(color);
        for (int i=0; i<ods.length; i++) {
            x = x0 + (int)(i*fx + 0.5f);
            y = y0 + (int)(ods[i]*fy + 0.5f);
            g.drawLine(lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }
    }
}
