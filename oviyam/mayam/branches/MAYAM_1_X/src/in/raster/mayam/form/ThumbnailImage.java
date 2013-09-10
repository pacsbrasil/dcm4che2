/* ***** BEGIN LICENSE BLOCK *****
* Version: MPL 1.1/GPL 2.0/LGPL 2.1
*
* The contents of this file are subject to the Mozilla Public License Version
* 1.1 (the "License"); you may not use this file except in compliance with
* the License. You may obtain a copy of the License at
* http://www.mozilla.org/MPL/
*
* Software distributed under the License is distributed on an "AS IS" basis,
* WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
* for the specific language governing rights and limitations under the
* License.
*
*
* The Initial Developer of the Original Code is
* Raster Images
* Portions created by the Initial Developer are Copyright (C) 2009-2010
* the Initial Developer. All Rights Reserved.
*
* Contributor(s):
* Babu Hussain A
* Meer Asgar Hussain B
* Prakash J
* Suresh V
*
* Alternatively, the contents of this file may be used under the terms of
* either the GNU General Public License Version 2 or later (the "GPL"), or
* the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
* in which case the provisions of the GPL or the LGPL are applicable instead
* of those above. If you wish to allow use of your version of this file only
* under the terms of either the GPL or the LGPL, and not to allow others to
* use your version of this file under the terms of the MPL, indicate your
* decision by deleting the provisions above and replace them with the notice
* and other provisions required by the GPL or the LGPL. If you do not delete
* the provisions above, a recipient may use your version of this file under
* the terms of any one of the MPL, the GPL or the LGPL.
*
* ***** END LICENSE BLOCK ***** */
package in.raster.mayam.form;

import in.raster.mayam.context.ApplicationContext;
import in.raster.mayam.delegate.SeriesListUpdator;
import in.raster.mayam.delegate.ShowImageViewDelegate;
import in.raster.mayam.model.Instance;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;
import javax.swing.border.LineBorder;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.dcm4che.imageio.plugins.DcmMetadata;

/**
 *
 * @author  BabuHussain
 * @version 0.5
 *
 */
public class ThumbnailImage extends javax.swing.JPanel implements MouseListener, FocusListener {
    
    private Iterator iter;
    private ImageReader reader;
    private ImageInputStream iis;
    private Dataset dataset;
    private Image loadedImage;
    private ImageIcon imageIcon;
    private BufferedImage currentbufferedimage;
    private BufferedImage image;
    private String fileUrl;   
    private String instanceNo="";
    private String totalInstace = "";
    public boolean focusGained = false;
    private Instance img=null;
    private boolean instanceListAdded=false;

    private int nFrames = 0;
    private boolean mulitiFrame = false;

    public ThumbnailImage()
    {       
    }
    public ThumbnailImage(String dicomFileUrl) {
        fileUrl = dicomFileUrl;
        try {
            readDicomFile(new File(dicomFileUrl));
            retrieveInfo();
        } catch (Exception e) {
            e.printStackTrace();
        }
        initComponents();
        this.addListeners();
    }
    public ThumbnailImage(Instance img)
    {
        this.img=img;
        instanceNo=img.getInstance_no();
        fileUrl=img.getFilepath();
        GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        BufferedImage temp = gc.createCompatibleImage(loadedImage.getWidth(null), loadedImage.getHeight(null),Transparency.BITMASK);
        Graphics2D g2 = temp.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.drawImage(loadedImage, 0, 0, temp.getWidth(), temp.getHeight(), null);
        g2.dispose();
        image = temp;
//        image = new BufferedImage(img.getPixelData().getWidth(null), img.getPixelData().getHeight(null), BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2 = image.createGraphics();
//        g2.drawImage(img.getPixelData(),0,0,null);     
        initComponents();
        this.addListeners();
    }

    private void addListeners() {
        this.addFocusListener(this);
        this.addMouseListener(this);
    }

    private void retrieveInfo() {        
        totalInstace = dataset.getString(Tags.NumberOfSeriesRelatedInstances);
        instanceNo = dataset.getString(Tags.InstanceNumber);
    }
    public void updateInstanceList()
    {
        SeriesListUpdator series=new SeriesListUpdator();
        String studyUID=dataset.getString(Tags.StudyInstanceUID);
        String seriesUID=dataset.getString(Tags.SeriesInstanceUID);
        String instanceUID=dataset.getString(Tags.SOPInstanceUID);
        if(ApplicationContext.databaseRef.getMultiframeStatus())
        {
            series.addSeriesToStudyList(studyUID,seriesUID,mulitiFrame,instanceUID,false);
        }
        else
        series.addSeriesToStudyList(studyUID,seriesUID,false);
    }

