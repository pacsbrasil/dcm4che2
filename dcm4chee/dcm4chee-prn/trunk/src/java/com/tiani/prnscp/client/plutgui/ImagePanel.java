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
    
    BufferedImage getBI()
    {
        return bi;
    }
    
    Dataset getDS()
    {
        return ds;
    }
    
    ColorModelParam getColorModelParam()
    {
        return cmParam;
    }
    
    int getWindowMin()
    {
        return windowMin;
    }
    
    int getWindowMax()
    {
        return windowMax;
    }
    
    int[] getSamples()
    {
        int[] samples = new int[bi.getWidth() * bi.getHeight()];
        
        bi.getRaster().getPixels(0, 0, bi.getWidth(), bi.getHeight(), samples);
        //bi.getRaster().getSamples(0,0,bi.getWidth(),bi.getHeight(),0,samples);
        
        boolean signed = (ds.getInt(Tags.PixelRepresentation, 0) == 1);
        int bs = ds.getInt(Tags.BitsStored, 16);
        
        //transform raw pixel values to sample values
        for (int i = 0; i < samples.length; i++) {
            samples[i] = cmParam.toSampleValue(samples[i]);
        }
        
        return samples;
    }
    
    private BufferedImage updateImageParams(BufferedImage bi, DcmImageReadParam param)
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
    
    void setPLut(byte[] plut)
        throws Exception
    {
        final int maxWidth = 1024;
        final int maxHeight = 1024;
        
        lastPLut = plut;
        readParam = (DcmImageReadParam)reader.getDefaultReadParam();
        readParam.setPValToDDL(plut);
        if (fis != null) { //check if reader input stream has been set
            if (bi == null) { //first time?
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
                System.out.println("sx="+subx+",sy="+suby);
                readParam.setSourceSubsampling(subx, suby, 0, 0);
                bi = reader.read(0, readParam);
				//get the actual dataset
				DcmMetadata dsMetaData = (DcmMetadata) ((DcmImageReader)reader).getStreamMetadata();
				ds = dsMetaData.getDataset();
				//generate color model params that would be the same
				// as those used in the actual image
				ColorModelFactory cmFactory = ColorModelFactory.getInstance();
				cmParam = cmFactory.makeParam(ds, plut);
                //set window min/max
                // these only need to be set when an image is loaded sine a
                // change in the P-LUT will not affect the ColorModels returned
                // windowing parameters
                if (cmParam.getNumberOfWindows() > 0) {
                    float wCenter, wWidth;
                    wCenter = cmParam.getWindowCenter(0);
                    wWidth = cmParam.getWindowWidth(0);
                    windowMin = cmParam.toSampleValue(
                                    cmParam.toPixelValue(wCenter - wWidth / 2));
                    windowMax = cmParam.toSampleValue(
                                    cmParam.toPixelValue(wCenter + wWidth / 2));
                }
                else {
                    windowMin = windowMax = 0;
                }
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
            final float biAspect = (float) bi.getWidth() / bi.getHeight();
            final float panAspect = (float) panWidth / panHeight;
            int newdim;
            if (biAspect > panAspect) {
                newdim = (int)(getWidth() / biAspect);
                g2.drawImage(bi, 0, (panHeight - newdim) / 2, panWidth, newdim, null);
            }
            else {
                newdim = (int)(getHeight() * biAspect);
                g2.drawImage(bi, (panWidth - newdim) / 2, 0, newdim, panHeight, null);
            }
        }
    }
}

