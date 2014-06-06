package org.idekerlab.PanGIAPlugin.data;

import java.util.ArrayList;
import java.util.List;

public abstract class DataMatrix
{

	private List<String> rownames;
	private List<String> colnames;

	protected abstract int dim(int dimension);

	protected abstract int numRows();

	protected abstract int numCols();

	protected abstract double getAsDouble(int row, int col);


	/**
	 * Sets the rownames.
	 * Note: Makes a copy of the passed list.
	 *
	 * @param rownames
	 */
	protected void setRowNames(List<String> rownames)
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
	protected void setColNames(List<String> colnames)
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
	protected ArrayList<String> getRowNames()
	{
		return (new ArrayList<String>(rownames));
	}

	/**
	 * Gets a copy of the colnames list.
	 */
	protected ArrayList<String> getColNames()
	{
		return (new ArrayList<String>(colnames));
	}

	/**
	 * Returns whether this DataTable has column names.
	 */
	protected boolean hasColNames()
	{
		return colnames != null;
	}

	/**
	 * Returns whether this DataTable has row names.
	 */
	protected boolean hasRowNames()
	{
		return rownames != null;
	}


}
