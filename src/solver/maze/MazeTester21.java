package solver.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import solver.cache.SubPath;
import solver.cache.SubPathCache;
import solver.cache.SubPathCache.SubPathCacheBuilder;
import solver.util.FieldCollection;
import solver.util.Type;

public class MazeTester21 {

	private static CacheResult askSubPathCache(SubPathCache cache, Field[] currentTarget, int lastBlockedId,
			Maze maze) {
//		if(lastBlocked!=null && cache!=null) {
		CacheResult cr = new CacheResult();
		cr.currentTarget = currentTarget;
		final SubPath[] cA = cache.get(currentTarget[0].id);
//		boolean hit = true;
		if (cA != null) {
			for (SubPath c : cA) {
//					final SubPath c = cA[subPathIndex];
				if (c != null && c.tpMask == maze.getTPMask() && c.path[lastBlockedId - 1] == false) {
					cr.resultIncrease += c.length;
					cr.currentTarget = c.endField;
					maze.setTPMask(c.endTPMask);
				} else {
//						hit = false;
					return cr;
				}
			}
//				if(hit) {
//					continue;
//				}
			cr.fullSuccess = true;
		}
		return cr;
//		}
	}

	private static class CacheResult {
		boolean fullSuccess;
		int resultIncrease;
		Field[] currentTarget;
	}

	/**
	 * Returns the path length.
	 */
	public final static int testPath(final Maze maze, final Field lastBlocked, final SubPathCache cache,
			final Type[] types) {
		maze.resetTPUsage();
		int result = 0;
		final Field[] data = new Field[maze.getResettableFields().length];
		Field[] currentTarget = maze.get(types[0]);
		int i = 0;
		boolean askSubCachePath = true;
		boolean cacheEnabled = lastBlocked!=null && cache!=null;
		do {
			if(askSubCachePath && cacheEnabled) {
				CacheResult cr = askSubPathCache(cache, currentTarget, lastBlocked.id, maze);
				result += cr.resultIncrease;
				currentTarget = cr.currentTarget;
				if(cr.fullSuccess) {
					continue;
				}
			}
			int blocked_offset = maze.getAndIncrementOffset();
			int size = 0;
			for (; size < currentTarget.length; size++) {
				final Field f = currentTarget[size];
				f.precedessor = blocked_offset;
				data[size] = f;
			}

			final Field temp = testPath(maze, types[i + 1], data, size, blocked_offset);
			if (temp != null) {
				result += temp.resultLength;
				if (Type.isTP_IN(temp.getType())) {
					currentTarget = maze.getTP_OUTForField(temp.getType());
					i--;
					askSubCachePath = false;
				} else {
					currentTarget = new Field[] { temp };
					askSubCachePath = true;
				}
			} else {
				result = -1;
				i = types.length;
			}
		} while (++i < types.length - 1);

		return result;
	}

	final static Field testPath(final Maze maze, final Type target, final Field[] data, int size,
			final int blocked_offset) {

		for (int iterationEnd = 0, iteration = 0, index = 0; iterationEnd < size; iteration++) {
			iterationEnd = size;
			while (index < iterationEnd) {
				final Field f = data[index];
				if (f.getType() == target) {
					return doFinishTest(maze, iteration, f, blocked_offset);
				}

				if (f.up.precedessor < blocked_offset) {
					f.up.precedessor = f.id + blocked_offset;
					data[size++] = f.up;
				}

				if (f.right.precedessor < blocked_offset) {
					f.right.precedessor = f.id + blocked_offset;
					data[size++] = f.right;
				}

				if (f.down.precedessor < blocked_offset) {
					f.down.precedessor = f.id + blocked_offset;
					data[size++] = f.down;
				}

				if (f.left.precedessor < blocked_offset) {
					f.left.precedessor = f.id + blocked_offset;
					data[size++] = f.left;
				}
				index++;
			}
		}
		return null;
	}

