/*                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.client.plutgui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import javax.swing.JPanel;
import javax.swing.event.MouseInputAdapter;

import com.tiani.prnscp.print.PLutBuilder;
import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.dict.Tags;
import org.dcm4che.image.ColorModelParam;

/**
 *  Description of the Class
 *
 * @author     <a href="mailto:joseph@tiani.com">Joseph Foraci</a>
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      March, 2003
 * @version    $Revision$
 */
public class PLutPanel extends JPanel
{
    private DcmObjectFactory dof = DcmObjectFactory.getInstance();
    private DecimalFormat numFormat = new DecimalFormat();
    private PLutBuilder builder = new PLutBuilder();
    private int[] plut;
    private ImagePanel imgPanel;
    //histo stuff
    private final int NUM_BINS = 100;
    private int[] histo = new int[NUM_BINS];
    private int binMin = 1;    //raw min
    private int binMax = 1024; //raw max
    private int binRange = binMax - binMin;//raw range of histo
    private int histoMax = 0;

    private final static int CHANGING_CENTER = 1;
    private final static int CHANGING_SLOPE = 2;
    private final static int CHANGING_GAMMA = 4;
    private int changingParam;
    private boolean logHisto = false;


    PLutPanel(ImagePanel imgPanel)
    {
        this.imgPanel = imgPanel;
        updatePLut();
        //add mouse motion listener
        MouseInputAdapter listener;
        addMouseMotionListener(listener =
            new MouseInputAdapter()
            {
                private int lastx, lasty;
                private final int DELTA = 1;
                private final float SLOPE_STEP = 0.1f;
                private final float CENTER_STEP = 0.01f;
                private final float GAMMA_FACT = 1.05f;
                private final int CHANGE_CENTER_MASK = InputEvent.SHIFT_DOWN_MASK;
                private final int CHANGE_SLOPE_MASK = InputEvent.CTRL_DOWN_MASK;


                private void update(MouseEvent e)
                {
                    lastx = e.getX();
                    lasty = e.getY();
                }


                public void mousePressed(MouseEvent e)
                {
                    update(e);
                }


                public void mouseReleased(MouseEvent e)
                {
                    changingParam = 0;
                    repaint();
                }


                public void mouseClicked(MouseEvent e)
                {
                    if ((e.getButton() & MouseEvent.BUTTON2) != 0) {//reset
                        builder.setSlope(0.f);
                        builder.setCenter(0.5f);
                        builder.setGamma(1.f);
                        updatePLut();
                    }
                }


                public void mouseDragged(MouseEvent e)
                {
                    int dx = e.getX() - lastx;
                    int dy = e.getY() - lasty;
                    update(e);
                    changingParam = 0;
                    if ((e.getModifiersEx() & CHANGE_SLOPE_MASK) != 0) {//slope
                        changingParam |= CHANGING_SLOPE;
                        if (dx < -DELTA) {
                            builder.setSlope(
                                Math.max(0.f,
                                builder.getSlope() - SLOPE_STEP));
                            updatePLut();
                        } else if (dx > DELTA) {
                            builder.setSlope(
                                Math.min(10.f,
                                builder.getSlope() + SLOPE_STEP));
                            updatePLut();
                        }
                    }
                    if ((e.getModifiersEx() & CHANGE_CENTER_MASK) != 0) {//center
                        changingParam |= CHANGING_CENTER;
                        builder.setCenter(
                            Math.max(0.f,
                                Math.min(1.f,
                                    (float) e.getY() / getHeight())));
                        updatePLut();
                    } else if ((e.getModifiersEx() & CHANGE_SLOPE_MASK) == 0) {//gamma
                        changingParam |= CHANGING_GAMMA;
                        if (dx < -DELTA) {
                            builder.setGamma(
                                Math.max(0.1f,
                                    builder.getGamma() / GAMMA_FACT));
                            updatePLut();
                        } else if (dx > DELTA) {
                            builder.setGamma(
                                Math.min(10.f,
                                    builder.getGamma() * GAMMA_FACT));
                            updatePLut();
                        }
                    }
                }
            });
        addMouseListener(listener);
        /*addMouseWheelListener(new MouseWheelListener() {
                private final float GAMMA_STEP = 0.01f;
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    int dy = e.getScrollAmount();
                    if (dy < 0) {
                        gam -= GAMMA_STEP;
                        if (gam<0) gam = 0;
                        builder.setGamma(gam);
                        updatePLut();
                    }
                    else if (dy > 0) {
                        gam += GAMMA_STEP;
                        builder.setGamma(gam);
                        updatePLut();
                    }
                }
            });*/
    }


    private void updatePLut()
    {
        updatePLut(true);
    }


