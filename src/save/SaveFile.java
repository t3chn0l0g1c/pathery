package save;

import java.util.ArrayList;
import java.util.List;

import solver.util.Coordinate;

public class SaveFile {

	private final String site;
	private final String id;
	
	private final int score;
	private final List<Coordinate> walls;
	
	public SaveFile(String site, String id, int score, List<Coordinate> walls) {
		this.site = site;
		this.id = id;
		this.score = score;
		this.walls = walls;
	}
	
	
	public String getName() {
		return site + "_" + id;
	}


	public String getSite() {
		return site;
	}

	public String getId() {
		return id;
	}


	public int getScore() {
		return score;
	}


	public List<Coordinate> getWalls() {
		return walls;
	}


	public byte[] getBytes() {
		StringBuilder str = new StringBuilder();
		str.append(score);
		str.append("\n");
		for(Coordinate c : walls) {
			str.append(c.x);
			str.append(",");
			str.append(c.y);
			str.append(";");
		}
		return str.toString().getBytes();
	}


	public static SaveFile readFromFile(String site, String id, List<String> lines) {
		int score = Integer.valueOf(lines.get(0));
		String[] w = lines.get(1).split(";");
		List<Coordinate> walls = new ArrayList<Coordinate>();
		for(String s : w) {
			String[] coords = s.split(",");
			walls.add(new Coordinate(Integer.valueOf(coords[0]), Integer.valueOf(coords[1])));
		}
		return new SaveFile(site, id, score, walls);
	}
	
	
}
