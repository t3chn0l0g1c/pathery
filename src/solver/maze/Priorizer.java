package solver.maze;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import solver.util.Type;
import solver.util.Util;

public class Priorizer {

	private final Field[][] field;
	private final Maze maze;
	private final int height;
	private final int width;
	private final Random RANDOM;

	private Field[] prio0 = null;
	private Field[] prio1 = null;
	private Field[] prio2 = null;
	private Field[] prio3 = null;
	private Field[] prioInvalid = null;

	private List<Field> volatilePrio0 = null;
	private List<Field> volatilePrio1 = null;
	private List<Field> volatilePrio2 = null;
	private List<Field> volatilePrio3 = null;
	
	public Priorizer(Maze maze, Field[][] field, Random random) {
		this.field = field;
		this.maze = maze;
		height = maze.getHeight();
		width = maze.getWidth();
		this.RANDOM = random;
		assignPriorities();
		initPrios();
	}

	public final Field getRandomNormal() {

		if (prio0 == null) {
			initPrios();
		}

		int p = (int) (Math.random() * getRandomFactor());
		if (p > 5 && volatilePrio3.size() > 0) {
			return Util.getRandom(volatilePrio3, RANDOM);
		}
		if (p > 2 && volatilePrio2.size() > 0) {
			return Util.getRandom(volatilePrio2, RANDOM);
		} 
		if (p > 0 && volatilePrio1.size() > 0) {
			return Util.getRandom(volatilePrio1, RANDOM);
		} 
		if(volatilePrio0.size() > 0){
			return Util.getRandom(volatilePrio0, RANDOM);
		}

		return null;
	}
	
	public final void resetPriorities() {
		volatilePrio0 = resetPrio(prio0, 0);
		volatilePrio1 = resetPrio(prio1, 1);
		volatilePrio2 = resetPrio(prio2, 2);
		volatilePrio3 = resetPrio(prio3, 3);
		for(Field f : prioInvalid) {
			f.priority = -1;
		}
	}
	
	private List<Field> resetPrio(Field[] a, int prio) {
		List<Field> result = new ArrayList<Field>();
		for (Field f : a) {
			result.add(f);
			f.priority = prio;
		}
		return result;
	}

	public final void reCalculatePriorities(Field current) {
		final int i = (current.id-1)/(field.length)-1;
		final int j = (current.id-1)%(field.length)-1;
		if (exists(i - 1, j - 1)) {
			removeFieldFromList(field[i - 1][j - 1]);
			addFieldToList(field[i - 1][j - 1], i - 1, j - 1);
		}
		if (exists(i - 1, j)) {
			removeFieldFromList(field[i - 1][j]);
			addFieldToList(field[i - 1][j], i - 1, j);
		}
		if (exists(i - 1, j + 1)) {
			removeFieldFromList(field[i - 1][j + 1]);
			addFieldToList(field[i - 1][j + 1], i - 1, j + 1);
		}
		if (exists(i, j - 1)) {
			removeFieldFromList(field[i][j - 1]);
			addFieldToList(field[i][j - 1], i, j - 1);
		}
		if (exists(i, j + 1)) {
			removeFieldFromList(field[i][j + 1]);
			addFieldToList(field[i][j + 1], i, j + 1);
		}
		if (exists(i + 1, j - 1)) {
			removeFieldFromList(field[i + 1][j - 1]);
			addFieldToList(field[i + 1][j - 1], i + 1, j - 1);
		}
		if (exists(i + 1, j)) {
			removeFieldFromList(field[i + 1][j]);
			addFieldToList(field[i + 1][j], i + 1, j);
		}
		if (exists(i + 1, j + 1)) {
			removeFieldFromList(field[i + 1][j + 1]);
			addFieldToList(field[i + 1][j + 1], i + 1, j + 1);
		}
	}

	protected final boolean exists(int i, int j) {
		return (i>=0&&i<field.length && j>=0 && j < field[i].length);
	}
	
	
	private void addFieldToList(Field f, int i, int j) {
		f.priority = calculatePriority(f, i, j);
		switch (f.priority) {
		case -1: return;
		case 0: volatilePrio0.add(f); return;
		case 1: volatilePrio1.add(f); return;
		case 2: volatilePrio2.add(f); return;
		case 3: volatilePrio3.add(f); return;
		}
	}

