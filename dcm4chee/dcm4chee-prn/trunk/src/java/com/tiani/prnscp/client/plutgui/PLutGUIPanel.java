package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;

public class PLutGUIPanel extends JPanel
{
    ImagePanel imgPanel;
    PLutPanel plutPanel;
    
    PLutGUIPanel()
    {
        super();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        //set layout
        setLayout(gridbag);
        //read img
        final String TEST = "../dcm4che14/test/conf/data/MRABDO";
        //
        imgPanel = new ImagePanel(null); //new File(TEST)
        plutPanel = new PLutPanel(imgPanel);
        //add image panel
        //plutPanel.setPreferredSize(new Dimension(300,600));
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 2;
        c.weighty = 1;
        gridbag.setConstraints(imgPanel,c);
        add(imgPanel);
        //add plut panel
        //c.fill = GridBagConstraints.NONE;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.weighty = 1;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(plutPanel,c);
        add(plutPanel);
    }
    
    public void equalize()
    {
        plutPanel.equalize();
    }
    
    public void setImage(File newImg)
    {
        try {
            imgPanel.setImage(newImg);
            imgPanel.repaint();
            plutPanel.buildHisto();
            plutPanel.repaint();
        }
        catch (Exception e) {
            //bi = null;
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "There is a problem!");
        }
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    }
}

