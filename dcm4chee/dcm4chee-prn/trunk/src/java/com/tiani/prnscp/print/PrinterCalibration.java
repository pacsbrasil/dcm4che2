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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import org.jboss.logging.Logger;

/**
 *  <description>
 *
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @created  November 19, 2002
 * @version  $Revision$
 */
class PrinterCalibration
{
    // Constants -----------------------------------------------------

    // Attributes ----------------------------------------------------
    /**  Holds value of property dateOfLastCalibration. */
    private String dateOfLastCalibration = "19700101";

    /**  Holds value of property timeOfLastCalibration. */
    private String timeOfLastCalibration = "000000";

    /**  Holds value of property grayscaleODs. */
    private float[] stepODs;

    private float[] ddl2od = new float[256];
    
    private final Logger log;


    // Static --------------------------------------------------------

    // Constructors --------------------------------------------------
    PrinterCalibration(Logger log) {
        this.log = log;
    }

    // Public --------------------------------------------------------

    /**
     *  Description of the Method
     *
     * @param  density Description of the Parameter
     * @return  Description of the Return Value
     */
    public int toDDL(float density)
    {
        int i = Arrays.binarySearch(ddl2od, density);
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
        float diff1 = ddl2od[i] - density;
        float diff2 = density - ddl2od[i - 1];
        return 255 - (diff1 < diff2 ? i : i - 1);
    }


    /**
     *  Gets the identityPValToDDL attribute of the PrinterCalibration object
     *
     * @return  The identityPValToDDL value
     */
    public byte[] getIdentityPValToDDL()
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
     * @param  n Description of the Parameter
     * @param  dmin Description of the Parameter
     * @param  dmax Description of the Parameter
     * @return  The pValToDDLwLinOD value
     */
    public byte[] getPValToDDLwLinOD(int n, float dmin, float dmax)
    {
        check(n, dmin, dmax);
        byte[] lut = new byte[1 << n];
        for (int p = 0; p < lut.length; ++p) {
            lut[p] = (byte) toDDL(dmax - (dmax - dmin) * p / ((1 << n) - 1));
        }
        if (log.isDebugEnabled()) {
            logLut("PValToDDLwLinOD[dmin=" + dmin + ", dmax=" + dmax + "]:", lut);
        }
        return lut;
    }
    

    /**
     *  Gets the pValToDDLwGSDF attribute of the PrinterCalibration object
     *
     * @param  n Description of the Parameter
     * @param  dmin Description of the Parameter
     * @param  dmax Description of the Parameter
     * @param  l0 Description of the Parameter
     * @param  la Description of the Parameter
     * @return  The pValToDDLwGSDF value
     */
    public byte[] getPValToDDLwGSDF(int n, float dmin, float dmax,
            float l0, float la)
    {
        check(n, dmin, dmax);
        if (l0 < 50 || l0 > 3000) {
            throw new IllegalArgumentException("l0: " + l0);
        }
        if (la < 0 || la > 50) {
            throw new IllegalArgumentException("la: " + la);
        }
        double jmin = inverseGSDF(lum(dmax, l0, la));
        double jmax = inverseGSDF(lum(dmin, l0, la));
        byte[] lut = new byte[1 << n];
        for (int pv = 0; pv < lut.length; ++pv) {
            lut[pv] = (byte) toDDL((float) density(pv, n, jmin, jmax, l0, la));
        }
        if (log.isDebugEnabled()) {
            logLut("PValToDDLwGSDF[dmin=" + dmin + ", dmax=" + dmax
                + ", L0=" + l0 + ", La=" + la + "]:", lut);
        }
        return lut;
    }

    private void logLut(String prompt, byte[] lut) {
        StringBuffer sb = new StringBuffer(prompt);
        for (int i = lut.length; --i >= 0;) {
            int ddl = lut[i] & 0xff;
            sb.append("\n\tpv=").append(i).append("\tddl=")
              .append(ddl).append("\tod=").append(ddl2od[255-ddl]);
        }
        log.debug(sb.toString());
    }

