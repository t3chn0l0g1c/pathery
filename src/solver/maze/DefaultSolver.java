package solver.maze;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import solver.cache.CacheMap;
import solver.cache.SubPathCache;
import solver.input.MazeDescriptor;
import solver.util.Coordinate;
import solver.util.FieldCollection.FieldSet;
import solver.util.Statistics;
import solver.util.ThreadStatistics;
import solver.util.Type;

/**
 * Level like "game field", since field was already taken...
 */
public class DefaultSolver extends AbstractSolver {

	// TODO make flags given to object
	public static final boolean EXPERIMENTAL_HEAVY_RE_USE = false;

	public static final boolean RE_USE_MAZE = true;

	public static final boolean DOUBLE_RECURSE = true;
	public static final int DR_LIMIT = 2;

	public static final boolean MARK_BLOCKING = true;

	private boolean doomed = false;

	private boolean isDoubleRecurse = false;

	private int factor = 0;
	private final int threadNr;

	private final Priorizer priorizer;

	private final boolean neverEnd;

	final Statistics statistics;
	
	public DefaultSolver(MazeDescriptor md, int threadNr, HighscoreHandler scoreHandler, boolean neverEnd) {
		super(md, scoreHandler);
		statistics = new Statistics();
		this.threadNr = threadNr;
		priorizer = new Priorizer(maze, field, RANDOM);
		this.neverEnd = neverEnd;
	}

	public boolean isDoubleRecurse() {
		return isDoubleRecurse;
	}

	public final void iterate() {
		cache = CacheMap.createCacheMap();
		setRandomFields();
		statistics.iterations++;
		test(false, true, null, null);
	}

	private final int reUse() {
		int i = 0;
		final List<Coordinate> current_best = scoreHandler.getCurrentBest();
		if (current_best == null) {
			return 0;
		}
		int rndCount = 0;
		if (EXPERIMENTAL_HEAVY_RE_USE) {
			rndCount = current_best.size() / 2 + (int) (Math.random() * (current_best.size() / 2 - 2));
		} else {
			rndCount = (int) (Math.random() * (current_best.size() - 2));
		}
		statistics.reUseCounts++;
		statistics.reUsedBlocks += rndCount;
		Collections.shuffle(current_best);
		while (rndCount > 0) {
			Coordinate c = current_best.remove(current_best.size() - 1);
			field[c.y][c.x].setType(Type.BLOCKED_BY_USER, maze);
			field[c.y][c.x].precedessor = Field.pred_constant;
			priorizer.reCalculatePriorities(field[c.y][c.x]);
			i++;
			rndCount--;
		}
		return i;
	}

	public final void setRandomFields() {
		setFieldsToType(Type.BLOCKED_BY_USER, Type.NORMAL);

		priorizer.resetPriorities();
		maze.resetFields();
		int i = 0;
		if (RE_USE_MAZE) {
			i = reUse();
		}
		for (; i < blocks_available; i++) {
			checkCancelled();
			Field f = priorizer.getRandomNormal();
			while (f == null || f.getType() != Type.NORMAL) {
				f = priorizer.getRandomNormal();
			}
			f.setType(Type.BLOCKED_BY_USER, maze);
			f.precedessor = Field.pred_constant;
			priorizer.reCalculatePriorities(f);
		}
	}

	protected int test(final boolean dontRecurse, final boolean recurseOnceMore, final Field lastBlocked, final SubPathCache map) {

		final int p = super.test(lastBlocked, map);
		if (!dontRecurse && p > -1) {
			if (checkLongestPath(p, this) || p > factor) {
				factor = p;
				recurse(true);
			} else if (EXPERIMENTAL_ADDITIONAL_RECURSE && recurseOnceMore && p == factor) {
				recurse(false);
			}
		}
		return p;
	}


	public final void recurse(final boolean recurseOnceMore) {
		final List<Field> current = Arrays.asList(maze.getFieldsForType(Type.BLOCKED_BY_USER));
		Collections.shuffle(current, RANDOM);

		for (final Field f : current) {
			f.setType(Type.NORMAL, maze);
			f.precedessor = 0;
			// TODO replace by properly sized arrayList?
			// duplicate/nulls will be filtered out in toShuffledArray
//			.
			FieldSet s = new FieldSet(maze.numberOfAllFields+1);
			retrievePath(true, s);
			final SubPathCache m = MazeTester21.retrievePathCache(maze, md.getTypes());
			final Field[] currentPath = toShuffledArray(s);
			final int[] blocking = new int[maze.allFieldsInclNull.length];
			for (final Field fi : currentPath) {
				final boolean blocks = MARK_BLOCKING && blocking[fi.id] != 0;
				if (blocks) {
					statistics.saved++;
				}
				if (fi.getType() == Type.NORMAL && !blocks) {
					fi.setType(Type.BLOCKED_BY_USER, maze);
					fi.precedessor = Field.pred_constant;
					final int temp = test(false, recurseOnceMore, fi, m);
					if (temp == -1 && MARK_BLOCKING) {
						fi.markBLocking(blocking);
					}
					fi.setType(Type.NORMAL, maze);
					fi.precedessor = 0;
				}
			}
			f.setType(Type.BLOCKED_BY_USER, maze);
			f.precedessor = Field.pred_constant;
		}
	}

	public final void bruteForce() {
		int count = 0;
		boolean didDoubleRecurse = false;
		while (neverEnd || !doomed) {
			final int currentHighscore = scoreHandler.getHighScore();
			iterate();
			factor = 0;
			if (currentHighscore == getLocalLongestPath()) { // means no improvement
				count++;
			} else {
				count = 0;
				didDoubleRecurse = false;
				doomed = false;
			}
			if (DOUBLE_RECURSE && count >= DR_LIMIT && !didDoubleRecurse) {
				count = 0;
				didDoubleRecurse = true;
				DoubleRecurseSolver dr = new DoubleRecurseSolver(this, threadNr, currentHighscore);
				dr.doDoubleRecurse();
				if (getLocalLongestPath() > currentHighscore) {
					didDoubleRecurse = false;
					doomed = false;
				} else {
					doomed = true;
					System.err.println("Thread " + threadNr + " says we´re doomed");
				}
			}
		}
	}

	private final void setFieldsToType(Type isType, Type willBeType) {
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
				if (field[i][j].getType() == isType) {
					field[i][j].setType(willBeType, maze);
					if (willBeType == Type.NORMAL) {
						field[i][j].precedessor = 0;
					}
					if (willBeType == Type.BLOCKED_BY_USER) {
						field[i][j].precedessor = Field.pred_constant;
					}
				}
			}
		}
	}

	public void updateStatistics(ThreadStatistics ts) {
		super.updateStatistics(ts);
		ts.addReUseCount(statistics.reUseCounts);
		ts.addReUsedBlocks(statistics.reUsedBlocks);
		ts.addSaved(statistics.saved);
	}

	
	@Override
	public void doStuff() {
		bruteForce();
	}
}
