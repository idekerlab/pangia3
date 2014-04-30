package org.idekerlab.PanGIAPlugin.networks;

import org.idekerlab.PanGIAPlugin.networks.hashNetworks.BooleanHashNetwork;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class SNodeModule implements Iterable<String>, Comparable<SNodeModule>
{

	private String id;
	private Set<String> members = new HashSet<String>();

	public SNodeModule(String name, Collection<String> members)
	{
		this.id = name;
		this.members = new HashSet<String>(members);
	}

	public SNodeModule clone()
	{
		return new SNodeModule(this.id, new HashSet<String>(this.members));
	}

	public boolean equals(Object c)
	{
		if (c == null) return false;
		if (c instanceof SNodeModule)
		{
			SNodeModule other = (SNodeModule) c;
			return other.id.equals(this.id) && other.members.equals(this.members);
		}
		else return false;
	}

	public int hashCode()
	{
		return members.hashCode();
	}

	public Set<String> getMemberData()
	{
		return members;
	}

	public String getID()
	{
		return id;
	}

	public String toString()
	{
		return id;
	}

	public Iterator<String> iterator()
	{
		return members.iterator();
	}

	public int size()
	{
		return members.size();
	}

	public boolean contains(SNodeModule complex)
	{
		for (String member : complex.members)
			if (!this.members.contains(member)) return false;

		return true;
	}

	public void add(String g)
	{
		this.members.add(g);
	}

	public void add(SNodeModule c)
	{
		for (String s : c.members)
			this.add(s);
	}

	public int compareTo(SNodeModule other)
	{
		return this.members.size() - other.members.size();
	}


	/**
	 * Creates a network object which is a complete graph between all complex members
	 *
	 * @return Network that is a complex graph between all complex members
	 */
	public SBNetwork asNetwork()
	{
		SBNetwork net = new BooleanHashNetwork(false, false);

		for (String s1 : members)
			for (String s2 : members)
			{
				if (s1.equals(s2)) continue;
				net.add(new UndirectedSEdge(s1, s2));
			}

		return net;
	}


	public void remove(String member)
	{
		this.members.remove(member);
	}

	public boolean contains(String member)
	{
		return members.contains(member);
	}
}
