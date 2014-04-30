package org.idekerlab.PanGIAPlugin.data;

import org.idekerlab.PanGIAPlugin.util.RandomFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;


public class DoubleVector extends DataVector
{

	private double[] data;
	private int size;

	public DoubleVector()
	{
		Initialize(0);
	}

	public DoubleVector(double[] data)
	{
		this.data = data;
		size = data.length;
	}

	public DoubleVector(int size)
	{
		Initialize(size);
	}


	public void Initialize(int size)
	{
		data = new double[size];
		this.size = 0;
	}

	public Object getDataAsObject()
	{
		return data;
	}

	/**
	 * Often gets the reference to the actual data.
	 */
	public double[] getData()
	{
		if (size == data.length) return data;
		else return DoubleVector.resize(data, this.size);
	}

	public static double[] resize(double[] vec, int size)
	{
		double[] out = new double[size];

		int n = Math.min(vec.length, size);
		System.arraycopy(vec, 0, out, 0, n);

		return out;
	}

	public synchronized void add(double o)
	{
		if (data.length == 0) data = new double[10];
		else if (this.size == data.length) data = DoubleVector.resize(data, data.length * 2);

		data[size] = o;
		size++;
	}

	public synchronized void add(String val)
	{
		this.add(Double.valueOf(val));
	}

	public void add(double o, String name)
	{
		this.add(o);
		addElementName(name);
	}

	public Object getAsObject(int i)
	{
		return data[i];
	}

	public String getAsString(int i)
	{
		return Double.toString(data[i]);
	}

	public double getAsDouble(int i)
	{
		return get(i);
	}

	public byte getAsByte(int i)
	{
		return new Double(get(i)).byteValue();
	}

	public int getAsInteger(int i)
	{
		return (int) get(i);
	}

	public float getAsFloat(int i)
	{
		return (float) get(i);
	}

	public double get(int i)
	{
		return (data[i]);
	}

	public void set(int i, double val)
	{
		data[i] = val;
	}

	public double getEmpiricalValueFromSortedDist(double score)
	{
		int count;
		for (count = 0; count < this.size(); count++)
		{
			if (score <= data[count])
				break;
		}
		return (count == (this.size()) ? (1 / (double) this.size()) : (1.0 - ((double) (count) / this.size())));
	}

	public DoubleVector clone()
	{
		DoubleVector copy = new DoubleVector(DoubleVector.copy(data));
		copy.size = this.size;

		if (this.hasListName()) copy.setListName(this.getListName());
		if (this.hasElementNames()) copy.setElementNames(this.getElementNames());

		return (copy);
	}

	public int size()
	{
		return this.size;
	}

	public DoubleVector plus(DoubleVector data2)
	{
		return new DoubleVector(DoubleVector.plus(data, data2.data));
	}

	public static double[] plus(double[] x, double[] vec)
	{
		double[] out = new double[x.length];

		for (int i = 0; i < x.length; i++)
			out[i] = x[i] + vec[i];

		return out;
	}

	public static double[] plus(double[] x, double val)
	{
		double[] out = new double[x.length];

		for (int i = 0; i < x.length; i++)
			out[i] = x[i] + val;

		return out;
	}

	public double max(boolean keepNaN)
	{
		return DoubleVector.max(this.getData(), keepNaN);
	}

	public static double max(double[] data, boolean keepNaN)
	{
		if (data.length == 0) return Double.NaN;

		if (keepNaN)
		{
			double max = data[0];
			for (int i = 0; i < data.length; i++)
				if (data[i] > max) max = data[i];
			return max;
		}
		else
		{
			double max = data[0];
			for (int i = 0; i < data.length; i++)
				if (data[i] > max || Double.isNaN(max)) max = data[i];
			return max;
		}
	}

	public static double max(double[] data)
	{
		if (data.length == 0) return Double.NaN;

		double max = data[0];
		for (int i = 0; i < data.length; i++)
			if (data[i] > max) max = data[i];
		return max;

	}

	public int maxI()
	{
		return DoubleVector.maxI(this.getData());
	}

	public static int maxI(double[] data)
	{
		double max = data[0];
		int index = 0;

		for (int i = 0; i < data.length; i++)
		{
			if (!Double.isNaN(data[i]) && data[i] > max)
			{
				max = data[i];
				index = i;
			}
		}

		return index;
	}

	public BooleanVector NaNs()
	{
		BooleanVector out = new BooleanVector(this.size());
		for (int i = 0; i < size(); i++)
			if (Double.isNaN(get(i))) out.add(true);
			else out.add(false);

		return out;
	}

	public double min(boolean keepNaN)
	{
		if (size() == 0) return Double.NaN;

		if (keepNaN)
		{
			if (data.length == 0) return Double.NaN;

			double min = data[0];
			for (int i = 1; i < this.size; i++)
				if (!(data[i] >= min)) min = data[i];

			return min;
		}
		else
		{
			DoubleVector newvec = this.get(this.NaNs().not());
			return newvec.min(true);
		}
	}

	public static double min(double[] data)
	{
		if (data.length == 0) return Double.NaN;

		double min = data[0];
		for (int i = 1; i < data.length; i++)
			if (!(data[i] >= min)) min = data[i];

		return min;
	}

	public static double[] abs(double[] v)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = Math.abs(v[i]);

