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

public class ImagePanel extends JPanel
{
    private ImageReader reader;
    private DcmImageReadParam readParam;
    private BufferedImage bi;
    private FileImageInputStream fis;
    
    ImagePanel(File image)
    {
        super();
        //get reader
        Iterator iter = ImageIO.getImageReadersByFormatName("DICOM");
        reader = (ImageReader) iter.next();
        try {fis = new FileImageInputStream(image);}catch(Exception e) {}
        reader.setInput(fis);
    }
    
    public void setPLut(byte[] plut)
    {
        readParam = (DcmImageReadParam) reader.getDefaultReadParam();
        readParam.setPValToDDL(plut);
        try {
            this.bi = reader.read(0, readParam);
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

