package com.tiani.prnscp.client.ddl2odgui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.tiani.prnscp.print.CalibrationException;
import com.tiani.prnscp.print.PrinterCalibration;
import com.tiani.prnscp.print.ScannerCalibration;

public class ODCurveGUIPanel extends JPanel
                             implements ButtonLegendPanel.ButtonLegendCallback
{
    private static final int NUM_OD_ENTRIES = 256;
    private static final Color[] COLOR_POOL = { Color.ORANGE, Color.RED, Color.BLUE, Color.CYAN,
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
    private ODCurve gsdfCurve = null;
    private File refCurveFile = null;
    private float maxODAllCurves = 0;
    private float illumination = 150, ambient = 0;
    private Collection curves;
    private ODCurve opResultCurve = null;
    private ButtonLegendPanel legendPanel;
    private ODCurveGUIFrame frParent;
    
    ODCurveGUIPanel(ODCurveGUIFrame parent)
    {
        frParent = parent;
        curves = new LinkedList();
    }
    
    private static final AffineTransform ROT_CW_90 = new AffineTransform(0, -1, 1, 0, 0, 0);
    private static final String X_AXIS_LABEL = "Device Driving Level (DDL)";
    private static final String Y_AXIS_LABEL = "Optical Density (OD)";
    private static final String MIN_DDL_LABEL = Integer.toString(0);
    private static final String MAX_DDL_LABEL = Integer.toString(255);
    
    private final int fontHeight = getFontMetrics(getFont()).getHeight();
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        
        Graphics2D g2 = (Graphics2D)g;
        
        //bkgnd
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, getWidth(), getHeight());

        //graph bounds
        final Rectangle rect = new Rectangle(50, 50,
                                             getWidth() - 100, getHeight() - 100);

        //draw this difference curve way before everything else
        if (opResultCurve != null)
            opResultCurve.draw(g2, rect, maxODAllCurves);

        g2.setColor(Color.BLACK);
        g2.draw(rect);

        //draw axis labels
        AffineTransform savedTx = g2.getTransform();
        g2.translate((int)rect.getX() - 4, (int)(rect.getY() + rect.getHeight()));
        g2.transform(ROT_CW_90);
        g2.drawString(Y_AXIS_LABEL, 1, 0);
        //g2.drawChars("Optical Density (OD)".toCharArray(), 0, "Optical Density (OD)".length(),0,0);
        g2.setTransform(savedTx);
        g2.drawString(X_AXIS_LABEL, (int)rect.getX(), (int)(rect.getY() + rect.getHeight()) + fontHeight);

        //draw curves, if any are loaded
        if (gsdfCurve != null) {
            //draw x-axis
            g2.setColor(Color.GRAY);
            for (int k = 0, p = (int)rect.getX(); k < 8; k++) {
                g2.drawLine(p, (int)rect.getY(),
                           p, (int)(rect.getY() + rect.getHeight()));
                p += rect.getWidth() / 8;
            }
            g2.drawString(MIN_DDL_LABEL, (int)rect.getX(),
                          (int)(rect.getY() + rect.getHeight() + 3*fontHeight));
            g2.drawString(MAX_DDL_LABEL, (int)(rect.getX() + rect.getWidth()),
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
            Iterator i;
            ODCurve selectedCurve = null, acurve;
            i = curves.iterator();
            while (i.hasNext()) {
                acurve = (ODCurve)i.next();
                if (!acurve.isSelected())
                    acurve.draw(g2, rect, maxODAllCurves);
                else
                    selectedCurve = acurve;
            }
            // draw GSDF reference curve
            gsdfCurve.draw(g2, rect, maxODAllCurves);
            // draw selected curve last
            if (selectedCurve != null)
                selectedCurve.draw(g2, rect, maxODAllCurves);
        }
    }
    
    private void createGSDFCurve()
    {
        //create the GSDF curve
        float tmp;
        float[] gsdf =
            PrinterCalibration.getPValToGsdfOD(8, 0, maxODAllCurves,
                                               illumination, ambient);
        for (int i = 0; i < gsdf.length / 2; i++) {
            tmp = gsdf[i];
            gsdf[i] = gsdf[gsdf.length - 1 - i];
            gsdf[gsdf.length - 1 - i] = tmp;
        }
        gsdfCurve = new ODCurve(gsdf, Color.BLACK);
    }
    
    public void setLegend(ButtonLegendPanel legend)
    {
        legendPanel = legend;
    }
    
    public void export(Object object)
    {
        ODCurve curve = (ODCurve)object;
        File file = frParent.promptUserForFile();

        if (file == null)
            return;
        try {
            sc.writeODs(file, curve.getData());
        }
        catch (IOException ioe) {
            frParent.showMsgDialog("There was a problem exporting to the file\n"
                                   + file, "Export error");
        }
    }
    
    public void remove(Object object)
    {
        if (curves.contains(object))
            curves.remove(object);
        else
            return;
        //get max
        ODCurve curve;
        Iterator i = curves.iterator();
        maxODAllCurves = 0;
        while (i.hasNext()) {
            curve = (ODCurve)i.next();
            if (maxODAllCurves < curve.getMax())
                maxODAllCurves = curve.getMax();
        }
        //recreate GSDF
        createGSDFCurve();
        //repaint
        repaint();
    }
    
    public void selected(Object object)
    {
        ODCurve curve = (ODCurve)object;
        Iterator i = curves.iterator();
        gsdfCurve.setSelected(false);
        while (i.hasNext()) {
            ((ODCurve)i.next()).setSelected(false);
        }
        if (curve != gsdfCurve) {
            opResultCurve =
                ODCurve.newCurveFromComparison(curve, gsdfCurve,
                                               CURVE_OP, CURVE_OP_COLOR);
            curve.setSelected(true);
        }
        else {
            opResultCurve = null;
        }
        repaint();
    }
    
    /**
     * Resets to initial state
     */
    public void reset()
    {
        curves = new LinkedList();
        legendPanel.removeAllKeys();
        gsdfCurve = null;
        refCurveFile = null;
        opResultCurve = null;
        nextColor = 0;
        maxODAllCurves = 0;
        sc = new ScannerCalibration(Logger.getRootLogger());
        ((ODCurveGUIFrame)frParent).validate();
    }
    
    /**
     * Loads and attempt to scale <code>file</code> for a scanned DSI256
     * pattern and creates a curve using the <code>ScannerCalibration</code>
     * class, and adds it to the collection of curves to display.
     * 
     * @param file File representing the image to scan for DSI256 pattern
     * @throws IOException Thrown by ScannerCalibration on IO problem
     * @throws CalibrationException Thrown by ScannerCalibration if analyzation
     *         or calibration fails
     */
    void loadScannedImageCurve(File file)
        throws IOException, CalibrationException
    {
        //when setting the scan directory, make sure that the referenced OD is
        // the same, otherwise reset by removing all curves and reload a new
        // reference curve before adding this one
        sc.setCalibrationDir(file.getParentFile().getParentFile());
        if (refCurveFile != null && !refCurveFile.equals(sc.getRefODsFile())) {
            reset();
        }
        
        float[] calcOds = sc.interpolate(sc.analyse(file));
        
        //create a curve...
        Color color = COLOR_POOL[(nextColor++) % COLOR_POOL.length];
        ODCurve curve = new ODCurve(calcOds, color);
        if (gsdfCurve != null && curve.getMax() > maxODAllCurves) {
            maxODAllCurves = curve.getMax();
            createGSDFCurve(); //need to re-create GSDF curve since max OD may have changed
        }
        // ...and add it to the collection
        curves.add(curve);
        
        //if the reference curve that was used wasn't already loaded, then
        // load it now since there exists at least one curve in the collection
        if (gsdfCurve == null) {
            maxODAllCurves = curve.getMax();
            createGSDFCurve();
            refCurveFile = sc.getRefODsFile();
            legendPanel.addKey("GSDF", Color.BLACK, gsdfCurve);
        }
        
        //add to legend panel
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        legendPanel.addKey(dateFmt.format(new Date(file.lastModified())),
                           color, curve);
    }
}
