package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.data.DoubleVector;
import org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms.LMTerm;

import java.util.List;

public abstract class AbstractSingleLinearModelD extends AbstractLinearModelD
{
	protected DoubleVector coefficients;
	protected final double[] y;

	protected AbstractSingleLinearModelD(List<LMTerm> terms, double[][] x, double[] y)
	{
		super(terms, x);
		this.y = y;
	}

	public String toString()
	{
		String out = "y=";

		if (coefficients == null)
			for (int i = 0; i < terms.size(); i++)
			{
				out += "b" + i + '*' + terms.get(i);
				if (i != terms.size() - 1)
					out += " + ";
			}
		else
			for (int i = 0; i < terms.size(); i++)
			{
				out += String.format("%.3f*" + terms.get(i), coefficients.get(i));
				if (i != terms.size() - 1)
					out += " + ";
			}

		return out;
	}


	public DoubleVector coefficients()
	{
		return coefficients;
	}
}
