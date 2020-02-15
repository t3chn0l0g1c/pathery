package solver.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solver.cache.CacheMap;
import solver.cache.SubPathCache;
import solver.input.MazeDescriptor;
import solver.util.Coordinate;
import solver.util.DRCoords;
import solver.util.FieldCollection.FieldSet;
import solver.util.Statistics;
import solver.util.ThreadStatistics;
import solver.util.Type;

/**
 * Level like "game field", since field was already taken...
 */
public class TrippleRecurseSolver extends AbstractSolver {

	
	// TODO make flags given to object
	public static final boolean EXPERIMENTAL_ADDITIONAL_RECURSE = true;	// NOTE very very very expensive...
	
	public static final boolean DOUBLE_RECURSE = true;
	public static final int DR_LIMIT = 2;
	
	public static final boolean MARK_BLOCKING = true;
	
	private final int threadNr;
	
	private int factor = 0;
	
	private final List<Coordinate> current_best_solution;
	private final List<Coordinate> drCoords;
	
	private final boolean flatRecurse;
	
	private final Statistics statistics;
	
	public TrippleRecurseSolver(MazeDescriptor md, int threadNr, HighscoreHandler scoreHandler, boolean flatRecurse, int length) {
		super(md, scoreHandler);
		this.threadNr = threadNr;
		this.flatRecurse = flatRecurse;
		statistics = new Statistics();
		DRCoords d = init(length);
		current_best_solution = d.getCurrent_best();
		drCoords = d.getDrCoords();
	}
	
	public TrippleRecurseSolver(DefaultSolver l, int threadNr, int length) {
		super(l);
		this.threadNr = threadNr;
		flatRecurse = false;
		statistics = l.statistics;
		DRCoords d = init(length);
		current_best_solution = d.getCurrent_best();
		drCoords = d.getDrCoords();
	}
	
