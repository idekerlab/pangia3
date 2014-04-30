package org.idekerlab.PanGIAPlugin.data;

public class FloatVector extends DataVector
{

	private float[] data;
	private int size;

	public FloatVector()
	{
		Initialize(0);
	}

	public FloatVector(double[] data)
	{
		this.data = new float[data.length];

		for (int i = 0; i < data.length; i++)
			this.data[i] = (float) data[i];

		size = data.length;
	}

	public FloatVector(float[] data)
	{
		this.data = data;

		this.size = data.length;
	}

	public FloatVector(int size)
	{
		Initialize(size);
	}

	public void Initialize(int size)
	{
		data = new float[size];
		this.size = 0;
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

	public static float[] resize(float[] vec, int size)
	{
		float[] out = new float[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	public synchronized void add(float o)
	{
		if (data.length == 0)
			data = new float[10];
		else if (this.size == data.length)
			data = FloatVector.resize(data, data.length * 2);

		data[size] = o;
		size++;
	}

	public synchronized void add(double o)
	{
		if (data.length == 0)
			data = new float[10];
		else if (this.size == data.length)
			data = FloatVector.resize(data, data.length * 2);

		data[size] = (float) o;
		size++;
	}

	public synchronized void add(String val)
	{
		this.add(Double.valueOf(val));
	}

	public Object getAsObject(int i)
	{
		return data[i];
	}

	public String getAsString(int i)
	{
		return Float.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		return get(i);
	}

	public byte getAsByte(int i)
	{
		return new Float(get(i)).byteValue();
	}

	public int getAsInteger(int i)
	{
		return (int) get(i);
	}

	public float getAsFloat(int i)
	{
		return get(i);
	}

	public float get(int i)
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

	public static float[] copy(float[] vec)
	{
		float[] out = new float[vec.length];

		System.arraycopy(vec, 0, out, 0, vec.length);

		return out;
	}
}
