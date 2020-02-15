package solver.util;

import java.util.List;

public class Solution {

	public static enum Direction {
		UP,
		DOWN,
		LEFT,
		RIGHT,
		NOT_CONNECTED; // yay NULL objects :D
	}
	
	public static class Wall extends Coordinate {

		final int value;
		public Wall(int x, int y, int value) {
			super(x, y);
			this.value = value;
		}
		@Override
		public String toString() {
			return super.toString() + " (" + value + ")";
		}
		public int getValue() {
			return value;
		}
		
	}
	
	private final List<Coordinate> fields;
	private final int length;
	private final List<Wall> walls;
	private final List<Wall> whatIf;
	
	public Solution(List<Coordinate> fields, int length, List<Wall> walls, List<Wall> whatIf) {
		this.fields = fields;
		this.length = length;
		this.walls = walls;
		this.whatIf = whatIf;
	}

	public List<Wall> getWhatIf() {
		return whatIf;
	}


	public List<Wall> getWalls() {
		return walls;
	}

	public int getLength() {
		return length;
	}

	public List<Coordinate> getFields() {
		return fields;
	}

}
