package org.idekerlab.PanGIAPlugin.utilities.collections;

/**
 * A utility class providing commonly-used operations on lists (vectors).
 *
 * @author rsrivas
 */
public class ListOps
{
	public static String collectionToString(float[] st, String delim)
	{
		if (st.length == 0)
			return "";

		String out = String.valueOf(st[0]);

		for (int i = 1; i < st.length; i++)
			out += delim + st[i];

		return out;
	}

}
