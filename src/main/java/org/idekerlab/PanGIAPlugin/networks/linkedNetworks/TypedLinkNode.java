package org.idekerlab.PanGIAPlugin.networks.linkedNetworks;

import org.idekerlab.PanGIAPlugin.utilities.IIterator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TypedLinkNode<NT, ET> implements Finalish
{
	private final int hc;
	private final NT value;
	private final Map<TypedLinkNode<NT, ET>, TypedLinkEdge<NT, ET>> neighbors;

	public TypedLinkNode(NT value)
	{
		this.value = value;
		neighbors = new HashMap<TypedLinkNode<NT, ET>, TypedLinkEdge<NT, ET>>();
		this.hc = value.hashCode();
	}

	public int hashCode()
	{
		return hc;
	}

	public boolean equals(Object o)
	{
		if (o instanceof TypedLinkNode)
		{
			return this.value.equals(((TypedLinkNode<?, ?>) o).value);
		}
		else
			return false;
	}

	public String toString()
	{
		return value.toString();
	}

	public IIterator<TypedLinkEdge<NT, ET>> edgeIterator()
	{
		return new IIterator<TypedLinkEdge<NT, ET>>(neighbors.values().iterator());
	}

	public NT value()
	{
		return this.value;
	}

	public TypedLinkEdge<NT, ET> getEdge(NT node)
	{
		return neighbors.get(new TypedLinkNode<NT, ET>(node));
	}

	public Set<TypedLinkEdge<NT, ET>> edges()
	{
		return new HashSet<TypedLinkEdge<NT, ET>>(neighbors.values());
	}

	public Set<TypedLinkNode<NT, ET>> neighbors()
	{
		return new HashSet<TypedLinkNode<NT, ET>>(neighbors.keySet());
	}

	public TypedLinkNodeModule<NT, ET> neighbors(int degree)
	{
		return new TypedLinkNodeModule<NT, ET>(this).neighbors(degree);
	}

	public void addNeighbor(TypedLinkNode<NT, ET> node, TypedLinkEdge<NT, ET> edge)
	{
		this.neighbors.put(node, edge);
	}

	public void removeNeighbor(TypedLinkNode<NT, ET> node)
	{
		this.neighbors.remove(node);
	}

	public int numNeighbors()
	{
		return neighbors.size();
	}
}
