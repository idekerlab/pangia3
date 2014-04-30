package org.idekerlab.PanGIAPlugin.networks;

public final class UndirectedSFEdge extends SFEdge {

	public UndirectedSFEdge(String s1, String s2, float value) {
		super(s1, s2, value);
	}

	public boolean isDirected() {
		return false;
	}

	public boolean equals(Object inter) {
		if (inter == null)
			return false;
		if (inter instanceof SEdge) {
			SEdge other = (SEdge) inter;
			return i1.equals(other.i1) && i2.equals(other.i2)
					|| i1.equals(other.i2) && i2.equals(other.i1);
		} else
			return false;
	}

}
