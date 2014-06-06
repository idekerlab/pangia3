package org.idekerlab.PanGIAPlugin;


public enum ScalingMethod
{
	NONE("none (prescaled)"),
	LINEAR_LOWER("lower"),
	LINEAR_UPPER("upper");

	private final String displayString;

	ScalingMethod(final String displayString)
	{
		this.displayString = displayString;
	}

	public String getDisplayString()
	{
		return displayString;
	}

	static public ScalingMethod getEnumValue(final String displayString)
	{
		for (final ScalingMethod method : ScalingMethod.values())
		{
			if (method.displayString.equals(displayString))
				return method;
		}

		throw new IllegalStateException("unknown string representation: \"" + displayString + "\"!");
	}
}