    private void updatePLut(boolean createFromParams)
    {
        if (createFromParams) {
            plut = builder.create();
        }
        byte[] bplut = new byte[plut.length];
        for (int i = 0; i < plut.length; i++) {
            bplut[i] = (byte) plut[i];
        }
        try {
            imgPanel.setPLut(bplut);
        } catch (Exception e) {}
        repaint();
        imgPanel.repaint();
    }


    /**
     *  Description of the Method
     *
     * @param  g  The graphics context
     */
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(Color.WHITE);
        g2.fill(new Rectangle2D.Float(0, 0, getWidth(), getHeight()));
        if (imgPanel.getBI() != null) {
            //draw the window range, which is the P-LUTs range
            drawPLut(g2, true);
            //draw histogram
            drawHisto(g2);
            //draw P-LUT curve
            drawPLut(g2, false);
            //draw curve parameters overlay
            g2.setColor(Color.WHITE);
            g2.fillRect(10, 10, 100, 34);
            g2.setColor(Color.BLACK);
            g2.drawRect(10, 10, 100, 34);
            g2.setClip(10, 10, 100, 34);
            g2.setColor((changingParam & CHANGING_CENTER) != 0
                        ? Color.RED
                        : Color.BLACK);
            g2.drawString("Center = " + numFormat.format(builder.getCenter()),
                          12, 22);
            g2.setColor((changingParam & CHANGING_SLOPE) != 0
                        ? Color.RED
                        : Color.BLACK);
            g2.drawString("Slope = " + numFormat.format(builder.getSlope()),
                          12, 32);
            g2.setColor((changingParam & CHANGING_GAMMA) != 0
                        ? Color.RED
                        : Color.BLACK);
            g2.drawString("Gamma = " + numFormat.format(builder.getGamma()),
                          12, 42);
            g2.setClip(0, 0, getWidth(), getHeight());
            //draw window/histogram information overlay
            g2.setColor(Color.WHITE);
            g2.fillRect(10, getHeight() - 32, 200, 24);
            g2.setColor(Color.BLACK);
            g2.drawRect(10, getHeight() - 32, 200, 24);
            g2.setClip(10, getHeight() - 32, 200, 24);
            int winMin = imgPanel.getWindowMin();
            int winMax = imgPanel.getWindowMax();
            final boolean winDefined = (winMin < winMax);
            if (!winDefined)
                g.drawString("No window", 11, getHeight() - 21);
            else
                g.drawString("Window [" + winMin + "-" + winMax + "]",
                             11, getHeight() - 21);
            g.drawString("Histo: N=" + NUM_BINS
                         + " [" + binMin + "-" + binMax + "]", 11, getHeight() - 11);
            g2.setClip(0, 0, getWidth(), getHeight());
        }
    }

    private void clearHisto()
    {
        histoMax = 0;
        for (int i = 0; i < histo.length; i++)
            histo[i] = 0;
    }

    void buildHisto()
    {
        //build histogram
        int[] samples;
        int val;
        float x;

        //get image samples. these are guaranteed not to be null since this
        // is called by the PLutGUIPanel
        samples = imgPanel.getSamples();
        if (samples.length == 0) {
            return;
        }
        //find min/max/range of samples for histogram min/max/range range
        binMin = binMax = samples[0];
        for (int i = 1; i < samples.length; i++) {
            if (binMin > samples[i]) {
                binMin = samples[i];
            } else if (binMax < samples[i]) {
                binMax = samples[i];
            }
        }
        binRange = binMax - binMin;
        //clear histogram
        clearHisto();
        //calculate histogram
        for (int i = 0; i < samples.length; i++) {
            if (samples[i] >= binMin && samples[i] <= binMax) {
                x = (samples[i] - binMin) / (float) binRange;  //x in [0..1]
                val = ++histo[(int) (x * (NUM_BINS - 1) + 0.5)];  //put in closest bin
                if (histoMax < val) {
                    histoMax = val;
                }
            }
        }
    }


    void equalize()
    {
        //check if an image is loaded
        if (imgPanel.getBI() == null)
            return;
        
        //get window min/max in pixel units
        int winMin = imgPanel.getWindowMin();
        int winMax = imgPanel.getWindowMax();
        final boolean winDefined = (winMin < winMax);
        final int startInd, endInd;
        
        if (!winDefined) {
            winMin = binMin;
            winMax = binMax;
            startInd = 0;
            endInd = NUM_BINS - 1;
        }
        else {
            startInd = (int)((float)((winMin - binMin) * (NUM_BINS - 1)) / binRange + 0.5);
            endInd = (int)((float)((winMax - binMin) * (NUM_BINS - 1)) / binRange + 0.5);
            if (startInd == endInd) {
                return; // do nothing
            }
        }
        
        final int plutMax = 255;
        int sum = 0;
        
        for (int i = Math.max(0, startInd); i <= endInd && i < histo.length; i++)
            sum += histo[i];
        
        final int totSum = sum;
        final float f = (float) plutMax / totSum;
        final float fPLUT = (float) (plut.length - 1) / (endInd - startInd);
        int n, v;
        int lastind = startInd;
        
        sum = 0;
        for (int i = startInd; i <= endInd; i++) {
            if (i >= 0 && i < histo.length)
                sum += histo[i];
            n = (int) ((i - startInd) * fPLUT);
            v = (int) (f * sum + 0.5);
            for (int j = lastind + 1; j <= n; j++) {
                plut[j] = v;
            }
            lastind = n;
        }
        updatePLut(false);
    }


    private void drawHisto(Graphics2D g)
    {
        final float binH = (float) getHeight() / NUM_BINS;
        int curveColor;
        BufferedImage bi = imgPanel.getBI();
        ColorModel cm = bi.getColorModel();
        final ColorModelParam cmParam = imgPanel.getColorModelParam();
        final float f = (float) binRange / (NUM_BINS - 1);
        final double logHistMax = Math.log(histoMax+1);
        
        for (int i = 0; i < NUM_BINS; i++) {
            int dx = (int) ((getWidth() - 1) * 
                (logHisto
                ? (Math.log(histo[i]+1) / logHistMax)
                : ((double) histo[i] / histoMax)));
            curveColor = cm.getRGB(
                cmParam.toPixelValueRaw(
                    ((int) (i * f + 0.5) + binMin)));
            g.setColor(Color.BLACK);
            g.drawLine(dx, (int) (binH * i), dx, (int) (binH * (i + 1)) - 1);
            // g.drawRect( 0, (int) (binH * i), dx, (int) binH);
            g.setColor(new Color(curveColor));
            g.fillRect(0, (int) (binH * i) + 1, dx - 1, (int) binH - 1);
        }
        g.setColor(Color.RED);
    }


    private void drawPLut(Graphics2D g, boolean onlyDrawWindowRange)
    {
        //get window min/max in pixel units
        int winMin = imgPanel.getWindowMin();
        int winMax = imgPanel.getWindowMax();
        final boolean winDefined = (winMin < winMax);
        final float fx = (float)(getWidth() - 1) / 255f;
        final float y0;
        final float fy;

        if (!winDefined) {
            winMin = binMin;
            winMax = binMax;
            y0 = 0;
            fy = (float)(getHeight() - 1) / (plut.length - 1);
        }
        else {
            y0 = (float)((getHeight() - 1) * (winMin - binMin))
                         / binRange;
            fy = (float)((getHeight() - 1) * (winMax - winMin))
                         / (binRange * (plut.length - 1));
            //draw window range
            if (onlyDrawWindowRange) {
                g.setColor(Color.LIGHT_GRAY);
                g.fillRect(0, (int)y0, getWidth(), (int)((plut.length-1) * fy + 0.5));
                g.setColor(Color.DARK_GRAY);
                g.drawRect(0, (int)y0, getWidth(), (int)((plut.length-1) * fy + 0.5));
                return;
            }
        }

        //draw P-LUT curve
        int lastx = (int) ((plut[0] < 0 ? plut[0] + 256 : plut[0]) * fx);
        int lasty = (int) (y0 + 0.5f);
        int x, y;

        g.setColor(Color.BLACK);
        for (int i = 0; i < plut.length; i++) {
            x = (int) ((plut[i] < 0 ? plut[i] + 256 : plut[i]) * fx);
            y = (int) (i * fy + y0 + 0.5f);
            g.drawLine(lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }

        //draw center
        g.setColor(Color.RED);
        y = (int) (builder.getCenter() * (plut.length - 1) * fy + y0 + 0.5);
        g.drawLine(0, y, getWidth(), y);
    }


    void exportPLutDicom(File file)
        throws IOException
    {
        Dataset dsLut = builder.toDataset(plut);
        dsLut.setFileMetaInfo(builder.makeFileMetaInfo());
        OutputStream out = new BufferedOutputStream(
                new FileOutputStream(file));
        try {
            dsLut.writeFile(out, null);
        } finally {
            try {
                out.close();
            } catch (IOException ignore) {}
        }
    }


    void importPLutDicom(File file)
        throws IOException
    {
        Dataset dsLut = dof.newDataset();
        InputStream in = new BufferedInputStream(
                new FileInputStream(file));
        try {
            dsLut.readFile(in, null, Tags.PixelData);
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
        DcmElement sq = dsLut.get(Tags.PresentationLUTSeq);
        Dataset item = sq.getItem();
        int[] lutDesc = item.getInts(Tags.LUTDescriptor);
        int bits = lutDesc[2];
        plut = item.getInts(Tags.LUTData);
        updatePLut(false);
    }
}

