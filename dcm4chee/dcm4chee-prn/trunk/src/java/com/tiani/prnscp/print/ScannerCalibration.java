/*****************************************************************************
 *                                                                           *
 *  Copyright (c) 2002 by TIANI MEDGRAPH AG                                  *
 *                                                                           *
 *  This file is part of dcm4che.                                            *
 *                                                                           *
 *  This library is free software; you can redistribute it and/or modify it  *
 *  under the terms of the GNU Lesser General Public License as published    *
 *  by the Free Software Foundation; either version 2 of the License, or     *
 *  (at your option) any later version.                                      *
 *                                                                           *
 *  This library is distributed in the hope that it will be useful, but      *
 *  WITHOUT ANY WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 *                                                                           *
 *****************************************************************************/

package com.tiani.prnscp.print;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import javax.imageio.ImageIO;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since December 31, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class ScannerCalibration {
   
   // Constants -----------------------------------------------------
   private static final float MIN_TOT_GRAY_STEP = 100.f;
   
   // Attributes ----------------------------------------------------
   
   /** Holds value of property scanGraySteps. */
   private int scanGraySteps = 32;
   
   /** Holds value of property scanGrayStepDir. */
   private String scanGrayStepDir;

   /** Holds value of property refGrayStepFile. */
   private String refGrayStepFile;
   
   /** Holds value of property refGrayStepODs. */
   private float[] refGrayStepODs;
   
   /** Holds value of property scanBorderThreshold. */
   private int scanBorderThreshold = 50;
   
   /** Holds value of property scanGradientThreshold. */
   private int scanGradientThreshold = 50;
   
   /** Holds value of property scanArea. */
   private int[] scanPointExtension = { 50, 50 };
      
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   /** Getter for property graySteps.
    * @return Value of property graySteps.
    */
   public int getScanGraySteps() {
      return scanGraySteps;
   }   

   /** Setter for property graySteps.
    * @param graySteps New value of property graySteps.
    */
   public void setScanGraySteps(int graySteps) {
      if (graySteps < 2 || graySteps > 65) {
         throw new IllegalArgumentException("steps: " + graySteps);
      }
      this.scanGraySteps = graySteps;
   }
   
   /** Getter for property refGrayStepFile.
    * @return Value of property refGrayStepFile.
    */
   public String getRefGrayStepFile() {
      return this.refGrayStepFile;
   }
   
   /** Setter for property refGrayStepFile.
    * @param refGrayStepFile New value of property refGrayStepFile.
    */
   public void setRefGrayStepFile(String refGrayStepFile) {
      this.refGrayStepFile = refGrayStepFile;
   }
   
   /** Getter for property scanGrayStepDir.
    * @return Value of property scanGrayStepDir.
    */
   public String getScanGrayStepDir() {
      return this.scanGrayStepDir;
   }
   
   /** Setter for property scanGrayStepDir.
    * @param scanGrayStepDir New value of property scanGrayStepDir.
    */
   public void setScanGrayStepDir(String scanGrayStepDir) {
      this.scanGrayStepDir = scanGrayStepDir;
   }
   
   /** Getter for property refGrayStepODs.
    * @return Value of property refGrayStepODs.
    */
   public float[] getRefGrayStepODs() {
      return this.refGrayStepODs;
   }
   
   /** Setter for property refGrayStepODs.
    * @param refGrayStepODs New value of property refGrayStepODs.
    */
   public void setRefGrayStepODs(float[] refGrayStepODs) {
      this.refGrayStepODs = refGrayStepODs;
   }
   
   /** Getter for property scanBorderThreshold.
    * @return Value of property scanBorderThreshold.
    */
   public int getScanBorderThreshold() {
      return this.scanBorderThreshold;
   }
   
   /** Setter for property scanBorderThreshold.
    * @param scanBorderThreshold New value of property scanBorderThreshold.
    */
   public void setScanBorderThreshold(int scanBorderThreshold) {
      this.scanBorderThreshold = scanBorderThreshold;
   }
   
   /** Getter for property scanGradientThreshold.
    * @return Value of property scanGradientThreshold.
    */
   public int getScanGradientThreshold() {
      return this.scanGradientThreshold;
   }
   
   /** Setter for property scanGradientThreshold.
    * @param scanGradientThreshold New value of property scanGradientThreshold.
    */
   public void setScanGradientThreshold(int scanGradientThreshold) {
      this.scanGradientThreshold = scanGradientThreshold;
   }
   
   /** Getter for property scanArea.
    * @return Value of property scanArea.
    */
   public String getScanPointExtension() {
      return "" + scanPointExtension[0] + "x" + scanPointExtension[1];
   }
   
   /** Setter for property scanArea.
    * @param scanArea New value of property scanArea.
    */
   public void setScanPointExtension(String extension) {
      try {
         int xpos = extension.indexOf('x');
         int w = Integer.parseInt(extension.substring(0, xpos));
         int h = Integer.parseInt(extension.substring(xpos+1));
         this.scanPointExtension[0] = w;
         this.scanPointExtension[1] = h;
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("ScanPointextension: " + extension);
      }
   }
   
   public float[] calcScanGrayStepODs() throws IOException {
      if (refGrayStepODs == null) {
         throw new IllegalStateException("refGrayStepODs not initalized!");
      }
      if (refGrayStepFile == null) {
         throw new IllegalStateException("refGrayStepFile not initalized!");
      }
      if (scanGrayStepDir == null) {
         throw new IllegalStateException("scanGrayStepDir not initalized!");
      }
      File refImageFile = PrinterService.toFile(refGrayStepFile);
      File scanDir = PrinterService.toFile(scanGrayStepDir);
      File[] imageFiles = scanDir.listFiles();
      if (imageFiles.length == 0) {
         throw new FileNotFoundException("empty scanGrayStepDir " + scanDir);
      }
      Arrays.sort(imageFiles,
         new Comparator() {
            public int compare(Object o1, Object o2) {
               return (int)(((File)o2).lastModified()
                          - ((File)o1).lastModified());
            }
         });
       
      return calcODs(ImageIO.read(refImageFile), ImageIO.read(imageFiles[0]));
   }
   
   float[] calcODs(BufferedImage refGrayStepImage, BufferedImage grayStepImage)
   {
      return interpolate(
         imgToInvPx(refGrayStepImage, refGrayStepODs.length),
         imgToInvPx(grayStepImage, scanGraySteps));
   }
   
   private float[] interpolate(float[] invRefPx, float[] invPx) {
      float[] result = new float[invPx.length];
      for (int i = 0; i < invPx.length; ++i) {
         int index = Arrays.binarySearch(invRefPx, invPx[i]);
         if (index >= 0) { // exact match            
            result[i] = refGrayStepODs[index];
         } else {
            index = (-index) - 1;
            if (index == 0) { // = extrapolation
               index = 1;
            } else if (index == invPx.length) { // = extrapolation
               index = invPx.length - 1;
            }
            result[i] = refGrayStepODs[index-1]
               + (refGrayStepODs[index] - refGrayStepODs[index-1])
                  * (invPx[i] - invRefPx[index-1])
                  / (invRefPx[index] - invRefPx[index-1]);
         }
      }
      return result;
   }
   
   private int[] findBorder(BufferedImage bi) {
      int w = bi.getWidth();
      int h = bi.getHeight();
      int x0 = w / 2;
      int y0 = h / 2;
      int[] border = { 0, 0, w - 1, h - 1 };
      int threshold = 255 * scanBorderThreshold / 100;
      while ((bi.getRGB(border[0], y0) & 0xff) > threshold
            && border[0] < border[2])
         ++border[0];
      while ((bi.getRGB(x0, border[1]) & 0xff) > threshold
            && border[1] < border[3])
         ++border[1];
      while ((bi.getRGB(border[2], y0) & 0xff) > threshold
            && border[0] < border[2])
         --border[2];
      while ((bi.getRGB(x0, border[3]) & 0xff) > threshold
            && border[1] < border[3])
         --border[3];
      if (border[0] >= border[2] || border[1] >= border[3]) {
         throw new IllegalArgumentException("Failed to detect border");
      }
      return border;
   }
   
   private float[] imgToInvPx(BufferedImage bi, int n) {
      int[] border = findBorder(bi);
      int w = border[2] - border[0];
      int h = border[3] - border[1];
      
      float[] invPx = new float[n];
      // assume top to bottom orientation
      int dw = w * scanPointExtension[0] / 100 + 1;
      int dh = h * scanPointExtension[1] / (100 * n) + 1;
      int x0 = (border[0] + border[2] - dw) / 2;
      int y0 = border[1] + (h / n - dh) / 2;

      float totGrayStep = getInvPx(bi, x0, y0 + h * (n-1)/n, dw, dh)
                        - getInvPx(bi, x0, y0, dw, dh);
                  
      if (Math.abs(totGrayStep) / 2.55f > scanGradientThreshold) {
         for (int i = 0; i < n; ++i) {
            invPx[totGrayStep < 0 ? n - i - 1 : i] = 
               getInvPx(bi, x0, y0 + h * i/n, dw, dh);
         }
      } else {
         // assume left to right orientation
         dw = w * scanPointExtension[1] / (100 * n) + 1;
         dh = h * scanPointExtension[0] / 100 + 1;
         x0 = border[0] + (w / n - dw) / 2;
         y0 = (border[1] + border[3] - dh) / 2;

         totGrayStep = getInvPx(bi, x0 + w * (n-1)/n, y0, dw, dh)
                       - getInvPx(bi, x0, y0, dw, dh);

         if (Math.abs(totGrayStep) / 2.55f > scanGradientThreshold) {
            for (int i = 0; i < n; ++i) {
               invPx[totGrayStep < 0 ? n - i - 1 : i] = 
                  getInvPx(bi, x0 + w * i/n, y0, dw, dh);
            }
         } else {
            throw new IllegalArgumentException(
               "Failed to detect gradient of gray step pattern");
         }
      }
      return invPx;
   }
   
   private float getInvPx(BufferedImage bi, int x, int y, int w, int h) {
      float invPx = 0;
      for (int i = 0; i < w; ++i) {
         for (int j = 0; j < h; ++j) {
            invPx += (bi.getRGB(x + i, y + j) & 0xff) ^ 0xff;
         }
      }
      return invPx / (w * h);
   }
      
}
