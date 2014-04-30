package org.idekerlab.PanGIAPlugin.networks.matrixNetworks;

import org.idekerlab.PanGIAPlugin.networks.SDEdge;
import org.idekerlab.PanGIAPlugin.networks.SDNetwork;
import org.idekerlab.PanGIAPlugin.networks.SEdge;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.*;


public class DoubleMatrixNetwork extends SDNetwork implements Iterable<SDEdge>
{
	private Map<String, Integer> nodeLookup;
	private List<String> nodeValues;
	private double[][] connectivity;

	public DoubleMatrixNetwork(boolean selfOk, boolean directed, Collection<String> nodeValues)
	{
		super(selfOk, directed);
		this.nodeValues = new ArrayList<String>(nodeValues);
		Initialize(nodeValues.size());
		InitializeMap();
	}

	public double[][] getConnectivityMatrix()
	{
		return connectivity;
	}

	public Set<String> getNodes()
	{
		return new HashSet<String>(this.nodeValues);
	}

	/**
	 * Returns the actual reference to the nodelist data. Use this carefully and only when speed is needed.
	 */
	public List<String> getNodeListData()
	{
		return this.nodeValues;
	}

	public IIterator<SDEdge> edgeIterator()
	{
		return new IIterator<SDEdge>(this.iterator());
	}

	public Iterator<SDEdge> iterator()
	{
		return new DoubleMatrixEdgeIterator(this);
	}

	public boolean isDirected()
	{
		return directed;
	}

	public boolean contains(int i, int j)
	{
		if (j > i)
		{
			int temp = j;
			j = i;
			i = temp;
		}

		if (selfOk || i != j) return !Double.isNaN(connectivity[i][j]);
		else return false;
	}

	public boolean contains(String n1, String n2)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);

		if (i1 == null || i2 == null) return false;

		return contains(i1, i2);
	}

	public boolean contains(SEdge e)
	{
		Integer i1 = nodeLookup.get(e.getI1());
		Integer i2 = nodeLookup.get(e.getI2());

		if (i1 == null || i2 == null) return false;

		return contains(i1, i2);
	}

	public int indexOf(String value)
	{
		Integer i = nodeLookup.get(value);
		if (i == null) return -1;
		else return i;
	}

	public void set(String n1, String n2, double value)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);

		this.set(i1, i2, value);
	}

	public void set(SDEdge e)
	{
		Integer i1 = nodeLookup.get(e.getI1());
		Integer i2 = nodeLookup.get(e.getI2());

		this.set(i1, i2, e.value());
	}

	public void set(int n1, int n2, double value)
	{
		if (!directed && n2 > n1)
		{
			int temp = n1;
			n1 = n2;
			n2 = temp;
		}

		if (selfOk || n1 != n2) this.connectivity[n1][n2] = value;
	}

	public void SetData(double[][] data)
	{
		for (int i = 0; i < connectivity.length; i++)
			System.arraycopy(data[i], 0, connectivity[i], 0, connectivity[i].length);
	}

	public String getNodeValue(int i)
	{
		return this.nodeValues.get(i);
	}

	public double edgeValue(int i, int j)
	{
		if (j > i)
		{
			int temp = j;
			j = i;
			i = temp;
		}

		if (selfOk || i != j) return connectivity[i][j];
		else return Double.NaN;
	}

	private void Initialize(int size)
	{
		if (directed)
		{
			connectivity = new double[size][size];

			for (int i = 0; i < size; i++)
				for (int j = 0; j < size; j++)
					connectivity[i][j] = Double.NaN;
		}
		else
		{
			connectivity = new double[size][];

			for (int i = 0; i < size; i++)
			{
				if (selfOk) connectivity[i] = new double[i + 1];
				else connectivity[i] = new double[i];

				for (int j = 0; j < connectivity[i].length; j++)
					connectivity[i][j] = Double.NaN;
			}
		}
	}

	private void InitializeMap()
	{
		this.nodeLookup = new HashMap<String, Integer>(nodeValues.size());
		for (int i = 0; i < nodeValues.size(); i++)
			nodeLookup.put(nodeValues.get(i), i);
	}

	public int numEdges()
	{
		int count = 0;
		for (int i = 0; i < connectivity.length; i++)
			for (int j = 0; j < connectivity[i].length; j++)
				if (!Double.isNaN(connectivity[i][j])) count++;
		return count;
	}

	public int numNodes()
	{
		return this.nodeValues.size();
	}

	public double[][] getData()
	{
		return connectivity;
	}

}