    /**
     *  Gets the pValToDDL attribute of the PrinterCalibration object
     *
     * @param  n Description of the Parameter
     * @param  dmin Description of the Parameter
     * @param  dmax Description of the Parameter
     * @param  l0 Description of the Parameter
     * @param  la Description of the Parameter
     * @param  plut Description of the Parameter
     * @return  The pValToDDL value
     */
    public byte[] getPValToDDL(int n, float dmin, float dmax,
            float l0, float la, Dataset plut)
    {
        String shape = plut.getString(Tags.PresentationLUTShape);
        if (shape != null) {
            if ("IDENTITY".equals(shape)) {
                return getPValToDDLwGSDF(n, dmin, dmax, l0, la);
            }
            if ("LIN OD".equals(shape)) {
                return getPValToDDLwLinOD(n, dmin, dmax);
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
        byte[] lut = getPValToDDLwGSDF(desc[2], dmin, dmax, l0, la);
        byte[] dst = new byte[src.length];
        for (int i = 0; i < src.length; ++i) {
            dst[i] = lut[src[i]];
        }
        return dst;
    }


    /**
     *  Getter for property dateOfLastCalibration.
     *
     * @return  Value of property dateOfLastCalibration.
     */
    public String getDateOfLastCalibration()
    {
        return this.dateOfLastCalibration;
    }


    /**
     *  Setter for property dateOfLastCalibration.
     *
     * @param  dateOfLastCalibration New value of property
     *      dateOfLastCalibration.
     */
    public void setDateOfLastCalibration(String dateOfLastCalibration)
    {
        this.dateOfLastCalibration = dateOfLastCalibration;
    }


    /**
     *  Getter for property timeOfLastCalibration.
     *
     * @return  Value of property timeOfLastCalibration.
     */
    public String getTimeOfLastCalibration()
    {
        return this.timeOfLastCalibration;
    }


    /**
     *  Setter for property timeOfLastCalibration.
     *
     * @param  timeOfLastCalibration New value of property
     *      timeOfLastCalibration.
     */
    public void setTimeOfLastCalibration(String timeOfLastCalibration)
    {
        this.timeOfLastCalibration = timeOfLastCalibration;
    }


    /**
     *  Getter for property GrayscaleODs.
     *
     * @return  Value of property GrayscaleODs.
     */
    public float[] getGrayscaleODs()
    {
        return stepODs == null ? null : (float[]) stepODs.clone();
    }


    /**
     *  Setter for property GrayscaleODs.
     *
     * @param  stepODs The new grayscaleODs value
     */
    public void setGrayscaleODs(float[] stepODs)
    {
        if (stepODs.length < 4 || stepODs.length > 64) {
            throw new IllegalArgumentException("steps: " + stepODs.length);
        }
        if (this.stepODs != null && Arrays.equals(this.stepODs, stepODs)) {
            return;
            // no change
        }

        float[] tmp = (float[]) stepODs.clone();
        Arrays.sort(tmp);
        if (!Arrays.equals(tmp, stepODs)) {
            throw new IllegalArgumentException(
                    "stepODs[" + tmp.length + "] not monotonic increasing");
        }
        this.stepODs = tmp;
        initDDL2OD();
    }


    // Package protected ---------------------------------------------

    // Protected -----------------------------------------------------

    // Private -------------------------------------------------------

    private void check(int n, float dmin, float dmax)
    {
        if (stepODs == null) {
            throw new IllegalStateException("grayscaleODs not yet set");
        }
        if (n < 8 || n > 16) {
            throw new IllegalArgumentException("n: " + n);
        }
        if (dmin > dmax) {
            throw new IllegalArgumentException("dmin: " + dmin + ", dmax: " + dmax);
        }
        /*
      if (dmin < stepODs[0]) {
         throw new IllegalArgumentException("dmin: " + dmin
         + ", ODmin: " + stepODs[0]);
      }
      if (dmax > stepODs[stepODs.length-1]) {
         throw new IllegalArgumentException("dmax: " + dmax
         + ", ODmax: " + stepODs[stepODs.length-1]);
      }
       */
    }


    private void initDDL2OD()
    {
        int x[] = new int[stepODs.length];
        for (int i = 0; i < x.length; ++i) {
            x[i] = Math.round(255.f * i / (x.length - 1));
        }
        for (int j = 0, i = 0; j < 256; ++j) {
            if (j > x[i + 1]) {
                ++i;
            }
            ddl2od[j] = stepODs[i]
                     + (stepODs[i + 1] - stepODs[i])
                     * (j - x[i]) / (x[i + 1] - x[i]);
        }
    }


    private double lum(double density, float l0, float la)
    {
        return la + l0 * Math.pow(10, -density);
    }


    private double density(int pv, int n, double jmin, double jmax,
            float l0, float la)
    {
        double j = jmin + (jmax - jmin) * pv / ((1 << n) - 1);
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


    private double gsdf(double j)
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


    private double inverseGSDF(double l)
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
    // Inner classes -------------------------------------------------
}

