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

class PolyFitCurve extends Curve
{
    private final int degree = 3;
    private float[][] coeff;

    PolyFitCurve()
    {
    }

    private void computeCoeff(int k)
        throws Exception
    {
        refreshSamples();
        //build matrix
        float[][] mX = new float[n][k+1];
        int i, j;
        
        for (i = 0; i < n; i++)
            mX[i][0] = 1;
        for (i = 0; i < n; i++) {
            for (j = 1; j <= k; j++) {
                mX[i][j] = mX[i][j-1] * x[i];
            }
        }
        //build inv(mXT*mX)*mXT
        float[][] mXT = trans(mX);
        float[][] b = new float[k+1][1]; for (i=0; i<=k; i++) b[i][0] = 0; //dummy array
        float[][] yn = new float[n][1]; for (i=0; i<n; i++) yn[i][0] = y[i];
        float[][] inv = mult(mXT, mX);
        gj(inv, k+1, b, 1);
        coeff = mult(mult(inv, mXT), yn);
        
        for (i=0; i<=k; i++)
            System.out.println(coeff[i][0] + ", ");
    }

    public float evaluate(float x0)
    {
        if (needRefresh) {
            try {
                computeCoeff(degree);
            }
            catch (Exception e) { e.printStackTrace(); }
        }
        float sum = coeff[0][0];
        float term = x0;
        for (int i = 1; i <= degree; i++) {
            sum += coeff[i][0] * term;
            term *= x0;
        }
        return sum;
    }
}
