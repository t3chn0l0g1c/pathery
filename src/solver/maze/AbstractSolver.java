package solver.maze;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import solver.cache.CacheMap;
import solver.cache.CacheMap.ArrayWrapper;
import solver.cache.SubPathCache;
import solver.input.MazeDescriptor;
import solver.util.CanceledException;
import solver.util.Counter;
import solver.util.FieldCollection;
import solver.util.FieldCollection.FieldSet;
import solver.util.IceKey;
import solver.util.ThreadStatistics;
import solver.util.Type;

public abstract class AbstractSolver {

	private static class ALStatistics {
		long tests = 0;
		long cacheHit = 0;
		int localLongestPath = 0;
		long localBestFoundAt = 0;
	}

	public static final boolean USE_CACHE = true;
//	public static final int CACHE_SIZE = 1024000;
	public static final boolean EXPERIMENTAL_ADDITIONAL_RECURSE = true; // NOTE
																		// very
																		// very
																		// very
																		// expensive...

	protected final Random RANDOM = new Random();

	protected final int width;
	protected final int height;
	protected final Field[][] field;

	protected final int blocks_available;

	protected final MazeDescriptor md;
	protected final Maze maze;
	protected CanceledException ex;

	private ALStatistics statistics;

	// TODO size configurable
	protected CacheMap<Integer> cache = CacheMap.createCacheMap();
	
	protected final HighscoreHandler scoreHandler;
	
	protected final long startTime;

	public AbstractSolver(final MazeDescriptor md, HighscoreHandler sh) {
		this.width = md.getWidth();
		this.height = md.getHeight();
		this.blocks_available = md.getWalls();
		field = new Field[height][width];
		this.md = md;
		Map<IceKey, Field> iceTiles = new HashMap<IceKey, Field>();
		final Field sentinel = new Field(2140000000);
		field[0][0] = new Field(width, height, field, 0, 0, md, sentinel,
				iceTiles, IceKey.Direction.HORIZONTAL, new Counter());
		this.maze = new Maze(md, field, iceTiles);
		this.scoreHandler = sh;
		statistics = new ALStatistics();
		startTime = System.currentTimeMillis();
		statistics.localBestFoundAt = startTime;
	}

	public AbstractSolver(AbstractSolver l) {
		this.width = l.width;
		this.height = l.height;
		this.blocks_available = l.blocks_available;
		this.field = l.field;
		this.md = l.md;
		this.maze = l.maze;
		this.scoreHandler = l.scoreHandler;
		statistics = l.statistics;
		startTime = l.startTime;
	}

	protected int getLocalLongestPath() {
		return statistics.localLongestPath;
	}
	
	protected final boolean checkLongestPath(final int path, final AbstractSolver l) {
		if (path > l.statistics.localLongestPath) {
			l.statistics.localLongestPath = path;
			statistics.localBestFoundAt = System.currentTimeMillis();
			return scoreHandler.checkLongestPathSyncronized(path, l,
					l.startTime);
		}
		return false;
	}
	
	protected int test(final Field lastBlocked, final SubPathCache map) {

		checkCancelled();

		statistics.tests++;

		int p = -1;

		if (USE_CACHE) {
			char[] walls = maze.getCompressedIDsOfWalls();
			ArrayWrapper aw = new ArrayWrapper(walls);
			int cacheResult = cache.get(aw);
			if (cacheResult == 0) {
				if (md.isNonDefault()) {
					p = testNonDefault(md);
				} else {
					p = MazeTester21.testPath(maze, lastBlocked, map,
							md.getTypes());
//					int x = MazeTester22.testPath(maze, lastBlocked, map, md.getTypes());
//					if(x!=p) {
//						throw new RuntimeException(p + " != " + x);
//					}
//					int p2 = MazeTester9.testPath(maze, lastBlocked, map,
//							md.getTypes());
//					int p3 = MazeTester10.testPath(maze, lastBlocked, map,
//							md.getTypes());
//					int p4 = MazeTester11.testPath(maze, lastBlocked, map,
//							md.getTypes());
//					int p5 = MazeTester19.testPath(maze, lastBlocked, map,
//							md.getTypes());
//					if(p != p2 || p != p3 || p!=p4 || p!=p5) {
//						throw new RuntimeException("Crap. " + p + " " + p2 + " " + p3 + " " + p4 + " " + p5);
//					}
				}
				cache.put(aw, p);
				// TODO cache should handle its size itself?
//				if (cache.size() > CACHE_SIZE - 10) {
//					cache = new CacheMapInt(CACHE_SIZE, 1f);
//				}
			} else {
				statistics.cacheHit++;
				p = cacheResult;
			}
		} else {
			if (md.isNonDefault()) {
				p = testNonDefault(md);
			} else {
				p = MazeTester21.testPath(maze, lastBlocked, map, md.getTypes());
			}
		}

		return p;
	}

