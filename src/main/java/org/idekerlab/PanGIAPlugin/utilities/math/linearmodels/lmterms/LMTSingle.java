package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms;


public final class LMTSingle extends LMTerm
{
	private final int varIndex;

	public LMTSingle(int varIndex)
	{
		this.varIndex = varIndex;
	}

	public double evaluate(double[][] data, int row)
	{
		return data[row][varIndex];
	}

	public String toString()
	{
		return "x" + varIndex;
	}

	public int hashCode()
	{
		return varIndex;
	}

	public boolean equals(Object other)
	{
		return other instanceof LMTSingle && ((LMTSingle) other).varIndex == varIndex;

	}

}
