package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.data.DoubleMatrix;
import org.idekerlab.PanGIAPlugin.data.DoubleVector;

/**
 * Most of this code is translated directly from R-2.9.0
 *
 * @author ghannum
 *         6/9/2009
 */
public class LogisticRegression
{
	private static double[] variance(double[] mu)
	{
		double[] out = new double[mu.length];

		for (int i = 0; i < mu.length; i++)
			out[i] = mu[i] * (1 - mu[i]);

		return out;
	}

	private static double[] linkfun(double[] mu)
	{
		double[] out = new double[mu.length];

		for (int i = 0; i < mu.length; i++)
			out[i] = Math.log(mu[i] / (1 - mu[i]));

		return out;
	}

	private static double[] linkinv(double[] eta)
	{
		double[] out = new double[eta.length];

		for (int i = 0; i < eta.length; i++)
		{
			double t = Math.exp(eta[i]);
			out[i] = t / (1 + t);
		}

		return out;
	}

	private static double[] mu_eta(double[] eta)
	{
		double[] out = new double[eta.length];

		for (int i = 0; i < eta.length; i++)
		{
			double t = Math.exp(eta[i]);
			double tp1 = t + 1;
			out[i] = t / (tp1 * tp1);
		}

		return out;
	}

	private static double y_log_y(double y, double mu)
	{
		//y_log_y is:  return (y) ? (y * log(y/mu)) : 0;
		if (y == 1)
			return Math.log(y / mu);
		else
			return 0;
	}

	private static double[] dev_resids(double[] y, double[] mu, double[] wt)
	{
		int n = y.length;
		int lmu = mu.length;
		int lwt = wt.length;

		double[] ans = y.clone(); //All (but this?) added to convert to reals, may not be necessary


		if (lmu != n && lmu != 1)
			System.out.println("error: argument mu must be a numeric vector or length of 1 or n");
		if (lwt != n && lwt != 1)
			System.out.println("error: argument wt must be a numeric vector or length of 1 or n");

		for (int i = 0; i < n; i++)
		{
			double mui = mu[i];
			double yi = y[i];
			ans[i] = 2 * wt[lwt > 1 ? i : 0] * (y_log_y(yi, mui) + y_log_y(1 - yi, 1 - mui));
		}

		return ans;
	}

	private static boolean validmu(double[] mu)
	{
		return DoubleVector.allGreaterThan(mu, 0) && DoubleVector.allLessThan(mu, 1);
	}

