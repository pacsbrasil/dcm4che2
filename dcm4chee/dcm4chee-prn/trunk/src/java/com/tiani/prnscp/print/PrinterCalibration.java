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

import org.dcm4che.data.Dataset;
import org.dcm4che.dict.Tags;

import java.util.Arrays;

/**
 * <description>
 *
 * @see <related>
 * @author  <a href="mailto:gunter@tiani.com">gunter zeilinger</a>
 * @version $Revision$
 * @since November 19, 2002
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>yyyymmdd author:</b>
 * <ul>
 * <li> explicit fix description (no line numbers but methods) go
 *            beyond the cvs commit message
 * </ul>
 */
class PrinterCalibration 
{   
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------
   private float[] odSteps;
   private float[] ddl2od = new float[256];
   
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   
   public int toDDL(float density) {
      int i = Arrays.binarySearch(ddl2od, density);
      if (i >= 0) {
         return 255 - i;
      }
      i = -i-1;
      if (i == 0) {
         return 255;
      }
      if (i > 255) {
         return 0;
      }
      float diff1 = ddl2od[i] - density;
      float diff2 = density - ddl2od[i-1];
      return 255 - (diff1 < diff2 ? i : i-1);
   }
   
   public byte[] getPValToDDLwLinOD(int n, float dmin, float dmax) {
      check(n, dmin, dmax);
      byte[] lut = new byte[1<<n];
      for (int p = 0; p < lut.length; ++p) {
         lut[p] = (byte) toDDL(dmax - (dmax - dmin) * p / ((1<<n) - 1));
      }
      return lut;
   }
   
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
      byte[] lut = new byte[1<<n];
      for (int pv = 0; pv < lut.length; ++pv) {
         lut[pv] = (byte) toDDL((float) density(pv, n, jmin, jmax, l0, la));
      }
      return lut;
   }
   
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
      if (src.length != (1<<desc[2])) {
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
   
   
   public float[] getODSteps() {
      return odSteps == null ? null : (float[]) odSteps.clone();
   }
   
   /** Setter for property odSteps.
    * @param odSteps New value of property odSteps.
    */
   public void setODSteps(float[] newODSteps) {
      if (newODSteps.length < 2 || newODSteps.length > 65) {
         throw new IllegalArgumentException("steps: " + newODSteps.length);
      }
      if (odSteps != null && Arrays.equals(odSteps, newODSteps)) {
         return; // no change
      }
      
      float[] tmp = (float[])newODSteps.clone();
      Arrays.sort(tmp);
      if (!Arrays.equals(tmp, newODSteps)) {
         throw new IllegalArgumentException(
         "odSteps[" + tmp.length + "] not monotonic increasing");
      }
      this.odSteps = tmp;
      initDLL2OD();
   }
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   private void check(int n, float dmin, float dmax) {
      if (odSteps == null) {
         throw new IllegalStateException("ODSteps not yet set");
      }
      if (n < 8 || n > 16) {
         throw new IllegalArgumentException("n: " + n);
      }
      if (dmin > dmax) {
         throw new IllegalArgumentException("dmin: " + dmin + ", dmax: " + dmax);
      }
      /*
      if (dmin < odSteps[0]) {
         throw new IllegalArgumentException("dmin: " + dmin
         + ", ODmin: " + odSteps[0]);
      }
      if (dmax > odSteps[odSteps.length-1]) {
         throw new IllegalArgumentException("dmax: " + dmax
         + ", ODmax: " + odSteps[odSteps.length-1]);
      }
       */
   }
   
   private void initDLL2OD() {
      int x[] = new int[odSteps.length];
      for (int i = 0; i < x.length; ++i) {
         x[i] = Math.round(255.f * i / (x.length - 1));
      }
      for (int j = 0, i = 0; j < 256; ++j) {
         if (j > x[i+1]) {
            ++i;
         }
         ddl2od[j] = odSteps[i]
            + (odSteps[i+1] - odSteps[i])
            * (j - x[i]) / (x[i+1] - x[i]);
      }
   }
   
   private double lum(double density, float l0, float la) {
      return la + l0 * Math.pow(10, -density);
   }
   
   private double density(int pv, int n, double jmin, double jmax,
      float l0, float la)
   {
      double j = jmin + (jmax - jmin) * pv / ((1<<n) - 1);
      return - Math.log((gsdf(j) - la) / l0) / LOG10;
   }
   
   private static final double A0 = -1.3011877;
   private static final double A1 = 8.0242636E-2;
   private static final double A2 = 1.3646699E-1;
   private static final double A3 = -2.5468404E-2;
   private static final double A4 = 1.3635334E-3;
   private static final double B1 = -2.5840191E-2;
   private static final double B2 = -1.0320229E-1;
   private static final double B3 = 2.8745620E-2;
   private static final double B4 = -3.1978977E-3;
   private static final double B5 = 1.2992634E-4;
   
   private double gsdf(double j) {
      double lnj = Math.log(j);
      double lnj2 = lnj * lnj;
      double lnj3 = lnj2 * lnj;
      double lnj4 = lnj2 * lnj2;
      double lnj5 = lnj2 * lnj3;
      return Math.pow(10, (A0 + A1*lnj + A2*lnj2 + A3*lnj3 + A4*lnj4)
                    / (1 + B1*lnj + B2*lnj2 + B3*lnj3 + B4*lnj4 + B5*lnj5));
   }
   
   private static final double C0 = 71.498068;
   private static final double C1 = 94.593053;
   private static final double C2 = 41.912053;
   private static final double C3 = 9.8247004;
   private static final double C4 = 0.28175407;
   private static final double C5 = -1.1878455;
   private static final double C6 = -0.18014349;
   private static final double C7 = 0.14710899;
   private static final double C8 = - 0.017046845;
   private static final double LOG10 = Math.log(10);
   
   private double inverseGSDF(double l) {
      double log10L = Math.log(l)/LOG10;
      double log10L2 = log10L * log10L;
      double log10L3 = log10L2 * log10L;
      double log10L4 = log10L2 * log10L2;
      double log10L5 = log10L2 * log10L3;
      double log10L6 = log10L3 * log10L3;
      double log10L7 = log10L4 * log10L3;
      double log10L8 = log10L4 * log10L4;
      return C0 + C1*log10L + C2*log10L2 + C3*log10L3 + C4*log10L4
         + C5*log10L5 + C6*log10L6 + C7*log10L7 + C8*log10L8;
   }
   
   // Inner classes -------------------------------------------------
}
