package org.idekerlab.PanGIAPlugin.data;

import java.util.ArrayList;
import java.util.List;

public abstract class DataVector
{

	protected List<String> elementnames;
	protected String listname;

	protected DataVector()
	{
	}

	protected abstract double getAsDouble(int i);

	protected abstract String getAsString(int i);

	protected abstract Object getDataAsObject();


	protected void setElementNames(List<String> elementnames)
	{
		this.elementnames = new ArrayList<String>(elementnames);
	}

	protected void setListName(String listname)
	{
		this.listname = listname;
	}

	protected String getListName()
	{
		return (listname);
	}

	public List<String> getElementNames()
	{
		return (new ArrayList<String>(elementnames));
	}

	protected void removeElementNames()
	{
		elementnames = null;
	}

	public boolean hasElementNames()
	{
		return elementnames != null;
	}

	protected boolean hasListName()
	{
		return listname != null;
	}

	protected String getElementName(int i)
	{
		return (elementnames.get(i));
	}

	public void setElementName(int index, String name)
	{
		elementnames.set(index, name);
	}

	protected void addElementName(String name)
	{
		elementnames.add(name);
	}

	protected abstract int size();


	public double[] asDoubleArray()
	{
		double[] da = new double[size()];

		for (int i = 0; i < this.size(); i++)
			da[i] = getAsDouble(i);

		return da;
	}

	public boolean equals(Object other)
	{
		if (other == null)
			return false;
		else if (!(other instanceof DataVector))
			return false;
		else
		{
			DataVector dv = (DataVector) other;

			if (dv.size() != size())
				return false;
			if (dv.listname != this.listname)
				return false;

			if (this.hasElementNames() && this.elementnames.size() == dv.getElementNames().size())
				for (int i = 0; i < size(); i++)
					if (!this.getElementName(i).equals(dv.getElementName(i)))
						return false;

			if (!this.getDataAsObject().equals(dv.getDataAsObject()))
				return false;
		}

		return true;
	}

	public int hashCode()
	{
		return getDataAsObject().hashCode();
	}

	public String toString()
	{
		if (size() == 0)
			return "[]";

		String out = '[' + getAsString(0);
		for (int i = 1; i < size(); i++)
			out += ',' + getAsString(i);

		return out + ']';
	}


}
