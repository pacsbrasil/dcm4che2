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
 *  WITHOUT Any WARRANTY; without even the implied warranty of               *
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU        *
 *  Lesser General Public License for more details.                          *
 *                                                                           *
 *  You should have received a copy of the GNU Lesser General Public         *
 *  License along with this library; if not, write to the Free Software      *
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA  *
 */
package com.tiani.prnscp.print;
import java.awt.Point;

import java.awt.Rectangle;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
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
 * @author     <a href="mailto:joseph@tiani.com">joseph foracci</a>
 * @since      December 31, 2002 <p>
 * @version    $Revision$
 */
public class ScannerCalibration
{

    // Constants -----------------------------------------------------
    private final static String ODS_EXT = ".ods";
    private final static String DSI256_JPG = "DSI256.jpg";
    private final static String DSI256_ODS = "DSI256.ods";
    private final static int EXTENSION_MAX = 80;
    private final static int THRESHOLD_MIN = 100;
    private final static int THRESHOLD_MAX = 250;
    private final static int BORDER_MIN = 50;
    private final static int NUM_BOXES_X = 4;
    private final static int NUM_BOXES_Y = 4;
    private final static int NUM_INNER_BOXES_X = 4;
    private final static int NUM_INNER_BOXES_Y = 4;

    // Attributes ----------------------------------------------------
    private Category log;

    private int scanPointExtension = 50;

    private int scanThreshold = 200;

