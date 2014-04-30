package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms.LMTerm;

import java.util.List;

public class LogisticModelD extends AbstractSingleLinearModelD
{


	public LogisticModelD(List<LMTerm> terms, double[][] x, double[] y)
	{
		super(terms, x, y);
	}

	public void regress(int maxit, double epsilon, double[] weights)
	{
		this.coefficients = LogisticRegression.logisticRegression(this.evaluateX(), y, weights, maxit, epsilon, false);
	}


	public double yhat(int i)
	{
		double e = 0;

		for (int t = 0; t < terms.size(); t++)
			e += coefficients.get(t) * terms.get(t).evaluate(x, i);

		e = Math.exp(e);

		return e / (1 + e);
	}


}
