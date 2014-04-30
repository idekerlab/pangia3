package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms;

public final class LMTIntercept extends LMTerm
{
	public double evaluate(double[][] data, int row)
	{
		return 1;
	}

	public String toString()
	{
		return "1";
	}

	public int hashCode()
	{
		return -1;
	}

	public boolean equals(Object other)
	{
		return other instanceof LMTIntercept;
	}
}