	private void removeFieldFromList(Field f) {
		switch (f.priority) {
		case -1: return;
		case 0: volatilePrio0.remove(f); return;
		case 1: volatilePrio1.remove(f); return;
		case 2: volatilePrio2.remove(f); return;
		case 3: volatilePrio3.remove(f); return;
		}
	}
	
	
	private int getRandomFactor() {
		if (volatilePrio3.size() > 0) {
			return 10;
		} else if (volatilePrio2.size() > 0) {
			return 6;
		} else if (volatilePrio1.size() > 0) {
			return 3;
		} else {
			return 1;
		}
	}
	
	private void assignPriorities() {
		for (int i = 0; i < field.length; i++) {
			for (int j = 0; j < field[i].length; j++) {
				field[i][j].setPriority(calculatePriority(field[i][j], i, j));
			}
		}
	}


	private final void initPrios() {
		Field[] normalFields = maze.getFieldsForType(Type.NORMAL);
		List<Field> p0 = new ArrayList<Field>();
		List<Field> p1 = new ArrayList<Field>();
		List<Field> p2 = new ArrayList<Field>();
		List<Field> p3 = new ArrayList<Field>();
		List<Field> pInvalid = new ArrayList<Field>();
		
		for (Field f : normalFields) {
			switch(f.priority) {
			case -1: pInvalid.add(f); break;
			case 0: p0.add(f); break;
			case 1: p1.add(f); break;
			case 2: p2.add(f); break;
			case 3: p3.add(f); break;
			}
		}
		
		Collections.sort(p0);
		prio0 = p0.toArray(new Field[p0.size()]);
		volatilePrio0 = new ArrayList<Field>(p0);

		Collections.sort(p1);
		prio1 = p1.toArray(new Field[p1.size()]);
		volatilePrio1 = new ArrayList<Field>(p1);
		
		Collections.sort(p2);
		prio2 = p2.toArray(new Field[p2.size()]);
		volatilePrio2 = new ArrayList<Field>(p2);

		Collections.sort(p3);
		prio3 = p3.toArray(new Field[p3.size()]);
		volatilePrio3 = new ArrayList<Field>(p3);
		
		prioInvalid = pInvalid.toArray(new Field[pInvalid.size()]);
	}
	
	
	protected final int calculatePriority(Field f, int i, int j) {

		if (f.getType() != Type.NORMAL) {
			return -1;
		}

		int p = 0;
		if (isRedundant(i, j)) {
			return -1;
		} else if (isLineBound(i, j)) {
			return 1;
		} else if (isBetweenLine(i, j)) {
			p = 3;
		} else if (isBetweenRow(i, j)) {
			p = 3;
		} else if (hasOnlyEdges(i, j)) {
			p = 2;
		} else if (touches(i, j)) {
			return 1;
		}
		if (p == 3) {
			if (!hasEdge(i, j)) {
				return 1;
			}
		}
		return p;
	}

	protected final boolean isLineBound(int i, int j) {
		if ((isPrioSignificant(i - 1, j - 1) && isPrioSignificant(i - 1, j) && isPrioSignificant(
				i - 1, j + 1))
				&& !(isPrioSignificant(i + 1, j - 1)
						|| isPrioSignificant(i + 1, j) || isPrioSignificant(
							i + 1, j + 1))) {
			return true;
		}
		if ((isPrioSignificant(i + 1, j - 1) && isPrioSignificant(i + 1, j) && isPrioSignificant(
				i + 1, j + 1))
				&& !(isPrioSignificant(i - 1, j - 1)
						|| isPrioSignificant(i - 1, j) || isPrioSignificant(
							i - 1, j + 1))) {
			return true;
		}
		if ((isPrioSignificant(i - 1, j - 1) && isPrioSignificant(i, j - 1) && isPrioSignificant(
				i + 1, j - 1))
				&& !(isPrioSignificant(i - 1, j + 1)
						|| isPrioSignificant(i, j + 1) || isPrioSignificant(
							i + 1, j + 1))) {
			return true;
		}
		if ((isPrioSignificant(i - 1, j + 1) && isPrioSignificant(i, j + 1) && isPrioSignificant(
				i + 1, j + 1))
				&& !(isPrioSignificant(i - 1, j - 1)
						|| isPrioSignificant(i, j - 1) || isPrioSignificant(
							i + 1, j - 1))) {
			return true;
		}
		return false;
	}

	protected final boolean isBetweenLine(int i, int j) {
		if ((isPrioSignificant(i - 1, j - 1) || isPrioSignificant(i - 1, j) || isPrioSignificant(
				i - 1, j + 1))
				&& (isPrioSignificant(i + 1, j - 1)
						|| isPrioSignificant(i + 1, j) || isPrioSignificant(
							i + 1, j + 1))) {
			return true;
		}
		return false;
	}

