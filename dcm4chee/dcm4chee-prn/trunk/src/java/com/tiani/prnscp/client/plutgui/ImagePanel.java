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
        if (image != null) {
            try {
                setImage(image);
            }
            catch (Exception e) {}
            repaint();
        }
    }
    
    public BufferedImage getBI()
    {
        return bi;
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
        throws Exception
    {
        try {
            fis = new FileImageInputStream(newImg);
        }
        catch(Exception e) {
            return;
        }
        bi = null;
        reader.setInput(fis);
        setPLut(lastPLut);
    }
    
    public void setPLut(byte[] plut)
        throws Exception
    {
        final int MaxWidth = 512;
        final int MaxHeight = 512;
        
        lastPLut = plut;
        readParam = (DcmImageReadParam)reader.getDefaultReadParam();
        readParam.setPValToDDL(plut);
        if (fis != null) { //check if reader input stream has been set
            if (bi == null) { //first time?
                int width, height;
                int subx = 1, suby = 1;
                width = reader.getWidth(0);
                height = reader.getHeight(0);
                if (width > MaxWidth)
                    subx = width/MaxWidth;
                if (height > MaxHeight)
                    suby = height/MaxHeight;
                readParam.setSourceSubsampling(subx, suby, 0, 0);
                bi = reader.read(0, readParam);
            }
            else
                bi = updateImageParams(bi, readParam);
        }
    }
    
    public void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        if (bi != null) {
            final int panWidth = getWidth();
            final int panHeight = getHeight();
            final float biAspect = (float)bi.getWidth()/bi.getHeight();
            final float panAspect = (float)panWidth/panHeight;
            int newdim;
            if (biAspect > panAspect) {
                newdim = (int)(getWidth()/biAspect);
                g2.drawImage(bi,0,(panHeight-newdim)/2,panWidth,newdim,null);
            }
            else {
                newdim = (int)(getHeight()*biAspect);
                g2.drawImage(bi,(panWidth-newdim)/2,0,newdim,panHeight,null);
            }
        }
    }
}

