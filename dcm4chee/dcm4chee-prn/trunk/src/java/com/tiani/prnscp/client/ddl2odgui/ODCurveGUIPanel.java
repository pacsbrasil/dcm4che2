package com.tiani.prnscp.client.ddl2odgui;

import java.util.*;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import javax.swing.*;

import org.apache.log4j.Logger;

import com.tiani.prnscp.print.CalibrationException;
import com.tiani.prnscp.print.ScannerCalibration;

public class ODCurveGUIPanel extends JPanel
{
    private final Color[] ColorPool = { Color.ORANGE, Color.RED, Color.BLUE, Color.CYAN,
                                        Color.GREEN, Color.MAGENTA, Color.YELLOW };
    private int nextColor = 0;
    
    private final ScannerCalibration sc = new ScannerCalibration(Logger.getRootLogger());
    private final int NumODEntries = 256;
    private ODCurve refCurve = null;
    private File refCurveFile = null;
    private float maxODAllCurves = 0;
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
        g2.setClip(-1000,-1000,2000,2000);
        g2.setColor(Color.WHITE);
        g2.fillRect(0,0,getWidth(),getHeight());

        //graph
        final int fontHeight = 10;
        final Rectangle rect = new Rectangle(50, 50, getWidth()-100, getHeight()-100);
        g2.draw(rect);

        //draw axis labels
        final String XLabel = "Device Driving Level (DDL)";
        final String YLabel = "Optical Density (OD)";
        AffineTransform savedTx = g2.getTransform();
        g2.setColor(Color.BLACK);
        g2.translate((int)rect.getX(), (int)(rect.getY() + rect.getHeight()));
        g2.transform(rot90);
        //g2.rotate(3.14159/2.0);
        g2.drawString(YLabel, 0, 0);
        //g2.drawChars("Optical Density (OD)".toCharArray(), 0, "Optical Density (OD)".length(),0,0);
        g2.setTransform(savedTx);
        g2.drawString(XLabel, (int)rect.getX(), (int)(rect.getY() + rect.getHeight()) + fontHeight);

        //draw curves, if any are loaded
        if (refCurve != null) {
            //find maximum OD for all curve for scaling the graph
            Iterator i = curves.iterator();
            float tmp;
            maxODAllCurves = refCurve.getMax();
            while (i.hasNext())
                if (maxODAllCurves < (tmp = ((ODCurve)i.next()).getMax()))
                    maxODAllCurves = tmp;

            //draw x-axis
            g2.setColor(Color.GRAY);
            for (int k = 0, p = (int)rect.getX(); k < 8; k++) {
                g2.drawLine(p, (int)rect.getY(),
                           p, (int)(rect.getY() + rect.getHeight()));
                p += rect.getWidth() / 8;
            }
            g2.drawString("0", (int)rect.getX(),
                          (int)(rect.getY() + rect.getHeight() + fontHeight));
            g2.drawString("255", (int)rect.getX(),
                          (int)(rect.getY() + rect.getHeight() + fontHeight));

            //draw y-axis
            int yval;
            for (float k = 0; k < maxODAllCurves; k += 0.25f) {
                yval = (int)(rect.getY() + rect.getHeight() - 1
                       - (rect.getHeight() - 1) * k / maxODAllCurves
                       + 0.5f);
                g2.drawLine((int)(rect.getX()), yval,
                           (int)(rect.getX() + rect.getWidth()), yval);
                g2.drawString(Float.toString(k),
                              0,
                              yval);
            }

            //draw curves
            i = curves.iterator();
            while (i.hasNext())
                ((ODCurve)i.next()).draw(g2, rect, maxODAllCurves);
            refCurve.draw(g2, rect, maxODAllCurves);
        }
    }
    
    void loadCurve(File file)
        throws FileNotFoundException, Exception, NumberFormatException
    {
        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        int index = 0;
        String line;
        float[] ods = new float[NumODEntries];
        
        try {
            while ((line = in.readLine()) != null) {
                if (index < NumODEntries)
                    ods[index++] = Float.parseFloat(line);
                else
                    throw new Exception("Invalid OD Curve: More than 256 entries found");
            }
            if (index < NumODEntries)
                throw new Exception("Invalid OD Curve: "
                                    + index + " entries found, need "
                                    + NumODEntries);
        }
        catch (IOException ioe) {
            return;
        }
        finally {
            try { in.close(); } catch (IOException ioe) {}
        }
        
        //create a curve and add it
        ODCurve curve = new ODCurve(ods, ColorPool[(nextColor++) % ColorPool.length]);
        curves.add(curve);
    }
    
    void loadScannedImageCurve(File file)
        throws IOException, CalibrationException
    {
        file = file.getParentFile();
        sc.setScanDir(file);
        
        float[] calcdOds = sc.calculateGrayscaleODs();

        //make sure that the referenced OD is the same, otherwise
        // remove all curves and reload reference curve before adding
        // this one
        if (refCurve != null && !refCurveFile.equals(sc.getRefODsFile())) {
            curves = new LinkedList();
            refCurve = null;
        }
        
        //if reference curve used wasn't loaded, load it now
        if (refCurve == null) {
            refCurve = new ODCurve(sc.getRefODs(), Color.BLACK);
            refCurveFile = sc.getRefODsFile();
        }
        
        //create a curve and add it
        ODCurve curve = new ODCurve(calcdOds, ColorPool[(nextColor++) % ColorPool.length]);
        curves.add(curve);
    }
}
