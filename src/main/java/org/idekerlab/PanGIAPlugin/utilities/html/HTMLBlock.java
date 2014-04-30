package org.idekerlab.PanGIAPlugin.utilities.html;

import java.io.BufferedWriter;

public abstract class HTMLBlock
{
	protected abstract void write(BufferedWriter bw, int depth);
}
