package solver.cache;

import solver.maze.Field;

public class SubPath {

	public final boolean[] path;
	public final int length;
	public final int tpMask;
	public final int endTPMask;
	public final Field[] endField;

	public SubPath(boolean[] path, int length, int tpMask, int endTPMask, Field[] endField) {
		this.path = path;
		this.length = length;
		this.tpMask = tpMask;
		this.endTPMask = endTPMask;
		this.endField = endField;
	}
	
	
}
