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

import org.jboss.logging.Logger;

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
   private static final int EXTENSION_MAX = 80;
   private static final int THRESHOLD_MIN = 100;
   private static final int THRESHOLD_MAX = 250;
   private static final int FIND_STEP_NUM_MIN = 3;
   private static final float FIND_STEP_ERR_MAX = 0.2f;
   private static final int BORDER_MIN = 50;
   
   // Attributes ----------------------------------------------------
   private Logger log;
   
   /** Holds value of property scanGrayStepDir. */
   private File scanGrayStepDir;
   
   /** Holds value of property refGrayStepFile. */
   private File refGrayStepFile;
   
   /** Holds value of property refGrayStepODs. */
   private float[] refGrayStepODs;
   
   /** Holds value of property scanArea. */
   private int[] scanPointExtension = { 50, 50 };
   
   /** Holds value of property blackThreshold. */
   private int blackThreshold = 180;
      
   /** Holds value of property whiteThreshold. */
   private int whiteThreshold = 220;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public ScannerCalibration(Logger log) {
      this.log = log;
   }
   
   // Public --------------------------------------------------------
      
   /** Getter for property refGrayStepFile.
    * @return Value of property refGrayStepFile.
    */
   public File getRefGrayStepFile() {
      return this.refGrayStepFile;
   }
   
   /** Setter for property refGrayStepFile.
    * @param refGrayStepFile New value of property refGrayStepFile.
    */
   public void setRefGrayStepFile(File refGrayStepFile) {
      this.refGrayStepFile = refGrayStepFile;
   }
   
   /** Getter for property scanGrayStepDir.
    * @return Value of property scanGrayStepDir.
    */
   public File getScanGrayStepDir() {
      return this.scanGrayStepDir;
   }
   
   /** Setter for property scanGrayStepDir.
    * @param scanGrayStepDir New value of property scanGrayStepDir.
    */
   public void setScanGrayStepDir(File scanGrayStepDir) {
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
      float[] tmp = (float[]) refGrayStepODs.clone();
      Arrays.sort(tmp);
      if (!Arrays.equals(tmp, refGrayStepODs)) {
         throw new IllegalArgumentException(
            "refGrayStepODs[" + tmp.length + "] not monotonic increasing");
      }
      this.refGrayStepODs = tmp;
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
      int w, h;
      try {
         int xpos = extension.indexOf('x');
         w = Integer.parseInt(extension.substring(0, xpos));
         h = Integer.parseInt(extension.substring(xpos+1));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("extension: " + extension);
      }
      if (w <= 0 && w > EXTENSION_MAX && h <= 0 && h >= EXTENSION_MAX) {
         throw new IllegalArgumentException("extension: " + extension);
      }
      this.scanPointExtension[0] = w;
      this.scanPointExtension[1] = h;
   }
      
   /** Setter for property scanThreshold.
    * @param threshold New value of property scanThreshold.
    */
   public void setScanThreshold(String threshold) {
      int black, white;
      try {
         int delim = threshold.indexOf('/');
         black = Integer.parseInt(threshold.substring(0, delim));
         white = Integer.parseInt(threshold.substring(delim+1));
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("threshold: " + threshold);
      }
      if (black < THRESHOLD_MIN && white < THRESHOLD_MAX && black >= white) {            
         throw new IllegalArgumentException("threshold: " + threshold);
      }
      this.blackThreshold = black;
      this.whiteThreshold = white;
   }
   
   /** Getter for property scanThreshold.
    * @return Value of property scanThreshold.
    */
   public String getScanThreshold() {
      return "" + blackThreshold + "/" + whiteThreshold;
   }
   
   public float[] calculateGrayStepODs()
      throws CalibrationException
   {
      if (refGrayStepODs == null) {
         throw new IllegalStateException("refGrayStepODs not initalized!");
      }
      if (refGrayStepFile == null) {
         throw new IllegalStateException("refGrayStepFile not initalized!");
      }
      if (scanGrayStepDir == null) {
         throw new IllegalStateException("scanGrayStepDir not initalized!");
      }
      try {
         if (!scanGrayStepDir.isDirectory()) {
            throw new FileNotFoundException("scanGrayStepDir " + scanGrayStepDir
            + " is not a directory!");
         }
         File[] imageFiles = scanGrayStepDir.listFiles();
         if (imageFiles.length == 0) {
            throw new FileNotFoundException("empty scanGrayStepDir " + scanGrayStepDir);
         }
         Arrays.sort(imageFiles,
         new Comparator() {
            public int compare(Object o1, Object o2) {
               return (int)(((File)o2).lastModified()
               - ((File)o1).lastModified());
            }
         });

         return interpolate(
            imgToPx(ImageIO.read(refGrayStepFile), 0xff, refGrayStepFile),
            imgToPx(ImageIO.read(imageFiles[0]), 0xff, imageFiles[0]),
            refGrayStepFile,
            imageFiles[0]);
      } catch (IOException e) {
         throw new CalibrationException("calculateGrayStepODs failed: ", e);
      }
   }
   
   float[] calculateGrayStepODs(BufferedImage refGrayStepImage,
                                BufferedImage grayStepImage)
      throws CalibrationException
   {
      return interpolate(
         imgToPx(refGrayStepImage, 0xff, null),
         imgToPx(grayStepImage, 0xff, null),
         null, null);
   }
   
   private float[] interpolate(float[] invRefPx, float[] invPx,
         File refSrc, File src)
      throws CalibrationException
   {
      if (invRefPx.length != refGrayStepODs.length) {
         throw new CalibrationException("Mismatch refGrayStepImage[steps="
            + invRefPx.length + "] with refGrayStepODs[length="
            + refGrayStepODs.length + "]");
      }      
      invRefPx = ensureMonotonic(invRefPx, refSrc);
      invPx = ensureMonotonic(invPx, src);
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
      if (log != null && log.isDebugEnabled()) {
         StringBuffer sb = new StringBuffer("Calculated GrayStepODs:");
         for (int i = 0; i < invPx.length; ++i) { 
            sb.append("\r\n\t");
            sb.append(result[i]);
         }
         log.debug(sb.toString());
      }
      
      return result;
   }
   
   private float[] ensureMonotonic(float[] invPx, File src) {
      float[] tmp = (float[]) invPx.clone();
      Arrays.sort(tmp);
      if (Arrays.equals(tmp, invPx)) {
         return invPx;
      }
      if (log != null) {
         log.warn("Gray steps in " + src
            + " not monotonic increasing - sort steps to continue!");
      }
      return tmp;
   }
      
   
   private float[] imgToPx(BufferedImage bi, int xor, File src)
      throws CalibrationException
   {
      int[] border = findBorder(bi, src);
      int[] steps = findSteps(bi, border, src);
      int w = border[2] - border[0];
      int h = border[3] - border[1];
      int n = Math.abs(steps[0] + steps[1]);
      float[] px = new float[n];
      if (steps[0] == 0) {
      // top to bottom orientation
         int dw = w * scanPointExtension[0] / 100 + 1;
         int dh = h * scanPointExtension[1] / (100 * n) + 1;
         int x0 = (border[0] + border[2] - dw) / 2;
         int y0 = border[1] + (h / n - dh) / 2;
      
         for (int i = 0; i < n; ++i) {
            px[steps[1] < 0 ? n - i - 1 : i] =
               getMeanPxVal(bi, x0, y0 + h * i/n, dw, dh, xor);
         }
      } else {
         // left to right orientation
         int dw = w * scanPointExtension[1] / (100 * n) + 1;
         int dh = h * scanPointExtension[0] / 100 + 1;
         int x0 = border[0] + (w / n - dw) / 2;
         int y0 = (border[1] + border[3] - dh) / 2;
         
         for (int i = 0; i < n; ++i) {
            px[steps[0] < 0 ? n - i - 1 : i] =
               getMeanPxVal(bi, x0 + w * i/n, y0, dw, dh, xor);
         }
      }
      return px;
   }
   
   private float getMeanPxVal(BufferedImage bi,
         int x, int y, int w, int h, int xor)
   {
      float px = 0;
      for (int i = 0; i < w; ++i) {
         for (int j = 0; j < h; ++j) {
            px += (bi.getRGB(x + i, y + j) & 0xff) ^ xor;
         }
      }
      return px / (w * h);
   }
   
   private int[] findBorder(BufferedImage bi, File src)
      throws CalibrationException
   {
      int w = bi.getWidth();
      int h = bi.getHeight();
      int x0 = w / 2;
      int y0 = h / 2;
      int[] b = { 0, 0, w - 1, h - 1 };
      while (b[0] < b[2] && (bi.getRGB(b[0], y0) & 0xff) > blackThreshold)
         ++b[0];
      while (b[1] < b[3] && (bi.getRGB(x0, b[1]) & 0xff) > blackThreshold)
         ++b[1];
      while (b[0] < b[2] && (bi.getRGB(b[2], y0) & 0xff) > blackThreshold)
         --b[2];
      while (b[1] < b[3] && (bi.getRGB(x0, b[3]) & 0xff) > blackThreshold)
         --b[3];
      if ((b[2] - b[0]) * 100 < BORDER_MIN * w 
            || (b[3] - b[1]) * 100 < BORDER_MIN * h)
      {
         throw new CalibrationException("Failed to detect border in " + src);
      }
      if (log != null && log.isDebugEnabled()) {
         log.debug("Detected border[left=" + b[0] + ", top=" + b[1]
            + ", right=" + b[2] + ", bottom=" + b[3] + "] in " + src);
      }
      return b;
   }

   private int[] findSteps(BufferedImage bi, int border[], File src)
      throws CalibrationException
   {
      int w = border[2] - border[0];
      int h = border[3] - border[1];
      int x0 = border[0] + w / 2;
      int y0 = border[1] + h / 2;
      int[] hLine = bi.getRGB(border[0], y0, w, 1, null, 0, w);
      int[] vLine = bi.getRGB(x0, border[1], 1, h, null, 0, 1);
      int hG = gradient(hLine);
      int vG = gradient(vLine);
      boolean portait = Math.abs(vG) > Math.abs(hG);
      if (log != null && log.isDebugEnabled()) {
         log.debug("Detected "
            + (portait ? (vG > 0 ? "bottom-top" : "top-bottom")
                       : (hG > 0 ? "right-left" : "left-right"))
            + " gray step gradient in " + src);
      }
      
      int[] result = new int[2];
      result[portait ? 1 : 0] = portait
         ? detectSteps(vLine, vG > 0, src) 
         : detectSteps(hLine, hG > 0, src); 
      return result;
   }
   
   private int gradient(int[] argb) {
      int g = 0;
      int n2 = n2 = argb.length/2;
      for (int i = 0; i < n2; ++i) {
         g -= argb[i] & 0xff;
      }
      for (int i = n2; i < argb.length; ++i) {
         g += argb[i] & 0xff;
      }
      return g;
   }
   
   private int detectSteps(int[] a, boolean b2w, File src)
      throws CalibrationException
   {
      float h = 0.f;
      float err = 0.f;
      int n = 0;
      for (int i = 0; i < a.length && err < FIND_STEP_ERR_MAX; ++n) { 
         if (n > 0) {
            h = (float) i / n;
         }
         int d = 0;
         while (i < a.length
               && (a[b2w ? i : a.length-i-1] & 0xff) < whiteThreshold)
         {
            ++i; ++d;
         }
         while (i < a.length
               && (a[b2w ? i : a.length-i-1] & 0xff) > blackThreshold)
         {
            ++i; ++d;
         }
         if (n > 0) {
            err = Math.abs(h - d) / h;
         }
      }
      if (n < FIND_STEP_NUM_MIN) {
         throw new CalibrationException(
            "Failed to detect more than " + (n-1) + " steps on " + src);
      }
      int steps = Math.round(a.length / h);
      if (log != null && log.isDebugEnabled()) {
         log.debug("Detected " + (n-1) + " from " + steps + " gray steps in " + src);
      }
      return b2w ? -steps : steps;
   }
      
}
