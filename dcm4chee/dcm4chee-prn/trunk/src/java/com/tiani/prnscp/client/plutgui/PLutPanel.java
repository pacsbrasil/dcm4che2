package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;

import com.tiani.prnscp.client.PLut;

public class PLutPanel extends JPanel
{
    byte[] plut;
    double cntr = 0.5;
    double enh = 0;
    PLut plutGen;
    ImagePanel imgPanel;
    
    PLutPanel(ImagePanel imgPanel)
    {
        super();
        this.imgPanel = imgPanel;
        //def plut
        int len = 1024;
        plut = new byte[len];
        plutGen = new PLut();
        plutGen.setBits(8);
        plutGen.setLength(len);
        plutGen.setCenter(cntr);
        plutGen.setEnhance(enh);
        updatePLut();
        //add mouse motion listener
        addMouseMotionListener(new MouseInputAdapter()
            {
                private int lastx, lasty;
                private final int Delta = 4;
                
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
                    if (dy < -Delta) {
                        enh -= 0.2f;
                        if (enh<0) enh = 0;
                        plutGen.setEnhance(enh);
                        updatePLut();
                    }
                    else if (dy > Delta) {
                        enh += 0.2f;
                        plutGen.setEnhance(enh);
                        updatePLut();
                    }
                    if (dx < -Delta) {
                        cntr -= 0.05f;
                        if (cntr<0) cntr = 0;
                        plutGen.setCenter(cntr);
                        updatePLut();
                    }
                    else if (dx > Delta) {
                        cntr += 0.05f;
                        if (cntr>1) cntr = 1;
                        plutGen.setCenter(cntr);
                        updatePLut();
                    }
                }
            });
    }
    
    private void updatePLut()
    {
        int[] iplut = plutGen.create();
        for (int i=0; i<plut.length; i++) {
            plut[i] = (byte)iplut[i];
        }
        imgPanel.setPLut(plut);
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
        g2.drawString("center = " + String.valueOf(cntr), 10, 10);
        g2.drawString("enhance = " + String.valueOf(enh), 10, 20);
        //drawHisto();
        //drawPLut();
    }
}