	/**
	 * Typical maxit = 25
	 * Typical epsilon = 1e-8
	 */
	public static DoubleVector logisticRegression(double[][] x, double[] y, double[] weights, int maxit, double controlepsilon, boolean silent)
	{
		if (x.length != y.length)
			throw new IllegalArgumentException("Dimension mismatch: Xrows!=ylength, " + x.length + "!=" + y.length);

		DoubleVector coef = null;
		DoubleVector coefold = null;

		int nobs = y.length;

		double[] offset = DoubleVector.repeat(0, nobs);


		//Family initialize  y is a vector of 0s and 1s
		// allow factors as responses
		// added BDR 29/5/98

		// anything, e.g. NA/NaN, for cases with zero weight is OK.
		DoubleVector.set(y, DoubleVector.equalTo(weights, 0), 0);

		if (DoubleVector.anyGreaterThan(y, 1) || DoubleVector.anyLessThan(y, 0))
			throw new IllegalArgumentException("y values must be 0 <= y <= 1");


		double[] m = DoubleVector.times(weights, y);
		double[] mustart = DoubleVector.divideBy(DoubleVector.plus(m, .5), DoubleVector.plus(weights, 1));

		if (DoubleVector.anyGreaterThan(DoubleVector.abs(DoubleVector.subtract(m, DoubleVector.round(m))), 1e-3) && !silent)
			System.out.println("non-integer #successes in a binomial glm!");

		double[] eta = linkfun(mustart);
		double[] mu = linkinv(eta);


		// calculate initial deviance and coefficient
		double devold = DoubleVector.sum(dev_resids(y, mu, weights));

		boolean boundary = false;
		boolean conv = false;

		//timer.stopblock();
		//timer.startblock("IWLS");
		//------------- THE Iteratively Reweighting L.S. iteration -----------
		for (int iter = 0; iter < maxit; iter++)
		{
			double[] varmu = variance(mu);

			if (DoubleVector.isAnyNaN(varmu))
			{
				if (!silent)
					System.err.println("NaNs in V(mu)");
				return null;
			}
			if (DoubleVector.anyEqualTo(varmu, 0))
			{
				if (!silent)
					System.err.println("0s in V(mu)");
				return null;
			}

			double[] mu_eta_val = mu_eta(eta);

			double[] z = DoubleVector.plus(DoubleVector.subtract(eta, offset), DoubleVector.divideBy(DoubleVector.subtract(y, mu), mu_eta_val));
			double[] w = DoubleVector.sqrt(DoubleVector.divideBy(DoubleVector.times(weights, DoubleVector.square(mu_eta_val)), variance(mu)));
			double[][] x2 = DoubleMatrix.copy(x);

			//timer.stopblock();
			for (int row = 0; row < x2.length; row++)
				for (int col = 0; col < x2[0].length; col++)
					x2[row][col] = x2[row][col] * w[row];
			//timer.stopblock();	


			//timer.stopblock();
			//timer.startblock("LSR");

			double[] coefficients = LeastSquaresRegression.leastSquaresRegression(x2, DoubleVector.times(w, z));

			if (coefficients == null)
			{
				conv = false;
				if (!silent)
					System.out.println("Warning: Regression failed at iteration " + iter);
				break;
			}

			//System.out.println(iter);
			//System.out.println(coefficients);

			//timer.stopblock();
			//timer.startblock("Step 2");


			if (new DoubleVector(coefficients).isReal().anyEqualTo(false))
			{
				conv = false;
				if (!silent)
					System.out.println("Warning: non-finite coefficients at iteration " + iter);
				break;
			}

			// stop if not enough parameters
			//if (nobs < fit.rank) System.out.println("X matrix has rank "+fit.rank+", but only "+nobs+"observations");

			// calculate updated values of eta and mu with the new coef:
			//DoubleVector start = new DoubleVector(fit.pivot.max(),Double.NaN);
			//start.set(fit.pivot, fit.coefficients);
			double[] start = DoubleVector.copy(coefficients); //added to replace above

			eta = DoubleMatrix.times(x, start);
			eta = DoubleVector.plus(eta, offset);
			mu = linkinv(eta);
			double dev = DoubleVector.sum(dev_resids(y, mu, weights));

			// check for divergence
			boundary = false;
			if (Double.isInfinite(dev) || Double.isNaN(dev))
			{
				if (coefold == null && !silent)
				{
					System.err.println("no valid set of coefficients has been found: please supply starting values");
					System.exit(0);
				}

				if (!silent)
					System.out.println("Warning: step size truncated due to divergence");
				int ii = 1;

				while (Double.isInfinite(dev) || Double.isNaN(dev))
				{
					if (ii > maxit && !silent)
					{
						System.err.println("inner loop 1; cannot correct step size");
						System.exit(0);
					}

					ii++;
					start = new DoubleVector(start).plus(coefold).divideBy(2).getData();
					eta = DoubleMatrix.times(x, start);
					eta = DoubleVector.plus(eta, offset);
					mu = linkinv(eta);
					dev = DoubleVector.sum(dev_resids(y, mu, weights));
				}
				boundary = true;
			}

			// check for fitted values outside domain.
			if (!validmu(mu))
			{
				if (coefold == null && !silent)
				{
					System.err.println("no valid set of coefficients has been found: please supply starting values");
					System.exit(0);
				}

				if (!silent)
					System.out.println("Warning: step size truncated: out of bounds");

				int ii = 1;
				while (!validmu(mu))
				{
					if (ii > maxit)
					{
						if (!silent)
							System.err.println("inner loop 2; cannot correct step size");
						return null;
					}

					ii++;
					start = new DoubleVector(start).plus(coefold).divideBy(2).getData();
					eta = DoubleMatrix.times(x, start);
					eta = DoubleVector.plus(eta, offset);
					mu = linkinv(eta);
				}
				boundary = true;
				dev = DoubleVector.sum(dev_resids(y, mu, weights));
			}

			// check for convergence
			if (Math.abs(dev - devold) / (0.1 + Math.abs(dev)) < controlepsilon)
			{
				conv = true;
				coef = new DoubleVector(DoubleVector.copy(start));
				break;
			}
			else
			{
				devold = dev;
				coef = new DoubleVector(DoubleVector.copy(start));
				coefold = new DoubleVector(DoubleVector.copy(start));
			}


		} //-------------- end IRLS iteration -------------------------------


		if (!conv)
		{
			//System.out.println("Warning: algorithm did not converge");
			return null;
		}
		if (boundary && !silent)
			System.out.println("Warning: algorithm stopped at boundary value");

		if (DoubleVector.anyGreaterThan(mu, 1 - 2.220446e-15) || DoubleVector.anyLessThan(mu, 2.220446e-15))
		{
			if (!silent)
				System.out.println("Warning: fitted probabilities numerically 0 or 1 occurred");
			return null;
		}
		return coef;
	}

}

