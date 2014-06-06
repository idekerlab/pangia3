package org.idekerlab.PanGIAPlugin.utilities.math.svd;

public class SVD
{
	protected double[][] U;
	protected double[] S;
	protected double[][] V;

	protected SVD(double[][] U, double[] S, double[][] V)
	{
		this.U = U;
		this.S = S;
		this.V = V;
	}

	public double[][] U()
	{
		return U;
	}

	public double[] S()
	{
		return S;
	}

	public double[][] V()
	{
		return V;
	}

}
