package solver.maze;

import java.util.Map;

import solver.input.MazeDescriptor;
import solver.util.Counter;
import solver.util.IceKey;
import solver.util.Type;
import solver.util.IceKey.Direction;

public final class Field implements Comparable<Field> {

	private Type type = Type.NORMAL;
	int priority = 0;

	final Field up;
	final Field down;
	final Field left;
	final Field right;

	public int precedessor = 0;

	public final int id;
	
	// for compatibility with MazeTester10
	public final int extendedID;
	
	// what an ugly hack...
	int resultLength = 0;
	
	// for compatibility with MazeTester7
	public int blocked_offset = 0;
	
	public static final int pred_constant = 2000000000;
	
	public Field(int id) {
		this.id = id;
		this.extendedID=-1;
		up = null;
		down = null;
		left = null;
		right = null;
		precedessor = id;
		type = null;
	}
	
	public Field(int width, int height, Field[][] field, int x, int y, MazeDescriptor md, Field sentinel, Map<IceKey, Field> iceTiles, Direction direction, Counter iceTileCounter) {
		type = md.getTypeFor(x, y);
		if(field[y][x]!=null) {
			if(type!=Type.ICE) {
				throw new RuntimeException("Wtf");
			}
		}
		field[y][x] = this;	// null check for ice tiles (throws exception if not, check map too)

		boolean ice = type==Type.ICE;
		if(ice) {
			id = md.getWidth()*md.getHeight()+iceTileCounter.getAndIncrement();
			IceKey c = new IceKey(direction, x, y);
			Field fa = iceTiles.get(c);
			if(fa!=null) {
				throw new RuntimeException("Duplicate ice at " + x + "/" + "y"); 
			}
			iceTiles.put(c, this);
		} else {
			id = x+1 + (y)*width;
		}
		
		extendedID = fieldIDToIndex(id, width);
		
		if(!ice) {
			up = retrieve(x, y-1, width, height, field, md, sentinel, iceTiles, Direction.VERTICAL, iceTileCounter);
			down = retrieve(x, y+1, width, height, field, md, sentinel, iceTiles, Direction.VERTICAL, iceTileCounter);
			left = retrieve(x-1, y, width, height, field, md, sentinel, iceTiles, Direction.HORIZONTAL, iceTileCounter);
			right = retrieve(x+1, y, width, height, field, md, sentinel, iceTiles, Direction.HORIZONTAL, iceTileCounter);
		} else {
			if(direction==Direction.HORIZONTAL) {
				up = sentinel;
				down = sentinel;
				left = retrieve(x-1, y, width, height, field, md, sentinel, iceTiles, Direction.HORIZONTAL, iceTileCounter);
				right = retrieve(x+1, y, width, height, field, md, sentinel, iceTiles, Direction.HORIZONTAL, iceTileCounter);
			} else {
				up = retrieve(x, y-1, width, height, field, md, sentinel, iceTiles, Direction.VERTICAL, iceTileCounter);
				down = retrieve(x, y+1, width, height, field, md, sentinel, iceTiles, Direction.VERTICAL, iceTileCounter);
				left = sentinel;
				right = sentinel;
			}
		}
		
		if(up==null) {
			throw new RuntimeException("Dammit");
		}
		if(down==null) {
			throw new RuntimeException("Dammit");
		}if(left==null) {
			throw new RuntimeException("Dammit");
		}if(right==null) {
			throw new RuntimeException("Dammit");
		}
	
		// TODO ice tiles
		
		/**
		 * type == ice
		 * 		horizontal:
		 * 		left, right
		 * 		if not exists put, first in array
		 *
		 *		vertical:
		 *		up, down
		 *		if not exists put, second in array
		 *
		 *
		 *	retrieve: gets direction, looks in array, if type == ice, look in map
		 */
	}
	
	// TODO ice tiles are linked wrong!
//	.
	
