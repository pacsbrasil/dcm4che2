package com.tiani.prnscp.client.plutgui;

import java.awt.*;
import javax.swing.*;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

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
        //
        imgPanel = new ImagePanel(null);
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
    
    public ImagePanel getImagePanel() { return imgPanel; }
    
    public PLutPanel getPLutPanel() { return plutPanel; }
    
    public void equalize()
    {
        plutPanel.equalize();
    }
    
    public void setImage(File newImg)
    {
        try {
            imgPanel.setImage(newImg);
        }
        catch (UnsupportedOperationException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                                          "Image could not be read or loaded:\n"
                                          + "- May not be a DICOM image file, or\n"
                                          + "- The color model may not be applicable"
                                          + "to a P-LUT transformation.");
        }
        imgPanel.repaint();
        plutPanel.buildHisto();
        plutPanel.repaint();
    }
    
    public void displayImageInfo()
    {
        Dataset ds = imgPanel.getDS();
        if (ds == null) {
            JOptionPane.showMessageDialog(this, "No image has been loaded.");
            return;
        }
        int width = ds.getInt(Tags.Columns, 0);
        int height = ds.getInt(Tags.Rows, 0);
        int bitsStored = ds.getInt(Tags.BitsStored, 0);
        boolean signed = (ds.getInt(Tags.PixelRepresentation, 0) == 1);
        String pmi = ds.getString(Tags.PhotometricInterpretation);
        JOptionPane.showMessageDialog(this, "Width: " + width + "\n"
                                      + "Height: " + height + "\n"
                                      + "Bits: " + bitsStored + " ("
                                      + ((signed)?"signed":"unsigned") + ")\n"
                                      + "Color model: " + pmi + "\n"
                                      + "");
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
    }
}
