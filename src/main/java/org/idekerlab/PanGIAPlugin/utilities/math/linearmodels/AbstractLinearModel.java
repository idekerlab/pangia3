package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.data.DoubleVector;
import org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms.LMTerm;

import java.util.List;

public abstract class AbstractLinearModel
{
	protected final List<LMTerm> terms;
		
	public abstract int df();
	public abstract double SSE();
	public abstract void regress();
	public abstract double FANOVA(double SST);
	public abstract void printCoefficients();
	public abstract double yhat(int i);
	public abstract int ysize();
	
	public AbstractLinearModel(List<LMTerm> terms)
	{
		this.terms = terms;
	}

	public void addTerm(LMTerm term)
	{
		this.terms.add(term);
	}

	public void removeTerm(LMTerm term)
	{
		this.terms.remove(term);
	}

	public int numTerms()
	{
		return terms.size();
	}
	
	public List<LMTerm> terms()
	{
		return terms;
	}
	
	public abstract double[][] evaluateX();
	public abstract LogisticModelD deriveEigenMarkerLogisticSubmodel(double percentVar, int maxPC);
	public abstract int numCols();
	public abstract double logLikelyhoodBinary();
	public abstract DoubleVector coefficients();
	public abstract LogisticModelD getSubmodel();


}
