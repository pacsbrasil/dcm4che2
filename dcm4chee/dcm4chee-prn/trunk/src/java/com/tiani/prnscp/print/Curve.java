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
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at http://sourceforge.net/projects/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * TIANI Medgraph AG.
 * Portions created by the Initial Developer are Copyright (C) 2002-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * Gunter Zeilinger <gunter.zeilinger@tiani.com>
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

package com.tiani.prnscp.print;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

abstract class Curve
{
    private static class InterpPoint
    {
        public float x, y;
        
        InterpPoint(float x, float y)
        {
            this.x = x;
            this.y = y;
        }
    }

    private Comparator sortComparator = new Comparator()
    {
        public int compare(Object a, Object b)
        {
            return (((InterpPoint)a).x == ((InterpPoint)b).x) ?
                   0 : (((InterpPoint)a).x - ((InterpPoint)b).x < 0) ?
                       -1 : 1;
        }
    };

    private List pts;

    /** Flags if the set of points have been changed since last call to refreshSamples() */
    protected boolean needRefresh;
    /** List of independant x[1..n] values and their corresponding known y[1..n] values */
    protected float[] x, y;
    protected int n;

    protected Curve()
    {
        needRefresh = false;
        pts = new Vector(10, 10);
    }

    protected void refreshSamples()
    {
        InterpPoint[] interpPts = (InterpPoint[])pts.toArray(new InterpPoint[0]);
        Arrays.sort(interpPts, sortComparator);
        n = interpPts.length;
        x = new float[n];
        y = new float[n];
        
        for (int i = 0; i < n; i++) {
            x[i] = interpPts[i].x;
            y[i] = interpPts[i].y;
        }
        interpPts = null;
        needRefresh = false;
    }

    public void add(float x, float y)
    {
        Iterator i = pts.iterator();
        int ind = 0;
        
        while (i.hasNext() && x > ((InterpPoint)i.next()).x)
            ind++;
        pts.add(ind, new InterpPoint(x, y));
        needRefresh = true; //force to re-compute
    }

    public void addAll(float[] xa, float[] ya)
    {
        int size = xa.length;
        if (ya.length != size)
            throw new IllegalArgumentException("xa[] and ya[] arrays length must be equal");
        for (int i = 0; i < size; i++) {
            add(xa[i], ya[i]);
        }
    }

    public void addAll(float[] ya)
    {
        int size = ya.length;
        for (int i = 0; i < size; i++) {
            add(i, ya[i]);
        }
    }
    
    public abstract float evaluate(float x0);
    
    private static void swap(float[][] a, int i, int j,
                             float[][] b, int k, int l)
    {
        float tmp = a[i][j];
        a[i][j] = b[k][l];
        b[k][l] = tmp;
    }
    
    protected static void gj(float[][] a, int n, float[][] b, int m)
        throws Exception
    {
        int[] indxc = new int[n];
        int[] indxr = new int[n];
        int[] ipiv = new int[n];
        int i, j, k, l, ll, icol, irow;
        float big, dum, pivinv, temp;
        
        irow = icol = 0;
        
        for (i = 0; i < n; i++)
            ipiv[i] = 0;
        for (i = 0; i < n; i++) {
            big = 0;
            for (j = 0; j < n; j++) {
                if (ipiv[j] != 1) {
                    for (k = 0; k < n; k++) {
                        if (ipiv[k] == 0) {
                            if (Math.abs(a[j][k]) >= big) {
                                big = Math.abs(a[j][k]);
                                irow = j;
                                icol = k;
                            }
                        }
                    }
                }
            }
            ++ipiv[icol];
            if (irow != icol) {
                for (l = 0; l < n; l++)
                    swap(a, irow, l, a, icol, l);
                for (l = 0; l < m; l++)
                    swap(b, irow, l, b, icol, l);
            }
            indxr[i] = irow;
            indxc[i] = icol;
            if (a[icol][icol] == 0)
                throw new Exception("singular matrix");
            pivinv = 1 / a[icol][icol];
            a[icol][icol] = 1;
            for (l = 0; l < n; l++)
                a[icol][l] *= pivinv;
            for (l = 0; l < m; l++)
                b[icol][l] *= pivinv;
            for (ll = 0; ll < n; ll++) {
                if (ll != icol) {
                    dum = a[ll][icol];
                    a[ll][icol] = 0;
                    for (l = 0; l < n; l++)
                        a[ll][l] -= a[icol][l] * dum;
                    for (l = 0; l < m; l++)
                        b[ll][l] -= b[icol][l] * dum;
                }
            }
        }
        for (l = n - 1; l >= 0; l--) {
            if (indxr[l] != indxc[l]) {
                for (k = 0; k < n; k++) {
                    swap(a, k, indxr[l], a, k, indxc[l]);
                }
            }
        }
    }

    protected static float[][] mult(float[][] a, float[][] b)
    {
        int am = a.length;
        int an = a[0].length;
        int bm = b.length;
        int bn = b[0].length;
        int rm = am;
        int rn = bn;
        
        if (an != bm) {
            return null;
        }
        
        float[][] r = new float[am][bn];
        int i, j, k;
        float dot;
        
        for (i = 0; i < rm; i++) {
            for (j = 0; j < rn; j++) {
                //r[i][j] == j col of b dotted with i row of a
                dot = 0;
                for (k = 0; k < an; k++) {
                    dot += a[i][k] * b[k][j];
                }
                r[i][j] = dot;
            }
        }
        return r;
    }

    protected static float[][] trans(float[][] a)
    {
        int am = a.length;
        int an = a[0].length;
        float[][] t = new float[an][am];
        
        for (int i = 0; i < am; i++) {
            for (int j = 0; j < an; j++) {
                t[j][i] = a[i][j];
            }
        }
        return t;
    }
}
