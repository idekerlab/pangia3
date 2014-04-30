package org.idekerlab.PanGIAPlugin.data;

import org.idekerlab.PanGIAPlugin.util.RandomFactory;

import java.util.*;


public class StringVector extends DataVector implements Iterable<String>
{

	private List<String> data;

	public StringVector()
	{
		data = new ArrayList<String>();
	}

	public StringVector(Collection<String> vals)
	{
		Initialize(vals.size());

		for (String s : vals)
			data.add(s);
	}

	public StringVector(int size)
	{
		Initialize(size);
	}

	public void Initialize(int size)
	{
		data = new ArrayList<String>(size);
	}

	public void Initialize(int size, String value)
	{
		for (int i = 0; i < size; i++)
			data.add(value);
	}

	public String[] asStringArray()
	{
		String[] da = new String[size()];

		for (int i = 0; i < data.size(); i++)
			da[i] = get(i);

		return da;
	}

	public Object getAsObject(int i)
	{
		return data.get(i);
	}

	public String getAsString(int i)
	{
		return data.get(i);
	}

	public double getAsDouble(int i)
	{
		return Double.valueOf(get(i));
	}

	public byte getAsByte(int i)
	{
		return Byte.valueOf(get(i));
	}

	public int getAsInteger(int i)
	{
		return Integer.valueOf(get(i));
	}

	public float getAsFloat(int i)
	{
		return Float.valueOf(get(i));
	}

	protected Object getDataAsObject()
	{
		return data;
	}

	public String get(int i)
	{
		return (data.get(i));
	}

	public StringVector get(IntVector iv)
	{
		StringVector out = new StringVector(iv.size());

		for (int i = 0; i < iv.size(); i++)
			out.add(this.get(iv.get(i)));

		return out;
	}

	public String get(String element)
	{
		return (data.get(getElementNames().indexOf(element)));
	}

	public void set(String[] newData)
	{
		this.data = new ArrayList<String>(newData.length);

		Collections.addAll(this.data, newData);
	}

	private void set(int i, String val)
	{
		data.set(i, val);
	}

	public synchronized void add(String toAdd)
	{
		data.add(toAdd);
	}

	public void add(int val)
	{
		data.add("" + val);
	}

	public void add(double val)
	{
		data.add("" + val);
	}

	public void set(String element, String val)
	{
		data.set(getElementNames().indexOf(element), val);
	}

	public StringVector sample(int samplesize, boolean replace)
	{
		StringVector mysample = new StringVector(samplesize);

		StringVector cp = this.clone();

		Random randgen = RandomFactory.make();

		if (!replace)
		{
			int lsizem1 = cp.size() - 1;

			for (int i = 0; i < samplesize; i++)
			{
				int swapi = lsizem1 - randgen.nextInt(cp.size() - i);
				String temp = cp.get(i);
				cp.set(i, cp.get(swapi));
				cp.set(swapi, temp);
			}


			return cp.subVector(0, samplesize);
		}
		else
		{
			for (int r = 0; r < samplesize; r++)
			{
				int rand = randgen.nextInt(cp.size());
				mysample.add(cp.get(rand));
			}
		}

		return mysample;
	}

	public StringVector subVector(int i1, int size)
	{
		StringVector out = new StringVector(size);

		int i1s = i1 + size;

		for (int i = i1; i < i1s; i++)
			out.add(this.get(i));

		return out;
	}

	public StringVector clone()
	{
		StringVector copy = new StringVector();

		if (this.hasListName()) copy.listname = this.listname;
		if (this.hasElementNames()) copy.setElementNames(new ArrayList<String>(this.getElementNames()));

		int size = data.size();

		copy.data = new ArrayList<String>(size);

		for (int i = 0; i < size; i++)
			copy.data.add(get(i));

		return (copy);
	}

	public int size()
	{
		return data.size();
	}

	public Iterator<String> iterator()
	{
		return data.iterator();
	}

	public StringVector sort()
	{
		StringVector out;

		if (!this.hasElementNames())
		{
			out = this.clone();
			String[] mydata = out.asStringArray();
			Arrays.sort(mydata);
			out.set(mydata);
		}
		else
		{
			out = new StringVector(this.size());
			if (this.hasListName()) out.setListName(this.listname);

			IntVector sorti = this.sort_I();
			List<String> rownames = new ArrayList<String>(this.size());
			for (int i = 0; i < sorti.size(); i++)
			{
				out.add(this.get(sorti.get(i)));
				rownames.add(this.getElementName(sorti.get(i)));
			}
			out.elementnames = rownames;
		}

		return out;
	}

	public IntVector sort_I()
	{
		return Sorter.Sort_I(this.clone());
	}

	public void remove(int index)
	{
		this.data.remove(index);
	}

	public void addAll(StringVector sv)
	{
		for (String s : sv)
			this.add(s);
	}

	public StringVector get(BooleanVector bv)
	{
		StringVector out = new StringVector(bv.sum());

		for (int i = 0; i < bv.size(); i++)
			if (bv.get(i)) out.add(this.get(i));

		return out;
	}

	public int indexOf(String s)
	{
		return data.indexOf(s);
	}

}