	private final static Field doFinishTest(final Maze maze, final int iteration, Field f, final int blocked_offset) {

		f.resultLength = iteration;

		if (maze.unusedTPsLeft()) {
			final int usedTPMask = maze.getTPMask();
			Field pred = maze.allFieldsInclNull[f.precedessor - blocked_offset];
			for (int i = iteration - 1; i >= 0; i--) {
				if ((usedTPMask | pred.getType().bitMask) != usedTPMask) {
					f = pred;
					f.resultLength = i;
				}
				pred = maze.allFieldsInclNull[pred.precedessor - blocked_offset];
			}
		}
		return f;
	}

	/**
	 * Returns the Fields used in the path
	 */
	public final static int retrievePath(Maze maze, boolean ignoreTPsForList, FieldCollection path,
			final Type... types) {
		maze.resetTPUsage();
		int result = 0;
		final Field[] data = new Field[maze.getResettableFields().length];
		Field[] currentTarget = maze.get(types[0]);
		for (int i = 0; i < types.length - 1; i++) {
			maze.resetFields();
			int size = 0;

			for (final Field f : currentTarget) {
				f.precedessor = f.id;
				data[size++] = f;
			}

			final Field temp = retrievePath(maze, ignoreTPsForList, path, types[i + 1], maze.unusedTPsLeft(), data,
					size);

			if (temp != null) {
				result += temp.resultLength;
				if (temp.getType() == types[i + 1]) {
					currentTarget = new Field[] { temp };
				} else {
					currentTarget = maze.get(types[i + 1]);
				}
			} else {
				return -1;
			}
		}
		return result;
	}

	/**
	 * Returns the Fields used in the path TODO create subPathCaches
	 */
	public final static SubPathCache retrievePathCache(final Maze maze, final Type... types) {
		maze.resetTPUsage();
		final Field[] data = new Field[maze.getResettableFields().length];
		Field[] currentTarget = maze.get(types[0]);
		SubPathCacheBuilder s = new SubPathCacheBuilder();
		for (int i = 0; i < types.length - 1; i++) {
			maze.resetFields();
			int size = 0;

			for (final Field f : currentTarget) {
				f.precedessor = f.id;
				data[size++] = f;
			}

			final SubPath[] temp = retrievePathCache(maze, types[i + 1], maze.unusedTPsLeft(), data, size);

			if (temp != null) {
				s.put(currentTarget[0].id, temp);
				currentTarget = temp[temp.length - 1].endField;
			} else {
				break;
			}

		}
		return s.toCache();
	}

	private final static SubPath[] retrievePathCache(final Maze maze, final Type target, final boolean tpsLeft,
			final Field[] data, int size) {

		for (int iterationEnd = 0, iteration = 0, index = 0; iterationEnd < size; iteration++) {
			iterationEnd = size;
			while (index < iterationEnd) {
				final Field f = data[index];
				if (f.getType() == target) {
					return doFinishRetrievePathCache(maze, target, tpsLeft, iteration, f, data);
				}
				if (f.up.precedessor == 0) {
					f.up.precedessor = f.id;
					data[size++] = f.up;
				}
				if (f.right.precedessor == 0) {
					f.right.precedessor = f.id;
					data[size++] = f.right;
				}
				if (f.down.precedessor == 0) {
					f.down.precedessor = f.id;
					data[size++] = f.down;
				}
				if (f.left.precedessor == 0) {
					f.left.precedessor = f.id;
					data[size++] = f.left;
				}
				index++;
			}
		}

		return null;
	}

	private final static Field retrievePath(Maze maze, boolean ignoreTPsForList, FieldCollection path, Type target,
			boolean tpsLeft, Field[] data, int size) {

		for (int iterationEnd = 0, iteration = 0, index = 0; iterationEnd < size; iteration++) {
			iterationEnd = size;
			while (index < iterationEnd) {
				final Field f = data[index];
				if (f.getType() == target) {
					return doFinishRetrieve(maze, ignoreTPsForList, path, target, tpsLeft, iteration, f, data);
				}
				if (f.up.precedessor == 0) {
					f.up.precedessor = f.id;
					data[size++] = f.up;
				}
				if (f.right.precedessor == 0) {
					f.right.precedessor = f.id;
					data[size++] = f.right;
				}
				if (f.down.precedessor == 0) {
					f.down.precedessor = f.id;
					data[size++] = f.down;
				}
				if (f.left.precedessor == 0) {
					f.left.precedessor = f.id;
					data[size++] = f.left;
				}
				index++;
			}
		}

		return null;
	}

