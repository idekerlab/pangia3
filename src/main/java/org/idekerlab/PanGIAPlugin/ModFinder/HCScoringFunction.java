package org.idekerlab.PanGIAPlugin.ModFinder;

import org.idekerlab.PanGIAPlugin.networks.SFNetwork;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNodeModule;
import org.idekerlab.PanGIAPlugin.networks.matrixNetworks.FloatMatrixNetwork;

import java.util.ArrayList;
import java.util.List;

public abstract class HCScoringFunction
{

	protected FloatMatrixNetwork pscores;
	protected FloatMatrixNetwork gscores;

	/**
	 * Perform initial calculations. (build lookup matricies etc.)
	 */
	public abstract void Initialize(SFNetwork pnet, SFNetwork gnet);

	public abstract float getWithinScore(TypedLinkNodeModule<String, BFEdge> m1);

	public abstract float getBetweenScore(TypedLinkNodeModule<String, BFEdge> m1, TypedLinkNodeModule<String, BFEdge> m2);

	/**
	 * Retrieves the raw interaction data from a network and puts it into a lookup matrix.
	 */
	protected HCScoringFunction()
	{
	}

	protected void buildScoreTables(SFNetwork pnet, SFNetwork gnet)
	{
		//Build the physical within and between scores
		System.out.println("Initializing physical scores...");
		List<String> nodes = new ArrayList<String>(pnet.getNodes());
		pscores = new FloatMatrixNetwork(false, false, nodes);

		for (int i = 0; i < nodes.size() - 1; i++)
		{
			if (i % 1000 == 0)
				System.out.println((float) i / nodes.size() * 100 + "%");
			for (int j = i + 1; j < nodes.size(); j++)
			{
				float score = pnet.edgeValue(nodes.get(i), nodes.get(j));
				if (!Float.isNaN(score))
					pscores.set(i, j, score);
			}
		}

		//Build the genetic within and between scores
		System.out.println("Initializing genetic scores...");
		nodes = new ArrayList<String>(gnet.getNodes());
		gscores = new FloatMatrixNetwork(false, false, nodes);

		for (int i = 0; i < nodes.size() - 1; i++)
		{
			if (i % 1000 == 0)
				System.out.println((float) i / nodes.size() * 100 + "%");
			for (int j = i + 1; j < nodes.size(); j++)
			{
				float score = gnet.edgeValue(nodes.get(i), nodes.get(j));
				if (!Float.isNaN(score))
					gscores.set(i, j, score);
			}
		}
	}


}
