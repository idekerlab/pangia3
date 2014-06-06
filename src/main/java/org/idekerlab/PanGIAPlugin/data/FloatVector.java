package org.idekerlab.PanGIAPlugin.data;

public class FloatVector extends DataVector
{

	private final float[] data;
	private int size;

	public FloatVector(double[] data)
	{
		this.data = new float[data.length];

		for (int i = 0; i < data.length; i++)
			this.data[i] = (float) data[i];

		size = data.length;
	}

	private FloatVector(float[] data)
	{
		this.data = data;

		this.size = data.length;
	}

	protected Object getDataAsObject()
	{
		return data;
	}

	/**
	 * Often gets the reference to the actual data.
	 */
	public float[] getData()
	{
		if (size == data.length)
			return data;
		else
			return FloatVector.resize(data, this.size);
	}

	private static float[] resize(float[] vec, int size)
	{
		float[] out = new float[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	public String getAsString(int i)
	{
		return Float.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		return get(i);
	}

	protected float get(int i)
	{
		return (data[i]);
	}

	public FloatVector clone()
	{
		FloatVector copy = new FloatVector(FloatVector.copy(data));
		copy.size = this.size;

		if (this.hasListName())
			copy.setListName(this.getListName());
		if (this.hasElementNames())
			copy.setElementNames(this.getElementNames());

		return (copy);
	}

	public int size()
	{
		return size;
	}

	private static float[] copy(float[] vec)
	{
		float[] out = new float[vec.length];

		System.arraycopy(vec, 0, out, 0, vec.length);

		return out;
	}
}
