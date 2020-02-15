package solver.util;

import java.util.List;

public class DRCoords {

	private final List<Coordinate> current_best;
	private final List<Coordinate> drCoords;

	public DRCoords(List<Coordinate> current_best, List<Coordinate> drCoods) {
		this.current_best = current_best;
		this.drCoords = drCoods;
	}

	public List<Coordinate> getCurrent_best() {
		return current_best;
	}

	public List<Coordinate> getDrCoords() {
		return drCoords;
	}
	
	
	
	
}
