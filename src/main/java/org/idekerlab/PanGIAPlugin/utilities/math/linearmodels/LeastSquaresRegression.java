package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.data.DoubleMatrix;

public class LeastSquaresRegression
{
	public static double[] leastSquaresRegression(double[][] x, double[] y)
	{

		double[][] xTx = DoubleMatrix.xTx(x);
		double[][] xTxinv = DoubleMatrix.pseudoInverse(xTx);

		if (xTxinv == null)
			return null;

		double[] xTy = DoubleMatrix.xTy(x, y);
		return DoubleMatrix.times(xTxinv, xTy);
	}

}
