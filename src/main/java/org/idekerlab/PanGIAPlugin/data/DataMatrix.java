package org.idekerlab.PanGIAPlugin.data;

import org.idekerlab.PanGIAPlugin.utilities.files.FileIterator;

import java.io.*;
import java.util.*;

public abstract class DataMatrix {

	protected List<String> rownames;
	protected List<String> colnames;
	
	public abstract void set(int row, int col, String val);
	public abstract void set(int row, int col, double val);
	public abstract void set(int row, int col, float val);
	
	public abstract void Initialize(int numrows, int numcols);
	
	public abstract int dim(int dimension);
	public abstract int numRows();
	public abstract int numCols();
	public abstract int size();
	public abstract String getAsString(int row, int col);
	public abstract double getAsDouble(int row, int col);


	/**
	 * Sets the rownames.
	 * Note: Makes a copy of the passed list.
	 * @param rownames
	 */
	public void setRowNames(List<String> rownames)
	{	
		if (rownames.size()!=this.dim(0))
		{
			System.err.println("Error DataTable.setRowNames(List<String>): rownames size does not match the number of rows.");
			System.err.println("rownames size: "+rownames.size());
			System.err.println("number of rows: "+this.dim(0));
			System.exit(0);
		}
		
		this.rownames = new ArrayList<String>(rownames);
	}

	/**
	 * Sets the colnames.
	 * Note: Makes a copy of the passed list.
	 * @param colnames
	 */
	public void setColNames(List<String> colnames)
	{
		if (colnames.size()!=this.dim(1))
		{
			System.err.println("Error DataTable.setColNames(List<String>): colnames size does not match the number of columns.");
			System.err.println("colnames size: "+colnames.size());
			System.err.println("number of columns: "+this.dim(1));
			System.exit(0);
		}
		this.colnames = new ArrayList<String>(colnames);
	}
	
	/**
	 * Get the name of the row at the given index.
	 * @param index
	 */
	public String getRowName(int index)
	{
		if (this.hasRowNames())	return(rownames.get(index));
		else return null;
	}

	/**
	 * Gets a copy of the rownames list.
	 */
	public ArrayList<String> getRowNames()
	{
		return(new ArrayList<String>(rownames));
	}
	
	/**
	 * Gets a copy of the colnames list.
	 */
	public ArrayList<String> getColNames()
	{
		return(new ArrayList<String>(colnames));
	}
	
	/**
	 * Sets the row name at the given index.
	 * @param index
	 * @param name
	 */
	public void setRowName(int index, String name)
	{
		rownames.set(index, name);
	}

	/**
	 * Returns whether this DataTable has column names.
	 */
	public boolean hasColNames()
	{
		return colnames!=null;
	}
	
	/**
	 * Returns whether this DataTable has row names.
	 */
	public boolean hasRowNames()
	{
		return rownames!=null;
	}

	/**
	 * Sets the column name at the given index.
	 * @param index
	 * @param val
	 */
	public void setColName(int index, String val)
	{
		colnames.set(index, val);
	}

	/**
	 * Gets the row indexes corresponding to a list of indexes.
	 * Accepts lists of types: Double, Integer, String (rownames)
	 * @param indexes
	 */
	@SuppressWarnings("unchecked")
	protected IntVector getRowIs(List<?> indexes)
	{
		IntVector is = new IntVector(indexes.size());
		
		if (indexes.size()==0) return is;
		
		if (indexes.get(0) instanceof Double)
			for (int i=0;i<indexes.size();i++)
				is.add(((Double)indexes.get(i)).intValue());
		
		if (indexes.get(0) instanceof Integer)
			for (int i=0;i<indexes.size();i++)
				is.add(((Integer)indexes.get(i)).intValue());
		
		if (indexes.get(0) instanceof String)
		{
			if (!this.hasRowNames())
			{
				System.err.println("Error getRowIs(List<?=String> indexes): this DataTable does not have rownames.");
				System.exit(0);
			}
			
			is = DataUtil.getSListIs(rownames,(List<String>)indexes);
		}
		
		if (is.contains(-1))
		{
			System.err.println("Error getRowIs(List<?> indexes): row does not exist.");
			System.err.println(is.get(is.indexOf(-1)));
			System.exit(0);
		}
		
		return is;
	}
	
	protected int getColIs(String rname)
	{
		return DataUtil.getSListIs(colnames,rname);
	}

	@SuppressWarnings("unchecked")
	protected IntVector getColIs(List<?> indexes)
	{
		IntVector is = new IntVector(indexes.size());
		
		if (indexes.size()==0) return is;
		
		if (indexes.get(0) instanceof Double)
			for (int i=0;i<indexes.size();i++)
				is.add(((Double)indexes.get(i)).intValue());
		
		if (indexes.get(0) instanceof Integer)
			for (int i=0;i<indexes.size();i++)
				is.add(((Integer)indexes.get(i)).intValue());
		
		if (indexes.get(0) instanceof String)
			is = DataUtil.getSListIs(colnames,(List<String>)indexes);
			
		while (is.contains(-1)) is.removeElement(-1);
		
		return is;
	}
	
