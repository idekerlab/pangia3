package org.idekerlab.PanGIAPlugin.utilities.math.linearmodels;

import org.idekerlab.PanGIAPlugin.utilities.math.linearmodels.lmterms.LMTerm;

import java.util.List;

public abstract class AbstractLinearModel
{
	protected final List<LMTerm> terms;

	protected AbstractLinearModel(List<LMTerm> terms)
	{
		this.terms = terms;
	}
}
