package solver.util;

import solver.util.Solution.Direction;

public class Coordinate {
	
	public final int x;
	public final int y;
	
	public Coordinate(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public Direction getDirection(Coordinate c) {
		if(c.y == y+1 && c.x == x) {
			return Direction.UP;
		}
		if(c.y == y-1 && c.x == x) {
			return Direction.DOWN;
		}
		if(c.y == y && c.x == x+1) {
			return Direction.RIGHT;
		}
		if(c.y == y && c.x == x-1) {
			return Direction.LEFT;
		}
		return Direction.NOT_CONNECTED;
	}
	
	@Override
	public String toString() {
		return x + "/" + y;
	}
	
	@Override
	public int hashCode() {
		return x<<16 + y;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Coordinate) {
			Coordinate o = (Coordinate) obj;
			return this.x==o.x && this.y == o.y;
		}
		return false;
	}
	
}