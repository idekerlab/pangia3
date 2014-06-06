package org.idekerlab.PanGIAPlugin.utilities.files;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;


public class FileUtil
{

	public static BufferedWriter getBufferedWriter(String dtFile, boolean append)
	{
		FileWriter fw = null;
		try
		{
			fw = new FileWriter(dtFile, append);
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}

		return new BufferedWriter(fw);
	}

}
