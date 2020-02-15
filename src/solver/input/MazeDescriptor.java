package solver.input;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solver.util.Coordinate;
import solver.util.Type;

public class MazeDescriptor {
	
	private final Type[][] tiles;
	private final int width;
	private final int height;
	private int walls;
	private int walls_available;
	private final String name;
	private final String id;
	private final String mapCode;
	private final String site;
	
	private final int originalWallCount;
	/**
	 * Just a helper array containing all the types (in right order to test)
	 */
	private final Type[] types;
	private final Type[] types_red_start;
	
	public MazeDescriptor(String site, String id, String name, int width, int height, Type[][] tiles, int blocks_available, String mapCode, boolean correctRO) {
		this.width = width;
		this.height = height;
		this.id = id;
		this.site = site;
		this.mapCode = mapCode;
		if(tiles.length!=height && tiles[0].length!=width) {
			throw new RuntimeException("Expected " + width + "/" + height + " but was " + tiles.length + "/" + tiles[0].length);
		}
		this.tiles = tiles;
		this.walls = blocks_available;
		this.originalWallCount = walls;
		walls_available = blocks_available;
		this.name = name;
		
		if(correctRO && name.equals("Reverse Order")) {
			for(int i = 0; i<tiles.length; i++) {
				for(int j = 0; j<tiles[i].length; j++) {
					switch(tiles[i][j]) {
					case START_RED: tiles[i][j] = Type.START; break;
					case WP_A: tiles[i][j] = Type.WP_C; break;
					case WP_C: tiles[i][j] = Type.WP_A; break;
					default: // don't care
					}
				}
			}
		}
		types = initTypes(tiles);
		types_red_start = initTypesRed(tiles);
	}

	private static Type[] initTypesRed(Type[][] types) {
		Set<Type> set = new HashSet<>();
		for(Type[] ta : types) {
			for(Type t : ta) {
				set.add(t);
			}
		}
		if(!set.contains(Type.START_RED)) {
			return null;
		}
		List<Type> list = new ArrayList<Type>();
		list.add(Type.START_RED);
		if(set.contains(Type.WP_E)) {
			list.add(Type.WP_E);
		}
		if(set.contains(Type.WP_D)) {
			list.add(Type.WP_D);
		}
		if(set.contains(Type.WP_C)) {
			list.add(Type.WP_C);
		}
		if(set.contains(Type.WP_B)) {
			list.add(Type.WP_B);
		}
		if(set.contains(Type.WP_A)) {
			list.add(Type.WP_A);
		}
		list.add(Type.END);
		return list.toArray(new Type[list.size()]);
	}

	public String getSite() {
		return site;
	}
	
	public String getMapCode() {
		return mapCode;
	}

	private static Type[] initTypes(Type[][] types) {
		Set<Type> set = new HashSet<>();
		for(Type[] ta : types) {
			for(Type t : ta) {
				set.add(t);
			}
		}
		List<Type> list = new ArrayList<Type>();
		list.add(Type.START);
		for(Type t : Type.getWPs()) {
			if(set.contains(t)) {
				list.add(t);
			}
		}
		list.add(Type.END);
		return list.toArray(new Type[list.size()]);
	}
	
 
	public final String getId() {
		return id;
	}

	public final String getName() {
		return name;
	}

	public final int getWidth() {
		return width;
	}

	public final int getHeight() {
		return height;
	}

	public final int getWalls() {
		return walls;
	}

	public final int getWalls_available() {
		return walls_available;
	}

	public final boolean clickedOnTile(int x, int y)  {
		if(y<0||y>=tiles.length || x<0 || x>=tiles[y].length) {
			return false;
		}
		Type t = tiles[y][x];
		if(t==Type.NORMAL && walls_available>0) {
			tiles[y][x] = Type.BLOCKED_BY_USER;
			walls_available--;
			return true;
		} else if(t==Type.BLOCKED_BY_USER) {
			tiles[y][x] = Type.NORMAL;
			walls_available++;
			return true;
		}
		return false;
	}
	
	public final Type getTypeFor(int x, int y) {
		return tiles[y][x];
	}

	public final MazeDescriptor copy() {
		Type[][] ta = new Type[height][];
		for(int i = 0; i<height; i++) {
			ta[i] = new Type[width];
			for(int j = 0; j<width; j++) {
				ta[i][j] = tiles[i][j];
			}
		}
		return new MazeDescriptor(site, id, name, width, height, ta, walls, mapCode, false);
	}

	public final String persist() {
		StringBuilder str = new StringBuilder();
		for(int i = 0; i<tiles.length; i++) {
			for(int j = 0; j<tiles[i].length; j++) {
				if(tiles[i][j]==Type.BLOCKED_BY_USER) {
					str.append(String.valueOf(j));
					str.append(",");
					str.append(String.valueOf(i));
					str.append("\n");
				}
			}
		}
		return str.toString();
	}

	public void resetModifiedWallCount() {
		int dif = originalWallCount - walls;
		walls = originalWallCount;
		walls_available += dif;
	}
	
	public final void reset() {
		walls = originalWallCount;
		for(int i = 0; i<tiles.length; i++) {
			for(int j = 0; j<tiles[i].length; j++) {
				if(tiles[i][j]==Type.BLOCKED_BY_USER) {
					tiles[i][j] = Type.NORMAL;
				}
			}
		}
		walls_available = walls;
	}
	
	public final MazeDescriptor setWallsManually(Coordinate... coordinates) {
		int newWalls = walls_available;
		for(Coordinate c : coordinates) {
			if(tiles[c.y][c.x]!=Type.NORMAL) {
				throw new RuntimeException("Expected NORMAL but was " + tiles[c.y][c.x]);
			}
			tiles[c.y][c.x] = Type.BLOCKED_BY_MAP;
			newWalls--;
		}
		return new MazeDescriptor(site, id, name, width, height, tiles, newWalls, mapCode, false);
	}

	public final List<Coordinate> getWallCoords() {
		List<Coordinate> result = new ArrayList<Coordinate>();
		for(int y = 0;y<tiles.length; y++) {
			for(int x = 0; x<tiles[y].length; x++) {
				if(tiles[y][x]==Type.BLOCKED_BY_USER) {
					result.add(new Coordinate(x, y));
				}
			}
		}
		return result;
	}

	public final void printSettings() {
		System.out.println("name: " + name);
		System.out.println("width/height: " + width + " x " + height);
		System.out.println("walls: " + walls_available);
	}

	public final boolean hasTPs() {
		for(int i = 0; i<tiles.length; i++) {
			for(int j = 0; j<tiles[i].length; j++) {
				if(Type.isTP_IN(tiles[i][j])) {
					return true;
				}
			}
		}
		return false;
	}
	
	public final Type[] getTypes() {
		return types;
	}
	
	public final Type[] getTypesForRedStart() {
		return types_red_start;
	}
	
	public boolean isNonDefault() {
		return types_red_start!=null;
	}
	
	public final void setTile(int x, int y, Type path) {
		tiles[y][x] = path;
	}


	public final void setWallCount(int i) {
		int dif = i-walls;
		walls = i;
		walls_available += dif;
	}

	public final void changeType(int x, int y) {
		Type t = tiles[y][x];
		if(t==Type.BLOCKED_BY_USER) {
			tiles[y][x] = Type.BLOCKED_BY_MAP;
			walls--;
		} else if(t==Type.BLOCKED_BY_MAP) {
			tiles[y][x] = Type.BLOCKED_BY_USER;
			walls++;
		}
	}

	
//	public MazeTester createTesterObject() {
//		// TODO resolve by name
//	}

}