	private static Field retrieve(int x, int y, int width, int height, Field[][] field, MazeDescriptor md, Field sentinel, Map<IceKey, Field> iceTiles, Direction direction, Counter iceTileCounter) {
		if(x==-1 || x==width || y==-1 || y==height) {
			return sentinel;
		}
		if(field[y][x]==null) {
			field[y][x] = new Field(width, height, field, x, y, md, sentinel, iceTiles, direction, iceTileCounter);
		}
		Field f =field[y][x]; 
		if(f.type==Type.BLOCKED_BY_MAP) {
			return sentinel;
		}
		if(f.type==Type.ICE) {
			IceKey c = new IceKey(direction, x, y);
			Field fa = iceTiles.get(c);
			if(fa!=null) {
				return fa;
			}
			return new Field(width, height, field, x, y, md, sentinel, iceTiles, direction, iceTileCounter);
		}
		return f;
	}
	
	private static int fieldIDToIndex(final int f, final int width) {
		return (((f-1)/width)+1)*(width+2)+((f-1)%width)+1;
	}
	
	public final void setPriority(int priority) {
		this.priority = priority;
	}

	@Override
	public final String toString() {
		return String.valueOf(type.getLiteral());
	}

	@Override
	public final boolean equals(Object obj) {
		return obj == this;
	}
	
	@Override
	public int hashCode() {
		return id;
	}

	// TODO can probably be improved
	/**
	 * markBlocking guard type dependent (BLOCKED_BY must not mark blocking)	//
	 * guard against recursive loops (id check on entry)	//
	 * only propagate in "normal++" directions?  (should be implied by guards...)
	 */
//	.
	public void markBLocking(final int[] blocking) {
		if(type==null || blocking[id]!=0 || type==Type.BLOCKED_BY_MAP) {
			return;
		}
		blocking[id] = id;
		int normalCount = 0;
		boolean u = false;
		boolean d = false;
		boolean l = false;
		boolean r = false;
		
		if(up.type!=Type.BLOCKED_BY_MAP && up.type!=Type.BLOCKED_BY_USER && up.hasMoreThanOneNeighbor()) {
			normalCount++;
			u = true;
		}
		if(down.type!=Type.BLOCKED_BY_MAP && down.type!=Type.BLOCKED_BY_USER && down.hasMoreThanOneNeighbor()) {
			normalCount++;
			d = true;
		}
		if(left.type!=Type.BLOCKED_BY_MAP && left.type!=Type.BLOCKED_BY_USER && left.hasMoreThanOneNeighbor()) {
			normalCount++;
			l = true;
		}
		if(right.type!=Type.BLOCKED_BY_MAP && right.type!=Type.BLOCKED_BY_USER && right.hasMoreThanOneNeighbor()) {
			normalCount++;
			r = true;
		}
		if(normalCount<=2) {
			if(u) {
				up.markBLocking(blocking);
			}
			if(d) {
				down.markBLocking(blocking);
			}
			if(l) {
				left.markBLocking(blocking);
			}
			if(r) {
				right.markBLocking(blocking);
			}
		}
	}

	private boolean hasMoreThanOneNeighbor() {
		if(type==Type.NORMAL) {
			int normalCount = 0;
			if(up.type!=Type.BLOCKED_BY_MAP && up.type!=Type.BLOCKED_BY_USER) {
				normalCount++;
			}
			if(down.type!=Type.BLOCKED_BY_MAP && down.type!=Type.BLOCKED_BY_USER) {
				normalCount++;
			}
			if(left.type!=Type.BLOCKED_BY_MAP && left.type!=Type.BLOCKED_BY_USER) {
				normalCount++;
			}
			if(right.type!=Type.BLOCKED_BY_MAP && right.type!=Type.BLOCKED_BY_USER) {
				normalCount++;
			}
			return normalCount>1;
		}
		return true;
	}
	
	@Override
	public final int compareTo(Field o) {
		return id-o.id;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type, Maze maze) {
		this.type = type;
		maze.setWallBits(id, type==Type.BLOCKED_BY_USER);
	}

}
