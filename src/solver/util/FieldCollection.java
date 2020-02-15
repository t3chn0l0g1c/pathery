package solver.util;

import java.util.ArrayList;
import java.util.List;

import solver.maze.Field;
import solver.maze.Maze;

public interface FieldCollection {
	public void add(Field f);
	public void addAll(Field[] fa);
	public Field[] getArray(Maze m);
	
	public static class FieldList implements FieldCollection{

		private final Field[] array;
		private int index;
		
		public FieldList(int size) {
			array = new Field[size];
		}
		
		public void add(Field f) {
			array[index++] = f;
		}
		
		public void addAll(Field[] fa) {
			for(Field f : fa) {
				array[index++] = f;
			}
		}
		
		public Field[] getArray(Maze m) {
			Field[] result = new Field[index];
			for(int i = 0; i<index; i++) {
				result[i] = array[i];
			}
			return result;
		}
	}

	public static class FieldSet implements FieldCollection{

		private final int[] array;
//		private int size;
		
		public FieldSet(int size) {
//			this.size = size;
			array = new int[size];
		}

		@Override
		public void add(Field f) {
//			if(array[f.id]==0) {
				array[f.id] = f.id;
//				size++;
//			}
		}

		@Override
		public void addAll(Field[] fa) {
			for(Field f : fa) {
				array[f.id] = f.id;
			}
		}

		@Override
		public Field[] getArray(Maze m) {
			List<Field> list = new ArrayList<>(m.numberOfAllFields);
//			Field[] result = new Field[size];
//			int x = 0;
			for(int i = 0; i<array.length; i++) {
				if(array[i]!=0) {
					list.add(m.allFieldsInclNull[array[i]]);
				}
			}
			return list.toArray(new Field[0]);
		}
		
	}
	
	public static class FieldArrayList implements FieldCollection {
		private final List<Field> list = new ArrayList<>(5000);

		@Override
		public void add(Field f) {
			list.add(f);
		}

		@Override
		public void addAll(Field[] fa) {
			for(Field f : fa) {
				add(f);
			}
		}

		@Override
		public Field[] getArray(Maze m) {
			return list.toArray(new Field[0]);
		}
		
		public List<Field> getList() {
			return list;
		}
	}
}