	protected void Initialize(int numrows, int numcols, boolean arerownames, boolean arecolnames)
	{
		Initialize(numrows, numcols);
		
		if (arerownames)
		{
			rownames = new ArrayList<String>(numrows);
			for (int i=0;i<numrows;i++)
				rownames.add("");
		}
		if (arecolnames)
		{
			colnames = new ArrayList<String>(numcols);
			for (int i=0;i<numcols;i++)
				colnames.add("");
		}
	}
	
	
	protected void LabelRow(DataVector row, int index)
	{
		if (this.hasRowNames()) row.setListName(rownames.get(index));
		
		if (this.hasColNames()) row.setElementNames(new ArrayList<String>(colnames));
	}
	
	protected void LabelCol(DataVector col, int index)
	{
		if (this.hasColNames()) col.setListName(colnames.get(index));
		
		if (this.hasRowNames()) col.setElementNames(new ArrayList<String>(rownames));
	}


	public void Load(String file, boolean arerownames, boolean arecolnames, String delimiter)
	{
		Initialize(0,0);
		
		//Determine the number of rows and columns, and build the column names
		int numrows = arecolnames ? -1 : 0;
		int numcols=-1;
		List<String> newcolnames = new ArrayList<String>();
		
		for (String line : new FileIterator(file))
		{
			//Skip the first blank line?
			if (numrows == -1)
			{
				line=line.replaceAll(delimiter+delimiter, delimiter+"NaN"+delimiter);
				line=line.replaceAll(delimiter+delimiter, delimiter+"NaN"+delimiter);
				if (line.endsWith(delimiter)) line+="NaN";
				
				if (arecolnames)
				{
					newcolnames.addAll(Arrays.asList(line.split(delimiter)));
					if (arerownames) newcolnames.remove(0);
				}
				else numrows++;
				
			}
			
			if (numrows==0) numcols = line.split(delimiter).length;
			
			numrows++;
		}
						
		Initialize(numrows, numcols);
		
		List<String> newrownames = new ArrayList<String>(numrows-1);
		
		int i = 0;
		
		if (!arecolnames) i = 1;
		
		for (String line : new FileIterator(file)) 
		{
			if (i!=0)
			{
				line=line.replaceAll(delimiter+delimiter, delimiter+"NaN"+delimiter);
				line=line.replaceAll(delimiter+delimiter, delimiter+"NaN"+delimiter);
				if (line.endsWith(delimiter)) line+="NaN";
				
				String[] cols = line.split(delimiter);
				
				int collength = cols.length;
				if (arerownames) collength--;
				
				if (collength!=numcols) throw new AssertionError("Row "+i+" does not have the correct number of columns.");
								
				if (arerownames)
				{
					newrownames.add(cols[0]);
					for (int j=1;j<cols.length;j++)
						set(i-1, j-1, cols[j]);
				}else
				{
					for (int j=0;j<cols.length;j++)
						set(i-1, j, cols[j]);
				}
			}
			
			i++;
		}
			
		if (arecolnames) setColNames(newcolnames);
		if (arerownames) setRowNames(newrownames);
	}

	public void save(String file)
	{
		WriteOut(file, false);
	}
	
	private void WriteOut(String file, boolean append)
	{
		//Open/Create file for writing. If no file exists append->false
		File outfile = new File(file);
		if (!outfile.exists())
		{
			append = false;
			
			try
			{
				outfile.createNewFile();
			}catch (IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
		}
		
		FileWriter fw = null;
		
		try
		{
			fw = new FileWriter(file, append);
		}catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}catch (IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
		
		BufferedWriter bw = new BufferedWriter(fw);
		
		//Check to see if there is no data to write
		if (this.numRows()==0 || this.numCols()==0) return;
		
		if (this.hasColNames() && !append)
		{
			try
			{
				if (this.hasRowNames()) bw.write("\t");
				
				bw.write(colnames.get(0));
				for (int i=1;i<dim(1);i++)
					bw.write("\t" + colnames.get(i));
				
				bw.write("\n");
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
		}
		
		for (int i=0;i<dim(0);i++)
		{
			try
			{
				if (this.hasRowNames()) bw.write(rownames.get(i)+"\t");
				
				bw.write(getAsString(i,0));
				for (int j=1;j<dim(1);j++)
					bw.write("\t" + getAsString(i,j));
							
				bw.write("\n");
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				System.exit(0);
			}
		}
		
		try {bw.close();}
		catch(IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
	}

}
