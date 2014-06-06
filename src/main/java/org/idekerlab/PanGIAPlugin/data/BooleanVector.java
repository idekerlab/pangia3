package org.idekerlab.PanGIAPlugin.data;

import java.util.ArrayList;


public class BooleanVector extends DataVector
{

	private boolean[] data;
	private int size;

	public BooleanVector(boolean[] data)
	{
		this.data = data;
		this.size = data.length;
	}

	public BooleanVector(int size)
	{
		Initialize(size);
	}

	protected void Initialize(int size)
	{
		data = new boolean[size];
		this.size = 0;
	}

	protected Object getDataAsObject()
	{
		return data;
	}

	protected boolean[] getData()
	{
		if (size == data.length)
			return data;
		else
			return BooleanVector.resize(data, this.size);
	}

	private static boolean[] resize(boolean[] vec, int size)
	{
		boolean[] out = new boolean[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	public void add(boolean val)
	{
		if (data.length == 0)
			data = new boolean[10];
		else if (this.size == data.length)
			data = BooleanVector.resize(data, data.length * 2);

		data[size] = val;
		size++;
	}

	public String getAsString(int i)
	{
		return Boolean.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		if (get(i))
			return 1;
		else
			return 0;
	}

	public Boolean get(int i)
	{
		return data[i];
	}

	protected void set(int i, Boolean val)
	{
		data[i] = val;
	}

	public BooleanVector clone()
	{
		BooleanVector copy = new BooleanVector(BooleanVector.copy(this.data));

		if (this.hasListName())
			copy.setListName(this.getListName());
		if (this.hasElementNames())
			copy.setElementNames(new ArrayList<String>(this.getElementNames()));

		return copy;
	}

	private static boolean[] copy(boolean[] data)
	{
		boolean[] out = new boolean[data.length];
		System.arraycopy(data, 0, out, 0, data.length);

		return out;
	}

	public int size()
	{
		return size;
	}

	public BooleanVector not()
	{
		BooleanVector out = this.clone();

		for (int i = 0; i < size(); i++)
			out.set(i, !out.get(i));

		return out;
	}

	public int sum()
	{
		return BooleanVector.sum(getData());
	}

	private static int sum(boolean[] data)
	{
		int mysum = 0;

		for (boolean item : data)
			if (item)
				mysum++;

		return mysum;
	}

	private static int[] asIndexes(boolean[] data)
	{
		int[] out = new int[BooleanVector.sum(data)];

		int j = 0;
		for (int i = 0; i < data.length; i++)
			if (data[i])
			{
				out[j] = i;
				j++;
			}

		return out;
	}


	public IntVector asIndexes()
	{
		return new IntVector(asIndexes(getData()));
	}

	public boolean anyEqualTo(boolean val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return true;

		return false;
	}
}
