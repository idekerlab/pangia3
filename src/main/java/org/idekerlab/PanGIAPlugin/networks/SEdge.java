package org.idekerlab.PanGIAPlugin.networks;

public abstract class SEdge extends AbstractEdge
{

	protected final String i1;
	protected final String i2;

	public abstract boolean equals(Object o);

	protected SEdge(String s1, String s2)
	{
		i1 = s1;
		i2 = s2;
	}


	public String getI1()
	{
		return i1;
	}

	public String getI2()
	{
		return i2;
	}

	@Override
	public int hashCode()
	{
		return (i1.hashCode() + i2.hashCode());
	}

	@Override
	public String toString()
	{
		return (i1 + '\t' + i2);
	}


	/**
	 * Is this interaction a self-interaction?
	 *
	 * @return <i>true</i> if interaction is a self-interaction
	 */
	public boolean isSelf()
	{
		return (i1.equals(i2));
	}
}
