package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.text.DecimalFormat;

import com.tiani.prnscp.client.PLut;

public class PLutPanel extends JPanel
{
    private final DecimalFormat NumFmt = new DecimalFormat();
    private int[] plut;
    private double cntr = 0.5;
    private double enh = 0;
    private PLut plutGen;
    private ImagePanel imgPanel;
    private int[] histo;
    private int histoMax = 0;
    
    PLutPanel(ImagePanel imgPanel)
    {
        super();
        this.imgPanel = imgPanel;
        //def plut
        int len = 1024;
        plut = new int[len];
        plutGen = new PLut();
        plutGen.setBits(8);
        plutGen.setLength(len);
        plutGen.setCenter(cntr);
        plutGen.setEnhance(enh);
        updatePLut();
        //build histo from sample values
        buildHisto();
        //add mouse motion listener
        addMouseMotionListener(new MouseInputAdapter()
            {
                private int lastx, lasty;
                private final int Delta = 1;
                private final int ChangeCenterMask = InputEvent.SHIFT_DOWN_MASK;
                private final int ChangeEnhanceMask = InputEvent.CTRL_DOWN_MASK;
                
                private void update(MouseEvent e)
                {
                    lastx = e.getX();
                    lasty = e.getY();
                }
                
                public void mouseClicked(MouseEvent e)
                {
                    update(e);
                }
                
                public void mouseDragged(MouseEvent e)
                {
                    int dx = e.getX() - lastx;
                    int dy = e.getY() - lasty;
                    update(e);
                    if ((e.getModifiersEx() & ChangeEnhanceMask) != 0) {
                        if (dy < -Delta) {
                            enh -= 0.05f;
                            if (enh<0) enh = 0;
                            plutGen.setEnhance(enh);
                            updatePLut();
                        }
                        else if (dy > Delta) {
                            enh += 0.05f;
                            plutGen.setEnhance(enh);
                            updatePLut();
                        }
                    }
                    if ((e.getModifiersEx() & ChangeCenterMask) != 0) {
                        if (dx < -Delta) {
                            cntr -= 0.005f;
                            if (cntr<0) cntr = 0;
                            plutGen.setCenter(cntr);
                            updatePLut();
                        }
                        else if (dx > Delta) {
                            cntr += 0.005f;
                            if (cntr>1) cntr = 1;
                            plutGen.setCenter(cntr);
                            updatePLut();
                        }
                    }
                }
            });
    }
    
    private void updatePLut()
    {
        plut = plutGen.create();
        byte[] bplut = new byte[plut.length];
        for (int i=0; i<plut.length; i++) {
            bplut[i] = (byte)plut[i];
        }
        imgPanel.setPLut(bplut);
        repaint();
        imgPanel.repaint();
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.white);
        Rectangle2D rect = new Rectangle2D.Float(0,0,getWidth(),getHeight());
        g2.fill(rect);
        g2.setColor(Color.black);
        drawHisto(g2);
        drawPLut(g2);
        g2.drawString("center = " + NumFmt.format(cntr), 10, 10);
        g2.drawString("enhance = " + NumFmt.format(enh), 10, 20);
    }
    
    private void buildHisto()
    {
        //build histogram
        final int NumBins = 256;
        final int BinMin = 1;
        final int BinMax = 1024;
        final int BinRng = BinMax - BinMin;
        int[] samples = null;
        histo = new int[NumBins];
        int val;
        float x;
        
        samples = imgPanel.getSamples();
        for (int i=0; i<samples.length; i++) {
            if (samples[i] >= BinMin && samples[i] <= BinMax) {
                x = (samples[i]-BinMin)/(float)BinRng; //x in [0..1]
                val = ++histo[(int)(x*(NumBins-1)+0.5)]; //put in closest bin
                if (histoMax < val)
                    histoMax = val;
            }
        }
    }
    
    private void drawHisto(Graphics2D g)
    {
        final int NumBins = histo.length;
        final float binH = (float)getHeight()/NumBins;
        
        g.setColor(new Color(100,250,250));
        for (int i=0; i<histo.length; i++) {
            g.drawRect(0, (int)(binH*i), (int)((getWidth()-1)*histo[i]/(float)histoMax), (int)binH);
            //g.drawString(String.valueOf(histo[i]), 0, (int)binH*i);
        }
        g.setColor(Color.RED);
        g.drawString("histo max = " + histoMax, 0, (int)(binH*(NumBins-1)));
    }
    
    private void drawPLut(Graphics2D g)
    {
        final float fx = (float)(getWidth()-1)/255f;
        final float fy = (float)(getHeight()-1)/(float)(plut.length-1);
        int lastx = 0, lasty = 0;
        int x, y;
        
        g.setColor(Color.BLUE);
        for (int i=0; i<plut.length; i++) {
            x = (int)( (plut[i]<0?plut[i]+256:plut[i]) * fx);
            y = (int)(i*fy);
            g.drawLine(lastx, lasty, x, y);
            lastx = x;
            lasty = y;
        }
        //draw center
        g.setColor(Color.RED);
        g.drawLine(0, (int)(cntr*(float)(getHeight()-1)), getWidth(), (int)(cntr*(float)(getHeight()-1)));
    }
}

