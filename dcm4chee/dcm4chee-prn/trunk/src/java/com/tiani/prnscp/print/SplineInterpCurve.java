package com.tiani.prnscp.print;

public class SplineInterpCurve extends Curve
{
    private float[] y2; //second derivatives
    
    SplineInterpCurve()
    {
    }
    
    private void computeSecondDerivs(float yp1, float ypn)
    {
        refreshSamples();
        y2 = new float[n];
        
        int i, k;
        float p, qn, sig, un;
        float[] u = new float[n-1];
        
        if (yp1 > 0.99e30) {
            y2[0] = u[0] = 0;
        }
        else {
            y2[0] = -0.5f;
            u[0] = (3.0f/(x[1] - x[0])) * ((y[1] - y[0])/(x[1] - x[0]) - yp1);
        }
        
        for (i = 1; i < n - 1; i++) {
            sig = (x[i] - x[i-1]) / (x[i+1] - x[i-1]);
            p = sig * y2[i-1] + 2.0f;
            y2[i] = (sig - 1.0f) / p;
            u[i] = (y[i+1] - y[i]) / (x[i+1] - x[i]) - (y[i] - y[i-1]) / (x[i] - x[i-1]);
            u[i] = (6.0f * u[i] / (x[i+1] - x[i-1]) - sig * u[i-1]) / p;
        }
        
        if (ypn > 0.99e30) {
            qn = un = 0f;
        }
        else {
            qn = 0.5f;
            un = (3.0f / (x[n-1] - x[n-2]))
                 * (ypn - (y[n-1] - y[n-2]) / (x[n-1] - x[n-2]));
        }
        
        y2[n-1] = (un - qn * u[n-2]) / (qn * y2[n-2] + 1.0f);
        for (k = n - 2; k >= 0; k--) { //backsubstitution loop to solve tridiagonal matrix of equations
            y2[k] = y2[k] * y2[k+1] + u[k];
        }
    }
    
    public float evaluate(float x0)
    {
        final float yp1 = 0;
        final float ypn = 0;
        //precomute second derivatives (y2[1..n]) for the spline
        if (needRefresh) {
            computeSecondDerivs(yp1, ypn);
        }
        //err check
        if (x0 < x[0] || x0 > x[n - 1]) {
            throw new UnsupportedOperationException("Extrapolation is not supported");
        }
        //evaluate
        int klo, khi, k;
        float h, b, a;
        
        klo = 0;
        khi = n - 1;
        while (khi - klo > 1) {
            k = ((khi + klo + 2) >> 1) - 1;
            if (x[k] > x0)
                khi = k;
            else
                klo = k;
        }
        
        h = x[khi] - x[klo];
        if (h == 0.0)
            throw new IllegalStateException("x's must be distinct");
        a = (x[khi] - x0) / h;
        b = (x0 - x[klo]) / h;
        return a * y[klo] + b * y[khi] + ((a*a*a - a) * y2[klo] + (b*b*b - b) * y2[khi]) * h * h / 6.0f;
    }
}
