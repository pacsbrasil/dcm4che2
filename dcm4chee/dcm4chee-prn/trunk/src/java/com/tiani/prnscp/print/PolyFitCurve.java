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