		return out;
	}

	public static double[] subtract(double[] v, double val)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = v[i] - val;

		return out;
	}

	public static double[] subtract(double[] v, int[] v2)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = v[i] - v2[i];

		return out;
	}

	public static double[] subtract(double[] v1, double[] v2)
	{
		double[] out = new double[v1.length];

		for (int i = 0; i < v1.length; i++)
			out[i] = v1[i] - v2[i];

		return out;
	}

	public static double[] divideBy(double[] v, double val)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = v[i] / val;

		return out;
	}

	public static double[] divideBy(double[] v, double[] val)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = v[i] / val[i];

		return out;
	}

	public DoubleVector divideBy(int val)
	{
		DoubleVector out = this.clone();

		for (int i = 0; i < size(); i++)
			out.set(i, out.get(i) / val);

		return out;
	}

	public static double[] times(double[] x, double[] vec)
	{
		double[] out = new double[x.length];

		for (int i = 0; i < x.length; i++)
			out[i] = x[i] * vec[i];

		return out;
	}

	public double sum()
	{
		return DoubleVector.sum(this.getData());
	}

	public static double sum(double[] x)
	{
		double sum = 0.0;
		boolean nans = true;

		for (int i = 0; i < x.length; i++)
			if (!Double.isNaN(x[i]))
			{
				sum += x[i];
				nans = false;
			}

		if (nans) return Double.NaN;

		return sum;
	}

	public DoubleVector sample(int samplesize, boolean replace)
	{
		DoubleVector mysample = new DoubleVector(samplesize);

		DoubleVector cp = this.clone();

		Random randgen = RandomFactory.make();
		;

		if (!replace)
		{
			int lsizem1 = cp.size() - 1;

			for (int i = 0; i < samplesize; i++)
			{
				int swapi = lsizem1 - randgen.nextInt(cp.size() - i);
				double temp = cp.get(i);
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

	public DoubleVector subVector(int i1, int size)
	{
		DoubleVector out = new DoubleVector(size);

		int i1s = i1 + size;

		for (int i = i1; i < i1s; i++)
			out.add(data[i]);

		return out;
	}

	public static double[] get(double[] v, int[] indexes)
	{
		double[] out = new double[indexes.length];

		for (int i = 0; i < indexes.length; i++)
			out[i] = v[indexes[i]];

		return out;
	}

	public DoubleVector get(BooleanVector bv)
	{
		if (bv.size() != this.size())
			throw new IllegalArgumentException("The two vectors must be the same size. " + bv.size() + "!=" + this.size);


		DoubleVector sub = new DoubleVector();

		boolean found = false;
		if (this.elementnames != null)
		{
			sub.setElementNames(new ArrayList<String>());

			for (int i = 0; i < size(); i++)
				if (bv.get(i))
				{
					sub.add(data[i], this.getElementName(i));
					found = true;
				}
		}
		else
		{
			for (int i = 0; i < size(); i++)
				if (bv.get(i))
				{
					sub.add(data[i]);
					found = true;
				}
		}

		if (!found) sub.removeElementNames();

		return sub;
	}

	public static double[] repeat(double val, int num)
	{
		double[] out = new double[num];

		for (int i = 0; i < num; i++)
			out[i] = val;

		return out;
	}

	public BooleanVector isReal()
	{
		BooleanVector out = new BooleanVector(size());

		for (int i = 0; i < size(); i++)
			out.add(!(Double.isNaN(get(i)) || Double.isInfinite(get(i))));

		return out;
	}

	public DoubleVector sort()
	{
		DoubleVector out;

		if (!this.hasElementNames())
		{
			out = this.clone();
			double[] mydata = out.asDoubleArray();
			Arrays.sort(mydata);
			out = new DoubleVector(mydata);
		}
		else
		{
			out = new DoubleVector(this.size());
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

	public static void set(double[] v, boolean[] which, double val)
	{
		for (int i = 0; i < which.length; i++)
			if (which[i]) v[i] = val;
	}

	public static boolean isAnyNaN(double[] v)
	{
		for (int i = 0; i < v.length; i++)
			if (Double.isNaN(v[i])) return true;

		return false;
	}

	public static double[] sqrt(double[] v)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = Math.sqrt(v[i]);

		return out;
	}

	public static boolean allLessThan(double[] v, int val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] >= val) return false;

		return true;
	}

	public static boolean allGreaterThan(double[] v, int val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] <= val) return false;

		return true;
	}

	public static boolean anyLessThan(double[] v, int val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] < val) return true;

		return false;
	}

	public static boolean anyLessThan(double[] v, double val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] < val) return true;

		return false;
	}

	public static boolean anyGreaterThan(double[] v, int val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] > val) return true;

		return false;
	}

	public static boolean anyGreaterThan(double[] v, double val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] > val) return true;

		return false;
	}

	public static boolean anyEqualTo(double[] v, int val)
	{
		for (int i = 0; i < v.length; i++)
			if (v[i] == val) return true;

		return false;
	}

	public static boolean[] equalTo(double[] v, int val)
	{
		boolean[] out = new boolean[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = v[i] == val;

		return out;
	}

	public static double[] copy(double[] vec)
	{
		return vec.clone();
	}

	public static double[] square(double[] v)
	{
		double[] out = new double[v.length];

		for (int i = 0; i < out.length; i++)
			out[i] = v[i] * v[i];

		return out;
	}

	public static int[] round(double[] v)
	{
		int[] out = new int[v.length];

		for (int i = 0; i < out.length; i++)
			out[i] = (int) Math.round(v[i]);

		return out;
	}

	public static int[] sort_I(double[] v)
	{
		return Sorter.Sort_I(DoubleVector.copy(v));
	}

	public static boolean[] isNaN(double[] v)
	{
		boolean[] out = new boolean[v.length];

		for (int i = 0; i < v.length; i++)
			out[i] = Double.isNaN(v[i]);

		return out;
	}

}