	private DRCoords init(int length) {
		scoreHandler.checkLongestPathSyncronized(length, this, startTime);
		DRCoords d = scoreHandler.getDoubleRecurseChunk();
		if(d==null) {
			return new DRCoords(null, null);
		}
		return d;
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
	
	@Override
	public void doStuff() {
		doTrippleRecurse();
	}
	
	public final void recurse(final boolean recurseOnceMore) {
		final List<Field> current = Arrays.asList(maze.getFieldsForType(Type.BLOCKED_BY_USER));
		Collections.shuffle(current);
		
		for (final Field f : current) {
			f.setType(Type.NORMAL, maze);
			f.precedessor = 0;
			FieldSet s = new FieldSet(maze.numberOfAllFields+1);
			retrievePath(true, s);
			final SubPathCache m = MazeTester21.retrievePathCache(maze, md.getTypes());
			final Field[] currentPath = toShuffledArray(s);
			final int[] blocking = new int[maze.allFieldsInclNull.length];
			for(final Field fi : currentPath) {
				final boolean blocks = MARK_BLOCKING && blocking[fi.id]!=0;
				if(blocks) {
					statistics.saved++;
				}
				if(fi.getType()==Type.NORMAL && !blocks) {
					fi.setType(Type.BLOCKED_BY_USER, maze);
					fi.precedessor = Field.pred_constant;
					final int temp = test(flatRecurse, recurseOnceMore, fi, m);
					if(temp==-1 && MARK_BLOCKING) {
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
	
	public void doTrippleRecurse() {

		if(current_best_solution==null || drCoords==null) {
			return;
		}
		
		setFieldsToType(Type.BLOCKED_BY_USER, Type.NORMAL);
		
		for(Coordinate c : current_best_solution) {
			field[c.y][c.x].precedessor = Field.pred_constant;
			field[c.y][c.x].setType(Type.BLOCKED_BY_USER, maze);
		}
		
		recurseTripple();
		
		setFieldsToType(Type.BLOCKED_BY_USER, Type.NORMAL);
	}
	
	private void recurseTripple() {
		cache = CacheMap.createCacheMap();
		System.out.println("RecurseWorker " + threadNr + " starting trippleRecurse");
		final Set<Field> subSet = new HashSet<Field>();
		for(final Coordinate c : drCoords) {
			final Field f = field[c.y][c.x];
			if(f.getType()!=Type.BLOCKED_BY_USER) {
				throw new RuntimeException("Invalid coordinate: " + c);
			}
			subSet.add(f);
		}
		
		int c = 0;
		final Set<Field> current = new HashSet<Field>(Arrays.asList(maze.getFieldsForType(Type.BLOCKED_BY_USER)));
		final Set<Field> current2 = new HashSet<Field>(Arrays.asList(maze.getFieldsForType(Type.BLOCKED_BY_USER)));
		
		final List<Field> subSetList = new ArrayList<Field>(subSet);
		Collections.shuffle(subSetList);
		for (final Field f : subSetList) {
			f.setType(Type.NORMAL, maze);
			f.precedessor = 0;
			for(final Field f2 : current) {
				if(f2==f) {
					continue;
				}
				f2.setType(Type.NORMAL, maze);
				f2.precedessor = 0;
				for(final Field f3 : current2) {
					if(f3==f || f3==f2) {
						continue;
					}
					f3.setType(Type.NORMAL, maze);
					f3.precedessor = 0;
					FieldSet s = new FieldSet(maze.numberOfAllFields+1);
					retrievePath(true, s);
					Field[] currentPath = toShuffledArray(s);
//					Collections.shuffle(currentPath);
					for(final Field fi : currentPath) {
						if(fi.getType()==Type.NORMAL) {
							fi.setType(Type.BLOCKED_BY_USER, maze);
							fi.precedessor = Field.pred_constant;
							FieldSet s2 = new FieldSet(maze.numberOfAllFields+1);
							retrievePath(true, s2);
							Field[] currentPath2 = toShuffledArray(s2);
//							Collections.shuffle(currentPath2);
							for(final Field fi2 : currentPath2) {
								
								if(fi2.getType()==Type.NORMAL) {
									fi2.setType(Type.BLOCKED_BY_USER, maze);
									fi2.precedessor = Field.pred_constant;
									FieldSet s3 = new FieldSet(maze.numberOfAllFields+1);
									retrievePath(true, s3);
									final SubPathCache m = MazeTester21.retrievePathCache(maze, md.getTypes());
									final Field[] currentPath3 = toShuffledArray(s3);
									final int[] blocking = new int[maze.allFieldsInclNull.length];
									for(final Field fi3 : currentPath3) {
										final boolean blocks = MARK_BLOCKING && blocking[fi3.id]!=0;
										if(blocks) {
											statistics.saved++;
										}
										if(fi3.getType()==Type.NORMAL) {
											fi3.setType(Type.BLOCKED_BY_USER, maze);
											fi3.precedessor = Field.pred_constant;
											final int temp = test(flatRecurse, true, fi3, m);
											if(temp==-1 && MARK_BLOCKING) {
												fi3.markBLocking(blocking);
											}
											fi3.setType(Type.NORMAL, maze);
											fi3.precedessor = 0;
										}
									}
									fi2.setType(Type.NORMAL, maze);
									fi2.precedessor = 0;
								}
							}
							fi.setType(Type.NORMAL, maze);
							fi.precedessor = 0;
						}
					}
					f3.setType(Type.BLOCKED_BY_USER, maze);
					f3.precedessor = Field.pred_constant;
				}
				f2.setType(Type.BLOCKED_BY_USER, maze);
				f2.precedessor = Field.pred_constant;
			}

			f.setType(Type.BLOCKED_BY_USER, maze);
			f.precedessor = Field.pred_constant;
			factor = 0;
			c++;
			System.out.println("RecurseWorker " + threadNr + " finished " + c + " of " + subSet.size());
		}
		cache = CacheMap.createCacheMap();
	}


	private final void setFieldsToType(Type isType, Type willBeType) {
		for(int i = 0; i<field.length; i++) {
			for(int j = 0; j<field[i].length; j++) {
				if(field[i][j].getType() == isType) {
					field[i][j].setType(willBeType, maze);
					if(willBeType==Type.NORMAL) {
						field[i][j].precedessor = 0;
					}
					if(willBeType==Type.BLOCKED_BY_USER) {
						field[i][j].precedessor = Field.pred_constant;
					}
				}
			}
		}
	}
	
	public void updateStatistics(ThreadStatistics ts) {
		super.updateStatistics(ts);
		ts.addSaved(statistics.saved);
	}

}
