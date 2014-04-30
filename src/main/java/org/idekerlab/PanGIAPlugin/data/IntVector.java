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

	public void Initialize(int size)
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

	public void add(String val)
	{
		this.add(Integer.valueOf(val));
	}

	public static int[] resize(int[] vec, int size)
	{
		int[] out = new int[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	public Object getAsObject(int i)
	{
		return data[i];
	}

	public String getAsString(int i)
	{
		return Integer.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		return get(i);
	}

	public byte getAsByte(int i)
	{
		return (byte) get(i);
	}

	public int getAsInteger(int i)
	{
		return get(i);
	}

	public float getAsFloat(int i)
	{
		return get(i);
	}

	public int get(int i)
	{
		return (data[i]);
	}

	public void set(int i, int val)
	{
		data[i] = val;
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

	public static int[] copy(int[] vec)
	{
		int[] out = new int[vec.length];

		System.arraycopy(vec, 0, out, 0, vec.length);

		return out;
	}

	public int size()
	{
		return this.size;
	}

	public IntVector subVector(int i1, int size)
	{
		IntVector out = new IntVector(size);

		int i1s = i1 + size;

		for (int i = i1; i < i1s; i++)
			out.add(this.get(i));

		return out;
	}

	public IntVector sort()
	{
		IntVector out = this.clone();
		Sorter.Sort_I(out);

		return out;
	}

	public void set(int[] iarray)
	{
		data = IntVector.copy(iarray);
		this.size = iarray.length;
	}


	public int indexOf(int val)
	{
		for (int i = 0; i < this.size; i++)
			if (data[i] == val)
				return i;

		return -1;
	}

	public void removeElement(int value)
	{
		for (int i = 0; i < data.length; i++)
		{
			if (data[i] == value)
			{
				this.removeElementAt(i);
				break;
			}
		}
	}

	public void removeElementAt(int index)
	{
		int[] newdata = new int[data.length - 1];

		System.arraycopy(data, 0, newdata, 0, index);

		System.arraycopy(data, index + 1, newdata, index + 1 - 1, data.length - (index + 1));
	}
}
