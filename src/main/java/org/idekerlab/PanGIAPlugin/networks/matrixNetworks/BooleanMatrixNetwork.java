package org.idekerlab.PanGIAPlugin.networks.matrixNetworks;

import org.idekerlab.PanGIAPlugin.data.IntVector;
import org.idekerlab.PanGIAPlugin.data.StringVector;
import org.idekerlab.PanGIAPlugin.networks.*;
import org.idekerlab.PanGIAPlugin.networks.hashNetworks.BooleanHashNetwork;
import org.idekerlab.PanGIAPlugin.utilities.IIterator;
import org.idekerlab.PanGIAPlugin.utilities.collections.SetUtil;
import org.idekerlab.PanGIAPlugin.utilities.files.FileIterator;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;


public class BooleanMatrixNetwork extends SBNetwork
{
	private Map<String,Integer> nodeLookup;
	private List<String> nodeValues;
	private boolean[][] connectivity;

	public BooleanMatrixNetwork(boolean selfOk, boolean directed, Collection<String> nodeValues)
	{
		super(selfOk,directed);
		this.nodeValues = new ArrayList<String>(nodeValues);
		Initialize(nodeValues.size());
		InitializeMap();
	}

	public boolean isDirected()
	{
		return directed;
	}
	
	public boolean isSelfOk()
	{
		return selfOk;
	}
	
	public boolean contains(int i,int j)
	{
		if (j>i)
		{
			int temp = j;
			j = i;
			i = temp;
		}
		
		if (selfOk || i!=j)	return connectivity[i][j];
		else return false;
	}
	
