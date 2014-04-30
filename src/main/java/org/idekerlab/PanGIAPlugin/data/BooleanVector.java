package org.idekerlab.PanGIAPlugin.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class BooleanVector extends DataVector {

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

	public void Initialize(int size)
	{
		data = new boolean[size];
		this.size = 0;
	}
	
	public void Initialize(int count, boolean val)
	{
		data = new boolean[count];
		size = count;
		
		for (int i=0;i<count;i++)
			data[i] = val;
	}
	
	protected Object getDataAsObject()
	{
		return data;
	}
	
	public boolean[] getData()
	{
		if (size==data.length) return data;
		else return BooleanVector.resize(data, this.size);
	}
	
	public static boolean[] resize(boolean[] vec, int size)
	{
		boolean[] out = new boolean[size];
		
		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);
		
		return out;
	}
	
	public void add(boolean val)
	{
		if (data.length==0) data = new boolean[10];
		else if (this.size==data.length)	data = BooleanVector.resize(data,data.length*2);
		
		data[size] = val;
		size++;
	}
	
	public void add(String val)
	{
		this.add(Boolean.valueOf(val));
	}
	
	public void addAll(boolean[] vals)
	{
		if (data.length<this.size+vals.length) data = BooleanVector.resize(data,data.length+vals.length);
		
		for (Boolean d : vals)
			this.add(d);
	}
	
	public void addAll(Collection<Boolean> vals)
	{
		if (data.length<this.size+vals.size()) data = BooleanVector.resize(data,data.length+vals.size());
		
		for (Boolean d : vals)
			this.add(d);
	}
	
	public void addAll(BooleanVector vals)
	{
		if (data.length<this.size+vals.size()) data = BooleanVector.resize(data,data.length+vals.size());
		
		for (int i=0;i<vals.size();i++)
			this.add(vals.get(i));
	}
	
	public Object getAsObject(int i)
	{
		return data[i];
	}
	
	public String getAsString(int i)
	{
		return Boolean.toString(data[i]);
	}
	
	public double getAsDouble(int i)
	{
		if (get(i)) return 1;
		else return 0;
	}
	
	public boolean getAsBoolean(int i)
	{
		return get(i);
	}
	
	public byte getAsByte(int i)
	{
		if (get(i)) return 1;
		else return 0;
	}
	
	public float getAsFloat(int i)
	{
		if (get(i)) return 1;
		else return 0;
	}
	
	public int getAsInteger(int i)
	{
		if (get(i)) return 1;
		else return 0;
	}
	
	public Boolean get(int i)
	{
		return data[i];
	}
	
	public Boolean get(String element)
	{
		return data[getElementNames().indexOf(element)];
	}
	
	public void set(int i, Boolean val)
	{
		data[i] = val;
	}
	
	public void set(int i, Integer val)
	{
		if (val.intValue()==0) data[i] = false;
		else if (val.intValue()==1) data[i] = true;
		else throw new IllegalArgumentException("Value must be 0 or 1");
	}
	
	public void set(String element, Boolean val)
	{
		data[getElementNames().indexOf(element)] = val;
	}
	
	public void set(int i, String val)
	{
		data[i] = Boolean.valueOf(val);
	}
	
	public BooleanVector clone()
	{
		BooleanVector copy = new BooleanVector(BooleanVector.copy(this.data));
		
		if (this.hasListName()) copy.setListName(this.getListName());
		if (this.hasElementNames()) copy.setElementNames(new ArrayList<String>(this.getElementNames()));
		
		return copy;
	}
	
	public static boolean[] copy(boolean[] data)
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
		
		for (int i=0;i<size();i++)
			out.set(i,!out.get(i));
		
		return out;
	}
	
	public BooleanVector and(BooleanVector v2)
	{
		if (v2.size()!=size())
		{
			System.out.println("Error and(): Vectors must be the same size.");
			System.exit(0);
		}
		
		BooleanVector out = new BooleanVector(size());
		
		for (int i=0;i<size();i++)
			out.add(get(i) && v2.get(i));
		
		return out;
	}
	
	public BooleanVector or(BooleanVector v2)
	{
		if (v2.size()!=size())
		{
			System.out.println("Error or(): Vectors must be the same size.");
			System.exit(0);
		}
		
		BooleanVector out = new BooleanVector(size());
		
		for (int i=0;i<size();i++)
			out.add(get(i) || v2.get(i));
		
		return out;
	}

	public int sum()
	{
		return BooleanVector.sum(getData());
	}
	
	public static int sum(boolean[] data)
	{
		int mysum = 0;
		
		for (int i=0;i<data.length;i++)
			if (data[i]) 
				mysum++;
		
		return mysum;
	}

	public static int[] asIndexes(boolean[] data)
	{
		int[] out = new int[BooleanVector.sum(data)];
		
		int j = 0;
		for (int i=0;i<data.length;i++)
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
	
	public BooleanVector get(List<?> indexes)
	{
		BooleanVector sub = new BooleanVector(indexes.size());
		
		for (int i=0;i<indexes.size();i++)
			sub.add(data[((Double)(indexes.get(i))).intValue()]);
		
		return sub;
	}

	public boolean anyEqualTo(boolean val)
	{
		for (int i=0;i<size();i++)
			if(get(i)==val) return true;
		
		return false;
	}
}
