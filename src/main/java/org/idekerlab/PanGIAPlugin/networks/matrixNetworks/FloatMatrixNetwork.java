package org.idekerlab.PanGIAPlugin.networks.matrixNetworks;

import org.idekerlab.PanGIAPlugin.networks.SEdge;
import org.idekerlab.PanGIAPlugin.networks.SFEdge;
import org.idekerlab.PanGIAPlugin.networks.SFNetwork;
import org.idekerlab.PanGIAPlugin.networks.linkedNetworks.TypedLinkNetwork;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.*;


public class FloatMatrixNetwork extends SFNetwork
{
	private Map<String, Integer> nodeLookup;
	private List<String> nodeValues;
	private float[][] connectivity;

	public FloatMatrixNetwork(boolean selfOk, boolean directed, Collection<String> nodeValues)
	{
		super(selfOk, directed);
		this.nodeValues = new ArrayList<String>(nodeValues);
		Initialize(nodeValues.size());
		InitializeMap();
	}

	public Set<String> getNodes()
	{
		return new HashSet<String>(nodeValues);
	}

	public boolean contains(int i, int j)
	{
		if (j > i)
		{
			int temp = j;
			j = i;
			i = temp;
		}

		if (selfOk || i != j) return !Float.isNaN(connectivity[i][j]);
		else return false;
	}

	public boolean contains(String n1, String n2)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);

		if (i1 == null || i2 == null) return false;

		return contains(i1, i2);
	}

	public void set(String n1, String n2, float value)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);

		this.set(i1, i2, value);
	}

	public void set(int n1, int n2, float value)
	{
		if (!directed && n2 > n1)
		{
			int temp = n1;
			n1 = n2;
			n2 = temp;
		}

		if (selfOk || n1 != n2) this.connectivity[n1][n2] = value;
	}

	public String getNodeValue(int i)
	{
		return this.nodeValues.get(i);
	}

	public float edgeValue(int i, int j)
	{
		if (j > i)
		{
			int temp = j;
			j = i;
			i = temp;
		}

		if (selfOk || i != j) return connectivity[i][j];
		else return Float.NaN;
	}

	public float edgeValue(String n1, String n2)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);

		if (i1 == null || i2 == null) return Float.NaN;

		return edgeValue(i1, i2);
	}

	private void Initialize(int size)
	{
		if (directed)
		{
			connectivity = new float[size][size];

			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					connectivity[i][j] = Float.NaN;
		}
		else
		{
			connectivity = new float[size][];

			for (int i = 0; i < size; i++)
			{
				if (selfOk) connectivity[i] = new float[i + 1];
				else connectivity[i] = new float[i];

				for (int j = 0; j < connectivity[i].length; j++)
					connectivity[i][j] = Float.NaN;
			}
		}
	}

	private void InitializeMap()
	{
		this.nodeLookup = new HashMap<String, Integer>(nodeValues.size());
		for (int i = 0; i < nodeValues.size(); i++)
			nodeLookup.put(nodeValues.get(i), i);
	}

	public int numNodes()
	{
		return this.nodeValues.size();
	}

	public int numEdges()
	{
		int count = 0;
		for (int i = 0; i < connectivity.length; i++)
			for (int j = 0; j < connectivity[i].length; j++)
				if (!Float.isNaN(connectivity[i][j])) count++;
		return count;
	}

	public TypedLinkNetwork<String, Float> asTypedLinkNetwork()
	{
		TypedLinkNetwork<String, Float> net = new TypedLinkNetwork<String, Float>(this.nodeValues, false, this.directed);

		for (int i = 0; i < this.connectivity.length; i++)
			for (int j = 0; j < this.connectivity[i].length; j++)
			{
				float score = this.edgeValue(i, j);
				if (!Float.isNaN(score))
					net.addEdgeWNodeUpdate(this.getNodeValue(i), this.getNodeValue(j), score);
			}

		return net;
	}

	public IIterator<SFEdge> edgeIterator()
	{
		throw new UnsupportedOperationException("Matrix networks do not currently support edge iteration.");
	}

	public IIterator<String> nodeIterator()
	{
		return new IIterator<String>(this.nodeValues.iterator());
	}

	public boolean contains(SEdge e)
	{
		return contains(e.getI1(), e.getI2());
	}
}


