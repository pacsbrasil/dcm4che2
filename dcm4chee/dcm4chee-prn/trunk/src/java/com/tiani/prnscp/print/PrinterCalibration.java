/*                                                                           *
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
 */
package com.tiani.prnscp.print;

import java.util.Arrays;
import javax.print.attribute.standard.Chromaticity;
import org.apache.log4j.Category;
import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;
import org.apache.log4j.Category;

/**
 *  <description>
 *
 * @author     <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @since      November 19, 2003
 * @version    $Revision$
 */
public class PrinterCalibration
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    private Category log;
    private int skipNonMonotonicODs = 10;
    private int monochromeMinDensity;
    private int monochromeMaxDensity;
    private int colorMinDensity;
    private int colorMaxDensity;
    private final float[] monochromeODs = new float[256];
    private final float[] colorODs = new float[256];


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    public PrinterCalibration(Category log)
    {
        if (log == null) {
            throw new NullPointerException();
        }
        this.log = log;
    }

    // Public --------------------------------------------------------
    /**
     *  Gets the skipNonMonotonicODs attribute of the PrinterCalibration object
     *
     * @return    The skipNonMonotonicODs value
     */
    public int getSkipNonMonotonicODs()
    {
        return skipNonMonotonicODs;
    }


    /**
     *  Sets the skipNonMonotonicODs attribute of the PrinterCalibration object
     *
     * @param  skipNonMonotonicODs  The new skipNonMonotonicODs value
     */
    public void setSkipNonMonotonicODs(int skipNonMonotonicODs)
    {
        this.skipNonMonotonicODs = skipNonMonotonicODs;
    }

    /**
     *  Description of the Method
     *
     * @param  chromaticity  Description of the Parameter
     * @param  od            Description of the Parameter
     * @return               Description of the Return Value
     */
    public int toDDL(Chromaticity chromaticity, float od)
    {
        float[] ddl2od = odsFor(chromaticity);
        int i = Arrays.binarySearch(ddl2od, od);
        if (i >= 0) {
            return 255 - i;
        }
        i = -i - 1;
        if (i == 0) {
            return 255;
        }
        if (i > 255) {
            return 0;
        }
        float diff1 = ddl2od[i] - od;
        float diff2 = od - ddl2od[i - 1];
        return 255 - (diff1 < diff2 ? i : i - 1);
    }


    private byte[] toDDL(Chromaticity chromaticity, float[] od)
    {
        byte[] ddl = new byte[od.length];
        for (int pv = 0; pv < ddl.length; ++pv) {
            ddl[pv] = (byte) toDDL(chromaticity, od[pv]);
        }
        return ddl;
    }


    /**
     *  Returns Identity function
     *
     * @return    Identity function
     */
    public static byte[] getIdentityPValToDDL()
    {
        byte[] lut = new byte[256];
        for (int i = 0; i < 256; ++i) {
            lut[i] = (byte) i;
        }
        return lut;
    }


    /**
     *  Gets the pValToDDLwLinOD attribute of the PrinterCalibration object
     *
     * @param  chromaticity  Description of the Parameter
     * @param  n             Description of the Parameter
     * @param  dmin          Description of the Parameter
     * @param  dmax          Description of the Parameter
     * @return               The pValToDDLwLinOD value
     */
    public byte[] getPValToDDLwLinOD(Chromaticity chromaticity,
            int n, float dmin, float dmax)
    {
        return toDDL(chromaticity, getPValToLinOD(n, dmin, dmax));
    }


    /**
     *  Returns Linear Optical Densities for given density range.
     *
     * @param  n     p-value bit size
     * @param  dmin  minimal optical density
     * @param  dmax  maximum optical density
     * @return       Optical Densities for P-Values
     */
    public static float[] getPValToLinOD(int n, float dmin, float dmax)
    {
        if (n < 8 || n > 16) {
            throw new IllegalArgumentException("n: " + n);
        }
        if (dmin > dmax) {
            throw new IllegalArgumentException("dmin: " + dmin + ", dmax: " + dmax);
        }
        float[] od = new float[1 << n];
        final int pvmax = od.length - 1;
        for (int pv = 0; pv < od.length; ++pv) {
            od[pv] = dmin + (dmax - dmin) * pv / pvmax;
        }
        return od;
    }



    /**
     *  Returns Device Driving Levels for P-Values according DICOM GSDF
     *  for given density range and illumination conditions.
     *
     * @param  n             p-value bit size
     * @param  dmin          minimal optical density
     * @param  dmax          maximum optical density
     * @param  l0            illumination in cd/m*m
     * @param  la            reflected ambient light in cd/m*m
     * @param  chromaticity  Description of the Parameter
     * @return               Device Driving Levels for P-Values
     */
    public byte[] getPValToDDLwGSDF(Chromaticity chromaticity,
            int n, float dmin, float dmax, float l0, float la)
    {
        return toDDL(chromaticity, getPValToGsdfOD(n, dmin, dmax, l0, la));
    }


    /**
     *  Returns Optical Densities for P-Values according DICOM GSDF
     *  for given density range and illumination conditions.
     *
     * @param  n     p-value bit size
     * @param  dmin  minimal optical density
     * @param  dmax  maximum optical density
     * @param  l0    illumination in cd/m*m
     * @param  la    reflected ambient light in cd/m*m
     * @return       Optical Densities for P-Values
     */
    public static float[] getPValToGsdfOD(int n, float dmin, float dmax,
            float l0, float la)
    {
        if (n < 8 || n > 16) {
            throw new IllegalArgumentException("n: " + n);
        }
        if (dmin > dmax) {
            throw new IllegalArgumentException(
                    "dmin: " + dmin + ", dmax: " + dmax);
        }
        if (l0 < 50 || l0 > 3000) {
            throw new IllegalArgumentException("l0: " + l0);
        }
        if (la < 0 || la > 50) {
            throw new IllegalArgumentException("la: " + la);
        }
        double jmin = inverseGSDF(lum(dmax, l0, la));
        double jmax = inverseGSDF(lum(dmin, l0, la));
        float[] od = new float[1 << n];
        final int pvmax = od.length - 1;
        for (int pv = 0; pv < od.length; ++pv) {
            od[pv] = (float) density(pv, pvmax, jmin, jmax, l0, la);
        }
        return od;
    }


    /**
     *  Returns Optical Densities for P-Values evaluating the given
     *  Presentation LUT, the density range and illumination conditions.
     *
     * @param  n             p-value bit size
     * @param  dmin          minimal optical density
     * @param  dmax          maximum optical density
     * @param  l0            illumination in cd/m*m
     * @param  la            reflected ambient light in cd/m*m
     * @param  plut          Presentation LUT
     * @param  chromaticity  Description of the Parameter
     * @return               Device Driving Levels for P-Values
     */
    public byte[] getPValToDDL(Chromaticity chromaticity,
            int n, float dmin, float dmax, float l0, float la, Dataset plut)
    {
        if (plut == null) {
            return getIdentityPValToDDL();
        }
        String shape = plut.getString(Tags.PresentationLUTShape);
        if (shape != null) {
            if ("IDENTITY".equals(shape)) {
                return getPValToDDLwGSDF(chromaticity, n, dmin, dmax, l0, la);
            }
            if ("LIN OD".equals(shape)) {
                return getPValToDDLwLinOD(chromaticity, n, dmin, dmax);
            }
            throw new IllegalArgumentException("LUTShape: " + shape);
        }
        Dataset item = plut.getItem(Tags.PresentationLUTSeq);
        if (item == null) {
            throw new IllegalArgumentException("Neither LUT Shape nor LUT Seq");
        }
        int[] desc = item.getInts(Tags.LUTDescriptor);
        if (desc.length != 3) {
            throw new IllegalArgumentException("LUT Desc: VM=" + desc.length);
        }

        if ((desc[0] != 256 && desc[0] != 4096) || desc[1] != 0
                 || desc[2] < 10 && desc[2] > 16) {
            throw new IllegalArgumentException(
                    "LUT Desc: " + desc[0] + "\\" + desc[1] + "\\" + desc[2]);
        }
        int[] src = item.getInts(Tags.LUTData);
        if (src == null) {
            throw new IllegalArgumentException("Missing LUT Data");
        }
        if (src.length != desc[0]) {
            throw new IllegalArgumentException("LUT Data Lenth: " + src.length
                     + " does not match 1.value of LUT Descriptor: " + desc[0]);
        }
        byte[] lut = getPValToDDLwGSDF(chromaticity, desc[2], dmin, dmax, l0, la);
        byte[] dst = new byte[src.length];
        for (int i = 0; i < src.length; ++i) {
            dst[i] = lut[src[i]];
        }
        return dst;
    }

    private float[] odsFor(Chromaticity chromaticity)
    {
        return Chromaticity.COLOR.equals(chromaticity)
                 ? colorODs
                 : monochromeODs;
    }

    /**
     *  Sets the oDs attribute of the PrinterCalibration object
     *
     * @param  chromaticity  The new oDs value
     * @param  src           The new oDs value
     */
    public void setODs(Chromaticity chromaticity, float[] src)
    {
        if (src.length != 256) {
            throw new IllegalArgumentException("src.length:" + src.length);
        }

        float[] ddl2od = odsFor(chromaticity);
        System.arraycopy(src, 0, ddl2od, 0, 256);
        
        // ensure monoton increasing
        float max = ddl2od[255];
        float minMax = max * (100 - skipNonMonotonicODs) / 100;
        int minUsed = 0;
        int maxUsed = 255;
        for (int i = 254; i > 0; --i) {
            final float val = ddl2od[i];
            if (val < ddl2od[i + 1]) {
                continue;
            }
            if (val > max) {
                max = val;
                minMax = max * (100 - skipNonMonotonicODs) / 100;
            }
            if (val > minMax) {
                maxUsed = i;
            }
        }
        Arrays.sort(ddl2od, 0, maxUsed);
        Arrays.fill(ddl2od, maxUsed+1, 256, Float.POSITIVE_INFINITY);
        while (ddl2od[minUsed] == ddl2od[minUsed+1]) {
            ddl2od[minUsed++] = Float.NEGATIVE_INFINITY;
        }        
        if (Chromaticity.COLOR.equals(chromaticity)) {
            colorMinDensity = (int) (ddl2od[minUsed] * 100);
            colorMaxDensity = (int) (ddl2od[maxUsed] * 100);
        } else {
            monochromeMinDensity = (int) (ddl2od[minUsed] * 100);
            monochromeMaxDensity = (int) (ddl2od[maxUsed] * 100);
        }
        if (log.isDebugEnabled()) {
            StringBuffer prompt = new StringBuffer(2000);
            prompt.append("Used Printer ODs, chromaticity=")
                .append(chromaticity).append(':');
            for (int i = 0; i < ddl2od.length; ++i) {
                prompt.append("\n\t").append(ddl2od[i]);
            }
            log.debug(prompt.toString());
        }
        log.info("Printer OD range [" + chromaticity
            + "]: total=" + ddl2od[minUsed] + "-" + max
            + ", used=" + ddl2od[minUsed] + "-" + ddl2od[maxUsed]
            + ", DDL=" + (255-minUsed) + "-" + (255-maxUsed));
    }


    /**
     *  Gets the minDensity attribute of the PrinterCalibration object
     *
     * @param  chromaticity  Description of the Parameter
     * @return               The minDensity value
     */
    public int getMinDensity(Chromaticity chromaticity)
    {
        return Chromaticity.COLOR.equals(chromaticity)
                 ? colorMinDensity
                 : monochromeMinDensity;
    }


    /**
     *  Gets the maxDensity attribute of the PrinterCalibration object
     *
     * @param  chromaticity  Description of the Parameter
     * @return               The maxDensity value
     */
    public int getMaxDensity(Chromaticity chromaticity)
    {
        return Chromaticity.COLOR.equals(chromaticity)
                 ? colorMaxDensity
                 : monochromeMaxDensity;
    }


    // Private -------------------------------------------------------

    private static double lum(double density, float l0, float la)
    {
        return la + l0 * Math.pow(10, -density);
    }


    private static double density(int pv, int pvmax, double jmin, double jmax,
            float l0, float la)
    {
        double j = jmin + (jmax - jmin) * pv / pvmax;
        return -Math.log((gsdf(j) - la) / l0) / LOG10;
    }


    private final static double A0 = -1.3011877;
    private final static double A1 = 8.0242636E-2;
    private final static double A2 = 1.3646699E-1;
    private final static double A3 = -2.5468404E-2;
    private final static double A4 = 1.3635334E-3;
    private final static double B1 = -2.5840191E-2;
    private final static double B2 = -1.0320229E-1;
    private final static double B3 = 2.8745620E-2;
    private final static double B4 = -3.1978977E-3;
    private final static double B5 = 1.2992634E-4;


    private static double gsdf(double j)
    {
        double lnj = Math.log(j);
        double lnj2 = lnj * lnj;
        double lnj3 = lnj2 * lnj;
        double lnj4 = lnj2 * lnj2;
        double lnj5 = lnj2 * lnj3;
        return Math.pow(10, (A0 + A1 * lnj + A2 * lnj2 + A3 * lnj3 + A4 * lnj4)
                 / (1 + B1 * lnj + B2 * lnj2 + B3 * lnj3 + B4 * lnj4 + B5 * lnj5));
    }


    private final static double C0 = 71.498068;
    private final static double C1 = 94.593053;
    private final static double C2 = 41.912053;
    private final static double C3 = 9.8247004;
    private final static double C4 = 0.28175407;
    private final static double C5 = -1.1878455;
    private final static double C6 = -0.18014349;
    private final static double C7 = 0.14710899;
    private final static double C8 = -0.017046845;
    private final static double LOG10 = Math.log(10);


    private static double inverseGSDF(double l)
    {
        double log10L = Math.log(l) / LOG10;
        double log10L2 = log10L * log10L;
        double log10L3 = log10L2 * log10L;
        double log10L4 = log10L2 * log10L2;
        double log10L5 = log10L2 * log10L3;
        double log10L6 = log10L3 * log10L3;
        double log10L7 = log10L4 * log10L3;
        double log10L8 = log10L4 * log10L4;
        return C0 + C1 * log10L + C2 * log10L2 + C3 * log10L3 + C4 * log10L4
                 + C5 * log10L5 + C6 * log10L6 + C7 * log10L7 + C8 * log10L8;
    }
}

