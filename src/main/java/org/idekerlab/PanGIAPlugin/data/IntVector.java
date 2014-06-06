package org.idekerlab.PanGIAPlugin.data;

public class IntVector extends DataVector
{

	private int[] data;
	private int size;

	public IntVector(int[] data)
	{
		this.data = data;
		this.size = data.length;
	}

	public IntVector(int size)
	{
		Initialize(size);
	}

	public int[] getData()
	{
		if (size == data.length)
			return data;
		else
			return IntVector.resize(data, this.size);
	}

	protected void Initialize(int size)
	{
		data = new int[size];
		this.size = 0;
	}

	protected Object getDataAsObject()
	{
		return data;
	}

	public void add(int integer)
	{
		if (data.length == 0)
			data = new int[10];
		else if (this.size == data.length)
			data = IntVector.resize(data, data.length * 2);

		data[size] = integer;
		size++;
	}

	private static int[] resize(int[] vec, int size)
	{
		int[] out = new int[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	public String getAsString(int i)
	{
		return Integer.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		return get(i);
	}

	public int get(int i)
	{
		return (data[i]);
	}

	public IntVector clone()
	{
		IntVector copy = new IntVector(IntVector.copy(data));
		copy.size = this.size;

		if (this.hasListName())
			copy.setListName(this.getListName());
		if (this.hasElementNames())
			copy.setElementNames(this.getElementNames());

		return (copy);
	}

	private static int[] copy(int[] vec)
	{
		int[] out = new int[vec.length];

		System.arraycopy(vec, 0, out, 0, vec.length);

		return out;
	}

	public int size()
	{
		return this.size;
	}


}
