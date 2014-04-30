package org.idekerlab.PanGIAPlugin.networks;

public final class DirectedSDEdge extends SDEdge{

	public DirectedSDEdge(SDEdge inter)
	{
		super(inter);
	}
	
	public DirectedSDEdge(String s1, String s2, double value)
	{
		super(s1,s2,value);
	}

	public boolean isDirected()
	{
		return true;
	}
	
	public boolean equals(Object inter)
	{
		if (inter == null) return false;
		if (inter instanceof SEdge)
		{
			SEdge other = (SEdge)inter;
			return i1.equals(other.i1) && i2.equals(other.i2);
		}else return false;
	}

}