	private final int testNonDefault(MazeDescriptor md) {

		Type[] types = md.getTypes();
		Field[] greens = maze.get(Type.GREEN_ONLY);
		Field[] reds = maze.get(Type.RED_ONLY);

		setType(greens, Type.PATH, -1);
		setType(reds, Type.BLOCKED_BY_USER, Field.pred_constant);

		int p = MazeTester21.testPath(maze, null, null, types);
//		int x = MazeTester22.testPath(maze, null, null, md.getTypes());
//		if(x!=p) {
//			throw new RuntimeException(p + " != " + x);
//		}
		if (p > -1) {
			setType(greens, Type.BLOCKED_BY_USER, Field.pred_constant);
			setType(reds, Type.PATH, 0);

			Type[] tfr = md.getTypesForRedStart();

			int temp = MazeTester21.testPath(maze, null, null, tfr);
//			int x2 = MazeTester22.testPath(maze, null, null, md.getTypes());
//			if(x2!=temp) {
//				throw new RuntimeException(temp + " != " + x2);
//			}
			if (temp > -1) {
				p += temp;
			} else {
				p = -1;
			}
		}
		setType(greens, Type.GREEN_ONLY, 0);
		setType(reds, Type.RED_ONLY, 0);

		return p;
	}
	
	protected Field[] toShuffledArray(FieldSet s) {
//		s.remove(null); // yes thats a small hack
		Field[] result = s.getArray(maze);
		for (int i = result.length; i > 1; i--) {
			int j = RANDOM.nextInt(i);
			Field tmp = result[i - 1];
			result[i - 1] = result[j];
			result[j] = tmp;
		}
		return result;
	}
	
	protected int retrievePath(boolean ignoreTPsForList, FieldCollection path) {

		statistics.tests++;

		int p = -1;
		if (md.isNonDefault()) {
			p = retrieveNonDefault(ignoreTPsForList, path, md);
		} else {
			p = MazeTester21.retrievePath(maze, ignoreTPsForList, path,
					md.getTypes());
		}
		return p;
	}
	
	private final int retrieveNonDefault(boolean ignoreTPsForList,
			FieldCollection list, MazeDescriptor md) {
		Type[] types = md.getTypes();
		Field[] greens = maze.get(Type.GREEN_ONLY);
		Field[] reds = maze.get(Type.RED_ONLY);

		setType(greens, Type.PATH, -1);
		setType(reds, Type.BLOCKED_BY_USER, Field.pred_constant);

		int p = MazeTester21.retrievePath(maze, ignoreTPsForList, list, types);

		if (p > -1) {
			setType(greens, Type.BLOCKED_BY_USER, Field.pred_constant);
			setType(reds, Type.PATH, 0);

			Type[] tfr = md.getTypesForRedStart();

			int temp = MazeTester21.retrievePath(maze, ignoreTPsForList, list,
					tfr);
			if (temp > -1) {
				p += temp;
			} else {
				p = -1;
			}
		}
		setType(greens, Type.GREEN_ONLY, 0);
		setType(reds, Type.RED_ONLY, 0);

		return p;
	}

	protected void setType(Field[] fa, Type t, final int pred) {
		if (pred != -1) {
			for (Field f : fa) {
				f.setType(t, maze);
				f.precedessor = pred;
			}
		} else {
			for (Field f : fa) {
				f.setType(t, maze);
			}
		}
	}

	public void cancel() {
		// nasty hack for thread primitive caching problem
		ex = new CanceledException();
	}

	protected final void checkCancelled() {
		if (ex!=null) {
			throw ex;
		}
	}

	protected final String calcPossibilities(int length) {
		BigInteger result = BigInteger.ONE;
		for (int i = 0; i < blocks_available; i++) {
			result = result.multiply(BigInteger.valueOf(length - i));
		}
		return result.toString();
	}

	public final void print() {
		for (int i = 0; i < height; i++) {
			for (int j = 0; j < width; j++) {
				System.out.print(" " + field[i][j].getType().getLiteral());
			}
			System.out.print("\n");
		}
	}

	public final void printPermutationCount() {
		System.err.println("Permutations: "
				+ calcPossibilities(maze.getFieldsForType(Type.NORMAL).length));
	}

	public Maze getMaze() {
		return maze;
	}

	public MazeDescriptor getMd() {
		return md;
	}
	
	public void updateStatistics(ThreadStatistics ts) {
		ts.setBestSolution(statistics.localLongestPath, statistics.localBestFoundAt);
		ts.addCached(statistics.cacheHit);
		ts.addTests(statistics.tests);
	}
	
	public abstract void doStuff();
}
