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
                             implements ButtonLegendPanel.ButtonLegendCallback
{
    private final Color[] COLOR_POOL = { Color.ORANGE, Color.RED, Color.BLUE, Color.CYAN,
                                        Color.GREEN, Color.MAGENTA, Color.YELLOW };
    private final ODCurve.ODCurveOperation CURVE_OP = new ODCurve.ODCurveOperation()
        {
            public float operate(float v1, float v2)
            {
                return v1 - v2;
            }
        };
    private final Color CURVE_OP_COLOR = new Color(200, 255, 255);

    private int nextColor = 0;
    private ScannerCalibration sc = new ScannerCalibration(Logger.getRootLogger());
    private final int NumODEntries = 256;
    private ODCurve refCurve = null;
    private File refCurveFile = null;
    private float maxODAllCurves = 0;
    private Collection curves;
    private ODCurve opResultCurve = null;
    private ButtonLegendPanel legendPanel;
    
    ODCurveGUIPanel()
    {
        curves = new LinkedList();
    }
    
    private final AffineTransform rot90 = new AffineTransform(0, -1, 1, 0, 0, 0);
    private final String XLabel = "Device Driving Level (DDL)";
    private final String YLabel = "Optical Density (OD)";
    private final String MinDDL = Integer.toString(0);
    private final String MaxDDL = Integer.toString(255);
    private final int fontHeight = getFontMetrics(getFont()).getHeight();
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        
        //bkgnd
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        //graph
        final Rectangle rect = new Rectangle(50, 50,
                                             getWidth() - 100, getHeight() - 100);

        //draw this difference curve way before everything else
        if (opResultCurve != null)
            opResultCurve.draw(g2, rect, maxODAllCurves);

        g2.setColor(Color.BLACK);
        g2.draw(rect);

        //draw axis labels
        AffineTransform savedTx = g2.getTransform();
        g2.translate((int)rect.getX() - 2, (int)(rect.getY() + rect.getHeight()));
        g2.transform(rot90);
        g2.drawString(YLabel, 1, 0);
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
            g2.drawString(MinDDL, (int)rect.getX(),
                          (int)(rect.getY() + rect.getHeight() + 3*fontHeight));
            g2.drawString(MaxDDL, (int)(rect.getX() + rect.getWidth()),
                          (int)(rect.getY() + rect.getHeight() + 3*fontHeight));

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

            //draw all other curves
            i = curves.iterator();
            while (i.hasNext())
                ((ODCurve)i.next()).draw(g2, rect, maxODAllCurves);
            refCurve.draw(g2, rect, maxODAllCurves);
        }
    }
    
    public void setLegend(ButtonLegendPanel legend)
    {
        legendPanel = legend;
    }
    
    public void selected(Object object)
    {
        ODCurve curve = (ODCurve)object;
        Iterator i = curves.iterator();
        refCurve.setSelected(false);
        while (i.hasNext()) {
            ((ODCurve)i.next()).setSelected(false);
        }
        curve.setSelected(true);
        opResultCurve = ODCurve.newCurveFromComparison(curve, refCurve, CURVE_OP, CURVE_OP_COLOR);
        repaint();
    }
    
    void loadScannedImageCurve(File file)
        throws IOException, CalibrationException
    {
        File dirfile = file.getParentFile();
        sc.setScanDir(dirfile);
        
        float[] calcdOds = sc.calculateGrayscaleODs();
        
        //make sure that the referenced OD is the same, otherwise reset by
        // removing all curves and reload a new reference curve before adding
        // this one
        if (refCurve != null && !refCurveFile.equals(sc.getRefODsFile())) {
            curves = new LinkedList();
            legendPanel.removeAllKeys();
            legendPanel.removeKey(refCurve);
            refCurve = null;
            sc = new ScannerCalibration(Logger.getRootLogger());
        }
        
        //if reference curve used wasn't loaded, load it now
        if (refCurve == null) {
            refCurve = new ODCurve(sc.getRefODs(), Color.BLACK);
            refCurveFile = sc.getRefODsFile();
            legendPanel.addKey("Ref: " + refCurveFile.getName(), Color.BLACK, refCurve);
        }
        
        //create a curve and add it
        Color color = COLOR_POOL[(nextColor++) % COLOR_POOL.length];
        ODCurve curve = new ODCurve(calcdOds,
                                    color);
        curves.add(curve);
        legendPanel.addKey(file.getName(), color, curve);
    }
}
