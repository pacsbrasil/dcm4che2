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

import org.dcm4che.util.DAFormat;
import org.dcm4che.util.TMFormat;

import org.jboss.logging.Logger;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.ImageReadParam;
import javax.imageio.stream.ImageInputStream;

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
   
   /** Holds value of property calibrationDir. */
   private File calibrationDir;
   
   /** Holds value of property refGrayscaleODs. */
   private float[] refGrayscaleODs;
   
   /** Holds value of property scanArea. */
   private int[] scanPointExtension = { 50, 50 };
   
   /** Holds value of property blackThreshold. */
   private int blackThreshold = 180;
      
   /** Holds value of property whiteThreshold. */
   private int whiteThreshold = 220;
   
   private File lastRefFile;

   private long lastRefFileModified;
   
   private float[] cachedRefData;

   private File lastScanFile;

   private long lastScanFileModified;
   
   private float[] cachedScanData;

   private float[] cachedODs;
   
   /** Holds value of property refGrayscaleFileName. */
   private String refFileName;
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public ScannerCalibration(Logger log) {
      this.log = log;
   }
   
   // Public --------------------------------------------------------
      
   /** Getter for property scanGrayscaleDir.
    * @return Value of property scanGrayscaleDir.
    */
   public File getCalibrationDir() {
      return this.calibrationDir;
   }
   
   /** Setter for property scanGrayscaleDir.
    * @param scanGrayscaleDir New value of property scanGrayscaleDir.
    */
   public void setCalibrationDir(File calibrationDir) {
      this.calibrationDir = calibrationDir;
   }
   
   /** Getter for property refFileName.
    * @return Value of property refFileName.
    */
   public String getRefGrayscaleFileName() {
      return this.refFileName;
   }
   
   /** Setter for property refFileName.
    * @param refFileName New value of property refFileName.
    */
   public void setRefGrayscaleFileName(String refFileName) {
      this.refFileName = refFileName;
   }
   
   /** Getter for property refGrayscaleODs.
    * @return Value of property refGrayscaleODs.
    */
   public float[] getRefGrayscaleODs() {
      return this.refGrayscaleODs;
   }
   
   /** Setter for property refGrayscaleODs.
    * @param refGrayscaleODs New value of property refGrayscaleODs.
    */
   public void setRefGrayscaleODs(float[] refGrayscaleODs) {
      float[] tmp = (float[]) refGrayscaleODs.clone();
      Arrays.sort(tmp);
      if (!Arrays.equals(tmp, refGrayscaleODs)) {
         throw new IllegalArgumentException(
            "refGrayscaleODs[" + tmp.length + "] not monotonic increasing");
      }
      this.refGrayscaleODs = tmp;
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
         int delim = threshold.indexOf('\\');
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
      return "" + blackThreshold + "\\" + whiteThreshold;
   }
   
   /** Getter for property dateOfLastCalibration.
    * @return Value of property dateOfLastCalibration.
    */
   public String getDateOfLastCalibration() {
      return new DAFormat().format(new Date(lastScanFileModified));
   }
   
   /** Getter for property timeOfLastCalibration.
    * @return Value of property timeOfLastCalibration.
    */
   public String getTimeOfLastCalibration() {
      return new TMFormat().format(new Date(lastScanFileModified));
   }
   
   public File getScanDir(String printer) {
      if (calibrationDir == null) {
         throw new IllegalStateException("calibrationDir not initalized!");
      }
      char[] a = printer.toCharArray();
      for (int i = 0; i < a.length; ++i) {
         char c = a[i];
         if (!((c >= 'A' && c <= 'Z')
            || (c >= 'a' && c <= 'z')
            || (c >= '0' && c <= '9')))
         {
            a[i] = '_';
         }
      }
      return new File(calibrationDir, new String(a));
   }
   
   public float[] calculateGrayscaleODs(String printer, boolean force)
      throws CalibrationException
   {
      if (refGrayscaleODs == null) {
         throw new IllegalStateException("refGrayscaleODs not initalized!");
      }
      try {
         File refFile = new File(calibrationDir, refFileName);
         if (!refFile.isFile()) {
            throw new FileNotFoundException(
               "Could not find file " + refFile);
         }
         File scanDir = getScanDir(printer);
         if (!scanDir.isDirectory()) {
            throw new FileNotFoundException(
               "Could not find directory " + scanDir);
         }
         File[] scanFiles = scanDir.listFiles();
         if (scanFiles.length == 0) {
            throw new FileNotFoundException(
               "No scans in directory " + scanDir);
         }
         Arrays.sort(scanFiles,
         new Comparator() {
            public int compare(Object o1, Object o2) {
               return (int)(((File)o2).lastModified()
               - ((File)o1).lastModified());
            }
         });
         if (force || cachedRefData == null
                   || !refFile.equals(lastRefFile)
                   || refFile.lastModified() > lastRefFileModified)
         {
            if (log != null && log.isDebugEnabled()) {
               log.debug("analysing " + refFile.getName());
            }
            cachedRefData = analyse(refFile);
            lastRefFile = refFile;
            lastRefFileModified = refFile.lastModified();
            cachedODs = null;
         } else {
            if (log != null && log.isDebugEnabled()) {
               log.debug("use cached data for " + lastRefFile.getName());
            }
         }
         
         if (force || cachedScanData == null
                   || !scanFiles[0].equals(lastScanFile)
                   || scanFiles[0].lastModified() > lastScanFileModified)
         {
            if (log != null && log.isDebugEnabled()) {
               log.debug("analysing " + scanFiles[0].getName());
            }
            cachedScanData = analyse(scanFiles[0]);
            lastScanFile = scanFiles[0];
            lastScanFileModified = scanFiles[0].lastModified();
            cachedODs = null;
         } else {
            if (log != null && log.isDebugEnabled()) {
               log.debug("use cached data for " + lastScanFile.getName());
            }
         }
         
         if (cachedODs == null) {
            if (log != null) {
               log.debug("interpolating ODs");
            }
            cachedODs = interpolate(cachedRefData, cachedScanData);
         } else {
            if (log != null) {
               log.debug("use cached ODs");
            }
         }
         return cachedODs;
      } catch (IOException e) {
         throw new CalibrationException("calculateGrayscaleODs failed: ", e);
      }
   }
   
   private float[] interpolate(float[] invRefPx, float[] invPx)
      throws CalibrationException
   {
      if (invRefPx.length != refGrayscaleODs.length) {
         throw new CalibrationException("Mismatch of detected gray steps["
            + invRefPx.length + "] in "
            + (lastRefFile == null ? "?" : lastRefFile.getName())
            + " with refGrayscaleODs float[" + refGrayscaleODs.length + "]");
      }
      ensureMonotonic(invRefPx, lastRefFile);
      ensureMonotonic(invPx, lastScanFile);
      float[] result = new float[invPx.length];
      for (int i = 0; i < invPx.length; ++i) {
         int index = Arrays.binarySearch(invRefPx, invPx[i]);
         if (index >= 0) { // exact match
            result[i] = refGrayscaleODs[index];
         } else {
            index = (-index) - 1;
            if (index == 0) { // = extrapolation
               index = 1;
            } else if (index == invPx.length) { // = extrapolation
               index = invPx.length - 1;
            }
            result[i] = refGrayscaleODs[index-1]
               + (refGrayscaleODs[index] - refGrayscaleODs[index-1])
                  * (invPx[i] - invRefPx[index-1])
                  / (invRefPx[index] - invRefPx[index-1]);
         }
      }
      if (log != null && log.isDebugEnabled()) {
         StringBuffer sb = new StringBuffer("calculated GrayscaleODs:");
         for (int i = 0; i < invPx.length; ++i) { 
            sb.append("\r\n\t");
            sb.append(result[i]);
         }
         log.debug(sb.toString());
      }
      
      return result;
   }
   
   private boolean isMonotonic(float[] a) {
      for (int i = 1; i < a.length; ++i) {
         if (a[i] < a[i-1]) {
            return false;
         }
      }
      return true;
   }
   
   private void ensureMonotonic(float[] a, File src) {
      if (!isMonotonic(a)) {
         Arrays.sort(a);
         if (log != null) {
            log.warn("Grayscale " + (src == null ? "" : src.getName())
               + " not monotonic increasing! Calibrate with sorted steps!");
         }
      }
   }

   private ImageReader findReader(File f)
      throws CalibrationException
   {
      String fname = f.getName();
      String fileSuffix = fname.substring(fname.lastIndexOf('.')+1);
      Iterator it = ImageIO.getImageReadersBySuffix(fileSuffix);
      if (!it.hasNext()) {
         throw new CalibrationException("Unsupported Image Format " + f);
      }
      return (ImageReader) it.next();
   }
   
   private float[] analyse(File f)
      throws CalibrationException, IOException
   {
      ImageReader r = findReader(f);
      ImageInputStream in = ImageIO.createImageInputStream(f);
      try {
         BufferedImage bi;
         ImageReadParam rParam = r.getDefaultReadParam();
         r.setInput(in);
         int w = r.getWidth(0);
         int h = r.getHeight(0);
         int x0 = w / 2;
         int y0 = h / 2; 
         rParam.setSourceRegion(new Rectangle(0, y0, w, 1));
         int[] hline = r.read(0, rParam).getRGB(0, 0, w, 1, null, 0, w);
         rParam.setSourceRegion(new Rectangle(x0, 0, 1, h));
         int[] vline = r.read(0, rParam).getRGB(0, 0, 1, h, null, 0, 1);
         int[] lr = findBorder(hline);
         int[] tb = findBorder(vline);
         int hgrad = gradient(hline, lr);
         int vgrad = gradient(vline, tb);
         boolean portrait = Math.abs(vgrad) > Math.abs(hgrad);
         if (log != null && log.isDebugEnabled()) {
            log.debug("detected Border[left=" + lr[0] + ", right=" + lr[1]
               + ", top=" + tb[0] + ", bottom=" + tb[1] + "], Gradient["
               + (portrait ? (vgrad > 0 ? "bottom-top" : "top-bottom")
                           : (hgrad > 0 ? "right-left" : "left-right"))
               + "]");
         }
         int n = portrait
            ? findSteps(vline, tb[0], tb[1], vgrad > 0)
            : findSteps(hline, lr[0], lr[1], hgrad > 0);
         float[] px = new float[n];
         if (portrait) {
         // top to bottom orientation
            int h1 = tb[1] - tb[0];
            int dw = (lr[1] - lr[0]) * scanPointExtension[0] / 100 + 1;
            int dh = h1 * scanPointExtension[1] / (100 * n) + 1;
            int x1 = (lr[0] + lr[1] - dw) / 2;
            int y = tb[0] + (h1 / n - dh) / 2;
            int[] buf = new int[dw * dh];

            for (int i = 0; i < n; ++i) {
               rParam.setSourceRegion(new Rectangle(x1, y + h1 * i/n, dw, dh));
               px[vgrad > 0 ? n - i - 1 : i] =
                  average(r.read(0, rParam).getRGB(0, 0, dw, dh, buf, 0, dw));
            }
         } else {
            // left to right orientation
            int w1 = lr[1] - lr[0];
            int dw = w1 * scanPointExtension[1] / (100 * n) + 1;
            int dh = (tb[1] - tb[0]) * scanPointExtension[0] / 100 + 1;
            int x = lr[0] + (w1/ n - dw) / 2;
            int y1 = (tb[0] + tb[1] - dh) / 2;
            int[] buf = new int[dw * dh];

            for (int i = 0; i < n; ++i) {
              rParam.setSourceRegion(new Rectangle(x + w1 * i/n, y1, dw, dh));
              px[hgrad > 0 ? n - i - 1 : i] =
                  average(r.read(0, rParam).getRGB(0, 0, dw, dh, buf, 0, dw));
            }
         }
         if (log != null && log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("detected Grayscale 255-pxval:");
            for (int i = 0; i < px.length; ++i) { 
               sb.append("\r\n\t");
               sb.append(px[i]);
            }
            log.debug(sb.toString());
         }
         return px;
      } finally {
         try { in.close(); } catch (IOException ignore) {}
      }
   }
   
   private float average(int[] rgb) {
      int v = 0;
      for (int i = 0; i < rgb.length; ++i) {
         v += (rgb[i] & 0xff) ^ 0xff;
      }
      return (float) v /  rgb.length;
   }
   
   private int[] findBorder(int[] rgb)
      throws CalibrationException
   {
      int[] b = { 0, rgb.length };
      while (b[0] < b[1] && (rgb[b[0]] & 0xff) > blackThreshold)
         ++b[0];
      while (b[0] < b[1] && (rgb[b[1]-1] & 0xff) > blackThreshold)
         --b[1];
      if ((b[1] - b[0]) * 100 < rgb.length * BORDER_MIN) {
         throw new CalibrationException("Failed to detect border");
      }
      return b;
   }

   private int gradient(int[] argb, int[] firstLast) {
      int g = 0;
      int m = (firstLast[1] + firstLast[0]) / 2;
      for (int i = firstLast[0]; i < m; ++i) {
         g -= argb[i] & 0xff;
      }
      for (int i = m; i < firstLast[1]; ++i) {
         g += argb[i] & 0xff;
      }
      return g;
   }
      
   private int findSteps(int[] a, int first, int last, boolean b2w)
      throws CalibrationException
   {
      float h = 0.f;
      float err = 0.f;
      int n = 0;
      if (b2w) {
         for (int i = first; i < last && err < FIND_STEP_ERR_MAX; ++n) { 
            if (n > 0) {
               h = (float) (i - first) / n;
            }
            int d = 0;         
            while (i < last && (a[i] & 0xff) < whiteThreshold)
            {
               ++i; ++d;
            }
            while (i < last &&  (a[i] & 0xff) > blackThreshold)
            {
               ++i; ++d;
            }
            if (n > 0) {
               err = Math.abs(h - d) / h;
            }
         }
      } else {
        for (int i = last - 1; i >= first && err < FIND_STEP_ERR_MAX; ++n) { 
            if (n > 0) {
               h = (float) (last - 1 - i) / n;
            }
            int d = 0;         
            while (i >= first && (a[i] & 0xff) < whiteThreshold)
            {
               --i; ++d;
            }
            while (i >= first &&  (a[i] & 0xff) > blackThreshold)
            {
               --i; ++d;
            }
            if (n > 0) {
               err = Math.abs(h - d) / h;
            }
         }
      }
      if (n < FIND_STEP_NUM_MIN) {
         throw new CalibrationException("Failed to detect more than "
            + (n-1) + " gray steps");
      }
      int steps = Math.round((last - first) / h);
      if (log != null && log.isDebugEnabled()) {
         log.debug("detected " + (n-1) + " from " + steps + " gray steps");
      }
      return steps;
   }

}
