package com.tiani.prnscp.print;

class PolyInterpCurve extends Curve
{
    PolyInterpCurve()
    {
    }

    public float evaluate(float x0)
    {
        if (needRefresh) {
            refreshSamples();
        }
        if (x0 < x[0] || x0 > x[n - 1]) {
            throw new UnsupportedOperationException("Extrapolation is not supported");
        }
        return p(x0, 0, n - 1, x, y);
    }
    
    private float p(float x0, int minInd, int maxInd, float[] x, float[] y)
    {
        int m = maxInd - minInd;
        if (m == 0)
            return y[minInd];
        else
            return ((x0 - x[maxInd]) * p(x0, minInd, maxInd - 1, x, y)
                    + (x[minInd] - x0) * p(x0, minInd + 1, maxInd, x, y))
                   / (x[minInd] - x[maxInd]);
    }
}
