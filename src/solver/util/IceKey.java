package solver.util;


public class IceKey {
	
	public enum Direction {
		HORIZONTAL,
		VERTICAL;
	}
	
	public final Direction direction;
	public final int x;
	public final int y;
	public IceKey(Direction direction, int x, int y) {
		this.direction = direction;
		this.x = x;
		this.y = y;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((direction == null) ? 0 : direction.hashCode());
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IceKey other = (IceKey) obj;
		if (direction != other.direction)
			return false;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}
	
}