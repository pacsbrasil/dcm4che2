/*
 *  Copyright (c) 2003 by TIANI MEDGRAPH AG                                  *
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
 */
package com.tiani.prnscp.print;

import java.awt.Rectangle;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Category;
import org.apache.log4j.Logger;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      December 31, 2002 <p>
 * @version    $Revision$
 */
public class ScannerCalibration
{

    // Constants -----------------------------------------------------
    private final static int EXTENSION_MAX = 80;
    private final static int THRESHOLD_MIN = 100;
    private final static int THRESHOLD_MAX = 250;
    private final static int FIND_STEP_NUM_MIN = 3;
    private final static float FIND_STEP_ERR_MAX = 0.2f;
    private final static int BORDER_MIN = 50;
    final static String DSI256_JPG = "DSI256.jpg";
    final static String DSI256_ODS = "DSI256.ods";

    // Attributes ----------------------------------------------------
    private Category log;

    /**  Holds value of property scanArea. */
    private int scanPointExtension = 50;

    /**  Holds value of property blackThreshold. */
    private int blackThreshold = 190;

    /**  Holds value of property whiteThreshold. */
    private int whiteThreshold = 220;

    private File scanDir;

    private File dsi256jpg;

    private File dsi256ods;

    private File lastScanFile;

    private long lastScanFileModified;

    private float[] cachedODs;

    private float[] refODs;

    private float[] invRefPx;

    // Constructors --------------------------------------------------
    /**
     *  Constructor for the ScannerCalibration object
     *
     * @param  log  Description of the Parameter
     */
    public ScannerCalibration(Category log)
    {
        if (log == null) {
            throw new NullPointerException();
        }
        this.log = log;
    }


    // Public --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @param  f                Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    public float[] readODs(File f)
        throws IOException
    {
        if (log.isDebugEnabled()) {
            log.debug("Read ODs from " + f);
        }
        BufferedReader r = new BufferedReader(new FileReader(f));
        int lineNo = 0;
        try {
            float[] ods = new float[256];
            String line;
            int i = 0;
            while ((line = r.readLine()) != null) {
                lineNo++;
                if (line.startsWith("#")) {
                    continue;
                }
                String s = line.trim();
                if (s.length() == 0) {
                    continue;
                }
                ods[i++] = Float.parseFloat(s);
            }
            if (i != 256) {
                throw new IOException("Only " + i + " OD values in " + f);
            }
            return ods;
        } catch (IllegalArgumentException e) {
            throw new IOException("Error in line " + lineNo + " in " + f
                     + ": " + e.getMessage());
        } finally {
            r.close();
        }
    }


    /**
     *  Gets the scanDir attribute of the ScannerCalibration object
     *
     * @return    The scanDir value
     */
    public File getScanDir()
    {
        return this.scanDir;
    }


    /**
     *  Sets the scanDir attribute of the ScannerCalibration object
     *
     * @param  scanDir          The new scanDir value
     * @exception  IOException  Description of the Exception
     */
    public void setScanDir(File scanDir)
        throws IOException
    {
        if (!scanDir.isDirectory()) {
            if (scanDir.mkdir()) {
                log.warn("Scan Directory " + scanDir + " did not exits. Created new one.");
            } else {
                throw new IOException("Failed to create new Scan Directory " + scanDir);
            }
        }
        if (scanDir.list().length == 0) {
            log.warn("No scans in directory " + scanDir
                     + " required for auto-calibration");
        }
        this.scanDir = scanDir;
        this.dsi256jpg = new File(scanDir.getParent(), DSI256_JPG);
        this.dsi256ods = new File(scanDir.getParent(), DSI256_ODS);
        if (!dsi256ods.isFile()) {
            log.warn("Could not find file " + dsi256ods
                     + " required for auto-calibration");
        }
        if (!dsi256jpg.isFile()) {
            log.warn("Could not find file " + dsi256jpg
                     + " required for auto-calibration");
        }
    }


