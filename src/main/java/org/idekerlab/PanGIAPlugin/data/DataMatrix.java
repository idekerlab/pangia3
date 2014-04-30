package org.idekerlab.PanGIAPlugin.data;

import java.util.ArrayList;
import java.util.List;

public abstract class DataMatrix
{

	protected List<String> rownames;
	protected List<String> colnames;

	public abstract void Initialize(int numrows, int numcols);

	public abstract int dim(int dimension);

	public abstract int numRows();

	public abstract int numCols();

	public abstract String getAsString(int row, int col);

	public abstract double getAsDouble(int row, int col);


	/**
	 * Sets the rownames.
	 * Note: Makes a copy of the passed list.
	 *
	 * @param rownames
	 */
	public void setRowNames(List<String> rownames)
	{
		if (rownames.size() != this.dim(0))
		{
			System.err.println("Error DataTable.setRowNames(List<String>): rownames size does not match the number of rows.");
			System.err.println("rownames size: " + rownames.size());
			System.err.println("number of rows: " + this.dim(0));
			System.exit(0);
		}

		this.rownames = new ArrayList<String>(rownames);
	}

	/**
	 * Sets the colnames.
	 * Note: Makes a copy of the passed list.
	 *
	 * @param colnames
	 */
	public void setColNames(List<String> colnames)
	{
		if (colnames.size() != this.dim(1))
		{
			System.err.println("Error DataTable.setColNames(List<String>): colnames size does not match the number of columns.");
			System.err.println("colnames size: " + colnames.size());
			System.err.println("number of columns: " + this.dim(1));
			System.exit(0);
		}
		this.colnames = new ArrayList<String>(colnames);
	}

	/**
	 * Gets a copy of the rownames list.
	 */
	public ArrayList<String> getRowNames()
	{
		return (new ArrayList<String>(rownames));
	}

	/**
	 * Gets a copy of the colnames list.
	 */
	public ArrayList<String> getColNames()
	{
		return (new ArrayList<String>(colnames));
	}

	/**
	 * Returns whether this DataTable has column names.
	 */
	public boolean hasColNames()
	{
		return colnames != null;
	}

	/**
	 * Returns whether this DataTable has row names.
	 */
	public boolean hasRowNames()
	{
		return rownames != null;
	}


}
