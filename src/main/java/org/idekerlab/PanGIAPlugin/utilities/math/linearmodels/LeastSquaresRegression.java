package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.data.DoubleMatrix;

public class LeastSquaresRegression
{
	public static double[] leastSquaresRegression(double[][] x, double[] y)
	{

		double[][] xTx = DoubleMatrix.xTx(x);
		double[][] xTxinv = DoubleMatrix.pseudoInverse(xTx);

		if (xTxinv == null) return null;

		double[] xTy = DoubleMatrix.xTy(x, y);
		return DoubleMatrix.times(xTxinv, xTy);
	}

	/**
	 * Performs a separate least-squares regression for each row in y
	 *
	 * @param x
	 * @param y
	 */
	public static double[][] leastSquaresRegression(double[][] x, double[][] y)
	{
		double[][] xTx = DoubleMatrix.xTx(x);
		double[][] xTxinv = DoubleMatrix.pseudoInverse(xTx);

		if (xTxinv == null) return null;

		double[][] coeff = new double[y.length][x[0].length];

		for (int r = 0; r < y.length; r++)
		{
			double[] xTy = DoubleMatrix.xTy(x, y[r]);
			double[] coefr = DoubleMatrix.times(xTxinv, xTy);

			System.arraycopy(coefr, 0, coeff[r], 0, x[0].length);
		}

		return coeff;
	}

	public static double[] leastSquaresRegression(double[][] x, double[][] y, int yi, double[][] xTxinv)
	{
		double[] xTy = DoubleMatrix.xTy(x, y[yi]);
		double[] coefr = DoubleMatrix.times(xTxinv, xTy);

		double[] coeff = new double[x[0].length];

		System.arraycopy(coefr, 0, coeff, 0, x[0].length);

		return coeff;
	}
}
