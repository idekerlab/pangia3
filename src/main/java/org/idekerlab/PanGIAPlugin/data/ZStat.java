package org.idekerlab.PanGIAPlugin.data;

public final class ZStat {

	private final double sd;
	private final double mn;

	public ZStat(double mn, double sd)
	{
		this.sd = sd;
		this.mn = mn;
	}
	
	public final double getSD()
	{
		return sd;
	}
	
	public final double getMean()
	{
		return mn;
	}
	
}
