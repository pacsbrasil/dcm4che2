package com.tiani.prnscp.client.ddl2odgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;

public class ODCurveGUIPanel extends JPanel
{
    private final Color[] ColorPool = { Color.BLACK, Color.RED, Color.BLUE, Color.CYAN,
                                         Color.GREEN, Color.MAGENTA, Color.YELLOW };
    private int nextColor = 0;
    private final int NumODEntries = 256;
    
    private Collection curves;
    
    ODCurveGUIPanel()
    {
        curves = new LinkedList();
    }
    
    final AffineTransform rot90 = new AffineTransform(0, -1, 1, 0, 0, 0);
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        
        //bkgnd
        g2.setColor(Color.WHITE);
        g2.fillRect(0,0,getWidth(),getHeight());
        //draw axis labels
        AffineTransform savedTx = g2.getTransform();
        g2.setColor(Color.BLACK);
        //g2.rotate(3.14159f/2, getWidth()/2,getHeight()/2);
        //g2.translate(-getWidth()/2,-getHeight()/2);
        g2.translate(25, getHeight()/2);
        g2.transform(rot90);
        g2.drawString("OD", 0, 0);
        g2.setTransform(savedTx);
        g2.drawString("DDL", getWidth()/2, getHeight()-25);
        //draw curves
        final Rectangle rect = new Rectangle(50, 50, getWidth()-100, getHeight()-100);
        g2.draw(rect);
        Iterator i = curves.iterator();
        while (i.hasNext())
            ((ODCurve)i.next()).draw(g2, rect);
    }
    
    public void loadCurve(File file)
        throws FileNotFoundException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        int index = 0;
        String line;
        float[] ods = new float[NumODEntries];
        
        try {
            while ((line = in.readLine()) != null) {
                if (index < NumODEntries)
                    ods[index++] = Float.parseFloat(line);
            }
        }
        catch (IOException ioe) {
            return;
        }
        finally {
            try { in.close(); } catch (IOException ioe) {}
        }
        
        //create a curve and add it
        ODCurve curve = new ODCurve(ods, ColorPool[(nextColor++)%ColorPool.length]);
        curves.add(curve);
    }
}
