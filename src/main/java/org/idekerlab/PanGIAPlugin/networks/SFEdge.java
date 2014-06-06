package org.idekerlab.PanGIAPlugin.networks;

public abstract class SFEdge extends SEdge
{

	private final float value;

	protected SFEdge(String s1, String s2, float value)
	{
		super(s1, s2);
		this.value = value;
	}

	public float value()
	{
		return value;
	}

}
