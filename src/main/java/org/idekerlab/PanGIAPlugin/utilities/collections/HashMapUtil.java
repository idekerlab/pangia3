package org.idekerlab.PanGIAPlugin.utilities.collections;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HashMapUtil
{
	public static <T1, T2> void updateMapSet(Map<T1, Set<T2>> map, T1 key, T2 addition)
	{
		Set<T2> tset = map.get(key);
		if (tset == null)
		{
			tset = new HashSet<T2>();
			tset.add(addition);
			map.put(key, tset);
		}
		else
			tset.add(addition);
	}

}