	public boolean contains(String n1, String n2)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);
		
		if (i1==null || i2==null) return false;
		
		return contains(i1,i2);
	}
	
	public boolean contains(SEdge e)
	{
		return contains(e.getI1(),e.getI2());
	}
	
	public int indexOf(String value)
	{
		Integer i = nodeLookup.get(value); 
		if (i==null) return -1;
		else return i;
	}
	
	public void add(String n1, String n2)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);
		
		this.add(i1,i2);
	}
	
	public void addAll(SNetwork n)
	{
		for (SEdge i : n.edgeIterator())
			this.add(i);
	}
	
	public void add(SEdge i)
	{
		Integer i1 = nodeLookup.get(i.getI1());
		Integer i2 = nodeLookup.get(i.getI2());
		
		this.add(i1,i2);
	}
	
	public void remove(SEdge i)
	{
		Integer i1 = nodeLookup.get(i.getI1());
		Integer i2 = nodeLookup.get(i.getI2());
		
		if (i1==null || i2==null) return;
		
		this.remove(i1,i2);
	}

	public void remove(String n1, String n2)
	{
		Integer i1 = nodeLookup.get(n1);
		Integer i2 = nodeLookup.get(n2);
		
		this.remove(i1,i2);
	}

	public void add(int n1, int n2)
	{
		if (!directed && n2>n1)
		{
			int temp = n1;
			n1 = n2;
			n2 = temp;
		}
		
		if (selfOk || n1!=n2) this.connectivity[n1][n2] = true;
	}
	
	public void remove(int n1, int n2)
	{
		if (n1<0 || n2<0) return;
		
		if (!directed && n2>n1)
		{
			int temp = n1;
			n1 = n2;
			n2 = temp;
		}
		
		if (selfOk || n1!=n2) this.connectivity[n1][n2] = false;
	}
	
	public String getNodeValue(int i)
	{
		return this.nodeValues.get(i);
	}
	
	public static BooleanMatrixNetwork allVsAll(Collection<String> nodes)
	{
		BooleanMatrixNetwork out = new BooleanMatrixNetwork(false,false,nodes);		
		out.fill();
		
		return out;
	}

	private void Initialize(int size)
	{
		if (directed)
		{
			connectivity = new boolean[size][size];
			
			for (int i=0;i<size;i++)
				for (int j=0;j<size;j++)
					connectivity[i][j] = false;
		}
		else
		{
			connectivity = new boolean[size][];
			
			for (int i=0;i<size;i++)
			{
				if (selfOk) connectivity[i] = new boolean[i+1];
				else connectivity[i] = new boolean[i];
				
				for (int j=0;j<connectivity[i].length;j++)
					connectivity[i][j] = false;
			}
		}
	}
	
	private void InitializeMap()
	{
		this.nodeLookup = new HashMap<String,Integer>(nodeValues.size());
		for (int i=0;i<nodeValues.size();i++)
			nodeLookup.put(nodeValues.get(i), i);
	}

	public int numNodes()
	{
		return this.nodeValues.size();
	}
	
	public int numEdges()
	{
		int count = 0;
		for (int i=0;i<connectivity.length;i++)
			for (int j=0;j<connectivity[i].length;j++)
				if (connectivity[i][j]) count++;
		return count;
	}


	public void save(String file)
	{
		// Open file stream
		FileWriter fw = null;
		try 
		{
			fw = new FileWriter(file);
		} 
		catch (FileNotFoundException e) 
		{
			System.out.println(e.getMessage());
			System.out.println("Error MNetwork.Save(String)");
			System.exit(0);
		} 
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
			System.out.println("Error MNetwork.Save(String)");
			System.exit(0);
		}

		// Write interactions to file
		BufferedWriter bw = new BufferedWriter(fw);
		
		
		for (int i=0;i<this.connectivity.length;i++)
		{
			String nodeI = this.getNodeValue(i);
			for (int j=0;j<this.connectivity[i].length;j++)
			{
				//System.out.println(i+","+j+"  | "+this.connectivity.length+","+this.connectivity[i].length);
				
				if (this.connectivity[i][j])
				{
					try 
					{
						bw.write(nodeI+"\t"+this.getNodeValue(j)+"\n");
					} 
					catch (IOException e) 
					{
						System.out.println(e.getMessage());
						System.exit(0);
					}
				}
			}
		}
		
		// Close writer
		try 
		{
			bw.close();
		} 
		catch (IOException e) 
		{
			System.out.println(e.getMessage());
			System.out.println("Error MNetwork.Save(String)");
			System.exit(0);
		}
	}

	/**
	 * Completely connects the matrix.
	 */
	public void fill()
	{
		for (int i=0;i<connectivity.length;i++)
			for (int j=0;j<connectivity[i].length;j++)
				connectivity[i][j] = true;
	}

	public boolean containsNode(String node)
	{
		return nodeLookup.get(node)!=null;
	}
	
	/**
	 * Caution: For speed, this returns the actual reference, not a copy. Do not modify it.
	 */
	public List<String> nodeValues()
	{
		return nodeValues;
	}
	
	public Set<String> getNodes()
	{
		return new HashSet<String>(nodeValues);
	}

	public String toString()
	{
		String out = "[";
		
		for (int i=0;i<connectivity.length;i++)
		{
			for (int j=0;j<connectivity[i].length;j++)
				if (this.contains(i, j))
				{
					if (out.length()!=1) out += ",";
					out+=this.nodeValues().get(i)+"-"+this.nodeValues().get(j);
					
					if (out.length()>2000)
					{
						out+= ",...";
						break;
					}
				}
				
			if (out.length()>2000) break;
		}
		
		
		out+= "]";
		
		return out;
	}
	
	public BooleanMatrixNetwork subNetworkExclusive(Set<String> nodes)
	{
		BooleanMatrixNetwork out = new BooleanMatrixNetwork(this.selfOk, this.directed, SetUtil.intersect(nodes, this.nodeValues()));
		
		for (int i=0;i<connectivity.length;i++)
			if (out.containsNode(this.nodeValues.get(i)))
					for (int j=0;j<connectivity[i].length;j++)
						if (out.containsNode(this.nodeValues.get(j)) && this.contains(i, j)) out.add(this.getNodeValue(i),this.getNodeValue(j));
		
		return out;
				
	}
	
	public BooleanMatrixNetwork shuffleNodes()
	{
		StringVector nodes = new StringVector(this.nodeValues);
		StringVector rnodes = nodes.sample(nodes.size(), false);
		Map<String,String> rnodeMap = new HashMap<String,String>(rnodes.size(),1); 
		
		for (int i=0;i<rnodes.size();i++)
			rnodeMap.put(nodes.get(i), rnodes.get(i));
		
		BooleanMatrixNetwork out = new BooleanMatrixNetwork(this.selfOk,this.directed,rnodes.asStringList());
		for (int i=0;i<this.connectivity.length;i++)
			for (int j=0;j<this.connectivity[i].length;j++)
				if (this.connectivity[i][j]) out.add(i, j);
		
		return out;
	}
	
	public IIterator<String> nodeIterator()
	{
		return new IIterator<String>(this.nodeValues.iterator());
	}
	
	public IIterator<SEdge> edgeIterator()
	{
		throw new UnsupportedOperationException("Matrix networks do not currently support edge iteration.");
	}
	
	public Set<SEdge> getEdges()
	{
		throw new UnsupportedOperationException("Matrix networks do not currently support getEdges().");
	}

}


