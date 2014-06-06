package org.idekerlab.PanGIAPlugin.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ByteVector extends DataVector
{

	private byte[] data;
	private int size;

	private ByteVector()
	{
		Initialize(0);
	}

	private ByteVector(byte[] data)
	{
		this.data = data;
		this.size = data.length;
	}

	private ByteVector(int size)
	{
		Initialize(size);
	}

	protected void Initialize(int size)
	{
		data = new byte[size];
		this.size = 0;
	}

	protected void Initialize(int count, byte val)
	{
		data = new byte[count];
		size = count;

		for (int i = 0; i < count; i++)
			data[i] = val;
	}

	public void Initialize(int count, int val)
	{
		Initialize(count, (byte) val);
	}

	private static byte[] resize(byte[] vec, int size)
	{
		byte[] out = new byte[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	protected void add(byte val)
	{
		if (data.length == 0)
			data = new byte[10];
		else if (this.size == data.length)
			data = ByteVector.resize(data, data.length * 2);

		data[size] = val;
		size++;
	}

	public void add(int val)
	{
		this.add((byte) val);
	}

	public void add(String val)
	{
		this.add(Byte.valueOf(val));
	}

	public synchronized void addAll(byte[] vals)
	{
		if (data.length < this.size + vals.length)
			data = ByteVector.resize(data, data.length + vals.length);

		for (Byte d : vals)
			this.add(d);
	}

	public synchronized void addAll(Collection<Byte> vals)
	{
		if (data.length < this.size + vals.size())
			data = ByteVector.resize(data, data.length + vals.size());

		for (Byte d : vals)
			this.add(d);
	}

	public synchronized void addAll(ByteVector vals)
	{
		if (data.length < this.size + vals.size())
			data = ByteVector.resize(data, data.length + vals.size());

		for (int i = 0; i < vals.size; i++)
			this.add(vals.get(i));
	}

	protected void add(byte Byte, String name)
	{
		this.add(Byte);
		addElementName(name);
	}

	protected Object getDataAsObject()
	{
		return data;
	}

	public String getAsString(int i)
	{
		return Byte.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		return get(i);
	}

	protected byte get(int i)
	{
		return data[i];
	}

	public byte get(String element)
	{
		return data[getElementNames().indexOf(element)];
	}

	protected void set(int i, byte val)
	{
		data[i] = val;
	}

	protected void set(int i, int val)
	{
		data[i] = (byte) val;
	}

	public void set(List<Integer> indices, byte val)
	{
		for (Integer index : indices)
			data[index] = val;
	}

	public void set(String element, byte val)
	{
		data[getElementNames().indexOf(element)] = val;
	}

	public ByteVector clone()
	{
		ByteVector copy = new ByteVector(ByteVector.copy(this.data));

		if (this.hasListName())
			copy.setListName(this.getListName());
		if (this.hasElementNames())
			copy.setElementNames(this.getElementNames());

		return copy;
	}

	private static byte[] copy(byte[] data)
	{
		byte[] out = new byte[data.length];
		System.arraycopy(data, 0, out, 0, data.length);

		return out;
	}

	public byte[] getData()
	{
		if (size == data.length)
			return data;
		else
			return ByteVector.resize(data, this.size);
	}

	public int size()
	{
		return size;
	}

	public ByteVector reZero(byte zero)
	{
		ByteVector out = this.clone();

		for (int i = 0; i < data.length; i++)
			if (out.get(i) == 0)
				out.set(i, zero);

		return out;
	}

	public int max()
	{
		if (size() == 0)
			return -1;

		int max = data[0];
		for (int i = 1; i < data.length; i++)
			if (data[i] > max)
				max = data[i];
		return max;
	}

	public int min()
	{
		if (size() == 0)
			return -1;

		int min = data[0];
		for (byte item : data)
			if (item < min)
				min = item;
		return min;
	}

	public ByteVector abs()
	{
		ByteVector out = this.clone();

		for (int i = 0; i < size(); i++)
			if (out.get(i) < 0)
				out.set(i, -out.get(i));

		return out;
	}

	public ByteVector negative()
	{
		ByteVector out = this.clone();

		for (int i = 0; i < size(); i++)
			out.set(i, -out.get(i));

		return out;
	}

	public boolean isNaN()
	{
		for (int i = 0; i < size(); i++)
			if (!Double.isNaN(data[i]))
				return false;

		return true;
	}


	public double mean()
	{
		double sum = 0.0;
		int valcount = 0;

		for (int i = 0; i < size(); i++)
			if (!Double.isNaN(data[i]))
			{
				sum += data[i];
				valcount++;
			}

		if (valcount == 0)
			return Double.NaN;

		return sum / (valcount);
	}

	public int sum()
	{
		int sum = 0;
		int valcount = 0;

		for (int i = 0; i < size(); i++)
			if (!Double.isNaN(data[i]))
			{
				sum += data[i];
				valcount++;
			}

		if (valcount == 0)
			return 0;

		return sum;
	}

	public ByteVector pow(double power)
	{
		ByteVector pdt = this.clone();

		for (int i = 0; i < data.length; i++)
			pdt.set(i, (int) Math.pow(get(i), power));

		return pdt;
	}

	public ByteVector get(List<?> indexes)
	{
		ByteVector sub = new ByteVector(indexes.size());

		for (Object indexe : indexes)
			sub.add(data[(((Double) indexe).intValue())]);

		return sub;
	}

	public ByteVector get(int[] indexes)
	{
		ByteVector sub = new ByteVector(indexes.length);

		for (int index : indexes)
			sub.add(data[index]);

		return sub;
	}

	public ByteVector get(ByteVector indexes)
	{
		ByteVector sub = new ByteVector(indexes.size());

		for (int i = 0; i < indexes.size(); i++)
			sub.add(data[indexes.get(i)]);

		return sub;
	}

	public ByteVector get(BooleanVector bv)
	{
		if (bv.size() != this.size())
		{
			System.err
					.println("Error DoubleVector.get(BooleanVector): The two vectors must be the same size.");
			System.err.println("this.size = " + this.size() + ", bvsize = "
					+ bv.size());
			System.exit(0);
		}

		ByteVector sub = new ByteVector();

		boolean found = false;
		if (this.elementnames != null)
		{
			sub.setElementNames(new ArrayList<String>());

			for (int i = 0; i < size(); i++)
				if (bv.get(i))
				{
					sub.add(this.get(i), this.getElementName(i));
					found = true;
				}
		}
		else
		{
			for (int i = 0; i < size(); i++)
				if (bv.get(i))
				{
					sub.add(this.get(i));
					found = true;
				}
		}

		if (!found)
			sub.removeElementNames();

		return sub;
	}

	public void set(BooleanVector bv, int val)
	{
		for (int i = 0; i < size(); i++)
			if (bv.get(i))
				set(i, val);
	}


	public ByteVector reverse()
	{
		ByteVector out = new ByteVector(this.size());

		for (int i = this.size() - 1; i >= 0; i--)
			out.add(this.get(i));

		return out;
	}

	public boolean allEqualTo(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) != val)
				return false;

		return true;
	}

	public boolean allEqualTo(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) != val)
				return false;

		return true;
	}

	public boolean allEqualTo(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) != val)
				return false;

		return true;
	}

	public boolean allEqualTo(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) != val)
				return false;

		return true;
	}

	public boolean noneEqualTo(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return false;

		return true;
	}

	public boolean noneEqualTo(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return false;

		return true;
	}

	public boolean noneEqualTo(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return false;

		return true;
	}

	public boolean noneEqualTo(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return false;

		return true;
	}

	public boolean anyEqualTo(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return true;

		return false;
	}

	public boolean anyEqualTo(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return true;

		return false;
	}

	public boolean anyEqualTo(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return true;

		return false;
	}

	public boolean anyEqualTo(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) == val)
				return true;

		return false;
	}

	public boolean allLessThan(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) >= val)
				return false;

		return true;
	}

	public boolean allLessThan(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) >= val)
				return false;

		return true;
	}

	public boolean allLessThan(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) >= val)
				return false;

		return true;
	}

	public boolean allLessThan(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) >= val)
				return false;

		return true;
	}

	public boolean allGreaterThan(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) <= val)
				return false;

		return true;
	}

	public boolean allGreaterThan(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) <= val)
				return false;

		return true;
	}

	public boolean allGreaterThan(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) <= val)
				return false;

		return true;
	}

	public boolean allGreaterThan(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) <= val)
				return false;

		return true;
	}

	public boolean anyLessThan(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) < val)
				return true;

		return false;
	}

	public boolean anyLessThan(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) < val)
				return true;

		return false;
	}

	public boolean anyLessThan(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) < val)
				return true;

		return false;
	}

	public boolean anyLessThan(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) < val)
				return true;

		return false;
	}

	public boolean anyGreaterThan(double val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) > val)
				return true;

		return false;
	}

	public boolean anyGreaterThan(float val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) > val)
				return true;

		return false;
	}

	public boolean anyGreaterThan(int val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) > val)
				return true;

		return false;
	}

	public boolean anyGreaterThan(byte val)
	{
		for (int i = 0; i < size(); i++)
			if (get(i) > val)
				return true;

		return false;
	}

	public BooleanVector equalTo(double val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) == val);

		return out;
	}

	public BooleanVector equalTo(int val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) == val);

		return out;
	}

	public BooleanVector equalTo(float val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) == val);

		return out;
	}

	public BooleanVector equalTo(byte val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) == val);

		return out;
	}

	public BooleanVector notEqualTo(double val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) != val);

		return out;
	}

	public BooleanVector notEqualTo(int val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) != val);

		return out;
	}

	public BooleanVector notEqualTo(float val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) != val);

		return out;
	}

	public BooleanVector notEqualTo(byte val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) != val);

		return out;
	}

	public BooleanVector lessThanOrEqual(float val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) <= val);

		return out;
	}

	public BooleanVector lessThanOrEqual(int val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) <= val);

		return out;
	}

	public BooleanVector lessThanOrEqual(byte val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) <= val);

		return out;
	}

	public BooleanVector lessThanOrEqual(double val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) <= val);

		return out;
	}

	public BooleanVector greaterThan(double val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) > val);

		return out;
	}

	public BooleanVector greaterThan(int val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) > val);

		return out;
	}

	public BooleanVector greaterThan(float val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) > val);

		return out;
	}

	public BooleanVector greaterThan(byte val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) > val);

		return out;
	}

	public BooleanVector lessThan(double val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) < val);

		return out;
	}

	public BooleanVector lessThan(int val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) < val);

		return out;
	}

	public BooleanVector lessThan(float val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) < val);

		return out;
	}

	public BooleanVector lessThan(byte val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) < val);

		return out;
	}

	public BooleanVector greaterThanOrEqual(double val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) >= val);

		return out;
	}

	public BooleanVector greaterThanOrEqual(int val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) >= val);

		return out;
	}

	public BooleanVector greaterThanOrEqual(float val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) >= val);

		return out;
	}

	public BooleanVector greaterThanOrEqual(byte val)
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(get(i) >= val);

		return out;
	}

}
