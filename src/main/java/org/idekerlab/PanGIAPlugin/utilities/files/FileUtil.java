package org.idekerlab.PanGIAPlugin.utilities.files;

import java.io.*;
import java.util.zip.GZIPInputStream;


public class FileUtil
{
	public static BufferedReader getGZBufferedReader(String file)
	{
		try
		{
			return new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());
			e.printStackTrace();
		}

		return null;
	}

	public static int countLines(String file)
	{
		int count = 0;
		for (String line : new FileIterator(file))
			count++;

		return count;
	}

	/**
	 * http://www.roseindia.net/java/beginners/CopyFile.shtml
	 */
	private static void copyfile(String srFile, String dtFile, boolean append)
	{
		try
		{
			File f1 = new File(srFile);
			File f2 = new File(dtFile);
			InputStream in = new FileInputStream(f1);

			OutputStream out = new FileOutputStream(f2, append);

			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0)
			{
				out.write(buf, 0, len);
			}
			in.close();
			out.close();
			System.out.println("File copied.");
		}
		catch (FileNotFoundException ex)
		{
			System.out.println(ex.getMessage() + " in the specified directory.");
			System.exit(0);
		}
		catch (IOException e)
		{
			System.out.println(e.getMessage());
		}
	}

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

	public static BufferedReader getBufferedReader(String srFile)
	{
		FileReader fr = null;
		try
		{
			fr = new FileReader(srFile);
		}
		catch (FileNotFoundException e)
		{
			System.out.println(e.getMessage());
			System.exit(0);
		}

		return new BufferedReader(fr);
	}

	public static void delete(File f)
	{
		if (!f.delete())
		{
			for (File fin : f.listFiles())
				delete(fin);

			f.delete();
		}
	}

}