    private void readDicomFile(File selFile) {
        try {
            iis = ImageIO.createImageInputStream(selFile);
            iter = ImageIO.getImageReadersByFormatName("DICOM");
            reader = (ImageReader) iter.next();
            reader.setInput(iis, false);
            dataset = ((DcmMetadata) reader.getStreamMetadata()).getDataset();
            try {                
                currentbufferedimage = reader.read(0);
                nFrames = reader.getNumImages(true);
                if (nFrames - 1 > 0) {
                    mulitiFrame = true;
                }
                imageIcon = new ImageIcon();
                imageIcon.setImage(currentbufferedimage);
                loadedImage = imageIcon.getImage();
                GraphicsConfiguration gc = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
                BufferedImage temp = gc.createCompatibleImage(loadedImage.getWidth(null), loadedImage.getHeight(null),Transparency.BITMASK);
                Graphics2D g2 = temp.createGraphics();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g2.drawImage(loadedImage, 0, 0, temp.getWidth(), temp.getHeight(), null);
                g2.dispose();
                image = temp;
//                image = new BufferedImage(loadedImage.getWidth(null), loadedImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
//                Graphics2D g2 = image.createGraphics();
//                g2.drawImage(loadedImage,0,0,null);
            } catch (RuntimeException e) {
                System.out.println("while Reading Image " + e.getMessage());
            }
        } catch (Exception e) {            
            System.out.println(e.toString());
        }
        finally
        {
            try {
                iis.close();
                iter=null;                
                currentbufferedimage=null;
                imageIcon=null;
                loadedImage=null;

            } catch (IOException ex) {
                Logger.getLogger(ThumbnailImage.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }  
    
    public void openSingleImage(String filePath) {      
         if(!ApplicationContext.imageViewExist())
        {
            ApplicationContext.createImageView();
        }
        ShowImageViewDelegate showImgView = new ShowImageViewDelegate(filePath);
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }
    private void setHeaders() {
        String seriesDesc=dataset.getString(Tags.SeriesDescription);
        String totalInstance=dataset.getString(Tags.NumberOfSeriesRelatedInstances)==null?"":dataset.getString(Tags.NumberOfSeriesRelatedInstances);
        seriesDescriptionLabel.setText(""+seriesDesc);
        totalImagesLabel.setText(totalInstance+" Images");       
    }

    /**
     * This override routine used to paint the image box
     * @param gs
     */
    @Override
    public void paintComponent(Graphics gs) {
        Graphics2D g = (Graphics2D) gs;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        setHeaders();
        if (image != null) {
            g.drawImage(image, (this.getSize().width - 70) / 2, 35, 70, 70, null);
        }
        if (focusGained) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.BLACK);
        }
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
        this.currentbufferedimage = null;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        seriesDescriptionLabel = new javax.swing.JLabel();
        totalImagesLabel = new javax.swing.JLabel();

        setBackground(new java.awt.Color(0, 0, 0));
        setDoubleBuffered(false);
        setOpaque(false);
        setPreferredSize(new java.awt.Dimension(128, 128));

        seriesDescriptionLabel.setFont(new java.awt.Font("Times", 0, 10));
        seriesDescriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        seriesDescriptionLabel.setText("Series Discription");

        totalImagesLabel.setFont(new java.awt.Font("Times", 0, 10));
        totalImagesLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        totalImagesLabel.setText("Total Images");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(seriesDescriptionLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
            .add(totalImagesLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(seriesDescriptionLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(totalImagesLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 11, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(94, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    public void mouseClicked(MouseEvent e) {
        this.requestFocus();
        if (e.getClickCount() == 2) {
           // openSingleImage(this.fileUrl);
        }
    }

    public void mousePressed(MouseEvent e) {
        if(!instanceListAdded)
            updateInstanceList();
    }

    public void mouseReleased(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void focusGained(FocusEvent e) {     
         this.setBorder(new LineBorder(Color.BLUE));       
    }

    public void focusLost(FocusEvent e) {      
         this.setBorder(new LineBorder(Color.DARK_GRAY));      
    }
    public void clearAll() {
        this.removeFocusListener(this);
        this.removeMouseListener(this);
        this.removeAll();
        iter = null;
        reader = null;
        iis = null;
        dataset = null;
        loadedImage = null;
        imageIcon = null;
        currentbufferedimage = null;
        image.flush();
        image = null;
        fileUrl = null;
        instanceNo = null;
        totalInstace = null;
        focusGained = false;
        img = null;
        seriesDescriptionLabel=null;
        totalImagesLabel=null;
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel seriesDescriptionLabel;
    private javax.swing.JLabel totalImagesLabel;
    // End of variables declaration//GEN-END:variables
}
