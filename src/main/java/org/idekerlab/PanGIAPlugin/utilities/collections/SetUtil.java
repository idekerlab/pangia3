package org.idekerlab.PanGIAPlugin.utilities.collections;

import java.util.HashSet;
import java.util.Set;

public class SetUtil
{
	public static double jaccard(Set<String> set1, Set<String> set2)
	{
		Set<String> intersect = new HashSet<String>(set1);
		intersect.retainAll(set2);

		Set<String> union = new HashSet<String>(set1);
		union.addAll(set2);

		return intersect.size() / (double) union.size();
	}

}
