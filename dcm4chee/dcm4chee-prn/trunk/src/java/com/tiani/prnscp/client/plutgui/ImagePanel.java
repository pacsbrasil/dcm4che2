package com.tiani.prnscp.client.plutgui;

import java.util.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.*;
import java.io.*;
import javax.imageio.*;
import javax.imageio.stream.*;

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmImageReadParam;
import org.dcm4che.image.ColorModelFactory;
import org.dcm4che.image.ColorModelParam;
import org.dcm4che.imageio.plugins.DcmMetadata;
import org.dcm4cheri.imageio.plugins.DcmImageReader;

public class ImagePanel extends JPanel
{
    private ImageReader reader;
    private DcmImageReadParam readParam;
    private Dataset ds;
    private int windowMin, windowMax;
    private BufferedImage bi;
    private ColorModelParam cmParam;
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
            catch (Exception e) {
                e.printStackTrace();
            }
            repaint();
        }
    }
    
    public BufferedImage getBI()
    {
        return bi;
    }
    
    public int getWindowMin()
    {
        return windowMin;
    }
    public int getWindowMax()
    {
        return windowMax;
    }
    
    public BufferedImage updateImageParams(BufferedImage bi, DcmImageReadParam param)
        throws IOException
    {
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
            e.printStackTrace();
            return;
        }
        bi = null;
        reader.setInput(fis);
        setPLut(lastPLut);
    }
    
    public void setPLut(byte[] plut)
        throws Exception
    {
        final int maxWidth = 512;
        final int maxHeight = 512;
        
        lastPLut = plut;
        readParam = (DcmImageReadParam)reader.getDefaultReadParam();
        readParam.setPValToDDL(plut);
        if (fis != null) { //check if reader input stream has been set
            if (bi == null) { //first time?
                //get the actual dataset
                DcmMetadata dsMetaData = (DcmMetadata)((DcmImageReader)reader).getImageMetadata(0);
                ds = dsMetaData.getDataset();
                //generate color model params that would be the same
                // as those used in the actual image
                ColorModelFactory cmFactory = ColorModelFactory.getInstance();
                cmParam = cmFactory.makeParam(ds, plut);
                //see if it is necessary to subsample pixel data
                int width, height;
                int subx = 1, suby = 1;
                width = reader.getWidth(0);
                height = reader.getHeight(0);
                if (width > maxWidth)
                    subx = width / maxWidth;
                if (height > maxHeight)
                    suby = height / maxHeight;
                //read pixel data
                readParam.setSourceSubsampling(subx, suby, 0, 0);
                bi = reader.read(0, readParam);
                //set window min/max
                // these only need to be set when an image is loaded sine a
                // change in the P-LUT will not affect the ColorModels returned
                // windowing parameters
                float wCenter = cmParam.getWindowCenter(0);
                float wWidth = cmParam.getWindowWidth(0);
                windowMin = cmParam.toPixelValue(wCenter - wWidth / 2);
                windowMax = cmParam.toPixelValue(wCenter + wWidth / 2);
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

