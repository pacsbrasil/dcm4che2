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
    private Dataset ds;
    private int windowMin, windowMax;
    private BufferedImage bi;
    private ColorModelParam cmParam;
    private FileImageInputStream fis;
    private byte[] lastPLut;
    //only used for saving the originally loaded BufferedImage when we have to
    // apply (and reapply) the P-LUT to an RGB image's Raster (thereby changing it)
    private BufferedImage origBI;
    private boolean applyingPLutToRGB;
    
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
            catch (UnsupportedOperationException e) {
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

    boolean isApplyingPLutToRGB() {
        return applyingPLutToRGB;
    }

    void setApplyPLutToRGB(boolean b) {
        applyingPLutToRGB = b;
    }

    private int rgbToGrayThreshold = 10; //default

    int getRgbToGrayThreshold() {
        return rgbToGrayThreshold;
    }

    void setRgbToGrayThreshold(int i) {
        rgbToGrayThreshold = i;
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
        if (applyingPLutToRGB) {
            return applyPLutToRGB(param.getPValToDDL());
        }
        else {
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
    }

    public void setImage(File newImg)
        throws UnsupportedOperationException
    {
        FileImageInputStream oldFIS = fis;
        try {
            fis = new FileImageInputStream(newImg);
        }
        catch(Exception e) {
            fis = oldFIS;
            e.printStackTrace();
            return;
        }
        
        BufferedImage oldBI = bi;
        Dataset oldDS = ds;
        
        bi = null;
        reader.setInput(fis);
        try {
            setPLut(lastPLut);
        }
        catch (Exception e) {
            //restore old state
            bi = oldBI;
            fis = oldFIS;
            ds = oldDS;
            e.printStackTrace();
            throw new UnsupportedOperationException("Could not open image");
        }
    }
    
    void setPLut(byte[] plut)
        throws Exception
    {
        final int maxWidth = 1024;
        final int maxHeight = 1024;
        
        lastPLut = plut;
        DcmImageReadParam readParam = (DcmImageReadParam)reader.getDefaultReadParam();
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
                //System.out.println("sx="+subx+",sy="+suby);
                //read pixel data
                readParam.setSourceSubsampling(subx, suby, 0, 0);
                bi = reader.read(0, readParam);
				//get the actual dataset
				DcmMetadata dsMetaData = (DcmMetadata) ((DcmImageReader)reader).getStreamMetadata();
				ds = dsMetaData.getDataset();
				//generate color model params that would be the same
				// as those used in the actual image
				ColorModelFactory cmFactory = ColorModelFactory.getInstance();
			    if (bi.getColorModel() instanceof IndexColorModel
                    && !ds.getString(Tags.PhotometricInterpretation, "MONOCHROME2").equals("PALETTE COLOR")) {
                    cmParam = cmFactory.makeParam(ds, plut);
                    applyingPLutToRGB = false;
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
                else {
                    //on failure, apply the P-LUT directly to gray RGB values
                    // in the BufferedImage raster
                    origBI = bi;
                    bi = applyPLutToRGB(plut);
                    applyingPLutToRGB = true;
                    //ignore window for these types of images
                    windowMin = windowMax = 0;
                }
            }
            else {
                bi = updateImageParams(bi, readParam);
            }
        }
    }

    private boolean isGray(int rgb)
    {
        final int b = rgb & 0xff;
        final int g = (rgb >> 8) & 0xff;
        final int r = (rgb >> 16) & 0xff;
        return rgbToGrayThreshold >=
                Math.max(Math.max(r, g), b) - Math.min(Math.min(r, g), b);
    }
    
    private BufferedImage applyPLutToRGB(byte[] pValToDDL)
    {
        final int w = origBI.getWidth();
        final int h = origBI.getHeight();
        final int[] data = origBI.getRGB(0, 0, w, h, null, 0, w);
        int count = 0;
        int shift = pValToDDL.length == 4096 ? 4 : 0;
        
        for (int rgb, i = 0; i < data.length; ++i) {
            rgb = data[i];
            if (isGray(rgb)) {
                data[i] = (pValToDDL[(rgb & 0xff) << shift] & 0xff)
                     | ((pValToDDL[(rgb >> 8 & 0xff) << shift] & 0xff) << 8)
                     | ((pValToDDL[(rgb >> 16 & 0xff) << shift] & 0xff) << 16);
                ++count;
            }
        }
        BufferedImage newbi = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        newbi.setRGB(0, 0, w, h, data, 0, w);
        return newbi;
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
