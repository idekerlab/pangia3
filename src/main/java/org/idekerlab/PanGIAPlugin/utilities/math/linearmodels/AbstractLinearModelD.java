package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms.LMTerm;

import java.util.List;


public abstract class AbstractLinearModelD extends AbstractLinearModel
{
	protected final double[][] x; //individuals x markers

	protected AbstractLinearModelD(List<LMTerm> terms, double[][] x)
	{
		super(terms);
		this.x = x;
	}


	/**
	 * Compute the term evaluation matrix X.
	 */
	protected double[][] evaluateX()
	{
		double[][] X = new double[this.x.length][terms.size()];

		for (int i = 0; i < this.x.length; i++)
			for (int j = 0; j < terms.size(); j++)
				X[i][j] = terms.get(j).evaluate(x, i);

		return X;
	}

}
