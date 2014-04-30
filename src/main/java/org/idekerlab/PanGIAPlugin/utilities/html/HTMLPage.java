package org.idekerlab.PanGIAPlugin.utilities.html;

import org.idekerlab.PanGIAPlugin.utilities.files.FileUtil;

import java.io.BufferedWriter;
import java.io.IOException;

public class HTMLPage extends HTMLHyperBlock
{
	private HTMLHead head;
	private HTMLBody body;

	public HTMLPage()
	{
		super(2);
		this.head = new HTMLHead();
		this.body = new HTMLBody();

		this.add(head);
		this.add(body);
	}


	public void setTitle(String title)
	{
		head.setTitle(title);
	}

	public void addToBody(HTMLBlock b)
	{
		body.add(b);
	}

	public void write(String file)
	{
		BufferedWriter bw = FileUtil.getBufferedWriter(file, false);

		try
		{
			bw.write("<HTML>\n");

			super.write(bw, 0);

			bw.write("</HTML>\n");
			bw.close();
		}
		catch (IOException e)
		{
			System.out.println("Error HTMLPage.write(String): " + e);
			System.exit(0);
		}
	}
}