    /**
     *  Getter for property scanArea.
     *
     * @return    Value of property scanArea.
     */
    public int getScanPointExtension()
    {
        return scanPointExtension;
    }


    /**
     *  Setter for property scanArea.
     *
     * @param  extension  The new scanPointExtension value
     */
    public void setScanPointExtension(int extension)
    {
        if (extension <= 0 && extension > EXTENSION_MAX) {
            throw new IllegalArgumentException("extension: " + extension);
        }
        this.scanPointExtension = extension;
    }


    /**
     *  Setter for property scanThreshold.
     *
     * @param  threshold  New value of property scanThreshold.
     */
    public void setScanThreshold(String threshold)
    {
        int black;
        int white;
        try {
            int delim = threshold.indexOf('/');
            black = Integer.parseInt(threshold.substring(0, delim));
            white = Integer.parseInt(threshold.substring(delim + 1));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("threshold: " + threshold);
        }
        if (black < THRESHOLD_MIN && white < THRESHOLD_MAX && black >= white) {
            throw new IllegalArgumentException("threshold: " + threshold);
        }
        this.blackThreshold = black;
        this.whiteThreshold = white;
    }


    /**
     *  Getter for property scanThreshold.
     *
     * @return    Value of property scanThreshold.
     */
    public String getScanThreshold()
    {
        return "" + blackThreshold + "/" + whiteThreshold;
    }


    /**
     *  Gets the lastScanFileModified attribute of the ScannerCalibration object
     *
     * @return    The lastScanFileModified value
     */
    public long getLastScanFileModified()
    {
        return lastScanFileModified;
    }


