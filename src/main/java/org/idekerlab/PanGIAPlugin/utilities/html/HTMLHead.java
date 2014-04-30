package org.idekerlab.PanGIAPlugin.utilities.html;

import java.util.ArrayList;

public class HTMLHead extends HTMLTextBlock
{

	public HTMLHead()
	{

	}

	public void setTitle(String title)
	{
		this.text = new ArrayList<String>(3);
		this.add("<HEAD>");
		this.add("\t<TITLE>" + title + "</TITLE>");
		this.add("</HEAD>");
	}
}