    private File scanDir;

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
            log.debug("Reading ODs from " + f);
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
     *  Description of the Method
     *
     * @param  f                Description of the Parameter
     * @param  ods              Description of the Parameter
     * @exception  IOException  Description of the Exception
     */
    public void writeODs(File f, float[] ods)
        throws IOException
    {
        if (ods.length != 256) {
            throw new IllegalArgumentException("ods.length:" + ods.length);
        }
        if (log.isDebugEnabled()) {
            log.debug("Writing ODs to " + f);
        }
        PrintWriter out = new PrintWriter(new FileOutputStream(f));
        try {
            for (int i = 0; i < ods.length; ++i) {
                out.println(ods[i]);
            }
        } finally {
            out.close();
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
     */
    public void setScanDir(File scanDir)
    {
        this.scanDir = scanDir;
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
    public void setScanThreshold(int threshold)
    {
        if (threshold < THRESHOLD_MIN && threshold < THRESHOLD_MAX) {
            throw new IllegalArgumentException("threshold: " + threshold);
        }
        this.scanThreshold = threshold;
    }


    /**
     *  Getter for property scanThreshold.
     *
     * @return    Value of property scanThreshold.
     */
    public int getScanThreshold()
    {
        return scanThreshold;
    }


    /**
     *  Gets the mostRecentScanFile attribute of the ScannerCalibration object
     *
     * @return                  The mostRecentScanFile value
     * @exception  IOException  Description of the Exception
     */
    public File getMostRecentScanFile()
        throws IOException
    {
        if (scanDir == null) {
            throw new IllegalStateException("Scan Dir not initalized!");
        }
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
        return scanFiles[0];
    }


    /**
     *  Description of the Method
     *
     * @return                           Description of the Return Value
     * @exception  CalibrationException  Description of the Exception
     */
    public float[] calculateGrayscaleODs()
        throws CalibrationException, IOException
    {
        return interpolate(analyse(getMostRecentScanFile()));
    }


    private void checkScanDir()
    {
        if (scanDir == null) {
            throw new IllegalStateException("scanDir not intialized");
        }
    }


    /**
     *  Gets the refODsFile attribute of the ScannerCalibration object
     *
     * @return    The refODsFile value
     */
    public File getRefODsFile()
    {
        checkScanDir();
        return new File(scanDir.getParent(), DSI256_ODS);
    }


    /**
     *  Gets the backupODsFile attribute of the ScannerCalibration object
     *
     * @return    The backupODsFile value
     */
    public File getBackupODsFile()
    {
        checkScanDir();
        return new File(scanDir.getParent(), scanDir.getName() + ODS_EXT);
    }


    /**
     *  Gets the refDSI256File attribute of the ScannerCalibration object
     *
     * @return    The refDSI256File value
     */
    public File getRefDSI256File()
    {
        checkScanDir();
        return new File(scanDir, DSI256_JPG);
    }


    private float[] interpolate(float[] invPx)
        throws CalibrationException, IOException
    {
        if (invRefPx == null) {
            invRefPx = analyse(getRefDSI256File());
            Arrays.sort(invRefPx);
        }
        if (refODs == null) {
            refODs = readODs(getRefODsFile());
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
        // TODO eliminate some of the 27(!) temporary variables used in this method [GZ]
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

            // TODO fails if Pixel or Pattern Ratio not 1:1 [GZ]
            w_outer = (w_outer + h_outer) / 2;//avg since w_outer ~= h_outer
            //calc (predicted) actual dimensions in pixels
            final int absPatternSpacing = (w_outer * 16) / 512;
            final int absPatternBoxSize = (w_outer * 48) / 512;
            final int pattern_left = lr[0] + (w_outer * 136) / 512;
            final int pattern_top = tb[0] + (w_outer * 136) / 512;

            float[] px = null;
            float[] samples;
            int[] box;

            // TODO fails if Pattern is printed in LANDSCAPE format [GZ]
            final boolean portrait = (w <= h);
            final boolean upSideDown;
            float first;
            float second;
            Point pt;
            final int stepSize = absPatternBoxSize + absPatternSpacing;

            if (portrait) {
                //sample inside left/right border to see if the image is rotated 180
                first = average(hline, w, lr[0] + (w_outer * 80) / (2 * 512), 0, 5, 1);
                second = average(hline, w, lr[1] - (w_outer * 80) / (2 * 512), 0, 5, 1);
            } else {
                //sample inside top/bottom border to see if the image is rotated 180
                first = average(vline, 1, 0, tb[0] + (h_outer * 80) / (2 * 512), 1, 5);
                second = average(vline, 1, 0, tb[1] - (h_outer * 80) / (2 * 512), 1, 5);
            }
            upSideDown = (first < second) ? false : true;
            // debug
            if (log.isDebugEnabled()) {
                log.debug("tot width = " + w + ", tot height = " + h);
                log.debug("lr = " + lr[0] + "," + lr[1]);
                log.debug("tb = " + tb[0] + "," + tb[1]);
                log.debug("w outer = " + w_outer);
                log.debug("h outer = " + h_outer);
                log.debug("absPatternSpacing = " + absPatternSpacing);
                log.debug("absPatternBoxSize = " + absPatternBoxSize);
                log.debug("first = " + first + ", second = " + second);
            }
            //
            px = new float[NUM_BOXES_X * NUM_BOXES_Y *
                    NUM_INNER_BOXES_X * NUM_INNER_BOXES_Y];
            //start winding through pattern
            for (int k = 0, n = NUM_BOXES_X * NUM_BOXES_Y; k < n; k++) {
                pt = calcBox(k, NUM_BOXES_X, NUM_BOXES_Y, portrait, upSideDown);
                rParam.setSourceRegion(new Rectangle(
                        pattern_left + pt.x * stepSize,
                        pattern_top + pt.y * stepSize,
                        absPatternBoxSize, absPatternBoxSize));
                //log.debug("i=" + i + ", j=" + j);
                box = r.read(0, rParam).getRGB(0, 0, absPatternBoxSize, absPatternBoxSize,
                        null, 0, absPatternBoxSize);
                samples = sampleBoxPattern(box, absPatternBoxSize, absPatternBoxSize,
                        NUM_INNER_BOXES_X, NUM_INNER_BOXES_Y,
                        portrait, upSideDown);
                System.arraycopy(samples, 0, px, samples.length * k, samples.length);
            }

            //pix
            if (log.isDebugEnabled()) {
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


    private Point calcBox(int n, final int nx, final int ny, boolean portrait, boolean upSideDown)
    {
        int i;
        int j;
        if (portrait) {
            i = n / ny;
            j = (i % 2 == 0) ? ny - 1 - n % ny : n % ny;
        } else {
            j = n / nx;
            i = (j % 2 == 1) ? nx - 1 - n % nx : n % nx;
        }
        if (upSideDown) {
            i = nx - 1 - i;
            j = ny - 1 - j;
        }
        return new Point(i, j);
    }


    // TODO should consider scanExtension attribute [GZ]
    private float[] sampleBoxPattern(int[] pixels, final int width, final int height,
            final int nx, final int ny,
            final boolean portrait, final boolean upSideDown)
    {
        final int xstep = width / nx;
        final int ystep = height / ny;
        final float[] samples = new float[nx * ny];
        int left = xstep / 2;
        int top = ystep / 2;
        int x;
        int y;
        Point pt;

        for (int k = 0, N = samples.length; k < N; k++) {
            pt = calcBox(k, nx, ny, portrait, upSideDown);
            x = left + pt.x * xstep;
            y = top + pt.y * ystep;
            samples[k] = average(pixels, width,
                    x - width / (nx * 4), y - height / (ny * 4),
                    width / (nx * 2), height / (ny * 2));//(pixels[x+y*width] & 0xff) ^ 0xff;
        }
        return samples;
    }

    //(x,y) is upper-left corner of region to average
    private float average(int[] rgb, int width, int x, int y, int sizex, int sizey)
    {
        int v = 0;
        for (int i = x; i < x + sizex; ++i) {
            for (int j = y; j < y + sizey; ++j) {
                v += (rgb[i + j * width] & 0xff) ^ 0xff;
            }
        }
        return (float) v / (sizex * sizey);
    }


    private int[] findBorder(int[] rgb)
        throws CalibrationException
    {
        int[] b = {0, rgb.length};
        while (b[0] < b[1] && (rgb[b[0]] & 0xff) > scanThreshold) {
            ++b[0];
        }
        while (b[0] < b[1] && (rgb[b[1] - 1] & 0xff) > scanThreshold) {
            --b[1];
        }
        if ((b[1] - b[0]) * 100 < rgb.length * BORDER_MIN) {
            throw new CalibrationException("Failed to detect border");
        }
        return b;
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
                        sc.setScanThreshold(Integer.parseInt(g.getOptarg()));
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
            " -t --threshold   Specifies threshold in device driving levels\n" +
            "                  for border detection [default: 200].\n" +
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