    /**
     *  Description of the Method
     *
     * @param  force                     Description of the Parameter
     * @return                           Description of the Return Value
     * @exception  CalibrationException  Description of the Exception
     */
    public float[] calculateGrayscaleODs(boolean force)
        throws CalibrationException
    {
        if (scanDir == null) {
            throw new IllegalStateException("Scan Dir not initalized!");
        }
        try {
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
                new Comparator()
                {
                    public int compare(Object o1, Object o2)
                    {
                        return (int) (((File) o2).lastModified()
                                 - ((File) o1).lastModified());
                    }
                });

            if (force || cachedODs == null
                     || !scanFiles[0].equals(lastScanFile)
                     || scanFiles[0].lastModified() > lastScanFileModified) {
                cachedODs = interpolate(analyse(scanFiles[0]));
                lastScanFile = scanFiles[0];
                lastScanFileModified = scanFiles[0].lastModified();
            } else {
                log.debug("use cached ODs");
            }
            return cachedODs;
        } catch (IOException e) {
            throw new CalibrationException("calculateGrayscaleODs failed: ", e);
        }
    }


    private float[] interpolate(float[] invPx)
        throws CalibrationException, IOException
    {
        if (invRefPx == null) {
            invRefPx = analyse(dsi256jpg);
            Arrays.sort(invRefPx);
        }
        if (refODs == null) {
            refODs = readODs(dsi256ods);
            Arrays.sort(refODs);
        }
        log.debug("interpolating ODs");
        float[] result = new float[invPx.length];
        for (int i = 0; i < invPx.length; ++i) {
            int index = Arrays.binarySearch(invRefPx, invPx[i]);
            if (index >= 0) {
                // exact match
                result[i] = refODs[index];
            } else {
                index = (-index) - 1;
                if (index == 0) {
                    // = extrapolation
                    index = 1;
                } else if (index == invRefPx.length) {
                    // = extrapolation
                    index = invRefPx.length - 1;
                }
                result[i] = refODs[index - 1]
                         + (refODs[index] - refODs[index - 1])
                         * (invPx[i] - invRefPx[index - 1])
                         / (invRefPx[index] - invRefPx[index - 1]);
            }
        }
        if (log.isDebugEnabled()) {
            StringBuffer sb = new StringBuffer("calculated GrayscaleODs:");
            for (int i = 0; i < invPx.length; ++i) {
                sb.append("\r\n\t");
                sb.append(result[i]);
            }
            log.debug(sb.toString());
        }

        return result;
    }


    /*
    private boolean isMonotonic(float[] a)
    {
        for (int i = 1; i < a.length; ++i) {
            if (a[i] < a[i - 1]) {
                return false;
            }
        }
        return true;
    }
*/
    private ImageReader findReader(File f)
        throws CalibrationException
    {
        String fname = f.getName();
        String fileSuffix = fname.substring(fname.lastIndexOf('.') + 1);
        Iterator it = ImageIO.getImageReadersBySuffix(fileSuffix);
        if (!it.hasNext()) {
            throw new CalibrationException("Unsupported Image Format " + f);
        }
        return (ImageReader) it.next();
    }


    private float[] analyse_old(File f)
        throws CalibrationException, IOException
    {
        if (log.isDebugEnabled()) {
            log.debug("analysing " + f);
        }
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
            float[] px;
            if (portrait) {
                int n = findSteps(vline, tb[0], tb[1], vgrad > 0);
                px = new float[n];
                // top to bottom orientation
                int h1 = tb[1] - tb[0];
                int dh = h1 * scanPointExtension / (100 * n) + 1;
                int y = tb[0] + (h1 / n - dh) / 2;

                for (int i = 0; i < n; ++i) {
                    px[vgrad > 0 ? n - i - 1 : i] =
                            average(vline, y + h1 * i / n, dh);
                }
            } else {
                // left to right orientation
                int n = findSteps(hline, lr[0], lr[1], hgrad > 0);
                px = new float[n];
                int w1 = lr[1] - lr[0];
                int dw = w1 * scanPointExtension / (100 * n) + 1;
                int x = lr[0] + (w1 / n - dw) / 2;

                for (int i = 0; i < n; ++i) {
                    px[hgrad > 0 ? n - i - 1 : i] =
                            average(hline, x + w1 * i / n, dw);
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
//            if (!isMonotonic(px)) {
//                Arrays.sort(px);
//                log.warn("Grayscale " + f.getName()
//                         + " not monotonic increasing! Calibrate with sorted steps!");
//            }
            return px;
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
    }

    //preconditions: start in [0..arr.length-1], end in [start..arr.length]
    private int[] subarray(int[] arr, int start, int end)
    {
        if (start > end || start < 0 || end < 0
                 || start >= arr.length || end > arr.length) {
            throw new IllegalArgumentException("illegal start=" + start + ", end=" + end);
        }
        int[] subarr = new int[end - start];
        int index = 0;
        for (int i = start; i < end; i++) {
            subarr[index++] = arr[i];
        }
        return subarr;
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
            int w_outer = lr[1] - lr[0];
            int h_outer = tb[1] - tb[0];
            w_outer = (w_outer + h_outer) / 2;//avg
            //final int PatternBorderToPatternDistRatio = 512/136;  //3:1
            final int PatternNumberBoxesX = 4;
            final int PatternNumberBoxesY = 4;
            final int PatternNumberInnerBoxesX = 4;
            final int PatternNumberInnerBoxesY = 4;
            final int AbsolutePatternSpacing = (w_outer * 16) / 512;
            final int AbsolutePatternBoxSize = (w_outer * 48) / 512;
            int pattern_left = lr[0] + (w_outer * 136) / 512;
            int pattern_top = tb[0] + (w_outer * 136) / 512;

            // debug
            if (log != null && log.isDebugEnabled()) {
                log.debug("tot width = " + w + ", tot height = " + h);
                log.debug("lr = " + lr[0] + "," + lr[1]);
                log.debug("tb = " + tb[0] + "," + tb[1]);
                log.debug("w outer = " + w_outer);
                log.debug("h outer = " + h_outer);
                log.debug("AbsolutePatternSpacing = " + AbsolutePatternSpacing);
                log.debug("AbsolutePatternBoxSize = " + AbsolutePatternBoxSize);
            }

            float[] px;
            float[] samples;
            int[] box;
            //if (portrait) {
            px = new float[PatternNumberBoxesX * PatternNumberBoxesY *
                    PatternNumberInnerBoxesX * PatternNumberInnerBoxesY];
            final int TotWidth = PatternNumberBoxesX * AbsolutePatternBoxSize + (PatternNumberBoxesX - 1) * AbsolutePatternSpacing;
            int i;
            int j;
            for (int k = 0; k < PatternNumberBoxesX * PatternNumberBoxesY; k++) {
                i = k / PatternNumberBoxesY;
                j = (i % 2 == 0) ? PatternNumberBoxesY - 1 - k % PatternNumberBoxesY :
                        k % PatternNumberBoxesY;
                rParam.setSourceRegion(new Rectangle(pattern_left + i * (AbsolutePatternBoxSize + AbsolutePatternSpacing),
                        pattern_top + j * (AbsolutePatternBoxSize + AbsolutePatternSpacing),
                        AbsolutePatternBoxSize, AbsolutePatternBoxSize));
                //log.debug("i=" + i + ", j=" + j);
                box = r.read(0, rParam).getRGB(0, 0, AbsolutePatternBoxSize, AbsolutePatternBoxSize,
                        null, 0, AbsolutePatternBoxSize);
                samples = sampleBoxPattern(box, AbsolutePatternBoxSize, AbsolutePatternBoxSize,
                        PatternNumberInnerBoxesX, PatternNumberInnerBoxesY);
                System.arraycopy(samples, 0, px, samples.length * k, samples.length);
            }
            /*} else {
            }*/
            if (log != null && log.isDebugEnabled()) {
                StringBuffer sb = new StringBuffer("detected Grayscale 255-pxval:");
                for (int ii = 0; ii < px.length; ++ii) {
                    sb.append("\r\n\t");
                    sb.append(px[ii]);
                }
                log.debug(sb.toString());
            }
            return px;
        } finally {
            try {
                in.close();
            } catch (IOException ignore) {}
        }
    }


    private float[] sampleBoxPattern(int[] pixels, int width, int height, int nX, int nY)
    {
        final int xstep = width / nX;
        final int ystep = height / nY;
        final float[] samples = new float[nX * nY];
        int left = xstep / 2;
        int top = ystep / 2;
        int x;
        int y;
        int i;
        int j;

        for (int k = 0; k < samples.length; k++) {
            i = k / nY;
            j = (i % 2 == 0) ? nY - 1 - k % nY :
                    k % nY;
            x = left + i * xstep;
            y = top + j * ystep;
            samples[k] = average(pixels, width, x - 2, y - 2, 5);//(pixels[x+y*width] & 0xff) ^ 0xff
        }
        return samples;
    }


    private float average(int[] rgb, int width, int x, int y, int size)
    {
        int v = 0;
        for (int i = x; i < x + size; ++i) {
            for (int j = y; j < y + size; ++j) {
                v += (rgb[i + j * width] & 0xff) ^ 0xff;
            }
        }
        return (float) v / (size * size);
    }


    private float average(int[] rgb, int off, int len)
    {
        int v = 0;
        for (int i = off, n = off + len; i < n; ++i) {
            v += (rgb[i] & 0xff) ^ 0xff;
        }
        return (float) v / len;
    }


    private int[] findBorder(int[] rgb)
        throws CalibrationException
    {
        int[] b = {0, rgb.length};
        while (b[0] < b[1] && (rgb[b[0]] & 0xff) > blackThreshold) {
            ++b[0];
        }
        while (b[0] < b[1] && (rgb[b[1] - 1] & 0xff) > blackThreshold) {
            --b[1];
        }
        if ((b[1] - b[0]) * 100 < rgb.length * BORDER_MIN) {
            throw new CalibrationException("Failed to detect border");
        }
        return b;
    }


    private int[] findInnerBorder(int[] rgb)
        throws CalibrationException
    {
        int[] b = {1, rgb.length - 1};
        while (b[0] < b[1] && (rgb[b[0]] & 0xff) < whiteThreshold) {
            ++b[0];
        }
        while (b[0] < b[1] && (rgb[b[1] - 1] & 0xff) < whiteThreshold) {
            --b[1];
        }
        if ((b[1] - b[0]) * 100 < rgb.length * BORDER_MIN) {
            throw new CalibrationException("Failed to detect inner border");
        }
        return b;
    }


    private int gradient(int[] argb, int[] firstLast)
    {
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
                while (i < last && (a[i] & 0xff) < whiteThreshold) {
                    ++i;
                    ++d;
                }
                while (i < last && (a[i] & 0xff) > blackThreshold) {
                    ++i;
                    ++d;
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
                while (i >= first && (a[i] & 0xff) < whiteThreshold) {
                    --i;
                    ++d;
                }
                while (i >= first && (a[i] & 0xff) > blackThreshold) {
                    --i;
                    ++d;
                }
                if (n > 0) {
                    err = Math.abs(h - d) / h;
                }
            }
        }
        if (n < FIND_STEP_NUM_MIN) {
            throw new CalibrationException("Failed to detect more than "
                     + (n - 1) + " gray steps");
        }
        int steps = Math.round((last - first) / h);
        if (log != null && log.isDebugEnabled()) {
            log.debug("detected " + (n - 1) + " from " + steps + " gray steps");
        }
        return steps;
    }

    // Main --------------------------------------------------------
    /**
     *  Description of the Method
     *
     * @param  args           Description of the Parameter
     * @exception  Exception  Description of the Exception
     */
    public static void main(String args[])
        throws Exception
    {
        BasicConfigurator.configure();
        LongOpt[] longopts = {
                new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't'),
                new LongOpt("ext", LongOpt.REQUIRED_ARGUMENT, null, 'e'),
                new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h')
                };

        Getopt g = new Getopt("probescan", args, "t:e:h", longopts, true);
        try {
            ScannerCalibration sc =
                    new ScannerCalibration(Logger.getLogger("probescan"));
            int c;
            while ((c = g.getopt()) != -1) {
                switch (c) {
                    case 't':
                        sc.setScanThreshold(g.getOptarg());
                        break;
                    case 'e':
                        sc.setScanPointExtension(Integer.parseInt(g.getOptarg()));
                        break;
                    case 'h':
                    case '?':
                        exit("");
                        break;
                }
            }
            int optind = g.getOptind();
            int argc = args.length - optind;
            if (argc != 1) {
                exit("probescan: wrong number of arguments\n");
            }
            sc.analyse(new File(args[optind]));
        } catch (IllegalArgumentException e) {
            exit("probescan: illegal argument - " + e.getMessage() + "\n");
        } catch (IOException e) {
            System.err.println("probescan: i/o error - " + e.getMessage() + "\n");
            System.exit(1);
        }
    }


    private final static String USAGE =
            "Usage: java -jar probescan.jar [OPTIONS] FILE\n\n" +
            "Analyses grayscale scan in specified FILE.\n" +
            "Options:\n" +
            " -t --threshold   Specifies black/white threshold in device driving levels\n" +
            "                  for step detection [default: 180/220].\n" +
            " -e --extension   Specifies extension of measurement area in % of\n" +
            "                  step height [default: 50]\n" +
            " -h --help        show this help and exit\n";


    private static void exit(String prompt)
    {
        System.err.println(prompt);
        System.err.println(USAGE);
        System.exit(1);
    }

}

