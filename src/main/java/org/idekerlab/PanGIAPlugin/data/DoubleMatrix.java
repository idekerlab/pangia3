package org.idekerlab.PanGIAPlugin.data;

import org.idekerlab.PanGIAPlugin.utilities.math.svd.LinpackSVD;
import org.idekerlab.PanGIAPlugin.utilities.math.svd.SVD;

import java.util.ArrayList;

public class DoubleMatrix extends DataMatrix
{

	private final double[][] data;

	private DoubleMatrix(DataMatrix dt)
	{
		this.data = new double[dt.numRows()][dt.numCols()];

		for (int i = 0; i < dt.numRows(); i++)
			for (int j = 0; j < dt.numCols(); j++)
				data[i][j] = dt.getAsDouble(i, j);

		if (dt.hasColNames())
			this.setColNames(new ArrayList<String>(dt.getColNames()));
		if (dt.hasRowNames())
			this.setRowNames(new ArrayList<String>(dt.getRowNames()));
	}

	public static double[][] copy(double[][] m)
	{
		double[][] out = new double[m.length][m[0].length];

		for (int i = 0; i < m.length; i++)
			System.arraycopy(m[i], 0, out[i], 0, m[0].length);

		return out;
	}

	public DoubleMatrix clone()
	{
		return new DoubleMatrix(this);
	}

	public int dim(int dimension)
	{
		if (dimension == 0)
			return data.length;

		if (data.length > 0 && dimension == 1)
			return data[0].length;

		return -1;
	}

	public double getAsDouble(int i, int j)
	{
		return (data[i][j]);
	}

	public static double[][] getCol(double[][] x, int[] indexes)
	{
		if (indexes.length == 0)
			return new double[0][0];

		double[][] cols = new double[x.length][indexes.length];

		for (int j = 0; j < indexes.length; j++)
		{
			int indexj = indexes[j];
			for (int i = 0; i < x.length; i++)
				cols[i][j] = x[i][indexj];
		}

		return cols;
	}

	public static double[][] xTx(double[][] x)
	{
		double[][] out = new double[x[0].length][x[0].length];

		for (int i = 0; i < x[0].length; i++)
			for (int j = 0; j < x[0].length; j++)
			{
				double sum = 0;
				for (double[] aX : x)
					sum += aX[i] * aX[j];

				out[i][j] = sum;
			}

		return out;
	}


	public static double[][] xxT(double[][] M)
	{
		double[][] out = new double[M.length][M.length];

		for (int i = 0; i < M.length; i++)
		{
			for (int j = 0; j < M.length; j++)
			{
				double sum = 0;
				for (int k = 0; k < M[0].length; k++)
					sum += M[j][k] * M[i][k];

				out[i][j] = sum;
			}
		}

		return out;
	}


	public static double[] xTy(double[][] x, double[] vec)
	{
		double[] out = new double[x[0].length];

		for (int i = 0; i < x[0].length; i++)
		{
			double sum = 0;
			for (int j = 0; j < vec.length; j++)
				sum += x[j][i] * vec[j];

			out[i] = sum;
		}

		return out;
	}

	public int numRows()
	{
		return data.length;
	}

	public int numCols()
	{
		if (data.length == 0)
			return 0;
		return data[0].length;
	}

	/**
	 * Modifies the original matrix.
	 *
	 * @param x
	 */
	public static double[][] pseudoInverse(double[][] x)
	{
		SVD svd = new LinpackSVD(x, true);

		//If x is singular
		if (svd.U()[0].length != x[0].length)
			return null;

		double[][] u = svd.U();
		double[] s = svd.S();
		double[][] v = svd.V();

		double smax = 0;

		for (double value : s)
			if (value > smax)
				smax = value;

		//Based on the machine double precision of 2.220446e-16
		double tol = 2.220446e-16 * Math.max(x.length, x[0].length) * smax;

		for (int i = 0; i < s.length; i++)
		{
			if (s[i] <= tol)
				s[i] = 0;
			else
				s[i] = 1 / s[i];
		}

		DoubleMatrix.multiplyCols(v, s);

		return DoubleMatrix.timesXYT(v, u);
	}

	private static void multiplyCols(double[][] x, double[] vec)
	{
		for (int j = 0; j < vec.length; j++)
			for (int i = 0; i < x.length; i++)
				x[i][j] *= vec[j];
	}

	private static double[][] timesXYT(double[][] x, double[][] y)
	{
		double[][] out = new double[x.length][y.length];

		for (int i = 0; i < x.length; i++)
			for (int j = 0; j < y.length; j++)
			{
				double sum = 0;

				for (int k = 0; k < x[0].length; k++)
					sum += x[i][k] * y[j][k];

				out[i][j] = sum;
			}

		return out;
	}

	public static double[] times(double[][] x, double[] vec)
	{
		if (vec.length != x[0].length)
			throw new IllegalArgumentException("Dimension mismatch: xcol!=vlength, " + x[0].length + " != " + vec.length);

		double[] out = new double[x.length];

		for (int i = 0; i < x.length; i++)
		{
			double sum = 0;

			for (int j = 0; j < x[0].length; j++)
				sum += vec[j] * x[i][j];

			out[i] = sum;
		}

		return out;
	}


}

