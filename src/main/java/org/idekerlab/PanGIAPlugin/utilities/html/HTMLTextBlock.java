package org.idekerlab.PanGIAPlugin.utilities.html;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HTMLTextBlock extends HTMLBlock
{
	protected List<String> text;

	protected HTMLTextBlock()
	{
		text = new ArrayList<String>();
	}

	protected HTMLTextBlock(int numLines)
	{
		text = new ArrayList<String>(numLines);
	}

	public void add(String line)
	{
		this.text.add(line);
	}

	protected void write(BufferedWriter bw, int depth)
	{
		String tabs = "";
		for (int i = 0; i < depth; i++)
			tabs += "\t";

		try
		{
			for (String line : text)
				bw.write(tabs + line + '\n');
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
