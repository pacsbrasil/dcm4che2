package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import org.dcm4che.data.Dataset;
import org.dcm4che.data.DcmElement;
import org.dcm4che.data.DcmObjectFactory;
import org.dcm4che.data.FileFormat;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmImageReadParam;
import org.dcm4cheri.imageio.plugins.DcmImageReader;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.imageio.plugins.DcmMetadata;

public class ImagePanel extends JPanel
{
    private ImageReader reader;
    private DcmImageReadParam readParam;
    private BufferedImage bi;
    private FileImageInputStream fis;
    private byte[] lastPLut;
    
    ImagePanel(File image)
    {
        super();
        //get reader
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        reader = (ImageReader) iter.next();
        if (image != null)
            setImage(image);
    }
    
    public int[] getSamples()
    {
        int[] samples = new int[bi.getWidth()*bi.getHeight()];
        
        bi.getRaster().getPixels(0,0,bi.getWidth(),bi.getHeight(),samples);
        //bi.getRaster().getSamples(0,0,bi.getWidth(),bi.getHeight(),0,samples);
        return samples;
    }
    
    public BufferedImage updateImageParams(BufferedImage bi, DcmImageReadParam param)
        throws IOException
    {
        Dataset ds = ((DcmMetadata)reader.getStreamMetadata()).getDataset();
        ColorModelFactory cmFactory = ColorModelFactory.getInstance();
        BufferedImage newbi = new BufferedImage(
            cmFactory.getColorModel(
            cmFactory.makeParam(ds,
                param != null ? param.getPValToDDL() : null)),
            bi.getRaster(),
            bi.isAlphaPremultiplied(),
            null);
        return newbi;
    }

    public void setImage(File newImg)
    {
        try {
            fis = new FileImageInputStream(newImg);
            bi = null;
        }
        catch(Exception e) {}
        reader.setInput(fis);
        setPLut(lastPLut);
        repaint();
    }
    
    public void setPLut(byte[] plut)
    {
        lastPLut = plut;
        readParam = (DcmImageReadParam)reader.getDefaultReadParam();
        readParam.setPValToDDL(plut);
        try {
            if (this.bi == null)
                this.bi = reader.read(0, readParam);
            else
                this.bi = updateImageParams(bi, readParam);
        }
        catch (Exception e) {
            bi = null;
            e.printStackTrace();
        }
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        if (bi != null)
            g2.drawImage(bi,0,0,getWidth(),getHeight(),null);
    }
}