	private final static Field doFinishRetrieveTP(Maze maze, Type target, int i, Field[] data, Type pType,
			boolean ignoreTPsForList, FieldCollection path, Field[] list) {
		for (int j = 0; j <= i; j++) {
			path.add(list[j]);
		}
		maze.resetFields();
		int size = 0;
		for (final Field f : maze.getTP_OUTForField(pType)) {
			f.precedessor = f.id;
			data[size++] = f;
		}
		final Field t = retrievePath(maze, ignoreTPsForList, path, target, maze.unusedTPsLeft(), data, size);
		if (t == null) {
			return null;
		}
		t.resultLength += i;
		return t;
	}

	private final static SubPath[] doFinishRetrieveTPPathCache(final Maze maze, final Type target, final int i,
			final Field[] data, final Type pType, final Field[] list) {
		List<SubPath> subPaths = new ArrayList<SubPath>();
		maze.resetFields();
		int size = 0;
		int tpMask = maze.getTPMask();
		final Field[] out = maze.getTP_OUTForField(pType);
		for (final Field f : out) {
			f.precedessor = f.id;
			data[size++] = f;
		}
		boolean[] ba = new boolean[maze.numberOfAllFields];
		for (Field field : list) {
			ba[field.id - 1] = true;
		}
		SubPath p = new SubPath(ba, i, tpMask, maze.getTPMask(), out);
		subPaths.add(p);
		final SubPath[] t = retrievePathCache(maze, target, maze.unusedTPsLeft(), data, size);
		if (t != null) {
			subPaths.addAll(Arrays.asList(t));
		}
		return subPaths.toArray(new SubPath[subPaths.size()]);
	}

	private final static SubPath[] doFinishRetrievePathCache(final Maze maze, final Type target, final boolean tpsLeft,
			final int iteration, final Field f, final Field[] data) {
		final Field[] list = new Field[iteration + 1];
		Field pred = f;
		for (int i = iteration; i >= 0; i--) {
			list[i] = pred;
			pred = maze.allFieldsInclNull[pred.precedessor];
		}

		List<SubPath> subPaths = new ArrayList<SubPath>();
		if (tpsLeft) {
			final int usedTPMask = maze.getTPMask();
			for (int i = 0; i < list.length; i++) {
				final Type pType = list[i].getType();
				if ((usedTPMask | pType.bitMask) != usedTPMask) {
					SubPath[] array = doFinishRetrieveTPPathCache(maze, target, i, data, pType, list);
					subPaths.addAll(Arrays.asList(array));
					break;
				}
			}
		}
		if (subPaths.isEmpty()) {
			boolean[] ba = new boolean[maze.numberOfAllFields];
			for (Field field : list) {
				ba[field.id - 1] = true;
			}
			SubPath p = new SubPath(ba, iteration, maze.getTPMask(), maze.getTPMask(), new Field[] { f });
			subPaths.add(p);
		}
		return subPaths.toArray(new SubPath[subPaths.size()]);
	}

	private final static Field doFinishRetrieve(Maze maze, boolean ignoreTPsForList, FieldCollection path, Type target,
			boolean tpsLeft, int iteration, Field f, Field[] data) {

		final Field[] list = new Field[iteration + 1];
		Field pred = f;
		for (int i = iteration; i >= 0; i--) {
			list[i] = pred;
			pred = maze.allFieldsInclNull[pred.precedessor];
		}

		if (ignoreTPsForList) {
			path.addAll(list);
		}
		if (tpsLeft) {
			final int usedTPMask = maze.getTPMask();
			for (int i = 0; i < list.length; i++) {
				final Type pType = list[i].getType();
				if ((usedTPMask | pType.bitMask) != usedTPMask) {
					return doFinishRetrieveTP(maze, target, i, data, pType, ignoreTPsForList, path, list);
				}
			}
		}
		if (!ignoreTPsForList) {
			path.addAll(list);
		}
		f.resultLength = iteration;
		return f;
	}

}