	protected final boolean isBetweenRow(int i, int j) {
		if ((isPrioSignificant(i - 1, j - 1) || isPrioSignificant(i, j - 1) || isPrioSignificant(
				i + 1, j - 1))
				&& (isPrioSignificant(i - 1, j + 1)
						|| isPrioSignificant(i, j + 1) || isPrioSignificant(
							i + 1, j + 1))) {
			return true;
		}
		return false;
	}

	protected final boolean hasEdge(int i, int j) {
		if (isPrioSignificant(i - 1, j - 1)) {
			return true;
		} else if (isPrioSignificant(i - 1, j + 1)) {
			return true;
		} else if (isPrioSignificant(i + 1, j - 1)) {
			return true;
		} else if (isPrioSignificant(i + 1, j + 1)) {
			return true;
		} else {
			return false;
		}
	}

	protected final boolean hasOnlyEdges(int i, int j) {
		boolean result = false;
		if (isPrioSignificant(i - 1, j - 1)) {
			result = true;
		} else if (isPrioSignificant(i - 1, j + 1)) {
			result = true;
		} else if (isPrioSignificant(i + 1, j - 1)) {
			result = true;
		} else if (isPrioSignificant(i + 1, j + 1)) {
			result = true;
		}
		if (isPrioSignificant(i, j - 1)) {
			result = false;
		} else if (isPrioSignificant(i, j + 1)) {
			result = false;
		} else if (isPrioSignificant(i + 1, j)) {
			result = false;
		} else if (isPrioSignificant(i - 1, j)) {
			result = false;
		}
		return result;
	}

	protected final boolean isPrioSignificant(int i, int j) {
		if (i >= 0 && i < field.length) {
			if (j >= 0 && j < field[i].length) {
				Type t = field[i][j].getType();
				return t != Type.NORMAL && t != Type.END && t != Type.START;
			}
		}
		return false;
	}

	// could be made so much prettier with lambdas. why though.
	protected final boolean touches(int i, int j) {

		if (isPrioSignificant(i - 1, j - 1)) {
			return true;
		} else if (isPrioSignificant(i - 1, j)) {
			return true;
		} else if (isPrioSignificant(i - 1, j + 1)) {
			return true;
		} else if (isPrioSignificant(i, j - 1)) {
			return true;
		} else if (isPrioSignificant(i, j + 1)) {
			return true;
		} else if (isPrioSignificant(i + 1, j - 1)) {
			return true;
		} else if (isPrioSignificant(i + 1, j)) {
			return true;
		} else if (isPrioSignificant(i + 1, j + 1)) {
			return true;
		} else {
			return false;
		}
	}
	
	// suboptimal. needs extensive recursive check for redundancy
		protected final boolean isRedundant(int i, int j) {
			int count = 0;
			if(isAccessible(i-1, j)) {
				count++;
			}
			if(isAccessible(i+1, j)) {
				count++;
			}
			if(isAccessible(i, j-1)) {
				count++;
			}
			if(isAccessible(i, j+1)) {
				count++;
			}
			if(count==2) {
				count = 0;
				if(isAccessible(i-1, j) && !isRedundant2(i-1, j)) {
					count++;
				}
				if(isAccessible(i+1, j) && !isRedundant2(i+1, j)) {
					count++;
				}
				if(isAccessible(i, j-1) && !isRedundant2(i, j-1)) {
					count++;
				}
				if(isAccessible(i, j+1) && !isRedundant2(i, j+1)) {
					count++;
				}
			}
			return count<=1;
		}
		
		
		protected final boolean isAccessible(int i, int j) {
			if(i>=0 && i<field.length) {
				if(j>=0 && j<field[i].length) {
					Type t = field[i][j].getType();
					return t!=Type.BLOCKED_BY_MAP && t!=Type.BLOCKED_BY_USER;
				}
			}
			return false;
		}

		protected final boolean isRedundant2(int i, int j) {
			int count = 0;
			if(isAccessible(i-1, j)) {
				count++;
			}
			if(isAccessible(i+1, j)) {
				count++;
			}
			if(isAccessible(i, j-1)) {
				count++;
			}
			if(isAccessible(i, j+1)) {
				count++;
			}
			return count<=1;
		}
		
		
		// for debug use
		public final void printPriorities() {
			System.out.println("############ PRIO\n");
			for(int i = 0; i<height; i++) {
				for(int j = 0; j<width; j++) {
					if(field[i][j].priority>0) {
						System.out.print(field[i][j].priority + " ");
					} else {
						System.out.print(field[i][j].getType().getLiteral() + " ");
					}
				}
				System.out.print("\n");
			}
			System.out.println("############\n");
		}
}
