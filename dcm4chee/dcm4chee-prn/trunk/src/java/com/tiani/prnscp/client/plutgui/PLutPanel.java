package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.io.*;
import java.text.DecimalFormat;
import javax.swing.*;
import javax.swing.event.*;

import com.tiani.prnscp.client.PLut;

import org.dcm4che.data.*;
import org.dcm4che.dict.*;

public class PLutPanel extends JPanel
{
    private final DecimalFormat NumFmt = new DecimalFormat();
    private int[] plut;
    private double cntr = 0.5;
    private double enh = 0;
    private double gam = 1;
    private PLut plutGen;
    private ImagePanel imgPanel;
    //histo stuff
    private final int NumBins = 100;
    private  int BinMin = 1; //raw min
    private  int BinMax = 1024; //raw max
    private  int BinRng = BinMax - BinMin; //raw range of histo
    private int[] histo;
    private int histoMax = 0;
    
    private final int ChangingCenter = 1;
    private final int ChangingEnhance = 2;
    private final int ChangingGamma = 4;
    private int changingParam;

    PLutPanel(ImagePanel imgPanel)
    {
        super();
        this.imgPanel = imgPanel;
        //def plut
        final int PLutLen = 1024;
        plut = new int[PLutLen];
        plutGen = new PLut();
        plutGen.setBits(8);
        plutGen.setLength(PLutLen);
        plutGen.setCenter(cntr);
        plutGen.setEnhance(enh);
        plutGen.setGamma(gam);
        updatePLut();
        //add mouse motion listener
        MouseInputAdapter listener;
        addMouseMotionListener(listener = new MouseInputAdapter()
            {
                private int lastx, lasty;
                private final int Delta = 1;
                private final float EnhanceStep = 0.1f;
                private final float CenterStep = 0.01f;
                private final float GammaStep = 0.05f;
                private final int ChangeCenterMask = InputEvent.SHIFT_DOWN_MASK;
                private final int ChangeEnhanceMask = InputEvent.CTRL_DOWN_MASK;
                
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
                    if ((e.getButton() & MouseEvent.BUTTON2)!=0) { //reset
                        enh = 0;
                        cntr = 0.5;
                        gam = 1;
                        plutGen.setEnhance(enh);
                        plutGen.setCenter(cntr);
                        plutGen.setGamma(gam);
                        updatePLut();
                    }
                }
                
                public void mouseDragged(MouseEvent e)
                {
                    int dx = e.getX() - lastx;
                    int dy = e.getY() - lasty;
                    update(e);
                    changingParam = 0;
                    if ((e.getModifiersEx() & ChangeEnhanceMask) != 0) { //enhancement
                        changingParam |= ChangingEnhance;
                        if (dx < -Delta) {
                            enh -= EnhanceStep;
                            if (enh<0) enh = 0;
                            plutGen.setEnhance(enh);
                            updatePLut();
                        }
                        else if (dx > Delta) {
                            enh += EnhanceStep;
                            plutGen.setEnhance(enh);
                            updatePLut();
                        }
                    }
                    if ((e.getModifiersEx() & ChangeCenterMask) != 0) { //center
                        changingParam |= ChangingCenter;
                        cntr = (double)e.getY()/PLutPanel.this.getHeight();
                        if (cntr<0) cntr = 0;
                        else if (cntr>1) cntr = 1;
                        plutGen.setCenter(cntr);
                        updatePLut();
                    }
                    else if ((e.getModifiersEx() & ChangeEnhanceMask) == 0) { //gamma
                        changingParam |= ChangingGamma;
                        if (dx < -Delta) {
                            gam -= GammaStep;
                            if (gam<0) gam = 0;
                            plutGen.setGamma(gam);
                            updatePLut();
                        }
                        else if (dx > Delta) {
                            gam += GammaStep;
                            plutGen.setGamma(gam);
                            updatePLut();
                        }
                    }
                }
            });
            addMouseListener(listener);
            /*addMouseWheelListener(new MouseWheelListener() {
                private final float GammaStep = 0.01f;
                
                public void mouseWheelMoved(MouseWheelEvent e)
                {
                    int dy = e.getScrollAmount();
                    if (dy < 0) {
                        gam -= GammaStep;
                        if (gam<0) gam = 0;
                        plutGen.setGamma(gam);
                        updatePLut();
                    }
                    else if (dy > 0) {
                        gam += GammaStep;
                        plutGen.setGamma(gam);
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
        if (createFromParams)
            plut = plutGen.create();
        byte[] bplut = new byte[plut.length];
        for (int i=0; i<plut.length; i++) {
            bplut[i] = (byte)plut[i];
        }
        try {
            imgPanel.setPLut(bplut);
        }
        catch (Exception e) {}
        repaint();
        imgPanel.repaint();
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(Color.WHITE);
        Rectangle2D rect = new Rectangle2D.Float(0,0,getWidth(),getHeight());
        g2.fill(rect);
        if (imgPanel.getBI() != null) {
            drawHisto(g2);
            drawPLut(g2);
            g2.setColor(Color.WHITE);
            g2.fillRect(10,10,100,30);
            g2.setColor(Color.BLACK);
            g2.drawRect(10,10,100,30);
            g2.setClip(10,10,100,30);
            g2.setColor( (changingParam & ChangingCenter) != 0 ? Color.RED : Color.BLACK);
            g2.drawString("Center = " + NumFmt.format(cntr), 10, 20);
            g2.setColor( (changingParam & ChangingEnhance) != 0 ? Color.RED : Color.BLACK);
            g2.drawString("Enhance = " + NumFmt.format(enh), 10, 30);
            g2.setColor( (changingParam & ChangingGamma) != 0 ? Color.RED : Color.BLACK);
            g2.drawString("Gamma = " + NumFmt.format(gam), 10, 40);
            g2.setClip(0,0,getWidth(),getHeight());
        }
    }
    
    private int[] getSamples()
    {
        BufferedImage bi = imgPanel.getBI();
        int[] samples = new int[bi.getWidth()*bi.getHeight()];
        
        bi.getRaster().getPixels(0,0,bi.getWidth(),bi.getHeight(),samples);
        //bi.getRaster().getSamples(0,0,bi.getWidth(),bi.getHeight(),0,samples);
        return samples;
    }
    
    public void buildHisto()
    {
        //build histogram
        int[] samples = null;
        int val;
        float x;
        
        samples = getSamples(); //guaranteed not to be null since this is called by imgPanel
        if (samples.length == 0)
            return;
        BinMin = BinMax = samples[0];
        for (int i=1; i<samples.length; i++) {
            if (BinMin > samples[i])
                BinMin = samples[i];
            else if (BinMax < samples[i])
                BinMax = samples[i];
        }
        BinRng = BinMax - BinMin;
        histo = new int[NumBins];
        for (int i=0; i<samples.length; i++) {
            if (samples[i] >= BinMin && samples[i] <= BinMax) {
                x = (samples[i]-BinMin)/(float)BinRng; //x in [0..1]
                val = ++histo[(int)(x*(NumBins-1)+0.5)]; //put in closest bin
                if (histoMax < val)
                    histoMax = val;
            }
        }
    }
    
    public void equalize()
    {
        final int PLutMax = 255;
        int sum = 0,n,v;
        int lastind =0;
        
        for (int i=0; i<histo.length; i++) {
            sum += histo[i];
        }
        final int TotSum = sum;
        final float f = (float)PLutMax/TotSum;
        sum = 0;
        for (int i=0; i<histo.length; i++) {
            sum += histo[i];
            n = (int)((float)i*(plut.length-1)/(histo.length-1));
            v = (int)(f*sum + 0.5);
            for (int j=lastind+1; j<=n; j++)
                plut[j] = v; 
            lastind = n;
        }
        updatePLut(false);
    }
    
    private void drawHisto(Graphics2D g)
    {
        final int NumBins = histo.length;
        final float binH = (float)getHeight()/NumBins;
        int curveColor;
        ColorModel cm = imgPanel.getBI().getColorModel();
        
        //g.setColor(new Color(100,250,250));
        float f = (float)BinRng/(histo.length - 1);
        for (int i=0; i<histo.length; i++) {
            curveColor = cm.getRGB((int)(i*f + 0.5) + BinMin);
            g.setColor(Color.BLACK);
            g.drawRect(0, (int)(binH*i), (int)((getWidth()-1)*histo[i]/(float)histoMax), (int)binH);
            g.setColor(new Color(curveColor));
            g.fillRect(1, (int)(binH*i)+1, (int)((getWidth()-1)*histo[i]/(float)histoMax) - 1, (int)binH - 1);
            //g.drawString(String.valueOf(histo[i]), 0, (int)binH*i);
        }
        g.setColor(Color.RED);
        g.drawString("histo: N=" + NumBins + " [" + BinMin + "-" + BinMax + "]", 0, (int)(binH*(NumBins-1)));
    }
    
    private void drawPLut(Graphics2D g)
    {
        final float fx = (float)(getWidth()-1)/255f;
        final float fy = (float)(getHeight()-1)/(float)(plut.length-1);
        int lastx = 0, lasty = 0;
        int x, y;
        
        //g.setStroke(new BasicStroke(2,BasicStroke.CAP_BUTT,BasicStroke.JOIN_BEVEL,0));
        for (int i=0; i<plut.length; i++) {
            x = (int)((plut[i]<0?plut[i]+256:plut[i]) * fx);
            y = (int)(i*fy);
            g.setColor(Color.BLACK);
            //g.drawLine(lastx, lasty-2, x, y-2);
            g.drawLine(lastx, lasty-1, x, y-1);
            g.drawLine(lastx, lasty, x, y);
            g.drawLine(lastx, lasty+1, x, y+1);
            //g.setColor(Color.BLACK);
            //g.drawLine(lastx, lasty+2, x, y+2);
            lastx = x;
            lasty = y;
        }
        //draw center
        g.setColor(Color.RED);
        g.drawLine(0, (int)(cntr*(float)(getHeight()-1)), getWidth(), (int)(cntr*(float)(getHeight()-1)));
    }
    
    void exportPLutDicom(File file)
        throws IOException
    {
        plutGen.setOutFile(file.getPath());
        plutGen.write(plutGen.toDataset(plut));
    }

    void importPLutDicom(File file)
        throws IOException
    {
        DcmObjectFactory dcmObjFact = DcmObjectFactory.getInstance();
        Dataset dsLut = dcmObjFact.newDataset();
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        dsLut.readFile(in, null, Tags.PixelData);
        
        DcmElement sq = dsLut.get(Tags.PresentationLUTSeq);
        Dataset item = sq.getItem();
        int[] lutDesc = item.getInts(Tags.LUTDescriptor);
        int bits = lutDesc[2];
        plut = item.getInts(Tags.LUTData);
        /*item.putLO(Tags.LUTExplanation,
                explanation != null ? explanation : createExplanation());
        item.putUS(Tags.LUTData, plut);*/
        
        updatePLut(false);
    }
}

