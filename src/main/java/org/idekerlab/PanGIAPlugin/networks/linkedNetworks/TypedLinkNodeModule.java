package org.idekerlab.PanGIAPlugin.networks.linkedNetworks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class TypedLinkNodeModule<NT, ET> implements Iterable<TypedLinkNode<NT, ET>>, Finalish
{

	private final int hc;
	private double score;
	private final Set<TypedLinkNode<NT, ET>> nodeSet;

	public TypedLinkNodeModule(Set<TypedLinkNode<NT, ET>> nodes)
	{
		this.nodeSet = new HashSet<TypedLinkNode<NT, ET>>(nodes);
		hc = nodeSet.hashCode();
	}

	public TypedLinkNodeModule(TypedLinkNode<NT, ET> node)
	{
		this.nodeSet = single(node);
		hc = nodeSet.hashCode();
	}

	public TypedLinkNodeModule(NT node)
	{
		this.nodeSet = single(new TypedLinkNode<NT, ET>(node));
		hc = nodeSet.hashCode();
	}

	private static <ST> Set<ST> single(ST member)
	{
		Set<ST> out = new HashSet<ST>(1);
		out.add(member);
		return out;
	}

	public TypedLinkNodeModule(int size)
	{
		this.nodeSet = new HashSet<TypedLinkNode<NT, ET>>(size);
		hc = nodeSet.hashCode();
	}

	public void setScore(double score)
	{
		this.score = score;
	}

	public double score()
	{
		return this.score;
	}

	public Iterator<TypedLinkNode<NT, ET>> iterator()
	{
		return this.nodeSet.iterator();
	}

	public boolean contains(NT node)
	{
		return nodeSet.contains(node);
	}

	public int size()
	{
		return nodeSet.size();
	}


	/**
	 * Gets the total number of connections between this module and another node.
	 */
	public int getConnectedness(TypedLinkNode<NT, ?> node)
	{
		int connectedness = 0;

		for (TypedLinkNode<NT, ?> n : this)
			if (n.isConnected(node)) connectedness++;

		return connectedness;
	}

	/**
	 * Gets a set of the nodes as their actual values in the network.
	 */
	public Set<NT> getMemberValues()
	{
		Set<NT> out = new HashSet<NT>(size());

		for (TypedLinkNode<NT, ET> n : this)
			out.add(n.value());

		return out;
	}

	public String toString()
	{
		String out = "{";

		boolean first = true;

		for (TypedLinkNode<NT, ET> n : this)
		{
			if (first)
			{
				out += n.toString();
				first = false;
			}
			else
			{
				out += "," + n.toString();
			}
		}

		return out + "}";
	}

	/**
	 * Intersects the nodes in two modules.
	 */
	public static <NT, ET> TypedLinkNodeModule<NT, ET> intersect(TypedLinkNodeModule<NT, ET> m1, TypedLinkNodeModule<NT, ET> m2)
	{
		Set<TypedLinkNode<NT, ET>> outSet = new HashSet<TypedLinkNode<NT, ET>>(m1.members());
		outSet.retainAll(m2.members());

		return new TypedLinkNodeModule<NT, ET>(outSet);
	}

	/**
	 * Unions the nodes in two modules.
	 */
	public static <NT, ET> TypedLinkNodeModule<NT, ET> union(TypedLinkNodeModule<NT, ET> m1, TypedLinkNodeModule<NT, ET> m2)
	{
		Set<TypedLinkNode<NT, ET>> outSet = new HashSet<TypedLinkNode<NT, ET>>(m1.members());
		outSet.addAll(m2.members());

		return new TypedLinkNodeModule<NT, ET>(outSet);
	}

	public boolean equals(Object mod)
	{
		if (mod == null) return false;
		if (mod instanceof TypedLinkNodeModule)
		{
			TypedLinkNodeModule<?, ?> other = (TypedLinkNodeModule<?, ?>) mod;
			return other.nodeSet.equals(this.nodeSet);
		}
		else return false;
	}

	public int hashCode()
	{
		return hc;
	}

	public Set<String> asStringSet()
	{
		Set<String> out = new HashSet<String>(nodeSet.size());

		for (TypedLinkNode<NT, ET> n : this)
			out.add(n.toString());

		return out;
	}

	public Set<TypedLinkNode<NT, ET>> members()
	{
		return this.nodeSet;
	}

	public TypedLinkNodeModule<NT, ET> neighbors()
	{
		Set<TypedLinkNode<NT, ET>> outSet = new HashSet<TypedLinkNode<NT, ET>>(this.members());

		for (TypedLinkNode<NT, ET> node : this)
			outSet.addAll(node.neighbors());

		return new TypedLinkNodeModule<NT, ET>(outSet);
	}

	public TypedLinkNodeModule<NT, ET> neighbors(int degree)
	{
		if (degree < 0)
			throw new IllegalArgumentException("Error TypedLinkNodeModule.neighbors(int): Degree must be >= 0. Degree = " + degree);

		if (degree == 0) return new TypedLinkNodeModule<NT, ET>(0);

		if (degree == 1) return this.neighbors();

		TypedLinkNodeModule<NT, ET> out = this.neighbors(degree - 1);
		Set<TypedLinkNode<NT, ET>> outSet = new HashSet<TypedLinkNode<NT, ET>>(out.nodeSet);

		for (TypedLinkNode<NT, ET> node : out)
			outSet.addAll(node.neighbors());

		return new TypedLinkNodeModule<NT, ET>(outSet);
	}

}
